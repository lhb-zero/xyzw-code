import request from '@/utils/request'

// 获取战绩统计概览
export function getOverview() {
  return request({
    url: '/club/statistics/overview',
    method: 'get'
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