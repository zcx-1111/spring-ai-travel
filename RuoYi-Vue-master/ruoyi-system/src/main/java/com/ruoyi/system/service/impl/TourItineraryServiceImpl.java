package com.ruoyi.system.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.ai.ItineraryAiAgent;
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
import com.ruoyi.system.service.ITourItineraryService;

/**
 * 前台行程详情与多轮对话 服务层实现
 *
 * 说明：当前对话能力采用"意图规则解析 + 景点知识库召回"实现行程调整，
 * 不依赖外部大模型即可完整演示多轮修改（改天数/预算/换景点/加项目/美食偏好）。
 * 后续接入 SpringAI + Qdrant 时，将 parseIntent 替换为大模型理解与生成即可，
 * 行程落库结构与对话记录结构保持不变。
 *
 * @author ruoyi
 * @date 2026-09-21
 */
@Service
public class TourItineraryServiceImpl implements ITourItineraryService
{
    private static final Logger log = LoggerFactory.getLogger(TourItineraryServiceImpl.class);

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
    /** 百炼大模型多轮对话调整代理（RAG + 历史对话上下文），不可用时降级下方规则引擎 */
    @Autowired
    private ItineraryAiAgent itineraryAiAgent;

    /** 消费等级 -> 人均每日基础花费 */
    private static final Map<Integer, BigDecimal> PER_CAPITA_DAILY = new HashMap<>();
    /** 从提示词快照中提取备注内容的正则（备注是快照最后一段） */
    private static final Pattern REMARK_PATTERN = Pattern.compile("备注:(.*)$");
    static
    {
        PER_CAPITA_DAILY.put(1, new BigDecimal("400"));
        PER_CAPITA_DAILY.put(2, new BigDecimal("800"));
        PER_CAPITA_DAILY.put(3, new BigDecimal("1500"));
    }

    // ============================== 行程列表与删除 ==============================

    /**
     * 分页查询当前游客的行程方案（Controller已通过PageHelper拦截SQL实现物理分页）
     * 注意：必须返回PageHelper的Page对象，Controller的getDataTable才能从中读取total总数，
     *       因此不能用stream collect成普通ArrayList
     */
    @Override
    public List<Map<String, Object>> listMyPlans(Long userId)
    {
        BizTravelPlan query = new BizTravelPlan();
        query.setUserId(userId);
        // 排序由Controller的startPage(orderBy)在SQL层完成（create_time desc）
        List<BizTravelPlan> plans = travelPlanMapper.selectBizTravelPlanList(query);
        long total = new PageInfo<>(plans).getTotal();

        // 用Page承载转换后的卡片数据并保留total，供getDataTable分页
        Page<Map<String, Object>> result = new Page<>();
        result.setTotal(total);
        for (BizTravelPlan plan : plans)
        {
            Map<String, Object> card = new LinkedHashMap<>();
            card.put("itineraryId", plan.getId());
            card.put("title", plan.getPlanTitle());
            card.put("destination", plan.getDestinationCity());
            card.put("days", plan.getTravelDays());
            // 消费等级转预算中文名
            card.put("budget", consumptionName(plan.getConsumptionLevel()));
            card.put("crowd", plan.getPeopleType());
            card.put("startDate", plan.getStartDate());
            card.put("spotCount", plan.getSpotCount());
            card.put("totalCost", plan.getTotalCost());
            card.put("createTime", plan.getCreateTime());
            // 方案来源：1=初次AI生成 2=对话调整过
            card.put("planSource", plan.getPlanSource());
            result.add(card);
        }
        return result;
    }

    /**
     * 删除行程：级联物理删除 行程景点关联 -> 每日行程 -> 行程主表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int removePlan(Long itineraryId, Long userId)
    {
        // 先做存在性与归属校验，防止越权删除他人行程
        BizTravelPlan plan = loadAndCheckPlan(itineraryId, userId);

        // 1. 查询所有天，逐天删除其景点关联
        BizTravelPlanDay dayQuery = new BizTravelPlanDay();
        dayQuery.setPlanId(plan.getId());
        List<BizTravelPlanDay> days = travelPlanDayMapper.selectBizTravelPlanDayList(dayQuery);
        for (BizTravelPlanDay day : days)
        {
            BizTravelPlanDaySpot linkQuery = new BizTravelPlanDaySpot();
            linkQuery.setPlanDayId(day.getId());
            List<BizTravelPlanDaySpot> links = travelPlanDaySpotMapper.selectBizTravelPlanDaySpotList(linkQuery);
            for (BizTravelPlanDaySpot link : links)
            {
                travelPlanDaySpotMapper.deleteBizTravelPlanDaySpotById(link.getId());
            }
            // 2. 删除当日行程
            travelPlanDayMapper.deleteBizTravelPlanDayById(day.getId());
        }

        // 3. 删除对话会话与消息（可选清理，避免残留孤儿对话数据）
        BizChatSession sessionQuery = new BizChatSession();
        sessionQuery.setPlanId(plan.getId());
        for (BizChatSession session : chatSessionMapper.selectBizChatSessionList(sessionQuery))
        {
            BizChatMessage msgQuery = new BizChatMessage();
            msgQuery.setSessionId(session.getId());
            for (BizChatMessage msg : chatMessageMapper.selectBizChatMessageList(msgQuery))
            {
                chatMessageMapper.deleteBizChatMessageById(msg.getId());
            }
            chatSessionMapper.deleteBizChatSessionById(session.getId());
        }

        // 4. 删除行程主表
        return travelPlanMapper.deleteBizTravelPlanById(plan.getId());
    }

    // ============================== 行程详情聚合 ==============================

    /**
     * 加载行程完整详情
     */
    @Override
    public Map<String, Object> getItinerary(Long itineraryId, Long userId)
    {
        BizTravelPlan plan = loadAndCheckPlan(itineraryId, userId);
        return buildItineraryPayload(plan);
    }

    /**
     * 组装返回给前端的完整行程数据
     */
    private Map<String, Object> buildItineraryPayload(BizTravelPlan plan)
    {
        // 1. 查询每日行程（按天序号排序）
        BizTravelPlanDay dayQuery = new BizTravelPlanDay();
        dayQuery.setPlanId(plan.getId());
        List<BizTravelPlanDay> days = travelPlanDayMapper.selectBizTravelPlanDayList(dayQuery)
                .stream()
                .sorted(Comparator.comparing(BizTravelPlanDay::getDayNum))
                .collect(Collectors.toList());

        // 2. 收集所有景点关联，批量查询景点信息（避免N+1查询）
        List<BizTravelPlanDaySpot> allLinks = new ArrayList<>();
        Map<Long, List<BizTravelPlanDaySpot>> linkGroup = new LinkedHashMap<>();
        for (BizTravelPlanDay day : days)
        {
            BizTravelPlanDaySpot linkQuery = new BizTravelPlanDaySpot();
            linkQuery.setPlanDayId(day.getId());
            List<BizTravelPlanDaySpot> links = travelPlanDaySpotMapper.selectBizTravelPlanDaySpotList(linkQuery)
                    .stream()
                    .sorted(Comparator.comparing(BizTravelPlanDaySpot::getSortNum))
                    .collect(Collectors.toList());
            linkGroup.put(day.getId(), links);
            allLinks.addAll(links);
        }
        Map<Long, BizScenicSpot> spotMap = new HashMap<>();
        if (!allLinks.isEmpty())
        {
            Long[] spotIds = allLinks.stream().map(BizTravelPlanDaySpot::getSpotId).distinct().toArray(Long[]::new);
            for (BizScenicSpot spot : scenicSpotMapper.selectBizScenicSpotByIds(spotIds))
            {
                spotMap.put(spot.getId(), spot);
            }
        }

        // 3. 组装每日行程（含景点明细）
        List<Map<String, Object>> dayList = new ArrayList<>();
        for (BizTravelPlanDay day : days)
        {
            Map<String, Object> dayMap = new LinkedHashMap<>();
            dayMap.put("dayNum", day.getDayNum());
            dayMap.put("dayTheme", day.getDayTheme());
            dayMap.put("dailyBudget", day.getDailyBudget());
            dayMap.put("hotelRecommend", day.getHotelRecommend());
            dayMap.put("trafficDesc", day.getTrafficDesc());
            dayMap.put("daySummary", day.getDaySummary());
            dayMap.put("attention", day.getAttention());

            List<Map<String, Object>> spotVoList = new ArrayList<>();
            for (BizTravelPlanDaySpot link : linkGroup.get(day.getId()))
            {
                BizScenicSpot spot = spotMap.get(link.getSpotId());
                Map<String, Object> spotVo = new LinkedHashMap<>();
                spotVo.put("sortNum", link.getSortNum());
                spotVo.put("spotId", link.getSpotId());
                spotVo.put("spotName", spot == null ? "景点#" + link.getSpotId() : spot.getSpotName());
                spotVo.put("spotLevel", spot == null ? null : spot.getSpotLevel());
                spotVo.put("playDuration", link.getPlayDuration());
                spotVo.put("ticketCost", link.getTicketCost());
                spotVo.put("playTip", link.getPlayTip());
                spotVoList.add(spotVo);
            }
            dayMap.put("spots", spotVoList);
            dayList.add(dayMap);
        }

        // 4. 组装行程基础信息
        Map<String, Object> itinerary = new LinkedHashMap<>();
        itinerary.put("itineraryId", plan.getId());
        itinerary.put("title", plan.getPlanTitle());
        itinerary.put("destination", plan.getDestinationCity());
        itinerary.put("days", plan.getTravelDays());
        itinerary.put("startDate", plan.getStartDate());
        itinerary.put("endDate", plan.getEndDate());
        itinerary.put("budget", consumptionName(plan.getConsumptionLevel()));
        itinerary.put("crowd", plan.getPeopleType());
        itinerary.put("preferences", StringUtils.isEmpty(plan.getScenicTypePrefer())
                ? Collections.emptyList()
                : Arrays.asList(plan.getScenicTypePrefer().split(",")));
        itinerary.put("summary", plan.getPlanSummary());
        itinerary.put("totalCost", plan.getTotalCost());
        itinerary.put("spotCount", plan.getSpotCount());
        // 从生成时的提示词快照中解析备注，供"复用行程"回填表单
        itinerary.put("remark", parseRemarkFromSnapshot(plan.getPromptSnapshot()));

        // 5. 查询历史对话记录
        List<Map<String, Object>> messages = new ArrayList<>();
        BizChatSession sessionQuery = new BizChatSession();
        sessionQuery.setPlanId(plan.getId());
        List<BizChatSession> sessions = chatSessionMapper.selectBizChatSessionList(sessionQuery);
        if (!sessions.isEmpty())
        {
            BizChatMessage msgQuery = new BizChatMessage();
            msgQuery.setSessionId(sessions.get(0).getId());
            List<BizChatMessage> msgList = chatMessageMapper.selectBizChatMessageList(msgQuery)
                    .stream()
                    .sorted(Comparator.comparing(BizChatMessage::getSendTime))
                    .collect(Collectors.toList());
            for (BizChatMessage msg : msgList)
            {
                Map<String, Object> m = new LinkedHashMap<>();
                // 数据库存 user/assistant，前端据此区分气泡左右
                m.put("role", msg.getMessageRole());
                m.put("content", msg.getMessageContent());
                m.put("sendTime", msg.getSendTime());
                messages.add(m);
            }
        }

        // 6. 组装最终返回结构
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("itinerary", itinerary);
        payload.put("dayList", dayList);
        payload.put("messages", messages);
        return payload;
    }

    // ============================== 多轮对话 ==============================

    /**
     * 多轮对话修改行程
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> chat(Long itineraryId, Long userId, String userMessage)
    {
        if (StringUtils.isEmpty(userMessage) || StringUtils.isEmpty(userMessage.trim()))
        {
            throw new ServiceException("发送内容不能为空");
        }
        String content = userMessage.trim();
        BizTravelPlan plan = loadAndCheckPlan(itineraryId, userId);

        // 1. 行程调整：优先百炼大模型（历史对话+Qdrant知识库+原行程整体重写），
        //    百炼API/Key/Qdrant/JSON解析等任意异常时，降级为规则意图引擎，保证对话始终可用
        String aiReply;
        try
        {
            aiReply = itineraryAiAgent.modifyPlanByAi(plan, content);
        }
        catch (Exception e)
        {
            log.warn("[AI对话调整] 大模型链路失败，降级规则意图引擎。原因：{}", e.getMessage());
            aiReply = applyUserIntent(plan, content);
        }

        // 2. 获取或创建对话会话（一个行程对应一个会话）
        BizChatSession sessionQuery = new BizChatSession();
        sessionQuery.setPlanId(plan.getId());
        List<BizChatSession> sessions = chatSessionMapper.selectBizChatSessionList(sessionQuery);
        BizChatSession session;
        if (sessions.isEmpty())
        {
            session = new BizChatSession();
            session.setUserId(userId);
            session.setPlanId(plan.getId());
            session.setSessionTitle(plan.getDestinationCity() + "行程调整对话");
            session.setSessionStatus(0);
            session.setLastMessageTime(DateUtils.getNowDate());
            session.setCreateTime(DateUtils.getNowDate());
            chatSessionMapper.insertBizChatSession(session);
        }
        else
        {
            session = sessions.get(0);
        }

        // 3. 保存用户消息与AI回复消息
        saveChatMessage(session.getId(), userId, "user", content);
        saveChatMessage(session.getId(), userId, "assistant", aiReply);

        // 4. 刷新会话最后消息时间
        BizChatSession sessionUpdate = new BizChatSession();
        sessionUpdate.setId(session.getId());
        sessionUpdate.setLastMessageTime(DateUtils.getNowDate());
        sessionUpdate.setUpdateTime(DateUtils.getNowDate());
        chatSessionMapper.updateBizChatSession(sessionUpdate);

        // 5. 标记方案来源为"对话调整后"
        BizTravelPlan planUpdate = new BizTravelPlan();
        planUpdate.setId(plan.getId());
        planUpdate.setPlanSource(2);
        planUpdate.setUpdateTime(DateUtils.getNowDate());
        travelPlanMapper.updateBizTravelPlan(planUpdate);

        // 6. 重新查询最新行程并返回（含本轮新增的两条消息）
        BizTravelPlan latest = travelPlanMapper.selectBizTravelPlanById(plan.getId());
        Map<String, Object> payload = buildItineraryPayload(latest);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("reply", aiReply);
        result.put("itinerary", payload.get("itinerary"));
        result.put("dayList", payload.get("dayList"));
        result.put("messages", payload.get("messages"));
        return result;
    }

    /**
     * 保存一条对话消息
     */
    private void saveChatMessage(Long sessionId, Long userId, String role, String content)
    {
        BizChatMessage message = new BizChatMessage();
        message.setSessionId(sessionId);
        message.setUserId(userId);
        message.setMessageRole(role);
        // 消息类型：1=普通文本
        message.setMessageType(1);
        message.setMessageContent(content);
        message.setSendTime(DateUtils.getNowDate());
        message.setTokenUsage(0L);
        message.setCreateTime(DateUtils.getNowDate());
        chatMessageMapper.insertBizChatMessage(message);
    }

    /**
     * 意图规则引擎：根据用户文本判断调整类型并执行
     * 返回AI回复文案
     */
    private String applyUserIntent(BizTravelPlan plan, String content)
    {
        long oldDays = plan.getTravelDays() == null ? 1 : plan.getTravelDays();

        // ===== 意图1：调整天数（增加/减少 N 天） =====
        Long targetDays = parseTargetDays(content, (int) oldDays);
        boolean wantAdd = content.contains("增加") || content.contains("多加") || content.matches(".*加\\s*\\d*\\s*天.*") || content.contains("延长");
        boolean wantReduce = content.contains("减少") || content.matches(".*减\\s*\\d*\\s*天.*") || content.contains("缩短");
        if (targetDays != null && targetDays > oldDays)
        {
            return adjustDays(plan, targetDays.intValue(), true);
        }
        if (targetDays != null && targetDays < oldDays)
        {
            return adjustDays(plan, targetDays.intValue(), false);
        }
        if (wantAdd && content.contains("天"))
        {
            return adjustDays(plan, (int) Math.min(oldDays + 1, 30), true);
        }
        if (wantReduce && content.contains("天"))
        {
            return adjustDays(plan, (int) Math.max(oldDays - 1, 1), false);
        }

        // ===== 意图2：调整预算/消费档次 =====
        if (content.contains("预算") || content.contains("经济") || content.contains("省钱")
                || content.contains("舒适") || content.contains("中等") || content.contains("高端")
                || content.contains("豪华") || content.contains("奢华"))
        {
            int level = plan.getConsumptionLevel() == null ? 2 : plan.getConsumptionLevel();
            if (content.contains("经济") || content.contains("省钱"))
            {
                level = 1;
            }
            else if (content.contains("高端") || content.contains("豪华") || content.contains("奢华"))
            {
                level = 3;
            }
            else if (content.contains("舒适") || content.contains("中等"))
            {
                level = 2;
            }
            return adjustBudget(plan, level);
        }

        // ===== 意图3：更换景点 =====
        if (content.contains("换景点") || content.contains("换一个") || content.contains("换个") || content.contains("不想去"))
        {
            return swapSpot(plan);
        }

        // ===== 意图4：增加景点/多安排项目 =====
        if (content.contains("加景点") || content.contains("增加景点") || content.contains("多安排")
                || content.contains("加点") || content.contains("再去"))
        {
            return addSpot(plan);
        }

        // ===== 意图5：美食需求 =====
        if (content.contains("美食") || content.contains("小吃") || content.contains("好吃"))
        {
            return addFoodTheme(plan);
        }

        // ===== 默认回复：未识别到明确调整意图 =====
        return String.format("收到你的需求～目前我支持这样调整行程：①「增加/减少N天」修改天数；②「经济/舒适/高端」调整预算档次；"
                + "③「换一个景点」替换安排；④「多安排景点」增加游玩项目；⑤告诉我「想多吃当地美食」补充美食安排。"
                + "当前为你规划的是%s%s天行程，你想怎么调整呢？", plan.getDestinationCity(), oldDays);
    }

    /**
     * 从文本中解析目标天数，如"改成5天"、"增加到7天"
     */
    private Long parseTargetDays(String content, int oldDays)
    {
        Pattern p = Pattern.compile("(\\d+)\\s*天");
        Matcher m = p.matcher(content);
        if (m.find())
        {
            int days = Integer.parseInt(m.group(1));
            if (days >= 1 && days <= 30)
            {
                return (long) days;
            }
        }
        // 中文数字兜底：一/两/二/三/四/五/六/七
        Map<String, Integer> cn = new HashMap<>();
        cn.put("一天", 1); cn.put("两天", 2); cn.put("二天", 2); cn.put("三天", 3);
        cn.put("四天", 4); cn.put("五天", 5); cn.put("六天", 6); cn.put("七天", 7);
        for (Map.Entry<String, Integer> e : cn.entrySet())
        {
            if (content.contains(e.getKey()))
            {
                return e.getValue().longValue();
            }
        }
        return null;
    }

    /**
     * 调整行程天数：增加则追加新的一天（并尽量补入未安排景点），减少则删除最后几天
     */
    private String adjustDays(BizTravelPlan plan, int newDays, boolean increase)
    {
        int oldDays = plan.getTravelDays().intValue();
        LocalDate startDate = plan.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        if (increase)
        {
            // 召回目的地景点，排除行程中已安排的
            List<BizScenicSpot> candidates = recallUnusedSpots(plan);
            int candidateIdx = 0;
            List<String> themes = Arrays.asList("经典打卡", "城市漫游", "美食休闲", "人文探索", "自然风光体验");
            for (int d = oldDays + 1; d <= newDays; d++)
            {
                BizTravelPlanDay day = new BizTravelPlanDay();
                day.setPlanId(plan.getId());
                day.setDayNum((long) d);
                day.setDayTheme(plan.getDestinationCity() + "·" + themes.get((d - 1) % themes.size()));
                day.setDailyBudget(estimateDailyBudget(plan));
                day.setHotelRecommend(buildHotelRecommend(plan.getConsumptionLevel()));
                day.setTrafficDesc("市内以地铁/公交搭配网约车出行，景点间建议预留30分钟车程");
                day.setAttention(buildAttention(plan.getPeopleType()));
                day.setCreateTime(DateUtils.getNowDate());
                travelPlanDayMapper.insertBizTravelPlanDay(day);

                // 新增的每一天补入1个候选景点（若还有未安排的）
                if (candidateIdx < candidates.size())
                {
                    BizScenicSpot spot = candidates.get(candidateIdx++);
                    insertDaySpot(day.getId(), spot, 1);
                    day.setDaySummary("第" + d + "天：1、" + spot.getSpotName()
                            + (StringUtils.isNotEmpty(spot.getSpotLevel()) ? "（" + spot.getSpotLevel() + "）" : "")
                            + "。新增行程日，可继续在对话中细化安排。");
                    BizTravelPlanDay upd = new BizTravelPlanDay();
                    upd.setId(day.getId());
                    upd.setDaySummary(day.getDaySummary());
                    travelPlanDayMapper.updateBizTravelPlanDay(upd);
                }
                else
                {
                    day.setDaySummary("第" + d + "天：自由活动日，可根据兴趣探索当地街区、市集与特色美食。");
                    BizTravelPlanDay upd = new BizTravelPlanDay();
                    upd.setId(day.getId());
                    upd.setDaySummary(day.getDaySummary());
                    travelPlanDayMapper.updateBizTravelPlanDay(upd);
                }
            }
        }
        else
        {
            // 查询所有天，删除最后 (oldDays-newDays) 天及其景点关联
            BizTravelPlanDay dayQuery = new BizTravelPlanDay();
            dayQuery.setPlanId(plan.getId());
            List<BizTravelPlanDay> days = travelPlanDayMapper.selectBizTravelPlanDayList(dayQuery)
                    .stream().sorted(Comparator.comparing(BizTravelPlanDay::getDayNum)).collect(Collectors.toList());
            for (int i = days.size() - 1; i >= newDays; i--)
            {
                BizTravelPlanDay day = days.get(i);
                BizTravelPlanDaySpot linkQuery = new BizTravelPlanDaySpot();
                linkQuery.setPlanDayId(day.getId());
                for (BizTravelPlanDaySpot link : travelPlanDaySpotMapper.selectBizTravelPlanDaySpotList(linkQuery))
                {
                    travelPlanDaySpotMapper.deleteBizTravelPlanDaySpotById(link.getId());
                }
                travelPlanDayMapper.deleteBizTravelPlanDayById(day.getId());
            }
        }

        // 更新主表：天数、结束日期、标题、预算
        BizTravelPlan update = new BizTravelPlan();
        update.setId(plan.getId());
        update.setTravelDays((long) newDays);
        update.setEndDate(toDate(startDate.plusDays(newDays - 1L)));
        update.setPlanTitle(plan.getDestinationCity() + newDays + "日" + consumptionName(plan.getConsumptionLevel()) + "游");
        BigDecimal newBudget = estimateDailyBudget(plan).multiply(new BigDecimal(newDays));
        update.setTotalBudget(newBudget);
        update.setPlanSummary(String.format("行程已根据你的要求%s为%s天，结束日期调整为%tF。左侧卡片已同步更新，可继续提出修改。",
                increase ? "延长" : "缩短", newDays, toDate(startDate.plusDays(newDays - 1L))));
        update.setUpdateTime(DateUtils.getNowDate());
        travelPlanMapper.updateBizTravelPlan(update);
        // 同步内存对象，后续逻辑用到
        plan.setTravelDays((long) newDays);
        plan.setEndDate(update.getEndDate());
        plan.setPlanTitle(update.getPlanTitle());

        return String.format("好的，已将行程%s为 %d 天，结束日期同步调整为 %tF，左侧行程卡片已更新。还需要其他调整吗？",
                increase ? "延长" : "缩短", newDays, update.getEndDate());
    }

    /**
     * 调整预算档次
     */
    private String adjustBudget(BizTravelPlan plan, int level)
    {
        BizTravelPlan update = new BizTravelPlan();
        update.setId(plan.getId());
        update.setConsumptionLevel(level);
        update.setAccommodationPrefer(level);
        BigDecimal daily = estimateDailyBudget(plan, level);
        long days = plan.getTravelDays();
        update.setTotalBudget(daily.multiply(new BigDecimal(days)));
        update.setPlanTitle(plan.getDestinationCity() + days + "日" + consumptionName(level) + "游");
        update.setPlanSummary(String.format("行程预算档次已调整为「%s」，住宿与每日花费建议同步更新，左侧卡片可查看最新安排。",
                budgetFullName(level)));
        update.setUpdateTime(DateUtils.getNowDate());
        travelPlanMapper.updateBizTravelPlan(update);

        // 同步更新每日住宿推荐与当日基础预算
        BizTravelPlanDay dayQuery = new BizTravelPlanDay();
        dayQuery.setPlanId(plan.getId());
        for (BizTravelPlanDay day : travelPlanDayMapper.selectBizTravelPlanDayList(dayQuery))
        {
            BizTravelPlanDay upd = new BizTravelPlanDay();
            upd.setId(day.getId());
            upd.setHotelRecommend(buildHotelRecommend(level));
            travelPlanDayMapper.updateBizTravelPlanDay(upd);
        }
        plan.setConsumptionLevel(level);

        return String.format("已将预算档次调整为「%s」，住宿推荐和每日预算已同步更新，左侧卡片可查看。", budgetFullName(level));
    }

    /**
     * 更换最后一天的最后一个景点（用知识库中未安排的景点替换）
     */
    private String swapSpot(BizTravelPlan plan)
    {
        List<BizScenicSpot> candidates = recallUnusedSpots(plan);
        if (candidates.isEmpty())
        {
            return String.format("抱歉，景点知识库中%s的其他景点暂时收录不多，无法替换。你可以试试更换目的地重新规划，或先保留当前安排～",
                    plan.getDestinationCity());
        }
        BizScenicSpot newSpot = candidates.get(0);

        // 找到最后一天及其最后一个景点关联
        BizTravelPlanDay lastDay = getLastDay(plan.getId());
        if (lastDay == null)
        {
            return "当前行程还没有可替换的景点安排，请先增加景点或重新规划。";
        }
        BizTravelPlanDaySpot linkQuery = new BizTravelPlanDaySpot();
        linkQuery.setPlanDayId(lastDay.getId());
        List<BizTravelPlanDaySpot> links = travelPlanDaySpotMapper.selectBizTravelPlanDaySpotList(linkQuery)
                .stream().sorted(Comparator.comparing(BizTravelPlanDaySpot::getSortNum)).collect(Collectors.toList());
        if (links.isEmpty())
        {
            insertDaySpot(lastDay.getId(), newSpot, 1);
            return String.format("已为第%d天补充安排「%s」，左侧卡片已更新。", lastDay.getDayNum(), newSpot.getSpotName());
        }
        BizTravelPlanDaySpot target = links.get(links.size() - 1);
        BizTravelPlanDaySpot upd = new BizTravelPlanDaySpot();
        upd.setId(target.getId());
        upd.setSpotId(newSpot.getId());
        upd.setPlayDuration(newSpot.getPlayHours() == null ? new BigDecimal("2") : newSpot.getPlayHours());
        upd.setTicketCost(newSpot.getAdultPrice() == null ? BigDecimal.ZERO : newSpot.getAdultPrice());
        upd.setPlayTip(buildPlayTip(newSpot));
        travelPlanDaySpotMapper.updateBizTravelPlanDaySpot(upd);

        // 同步当日概述
        refreshDaySummary(lastDay);
        return String.format("已将第%d天的最后一个景点更换为「%s」%s，门票约%s元，左侧行程已更新。",
                lastDay.getDayNum(), newSpot.getSpotName(),
                StringUtils.isNotEmpty(newSpot.getSpotLevel()) ? "（" + newSpot.getSpotLevel() + "）" : "",
                newSpot.getAdultPrice() == null ? "0" : newSpot.getAdultPrice().stripTrailingZeros().toPlainString());
    }

    /**
     * 在最后一天追加一个景点
     */
    private String addSpot(BizTravelPlan plan)
    {
        List<BizScenicSpot> candidates = recallUnusedSpots(plan);
        if (candidates.isEmpty())
        {
            return String.format("景点知识库中%s暂未收录更多景点了，后续会持续扩充。你也可以尝试调整天数或更换目的地～",
                    plan.getDestinationCity());
        }
        BizTravelPlanDay lastDay = getLastDay(plan.getId());
        if (lastDay == null)
        {
            return "当前行程天数数据异常，请重新规划。";
        }
        BizTravelPlanDaySpot linkQuery = new BizTravelPlanDaySpot();
        linkQuery.setPlanDayId(lastDay.getId());
        long maxSort = travelPlanDaySpotMapper.selectBizTravelPlanDaySpotList(linkQuery)
                .stream().map(BizTravelPlanDaySpot::getSortNum).max(Long::compare).orElse(0L);
        BizScenicSpot newSpot = candidates.get(0);
        insertDaySpot(lastDay.getId(), newSpot, (int) maxSort + 1);
        refreshDaySummary(lastDay);

        // 主表景点数+1
        BizTravelPlan upd = new BizTravelPlan();
        upd.setId(plan.getId());
        upd.setSpotCount((plan.getSpotCount() == null ? 0 : plan.getSpotCount()) + 1);
        travelPlanMapper.updateBizTravelPlan(upd);

        return String.format("已在第%d天追加景点「%s」%s，左侧行程卡片已更新，还想调整什么？",
                lastDay.getDayNum(), newSpot.getSpotName(),
                StringUtils.isNotEmpty(newSpot.getSpotLevel()) ? "（" + newSpot.getSpotLevel() + "）" : "");
    }

    /**
     * 补充美食主题：把一个未设置美食主题的日期改为美食探店体验
     */
    private String addFoodTheme(BizTravelPlan plan)
    {
        BizTravelPlanDay dayQuery = new BizTravelPlanDay();
        dayQuery.setPlanId(plan.getId());
        List<BizTravelPlanDay> days = travelPlanDayMapper.selectBizTravelPlanDayList(dayQuery)
                .stream().sorted(Comparator.comparing(BizTravelPlanDay::getDayNum)).collect(Collectors.toList());
        BizTravelPlanDay target = days.stream().filter(d -> d.getDayTheme() == null || !d.getDayTheme().contains("美食")).findFirst().orElse(null);
        if (target == null)
        {
            return "你的每一天都已经安排了美食主题，建议关注当地老字号与夜市小吃，注意饮食卫生哦～";
        }
        BizTravelPlanDay upd = new BizTravelPlanDay();
        upd.setId(target.getId());
        upd.setDayTheme(plan.getDestinationCity() + "·美食探店体验");
        String extra = "今日重点安排当地特色美食：可在正餐选择本地老字号，晚上逛夜市品尝小吃，注意荤素搭配与饮食卫生。";
        upd.setDaySummary(StringUtils.isNotEmpty(target.getDaySummary()) ? target.getDaySummary() + " " + extra : extra);
        travelPlanDayMapper.updateBizTravelPlanDay(upd);
        return String.format("已把第%d天的主题调整为「美食探店体验」，并补充了当地美食安排建议，左侧卡片已更新。", target.getDayNum());
    }

    // ============================== 公共辅助方法 ==============================

    /**
     * 加载行程并做归属/存在性校验
     */
    private BizTravelPlan loadAndCheckPlan(Long itineraryId, Long userId)
    {
        if (itineraryId == null)
        {
            throw new ServiceException("行程ID不能为空");
        }
        BizTravelPlan plan = travelPlanMapper.selectBizTravelPlanById(itineraryId);
        if (plan == null)
        {
            throw new ServiceException("行程方案不存在或已被删除");
        }
        if (plan.getUserId() == null || !plan.getUserId().equals(userId))
        {
            throw new ServiceException("无权查看该行程方案");
        }
        return plan;
    }

    /**
     * 获取行程的最后一天
     */
    private BizTravelPlanDay getLastDay(Long planId)
    {
        BizTravelPlanDay dayQuery = new BizTravelPlanDay();
        dayQuery.setPlanId(planId);
        return travelPlanDayMapper.selectBizTravelPlanDayList(dayQuery)
                .stream().max(Comparator.comparing(BizTravelPlanDay::getDayNum)).orElse(null);
    }

    /**
     * 召回目的地景点中、当前行程尚未安排的候选景点
     */
    private List<BizScenicSpot> recallUnusedSpots(BizTravelPlan plan)
    {
        List<BizScenicSpot> recalled = scenicSpotMapper.selectSpotsByDestination(plan.getDestinationCity());
        // 收集行程已使用的景点ID
        Set<Long> usedIds = new HashSet<>();
        BizTravelPlanDay dayQuery = new BizTravelPlanDay();
        dayQuery.setPlanId(plan.getId());
        for (BizTravelPlanDay day : travelPlanDayMapper.selectBizTravelPlanDayList(dayQuery))
        {
            BizTravelPlanDaySpot linkQuery = new BizTravelPlanDaySpot();
            linkQuery.setPlanDayId(day.getId());
            for (BizTravelPlanDaySpot link : travelPlanDaySpotMapper.selectBizTravelPlanDaySpotList(linkQuery))
            {
                usedIds.add(link.getSpotId());
            }
        }
        return recalled.stream().filter(s -> !usedIds.contains(s.getId())).collect(Collectors.toList());
    }

    /**
     * 新增一条"天-景点"关联记录
     */
    private void insertDaySpot(Long dayId, BizScenicSpot spot, int sortNum)
    {
        BizTravelPlanDaySpot daySpot = new BizTravelPlanDaySpot();
        daySpot.setPlanDayId(dayId);
        daySpot.setSpotId(spot.getId());
        daySpot.setSortNum((long) sortNum);
        daySpot.setPlayDuration(spot.getPlayHours() == null ? new BigDecimal("2") : spot.getPlayHours());
        daySpot.setTicketCost(spot.getAdultPrice() == null ? BigDecimal.ZERO : spot.getAdultPrice());
        daySpot.setPlayTip(buildPlayTip(spot));
        daySpot.setCreateTime(DateUtils.getNowDate());
        travelPlanDaySpotMapper.insertBizTravelPlanDaySpot(daySpot);
    }

    /**
     * 根据当日景点关联重新生成当日概述
     */
    private void refreshDaySummary(BizTravelPlanDay day)
    {
        BizTravelPlanDaySpot linkQuery = new BizTravelPlanDaySpot();
        linkQuery.setPlanDayId(day.getId());
        List<BizTravelPlanDaySpot> links = travelPlanDaySpotMapper.selectBizTravelPlanDaySpotList(linkQuery)
                .stream().sorted(Comparator.comparing(BizTravelPlanDaySpot::getSortNum)).collect(Collectors.toList());
        if (links.isEmpty())
        {
            return;
        }
        Long[] ids = links.stream().map(BizTravelPlanDaySpot::getSpotId).toArray(Long[]::new);
        Map<Long, BizScenicSpot> spotMap = new HashMap<>();
        for (BizScenicSpot spot : scenicSpotMapper.selectBizScenicSpotByIds(ids))
        {
            spotMap.put(spot.getId(), spot);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("第").append(day.getDayNum()).append("天：");
        for (int i = 0; i < links.size(); i++)
        {
            BizScenicSpot spot = spotMap.get(links.get(i).getSpotId());
            if (i > 0)
            {
                sb.append("；");
            }
            sb.append(i + 1).append("、").append(spot == null ? "景点#" + links.get(i).getSpotId() : spot.getSpotName());
            if (spot != null && StringUtils.isNotEmpty(spot.getSpotLevel()))
            {
                sb.append("（").append(spot.getSpotLevel()).append("）");
            }
        }
        sb.append("。");
        BizTravelPlanDay upd = new BizTravelPlanDay();
        upd.setId(day.getId());
        upd.setDaySummary(sb.toString());
        travelPlanDayMapper.updateBizTravelPlanDay(upd);
    }

    /**
     * 按当前消费等级估算单日总预算（人均基础花费 × 人数）
     */
    private BigDecimal estimateDailyBudget(BizTravelPlan plan)
    {
        return estimateDailyBudget(plan, plan.getConsumptionLevel() == null ? 2 : plan.getConsumptionLevel());
    }

    private BigDecimal estimateDailyBudget(BizTravelPlan plan, int level)
    {
        long people = plan.getPeopleCount() == null || plan.getPeopleCount() < 1 ? 1 : plan.getPeopleCount();
        return PER_CAPITA_DAILY.getOrDefault(level, PER_CAPITA_DAILY.get(2)).multiply(new BigDecimal(people));
    }

    /**
     * 从提示词快照（格式："目的地:xx; ...; 备注:xx"）中解析备注内容
     */
    private String parseRemarkFromSnapshot(String snapshot)
    {
        if (StringUtils.isEmpty(snapshot))
        {
            return "";
        }
        Matcher m = REMARK_PATTERN.matcher(snapshot);
        return m.find() ? m.group(1).trim() : "";
    }

    private String consumptionName(Integer level)
    {
        if (level == null)
        {
            return "舒适";
        }
        return level == 1 ? "经济" : level == 3 ? "高端" : "舒适";
    }

    private String budgetFullName(int level)
    {
        return level == 1 ? "经济型" : level == 3 ? "高端" : "中等（舒适）";
    }

    private Date toDate(LocalDate localDate)
    {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
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

    private String buildHotelRecommend(Integer level)
    {
        int lv = level == null ? 2 : level;
        if (lv == 1)
        {
            return "推荐选择连锁快捷酒店或青年旅舍，经济实惠，优先选近地铁/公交站点";
        }
        if (lv == 3)
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
