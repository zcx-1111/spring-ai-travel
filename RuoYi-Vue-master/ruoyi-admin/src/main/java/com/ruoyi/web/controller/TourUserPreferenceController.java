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
import com.ruoyi.system.domain.TourUserPreference;
import com.ruoyi.system.service.ITourUserPreferenceService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 用户旅游偏好Controller
 * 
 * @author ruoyi
 * @date 2026-09-19
 */
@RestController
@RequestMapping("/system/preferenceloginn")
public class TourUserPreferenceController extends BaseController
{
    @Autowired
    private ITourUserPreferenceService tourUserPreferenceService;

    /**
     * 查询用户旅游偏好列表
     */
    @PreAuthorize("@ss.hasPermi('system:preferenceloginn:list')")
    @GetMapping("/list")
    public TableDataInfo list(TourUserPreference tourUserPreference)
    {
        startPage();
        List<TourUserPreference> list = tourUserPreferenceService.selectTourUserPreferenceList(tourUserPreference);
        return getDataTable(list);
    }

    /**
     * 导出用户旅游偏好列表
     */
    @PreAuthorize("@ss.hasPermi('system:preferenceloginn:export')")
    @Log(title = "用户旅游偏好", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TourUserPreference tourUserPreference)
    {
        List<TourUserPreference> list = tourUserPreferenceService.selectTourUserPreferenceList(tourUserPreference);
        ExcelUtil<TourUserPreference> util = new ExcelUtil<TourUserPreference>(TourUserPreference.class);
        util.exportExcel(response, list, "用户旅游偏好数据");
    }

    /**
     * 获取用户旅游偏好详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:preferenceloginn:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(tourUserPreferenceService.selectTourUserPreferenceById(id));
    }

    /**
     * 新增用户旅游偏好
     */
    @PreAuthorize("@ss.hasPermi('system:preferenceloginn:add')")
    @Log(title = "用户旅游偏好", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TourUserPreference tourUserPreference)
    {
        return toAjax(tourUserPreferenceService.insertTourUserPreference(tourUserPreference));
    }

    /**
     * 修改用户旅游偏好
     */
    @PreAuthorize("@ss.hasPermi('system:preferenceloginn:edit')")
    @Log(title = "用户旅游偏好", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TourUserPreference tourUserPreference)
    {
        return toAjax(tourUserPreferenceService.updateTourUserPreference(tourUserPreference));
    }

    /**
     * 删除用户旅游偏好
     */
    @PreAuthorize("@ss.hasPermi('system:preferenceloginn:remove')")
    @Log(title = "用户旅游偏好", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(tourUserPreferenceService.deleteTourUserPreferenceByIds(ids));
    }
}
