import request from '@/utils/axios'

export function readFile(params) {
  return request({
    url: '../../public/taskTable.json',
    method: 'get',
    params: params
  })
}
export function previewFile(params) {
  return request({
    url: '/money/previewFile',
    method: 'get',
    params: params
  })
}

export function getModelListAPI(params) {
  return request({
    url: '/model/getAll',
    method: 'get',
    params: params
  })
}

export function getSeedListAPI(params) {
  return request({
    url: '/model/getAll',
    method: 'get',
    params: params
  })
}

export function addMoney(params) {
  return request({
    url: '/money/add',
    method: 'get',
    params: params
  })
}

export function removeMoney(params) {
  return request({
    url: '/money/remove',
    method: 'get',
    params: params
  })
}

export function batchremoveMoney(params) {
  return request({
    url: '/money/batchremove',
    method: 'get',
    params: params
  })
}

export function updateMoney(params) {
  return request({
    url: '/money/edit',
    method: 'get',
    params: params
  })
}

export function uploadModel(params) {
  return request({
    url: '/model/upload',
    method: 'post',
    params: params
  })
}

// export const addUser = params => { return axios.get(`${base}/user/add`, { params: params }) }

