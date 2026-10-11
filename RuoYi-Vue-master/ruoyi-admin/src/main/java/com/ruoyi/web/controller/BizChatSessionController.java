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
import com.ruoyi.system.domain.BizChatSession;
import com.ruoyi.system.service.IBizChatSessionService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 对话会话Controller
 * 
 * @author ruoyi
 * @date 2026-09-19
 */
@RestController
@RequestMapping("/system/sessionlogin")
public class BizChatSessionController extends BaseController
{
    @Autowired
    private IBizChatSessionService bizChatSessionService;

    /**
     * 查询对话会话列表
     */
    @PreAuthorize("@ss.hasPermi('system:sessionlogin:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizChatSession bizChatSession)
    {
        startPage();
        List<BizChatSession> list = bizChatSessionService.selectBizChatSessionList(bizChatSession);
        return getDataTable(list);
    }

    /**
     * 导出对话会话列表
     */
    @PreAuthorize("@ss.hasPermi('system:sessionlogin:export')")
    @Log(title = "对话会话", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BizChatSession bizChatSession)
    {
        List<BizChatSession> list = bizChatSessionService.selectBizChatSessionList(bizChatSession);
        ExcelUtil<BizChatSession> util = new ExcelUtil<BizChatSession>(BizChatSession.class);
        util.exportExcel(response, list, "对话会话数据");
    }

    /**
     * 获取对话会话详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:sessionlogin:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(bizChatSessionService.selectBizChatSessionById(id));
    }

    /**
     * 新增对话会话
     */
    @PreAuthorize("@ss.hasPermi('system:sessionlogin:add')")
    @Log(title = "对话会话", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizChatSession bizChatSession)
    {
        return toAjax(bizChatSessionService.insertBizChatSession(bizChatSession));
    }

    /**
     * 修改对话会话
     */
    @PreAuthorize("@ss.hasPermi('system:sessionlogin:edit')")
    @Log(title = "对话会话", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizChatSession bizChatSession)
    {
        return toAjax(bizChatSessionService.updateBizChatSession(bizChatSession));
    }

    /**
     * 删除对话会话
     */
    @PreAuthorize("@ss.hasPermi('system:sessionlogin:remove')")
    @Log(title = "对话会话", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(bizChatSessionService.deleteBizChatSessionByIds(ids));
    }
}
