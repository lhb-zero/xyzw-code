package com.ruoyi.club.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * OCR识别结果对象 club_ocr_result
 * 
 * @author lhb
 * @date 2025-11-19
 */
public class OcrResult extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 识别ID */
    private Long id;

    /** 图片文件名 */
    @Excel(name = "图片文件名")
    private String filename;

    /** 图片URL */
    private String imageUrl;

    /** 识别出的全部文本 */
    @Excel(name = "识别文本")
    private String text;

    /** 识别出的每一行文本 */
    private String[] lines;

    /** 处理耗时（秒） */
    @Excel(name = "处理耗时")
    private Double processingTime;

    /** 识别状态（processing/matched/failed） */
    private String status;

    /** OCR识别置信度 */
    @Excel(name = "OCR置信度")
    private Double confidenceScore;

    /** 是否已确认（0未确认，1已确认） */
    @Excel(name = "是否已确认")
    private Integer isConfirmed;

    /** 关联的战绩记录ID */
    @Excel(name = "关联战绩记录ID")
    private Long confirmedRecordId;

    /** OCR引擎版本 */
    @Excel(name = "OCR引擎版本")
    private String ocrEngineVersion;

    /** 重试次数 */
    @Excel(name = "重试次数")
    private Integer retryCount;

    /** 备注信息 */
    private String remark;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setFilename(String filename) 
    {
        this.filename = filename;
    }

    public String getFilename() 
    {
        return filename;
    }

    public void setImageUrl(String imageUrl) 
    {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() 
    {
        return imageUrl;
    }

    public void setText(String text) 
    {
        this.text = text;
    }

    public String getText() 
    {
        return text;
    }

    public void setLines(String[] lines) 
    {
        this.lines = lines;
    }

    public String[] getLines() 
    {
        return lines;
    }

    public void setProcessingTime(Double processingTime) 
    {
        this.processingTime = processingTime;
    }

    public Double getProcessingTime() 
    {
        return processingTime;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    public void setRemark(String remark) 
    {
        this.remark = remark;
    }

    public String getRemark() 
    {
        return remark;
    }

    public void setConfidenceScore(Double confidenceScore) 
    {
        this.confidenceScore = confidenceScore;
    }

    public Double getConfidenceScore() 
    {
        return confidenceScore;
    }

    public void setIsConfirmed(Integer isConfirmed) 
    {
        this.isConfirmed = isConfirmed;
    }

    public Integer getIsConfirmed() 
    {
        return isConfirmed;
    }

    public void setConfirmedRecordId(Long confirmedRecordId) 
    {
        this.confirmedRecordId = confirmedRecordId;
    }

    public Long getConfirmedRecordId() 
    {
        return confirmedRecordId;
    }

    public void setOcrEngineVersion(String ocrEngineVersion) 
    {
        this.ocrEngineVersion = ocrEngineVersion;
    }

    public String getOcrEngineVersion() 
    {
        return ocrEngineVersion;
    }

    public void setRetryCount(Integer retryCount) 
    {
        this.retryCount = retryCount;
    }

    public Integer getRetryCount() 
    {
        return retryCount;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("filename", getFilename())
            .append("imageUrl", getImageUrl())
            .append("text", getText())
            .append("lines", getLines())
            .append("processingTime", getProcessingTime())
            .append("status", getStatus())
            .append("confidenceScore", getConfidenceScore())
            .append("isConfirmed", getIsConfirmed())
            .append("confirmedRecordId", getConfirmedRecordId())
            .append("ocrEngineVersion", getOcrEngineVersion())
            .append("retryCount", getRetryCount())
            .append("remark", getRemark())
            .toString();
    }
}