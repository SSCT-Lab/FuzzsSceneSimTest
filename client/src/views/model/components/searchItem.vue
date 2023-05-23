<template>
  <div class="search_container searchArea">
    <el-form
      ref="search_data"
      :inline="true"
      :model="search_data"
      :rules="rules"
      class="demo-form-inline search-form"
    >
      <el-form-item label="">
        <el-input v-model="search_data.name" placeholder="文件名" @keyup.enter.native="onFilter()" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" size="small" icon="search" @click="onFilter()">筛选</el-button>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" size="small" icon="view" @click="onAddMoney()">上传</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
  name: 'SearchItem',
  data() {
    return {
      search_data: {
        startTime: '',
        endTime: '',
        name: ''
      },
      rules: {
        name: [
          { required: true, message: '请输入用户名', trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    ...mapGetters(['searchBtnDisabled'])

  },
  created() {
  },
  methods: {
    onFilter() {
      this.$emit('filterList', this.search_data.name)
    },
    onAddMoney() {
      this.$emit('showDialog', 'add')
    },
    onBatchDelMoney() {
      this.$emit('onBatchDelMoney')
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
