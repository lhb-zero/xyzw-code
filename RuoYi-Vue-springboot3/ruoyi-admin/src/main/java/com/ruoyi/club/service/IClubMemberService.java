package com.ruoyi.club.service;

import java.util.List;
import com.ruoyi.club.domain.ClubMember;

/**
 * 俱乐部成员信息Service接口
 * 
 * @author lhb
 * @date 2025-11-18
 */
public interface IClubMemberService 
{
    /**
     * 查询俱乐部成员信息
     * 
     * @param id 俱乐部成员信息主键
     * @return 俱乐部成员信息
     */
    public ClubMember selectClubMemberById(Long id);

    /**
     * 查询俱乐部成员信息列表
     * 
     * @param clubMember 俱乐部成员信息
     * @return 俱乐部成员信息集合
     */
    public List<ClubMember> selectClubMemberList(ClubMember clubMember);

    /**
     * 新增俱乐部成员信息
     * 
     * @param clubMember 俱乐部成员信息
     * @return 结果
     */
    public int insertClubMember(ClubMember clubMember);

    /**
     * 修改俱乐部成员信息
     * 
     * @param clubMember 俱乐部成员信息
     * @return 结果
     */
    public int updateClubMember(ClubMember clubMember);

    /**
     * 批量删除俱乐部成员信息
     * 
     * @param ids 需要删除的俱乐部成员信息主键集合
     * @return 结果
     */
    public int deleteClubMemberByIds(Long[] ids);

    /**
     * 删除俱乐部成员信息信息
     * 
     * @param id 俱乐部成员信息主键
     * @return 结果
     */
    public int deleteClubMemberById(Long id);
    
    /**
     * 更新红淬炼数量
     * 
     * @param id 俱乐部成员信息主键
     * @param operation 操作类型（increment或decrement）
     * @return 更新后的俱乐部成员信息
     */
    public ClubMember updateRedRefine(Long id, String operation);

    /**
     * 更新四圣数量
     * 
     * @param id 俱乐部成员信息主键
     * @param operation 操作类型（increment或decrement）
     * @return 更新后的俱乐部成员信息
     */
    public ClubMember updateFourSacred(Long id, String operation);
}
