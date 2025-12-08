package com.ruoyi.club.service.impl;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.club.mapper.ClubDetailMapper;
import com.ruoyi.club.domain.ClubMember;
import com.ruoyi.club.domain.ClubDetail;
import com.ruoyi.club.service.IClubDetailService;
import com.ruoyi.club.service.IClubMemberService;
import com.ruoyi.common.core.domain.entity.SysDictData;
import com.ruoyi.system.service.ISysDictTypeService;

/**
 * 俱乐部详情Service业务层处理
 * 
 * @author lhb
 * @date 2025-12-06
 */
@Service
public class ClubDetailServiceImpl implements IClubDetailService 
{
    @Autowired
    private ClubDetailMapper clubDetailMapper;
    
    @Autowired
    private IClubMemberService clubMemberService;
    
    @Autowired
    private ISysDictTypeService dictTypeService;

    /**
     * 查询俱乐部详情
     * 
     * @param id 俱乐部详情主键
     * @return 俱乐部详情
     */
    @Override
    public ClubDetail selectClubDetailById(Long id)
    {
        return clubDetailMapper.selectClubDetailById(id);
    }

    /**
     * 根据团别查询俱乐部详情
     * 
     * @param teamGroup 团别
     * @return 俱乐部详情
     */
    @Override
    public ClubDetail selectClubDetailByTeamGroup(String teamGroup)
    {
        // 创建俱乐部详情对象
        ClubDetail clubDetail = new ClubDetail();
        clubDetail.setTeamGroup(teamGroup);
        
        // 查询该团所有成员
        ClubMember queryMember = new ClubMember();
        queryMember.setTeamGroup(teamGroup);
        List<ClubMember> members = clubMemberService.selectClubMemberList(queryMember);
        
        if (members == null || members.isEmpty()) {
            return clubDetail;
        }
        
        // 计算统计数据
        clubDetail.setMemberCount((long) members.size());
        
        // 计算总战力和平均战力
        long totalPower = members.stream().mapToLong(m -> m.getPower() != null ? m.getPower() : 0).sum();
        clubDetail.setTotalPower(totalPower);
        clubDetail.setAvgPower(members.size() > 0 ? totalPower / members.size() : 0);
        
        // 计算总红淬炼和平均红淬炼
        long totalRedRefine = members.stream().mapToLong(m -> m.getRedRefine() != null ? m.getRedRefine() : 0).sum();
        clubDetail.setTotalRedRefine(totalRedRefine);
        clubDetail.setAvgRedRefine(members.size() > 0 ? totalRedRefine / members.size() : 0);
        
        // 计算总四圣和平均四圣
        long totalFourSacred = members.stream().mapToLong(m -> m.getFourSacred() != null ? m.getFourSacred() : 0).sum();
        clubDetail.setTotalFourSacred(totalFourSacred);
        clubDetail.setAvgFourSacred(members.size() > 0 ? totalFourSacred / members.size() : 0);
        
        // 设置统计时间
        clubDetail.setPowerStatTime(DateUtils.dateTimeNow("yyyy-MM-dd HH:mm:ss"));
        
        // 计算阵容分布
        List<ClubDetail.LineupStats> lineupStats = calculateLineupStats(members);
        clubDetail.setLineupStats(lineupStats);
        
        return clubDetail;
    }

    /**
     * 计算阵容分布统计
     */
    private List<ClubDetail.LineupStats> calculateLineupStats(List<ClubMember> members) {
        // 获取主C阵容字典数据
        List<SysDictData> lineupDictList = dictTypeService.selectDictDataByType("sys_lineup");
        
        // 统计阵容数量
        Map<String, Long> lineupCountMap = members.stream()
            .filter(m -> m.getMainLineup() != null)
            .collect(Collectors.groupingBy(ClubMember::getMainLineup, Collectors.counting()));
        
        List<ClubDetail.LineupStats> lineupStats = new ArrayList<>();
        int totalMembers = members.size();
        
        for (Map.Entry<String, Long> entry : lineupCountMap.entrySet()) {
            String lineupType = entry.getKey();
            Long count = entry.getValue();
            
            // 查找阵容标签
            String lineupLabel = lineupType;
            for (SysDictData dict : lineupDictList) {
                if (dict.getDictValue().equals(lineupType)) {
                    lineupLabel = dict.getDictLabel();
                    break;
                }
            }
            
            // 计算占比
            Double percentage = totalMembers > 0 ? (double) count / totalMembers * 100 : 0.0;
            
            ClubDetail.LineupStats stats = new ClubDetail.LineupStats();
            stats.setLineupType(lineupType);
            stats.setLineupLabel(lineupLabel);
            stats.setCount(count);
            stats.setPercentage(Math.round(percentage * 100.0) / 100.0); // 保留两位小数
            
            lineupStats.add(stats);
        }
        
        // 按数量降序排序
        lineupStats.sort((a, b) -> b.getCount().compareTo(a.getCount()));
        
        return lineupStats;
    }

    /**
     * 查询俱乐部详情列表
     * 
     * @param clubDetail 俱乐部详情
     * @return 俱乐部详情
     */
    @Override
    public List<ClubDetail> selectClubDetailList(ClubDetail clubDetail)
    {
        return clubDetailMapper.selectClubDetailList(clubDetail);
    }

    /**
     * 查询所有团队概览
     * 
     * @return 团队概览列表
     */
    @Override
    public List<ClubDetail> selectTeamOverview()
    {
        List<ClubDetail> overview = new ArrayList<>();
        
        // 获取所有团别
        List<ClubMember> allMembers = clubMemberService.selectClubMemberList(new ClubMember());
        Map<String, List<ClubMember>> teamGroupMap = allMembers.stream()
            .filter(m -> m.getTeamGroup() != null)
            .collect(Collectors.groupingBy(ClubMember::getTeamGroup));
        
        // 为每个团别创建概览
        for (Map.Entry<String, List<ClubMember>> entry : teamGroupMap.entrySet()) {
            String teamGroup = entry.getKey();
            List<ClubMember> members = entry.getValue();
            
            ClubDetail teamDetail = new ClubDetail();
            teamDetail.setTeamGroup(teamGroup);
            teamDetail.setMemberCount((long) members.size());
            
            // 计算总战力和平均战力
            long totalPower = members.stream().mapToLong(m -> m.getPower() != null ? m.getPower() : 0).sum();
            teamDetail.setTotalPower(totalPower);
            teamDetail.setAvgPower(members.size() > 0 ? totalPower / members.size() : 0);
            
            // 计算总红淬炼和平均红淬炼
            long totalRedRefine = members.stream().mapToLong(m -> m.getRedRefine() != null ? m.getRedRefine() : 0).sum();
            teamDetail.setTotalRedRefine(totalRedRefine);
            teamDetail.setAvgRedRefine(members.size() > 0 ? totalRedRefine / members.size() : 0);
            
            // 计算总四圣和平均四圣
            long totalFourSacred = members.stream().mapToLong(m -> m.getFourSacred() != null ? m.getFourSacred() : 0).sum();
            teamDetail.setTotalFourSacred(totalFourSacred);
            teamDetail.setAvgFourSacred(members.size() > 0 ? totalFourSacred / members.size() : 0);
            
            // 设置统计时间
            teamDetail.setPowerStatTime(DateUtils.dateTimeNow("yyyy-MM-dd HH:mm:ss"));
            
            overview.add(teamDetail);
        }
        
        // 按团别名称排序
        overview.sort((a, b) -> a.getTeamGroup().compareTo(b.getTeamGroup()));
        
        return overview;
    }

    /**
     * 查询团别分布数据
     * 
     * @return 团别分布数据
     */
    @Override
    public List<ClubDetail> selectTeamDistribution()
    {
        return selectTeamOverview();
    }

    /**
     * 查询阵容分布数据
     * 
     * @return 阵容分布数据
     */
    @Override
    public List<ClubDetail.LineupStats> selectLineupDistribution()
    {
        // 获取所有成员
        List<ClubMember> allMembers = clubMemberService.selectClubMemberList(new ClubMember());
        return calculateLineupStats(allMembers);
    }

    /**
     * 查询团别战力对比数据
     * 
     * @return 团别战力对比数据
     */
    @Override
    public List<ClubDetail> selectPowerComparison()
    {
        return selectTeamOverview();
    }

    /**
     * 查询团别红淬炼对比数据
     * 
     * @return 团别红淬炼对比数据
     */
    @Override
    public List<ClubDetail> selectRedRefineComparison()
    {
        return selectTeamOverview();
    }

    /**
     * 查询团内战力分布数据
     * 
     * @param teamGroup 团别
     * @return 团内战力分布数据
     */
    @Override
    public List<ClubDetail> selectPowerDistributionByTeamGroup(String teamGroup)
    {
        // 查询该团所有成员
        ClubMember queryMember = new ClubMember();
        queryMember.setTeamGroup(teamGroup);
        List<ClubMember> members = clubMemberService.selectClubMemberList(queryMember);
        
        if (members == null || members.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 统计不同战力段的人数（单位：亿）
        long[] powerRanges = new long[4]; // <100亿, 100-120亿, 120-140亿, >=150亿
        
        for (ClubMember member : members) {
            Long power = member.getPower() != null ? member.getPower() : 0;
            if (power < 100) {
                powerRanges[0]++; // <100亿
            } else if (power >= 100 && power < 120) {
                powerRanges[1]++; // 100-120亿
            } else if (power >= 120 && power < 140) {
                powerRanges[2]++; // 120-140亿
            } else {
                powerRanges[3]++; // >=140亿（包含140-150亿和>150亿）
            }
        }
        
        // 创建分布数据
        List<ClubDetail> distribution = new ArrayList<>();
        String[] rangeLabels = new String[]{"<100亿", "100-120亿", "120-140亿", "≥150亿"};
        
        for (int i = 0; i < rangeLabels.length; i++) {
            ClubDetail item = new ClubDetail();
            item.setTeamGroup(rangeLabels[i]);
            item.setMemberCount(powerRanges[i]);
            distribution.add(item);
        }
        
        return distribution;
    }

    /**
     * 查询团内红淬炼分布数据
     * 
     * @param teamGroup 团别
     * @return 团内红淬炼分布数据
     */
    @Override
    public List<ClubDetail> selectRedRefineDistributionByTeamGroup(String teamGroup)
    {
        // 查询该团所有成员
        ClubMember queryMember = new ClubMember();
        queryMember.setTeamGroup(teamGroup);
        List<ClubMember> members = clubMemberService.selectClubMemberList(queryMember);
        
        if (members == null || members.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 统计不同红淬炼段的人数
        long[] refineRanges = new long[4]; // <30, 30-40, 40-50, >50
        
        for (ClubMember member : members) {
            Long redRefine = member.getRedRefine() != null ? member.getRedRefine() : 0;
            if (redRefine < 30) {
                refineRanges[0]++;
            } else if (redRefine >= 30 && redRefine < 40) {
                refineRanges[1]++;
            } else if (redRefine >= 40 && redRefine < 50) {
                refineRanges[2]++;
            } else {
                refineRanges[3]++;
            }
        }
        
        // 创建分布数据
        List<ClubDetail> distribution = new ArrayList<>();
        String[] rangeLabels = new String[]{"<30", "30-40", "40-50", ">50"};
        
        for (int i = 0; i < rangeLabels.length; i++) {
            ClubDetail item = new ClubDetail();
            item.setTeamGroup(rangeLabels[i]);
            item.setMemberCount(refineRanges[i]);
            distribution.add(item);
        }
        
        return distribution;
    }

    /**
     * 查询团内四圣数量分布数据
     * 
     * @param teamGroup 团别
     * @return 团内四圣数量分布数据
     */
    @Override
    public List<ClubDetail> selectFourSacredDistributionByTeamGroup(String teamGroup)
    {
        // 查询该团所有成员
        ClubMember queryMember = new ClubMember();
        queryMember.setTeamGroup(teamGroup);
        List<ClubMember> members = clubMemberService.selectClubMemberList(queryMember);
        
        if (members == null || members.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 统计每个具体四圣数量的人数
        Map<Integer, Long> sacredCountMap = new HashMap<>();
        
        for (ClubMember member : members) {
            Integer fourSacred = member.getFourSacred() != null ? member.getFourSacred().intValue() : 0;
            sacredCountMap.put(fourSacred, sacredCountMap.getOrDefault(fourSacred, 0L) + 1);
        }
        
        // 创建分布数据，按数量从小到大排序
        List<ClubDetail> distribution = new ArrayList<>();
        
        // 先找出最大值，确定需要显示的数量范围
        int maxSacred = sacredCountMap.keySet().stream().max(Integer::compare).orElse(0);
        
        // 从0到最大值，每个数量都创建一个条目
        for (int i = 0; i <= Math.max(maxSacred, 8); i++) {
            ClubDetail item = new ClubDetail();
            // 对于数量大于0的，直接显示具体数字；对于0的，显示"无"
            item.setTeamGroup(i == 0 ? "无" : String.valueOf(i));
            item.setMemberCount(sacredCountMap.getOrDefault(i, 0L));
            distribution.add(item);
        }
        
        // 如果有成员的四圣数量大于8，将这些大于8的合并为一个"8+"类别
        if (maxSacred > 8) {
            long countAbove8 = sacredCountMap.entrySet().stream()
                .filter(entry -> entry.getKey() > 8)
                .mapToLong(Map.Entry::getValue)
                .sum();
            
            // 移除所有大于8的条目
            distribution.removeIf(item -> {
                String itemTeamGroup = item.getTeamGroup();
                try {
                    int value = Integer.parseInt(itemTeamGroup);
                    return value > 8;
                } catch (NumberFormatException e) {
                    return false;
                }
            });
            
            // 添加"8+"条目
            ClubDetail item = new ClubDetail();
            item.setTeamGroup("8+");
            item.setMemberCount(countAbove8);
            distribution.add(item);
        }
        
        return distribution;
    }

    /**
     * 查询团内成员列表（用于散点图）
     * 
     * @param teamGroup 团别
     * @return 团内成员列表
     */
    @Override
    public List<ClubMember> selectMembersByTeamGroup(String teamGroup)
    {
        ClubMember queryMember = new ClubMember();
        queryMember.setTeamGroup(teamGroup);
        return clubMemberService.selectClubMemberList(queryMember);
    }

    /**
     * 新增俱乐部详情
     * 
     * @param clubDetail 俱乐部详情
     * @return 结果
     */
    @Override
    public int insertClubDetail(ClubDetail clubDetail)
    {
        return clubDetailMapper.insertClubDetail(clubDetail);
    }

    /**
     * 修改俱乐部详情
     * 
     * @param clubDetail 俱乐部详情
     * @return 结果
     */
    @Override
    public int updateClubDetail(ClubDetail clubDetail)
    {
        return clubDetailMapper.updateClubDetail(clubDetail);
    }

    /**
     * 批量删除俱乐部详情
     * 
     * @param ids 需要删除的俱乐部详情主键
     * @return 结果
     */
    @Override
    public int deleteClubDetailByIds(Long[] ids)
    {
        return clubDetailMapper.deleteClubDetailByIds(ids);
    }

    /**
     * 删除俱乐部详情信息
     * 
     * @param id 俱乐部详情主键
     * @return 结果
     */
    @Override
    public int deleteClubDetailById(Long id)
    {
        return clubDetailMapper.deleteClubDetailById(id);
    }
}