/**
 * 数据解析工具函数
 * 支持文本和HTML表格解析
 */

/**
 * 解析手动输入的战绩数据
 * @param {string} inputText 输入的文本或HTML表格
 * @returns {Array} 解析后的战绩数据
 */
export function parseManualInputData(inputText) {
  if (!inputText || !inputText.trim()) {
    return []
  }
  
  // 判断是否是HTML表格
  if (isHtmlTable(inputText)) {
    return parseHtmlTableData(inputText)
  } else {
    return parseTextData(inputText)
  }
}

/**
 * 判断输入是否为HTML表格
 * @param {string} text 输入文本
 * @returns {boolean} 是否为HTML表格
 */
function isHtmlTable(text) {
  return /<table[^>]*>.*?<\/table>/gi.test(text)
}

/**
 * 解析HTML表格数据
 * @param {string} html HTML表格字符串
 * @returns {Array} 解析后的数据数组
 */
function parseHtmlTableData(html) {
  // 创建一个临时的DOM元素来解析HTML
  const tempDiv = document.createElement('div')
  tempDiv.innerHTML = html
  
  const table = tempDiv.querySelector('table')
  if (!table) {
    return []
  }
  
  const rows = table.querySelectorAll('tr')
  if (rows.length < 2) {
    return []
  }
  
  // 获取表头
  const headers = []
  const headerCells = rows[0].querySelectorAll('td, th')
  headerCells.forEach(cell => {
    headers.push(cell.textContent.trim())
  })
  
  // 解析数据行
  const dataRows = []
  for (let i = 1; i < rows.length; i++) {
    const cells = rows[i].querySelectorAll('td')
    if (cells.length < headers.length) {
      continue
    }
    
    const rowData = {}
    headers.forEach((header, index) => {
      rowData[header] = cells[index].textContent.trim()
    })
    
    dataRows.push(rowData)
  }
  
  // 转换为标准格式
  return convertToStandardFormat(dataRows)
}

/**
 * 解析纯文本数据
 * @param {string} text 文本数据
 * @returns {Array} 解析后的数据数组
 */
function parseTextData(text) {
  // 按行分割
  const lines = text.trim().split('\n').filter(line => line.trim())
  
  if (lines.length === 0) {
    return []
  }
  
  // 尝试识别表头
  const headerLine = lines[0]
  const headers = extractHeadersFromLine(headerLine)
  
  // 解析数据行
  const dataRows = []
  for (let i = 1; i < lines.length; i++) {
    const line = lines[i].trim()
    if (!line) continue
    
    const rowData = extractDataFromLine(line, headers)
    if (rowData) {
      dataRows.push(rowData)
    }
  }
  
  // 转换为标准格式
  return convertToStandardFormat(dataRows)
}

/**
 * 从文本行中提取表头
 * @param {string} line 文本行
 * @returns {Array} 表头数组
 */
function extractHeadersFromLine(line) {
  // 常见的表头关键词
  const headerKeywords = ['昵称', '击杀', '创地', '刨地', '死亡', '复活', 'KD', 'K/D', 'K/D比', '总计']
  
  // 尝试分割行
  const parts = line.split(/\s+|\t+/).filter(part => part.trim())
  
  // 识别表头
  const headers = []
  parts.forEach(part => {
    const matchedKeyword = headerKeywords.find(keyword => 
      part.includes(keyword) || keyword.includes(part)
    )
    if (matchedKeyword) {
      headers.push(matchedKeyword)
    }
  })
  
  // 如果没有识别到表头，使用默认表头
  if (headers.length === 0) {
    return ['昵称', '击杀', '创地', '死亡', '复活', 'K/D比']
  }
  
  return headers
}

/**
 * 从文本行中提取数据
 * @param {string} line 文本行
 * @param {Array} headers 表头数组
 * @returns {Object} 数据对象
 */
function extractDataFromLine(line, headers) {
  // 尝试分割行 - 改进分割逻辑，支持中文标点
  const parts = line.split(/[\s、，,\t]+/).filter(part => part.trim())
  
  if (parts.length < 3) {
    return null
  }
  
  const rowData = {}
  
  // 根据表头数量分配数据
  if (parts.length >= headers.length) {
    headers.forEach((header, index) => {
      rowData[header] = parts[index]
    })
  } else {
    // 如果数据部分少于表头，尝试智能分配
    // 第一部分通常是昵称
    rowData[headers[0]] = parts[0]
    
    // 剩余部分是数字，按顺序分配
    const remainingParts = parts.slice(1)
    remainingParts.forEach((part, index) => {
      if (index + 1 < headers.length) {
        rowData[headers[index + 1]] = part
      }
    })
  }
  
  return rowData
}

/**
 * 将解析的数据转换为标准格式
 * @param {Array} dataRows 数据行数组
 * @returns {Array} 标准格式数据
 */
function convertToStandardFormat(dataRows) {
  const standardData = []
  
  dataRows.forEach(row => {
    // 尝试识别并转换各个字段
    let nickname = ''
    let kills = 0
    let digs = 0
    let deaths = 0
    let revives = 0
    let kdRatio = 0
    
    // 昵称
    if (row['昵称']) nickname = row['昵称']
    else if (row['姓名']) nickname = row['姓名']
    else if (row['玩家']) nickname = row['玩家']
    
    // 击杀
    if (row['击杀']) kills = parseInt(row['击杀']) || 0
    
    // 刨地
    if (row['创地']) digs = parseInt(row['创地']) || 0
    else if (row['刨地']) digs = parseInt(row['刨地']) || 0
    
    // 死亡
    if (row['死亡']) deaths = parseInt(row['死亡']) || 0
    
    // 复活
    if (row['复活']) revives = parseInt(row['复活']) || 0
    
    // KD比
    if (row['KD']) kdRatio = parseFloat(row['KD']) || 0
    else if (row['K/D']) kdRatio = parseFloat(row['K/D']) || 0
    else if (row['K/D比']) kdRatio = parseFloat(row['K/D比']) || 0
    
    // 如果KD比为0但有击杀和死亡数据，尝试计算
    if (kdRatio === 0 && kills > 0 && deaths > 0) {
      kdRatio = parseFloat((kills / deaths).toFixed(2))
    }
    
    // 跳过总计行
    if (nickname.includes('总计') || nickname.includes('平均')) {
      return
    }
    
    // 只有当至少有昵称或击杀数据时才添加
    if (nickname || kills > 0) {
      standardData.push({
        nickname,
        kills,
        digs,
        deaths,
        revives,
        kdRatio,
        // 原始行数据，用于调试
        originalData: row
      })
    }
  })
  
  return standardData
}

/**
 * 验证解析的数据
 * @param {Array} data 解析后的数据
 * @returns {Object} 验证结果
 */
export function validateParsedData(data) {
  const result = {
    valid: true,
    errors: [],
    warnings: []
  }
  
  if (!data || data.length === 0) {
    result.valid = false
    result.errors.push('未能解析到有效的战绩数据')
    return result
  }
  
  data.forEach((item, index) => {
    // 检查是否有至少一项有效数据
    if (!item.nickname && item.kills === 0 && item.digs === 0 && 
        item.deaths === 0 && item.revives === 0) {
      result.warnings.push(`第${index + 1}行数据不完整，缺少昵称和战绩数据`)
    }
    
    // 检查KD值是否合理
    if (item.kdRatio > 50 || item.kdRatio < 0) {
      result.warnings.push(`第${index + 1}行数据KD值异常: ${item.kdRatio}`)
    }
    
    // 检查数字是否异常大
    if (item.kills > 100) {
      result.warnings.push(`第${index + 1}行击杀数异常: ${item.kills}`)
    }
  })
  
  return result
}