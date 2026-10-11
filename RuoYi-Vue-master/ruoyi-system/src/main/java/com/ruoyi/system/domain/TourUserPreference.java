package com.ruoyi.system.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 用户旅游偏好对象 tour_user_preference
 * 
 * @author ruoyi
 * @date 2026-09-19
 */
public class TourUserPreference extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 关联游客用户id */
    @Excel(name = "关联游客用户id")
    private Long userId;

    /** 预算偏好 */
    @Excel(name = "预算偏好")
    private String budget;

    /** 偏好出行天数 */
    @Excel(name = "偏好出行天数")
    private Long travelDays;

    /** 出行类型偏好（亲子/徒步/古迹等，逗号分隔） */
    @Excel(name = "出行类型偏好", readConverterExp = "亲=子/徒步/古迹等，逗号分隔")
    private String travelType;

    /** 偏好出行季节 */
    @Excel(name = "偏好出行季节")
    private String preferredSeason;

    /** 出行人群：单人/情侣/家庭 */
    @Excel(name = "出行人群：单人/情侣/家庭")
    private String crowd;

    /** 逻辑删除 */
    private Long delFlag;

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

    public void setBudget(String budget) 
    {
        this.budget = budget;
    }

    public String getBudget() 
    {
        return budget;
    }

    public void setTravelDays(Long travelDays) 
    {
        this.travelDays = travelDays;
    }

    public Long getTravelDays() 
    {
        return travelDays;
    }

    public void setTravelType(String travelType) 
    {
        this.travelType = travelType;
    }

    public String getTravelType() 
    {
        return travelType;
    }

    public void setPreferredSeason(String preferredSeason) 
    {
        this.preferredSeason = preferredSeason;
    }

    public String getPreferredSeason() 
    {
        return preferredSeason;
    }

    public void setCrowd(String crowd) 
    {
        this.crowd = crowd;
    }

    public String getCrowd() 
    {
        return crowd;
    }

    public void setDelFlag(Long delFlag) 
    {
        this.delFlag = delFlag;
    }

    public Long getDelFlag() 
    {
        return delFlag;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("userId", getUserId())
            .append("budget", getBudget())
            .append("travelDays", getTravelDays())
            .append("travelType", getTravelType())
            .append("preferredSeason", getPreferredSeason())
            .append("crowd", getCrowd())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .append("delFlag", getDelFlag())
            .toString();
    }
}
