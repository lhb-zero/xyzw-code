<template>
  <div ref="chartRef" class="chart-container"></div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'
import { createBarChartData } from '../utils/chartOptions'

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
  
  // 创建图表数据
  const data = createBarChartData(props.records)
  
  const option = {
    title: {
      text: `战绩总览 (${props.timeRangeLabel})`,
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '10%',
      right: '10%',
      bottom: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['击杀', '死亡', '刨地', '复活'],
      axisLabel: {
        interval: 0,
        rotate: 0
      }
    },
    yAxis: {
      type: 'value',
      name: '数量',
      nameTextStyle: {
        padding: [0, 0, 0, 30]
      }
    },
    series: [{
      name: '数量',
      type: 'bar',
      data: data,
      barWidth: '50%',
      label: {
        show: true,
        position: 'top',
        fontSize: 12,
        color: '#606266'
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