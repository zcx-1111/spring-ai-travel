package com.ruoyi.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.ai.EsScenicSpotService;
import com.ruoyi.system.domain.es.EsScenicSearchResult;

/**
 * 景点 ES 全文检索（管理端）
 *
 * <p>提供关键词分页检索（用于后台验证ES效果）与全量同步触发入口。
 * 检索异常时Service内部自动降级MySQL，不向前端抛出。</p>
 *
 * @author ruoyi
 * @date 2026-10-05
 */
@RestController
@RequestMapping("/system/spot/es")
public class EsScenicAdminController extends BaseController
{
    @Autowired
    private EsScenicSpotService esScenicSpotService;

    /**
     * 关键词分页检索
     * GET /system/spot/es/search?keyword=象鼻山&city=桂林&pageNum=1&pageSize=10&sort=level
     *
     * @param keyword  关键词（名称/城市/标签/地址/简介）
     * @param city     城市锚点（先定位城市再城市内检索）
     * @param pageNum  页码
     * @param pageSize 每页数
     * @param sort     排序：relevance/level/priceAsc/priceDesc
     */
    @PreAuthorize("@ss.hasPermi('system:spot:list')")
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

    /**
     * 全量同步MySQL景点到ES（首次部署、批量导入后调用）
     * POST /system/spot/es/syncAll
     */
    @PreAuthorize("@ss.hasPermi('system:spot:add')")
    @Log(title = "景点ES索引", businessType = BusinessType.OTHER)
    @PostMapping("/syncAll")
    public AjaxResult syncAll()
    {
        try
        {
            int count = esScenicSpotService.syncAll();
            return AjaxResult.success("同步完成，共同步景点 " + count + " 个", count);
        }
        catch (IllegalStateException e)
        {
            // ES不可用/同步失败：明确提示（同步是人工触发动作，需要暴露失败原因）
            return AjaxResult.error(e.getMessage());
        }
    }
}
