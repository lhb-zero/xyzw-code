package com.ruoyi.club.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 盐场战绩记录对象 salt_field_record
 * 
 * @author lhb
 * @date 2025-11-18
 */
public class SaltFieldRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 关联成员ID */
    @Excel(name = "关联成员ID")
    private Long memberId;

    /** 活动日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "活动日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date recordDate;

    /** 杀敌数 */
    @Excel(name = "杀敌数")
    private Long kills;

    /** 死亡数 */
    @Excel(name = "死亡数")
    private Long deaths;

    /** 刨地/刨击数 */
    @Excel(name = "刨地/刨击数")
    private Long digs;

    /** 复活丹使用次数 */
    @Excel(name = "复活丹使用次数")
    private Long revives;

    /** KD战损比例 */
    @Excel(name = "KD战损比例")
    private BigDecimal kdRatio;

    /** 关联OCR识别记录ID */
    @Excel(name = "关联OCR识别记录ID")
    private Long ocrRecordId;

    /** 截图存储路径 */
    @Excel(name = "截图存储路径")
    private String imageUrl;

    /** 数据来源 */
    @Excel(name = "数据来源")
    private String dataSource;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "创建时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date createdAt;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
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
    public void setKills(Long kills) 
    {
        this.kills = kills;
    }

    public Long getKills() 
    {
        return kills;
    }
    public void setDeaths(Long deaths) 
    {
        this.deaths = deaths;
    }

    public Long getDeaths() 
    {
        return deaths;
    }
    public void setDigs(Long digs) 
    {
        this.digs = digs;
    }

    public Long getDigs() 
    {
        return digs;
    }
    public void setRevives(Long revives) 
    {
        this.revives = revives;
    }

    public Long getRevives() 
    {
        return revives;
    }
    public void setKdRatio(BigDecimal kdRatio) 
    {
        this.kdRatio = kdRatio;
    }

    public BigDecimal getKdRatio() 
    {
        return kdRatio;
    }
    public void setOcrRecordId(Long ocrRecordId) 
    {
        this.ocrRecordId = ocrRecordId;
    }

    public Long getOcrRecordId() 
    {
        return ocrRecordId;
    }
    public void setImageUrl(String imageUrl) 
    {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() 
    {
        return imageUrl;
    }
    public void setDataSource(String dataSource) 
    {
        this.dataSource = dataSource;
    }

    public String getDataSource() 
    {
        return dataSource;
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
            .append("memberId", getMemberId())
            .append("recordDate", getRecordDate())
            .append("kills", getKills())
            .append("deaths", getDeaths())
            .append("digs", getDigs())
            .append("revives", getRevives())
            .append("kdRatio", getKdRatio())
            .append("ocrRecordId", getOcrRecordId())
            .append("imageUrl", getImageUrl())
            .append("dataSource", getDataSource())
            .append("createdAt", getCreatedAt())
            .toString();
    }
}
