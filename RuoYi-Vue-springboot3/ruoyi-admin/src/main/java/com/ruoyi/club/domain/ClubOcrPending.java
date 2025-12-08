package com.ruoyi.club.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * OCR识别暂存对象 club_ocr_pending
 * 
 * @author lhb
 * @date 2025-11-21
 */
public class ClubOcrPending extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 关联OCR结果ID */
    @Excel(name = "关联OCR结果ID")
    private Long ocrResultId;

    /** 盐场成员ID */
    @Excel(name = "盐场成员ID")
    private Long memberId;

    /** 成员名称 */
    @Excel(name = "成员名称")
    private String memberName;

    /** 击杀数 */
    @Excel(name = "击杀数")
    private Integer kills;

    /** 死亡数 */
    @Excel(name = "死亡数")
    private Integer deaths;

    /** KD比率 */
    @Excel(name = "KD比率")
    private BigDecimal kdRatio;

    /** 刨地数 */
    @Excel(name = "刨地数")
    private Integer digging;

    /** 复活数 */
    @Excel(name = "复活数")
    private Integer revives;

    /** 战绩日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "战绩日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date recordDate;

    /** 删除标志（0代表存在 2代表删除） */
    private String delFlag;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }
    public void setOcrResultId(Long ocrResultId) 
    {
        this.ocrResultId = ocrResultId;
    }

    public Long getOcrResultId() 
    {
        return ocrResultId;
    }
    public void setMemberId(Long memberId) 
    {
        this.memberId = memberId;
    }

    public Long getMemberId() 
    {
        return memberId;
    }
    public void setMemberName(String memberName) 
    {
        this.memberName = memberName;
    }

    public String getMemberName() 
    {
        return memberName;
    }
    public void setKills(Integer kills) 
    {
        this.kills = kills;
    }

    public Integer getKills() 
    {
        return kills;
    }
    public void setDeaths(Integer deaths) 
    {
        this.deaths = deaths;
    }

    public Integer getDeaths() 
    {
        return deaths;
    }
    public void setKdRatio(BigDecimal kdRatio) 
    {
        this.kdRatio = kdRatio;
    }

    public BigDecimal getKdRatio() 
    {
        return kdRatio;
    }
    public void setDigging(Integer digging) 
    {
        this.digging = digging;
    }

    public Integer getDigging() 
    {
        return digging;
    }
    public void setRevives(Integer revives) 
    {
        this.revives = revives;
    }

    public Integer getRevives() 
    {
        return revives;
    }
    public void setRecordDate(Date recordDate) 
    {
        this.recordDate = recordDate;
    }

    public Date getRecordDate() 
    {
        return recordDate;
    }
    public void setDelFlag(String delFlag) 
    {
        this.delFlag = delFlag;
    }

    public String getDelFlag() 
    {
        return delFlag;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("ocrResultId", getOcrResultId())
            .append("memberId", getMemberId())
            .append("memberName", getMemberName())
            .append("kills", getKills())
            .append("deaths", getDeaths())
            .append("kdRatio", getKdRatio())
            .append("digging", getDigging())
            .append("revives", getRevives())
            .append("recordDate", getRecordDate())
            .append("remark", getRemark())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("delFlag", getDelFlag())
            .toString();
    }
}