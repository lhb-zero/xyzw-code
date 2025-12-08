package com.ruoyi.club.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.club.domain.ResourceLog;
import com.ruoyi.club.mapper.ResourceLogMapper;
import com.ruoyi.club.service.IResourceLogService;

/**
 * 资源日志Service业务层处理
 * 
 * @author lhb
 * @date 2025-11-19
 */
@Service
public class ResourceLogServiceImpl implements IResourceLogService 
{
    @Autowired
    private ResourceLogMapper resourceLogMapper;
    
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 查询资源日志
     * 
     * @param id 资源日志主键
     * @return 资源日志
     */
    @Override
    public ResourceLog selectResourceLogById(Long id)
    {
        return resourceLogMapper.selectResourceLogById(id);
    }

    /**
     * 查询资源日志列表
     * 
     * @param resourceLog 资源日志
     * @return 资源日志
     */
    @Override
    public List<ResourceLog> selectResourceLogList(ResourceLog resourceLog)
    {
        return resourceLogMapper.selectResourceLogList(resourceLog);
    }

    /**
     * 新增资源日志
     * 
     * @param resourceLog 资源日志
     * @return 结果
     */
    @Override
    public int insertResourceLog(ResourceLog resourceLog)
    {
        resourceLog.setCreatedAt(new Date());
        resourceLog.setUpdatedAt(new Date());
        return resourceLogMapper.insertResourceLog(resourceLog);
    }

    /**
     * 修改资源日志
     * 
     * @param resourceLog 资源日志
     * @return 结果
     */
    @Override
    public int updateResourceLog(ResourceLog resourceLog)
    {
        resourceLog.setUpdatedAt(new Date());
        return resourceLogMapper.updateResourceLog(resourceLog);
    }

    /**
     * 批量删除资源日志
     * 
     * @param ids 需要删除的资源日志主键
     * @return 结果
     */
    @Override
    public int deleteResourceLogByIds(Long[] ids)
    {
        return resourceLogMapper.deleteResourceLogByIds(ids);
    }

    /**
     * 删除资源日志信息
     * 
     * @param id 资源日志主键
     * @return 结果
     */
    @Override
    public int deleteResourceLogById(Long id)
    {
        return resourceLogMapper.deleteResourceLogById(id);
    }
    
    /**
     * 根据成员ID查询资源日志
     * 
     * @param memberId 成员ID
     * @return 资源日志集合
     */
    @Override
    public List<ResourceLog> selectResourceLogByMemberId(Long memberId)
    {
        return resourceLogMapper.selectResourceLogByMemberId(memberId);
    }
    
    /**
     * 获取资源对比数据
     * 
     * @param logId1 日志ID1
     * @param logId2 日志ID2
     * @return 对比数据
     */
    @Override
    public Map<String, Object> getResourceComparison(Long logId1, Long logId2)
    {
        Map<String, Object> result = new HashMap<>();
        
        // 获取两个资源日志
        List<ResourceLog> logs = resourceLogMapper.selectResourceLogForComparison(logId1, logId2);
        
        if (logs == null || logs.size() < 2) {
            return result;
        }
        
        ResourceLog log1 = logs.get(0);
        ResourceLog log2 = logs.get(1);
        
        // 确保log1是较新的日志
        if (log1.getRecordDate().before(log2.getRecordDate())) {
            ResourceLog temp = log1;
            log1 = log2;
            log2 = temp;
        }
        
        // 解析资源数据
        Map<String, Object> resources1 = parseResourceData(log1.getResourceData());
        Map<String, Object> resources2 = parseResourceData(log2.getResourceData());
        
        // 计算差值和百分比
        Map<String, Object> diffMap = new HashMap<>();
        Map<String, Object> percentMap = new HashMap<>();
        
        for (String key : resources1.keySet()) {
            Long value1 = getLongValue(resources1.get(key));
            Long value2 = resources2.containsKey(key) ? getLongValue(resources2.get(key)) : 0L;
            
            Long diff = value1 - value2;
            double percent = value2 > 0 ? (diff * 100.0 / value2) : (value1 > 0 ? 100.0 : 0.0);
            
            diffMap.put(key, diff);
            percentMap.put(key, percent);
        }
        
        // 组装结果
        result.put("log1", log1);
        result.put("log2", log2);
        result.put("resources1", resources1);
        result.put("resources2", resources2);
        result.put("diff", diffMap);
        result.put("percent", percentMap);
        
        return result;
    }
    
    /**
     * 解析资源数据JSON
     * 
     * @param jsonData JSON字符串
     * @return 解析后的Map
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseResourceData(String jsonData)
    {
        try {
            if (jsonData == null || jsonData.isEmpty()) {
                return new HashMap<>();
            }
            return objectMapper.readValue(jsonData, Map.class);
        } catch (Exception e) {
            return new HashMap<>();
        }
    }
    
    /**
     * 获取Long值
     * 
     * @param value 对象值
     * @return Long值
     */
    private Long getLongValue(Object value)
    {
        if (value == null) {
            return 0L;
        }
        
        if (value instanceof Long) {
            return (Long) value;
        } else if (value instanceof Integer) {
            return ((Integer) value).longValue();
        } else if (value instanceof Double) {
            return ((Double) value).longValue();
        } else if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                return 0L;
            }
        }
        
        return 0L;
    }
    
    /**
     * 获取多个成员的资源对比数据
     * 
     * @param memberIds 成员ID列表，逗号分隔
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param resourceType 资源类型
     * @return 对比数据
     */
    @Override
    public Map<String, Object> getMultiMemberResourceComparison(String memberIds, String startDate, String endDate, String resourceType)
    {
        Map<String, Object> result = new HashMap<>();
        
        // 解析成员ID列表
        String[] memberIdArray = memberIds.split(",");
        Long[] ids = new Long[memberIdArray.length];
        for (int i = 0; i < memberIdArray.length; i++) {
            try {
                ids[i] = Long.parseLong(memberIdArray[i].trim());
            } catch (NumberFormatException e) {
                ids[i] = 0L;
            }
        }
        
        // 获取指定日期范围内的资源日志
        List<ResourceLog> logs = resourceLogMapper.selectResourceLogByMembersAndDateRange(ids, startDate, endDate, resourceType);
        
        // 按成员ID分组
        Map<Long, List<ResourceLog>> memberLogsMap = new HashMap<>();
        for (ResourceLog log : logs) {
            Long memberId = log.getMemberId();
            if (!memberLogsMap.containsKey(memberId)) {
                memberLogsMap.put(memberId, new ArrayList<>());
            }
            memberLogsMap.get(memberId).add(log);
        }
        
        // 转换为前端需要的格式
        List<Map<String, Object>> memberData = new ArrayList<>();
        for (Long memberId : ids) {
            Map<String, Object> memberInfo = new HashMap<>();
            memberInfo.put("memberId", memberId);
            
            List<Map<String, Object>> changeHistory = new ArrayList<>();
            List<ResourceLog> memberLogs = memberLogsMap.get(memberId);
            if (memberLogs != null) {
                for (ResourceLog log : memberLogs) {
                    Map<String, Object> historyItem = new HashMap<>();
                    historyItem.put("date", log.getRecordDate().toString());
                    changeHistory.add(historyItem);
                }
            }
            
            memberInfo.put("changeHistory", changeHistory);
            memberData.add(memberInfo);
        }
        
        result.put("data", memberData);
        return result;
    }
}