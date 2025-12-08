<template>
  <div ref="chartRef" class="chart-container"></div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'
import { createScatterChartData } from '../utils/chartOptions'

const props = defineProps({
  records: {
    type: Array,
    default: () => []
  },
  timeRangeLabel: {
    type: String,
    default: ''
  }
})

const chartRef = ref(null)
let chart = null

// 初始化图表
onMounted(() => {
  if (chartRef.value) {
    chart = echarts.init(chartRef.value)
    updateChart()
    
    // 监听窗口大小变化
    window.addEventListener('resize', handleResize)
  }
})

// 清理资源
onUnmounted(() => {
  if (chart) {
    chart.dispose()
    chart = null
  }
  window.removeEventListener('resize', handleResize)
})

// 监听数据变化
watch(() => props.records, () => {
  nextTick(() => {
    updateChart()
  })
}, { deep: true })

// 更新图表
function updateChart() {
  if (!chart || !props.records || props.records.length === 0) {
    showEmptyChart()
    return
  }
  
  // 先清除图表
  chart.clear()
  
  // 创建散点图数据
  const scatterData = createScatterChartData(props.records)
  
  const option = {
    title: {
      text: `KD与击杀数关系分析 (${props.timeRangeLabel})`,
      left: 'center'
    },
    tooltip: {
      trigger: 'item',
      formatter: function(params) {
        return `
          <div>
            <strong>战斗记录</strong><br/>
            击杀数: ${params.value[0]}<br/>
            KD值: ${params.value[1].toFixed(2)}
          </div>
        `
      }
    },
    grid: {
      left: '10%',
      right: '10%',
      bottom: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'value',
      name: '击杀数',
      nameTextStyle: {
        padding: [0, 0, 0, 30]
      },
      splitLine: {
        lineStyle: {
          type: 'dashed'
        }
      }
    },
    yAxis: {
      type: 'value',
      name: 'KD值',
      nameTextStyle: {
        padding: [0, 0, 0, 40]
      },
      splitLine: {
        lineStyle: {
          type: 'dashed'
        }
      }
    },
    series: [{
      name: '战绩分布',
      type: 'scatter',
      data: scatterData,
      symbolSize: 10,
      itemStyle: {
        color: function(params) {
          // 根据KD值设置颜色：红色表示KD<1，绿色表示KD≥1
          return params.value[1] >= 1 ? '#67C23A' : '#F56C6C'
        },
        borderColor: '#fff',
        borderWidth: 1
      },
      label: {
        show: true,
        position: 'top',
        fontSize: 10,
        color: '#606266',
        formatter: function(params) {
          return `(${params.value[0]}, ${params.value[1].toFixed(1)})`
        }
      },
      emphasis: {
        itemStyle: {
          borderColor: '#333',
          borderWidth: 2
        }
      }
    }]
  }
  
  chart.setOption(option, true)
}

// 显示空数据图表
function showEmptyChart() {
  if (!chart) return
  
  const emptyOption = {
    title: {
      text: '暂无数据',
      left: 'center',
      textStyle: {
        color: '#909399',
        fontSize: 16
      }
    },
    xAxis: { show: false },
    yAxis: { show: false },
    series: []
  }
  
  chart.setOption(emptyOption, true)
}

// 处理窗口大小变化
function handleResize() {
  if (chart) {
    chart.resize()
  }
}
</script>

<style scoped>
.chart-container {
  height: 300px;
  width: 100%;
}
</style>