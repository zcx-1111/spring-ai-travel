package com.ruoyi.system.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 每日行程景点关联对象 biz_travel_plan_day_spot
 * 
 * @author ruoyi
 * @date 2026-09-19
 */
public class BizTravelPlanDaySpot extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 行程天ID（关联biz_travel_plan_day.id） */
    @Excel(name = "行程天ID", readConverterExp = "关=联biz_travel_plan_day.id")
    private Long planDayId;

    /** 景点ID（关联biz_scenic_spot.id） */
    @Excel(name = "景点ID", readConverterExp = "关=联biz_scenic_spot.id")
    private Long spotId;

    /** 游玩顺序 */
    @Excel(name = "游玩顺序")
    private Long sortNum;

    /** 预计游玩时长（小时） */
    @Excel(name = "预计游玩时长", readConverterExp = "小=时")
    private BigDecimal playDuration;

    /** 门票花费（元） */
    @Excel(name = "门票花费", readConverterExp = "元=")
    private BigDecimal ticketCost;

    /** 游玩建议 */
    @Excel(name = "游玩建议")
    private String playTip;

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

    public void setPlanDayId(Long planDayId) 
    {
        this.planDayId = planDayId;
    }

    public Long getPlanDayId() 
    {
        return planDayId;
    }

    public void setSpotId(Long spotId) 
    {
        this.spotId = spotId;
    }

    public Long getSpotId() 
    {
        return spotId;
    }

    public void setSortNum(Long sortNum) 
    {
        this.sortNum = sortNum;
    }

    public Long getSortNum() 
    {
        return sortNum;
    }

    public void setPlayDuration(BigDecimal playDuration) 
    {
        this.playDuration = playDuration;
    }

    public BigDecimal getPlayDuration() 
    {
        return playDuration;
    }

    public void setTicketCost(BigDecimal ticketCost) 
    {
        this.ticketCost = ticketCost;
    }

    public BigDecimal getTicketCost() 
    {
        return ticketCost;
    }

    public void setPlayTip(String playTip) 
    {
        this.playTip = playTip;
    }

    public String getPlayTip() 
    {
        return playTip;
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
            .append("planDayId", getPlanDayId())
            .append("spotId", getSpotId())
            .append("sortNum", getSortNum())
            .append("playDuration", getPlayDuration())
            .append("ticketCost", getTicketCost())
            .append("playTip", getPlayTip())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .append("delFlag", getDelFlag())
            .toString();
    }
}
