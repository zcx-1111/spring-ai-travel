package com.ruoyi.system.mapper;

import java.util.List;
import java.util.Map;

/**
 * 旅游统计数据 Mapper 接口
 * 提供首页数据大盘所需的各类统计查询
 *
 * @author ruoyi
 */
public interface TravelStatsMapper
{
    /** 景点总数 */
    int countScenicSpots();

    /** 行程方案总数 */
    int countTravelPlans();

    /** 问答交互总次数（用户提问消息数） */
    int countChatMessages();

    /** 注册游客总数 */
    int countTourUsers();

    /**
     * 景点等级分布（5A/4A/3A/2A/未评级）
     * 返回每行：{ name: 等级名, value: 数量 }
     */
    List<Map<String, Object>> spotLevelDistribution();

    /**
     * 各城市景点数量 TOP8
     * 返回每行：{ name: 城市名, value: 数量 }
     */
    List<Map<String, Object>> cityTopSpots();

    /**
     * 各省份景点数量 TOP10
     * 返回每行：{ name: 省份名, value: 数量 }
     */
    List<Map<String, Object>> provinceTopSpots();

    /**
     * 最近 7 天每日用户问答量趋势
     * 返回每行：{ date: 日期, count: 数量 }
     */
    List<Map<String, Object>> chatTrend7Days();

    /**
     * 用户提问分类分布（景点咨询/行程规划/美食推荐/交通住宿/其他）
     * 返回每行：{ name: 分类名, value: 数量 }
     */
    List<Map<String, Object>> chatCategoryDistribution();

    /**
     * 最近 10 条问答记录
     * 返回每行：{ question, answer, askTime }
     */
    List<Map<String, Object>> recentChats();
}
