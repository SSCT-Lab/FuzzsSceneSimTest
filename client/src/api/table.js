import request from '@/utils/request'

export function getList(params) {
  return request({
    url: '/vue-admin-template/table/list',
    method: 'get',
    params
  })
}

export function getFileContent(params) {
  return request({
    url: '/seed/preview',
    method: 'get',
    params
  })
}
