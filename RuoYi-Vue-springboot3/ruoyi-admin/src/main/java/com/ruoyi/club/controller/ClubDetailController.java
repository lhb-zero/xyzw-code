package com.ruoyi.club.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.club.domain.ClubMember;
import com.ruoyi.club.domain.ClubDetail;
import com.ruoyi.club.service.IClubDetailService;

/**
 * 俱乐部详情Controller
 * 
 * @author lhb
 * @date 2025-12-06
 */
@RestController
@RequestMapping("/club/detail")
public class ClubDetailController extends BaseController
{
    @Autowired
    private IClubDetailService clubDetailService;

    /**
     * 查询俱乐部详情列表
     */
    @PreAuthorize("@ss.hasPermi('club:detail:list')")
    @GetMapping("/list")
    public AjaxResult list(ClubDetail clubDetail)
    {
        List<ClubDetail> list = clubDetailService.selectClubDetailList(clubDetail);
        return success(list);
    }

    /**
     * 获取俱乐部详情详细信息
     */
    @PreAuthorize("@ss.hasPermi('club:detail:query')")
    @GetMapping(value = "/{teamGroup}")
    public AjaxResult getInfo(@PathVariable("teamGroup") String teamGroup)
    {
        ClubDetail clubDetail = clubDetailService.selectClubDetailByTeamGroup(teamGroup);
        return success(clubDetail);
    }

    /**
     * 获取所有团队概览
     */
    @PreAuthorize("@ss.hasPermi('club:detail:overview')")
    @GetMapping("/overview")
    public AjaxResult getOverview()
    {
        List<ClubDetail> overview = clubDetailService.selectTeamOverview();
        return success(overview);
    }

    /**
     * 获取团别分布数据（用于饼图）
     */
    @PreAuthorize("@ss.hasPermi('club:detail:teamDistribution')")
    @GetMapping("/teamDistribution")
    public AjaxResult getTeamDistribution()
    {
        List<ClubDetail> distribution = clubDetailService.selectTeamDistribution();
        return success(distribution);
    }

    /**
     * 获取阵容分布数据（用于饼图）
     */
    @PreAuthorize("@ss.hasPermi('club:detail:lineupDistribution')")
    @GetMapping("/lineupDistribution")
    public AjaxResult getLineupDistribution()
    {
        List<ClubDetail.LineupStats> distribution = clubDetailService.selectLineupDistribution();
        return success(distribution);
    }

    /**
     * 获取团别战力对比数据（用于柱状图）
     */
    @PreAuthorize("@ss.hasPermi('club:detail:powerComparison')")
    @GetMapping("/powerComparison")
    public AjaxResult getPowerComparison()
    {
        List<ClubDetail> comparison = clubDetailService.selectPowerComparison();
        return success(comparison);
    }

    /**
     * 获取团别红淬炼对比数据（用于柱状图）
     */
    @PreAuthorize("@ss.hasPermi('club:detail:redRefineComparison')")
    @GetMapping("/redRefineComparison")
    public AjaxResult getRedRefineComparison()
    {
        List<ClubDetail> comparison = clubDetailService.selectRedRefineComparison();
        return success(comparison);
    }

    /**
     * 获取团内战力分布数据（用于柱状图）
     */
    @PreAuthorize("@ss.hasPermi('club:detail:powerDistribution')")
    @GetMapping("/powerDistribution/{teamGroup}")
    public AjaxResult getPowerDistribution(@PathVariable String teamGroup)
    {
        List<ClubDetail> distribution = clubDetailService.selectPowerDistributionByTeamGroup(teamGroup);
        return success(distribution);
    }

    /**
     * 获取团内红淬炼分布数据（用于柱状图）
     */
    @PreAuthorize("@ss.hasPermi('club:detail:redRefineDistribution')")
    @GetMapping("/redRefineDistribution/{teamGroup}")
    public AjaxResult getRedRefineDistribution(@PathVariable String teamGroup)
    {
        List<ClubDetail> distribution = clubDetailService.selectRedRefineDistributionByTeamGroup(teamGroup);
        return success(distribution);
    }

    /**
     * 获取团内四圣数量分布数据（用于柱状图）
     */
    @PreAuthorize("@ss.hasPermi('club:detail:fourSacredDistribution')")
    @GetMapping("/fourSacredDistribution/{teamGroup}")
    public AjaxResult getFourSacredDistribution(@PathVariable String teamGroup)
    {
        List<ClubDetail> distribution = clubDetailService.selectFourSacredDistributionByTeamGroup(teamGroup);
        return success(distribution);
    }

    /**
     * 获取团内战力与红淬炼关系数据（用于散点图）
     */
    @PreAuthorize("@ss.hasPermi('club:detail:powerRedRefineScatter')")
    @GetMapping("/powerRedRefineScatter/{teamGroup}")
    public AjaxResult getPowerRedRefineScatter(@PathVariable String teamGroup)
    {
        List<ClubMember> members = clubDetailService.selectMembersByTeamGroup(teamGroup);
        return success(members);
    }

    /**
     * 导出俱乐部详情列表
     */
    @PreAuthorize("@ss.hasPermi('club:detail:export')")
    @Log(title = "俱乐部详情", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ClubDetail clubDetail)
    {
        List<ClubDetail> list = clubDetailService.selectClubDetailList(clubDetail);
        ExcelUtil<ClubDetail> util = new ExcelUtil<ClubDetail>(ClubDetail.class);
        util.exportExcel(response, list, "俱乐部详情数据");
    }
}