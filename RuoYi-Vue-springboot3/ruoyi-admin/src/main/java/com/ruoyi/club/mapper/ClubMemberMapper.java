package com.ruoyi.club.mapper;

import java.util.List;
import com.ruoyi.club.domain.ClubMember;
import com.ruoyi.club.domain.SaltFieldRecord;

/**
 * 俱乐部成员信息Mapper接口
 * 
 * @author lhb
 * @date 2025-11-18
 */
public interface ClubMemberMapper 
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
     * 删除俱乐部成员信息
     * 
     * @param id 俱乐部成员信息主键
     * @return 结果
     */
    public int deleteClubMemberById(Long id);

    /**
     * 批量删除俱乐部成员信息
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteClubMemberByIds(Long[] ids);

    /**
     * 批量删除盐场战绩记录
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteSaltFieldRecordByMemberIds(Long[] ids);
    
    /**
     * 批量新增盐场战绩记录
     * 
     * @param saltFieldRecordList 盐场战绩记录列表
     * @return 结果
     */
    public int batchSaltFieldRecord(List<SaltFieldRecord> saltFieldRecordList);
    

    /**
     * 通过俱乐部成员信息主键删除盐场战绩记录信息
     * 
     * @param id 俱乐部成员信息ID
     * @return 结果
     */
    public int deleteSaltFieldRecordByMemberId(Long id);
}
