package com.ruoyi.club.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.club.mapper.SaltFieldRecordMapper;
import com.ruoyi.club.domain.SaltFieldRecord;
import com.ruoyi.club.service.ISaltFieldRecordService;

/**
 * 盐场战绩记录Service业务层处理
 * 
 * @author lhb
 * @date 2025-11-18
 */
@Service
public class SaltFieldRecordServiceImpl implements ISaltFieldRecordService 
{
    @Autowired
    private SaltFieldRecordMapper saltFieldRecordMapper;

    /**
     * 查询盐场战绩记录
     * 
     * @param id 盐场战绩记录主键
     * @return 盐场战绩记录
     */
    @Override
    public SaltFieldRecord selectSaltFieldRecordById(Long id)
    {
        return saltFieldRecordMapper.selectSaltFieldRecordById(id);
    }

    /**
     * 查询盐场战绩记录列表
     * 
     * @param saltFieldRecord 盐场战绩记录
     * @return 盐场战绩记录
     */
    @Override
    public List<SaltFieldRecord> selectSaltFieldRecordList(SaltFieldRecord saltFieldRecord)
    {
        return saltFieldRecordMapper.selectSaltFieldRecordList(saltFieldRecord);
    }

    /**
     * 新增盐场战绩记录
     * 
     * @param saltFieldRecord 盐场战绩记录
     * @return 结果
     */
    @Override
    public int insertSaltFieldRecord(SaltFieldRecord saltFieldRecord)
    {
        return saltFieldRecordMapper.insertSaltFieldRecord(saltFieldRecord);
    }

    /**
     * 修改盐场战绩记录
     * 
     * @param saltFieldRecord 盐场战绩记录
     * @return 结果
     */
    @Override
    public int updateSaltFieldRecord(SaltFieldRecord saltFieldRecord)
    {
        return saltFieldRecordMapper.updateSaltFieldRecord(saltFieldRecord);
    }

    /**
     * 批量删除盐场战绩记录
     * 
     * @param ids 需要删除的盐场战绩记录主键
     * @return 结果
     */
    @Override
    public int deleteSaltFieldRecordByIds(Long[] ids)
    {
        return saltFieldRecordMapper.deleteSaltFieldRecordByIds(ids);
    }

    /**
     * 删除盐场战绩记录信息
     * 
     * @param id 盐场战绩记录主键
     * @return 结果
     */
    @Override
    public int deleteSaltFieldRecordById(Long id)
    {
        return saltFieldRecordMapper.deleteSaltFieldRecordById(id);
    }
    
    /**
     * 根据成员ID查询盐场战绩记录
     * 
     * @param memberId 成员ID
     * @return 盐场战绩记录集合
     */
    @Override
    public List<SaltFieldRecord> selectSaltFieldRecordByMemberId(Long memberId)
    {
        return saltFieldRecordMapper.selectSaltFieldRecordByMemberId(memberId);
    }
}