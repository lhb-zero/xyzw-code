import request from '@/utils/request'

// 查询俱乐部详情列表
export function listDetail(query) {
  return request({
    url: '/club/detail/list',
    method: 'get',
    params: query
  })
}

// 查询俱乐部详情详细
export function getDetail(teamGroup) {
  return request({
    url: '/club/detail/' + teamGroup,
    method: 'get'
  })
}

// 查询所有团队概览
export function getOverview() {
  return request({
    url: '/club/detail/overview',
    method: 'get'
  })
}

// 查询团别分布数据（用于饼图）
export function getTeamDistribution() {
  return request({
    url: '/club/detail/teamDistribution',
    method: 'get'
  })
}

// 查询阵容分布数据（用于饼图）
export function getLineupDistribution() {
  return request({
    url: '/club/detail/lineupDistribution',
    method: 'get'
  })
}

// 查询团别战力对比数据（用于柱状图）
export function getPowerComparison() {
  return request({
    url: '/club/detail/powerComparison',
    method: 'get'
  })
}

// 查询团别红淬炼对比数据（用于柱状图）
export function getRedRefineComparison() {
  return request({
    url: '/club/detail/redRefineComparison',
    method: 'get'
  })
}

// 查询团内战力分布数据（用于柱状图）
export function getPowerDistribution(teamGroup) {
  return request({
    url: '/club/detail/powerDistribution/' + teamGroup,
    method: 'get'
  })
}

// 查询团内红淬炼分布数据（用于柱状图）
export function getRedRefineDistribution(teamGroup) {
  return request({
    url: '/club/detail/redRefineDistribution/' + teamGroup,
    method: 'get'
  })
}

// 查询团内四圣数量分布数据（用于柱状图）
export function getFourSacredDistribution(teamGroup) {
  return request({
    url: '/club/detail/fourSacredDistribution/' + teamGroup,
    method: 'get'
  })
}

// 查询团内战力与红淬炼关系数据（用于散点图）
export function getPowerRedRefineScatter(teamGroup) {
  return request({
    url: '/club/detail/powerRedRefineScatter/' + teamGroup,
    method: 'get'
  })
}

// 新增俱乐部详情
export function addDetail(data) {
  return request({
    url: '/club/detail',
    method: 'post',
    data: data
  })
}

// 修改俱乐部详情
export function updateDetail(data) {
  return request({
    url: '/club/detail',
    method: 'put',
    data: data
  })
}

// 删除俱乐部详情
export function delDetail(id) {
  return request({
    url: '/club/detail/' + id,
    method: 'delete'
  })
}