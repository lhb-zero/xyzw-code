/**
 * 日期快捷筛选工具函数
 * 提供各种常用的日期筛选选项计算
 */

/**
 * 获取本周六的日期
 * @returns {Date} 本周六的日期
 */
export function getThisSaturday() {
  const today = new Date();
  const dayOfWeek = today.getDay(); // 0 = Sunday, 1 = Monday, ..., 6 = Saturday
  const daysUntilSaturday = 6 - dayOfWeek;
  const saturday = new Date(today);
  saturday.setDate(today.getDate() + daysUntilSaturday);
  return saturday;
}

/**
 * 获取上周六的日期
 * @returns {Date} 上周六的日期
 */
export function getLastSaturday() {
  const thisSaturday = getThisSaturday();
  const lastSaturday = new Date(thisSaturday);
  lastSaturday.setDate(thisSaturday.getDate() - 7);
  return lastSaturday;
}

/**
 * 获取本月第四个周日的日期
 * @returns {Date} 本月第四个周日的日期
 */
export function getFourthSundayOfMonth() {
  const today = new Date();
  const year = today.getFullYear();
  const month = today.getMonth();
  
  // 获取本月第一天
  const firstDay = new Date(year, month, 1);
  // 获取本月第一个周日
  const firstSunday = new Date(firstDay);
  firstSunday.setDate(firstDay.getDate() + (7 - firstDay.getDay()) % 7);
  
  // 获取第四个周日
  const fourthSunday = new Date(firstSunday);
  fourthSunday.setDate(firstSunday.getDate() + 21); // 3周后
  
  // 如果第四个周日超出本月，则返回最后一个周日
  if (fourthSunday.getMonth() !== month) {
    const lastDay = new Date(year, month + 1, 0); // 本月最后一天
    const lastSunday = new Date(lastDay);
    lastSunday.setDate(lastDay.getDate() - lastDay.getDay());
    return lastSunday;
  }
  
  return fourthSunday;
}

/**
 * 获取上月第四个周日的日期
 * @returns {Date} 上月第四个周日的日期
 */
export function getFourthSundayOfLastMonth() {
  const today = new Date();
  const year = today.getFullYear();
  const month = today.getMonth();
  
  // 获取上月
  let lastMonth = month - 1;
  let lastMonthYear = year;
  if (lastMonth < 0) {
    lastMonth = 11;
    lastMonthYear -= 1;
  }
  
  // 获取上月第一天
  const firstDay = new Date(lastMonthYear, lastMonth, 1);
  // 获取上月第一个周日
  const firstSunday = new Date(firstDay);
  firstSunday.setDate(firstDay.getDate() + (7 - firstDay.getDay()) % 7);
  
  // 获取第四个周日
  const fourthSunday = new Date(firstSunday);
  fourthSunday.setDate(firstSunday.getDate() + 21); // 3周后
  
  // 如果第四个周日超出上月，则返回上月最后一个周日
  if (fourthSunday.getMonth() !== lastMonth) {
    const lastDay = new Date(lastMonthYear, lastMonth + 1, 0); // 上月最后一天
    const lastSunday = new Date(lastDay);
    lastSunday.setDate(lastDay.getDate() - lastDay.getDay());
    return lastSunday;
  }
  
  return fourthSunday;
}

/**
 * 获取本月所有周六的日期数组
 * @returns {Array<Date>} 本月所有周六的日期数组
 */
export function getAllSaturdaysOfMonth() {
  const today = new Date();
  const year = today.getFullYear();
  const month = today.getMonth();
  
  // 获取本月第一天
  const firstDay = new Date(year, month, 1);
  // 获取本月最后一个日期
  const lastDay = new Date(year, month + 1, 0);
  
  const saturdays = [];
  // 从本月第一天开始查找周六
  const currentDate = new Date(firstDay);
  
  // 找到本月第一个周六
  while (currentDate.getDay() !== 6 && currentDate <= lastDay) {
    currentDate.setDate(currentDate.getDate() + 1);
  }
  
  // 收集所有周六
  while (currentDate <= lastDay) {
    saturdays.push(new Date(currentDate));
    currentDate.setDate(currentDate.getDate() + 7);
  }
  
  return saturdays;
}

/**
 * 获取上月所有周六的日期数组
 * @returns {Array<Date>} 上月所有周六的日期数组
 */
export function getAllSaturdaysOfLastMonth() {
  const today = new Date();
  const year = today.getFullYear();
  let month = today.getMonth() - 1;
  let targetYear = year;
  
  // 处理跨年情况
  if (month < 0) {
    month = 11;
    targetYear -= 1;
  }
  
  // 获取上月第一天
  const firstDay = new Date(targetYear, month, 1);
  // 获取上月最后一个日期
  const lastDay = new Date(targetYear, month + 1, 0);
  
  const saturdays = [];
  // 从上月第一天开始查找周六
  const currentDate = new Date(firstDay);
  
  // 找到上月第一个周六
  while (currentDate.getDay() !== 6 && currentDate <= lastDay) {
    currentDate.setDate(currentDate.getDate() + 1);
  }
  
  // 收集所有周六
  while (currentDate <= lastDay) {
    saturdays.push(new Date(currentDate));
    currentDate.setDate(currentDate.getDate() + 7);
  }
  
  return saturdays;
}

/**
 * 获取本月所有战绩的日期范围（从月初到今天或月末）
 * @returns {Object} 包含startDate和endDate的对象
 */
export function getThisMonthBattleRange() {
  const today = new Date();
  const year = today.getFullYear();
  const month = today.getMonth();
  
  // 本月第一天
  const startDate = new Date(year, month, 1);
  // 今天或者月末（取较早的）
  const endDate = new Date(Math.min(today, new Date(year, month + 1, 0)));
  
  return { startDate, endDate };
}

/**
 * 获取上月所有战绩的日期范围
 * @returns {Object} 包含startDate和endDate的对象
 */
export function getLastMonthBattleRange() {
  const today = new Date();
  const year = today.getFullYear();
  let month = today.getMonth() - 1;
  let targetYear = year;
  
  // 处理跨年情况
  if (month < 0) {
    month = 11;
    targetYear -= 1;
  }
  
  // 上月第一天
  const startDate = new Date(targetYear, month, 1);
  // 上月最后一天
  const endDate = new Date(targetYear, month + 1, 0);
  
  return { startDate, endDate };
}

/**
 * 格式化日期为 YYYY-MM-DD 字符串
 * @param {Date} date 日期对象
 * @returns {string} 格式化后的日期字符串
 */
export function formatDateToYYYYMMDD(date) {
  if (!date) return '';
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

/**
 * 获取上周的日期范围（周一到周日）
 * @returns {Object} 包含startDate和endDate的对象
 */
export function getLastWeekRange() {
  const today = new Date();
  const dayOfWeek = today.getDay(); // 0 = Sunday, 1 = Monday, ..., 6 = Saturday
  
  // 计算上周日（上周的结束日期）
  const lastSunday = new Date(today);
  lastSunday.setDate(today.getDate() - dayOfWeek - 1);
  
  // 计算上周一（上周的开始日期）
  const lastMonday = new Date(lastSunday);
  lastMonday.setDate(lastSunday.getDate() - 6);
  
  return { 
    startDate: lastMonday, 
    endDate: lastSunday 
  };
}

/**
 * 获取本周的日期范围（周一到今天）
 * @returns {Object} 包含startDate和endDate的对象
 */
export function getThisWeekRange() {
  const today = new Date();
  const dayOfWeek = today.getDay(); // 0 = Sunday, 1 = Monday, ..., 6 = Saturday
  
  // 本周一（本周的开始日期）
  const thisMonday = new Date(today);
  thisMonday.setDate(today.getDate() - (dayOfWeek === 0 ? 6 : dayOfWeek - 1));
  
  return { 
    startDate: thisMonday, 
    endDate: today 
  };
}