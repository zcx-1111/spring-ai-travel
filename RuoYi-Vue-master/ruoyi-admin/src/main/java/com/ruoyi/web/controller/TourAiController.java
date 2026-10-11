package com.ruoyi.web.controller;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.ai.ScenicKnowledgeLoader;
import com.ruoyi.system.domain.dto.TourChatDTO;
import com.ruoyi.system.domain.dto.TourPlanGenerateDTO;
import com.ruoyi.system.service.ITourItineraryService;
import com.ruoyi.system.service.ITourPlanAiService;

/**
 * 前台AI行程规划Controller
 * 路径前缀 /api/travel/ai，需登录（携带游客token）访问
 *
 * @author ruoyi
 * @date 2026-09-21
 */
@RestController
@RequestMapping("/api/travel/ai")
public class TourAiController extends BaseController
{
    @Autowired
    private ITourPlanAiService tourPlanAiService;

    @Autowired
    private ITourItineraryService tourItineraryService;

    @Autowired
    private ScenicKnowledgeLoader scenicKnowledgeLoader;

    /**
     * 生成AI行程方案
     * 请求体：{ destination, startDate, days, budget, crowd, preferences, remark }
     * 返回：data = { planId, spotCount }
     */
    @PostMapping("/generate")
    public AjaxResult generate(@RequestBody TourPlanGenerateDTO dto)
    {
        // 从登录态中获取当前游客ID（tour_user.id，登录签发token时写入）
        Long userId = SecurityUtils.getUserId();
        // 生成行程并落库
        Long planId = tourPlanAiService.generatePlan(dto, userId);
        // 组装返回数据
        Map<String, Object> data = new HashMap<>();
        data.put("planId", planId);
        AjaxResult ajax = AjaxResult.success("行程方案生成成功");
        ajax.put(AjaxResult.DATA_TAG, data);
        return ajax;
    }

    /**
     * 多轮对话修改行程
     * 请求体：{ itineraryId: 行程ID, userMessage: 用户提问 }
     * 返回：data = { reply: AI回复, itinerary: 更新后的基础信息, dayList: 每日行程, messages: 全部对话记录 }
     */
    @PostMapping("/chat")
    public AjaxResult chat(@RequestBody TourChatDTO dto)
    {
        Long userId = SecurityUtils.getUserId();
        Map<String, Object> data = tourItineraryService.chat(dto.getItineraryId(), userId, dto.getUserMessage());
        return success(data);
    }

    /**
     * 手动触发景点知识库全量同步（MySQL -> Qdrant向量库）
     * 使用场景：通过CSV/SQL导入新的景点数据后，无需重启后端即可刷新向量知识库。
     * 同步为幂等upsert语义，重复调用不会产生重复数据。
     * 返回：data = { success, total, successCount, failedBatches, message }
     */
    @PostMapping("/syncKnowledge")
    public AjaxResult syncKnowledge()
    {
        Map<String, Object> data = scenicKnowledgeLoader.syncNow();
        boolean success = Boolean.TRUE.equals(data.get("success"));
        AjaxResult ajax = success ? AjaxResult.success("知识库同步成功") : AjaxResult.error("知识库同步未成功");
        ajax.put(AjaxResult.DATA_TAG, data);
        return ajax;
    }
}
