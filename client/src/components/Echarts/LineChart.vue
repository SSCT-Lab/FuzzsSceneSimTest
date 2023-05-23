<template>
  <div :class="className" :style="{height:height,width:width}" />
</template>

<script>
import * as echarts from 'echarts'
import { debounce } from '@/utils'
require('echarts/theme/macarons') // echarts theme
// import resize from './mixins/resize'

export default {
  // mixins: [resize],
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
      default: '500px'
    },
    // autoResize: {
    //   type: Boolean,
    //   default: true
    // },
    data: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      chart: null,
      chartData: {
        legend: { data: [] },
        series: [],
        xAxis: { data: [] }
      }
    }
  },
  watch: {
    data: {
      handler(val) {
        this.chartData = Object.assign({}, val)
        this.$nextTick(() => {
          this.initChart()
        })
      },
      deep: true,
      immediate: true
    }
  },
  mounted() {
    // this.initChart()
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
    initChart() {
      this.chart = echarts.init(this.$el, 'macarons')
      this.setOptions(this.chartData)
    },
    setOptions({ expectedData, actualData } = {}) {
      this.chart.setOption({
        title: {
          text: '模型检出缺陷数量图',
          left: 'center',
          padding: [470, 0, 0, 0]
        },
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: this.chartData.legend.data
        },
        grid: {
          left: '3%',
          right: '8%',
          bottom: '10%',
          containLabel: true
        },
        // toolbox: {
        //   feature: {
        //     saveAsImage: {}
        //   }
        // },
        xAxis: {
          name: '迭代\n轮次',
          type: 'category',
          axisTick: { show: true },
          boundaryGap: false,
          data: this.chartData.xAxis.data,
          nameTextStyle: {
            fontSize: 15
          }
        },
        yAxis: {
          name: '缺陷数量',
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
        },
        series: this.chartData.series
      })
    }
  }
}
</script>
