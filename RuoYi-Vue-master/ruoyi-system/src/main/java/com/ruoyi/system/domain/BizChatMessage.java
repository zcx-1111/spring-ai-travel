package com.ruoyi.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 对话消息对象 biz_chat_message
 * 
 * @author ruoyi
 * @date 2026-09-19
 */
public class BizChatMessage extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 会话ID（关联biz_chat_session.id） */
    @Excel(name = "会话ID", readConverterExp = "关=联biz_chat_session.id")
    private Long sessionId;

    /** 用户ID（关联sys_user.id） */
    @Excel(name = "用户ID", readConverterExp = "关=联sys_user.id")
    private Long userId;

    /** 消息角色（user=用户提问 assistant=AI回复） */
    @Excel(name = "消息角色", readConverterExp = "u=ser=用户提问,a=ssistant=AI回复")
    private String messageRole;

    /** 消息类型（1=纯文本 2=行程方案卡片 3=调整确认） */
    @Excel(name = "消息类型", readConverterExp = "1==纯文本,2==行程方案卡片,3==调整确认")
    private Integer messageType;

    /** 消息内容 */
    @Excel(name = "消息内容")
    private String messageContent;

    /** 发送时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "发送时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date sendTime;

    /** 本次消息token消耗量 */
    @Excel(name = "本次消息token消耗量")
    private Long tokenUsage;

    /** 软删除标记（0=未删除，1=已删除） */
    private Integer delFlag;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setSessionId(Long sessionId) 
    {
        this.sessionId = sessionId;
    }

    public Long getSessionId() 
    {
        return sessionId;
    }

    public void setUserId(Long userId) 
    {
        this.userId = userId;
    }

    public Long getUserId() 
    {
        return userId;
    }

    public void setMessageRole(String messageRole) 
    {
        this.messageRole = messageRole;
    }

    public String getMessageRole() 
    {
        return messageRole;
    }

    public void setMessageType(Integer messageType) 
    {
        this.messageType = messageType;
    }

    public Integer getMessageType() 
    {
        return messageType;
    }

    public void setMessageContent(String messageContent) 
    {
        this.messageContent = messageContent;
    }

    public String getMessageContent() 
    {
        return messageContent;
    }

    public void setSendTime(Date sendTime) 
    {
        this.sendTime = sendTime;
    }

    public Date getSendTime() 
    {
        return sendTime;
    }

    public void setTokenUsage(Long tokenUsage) 
    {
        this.tokenUsage = tokenUsage;
    }

    public Long getTokenUsage() 
    {
        return tokenUsage;
    }

    public void setDelFlag(Integer delFlag) 
    {
        this.delFlag = delFlag;
    }

    public Integer getDelFlag() 
    {
        return delFlag;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("sessionId", getSessionId())
            .append("userId", getUserId())
            .append("messageRole", getMessageRole())
            .append("messageType", getMessageType())
            .append("messageContent", getMessageContent())
            .append("sendTime", getSendTime())
            .append("tokenUsage", getTokenUsage())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .append("delFlag", getDelFlag())
            .toString();
    }
}
