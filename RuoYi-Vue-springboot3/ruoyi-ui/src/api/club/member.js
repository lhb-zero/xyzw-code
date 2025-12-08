import request from '@/utils/request'

// 查询俱乐部成员信息列表
export function listMember(query) {
  return request({
    url: '/club/member/list',
    method: 'get',
    params: query
  })
}

// 查询俱乐部成员信息详细
export function getMember(id) {
  return request({
    url: '/club/member/' + id,
    method: 'get'
  })
}

// 新增俱乐部成员信息
export function addMember(data) {
  return request({
    url: '/club/member',
    method: 'post',
    data: data
  })
}

// 修改俱乐部成员信息
export function updateMember(data) {
  return request({
    url: '/club/member',
    method: 'put',
    data: data
  })
}

// 删除俱乐部成员信息
export function delMember(id) {
  return request({
    url: '/club/member/' + id,
    method: 'delete'
  })
}

// 更新红淬炼数量
export function updateRedRefine(data) {
  return request({
    url: '/club/member/redRefine',
    method: 'put',
    data: data,
    headers: {
      repeatSubmit: false
    }
  })
}

// 更新四圣数量
export function updateFourSacred(data) {
  return request({
    url: '/club/member/fourSacred',
    method: 'put',
    data: data,
    headers: {
      repeatSubmit: false
    }
  })
}