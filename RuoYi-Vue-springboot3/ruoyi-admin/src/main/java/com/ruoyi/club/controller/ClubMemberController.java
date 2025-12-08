package com.ruoyi.club.controller;

import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysDictData;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.club.domain.ClubMember;
import com.ruoyi.club.service.IClubMemberService;
import com.ruoyi.system.service.ISysDictTypeService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 俱乐部成员信息Controller
 * 
 * @author lhb
 * @date 2025-11-18
 */
@RestController
@RequestMapping("/club/member")
public class ClubMemberController extends BaseController
{
    @Autowired
    private IClubMemberService clubMemberService;
    
    @Autowired
    private ISysDictTypeService dictTypeService;

    /**
     * 查询俱乐部成员信息列表
     */
    @PreAuthorize("@ss.hasPermi('club:member:list')")
    @GetMapping("/list")
    public TableDataInfo list(ClubMember clubMember)
    {
        startPage();
        List<ClubMember> list = clubMemberService.selectClubMemberList(clubMember);
        return getDataTable(list);
    }

    /**
     * 导出俱乐部成员信息列表
     */
    @PreAuthorize("@ss.hasPermi('club:member:export')")
    @Log(title = "俱乐部成员信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ClubMember clubMember)
    {
        List<ClubMember> list = clubMemberService.selectClubMemberList(clubMember);
        
        // 获取主C阵容字典数据
        List<SysDictData> lineupDictList = dictTypeService.selectDictDataByType("sys_lineup");
        
        // 创建导出列表，并将主C阵容值转换为标签
        List<ClubMember> exportList = new ArrayList<>();
        for (ClubMember member : list) {
            // 创建新对象，复制原有属性
            ClubMember exportMember = new ClubMember();
            org.springframework.beans.BeanUtils.copyProperties(member, exportMember);
            
            // 转换主C阵容值
            if (StringUtils.isNotEmpty(exportMember.getMainLineup())) {
                for (SysDictData dict : lineupDictList) {
                    if (dict.getDictValue().equals(exportMember.getMainLineup())) {
                        // 设置转换后的主C阵容标签
                        exportMember.setMainLineup(dict.getDictLabel());
                        break;
                    }
                }
            }
            
            exportList.add(exportMember);
        }
        
        ExcelUtil<ClubMember> util = new ExcelUtil<ClubMember>(ClubMember.class);
        util.exportExcel(response, exportList, "俱乐部成员信息数据");
    }

    /**
     * 获取俱乐部成员信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('club:member:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(clubMemberService.selectClubMemberById(id));
    }

    /**
     * 新增俱乐部成员信息
     */
    @PreAuthorize("@ss.hasPermi('club:member:add')")
    @Log(title = "俱乐部成员信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ClubMember clubMember)
    {
        return toAjax(clubMemberService.insertClubMember(clubMember));
    }

    /**
     * 修改俱乐部成员信息
     */
    @PreAuthorize("@ss.hasPermi('club:member:edit')")
    @Log(title = "俱乐部成员信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ClubMember clubMember)
    {
        return toAjax(clubMemberService.updateClubMember(clubMember));
    }

    /**
     * 删除俱乐部成员信息
     */
    @PreAuthorize("@ss.hasPermi('club:member:remove')")
    @Log(title = "俱乐部成员信息", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(clubMemberService.deleteClubMemberByIds(ids));
    }
    
    /**
     * 获取所有俱乐部成员信息（不分页）
     */
    @PreAuthorize("@ss.hasPermi('club:member:list')")
    @GetMapping("/listAll")
    public AjaxResult listAll()
    {
        ClubMember clubMember = new ClubMember();
        List<ClubMember> list = clubMemberService.selectClubMemberList(clubMember);
        return success(list);
    }
    
    /**
     * 更新红淬炼数量
     */
    @PreAuthorize("@ss.hasPermi('club:member:edit')")
    @PutMapping("/redRefine")
    public AjaxResult updateRedRefine(@RequestBody java.util.Map<String, Object> params)
    {
        Long id = Long.valueOf(params.get("id").toString());
        String operation = params.get("operation").toString();
        
        ClubMember member = clubMemberService.updateRedRefine(id, operation);
        return success(member);
    }

    /**
     * 更新四圣数量
     */
    @PreAuthorize("@ss.hasPermi('club:member:edit')")
    @PutMapping("/fourSacred")
    public AjaxResult updateFourSacred(@RequestBody java.util.Map<String, Object> params)
    {
        Long id = Long.valueOf(params.get("id").toString());
        String operation = params.get("operation").toString();
        
        ClubMember member = clubMemberService.updateFourSacred(id, operation);
        return success(member);
    }
}
