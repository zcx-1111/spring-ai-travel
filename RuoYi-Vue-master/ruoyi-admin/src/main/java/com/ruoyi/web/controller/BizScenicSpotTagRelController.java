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
import com.ruoyi.system.domain.BizScenicSpotTagRel;
import com.ruoyi.system.service.IBizScenicSpotTagRelService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 景点-标签关联Controller
 * 
 * @author ruoyi
 * @date 2026-09-19
 */
@RestController
@RequestMapping("/system/rellogin")
public class BizScenicSpotTagRelController extends BaseController
{
    @Autowired
    private IBizScenicSpotTagRelService bizScenicSpotTagRelService;

    /**
     * 查询景点-标签关联列表
     */
    @PreAuthorize("@ss.hasPermi('system:rellogin:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizScenicSpotTagRel bizScenicSpotTagRel)
    {
        startPage();
        List<BizScenicSpotTagRel> list = bizScenicSpotTagRelService.selectBizScenicSpotTagRelList(bizScenicSpotTagRel);
        return getDataTable(list);
    }

    /**
     * 导出景点-标签关联列表
     */
    @PreAuthorize("@ss.hasPermi('system:rellogin:export')")
    @Log(title = "景点-标签关联", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BizScenicSpotTagRel bizScenicSpotTagRel)
    {
        List<BizScenicSpotTagRel> list = bizScenicSpotTagRelService.selectBizScenicSpotTagRelList(bizScenicSpotTagRel);
        ExcelUtil<BizScenicSpotTagRel> util = new ExcelUtil<BizScenicSpotTagRel>(BizScenicSpotTagRel.class);
        util.exportExcel(response, list, "景点-标签关联数据");
    }

    /**
     * 获取景点-标签关联详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:rellogin:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(bizScenicSpotTagRelService.selectBizScenicSpotTagRelById(id));
    }

    /**
     * 新增景点-标签关联
     */
    @PreAuthorize("@ss.hasPermi('system:rellogin:add')")
    @Log(title = "景点-标签关联", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizScenicSpotTagRel bizScenicSpotTagRel)
    {
        return toAjax(bizScenicSpotTagRelService.insertBizScenicSpotTagRel(bizScenicSpotTagRel));
    }

    /**
     * 修改景点-标签关联
     */
    @PreAuthorize("@ss.hasPermi('system:rellogin:edit')")
    @Log(title = "景点-标签关联", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizScenicSpotTagRel bizScenicSpotTagRel)
    {
        return toAjax(bizScenicSpotTagRelService.updateBizScenicSpotTagRel(bizScenicSpotTagRel));
    }

    /**
     * 删除景点-标签关联
     */
    @PreAuthorize("@ss.hasPermi('system:rellogin:remove')")
    @Log(title = "景点-标签关联", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(bizScenicSpotTagRelService.deleteBizScenicSpotTagRelByIds(ids));
    }
}
