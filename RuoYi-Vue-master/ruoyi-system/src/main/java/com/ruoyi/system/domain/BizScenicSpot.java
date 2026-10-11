package com.ruoyi.system.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 景点主对象 biz_scenic_spot
 * 
 * @author ruoyi
 * @date 2026-09-19
 */
public class BizScenicSpot extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 景点名称 */
    @Excel(name = "景点名称")
    private String spotName;

    /** 所属省份 */
    @Excel(name = "所属省份")
    private String province;

    /** 所属城市 */
    @Excel(name = "所属城市")
    private String city;

    /** 详细地址 */
    @Excel(name = "详细地址")
    private String address;

    /** 经度 */
    @Excel(name = "经度")
    private BigDecimal longitude;

    /** 纬度 */
    @Excel(name = "纬度")
    private BigDecimal latitude;

    /** 景点等级（如5A、4A、3A） */
    @Excel(name = "景点等级", readConverterExp = "如=5A、4A、3A")
    private String spotLevel;

    /** 建议游玩时长（小时） */
    @Excel(name = "建议游玩时长", readConverterExp = "小=时")
    private BigDecimal playHours;

    /** 成人票价（元） */
    @Excel(name = "成人票价", readConverterExp = "元=")
    private BigDecimal adultPrice;

    /** 儿童票价（元） */
    @Excel(name = "儿童票价", readConverterExp = "元=")
    private BigDecimal childPrice;

    /** 老人票价（元） */
    @Excel(name = "老人票价", readConverterExp = "元=")
    private BigDecimal oldPrice;

    /** 开放时间描述 */
    @Excel(name = "开放时间描述")
    private String openTimeDesc;

    /** 最佳游玩季节 */
    @Excel(name = "最佳游玩季节")
    private String bestSeason;

    /** 景点简介 */
    @Excel(name = "景点简介")
    private String spotIntro;

    /** 游玩须知 */
    @Excel(name = "游玩须知")
    private String playNotice;

    /** 封面图地址 */
    @Excel(name = "封面图地址")
    private String coverImg;

    /** 分类ID（关联biz_scenic_category.id） */
    @Excel(name = "分类ID", readConverterExp = "关=联biz_scenic_category.id")
    private Long categoryId;

    /** 软删除标记（0=未删除，1=已删除） */
    private Integer delFlag;

    /** 标签名称集合（逗号分隔，由标签关联表聚合查询得到，非主表字段，用于ES同步与详情展示） */
    private String tags;

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setSpotName(String spotName) 
    {
        this.spotName = spotName;
    }

    public String getSpotName() 
    {
        return spotName;
    }

    public void setProvince(String province) 
    {
        this.province = province;
    }

    public String getProvince() 
    {
        return province;
    }

    public void setCity(String city) 
    {
        this.city = city;
    }

    public String getCity() 
    {
        return city;
    }

    public void setAddress(String address) 
    {
        this.address = address;
    }

    public String getAddress() 
    {
        return address;
    }

    public void setLongitude(BigDecimal longitude) 
    {
        this.longitude = longitude;
    }

    public BigDecimal getLongitude() 
    {
        return longitude;
    }

    public void setLatitude(BigDecimal latitude) 
    {
        this.latitude = latitude;
    }

    public BigDecimal getLatitude() 
    {
        return latitude;
    }

    public void setSpotLevel(String spotLevel) 
    {
        this.spotLevel = spotLevel;
    }

    public String getSpotLevel() 
    {
        return spotLevel;
    }

    public void setPlayHours(BigDecimal playHours) 
    {
        this.playHours = playHours;
    }

    public BigDecimal getPlayHours() 
    {
        return playHours;
    }

    public void setAdultPrice(BigDecimal adultPrice) 
    {
        this.adultPrice = adultPrice;
    }

    public BigDecimal getAdultPrice() 
    {
        return adultPrice;
    }

    public void setChildPrice(BigDecimal childPrice) 
    {
        this.childPrice = childPrice;
    }

    public BigDecimal getChildPrice() 
    {
        return childPrice;
    }

    public void setOldPrice(BigDecimal oldPrice) 
    {
        this.oldPrice = oldPrice;
    }

    public BigDecimal getOldPrice() 
    {
        return oldPrice;
    }

    public void setOpenTimeDesc(String openTimeDesc) 
    {
        this.openTimeDesc = openTimeDesc;
    }

    public String getOpenTimeDesc() 
    {
        return openTimeDesc;
    }

    public void setBestSeason(String bestSeason) 
    {
        this.bestSeason = bestSeason;
    }

    public String getBestSeason() 
    {
        return bestSeason;
    }

    public void setSpotIntro(String spotIntro) 
    {
        this.spotIntro = spotIntro;
    }

    public String getSpotIntro() 
    {
        return spotIntro;
    }

    public void setPlayNotice(String playNotice) 
    {
        this.playNotice = playNotice;
    }

    public String getPlayNotice() 
    {
        return playNotice;
    }

    public void setCoverImg(String coverImg) 
    {
        this.coverImg = coverImg;
    }

    public String getCoverImg() 
    {
        return coverImg;
    }

    public void setCategoryId(Long categoryId) 
    {
        this.categoryId = categoryId;
    }

    public Long getCategoryId() 
    {
        return categoryId;
    }

    public void setDelFlag(Integer delFlag) 
    {
        this.delFlag = delFlag;
    }

    public Integer getDelFlag()
    {
        return delFlag;
    }

    public void setTags(String tags)
    {
        this.tags = tags;
    }

    public String getTags()
    {
        return tags;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("spotName", getSpotName())
            .append("province", getProvince())
            .append("city", getCity())
            .append("address", getAddress())
            .append("longitude", getLongitude())
            .append("latitude", getLatitude())
            .append("spotLevel", getSpotLevel())
            .append("playHours", getPlayHours())
            .append("adultPrice", getAdultPrice())
            .append("childPrice", getChildPrice())
            .append("oldPrice", getOldPrice())
            .append("openTimeDesc", getOpenTimeDesc())
            .append("bestSeason", getBestSeason())
            .append("spotIntro", getSpotIntro())
            .append("playNotice", getPlayNotice())
            .append("coverImg", getCoverImg())
            .append("categoryId", getCategoryId())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .append("delFlag", getDelFlag())
            .append("tags", getTags())
            .toString();
    }
}
