<template>
  <div class="app-container">
    <!-- OCR服务状态卡片 -->
    <ocr-status-card
      :ocr-status="ocrStatus"
      :ocr-initializing="ocrInitializing"
      @refresh-status="checkOcrStatus"
      @init-ocr="initOcrService"
    />

    <el-row :gutter="20" class="mt-20">
      <!-- 左侧：上传区域或手动输入 -->
      <el-col :span="12">
        <el-tabs v-model="activeLeftTab" type="border-card">
          <el-tab-pane label="OCR图片上传" name="ocr">
            <image-upload
              :uploaded-images="uploadedImages"
              :ocr-status="ocrStatus"
              @handle-upload="handleUpload"
              @view-ocr-result="viewOcrResult"
              @delete-image="deleteUploadedImage"
            />
          </el-tab-pane>
          <el-tab-pane label="手动输入" name="manual">
            <manual-input
              @data-confirmed="handleManualInputData"
            />
          </el-tab-pane>
        </el-tabs>
      </el-col>
      
      <!-- 右侧：识别结果 -->
      <el-col :span="12">
        <matched-results
          :matched-results="matchedResults"
          @show-batch-date-dialog="showBatchDateDialog"
          @refresh-results="refreshMatchedResults"
          @batch-confirm="batchConfirmResults"
          @show-member-selector="showMemberSelector"
          @edit-result="editPendingResult"
          @confirm-result="confirmPendingResult"
          @delete-result="deletePendingResult"
        />
      </el-col>
    </el-row>
    
    <el-row class="mt-20">
      <el-col :span="24">
        <confirmed-records
          :confirmed-records="confirmedRecords"
          :club-members="clubMembers"
          @refresh-records="fetchConfirmedRecords"
          @show-add-dialog="showAddDialog"
          @edit-record="editConfirmedRecord"
          @delete-record="deleteConfirmedRecord"
        />
      </el-col>
    </el-row>
    
    <!-- OCR结果详情弹窗 -->
    <ocr-result-dialog
      v-model:visible="ocrResultVisible"
      :current-ocr-result="currentOcrResult"
      @reprocess-image="reprocessImage"
    />
    
    <!-- 成员选择弹窗 -->
    <member-selector-dialog
      v-model:visible="memberSelectorVisible"
      :club-members="clubMembers"
      :current-matching-result="currentMatchingResult"
      @confirm-selection="confirmMemberSelection"
    />
    
    <!-- 战绩编辑弹窗 -->
    <battle-edit-dialog
      v-model:visible="battleEditVisible"
      :title="battleEditTitle"
      :editing-battle="editingBattle"
      :is-pending-edit="isPendingEdit"
      @save-battle="saveBattleEdit"
      @save-pending-edit="savePendingEdit"
      @save-pending-result="savePendingResult"
      @update:date="updateBattleDate"
      @update:editingBattle="updateEditingBattle"
    />
    
    <!-- 批量日期设置弹窗 -->
    <batch-date-dialog
      v-model:visible="batchDateVisible"
      @apply-batch-date="applyBatchDate"
    />
  </div>
</template>

<script setup name="IntelligentBattle">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  testOcrService,
  checkOcrHealth,
  initializeOcr,
  extractTextFromImage,
  uploadBattleImage,
  getClubMemberList,
  intelligentMatch,
  getUnconfirmedRecords,
  batchConfirmBattleRecords,
  confirmBattleRecord,
  savePendingRecord,
  addPendingRecord,
  matchPendingRecords,
  addManualRecord,
  confirmPendingRecord,
  batchConfirmPendingRecords,
  getPendingRecord,
  deletePendingRecord,
  clearAllPendingRecords,
  updatePendingMember,
  getBattleRecordList,
  updateBattleRecord,
  deleteBattleRecord
} from '@/api/club/battle'
import request from '@/utils/request'

// 导入子组件
import OcrStatusCard from './components/OcrStatusCard.vue'
import ImageUpload from './components/ImageUpload.vue'
import ManualInput from './components/ManualInput.vue'
import MatchedResults from './components/MatchedResults.vue'
import ConfirmedRecords from './components/ConfirmedRecords.vue'
import OcrResultDialog from './components/OcrResultDialog.vue'
import MemberSelectorDialog from './components/MemberSelectorDialog.vue'
import BattleEditDialog from './components/BattleEditDialog.vue'
import BatchDateDialog from './components/BatchDateDialog.vue'

// OCR服务状态
const ocrStatus = reactive({
  connected: false,
  healthy: false,
  lastCheckTime: null
})

const ocrInitializing = ref(false)

// 上传的图片
const uploadedImages = ref([])

// 俱乐部成员列表
const clubMembers = ref([])

// 匹配结果
const matchedResults = ref([])

// 已确认记录
const confirmedRecords = ref([])

// OCR结果弹窗
const ocrResultVisible = ref(false)
const currentOcrResult = ref({})
const currentImageIndex = ref(-1)

// 成员选择弹窗
const memberSelectorVisible = ref(false)
const currentMatchingResult = ref(null)

// 战绩编辑弹窗
const battleEditVisible = ref(false)
const battleEditTitle = ref('')
const editingBattle = ref({})
const isPendingEdit = ref(false) // 是否为暂存记录编辑

// 批量日期设置
const batchDateVisible = ref(false)

// 左侧标签页
const activeLeftTab = ref('ocr')

onMounted(async () => {
  try {
    // 使用try-catch包装每个异步调用，防止单个请求失败阻塞整个页面
    await checkOcrStatus()
  } catch (error) {
    console.error('检查OCR状态失败:', error)
  }
  
  try {
    await loadClubMembers()
  } catch (error) {
    console.error('加载俱乐部成员失败:', error)
  }
  
  // 暂时注释掉页面加载时自动清空暂存表数据，以便排查问题
  // try {
  //   await clearAllPendingRecords()
  // } catch (error) {
  //   console.error('清空暂存数据失败:', error)
  //   // 不显示错误消息，避免影响用户体验
  // }
  
  try {
    await fetchConfirmedRecords()
  } catch (error) {
    console.error('获取已确认记录失败:', error)
  }
})

// 检查OCR服务状态
async function checkOcrStatus() {
  try {
    // 测试连通性
    await testOcrService()
    ocrStatus.connected = true
    
    // 检查健康状态
    const healthResponse = await checkOcrHealth()
    ocrStatus.healthy = healthResponse.data && healthResponse.data.ocr_initialized === true
    
    ocrStatus.lastCheckTime = new Date()
  } catch (error) {
    console.error('OCR服务检查失败:', error)
    ocrStatus.connected = false
    ocrStatus.healthy = false
    
    // 不再显示错误消息，只在用户尝试使用OCR功能时才提示
    console.warn('OCR服务不可用，图片上传功能将被禁用')
  }
}

// 初始化OCR服务
async function initOcrService() {
  if (!ocrStatus.connected) {
    ElMessage.warning('OCR服务未连接，请先启动服务')
    return
  }
  
  ocrInitializing.value = true
  try {
    await initializeOcr()
    ElMessage.success('OCR引擎初始化成功')
    await checkOcrStatus() // 重新检查状态
  } catch (error) {
    console.error('OCR初始化失败:', error)
    ElMessage.error('OCR引擎初始化失败')
  } finally {
    ocrInitializing.value = false
  }
}

// 加载俱乐部成员列表
async function loadClubMembers() {
  try {
    const response = await getClubMemberList()
    if (response.code === 200) {
      clubMembers.value = response.data
    }
  } catch (error) {
    console.error('获取俱乐部成员列表失败:', error)
    ElMessage.error('获取俱乐部成员列表失败')
  }
}

// 处理图片上传
async function handleUpload(options) {
  const { file } = options
  
  if (!ocrStatus.connected) {
    ElMessage.error('OCR服务未连接，无法上传图片')
    return
  }
  
  // 验证文件大小
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    ElMessage.error('上传图片大小不能超过 10MB')
    return
  }
  
  try {
    // 在处理新图片前，清空之前的匹配结果
    matchedResults.value = []
    
    // 1. 提取文本
    const ocrResponse = await extractTextFromImage(file)
    
    // 调试日志
    console.log('OCR响应:', ocrResponse)
    
    // 确保数据存在，OCR服务可能直接返回数据而不是包装在data中
    if (!ocrResponse) {
      throw new Error('OCR服务响应无效')
    }
    
    // 获取数据，可能是直接返回或包装在data中
    const responseData = ocrResponse.data || ocrResponse
    
    // 确保所有必需字段都有值
    const filename = responseData.filename || file.name || 'unknown.png'
    const text = responseData.text || ''
    const lines = responseData.lines || []
    
    console.log('解析出的数据:', { filename, text, lines: lines.length })
    
    // 2. 上传图片到后端
    const uploadFormData = new FormData()
    uploadFormData.append('file', file)
    uploadFormData.append('filename', filename)
    uploadFormData.append('text', text)
    
    // 添加lines字段，将数组转换为JSON字符串
    uploadFormData.append('lines', JSON.stringify(lines))
    
    const uploadResponse = await uploadBattleImage(uploadFormData)
    
    // 3. 智能匹配
    const matchResponse = await intelligentMatch({
      id: uploadResponse.data.id,
      filename: filename,
      text: text,
      lines: lines
    })
    
    // 4. 添加到上传列表
    uploadedImages.value.push({
      id: uploadResponse.data.id,
      filename: filename,
      imageUrl: fixImageUrl(uploadResponse.data.imageUrl),
      text: text,
      lines: lines,
      status: 'matched',
      processingTime: (ocrResponse.data.cost_time || 0).toFixed(3)
    })
    
    // 5. 刷新匹配结果列表（从暂存表获取）
    await fetchMatchedResults()
    
    ElMessage.success('图片上传并识别成功')
  } catch (error) {
    console.error('图片处理失败:', error)
    ElMessage.error(`图片处理失败: ${error.message || '未知错误'}`)
  }
}

// 查看OCR识别结果
function viewOcrResult(index) {
  currentImageIndex.value = index
  currentOcrResult.value = { ...uploadedImages.value[index] }
  ocrResultVisible.value = true
}

// 删除已上传的图片
function deleteUploadedImage(index) {
  ElMessageBox.confirm('确定要删除这张图片吗？删除后将无法恢复', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    // 从数组中移除图片
    uploadedImages.value.splice(index, 1)
    ElMessage.success('图片已删除')
  }).catch(() => {
    // 用户取消删除
  })
}

// 重新处理图片
async function reprocessImage() {
  if (currentImageIndex.value < 0) return
  
  // 这里可以实现重新处理逻辑
  ElMessage.info('重新处理功能开发中...')
}

// 显示成员选择器
function showMemberSelector(result) {
  currentMatchingResult.value = result
  memberSelectorVisible.value = true
}

// 确认成员选择
async function confirmMemberSelection({ member, currentMatchingResult }) {
  if (!currentMatchingResult || !currentMatchingResult.id) {
    ElMessage.error('无效的记录ID')
    return
  }
  
  try {
    // 调用API更新数据库
    const response = await updatePendingMember(
      currentMatchingResult.id,
      member.id,
      member.gameId
    )
    
    if (response.code === 200) {
      // 更新前端数据
      currentMatchingResult.matchedMember = member
      currentMatchingResult.memberId = member.id
      memberSelectorVisible.value = false
      
      ElMessage.success('成员匹配成功')
    } else {
      ElMessage.error(response.msg || '成员匹配失败')
    }
  } catch (error) {
    console.error('成员匹配失败:', error)
    ElMessage.error('成员匹配失败: ' + (error.message || '未知错误'))
  }
  
  currentMatchingResult.value = null
}

// 获取匹配结果（从暂存表获取）
async function fetchMatchedResults() {
  try {
    // 获取所有记录，不分页
    const response = await getUnconfirmedRecords({ pageSize: 1000 })
    if (response.code === 200) {
      // 处理暂存表数据，转换为前端需要的格式
      const pendingRecords = response.rows || []
      matchedResults.value = pendingRecords.map(record => ({
        id: record.id,
        pendingId: record.id, // 暂存记录ID
        ocrResultId: record.ocrResultId,
        nickname: record.memberName,
        matchedMember: record.memberId ? { id: record.memberId, name: record.memberName, gameId: record.memberName } : null,
        memberId: record.memberId,
        memberName: record.memberName,
        kills: record.kills,
        deaths: record.deaths,
        digs: record.digging,
        revives: record.revives,
        kdRatio: record.kdRatio ? parseFloat(record.kdRatio) : 0,
        battleDate: formatDate(record.recordDate),
        recordDate: record.recordDate, // 保留原始日期对象
        isManualInput: record.ocrResultId === null // 如果没有ocrResultId，则为手动输入
      }))
    }
  } catch (error) {
    console.error('获取暂存记录失败:', error)
  }
}

// 清空匹配结果
async function refreshMatchedResults() {
  try {
    // 调用API清空所有暂存记录
    const response = await clearAllPendingRecords()
    if (response.code === 200) {
      // 清空前端显示
      matchedResults.value = []
      ElMessage.success('已清空所有暂存数据')
    } else {
      ElMessage.error('清空暂存数据失败: ' + (response.msg || '未知错误'))
    }
  } catch (error) {
    console.error('清空暂存数据失败:', error)
    ElMessage.error('清空暂存数据失败: ' + (error.message || '未知错误'))
  }
}

// 显示批量日期设置弹窗
function showBatchDateDialog() {
  batchDateVisible.value = true
}

// 应用批量日期设置
async function applyBatchDate(date) {
  try {
    // 更新所有暂存记录的日期
    const updatePromises = matchedResults.value.map(async (record) => {
      if (record.pendingId) {
        const response = await savePendingRecord({
          id: record.pendingId,
          recordDate: date
        })
        return response
      }
      return null
    })
    
    const results = await Promise.all(updatePromises)
    const successCount = results.filter(r => r && r.code === 200).length
    
    if (successCount > 0) {
      ElMessage.success(`成功更新 ${successCount} 条记录的日期`)
      // 刷新列表
      await fetchMatchedResults()
    } else {
      ElMessage.warning('没有记录被更新')
    }
  } catch (error) {
    console.error('批量设置日期失败:', error)
    ElMessage.error('批量设置日期失败: ' + (error.message || '未知错误'))
  }
}

// 编辑暂存记录
async function editPendingResult(result) {
  battleEditTitle.value = '编辑战绩'
  isPendingEdit.value = true
  
  // 直接使用已有数据，不再尝试获取最新数据
  // 这样可以避免"获取暂存记录失败"的错误
  editingBattle.value = {
    id: result.id,
    pendingId: result.pendingId || result.id,
    ocrResultId: result.ocrResultId,
    memberId: result.memberId,
    memberName: result.memberName || result.nickname,
    nickname: result.nickname || result.memberName,
    kills: result.kills,
    deaths: result.deaths,
    digs: result.digs,
    revives: result.revives,
    kdRatio: result.kdRatio,
    battleDate: formatDateForEdit(result.battleDate || result.recordDate)
  }
  
  battleEditVisible.value = true
}

// 暂存编辑结果
async function savePendingEdit(pendingData) {
  try {
    const response = await savePendingRecord(pendingData)
    if (response.code === 200) {
      ElMessage.success('暂存成功')
      battleEditVisible.value = false
// 刷新战绩记录列表
    await fetchConfirmedRecords()
    // 刷新暂存记录列表
    await fetchMatchedResults()
    } else {
      ElMessage.error(response.msg || '暂存失败')
    }
  } catch (error) {
    console.error('暂存失败:', error)
    ElMessage.error('暂存失败: ' + (error.message || '未知错误'))
  }
}

// 确认单个暂存记录
async function confirmPendingResult(result) {
  // 检查是否有匹配的成员
  if (!result.memberId && !result.matchedMember) {
    ElMessage.warning('该记录未匹配成员，请先手动选择成员')
    return
  }
  
  try {
    // 使用暂存记录ID确认
    if (result.pendingId) {
      const response = await confirmPendingRecord(result.pendingId)
      if (response.code === 200) {
        ElMessage.success('战绩确认成功')
        // 刷新列表
        await fetchMatchedResults()
        await fetchConfirmedRecords()
      } else {
        ElMessage.error(response.msg || '确认失败')
      }
    } else {
      ElMessage.error('暂存记录ID不存在')
    }
  } catch (error) {
    console.error('确认战绩失败:', error)
    ElMessage.error('确认战绩失败: ' + (error.message || '未知错误'))
  }
}

// 删除暂存记录
async function deletePendingResult(result) {
  if (!result.pendingId) {
    ElMessage.error('暂存记录ID不存在')
    return
  }
  
  try {
    await ElMessageBox.confirm('确定要删除这条暂存记录吗？此操作不可恢复！', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await deletePendingRecord(result.pendingId)
    if (response.code === 200) {
      ElMessage.success('删除成功')
      // 刷新列表
      await fetchMatchedResults()
    } else {
      ElMessage.error(response.msg || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除暂存记录失败:', error)
      ElMessage.error('删除失败: ' + (error.message || '未知错误'))
    }
  }
}

// 批量确认结果
async function batchConfirmResults() {
  if (matchedResults.value.length === 0) {
    ElMessage.warning('没有可确认的战绩')
    return
  }
  
  // 检查是否有未匹配成员的记录
  const unmatchedRecords = matchedResults.value.filter(r => !r.matchedMember)
  if (unmatchedRecords.length > 0) {
    // 确认用户是否要跳过未匹配的记录
    try {
      await ElMessageBox.confirm(
        `有 ${unmatchedRecords.length} 条记录未匹配成员，确认将只处理已匹配的记录？`,
        '确认批量操作',
        {
          confirmButtonText: '继续',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
      
      // 过滤掉未匹配的记录
      const recordsToConfirm = matchedResults.value.filter(r => r.matchedMember)
      await processBatchConfirm(recordsToConfirm)
    } catch (error) {
      if (error !== 'cancel') {
        console.error('批量确认失败:', error)
        ElMessage.error('批量确认失败')
      }
    }
  } else {
    // 所有记录都已匹配，直接处理
    await processBatchConfirm(matchedResults.value)
  }
}

// 处理批量确认的辅助函数
async function processBatchConfirm(records) {
  try {
    const response = await batchConfirmBattleRecords(records)
    if (response.code === 200) {
      ElMessage.success(`成功确认 ${response.data} 条战绩记录`)
      
      // 删除暂存表中的记录
      let deleteSuccessCount = 0
      for (const record of records) {
        try {
          // 如果记录有 pendingId，则从暂存表中删除
          if (record.pendingId) {
            const deleteResponse = await deletePendingRecord(record.pendingId)
            if (deleteResponse.code === 200) {
              deleteSuccessCount++
            }
          }
        } catch (error) {
          console.error(`删除暂存记录失败，ID: ${record.pendingId}`, error)
        }
      }
      
      // 从列表中移除已确认的记录
      records.forEach(record => {
        const index = matchedResults.value.findIndex(r => r.id === record.id)
        if (index !== -1) {
          matchedResults.value.splice(index, 1)
        }
      })
      
      if (deleteSuccessCount > 0) {
        ElMessage.success(`已删除 ${deleteSuccessCount} 条暂存记录`)
      }
      
      // 刷新已确认记录和暂存记录
      fetchConfirmedRecords()
      await fetchMatchedResults() // 刷新暂存记录，确保显示最新的数据库状态
    } else {
      ElMessage.error(response.msg || '批量确认失败')
    }
  } catch (error) {
    console.error('批量确认失败:', error)
    ElMessage.error('批量确认失败')
  }
}

// 获取已确认记录
async function fetchConfirmedRecords() {
  try {
    // 移除分页限制，获取所有记录
    const response = await getBattleRecordList({ pageNum: 1, pageSize: 10000 })
    if (response.code === 200) {
      confirmedRecords.value = response.rows || []
    }
  } catch (error) {
    console.error('获取已确认记录失败:', error)
  }
}

// 编辑已确认记录
// 根据成员ID获取成员昵称
function getMemberNickname(memberId) {
  if (!memberId) return ''
  
  const member = clubMembers.value.find(m => m.id === memberId)
  return member ? member.gameId || `成员${memberId}` : ''
}

function editConfirmedRecord(record) {
  battleEditTitle.value = '编辑已确认战绩'
  
  // 确保日期格式正确 - 使用recordDate字段而不是battleDate
  let recordDate = record.recordDate
  if (!recordDate) {
    // 如果没有日期，使用当前日期
    recordDate = new Date().toISOString().split('T')[0]
  } else if (typeof recordDate === 'string') {
    // 如果是字符串，确保格式正确
    // 检查是否是有效的日期格式
    const date = new Date(recordDate)
    if (isNaN(date.getTime())) {
      console.warn('无效的日期格式:', recordDate, '使用当前日期')
      recordDate = new Date().toISOString().split('T')[0]
    } else {
      recordDate = recordDate
    }
  } else if (recordDate instanceof Date) {
    // 如果是Date对象，转换为YYYY-MM-DD格式
    recordDate = recordDate.toISOString().split('T')[0]
  }
  
  // 根据memberId获取成员昵称
  const memberNickname = getMemberNickname(record.memberId)
  
  // 创建编辑对象，确保日期格式正确
  editingBattle.value = {
    ...record,
    recordDate: recordDate, // 使用recordDate字段而不是battleDate
    nickname: memberNickname || record.nickname || ''
  }
  
  isPendingEdit.value = false
  battleEditVisible.value = true
}

// 保存战绩编辑
async function saveBattleEdit(battleData) {
  try {
    // 使用id字段而不是recordId字段判断是更新还是新增
    if (battleData.id) {
      // 更新 - 确保传递id字段作为recordId
      const updateData = {
        ...battleData,
        recordId: battleData.id // 将id映射为recordId字段传递给后端
      }
      const response = await updateBattleRecord(updateData)
      if (response.code === 200) {
        ElMessage.success('战绩更新成功')
        battleEditVisible.value = false
        fetchConfirmedRecords()
      } else {
        ElMessage.error(response.msg || '更新失败')
      }
    } else {
      // 新增
      const response = await addManualRecord(battleData)
      if (response.code === 200){
        ElMessage.success('战绩添加成功')
        battleEditVisible.value = false
        fetchConfirmedRecords()
      } else {
        ElMessage.error(response.msg || '添加失败')
      }
    }
  } catch (error) {
    console.error('保存战绩失败:', error)
    ElMessage.error('保存战绩失败')
  }
}

// 处理暂存记录的保存操作（从智能识别结果移除）
async function savePendingResult(battleData) {
  try {
    // 确认暂存记录，将其转移到正式战绩表
    const response = await confirmPendingRecord(battleData.pendingId)
    if (response.code === 200) {
      ElMessage.success('战绩保存成功')
      battleEditVisible.value = false
      // 刷新两个列表
      await fetchMatchedResults()
      await fetchConfirmedRecords()
    } else {
      ElMessage.error(response.msg || '保存失败')
    }
  } catch (error) {
    console.error('保存战绩失败:', error)
    ElMessage.error('保存战绩失败: ' + (error.message || '未知错误'))
  }
}

// 删除已确认记录
async function deleteConfirmedRecord(record) {
  try {
    await ElMessageBox.confirm(
      '确定要删除这条战绩记录吗？',
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 使用id字段而不是recordId字段
    const response = await deleteBattleRecord(record.id)
    if (response.code === 200) {
      ElMessage.success('战绩删除成功')
      fetchConfirmedRecords()
    } else {
      ElMessage.error(response.msg || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除战绩失败:', error)
      ElMessage.error('删除战绩失败')
    }
  }
}

// 显示手动添加对话框
function showAddDialog() {
  battleEditTitle.value = '手动添加战绩'
  editingBattle.value = {
    nickname: '',
    memberId: null,
    battleDate: new Date().toISOString().split('T')[0],
    kills: null,
    deaths: null,
    digs: null,
    revives: null,
    kdRatio: 0
  }
  isPendingEdit.value = false
  battleEditVisible.value = true
}



// 格式化日期
function formatDate(dateStr) {
  if (!dateStr) return ''
  
  // 尝试解析日期
  let date
  if (typeof dateStr === 'string') {
    // 处理可能的字符串格式
    if (dateStr.includes('T')) {
      // ISO格式
      date = new Date(dateStr)
    } else if (dateStr.includes('-')) {
      // YYYY-MM-DD格式
      const parts = dateStr.split('-')
      if (parts.length === 3) {
        date = new Date(parseInt(parts[0]), parseInt(parts[1]) - 1, parseInt(parts[2]))
      } else {
        date = new Date(dateStr)
      }
    } else {
      date = new Date(dateStr)
    }
  } else {
    date = new Date(dateStr)
  }
  
  // 检查日期是否有效
  if (isNaN(date.getTime())) {
    console.warn('无效的日期格式:', dateStr)
    return dateStr // 返回原始字符串
  }
  
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`
}

// 格式化日期用于编辑表单
function formatDateForEdit(dateStr) {
  if (!dateStr) return ''
  
  // 如果已经是YYYY-MM-DD格式，直接返回
  if (typeof dateStr === 'string' && dateStr.includes('-')) {
    return dateStr.split(' ')[0] // 移除可能的时间部分
  }
  
  // 尝试解析为日期对象
  let date
  if (typeof dateStr === 'string') {
    date = new Date(dateStr)
  } else {
    date = dateStr
  }
  
  // 检查日期是否有效
  if (isNaN(date.getTime())) {
    console.warn('无效的日期格式:', dateStr)
    return ''
  }
  
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`
}

// 修复图片URL，确保包含协议头和域名
function fixImageUrl(url) {
  if (!url) return ''
  
  // 如果URL已经包含协议头，直接返回
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url
  }
  
  // 如果是相对路径，添加基础URL
  // 获取当前域名
  const protocol = window.location.protocol
  const hostname = window.location.hostname
  const port = window.location.port ? `:${window.location.port}` : ''
  const baseUrl = `${protocol}//${hostname}${port}`
  
  // 确保路径以/开头
  const path = url.startsWith('/') ? url : `/${url}`
  
  return `${baseUrl}${path}`
}
// 更新战绩日期
function updateBattleDate(dateStr) {
  if (editingBattle.value) {
    editingBattle.value.recordDate = dateStr
    console.log('更新战绩日期为:', dateStr)
  }
}

// 更新编辑对象
function updateEditingBattle(newEditingBattle) {
  if (newEditingBattle) {
    editingBattle.value = newEditingBattle
    console.log('更新编辑对象:', newEditingBattle)
  }
}

// 上传文本OCR结果（用于手动输入）
async function uploadTextResult(data) {
  return request({
    url: '/club/battle/upload-text-result',
    method: 'post',
    data: data
  })
}

// 处理手动输入的数据
async function handleManualInputData(data) {
  if (!data || data.length === 0) {
    ElMessage.warning('没有有效数据')
    return
  }
  
  try {
    // 使用后端API暂存记录，但提供ocrResultId为null标记为手动输入
    const savePromises = data.map((item) => {
      const recordData = {
        // 不提供id，让数据库自动生成
        ocrResultId: null, // null表示手动输入，非OCR识别
        memberId: null, // 初始为null，等待智能匹配
        memberName: item.nickname,
        kills: item.kills,
        digging: item.digs,
        deaths: item.deaths,
        revives: item.revives,
        kdRatio: item.kdRatio,
        recordDate: new Date().toISOString().split('T')[0]
      }
      return addPendingRecord(recordData)
    })
    
    await Promise.all(savePromises)
    
    // 调用智能匹配API处理这些记录
    await fetchMatchedResults()
    
    // 对所有没有memberId的记录进行智能匹配
    const unmatchedRecords = matchedResults.value.filter(r => !r.memberId)
    if (unmatchedRecords.length > 0) {
      try {
        // 调用专门的API来匹配已有暂存记录的成员
        const unmatchedIds = unmatchedRecords.map(r => r.id)
        const matchResponse = await matchPendingRecords(unmatchedIds)
        
        if (matchResponse.code === 200) {
          // 重新获取匹配结果
          await fetchMatchedResults()
          ElMessage.success(`智能匹配完成`)
        }
      } catch (matchError) {
        console.error('智能匹配失败:', matchError)
        ElMessage.warning('智能匹配失败，请手动匹配成员')
      }
    }
    
    // 切换到OCR标签页，显示匹配结果
    activeLeftTab.value = 'ocr'
    
    ElMessage.success(`成功添加 ${data.length} 条手动输入的战绩数据`)
  } catch (error) {
    console.error('处理手动输入数据时出错:', error)
    ElMessage.error('处理手动输入数据时出错: ' + error.message)
  }
}
</script>

<style scoped>
.mt-20 {
  margin-top: 20px;
}
</style> 