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
import com.ruoyi.system.domain.BizTravelPlanDay;
import com.ruoyi.system.service.IBizTravelPlanDayService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 行程每日详情Controller
 * 
 * @author ruoyi
 * @date 2026-09-19
 */
@RestController
@RequestMapping("/system/daylogin")
public class BizTravelPlanDayController extends BaseController
{
    @Autowired
    private IBizTravelPlanDayService bizTravelPlanDayService;

    /**
     * 查询行程每日详情列表
     */
    @PreAuthorize("@ss.hasPermi('system:daylogin:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizTravelPlanDay bizTravelPlanDay)
    {
        startPage();
        List<BizTravelPlanDay> list = bizTravelPlanDayService.selectBizTravelPlanDayList(bizTravelPlanDay);
        return getDataTable(list);
    }

    /**
     * 导出行程每日详情列表
     */
    @PreAuthorize("@ss.hasPermi('system:daylogin:export')")
    @Log(title = "行程每日详情", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BizTravelPlanDay bizTravelPlanDay)
    {
        List<BizTravelPlanDay> list = bizTravelPlanDayService.selectBizTravelPlanDayList(bizTravelPlanDay);
        ExcelUtil<BizTravelPlanDay> util = new ExcelUtil<BizTravelPlanDay>(BizTravelPlanDay.class);
        util.exportExcel(response, list, "行程每日详情数据");
    }

    /**
     * 获取行程每日详情详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:daylogin:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(bizTravelPlanDayService.selectBizTravelPlanDayById(id));
    }

    /**
     * 新增行程每日详情
     */
    @PreAuthorize("@ss.hasPermi('system:daylogin:add')")
    @Log(title = "行程每日详情", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizTravelPlanDay bizTravelPlanDay)
    {
        return toAjax(bizTravelPlanDayService.insertBizTravelPlanDay(bizTravelPlanDay));
    }

    /**
     * 修改行程每日详情
     */
    @PreAuthorize("@ss.hasPermi('system:daylogin:edit')")
    @Log(title = "行程每日详情", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizTravelPlanDay bizTravelPlanDay)
    {
        return toAjax(bizTravelPlanDayService.updateBizTravelPlanDay(bizTravelPlanDay));
    }

    /**
     * 删除行程每日详情
     */
    @PreAuthorize("@ss.hasPermi('system:daylogin:remove')")
    @Log(title = "行程每日详情", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(bizTravelPlanDayService.deleteBizTravelPlanDayByIds(ids));
    }
}
