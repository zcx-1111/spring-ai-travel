package com.ruoyi.system.service;

import com.ruoyi.system.domain.dto.TourPlanGenerateDTO;

/**
 * 前台AI行程生成 服务层
 *
 * @author ruoyi
 * @date 2026-09-21
 */
public interface ITourPlanAiService
{
    /**
     * 根据游客填写的出行信息生成行程方案
     * 流程：知识库召回景点 -> 组装行程主表 -> 按天分配景点 -> 落库
     *
     * @param dto    出行信息表单
     * @param userId 当前登录游客ID（tour_user.id）
     * @return 生成的行程方案ID（biz_travel_plan.id）
     */
    public Long generatePlan(TourPlanGenerateDTO dto, Long userId);
}
