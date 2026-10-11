package com.ruoyi.system.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 行程方案主对象 biz_travel_plan
 * 
 * @author ruoyi
 * @date 2026-09-19
 */
public class BizTravelPlan extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 用户ID（关联sys_user.id） */
    @Excel(name = "用户ID", readConverterExp = "关=联sys_user.id")
    private Long userId;

    /** 出发城市 */
    @Excel(name = "出发城市")
    private String departureCity;

    /** 目的地城市 */
    @Excel(name = "目的地城市")
    private String destinationCity;

    /** 出行天数 */
    @Excel(name = "出行天数")
    private Long travelDays;

    /** 开始日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "开始日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date startDate;

    /** 结束日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "结束日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date endDate;

    /** 总预算（元） */
    @Excel(name = "总预算", readConverterExp = "元=")
    private BigDecimal totalBudget;

    /** 出行人数 */
    @Excel(name = "出行人数")
    private Long peopleCount;

    /** 出行人员类型（亲子/情侣/老年等） */
    @Excel(name = "出行人员类型", readConverterExp = "亲=子/情侣/老年等")
    private String peopleType;

    /** 消费等级（1=经济 2=舒适 3=奢华） */
    @Excel(name = "消费等级", readConverterExp = "1==经济,2==舒适,3==奢华")
    private Integer consumptionLevel;

    /** 交通偏好（1=公共交通优先 2=打车优先 3=自驾） */
    @Excel(name = "交通偏好", readConverterExp = "1==公共交通优先,2==打车优先,3==自驾")
    private Integer trafficPrefer;

    /** 住宿偏好（1=经济酒店 2=舒适酒店 3=高端酒店） */
    @Excel(name = "住宿偏好", readConverterExp = "1==经济酒店,2==舒适酒店,3==高端酒店")
    private Integer accommodationPrefer;

    /** 景点类型偏好（多选存分类编码） */
    @Excel(name = "景点类型偏好", readConverterExp = "多=选存分类编码")
    private String scenicTypePrefer;

    /** 行程标题 */
    @Excel(name = "行程标题")
    private String planTitle;

    /** 行程摘要 */
    @Excel(name = "行程摘要")
    private String planSummary;

    /** 方案来源（1=AI自动生成 2=对话调整后 3=手动修改） */
    @Excel(name = "方案来源", readConverterExp = "1==AI自动生成,2==对话调整后,3==手动修改")
    private Integer planSource;

    /** 行程状态（0=草稿 1=已确认 2=已完成 3=已取消） */
    @Excel(name = "行程状态", readConverterExp = "0==草稿,1==已确认,2==已完成,3==已取消")
    private Integer planStatus;

    /** 预计总花费（元） */
    @Excel(name = "预计总花费", readConverterExp = "元=")
    private BigDecimal totalCost;

    /** 景点总数 */
    @Excel(name = "景点总数")
    private Long spotCount;

    /** 所用AI模型 */
    @Excel(name = "所用AI模型")
    private String aiModel;

    /** 生成耗时（秒） */
    @Excel(name = "生成耗时", readConverterExp = "秒=")
    private Long generationTime;

    /** 生成提示词快照 */
    @Excel(name = "生成提示词快照")
    private String promptSnapshot;

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

    public void setDepartureCity(String departureCity) 
    {
        this.departureCity = departureCity;
    }

    public String getDepartureCity() 
    {
        return departureCity;
    }

    public void setDestinationCity(String destinationCity) 
    {
        this.destinationCity = destinationCity;
    }

    public String getDestinationCity() 
    {
        return destinationCity;
    }

    public void setTravelDays(Long travelDays) 
    {
        this.travelDays = travelDays;
    }

    public Long getTravelDays() 
    {
        return travelDays;
    }

    public void setStartDate(Date startDate) 
    {
        this.startDate = startDate;
    }

    public Date getStartDate() 
    {
        return startDate;
    }

    public void setEndDate(Date endDate) 
    {
        this.endDate = endDate;
    }

    public Date getEndDate() 
    {
        return endDate;
    }

    public void setTotalBudget(BigDecimal totalBudget) 
    {
        this.totalBudget = totalBudget;
    }

    public BigDecimal getTotalBudget() 
    {
        return totalBudget;
    }

    public void setPeopleCount(Long peopleCount) 
    {
        this.peopleCount = peopleCount;
    }

    public Long getPeopleCount() 
    {
        return peopleCount;
    }

    public void setPeopleType(String peopleType) 
    {
        this.peopleType = peopleType;
    }

    public String getPeopleType() 
    {
        return peopleType;
    }

    public void setConsumptionLevel(Integer consumptionLevel) 
    {
        this.consumptionLevel = consumptionLevel;
    }

    public Integer getConsumptionLevel() 
    {
        return consumptionLevel;
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

    public void setScenicTypePrefer(String scenicTypePrefer) 
    {
        this.scenicTypePrefer = scenicTypePrefer;
    }

    public String getScenicTypePrefer() 
    {
        return scenicTypePrefer;
    }

    public void setPlanTitle(String planTitle) 
    {
        this.planTitle = planTitle;
    }

    public String getPlanTitle() 
    {
        return planTitle;
    }

    public void setPlanSummary(String planSummary) 
    {
        this.planSummary = planSummary;
    }

    public String getPlanSummary() 
    {
        return planSummary;
    }

    public void setPlanSource(Integer planSource) 
    {
        this.planSource = planSource;
    }

    public Integer getPlanSource() 
    {
        return planSource;
    }

    public void setPlanStatus(Integer planStatus) 
    {
        this.planStatus = planStatus;
    }

    public Integer getPlanStatus() 
    {
        return planStatus;
    }

    public void setTotalCost(BigDecimal totalCost) 
    {
        this.totalCost = totalCost;
    }

    public BigDecimal getTotalCost() 
    {
        return totalCost;
    }

    public void setSpotCount(Long spotCount) 
    {
        this.spotCount = spotCount;
    }

    public Long getSpotCount() 
    {
        return spotCount;
    }

    public void setAiModel(String aiModel) 
    {
        this.aiModel = aiModel;
    }

    public String getAiModel() 
    {
        return aiModel;
    }

    public void setGenerationTime(Long generationTime) 
    {
        this.generationTime = generationTime;
    }

    public Long getGenerationTime() 
    {
        return generationTime;
    }

    public void setPromptSnapshot(String promptSnapshot) 
    {
        this.promptSnapshot = promptSnapshot;
    }

    public String getPromptSnapshot() 
    {
        return promptSnapshot;
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
            .append("departureCity", getDepartureCity())
            .append("destinationCity", getDestinationCity())
            .append("travelDays", getTravelDays())
            .append("startDate", getStartDate())
            .append("endDate", getEndDate())
            .append("totalBudget", getTotalBudget())
            .append("peopleCount", getPeopleCount())
            .append("peopleType", getPeopleType())
            .append("consumptionLevel", getConsumptionLevel())
            .append("trafficPrefer", getTrafficPrefer())
            .append("accommodationPrefer", getAccommodationPrefer())
            .append("scenicTypePrefer", getScenicTypePrefer())
            .append("planTitle", getPlanTitle())
            .append("planSummary", getPlanSummary())
            .append("planSource", getPlanSource())
            .append("planStatus", getPlanStatus())
            .append("totalCost", getTotalCost())
            .append("spotCount", getSpotCount())
            .append("aiModel", getAiModel())
            .append("generationTime", getGenerationTime())
            .append("promptSnapshot", getPromptSnapshot())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .append("delFlag", getDelFlag())
            .toString();
    }
}
