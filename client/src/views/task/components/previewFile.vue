<template>
  <el-dialog
    v-dialogDrag
    :visible.sync="isVisible"
    title="错误可视化结果"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    :modal-append-to-body="false"
    width="75%"
    @close="closeDialog"
  >
    <el-collapse v-model="activeName" accordion @change="handleCollapseChange">
      <el-collapse-item v-for="(item, index) in items" :key="index" :title="item.title" :name="item.name">
        <div class="form">
          <el-form
            ref="form"
            style="margin:10px;width:auto;"
          >
            <img :src="item.oriImageUrl" class="image" alt="">
            <img :src="item.transImageUrl" class="image" alt="">
          </el-form>
        </div>
      </el-collapse-item>
    </el-collapse>

  </el-dialog>
</template>

<script>
import { mapState, mapGetters } from 'vuex'
import { addMoney, previewFile, updateMoney } from '@/api/money'
import { getFileContent, getList } from '@/api/table'
import axios from 'axios'
import JSZip from 'jszip'
import {str} from "mockjs/src/mock/random/basic";

export default {
  name: 'PreviewFile',
  props: {
    isShow: Boolean,
    dialogRow: Object
  },
  data() {
    return {
      activeName: '',
      oriImageUrl: '',
      transImageUrl: '',
      // items: [
      //   {
      //     title: 'Model1',
      //     id: 40,
      //     name: '1',
      //     form: {
      //       // Model1 对应的表单数据
      //       // ...
      //     },
      //     oriImageUrl: 'https://example.com/model1_ori.jpg',
      //     transImageUrl: 'https://example.com/model1_trans.jpg'
      //   },
      //   {
      //     title: 'Model2',
      //     name: '2',
      //     form: {
      //       // Model2 对应的表单数据
      //       // ...
      //     },
      //     oriImageUrl: 'https://example.com/model2_ori.jpg',
      //     transImageUrl: 'https://example.com/model2_trans.jpg'
      //   }
      // ],
      items: [],
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
      }
    }
  },
  computed: {
    ...mapGetters(['previewDialog'])
  },
  created() {
  },
  mounted() {
    console.log('dialogRow', this.dialogRow)
    this.getModelsInfo(this.dialogRow.id)
    // this.getImages(this.dialogRow.id, 0)
    // this.getImages(this.dialogRow.id).then(response => {
    // })
  },
  methods: {
    getModelsInfo(id) {
      axios.get('http://localhost:8080/testTask/getModels/' + id).then(response => {
        console.log('getModelsInfo: ', response.data)
        response.data.data.forEach((item, index) => {
          this.items.push({
            title: item.name,
            id: item.id,
            name: (index + 1) + '',
            oriImageUrl: '',
            transImageUrl: ''
          })
        })
      }).catch(error => {
        console.error(error)
      })
    },
    handleCollapseChange(activeNames) {
      // console.log('handleCollapseChange', activeNames)
      // 判断当前展开的面板是否为您需要执行操作的面板
      const activeItem = this.items.find(item => item.name === activeNames[0])
      if (activeItem) {
        if (activeItem.oriImageUrl === '' || activeItem.transImageUrl === '') {
          this.getImages(this.dialogRow.id, activeItem.name - 1)
        }
      }
    },
    async getImages(taskId, itemId) {
      try {
        const response = await axios.get('http://localhost:8080/testResult/getVisualizeResult/' + taskId, {
          responseType: 'blob',
          params: {
            modelName: this.items[itemId].title,
            type: 'ori'
          }
        })
        this.items[itemId].oriImageUrl = URL.createObjectURL(response.data)
      } catch (error) {
        console.error(error)
      }

      try {
        const response = await axios.get('http://localhost:8080/testResult/getVisualizeResult/' + taskId, {
          responseType: 'blob',
          params: {
            modelName: this.items[itemId].title,
            type: 'trans'
          }
        })
        this.items[itemId].transImageUrl = URL.createObjectURL(response.data)
      } catch (error) {
        console.error(error)
      }
    },
    previewFile,
    closeDialog() {
      this.$emit('closeDialog')
    }
  }
}
</script>

<style lang="less" scoped>
div {
  width: 100%;
  height: 100%;
  img {
    width: 50%
  };
}
</style>
