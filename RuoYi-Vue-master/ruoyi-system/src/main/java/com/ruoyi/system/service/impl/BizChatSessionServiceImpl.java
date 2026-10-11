package com.ruoyi.system.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.mapper.BizChatSessionMapper;
import com.ruoyi.system.domain.BizChatSession;
import com.ruoyi.system.service.IBizChatSessionService;

/**
 * 对话会话Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-09-19
 */
@Service
public class BizChatSessionServiceImpl implements IBizChatSessionService 
{
    @Autowired
    private BizChatSessionMapper bizChatSessionMapper;

    /**
     * 查询对话会话
     * 
     * @param id 对话会话主键
     * @return 对话会话
     */
    @Override
    public BizChatSession selectBizChatSessionById(Long id)
    {
        return bizChatSessionMapper.selectBizChatSessionById(id);
    }

    /**
     * 查询对话会话列表
     * 
     * @param bizChatSession 对话会话
     * @return 对话会话
     */
    @Override
    public List<BizChatSession> selectBizChatSessionList(BizChatSession bizChatSession)
    {
        return bizChatSessionMapper.selectBizChatSessionList(bizChatSession);
    }

    /**
     * 新增对话会话
     * 
     * @param bizChatSession 对话会话
     * @return 结果
     */
    @Override
    public int insertBizChatSession(BizChatSession bizChatSession)
    {
        bizChatSession.setCreateTime(DateUtils.getNowDate());
        return bizChatSessionMapper.insertBizChatSession(bizChatSession);
    }

    /**
     * 修改对话会话
     * 
     * @param bizChatSession 对话会话
     * @return 结果
     */
    @Override
    public int updateBizChatSession(BizChatSession bizChatSession)
    {
        bizChatSession.setUpdateTime(DateUtils.getNowDate());
        return bizChatSessionMapper.updateBizChatSession(bizChatSession);
    }

    /**
     * 批量删除对话会话
     * 
     * @param ids 需要删除的对话会话主键
     * @return 结果
     */
    @Override
    public int deleteBizChatSessionByIds(Long[] ids)
    {
        return bizChatSessionMapper.deleteBizChatSessionByIds(ids);
    }

    /**
     * 删除对话会话信息
     * 
     * @param id 对话会话主键
     * @return 结果
     */
    @Override
    public int deleteBizChatSessionById(Long id)
    {
        return bizChatSessionMapper.deleteBizChatSessionById(id);
    }
}
