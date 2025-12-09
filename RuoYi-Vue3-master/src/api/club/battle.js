import request from '@/utils/request'
import axios from 'axios'

// OCR服务相关API
// const OCR_BASE_URL = process.env.VUE_APP_OCR_BASE_URL
const OCR_BASE_URL = 'http://172.17.243.150:5111'
// const OCR_BASE_URL = 'http://172.28.41.154:5111'

// 创建独立的axios实例用于OCR服务
const ocrService = axios.create({
  baseURL: OCR_BASE_URL,
  timeout: 10000
})

// 测试OCR服务连通性
export function testOcrService() {
  return ocrService.get('/').catch(error => {
    console.error('OCR服务连通性测试失败:', error)
    throw error
  })
}

// 检查OCR服务健康状态
export function checkOcrHealth() {
  return ocrService.get('/health').catch(error => {
    console.error('OCR服务健康检查失败:', error)
    throw error
  })
}

// 初始化OCR引擎
export function initializeOcr() {
  return ocrService.post('/ocr/initialize').catch(error => {
    console.error('OCR服务初始化失败:', error)
    throw error
  })
}

// 上传图片到OCR服务进行文字提取
export function extractTextFromImage(file) {
  const formData = new FormData()
  formData.append('file', file)

  return ocrService.post('/ocr/extract', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  }).catch(error => {
    console.error('OCR文字提取失败:', error)
    throw error
  })
}

// 上传战绩截图（后端记录）
export function uploadBattleImage(imageData) {
  return request({
    url: '/club/battle/upload',
    method: 'post',
    data: imageData,
    headers: { 'Content-Type': 'multipart/form-data' },
    transformRequest: [data => data] // 确保FormData不被转换
  })
}

// 获取俱乐部成员列表（用于智能匹配）
export function getClubMemberList() {
  return request({
    url: '/club/member/listAll',
    method: 'get'
  })
}

// 智能匹配战绩到成员
export function intelligentMatch(ocrResult) {
  return request({
    url: '/club/battle/intelligent-match',
    method: 'post',
    data: ocrResult
  })
}

// 获取未确认的OCR识别记录列表
export function getUnconfirmedRecords(query) {
  return request({
    url: '/club/battle/unconfirmed',
    method: 'get',
    params: query
  })
}

// 确认单个暂存记录并创建战绩记录
export function confirmPendingRecord(id) {
  return request({
    url: '/club/battle/confirm-pending',
    method: 'post',
    params: { id: id }
  })
}

// 批量确认暂存记录
export function batchConfirmPendingRecords(ocrResultId) {
  return request({
    url: '/club/battle/batch-confirm-pending',
    method: 'post',
    params: { ocrResultId: ocrResultId }
  })
}

// 保存暂存记录
export function savePendingRecord(data) {
  return request({
    url: '/club/battle/pending/save',
    method: 'post',
    data: data
  })
}

// 新增暂存记录（用于手动输入）
export function addPendingRecord(data) {
  return request({
    url: '/club/battle/pending/add',
    method: 'post',
    data: data
  })
}

// 智能匹配已有暂存记录的成员
export function matchPendingRecords(ids) {
  return request({
    url: '/club/battle/pending/match-members',
    method: 'post',
    data: ids
  })
}

// 获取单个暂存记录
export function getPendingRecord(id) {
  return request({
    url: `/club/battle/pending/${id}`,
    method: 'get'
  })
}

// 更新暂存记录的成员信息
export function updatePendingMember(pendingId, memberId, memberName) {
  return request({
    url: '/club/battle/pending/update-member',
    method: 'put',
    data: {
      id: pendingId,
      memberId: memberId,
      memberName: memberName
    }
  })
}

// 删除暂存记录
export function deletePendingRecord(id) {
  return request({
    url: `/club/battle/pending/${id}`,
    method: 'delete'
  })
}

// 清空所有暂存记录
export function clearAllPendingRecords() {
  return request({
    url: '/club/battle/clear-pending',
    method: 'delete'
  })
}

// 确认OCR识别结果并创建战绩记录（保留原有接口用于兼容）
export function confirmBattleRecord(recordId, memberId, battleData) {
  const data = {
    recordId: recordId,
    memberId: memberId,
    battleData: battleData
  }

  return request({
    url: '/club/battle/confirm',
    method: 'post',
    data: data
  })
}

// 批量确认战绩记录（保留原有接口用于兼容）
export function batchConfirmBattleRecords(records) {
  return request({
    url: '/club/battle/batch-confirm',
    method: 'post',
    data: records
  })
}

// 手动添加战绩记录
export function addManualRecord(data) {
  return request({
    url: '/club/battle/manual',
    method: 'post',
    data: data
  })
}

// 获取战绩记录列表
export function getBattleRecordList(query) {
  return request({
    url: '/club/saltField/list',
    method: 'get',
    params: query
  })
}

// 更新战绩记录
export function updateBattleRecord(data) {
  return request({
    url: '/club/saltField',
    method: 'put',
    data: data
  })
}

// 删除战绩记录
export function deleteBattleRecord(id) {
  return request({
    url: `/club/saltField/${id}`,
    method: 'delete'
  })
}

// 获取服务器平均数据
export function getServerAverages(timeRange) {
  return request({
    url: '/club/battle/server-averages',
    method: 'get',
    params: { timeRange: timeRange }
  })
}
