package com.ruoyi.club.service.impl;

import java.util.List;
import java.util.Date;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.club.mapper.ClubOcrPendingMapper;
import com.ruoyi.club.mapper.OcrResultMapper;
import com.ruoyi.club.domain.ClubOcrPending;
import com.ruoyi.club.domain.OcrResult;
import com.ruoyi.club.domain.SaltFieldRecord;
import com.ruoyi.club.service.IClubOcrPendingService;
import com.ruoyi.club.service.ISaltFieldRecordService;

/**
 * OCR识别暂存Service业务层处理
 * 
 * @author lhb
 * @date 2025-11-21
 */
@Service
public class ClubOcrPendingServiceImpl implements IClubOcrPendingService 
{
    @Autowired
    private ClubOcrPendingMapper clubOcrPendingMapper;
    
    @Autowired
    private OcrResultMapper ocrResultMapper;
    
    @Autowired
    private ISaltFieldRecordService saltFieldRecordService;

    /**
     * 查询OCR识别暂存
     * 
     * @param id OCR识别暂存主键
     * @return OCR识别暂存
     */
    @Override
    public ClubOcrPending selectClubOcrPendingById(Long id)
    {
        return clubOcrPendingMapper.selectClubOcrPendingById(id);
    }

    /**
     * 查询OCR识别暂存列表
     * 
     * @param clubOcrPending OCR识别暂存
     * @return OCR识别暂存
     */
    @Override
    public List<ClubOcrPending> selectClubOcrPendingList(ClubOcrPending clubOcrPending)
    {
        return clubOcrPendingMapper.selectClubOcrPendingList(clubOcrPending);
    }

    /**
     * 根据OCR结果ID查询暂存记录
     * 
     * @param ocrResultId OCR结果ID
     * @return OCR识别暂存列表
     */
    @Override
    public List<ClubOcrPending> selectClubOcrPendingByOcrResultId(Long ocrResultId)
    {
        ClubOcrPending query = new ClubOcrPending();
        query.setOcrResultId(ocrResultId);
        query.setDelFlag("0");
        return clubOcrPendingMapper.selectClubOcrPendingList(query);
    }

    /**
     * 新增OCR识别暂存
     * 
     * @param clubOcrPending OCR识别暂存
     * @return 结果
     */
    @Override
    public int insertClubOcrPending(ClubOcrPending clubOcrPending)
    {
        clubOcrPending.setCreateTime(DateUtils.getNowDate());
        clubOcrPending.setDelFlag("0");
        return clubOcrPendingMapper.insertClubOcrPending(clubOcrPending);
    }

    /**
     * 修改OCR识别暂存
     * 
     * @param clubOcrPending OCR识别暂存
     * @return 结果
     */
    @Override
    public int updateClubOcrPending(ClubOcrPending clubOcrPending)
    {
        clubOcrPending.setUpdateTime(DateUtils.getNowDate());
        return clubOcrPendingMapper.updateClubOcrPending(clubOcrPending);
    }

    /**
     * 批量删除OCR识别暂存
     * 
     * @param ids 需要删除的OCR识别暂存主键
     * @return 结果
     */
    @Override
    public int deleteClubOcrPendingByIds(Long[] ids)
    {
        return clubOcrPendingMapper.deleteClubOcrPendingByIds(ids);
    }

    /**
     * 删除OCR识别暂存信息
     * 
     * @param id OCR识别暂存主键
     * @return 结果
     */
    @Override
    public int deleteClubOcrPendingById(Long id)
    {
        return clubOcrPendingMapper.deleteClubOcrPendingById(id);
    }

    /**
     * 批量插入OCR识别暂存
     * 
     * @param clubOcrPendingList OCR识别暂存列表
     * @return 结果
     */
    @Override
    public int batchInsertClubOcrPending(List<ClubOcrPending> clubOcrPendingList)
    {
        return clubOcrPendingMapper.batchInsertClubOcrPending(clubOcrPendingList);
    }

    /**
     * 从暂存表转移到正式战绩表（单条）
     * 
     * @param id 暂存记录ID
     * @return 结果
     */
    @Override
    @Transactional
    public int transferToSaltFieldRecord(Long id)
    {
        // 查询暂存记录
        ClubOcrPending pending = clubOcrPendingMapper.selectClubOcrPendingById(id);
        if (pending == null) {
            return 0;
        }

        // 创建正式战绩记录
        SaltFieldRecord record = new SaltFieldRecord();
        record.setMemberId(pending.getMemberId());
        record.setRecordDate(pending.getRecordDate());
        record.setKills(pending.getKills().longValue());
        record.setDeaths(pending.getDeaths().longValue());
        record.setKdRatio(pending.getKdRatio());
        record.setDigs(pending.getDigging().longValue());
        record.setRevives(pending.getRevives().longValue());
        record.setOcrRecordId(pending.getOcrResultId());
        record.setCreateBy(pending.getCreateBy());
        record.setCreateTime(new Date());

        // 插入正式战绩表
        int result = saltFieldRecordService.insertSaltFieldRecord(record);

        if (result > 0) {
            // 更新OCR结果状态
            OcrResult ocrResult = new OcrResult();
            ocrResult.setId(pending.getOcrResultId());
            ocrResult.setIsConfirmed(1);
            ocrResult.setConfirmedRecordId(record.getId());
            ocrResult.setUpdateTime(new Date());
            ocrResultMapper.updateOcrResult(ocrResult);

            // 删除暂存记录
            clubOcrPendingMapper.deleteClubOcrPendingById(id);
        }

        return result;
    }

    /**
     * 从暂存表批量转移到正式战绩表
     * 
     * @param ocrResultId OCR结果ID
     * @return 结果
     */
    @Override
    @Transactional
    public int batchTransferToSaltFieldRecord(Long ocrResultId)
    {
        // 查询所有相关的暂存记录
        List<ClubOcrPending> pendingList = selectClubOcrPendingByOcrResultId(ocrResultId);
        if (pendingList == null || pendingList.isEmpty()) {
            return 0;
        }

        int successCount = 0;
        for (ClubOcrPending pending : pendingList) {
            // 转移单条记录
            int result = transferToSaltFieldRecord(pending.getId());
            if (result > 0) {
                successCount++;
            }
        }

        return successCount;
    }
    
    /**
     * 清空所有暂存记录
     * 
     * @return 结果
     */
    @Override
    public int clearAllPendingRecords()
    {
        return clubOcrPendingMapper.clearAllPendingRecords();
    }
}