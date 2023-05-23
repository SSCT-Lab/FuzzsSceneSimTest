<template>
  <div class="fillcontain">
    <search-item @showDialog="showAddFundDialog" @searchList="getTaskList" />
    <div class="table_container">
      <el-table
        v-loading="loading"
        :data="tableData"
        :expand-row-keys="expands"
        :row-key="getRowKeys"
        style="width: 100%"
        align="center"
        @select="selectTable"
        @select-all="selectAll"
        @expand-change="expandChangeHandler"
      >
        <el-table-column
          align="center"
          label="ID"
          min-width="3.5%"
        >
          <template slot-scope="scope">
            {{ scope.$index + 1 + incomePayData.limit * (incomePayData.page - 1) }}
          </template>
        </el-table-column>
        <el-table-column
          prop="taskName"
          label="任务名称"
          align="center"
          min-width="10%"
        >
          <template slot-scope="scope">
            <span style="color:#00d053">{{ scope.row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="modelCnt"
          label="测试模型数"
          align="center"
          min-width="5%"
          sortable
        />
        <el-table-column
          prop="seedCnt"
          label="种子文件数"
          align="center"
          min-width="5%"
          sortable
        />
        <el-table-column
          prop="operatorCnt"
          label="变异算子数"
          align="center"
          sortable
          min-width="5%"
        >
          <!--          <template slot-scope="scope">-->
          <!--            <el-icon name="time" />-->
          <!--            <span style="margin-left: 10px">{{ scope.row.createTime }}</span>-->
          <!--          </template>-->
        </el-table-column>
        <el-table-column
          prop="creator"
          label="创建人"
          align="center"
          min-width="8%"
          sortable
        />
        <el-table-column
          prop="createTime"
          label="创建时间"
          align="center"
          sortable
          min-width="10%"
        >
          <template slot-scope="scope">
            <el-icon name="time" />
            <span style="margin-left: 10px">{{ scope.row.createDate }}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="status"
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
          prop="taskProgress"
          label="进度"
          align="center"
          min-width="10%"
          sortable
        >
          <template slot-scope="scope">
            <el-progress type="circle" :percentage="scope.row.taskProgress | progressFilter" :width="60" />
          </template>
        </el-table-column>
        <el-table-column
          prop="remark"
          label="备注"
          align="center"
          min-width="15%"
        >
          <template slot-scope="scope">
            <span style="color:#00d053">+ {{ scope.row.desc }}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="operation"
          align="center"
          label="操作"
          min-width="35%"
        >
          <template slot-scope="scope">
            <el-button
              type="primary"
              size="small"
              @click="executeTask(scope.row)"
            >执行</el-button>
            <el-button
              type="warning"
              size="small"
              @click="terminateTask(scope.row)"
            >停止</el-button>
            <el-button
              type="danger"
              size="small"
              @click="onDeleteTask(scope.row,scope.$index)"
            >删除</el-button>
            <el-button
              type="success"
              size="small"
              @click="displayResult(scope.row,scope.$index)"
            >结果</el-button>
            <el-button
              type="primary"
              size="small"
              @click="visualize(scope.row,scope.$index)"
            >错误可视化</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination :page-total="pageTotal" @handleCurrentChange="handleCurrentChange" @handleSizeChange="handleSizeChange" />
      <addFundDialog v-if="addFundDialog.show" :is-show="addFundDialog.show" :dialog-row="addFundDialog.dialogRow" @getFundList="getTaskList" @closeDialog="hideAddFundDialog" />
      <previewFile v-if="previewDialog.show" :is-show="previewDialog.show" :dialog-row="previewDialog.dialogRow" @closeDialog="hidePreviewDialog" />
    </div>
  </div>
</template>

<script>

import { mapGetters } from 'vuex'
import * as mutils from '@/utils/mUtils'
import SearchItem from './components/searchItem'
import AddFundDialog from './components/addFundDialog'
import PreviewFile from './components/previewFile'
import Pagination from '@/components/pagination'

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
    },
    progressFilter(progress) {
      if (progress > 100) {
        return 100
      }
      return progress
    }
  },
  components: {
    SearchItem,
    AddFundDialog,
    Pagination,
    PreviewFile
  },
  data() {
    const files = require('@/assets/test.zip')
    // const xoscs = require.context('@/assets/seeds/', true)
    return {
      getRowKeys(row) {
        return row.id
      },
      // 存放展开的id
      expands: [],
      downloadFileName: '',
      fileUrl: files,
      tableData: [],
      tableHeight: 0,
      loading: true,
      idFlag: false,
      isShow: false, // 是否显示资金modal,默认为false
      editid: '',
      rowIds: [],
      sortnum: 0,
      format_type_list: {
        0: '原始场景',
        1: '变异场景'
      },
      task_status_list: {
        0: '已终止',
        1: '进行中',
        2: '已完成'
      },
      addFundDialog: {
        show: false,
        dialogRow: {}
      },
      previewDialog: {
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
    this.getTaskList('')
  },
  methods: {
    // 点击展开的时候就会触发这个方法
    expandChangeHandler(row, expandedRows) {
      console.log('expandedRows', expandedRows)
      console.log('row', row)
      if (expandedRows.length) {
        this.expands = []
        if (row) {
          this.expands.push(row.id)
        }
      } else {
        this.expands = []
      }
    },
    download(row) {
      // window.open(
      //   this.fileUrl,
      //   '_self'
      // )
      // 下载文件的url地址
      // const url = this.fileUrl
      const url = this.fileUrl
      // 创建a标签
      const link = document.createElement('a')
      link.href = url
      // 设置下载的文件名
      link.download = row.taskName
      // 触发点击事件
      link.click()
    },
    setAddress(value) {

    },
    setTableHeight() {
      this.$nextTick(() => {
        this.tableHeight = document.body.clientHeight - 300
      })
    },
    getTaskList(filename) {
      axios.get('http://localhost:8080/testTask/getAll', { params: { name: filename }}).then(res => {
        console.log(res.data.data)
        this.loading = false
        this.pageTotal = res.data.data.length
        this.tableData = []
        res.data.data.forEach((item) => {
          item.taskStatus = this.formatStatusType(item)
          this.tableData.push(item)
        })
      })
    },
    // 显示资金弹框
    showAddFundDialog(val) {
      this.addFundDialog.show = true
    },
    showPreviewDialog(val) {
      this.previewDialog.show = true
    },
    hideAddFundDialog() {
      this.addFundDialog.show = false
    },
    hidePreviewDialog() {
      this.previewDialog.show = false
    },
    // 上下分页
    handleCurrentChange(val) {
      this.incomePayData.page = val
      this.getTaskList()
    },
    // 每页显示多少条
    handleSizeChange(val) {
      this.incomePayData.limit = val
      this.getTaskList()
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
      const type = parseInt(item.incomePayType)
      return this.format_type_list[type]
    },
    formatStatusType(item) {
      const type = parseInt(item.taskStatus)
      return this.task_status_list[type]
    },
    filterType(value, item) {
      const type = parseInt(item.incomePayType)
      return this.format_type_list[value] === this.format_type_list[type]
    },
    // 执行任务
    executeTask(row) {
      row.taskStatus = '进行中'
      this.$message({
        message: '测试任务开始执行',
        type: 'success'
      })
      axios.post('http://localhost:8080/testTask/execute/' + row.id).then(res => {
        this.getTaskList('')
      }).catch(() => {
      })
      // const data = getFileContent(row.status)
      // this.showPreviewDialog('preview')
      // console.log('#onPreviewFile', data)
    },
    // 终止任务
    terminateTask(row) {
      console.log(row)
      row.taskStatus = '已终止'
      axios.post('http://localhost:8080/testTask/terminate/' + row.id).then(res => {
        this.getTaskList('')
        this.$message({
          message: '任务已终止',
          type: 'success'
        })
      })
      // this.tableData.push({ 'id': 5, 'taskName': 'EpochTest', 'modelCnt': 1, 'seedCnt': 6, 'mutCnt': 5, 'creater': 'ylq', 'createTime': '2023-03-1 21:05:38', 'status': '已终止', 'remark': 'Epoch、原始种子、全算子 测试' })

      // this.addFundDialog.dialogRow = { ...row }
      // this.showAddFundDialog()
    },
    // 删除数据
    onDeleteTask(row) {
      this.$confirm('确认删除该记录吗?', '提示', {
        type: 'warning'
      }).then(() => {
        axios.post('http://localhost:8080/testTask/delete/' + row.id).then(res => {
          this.getTaskList('')
          this.$message({
            message: '删除成功',
            type: 'success'
          })
        })
      }).catch(() => {
      })
    },
    visualize(row) {
      if (row.taskStatus === '已完成') {
        this.previewDialog.dialogRow = { ...row }
        this.showPreviewDialog('visualize')
      } else {
        this.$message({
          message: '测试任务未完成，无法查看错误可视化结果',
          type: 'warning'
        })
      }
    },
    displayResult(row) {
      if (row.taskStatus === '已完成') {
        this.$router.push({
          path: '/result/index',
          query: row
        })
      } else {
        this.$message({
          message: '测试任务未完成，无法查看结果',
          type: 'warning'
        })
      }
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

