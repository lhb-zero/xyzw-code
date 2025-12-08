package com.ruoyi.club.controller;

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
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.club.domain.SaltFieldRecord;
import com.ruoyi.club.service.ISaltFieldRecordService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 盐场战绩记录Controller
 * 
 * @author lhb
 * @date 2025-11-18
 */
@RestController
@RequestMapping("/club/saltField")
public class SaltFieldRecordController extends BaseController
{
    @Autowired
    private ISaltFieldRecordService saltFieldRecordService;

    /**
     * 查询盐场战绩记录列表
     */
    @PreAuthorize("@ss.hasPermi('club:saltField:list')")
    @GetMapping("/list")
    public TableDataInfo list(SaltFieldRecord saltFieldRecord)
    {
        startPage();
        List<SaltFieldRecord> list = saltFieldRecordService.selectSaltFieldRecordList(saltFieldRecord);
        return getDataTable(list);
    }

    /**
     * 导出盐场战绩记录列表
     */
    @PreAuthorize("@ss.hasPermi('club:saltField:export')")
    @Log(title = "盐场战绩记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SaltFieldRecord saltFieldRecord)
    {
        List<SaltFieldRecord> list = saltFieldRecordService.selectSaltFieldRecordList(saltFieldRecord);
        ExcelUtil<SaltFieldRecord> util = new ExcelUtil<SaltFieldRecord>(SaltFieldRecord.class);
        util.exportExcel(response, list, "盐场战绩记录数据");
    }

    /**
     * 获取盐场战绩记录详细信息
     */
    @PreAuthorize("@ss.hasPermi('club:saltField:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(saltFieldRecordService.selectSaltFieldRecordById(id));
    }

    /**
     * 新增盐场战绩记录
     */
    @PreAuthorize("@ss.hasPermi('club:saltField:add')")
    @Log(title = "盐场战绩记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SaltFieldRecord saltFieldRecord)
    {
        return toAjax(saltFieldRecordService.insertSaltFieldRecord(saltFieldRecord));
    }

    /**
     * 修改盐场战绩记录
     */
    @PreAuthorize("@ss.hasPermi('club:saltField:edit')")
    @Log(title = "盐场战绩记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SaltFieldRecord saltFieldRecord)
    {
        return toAjax(saltFieldRecordService.updateSaltFieldRecord(saltFieldRecord));
    }

    /**
     * 删除盐场战绩记录
     */
    @PreAuthorize("@ss.hasPermi('club:saltField:remove')")
    @Log(title = "盐场战绩记录", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(saltFieldRecordService.deleteSaltFieldRecordByIds(ids));
    }
    
    /**
     * 获取指定成员的盐场战绩记录列表
     */
    @PreAuthorize("@ss.hasPermi('club:member:query')")
    @GetMapping(value = "/member/{memberId}")
    public AjaxResult getMemberRecords(@PathVariable("memberId") Long memberId)
    {
        List<SaltFieldRecord> list = saltFieldRecordService.selectSaltFieldRecordByMemberId(memberId);
        return success(list);
    }
}