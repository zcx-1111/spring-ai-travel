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
import com.ruoyi.system.domain.TourUser;
import com.ruoyi.system.service.ITourUserService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 游客用户Controller
 * 
 * @author ruoyi
 * @date 2026-09-19
 */
@RestController
@RequestMapping("/system/userloginn")
public class TourUserController extends BaseController
{
    @Autowired
    private ITourUserService tourUserService;

    /**
     * 查询游客用户列表
     */
    @PreAuthorize("@ss.hasPermi('system:userloginn:list')")
    @GetMapping("/list")
    public TableDataInfo list(TourUser tourUser)
    {
        startPage();
        List<TourUser> list = tourUserService.selectTourUserList(tourUser);
        return getDataTable(list);
    }

    /**
     * 导出游客用户列表
     */
    @PreAuthorize("@ss.hasPermi('system:userloginn:export')")
    @Log(title = "游客用户", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TourUser tourUser)
    {
        List<TourUser> list = tourUserService.selectTourUserList(tourUser);
        ExcelUtil<TourUser> util = new ExcelUtil<TourUser>(TourUser.class);
        util.exportExcel(response, list, "游客用户数据");
    }

    /**
     * 获取游客用户详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:userloginn:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(tourUserService.selectTourUserById(id));
    }

    /**
     * 新增游客用户
     */
    @PreAuthorize("@ss.hasPermi('system:userloginn:add')")
    @Log(title = "游客用户", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TourUser tourUser)
    {
        return toAjax(tourUserService.insertTourUser(tourUser));
    }

    /**
     * 修改游客用户
     */
    @PreAuthorize("@ss.hasPermi('system:userloginn:edit')")
    @Log(title = "游客用户", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TourUser tourUser)
    {
        return toAjax(tourUserService.updateTourUser(tourUser));
    }

    /**
     * 删除游客用户
     */
    @PreAuthorize("@ss.hasPermi('system:userloginn:remove')")
    @Log(title = "游客用户", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(tourUserService.deleteTourUserByIds(ids));
    }
}
