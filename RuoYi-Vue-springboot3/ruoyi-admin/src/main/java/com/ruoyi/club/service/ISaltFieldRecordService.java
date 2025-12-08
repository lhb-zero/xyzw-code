package com.ruoyi.club.service;

import java.util.List;
import com.ruoyi.club.domain.SaltFieldRecord;

/**
 * 盐场战绩记录Service接口
 * 
 * @author lhb
 * @date 2025-11-18
 */
public interface ISaltFieldRecordService 
{
    /**
     * 查询盐场战绩记录
     * 
     * @param id 盐场战绩记录主键
     * @return 盐场战绩记录
     */
    public SaltFieldRecord selectSaltFieldRecordById(Long id);

    /**
     * 查询盐场战绩记录列表
     * 
     * @param saltFieldRecord 盐场战绩记录
     * @return 盐场战绩记录集合
     */
    public List<SaltFieldRecord> selectSaltFieldRecordList(SaltFieldRecord saltFieldRecord);

    /**
     * 新增盐场战绩记录
     * 
     * @param saltFieldRecord 盐场战绩记录
     * @return 结果
     */
    public int insertSaltFieldRecord(SaltFieldRecord saltFieldRecord);

    /**
     * 修改盐场战绩记录
     * 
     * @param saltFieldRecord 盐场战绩记录
     * @return 结果
     */
    public int updateSaltFieldRecord(SaltFieldRecord saltFieldRecord);

    /**
     * 批量删除盐场战绩记录
     * 
     * @param ids 需要删除的盐场战绩记录主键集合
     * @return 结果
     */
    public int deleteSaltFieldRecordByIds(Long[] ids);

    /**
     * 删除盐场战绩记录信息
     * 
     * @param id 盐场战绩记录主键
     * @return 结果
     */
    public int deleteSaltFieldRecordById(Long id);
    
    /**
     * 根据成员ID查询盐场战绩记录
     * 
     * @param memberId 成员ID
     * @return 盐场战绩记录集合
     */
    public List<SaltFieldRecord> selectSaltFieldRecordByMemberId(Long memberId);
}