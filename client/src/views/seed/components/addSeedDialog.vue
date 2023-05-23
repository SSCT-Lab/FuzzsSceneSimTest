<template>
  <el-dialog
    :visible.sync="isVisible"
    :title="addSeedDialog.title"
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

        <el-form-item prop="file" label="选择场景文件">
          <el-upload
            action="#"
            :headers="headers"
            :auto-upload="false"
            :limit="1"
            :file-list="fileList"
            :on-change="handleFileChange"
            >
            <el-button size="small" type="primary">点击选择文件</el-button>
          </el-upload>
        </el-form-item>

        <el-form-item prop="seedName" label="种子文件名:">
          <el-input v-model="form.seedName" />
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

        <!--        <el-form-item prop="pay" label="支出:">-->
        <!--          <el-input v-model.number="form.pay" />-->
        <!--        </el-form-item>-->

        <!--        <el-form-item prop="accoutCash" label="账户现金:">-->
        <!--          <el-input v-model.number="form.accoutCash" />-->
        <!--        </el-form-item>-->

        <el-form-item label="备注:">
          <el-input v-model="form.remarks" type="textarea" />
        </el-form-item>
<!--        <el-form-item v-if="this.showProgress" label="上传进度">-->
<!--          <el-progress :text-inside="true" :stroke-width="26" :percentage="this.uploadPercentage" />-->
<!--        </el-form-item>-->
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
import addSeedDialog from '@/views/seed/components/addSeedDialog'
import axios from 'axios'

export default {
  name: 'AddSeedDialog',
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
      showProgress: false,
      fileList: [],
      uploadPercentage: 0,
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      areaData: [],
      isVisible: this.isShow,
      form: {
        seedName: '',
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
        seedName: [
          { required: true, message: '模型名不能为空！', trigger: 'blur' }
        ],
        carlaMap: [
          { required: true, validator: validateData, message: '地图号不能为空！', trigger: 'blur' }
        ],
        type: [
          { required: true, trigger: 'blur' }
        ],
        file: [
          { required: true, trigger: 'blur' }
        ],
        desc: [
          { required: true, message: '模型描述不能为空！', trigger: 'blur' }
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
    addSeedDialog() {
      return addSeedDialog
    },
    ...mapGetters(['addSeedDialog'])
  },
  created() {
    this.areaData = AreaJson
  },
  mounted() {
    this.$nextTick(() => {
      this.$refs['form'].resetFields()
    })
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
      this.form.seedName = file.name
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
    onSubmit(form) {
      const param = new FormData()
      // console.log('###' + this.fileList)
      this.fileList.forEach(
        (val, index) => {
          // console.log('cnt')
          param.append('file', val.raw)
        }
      )
      const seedInfo = { 'name': this.form.seedName, 'carlaMap': this.form.carlaMap, 'type': this.form.type, 'desc': this.form.desc }
      param.append('seedInfo', JSON.stringify(seedInfo))

      const config = {
        headers: {
          'Content-Type': 'multipart/form-data'
        },
        onUploadProgress: progressEvent => {
          const percent = (progressEvent.loaded / progressEvent.total * 100 | 0)		// 上传进度百分比
          this.uploadPercentage = percent
        }
      }
      this.showProgress = true

      axios.post('http://localhost:8080/seed/upload', param, config).then(res => {
        console.log(res)
        this.isVisible = false
        this.$emit('getSeedList', '')
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
