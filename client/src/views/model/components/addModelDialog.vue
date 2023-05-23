<template>
  <el-dialog
    :visible.sync="isVisible"
    :title="addModelDialog.title"
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

        <el-form-item prop="modelFile" label="上传模型">
          <el-upload
            action="#"
            :headers="headers"
            :auto-upload="false"
            :limit="1"
            :file-list="fileList"
            :on-change="handleFileChange"
            :on-progress="handleProgress"
          >
            <el-button size="small" type="primary">点击选择文件</el-button>
          </el-upload>
        </el-form-item>

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
        <el-form-item v-if="this.showProgress" label="上传进度">
          <el-progress :text-inside="true" :stroke-width="26" :percentage="this.uploadPercentage" />
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
import addModelDialog from '@/views/model/components/addModelDialog.vue'
import axios from 'axios'

export default {
  name: 'AddModelDialog',
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
      showProgress: false,
      fileList: [],
      uploadPercentage: 0,
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
        filename: '',
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
        modelFile: [
          { required: true, message: '模型文件不能为空！', trigger: 'blur' }
        ],
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
    addModelDialog() {
      return addModelDialog
    },
    ...mapGetters(['addModelDialog'])
  },
  created() {
    this.areaData = AreaJson
  },
  mounted() {
    if (this.addModelDialog.type === 'edit') {
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
      this.form.modelname = file.name.split('.')[0]
      this.form.filename = file.name
      this.fileList = fileList
    },
    handleProgress(event, file, fileList) {
      console.log(event, file, fileList)
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
    onSubmit(form) {
      var param = new FormData()
      this.fileList.forEach(
        (val, index) => {
          // console.log('cnt')
          param.append('file', val.raw)
        }
      )
      param.append('name', this.form.modelname)
      param.append('fileName', this.form.filename)
      param.append('desc', this.form.desc)

      const config = {
        headers: {
          'Content-Type': 'multipart/form-data'
        },
        transformRequest: [function(data) {
          return data
        }],
        onUploadProgress: progressEvent => {
          const percent = (progressEvent.loaded / progressEvent.total * 100 | 0)		// 上传进度百分比
          this.uploadPercentage = percent
        }
      }
      this.showProgress = true
      axios.post('http://localhost:8080/model/upload', param, config)	// 地址写你们自己的接口
        .then(response => {
          const result = response.data
          console.log('upload result', result)
          this.isVisible = false
          this.$emit('getModelList', '')
          this.$message({
            message: '上传成功！',
            type: 'success',
            duration: 1000
          })
          // if (result.status == 0) {
          //   console.log(result)
          // } else {
          //   this.$message({
          //     message: '上传失败',
          //     type: 'error',
          //     duration: '1000'
          //   })
          // }
        }).catch(err => {
          console.log(err)
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
