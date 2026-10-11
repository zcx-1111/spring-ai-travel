package com.ruoyi.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 对话会话对象 biz_chat_session
 * 
 * @author ruoyi
 * @date 2026-09-19
 */
public class BizChatSession extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 用户ID（关联sys_user.id） */
    @Excel(name = "用户ID", readConverterExp = "关=联sys_user.id")
    private Long userId;

    /** 关联行程ID（关联biz_travel_plan.id，新建行程时可为空） */
    @Excel(name = "关联行程ID", readConverterExp = "关=联biz_travel_plan.id，新建行程时可为空")
    private Long planId;

    /** 会话标题 */
    @Excel(name = "会话标题")
    private String sessionTitle;

    /** 会话状态（0=进行中 1=已结束） */
    @Excel(name = "会话状态", readConverterExp = "0==进行中,1==已结束")
    private Integer sessionStatus;

    /** 最后消息时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "最后消息时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date lastMessageTime;

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

    public void setUserId(Long userId) 
    {
        this.userId = userId;
    }

    public Long getUserId() 
    {
        return userId;
    }

    public void setPlanId(Long planId) 
    {
        this.planId = planId;
    }

    public Long getPlanId() 
    {
        return planId;
    }

    public void setSessionTitle(String sessionTitle) 
    {
        this.sessionTitle = sessionTitle;
    }

    public String getSessionTitle() 
    {
        return sessionTitle;
    }

    public void setSessionStatus(Integer sessionStatus) 
    {
        this.sessionStatus = sessionStatus;
    }

    public Integer getSessionStatus() 
    {
        return sessionStatus;
    }

    public void setLastMessageTime(Date lastMessageTime) 
    {
        this.lastMessageTime = lastMessageTime;
    }

    public Date getLastMessageTime() 
    {
        return lastMessageTime;
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
            .append("userId", getUserId())
            .append("planId", getPlanId())
            .append("sessionTitle", getSessionTitle())
            .append("sessionStatus", getSessionStatus())
            .append("lastMessageTime", getLastMessageTime())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .append("delFlag", getDelFlag())
            .toString();
    }
}
