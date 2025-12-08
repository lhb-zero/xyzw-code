package com.ruoyi.club.controller;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.club.domain.SaltFieldRecord;
import com.ruoyi.club.service.IClubMemberService;
import com.ruoyi.club.service.ISaltFieldRecordService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 战绩数据统计Controller
 * 
 * @author lhb
 * @date 2025-11-19
 */
@RestController
@RequestMapping("/club/statistics")
public class StatisticsController extends BaseController
{
    @Autowired
    private ISaltFieldRecordService saltFieldRecordService;
    
    @Autowired
    private IClubMemberService clubMemberService;
    
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 获取战绩统计概览
     */
    @PreAuthorize("@ss.hasPermi('club:statistics:list')")
    @GetMapping("/overview")
    public AjaxResult getOverview()
    {
        Map<String, Object> result = new HashMap<>();
        
        // 总体统计数据
        SaltFieldRecord query = new SaltFieldRecord();
        List<SaltFieldRecord> allRecords = saltFieldRecordService.selectSaltFieldRecordList(query);
        
        int totalRecords = allRecords.size();
        long totalKills = allRecords.stream().mapToLong(r -> r.getKills() != null ? r.getKills() : 0).sum();
        long totalDeaths = allRecords.stream().mapToLong(r -> r.getDeaths() != null ? r.getDeaths() : 0).sum();
        long totalDigs = allRecords.stream().mapToLong(r -> r.getDigs() != null ? r.getDigs() : 0).sum();
        long totalRevives = allRecords.stream().mapToLong(r -> r.getRevives() != null ? r.getRevives() : 0).sum();
        
        // 平均KD
        BigDecimal avgKdRatio = BigDecimal.ZERO;
        if (!allRecords.isEmpty()) {
            avgKdRatio = allRecords.stream()
                .filter(r -> r.getKdRatio() != null)
                .map(SaltFieldRecord::getKdRatio)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(allRecords.stream().mapToLong(r -> r.getKdRatio() != null ? 1 : 0).sum()), 2, BigDecimal.ROUND_HALF_UP);
        }
        
        result.put("totalRecords", totalRecords);
        result.put("totalKills", totalKills);
        result.put("totalDeaths", totalDeaths);
        result.put("totalDigs", totalDigs);
        result.put("totalRevives", totalRevives);
        result.put("avgKdRatio", avgKdRatio);
        
        return success(result);
    }
    
    /**
     * 获取成员战绩排名
     */
    @PreAuthorize("@ss.hasPermi('club:statistics:ranking')")
    @GetMapping("/ranking")
    public AjaxResult getRanking(@RequestParam(value = "type", defaultValue = "kills") String type, 
                                @RequestParam(value = "limit", defaultValue = "10") Integer limit)
    {
        Map<String, Object> result = new HashMap<>();
        
        // 这里简化实现，实际项目中应该在SQL层做聚合查询
        SaltFieldRecord query = new SaltFieldRecord();
        List<SaltFieldRecord> records = saltFieldRecordService.selectSaltFieldRecordList(query);
        
        // 按成员ID聚合数据
        Map<Long, Map<String, Object>> memberStats = new HashMap<>();
        
        for (SaltFieldRecord record : records) {
            Long memberId = record.getMemberId();
            if (!memberStats.containsKey(memberId)) {
                memberStats.put(memberId, new HashMap<>());
                memberStats.get(memberId).put("memberId", memberId);
                memberStats.get(memberId).put("totalKills", 0L);
                memberStats.get(memberId).put("totalDeaths", 0L);
                memberStats.get(memberId).put("totalDigs", 0L);
                memberStats.get(memberId).put("totalRevives", 0L);
                memberStats.get(memberId).put("avgKdRatio", BigDecimal.ZERO);
                memberStats.get(memberId).put("recordCount", 0);
            }
            
            Map<String, Object> stats = memberStats.get(memberId);
            stats.put("totalKills", (Long)stats.get("totalKills") + (record.getKills() != null ? record.getKills() : 0));
            stats.put("totalDeaths", (Long)stats.get("totalDeaths") + (record.getDeaths() != null ? record.getDeaths() : 0));
            stats.put("totalDigs", (Long)stats.get("totalDigs") + (record.getDigs() != null ? record.getDigs() : 0));
            stats.put("totalRevives", (Long)stats.get("totalRevives") + (record.getRevives() != null ? record.getRevives() : 0));
            stats.put("recordCount", (Integer)stats.get("recordCount") + 1);
            
            if (record.getKdRatio() != null) {
                BigDecimal currentKd = (BigDecimal)stats.get("avgKdRatio");
                int count = (Integer)stats.get("recordCount");
                stats.put("avgKdRatio", currentKd.add(record.getKdRatio())
                    .subtract(currentKd)
                    .divide(BigDecimal.valueOf(count), 2, BigDecimal.ROUND_HALF_UP));
            }
        }
        
        // 将Map转换为List并按指定类型排序
        List<Map<String, Object>> rankings = memberStats.values().stream()
            .sorted((a, b) -> {
                if ("kills".equals(type)) {
                    return ((Long)b.get("totalKills")).compareTo((Long)a.get("totalKills"));
                } else if ("deaths".equals(type)) {
                    return ((Long)b.get("totalDeaths")).compareTo((Long)a.get("totalDeaths"));
                } else if ("digs".equals(type)) {
                    return ((Long)b.get("totalDigs")).compareTo((Long)a.get("totalDigs"));
                } else if ("kd".equals(type)) {
                    return ((BigDecimal)b.get("avgKdRatio")).compareTo((BigDecimal)a.get("avgKdRatio"));
                }
                return 0;
            })
            .limit(limit)
            .toList();
        
        // 获取成员信息
        for (Map<String, Object> ranking : rankings) {
            Long memberId = (Long)ranking.get("memberId");
            // 这里应该调用clubMemberService获取成员信息
            // 为简化，直接设置memberId
            ranking.put("memberId", memberId);
        }
        
        result.put("rankings", rankings);
        result.put("type", type);
        result.put("limit", limit);
        
        return success(result);
    }
    
    /**
     * 获取日期范围内的战绩统计
     */
    @PreAuthorize("@ss.hasPermi('club:statistics:timeline')")
    @GetMapping("/timeline")
    public AjaxResult getTimelineStats(@RequestParam(value = "startDate", required = false) String startDate,
                                     @RequestParam(value = "endDate", required = false) String endDate)
    {
        Map<String, Object> result = new HashMap<>();
        
        // 这里简化实现，实际项目中应该在SQL层按日期分组
        SaltFieldRecord query = new SaltFieldRecord();
        List<SaltFieldRecord> records = saltFieldRecordService.selectSaltFieldRecordList(query);
        
        // 按日期分组统计
        Map<String, Map<String, Object>> dailyStats = new HashMap<>();
        
        for (SaltFieldRecord record : records) {
            if (record.getRecordDate() == null) continue;
            
            String dateKey = record.getRecordDate().toString();
            if (!dailyStats.containsKey(dateKey)) {
                dailyStats.put(dateKey, new HashMap<>());
                dailyStats.get(dateKey).put("date", dateKey);
                dailyStats.get(dateKey).put("totalKills", 0L);
                dailyStats.get(dateKey).put("totalDeaths", 0L);
                dailyStats.get(dateKey).put("totalDigs", 0L);
                dailyStats.get(dateKey).put("totalRevives", 0L);
                dailyStats.get(dateKey).put("recordCount", 0);
            }
            
            Map<String, Object> stats = dailyStats.get(dateKey);
            stats.put("totalKills", (Long)stats.get("totalKills") + (record.getKills() != null ? record.getKills() : 0));
            stats.put("totalDeaths", (Long)stats.get("totalDeaths") + (record.getDeaths() != null ? record.getDeaths() : 0));
            stats.put("totalDigs", (Long)stats.get("totalDigs") + (record.getDigs() != null ? record.getDigs() : 0));
            stats.put("totalRevives", (Long)stats.get("totalRevives") + (record.getRevives() != null ? record.getRevives() : 0));
            stats.put("recordCount", (Integer)stats.get("recordCount") + 1);
        }
        
        // 将Map转换为List并按日期排序
        List<Map<String, Object>> timeline = dailyStats.values().stream()
            .sorted((a, b) -> ((String)a.get("date")).compareTo((String)b.get("date")))
            .toList();
        
        result.put("timeline", timeline);
        
        return success(result);
    }
}