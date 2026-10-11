package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.BizChatMessage;

/**
 * 对话消息Service接口
 * 
 * @author ruoyi
 * @date 2026-09-19
 */
public interface IBizChatMessageService 
{
    /**
     * 查询对话消息
     * 
     * @param id 对话消息主键
     * @return 对话消息
     */
    public BizChatMessage selectBizChatMessageById(Long id);

    /**
     * 查询对话消息列表
     * 
     * @param bizChatMessage 对话消息
     * @return 对话消息集合
     */
    public List<BizChatMessage> selectBizChatMessageList(BizChatMessage bizChatMessage);

    /**
     * 新增对话消息
     * 
     * @param bizChatMessage 对话消息
     * @return 结果
     */
    public int insertBizChatMessage(BizChatMessage bizChatMessage);

    /**
     * 修改对话消息
     * 
     * @param bizChatMessage 对话消息
     * @return 结果
     */
    public int updateBizChatMessage(BizChatMessage bizChatMessage);

    /**
     * 批量删除对话消息
     * 
     * @param ids 需要删除的对话消息主键集合
     * @return 结果
     */
    public int deleteBizChatMessageByIds(Long[] ids);

    /**
     * 删除对话消息信息
     * 
     * @param id 对话消息主键
     * @return 结果
     */
    public int deleteBizChatMessageById(Long id);
}
