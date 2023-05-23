
import { getModelListAPI } from '@/api/money'
import axios from "axios"; // 导入资金信息相关接口

const money = {
  state: {
    addSeedDialog: {
      title: '种子场景上传',
      type: 'uploadSeed'
    },
    modifySeedDialog: {
      title: '种子场景编辑'
    },
    previewDialog: {
      title: '种子文件预览'
    },
    addModelDialog: {
      title: '模型上传',
      type: 'uploadModel'
    },
    modifyModelDialog: {
      title: '模型编辑',
      type: 'modifyModel'
    },
    modifySimulationDialog: {
      title: '仿真任务编辑',
      type: 'modifyModel'
    },
    search: {
      startTime: '',
      endTime: '',
      name: ''
    },
    searchBtnDisabled: true
  },
  getters: {
    addSeedDialog: state => state.addSeedDialog,
    modifySeedDialog: state => state.modifySeedDialog,
    addModelDialog: state => state.addModelDialog,
    modifyModelDialog: state => state.modifyModelDialog,
    previewDialog: state => state.previewDialog,
    modifySimulationDialog: state => state.modifySimulationDialog,
    search: state => state.search,
    searchBtnDisabled: state => state.searchBtnDisabled
  },
  mutations: {
    SET_SEARCH: (state, payload) => {
      state.search = payload
    },
    SET_SEARCHBTN_DISABLED: (state, payload) => {
      state.searchBtnDisabled = payload
    }
  },
  actions: {
    // 获取资金列表
    GetMoneyIncomePay({ commit }, reqData) {
      return new Promise(resolve => {
        getModelListAPI(reqData).then(response => {
          const data = response.data
          resolve(data)
        })
      })
    },
    getSimulationResult({ commit }, id) {
      axios.post('http://localhost:8080/seed/simulate/' + id).then(res => {
        // console.log(res.data.success)
        commit('SET_SIMULATION_STATUS', res)
        this.loading = false
        this.$message({
          message: '仿真任务创建成功',
          type: 'success',
          duration: 1000
        })
      })

    }
  }
}

export default money
