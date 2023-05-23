<template>
  <el-dialog
    :visible.sync="isVisible"
    :title="previewDialog.title"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    :modal-append-to-body="false"
    @close="closeDialog"
    v-dialogDrag
  >
    <div class="form">
      <el-form
        ref="form"
        :model="form"
        :label-width="dialog.formLabelWidth"
        style="margin:10px;width:auto;"
      >

        <el-form-item label="内容:">
          <el-input v-model="form.remarks" type="textarea" />
        </el-form-item>

        <el-form-item class="text_right">
          <el-button @click="isVisible = false">取 消</el-button>
<!--          <el-button type="primary" @click="onSubmit('form')">提  交</el-button>-->
        </el-form-item>

      </el-form>
    </div>
  </el-dialog>
</template>

<script>
import { mapState, mapGetters } from 'vuex'
import { addMoney, previewFile, updateMoney } from '@/api/money'
import { getFileContent, getList } from '@/api/table'

export default {
  name: 'PreviewFile',
  props: {
    isShow: Boolean,
    dialogRow: Object
  },
  data() {
    return {
      areaData: [],
      isVisible: this.isShow,
      form: {
        incomePayType: '',
        address: [],
        tableAddress: '',
        username: '',
        income: '',
        pay: '',
        accoutCash: '',
        remarks: ''
      },
      // 详情弹框信息
      dialog: {
        width: '400px',
        formLabelWidth: '120px'
      }
    }
  },
  computed: {
    ...mapGetters(['previewDialog'])
  },
  created() {
  },
  mounted() {
    getFileContent().then(response => {
      this.form.remarks = response.data.items
    })
  },
  methods: {
    previewFile,
    closeDialog() {
      this.$emit('closeDialog')
    },
    onSubmit(form) {
      // this.$refs[form].validate((valid) => {
      //   if (valid) { // 表单数据验证完成之后，提交数据;
      //     const formData = this[form]
      //     const para = Object.assign({}, formData)
      //     console.log(para)
      //     // edit
      //     if (this.addFundDialog.type === 'edit') {
      //       updateMoney(para).then(res => {
      //         this.$message({
      //           message: '修改成功',
      //           type: 'success'
      //         })
      //         this.$refs['form'].resetFields()
      //         this.isVisible = false
      //         this.$emit('getFundList')
      //       })
      //     } else {
      //       // add
      //       addMoney(para).then(res => {
      //         this.$message({
      //           message: '新增成功',
      //           type: 'success'
      //         })
      //         this.$refs['form'].resetFields()
      //         this.isVisible = false
      //         this.$emit('getFundList')
      //       })
      //     }
      //   }
      // })
    }
  }
}
</script>

<style lang="less" scoped>

</style>
