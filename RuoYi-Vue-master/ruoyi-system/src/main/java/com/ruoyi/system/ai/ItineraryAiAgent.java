package com.ruoyi.system.ai;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.BizChatMessage;
import com.ruoyi.system.domain.BizChatSession;
import com.ruoyi.system.domain.BizScenicSpot;
import com.ruoyi.system.domain.BizTravelPlan;
import com.ruoyi.system.domain.BizTravelPlanDay;
import com.ruoyi.system.domain.BizTravelPlanDaySpot;
import com.ruoyi.system.mapper.BizChatMessageMapper;
import com.ruoyi.system.mapper.BizChatSessionMapper;
import com.ruoyi.system.mapper.BizScenicSpotMapper;
import com.ruoyi.system.mapper.BizTravelPlanDayMapper;
import com.ruoyi.system.mapper.BizTravelPlanDaySpotMapper;
import com.ruoyi.system.mapper.BizTravelPlanMapper;

/**
 * 行程多轮对话调整AI代理（Agent）
 *
 * 执行流程（RAG + 多轮上下文）：
 * 1. 读取该行程当前的逐日安排（结构化JSON）；
 * 2. 读取该会话最近10条历史对话，让大模型理解上下文；
 * 3. 用用户本轮消息做Qdrant向量检索，召回Top5相关景点知识库；
 * 4. 把"原行程+历史对话+景点知识库+用户要求"一起交给百炼大模型，要求输出修改后的完整行程JSON；
 * 5. 校验模型输出（景点必须来自知识库或原行程，禁止编造），重写三张行程表；
 * 6. 返回给游客的回复文案（由调用方负责落对话消息）。
 *
 * 任意异常向上抛出，由 TourItineraryServiceImpl 捕获并降级为规则意图引擎。
 *
 * @author ruoyi
 * @date 2026-09-21
 */
@Component
public class ItineraryAiAgent
{
    private static final Logger log = LoggerFactory.getLogger(ItineraryAiAgent.class);

    /** 带入大模型上下文的最大历史对话轮次（user+assistant各算一条，共取最近10条） */
    private static final int MAX_HISTORY_MESSAGES = 10;

    @Autowired
    @Qualifier("travelChatClient")
    private ChatClient travelChatClient;

    @Autowired
    private ScenicVectorSearchService scenicVectorSearchService;

    @Autowired
    private BizTravelPlanMapper travelPlanMapper;
    @Autowired
    private BizTravelPlanDayMapper travelPlanDayMapper;
    @Autowired
    private BizTravelPlanDaySpotMapper travelPlanDaySpotMapper;
    @Autowired
    private BizScenicSpotMapper scenicSpotMapper;
    @Autowired
    private BizChatSessionMapper chatSessionMapper;
    @Autowired
    private BizChatMessageMapper chatMessageMapper;

    /**
     * 根据用户本轮对话要求，调用大模型修改行程并落库
     *
     * @param plan        行程主表实体（调用方已做归属校验）
     * @param userMessage 用户本轮消息
     * @return AI给游客的回复文案
     */
    public String modifyPlanByAi(BizTravelPlan plan, String userMessage)
    {
        long startTime = System.currentTimeMillis();
        Long planId = plan.getId();

        // ===== 1. 加载当前行程：每日安排 + 当日景点实体 =====
        BizTravelPlanDay dayQuery = new BizTravelPlanDay();
        dayQuery.setPlanId(planId);
        List<BizTravelPlanDay> days = travelPlanDayMapper.selectBizTravelPlanDayList(dayQuery)
                .stream().sorted(Comparator.comparing(BizTravelPlanDay::getDayNum))
                .collect(Collectors.toList());

        List<Long> dayIds = days.stream().map(BizTravelPlanDay::getId).collect(Collectors.toList());
        Map<Long, List<BizTravelPlanDaySpot>> linkGroup = new LinkedHashMap<>();
        Set<Long> usedSpotIds = new HashSet<>();
        for (BizTravelPlanDay day : days)
        {
            BizTravelPlanDaySpot linkQuery = new BizTravelPlanDaySpot();
            linkQuery.setPlanDayId(day.getId());
            List<BizTravelPlanDaySpot> links = travelPlanDaySpotMapper.selectBizTravelPlanDaySpotList(linkQuery)
                    .stream().sorted(Comparator.comparing(BizTravelPlanDaySpot::getSortNum))
                    .collect(Collectors.toList());
            linkGroup.put(day.getId(), links);
            links.forEach(l -> usedSpotIds.add(l.getSpotId()));
        }
        Map<Long, BizScenicSpot> usedSpotMap = new HashMap<>();
        if (!usedSpotIds.isEmpty())
        {
            for (BizScenicSpot spot : scenicSpotMapper.selectBizScenicSpotByIds(usedSpotIds.toArray(new Long[0])))
            {
                usedSpotMap.put(spot.getId(), spot);
            }
        }
        String currentPlanJson = buildCurrentPlanJson(plan, days, linkGroup, usedSpotMap);

        // ===== 2. 读取最近历史对话 =====
        String historyText = buildHistoryText(planId);

        // ===== 3. Qdrant向量召回相关景点知识库（Top5，异常内部降级SQL） =====
        String searchText = userMessage + " " + plan.getDestinationCity()
                + (StringUtils.isNotEmpty(plan.getScenicTypePrefer()) ? " " + plan.getScenicTypePrefer() : "");
        List<BizScenicSpot> recalled = scenicVectorSearchService.searchTopSpots(
                searchText, plan.getDestinationCity(), ScenicVectorSearchService.DEFAULT_TOP_K);
        String knowledgeText = scenicVectorSearchService.buildKnowledgeText(recalled);

        // 允许模型使用的景点白名单 = 向量召回景点 ∪ 原行程已用景点（保证保留原有安排）
        Map<String, BizScenicSpot> nameMap = new HashMap<>();
        for (BizScenicSpot spot : recalled)
        {
            nameMap.put(spot.getSpotName().trim(), spot);
        }
        for (BizScenicSpot spot : usedSpotMap.values())
        {
            nameMap.putIfAbsent(spot.getSpotName().trim(), spot);
        }
        if (nameMap.isEmpty())
        {
            throw new IllegalStateException("景点知识库为空，无法进行AI调整");
        }

        // ===== 4. 调用大模型输出修改后的完整行程JSON =====
        String prompt = buildChatPrompt(plan, userMessage, currentPlanJson, historyText, knowledgeText);
        String aiContent = travelChatClient.prompt().user(prompt).call().content();
        JSONObject root = JSON.parseObject(extractJson(aiContent));
        String reply = root.getString("reply");
        JSONArray dayArray = root.getJSONArray("days");
        if (StringUtils.isEmpty(reply) || dayArray == null || dayArray.isEmpty() || dayArray.size() > 30)
        {
            throw new IllegalStateException("大模型返回的对话调整结果结构不合法");
        }
        int newDays = dayArray.size();

        // ===== 5. 重写行程：先清空原有"每日行程+景点关联"，再按新JSON落库 =====
        for (Long dayId : dayIds)
        {
            BizTravelPlanDaySpot linkQuery = new BizTravelPlanDaySpot();
            linkQuery.setPlanDayId(dayId);
            for (BizTravelPlanDaySpot link : travelPlanDaySpotMapper.selectBizTravelPlanDaySpotList(linkQuery))
            {
                travelPlanDaySpotMapper.deleteBizTravelPlanDaySpotById(link.getId());
            }
            travelPlanDayMapper.deleteBizTravelPlanDayById(dayId);
        }

        int consumptionLevel = plan.getConsumptionLevel() == null ? 2 : plan.getConsumptionLevel();
        long peopleCount = plan.getPeopleCount() == null || plan.getPeopleCount() < 1 ? 1 : plan.getPeopleCount();
        // 预算口径为"人均"：首日到达含住宿、末日中午返程不住宿；多人同住房费按房间分摊
        // （详见 perCapitaDayBase），当日预算在按天循环中逐日计算

        long totalSpotCount = 0;
        BigDecimal totalCost = BigDecimal.ZERO;
        for (int d = 1; d <= newDays; d++)
        {
            JSONObject dayJson = dayArray.getJSONObject(d - 1);
            BigDecimal ticketSum = BigDecimal.ZERO;

            // 解析当日景点：白名单校验，每天最多2个，去重
            List<BizScenicSpot> todaySpots = new ArrayList<>();
            JSONArray spotArray = dayJson.getJSONArray("spots");
            if (spotArray != null)
            {
                for (int i = 0; i < spotArray.size() && todaySpots.size() < 2; i++)
                {
                    JSONObject spotJson = spotArray.getJSONObject(i);
                    String name = spotJson.getString("spotName");
                    if (StringUtils.isEmpty(name))
                    {
                        continue;
                    }
                    BizScenicSpot matched = nameMap.get(name.trim());
                    if (matched == null || todaySpots.contains(matched))
                    {
                        // 知识库外的编造景点直接丢弃
                        continue;
                    }
                    todaySpots.add(matched);
                    ticketSum = ticketSum.add(matched.getAdultPrice() == null ? BigDecimal.ZERO : matched.getAdultPrice());
                }
            }

            BizTravelPlanDay day = new BizTravelPlanDay();
            day.setPlanId(planId);
            day.setDayNum((long) d);
            day.setDayTheme(defaultIfBlank(dayJson.getString("dayTheme"),
                    plan.getDestinationCity() + "·第" + d + "天行程"));
            // 当日人均预算 = 当天人均基础（首日含住宿/末日不含）+ 当天门票
            day.setDailyBudget(perCapitaDayBase(consumptionLevel, (int) peopleCount, d, newDays).add(ticketSum));
            day.setHotelRecommend(defaultIfBlank(dayJson.getString("hotelRecommend"),
                    buildHotelRecommend(consumptionLevel)));
            day.setTrafficDesc(defaultIfBlank(dayJson.getString("trafficDesc"),
                    "市内以地铁/公交搭配网约车出行，景点间建议预留30分钟车程"));
            day.setDaySummary(defaultIfBlank(dayJson.getString("daySummary"),
                    buildSimpleDaySummary(plan.getDestinationCity(), d, todaySpots)));
            day.setAttention(defaultIfBlank(dayJson.getString("attention"),
                    buildAttention(plan.getPeopleType())));
            day.setCreateTime(DateUtils.getNowDate());
            travelPlanDayMapper.insertBizTravelPlanDay(day);

            for (int i = 0; i < todaySpots.size(); i++)
            {
                BizScenicSpot spot = todaySpots.get(i);
                BizTravelPlanDaySpot daySpot = new BizTravelPlanDaySpot();
                daySpot.setPlanDayId(day.getId());
                daySpot.setSpotId(spot.getId());
                daySpot.setSortNum((long) (i + 1));
                daySpot.setPlayDuration(spot.getPlayHours() == null ? new BigDecimal("2") : spot.getPlayHours());
                daySpot.setTicketCost(spot.getAdultPrice() == null ? BigDecimal.ZERO : spot.getAdultPrice());
                daySpot.setPlayTip(buildPlayTip(spot));
                daySpot.setCreateTime(DateUtils.getNowDate());
                travelPlanDaySpotMapper.insertBizTravelPlanDaySpot(daySpot);
            }

            totalSpotCount += todaySpots.size();
            totalCost = totalCost.add(perCapitaDayBase(consumptionLevel, (int) peopleCount, d, newDays)).add(ticketSum);
        }

        // ===== 6. 更新主表：天数/结束日期/标题/预算/摘要/模型标记 =====
        LocalDate startDate = plan.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        String modelTitle = root.getString("title");
        BizTravelPlan planUpdate = new BizTravelPlan();
        planUpdate.setId(planId);
        planUpdate.setTravelDays((long) newDays);
        planUpdate.setEndDate(toDate(startDate.plusDays(newDays - 1L)));
        planUpdate.setSpotCount(totalSpotCount);
        planUpdate.setTotalCost(totalCost);
        planUpdate.setTotalBudget(totalPerCapitaBase(consumptionLevel, (int) peopleCount, newDays));
        planUpdate.setPlanSummary(reply);
        if (StringUtils.isNotEmpty(modelTitle) && modelTitle.trim().length() <= 20)
        {
            planUpdate.setPlanTitle(modelTitle.trim());
        }
        else
        {
            planUpdate.setPlanTitle(plan.getDestinationCity() + newDays + "日"
                    + consumptionName(consumptionLevel) + "游");
        }
        planUpdate.setAiModel("qwen3.8-max + Qdrant向量知识库(对话调整)");
        planUpdate.setUpdateTime(DateUtils.getNowDate());
        travelPlanMapper.updateBizTravelPlan(planUpdate);

        log.info("[AI对话调整] planId={}，调整后天数={}，景点数={}，耗时{}ms",
                planId, newDays, totalSpotCount, System.currentTimeMillis() - startTime);
        return reply.trim();
    }

    /**
     * 组装当前行程JSON（提供给大模型作为"待修改原稿"）
     */
    private String buildCurrentPlanJson(BizTravelPlan plan, List<BizTravelPlanDay> days,
            Map<Long, List<BizTravelPlanDaySpot>> linkGroup, Map<Long, BizScenicSpot> spotMap)
    {
        JSONObject obj = new JSONObject(new LinkedHashMap<>());
        obj.put("destination", plan.getDestinationCity());
        obj.put("days", plan.getTravelDays());
        obj.put("budget", consumptionName(plan.getConsumptionLevel()));
        obj.put("crowd", plan.getPeopleType());
        JSONArray dayArr = new JSONArray();
        for (BizTravelPlanDay day : days)
        {
            JSONObject dj = new JSONObject(new LinkedHashMap<>());
            dj.put("dayNum", day.getDayNum());
            dj.put("dayTheme", day.getDayTheme());
            JSONArray spotArr = new JSONArray();
            for (BizTravelPlanDaySpot link : linkGroup.getOrDefault(day.getId(), new ArrayList<>()))
            {
                BizScenicSpot spot = spotMap.get(link.getSpotId());
                JSONObject sj = new JSONObject(new LinkedHashMap<>());
                sj.put("spotName", spot == null ? "景点#" + link.getSpotId() : spot.getSpotName());
                spotArr.add(sj);
            }
            dj.put("spots", spotArr);
            dj.put("daySummary", day.getDaySummary());
            dayArr.add(dj);
        }
        obj.put("dayList", dayArr);
        return obj.toJSONString();
    }

    /**
     * 查询本行程会话的最近历史对话，格式化为可读文本
     */
    private String buildHistoryText(Long planId)
    {
        BizChatSession sessionQuery = new BizChatSession();
        sessionQuery.setPlanId(planId);
        List<BizChatSession> sessions = chatSessionMapper.selectBizChatSessionList(sessionQuery);
        if (sessions.isEmpty())
        {
            return "（本轮为首次对话，暂无历史）";
        }
        BizChatMessage msgQuery = new BizChatMessage();
        msgQuery.setSessionId(sessions.get(0).getId());
        List<BizChatMessage> all = chatMessageMapper.selectBizChatMessageList(msgQuery)
                .stream().sorted(Comparator.comparing(BizChatMessage::getSendTime))
                .collect(Collectors.toList());
        // 只取最近若干条，避免prompt过长
        List<BizChatMessage> recent = all.size() > MAX_HISTORY_MESSAGES
                ? all.subList(all.size() - MAX_HISTORY_MESSAGES, all.size())
                : all;
        StringBuilder sb = new StringBuilder();
        for (BizChatMessage msg : recent)
        {
            sb.append("assistant".equals(msg.getMessageRole()) ? "助理：" : "游客：")
              .append(msg.getMessageContent()).append("\n");
        }
        return sb.length() == 0 ? "（暂无历史消息）" : sb.toString();
    }

    /**
     * 组装多轮对话Prompt：历史对话 + 当前行程 + 召回知识库 + 本轮要求
     */
    private String buildChatPrompt(BizTravelPlan plan, String userMessage,
            String currentPlanJson, String historyText, String knowledgeText)
    {
        return """
                游客正在对一份旅游行程进行多轮对话调整，请你结合全部上下文修改行程。

                【历史对话】
                %s

                【当前行程（修改前原稿）】
                %s

                【景点知识库】（只能使用这里出现的景点，以及当前行程中已有的景点，禁止编造其他景点）
                %s

                【游客本轮要求】
                %s

                【输出要求】
                只输出一个JSON对象，不要输出Markdown代码块，不要输出任何解释。结构如下：
                {
                  "reply": "给游客的回复，80字以内，用自然口语说明你做了哪些调整",
                  "title": "调整后的行程标题，12字以内；标题无需变化时可直接沿用",
                  "days": [
                    {
                      "dayTheme": "当日主题",
                      "daySummary": "当日安排概述，70字以内",
                      "trafficDesc": "交通建议，40字以内",
                      "hotelRecommend": "住宿建议，40字以内",
                      "attention": "注意事项，40字以内",
                      "spots": [
                        {"spotName":"必须与知识库或原行程中的景点名称完全一致"}
                      ]
                    }
                  ]
                }
                注意：
                1. days必须输出调整后完整行程的每一天（游客要求增减天数时，数组长度相应变化，范围1~30天），不要只输出变化的那一天；
                2. 游客没有要求调整的日期，尽量保持原有安排；
                3. 每天1-2个景点，同一天内景点不要重复；
                4. 目的地为%s，预算档次%s，出行人群%s。
                """.formatted(
                historyText,
                currentPlanJson,
                knowledgeText,
                userMessage,
                plan.getDestinationCity(),
                consumptionName(plan.getConsumptionLevel()),
                plan.getPeopleType());
    }

    /**
     * 从大模型输出中提取JSON（兼容```json围栏与多余解释文字）
     */
    private String extractJson(String content)
    {
        if (StringUtils.isEmpty(content))
        {
            throw new IllegalStateException("大模型返回内容为空");
        }
        String text = content.trim();
        if (text.startsWith("```"))
        {
            text = text.replaceAll("(?s)```(json)?", "").trim();
        }
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start < 0 || end <= start)
        {
            throw new IllegalStateException("大模型返回内容中未找到JSON结构");
        }
        return text.substring(start, end + 1);
    }

    // ============================== 文案/换算辅助 ==============================

    private String defaultIfBlank(String value, String defaultValue)
    {
        return StringUtils.isEmpty(value) ? defaultValue : value.trim();
    }

    /**
     * 计算某一天的人均基础花费（不含景点门票）
     *
     * 费用规则：
     * 1. 首日到达需住宿，末日中午返程、当晚不住宿（1天游视为当天往返不住宿）；
     * 2. 住宿按"房间"计费摊到人均：单人/情侣1间、家庭亲子1间家庭房、朋友4人2间；
     * 3. 餐饮与市内交通按人头，每天都发生（含末日去车站/机场）。
     *
     * @param level       消费档次 1=经济 2=舒适 3=高端
     * @param peopleCount 出行人数
     * @param dayNum      第几天（从1开始）
     * @param totalDays   总天数
     * @return 当天人均基础花费（元，四舍五入取整）
     */
    private BigDecimal perCapitaDayBase(int level, int peopleCount, int dayNum, int totalDays)
    {
        // 每人每天餐饮+市内交通：经济90 / 舒适150 / 高端300
        BigDecimal mealTraffic = level == 1 ? new BigDecimal("90")
                : level == 3 ? new BigDecimal("300") : new BigDecimal("150");
        // 末日（含1天游）中午返程，不计住宿费
        if (dayNum >= totalDays)
        {
            return mealTraffic;
        }
        // 每间夜房价：经济160 / 舒适300 / 高端600
        BigDecimal roomPrice = level == 1 ? new BigDecimal("160")
                : level == 3 ? new BigDecimal("600") : new BigDecimal("300");
        // 房间数：4人及以上按2人/间，其余1间
        int rooms = peopleCount >= 4 ? 2 : 1;
        // 人均房费 = 每间夜房价 × 房间数 ÷ 人数
        BigDecimal roomPerCapita = roomPrice.multiply(new BigDecimal(rooms))
                .divide(new BigDecimal(Math.max(peopleCount, 1)), 0, RoundingMode.HALF_UP);
        return mealTraffic.add(roomPerCapita);
    }

    /**
     * 全程人均基础花费合计（不含门票）= 各天人均基础之和，住宿晚数为 总天数-1
     */
    private BigDecimal totalPerCapitaBase(int level, int peopleCount, int totalDays)
    {
        BigDecimal total = BigDecimal.ZERO;
        for (int d = 1; d <= totalDays; d++)
        {
            total = total.add(perCapitaDayBase(level, peopleCount, d, totalDays));
        }
        return total;
    }

    private String consumptionName(Integer level)
    {
        if (level == null)
        {
            return "舒适";
        }
        return level == 1 ? "经济" : level == 3 ? "高端" : "舒适";
    }

    private Date toDate(LocalDate localDate)
    {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private String buildSimpleDaySummary(String destination, int dayNum, List<BizScenicSpot> spots)
    {
        StringBuilder sb = new StringBuilder("第").append(dayNum).append("天：");
        if (spots.isEmpty())
        {
            sb.append("抵达").append(destination).append("，自由活动，可探索市区风光与特色美食。");
        }
        else
        {
            for (int i = 0; i < spots.size(); i++)
            {
                if (i > 0)
                {
                    sb.append("；");
                }
                sb.append(i + 1).append("、").append(spots.get(i).getSpotName());
            }
            sb.append("。");
        }
        return sb.toString();
    }

    private String buildPlayTip(BizScenicSpot spot)
    {
        StringBuilder tip = new StringBuilder();
        tip.append("建议游玩")
           .append(spot.getPlayHours() == null ? "2" : spot.getPlayHours().stripTrailingZeros().toPlainString())
           .append("小时");
        if (StringUtils.isNotEmpty(spot.getOpenTimeDesc()))
        {
            tip.append("，开放时间：").append(spot.getOpenTimeDesc());
        }
        if (spot.getAdultPrice() != null && spot.getAdultPrice().compareTo(BigDecimal.ZERO) > 0)
        {
            tip.append("，成人票约").append(spot.getAdultPrice().stripTrailingZeros().toPlainString()).append("元");
        }
        else
        {
            tip.append("，免门票");
        }
        return tip.toString();
    }

    private String buildHotelRecommend(int level)
    {
        if (level == 1)
        {
            return "推荐选择连锁快捷酒店或青年旅舍，经济实惠，优先选近地铁/公交站点";
        }
        if (level == 3)
        {
            return "推荐五星酒店或高端度假酒店，关注景观房与配套服务，提前预订享优惠";
        }
        return "推荐市区四星标准酒店或高分民宿，交通便利、评价4.5分以上为佳";
    }

    private String buildAttention(String crowd)
    {
        if ("家庭亲子".equals(crowd))
        {
            return "带儿童出行注意防晒、补水，景区人多牵好孩子，随身备常用药品";
        }
        if ("情侣".equals(crowd))
        {
            return "可提前预订观景餐厅与网红打卡点门票，傍晚拍照光线最佳";
        }
        if ("朋友结伴".equals(crowd))
        {
            return "集体行动约定集合时间与地点，费用可AA，夜间出行注意安全";
        }
        return "单人出行注意保管证件财物，提前告知家人行程，保持手机畅通";
    }
}
