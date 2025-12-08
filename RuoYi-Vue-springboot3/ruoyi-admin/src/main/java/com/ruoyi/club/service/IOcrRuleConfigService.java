package com.ruoyi.club.service;

import java.util.List;
import com.ruoyi.club.domain.OcrRuleConfig;

/**
 * OCR规则配置Service接口
 * 
 * @author lhb
 * @date 2025-11-19
 */
public interface IOcrRuleConfigService 
{
    /**
     * 查询OCR规则配置
     * 
     * @param id OCR规则配置主键
     * @return OCR规则配置
     */
    public OcrRuleConfig selectOcrRuleConfigById(Long id);

    /**
     * 查询OCR规则配置列表
     * 
     * @param ocrRuleConfig OCR规则配置
     * @return OCR规则配置集合
     */
    public List<OcrRuleConfig> selectOcrRuleConfigList(OcrRuleConfig ocrRuleConfig);

    /**
     * 新增OCR规则配置
     * 
     * @param ocrRuleConfig OCR规则配置
     * @return 结果
     */
    public int insertOcrRuleConfig(OcrRuleConfig ocrRuleConfig);

    /**
     * 修改OCR规则配置
     * 
     * @param ocrRuleConfig OCR规则配置
     * @return 结果
     */
    public int updateOcrRuleConfig(OcrRuleConfig ocrRuleConfig);

    /**
     * 批量删除OCR规则配置
     * 
     * @param ids 需要删除的OCR规则配置主键集合
     * @return 结果
     */
    public int deleteOcrRuleConfigByIds(Long[] ids);

    /**
     * 删除OCR规则配置信息
     * 
     * @param id OCR规则配置主键
     * @return 结果
     */
    public int deleteOcrRuleConfigById(Long id);
}