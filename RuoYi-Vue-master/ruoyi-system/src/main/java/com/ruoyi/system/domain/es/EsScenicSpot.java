package com.ruoyi.system.domain.es;

import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;

/**
 * 景点 ES 索引实体（索引名 scenic_spot）
 *
 * <p>字段映射说明（中文分词使用 IK 分词器，需在 ES 中安装 analysis-ik 插件）：</p>
 * <ul>
 *   <li>spotName / city / tags：text 类型（ik_max_word 建索引、ik_smart 检索），
 *       并带 keyword 子字段，keyword 子字段用于城市精确过滤（terms）与名称精确匹配加权；</li>
 *   <li>address / spotIntro：纯 text，参与全文检索但权重较低；</li>
 *   <li>spotLevel：keyword，用于展示与过滤；</li>
 *   <li>levelRank：景区等级数值权重（5A=50、4A=40…），用于"质量优先"排序；</li>
 *   <li>adultPrice：double，支持按票价排序。</li>
 * </ul>
 *
 * <p><b>createIndex=false</b>：不在应用启动时自动建索引（ES 未启动不影响后端启动），
 * 索引由 {@code EsScenicSpotService#initIndex()} 在首次全量同步时按需创建并写入映射。</p>
 *
 * @author ruoyi
 * @date 2026-10-05
 */
@Document(indexName = "scenic_spot", createIndex = false)
public class EsScenicSpot
{
    /** 文档ID，直接使用MySQL景点ID（字符串形式），保证幂等同步 */
    @Id
    private String id;

    /** 景点名称：高权重全文检索字段，带keyword子字段支持精确匹配 */
    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart"),
            otherFields = { @InnerField(suffix = "keyword", type = FieldType.Keyword) }
    )
    private String spotName;

    /** 所属省份：精确过滤/聚合用 */
    @Field(type = FieldType.Keyword)
    private String province;

    /** 所属城市：支持分词检索，keyword子字段用于城市硬过滤（杜绝跨城误召回） */
    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart"),
            otherFields = { @InnerField(suffix = "keyword", type = FieldType.Keyword) }
    )
    private String city;

    /** 详细地址：低权重全文检索 */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String address;

    /** 标签名集合：中等权重全文检索，带keyword子字段 */
    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart"),
            otherFields = { @InnerField(suffix = "keyword", type = FieldType.Keyword) }
    )
    private List<String> tags;

    /** 景点简介：低权重全文检索（内容最长，权重在查询时控制） */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String spotIntro;

    /** 景点等级（5A/4A/3A…），keyword 不分词 */
    @Field(type = FieldType.Keyword)
    private String spotLevel;

    /** 等级数值权重：5A=50、4A=40、3A=30、2A=20、1A=10、其余=0，用于质量优先排序 */
    @Field(type = FieldType.Integer)
    private Integer levelRank;

    /** 成人票价（元），支持按票价排序与区间过滤 */
    @Field(type = FieldType.Double)
    private Double adultPrice;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getSpotName()
    {
        return spotName;
    }

    public void setSpotName(String spotName)
    {
        this.spotName = spotName;
    }

    public String getProvince()
    {
        return province;
    }

    public void setProvince(String province)
    {
        this.province = province;
    }

    public String getCity()
    {
        return city;
    }

    public void setCity(String city)
    {
        this.city = city;
    }

    public String getAddress()
    {
        return address;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }

    public List<String> getTags()
    {
        return tags;
    }

    public void setTags(List<String> tags)
    {
        this.tags = tags;
    }

    public String getSpotIntro()
    {
        return spotIntro;
    }

    public void setSpotIntro(String spotIntro)
    {
        this.spotIntro = spotIntro;
    }

    public String getSpotLevel()
    {
        return spotLevel;
    }

    public void setSpotLevel(String spotLevel)
    {
        this.spotLevel = spotLevel;
    }

    public Integer getLevelRank()
    {
        return levelRank;
    }

    public void setLevelRank(Integer levelRank)
    {
        this.levelRank = levelRank;
    }

    public Double getAdultPrice()
    {
        return adultPrice;
    }

    public void setAdultPrice(Double adultPrice)
    {
        this.adultPrice = adultPrice;
    }
}
