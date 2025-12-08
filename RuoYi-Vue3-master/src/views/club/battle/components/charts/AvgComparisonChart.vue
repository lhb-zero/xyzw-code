<template>
  <div ref="chartRef" class="chart-container"></div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getAvgComparisonRangeLabel } from '../utils/dataProcessing'

const props = defineProps({
  records: {
    type: Array,
    default: () => []
  },
  serverAvgData: {
    type: Object,
    default: () => ({
      kd: 0,
      kills: 0,
      deaths: 0,
      digs: 0,
      revives: 0
    })
  },
  avgComparisonRange: {
    type: String,
    default: 'thisMonth'
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
watch([() => props.records, () => props.serverAvgData, () => props.avgComparisonRange], () => {
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
  
  // 计算当前玩家的平均值
  const playerAvgKD = props.records.reduce((sum, record) => sum + (record.kdRatio || 0), 0) / props.records.length
  const playerAvgKills = props.records.reduce((sum, record) => sum + (record.kills || 0), 0) / props.records.length
  const playerAvgDeaths = props.records.reduce((sum, record) => sum + (record.deaths || 0), 0) / props.records.length
  const playerAvgDigs = props.records.reduce((sum, record) => sum + (record.digs || 0), 0) / props.records.length
  const playerAvgRevives = props.records.reduce((sum, record) => sum + (record.revives || 0), 0) / props.records.length
  
  // 使用从API获取的全员平均值
  const serverAvgKD = parseFloat(props.serverAvgData.kd)
  const serverAvgKills = parseFloat(props.serverAvgData.kills)
  const serverAvgDeaths = parseFloat(props.serverAvgData.deaths)
  const serverAvgDigs = parseFloat(props.serverAvgData.digs)
  const serverAvgRevives = parseFloat(props.serverAvgData.revives)
  
  const option = {
    title: {
      text: `个人数据与全员平均值对比 (${getAvgComparisonRangeLabel(props.avgComparisonRange)})`,
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      formatter: function(params) {
        const dataIndex = params[0].dataIndex
        const categories = ['KD值', '击杀数', '死亡数', '刨地数', '复活数']
        const playerValues = [playerAvgKD, playerAvgKills, playerAvgDeaths, playerAvgDigs, playerAvgRevives]
        const serverValues = [serverAvgKD, serverAvgKills, serverAvgDeaths, serverAvgDigs, serverAvgRevives]
        
        let diffPercent = 0
        if (dataIndex === 0) { // KD值
          diffPercent = ((playerAvgKD - serverAvgKD) / serverAvgKD * 100).toFixed(1)
        } else {
          const playerVal = playerValues[dataIndex]
          const serverVal = serverValues[dataIndex]
          diffPercent = ((playerVal - serverVal) / serverVal * 100).toFixed(1)
        }
        
        return `
          <div>
            <strong>${categories[dataIndex]}</strong><br/>
            个人数据: ${params[0].value.toFixed(2)}<br/>
            全员平均: ${params[1].value.toFixed(2)}<br/>
            差异: ${diffPercent > 0 ? '+' : ''}${diffPercent}%
          </div>
        `
      }
    },
    legend: {
      data: ['个人数据', '全员平均'],
      top: '10%'
    },
    grid: {
      left: '10%',
      right: '10%',
      bottom: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['KD值', '击杀数', '死亡数', '刨地数', '复活数'],
      axisLabel: {
        interval: 0,
        rotate: 30
      }
    },
    yAxis: {
      type: 'value',
      name: '平均值',
      nameTextStyle: {
        padding: [0, 0, 0, 30]
      }
    },
    series: [
      {
        name: '个人数据',
        type: 'bar',
        data: [
          playerAvgKD,
          playerAvgKills,
          playerAvgDeaths,
          playerAvgDigs,
          playerAvgRevives
        ],
        itemStyle: {
          color: '#409EFF'
        },
        label: {
          show: true,
          position: 'top',
          fontSize: 12,
          color: '#606266',
          formatter: function(params) {
            return params.value.toFixed(2)
          }
        }
      },
      {
        name: '全员平均',
        type: 'bar',
        data: [
          serverAvgKD,
          serverAvgKills,
          serverAvgDeaths,
          serverAvgDigs,
          serverAvgRevives
        ],
        itemStyle: {
          color: '#E6A23C'
        },
        label: {
          show: true,
          position: 'top',
          fontSize: 12,
          color: '#606266',
          formatter: function(params) {
            return params.value.toFixed(2)
          }
        }
      }
    ]
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