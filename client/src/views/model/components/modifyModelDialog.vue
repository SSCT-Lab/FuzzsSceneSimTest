<template>
  <el-dialog
    :visible.sync="isVisible"
    :title="modifyModelDialog.title"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    :modal-append-to-body="false"
    @close="closeDialog"
  >
    <div class="form">
      <el-form
        ref="form"
        :model="form"
        :rules="form_rules"
        :label-width="dialog.formLabelWidth"
        style="margin:10px;width:auto;"
      >

        <el-form-item prop="modelname" label="模型名:">
          <el-input v-model="form.modelname" type="text" />
        </el-form-item>

        <el-form-item prop="desc" label="模型描述:">
          <el-input v-model="form.desc" />
        </el-form-item>

        <!--        <el-form-item prop="pay" label="支出:">-->
        <!--          <el-input v-model.number="form.pay" />-->
        <!--        </el-form-item>-->

        <!--        <el-form-item prop="accoutCash" label="账户现金:">-->
        <!--          <el-input v-model.number="form.accoutCash" />-->
        <!--        </el-form-item>-->

        <el-form-item label="备注:">
          <el-input v-model="form.remarks" type="textarea" />
        </el-form-item>

        <el-form-item class="text_right">
          <el-button type="primary" @click="onSubmit('form')">提  交</el-button>
          <el-button @click="isVisible = false">取 消</el-button>
        </el-form-item>

      </el-form>
    </div>
  </el-dialog>
</template>

<script>
import { mapState, mapGetters } from 'vuex'
import { addMoney, updateMoney, uploadModel } from '@/api/money'
import AreaJson from '@/assets/datas/area.json'
import modifyModelDialog from '@/views/model/components/modifyModelDialog.vue'
import axios from 'axios'

export default {
  name: 'ModifyModelDialog',
  props: {
    isShow: Boolean,
    dialogRow: Object
  },
  data() {
    // const validateData = (rule, value, callback) => {
    //   if (value === '') {
    //     let text
    //     if (rule.field === 'income') {
    //       text = '收入'
    //     } else if (rule.field === 'pay') {
    //       text = '支出'
    //     } else if (rule.field === 'accoutCash') {
    //       text = '账户现金'
    //     }
    //     callback(new Error(text + '不能为空~'))
    //   } else {
    //     const numReg = /^[0-9]+.?[0-9]*$/
    //     if (!numReg.test(value)) {
    //       callback(new Error('请输入正数值'))
    //     } else {
    //       callback()
    //     }
    //   }
    // }
    return {
      fileList: [],
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      areaData: [],
      isVisible: this.isShow,
      form: {
        incomePayType: '',
        address: [],
        tableAddress: '',
        modelname: '',
        desc: '',
        pay: '',
        accoutCash: '',
        remarks: ''
      },
      payType: [
        { label: '原始场景', value: '0' },
        { label: '变异场景', value: '1' }
      ],
      form_rules: {
        modelname: [
          { required: true, message: '模型名不能为空！', trigger: 'blur' }
        ],
        desc: [
          { required: true, message: '模型描述不能为空！', trigger: 'blur' }
        ]
        // income: [
        //   { required: true, validator: validateData, trigger: 'blur' }
        // ],
        // pay: [
        //   { required: true, validator: validateData, trigger: 'blur' }
        // ],
        // accoutCash: [
        //   { required: true, validator: validateData, trigger: 'blur' }
        // ],
        // incomePayType: [
        //   { required: true, message: '请选择收支类型', trigger: 'change' }
        // ],
        // address: [
        //   { required: true, message: '请选择籍贯', trigger: 'change' }
        // ]
      },
      // 详情弹框信息
      dialog: {
        width: '400px',
        formLabelWidth: '120px'
      }
    }
  },
  computed: {
    modifyModelDialog() {
      return modifyModelDialog
    },
    ...mapGetters(['modifyModelDialog'])
  },
  created() {
    this.areaData = AreaJson
  },
  mounted() {
    this.form.modelname = this.dialogRow.name
    this.form.desc = this.dialogRow.desc
    if (this.modifyModelDialog.type === 'edit') {
      this.form = this.dialogRow
      console.log(this.form)
      this.form.incomePayType = (this.dialogRow.incomePayType).toString()
      // this.form.address = ["120000", "120200", "120223"]
    } else {
      this.$nextTick(() => {
        this.$refs['form'].resetFields()
      })
    }
  },
  methods: {
    emptyFunction() {
      // 空函数，不做任何操作
    },
    handleBeforeUpload(file) {
      this.form.username = file.name
      return null
    },
    handleFileChange(file, fileList) {
      this.fileList = fileList
    },
    getCascaderObj(val, opt) {
      return val.map(function(value, index, array) {
        for (var item of opt) {
          if (item.value === value) {
            opt = item.children
            return item.label
          }
        }
        return null
      })
    },
    handleChange(value) {
      console.log([...value]) // ["120000", "120200", "120223"]
      this.form.address = [...value]
      const vals = this.getCascaderObj([...value], this.areaData) // arr
      this.form.tableAddress = vals.join(',').replace(/,/g, '')
    },
    closeDialog() {
      this.$emit('closeDialog')
    },
    // 表单提交
    onSubmit() {
      var param = new FormData()
      param.append('id', this.dialogRow.id)
      param.append('name', this.form.modelname)
      param.append('desc', this.form.desc)
      console.log('edit param' + param)
      // console.log(param.getAll('file'))
      axios.post('http://localhost:8080/model/modify', param).then(res => {
        console.log(res)
        this.isVisible = false
        this.$emit('getModelList', '')
      })

      // uploadModel(param).then(responce => {
      //   this.$message({
      //     message: '上传成功！',
      //     duration: 1000
      //   })
      // })

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
.search_container{
  margin-bottom: 20px;
}
.btnRight{
  float: right;
  margin-right: 0px !important;
}
.searchArea{
  background:rgba(255,255,255,1);
  border-radius:2px;
  padding: 18px 18px 0;
}
</style>
