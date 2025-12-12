<template>
  <el-card class="data-table-card">
    <template #header>
      <div class="table-header">
        <span>详细战绩记录</span>
        <el-button type="primary" size="small" @click="exportPlayerData">导出数据</el-button>
      </div>
    </template>
    <el-table :data="records" stripe>
      <el-table-column prop="record_date" label="日期" min-width="100">
        <template #default="scope">
          {{ formatDate(scope.row.record_date) }}
        </template>
      </el-table-column>
      <el-table-column prop="kills" label="击杀" min-width="70" align="center" />
      <el-table-column prop="deaths" label="死亡" min-width="70" align="center" />
      <el-table-column prop="digs" label="刨地" min-width="70" align="center" />
      <el-table-column prop="revives" label="复活" min-width="70" align="center" />
      <el-table-column prop="kdRatio" label="KD" min-width="70" align="center">
        <template #default="scope">
          <span :class="[
            'kd-value',
            { 'kd-high': scope.row.kdRatio >= 1 },
            { 'kd-low': scope.row.kdRatio < 1 }
          ]">
            {{ scope.row.kdRatio ? scope.row.kdRatio.toFixed(2) : '-' }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="created_at" label="记录时间" min-width="140">
        <template #default="scope">
          {{ formatDateTime(scope.row.created_at) }}
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import { formatDate, formatDateTime } from '../utils/dateUtils'

const props = defineProps({
  records: {
    type: Array,
    default: () => []
  },
  playerData: {
    type: Object,
    default: () => ({})
  }
})

// 导出玩家数据
function exportPlayerData() {
  if (!props.playerData || !props.playerData.records) {
    ElMessage.warning('没有可导出的数据')
    return
  }
  
  const exportData = props.records.map(record => ({
    '日期': formatDate(record.record_date || record.recordDate),
    '击杀': record.kills || 0,
    '死亡': record.deaths || 0,
    '刨地': record.digs || 0,
    '复活': record.revives || 0,
    'KD': (record.kdRatio || 0).toFixed(2),
    '记录时间': formatDateTime(record.created_at || record.createdAt)
  }))
  
  // 转换为CSV格式
  const csv = convertToCSV(exportData)
  
  // 创建下载链接
  const blob = new Blob(['\uFEFF' + csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `${props.playerData.nickname}_战绩数据_${new Date().toISOString().slice(0, 10)}.csv`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
  
  ElMessage.success('数据导出成功')
}

// 转换为CSV格式
function convertToCSV(data) {
  if (data.length === 0) return ''
  
  const headers = Object.keys(data[0])
  let csv = headers.join(',') + '\n'
  
  data.forEach(row => {
    const values = headers.map(header => {
      let value = row[header] || ''
      if (typeof value === 'string' && (value.includes(',') || value.includes('"') || value.includes('\n'))) {
        value = `"${value.replace(/"/g, '""')}"`
      }
      return value
    })
    csv += values.join(',') + '\n'
  })
  
  return csv
}
</script>

<style scoped>
.data-table-card {
  margin-top: 20px;
  width: 100%;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 表格样式优化 */
.el-table {
  width: 100%;
  font-size: 14px;
}

/* 表格列样式 */
.el-table__cell {
  padding: 8px 0;
}

/* 表头样式 */
.el-table__header th {
  background-color: #f5f7fa;
  color: #606266;
  font-weight: 500;
}

/* KD值样式 */
.kd-value {
  font-weight: 500;
}

.kd-high {
  color: #67C23A;
}

.kd-low {
  color: #F56C6C;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .data-table-card {
    margin-top: 15px;
  }
  
  .el-table {
    font-size: 13px;
  }
  
  .el-table__cell {
    padding: 6px 0;
  }
  
  .el-table__header th {
    font-size: 12px;
  }
  
  /* 在小屏幕上隐藏部分列以节省空间 */
  :deep(.el-table__body-wrapper) {
    overflow-x: auto;
  }
}
</style>