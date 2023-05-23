<template>
  <div class="app-container">
    <el-form ref="form" :model="form" label-width="120px">
      <el-form-item label="任务名">
        <el-input v-model="form.configuration.taskName" style="width: 50%; " />
      </el-form-item>
      <el-form-item label="模型选择">
        <el-select
          v-model="form.configuration.selectedModels"
          :multiple-limit="4"
          class="filter-item"
          placeholder="请选择"
          multiple
          style="width: 50%; "
        >
          <el-option
            v-for="item in form.models"
            :key="item.key"
            :label="item.label"
            :value="item.key"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="种子文件选择">
        <el-select
          v-model="form.configuration.selectedSeeds"
          :multiple-limit="6"
          class="filter-item"
          placeholder="请选择"
          multiple
          style="width: 50%; "
        >
          <el-option
            v-for="item in form.seeds"
            :key="item.key"
            :label="item.label"
            :value="item.key"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="变异算子选择">
        <el-select
          v-model="form.configuration.selectedMutSeeds"
          class="filter-item"
          placeholder="请选择"
          multiple
          style="width: 50%; "
        >
          <el-option
            v-for="item in form.mutSeeds"
            :key="item.key"
            :label="item.label"
            :value="item.key"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="优先级队列大小">
        <el-input v-model="form.configuration.priQueSize" style="width: 50%;" />
      </el-form-item>
      <el-form-item label="初始种群大小">
        <el-input v-model="form.configuration.initPopNum" style="width: 50%;" />
      </el-form-item>
      <!--      <el-form-item label="Activity zone">-->
      <!--        <el-select v-model="form.region" placeholder="please select your zone">-->
      <!--          <el-option label="Zone one" value="shanghai" />-->
      <!--          <el-option label="Zone two" value="beijing" />-->
      <!--        </el-select>-->
      <!--      </el-form-item>-->
      <!--      <el-form-item label="Activity time">-->
      <!--        <el-col :span="11">-->
      <!--          <el-date-picker v-model="form.date1" type="date" placeholder="Pick a date" style="width: 100%;" />-->
      <!--        </el-col>-->
      <!--        <el-col :span="2" class="line">-</el-col>-->
      <!--        <el-col :span="11">-->
      <!--          <el-time-picker v-model="form.date2" type="fixed-time" placeholder="Pick a time" style="width: 100%;" />-->
      <!--        </el-col>-->
      <!--      </el-form-item>-->
      <el-form-item label="是否采样">
        <el-switch v-model="form.configuration.sampling" />
      </el-form-item>
      <el-form-item label="算子随机轮数">
        <el-input v-model="form.configuration.radarLoopNum" style="width: 50%;" />
      </el-form-item>
      <el-form-item label="测试循环轮数">
        <el-input v-model="form.configuration.loopNum" style="width: 50%;" />
      </el-form-item>
      <!--      <el-form-item label="Activity type">-->
      <!--        <el-checkbox-group v-model="form.type">-->
      <!--          <el-checkbox label="Online activities" name="type" />-->
      <!--          <el-checkbox label="Promotion activities" name="type" />-->
      <!--          <el-checkbox label="Offline activities" name="type" />-->
      <!--          <el-checkbox label="Simple brand exposure" name="type" />-->
      <!--        </el-checkbox-group>-->
      <!--      </el-form-item>-->
      <el-form-item label="适应度函数">
        <el-radio-group v-model="form.configuration.fitnessFuc">
          <el-radio label="Error" />
          <el-radio label="Diversity" />
          <el-radio label="Error+Diversity" />
        </el-radio-group>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.desc" style="width: 50%;" type="textarea" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :plain="true" @click="onSubmit">提交</el-button>
        <el-button @click="onCancel">取消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  data() {
    return {
      form: {
        models: [],
        seeds: [],
        mutSeeds: [{ label: '车辆颜色', key: 'vehicleColor' },
          { label: '时间', key: 'time' },
          { label: '光照强度', key: 'sunIntensity' },
          { label: '雾天', key: 'fog' },
          { label: '雨天', key: 'rain' }],
        configuration: {
          taskName: '',
          selectedModels: [],
          selectedSeeds: [],
          selectedMutSeeds: [],
          priQueSize: '',
          initPopNum: '',
          sampling: false,
          radarLoopNum: '',
          loopNum: '',
          fitnessFuc: ''
        },
        region: '',
        date1: '',
        date2: '',
        type: [],
        desc: ''
      }
    }
  },
  mounted() {
    axios.get('http://localhost:8080/testTask/init').then(response => {
      console.log(response.data.data)
      this.form.models = []
      this.form.seeds = []
      this.form.mutSeeds = []
      response.data.data[0].forEach((item) => {
        this.form.models.push({ label: item.name, key: item.id })
      })
      response.data.data[1].forEach((item) => {
        this.form.seeds.push({ label: item.name, key: item.id })
      })
      response.data.data[2].forEach((item) => {
        this.form.mutSeeds.push({ label: item.label, key: item.id })
      })
    })
  },
  methods: {
    onSubmit() {
      const param = new FormData()
      param.append('name', this.form.configuration.taskName)
      param.append('selectedModels', this.form.configuration.selectedModels)
      param.append('selectedSeeds', this.form.configuration.selectedSeeds)
      param.append('selectedMutSeeds', this.form.configuration.selectedMutSeeds)
      param.append('desc', this.form.desc)
      const subparam = { 'priQueueSize': this.form.configuration.priQueSize,
        'initPopSize': this.form.configuration.initPopNum,
        'sampling': this.form.configuration.sampling,
        'radarLoop': this.form.configuration.radarLoopNum,
        'loop': this.form.configuration.loopNum,
        'fitFunction': this.form.configuration.fitnessFuc }
      param.append('config', JSON.stringify(subparam))
      // console.log('CREATE param' + param.get('name') + param.get('config'))
      axios.post('http://localhost:8080/testTask/create', param).then(res => {
        console.log(res)
        this.$message({
          message: '测试任务创建成功',
          type: 'success'
        })
        // this.$emit('getModelList', '')
      })
    },
    onCancel() {
      this.$message({
        message: 'cancel!',
        type: 'warning'
      })
    }
  }
}
</script>

<style scoped>
.line{
  text-align: center;
}
</style>

