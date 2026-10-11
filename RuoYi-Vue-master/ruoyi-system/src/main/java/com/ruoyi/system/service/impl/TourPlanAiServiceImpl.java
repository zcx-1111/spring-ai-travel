package com.ruoyi.system.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.ai.ScenicVectorSearchService;
import com.ruoyi.system.domain.BizScenicSpot;
import com.ruoyi.system.domain.BizTravelPlan;
import com.ruoyi.system.domain.BizTravelPlanDay;
import com.ruoyi.system.domain.BizTravelPlanDaySpot;
import com.ruoyi.system.domain.dto.TourPlanGenerateDTO;
import com.ruoyi.system.mapper.BizScenicSpotMapper;
import com.ruoyi.system.mapper.BizTravelPlanDayMapper;
import com.ruoyi.system.mapper.BizTravelPlanDaySpotMapper;
import com.ruoyi.system.mapper.BizTravelPlanMapper;
import com.ruoyi.system.service.ITourPlanAiService;

/**
 * 前台AI行程生成 服务层实现
 *
 * 主链路（RAG）：Qdrant向量检索景点知识库 -> ChatClient调用阿里云百炼大模型生成结构化行程JSON
 *            -> 校验并落库 biz_travel_plan / biz_travel_plan_day / biz_travel_plan_day_spot 三张表。
 * 降级链路：百炼API不可用、Key无效、Qdrant异常、模型返回JSON无法解析等任意故障时，
 *          自动回退到"知识库召回+规则编排"算法，保证前端生成功能始终可用。
 *
 * @author ruoyi
 * @date 2026-09-21
 */
@Service
public class TourPlanAiServiceImpl implements ITourPlanAiService
{
    private static final Logger log = LoggerFactory.getLogger(TourPlanAiServiceImpl.class);

    @Autowired
    private BizTravelPlanMapper travelPlanMapper;

    @Autowired
    private BizTravelPlanDayMapper travelPlanDayMapper;

    @Autowired
    private BizTravelPlanDaySpotMapper travelPlanDaySpotMapper;

    @Autowired
    private BizScenicSpotMapper scenicSpotMapper;

    /** 阿里云百炼大模型客户端（由TravelChatClientConfig配置系统提示词） */
    @Autowired
    @Qualifier("travelChatClient")
    private ChatClient travelChatClient;

    /** Qdrant景点向量检索服务（内部含SQL降级） */
    @Autowired
    private ScenicVectorSearchService scenicVectorSearchService;

    /**
     * 生成行程方案（整个过程事务化，任一步失败全部回滚）
     * 优先走百炼大模型RAG链路，任意异常自动降级规则引擎
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long generatePlan(TourPlanGenerateDTO dto, Long userId)
    {
        try
        {
            return generateByAi(dto, userId);
        }
        catch (ServiceException se)
        {
            // 参数类业务异常直接抛出，不降级
            throw se;
        }
        catch (Exception e)
        {
            log.warn("[AI行程生成] 百炼大模型链路失败，降级为规则引擎生成。原因：{}", e.getMessage());
            return generateByRule(dto, userId);
        }
    }

    // ============================== 百炼大模型 RAG 生成链路 ==============================

    /**
     * 调用大模型生成结构化行程JSON并落库
     */
    private Long generateByAi(TourPlanGenerateDTO dto, Long userId)
    {
        long startTime = System.currentTimeMillis();

        // 1. 参数校验（与规则链路一致，后端兜底）
        validateDto(dto);
        int days = dto.getDays();
        LocalDate startDate = LocalDate.parse(dto.getStartDate());

        // 2. 预算/人群映射
        int consumptionLevel = "高端".equals(dto.getBudget()) ? 3 : ("经济型".equals(dto.getBudget()) ? 1 : 2);
        int peopleCount = "情侣".equals(dto.getCrowd()) ? 2
                : ("家庭亲子".equals(dto.getCrowd()) ? 3
                : ("朋友结伴".equals(dto.getCrowd()) ? 4 : 1));
        // 预算口径为"人均"：当日预算 = 当天人均基础花费 + 当天门票。
        // 首日到达需住宿、末日中午返程不住宿；多人同住一间房，房费按房间分摊（详见 perCapitaDayBase）

        // 3. RAG召回目的地景点知识库：
        //    语义通道（Qdrant向量，异常内部自动SQL降级）解决偏好类泛化需求；
        //    额外备注非空时叠加关键词通道（ES全文检索，异常内部自动MySQL like降级），
        //    保证用户用描述性语言点名的景点（如"供奉千里眼顺风耳的地方"→南沙天后宫）确定性能进入白名单。
        String preferenceText = dto.getPreferences() == null ? "" : String.join(" ", dto.getPreferences());
        String remark = dto.getRemark() == null ? "" : dto.getRemark().trim();
        String searchText = dto.getDestination() + " " + preferenceText + " " + dto.getCrowd()
                + (StringUtils.isNotEmpty(remark) ? " " + remark : "");
        List<BizScenicSpot> candidateSpots = StringUtils.isNotEmpty(remark)
                ? scenicVectorSearchService.searchTopSpotsHybrid(
                        searchText, remark, dto.getDestination(), ScenicVectorSearchService.DEFAULT_TOP_K)
                : scenicVectorSearchService.searchTopSpots(
                        searchText, dto.getDestination(), ScenicVectorSearchService.DEFAULT_TOP_K);
        if (candidateSpots.isEmpty())
        {
            // 知识库完全没有候选景点时交给规则链路（其内部有"未收录"提示文案）
            throw new IllegalStateException("景点知识库未召回到任何景点");
        }
        String knowledgeText = scenicVectorSearchService.buildKnowledgeText(candidateSpots);
        // 景点名称 -> 实体映射，用于把模型返回的景点名还原为spotId（防止模型编造景点）
        Map<String, BizScenicSpot> nameMap = new HashMap<>();
        for (BizScenicSpot spot : candidateSpots)
        {
            nameMap.put(spot.getSpotName().trim(), spot);
        }

        // 4. 组装Prompt并调用百炼大模型
        String prompt = buildGeneratePrompt(dto, days, knowledgeText);
        String aiContent = travelChatClient.prompt().user(prompt).call().content();
        JSONObject root = JSON.parseObject(extractJson(aiContent));
        JSONArray dayArray = root.getJSONArray("days");
        if (dayArray == null || dayArray.size() != days)
        {
            throw new IllegalStateException("大模型返回的行程天数与需求不一致");
        }

        // 5. 组装并保存行程主表
        LocalDate endLocalDate = startDate.plusDays(days - 1L);
        String title = StringUtils.isNotEmpty(root.getString("title"))
                ? root.getString("title").trim()
                : dto.getDestination() + days + "日" + consumptionName(consumptionLevel) + "游";
        BizTravelPlan plan = new BizTravelPlan();
        plan.setUserId(userId);
        plan.setDestinationCity(dto.getDestination());
        plan.setTravelDays((long) days);
        plan.setStartDate(toDate(startDate));
        plan.setEndDate(toDate(endLocalDate));
        plan.setPeopleCount((long) peopleCount);
        plan.setPeopleType(dto.getCrowd());
        plan.setConsumptionLevel(consumptionLevel);
        plan.setAccommodationPrefer(consumptionLevel);
        plan.setScenicTypePrefer(dto.getPreferences() == null ? "" : String.join(",", dto.getPreferences()));
        plan.setPlanTitle(title);
        plan.setPlanSource(1);
        plan.setPlanStatus(1);
        // 标记真实生成链路（区别于降级规则引擎）
        plan.setAiModel("qwen3.8-max + Qdrant向量知识库");
        plan.setTotalBudget(totalPerCapitaBase(consumptionLevel, peopleCount, days));
        plan.setPromptSnapshot(buildPromptSnapshot(dto));
        plan.setPlanSummary(StringUtils.isNotEmpty(root.getString("summary"))
                ? root.getString("summary").trim()
                : String.format("AI结合景点知识库，为你生成了%s%s天行程，可在对话中继续调整。", dto.getDestination(), days));
        plan.setCreateTime(DateUtils.getNowDate());
        travelPlanMapper.insertBizTravelPlan(plan);
        Long planId = plan.getId();

        // 6. 按天解析并保存每日行程与景点（只接受知识库中存在的景点名，模型编造的名称直接丢弃）
        long totalSpotCount = 0;
        BigDecimal totalCost = BigDecimal.ZERO;
        for (int d = 1; d <= days; d++)
        {
            JSONObject dayJson = dayArray.getJSONObject(d - 1);
            // 当日门票合计
            BigDecimal ticketSum = BigDecimal.ZERO;
            // 解析当日景点（每天最多2个，按知识库名校验）
            List<BizScenicSpot> todaySpots = new ArrayList<>();
            List<JSONObject> spotJsonList = new ArrayList<>();
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
                        // 模型编造了知识库外景点或重复景点：跳过（System提示词已要求禁止编造，此处二次兜底）
                        continue;
                    }
                    todaySpots.add(matched);
                    spotJsonList.add(spotJson);
                    ticketSum = ticketSum.add(matched.getAdultPrice() == null ? BigDecimal.ZERO : matched.getAdultPrice());
                }
            }

            BizTravelPlanDay day = new BizTravelPlanDay();
            day.setPlanId(planId);
            day.setDayNum((long) d);
            day.setDayTheme(defaultIfBlank(dayJson.getString("dayTheme"),
                    dto.getDestination() + "·第" + d + "天行程"));
            // 当日人均预算 = 当天人均基础（首日含住宿/末日不含）+ 当天门票
            day.setDailyBudget(perCapitaDayBase(consumptionLevel, peopleCount, d, days).add(ticketSum));
            day.setHotelRecommend(defaultIfBlank(dayJson.getString("hotelRecommend"),
                    buildHotelRecommend(consumptionLevel)));
            day.setTrafficDesc(defaultIfBlank(dayJson.getString("trafficDesc"),
                    "市内以地铁/公交搭配网约车出行，景点间建议预留30分钟车程"));
            day.setDaySummary(defaultIfBlank(dayJson.getString("daySummary"),
                    buildDaySummary(d, todaySpots, dto)));
            day.setAttention(defaultIfBlank(dayJson.getString("attention"),
                    buildAttention(dto.getCrowd())));
            day.setCreateTime(DateUtils.getNowDate());
            travelPlanDayMapper.insertBizTravelPlanDay(day);

            // 保存当日景点关联
            for (int i = 0; i < todaySpots.size(); i++)
            {
                BizScenicSpot spot = todaySpots.get(i);
                JSONObject spotJson = spotJsonList.get(i);
                BizTravelPlanDaySpot daySpot = new BizTravelPlanDaySpot();
                daySpot.setPlanDayId(day.getId());
                daySpot.setSpotId(spot.getId());
                daySpot.setSortNum((long) (i + 1));
                // 模型给了时长/票价则采用，否则用知识库默认值
                BigDecimal duration = spotJson.getBigDecimal("playDuration");
                daySpot.setPlayDuration(duration != null && duration.compareTo(BigDecimal.ZERO) > 0
                        ? duration
                        : (spot.getPlayHours() == null ? new BigDecimal("2") : spot.getPlayHours()));
                BigDecimal ticket = spotJson.getBigDecimal("ticketCost");
                daySpot.setTicketCost(ticket != null ? ticket
                        : (spot.getAdultPrice() == null ? BigDecimal.ZERO : spot.getAdultPrice()));
                daySpot.setPlayTip(defaultIfBlank(spotJson.getString("playTip"), buildPlayTip(spot)));
                daySpot.setCreateTime(DateUtils.getNowDate());
                travelPlanDaySpotMapper.insertBizTravelPlanDaySpot(daySpot);
            }

            totalSpotCount += todaySpots.size();
            totalCost = totalCost.add(perCapitaDayBase(consumptionLevel, peopleCount, d, days)).add(ticketSum);
        }

        // 7. 回写统计信息
        BizTravelPlan update = new BizTravelPlan();
        update.setId(planId);
        update.setSpotCount(totalSpotCount);
        update.setTotalCost(totalCost);
        update.setGenerationTime((System.currentTimeMillis() - startTime) / 1000);
        update.setUpdateTime(DateUtils.getNowDate());
        travelPlanMapper.updateBizTravelPlan(update);

        log.info("[AI行程生成] 百炼大模型生成成功，planId={}，景点数={}，耗时{}ms",
                planId, totalSpotCount, System.currentTimeMillis() - startTime);
        return planId;
    }

    /**
     * 组装行程生成Prompt（强约束JSON输出 + 只允许使用召回景点）
     */
    private String buildGeneratePrompt(TourPlanGenerateDTO dto, int days, String knowledgeText)
    {
        return """
                请根据以下【景点知识库】为游客生成一份旅游行程方案。

                【景点知识库】（只能使用这里出现的景点，禁止编造其他景点）
                %s

                【出行需求】
                目的地：%s
                开始日期：%s
                出行天数：%d天（必须输出恰好%d天的days数组）
                预算档次：%s
                出行人群：%s
                出行偏好：%s
                额外备注：%s

                【输出要求】
                只输出一个JSON对象，不要输出Markdown代码块，不要输出任何解释。结构如下：
                {
                  "title": "行程标题，12字以内",
                  "summary": "行程摘要，60字以内",
                  "days": [
                    {
                      "dayTheme": "当日主题，如：杭州·自然风光体验",
                      "daySummary": "当日安排概述，70字以内",
                      "trafficDesc": "当日交通建议，40字以内",
                      "hotelRecommend": "当日住宿建议，40字以内",
                      "attention": "当日注意事项，40字以内",
                      "spots": [
                        {"spotName":"必须与知识库景点名称完全一致","playDuration":2,"ticketCost":150,"playTip":"游玩建议，30字以内"}
                      ]
                    }
                  ]
                }
                每天安排1-2个景点；playDuration为数字（小时），ticketCost为数字（元，免票填0）；
                同一景点不要在同一天重复；安排顺序考虑地理位置顺路。
                %s
                """.formatted(
                knowledgeText,
                dto.getDestination(),
                dto.getStartDate(),
                days, days,
                dto.getBudget(),
                dto.getCrowd(),
                dto.getPreferences() == null || dto.getPreferences().isEmpty() ? "无" : String.join("、", dto.getPreferences()),
                StringUtils.isEmpty(dto.getRemark()) ? "无" : dto.getRemark(),
                // 备注非空时追加强约束：描述相符的景点必须优先安排，不得以「知识库无此景点」搪塞
                StringUtils.isEmpty(dto.getRemark()) ? ""
                        : "【备注优先规则】若【景点知识库】中存在与「额外备注」描述相符的景点（可能是小众景点），"
                        + "必须优先将其安排进行程，并在summary中回应用户的备注诉求；不得回复「知识库无该景点」。");
    }

    /**
     * 从大模型输出中提取JSON字符串（去除可能的```json代码块围栏与解释文字）
     */
    private String extractJson(String content)
    {
        if (StringUtils.isEmpty(content))
        {
            throw new IllegalStateException("大模型返回内容为空");
        }
        String text = content.trim();
        // 去除Markdown代码块围栏
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

    /**
     * 表单参数基础校验
     */
    private void validateDto(TourPlanGenerateDTO dto)
    {
        if (StringUtils.isEmpty(dto.getDestination()))
        {
            throw new ServiceException("目的地不能为空");
        }
        if (StringUtils.isEmpty(dto.getStartDate()))
        {
            throw new ServiceException("出行开始日期不能为空");
        }
        int days = dto.getDays() == null ? 0 : dto.getDays();
        if (days < 1 || days > 30)
        {
            throw new ServiceException("出行天数必须在1~30之间");
        }
        try
        {
            LocalDate.parse(dto.getStartDate());
        }
        catch (Exception e)
        {
            throw new ServiceException("开始日期格式不正确，应为 yyyy-MM-dd");
        }
    }

    /** 字符串为空时返回默认值 */
    private String defaultIfBlank(String value, String defaultValue)
    {
        return StringUtils.isEmpty(value) ? defaultValue : value.trim();
    }

    // ============================== 规则引擎降级链路 ==============================

    /**
     * 规则编排生成行程（大模型链路不可用时的降级实现）
     */
    private Long generateByRule(TourPlanGenerateDTO dto, Long userId)
    {
        long startTime = System.currentTimeMillis();

        // ===== 1. 参数基础校验（前端已校验，后端兜底） =====
        if (StringUtils.isEmpty(dto.getDestination()))
        {
            throw new ServiceException("目的地不能为空");
        }
        if (StringUtils.isEmpty(dto.getStartDate()))
        {
            throw new ServiceException("出行开始日期不能为空");
        }
        int days = dto.getDays() == null ? 0 : dto.getDays();
        if (days < 1 || days > 30)
        {
            throw new ServiceException("出行天数必须在1~30之间");
        }
        LocalDate startDate;
        try
        {
            startDate = LocalDate.parse(dto.getStartDate());
        }
        catch (Exception e)
        {
            throw new ServiceException("开始日期格式不正确，应为 yyyy-MM-dd");
        }

        // ===== 2. 预算/人群映射 =====
        // 消费等级：1=经济 2=舒适 3=高端
        int consumptionLevel = "高端".equals(dto.getBudget()) ? 3 : ("经济型".equals(dto.getBudget()) ? 1 : 2);
        // 出行人数估算
        int peopleCount = "情侣".equals(dto.getCrowd()) ? 2
                : ("家庭亲子".equals(dto.getCrowd()) ? 3
                : ("朋友结伴".equals(dto.getCrowd()) ? 4 : 1));
        // 预算口径为"人均"：首日到达含住宿、末日中午返程不住宿；多人同住房费按房间分摊
        // （详见 perCapitaDayBase），当日预算在按天循环中逐日计算

        // ===== 3. 从景点知识库召回目的地景点（RAG向量检索的当前替代实现） =====
        List<BizScenicSpot> spots = scenicSpotMapper.selectSpotsByDestination(dto.getDestination());
        // 使用目的地哈希做随机种子：同一目的地多次生成结果稳定，不同目的地有差异
        Collections.shuffle(spots, new java.util.Random(dto.getDestination().hashCode()));

        // ===== 4. 组装并保存行程主表 =====
        LocalDate endLocalDate = startDate.plusDays(days - 1L);
        BizTravelPlan plan = new BizTravelPlan();
        plan.setUserId(userId);
        plan.setDestinationCity(dto.getDestination());
        plan.setTravelDays((long) days);
        plan.setStartDate(toDate(startDate));
        plan.setEndDate(toDate(endLocalDate));
        plan.setPeopleCount((long) peopleCount);
        plan.setPeopleType(dto.getCrowd());
        plan.setConsumptionLevel(consumptionLevel);
        plan.setAccommodationPrefer(consumptionLevel);
        // 偏好多选以逗号拼接存储
        plan.setScenicTypePrefer(dto.getPreferences() == null ? "" : String.join(",", dto.getPreferences()));
        plan.setPlanTitle(dto.getDestination() + days + "日" + consumptionName(consumptionLevel) + "游");
        plan.setPlanSource(1);
        plan.setPlanStatus(1);
        plan.setAiModel("知识库规则引擎V1");
        plan.setTotalBudget(totalPerCapitaBase(consumptionLevel, peopleCount, days));
        plan.setPromptSnapshot(buildPromptSnapshot(dto));
        plan.setCreateTime(DateUtils.getNowDate());
        travelPlanMapper.insertBizTravelPlan(plan);
        Long planId = plan.getId();

        // ===== 5. 按天分配景点（顺序均分，不重复安排同一景点） =====
        List<String> themes = buildDayThemes(dto.getPreferences());
        int spotTotal = spots.size();
        int baseCount = spotTotal / days;
        int remainder = spotTotal % days;
        int spotIndex = 0;
        long totalSpotCount = 0;
        BigDecimal totalCost = BigDecimal.ZERO;

        for (int d = 1; d <= days; d++)
        {
            // 前 remainder 天多分一个景点保证均匀；同时每天最多安排2个主要景点（避免太赶），
            // 未安排的景点保留在知识库中，供多轮对话阶段"换景点/加景点"动态使用
            int planned = baseCount + (d <= remainder ? 1 : 0);
            int todayCount = Math.min(Math.min(planned, 2), spotTotal - spotIndex);
            List<BizScenicSpot> todaySpots = new ArrayList<>();
            for (int i = 0; i < todayCount; i++)
            {
                todaySpots.add(spots.get(spotIndex++));
            }

            // 当日门票合计
            BigDecimal ticketSum = BigDecimal.ZERO;
            for (BizScenicSpot spot : todaySpots)
            {
                if (spot.getAdultPrice() != null)
                {
                    ticketSum = ticketSum.add(spot.getAdultPrice());
                }
            }

            // 组装当日行程概述
            String daySummary = buildDaySummary(d, todaySpots, dto);
            BizTravelPlanDay day = new BizTravelPlanDay();
            day.setPlanId(planId);
            day.setDayNum((long) d);
            day.setDayTheme(dto.getDestination() + "·" + themes.get((d - 1) % themes.size()));
            // 当日人均预算 = 当天人均基础（首日含住宿/末日不含）+ 当天门票
            day.setDailyBudget(perCapitaDayBase(consumptionLevel, peopleCount, d, days).add(ticketSum));
            day.setHotelRecommend(buildHotelRecommend(consumptionLevel));
            day.setTrafficDesc("市内以地铁/公交搭配网约车出行，景点间建议预留30分钟车程");
            day.setDaySummary(daySummary);
            day.setAttention(buildAttention(dto.getCrowd()));
            day.setCreateTime(DateUtils.getNowDate());
            travelPlanDayMapper.insertBizTravelPlanDay(day);
            Long dayId = day.getId();

            // 保存当日景点关联（含游玩顺序、时长、门票、建议）
            for (int i = 0; i < todaySpots.size(); i++)
            {
                BizScenicSpot spot = todaySpots.get(i);
                BizTravelPlanDaySpot daySpot = new BizTravelPlanDaySpot();
                daySpot.setPlanDayId(dayId);
                daySpot.setSpotId(spot.getId());
                daySpot.setSortNum((long) (i + 1));
                daySpot.setPlayDuration(spot.getPlayHours() == null ? new BigDecimal("2") : spot.getPlayHours());
                daySpot.setTicketCost(spot.getAdultPrice() == null ? BigDecimal.ZERO : spot.getAdultPrice());
                daySpot.setPlayTip(buildPlayTip(spot));
                daySpot.setCreateTime(DateUtils.getNowDate());
                travelPlanDaySpotMapper.insertBizTravelPlanDaySpot(daySpot);
            }

            totalSpotCount += todaySpots.size();
            totalCost = totalCost.add(perCapitaDayBase(consumptionLevel, peopleCount, d, days)).add(ticketSum);
        }

        // ===== 6. 回写行程主表统计信息 =====
        BizTravelPlan update = new BizTravelPlan();
        update.setId(planId);
        update.setSpotCount(totalSpotCount);
        update.setTotalCost(totalCost);
        update.setGenerationTime((System.currentTimeMillis() - startTime) / 1000);
        update.setPlanSummary(spots.isEmpty()
                ? String.format("AI已为你生成%s%s天的行程框架。当前景点知识库暂未收录%s的景点数据，详细景点将在多轮对话中补充完善。",
                        dto.getDestination(), days, dto.getDestination())
                : String.format("AI结合景点知识库，为你安排了%s的%s天行程，共精选%d个景点，预算%s。可在对话中继续调整。",
                        dto.getDestination(), days, totalSpotCount, dto.getBudget()));
        update.setUpdateTime(DateUtils.getNowDate());
        travelPlanMapper.updateBizTravelPlan(update);

        return planId;
    }

    /**
     * LocalDate 转 Date（系统默认时区）
     */
    private Date toDate(LocalDate localDate)
    {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 计算某一天的人均基础花费（不含景点门票）
     *
     * 费用规则（贴近实际出行）：
     * 1. 首日到达需要住宿，最后一天中午返程、当晚不住宿，故末日只计餐饮+市内交通；
     *    总住宿晚数 = 总天数 - 1（1天游视为当天往返、不住宿）；
     * 2. 住宿按"房间"计费再摊到人均：单人/情侣住1间，家庭亲子按1间家庭房，
     *    朋友4人住2间（每间2人），避免把房费按人头重复收取；
     * 3. 餐饮、市内交通按人头计，每天都发生（含末日前往车站/机场）。
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
        // 房间数：4人及以上按2人/间（朋友4人=2间），其余1间（家庭3人按1间家庭房）
        int rooms = peopleCount >= 4 ? 2 : 1;
        // 人均房费 = 每间夜房价 × 房间数 ÷ 人数
        BigDecimal roomPerCapita = roomPrice.multiply(new BigDecimal(rooms))
                .divide(new BigDecimal(Math.max(peopleCount, 1)), 0, RoundingMode.HALF_UP);
        return mealTraffic.add(roomPerCapita);
    }

    /**
     * 全程人均基础花费合计（不含景点门票）= 各天人均基础之和
     * 住宿晚数为 总天数-1（首日住、末日走）
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

    /**
     * 消费等级中文名
     */
    private String consumptionName(int level)
    {
        return level == 1 ? "经济" : level == 3 ? "高端" : "舒适";
    }

    /**
     * 根据偏好生成每日主题池（无偏好时使用通用主题）
     */
    private List<String> buildDayThemes(List<String> preferences)
    {
        if (preferences != null && !preferences.isEmpty())
        {
            List<String> themes = new ArrayList<>();
            for (String pref : preferences)
            {
                themes.add(pref + "体验");
            }
            return themes;
        }
        return new ArrayList<>(Arrays.asList("经典打卡", "城市漫游", "美食休闲", "人文探索"));
    }

    /**
     * 当日行程概述文案
     */
    private String buildDaySummary(int dayNum, List<BizScenicSpot> todaySpots, TourPlanGenerateDTO dto)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("第").append(dayNum).append("天：");
        if (todaySpots.isEmpty())
        {
            sb.append("抵达").append(dto.getDestination())
              .append("，适应当地节奏，可自由探索市区风光与特色美食。");
        }
        else
        {
            for (int i = 0; i < todaySpots.size(); i++)
            {
                BizScenicSpot spot = todaySpots.get(i);
                if (i > 0)
                {
                    sb.append("；");
                }
                sb.append(i + 1).append("、").append(spot.getSpotName());
                if (StringUtils.isNotEmpty(spot.getSpotLevel()))
                {
                    sb.append("（").append(spot.getSpotLevel()).append("）");
                }
            }
            sb.append("。");
        }
        if (StringUtils.isNotEmpty(dto.getRemark()))
        {
            sb.append("已兼顾你的特别要求：").append(dto.getRemark()).append("。");
        }
        return sb.toString();
    }

    /**
     * 单个景点的游玩建议
     */
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

    /**
     * 按消费等级给出住宿建议
     */
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

    /**
     * 按出行人群给出注意事项
     */
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

    /**
     * 保存生成时的提示词快照（便于后续追溯AI生成依据）
     */
    private String buildPromptSnapshot(TourPlanGenerateDTO dto)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("目的地:").append(dto.getDestination())
          .append("; 开始日期:").append(dto.getStartDate())
          .append("; 天数:").append(dto.getDays())
          .append("; 预算:").append(dto.getBudget())
          .append("; 人群:").append(dto.getCrowd())
          .append("; 偏好:").append(dto.getPreferences() == null ? "" : String.join("/", dto.getPreferences()))
          .append("; 备注:").append(dto.getRemark() == null ? "" : dto.getRemark());
        return sb.toString();
    }
}
