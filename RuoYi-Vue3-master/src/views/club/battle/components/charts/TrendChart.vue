<template>
  <div ref="chartRef" class="chart-container"></div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getTrendChartTitle, getTrendChartColor, getTrendChartYAxisName } from '../utils/chartOptions'
import { formatDate } from '../utils/dateUtils'

const props = defineProps({
  records: {
    type: Array,
    default: () => []
  },
  trendMetric: {
    type: String,
    default: 'kd'
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
watch([() => props.records, () => props.trendMetric], () => {
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
  
  // 准备数据 - 按日期排序
  const sortedRecords = [...props.records].sort((a, b) => {
    const dateA = new Date(a.record_date)
    const dateB = new Date(b.record_date)
    return dateA - dateB
  })
  
  const dates = sortedRecords.map(record => formatDate(record.record_date))
  let data = []
  let series = []
  
  switch (props.trendMetric) {
    case 'kd':
      data = sortedRecords.map(record => record.kdRatio || 0)
      series = [{
        name: 'KD值',
        type: 'line',
        data: data,
        smooth: true,
        lineStyle: {
          width: 3,
          color: getTrendChartColor(props.trendMetric)
        },
        itemStyle: {
          color: getTrendChartColor(props.trendMetric)
        },
        label: {
          show: true,
          position: 'top',
          fontSize: 10,
          color: '#606266',
          formatter: function(params) {
            return params.value.toFixed(2)
          }
        },
        areaStyle: {
          opacity: 0.3,
          color: getTrendChartColor(props.trendMetric)
        }
      }]
      break
    case 'kills':
      data = sortedRecords.map(record => record.kills || 0)
      series = [{
        name: '击杀数',
        type: 'line',
        data: data,
        smooth: true,
        lineStyle: {
          width: 3,
          color: getTrendChartColor(props.trendMetric)
        },
        itemStyle: {
          color: getTrendChartColor(props.trendMetric)
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
          color: getTrendChartColor(props.trendMetric)
        }
      }]
      break
    case 'deaths':
      data = sortedRecords.map(record => record.deaths || 0)
      series = [{
        name: '死亡数',
        type: 'line',
        data: data,
        smooth: true,
        lineStyle: {
          width: 3,
          color: getTrendChartColor(props.trendMetric)
        },
        itemStyle: {
          color: getTrendChartColor(props.trendMetric)
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
          color: getTrendChartColor(props.trendMetric)
        }
      }]
      break
    case 'comprehensive':
      const kdData = sortedRecords.map(record => record.kdRatio || 0)
      const killsData = sortedRecords.map(record => record.kills || 0)
      const deathsData = sortedRecords.map(record => record.deaths || 0)
      series = [
        {
          name: 'KD值',
          type: 'line',
          data: kdData,
          smooth: true,
          lineStyle: {
            width: 2,
            color: '#409EFF'
          },
          itemStyle: {
            color: '#409EFF'
          }
        },
        {
          name: '击杀数',
          type: 'line',
          data: killsData,
          smooth: true,
          lineStyle: {
            width: 2,
            color: '#67C23A'
          },
          itemStyle: {
            color: '#67C23A'
          }
        },
        {
          name: '死亡数',
          type: 'line',
          data: deathsData,
          smooth: true,
          lineStyle: {
            width: 2,
            color: '#F56C6C'
          },
          itemStyle: {
            color: '#F56C6C'
          }
        }
      ]
      break
  }
  
  const option = {
    title: {
      text: getTrendChartTitle(props.trendMetric),
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      formatter: function(params) {
        const dataIndex = params[0].dataIndex
        const record = sortedRecords[dataIndex]
        
        if (props.trendMetric === 'comprehensive') {
          return `
            <div>
              <strong>${params[0].axisValue}</strong><br/>
              KD: ${(record.kdRatio || 0).toFixed(2)}<br/>
              击杀: ${record.kills || 0}<br/>
              死亡: ${record.deaths || 0}
            </div>
          `
        } else {
          return `
            <div>
              <strong>${params[0].axisValue}</strong><br/>
              ${props.trendMetric === 'kd' ? `KD: ${(record.kdRatio || 0).toFixed(2)}` : ''}
              ${props.trendMetric === 'kills' ? `击杀: ${record.kills || 0}` : ''}
              ${props.trendMetric === 'deaths' ? `死亡: ${record.deaths || 0}` : ''}
            </div>
          `
        }
      }
    },
    legend: {
      data: props.trendMetric === 'comprehensive' ? ['KD值', '击杀数', '死亡数'] : [],
      top: '8%'
    },
    grid: {
      left: '10%',
      right: '10%',
      bottom: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: dates,
      axisLabel: {
        rotate: 45,
        interval: 0
      }
    },
    yAxis: {
      type: 'value',
      name: getTrendChartYAxisName(props.trendMetric),
      nameTextStyle: {
        padding: [0, 0, 0, 30]
      }
    },
    series: series.length > 0 ? series : [{
      name: getTrendChartTitle(props.trendMetric),
      type: 'line',
      data: data,
      smooth: true,
      lineStyle: {
        width: 3,
        color: getTrendChartColor(props.trendMetric)
      },
      itemStyle: {
        color: getTrendChartColor(props.trendMetric)
      },
      label: {
        show: true,
        position: 'top',
        fontSize: 10,
        color: '#606266',
        formatter: function(params) {
          if (props.trendMetric === 'kd') {
            return params.value.toFixed(2)
          } else {
            return params.value.toFixed(0)
          }
        }
      },
      areaStyle: {
        opacity: 0.3,
        color: getTrendChartColor(props.trendMetric)
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