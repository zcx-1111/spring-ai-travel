package com.ruoyi.system.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 用户旅游偏好对象 biz_user_travel_preference
 * 
 * @author ruoyi
 * @date 2026-09-19
 */
public class BizUserTravelPreference extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 用户ID（关联sys_user.id） */
    @Excel(name = "用户ID", readConverterExp = "关=联sys_user.id")
    private Long userId;

    /** 默认出发城市 */
    @Excel(name = "默认出发城市")
    private String defaultDeparture;

    /** 默认预算等级（1=经济 2=舒适 3=奢华） */
    @Excel(name = "默认预算等级", readConverterExp = "1==经济,2==舒适,3==奢华")
    private Integer budgetLevel;

    /** 出行风格偏好（多选存字典编码，如亲子、情侣、老年、背包客） */
    @Excel(name = "出行风格偏好", readConverterExp = "多=选存字典编码，如亲子、情侣、老年、背包客")
    private String travelStyle;

    /** 景点类型偏好（多选存分类编码） */
    @Excel(name = "景点类型偏好", readConverterExp = "多=选存分类编码")
    private String scenicTypePrefer;

    /** 交通偏好（1=公共交通优先 2=打车优先 3=自驾） */
    @Excel(name = "交通偏好", readConverterExp = "1==公共交通优先,2==打车优先,3==自驾")
    private Integer trafficPrefer;

    /** 住宿偏好（1=经济酒店 2=舒适酒店 3=高端酒店） */
    @Excel(name = "住宿偏好", readConverterExp = "1==经济酒店,2==舒适酒店,3==高端酒店")
    private Integer accommodationPrefer;

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

    public void setDefaultDeparture(String defaultDeparture) 
    {
        this.defaultDeparture = defaultDeparture;
    }

    public String getDefaultDeparture() 
    {
        return defaultDeparture;
    }

    public void setBudgetLevel(Integer budgetLevel) 
    {
        this.budgetLevel = budgetLevel;
    }

    public Integer getBudgetLevel() 
    {
        return budgetLevel;
    }

    public void setTravelStyle(String travelStyle) 
    {
        this.travelStyle = travelStyle;
    }

    public String getTravelStyle() 
    {
        return travelStyle;
    }

    public void setScenicTypePrefer(String scenicTypePrefer) 
    {
        this.scenicTypePrefer = scenicTypePrefer;
    }

    public String getScenicTypePrefer() 
    {
        return scenicTypePrefer;
    }

    public void setTrafficPrefer(Integer trafficPrefer) 
    {
        this.trafficPrefer = trafficPrefer;
    }

    public Integer getTrafficPrefer() 
    {
        return trafficPrefer;
    }

    public void setAccommodationPrefer(Integer accommodationPrefer) 
    {
        this.accommodationPrefer = accommodationPrefer;
    }

    public Integer getAccommodationPrefer() 
    {
        return accommodationPrefer;
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
            .append("defaultDeparture", getDefaultDeparture())
            .append("budgetLevel", getBudgetLevel())
            .append("travelStyle", getTravelStyle())
            .append("scenicTypePrefer", getScenicTypePrefer())
            .append("trafficPrefer", getTrafficPrefer())
            .append("accommodationPrefer", getAccommodationPrefer())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .append("delFlag", getDelFlag())
            .toString();
    }
}
