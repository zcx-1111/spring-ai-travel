package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.BizChatSession;

/**
 * 对话会话Mapper接口
 * 
 * @author ruoyi
 * @date 2026-09-19
 */
public interface BizChatSessionMapper 
{
    /**
     * 查询对话会话
     * 
     * @param id 对话会话主键
     * @return 对话会话
     */
    public BizChatSession selectBizChatSessionById(Long id);

    /**
     * 查询对话会话列表
     * 
     * @param bizChatSession 对话会话
     * @return 对话会话集合
     */
    public List<BizChatSession> selectBizChatSessionList(BizChatSession bizChatSession);

    /**
     * 新增对话会话
     * 
     * @param bizChatSession 对话会话
     * @return 结果
     */
    public int insertBizChatSession(BizChatSession bizChatSession);

    /**
     * 修改对话会话
     * 
     * @param bizChatSession 对话会话
     * @return 结果
     */
    public int updateBizChatSession(BizChatSession bizChatSession);

    /**
     * 删除对话会话
     * 
     * @param id 对话会话主键
     * @return 结果
     */
    public int deleteBizChatSessionById(Long id);

    /**
     * 批量删除对话会话
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteBizChatSessionByIds(Long[] ids);
}
