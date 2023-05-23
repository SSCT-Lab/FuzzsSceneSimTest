<template>
  <el-dialog
    :visible.sync="isVisible"
    :title="addFundDialog.title"
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

        <el-form-item prop="username" label="种子文件名:">
          <el-input v-model="form.username" type="text" />
        </el-form-item>

        <el-form-item prop="address" label="Carla地图号:">
          <el-input v-model="form.address" type="text" />
        </el-form-item>

        <el-form-item prop="incomePayType" label="场景类型:">
          <el-select v-model="form.incomePayType" placeholder="场景类型">
            <el-option
              v-for="item in payType"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item prop="income" label="场景描述:">
          <el-input v-model.number="form.income" />
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
          <el-button @click="isVisible = false">取 消</el-button>
          <el-button type="primary" @click="onSubmit('form')">提  交</el-button>
        </el-form-item>

      </el-form>
    </div>
  </el-dialog>
</template>

<script>
import { mapState, mapGetters } from 'vuex'
import { addMoney, updateMoney } from '@/api/money'
import AreaJson from '@/assets/datas/area.json'

export default {
  name: 'AddFundDialogs',
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
      payType: [
        { label: '原始场景', value: '0' },
        { label: '变异场景', value: '1' }
      ],
      form_rules: {
        username: [
          { required: true, message: '用户名不能为空！', trigger: 'blur' }
        ],
        income: [
          { required: true, validator: validateData, trigger: 'blur' }
        ],
        pay: [
          { required: true, validator: validateData, trigger: 'blur' }
        ],
        accoutCash: [
          { required: true, validator: validateData, trigger: 'blur' }
        ],
        incomePayType: [
          { required: true, message: '请选择收支类型', trigger: 'change' }
        ],
        address: [
          { required: true, message: '请选择籍贯', trigger: 'change' }
        ]
      },
      // 详情弹框信息
      dialog: {
        width: '400px',
        formLabelWidth: '120px'
      }
    }
  },
  computed: {
    ...mapGetters(['addFundDialog'])
  },
  created() {
    this.areaData = AreaJson
  },
  mounted() {
    if (this.addFundDialog.type === 'edit') {
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
      this.$refs[form].validate((valid) => {
        if (valid) { // 表单数据验证完成之后，提交数据;
          const formData = this[form]
          const para = Object.assign({}, formData)
          console.log(para)
          // edit
          if (this.addFundDialog.type === 'edit') {
            updateMoney(para).then(res => {
              this.$message({
                message: '修改成功',
                type: 'success'
              })
              this.$refs['form'].resetFields()
              this.isVisible = false
              this.$emit('getFundList')
            })
          } else {
            // add
            addMoney(para).then(res => {
              this.$message({
                message: '新增成功',
                type: 'success'
              })
              this.$refs['form'].resetFields()
              this.isVisible = false
              this.$emit('getFundList')
            })
          }
        }
      })
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
