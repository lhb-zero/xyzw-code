package com.ruoyi.club.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 战绩匹配结果对象 club_battle_match_result
 * 
 * @author lhb
 * @date 2025-11-19
 */
public class BattleMatchResult extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 匹配结果ID */
    private Long id;

    /** OCR识别结果ID */
    @Excel(name = "OCR识别ID")
    private Long ocrResultId;

    /** 昵称 */
    @Excel(name = "昵称")
    private String nickname;

    /** 成员ID */
    @Excel(name = "成员ID")
    private Long memberId;

    /** 匹配的成员信息 */
    private ClubMember matchedMember;

    /** 击杀数 */
    @Excel(name = "击杀数")
    private Integer kills;

    /** 死亡数 */
    @Excel(name = "死亡数")
    private Integer deaths;

    /** 刨地数 */
    @Excel(name = "刨地数")
    private Integer digs;

    /** 复活数 */
    @Excel(name = "复活数")
    private Integer revives;

    /** KD比例 */
    @Excel(name = "KD比例")
    private Double kdRatio;

    /** 战斗日期 */
    @Excel(name = "战斗日期", dateFormat = "yyyy-MM-dd")
    private String battleDate;

    /** 是否已确认（0未确认，1已确认） */
    private Integer confirmed;

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

    public void setOcrResultId(Long ocrResultId) 
    {
        this.ocrResultId = ocrResultId;
    }

    public Long getOcrResultId() 
    {
        return ocrResultId;
    }

    public void setNickname(String nickname) 
    {
        this.nickname = nickname;
    }

    public String getNickname() 
    {
        return nickname;
    }

    public void setMemberId(Long memberId) 
    {
        this.memberId = memberId;
    }

    public Long getMemberId() 
    {
        return memberId;
    }

    public void setMatchedMember(ClubMember matchedMember) 
    {
        this.matchedMember = matchedMember;
    }

    public ClubMember getMatchedMember() 
    {
        return matchedMember;
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

    public void setDigs(Integer digs) 
    {
        this.digs = digs;
    }

    public Integer getDigs() 
    {
        return digs;
    }

    public void setRevives(Integer revives) 
    {
        this.revives = revives;
    }

    public Integer getRevives() 
    {
        return revives;
    }

    public void setKdRatio(Double kdRatio) 
    {
        this.kdRatio = kdRatio;
    }

    public Double getKdRatio() 
    {
        return kdRatio;
    }

    public void setBattleDate(String battleDate) 
    {
        this.battleDate = battleDate;
    }

    public String getBattleDate() 
    {
        return battleDate;
    }

    public void setConfirmed(Integer confirmed) 
    {
        this.confirmed = confirmed;
    }

    public Integer getConfirmed() 
    {
        return confirmed;
    }

    public void setRemark(String remark) 
    {
        this.remark = remark;
    }

    public String getRemark() 
    {
        return remark;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("ocrResultId", getOcrResultId())
            .append("nickname", getNickname())
            .append("memberId", getMemberId())
            .append("matchedMember", getMatchedMember())
            .append("kills", getKills())
            .append("deaths", getDeaths())
            .append("digs", getDigs())
            .append("revives", getRevives())
            .append("kdRatio", getKdRatio())
            .append("battleDate", getBattleDate())
            .append("confirmed", getConfirmed())
            .append("remark", getRemark())
            .toString();
    }
}