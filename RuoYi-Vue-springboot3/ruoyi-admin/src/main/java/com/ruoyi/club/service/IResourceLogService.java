package com.ruoyi.club.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.club.domain.ResourceLog;

/**
 * 资源日志Service接口
 * 
 * @author lhb
 * @date 2025-11-19
 */
public interface IResourceLogService 
{
    /**
     * 查询资源日志
     * 
     * @param id 资源日志主键
     * @return 资源日志
     */
    public ResourceLog selectResourceLogById(Long id);

    /**
     * 查询资源日志列表
     * 
     * @param resourceLog 资源日志
     * @return 资源日志集合
     */
    public List<ResourceLog> selectResourceLogList(ResourceLog resourceLog);

    /**
     * 新增资源日志
     * 
     * @param resourceLog 资源日志
     * @return 结果
     */
    public int insertResourceLog(ResourceLog resourceLog);

    /**
     * 修改资源日志
     * 
     * @param resourceLog 资源日志
     * @return 结果
     */
    public int updateResourceLog(ResourceLog resourceLog);

    /**
     * 批量删除资源日志
     * 
     * @param ids 需要删除的资源日志主键集合
     * @return 结果
     */
    public int deleteResourceLogByIds(Long[] ids);

    /**
     * 删除资源日志信息
     * 
     * @param id 资源日志主键
     * @return 结果
     */
    public int deleteResourceLogById(Long id);
    
    /**
     * 根据成员ID查询资源日志
     * 
     * @param memberId 成员ID
     * @return 资源日志集合
     */
    public List<ResourceLog> selectResourceLogByMemberId(Long memberId);
    
    /**
     * 获取资源对比数据
     * 
     * @param logId1 日志ID1
     * @param logId2 日志ID2
     * @return 对比数据
     */
    public Map<String, Object> getResourceComparison(Long logId1, Long logId2);
    
    /**
     * 获取多个成员的资源对比数据
     * 
     * @param memberIds 成员ID列表，逗号分隔
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param resourceType 资源类型
     * @return 对比数据
     */
    public Map<String, Object> getMultiMemberResourceComparison(String memberIds, String startDate, String endDate, String resourceType);
}