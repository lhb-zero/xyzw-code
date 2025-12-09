import request from '@/utils/request'

// 获取战绩统计概览
export function getOverview(params) {
  return request({
    url: '/club/statistics/overview',
    method: 'get',
    params
  })
}

// 获取成员战绩排名
export function getRanking(params) {
  return request({
    url: '/club/statistics/ranking',
    method: 'get',
    params
  })
}

// 获取日期范围内的战绩统计
export function getTimelineStats(params) {
  return request({
    url: '/club/statistics/timeline',
    method: 'get',
    params
  })
}

// 获取成员列表统计数据
export function getMemberList(params) {
  return request({
    url: '/club/statistics/memberList',
    method: 'get',
    params
  })
}

// 获取数据来源统计
export function getDataSourceStats(params) {
  return request({
    url: '/club/statistics/dataSource',
    method: 'get',
    params
  })
}

// 获取成员详细战绩数据
export function getMemberBattleDetail(params) {
  return request({
    url: '/club/statistics/memberDetail',
    method: 'get',
    params
  })
}

// 获取团队活跃度分析
export function getActivityAnalysis(params) {
  return request({
    url: '/club/statistics/activity',
    method: 'get',
    params
  })
}

// 获取战绩对比数据
export function getComparisonData(params) {
  return request({
    url: '/club/statistics/comparison',
    method: 'get',
    params
  })
}