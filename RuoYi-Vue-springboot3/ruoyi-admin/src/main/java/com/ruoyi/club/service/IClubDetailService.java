package com.ruoyi.club.service;

import java.util.List;
import com.ruoyi.club.domain.ClubMember;
import com.ruoyi.club.domain.ClubDetail;

/**
 * 俱乐部详情Service接口
 * 
 * @author lhb
 * @date 2025-12-06
 */
public interface IClubDetailService 
{
    /**
     * 查询俱乐部详情
     * 
     * @param id 俱乐部详情主键
     * @return 俱乐部详情
     */
    public ClubDetail selectClubDetailById(Long id);

    /**
     * 根据团别查询俱乐部详情
     * 
     * @param teamGroup 团别
     * @return 俱乐部详情
     */
    public ClubDetail selectClubDetailByTeamGroup(String teamGroup);

    /**
     * 查询俱乐部详情列表
     * 
     * @param clubDetail 俱乐部详情
     * @return 俱乐部详情集合
     */
    public List<ClubDetail> selectClubDetailList(ClubDetail clubDetail);

    /**
     * 查询所有团队概览
     * 
     * @return 团队概览列表
     */
    public List<ClubDetail> selectTeamOverview();

    /**
     * 查询团别分布数据
     * 
     * @return 团别分布数据
     */
    public List<ClubDetail> selectTeamDistribution();

    /**
     * 查询阵容分布数据
     * 
     * @return 阵容分布数据
     */
    public List<ClubDetail.LineupStats> selectLineupDistribution();

    /**
     * 查询团别战力对比数据
     * 
     * @return 团别战力对比数据
     */
    public List<ClubDetail> selectPowerComparison();

    /**
     * 查询团别红淬炼对比数据
     * 
     * @return 团别红淬炼对比数据
     */
    public List<ClubDetail> selectRedRefineComparison();

    /**
     * 查询团内战力分布数据
     * 
     * @param teamGroup 团别
     * @return 团内战力分布数据
     */
    public List<ClubDetail> selectPowerDistributionByTeamGroup(String teamGroup);

    /**
     * 查询团内红淬炼分布数据
     * 
     * @param teamGroup 团别
     * @return 团内红淬炼分布数据
     */
    public List<ClubDetail> selectRedRefineDistributionByTeamGroup(String teamGroup);

    /**
     * 查询团内四圣数量分布数据
     * 
     * @param teamGroup 团别
     * @return 团内四圣数量分布数据
     */
    public List<ClubDetail> selectFourSacredDistributionByTeamGroup(String teamGroup);

    /**
     * 查询团内成员列表（用于散点图）
     * 
     * @param teamGroup 团别
     * @return 团内成员列表
     */
    public List<ClubMember> selectMembersByTeamGroup(String teamGroup);

    /**
     * 新增俱乐部详情
     * 
     * @param clubDetail 俱乐部详情
     * @return 结果
     */
    public int insertClubDetail(ClubDetail clubDetail);

    /**
     * 修改俱乐部详情
     * 
     * @param clubDetail 俱乐部详情
     * @return 结果
     */
    public int updateClubDetail(ClubDetail clubDetail);

    /**
     * 批量删除俱乐部详情
     * 
     * @param ids 需要删除的俱乐部详情主键集合
     * @return 结果
     */
    public int deleteClubDetailByIds(Long[] ids);

    /**
     * 删除俱乐部详情信息
     * 
     * @param id 俱乐部详情主键
     * @return 结果
     */
    public int deleteClubDetailById(Long id);
}