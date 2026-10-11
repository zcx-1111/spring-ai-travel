package com.ruoyi.system.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 景点标签对象 biz_scenic_tag
 * 
 * @author ruoyi
 * @date 2026-09-19
 */
public class BizScenicTag extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 标签名称 */
    @Excel(name = "标签名称")
    private String tagName;

    /** 标签颜色（十六进制色值） */
    @Excel(name = "标签颜色", readConverterExp = "十=六进制色值")
    private String tagColor;

    /** 排序 */
    @Excel(name = "排序")
    private Long sort;

    /** 状态（0=停用 1=启用） */
    @Excel(name = "状态", readConverterExp = "0==停用,1==启用")
    private Integer status;

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

    public void setTagName(String tagName) 
    {
        this.tagName = tagName;
    }

    public String getTagName() 
    {
        return tagName;
    }

    public void setTagColor(String tagColor) 
    {
        this.tagColor = tagColor;
    }

    public String getTagColor() 
    {
        return tagColor;
    }

    public void setSort(Long sort) 
    {
        this.sort = sort;
    }

    public Long getSort() 
    {
        return sort;
    }

    public void setStatus(Integer status) 
    {
        this.status = status;
    }

    public Integer getStatus() 
    {
        return status;
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
            .append("tagName", getTagName())
            .append("tagColor", getTagColor())
            .append("sort", getSort())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .append("delFlag", getDelFlag())
            .toString();
    }
}
