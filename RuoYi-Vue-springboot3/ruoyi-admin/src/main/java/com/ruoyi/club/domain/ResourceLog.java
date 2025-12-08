package com.ruoyi.club.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 资源日志对象 resource_log
 * 
 * @author lhb
 * @date 2025-11-19
 */
public class ResourceLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 日志标题 */
    @Excel(name = "日志标题")
    private String logTitle;

    /** 日志类型 */
    @Excel(name = "日志类型", readConverterExp = "personal=个人,club=俱乐部,member=成员")
    private String logType;

    /** 关联成员ID */
    @Excel(name = "关联成员ID")
    private Long memberId;

    /** 记录日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "记录日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date recordDate;

    /** 资源数据 */
    private String resourceData;

    /** 备注说明 */
    private String notes;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "创建时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date createdAt;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "更新时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date updatedAt;

    /** 成员信息 */
    private ClubMember memberInfo;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setLogTitle(String logTitle) 
    {
        this.logTitle = logTitle;
    }

    public String getLogTitle() 
    {
        return logTitle;
    }

    public void setLogType(String logType) 
    {
        this.logType = logType;
    }

    public String getLogType() 
    {
        return logType;
    }

    public void setMemberId(Long memberId) 
    {
        this.memberId = memberId;
    }

    public Long getMemberId() 
    {
        return memberId;
    }

    public void setRecordDate(Date recordDate) 
    {
        this.recordDate = recordDate;
    }

    public Date getRecordDate() 
    {
        return recordDate;
    }

    public void setResourceData(String resourceData) 
    {
        this.resourceData = resourceData;
    }

    public String getResourceData() 
    {
        return resourceData;
    }

    public void setNotes(String notes) 
    {
        this.notes = notes;
    }

    public String getNotes() 
    {
        return notes;
    }

    public void setCreatedAt(Date createdAt) 
    {
        this.createdAt = createdAt;
    }

    public Date getCreatedAt() 
    {
        return createdAt;
    }

    public void setUpdatedAt(Date updatedAt) 
    {
        this.updatedAt = updatedAt;
    }

    public Date getUpdatedAt() 
    {
        return updatedAt;
    }

    public ClubMember getMemberInfo() 
    {
        return memberInfo;
    }

    public void setMemberInfo(ClubMember memberInfo) 
    {
        this.memberInfo = memberInfo;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("logTitle", getLogTitle())
            .append("logType", getLogType())
            .append("memberId", getMemberId())
            .append("recordDate", getRecordDate())
            .append("resourceData", getResourceData())
            .append("notes", getNotes())
            .append("createdAt", getCreatedAt())
            .append("updatedAt", getUpdatedAt())
            .append("memberInfo", getMemberInfo())
            .toString();
    }
}