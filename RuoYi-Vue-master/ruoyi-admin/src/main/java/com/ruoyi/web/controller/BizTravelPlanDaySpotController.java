package com.ruoyi.web.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.BizTravelPlanDaySpot;
import com.ruoyi.system.service.IBizTravelPlanDaySpotService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 每日行程景点关联Controller
 * 
 * @author ruoyi
 * @date 2026-09-19
 */
@RestController
@RequestMapping("/system/spotlogin")
public class BizTravelPlanDaySpotController extends BaseController
{
    @Autowired
    private IBizTravelPlanDaySpotService bizTravelPlanDaySpotService;

    /**
     * 查询每日行程景点关联列表
     */
    @PreAuthorize("@ss.hasPermi('system:spotlogin:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizTravelPlanDaySpot bizTravelPlanDaySpot)
    {
        startPage();
        List<BizTravelPlanDaySpot> list = bizTravelPlanDaySpotService.selectBizTravelPlanDaySpotList(bizTravelPlanDaySpot);
        return getDataTable(list);
    }

    /**
     * 导出每日行程景点关联列表
     */
    @PreAuthorize("@ss.hasPermi('system:spotlogin:export')")
    @Log(title = "每日行程景点关联", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BizTravelPlanDaySpot bizTravelPlanDaySpot)
    {
        List<BizTravelPlanDaySpot> list = bizTravelPlanDaySpotService.selectBizTravelPlanDaySpotList(bizTravelPlanDaySpot);
        ExcelUtil<BizTravelPlanDaySpot> util = new ExcelUtil<BizTravelPlanDaySpot>(BizTravelPlanDaySpot.class);
        util.exportExcel(response, list, "每日行程景点关联数据");
    }

    /**
     * 获取每日行程景点关联详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:spotlogin:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(bizTravelPlanDaySpotService.selectBizTravelPlanDaySpotById(id));
    }

    /**
     * 新增每日行程景点关联
     */
    @PreAuthorize("@ss.hasPermi('system:spotlogin:add')")
    @Log(title = "每日行程景点关联", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizTravelPlanDaySpot bizTravelPlanDaySpot)
    {
        return toAjax(bizTravelPlanDaySpotService.insertBizTravelPlanDaySpot(bizTravelPlanDaySpot));
    }

    /**
     * 修改每日行程景点关联
     */
    @PreAuthorize("@ss.hasPermi('system:spotlogin:edit')")
    @Log(title = "每日行程景点关联", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizTravelPlanDaySpot bizTravelPlanDaySpot)
    {
        return toAjax(bizTravelPlanDaySpotService.updateBizTravelPlanDaySpot(bizTravelPlanDaySpot));
    }

    /**
     * 删除每日行程景点关联
     */
    @PreAuthorize("@ss.hasPermi('system:spotlogin:remove')")
    @Log(title = "每日行程景点关联", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(bizTravelPlanDaySpotService.deleteBizTravelPlanDaySpotByIds(ids));
    }
}
