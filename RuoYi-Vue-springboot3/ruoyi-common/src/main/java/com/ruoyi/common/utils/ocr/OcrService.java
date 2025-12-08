package com.ruoyi.common.utils.ocr;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.utils.StringUtils;

/**
 * OCR服务工具类
 * 
 * @author lhb
 */
@Component
public class OcrService 
{
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 从OCR原始文本中提取战绩数据
     * 
     * @param ocrText OCR识别的原始文本
     * @param rules OCR识别规则列表
     * @return 提取的结构化数据
     */
    public Map<String, Object> extractBattleData(String ocrText, List<Map<String, Object>> rules) 
    {
        Map<String, Object> result = new HashMap<>();
        
        if (StringUtils.isEmpty(ocrText) || rules == null || rules.isEmpty()) {
            return result;
        }
        
        for (Map<String, Object> rule : rules) {
            String keyword = (String) rule.get("keyword");
            String targetField = (String) rule.get("targetField");
            String fieldType = (String) rule.get("fieldType");
            
            if (StringUtils.isEmpty(keyword) || StringUtils.isEmpty(targetField)) {
                continue;
            }
            
            // 构建正则表达式模式，用于匹配关键词后的数字
            String patternStr = keyword + "\\s*[:：]?\\s*(\\d+(?:\\.\\d+)?)";
            Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(ocrText);
            
            if (matcher.find()) {
                String valueStr = matcher.group(1);
                Object value = parseValue(valueStr, fieldType);
                result.put(targetField, value);
            }
        }
        
        // 特殊处理：计算KD比例
        if (result.containsKey("kills") && result.containsKey("deaths")) {
            Double kills = convertToDouble(result.get("kills"));
            Double deaths = convertToDouble(result.get("deaths"));
            
            if (deaths != null && deaths > 0) {
                result.put("kdRatio", kills / deaths);
            } else if (kills != null) {
                result.put("kdRatio", kills);
            } else {
                result.put("kdRatio", 0.0);
            }
        }
        
        return result;
    }
    
    /**
     * 根据字段类型解析值
     * 
     * @param valueStr 值字符串
     * @param fieldType 字段类型
     * @return 解析后的值
     */
    private Object parseValue(String valueStr, String fieldType) 
    {
        if (StringUtils.isEmpty(valueStr)) {
            return null;
        }
        
        try {
            if ("int".equals(fieldType)) {
                return Integer.parseInt(valueStr);
            } else if ("decimal".equals(fieldType)) {
                return Double.parseDouble(valueStr);
            } else {
                return valueStr; // 默认返回字符串
            }
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * 将对象转换为Double类型
     * 
     * @param value 值对象
     * @return Double值
     */
    private Double convertToDouble(Object value) 
    {
        if (value == null) {
            return null;
        }
        
        if (value instanceof Double) {
            return (Double) value;
        } else if (value instanceof Integer) {
            return ((Integer) value).doubleValue();
        } else if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        
        return null;
    }
    
    /**
     * 将Map对象转换为JSON字符串
     * 
     * @param data Map对象
     * @return JSON字符串
     */
    public String toJson(Map<String, Object> data) 
    {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            return "{}";
        }
    }
    
    /**
     * 将JSON字符串转换为Map对象
     * 
     * @param json JSON字符串
     * @return Map对象
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> fromJson(String json) 
    {
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            return new HashMap<>();
        }
    }
}