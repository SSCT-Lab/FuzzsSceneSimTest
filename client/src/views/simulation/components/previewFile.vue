<template>
  <div class="dialog-wrapper">
    <el-dialog
      v-dialogDrag
      :visible.sync="isVisible"
      title="场景可视化展示"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :modal-append-to-body="false"
      :style="{ width: 'calc(100% + 400px)', height: 'calc(100% + 250px)',  top: '50%', left: '50%', transform: 'translate(-50%, -55%)' }"
      @close="closeDialog"
    >
      <div class="form">
        <el-form
          ref="form"
          :model="form"
          :label-width="dialog.formLabelWidth"
          style="margin:10px;width:auto;"
        >
          <div class="image-container">
            <img :src="imageUrl" class="image">
          </div>
        </el-form>
      </div>
    </el-dialog>
  </div>
</template>
<style lang="less" scoped>
div {
  width: 100%;
  height: 100%;
  display: flex; /* 设置父元素为 flex 布局 */
  justify-content: center; /* 水平居中 */
  align-items: center; /* 垂直居中 */
  //}
}
.dialog-wrapper {
  width: 100%;
  height: 100%;
}
.image-container {
  text-align: center;
}
.image {
  max-width: 100%;
  max-height: calc(100% - 50px); /* 留出一些空间放标题栏 */
  margin: 0 auto;
}
</style>
<script>
import { mapState, mapGetters } from 'vuex'
import axios from 'axios'
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
      imageUrl: '',
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
    this.getImage(this.dialogRow.id)
  },
  methods: {
    async getImage(id) {
      try {
        const response = await axios.get('http://localhost:8080/sim_result/visualize/' + id, { responseType: 'blob' })
        console.log('getImage response', response.data)
        this.imageUrl = URL.createObjectURL(response.data)
      } catch (error) {
        console.error(error)
      }
    },
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
.dialog-wrapper {
  width: 100%;
  height: 100%;
  display: flex; /* 设置父元素为 flex 布局 */
  justify-content: center; /* 水平居中 */
  align-items: center; /* 垂直居中 */
}
.image-container {
  text-align: center;
}
.image {
  max-width: 100%;
  max-height: calc(100% - 50px); /* 留出一些空间放标题栏 */
  margin: 0 auto;
}
</style>

