<template>
  <div :class="className" :style="{height:height,width:width}" />
</template>

<script>
import * as echarts from 'echarts'
require('echarts/theme/macarons') // echarts theme
import { debounce } from '@/utils'

// const animationDuration = 6000

export default {
  props: {
    data: {
      type: Object
    },
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
      default: '500px'
    }
  },
  data() {
    return {
      chart: null,
      chartData: {
        series: { data: [] },
        xAxisData: []
      }
      // xAxisData: [
      //   {
      //     value: 'Dave_v1',
      //     textStyle: {
      //       fontSize: 15
      //     }
      //   },
      //   {
      //     value: 'Dave_v2',
      //     textStyle: {
      //       fontSize: 15
      //     }
      //   },
      //   {
      //     value: 'Dave_v3',
      //     textStyle: {
      //       fontSize: 15
      //     }
      //   },
      //   {
      //     value: 'Epoch',
      //     textStyle: {
      //       fontSize: 15
      //     }
      //   }]
    }
  },
  watch: {
    data: {
      handler(newVal) {
        // 将props中的数据复制到本地状态中
        this.chartData = Object.assign({}, newVal)
        this.$nextTick(() => {
          this.initChart()
        })
      },
      deep: true,
      immediate: true
    }
  },
  mounted() {
    // console.log('BarChart mounted')
    this.__resizeHandler = debounce(() => {
      if (this.chart) {
        this.chart.resize()
      }
    }, 100)
    window.addEventListener('resize', this.__resizeHandler)
    // console.log('mounted data: ', this.$props.data)
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
    initChart() {
      this.chart = echarts.init(this.$el, 'macarons')
      const labelOption = {
        show: true,
        fontSize: 16
      }
      this.chart.setOption({
        // legend: {
        //   data: ['FuzzScene', 'FA-FAIL'],
        //   textStyle: {
        //     fontSize: 15
        //   }
        // },
        title: {
          text: '模型检出缺陷多样性图', // 设置标题文本
          left: 'center', // 设置标题和副标题的水平对齐方式为居中
          // top: 'bottom' // 设置标题距离顶部的距离,默认是20
          padding: [470, 0, 0, 0] // 设置标题内边距,上，右，下，左
        },
        xAxis: [
          {
            name: '模型',
            type: 'category',
            axisTick: { show: true },
            data: this.chartData.xAxisData,
            nameTextStyle: {
              fontSize: 15
            }
          }
        ],
        yAxis: [
          {
            name: '缺陷多样性',
            type: 'value',
            nameTextStyle: {
              fontSize: 15
            },
            axisLine: {
              show: true
            },
            axisLabel: {
              fontSize: 15
            }
          }
        ],
        grid: {
          left: '3%',
          right: '8%',
          bottom: '10%',
          containLabel: true
        },
        series: [
          {
            name: 'FuzzScene',
            label: labelOption,
            type: 'bar',
            barWidth: 50,
            data: this.chartData.series.data
          }
          // {
          //   name: 'FA-FAIL',
          //   type: 'bar',
          //   label: labelOption,
          //   data: [220, 182, 191, 234, 290]
          // }
        ]
      })
    }
  }
}
</script>
