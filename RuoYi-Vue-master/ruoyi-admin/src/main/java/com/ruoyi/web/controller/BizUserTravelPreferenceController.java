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
import com.ruoyi.system.domain.BizUserTravelPreference;
import com.ruoyi.system.service.IBizUserTravelPreferenceService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 用户旅游偏好Controller
 * 
 * @author ruoyi
 * @date 2026-09-19
 */
@RestController
@RequestMapping("/system/preferencelogin")
public class BizUserTravelPreferenceController extends BaseController
{
    @Autowired
    private IBizUserTravelPreferenceService bizUserTravelPreferenceService;

    /**
     * 查询用户旅游偏好列表
     */
    @PreAuthorize("@ss.hasPermi('system:preferencelogin:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizUserTravelPreference bizUserTravelPreference)
    {
        startPage();
        List<BizUserTravelPreference> list = bizUserTravelPreferenceService.selectBizUserTravelPreferenceList(bizUserTravelPreference);
        return getDataTable(list);
    }

    /**
     * 导出用户旅游偏好列表
     */
    @PreAuthorize("@ss.hasPermi('system:preferencelogin:export')")
    @Log(title = "用户旅游偏好", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BizUserTravelPreference bizUserTravelPreference)
    {
        List<BizUserTravelPreference> list = bizUserTravelPreferenceService.selectBizUserTravelPreferenceList(bizUserTravelPreference);
        ExcelUtil<BizUserTravelPreference> util = new ExcelUtil<BizUserTravelPreference>(BizUserTravelPreference.class);
        util.exportExcel(response, list, "用户旅游偏好数据");
    }

    /**
     * 获取用户旅游偏好详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:preferencelogin:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(bizUserTravelPreferenceService.selectBizUserTravelPreferenceById(id));
    }

    /**
     * 新增用户旅游偏好
     */
    @PreAuthorize("@ss.hasPermi('system:preferencelogin:add')")
    @Log(title = "用户旅游偏好", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizUserTravelPreference bizUserTravelPreference)
    {
        return toAjax(bizUserTravelPreferenceService.insertBizUserTravelPreference(bizUserTravelPreference));
    }

    /**
     * 修改用户旅游偏好
     */
    @PreAuthorize("@ss.hasPermi('system:preferencelogin:edit')")
    @Log(title = "用户旅游偏好", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizUserTravelPreference bizUserTravelPreference)
    {
        return toAjax(bizUserTravelPreferenceService.updateBizUserTravelPreference(bizUserTravelPreference));
    }

    /**
     * 删除用户旅游偏好
     */
    @PreAuthorize("@ss.hasPermi('system:preferencelogin:remove')")
    @Log(title = "用户旅游偏好", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(bizUserTravelPreferenceService.deleteBizUserTravelPreferenceByIds(ids));
    }
}
