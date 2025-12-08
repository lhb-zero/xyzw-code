<template>
  <el-card class="data-table-card">
    <template #header>
      <div class="table-header">
        <span>详细战绩记录</span>
        <el-button type="primary" size="small" @click="exportPlayerData">导出数据</el-button>
      </div>
    </template>
    <el-table :data="records" stripe>
      <el-table-column prop="record_date" label="日期" width="120">
        <template #default="scope">
          {{ formatDate(scope.row.record_date) }}
        </template>
      </el-table-column>
      <el-table-column prop="kills" label="击杀" width="80" align="center" />
      <el-table-column prop="deaths" label="死亡" width="80" align="center" />
      <el-table-column prop="digs" label="刨地" width="80" align="center" />
      <el-table-column prop="revives" label="复活" width="80" align="center" />
      <el-table-column prop="kdRatio" label="KD" width="80" align="center">
        <template #default="scope">
          <span :style="{ color: scope.row.kdRatio >= 1 ? '#67C23A' : '#F56C6C', fontWeight: 'bold' }">
            {{ scope.row.kdRatio ? scope.row.kdRatio.toFixed(2) : '-' }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="created_at" label="记录时间" width="160">
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
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>