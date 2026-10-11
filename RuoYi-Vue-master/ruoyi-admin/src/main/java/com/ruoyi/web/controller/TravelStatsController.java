package com.ruoyi.web.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.mapper.TravelStatsMapper;

/**
 * 旅游统计数据 Controller
 * 为管理后台首页数据大盘提供统一查询接口
 *
 * @author ruoyi
 * @date 2026-09-23
 */
@RestController
@RequestMapping("/travel/stats")
public class TravelStatsController extends BaseController
{
    @Autowired
    private TravelStatsMapper travelStatsMapper;

    /**
     * 获取首页数据大盘全部统计数据
     * GET /travel/stats/getHomeData
     *
     * 返回 data 结构：
     * {
     *   scenicTotal: 景点总数,
     *   planTotal: 行程方案数,
     *   chatTotal: 问答交互数,
     *   userTotal: 注册游客数,
     *   spotLevels: [{ name, value }] 景点等级分布,
     *   cityTopSpots: [{ name, value }] 城市景点TOP8,
     *   chatTrend7Days: [{ date, count }] 近7天问答趋势,
     *   chatCategories: [{ name, value }] 提问分类分布,
     *   recentChats: [{ question, answer, askTime }] 最近10条问答
     * }
     */
    @GetMapping("/getHomeData")
    public AjaxResult getHomeData()
    {
        Map<String, Object> data = new HashMap<>();
        // 四个统计数字
        data.put("scenicTotal", travelStatsMapper.countScenicSpots());
        data.put("planTotal", travelStatsMapper.countTravelPlans());
        data.put("chatTotal", travelStatsMapper.countChatMessages());
        data.put("userTotal", travelStatsMapper.countTourUsers());
        // 图表数据
        data.put("spotLevels", travelStatsMapper.spotLevelDistribution());
        data.put("provinceTopSpots", travelStatsMapper.provinceTopSpots());
        data.put("cityTopSpots", travelStatsMapper.cityTopSpots());
        data.put("chatTrend7Days", travelStatsMapper.chatTrend7Days());
        data.put("chatCategories", travelStatsMapper.chatCategoryDistribution());
        // 最近问答
        data.put("recentChats", travelStatsMapper.recentChats());
        return success(data);
    }
}
