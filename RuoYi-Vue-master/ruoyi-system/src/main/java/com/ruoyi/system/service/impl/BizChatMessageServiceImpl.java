package com.ruoyi.system.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.mapper.BizChatMessageMapper;
import com.ruoyi.system.domain.BizChatMessage;
import com.ruoyi.system.service.IBizChatMessageService;

/**
 * 对话消息Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-09-19
 */
@Service
public class BizChatMessageServiceImpl implements IBizChatMessageService 
{
    @Autowired
    private BizChatMessageMapper bizChatMessageMapper;

    /**
     * 查询对话消息
     * 
     * @param id 对话消息主键
     * @return 对话消息
     */
    @Override
    public BizChatMessage selectBizChatMessageById(Long id)
    {
        return bizChatMessageMapper.selectBizChatMessageById(id);
    }

    /**
     * 查询对话消息列表
     * 
     * @param bizChatMessage 对话消息
     * @return 对话消息
     */
    @Override
    public List<BizChatMessage> selectBizChatMessageList(BizChatMessage bizChatMessage)
    {
        return bizChatMessageMapper.selectBizChatMessageList(bizChatMessage);
    }

    /**
     * 新增对话消息
     * 
     * @param bizChatMessage 对话消息
     * @return 结果
     */
    @Override
    public int insertBizChatMessage(BizChatMessage bizChatMessage)
    {
        bizChatMessage.setCreateTime(DateUtils.getNowDate());
        return bizChatMessageMapper.insertBizChatMessage(bizChatMessage);
    }

    /**
     * 修改对话消息
     * 
     * @param bizChatMessage 对话消息
     * @return 结果
     */
    @Override
    public int updateBizChatMessage(BizChatMessage bizChatMessage)
    {
        bizChatMessage.setUpdateTime(DateUtils.getNowDate());
        return bizChatMessageMapper.updateBizChatMessage(bizChatMessage);
    }

    /**
     * 批量删除对话消息
     * 
     * @param ids 需要删除的对话消息主键
     * @return 结果
     */
    @Override
    public int deleteBizChatMessageByIds(Long[] ids)
    {
        return bizChatMessageMapper.deleteBizChatMessageByIds(ids);
    }

    /**
     * 删除对话消息信息
     * 
     * @param id 对话消息主键
     * @return 结果
     */
    @Override
    public int deleteBizChatMessageById(Long id)
    {
        return bizChatMessageMapper.deleteBizChatMessageById(id);
    }
}
