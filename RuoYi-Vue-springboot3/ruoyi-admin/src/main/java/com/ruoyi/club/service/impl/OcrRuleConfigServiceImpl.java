package com.ruoyi.club.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.club.mapper.OcrRuleConfigMapper;
import com.ruoyi.club.domain.OcrRuleConfig;
import com.ruoyi.club.service.IOcrRuleConfigService;

/**
 * OCR规则配置Service业务层处理
 * 
 * @author lhb
 * @date 2025-11-19
 */
@Service
public class OcrRuleConfigServiceImpl implements IOcrRuleConfigService 
{
    @Autowired
    private OcrRuleConfigMapper ocrRuleConfigMapper;

    /**
     * 查询OCR规则配置
     * 
     * @param id OCR规则配置主键
     * @return OCR规则配置
     */
    @Override
    public OcrRuleConfig selectOcrRuleConfigById(Long id)
    {
        return ocrRuleConfigMapper.selectOcrRuleConfigById(id);
    }

    /**
     * 查询OCR规则配置列表
     * 
     * @param ocrRuleConfig OCR规则配置
     * @return OCR规则配置
     */
    @Override
    public List<OcrRuleConfig> selectOcrRuleConfigList(OcrRuleConfig ocrRuleConfig)
    {
        return ocrRuleConfigMapper.selectOcrRuleConfigList(ocrRuleConfig);
    }

    /**
     * 新增OCR规则配置
     * 
     * @param ocrRuleConfig OCR规则配置
     * @return 结果
     */
    @Override
    public int insertOcrRuleConfig(OcrRuleConfig ocrRuleConfig)
    {
        return ocrRuleConfigMapper.insertOcrRuleConfig(ocrRuleConfig);
    }

    /**
     * 修改OCR规则配置
     * 
     * @param ocrRuleConfig OCR规则配置
     * @return 结果
     */
    @Override
    public int updateOcrRuleConfig(OcrRuleConfig ocrRuleConfig)
    {
        return ocrRuleConfigMapper.updateOcrRuleConfig(ocrRuleConfig);
    }

    /**
     * 批量删除OCR规则配置
     * 
     * @param ids 需要删除的OCR规则配置主键
     * @return 结果
     */
    @Override
    public int deleteOcrRuleConfigByIds(Long[] ids)
    {
        return ocrRuleConfigMapper.deleteOcrRuleConfigByIds(ids);
    }

    /**
     * 删除OCR规则配置信息
     * 
     * @param id OCR规则配置主键
     * @return 结果
     */
    @Override
    public int deleteOcrRuleConfigById(Long id)
    {
        return ocrRuleConfigMapper.deleteOcrRuleConfigById(id);
    }
}