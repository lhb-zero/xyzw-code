package com.ruoyi.club.mapper;

import java.util.List;
import com.ruoyi.club.domain.ClubMember;
import com.ruoyi.club.domain.ClubDetail;

/**
 * 俱乐部详情Mapper接口
 * 
 * @author lhb
 * @date 2025-12-06
 */
public interface ClubDetailMapper 
{
    /**
     * 查询俱乐部详情
     * 
     * @param id 俱乐部详情主键
     * @return 俱乐部详情
     */
    public ClubDetail selectClubDetailById(Long id);

    /**
     * 查询俱乐部详情列表
     * 
     * @param clubDetail 俱乐部详情
     * @return 俱乐部详情集合
     */
    public List<ClubDetail> selectClubDetailList(ClubDetail clubDetail);

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
     * 删除俱乐部详情
     * 
     * @param id 俱乐部详情主键
     * @return 结果
     */
    public int deleteClubDetailById(Long id);

    /**
     * 批量删除俱乐部详情
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteClubDetailByIds(Long[] ids);

    /**
     * 查询指定团别的成员列表
     * 
     * @param teamGroup 团别
     * @return 成员列表
     */
    public List<ClubMember> selectMembersByTeamGroup(String teamGroup);
}