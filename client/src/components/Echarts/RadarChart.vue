<template>
  <div :class="className" :style="{height:height,width:width}" />
</template>

<script>
import * as echarts from 'echarts'
require('echarts/theme/macarons') // echarts theme
import { debounce } from '@/utils'

// let chartData = null

export default {
  props: {
    className: {
      type: String,
      default: 'chart'
    },
    width: {
      type: String,
      default: '100%'
    },
    height: {
      type: String,
      default: '350px'
    },
    chartData: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      chart: null,
      max: 0.00
    }
  },
  mounted() {
    this.getMax(this.chartData)
    this.initChart()
    this.__resizeHandler = debounce(() => {
      if (this.chart) {
        this.chart.resize()
      }
    }, 100)
    window.addEventListener('resize', this.__resizeHandler)
  },
  beforeDestroy() {
    if (!this.chart) {
      return
    }
    window.removeEventListener('resize', this.__resizeHandler)
    this.chart.dispose()
    this.chart = null
  },
  methods: {
    getMax(chartData) {
      console.log('chartData in RadarChart', chartData)
      let max = 0.00
      const dataA = chartData.dataA
      const dataB = chartData.dataB
      dataA.forEach((itemA) => {
        if (itemA > max) {
          max = itemA
        }
      })
      dataB.forEach((itemB) => {
        if (itemB > max) {
          max = itemB
        }
      })
      max = Math.ceil(max * 100) / 100
      this.max = max
      console.log('max', max)
    },
    initChart() {
      this.chart = echarts.init(this.$el, 'macarons')
      this.chart.setOption({
        color: ['#67F9D8', '#FFE434', '#56A3F1', '#FF917C'],
        title: {
          text: this.chartData.title,
          x: 'center',
          y: 'bottom'
          // padding: [
          //   // 330, // 上
          //   // 135 // 左
          // ]
        },
        legend: {
          show: this.chartData.showLegend,
          top: 0, // 上
          left: 0, // 左
          textStyle: {
            fontSize: 14
          }
        },
        radar: [
          {
            indicator: [
              { text: 'VC', max: this.max },
              { text: 'TM', max: this.max },
              { text: 'FG', max: this.max },
              { text: 'RN', max: this.max },
              { text: 'SI', max: this.max }
            ],
            center: ['50%', '50%'],
            radius: 110,
            startAngle: 90,
            splitNumber: 5,
            shape: 'circle',
            axisName: {
              formatter: '{value}',
              color: '#428BD4'
            },
            splitArea: {
              areaStyle: {
                color: ['#77EADF', '#26C3BE', '#64AFE9', '#428BD4'],
                shadowColor: 'rgba(0, 0, 0, 0.2)',
                shadowBlur: 10
              }
            },
            axisLine: {
              lineStyle: {
                color: 'rgba(211, 253, 250, 0.8)'
              }
            },
            splitLine: {
              lineStyle: {
                color: 'rgba(211, 253, 250, 0.8)'
              }
            }
          }
        ],
        series: [
          {
            type: 'radar',
            emphasis: {
              lineStyle: {
                width: 4
              }
            },
            data: [
              {
                value: this.chartData.dataA,
                name: 'Original MSE'

              },
              {
                value: this.chartData.dataB,
                name: 'Transformed MSE',
                areaStyle: {
                  color: 'rgba(255, 228, 52, 0.6)'
                }
              }
            ]
          }
        ]
      })
    }
  }
}
</script>
