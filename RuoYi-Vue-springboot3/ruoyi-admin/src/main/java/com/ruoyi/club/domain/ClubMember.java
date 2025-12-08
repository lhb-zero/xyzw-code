package com.ruoyi.club.domain;

import java.util.List;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 俱乐部成员信息对象 club_member
 * 
 * @author lhb
 * @date 2025-11-18
 */
public class ClubMember extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 游戏昵称/ID */
    @Excel(name = "游戏昵称/ID")
    private String gameId;

    /** 战力值 */
    @Excel(name = "战力值")
    private Long power;

    /** 原俱乐部名称 */
    @Excel(name = "原俱乐部名称")
    private String server;

    /** 团别（1团、2团、3团等） */
    @Excel(name = "团别")
    private String teamGroup;

    /** 红淬炼数量范围查询-最小值 */
    private Long redRefineMin;

    /** 红淬炼数量范围查询-最大值 */
    private Long redRefineMax;

    /** 红淬炼数量范围查询-操作符（gte, lt） */
    private String redRefineOp;

    /** 加入日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "加入日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date joinDate;

    /** 四圣数量 */
    @Excel(name = "四圣数量")
    private Long fourSacred;

    /** 红色淬炼数量 */
    @Excel(name = "红色淬炼数量")
    private Long redRefine;

    /** 主C阵容 */
    @Excel(name = "主C阵容")
    private String mainLineup;

    /** 自定义扩展字段1 */
    private String customField1;

    /** 自定义扩展字段2 */
    private String customField2;

    /** 创建时间 */
    private Date createdAt;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "更新时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date updatedAt;

    /** 盐场战绩记录信息 */
    private List<SaltFieldRecord> saltFieldRecordList;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setGameId(String gameId) 
    {
        this.gameId = gameId;
    }

    public String getGameId() 
    {
        return gameId;
    }

    public void setPower(Long power) 
    {
        this.power = power;
    }

    public Long getPower() 
    {
        return power;
    }

    public void setServer(String server) 
    {
        this.server = server;
    }

    public String getServer() 
    {
        return server;
    }

    public void setTeamGroup(String teamGroup) 
    {
        this.teamGroup = teamGroup;
    }

    public String getTeamGroup() 
    {
        return teamGroup;
    }

    public void setRedRefineMin(Long redRefineMin) 
    {
        this.redRefineMin = redRefineMin;
    }

    public Long getRedRefineMin() 
    {
        return redRefineMin;
    }

    public void setRedRefineMax(Long redRefineMax) 
    {
        this.redRefineMax = redRefineMax;
    }

    public Long getRedRefineMax() 
    {
        return redRefineMax;
    }

    public void setRedRefineOp(String redRefineOp) 
    {
        this.redRefineOp = redRefineOp;
    }

    public String getRedRefineOp() 
    {
        return redRefineOp;
    }

    public void setJoinDate(Date joinDate) 
    {
        this.joinDate = joinDate;
    }

    public Date getJoinDate() 
    {
        return joinDate;
    }

    public void setFourSacred(Long fourSacred) 
    {
        this.fourSacred = fourSacred;
    }

    public Long getFourSacred() 
    {
        return fourSacred;
    }

    public void setRedRefine(Long redRefine) 
    {
        this.redRefine = redRefine;
    }

    public Long getRedRefine() 
    {
        return redRefine;
    }

    public void setMainLineup(String mainLineup) 
    {
        this.mainLineup = mainLineup;
    }

    public String getMainLineup() 
    {
        return mainLineup;
    }

    public void setCustomField1(String customField1) 
    {
        this.customField1 = customField1;
    }

    public String getCustomField1() 
    {
        return customField1;
    }

    public void setCustomField2(String customField2) 
    {
        this.customField2 = customField2;
    }

    public String getCustomField2() 
    {
        return customField2;
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

    public List<SaltFieldRecord> getSaltFieldRecordList()
    {
        return saltFieldRecordList;
    }

    public void setSaltFieldRecordList(List<SaltFieldRecord> saltFieldRecordList)
    {
        this.saltFieldRecordList = saltFieldRecordList;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("gameId", getGameId())
            .append("power", getPower())
            .append("server", getServer())
            .append("teamGroup", getTeamGroup())
            .append("joinDate", getJoinDate())
            .append("fourSacred", getFourSacred())
            .append("redRefine", getRedRefine())
            .append("mainLineup", getMainLineup())
            .append("customField1", getCustomField1())
            .append("customField2", getCustomField2())
            .append("createdAt", getCreatedAt())
            .append("updatedAt", getUpdatedAt())
            .append("saltFieldRecordList", getSaltFieldRecordList())
            .toString();
    }
}
