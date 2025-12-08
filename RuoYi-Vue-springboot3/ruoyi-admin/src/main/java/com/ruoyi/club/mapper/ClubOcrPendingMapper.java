package com.ruoyi.club.mapper;

import java.util.List;
import com.ruoyi.club.domain.ClubOcrPending;

/**
 * OCR识别暂存Mapper接口
 * 
 * @author lhb
 * @date 2025-11-21
 */
public interface ClubOcrPendingMapper 
{
    /**
     * 查询OCR识别暂存
     * 
     * @param id OCR识别暂存主键
     * @return OCR识别暂存
     */
    public ClubOcrPending selectClubOcrPendingById(Long id);

    /**
     * 查询OCR识别暂存列表
     * 
     * @param clubOcrPending OCR识别暂存
     * @return OCR识别暂存集合
     */
    public List<ClubOcrPending> selectClubOcrPendingList(ClubOcrPending clubOcrPending);

    /**
     * 根据OCR结果ID查询暂存记录
     * 
     * @param ocrResultId OCR结果ID
     * @return OCR识别暂存列表
     */
    public List<ClubOcrPending> selectClubOcrPendingByOcrResultId(Long ocrResultId);

    /**
     * 新增OCR识别暂存
     * 
     * @param clubOcrPending OCR识别暂存
     * @return 结果
     */
    public int insertClubOcrPending(ClubOcrPending clubOcrPending);

    /**
     * 修改OCR识别暂存
     * 
     * @param clubOcrPending OCR识别暂存
     * @return 结果
     */
    public int updateClubOcrPending(ClubOcrPending clubOcrPending);

    /**
     * 删除OCR识别暂存
     * 
     * @param id OCR识别暂存主键
     * @return 结果
     */
    public int deleteClubOcrPendingById(Long id);

    /**
     * 批量删除OCR识别暂存
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteClubOcrPendingByIds(Long[] ids);
    
    /**
     * 批量插入OCR识别暂存
     * 
     * @param clubOcrPendingList OCR识别暂存列表
     * @return 结果
     */
    public int batchInsertClubOcrPending(List<ClubOcrPending> clubOcrPendingList);
    
    /**
     * 清空所有暂存记录
     * 
     * @return 结果
     */
    public int clearAllPendingRecords();
}