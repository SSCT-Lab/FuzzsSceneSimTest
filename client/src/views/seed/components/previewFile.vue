<template>
  <el-dialog
    v-dialogDrag
    :visible.sync="isVisible"
    :title="previewDialog.title"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    :modal-append-to-body="false"
    @close="closeDialog"
  >
    <div class="form">
      <el-form
        ref="form"
        :model="form"
        :label-width="dialog.formLabelWidth"
        style="margin:10px;width:auto;"
      >

        <el-form-item label="种子文件内容:">
          <el-input v-model="form.contents" type="textarea" />
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
import axios from 'axios'

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
        contents: '',
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
    axios.get('http://localhost:8080/seed/preview/' + this.dialogRow.id).then(response => {
      console.log(response.data)
      this.form.contents = response.data.data
    })
  },
  methods: {
    previewFile,
    closeDialog() {
      this.$emit('closeDialog')
    },
    onSubmit(form) {
    }
  }
}
</script>

<style lang="less" scoped>

</style>
