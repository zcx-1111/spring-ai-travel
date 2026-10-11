package com.ruoyi.system.domain.dto;

import java.util.List;

/**
 * 前台AI行程生成请求参数
 * 对应前端 /travel/plan 页面表单提交
 *
 * @author ruoyi
 * @date 2026-09-21
 */
public class TourPlanGenerateDTO
{
    /** 目的地，如：桂林、张家界 */
    private String destination;

    /** 出行开始日期，格式 yyyy-MM-dd */
    private String startDate;

    /** 出行天数（1~30） */
    private Integer days;

    /** 预算区间：经济型 / 中等 / 高端 */
    private String budget;

    /** 出行人群：单人 / 情侣 / 家庭亲子 / 朋友结伴 */
    private String crowd;

    /** 出行偏好：自然风光、美食探店、人文古迹、购物休闲、小众冷门 */
    private List<String> preferences;

    /** 额外备注（可空） */
    private String remark;

    public String getDestination()
    {
        return destination;
    }

    public void setDestination(String destination)
    {
        this.destination = destination;
    }

    public String getStartDate()
    {
        return startDate;
    }

    public void setStartDate(String startDate)
    {
        this.startDate = startDate;
    }

    public Integer getDays()
    {
        return days;
    }

    public void setDays(Integer days)
    {
        this.days = days;
    }

    public String getBudget()
    {
        return budget;
    }

    public void setBudget(String budget)
    {
        this.budget = budget;
    }

    public String getCrowd()
    {
        return crowd;
    }

    public void setCrowd(String crowd)
    {
        this.crowd = crowd;
    }

    public List<String> getPreferences()
    {
        return preferences;
    }

    public void setPreferences(List<String> preferences)
    {
        this.preferences = preferences;
    }

    public String getRemark()
    {
        return remark;
    }

    public void setRemark(String remark)
    {
        this.remark = remark;
    }
}
