<template>
  <div class="fillcontain">
    <search-item @filterList="getResultList" @searchList="getResultList" @onBatchDelMoney="onBatchDelMoney" />
    <div class="table_container">
      <el-table
        v-loading="loading"
        :data="tableData"
        style="width: 100%"
        align="center"
        @select="selectTable"
        @select-all="selectAll"
      >
        <el-table-column
          align="center"
          label="ID"
          min-width="5%"
        >
          <template slot-scope="scope">
            {{ scope.$index + 1 + incomePayData.limit * (incomePayData.page - 1) }}
          </template>
        </el-table-column>
        <!--        <el-table-column-->
        <!--          type="selection"-->
        <!--          align="center"-->
        <!--          min-width="5%"-->
        <!--        />-->
        <el-table-column
          prop="taskName"
          label="任务名称"
          align="center"
          min-width="15%"
        >
          <template slot-scope="scope">
            <span style="color:#00d053">{{ scope.row.name }}</span>
          </template>
        </el-table-column>
        <!--        <el-table-column-->
        <!--          v-if="idFlag"-->
        <!--          prop="address"-->
        <!--          label="籍贯"-->
        <!--          align="center"-->
        <!--        />-->
        <el-table-column
          prop="seedName"
          label="对应种子场景名"
          align="center"
          sortable
          min-width="10%"
        >
          <!--          <template slot-scope="scope">-->
          <!--            <el-icon name="time" />-->
          <!--            <span style="margin-left: 10px">{{ scope.row.createTime }}</span>-->
          <!--          </template>-->
        </el-table-column>
        <el-table-column
          prop="author"
          label="创建人"
          align="center"
          min-width="8%"
          sortable
        />
        <el-table-column
          prop="createDate"
          label="创建时间"
          align="center"
          sortable
          min-width="12%"
        >
          <template slot-scope="scope">
            <el-icon name="time" />
            <span style="margin-left: 10px">{{ scope.row.createDate }}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="taskStatus"
          class-name="status-col"
          label="任务状态"
          min-width="10%"
          align="center"
        >
          <template slot-scope="scope">
            <el-tag :type="scope.row.taskStatus | statusFilter">{{ scope.row.taskStatus }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="taskDesc"
          label="任务描述"
          align="center"
          min-width="15%"
        >
          <template slot-scope="scope">
            <span style="color:#00d053">+ {{ scope.row.taskDesc }}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="operation"
          align="center"
          label="操作"
          min-width="25%"
        >
          <template slot-scope="scope">
            <el-button
              type="warning"
              size="small"
              @click="onEdit(scope.row)"
            >编辑</el-button>
            <el-button
              type="danger"
              size="small"
              @click="onDelete(scope.row,scope.$index)"
            >删除</el-button>
            <el-button
              type="primary"
              size="small"
              @click="onVisualize(scope.row)"
            >结果可视化</el-button>
            <el-button
              type="info"
              size="small"
              @click="download(scope.row,scope.$index)"
            >下载</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination :page-total="pageTotal" @handleCurrentChange="handleCurrentChange" @handleSizeChange="handleSizeChange" />
      <modifyDialog v-if="modifyDialog.show" :is-show="modifyDialog.show" :dialog-row="modifyDialog.dialogRow" @getResultList="getResultList" @closeDialog="hideModifyDialog" />
      <previewFile v-if="previewDialog.show" :is-show="previewDialog.show" :dialog-row="previewDialog.dialogRow" @closeDialog="hidePreviewDialog" />
    </div>
  </div>
</template>

<script>

import { mapGetters } from 'vuex'
import * as mutils from '@/utils/mUtils'
import SearchItem from './components/searchItem'
import ModifyDialog from '@/views/simulation/components/modifyDialog.vue'
import Pagination from '@/components/pagination'
import PreviewFile from '@/views/simulation/components/previewFile.vue'
import { getModelListAPI, removeMoney, batchremoveMoney } from '@/api/money'
import { getFileContent } from '@/api/table'
import axios from 'axios'

export default {
  filters: {
    statusFilter(status) {
      const statusMap = {
        '已完成': 'success',
        '进行中': 'gray',
        '已终止': 'danger'
      }
      return statusMap[status]
    }
  },
  components: {
    SearchItem,
    ModifyDialog,
    Pagination,
    PreviewFile
  },
  data() {
    // const xoscs = require.context('@/assets/seeds/', true)
    return {
      // files,
      modifyDialog: {
        show: false,
        dialogRow: {}
      },
      previewDialog: {
        show: false,
        dialogRow: {}
      },
      gifUrl: 0,
      downloadFileName: '',
      fileUrl: '',
      tableData: [],
      tableHeight: 0,
      loading: true,
      idFlag: false,
      isShow: false, // 是否显示资金modal,默认为false
      editid: '',
      rowIds: [],
      sortnum: 0,
      format_type_list: {
        0: '已终止',
        1: '进行中',
        2: '已完成'
      },
      addFundDialog: {
        show: false,
        dialogRow: {}
      },
      incomePayData: {
        page: 1,
        limit: 10,
        name: ''
      },
      pageTotal: 0,
      // 用于列表筛选
      fields: {
        incomePayType: {
          filter: {
            list: [{
              text: '原始场景',
              value: 0
            }, {
              text: '变异场景',
              value: 1
            }],
            multiple: true
          }
        }
      },
      showEditFile: false,
      currentUrl: ''
    }
  },
  computed: {
    ...mapGetters(['search'])
  },
  mounted() {
    this.getResultList('')
  },
  methods: {
    download(row) {
      axios({
        url: 'http://localhost:8080/sim_result/download',
        method: 'GET',
        params: { id: row.id },
        responseType: 'arraybuffer'
      })
        .then(response => {
          // console.log(response)
          // console.log(response.headers)
          const contentDisposition = response.headers['content-disposition']
          // console.log(contentDisposition)
          const filename = contentDisposition.split(';')[1].trim().split('=')[1].replace(/"/g, '')
          console.log('download: ', filename)
          const blob = new Blob([response.data], { type: 'application/zip' })
          const url = window.URL.createObjectURL(blob)
          const link = document.createElement('a')
          link.href = url
          link.setAttribute('download', filename)
          document.body.appendChild(link)
          link.click()
        })
        .catch(error => {
          console.log(error)
        })
    },

    setAddress(value) {

    },
    setTableHeight() {
      this.$nextTick(() => {
        this.tableHeight = document.body.clientHeight - 300
      })
    },
    getResultList(filename) {
      axios.get('http://localhost:8080/sim_result/getAll', { params: { name: filename }}).then(res => {
        this.tableData = []
        this.loading = false
        res.data.data.forEach((item) => {
          // console.log('item', JSON.parse(item))
          item = JSON.parse(item)
          item.taskStatus = this.formatterType(item)
          this.tableData.push(item)
        })
        this.pageTotal = this.tableData.length
      })
    },
    // 显示资金弹框
    showModifyDialog(val) {
      // this.$store.commit('SET_DIALOG_TITLE', val)
      this.modifyDialog.show = true
    },
    hideModifyDialog() {
      this.modifyDialog.show = false
    },
    showPreviewDialog(val) {
      this.previewDialog.show = true
    },
    hidePreviewDialog() {
      this.previewDialog.show = false
    },
    // 上下分页
    handleCurrentChange(val) {
      this.incomePayData.page = val
      this.getResultList()
    },
    // 每页显示多少条
    handleSizeChange(val) {
      this.incomePayData.limit = val
      this.getResultList()
    },
    getPay(val) {
      if (mutils.isInteger(val)) {
        return -val
      } else {
        return val
      }
    },
    /**
    * 格式化状态
    */
    formatterType(item) {
      const type = parseInt(item.taskStatus)
      return this.format_type_list[type]
    },
    filterType(value, item) {
      const type = parseInt(item.incomePayType)
      return this.format_type_list[value] === this.format_type_list[type]
    },
    // 执行任务
    onVisualize(row) {
      this.previewDialog.dialogRow = { ...row }
      this.showPreviewDialog()
    },
    // 终止任务
    terminateTask(row) {
      console.log(row)
      row.taskStatus = '已终止'
      // this.tableData.push({ 'id': 5, 'name': 'EpochTest', 'modelCnt': 1, 'seedCnt': 6, 'seedName': 5, 'creater': 'ylq', 'createDate': '2023-03-1 21:05:38', 'taskStatus': '已终止', 'taskDesc': 'Epoch、原始种子、全算子 测试' })

      // this.addFundDialog.dialogRow = { ...row }
      // this.showAddFundDialog()
    },
    // 编辑数据
    onEdit(row) {
      this.modifyDialog.dialogRow = { ...row }
      this.showModifyDialog()
    },
    // 删除数据
    onDelete(row) {
      // console.log(this.tableData)
      this.$confirm('确认删除该记录吗?', '提示', {
        type: 'warning'
      })
        .then(() => {
          axios.post('http://localhost:8080/sim_result/delete/' + row.id).then(res => {
            // console.log('getSeedDelete res:', res)
            this.getResultList('')
            // this.tableData = []
            // Array.from(res.data.data).forEach((item) => {
            //   item = JSON.parse(item)
            //   item.taskStatus = this.formatterType(item)
            //   this.tableData.push(item)
            // })
            // this.loading = false
          })
        })
        .catch(() => {})
    },
    onBatchDelMoney() {
      this.$confirm('确认批量删除记录吗?', '提示', {
        type: 'warning'
      })
        .then(() => {
          const ids = this.rowIds.map(item => item.id).toString()
          const para = { ids: ids }
          batchremoveMoney(para).then(res => {
            this.$message({
              message: '批量删除成功',
              type: 'success'
            })
            this.getResultList()
          })
        })
        .catch(() => {})
    },
    // 当用户手动勾选数据行的 Checkbox 时触发的事件
    selectTable(val, row) {
      this.setSearchBtn(val)
    },
    // 用户全选checkbox时触发该事件
    selectAll(val) {
      val.forEach((item) => {
        this.rowIds.push(item.id)
      })
      this.setSearchBtn(val)
    },
    setSearchBtn(val) {
      let isFlag = true
      if (val.length > 0) {
        isFlag = false
      } else {
        isFlag = true
      }
      this.$store.commit('SET_SEARCHBTN_DISABLED', isFlag)
    }
  }
}
</script>

<style lang="less" scoped>
    .table_container{
        padding: 10px;
        background: #fff;
        border-radius: 2px;
    }
    .el-dialog--small{
       width: 600px !important;
    }
    .pagination{
        text-align: left;
        margin-top: 10px;
    }

</style>

