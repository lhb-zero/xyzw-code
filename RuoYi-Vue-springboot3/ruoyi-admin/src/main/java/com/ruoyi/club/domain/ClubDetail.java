package com.ruoyi.club.domain;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 俱乐部详情对象 club_detail
 * 
 * @author lhb
 * @date 2025-12-06
 */
public class ClubDetail extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 团别（1团、2团、3团等） */
    @Excel(name = "团别")
    private String teamGroup;

    /** 团队成员数量 */
    @Excel(name = "团队成员数量")
    private Long memberCount;

    /** 总战力值 */
    @Excel(name = "总战力值")
    private Long totalPower;

    /** 平均战力值 */
    @Excel(name = "平均战力值")
    private Long avgPower;

    /** 总红淬炼数量 */
    @Excel(name = "总红淬炼数量")
    private Long totalRedRefine;

    /** 平均红淬炼数量 */
    @Excel(name = "平均红淬炼数量")
    private Long avgRedRefine;
    
    /** 总四圣数量 */
    @Excel(name = "总四圣数量")
    private Long totalFourSacred;
    
    /** 平均四圣数量 */
    @Excel(name = "平均四圣数量")
    private Long avgFourSacred;

    /** 战力统计时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "战力统计时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private String powerStatTime;

    /** 阵容分布统计信息 */
    private List<LineupStats> lineupStats;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setTeamGroup(String teamGroup) 
    {
        this.teamGroup = teamGroup;
    }

    public String getTeamGroup() 
    {
        return teamGroup;
    }

    public void setMemberCount(Long memberCount) 
    {
        this.memberCount = memberCount;
    }

    public Long getMemberCount() 
    {
        return memberCount;
    }

    public void setTotalPower(Long totalPower) 
    {
        this.totalPower = totalPower;
    }

    public Long getTotalPower() 
    {
        return totalPower;
    }

    public void setAvgPower(Long avgPower) 
    {
        this.avgPower = avgPower;
    }

    public Long getAvgPower() 
    {
        return avgPower;
    }

    public void setTotalRedRefine(Long totalRedRefine) 
    {
        this.totalRedRefine = totalRedRefine;
    }

    public Long getTotalRedRefine() 
    {
        return totalRedRefine;
    }

    public void setAvgRedRefine(Long avgRedRefine) 
    {
        this.avgRedRefine = avgRedRefine;
    }

    public Long getAvgRedRefine() 
    {
        return avgRedRefine;
    }
    
    public void setTotalFourSacred(Long totalFourSacred) 
    {
        this.totalFourSacred = totalFourSacred;
    }

    public Long getTotalFourSacred() 
    {
        return totalFourSacred;
    }
    
    public void setAvgFourSacred(Long avgFourSacred) 
    {
        this.avgFourSacred = avgFourSacred;
    }

    public Long getAvgFourSacred() 
    {
        return avgFourSacred;
    }

    public void setPowerStatTime(String powerStatTime) 
    {
        this.powerStatTime = powerStatTime;
    }

    public String getPowerStatTime() 
    {
        return powerStatTime;
    }

    public void setLineupStats(List<LineupStats> lineupStats) 
    {
        this.lineupStats = lineupStats;
    }

    public List<LineupStats> getLineupStats() 
    {
        return lineupStats;
    }

    /**
     * 阵容统计信息内部类
     */
    public static class LineupStats {
        /** 阵容类型 */
        private String lineupType;
        
        /** 阵容标签 */
        private String lineupLabel;
        
        /** 阵容数量 */
        private Long count;
        
        /** 阵容占比 */
        private Double percentage;

        public String getLineupType() {
            return lineupType;
        }

        public void setLineupType(String lineupType) {
            this.lineupType = lineupType;
        }

        public String getLineupLabel() {
            return lineupLabel;
        }

        public void setLineupLabel(String lineupLabel) {
            this.lineupLabel = lineupLabel;
        }

        public Long getCount() {
            return count;
        }

        public void setCount(Long count) {
            this.count = count;
        }

        public Double getPercentage() {
            return percentage;
        }

        public void setPercentage(Double percentage) {
            this.percentage = percentage;
        }
    }
}