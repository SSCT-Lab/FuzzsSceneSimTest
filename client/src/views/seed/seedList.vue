<template>
  <div class="fillcontain">
    <search-item @filterList="filterSeedList" @showDialog="showAddSeedDialog" @searchList="getSeedList" />
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
          min-width="3.5%"
        >
          <template slot-scope="scope">
            {{ scope.$index + 1 + incomePayData.limit * (incomePayData.page - 1) }}
          </template>
        </el-table-column>
        <el-table-column
          prop="name"
          label="种子文件名"
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
          prop="carlaMap"
          label="Carla地图号"
          align="center"
          min-width="8%"
          sortable
        />
        <el-table-column
          prop="createDate"
          label="生成时间"
          align="center"
          sortable
          min-width="15%"
        >
          <template slot-scope="scope">
            <el-icon name="time" />
            <span style="margin-left: 10px">{{ scope.row.createDate }}</span>
          </template>
        </el-table-column>
<!--        <el-table-column-->
<!--          prop="type"-->
<!--          label="场景类型"-->
<!--          align="center"-->
<!--          min-width="5%"-->
<!--          :formatter="formatterType"-->
<!--          :filters="fields.seedType.filter.list"-->
<!--          :filter-method="filterType"-->
<!--        />-->
        <el-table-column
          prop="desc"
          label="场景描述"
          align="center"
          min-width="20%"
          sortable
        >
          <template slot-scope="scope">
            <span style="color:#00d053">+ {{ scope.row.desc }}</span>
          </template>
        </el-table-column>
        <!--        <el-table-column-->
        <!--          prop="pay"-->
        <!--          label="支出"-->
        <!--          align="center"-->
        <!--          width="130"-->
        <!--          sortable-->
        <!--        >-->
        <!--          <template slot-scope="scope">-->
        <!--            <span style="color:#f56767">{{ scope.row.pay }}</span>-->
        <!--          </template>-->
        <!--        </el-table-column>-->
        <!--        <el-table-column-->
        <!--          prop="accoutCash"-->
        <!--          label="账户现金"-->
        <!--          align="center"-->
        <!--          width="130"-->
        <!--          sortable-->
        <!--        >-->
        <!--          <template slot-scope="scope">-->
        <!--            <span style="color:#4db3ff">{{ scope.row.accoutCash }}</span>-->
        <!--          </template>-->
        <!--        </el-table-column>-->
        <el-table-column
          prop="operation"
          align="center"
          label="操作"
          min-width="25%"
        >
          <template slot-scope="scope">
            <el-button
              type="primary"
              size="small"
              @click="onPreviewFile(scope.row)"
            >查看</el-button>
            <el-button
              type="warning"
              size="small"
              @click="onEdit(scope.row)"
            >编辑</el-button>
            <el-button
              type="danger"
              size="small"
              @click="onDeleteSeed(scope.row,scope.$index)"
            >删除</el-button>
            <el-button
              type="info"
              size="small"
              @click="onSimulate(scope.row,scope.$index)"
            >仿真</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination :page-total="pageTotal" @handleCurrentChange="handleCurrentChange" @handleSizeChange="handleSizeChange" />
      <modifySeedDialog v-if="modifySeedDialog.show" :is-show="modifySeedDialog.show" :dialog-row="modifySeedDialog.dialogRow" @getSeedList="getSeedList" @closeDialog="hideModifySeedDialog" />
      <addSeedDialog v-if="addSeedDialog.show" :is-show="addSeedDialog.show" :dialog-row="addSeedDialog.dialogRow" @getSeedList="getSeedList" @closeDialog="hideAddSeedDialog" />
      <previewFile v-if="previewSeedDialog.show" :is-show="previewSeedDialog.show" :dialog-row="previewSeedDialog.dialogRow" @closeDialog="hidePreviewDialog" />
    </div>
  </div>
</template>

<script>

import { mapGetters } from 'vuex'
import * as mutils from '@/utils/mUtils'
import SearchItem from './components/searchItem'
import ModifySeedDialog from '@/views/seed/components/modifySeedDialog'
import AddSeedDialog from '@/views/seed/components/addSeedDialog.vue'
import PreviewFile from './components/previewFile'
import Pagination from '@/components/pagination'

import { batchremoveMoney } from '@/api/money'
import { getFileContent } from '@/api/table'
import axios from 'axios'

export default {
  components: {
    SearchItem,
    Pagination,
    PreviewFile,
    AddSeedDialog,
    ModifySeedDialog
  },
  data() {
    return {
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
      modifySeedDialog: {
        show: false,
        dialogRow: {}
      },
      addSeedDialog: {
        type: 'add',
        show: false,
        dialogRow: {}
      },
      previewSeedDialog: {
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
        seedType: {
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
    console.log('mounted')
    this.getSeedList('')
  },
  methods: {
    filterSeedList(filename) {
      this.getSeedList(filename)
    },
    setTableHeight() {
      this.$nextTick(() => {
        this.tableHeight = document.body.clientHeight - 300
      })
    },
    // 获取种子文件列表
    getSeedList(filename) {
      axios.get('http://localhost:8080/seed/getAll', { params: { name: filename }}).then(res => {
        console.log('getSeedList', res.data.data)
        this.tableData = []
        res.data.data.forEach((item) => {
          item.carlaMap = 'Town_' + ('00' + item.carlaMap).slice(-2)
          this.tableData.push(item)
        })
        this.loading = false
        this.pageTotal = res.data.data.length
      })
    },
    // 删除数据
    onDeleteSeed(row) {
      // console.log(this.tableData)
      this.$confirm('确认删除该记录吗?', '提示', {
        type: 'warning'
      })
        .then(() => {
          axios.post('http://localhost:8080/seed/delete/' + row.id).then(res => {
            console.log('getSeedDelete res:', res)
            this.tableData = []
            Array.from(res.data.data).forEach((item) => {
              item.carlaMap = 'Town_' + ('00' + item.carlaMap).slice(-2)
              this.tableData.push(item)
            })
            this.loading = false
          })
        })
        .catch(() => {})
    },
    onSimulate(row) {
      this.$message({
        message: '仿真任务创建成功',
        type: 'success',
        duration: 2000
      })
      axios.post('http://localhost:8080/seed/simulate/' + row.id).then(res => {
        // console.log(res.data.success)
        this.loading = false
      })
    },
    onBatchDelMoney() {
      this.$confirm('确认批量删除记录吗?', '提示', {
        type: 'warning'
      })
        .then(() => {
          const ids = this.rowIds.map(item => item.id).toString()
          console.log('choose ids:', ids)
          const para = { ids: ids }
          batchremoveMoney(para).then(res => {
            this.$message({
              message: '批量删除成功',
              type: 'success'
            })
            this.getSeedList()
          })
        })
        .catch(() => {})
    },
    showModifySeedDialog(val) {
      // this.$store.commit('SET_DIALOG_TITLE', val)
      this.modifySeedDialog.show = true
    },
    showAddSeedDialog(val) {
      // this.$store.commit('SET_DIALOG_TITLE', val)
      this.addSeedDialog.show = true
    },
    showPreviewDialog(val) {
      // this.$store.commit('SET_DIALOG_TITLE', val)
      this.previewSeedDialog.show = true
    },
    hideModifySeedDialog() {
      this.modifySeedDialog.show = false
    },
    hideAddSeedDialog() {
      this.addSeedDialog.show = false
    },
    hidePreviewDialog() {
      this.previewSeedDialog.show = false
    },
    // 上下分页
    handleCurrentChange(val) {
      this.incomePayData.page = val
      this.getSeedList()
    },
    // 每页显示多少条
    handleSizeChange(val) {
      this.incomePayData.limit = val
      this.getSeedList()
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
      const type = parseInt(item.type)
      return this.format_type_list[type]
    },
    filterType(value, item) {
      const type = parseInt(item.type)
      return this.format_type_list[value] === this.format_type_list[type]
    },
    // 查看操作方法
    onPreviewFile(row) {
      this.previewSeedDialog.dialogRow = { ...row }
      // console.log('onPreviewFile', this.previewSeedDialog.dialogRow)
      this.showPreviewDialog('preview')
    },
    // 编辑操作方法
    onEdit(row) {
      this.modifySeedDialog.dialogRow = { ...row }
      this.showModifySeedDialog()
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

