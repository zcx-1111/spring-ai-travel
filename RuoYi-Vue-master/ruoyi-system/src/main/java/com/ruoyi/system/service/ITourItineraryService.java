package com.ruoyi.system.service;

import java.util.List;
import java.util.Map;

/**
 * 前台行程详情与多轮对话 服务层
 *
 * @author ruoyi
 * @date 2026-09-21
 */
public interface ITourItineraryService
{
    /**
     * 分页查询当前登录游客的行程方案列表
     *
     * @param userId 当前登录游客ID
     * @return 行程卡片数据列表（分页由Controller层PageHelper拦截）
     */
    public List<Map<String, Object>> listMyPlans(Long userId);

    /**
     * 删除行程方案（级联删除每日行程与景点关联）
     *
     * @param itineraryId 行程方案ID
     * @param userId      当前登录游客ID（归属校验）
     * @return 影响行数
     */
    public int removePlan(Long itineraryId, Long userId);

    /**
     * 加载行程完整详情（基础信息+每日行程+景点+历史对话）
     *
     * @param itineraryId 行程方案ID
     * @param userId      当前登录游客ID（归属校验）
     * @return 聚合后的行程数据
     */
    public Map<String, Object> getItinerary(Long itineraryId, Long userId);

    /**
     * 多轮对话：解析用户意图 -> 更新行程 -> 保存对话记录 -> 返回AI回复与最新行程
     *
     * @param itineraryId 行程方案ID
     * @param userId      当前登录游客ID
     * @param userMessage 用户提问内容
     * @return ai回复内容 + 最新行程聚合数据
     */
    public Map<String, Object> chat(Long itineraryId, Long userId, String userMessage);
}
