package com.ruoyi.club.mapper;

import java.util.List;
import com.ruoyi.club.domain.OcrRuleConfig;

/**
 * OCR规则配置Mapper接口
 * 
 * @author lhb
 * @date 2025-11-19
 */
public interface OcrRuleConfigMapper 
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
     * 删除OCR规则配置
     * 
     * @param id OCR规则配置主键
     * @return 结果
     */
    public int deleteOcrRuleConfigById(Long id);

    /**
     * 批量删除OCR规则配置
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteOcrRuleConfigByIds(Long[] ids);
}