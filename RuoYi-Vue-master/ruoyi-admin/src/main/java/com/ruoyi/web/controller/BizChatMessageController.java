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
import com.ruoyi.system.domain.BizChatMessage;
import com.ruoyi.system.service.IBizChatMessageService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 对话消息Controller
 * 
 * @author ruoyi
 * @date 2026-09-19
 */
@RestController
@RequestMapping("/system/messagelogin")
public class BizChatMessageController extends BaseController
{
    @Autowired
    private IBizChatMessageService bizChatMessageService;

    /**
     * 查询对话消息列表
     */
    @PreAuthorize("@ss.hasPermi('system:messagelogin:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizChatMessage bizChatMessage)
    {
        startPage();
        List<BizChatMessage> list = bizChatMessageService.selectBizChatMessageList(bizChatMessage);
        return getDataTable(list);
    }

    /**
     * 导出对话消息列表
     */
    @PreAuthorize("@ss.hasPermi('system:messagelogin:export')")
    @Log(title = "对话消息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BizChatMessage bizChatMessage)
    {
        List<BizChatMessage> list = bizChatMessageService.selectBizChatMessageList(bizChatMessage);
        ExcelUtil<BizChatMessage> util = new ExcelUtil<BizChatMessage>(BizChatMessage.class);
        util.exportExcel(response, list, "对话消息数据");
    }

    /**
     * 获取对话消息详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:messagelogin:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(bizChatMessageService.selectBizChatMessageById(id));
    }

    /**
     * 新增对话消息
     */
    @PreAuthorize("@ss.hasPermi('system:messagelogin:add')")
    @Log(title = "对话消息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizChatMessage bizChatMessage)
    {
        return toAjax(bizChatMessageService.insertBizChatMessage(bizChatMessage));
    }

    /**
     * 修改对话消息
     */
    @PreAuthorize("@ss.hasPermi('system:messagelogin:edit')")
    @Log(title = "对话消息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizChatMessage bizChatMessage)
    {
        return toAjax(bizChatMessageService.updateBizChatMessage(bizChatMessage));
    }

    /**
     * 删除对话消息
     */
    @PreAuthorize("@ss.hasPermi('system:messagelogin:remove')")
    @Log(title = "对话消息", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(bizChatMessageService.deleteBizChatMessageByIds(ids));
    }
}
