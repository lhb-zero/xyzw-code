package com.ruoyi.club.controller;

import java.util.List;
import java.util.Map;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.club.domain.ResourceLog;
import com.ruoyi.club.service.IResourceLogService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 资源日志Controller
 * 
 * @author lhb
 * @date 2025-11-19
 */
@RestController
@RequestMapping("/club/log")
public class ResourceLogController extends BaseController
{
    @Autowired
    private IResourceLogService resourceLogService;

    /**
     * 查询资源日志列表
     */
    @PreAuthorize("@ss.hasPermi('club:log:list')")
    @GetMapping("/list")
    public TableDataInfo list(ResourceLog resourceLog)
    {
        startPage();
        List<ResourceLog> list = resourceLogService.selectResourceLogList(resourceLog);
        return getDataTable(list);
    }

    /**
     * 导出资源日志列表
     */
    @PreAuthorize("@ss.hasPermi('club:log:export')")
    @Log(title = "资源日志", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ResourceLog resourceLog)
    {
        List<ResourceLog> list = resourceLogService.selectResourceLogList(resourceLog);
        ExcelUtil<ResourceLog> util = new ExcelUtil<ResourceLog>(ResourceLog.class);
        util.exportExcel(response, list, "资源日志数据");
    }

    /**
     * 获取资源日志详细信息
     */
    @PreAuthorize("@ss.hasPermi('club:log:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(resourceLogService.selectResourceLogById(id));
    }

    /**
     * 新增资源日志
     */
    @PreAuthorize("@ss.hasPermi('club:log:add')")
    @Log(title = "资源日志", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ResourceLog resourceLog)
    {
        return toAjax(resourceLogService.insertResourceLog(resourceLog));
    }

    /**
     * 修改资源日志
     */
    @PreAuthorize("@ss.hasPermi('club:log:edit')")
    @Log(title = "资源日志", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ResourceLog resourceLog)
    {
        return toAjax(resourceLogService.updateResourceLog(resourceLog));
    }

    /**
     * 删除资源日志
     */
    @PreAuthorize("@ss.hasPermi('club:log:remove')")
    @Log(title = "资源日志", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(resourceLogService.deleteResourceLogByIds(ids));
    }
    
    /**
     * 获取指定成员的资源日志列表
     */
    @PreAuthorize("@ss.hasPermi('club:member:query')")
    @GetMapping(value = "/member/{memberId}")
    public AjaxResult getMemberLogs(@PathVariable("memberId") Long memberId)
    {
        List<ResourceLog> list = resourceLogService.selectResourceLogByMemberId(memberId);
        return success(list);
    }
    
    /**
     * 获取资源对比数据
     */
    @PreAuthorize("@ss.hasPermi('club:log:query')")
    @GetMapping(value = "/compare")
    public AjaxResult compareResources(@RequestParam("logId1") Long logId1, 
                                    @RequestParam("logId2") Long logId2)
    {
        Map<String, Object> result = resourceLogService.getResourceComparison(logId1, logId2);
        return success(result);
    }
    
    /**
     * 获取多个成员的资源对比数据
     */
    @PreAuthorize("@ss.hasPermi('club:log:query')")
    @GetMapping(value = "/resourceComparison")
    public AjaxResult getResourceComparison(@RequestParam("memberIds") String memberIds,
                                         @RequestParam("startDate") String startDate,
                                         @RequestParam("endDate") String endDate,
                                         @RequestParam("resourceType") String resourceType)
    {
        Map<String, Object> result = resourceLogService.getMultiMemberResourceComparison(memberIds, startDate, endDate, resourceType);
        return success(result);
    }
}