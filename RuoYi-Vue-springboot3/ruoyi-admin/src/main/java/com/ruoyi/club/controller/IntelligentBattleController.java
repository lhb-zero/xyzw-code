package com.ruoyi.club.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.Calendar;

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
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.club.domain.OcrResult;
import com.ruoyi.club.domain.BattleMatchResult;
import com.ruoyi.club.domain.SaltFieldRecord;
import com.ruoyi.club.domain.ClubMember;
import com.ruoyi.club.domain.ClubOcrPending;
import com.ruoyi.club.service.IOcrResultService;
import com.ruoyi.club.service.IBattleMatchResultService;
import com.ruoyi.club.service.IClubMemberService;
import com.ruoyi.club.service.ISaltFieldRecordService;
import com.ruoyi.club.service.IUploadImageService;
import com.ruoyi.club.service.IClubOcrPendingService;

/**
 * 智能战绩录入Controller
 * 
 * @author lhb
 * @date 2025-11-19
 */
@RestController
@RequestMapping("/club/battle")
public class IntelligentBattleController extends BaseController
{
    @Autowired
    private IOcrResultService ocrResultService;
    
    @Autowired
    private IBattleMatchResultService battleMatchResultService;
    
    @Autowired
    private IClubMemberService clubMemberService;
    
    @Autowired
    private ISaltFieldRecordService saltFieldRecordService;
    
    @Autowired
    private IUploadImageService uploadImageService;
    
    @Autowired
    private IClubOcrPendingService clubOcrPendingService;

    /**
     * 上传图片并保存OCR结果
     */
    @PreAuthorize("@ss.hasPermi('club:battle:upload')")
    @Log(title = "战绩图片上传", businessType = BusinessType.INSERT)
    @PostMapping("/upload")
    public AjaxResult uploadImage(@RequestParam("file") MultipartFile file, 
                              @RequestParam("filename") String filename,
                              @RequestParam("text") String text,
                              @RequestParam(value = "lines", required = false) String linesJson) throws Exception
    {
        try {
            // 添加详细日志
            
            // 检查参数有效性
            if (file == null || file.isEmpty()) {
                logger.error("文件为空");
                return error("文件不能为空");
            }
            
            if (filename == null || filename.trim().isEmpty()) {
                logger.error("文件名为空");
                return error("文件名不能为空");
            }
            
            if (text == null) {
                logger.warn("文本为null，设置为空字符串");
                text = "";
            }
            
            // 上传图片并记录
            com.ruoyi.club.domain.UploadImage image = uploadImageService.uploadImage(file, "battle");
            if (image == null) {
                return error("图片上传失败");
            }
            
            // 保存OCR结果
            OcrResult ocrResult = new OcrResult();
            ocrResult.setFilename(filename.trim());
            ocrResult.setText(text);
            
            // 设置图片URL - 使用服务方法获取正确格式的URL
            String imageUrl = uploadImageService.getImageUrl(image.getId());
            ocrResult.setImageUrl(imageUrl);
            
            // 确保状态不为null
            ocrResult.setStatus("matched");
            
            // 设置OCR引擎版本和重试次数
            ocrResult.setOcrEngineVersion("1.0");
            ocrResult.setRetryCount(0);
            ocrResult.setIsConfirmed(0); // 默认未确认
            
            // 解析行文本，优先使用lines参数
            String[] lines;
            if (linesJson != null && !linesJson.trim().isEmpty()) {
                try {
                    // 使用JSON解析lines参数
                    // logger.info("开始解析lines参数: {}", linesJson);
                    com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    lines = objectMapper.readValue(linesJson, String[].class);
                    // logger.info("成功解析lines数组，长度: {}", lines.length);/
                } catch (Exception e) {
                    logger.warn("解析lines参数失败，使用文本分割: " + e.getMessage(), e);
                    if (text != null && !text.trim().isEmpty()) {
                        lines = text.split("\n");
                        // logger.info("使用文本分割，行数: {}", lines.length);
                    } else {
                        lines = new String[0]; // 空数组而不是null
                        // logger.info("文本为空，使用空数组");
                    }
                }
            } else if (text != null && !text.trim().isEmpty()) {
                lines = text.split("\n");
                // logger.info("lines参数为空，使用文本分割，行数: {}", lines.length);
            } else {
                lines = new String[0]; // 空数组而不是null
                // logger.info("lines和文本都为空，使用空数组");
            }
            ocrResult.setLines(lines);
            
            // 保存前的检查日志
            logger.info("准备保存OCR结果: filename={}, imageUrl={}, linesCount={}", 
                filename, imageUrl, lines.length);
            
            // 创建时间会由BaseEntity自动处理
            
            // 保存到数据库
            ocrResultService.insertOcrResult(ocrResult);
            
            return success(ocrResult);
        } catch (Exception e) {
            logger.error("上传图片失败", e);
            return error("上传失败: " + e.getMessage());
        }
    }

    /**
     * 智能匹配战绩到成员并保存到暂存表
     */
    @PreAuthorize("@ss.hasPermi('club:battle:match')")
    @Log(title = "智能匹配战绩", businessType = BusinessType.INSERT)
    @PostMapping("/intelligent-match")
    public AjaxResult intelligentMatch(@RequestBody OcrResult ocrData) throws Exception
    {
        try {
            // 添加详细日志
            // logger.info("开始智能匹配OCR结果: {}", ocrData.getId());
            // logger.info("OCR数据文件名: {}", ocrData.getFilename());
            // logger.info("OCR数据文本: {}", ocrData.getText());
            // logger.info("OCR数据行数: {}", ocrData.getLines() != null ? ocrData.getLines().length : 0);
            
            // 优先使用lines数组解析，如果不存在则使用文本解析
            Map<String, Object> parsedData;
            if (ocrData.getLines() != null && ocrData.getLines().length > 0) {
                // logger.info("使用lines数组解析战绩");
                parsedData = parseOcrLines(ocrData.getLines());
            } else {
                // logger.info("使用文本解析战绩");
                parsedData = parseOcrText(ocrData.getText());
            }
            
            logger.info("解析结果包含条目数: {}", parsedData.containsKey("nicknameStats") ? 
                ((List<?>) parsedData.get("nicknameStats")).size() : 0);
            
            if (parsedData.isEmpty()) {
                return error("未能从OCR文本中解析出战绩数据");
            }
            
            // 获取所有俱乐部成员
            ClubMember queryMember = new ClubMember();
            List<ClubMember> memberList = clubMemberService.selectClubMemberList(queryMember);
            
            // 智能匹配昵称到成员
            List<BattleMatchResult> matchResults = matchNicknamesToMembers(
                (List<Map<String, Object>>) parsedData.get("nicknameStats"), 
                memberList,
                ocrData.getId()
            );
            
            // 保存匹配结果到暂存表
            List<ClubOcrPending> pendingRecords = new ArrayList<>();
            Date currentDate = new Date();
            
            for (BattleMatchResult matchResult : matchResults) {
                ClubOcrPending pending = new ClubOcrPending();
                pending.setOcrResultId(ocrData.getId());
                
                // 设置成员信息
                if (matchResult.getMemberId() != null && matchResult.getMatchedMember() != null) {
                    pending.setMemberId(matchResult.getMemberId());
                    pending.setMemberName(matchResult.getMatchedMember().getGameId());
                } else {
                    // 未匹配到成员，使用昵称
                    pending.setMemberId(null);
                    pending.setMemberName(matchResult.getNickname());
                }
                
                // 设置战绩数据
                pending.setKills(matchResult.getKills());
                pending.setDeaths(matchResult.getDeaths());
                // 处理可能为null的KD比率
                if (matchResult.getKdRatio() != null) {
                    pending.setKdRatio(BigDecimal.valueOf(matchResult.getKdRatio()));
                } else {
                    pending.setKdRatio(BigDecimal.ZERO);
                }
                pending.setDigging(matchResult.getDigs());
                pending.setRevives(matchResult.getRevives());
                
                // 设置战绩日期
                if (matchResult.getBattleDate() != null && !matchResult.getBattleDate().isEmpty()) {
                    try {
                        pending.setRecordDate(new java.text.SimpleDateFormat("yyyy-MM-dd").parse(matchResult.getBattleDate()));
                    } catch (Exception e) {
                        logger.warn("解析battleDate失败: {}, 使用当前日期", matchResult.getBattleDate());
                        pending.setRecordDate(currentDate);
                    }
                } else {
                    pending.setRecordDate(currentDate);
                }
                
                pending.setCreateBy(getUsername());
                pending.setDelFlag("0"); // 设置删除标志为0，表示未删除
                pendingRecords.add(pending);
            }
            
            // 批量插入暂存表
            clubOcrPendingService.batchInsertClubOcrPending(pendingRecords);
            
            // 返回匹配结果，包含暂存表ID
            return success(matchResults);
        } catch (Exception e) {
            return error("智能匹配失败: " + e.getMessage());
        }
    }

    /**
     * 获取未确认的暂存记录列表
     */
    @PreAuthorize("@ss.hasPermi('club:battle:list')")
    @GetMapping("/unconfirmed")
    public TableDataInfo getUnconfirmedList(ClubOcrPending clubOcrPending)
    {
        startPage();
        clubOcrPending.setDelFlag("0"); // 只查询未删除的
        List<ClubOcrPending> list = clubOcrPendingService.selectClubOcrPendingList(clubOcrPending);
        return getDataTable(list);
    }

    /**
     * 确认单个匹配结果
     */
    @PreAuthorize("@ss.hasPermi('club:battle:confirm')")
    @Log(title = "确认战绩", businessType = BusinessType.UPDATE)
    @PostMapping("/confirm")
    public AjaxResult confirmRecord(@RequestParam("recordId") Long recordId,
                                @RequestParam("memberId") Long memberId,
                                @RequestBody(required = false) Map<String, Object> battleData)
    {
        try {
            logger.info("确认单个战绩记录，recordId: {}, memberId: {}", recordId, memberId);
            
            // 更新匹配结果状态
            BattleMatchResult result = battleMatchResultService.selectBattleMatchResultById(recordId);
            if (result == null) {
                return error("记录不存在");
            }
            
            result.setMemberId(memberId);
            result.setConfirmed(1);
            
            // 确保battleDate有值，如果为空则设置为当前日期
            if (result.getBattleDate() == null || result.getBattleDate().isEmpty()) {
                String currentDate = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new Date());
                result.setBattleDate(currentDate);
                logger.info("battleDate为空，设置为当前日期: {}", currentDate);
            }
            
            // 从battleData中提取战绩数据并更新
            if (battleData != null) {
                extractBattleData(result, battleData);
            }
            
            battleMatchResultService.updateBattleMatchResult(result);
            
            // 更新OCR记录，标记为已确认
            OcrResult ocrResult = ocrResultService.selectOcrResultById(result.getOcrResultId());
            if (ocrResult != null) {
                ocrResult.setIsConfirmed(1);
                ocrResult.setConfirmedRecordId(recordId);
                ocrResultService.updateOcrResult(ocrResult);
            }
            
            // 创建正式战绩记录
            SaltFieldRecord record = new SaltFieldRecord();
            record.setMemberId(memberId);
            record.setOcrRecordId(result.getOcrResultId()); // 设置关联OCR记录ID
            
            // 确保recordDate不为null - 强制设置为当前日期
            Date recordDate = new Date();
            if (result.getBattleDate() != null && !result.getBattleDate().isEmpty()) {
                try {
                    recordDate = new java.text.SimpleDateFormat("yyyy-MM-dd").parse(result.getBattleDate());
                    logger.info("解析battleDate成功: {} -> {}", result.getBattleDate(), recordDate);
                } catch (Exception e) {
                    logger.warn("解析battleDate失败: {}, 使用当前日期", result.getBattleDate());
                    recordDate = new Date();
                }
            } else {
                logger.info("battleDate为空，使用当前日期");
            }
            
            // 强制设置recordDate，确保不为null
            record.setRecordDate(recordDate);
            logger.info("设置recordDate: {}", record.getRecordDate());
            
            // 设置创建时间和数据来源
            record.setCreatedAt(new Date());
            record.setDataSource("ocr");
            
            // 处理Integer到Long的类型转换
            if (result.getKills() != null) {
                record.setKills(result.getKills().longValue());
            }
            if (result.getDeaths() != null) {
                record.setDeaths(result.getDeaths().longValue());
            }
            if (result.getDigs() != null) {
                record.setDigs(result.getDigs().longValue());
            }
            if (result.getRevives() != null) {
                record.setRevives(result.getRevives().longValue());
            }
            
            // 处理Double到BigDecimal的类型转换
            if (result.getKdRatio() != null) {
                record.setKdRatio(BigDecimal.valueOf(result.getKdRatio()));
            } else {
                record.setKdRatio(BigDecimal.ZERO);
            }
            
            int insertResult = saltFieldRecordService.insertSaltFieldRecord(record);
            logger.info("插入战绩记录结果: {}", insertResult);
            
            return success("确认成功");
        } catch (Exception e) {
            logger.error("确认战绩失败", e);
            return error("确认失败: " + e.getMessage());
        }
    }

    /**
     * 确认单个暂存记录
     */
    @PreAuthorize("@ss.hasPermi('club:battle:confirm')")
    @Log(title = "确认暂存战绩", businessType = BusinessType.UPDATE)
    @PostMapping("/confirm-pending")
    public AjaxResult confirmPendingRecord(@RequestParam("id") Long id)
    {
        try {
            logger.info("确认单个暂存战绩记录，id: {}", id);
            
            // 从暂存表转移到正式战绩表
            int result = clubOcrPendingService.transferToSaltFieldRecord(id);
            
            if (result > 0) {
                return success("确认成功");
            } else {
                return error("确认失败，记录不存在");
            }
        } catch (Exception e) {
            logger.error("确认暂存战绩失败", e);
            return error("确认失败: " + e.getMessage());
        }
    }

    /**
     * 批量确认暂存记录
     */
    @PreAuthorize("@ss.hasPermi('club:battle:confirm')")
    @Log(title = "批量确认暂存战绩", businessType = BusinessType.UPDATE)
    @PostMapping("/batch-confirm-pending")
    public AjaxResult batchConfirmPendingRecords(@RequestParam("ocrResultId") Long ocrResultId)
    {
        try {
            logger.info("批量确认暂存战绩记录，ocrResultId: {}", ocrResultId);
            
            // 从暂存表批量转移到正式战绩表
            int count = clubOcrPendingService.batchTransferToSaltFieldRecord(ocrResultId);
            
            return success("成功确认 " + count + " 条记录");
        } catch (Exception e) {
            logger.error("批量确认暂存战绩失败", e);
            return error("批量确认失败: " + e.getMessage());
        }
    }

    /**
     * 更新暂存记录（暂存功能）
     */
    @PreAuthorize("@ss.hasPermi('club:battle:edit')")
    @Log(title = "暂存战绩", businessType = BusinessType.UPDATE)
    @PostMapping("/pending/save")
    public AjaxResult savePendingRecord(@RequestBody ClubOcrPending clubOcrPending)
    {
        try {
            if (clubOcrPending.getId() == null) {
                return error("记录ID不能为空");
            }
            
            clubOcrPending.setUpdateBy(getUsername());
            int result = clubOcrPendingService.updateClubOcrPending(clubOcrPending);
            
            if (result > 0) {
                return success("暂存成功");
            } else {
                return error("暂存失败");
            }
        } catch (Exception e) {
            logger.error("暂存战绩失败", e);
            return error("暂存失败: " + e.getMessage());
        }
    }

    /**
     * 新增暂存记录（用于手动输入战绩）
     */
    @PreAuthorize("@ss.hasPermi('club:battle:add')")
    @Log(title = "新增暂存战绩", businessType = BusinessType.INSERT)
    @PostMapping("/pending/add")
    public AjaxResult addPendingRecord(@RequestBody ClubOcrPending clubOcrPending)
    {
        try {
            clubOcrPending.setCreateBy(getUsername());
            clubOcrPending.setDelFlag("0");
            
            // 设置默认创建时间
            if (clubOcrPending.getCreateTime() == null) {
                clubOcrPending.setCreateTime(new Date());
            }
            
            int result = clubOcrPendingService.insertClubOcrPending(clubOcrPending);
            
            if (result > 0) {
                return success("添加成功");
            } else {
                return error("添加失败");
            }
        } catch (Exception e) {
            logger.error("添加暂存战绩失败", e);
            return error("添加失败: " + e.getMessage());
        }
    }

    /**
     * 智能匹配已有暂存记录的成员
     */
    @PreAuthorize("@ss.hasPermi('club:battle:match')")
    @Log(title = "智能匹配暂存记录成员", businessType = BusinessType.UPDATE)
    @PostMapping("/pending/match-members")
    public AjaxResult matchPendingRecords(@RequestBody List<Long> ids)
    {
        try {
            // 获取所有俱乐部成员
            ClubMember queryMember = new ClubMember();
            List<ClubMember> memberList = clubMemberService.selectClubMemberList(queryMember);
            
            // 处理每个暂存记录
            List<ClubOcrPending> updatedRecords = new ArrayList<>();
            
            for (Long id : ids) {
                ClubOcrPending pending = clubOcrPendingService.selectClubOcrPendingById(id);
                if (pending == null || pending.getDelFlag().equals("2")) {
                    continue;
                }
                
                // 如果已经有memberId，跳过
                if (pending.getMemberId() != null) {
                    continue;
                }
                
                // 创建模拟战绩数据用于匹配
                Map<String, Object> stat = new HashMap<>();
                stat.put("nickname", pending.getMemberName());
                stat.put("kills", pending.getKills());
                stat.put("deaths", pending.getDeaths());
                stat.put("digs", pending.getDigging());
                stat.put("revives", pending.getRevives());
                stat.put("kdRatio", pending.getKdRatio() != null ? pending.getKdRatio().doubleValue() : 0.0);
                
                // 智能匹配昵称到成员
                List<BattleMatchResult> matchResults = matchNicknamesToMembers(
                    Arrays.asList(stat), 
                    memberList,
                    pending.getOcrResultId()
                );
                
                // 如果匹配成功，更新暂存记录
                if (!matchResults.isEmpty()) {
                    BattleMatchResult matchResult = matchResults.get(0);
                    if (matchResult.getMemberId() != null && matchResult.getMatchedMember() != null) {
                        pending.setMemberId(matchResult.getMemberId());
                        pending.setMemberName(matchResult.getMatchedMember().getGameId());
                        pending.setUpdateBy(getUsername());
                        pending.setUpdateTime(new Date());
                        updatedRecords.add(pending);
                    }
                }
            }
            
            // 批量更新暂存记录
            int successCount = 0;
            for (ClubOcrPending pending : updatedRecords) {
                int result = clubOcrPendingService.updateClubOcrPending(pending);
                if (result > 0) {
                    successCount++;
                }
            }
            
            return success("成功匹配 " + successCount + " 条记录");
        } catch (Exception e) {
            logger.error("智能匹配暂存记录成员失败", e);
            return error("智能匹配失败: " + e.getMessage());
        }
    }

    /**
     * 更新暂存记录的成员信息
     */
    @PreAuthorize("@ss.hasPermi('club:battle:edit')")
    @Log(title = "更新暂存战绩成员", businessType = BusinessType.UPDATE)
    @PutMapping("/pending/update-member")
    public AjaxResult updatePendingMember(@RequestBody Map<String, Object> params)
    {
        try {
            Long id = Long.valueOf(params.get("id").toString());
            Long memberId = Long.valueOf(params.get("memberId").toString());
            String memberName = params.get("memberName").toString();
            
            logger.info("更新暂存战绩成员，id: {}, memberId: {}, memberName: {}", id, memberId, memberName);
            
            ClubOcrPending pending = clubOcrPendingService.selectClubOcrPendingById(id);
            if (pending == null || pending.getDelFlag().equals("2")) {
                return error("记录不存在");
            }
            
            pending.setMemberId(memberId);
            pending.setMemberName(memberName);
            pending.setUpdateBy(getUsername());
            pending.setUpdateTime(new Date());
            
            int result = clubOcrPendingService.updateClubOcrPending(pending);
            
            if (result > 0) {
                return success("更新成功");
            } else {
                return error("更新失败");
            }
        } catch (Exception e) {
            logger.error("更新暂存战绩成员失败", e);
            return error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除暂存记录
     */
    @PreAuthorize("@ss.hasPermi('club:battle:remove')")
    @Log(title = "删除暂存战绩", businessType = BusinessType.DELETE)
    @DeleteMapping("/pending/{id}")
    public AjaxResult deletePendingRecord(@PathVariable("id") Long id)
    {
        try {
            int result = clubOcrPendingService.deleteClubOcrPendingById(id);
            
            if (result > 0) {
                return success("删除成功");
            } else {
                return error("删除失败");
            }
        } catch (Exception e) {
            logger.error("删除暂存战绩失败", e);
            return error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 获取单个暂存记录
     */
    @PreAuthorize("@ss.hasPermi('club:battle:query')")
    @GetMapping("/pending/{id}")
    public AjaxResult getPendingRecord(@PathVariable("id") Long id)
    {
        ClubOcrPending clubOcrPending = clubOcrPendingService.selectClubOcrPendingById(id);
        return success(clubOcrPending);
    }
    
    /**
     * 清空所有暂存记录
     */
    @PreAuthorize("@ss.hasPermi('club:battle:remove')")
    @Log(title = "清空所有暂存战绩", businessType = BusinessType.DELETE)
    @DeleteMapping("/clear-pending")
    public AjaxResult clearAllPendingRecords()
    {
        try {
            int result = clubOcrPendingService.clearAllPendingRecords();
            
            if (result >= 0) {
                logger.info("成功清空 {} 条暂存记录", result);
                return success("清空成功");
            } else {
                return error("清空失败");
            }
        } catch (Exception e) {
            logger.error("清空所有暂存战绩失败", e);
            return error("清空失败: " + e.getMessage());
        }
    }

    /**
     * 批量确认匹配结果
     */
    @PreAuthorize("@ss.hasPermi('club:battle:confirm')")
    @Log(title = "批量确认战绩", businessType = BusinessType.UPDATE)
    @PostMapping("/batch-confirm")
    public AjaxResult batchConfirmRecords(@RequestBody List<BattleMatchResult> records)
    {
        try {
            int successCount = 0;
            
            logger.info("开始批量确认战绩记录，共 {} 条", records.size());
            
            for (BattleMatchResult result : records) {
                if (result.getMemberId() == null || result.getMatchedMember() == null) {
                    logger.warn("跳过未匹配成员的记录: {}", result.getNickname());
                    continue; // 跳过未匹配成员的记录
                }
                
                result.setConfirmed(1);
                battleMatchResultService.updateBattleMatchResult(result);
                
                // 更新OCR记录，标记为已确认
                OcrResult ocrResult = ocrResultService.selectOcrResultById(result.getOcrResultId());
                if (ocrResult != null) {
                    ocrResult.setIsConfirmed(1);
                    ocrResult.setConfirmedRecordId(result.getId());
                    ocrResultService.updateOcrResult(ocrResult);
                }
                
                // 创建正式战绩记录
                SaltFieldRecord record = new SaltFieldRecord();
                record.setMemberId(result.getMemberId());
                record.setOcrRecordId(result.getOcrResultId()); // 设置关联OCR记录ID
                
                // 确保recordDate不为null - 强制设置为当前日期
                Date recordDate = new Date();
                if (result.getBattleDate() != null && !result.getBattleDate().isEmpty()) {
                    try {
                        recordDate = new java.text.SimpleDateFormat("yyyy-MM-dd").parse(result.getBattleDate());
                        logger.info("批量确认解析battleDate成功: {} -> {}", result.getBattleDate(), recordDate);
                    } catch (Exception e) {
                        logger.warn("批量确认时解析battleDate失败: {}, 使用当前日期", result.getBattleDate());
                        recordDate = new Date();
                    }
                } else {
                    logger.info("批量确认时battleDate为空，使用当前日期");
                }
                
                // 强制设置recordDate，确保不为null
                record.setRecordDate(recordDate);
                logger.info("批量确认设置recordDate: {}", record.getRecordDate());
                
                // 设置创建时间和数据来源
                record.setCreatedAt(new Date());
                record.setDataSource("ocr");
                
                // 处理Integer到Long的类型转换
                if (result.getKills() != null) {
                    record.setKills(result.getKills().longValue());
                }
                if (result.getDeaths() != null) {
                    record.setDeaths(result.getDeaths().longValue());
                }
                if (result.getDigs() != null) {
                    record.setDigs(result.getDigs().longValue());
                }
                if (result.getRevives() != null) {
                    record.setRevives(result.getRevives().longValue());
                }
                
                // 处理Double到BigDecimal的类型转换
                if (result.getKdRatio() != null) {
                    record.setKdRatio(BigDecimal.valueOf(result.getKdRatio()));
                } else {
                    record.setKdRatio(BigDecimal.ZERO);
                }
                
                int insertResult = saltFieldRecordService.insertSaltFieldRecord(record);
                if (insertResult > 0) {
                    successCount++;
                    logger.info("成功插入战绩记录: {}", result.getNickname());
                } else {
                    logger.error("插入战绩记录失败: {}", result.getNickname());
                }
            }
            
            logger.info("批量确认完成，成功确认 {} 条记录", successCount);
            return AjaxResult.success("成功确认 " + successCount + " 条记录", successCount);
        } catch (Exception e) {
            logger.error("批量确认失败", e);
            return error("批量确认失败: " + e.getMessage());
        }
    }

    /**
     * 获取战绩记录列表
     */
    @PreAuthorize("@ss.hasPermi('club:battle:list')")
    @GetMapping("/list")
    public TableDataInfo getBattleList(SaltFieldRecord saltFieldRecord)
    {
        startPage();
        List<SaltFieldRecord> list = saltFieldRecordService.selectSaltFieldRecordList(saltFieldRecord);
        return getDataTable(list);
    }

    /**
     * 更新战绩记录
     */
    @PreAuthorize("@ss.hasPermi('club:battle:edit')")
    @Log(title = "修改战绩", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult updateRecord(@RequestBody SaltFieldRecord saltFieldRecord)
    {
        // 确保recordDate不为null
        if (saltFieldRecord.getRecordDate() == null) {
            saltFieldRecord.setRecordDate(new Date());
            logger.info("更新战绩时recordDate为null，设置为当前日期");
        } else {
            logger.info("更新战绩时recordDate为: {}", saltFieldRecord.getRecordDate());
        }
        
        // 计算KD比例
        if (saltFieldRecord.getKills() != null && saltFieldRecord.getDeaths() != null && 
            saltFieldRecord.getDeaths() > 0) {
            BigDecimal kdRatio = BigDecimal.valueOf((double) saltFieldRecord.getKills() / saltFieldRecord.getDeaths());
            saltFieldRecord.setKdRatio(kdRatio);
        }
        
        int result = saltFieldRecordService.updateSaltFieldRecord(saltFieldRecord);
        logger.info("更新战绩结果: {}", result);
        
        return toAjax(result);
    }

    /**
     * 删除战绩记录
     */
    @PreAuthorize("@ss.hasPermi('club:battle:remove')")
    @Log(title = "删除战绩", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public AjaxResult deleteRecord(@PathVariable Long id)
    {
        return toAjax(saltFieldRecordService.deleteSaltFieldRecordById(id));
    }

    /**
     * 获取全员平均值数据
     */
    @PreAuthorize("@ss.hasPermi('club:battle:list')")
    @GetMapping("/server-averages")
    public AjaxResult getServerAverages(@RequestParam(required = false) String timeRange)
    {
        try {
            // 默认查询本月数据
            if (timeRange == null || timeRange.isEmpty()) {
                timeRange = "thisMonth";
            }
            
            // 创建查询条件
            SaltFieldRecord queryRecord = new SaltFieldRecord();
            List<SaltFieldRecord> records;
            
            // 根据时间范围过滤数据
            switch (timeRange) {
                case "thisWeek":
                    // 查询本周数据
                    records = getRecordsByTimeRange("thisWeek");
                    break;
                case "lastWeek":
                    // 查询上周数据
                    records = getRecordsByTimeRange("lastWeek");
                    break;
                case "thisMonth":
                    // 查询本月数据
                    records = getRecordsByTimeRange("thisMonth");
                    break;
                case "lastMonth":
                    // 查询上月数据
                    records = getRecordsByTimeRange("lastMonth");
                    break;
                case "thisQuarter":
                    // 查询本季度数据
                    records = getRecordsByTimeRange("thisQuarter");
                    break;
                case "thisYear":
                    // 查询今年数据
                    records = getRecordsByTimeRange("thisYear");
                    break;
                case "all":
                default:
                    // 查询全部数据
                    records = saltFieldRecordService.selectSaltFieldRecordList(queryRecord);
                    break;
            }
            
            if (records == null || records.isEmpty()) {
                return error("暂无战绩数据");
            }
            
            // 计算平均值
            Map<String, Object> result = new HashMap<>();
            
            // 计算KD平均值
            double totalKD = records.stream()
                .filter(r -> r.getKdRatio() != null)
                .mapToDouble(r -> r.getKdRatio().doubleValue())
                .sum();
            double avgKD = totalKD / records.size();
            result.put("kd", String.format("%.2f", avgKD));
            
            // 计算击杀平均值
            double totalKills = records.stream()
                .filter(r -> r.getKills() != null)
                .mapToInt(r -> r.getKills().intValue())
                .sum();
            double avgKills = totalKills / records.size();
            result.put("kills", String.format("%.1f", avgKills));
            
            // 计算死亡平均值
            double totalDeaths = records.stream()
                .filter(r -> r.getDeaths() != null)
                .mapToInt(r -> r.getDeaths().intValue())
                .sum();
            double avgDeaths = totalDeaths / records.size();
            result.put("deaths", String.format("%.1f", avgDeaths));
            
            // 计算刨地平均值
            double totalDigs = records.stream()
                .filter(r -> r.getDigs() != null)
                .mapToInt(r -> r.getDigs().intValue())
                .sum();
            double avgDigs = totalDigs / records.size();
            result.put("digs", String.format("%.1f", avgDigs));
            
            // 计算复活平均值
            double totalRevives = records.stream()
                .filter(r -> r.getRevives() != null)
                .mapToInt(r -> r.getRevives().intValue())
                .sum();
            double avgRevives = totalRevives / records.size();
            result.put("revives", String.format("%.1f", avgRevives));
            
            // 记录总数
            result.put("totalRecords", records.size());
            
            return success(result);
        } catch (Exception e) {
            logger.error("获取全员平均值数据失败", e);
            return error("获取全员平均值数据失败: " + e.getMessage());
        }
    }

    /**
     * 根据时间范围获取战绩记录
     */
    private List<SaltFieldRecord> getRecordsByTimeRange(String timeRange)
    {
        SaltFieldRecord queryRecord = new SaltFieldRecord();
        List<SaltFieldRecord> allRecords = saltFieldRecordService.selectSaltFieldRecordList(queryRecord);
        
        if (allRecords == null || allRecords.isEmpty()) {
            return allRecords;
        }
        
        Calendar now = Calendar.getInstance();
        
        switch (timeRange) {
            case "thisWeek":
                // 获取本周第一天（周一）
                Calendar firstDayOfWeek = Calendar.getInstance();
                firstDayOfWeek.set(Calendar.YEAR, now.get(Calendar.YEAR));
                firstDayOfWeek.set(Calendar.MONTH, now.get(Calendar.MONTH));
                firstDayOfWeek.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH));
                // 调整到本周一
                int dayOfWeek = firstDayOfWeek.get(Calendar.DAY_OF_WEEK);
                int daysToMonday = (dayOfWeek == Calendar.SUNDAY) ? 6 : dayOfWeek - Calendar.MONDAY;
                firstDayOfWeek.add(Calendar.DAY_OF_MONTH, -daysToMonday);
                firstDayOfWeek.set(Calendar.HOUR_OF_DAY, 0);
                firstDayOfWeek.set(Calendar.MINUTE, 0);
                firstDayOfWeek.set(Calendar.SECOND, 0);
                firstDayOfWeek.set(Calendar.MILLISECOND, 0);
                
                return allRecords.stream()
                    .filter(r -> r.getRecordDate() != null && r.getRecordDate().after(firstDayOfWeek.getTime()))
                    .collect(Collectors.toList());
                    
            case "lastWeek":
                // 获取上周第一天（周一）
                Calendar firstDayOfLastWeek = Calendar.getInstance();
                firstDayOfLastWeek.set(Calendar.YEAR, now.get(Calendar.YEAR));
                firstDayOfLastWeek.set(Calendar.MONTH, now.get(Calendar.MONTH));
                firstDayOfLastWeek.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH));
                // 调整到本周一
                dayOfWeek = firstDayOfLastWeek.get(Calendar.DAY_OF_WEEK);
                daysToMonday = (dayOfWeek == Calendar.SUNDAY) ? 6 : dayOfWeek - Calendar.MONDAY;
                firstDayOfLastWeek.add(Calendar.DAY_OF_MONTH, -daysToMonday);
                // 再调整到上周
                firstDayOfLastWeek.add(Calendar.DAY_OF_MONTH, -7);
                firstDayOfLastWeek.set(Calendar.HOUR_OF_DAY, 0);
                firstDayOfLastWeek.set(Calendar.MINUTE, 0);
                firstDayOfLastWeek.set(Calendar.SECOND, 0);
                firstDayOfLastWeek.set(Calendar.MILLISECOND, 0);
                
                // 获取上周最后一天（周日）
                Calendar lastDayOfLastWeek = Calendar.getInstance();
                lastDayOfLastWeek.setTime(firstDayOfLastWeek.getTime());
                lastDayOfLastWeek.add(Calendar.DAY_OF_MONTH, 6);
                lastDayOfLastWeek.set(Calendar.HOUR_OF_DAY, 23);
                lastDayOfLastWeek.set(Calendar.MINUTE, 59);
                lastDayOfLastWeek.set(Calendar.SECOND, 59);
                lastDayOfLastWeek.set(Calendar.MILLISECOND, 999);
                
                return allRecords.stream()
                    .filter(r -> r.getRecordDate() != null && 
                             r.getRecordDate().after(firstDayOfLastWeek.getTime()) && 
                             r.getRecordDate().before(lastDayOfLastWeek.getTime()))
                    .collect(Collectors.toList());
                    
            case "thisMonth":
                // 获取本月第一天
                Calendar firstDayOfMonth = Calendar.getInstance();
                firstDayOfMonth.set(Calendar.YEAR, now.get(Calendar.YEAR));
                firstDayOfMonth.set(Calendar.MONTH, now.get(Calendar.MONTH));
                firstDayOfMonth.set(Calendar.DAY_OF_MONTH, 1);
                firstDayOfMonth.set(Calendar.HOUR_OF_DAY, 0);
                firstDayOfMonth.set(Calendar.MINUTE, 0);
                firstDayOfMonth.set(Calendar.SECOND, 0);
                firstDayOfMonth.set(Calendar.MILLISECOND, 0);
                
                return allRecords.stream()
                    .filter(r -> r.getRecordDate() != null && r.getRecordDate().after(firstDayOfMonth.getTime()))
                    .collect(Collectors.toList());
                    
            case "lastMonth":
                // 获取上月第一天
                Calendar firstDayOfLastMonth = Calendar.getInstance();
                firstDayOfLastMonth.set(Calendar.YEAR, now.get(Calendar.YEAR));
                firstDayOfLastMonth.set(Calendar.MONTH, now.get(Calendar.MONTH) - 1);
                firstDayOfLastMonth.set(Calendar.DAY_OF_MONTH, 1);
                firstDayOfLastMonth.set(Calendar.HOUR_OF_DAY, 0);
                firstDayOfLastMonth.set(Calendar.MINUTE, 0);
                firstDayOfLastMonth.set(Calendar.SECOND, 0);
                firstDayOfLastMonth.set(Calendar.MILLISECOND, 0);
                
                // 获取上月最后一天
                Calendar lastDayOfLastMonth = Calendar.getInstance();
                lastDayOfLastMonth.set(Calendar.YEAR, now.get(Calendar.YEAR));
                lastDayOfLastMonth.set(Calendar.MONTH, now.get(Calendar.MONTH));
                lastDayOfLastMonth.set(Calendar.DAY_OF_MONTH, 1);
                lastDayOfLastMonth.add(Calendar.DAY_OF_MONTH, -1);
                lastDayOfLastMonth.set(Calendar.HOUR_OF_DAY, 23);
                lastDayOfLastMonth.set(Calendar.MINUTE, 59);
                lastDayOfLastMonth.set(Calendar.SECOND, 59);
                lastDayOfLastMonth.set(Calendar.MILLISECOND, 999);
                
                return allRecords.stream()
                    .filter(r -> r.getRecordDate() != null && 
                             r.getRecordDate().after(firstDayOfLastMonth.getTime()) && 
                             r.getRecordDate().before(lastDayOfLastMonth.getTime()))
                    .collect(Collectors.toList());
                    
            case "thisQuarter":
                // 获取本季度第一天
                int currentMonth = now.get(Calendar.MONTH);
                int quarterStartMonth = (currentMonth / 3) * 3;
                Calendar firstDayOfQuarter = Calendar.getInstance();
                firstDayOfQuarter.set(Calendar.YEAR, now.get(Calendar.YEAR));
                firstDayOfQuarter.set(Calendar.MONTH, quarterStartMonth);
                firstDayOfQuarter.set(Calendar.DAY_OF_MONTH, 1);
                firstDayOfQuarter.set(Calendar.HOUR_OF_DAY, 0);
                firstDayOfQuarter.set(Calendar.MINUTE, 0);
                firstDayOfQuarter.set(Calendar.SECOND, 0);
                firstDayOfQuarter.set(Calendar.MILLISECOND, 0);
                
                return allRecords.stream()
                    .filter(r -> r.getRecordDate() != null && r.getRecordDate().after(firstDayOfQuarter.getTime()))
                    .collect(Collectors.toList());
                    
            case "thisYear":
                // 获取今年第一天
                Calendar firstDayOfYear = Calendar.getInstance();
                firstDayOfYear.set(Calendar.YEAR, now.get(Calendar.YEAR));
                firstDayOfYear.set(Calendar.MONTH, 0);
                firstDayOfYear.set(Calendar.DAY_OF_MONTH, 1);
                firstDayOfYear.set(Calendar.HOUR_OF_DAY, 0);
                firstDayOfYear.set(Calendar.MINUTE, 0);
                firstDayOfYear.set(Calendar.SECOND, 0);
                firstDayOfYear.set(Calendar.MILLISECOND, 0);
                
                return allRecords.stream()
                    .filter(r -> r.getRecordDate() != null && r.getRecordDate().after(firstDayOfYear.getTime()))
                    .collect(Collectors.toList());
                    
            default:
                return allRecords;
        }
    }

    /**
     * 手动添加战绩
     */
    @PreAuthorize("@ss.hasPermi('club:battle:add')")
    @Log(title = "手动添加战绩", businessType = BusinessType.INSERT)
    @PostMapping("/manual")
    public AjaxResult addManualRecord(@RequestBody SaltFieldRecord saltFieldRecord)
    {
        // 计算KD比例
        if (saltFieldRecord.getKills() != null && saltFieldRecord.getDeaths() != null && 
            saltFieldRecord.getDeaths() > 0) {
            BigDecimal kdRatio = BigDecimal.valueOf((double) saltFieldRecord.getKills() / saltFieldRecord.getDeaths());
            saltFieldRecord.setKdRatio(kdRatio);
        }
        
        return toAjax(saltFieldRecordService.insertSaltFieldRecord(saltFieldRecord));
    }

    /**
     * 解析OCR行数组，提取战绩数据
     * 支持多种表头顺序和字段组合的智能识别
     */
    private Map<String, Object> parseOcrLines(String[] lines) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> nicknameStats = new ArrayList<>();
        
        try {
            logger.info("开始解析OCR行数组，总行数: {}", lines.length);
            
            // 识别日期
            String dateStr = null;
            for (String line : lines) {
                if (line.matches("\\d{4}/\\d{1,2}/\\d{1,2}")) {
                    dateStr = line.trim();
                    logger.info("识别到日期: {}", dateStr);
                    break;
                }
            }
            
            // 干扰词列表，遇到这些词则跳过
            Set<String> stopWords = new HashSet<>();
            stopWords.add("总胜率");
            stopWords.add("总战斗");
            stopWords.add("总击杀");
            stopWords.add("总死亡");
            stopWords.add("总刨地");
            stopWords.add("总用丹");
            stopWords.add("击杀前三");
            stopWords.add("刨地前三");
            stopWords.add("死亡前三");
            stopWords.add("用丹前三");
            stopWords.add("KD前三");
            stopWords.add("平均");
            stopWords.add("总计");
            stopWords.add("总体KD");
            stopWords.add("总体统计");
            
            // 使用昵称为键，用于去重
            Map<String, Map<String, Object>> uniqueStats = new HashMap<>();
            
            // 识别表头模式
            HeaderPattern headerPattern = identifyHeaderPattern(lines);
            logger.info("识别到的表头模式: {}", headerPattern.getDescription());
            
            // 根据识别到的表头模式处理数据
            if (headerPattern.isPatternRecognized()) {
                // 模式1: 有明确的表头行
                logger.info("使用表头模式解析数据");
                processWithHeaderPattern(lines, headerPattern, uniqueStats, stopWords);
            } else {
                // 模式2: 没有明确表头，使用昵称识别模式
                logger.info("使用昵称识别模式解析数据");
                processWithNicknamePattern(lines, uniqueStats, stopWords);
            }
            
            // 将去重后的结果转换为List
            nicknameStats.addAll(uniqueStats.values());
            
            result.put("date", dateStr);
            result.put("nicknameStats", nicknameStats);
            
            logger.info("解析完成，共识别到 {} 条玩家数据", nicknameStats.size());
            return result;
        } catch (Exception e) {
            logger.error("解析OCR行数组时发生异常", e);
            return result; // 返回空结果
        }
    }
    
    /**
     * 表头模式类，用于存储识别到的表头信息
     */
    private static class HeaderPattern {
        private boolean patternRecognized = false;
        private int headerLineIndex = -1;
        private Map<String, Integer> columnMapping = new HashMap<>();
        private String description = "";
        
        public boolean isPatternRecognized() {
            return patternRecognized;
        }
        
        public void setPatternRecognized(boolean recognized) {
            this.patternRecognized = recognized;
        }
        
        public int getHeaderLineIndex() {
            return headerLineIndex;
        }
        
        public void setHeaderLineIndex(int index) {
            this.headerLineIndex = index;
        }
        
        public Map<String, Integer> getColumnMapping() {
            return columnMapping;
        }
        
        public void setColumnMapping(Map<String, Integer> mapping) {
            this.columnMapping = mapping;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String desc) {
            this.description = desc;
        }
    }
    
    /**
     * 识别表头模式
     */
    private HeaderPattern identifyHeaderPattern(String[] lines) {
        HeaderPattern pattern = new HeaderPattern();
        
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            
            // 检查是否包含表头关键词
            if (line.contains("昵称") || line.contains("击杀") || 
                line.contains("死亡") || line.contains("刨地") || 
                line.contains("复活") || line.contains("KD") || line.contains("K/D")) {
                
                pattern.setPatternRecognized(true);
                pattern.setHeaderLineIndex(i);
                
                // 分割表头
                String[] headers = line.split("\\s+");
                Map<String, Integer> columnMapping = new HashMap<>();
                
                for (int j = 0; j < headers.length; j++) {
                    String header = headers[j].trim();
                    if (header.contains("昵称")) {
                        columnMapping.put("nickname", j);
                    } else if (header.contains("K/D") || header.contains("KD") || header.contains("击杀死亡比")) {
                        columnMapping.put("kdRatio", j);
                    } else if (header.contains("击杀")) {
                        columnMapping.put("kills", j);
                    } else if (header.contains("死亡")) {
                        columnMapping.put("deaths", j);
                    } else if (header.contains("刨地") || header.contains("挖地") || header.contains("埋地")) {
                        columnMapping.put("digs", j);
                    } else if (header.contains("复活") || header.contains("救助")) {
                        columnMapping.put("revives", j);
                    }
                }
                
                pattern.setColumnMapping(columnMapping);
                pattern.setDescription("表头行索引: " + i + ", 列映射: " + columnMapping);
                return pattern;
            }
        }
        
        return pattern;
    }
    
    /**
     * 使用表头模式处理数据
     */
    private void processWithHeaderPattern(String[] lines, HeaderPattern headerPattern, 
                                         Map<String, Map<String, Object>> uniqueStats, 
                                         Set<String> stopWords) {
        
        int headerLineIndex = headerPattern.getHeaderLineIndex();
        Map<String, Integer> columnMapping = headerPattern.getColumnMapping();
        
        // 从表头下一行开始处理数据
        for (int i = headerLineIndex + 1; i < lines.length; i++) {
            String line = lines[i].trim();
            
            // 跳过空行和干扰数据
            if (line.isEmpty() || isStopWord(line, stopWords)) {
                continue;
            }
            
            // 分割数据
            String[] values = line.split("\\s+");
            
            // 检查是否有足够的列和数据
            if (values.length < 2) {
                continue;
            }
            
            // 获取昵称
            Integer nicknameCol = columnMapping.get("nickname");
            if (nicknameCol == null || nicknameCol >= values.length || values[nicknameCol].isEmpty()) {
                continue;
            }
            
            String nickname = values[nicknameCol].trim();
            
            // 如果已经处理过这个昵称，跳过
            if (uniqueStats.containsKey(nickname)) {
                continue;
            }
            
            // 创建战绩记录
            Map<String, Object> stat = new HashMap<>();
            stat.put("nickname", nickname);
            
            // 提取各列数据
            extractColumnData(stat, values, columnMapping, "kills", Integer.class);
            extractColumnData(stat, values, columnMapping, "deaths", Integer.class);
            extractColumnData(stat, values, columnMapping, "digs", Integer.class);
            extractColumnData(stat, values, columnMapping, "revives", Integer.class);
            extractColumnData(stat, values, columnMapping, "kdRatio", Double.class);
            
            // 如果没有直接获取到KD值，尝试计算
            if (!stat.containsKey("kdRatio") && stat.containsKey("kills") && stat.containsKey("deaths")) {
                int kills = (Integer) stat.get("kills");
                int deaths = (Integer) stat.get("deaths");
                if (deaths > 0) {
                    stat.put("kdRatio", (double) kills / deaths);
                }
            }
            
            // 尝试从原始行中查找KD值（如果表头识别失败）
            if (!stat.containsKey("kdRatio")) {
                findKdValueFromLine(stat, lines, i);
            }
            
            uniqueStats.put(nickname, stat);
        }
    }
    
    /**
     * 使用昵称模式处理数据（无明确表头）
     */
    private void processWithNicknamePattern(String[] lines, 
                                          Map<String, Map<String, Object>> uniqueStats, 
                                          Set<String> stopWords) {
        
        // 遍历行数组，寻找昵称行
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            
            // 检查是否包含"、"(中文顿号)且不是干扰词，判断可能是昵称
            if (line.contains("、") && !isStopWord(line, stopWords)) {
                // 提取昵称部分
                String[] parts = line.split("、");
                if (parts.length >= 2) {
                    String nickname = parts[0] + "、" + parts[1];
                    
                    // 如果已经处理过这个昵称，跳过
                    if (uniqueStats.containsKey(nickname)) {
                        continue;
                    }
                    
                    // 创建战绩记录
                    Map<String, Object> stat = new HashMap<>();
                    stat.put("nickname", nickname);
                    
                    // 往后查找5个数值
                    int foundValues = 0;
                    for (int j = i + 1; j < lines.length && foundValues < 5; j++) {
                        String nextLine = lines[j].trim();
                        
                        // 跳过空行和干扰数据
                        if (nextLine.isEmpty() || isStopWord(nextLine, stopWords)) {
                            continue;
                        }
                        
                        // 跳过百分比数据
                        if (nextLine.contains("%")) {
                            continue;
                        }
                        
                        // 尝试解析为数字
                        try {
                            int value = Integer.parseInt(nextLine);
                            
                            // 尝试智能识别字段类型
                            String fieldType = identifyFieldType(foundValues, nextLine, j, lines);
                            
                            switch (fieldType) {
                                case "kills":
                                    stat.put("kills", value);
                                    break;
                                case "deaths":
                                    stat.put("deaths", value);
                                    break;
                                case "digs":
                                    stat.put("digs", value);
                                    break;
                                case "revives":
                                    stat.put("revives", value);
                                    break;
                                default:
                                    // 默认按照顺序分配 - 修改为实际数据顺序：击杀、刨地、死亡、复活、KD
                                    if (foundValues == 0) stat.put("kills", value);
                                    else if (foundValues == 1) stat.put("digs", value);
                                    else if (foundValues == 2) stat.put("deaths", value);
                                    else if (foundValues == 3) stat.put("revives", value);
                                    break;
                            }
                            foundValues++;
                        } catch (NumberFormatException e) {
                            // 不是整数，检查是否是KD值（小数）
                            try {
                                double kdValue = Double.parseDouble(nextLine);
                                stat.put("kdRatio", kdValue);
                            } catch (NumberFormatException e2) {
                                // 不是数字，继续查找
                            }
                        }
                    }
                    
                    // 计算KD比例，如果没有直接解析到的话
                    if (stat.containsKey("kills") && stat.containsKey("deaths") && !stat.containsKey("kdRatio")) {
                        int kills = (Integer) stat.get("kills");
                        int deaths = (Integer) stat.get("deaths");
                        if (deaths > 0) {
                            stat.put("kdRatio", (double) kills / deaths);
                        }
                    }
                    
                    uniqueStats.put(nickname, stat);
                }
            }
        }
    }
    
    /**
     * 智能识别字段类型
     */
    private String identifyFieldType(int position, String value, int lineIndex, String[] lines) {
        // 如果是最后一个数字，且后面有小数，可能是KD值
        if (lineIndex + 1 < lines.length) {
            String nextLine = lines[lineIndex + 1].trim();
            if (nextLine.matches("\\d+\\.\\d+") || nextLine.matches("\\d+")) {
                try {
                    Double.parseDouble(nextLine);
                    return "kdRatio";
                } catch (NumberFormatException e) {
                    // 不是数字
                }
            }
        }
        
        // 检查上下文线索
        // 查找前面是否有表头线索
        for (int i = Math.max(0, lineIndex - 10); i < lineIndex; i++) {
            String prevLine = lines[i].trim();
            if (prevLine.contains("击杀") && !prevLine.contains("总击杀")) {
                return position == 0 ? "kills" : "deaths";
            }
            if (prevLine.contains("死亡") && !prevLine.contains("总死亡")) {
                return "deaths";
            }
            if (prevLine.contains("刨地") && !prevLine.contains("总刨地")) {
                return "digs";
            }
            if (prevLine.contains("复活") && !prevLine.contains("总复活")) {
                return "revives";
            }
        }
        
        // 根据数值大小和位置进行推测
        try {
            int intValue = Integer.parseInt(value);
            // 通常击杀数不会大于死亡数太多，反之亦然
            // 刨地数通常比较大
            if (intValue > 50) return "digs";
            
            // 默认按照顺序
            switch (position) {
                case 0: return "kills";
                case 1: return "deaths";
                case 2: return "digs";
                case 3: return "revives";
                default: return "unknown";
            }
        } catch (NumberFormatException e) {
            return "unknown";
        }
    }
    
    /**
     * 从列中提取数据
     */
    private <T> void extractColumnData(Map<String, Object> stat, String[] values, 
                                       Map<String, Integer> columnMapping, 
                                       String fieldName, Class<T> type) {
        Integer colIndex = columnMapping.get(fieldName);
        if (colIndex != null && colIndex < values.length) {
            try {
                String valueStr = values[colIndex].trim();
                if (!valueStr.isEmpty()) {
                    if (type == Integer.class) {
                        stat.put(fieldName, Integer.parseInt(valueStr));
                    } else if (type == Double.class) {
                        stat.put(fieldName, Double.parseDouble(valueStr));
                    }
                }
            } catch (NumberFormatException e) {
                // 解析失败，忽略该字段
            }
        }
    }
    
    /**
     * 从行中查找KD值
     */
    private void findKdValueFromLine(Map<String, Object> stat, String[] lines, int currentIndex) {
        // 向后查找几行，寻找可能是KD值的数字
        for (int i = currentIndex + 1; i < Math.min(currentIndex + 10, lines.length); i++) {
            String line = lines[i].trim();
            if (line.isEmpty() || line.contains("总计") || line.contains("平均") || line.contains("前三")) {
                continue;
            }
            
            // 检查是否是KD值（小数或简单的整数）
            if (line.matches("\\d+\\.\\d+") || line.matches("\\d+")) {
                try {
                    double kdValue = Double.parseDouble(line);
                    // KD值通常在0.1到5.0之间
                    if (kdValue >= 0.1 && kdValue <= 10.0) {
                        stat.put("kdRatio", kdValue);
                        return;
                    }
                } catch (NumberFormatException e) {
                    // 解析失败，继续
                }
            }
        }
    }
    
    /**
     * 检查是否是干扰词
     */
    private boolean isStopWord(String line, Set<String> stopWords) {
        for (String word : stopWords) {
            if (line.contains(word)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 解析OCR文本，提取战绩数据
     */
    private Map<String, Object> parseOcrText(String text) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 按行分割文本
            String[] lines = text.split(" \n");
            // 尝试识别日期
            String dateStr = null;
            for (String line : lines) {
                if (line.matches("\\d{4}/\\d{1,2}/\\d{1,2}")) {
                    dateStr = line.trim();
                    break;
                }
            }
            
            // 尝试提取表头和数据
            Map<String, Integer> columnIndices = new HashMap<>();
            List<Map<String, Object>> nicknameStats = new ArrayList<>();
            
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i].trim();
                
                // 尝试识别表头行
                if (line.contains("昵称") || line.contains("K/D") || 
                    line.contains("击杀") || line.contains("死亡") || 
                    line.contains("刨地") || line.contains("复活")) {
                    
                    String[] headers = line.split("\\s+");
                    for (int j = 0; j < headers.length; j++) {
                        String header = headers[j].trim();
                        if (header.contains("昵称")) {
                            columnIndices.put("nickname", j);
                        } else if (header.contains("K/D") || header.contains("KD")) {
                            columnIndices.put("kdRatio", j);
                        } else if (header.contains("击杀")) {
                            columnIndices.put("kills", j);
                        } else if (header.contains("死亡")) {
                            columnIndices.put("deaths", j);
                        } else if (header.contains("刨地") || header.contains("刨击")) {
                            columnIndices.put("digs", j);
                        } else if (header.contains("复活")) {
                            columnIndices.put("revives", j);
                        }
                    }
                    continue;
                }
                
                // 尝试识别数据行
                if (columnIndices.size() > 0 && !line.contains("总计") && !line.contains("KD")) {
                    String[] values = line.split("\\s+");
                    
                    // 检查是否有昵称列且值非空
                    if (columnIndices.containsKey("nickname") && 
                        columnIndices.get("nickname") < values.length && 
                        StringUtils.isNotEmpty(values[columnIndices.get("nickname")])) {
                        
                        Map<String, Object> stat = new HashMap<>();
                        stat.put("nickname", values[columnIndices.get("nickname")]);
                        
                        // 提取数值数据
                        if (columnIndices.containsKey("kills") && columnIndices.get("kills") < values.length) {
                            try {
                                stat.put("kills", Integer.parseInt(values[columnIndices.get("kills")]));
                            } catch (NumberFormatException e) {
                                // 忽略解析错误
                            }
                        }
                        
                        if (columnIndices.containsKey("deaths") && columnIndices.get("deaths") < values.length) {
                            try {
                                stat.put("deaths", Integer.parseInt(values[columnIndices.get("deaths")]));
                            } catch (NumberFormatException e) {
                                // 忽略解析错误
                            }
                        }
                        
                        if (columnIndices.containsKey("digs") && columnIndices.get("digs") < values.length) {
                            try {
                                stat.put("digs", Integer.parseInt(values[columnIndices.get("digs")]));
                            } catch (NumberFormatException e) {
                                // 忽略解析错误
                            }
                        }
                        
                        if (columnIndices.containsKey("revives") && columnIndices.get("revives") < values.length) {
                            try {
                                stat.put("revives", Integer.parseInt(values[columnIndices.get("revives")]));
                            } catch (NumberFormatException e) {
                                // 忽略解析错误
                            }
                        }
                        
                        if (columnIndices.containsKey("kdRatio") && columnIndices.get("kdRatio") < values.length) {
                            try {
                                stat.put("kdRatio", Double.parseDouble(values[columnIndices.get("kdRatio")]));
                            } catch (NumberFormatException e) {
                                // 忽略解析错误
                            }
                        }
                        
                        nicknameStats.add(stat);
                    }
                }
            }
            
            result.put("date", dateStr);
            result.put("nicknameStats", nicknameStats);
            
            return result;
        } catch (Exception e) {
            return result; // 返回空结果
        }
    }

    /**
     * 智能匹配昵称到俱乐部成员
     * 使用更智能的模糊匹配算法，自动匹配最佳成员
     */
    private List<BattleMatchResult> matchNicknamesToMembers(List<Map<String, Object>> nicknameStats, 
                                                      List<ClubMember> members, 
                                                      Long ocrResultId) {
        List<BattleMatchResult> results = new ArrayList<>();
        
        for (Map<String, Object> stat : nicknameStats) {
            String nickname = (String) stat.get("nickname");
            // logger.info("开始匹配昵称: {}", nickname);
            
            // 使用智能匹配算法
            ClubMember matchedMember = intelligentNicknameMatch(nickname, members);
            
            // 创建匹配结果
            BattleMatchResult result = new BattleMatchResult();
            result.setOcrResultId(ocrResultId);
            result.setNickname(nickname);
            result.setBattleDate(new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()));
            
            if (matchedMember != null) {
                result.setMemberId(matchedMember.getId());
                result.setMatchedMember(matchedMember);
                // logger.info("昵称 {} 成功匹配到成员ID: {}", nickname, matchedMember.getId());
            } else {
                logger.info("昵称 {} 未能匹配到任何成员", nickname);
            }
            
            // 提取战绩数据
            if (stat.containsKey("kills")) {
                result.setKills((Integer) stat.get("kills"));
            }
            if (stat.containsKey("deaths")) {
                result.setDeaths((Integer) stat.get("deaths"));
            }
            if (stat.containsKey("digs")) {
                result.setDigs((Integer) stat.get("digs"));
            }
            if (stat.containsKey("revives")) {
                result.setRevives((Integer) stat.get("revives"));
            }
            
            // 计算KD比例
            Integer kills = result.getKills();
            Integer deaths = result.getDeaths();
            if (kills != null && deaths != null) {
                if (deaths > 0) {
                    result.setKdRatio((double) kills / deaths);
                } else {
                    // 死亡数为0时，直接使用击杀数作为KD比率
                    result.setKdRatio((double) kills);
                }
            }
            
            result.setConfirmed(0); // 默认未确认
            
            results.add(result);
        }
        
        return results;
    }
    
    /**
     * 智能昵称匹配算法
     * 实现多种匹配策略，提高匹配准确性
     * 处理统一前缀问题，提高匹配准确度
     */
    private ClubMember intelligentNicknameMatch(String nickname, List<ClubMember> members) {
        if (nickname == null || nickname.trim().isEmpty() || members == null || members.isEmpty()) {
            return null;
        }
        
        // 处理昵称，去除中文顿号和特殊字符
        String cleanNickname = nickname.replaceAll("[、，,]+", " ").trim();
        
        // 分割昵称部分
        String[] nicknameParts = cleanNickname.split("\\s+");
        
        // 提取核心昵称（去除统一前缀，如"千寻丶"）
        String coreNickname = removeCommonPrefix(cleanNickname);
        String[] coreNicknameParts = coreNickname.split("\\s+");
        
        ClubMember bestMatch = null;
        double bestScore = 0.0;
        String matchType = "";
        
        for (ClubMember member : members) {
            if (member.getGameId() == null || member.getGameId().trim().isEmpty()) {
                continue;
            }
            
            // 优先使用核心昵称进行匹配，避免前缀干扰
            double coreScore = calculateMatchScore(coreNickname, coreNicknameParts, member.getGameId());
            
            // 只有当核心昵称匹配得分较低时，才考虑原始昵称匹配
            double originalScore = 0.0;
            if (coreScore < 0.8) {
                originalScore = calculateMatchScore(cleanNickname, nicknameParts, member.getGameId());
            }
            
            // 选择更高的得分，但优先核心昵称匹配
            double score;
            if (coreScore >= originalScore * 1.2) { // 核心昵称匹配有20%的加分
                score = coreScore;
                matchType = "核心昵称";
            } else {
                score = originalScore;
                matchType = "原始昵称";
            }
            
            // 记录匹配得分和最佳匹配
            if (score > bestScore) {
                bestScore = score;
                bestMatch = member;
                
                // logger.debug("昵称: {}, 成员: {}, {}得分: {}", nickname, member.getGameId(), matchType, score);
            }
            
            // 如果是完美匹配，直接返回
            if (score >= 1.0) {
                // logger.info("找到完美匹配: {} -> {} ({})", nickname, member.getGameId(), matchType);
                return member;
            }
        }
        
        // 提高匹配阈值，避免误匹配
        if (bestScore >= 0.75) { // 提高阈值从0.6到0.75
            // logger.info("找到最佳匹配: {} -> {}, 得分: {}", nickname, bestMatch.getGameId(), bestScore);
            return bestMatch;
        }
        
        // logger.info("昵称 {} 未找到足够好的匹配，最佳得分: {}", nickname, bestScore);
        return null; // 没有找到足够好的匹配
    }
    
    /**
     * 去除昵称中的统一前缀
     * 如"千寻丶星辰" -> "星辰"
     * 改进前缀识别逻辑，提高准确性
     */
    private String removeCommonPrefix(String nickname) {
        if (nickname == null || nickname.trim().isEmpty()) {
            return nickname;
        }
        
        String result = nickname;
        
        // 首先尝试去除长前缀（优先级从高到低）
        String[] longPrefixes = {
            "千寻丶", "千寻、", "千寻,", "千寻 "
        };
        
        for (String prefix : longPrefixes) {
            if (result.startsWith(prefix)) {
                result = result.substring(prefix.length());
                // logger.debug("去除长前缀 '{}'，原昵称: '{}' -> 新昵称: '{}'", prefix, nickname, result);
                break; // 只去除一个长前缀
            }
        }
        
        // 然后尝试去除单字前缀（仅在未去除长前缀的情况下）
        if (result.equals(nickname)) {
            String[] singlePrefixes = {
                "千寻","寻",
            };
            
            for (String prefix : singlePrefixes) {
                if (result.startsWith(prefix) && result.length() > prefix.length()) {
                    result = result.substring(prefix.length());
                    // logger.debug("去除单字前缀 '{}'，原昵称: '{}' -> 新昵称: '{}'", prefix, nickname, result);
                    break; // 只去除一个前缀
                }
            }
        }
        
        // 最后去除分隔符
        result = result.replaceAll("^[丶、，,\\s]+", "");
        
        return result.trim();
    }
    
    /**
     * 计算昵称匹配得分
     * 使用多种策略计算匹配度
     * 改进算法，提高匹配精确性
     */
    private double calculateMatchScore(String cleanNickname, String[] nicknameParts, String memberGameId) {
        if (cleanNickname.equals(memberGameId)) {
            return 1.0; // 完全匹配
        }
        
        // 策略1: 精确部分匹配（检查昵称部分是否精确匹配）
        for (String part : nicknameParts) {
            if (!part.trim().isEmpty() && part.equals(memberGameId)) {
                return 0.95; // 部分完全匹配，给予高分
            }
        }
        
        // 策略2: 检查完全包含关系
        if (cleanNickname.contains(memberGameId) && memberGameId.length() >= 2) {
            // 只有当memberGameId不太短时才给高分
            return 0.8;
        }
        
        if (memberGameId.contains(cleanNickname) && cleanNickname.length() >= 2) {
            // 只有当cleanNickname不太短时才给高分
            return 0.8;
        }
        
        // 策略3: 检查部分包含关系（降低分数）
        double maxPartialScore = 0.0;
        for (String part : nicknameParts) {
            if (!part.trim().isEmpty()) {
                if (part.contains(memberGameId) || memberGameId.contains(part)) {
                    // 部分包含，但不是完全相等，分数较低
                    maxPartialScore = Math.max(maxPartialScore, 0.5);
                }
            }
        }
        
        // 策略4: 基于编辑距离的相似度（降低权重）
        double similarityScore = calculateSimilarity(cleanNickname, memberGameId) * 0.6;
        
        // 策略5: 检查共同字符比例（降低权重）
        double commonCharScore = calculateCommonCharRatio(cleanNickname, memberGameId) * 0.5;
        
        // 综合得分，但确保不超过部分匹配的分数
        double finalScore = Math.max(maxPartialScore, Math.max(similarityScore, commonCharScore));
        
        // 如果分数过高但不是精确匹配，降低分数避免误匹配
        if (finalScore > 0.7 && !cleanNickname.equals(memberGameId) && !containsExactPart(nicknameParts, memberGameId)) {
            finalScore = Math.max(finalScore * 0.7, 0.6);
        }
        
        return finalScore;
    }
    
    /**
     * 检查昵称部分是否包含精确匹配
     */
    private boolean containsExactPart(String[] nicknameParts, String memberGameId) {
        for (String part : nicknameParts) {
            if (part.equals(memberGameId)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 计算字符串相似度（基于编辑距离）
     */
    private double calculateSimilarity(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return 0.0;
        }
        
        int maxLength = Math.max(s1.length(), s2.length());
        if (maxLength == 0) {
            return 1.0;
        }
        
        // 计算编辑距离
        int editDistance = calculateLevenshteinDistance(s1, s2);
        
        // 转换为相似度得分
        return 1.0 - (double) editDistance / maxLength;
    }
    
    /**
     * 计算Levenshtein编辑距离
     */
    private int calculateLevenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }
        
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(Math.min(dp[i - 1][j], dp[i][j - 1]), dp[i - 1][j - 1]);
                }
            }
        }
        
        return dp[s1.length()][s2.length()];
    }
    
    /**
     * 计算共同字符比例
     */
    private double calculateCommonCharRatio(String s1, String s2) {
        Set<Character> chars1 = new HashSet<>();
        Set<Character> chars2 = new HashSet<>();
        
        for (char c : s1.toCharArray()) {
            chars1.add(c);
        }
        
        for (char c : s2.toCharArray()) {
            chars2.add(c);
        }
        
        Set<Character> commonChars = new HashSet<>(chars1);
        commonChars.retainAll(chars2);
        
        int totalUniqueChars = chars1.size() + chars2.size() - commonChars.size();
        if (totalUniqueChars == 0) {
            return 0.0;
        }
        
        return (double) commonChars.size() / totalUniqueChars;
    }

    /**
     * 从battleData中提取战绩数据
     */
    private void extractBattleData(BattleMatchResult result, Map<String, Object> battleData) {
        if (battleData.containsKey("nickname")) {
            result.setNickname((String) battleData.get("nickname"));
        }
        if (battleData.containsKey("kills")) {
            result.setKills((Integer) battleData.get("kills"));
        }
        if (battleData.containsKey("deaths")) {
            result.setDeaths((Integer) battleData.get("deaths"));
        }
        if (battleData.containsKey("digs")) {
            result.setDigs((Integer) battleData.get("digs"));
        }
        if (battleData.containsKey("revives")) {
            result.setRevives((Integer) battleData.get("revives"));
        }
        
        // 重新计算KD比例
        Integer kills = result.getKills();
        Integer deaths = result.getDeaths();
        if (kills != null && deaths != null && deaths > 0) {
            result.setKdRatio((double) kills / deaths);
        }
    }
}