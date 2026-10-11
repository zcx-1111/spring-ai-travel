package com.ruoyi.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.ai.EsScenicSpotService;
import com.ruoyi.system.domain.es.EsScenicSearchResult;

/**
 * 景点关键词检索（游客端）
 *
 * <p>面向游客前台提供独立的关键词全文检索能力（区别于AI行程生成的向量召回）。
 * 走游客Token鉴权（/api/travel/** 默认需登录），检索异常由Service内部自动降级MySQL。</p>
 *
 * @author ruoyi
 * @date 2026-10-05
 */
@RestController
@RequestMapping("/api/travel/scenic")
public class TourScenicSearchController extends BaseController
{
    @Autowired
    private EsScenicSpotService esScenicSpotService;

    /**
     * 关键词分页检索景点
     * GET /api/travel/scenic/search?keyword=象鼻山&city=桂林&pageNum=1&pageSize=10&sort=level
     *
     * @param keyword  关键词（匹配名称/城市/标签/地址/简介）
     * @param city     城市名（先锚定城市再在该城市内检索，避免跨城误召回），可空
     * @param pageNum  页码，从1开始
     * @param pageSize 每页条数
     * @param sort     排序：relevance相关度(默认)、level等级优先、priceAsc/priceDesc票价
     * @return AjaxResult.data = EsScenicSearchResult { total, pageNum, pageSize, rows, source }
     */
    @GetMapping("/search")
    public AjaxResult search(@RequestParam(value = "keyword", required = false) String keyword,
                             @RequestParam(value = "city", required = false) String city,
                             @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                             @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
                             @RequestParam(value = "sort", defaultValue = "relevance") String sort)
    {
        EsScenicSearchResult result = esScenicSpotService
                .searchSpots(keyword, city, pageNum, pageSize, sort);
        return AjaxResult.success(result);
    }
}
