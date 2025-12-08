package com.ruoyi.club.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * OCR规则配置对象 ocr_rule_config
 * 
 * @author lhb
 * @date 2025-11-19
 */
public class OcrRuleConfig extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 识别关键词 */
    @Excel(name = "识别关键词")
    private String keyword;

    /** 目标字段名 */
    @Excel(name = "目标字段名")
    private String targetField;

    /** 字段类型 */
    @Excel(name = "字段类型", readConverterExp = "int=整数,decimal=小数,string=字符串")
    private String fieldType;

    /** 是否启用 */
    @Excel(name = "是否启用", readConverterExp = "0=否,1=是")
    private Long isActive;

    /** 排序序号 */
    @Excel(name = "排序序号")
    private Long sortOrder;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "创建时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date createdAt;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setKeyword(String keyword) 
    {
        this.keyword = keyword;
    }

    public String getKeyword() 
    {
        return keyword;
    }

    public void setTargetField(String targetField) 
    {
        this.targetField = targetField;
    }

    public String getTargetField() 
    {
        return targetField;
    }

    public void setFieldType(String fieldType) 
    {
        this.fieldType = fieldType;
    }

    public String getFieldType() 
    {
        return fieldType;
    }

    public void setIsActive(Long isActive) 
    {
        this.isActive = isActive;
    }

    public Long getIsActive() 
    {
        return isActive;
    }

    public void setSortOrder(Long sortOrder) 
    {
        this.sortOrder = sortOrder;
    }

    public Long getSortOrder() 
    {
        return sortOrder;
    }

    public void setCreatedAt(Date createdAt) 
    {
        this.createdAt = createdAt;
    }

    public Date getCreatedAt() 
    {
        return createdAt;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("keyword", getKeyword())
            .append("targetField", getTargetField())
            .append("fieldType", getFieldType())
            .append("isActive", getIsActive())
            .append("sortOrder", getSortOrder())
            .append("createdAt", getCreatedAt())
            .toString();
    }
}