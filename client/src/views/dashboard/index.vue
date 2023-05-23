<template>
  <div class="dashboard-container">
    <div class="dashboard-editor-container">
      <el-row style="background:#fff;padding:16px 16px 16px;margin-bottom:32px;">
        <el-col :span="6">
          <div class="chart-wrapper">
            <radar-chart v-if="flag" :chart-data="radarChartData[0]" v-show="radarChartShow[0]"/>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="chart-wrapper">
            <radar-chart v-if="flag" :chart-data="radarChartData[1]" v-show="radarChartShow[1]"/>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="chart-wrapper">
            <radar-chart v-if="flag" :chart-data="radarChartData[2]" v-show="radarChartShow[2]"/>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="chart-wrapper">
            <radar-chart v-if="flag" :chart-data="radarChartData[3]" v-show="radarChartShow[3]"/>
          </div>
        </el-col>
        <el-col :span="24" style="text-align: center; margin-top: -20px; font-size: 18px; font-weight: normal; color: #008ACD;">
          不同语义变异算子对模型MSE扰动结果图
        </el-col>
      </el-row>

      <el-row class="chart-row" :gutter="32">
        <el-col :span="12">
          <div class="chart-wrapper">
            <bar-chart v-if="flag" :data="barChartData"/>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="chart-wrapper">
            <line-chart v-if="flag" :data="lineChartData"/>
          </div>
        </el-col>
      </el-row>

<!--      <el-row :gutter="32">-->
<!--        <el-col :span="12" >-->
<!--          <div class="chart-wrapper">-->
<!--            <bar-chart />-->
<!--          </div>-->
<!--        </el-col>-->
<!--        <el-col :span="9">-->
<!--          <img src="../../../public/lineChart.png" class="image">-->
<!--        </el-col>-->
<!--      </el-row>-->

    </div>
  </div>
</template>

<script>
import RadarChart from '@/components/Echarts/RadarChart'
import BarChart from '@/components/Echarts/BarChart'
import LineChart from '@/components/Echarts/LineChart'
import axios from 'axios'

// const radarChartData = {
//   radarData1: {
//     title: 'Dave_V1',
//     dataA: [0.01693656, 0.01693656, 0.01693656, 0.01693656, 0.01693656],
//     dataB: [0.017615154, 0.026082465, 0.01848642, 0.022209135, 0.019575981],
//     showLegend: true
//   },
//   radarData2: {
//     title: 'Dave_V2',
//     dataA: [0.015959088, 0.015959088, 0.015959088, 0.015959088, 0.015959088],
//     dataB: [0.01652912, 0.022330145, 0.018156395, 0.019178614, 0.019593032],
//     showLegend: false
//   },
//   radarData3: {
//     title: 'Dave_V3',
//     dataA: [0.028190091, 0.028190091, 0.028190091, 0.028190091, 0.028190091],
//     dataB: [0.028732548, 0.033074924, 0.028015073, 0.028827736, 0.035553952],
//     showLegend: false
//   },
//   radarData4: {
//     title: 'Epoch',
//     dataA: [0.0259132119258627, 0.0259132119258627, 0.0259132119258627, 0.0259132119258627, 0.0259132119258627],
//     dataB: [0.0284872849188282, 0.0329320467918998, 0.0320784554350599, 0.0290082220087463, 0.0325741545396306],
//     showLegend: false
//   }
// }

export default {
  name: 'Dashboard',
  components: {
    RadarChart,
    BarChart,
    // eslint-disable-next-line vue/no-unused-components
    LineChart
  },
  data() {
    return {
      flag: false,
      testTaskRow: '',
      testResult: '',
      barChartData: {},
      lineChartData: {},
      radarChartShow: [false, false, false, false],
      radarChartData: [],
      // radarChartData1: radarChartData.radarData1,
      // radarChartData2: radarChartData.radarData2,
      // radarChartData3: radarChartData.radarData3,
      // radarChartData4: radarChartData.radarData4,
      legendWidth: '100%'
    }
  },
  mounted() {
    this.testTaskRow = this.$route.query
    // console.log('testTaskRow', this.testTaskRow)
    axios.get('http://localhost:8080/testResult/getResult/' + this.testTaskRow.id).then(res => {
      console.log('getResult res.data.data', res.data.data)
      this.testResult = res.data.data

      const modelName = []
      const barChartData = []
      const lineChartXAxis = []
      const lineChartData = []

      for (let i = 0; i < this.testResult.length; i++) {
        const item = this.testResult[i].testResult
        const mseItem = this.testResult[i].mse
        const radarChartDataItem = {
          title: item.modelName,
          dataA: [mseItem.oriMse, mseItem.oriMse, mseItem.oriMse, mseItem.oriMse, mseItem.oriMse],
          dataB: [mseItem.vcMse, mseItem.tmMse, mseItem.fgMse, mseItem.rnMse, mseItem.siMse],
          showLegend: i === 0
        }
        this.radarChartData.push(radarChartDataItem)
        this.radarChartShow[i] = true
        modelName.push(item.modelName)
        barChartData.push(item.divCount)
        const lineChartDataTemp = {
          name: item.modelName,
          type: 'line',
          data: []
        }
        item.errorCountPerLoop = item.errorCountPerLoop.substring(1, item.errorCountPerLoop.length - 1).split(',')
        for (let j = 0; j < item.errorCountPerLoop.length; j++) {
          if (j % 4 === 0 || j === item.errorCountPerLoop.length - 1) {
            if (i === 0) {
              lineChartXAxis.push(j)
            }
            lineChartDataTemp.data.push(parseInt(item.errorCountPerLoop[j]))
          }
        }
        lineChartData.push(lineChartDataTemp)
      }
      this.barChartData.xAxisData = modelName.map(name => ({
        value: `${name}`,
        textStyle: { fontSize: 15 }
      }))
      this.barChartData.series = { data: barChartData }
      console.log('barChartData: ', this.barChartData)

      this.lineChartData.legend = { data: modelName }
      this.lineChartData.xAxis = { data: lineChartXAxis }
      this.lineChartData.series = lineChartData
      console.log('lineChartData: ', this.lineChartData)

      console.log('radarChartData: ', this.radarChartData)
      console.log('radarChartShow: ', this.radarChartShow)
      // 控制图加载，只有数据就绪才能加载
      this.flag = true
    })
  },
  methods: {

  }
}
</script>

<style lang="scss" scoped>
.dashboard-editor-container {
  padding: 32px;
  background-color: rgb(240, 242, 245);
  position: relative;

  .chart-wrapper {
    background: #fff;
    padding: 16px 16px 0;
    margin-bottom: 32px;
  }
}

@media (max-width:1024px) {
  .chart-wrapper {
    padding: 8px;
  }
}

.image {
  width: 70%;
  height: 70%;
  float: right;
  margin-left: 0%;
}
</style>
