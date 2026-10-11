package com.ruoyi.system.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 行程每日详情对象 biz_travel_plan_day
 * 
 * @author ruoyi
 * @date 2026-09-19
 */
public class BizTravelPlanDay extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 行程ID（关联biz_travel_plan.id） */
    @Excel(name = "行程ID", readConverterExp = "关=联biz_travel_plan.id")
    private Long planId;

    /** 第几天 */
    @Excel(name = "第几天")
    private Long dayNum;

    /** 当日主题 */
    @Excel(name = "当日主题")
    private String dayTheme;

    /** 当日预算（元） */
    @Excel(name = "当日预算", readConverterExp = "元=")
    private BigDecimal dailyBudget;

    /** 住宿推荐 */
    @Excel(name = "住宿推荐")
    private String hotelRecommend;

    /** 当日交通方式描述 */
    @Excel(name = "当日交通方式描述")
    private String trafficDesc;

    /** 当日行程概述 */
    @Excel(name = "当日行程概述")
    private String daySummary;

    /** 当日注意事项 */
    @Excel(name = "当日注意事项")
    private String attention;

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

    public void setPlanId(Long planId) 
    {
        this.planId = planId;
    }

    public Long getPlanId() 
    {
        return planId;
    }

    public void setDayNum(Long dayNum) 
    {
        this.dayNum = dayNum;
    }

    public Long getDayNum() 
    {
        return dayNum;
    }

    public void setDayTheme(String dayTheme) 
    {
        this.dayTheme = dayTheme;
    }

    public String getDayTheme() 
    {
        return dayTheme;
    }

    public void setDailyBudget(BigDecimal dailyBudget) 
    {
        this.dailyBudget = dailyBudget;
    }

    public BigDecimal getDailyBudget() 
    {
        return dailyBudget;
    }

    public void setHotelRecommend(String hotelRecommend) 
    {
        this.hotelRecommend = hotelRecommend;
    }

    public String getHotelRecommend() 
    {
        return hotelRecommend;
    }

    public void setTrafficDesc(String trafficDesc) 
    {
        this.trafficDesc = trafficDesc;
    }

    public String getTrafficDesc() 
    {
        return trafficDesc;
    }

    public void setDaySummary(String daySummary) 
    {
        this.daySummary = daySummary;
    }

    public String getDaySummary() 
    {
        return daySummary;
    }

    public void setAttention(String attention) 
    {
        this.attention = attention;
    }

    public String getAttention() 
    {
        return attention;
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
            .append("planId", getPlanId())
            .append("dayNum", getDayNum())
            .append("dayTheme", getDayTheme())
            .append("dailyBudget", getDailyBudget())
            .append("hotelRecommend", getHotelRecommend())
            .append("trafficDesc", getTrafficDesc())
            .append("daySummary", getDaySummary())
            .append("attention", getAttention())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .append("delFlag", getDelFlag())
            .toString();
    }
}
