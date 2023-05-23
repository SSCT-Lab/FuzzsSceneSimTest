<template>
  <el-dialog
    :visible.sync="isVisible"
    :title="modifySeedDialog.title"
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
        <el-form-item prop="seedName" label="种子文件名:">
          <el-input v-model="form.seedName" :disabled="true"/>
        </el-form-item>

        <el-form-item prop="carlaMap" label="Carla地图号:">
          <el-input v-model.number="form.carlaMap" />
        </el-form-item>

        <el-form-item prop="type" label="场景类型:">
          <el-select
            v-model="form.type"
            class="filter-item"
            placeholder="请选择"
            style="width: auto; "
          >
            <el-option
              v-for="item in types"
              :key="item.key"
              :label="item.label"
              :value="item.key"
            />
          </el-select>
        </el-form-item>

        <el-form-item prop="desc" label="场景描述:">
          <el-input v-model="form.desc" />
        </el-form-item>

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
import ModifySeedDialog from '@/views/seed/components/modifySeedDialog'
import axios from 'axios'

export default {
  name: 'ModifyModelDialog',
  props: {
    isShow: Boolean,
    dialogRow: Object
  },
  data() {
    const validateData = (rule, value, callback) => {
      if (value === '') {
        let text
        if (rule.field === 'income') {
          text = '收入'
        } else if (rule.field === 'pay') {
          text = '支出'
        } else if (rule.field === 'accoutCash') {
          text = '账户现金'
        }
        callback(new Error(text + '不能为空~'))
      } else {
        const numReg = /^[0-9]+.?[0-9]*$/
        if (!numReg.test(value)) {
          callback(new Error('请输入正数值'))
        } else {
          callback()
        }
      }
    }
    return {
      fileList: [],
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      areaData: [],
      isVisible: this.isShow,
      form: {
        carlaMap: '',
        type: '',
        desc: '',
        incomePayType: '',
        address: [],
        tableAddress: '',
        pay: '',
        accoutCash: '',
        remarks: ''
      },
      types: [
        { label: '原始场景', key: '0' },
        { label: '变异场景', key: '1' }
      ],
      form_rules: {
        // prop属性用于规则验证
        carlaMap: [
          { required: true, validator: validateData, message: '地图号不能为空！', trigger: 'blur' }
        ],
        type: [
          { required: true, trigger: 'blur' }
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
    modifySeedDialog() {
      return ModifySeedDialog
    },
    ...mapGetters(['modifySeedDialog'])
  },
  created() {
    this.areaData = AreaJson
  },
  mounted() {
    this.form.seedName = this.dialogRow.name
    this.form.carlaMap = parseInt(this.dialogRow.carlaMap.split('_')[1])
    this.form.type = this.dialogRow.type
    this.form.desc = this.dialogRow.desc
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
      // var param = new FormData()
      // param.append('name', this.form.seedName)
      // param.append('carla_map', this.form.seedname)
      // param.append('desc', this.form.desc)
      // console.log('edit param' + param)
      // console.log(param.getAll('file'))
      axios.post('http://localhost:8080/seed/modify',
        { id: this.dialogRow.id, carla_map: this.form.carlaMap, type: this.form.type, desc: this.form.desc })
        .then(res => {
          console.log(res)
          this.isVisible = false
          this.$emit('getSeedList', '')
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
