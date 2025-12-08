package com.ruoyi.club.service;

import java.util.List;
import com.ruoyi.club.domain.OcrResult;

/**
 * OCR识别结果Service接口
 * 
 * @author lhb
 * @date 2025-11-19
 */
public interface IOcrResultService 
{
    /**
     * 查询OCR识别结果
     * 
     * @param id OCR识别结果主键
     * @return OCR识别结果
     */
    public OcrResult selectOcrResultById(Long id);

    /**
     * 查询OCR识别结果列表
     * 
     * @param ocrResult OCR识别结果
     * @return OCR识别结果集合
     */
    public List<OcrResult> selectOcrResultList(OcrResult ocrResult);

    /**
     * 新增OCR识别结果
     * 
     * @param ocrResult OCR识别结果
     * @return 结果
     */
    public int insertOcrResult(OcrResult ocrResult);

    /**
     * 修改OCR识别结果
     * 
     * @param ocrResult OCR识别结果
     * @return 结果
     */
    public int updateOcrResult(OcrResult ocrResult);

    /**
     * 批量删除OCR识别结果
     * 
     * @param ids 需要删除的OCR识别结果主键集合
     * @return 结果
     */
    public int deleteOcrResultByIds(Long[] ids);

    /**
     * 删除OCR识别结果信息
     * 
     * @param id OCR识别结果主键
     * @return 结果
     */
    public int deleteOcrResultById(Long id);
}