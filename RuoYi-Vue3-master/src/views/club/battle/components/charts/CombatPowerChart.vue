<template>
  <div ref="chartRef" class="chart-container"></div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'
import { createCombatPowerChartData } from '../utils/chartOptions'

const props = defineProps({
  records: {
    type: Array,
    default: () => []
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
  
  // 按日期排序
  const sortedRecords = [...props.records].sort((a, b) => {
    const dateA = new Date(a.record_date)
    const dateB = new Date(b.record_date)
    return dateA - dateB
  })
  
  // 计算战斗力数据
  const combatPowerData = createCombatPowerChartData(props.records)
  
  const option = {
    title: {
      text: '综合战斗力指数趋势',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      formatter: function(params) {
        const dataIndex = params[0].dataIndex
        const data = combatPowerData[dataIndex]
        return `
          <div>
            <strong>${params[0].axisValue}</strong><br/>
            战斗力指数: ${data.combatPower.toFixed(1)}<br/>
            KD值: ${data.kdRatio.toFixed(2)} (${data.details.kdScore}分)<br/>
            击杀数: ${sortedRecords[dataIndex].kills || 0} (${data.details.killsScore}分)<br/>
            复活数: ${sortedRecords[dataIndex].revives || 0} (${data.details.revivesScore}分)<br/>
            刨地数: ${sortedRecords[dataIndex].digs || 0} (${data.details.digsScore}分)
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
      type: 'category',
      data: combatPowerData.map(item => item.date),
      axisLabel: {
        rotate: 45,
        interval: 0
      }
    },
    yAxis: {
      type: 'value',
      name: '战斗力指数',
      nameTextStyle: {
        padding: [0, 0, 0, 30]
      },
      max: 100
    },
    series: [{
      name: '战斗力指数',
      type: 'line',
      data: combatPowerData.map(item => item.combatPower),
      smooth: true,
      lineStyle: {
        width: 3,
        color: '#FF9F7A'
      },
      itemStyle: {
        color: '#FF9F7A'
      },
      label: {
        show: true,
        position: 'top',
        fontSize: 10,
        color: '#606266',
        formatter: function(params) {
          return params.value.toFixed(0)
        }
      },
      areaStyle: {
        opacity: 0.3,
        color: '#FF9F7A'
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