package com.ruoyi.club.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.club.mapper.OcrResultMapper;
import com.ruoyi.club.domain.OcrResult;
import com.ruoyi.club.service.IOcrResultService;

/**
 * OCR识别结果Service业务层处理
 * 
 * @author lhb
 * @date 2025-11-19
 */
@Service
public class OcrResultServiceImpl implements IOcrResultService 
{
    @Autowired
    private OcrResultMapper ocrResultMapper;

    /**
     * 查询OCR识别结果
     * 
     * @param id OCR识别结果主键
     * @return OCR识别结果
     */
    @Override
    public OcrResult selectOcrResultById(Long id)
    {
        return ocrResultMapper.selectOcrResultById(id);
    }

    /**
     * 查询OCR识别结果列表
     * 
     * @param ocrResult OCR识别结果
     * @return OCR识别结果
     */
    @Override
    public List<OcrResult> selectOcrResultList(OcrResult ocrResult)
    {
        return ocrResultMapper.selectOcrResultList(ocrResult);
    }

    /**
     * 新增OCR识别结果
     * 
     * @param ocrResult OCR识别结果
     * @return 结果
     */
    @Override
    public int insertOcrResult(OcrResult ocrResult)
    {
        ocrResult.setCreateTime(DateUtils.getNowDate());
        return ocrResultMapper.insertOcrResult(ocrResult);
    }

    /**
     * 修改OCR识别结果
     * 
     * @param ocrResult OCR识别结果
     * @return 结果
     */
    @Override
    public int updateOcrResult(OcrResult ocrResult)
    {
        ocrResult.setUpdateTime(DateUtils.getNowDate());
        return ocrResultMapper.updateOcrResult(ocrResult);
    }

    /**
     * 批量删除OCR识别结果
     * 
     * @param ids 需要删除的OCR识别结果主键
     * @return 结果
     */
    @Override
    public int deleteOcrResultByIds(Long[] ids)
    {
        return ocrResultMapper.deleteOcrResultByIds(ids);
    }

    /**
     * 删除OCR识别结果信息
     * 
     * @param id OCR识别结果主键
     * @return 结果
     */
    @Override
    public int deleteOcrResultById(Long id)
    {
        return ocrResultMapper.deleteOcrResultById(id);
    }
}