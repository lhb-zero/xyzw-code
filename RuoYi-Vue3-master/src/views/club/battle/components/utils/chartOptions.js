/**
 * 图表配置工具函数
 */
import { formatDate } from './dateUtils'

// 获取趋势图标题
export function getTrendChartTitle(trendMetric) {
  switch (trendMetric) {
    case 'kd':
      return 'KD趋势'
    case 'kills':
      return '击杀趋势'
    case 'deaths':
      return '死亡趋势'
    case 'comprehensive':
      return '综合趋势'
    default:
      return '趋势图'
  }
}

// 获取趋势图颜色
export function getTrendChartColor(trendMetric) {
  switch (trendMetric) {
    case 'kd':
      return '#409EFF'
    case 'kills':
      return '#67C23A'
    case 'deaths':
      return '#F56C6C'
    case 'comprehensive':
      return '#909399' // 综合趋势使用灰色
    default:
      return '#409EFF'
  }
}

// 获取趋势图Y轴名称
export function getTrendChartYAxisName(trendMetric) {
  switch (trendMetric) {
    case 'kd':
      return 'KD值'
    case 'kills':
      return '击杀数'
    case 'deaths':
      return '死亡数'
    case 'comprehensive':
      return '数值'
    default:
      return ''
  }
}

// 创建空数据图表选项
export function createEmptyChartOption() {
  return {
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
}

// 创建KD分布区间数据
export function createKDRangesData() {
  return [
    { name: '0-0.5', min: 0, max: 0.5, count: 0 },
    { name: '0.5-1.0', min: 0.5, max: 1.0, count: 0 },
    { name: '1.0-1.5', min: 1.0, max: 1.5, count: 0 },
    { name: '1.5-2.0', min: 1.5, max: 2.0, count: 0 },
    { name: '2.0-2.5', min: 2.0, max: 2.5, count: 0 },
    { name: '2.5+', min: 2.5, max: Infinity, count: 0 }
  ]
}

// 计算KD分布
export function calculateKDDistribution(records) {
  const ranges = createKDRangesData()
  const kdValues = records.map(record => record.kdRatio || 0)
  
  // 统计每个区间的数量
  kdValues.forEach(kd => {
    const range = ranges.find(r => kd >= r.min && kd < r.max)
    if (range) range.count++
  })
  
  return ranges
}

// 创建柱状图数据
export function createBarChartData(records) {
  const totalKills = records.reduce((sum, record) => sum + (record.kills || 0), 0)
  const totalDeaths = records.reduce((sum, record) => sum + (record.deaths || 0), 0)
  const totalDigs = records.reduce((sum, record) => sum + (record.digs || 0), 0)
  const totalRevives = records.reduce((sum, record) => sum + (record.revives || 0), 0)
  
  return [
    { value: totalKills, itemStyle: { color: '#67C23A' } },
    { value: totalDeaths, itemStyle: { color: '#F56C6C' } },
    { value: totalDigs, itemStyle: { color: '#E6A23C' } },
    { value: totalRevives, itemStyle: { color: '#409EFF' } }
  ]
}

// 创建效率图数据
export function createEfficiencyChartData(records) {
  const avgKills = records.reduce((sum, record) => sum + (record.kills || 0), 0) / records.length
  const avgDeaths = records.reduce((sum, record) => sum + (record.deaths || 0), 0) / records.length
  const avgDigs = records.reduce((sum, record) => sum + (record.digs || 0), 0) / records.length
  const avgRevives = records.reduce((sum, record) => sum + (record.revives || 0), 0) / records.length
  
  return [
    { value: avgKills, itemStyle: { color: '#67C23A' } },
    { value: avgDeaths, itemStyle: { color: '#F56C6C' } },
    { value: avgDigs, itemStyle: { color: '#E6A23C' } },
    { value: avgRevives, itemStyle: { color: '#409EFF' } }
  ]
}

// 创建散点图数据
export function createScatterChartData(records) {
  return records.map(record => [
    record.kills || 0,
    record.kdRatio || 0
  ])
}

// 创建战斗力趋势数据
export function createCombatPowerChartData(records) {
  // 按日期排序
  const sortedRecords = [...records].sort((a, b) => {
    const dateA = new Date(a.record_date)
    const dateB = new Date(b.record_date)
    return dateA - dateB
  })
  
  return sortedRecords.map(record => {
    // 计算综合战斗力指数 (0-100)
    // 归一化各项指标
    // KD值：满分25分，2.0以上为满分
    const kdScore = Math.min((record.kdRatio || 0) * 12.5, 25)
    
    // 击杀数：满分25分，15击杀以上为满分
    const killsScore = Math.min((record.kills || 0) * (25/15), 25)
    
    // 复活数：满分25分，5复活以上为满分
    const revivesScore = Math.min((record.revives || 0) * 5, 25)
    
    // 刨地数：满分25分，20刨地以上为满分
    const digsScore = Math.min((record.digs || 0) * (25/20), 25)
    
    return {
      date: formatDate(record.record_date),
      combatPower: kdScore + killsScore + revivesScore + digsScore,
      kdRatio: record.kdRatio || 0,
      details: {
        kdScore: kdScore.toFixed(1),
        killsScore: killsScore.toFixed(1),
        revivesScore: revivesScore.toFixed(1),
        digsScore: digsScore.toFixed(1)
      }
    }
  })
}