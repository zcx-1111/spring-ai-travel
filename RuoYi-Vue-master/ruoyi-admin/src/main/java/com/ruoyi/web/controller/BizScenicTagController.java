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
import com.ruoyi.system.domain.BizScenicTag;
import com.ruoyi.system.service.IBizScenicTagService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 景点标签Controller
 * 
 * @author ruoyi
 * @date 2026-09-19
 */
@RestController
@RequestMapping("/system/taglogin")
public class BizScenicTagController extends BaseController
{
    @Autowired
    private IBizScenicTagService bizScenicTagService;

    /**
     * 查询景点标签列表
     */
    @PreAuthorize("@ss.hasPermi('system:taglogin:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizScenicTag bizScenicTag)
    {
        startPage();
        List<BizScenicTag> list = bizScenicTagService.selectBizScenicTagList(bizScenicTag);
        return getDataTable(list);
    }

    /**
     * 导出景点标签列表
     */
    @PreAuthorize("@ss.hasPermi('system:taglogin:export')")
    @Log(title = "景点标签", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BizScenicTag bizScenicTag)
    {
        List<BizScenicTag> list = bizScenicTagService.selectBizScenicTagList(bizScenicTag);
        ExcelUtil<BizScenicTag> util = new ExcelUtil<BizScenicTag>(BizScenicTag.class);
        util.exportExcel(response, list, "景点标签数据");
    }

    /**
     * 获取景点标签详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:taglogin:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(bizScenicTagService.selectBizScenicTagById(id));
    }

    /**
     * 新增景点标签
     */
    @PreAuthorize("@ss.hasPermi('system:taglogin:add')")
    @Log(title = "景点标签", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizScenicTag bizScenicTag)
    {
        return toAjax(bizScenicTagService.insertBizScenicTag(bizScenicTag));
    }

    /**
     * 修改景点标签
     */
    @PreAuthorize("@ss.hasPermi('system:taglogin:edit')")
    @Log(title = "景点标签", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizScenicTag bizScenicTag)
    {
        return toAjax(bizScenicTagService.updateBizScenicTag(bizScenicTag));
    }

    /**
     * 删除景点标签
     */
    @PreAuthorize("@ss.hasPermi('system:taglogin:remove')")
    @Log(title = "景点标签", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(bizScenicTagService.deleteBizScenicTagByIds(ids));
    }
}
