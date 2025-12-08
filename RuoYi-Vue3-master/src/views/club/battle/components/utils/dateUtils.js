/**
 * 日期处理工具函数
 */

// 格式化日期
export function formatDate(dateStr) {
  if (!dateStr) return ''
  
  let date
  if (typeof dateStr === 'string') {
    if (dateStr.includes('T')) {
      date = new Date(dateStr)
    } else if (dateStr.includes('-') && dateStr.includes(' ')) {
      return dateStr.split(' ')[0]
    } else {
      date = new Date(dateStr)
    }
  } else {
    date = new Date(dateStr)
  }
  
  if (isNaN(date.getTime())) {
    return dateStr
  }
  
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`
}

// 格式化日期时间
export function formatDateTime(dateStr) {
  if (!dateStr) return ''
  
  let date
  if (typeof dateStr === 'string') {
    if (dateStr.includes('T')) {
      date = new Date(dateStr)
    } else if (dateStr.includes('-') && dateStr.includes(' ')) {
      return dateStr
    } else {
      date = new Date(dateStr)
    }
  } else {
    date = new Date(dateStr)
  }
  
  if (isNaN(date.getTime())) {
    return dateStr
  }
  
  const year = date.getFullYear()
  const month = (date.getMonth() + 1).toString().padStart(2, '0')
  const day = date.getDate().toString().padStart(2, '0')
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  const seconds = date.getSeconds().toString().padStart(2, '0')
  
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

// 获取循环指示文本
export function getCycleIndicatorText(timeRange, lastWeekCount, lastMonthCount, fourthSundaysCount) {
  switch (timeRange) {
    case 'lastWeek':
      return `上周${lastWeekCount > 0 ? `(-${lastWeekCount})` : ''}`
    case 'lastMonth':
      return `上月${lastMonthCount > 0 ? `(-${lastMonthCount})` : ''}`
    case 'fourthSundays':
      return `每月第四周日${fourthSundaysCount.value > 0 ? `(-${fourthSundaysCount.value})` : ''}`
    default:
      return ''
  }
}

// 检查是否可以向前循环
export function canGoPrevious(timeRange, lastWeekCount, lastMonthCount, fourthSundaysCount) {
  if (timeRange === 'lastWeek') return lastWeekCount > 0
  if (timeRange === 'lastMonth') return lastMonthCount > 0
  if (timeRange === 'fourthSundays') return fourthSundaysCount > 0
  return false
}