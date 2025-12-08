import request from '@/utils/request'

// 查询资源日志列表
export function listLog(query) {
  return request({
    url: '/club/log/list',
    method: 'get',
    params: query
  })
}

// 查询资源日志详细
export function getLog(id) {
  return request({
    url: '/club/log/' + id,
    method: 'get'
  })
}

// 新增资源日志
export function addLog(data) {
  return request({
    url: '/club/log',
    method: 'post',
    data: data
  })
}

// 修改资源日志
export function updateLog(data) {
  return request({
    url: '/club/log',
    method: 'put',
    data: data
  })
}

// 删除资源日志
export function delLog(id) {
  return request({
    url: '/club/log/' + id,
    method: 'delete'
  })
}

// 获取指定成员的资源日志列表
export function getMemberLogs(memberId) {
  return request({
    url: '/club/log/member/' + memberId,
    method: 'get'
  })
}

// 获取资源对比数据
export function compareResources(logId1, logId2) {
  return request({
    url: '/club/log/compare',
    method: 'get',
    params: {
      logId1: logId1,
      logId2: logId2
    }
  })
}

// 获取资源对比数据(新版本)
export function getResourceComparison(params) {
  return request({
    url: '/club/log/resourceComparison',
    method: 'get',
    params
  })
}

// 获取成员列表
export function getMemberList() {
  return request({
    url: '/club/member/listAll',
    method: 'get'
  })
}