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
import com.ruoyi.system.domain.BizScenicCategory;
import com.ruoyi.system.service.IBizScenicCategoryService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 景点分类Controller
 * 
 * @author ruoyi
 * @date 2026-09-19
 */
@RestController
@RequestMapping("/system/categorylogin")
public class BizScenicCategoryController extends BaseController
{
    @Autowired
    private IBizScenicCategoryService bizScenicCategoryService;

    /**
     * 查询景点分类列表
     */
    @PreAuthorize("@ss.hasPermi('system:categorylogin:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizScenicCategory bizScenicCategory)
    {
        startPage();
        List<BizScenicCategory> list = bizScenicCategoryService.selectBizScenicCategoryList(bizScenicCategory);
        return getDataTable(list);
    }

    /**
     * 导出景点分类列表
     */
    @PreAuthorize("@ss.hasPermi('system:categorylogin:export')")
    @Log(title = "景点分类", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BizScenicCategory bizScenicCategory)
    {
        List<BizScenicCategory> list = bizScenicCategoryService.selectBizScenicCategoryList(bizScenicCategory);
        ExcelUtil<BizScenicCategory> util = new ExcelUtil<BizScenicCategory>(BizScenicCategory.class);
        util.exportExcel(response, list, "景点分类数据");
    }

    /**
     * 获取景点分类详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:categorylogin:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(bizScenicCategoryService.selectBizScenicCategoryById(id));
    }

    /**
     * 新增景点分类
     */
    @PreAuthorize("@ss.hasPermi('system:categorylogin:add')")
    @Log(title = "景点分类", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizScenicCategory bizScenicCategory)
    {
        return toAjax(bizScenicCategoryService.insertBizScenicCategory(bizScenicCategory));
    }

    /**
     * 修改景点分类
     */
    @PreAuthorize("@ss.hasPermi('system:categorylogin:edit')")
    @Log(title = "景点分类", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizScenicCategory bizScenicCategory)
    {
        return toAjax(bizScenicCategoryService.updateBizScenicCategory(bizScenicCategory));
    }

    /**
     * 删除景点分类
     */
    @PreAuthorize("@ss.hasPermi('system:categorylogin:remove')")
    @Log(title = "景点分类", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(bizScenicCategoryService.deleteBizScenicCategoryByIds(ids));
    }
}
