/**
 * 数据处理工具函数
 */

// 根据时间范围筛选记录
export function filterRecordsByTimeRange(records, timeRange, customDateRange, lastWeekCount, lastMonthCount, fourthSundaysCount) {
  if (!records || records.length === 0) return []
  
  const now = new Date()
  
  switch (timeRange) {
    case 'all':
      return records
    case 'thisWeek':
      return getThisWeekRecords(records, now)
    case 'lastWeek':
      return getLastWeekRecords(records, now, lastWeekCount)
    case 'thisMonth':
      return getThisMonthRecords(records, now)
    case 'lastMonth':
      return getLastMonthRecords(records, now, lastMonthCount)
    case 'fourthSundays':
      return getFourthSundaysRecords(records, now, fourthSundaysCount)
    case 'custom':
      if (!customDateRange || customDateRange.length !== 2) return records
      const [startDate, endDate] = customDateRange
      return records.filter(record => {
        const recordDate = new Date(record.record_date || record.recordDate)
        return recordDate >= startDate && recordDate <= endDate
      })
    default:
      return records
  }
}

// 获取本周的战绩记录
export function getThisWeekRecords(records, now) {
  const day = now.getDay()
  const diff = now.getDate() - day + (day === 0 ? -6 : 1) // 调整到周一开始
  const monday = new Date(now.getFullYear(), now.getMonth(), diff)
  monday.setHours(0, 0, 0, 0)
  
  return records.filter(record => {
    const recordDate = new Date(record.record_date)
    return recordDate >= monday && !isNaN(recordDate.getTime())
  })
}

// 获取上周的战绩记录
export function getLastWeekRecords(records, now, count = 0) {
  const day = now.getDay()
  const diff = now.getDate() - day + (day === 0 ? -6 : 1) // 调整到周一开始
  const thisMonday = new Date(now.getFullYear(), now.getMonth(), diff)
  thisMonday.setHours(0, 0, 0, 0)
  
  const lastMonday = new Date(thisMonday)
  lastMonday.setDate(thisMonday.getDate() - 7 - (count * 7))
  
  const lastSunday = new Date(lastMonday)
  lastSunday.setDate(lastMonday.getDate() + 6)
  lastSunday.setHours(23, 59, 59, 999)
  
  return records.filter(record => {
    const recordDate = new Date(record.record_date)
    return recordDate >= lastMonday && recordDate <= lastSunday && !isNaN(recordDate.getTime())
  })
}

// 获取本月的战绩记录
export function getThisMonthRecords(records, now) {
  const firstDayOfMonth = new Date(now.getFullYear(), now.getMonth(), 1)
  firstDayOfMonth.setHours(0, 0, 0, 0)
  
  return records.filter(record => {
    const recordDate = new Date(record.record_date)
    return recordDate >= firstDayOfMonth && !isNaN(recordDate.getTime())
  })
}

// 获取上月的战绩记录
export function getLastMonthRecords(records, now, count = 0) {
  const firstDayOfThisMonth = new Date(now.getFullYear(), now.getMonth(), 1)
  const firstDayOfLastMonth = new Date(firstDayOfThisMonth)
  firstDayOfLastMonth.setMonth(firstDayOfThisMonth.getMonth() - 1 - count)
  
  const firstDayOfNextMonth = new Date(firstDayOfThisMonth)
  firstDayOfNextMonth.setMonth(firstDayOfThisMonth.getMonth() - count)
  
  return records.filter(record => {
    const recordDate = new Date(record.record_date)
    return recordDate >= firstDayOfLastMonth && recordDate < firstDayOfNextMonth && !isNaN(recordDate.getTime())
  })
}

// 获取每月第四周日的战绩记录
export function getFourthSundaysRecords(records, now, count = 0) {
  return records.filter(record => {
    const recordDate = new Date(record.record_date)
    return isFourthSundayWithOffset(recordDate, now, count) && !isNaN(recordDate.getTime())
  })
}

// 检查是否为每月第四个周日（支持偏移）
export function isFourthSundayWithOffset(date, now, offset = 0) {
  const year = date.getFullYear()
  const month = date.getMonth()
  
  // 计算目标月份
  const targetYear = now.getFullYear()
  const targetMonth = now.getMonth() - offset
  
  // 检查日期是否在目标月份
  if (year !== targetYear || month !== targetMonth) return false
  
  // 获取当月第一个周日
  const firstDay = new Date(targetYear, targetMonth, 1)
  const firstDayOfWeek = firstDay.getDay()
  const firstSunday = new Date(targetYear, targetMonth, 1 + ((7 - firstDayOfWeek) % 7))
  
  // 计算第四个周日
  const fourthSunday = new Date(firstSunday)
  fourthSunday.setDate(firstSunday.getDate() + 21)
  
  // 检查是否是同一天
  return date.getDate() === fourthSunday.getDate() && 
         date.getMonth() === fourthSunday.getMonth() && 
         date.getFullYear() === fourthSunday.getFullYear()
}

// 获取时间范围标签
export function getTimeRangeLabel(timeRange, lastWeekCount, lastMonthCount, fourthSundaysCount, customDateRange) {
  switch (timeRange) {
    case 'all':
      return '全部时间'
    case 'thisWeek':
      return '本周'
    case 'lastWeek':
      return `上周${lastWeekCount > 0 ? `(-${lastWeekCount})` : ''}`
    case 'thisMonth':
      return '本月'
    case 'lastMonth':
      return `上月${lastMonthCount > 0 ? `(-${lastMonthCount})` : ''}`
    case 'fourthSundays':
      return `每月第四周日${fourthSundaysCount > 0 ? `(-${fourthSundaysCount})` : ''}`
    case 'custom':
      if (customDateRange && customDateRange.length === 2) {
        return `${formatDate(customDateRange[0])} 至 ${formatDate(customDateRange[1])}`
      }
      return '自定义时间'
    default:
      return '未知时间范围'
  }
}

// 获取平均值对比范围标签
export function getAvgComparisonRangeLabel(avgComparisonRange) {
  switch (avgComparisonRange) {
    case 'thisWeek':
      return '本周'
    case 'lastWeek':
      return '上周'
    case 'thisMonth':
      return '本月'
    case 'lastMonth':
      return '上月'
    case 'thisQuarter':
      return '本季度'
    case 'thisYear':
      return '今年'
    case 'all':
      return '全部时间'
    default:
      return '未知时间范围'
  }
}

// 获取空数据提示消息
export function getEmptyDataMessage(timeRange, lastWeekCount, lastMonthCount, fourthSundaysCount) {
  switch (timeRange) {
    case 'thisWeek':
      return '本周暂无战绩数据'
    case 'lastWeek':
      return `上周${lastWeekCount > 0 ? `(-${lastWeekCount})` : ''}暂无战绩数据`
    case 'thisMonth':
      return '本月暂无战绩数据'
    case 'lastMonth':
      return `上月${lastMonthCount > 0 ? `(-${lastMonthCount})` : ''}暂无战绩数据`
    case 'fourthSundays':
      return `每月第四周日${fourthSundaysCount > 0 ? `(-${fourthSundaysCount})` : ''}暂无战绩数据`
    case 'custom':
      return '选定时间范围内暂无战绩数据'
    case 'all':
      return '该成员暂无任何战绩数据'
    default:
      return '暂无战绩数据'
  }
}

// 从现有数据计算全员平均值
export function calculateServerAveragesFromData(allRecords, avgComparisonRange) {
  if (!allRecords || allRecords.length === 0) {
    return {
      kd: 0,
      kills: 0,
      deaths: 0,
      digs: 0,
      revives: 0
    }
  }
  
  // 根据时间范围过滤数据
  let filteredServerRecords = [...allRecords]
  const now = new Date()
  
  switch (avgComparisonRange) {
    case 'thisWeek':
      // 获取本周第一天（周一）
      const dayOfWeek = now.getDay()
      const daysToMonday = (dayOfWeek === 0) ? 6 : dayOfWeek - 1
      const firstDayOfWeek = new Date(now.getFullYear(), now.getMonth(), now.getDate() - daysToMonday)
      firstDayOfWeek.setHours(0, 0, 0, 0)
      filteredServerRecords = filteredServerRecords.filter(record => {
        const recordDate = new Date(record.record_date || record.recordDate)
        return recordDate >= firstDayOfWeek && !isNaN(recordDate.getTime())
      })
      break
      
    case 'lastWeek':
      // 获取上周的第一天和最后一天
      const lastWeekStart = new Date(now.getFullYear(), now.getMonth(), now.getDate() - daysToMonday - 7)
      lastWeekStart.setHours(0, 0, 0, 0)
      const lastWeekEnd = new Date(lastWeekStart)
      lastWeekEnd.setDate(lastWeekStart.getDate() + 6)
      lastWeekEnd.setHours(23, 59, 59, 999)
      filteredServerRecords = filteredServerRecords.filter(record => {
        const recordDate = new Date(record.record_date || record.recordDate)
        return recordDate >= lastWeekStart && recordDate <= lastWeekEnd && !isNaN(recordDate.getTime())
      })
      break
      
    case 'thisMonth':
      const firstDayOfMonth = new Date(now.getFullYear(), now.getMonth(), 1)
      firstDayOfMonth.setHours(0, 0, 0, 0)
      filteredServerRecords = filteredServerRecords.filter(record => {
        const recordDate = new Date(record.record_date || record.recordDate)
        return recordDate >= firstDayOfMonth && !isNaN(recordDate.getTime())
      })
      break
      
    case 'lastMonth':
      // 获取上月的第一天和最后一天
      const firstDayOfLastMonth = new Date(now.getFullYear(), now.getMonth() - 1, 1)
      firstDayOfLastMonth.setHours(0, 0, 0, 0)
      const lastDayOfLastMonth = new Date(now.getFullYear(), now.getMonth(), 1)
      lastDayOfLastMonth.setDate(lastDayOfLastMonth.getDate() - 1)
      lastDayOfLastMonth.setHours(23, 59, 59, 999)
      filteredServerRecords = filteredServerRecords.filter(record => {
        const recordDate = new Date(record.record_date || record.recordDate)
        return recordDate >= firstDayOfLastMonth && recordDate <= lastDayOfLastMonth && !isNaN(recordDate.getTime())
      })
      break
      
    case 'thisQuarter':
      const currentMonth = now.getMonth()
      const quarterStartMonth = Math.floor(currentMonth / 3) * 3
      const firstDayOfQuarter = new Date(now.getFullYear(), quarterStartMonth, 1)
      firstDayOfQuarter.setHours(0, 0, 0, 0)
      filteredServerRecords = filteredServerRecords.filter(record => {
        const recordDate = new Date(record.record_date || record.recordDate)
        return recordDate >= firstDayOfQuarter && !isNaN(recordDate.getTime())
      })
      break
      
    case 'thisYear':
      const firstDayOfYear = new Date(now.getFullYear(), 0, 1)
      firstDayOfYear.setHours(0, 0, 0, 0)
      filteredServerRecords = filteredServerRecords.filter(record => {
        const recordDate = new Date(record.record_date || record.recordDate)
        return recordDate >= firstDayOfYear && !isNaN(recordDate.getTime())
      })
      break
      
    // 'all' 不需要过滤
  }
  
  // 计算平均值
  const totalRecords = filteredServerRecords.length
  if (totalRecords === 0) {
    return {
      kd: 0,
      kills: 0,
      deaths: 0,
      digs: 0,
      revives: 0
    }
  }
  
  const totalKD = filteredServerRecords.reduce((sum, record) => sum + (record.kdRatio || 0), 0)
  const totalKills = filteredServerRecords.reduce((sum, record) => sum + (record.kills || 0), 0)
  const totalDeaths = filteredServerRecords.reduce((sum, record) => sum + (record.deaths || 0), 0)
  const totalDigs = filteredServerRecords.reduce((sum, record) => sum + (record.digs || 0), 0)
  const totalRevives = filteredServerRecords.reduce((sum, record) => sum + (record.revives || 0), 0)
  
  return {
    kd: (totalKD / totalRecords).toFixed(2),
    kills: (totalKills / totalRecords).toFixed(1),
    deaths: (totalDeaths / totalRecords).toFixed(1),
    digs: (totalDigs / totalRecords).toFixed(1),
    revives: (totalRevives / totalRecords).toFixed(1)
  }
}

// 计算玩家统计数据
export function calculatePlayerStats(records) {
  if (!records || records.length === 0) {
    return {
      totalBattles: 0,
      avgKD: 0,
      maxKD: 0,
      avgKills: 0,
      avgDeaths: 0,
      avgDigs: 0,
      avgRevives: 0
    }
  }
  
  const totalBattles = records.length
  const totalKD = records.reduce((sum, record) => sum + (record.kdRatio || 0), 0)
  const totalKills = records.reduce((sum, record) => sum + (record.kills || 0), 0)
  const totalDeaths = records.reduce((sum, record) => sum + (record.deaths || 0), 0)
  const totalDigs = records.reduce((sum, record) => sum + (record.digs || 0), 0)
  const totalRevives = records.reduce((sum, record) => sum + (record.revives || 0), 0)
  const maxKD = Math.max(...records.map(record => record.kdRatio || 0))
  
  return {
    totalBattles,
    avgKD: totalKD / totalBattles,
    maxKD,
    avgKills: totalKills / totalBattles,
    avgDeaths: totalDeaths / totalBattles,
    avgDigs: totalDigs / totalBattles,
    avgRevives: totalRevives / totalBattles
  }
}

// 计算战斗力指数
export function calculateCombatPower(record) {
  // 归一化各项指标
  // KD值：满分25分，2.0以上为满分
  const kdScore = Math.min((record.kdRatio || 0) * 12.5, 25)
  
  // 击杀数：满分25分，15击杀以上为满分
  const killsScore = Math.min((record.kills || 0) * (25/15), 25)
  
  // 复活数：满分25分，5复活以上为满分
  const revivesScore = Math.min((record.revives || 0) * 5, 25)
  
  // 刨地数：满分25分，20刨地以上为满分
  const digsScore = Math.min((record.digs || 0) * (25/20), 25)
  
  return kdScore + killsScore + revivesScore + digsScore
}