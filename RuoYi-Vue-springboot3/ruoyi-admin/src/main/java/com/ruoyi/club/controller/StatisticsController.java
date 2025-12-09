package com.ruoyi.club.controller;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.club.domain.SaltFieldRecord;
import com.ruoyi.club.domain.ClubMember;
import com.ruoyi.club.service.IClubMemberService;
import com.ruoyi.club.service.ISaltFieldRecordService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.utils.DateUtils;
import org.apache.commons.lang3.StringUtils;

import jakarta.servlet.http.HttpServletResponse;

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
    private static final ExcelUtil<SaltFieldRecord> util = new ExcelUtil<SaltFieldRecord>(SaltFieldRecord.class);

    /**
     * 获取战绩统计概览
     */
    @PreAuthorize("@ss.hasPermi('club:statistics:list')")
    @GetMapping("/overview")
    public AjaxResult getOverview(
            @RequestParam(value = "teamGroup", required = false) String teamGroup,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate)
    {
        try {
            Map<String, Object> result = new HashMap<>();
            
            // 构建查询条件
            SaltFieldRecord query = new SaltFieldRecord();
            
            // 按团队筛选
            List<Long> memberIds = null;
            if (StringUtils.isNotBlank(teamGroup)) {
                ClubMember memberQuery = new ClubMember();
                memberQuery.setTeamGroup(teamGroup);
                List<ClubMember> members = clubMemberService.selectClubMemberList(memberQuery);
                System.out.println("Team group " + teamGroup + " has " + members.size() + " members");
                memberIds = members.stream().map(ClubMember::getId).collect(Collectors.toList());
            }
            
            // 获取所有记录
            List<SaltFieldRecord> allRecords = saltFieldRecordService.selectSaltFieldRecordList(query);
            System.out.println("Total records in database: " + allRecords.size());
            
            // 筛选记录
            final List<Long> finalMemberIds = memberIds; // 创建final副本
            final String finalStartDate = startDate; // 创建final副本
            final String finalEndDate = endDate; // 创建final副本
            
            List<SaltFieldRecord> filteredRecords = allRecords.stream()
                    .filter(record -> {
                        try {
                            // 团队筛选
                            if (finalMemberIds != null && record.getMemberId() != null && !finalMemberIds.contains(record.getMemberId())) {
                                return false;
                            }
                            
                            // 日期筛选
                            if (StringUtils.isNotBlank(finalStartDate) && record.getRecordDate() != null) {
                                Date start = DateUtils.parseDate(finalStartDate);
                                if (record.getRecordDate().before(start)) {
                                    return false;
                                }
                            }
                            
                            if (StringUtils.isNotBlank(finalEndDate) && record.getRecordDate() != null) {
                                Date end = DateUtils.parseDate(finalEndDate);
                                if (record.getRecordDate().after(end)) {
                                    return false;
                                }
                            }
                            
                            return true;
                        } catch (Exception e) {
                            System.err.println("Error filtering record: " + e.getMessage());
                            return false;
                        }
                    })
                    .collect(Collectors.toList());
            
            System.out.println("Filtered records count: " + filteredRecords.size());
            
            int totalRecords = filteredRecords.size();
        long totalKills = filteredRecords.stream().mapToLong(r -> r.getKills() != null ? r.getKills() : 0).sum();
        long totalDeaths = filteredRecords.stream().mapToLong(r -> r.getDeaths() != null ? r.getDeaths() : 0).sum();
        long totalDigs = filteredRecords.stream().mapToLong(r -> r.getDigs() != null ? r.getDigs() : 0).sum();
        long totalRevives = filteredRecords.stream().mapToLong(r -> r.getRevives() != null ? r.getRevives() : 0).sum();
        
        // 平均KD
        BigDecimal avgKdRatio = BigDecimal.ZERO;
        if (!filteredRecords.isEmpty()) {
            long kdCount = filteredRecords.stream().mapToLong(r -> r.getKdRatio() != null ? 1 : 0).sum();
            if (kdCount > 0) {
                BigDecimal kdSum = filteredRecords.stream()
                    .filter(r -> r.getKdRatio() != null)
                    .map(SaltFieldRecord::getKdRatio)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                avgKdRatio = kdSum.divide(BigDecimal.valueOf(kdCount), 2, BigDecimal.ROUND_HALF_UP);
            }
        }
        
            result.put("totalRecords", totalRecords);
            result.put("totalKills", totalKills);
            result.put("totalDeaths", totalDeaths);
            result.put("totalDigs", totalDigs);
            result.put("totalRevives", totalRevives);
            result.put("avgKdRatio", avgKdRatio);
            
            return AjaxResult.success(result);
        } catch (Exception e) {
            System.err.println("Error in getOverview: " + e.getMessage());
            e.printStackTrace();
            return AjaxResult.error("获取概览数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取成员战绩排名
     */
    @PreAuthorize("@ss.hasPermi('club:statistics:ranking')")
    @GetMapping("/ranking")
    public AjaxResult getRanking(
            @RequestParam(value = "type", defaultValue = "kills") String type, 
            @RequestParam(value = "limit", defaultValue = "10") Integer limit,
            @RequestParam(value = "teamGroup", required = false) String teamGroup,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate)
    {
        try {
            Map<String, Object> result = new HashMap<>();
            
            // 构建查询条件
            SaltFieldRecord query = new SaltFieldRecord();
            
            // 按团队筛选
            List<Long> memberIds = null;
            if (StringUtils.isNotBlank(teamGroup)) {
                ClubMember memberQuery = new ClubMember();
                memberQuery.setTeamGroup(teamGroup);
                List<ClubMember> members = clubMemberService.selectClubMemberList(memberQuery);
                System.out.println("Team group " + teamGroup + " has " + members.size() + " members");
                memberIds = members.stream().map(ClubMember::getId).collect(Collectors.toList());
            }
            
            // 获取所有记录
            List<SaltFieldRecord> records = saltFieldRecordService.selectSaltFieldRecordList(query);
            System.out.println("Ranking API - Total records: " + records.size());
            
            // 筛选记录
            final List<Long> finalMemberIds = memberIds; // 创建final副本
            final String finalStartDate = startDate; // 创建final副本
            final String finalEndDate = endDate; // 创建final副本
            
            List<SaltFieldRecord> filteredRecords = records.stream()
                    .filter(record -> {
                        try {
                            // 团队筛选
                            if (finalMemberIds != null && record.getMemberId() != null && !finalMemberIds.contains(record.getMemberId())) {
                                return false;
                            }
                            
                            // 日期筛选
                            if (StringUtils.isNotBlank(finalStartDate) && record.getRecordDate() != null) {
                                Date start = DateUtils.parseDate(finalStartDate);
                                if (record.getRecordDate().before(start)) {
                                    return false;
                                }
                            }
                            
                            if (StringUtils.isNotBlank(finalEndDate) && record.getRecordDate() != null) {
                                Date end = DateUtils.parseDate(finalEndDate);
                                if (record.getRecordDate().after(end)) {
                                    return false;
                                }
                            }
                            
                            return true;
                        } catch (Exception e) {
                            System.err.println("Error filtering record: " + e.getMessage());
                            return false;
                        }
                    })
                    .collect(Collectors.toList());
            
            System.out.println("Ranking API - Filtered records: " + filteredRecords.size());
            
            // 按成员ID聚合数据
            Map<Long, Map<String, Object>> memberStats = new HashMap<>();
        
        for (SaltFieldRecord record : filteredRecords) {
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
        }
        
        // 计算平均KD
        for (Map.Entry<Long, Map<String, Object>> entry : memberStats.entrySet()) {
            Map<String, Object> stats = entry.getValue();
            int recordCount = (Integer)stats.get("recordCount");
            if (recordCount > 0) {
                // 获取该成员的所有记录以计算平均KD
                BigDecimal kdSum = filteredRecords.stream()
                    .filter(r -> r.getMemberId().equals(entry.getKey()) && r.getKdRatio() != null)
                    .map(SaltFieldRecord::getKdRatio)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                long kdCount = filteredRecords.stream()
                    .filter(r -> r.getMemberId().equals(entry.getKey()) && r.getKdRatio() != null)
                    .count();
                
                if (kdCount > 0) {
                    stats.put("avgKdRatio", kdSum.divide(BigDecimal.valueOf(kdCount), 2, BigDecimal.ROUND_HALF_UP));
                }
            }
        }
        
        // 将Map转换为List并按指定类型排序
        final String sortType = type; // 创建final副本
        final int sortLimit = limit; // 创建final副本
        
        List<Map<String, Object>> rankings = memberStats.values().stream()
            .sorted((a, b) -> {
                if ("kills".equals(sortType)) {
                    return ((Long)b.get("totalKills")).compareTo((Long)a.get("totalKills"));
                } else if ("deaths".equals(sortType)) {
                    return ((Long)b.get("totalDeaths")).compareTo((Long)a.get("totalDeaths"));
                } else if ("digs".equals(sortType)) {
                    return ((Long)b.get("totalDigs")).compareTo((Long)a.get("totalDigs"));
                } else if ("kd".equals(sortType)) {
                    return ((BigDecimal)b.get("avgKdRatio")).compareTo((BigDecimal)a.get("avgKdRatio"));
                }
                return 0;
            })
            .limit(sortLimit)
            .collect(Collectors.toList());
        
        // 获取成员信息
        List<ClubMember> allMembers = clubMemberService.selectClubMemberList(new ClubMember());
        Map<Long, ClubMember> memberMap = allMembers.stream()
            .collect(Collectors.toMap(ClubMember::getId, member -> member));
        
        for (Map<String, Object> ranking : rankings) {
            Long memberId = (Long)ranking.get("memberId");
            ClubMember member = memberMap.get(memberId);
            if (member != null) {
                ranking.put("gameId", member.getGameId());
            }
        }
        
            result.put("rankings", rankings);
            result.put("type", type);
            result.put("limit", limit);
            
            return AjaxResult.success(result);
        } catch (Exception e) {
            System.err.println("Error in getRanking: " + e.getMessage());
            e.printStackTrace();
            return AjaxResult.error("获取排名数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取日期范围内的战绩统计
     */
    @PreAuthorize("@ss.hasPermi('club:statistics:timeline')")
    @GetMapping("/timeline")
    public AjaxResult getTimelineStats(
            @RequestParam(value = "teamGroup", required = false) String teamGroup,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate)
    {
        Map<String, Object> result = new HashMap<>();
        
        // 构建查询条件
        SaltFieldRecord query = new SaltFieldRecord();
        
        // 按团队筛选
        List<Long> memberIds = null;
        if (StringUtils.isNotBlank(teamGroup)) {
            ClubMember memberQuery = new ClubMember();
            memberQuery.setTeamGroup(teamGroup);
            List<ClubMember> members = clubMemberService.selectClubMemberList(memberQuery);
            memberIds = members.stream().map(ClubMember::getId).collect(Collectors.toList());
        }
        
        // 获取所有记录
        List<SaltFieldRecord> records = saltFieldRecordService.selectSaltFieldRecordList(query);
        System.out.println("Ranking API - Total records: " + records.size());
        
        // 筛选记录
        final List<Long> finalMemberIds = memberIds; // 创建final副本
        final String finalStartDate = startDate; // 创建final副本
        final String finalEndDate = endDate; // 创建final副本
        
        List<SaltFieldRecord> filteredRecords = records.stream()
                .filter(record -> {
                    // 团队筛选
                    if (finalMemberIds != null && !finalMemberIds.contains(record.getMemberId())) {
                        return false;
                    }
                    
                    // 日期筛选
                    if (StringUtils.isNotBlank(finalStartDate) && record.getRecordDate() != null) {
                        Date start = DateUtils.parseDate(finalStartDate);
                        if (record.getRecordDate().before(start)) {
                            return false;
                        }
                    }
                    
                    if (StringUtils.isNotBlank(finalEndDate) && record.getRecordDate() != null) {
                        Date end = DateUtils.parseDate(finalEndDate);
                        if (record.getRecordDate().after(end)) {
                            return false;
                        }
                    }
                    
                    return true;
                })
                .collect(Collectors.toList());
        
        // 按日期分组统计
        Map<String, Map<String, Object>> dailyStats = new HashMap<>();
        
        for (SaltFieldRecord record : filteredRecords) {
            if (record.getRecordDate() == null) continue;
            
            // 格式化日期为 yyyy-MM-dd
            String dateKey = DateUtils.parseDateToStr("yyyy-MM-dd", record.getRecordDate());
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
            .collect(Collectors.toList());
        
        result.put("timeline", timeline);
        
        return AjaxResult.success(result);
    }
    
    /**
     * 获取成员列表统计数据
     */
    @PreAuthorize("@ss.hasPermi('club:statistics:memberList')")
    @GetMapping("/memberList")
    public TableDataInfo getMemberList(
            @RequestParam(value = "teamGroup", required = false) String teamGroup,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize)
    {
        // 构建查询条件
        SaltFieldRecord query = new SaltFieldRecord();
        
        // 按团队筛选
        List<Long> memberIds = null;
        if (StringUtils.isNotBlank(teamGroup)) {
            ClubMember memberQuery = new ClubMember();
            memberQuery.setTeamGroup(teamGroup);
            List<ClubMember> members = clubMemberService.selectClubMemberList(memberQuery);
            memberIds = members.stream().map(ClubMember::getId).collect(Collectors.toList());
        }
        
        // 获取所有记录
        List<SaltFieldRecord> records = saltFieldRecordService.selectSaltFieldRecordList(query);
        System.out.println("Ranking API - Total records: " + records.size());
        
        // 筛选记录
        final List<Long> finalMemberIds = memberIds; // 创建final副本
        final String finalStartDate = startDate; // 创建final副本
        final String finalEndDate = endDate; // 创建final副本
        
        List<SaltFieldRecord> filteredRecords = records.stream()
                .filter(record -> {
                    // 团队筛选
                    if (finalMemberIds != null && !finalMemberIds.contains(record.getMemberId())) {
                        return false;
                    }
                    
                    // 日期筛选
                    if (StringUtils.isNotBlank(finalStartDate) && record.getRecordDate() != null) {
                        Date start = DateUtils.parseDate(finalStartDate);
                        if (record.getRecordDate().before(start)) {
                            return false;
                        }
                    }
                    
                    if (StringUtils.isNotBlank(finalEndDate) && record.getRecordDate() != null) {
                        Date end = DateUtils.parseDate(finalEndDate);
                        if (record.getRecordDate().after(end)) {
                            return false;
                        }
                    }
                    
                    return true;
                })
                .collect(Collectors.toList());
        
        System.out.println("Ranking API - Filtered records: " + filteredRecords.size());
        
        // 按成员ID聚合数据
        Map<Long, Map<String, Object>> memberStats = new HashMap<>();
        Map<Long, Date> lastActiveDates = new HashMap<>();
        
        for (SaltFieldRecord record : filteredRecords) {
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
            
            // 更新最后活跃时间
            Date currentActive = lastActiveDates.get(memberId);
            if (currentActive == null || (record.getRecordDate() != null && record.getRecordDate().after(currentActive))) {
                lastActiveDates.put(memberId, record.getRecordDate() != null ? record.getRecordDate() : record.getCreatedAt());
            }
        }
        
        // 计算平均KD和场均杀敌
        for (Map.Entry<Long, Map<String, Object>> entry : memberStats.entrySet()) {
            final Long memberIdKey = entry.getKey(); // 创建final副本
            Map<String, Object> stats = entry.getValue();
            int recordCount = (Integer)stats.get("recordCount");
            
            if (recordCount > 0) {
                // 计算平均KD
                BigDecimal kdSum = filteredRecords.stream()
                    .filter(r -> r.getMemberId().equals(memberIdKey) && r.getKdRatio() != null)
                    .map(SaltFieldRecord::getKdRatio)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                long kdCount = filteredRecords.stream()
                    .filter(r -> r.getMemberId().equals(memberIdKey) && r.getKdRatio() != null)
                    .count();
                
                if (kdCount > 0) {
                    stats.put("avgKdRatio", kdSum.divide(BigDecimal.valueOf(kdCount), 2, BigDecimal.ROUND_HALF_UP));
                }
                
                // 计算场均杀敌
                stats.put("avgKills", BigDecimal.valueOf((Long)stats.get("totalKills"))
                    .divide(BigDecimal.valueOf(recordCount), 1, BigDecimal.ROUND_HALF_UP));
            }
            
            // 设置最后活跃时间
            stats.put("lastActiveTime", lastActiveDates.get(memberIdKey));
        }
        
        // 获取成员信息
        List<ClubMember> allMembers = clubMemberService.selectClubMemberList(new ClubMember());
        Map<Long, ClubMember> memberMap = allMembers.stream()
            .collect(Collectors.toMap(ClubMember::getId, member -> member));
        
        List<Map<String, Object>> resultList = memberStats.values().stream()
            .map(stats -> {
                Long memberId = (Long)stats.get("memberId");
                ClubMember member = memberMap.get(memberId);
                if (member != null) {
                    stats.put("gameId", member.getGameId());
                    stats.put("avatar", ""); // 可以添加头像路径
                }
                return stats;
            })
            .collect(Collectors.toList());
        
        // 分页处理
        int total = resultList.size();
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        List<Map<String, Object>> pageData = resultList.subList(fromIndex, toIndex);
        
        TableDataInfo dataTable = new TableDataInfo();
        dataTable.setCode(200);
        dataTable.setRows(pageData);
        dataTable.setTotal(total);
        return dataTable;
    }
    
    /**
     * 获取数据来源统计
     */
    @PreAuthorize("@ss.hasPermi('club:statistics:dataSource')")
    @GetMapping("/dataSource")
    public AjaxResult getDataSourceStats(
            @RequestParam(value = "teamGroup", required = false) String teamGroup,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate)
    {
        Map<String, Object> result = new HashMap<>();
        
        // 构建查询条件
        SaltFieldRecord query = new SaltFieldRecord();
        
        // 按团队筛选
        List<Long> memberIds = null;
        if (StringUtils.isNotBlank(teamGroup)) {
            ClubMember memberQuery = new ClubMember();
            memberQuery.setTeamGroup(teamGroup);
            List<ClubMember> members = clubMemberService.selectClubMemberList(memberQuery);
            memberIds = members.stream().map(ClubMember::getId).collect(Collectors.toList());
        }
        
        // 获取所有记录
        List<SaltFieldRecord> records = saltFieldRecordService.selectSaltFieldRecordList(query);
        System.out.println("Ranking API - Total records: " + records.size());
        
        // 筛选记录
        final List<Long> finalMemberIds = memberIds; // 创建final副本
        final String finalStartDate = startDate; // 创建final副本
        final String finalEndDate = endDate; // 创建final副本
        
        List<SaltFieldRecord> filteredRecords = records.stream()
                .filter(record -> {
                    // 团队筛选
                    if (finalMemberIds != null && !finalMemberIds.contains(record.getMemberId())) {
                        return false;
                    }
                    
                    // 日期筛选
                    if (StringUtils.isNotBlank(finalStartDate) && record.getRecordDate() != null) {
                        Date start = DateUtils.parseDate(finalStartDate);
                        if (record.getRecordDate().before(start)) {
                            return false;
                        }
                    }
                    
                    if (StringUtils.isNotBlank(finalEndDate) && record.getRecordDate() != null) {
                        Date end = DateUtils.parseDate(finalEndDate);
                        if (record.getRecordDate().after(end)) {
                            return false;
                        }
                    }
                    
                    return true;
                })
                .collect(Collectors.toList());
        
        // 按数据源分组统计
        Map<String, Long> dataSourceCount = new HashMap<>();
        dataSourceCount.put("ocr", 0L);
        dataSourceCount.put("manual", 0L);
        
        for (SaltFieldRecord record : filteredRecords) {
            String source = record.getDataSource();
            if (StringUtils.isNotBlank(source)) {
                dataSourceCount.put(source, dataSourceCount.getOrDefault(source, 0L) + 1);
            } else {
                // 默认为ocr
                dataSourceCount.put("ocr", dataSourceCount.get("ocr") + 1);
            }
        }
        
        // 转换为List
        List<Map<String, Object>> dataSourceList = dataSourceCount.entrySet().stream()
            .map(entry -> {
                final Map<String, Object> item = new HashMap<>();
                item.put("dataSource", entry.getKey());
                item.put("count", entry.getValue());
                return item;
            })
            .collect(Collectors.toList());
        
        // 直接返回数据列表，不再嵌套一层data
        return AjaxResult.success(dataSourceList);
    }
    
    /**
     * 导出战绩数据
     */
    @PreAuthorize("@ss.hasPermi('club:statistics:export')")
    @GetMapping("/export")
    public void export(
            HttpServletResponse response,
            @RequestParam(value = "teamGroup", required = false) String teamGroup,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate)
    {
        // 构建查询条件
        SaltFieldRecord query = new SaltFieldRecord();
        
        // 按团队筛选
        List<Long> memberIds = null;
        if (StringUtils.isNotBlank(teamGroup)) {
            ClubMember memberQuery = new ClubMember();
            memberQuery.setTeamGroup(teamGroup);
            List<ClubMember> members = clubMemberService.selectClubMemberList(memberQuery);
            memberIds = members.stream().map(ClubMember::getId).collect(Collectors.toList());
        }
        
        // 获取所有记录
        List<SaltFieldRecord> records = saltFieldRecordService.selectSaltFieldRecordList(query);
        System.out.println("Ranking API - Total records: " + records.size());
        
        // 筛选记录
        final List<Long> finalMemberIds = memberIds; // 创建final副本
        final String finalStartDate = startDate; // 创建final副本
        final String finalEndDate = endDate; // 创建final副本
        
        List<SaltFieldRecord> filteredRecords = records.stream()
                .filter(record -> {
                    // 团队筛选
                    if (finalMemberIds != null && !finalMemberIds.contains(record.getMemberId())) {
                        return false;
                    }
                    
                    // 日期筛选
                    if (StringUtils.isNotBlank(finalStartDate) && record.getRecordDate() != null) {
                        Date start = DateUtils.parseDate(finalStartDate);
                        if (record.getRecordDate().before(start)) {
                            return false;
                        }
                    }
                    
                    if (StringUtils.isNotBlank(finalEndDate) && record.getRecordDate() != null) {
                        Date end = DateUtils.parseDate(finalEndDate);
                        if (record.getRecordDate().after(end)) {
                            return false;
                        }
                    }
                    
                    return true;
                })
                .collect(Collectors.toList());
        
        // 获取成员信息
        List<ClubMember> allMembers = clubMemberService.selectClubMemberList(new ClubMember());
        Map<Long, ClubMember> memberMap = allMembers.stream()
            .collect(Collectors.toMap(ClubMember::getId, member -> member));
        
        // 为记录添加成员信息
        for (SaltFieldRecord record : filteredRecords) {
            ClubMember member = memberMap.get(record.getMemberId());
            if (member != null) {
                // 这里可以添加一些额外的字段用于导出
            }
        }
        
        // 导出Excel
        util.exportExcel(response, filteredRecords, "战绩数据");
    }
}