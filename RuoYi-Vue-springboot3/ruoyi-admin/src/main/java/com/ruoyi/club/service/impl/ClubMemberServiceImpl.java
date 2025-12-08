package com.ruoyi.club.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.club.domain.SaltFieldRecord;
import com.ruoyi.club.mapper.ClubMemberMapper;
import com.ruoyi.club.domain.ClubMember;
import com.ruoyi.club.service.IClubMemberService;

/**
 * 俱乐部成员信息Service业务层处理
 * 
 * @author lhb
 * @date 2025-11-18
 */
@Service
public class ClubMemberServiceImpl implements IClubMemberService 
{
    @Autowired
    private ClubMemberMapper clubMemberMapper;

    /**
     * 查询俱乐部成员信息
     * 
     * @param id 俱乐部成员信息主键
     * @return 俱乐部成员信息
     */
    @Override
    public ClubMember selectClubMemberById(Long id)
    {
        return clubMemberMapper.selectClubMemberById(id);
    }

    /**
     * 查询俱乐部成员信息列表
     * 
     * @param clubMember 俱乐部成员信息
     * @return 俱乐部成员信息
     */
    @Override
    public List<ClubMember> selectClubMemberList(ClubMember clubMember)
    {
        return clubMemberMapper.selectClubMemberList(clubMember);
    }

    /**
     * 新增俱乐部成员信息
     * 
     * @param clubMember 俱乐部成员信息
     * @return 结果
     */
    @Transactional
    @Override
    public int insertClubMember(ClubMember clubMember)
    {
        int rows = clubMemberMapper.insertClubMember(clubMember);
        insertSaltFieldRecord(clubMember);
        return rows;
    }

    /**
     * 修改俱乐部成员信息
     * 
     * @param clubMember 俱乐部成员信息
     * @return 结果
     */
    @Transactional
    @Override
    public int updateClubMember(ClubMember clubMember)
    {
        clubMemberMapper.deleteSaltFieldRecordByMemberId(clubMember.getId());
        insertSaltFieldRecord(clubMember);
        return clubMemberMapper.updateClubMember(clubMember);
    }

    /**
     * 批量删除俱乐部成员信息
     * 
     * @param ids 需要删除的俱乐部成员信息主键
     * @return 结果
     */
    @Transactional
    @Override
    public int deleteClubMemberByIds(Long[] ids)
    {
        clubMemberMapper.deleteSaltFieldRecordByMemberIds(ids);
        return clubMemberMapper.deleteClubMemberByIds(ids);
    }

    /**
     * 删除俱乐部成员信息信息
     * 
     * @param id 俱乐部成员信息主键
     * @return 结果
     */
    @Transactional
    @Override
    public int deleteClubMemberById(Long id)
    {
        clubMemberMapper.deleteSaltFieldRecordByMemberId(id);
        return clubMemberMapper.deleteClubMemberById(id);
    }
    
    /**
     * 更新红淬炼数量
     * 
     * @param id 俱乐部成员信息主键
     * @param operation 操作类型（increment或decrement）
     * @return 更新后的俱乐部成员信息
     */
    @Override
    public ClubMember updateRedRefine(Long id, String operation)
    {
        // 获取当前成员信息
        ClubMember member = clubMemberMapper.selectClubMemberById(id);
        if (member == null) {
            throw new RuntimeException("成员不存在");
        }
        
        Long currentRedRefine = member.getRedRefine();
        if (currentRedRefine == null) {
            currentRedRefine = 0L;
        }
        
        // 根据操作类型更新红淬炼数量
        if ("increment".equals(operation)) {
            member.setRedRefine(currentRedRefine + 1);
        } else if ("decrement".equals(operation)) {
            if (currentRedRefine <= 0) {
                throw new RuntimeException("红淬炼数量不能小于0");
            }
            member.setRedRefine(currentRedRefine - 1);
        } else {
            throw new RuntimeException("无效的操作类型");
        }
        
        // 更新数据库
        clubMemberMapper.updateClubMember(member);
        
        return member;
    }

    /**
     * 更新四圣数量
     * 
     * @param id 俱乐部成员信息主键
     * @param operation 操作类型（increment或decrement）
     * @return 更新后的俱乐部成员信息
     */
    @Override
    public ClubMember updateFourSacred(Long id, String operation)
    {
        // 获取当前成员信息
        ClubMember member = clubMemberMapper.selectClubMemberById(id);
        if (member == null) {
            throw new RuntimeException("成员不存在");
        }
        
        Long currentFourSacred = member.getFourSacred();
        if (currentFourSacred == null) {
            currentFourSacred = 0L;
        }
        
        // 根据操作类型更新四圣数量
        if ("increment".equals(operation)) {
            member.setFourSacred(currentFourSacred + 1);
        } else if ("decrement".equals(operation)) {
            if (currentFourSacred <= 0) {
                throw new RuntimeException("四圣数量不能小于0");
            }
            member.setFourSacred(currentFourSacred - 1);
        } else {
            throw new RuntimeException("无效的操作类型");
        }
        
        // 更新数据库
        clubMemberMapper.updateClubMember(member);
        
        return member;
    }

    /**
     * 新增盐场战绩记录信息
     * 
     * @param clubMember 俱乐部成员信息对象
     */
    public void insertSaltFieldRecord(ClubMember clubMember)
    {
        List<SaltFieldRecord> saltFieldRecordList = clubMember.getSaltFieldRecordList();
        Long id = clubMember.getId();
        if (StringUtils.isNotNull(saltFieldRecordList))
        {
            List<SaltFieldRecord> list = new ArrayList<SaltFieldRecord>();
            for (SaltFieldRecord saltFieldRecord : saltFieldRecordList)
            {
                saltFieldRecord.setMemberId(id);
                list.add(saltFieldRecord);
            }
            if (list.size() > 0)
            {
                clubMemberMapper.batchSaltFieldRecord(list);
            }
        }
    }
}
