<template>
  <el-card shadow="always">
    <template #header>
      <div class="card-header">
        <span>俱乐部战绩详情</span>
        <div>
          <el-button size="small" @click="$emit('refresh-records')">
            <el-icon><refresh /></el-icon>
            刷新
          </el-button>
          <el-button size="small" type="primary" @click="$emit('show-add-dialog')">手动添加</el-button>
        </div>
      </div>
    </template>
    
    <el-row class="stats-row" :gutter="20">
      <el-col :span="6" :xs="12">
        <el-card shadow="hover" class="statistic-card statistic-card-blue">
          <el-statistic title="总战绩数" :value="totalCount">
            <template #prefix>
              <el-icon class="statistic-icon"><document /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6" :xs="12">
        <el-card shadow="hover" class="statistic-card statistic-card-green">
          <el-statistic title="平均KD" :value="avgKdRatio" :precision="2">
            <template #prefix>
              <el-icon class="statistic-icon"><trend-charts /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6" :xs="12">
        <el-card shadow="hover" class="statistic-card statistic-card-orange">
          <el-statistic title="总击杀数" :value="totalKills">
            <template #prefix>
              <el-icon class="statistic-icon"><star /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6" :xs="12">
        <el-card shadow="hover" class="statistic-card statistic-card-red">
          <el-statistic title="总死亡数" :value="totalDeaths">
            <template #prefix>
              <el-icon class="statistic-icon"><close /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row class="filter-row" :gutter="20">
      <el-col :span="6" :xs="24">
        <el-select
          v-model="filters.memberId"
          placeholder="选择成员"
          clearable
          @change="handleFilterChange"
          style="width: 100%;"
        >
          <el-option
            v-for="member in clubMembers"
            :key="member.id"
            :label="member.gameId || `成员${member.id}`"
            :value="member.id"
          />
        </el-select>
      </el-col>
      <el-col :span="12" :xs="24">
        <el-date-picker
          v-model="filters.dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          @change="handleFilterChange"
          style="width: 100%;"
        />
      </el-col>
      <el-col :span="6" :xs="12" class="action-buttons">
        <el-button @click="resetFilters">重置</el-button>
        <el-button @click="showColumnSelector">自定义列</el-button>
        <el-button @click="exportData">导出</el-button>
      </el-col>
    </el-row>
    
    <!-- 快捷日期筛选 -->
    <el-row class="quick-filter-row" :gutter="10">
      <el-col :span="24">
        <span class="filter-label">快捷筛选：</span>
        <el-button size="small" type="info" @click="filterThisWeekSaturday">本周六</el-button>
        <el-button size="small" type="info" @click="filterLastWeekSaturday">上周六</el-button>
        <el-button size="small" type="info" @click="filterThisMonthFourthSunday">本月第四个周日</el-button>
        <el-button size="small" type="info" @click="filterLastMonthFourthSunday">上月第四个周日</el-button>
        <el-button size="small" type="info" @click="filterAllSaturdaysThisMonth">本月所有周六</el-button>
        <el-button size="small" type="info" @click="filterAllSaturdaysLastMonth">上月所有周六</el-button>
        <el-button size="small" type="primary" @click="filterThisMonthAll">本月所有战绩</el-button>
        <el-button size="small" type="primary" @click="filterLastMonthAll">上月所有战绩</el-button>
      </el-col>
    </el-row>
    
    <div v-if="selectedRecords.length > 0" class="batch-actions">
      <span>已选择 **{{ selectedRecords.length }}** 项</span>
      <el-button size="small" type="danger" @click="batchDeleteRecords">批量删除</el-button>
    </div>
    
    <div class="table-container">
      <el-table
        :data="displayRecords"
        style="width: 100%"
        @selection-change="handleSelectionChange"
        :default-sort="{prop: 'createdAt', order: 'descending'}"
        @sort-change="handleSortChange"
        border
      >
        <el-table-column type="selection" width="55" />
        
        <el-table-column 
          v-if="visibleColumns.memberId"
          label="成员ID" 
          width="100" 
          prop="memberId" 
          sortable 
        />
        
        <el-table-column 
          v-if="visibleColumns.nickname"
          label="昵称" 
          min-width="100" 
          prop="nickname"
          sortable
          show-overflow-tooltip
        >
          <template #default="scope">
            <el-tag size="small">{{ getMemberNickname(scope.row.memberId) || '-' }}</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column 
          v-if="visibleColumns.recordDate"
          label="日期" 
          min-width="100"
          prop="record_date"
          sortable
        >
          <template #default="scope">
            {{ formatDate(scope.row.record_date || scope.row.recordDate) }}
          </template>
        </el-table-column>
        
        <el-table-column 
          v-if="visibleColumns.kills"
          prop="kills" 
          label="击杀" 
          width="80" 
          sortable 
          align="center"
        />
        
        <el-table-column 
          v-if="visibleColumns.deaths"
          prop="deaths" 
          label="死亡" 
          width="80" 
          sortable 
          align="center"
        />
        
        <el-table-column 
          v-if="visibleColumns.digs"
          prop="digs" 
          label="刨地" 
          width="80" 
          sortable 
          align="center"
        />
        
        <el-table-column 
          v-if="visibleColumns.revives"
          prop="revives" 
          label="复活" 
          width="80" 
          sortable 
          align="center"
        />
        
        <el-table-column 
          v-if="visibleColumns.kdRatio"
          prop="kdRatio" 
          label="KD" 
          width="90" 
          sortable
          align="center"
        >
          <template #default="scope">
            <span :style="{ color: (scope.row.kdRatio >= 1 ? '#67C23A' : '#F56C6C'), fontWeight: 'bold' }">
              {{ scope.row.kdRatio ? scope.row.kdRatio.toFixed(2) : '-' }}
            </span>
          </template>
        </el-table-column>
        
        <el-table-column 
          v-if="visibleColumns.createdAt"
          prop="created_at" 
          label="添加时间" 
          min-width="130"
          sortable
        >
          <template #default="scope">
            {{ formatDateTime(scope.row.created_at || scope.row.createdAt) }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="240" fixed="right" align="center">
          <template #default="scope">
            <el-button size="small" link type="primary" @click="$emit('edit-record', scope.row)">编辑</el-button>
            <el-button size="small" link type="success" @click="showPlayerStats(scope.row)">个人详情</el-button>
            <el-button size="small" link type="danger" @click="$emit('delete-record', scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    
    <el-pagination
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :page-sizes="[30, 60, 90, 120]"
      :total="props.confirmedRecords.length"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />
    
    <el-dialog v-model="columnSelectorVisible" title="自定义显示列" width="500px">
      <el-checkbox-group v-model="checkedColumns">
        <el-checkbox
          v-for="(label, key) in allColumns"
          :key="key"
          :label="key"
        >
          {{ label }}
        </el-checkbox>
      </el-checkbox-group>
      <template #footer>
        <el-button @click="columnSelectorVisible = false">取消</el-button>
        <el-button type="primary" @click="saveColumnSettings">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- 个人详情对话框 -->
    <player-stats-dialog
      v-model:visible="playerStatsVisible"
      :member-id="currentMemberId"
      :nickname="currentNickname"
      :all-records="props.confirmedRecords"
    />
  </el-card>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { Refresh, Document, TrendCharts, Star, Close } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PlayerStatsDialog from './PlayerStatsDialog.vue'

const props = defineProps({
  confirmedRecords: {
    type: Array,
    default: () => []
  },
  clubMembers: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits([
  'refresh-records',
  'show-add-dialog',
  'edit-record',
  'delete-record'
])

// 分页相关
const currentPage = ref(1)
const pageSize = ref(30)

// 筛选条件
const filters = ref({
  memberId: null,
  dateRange: null
})

// 排序条件
const sortProp = ref('created_at')  // 使用数据库字段名
const sortOrder = ref('descending')

// 选中的记录
const selectedRecords = ref([])

// 自定义列显示
const columnSelectorVisible = ref(false)

// 个人详情对话框
const playerStatsVisible = ref(false)
const currentMemberId = ref(null)
const currentNickname = ref('')
const allColumns = ref({
  memberId: '成员ID',
  nickname: '昵称',
  recordDate: '日期',
  kills: '击杀',
  deaths: '死亡',
  digs: '刨地',
  revives: '复活',
  kdRatio: 'KD',
  createdAt: '添加时间'
})

const checkedColumns = ref(Object.keys(allColumns.value))
const visibleColumns = ref({})

// 初始化可见列
onMounted(() => {
  updateVisibleColumns()
})

// 监听选中的列变化
watch(checkedColumns, () => {
  updateVisibleColumns()
}, { deep: true })

// 更新可见列
function updateVisibleColumns() {
  const result = {}
  checkedColumns.value.forEach(key => {
    result[key] = true
  })
  visibleColumns.value = result
}

// 根据成员ID获取成员昵称
function getMemberNickname(memberId) {
  if (!memberId) return ''
  
  const member = props.clubMembers.find(m => m.id === memberId || m.member_id === memberId)
  return member ? member.gameId || `成员${memberId}` : ''
}

// 格式化日期
function formatDate(dateStr) {
  if (!dateStr) return ''
  
  // 如果已经是格式化的日期字符串（只包含日期部分），直接返回
  if (typeof dateStr === 'string' && dateStr.length === 10 && dateStr.includes('-')) {
    return dateStr
  }
  
  // 尝试解析日期
  let date
  if (typeof dateStr === 'string') {
    // 处理可能的字符串格式
    if (dateStr.includes('T')) {
      // ISO格式
      date = new Date(dateStr)
    } else if (dateStr.includes('-') && dateStr.includes(' ')) {
      // YYYY-MM-DD HH:MM:SS 格式
      // 只取日期部分
      return dateStr.split(' ')[0]
    } else if (dateStr.includes('-')) {
      // YYYY-MM-DD格式
      const parts = dateStr.split('-')
      if (parts.length >= 3) {
        date = new Date(parseInt(parts[0]), parseInt(parts[1]) - 1, parseInt(parts[2]))
      } else {
        date = new Date(dateStr)
      }
    } else {
      date = new Date(dateStr)
    }
  } else if (typeof dateStr === 'number') {
    // 时间戳（毫秒）
    date = new Date(dateStr)
  } else if (dateStr instanceof Date) {
    date = dateStr
  } else {
    date = new Date(dateStr)
  }
  
  // 检查日期是否有效
  if (isNaN(date.getTime())) {
    console.warn('无效的日期格式:', dateStr)
    return dateStr // 返回原始字符串
  }
  
  // 使用本地时间格式化，而不是UTC时间
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`
}

// 格式化日期时间
function formatDateTime(dateStr) {
  if (!dateStr) return ''
  
  // 如果已经是格式化的日期时间字符串，直接返回
  if (typeof dateStr === 'string' && dateStr.length > 10 && dateStr.includes(' ')) {
    return dateStr
  }
  
  // 尝试解析日期
  let date
  if (typeof dateStr === 'number') {
    // 如果是时间戳（毫秒）
    date = new Date(dateStr)
  } else if (dateStr instanceof Date) {
    date = dateStr
  } else if (typeof dateStr === 'string') {
    // 检查是否是有效的日期时间字符串
    if (dateStr.includes('T')) {
      date = new Date(dateStr)
    } else if (dateStr.includes('-') && dateStr.includes(' ')) {
      // 可能已经是格式化过的日期时间
      return dateStr
    } else {
      date = new Date(dateStr)
    }
  } else {
    date = new Date(dateStr)
  }

  if (isNaN(date.getTime())) {
    console.warn('无效的日期时间格式:', dateStr)
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

// 筛选记录
const filteredRecords = computed(() => {
  let result = [...props.confirmedRecords]
  
  // 按成员筛选
  if (filters.value.memberId) {
    result = result.filter(record => 
      record.member_id === filters.value.memberId || 
      record.memberId === filters.value.memberId
    )
  }
  
  // 按日期范围筛选
  if (filters.value.dateRange && filters.value.dateRange.length === 2) {
    const [startDate, endDate] = filters.value.dateRange
    // 将结束日期推到当天 23:59:59，以包含当天记录
    const inclusiveEndDate = new Date(endDate)
    inclusiveEndDate.setHours(23, 59, 59, 999)

    result = result.filter(record => {
      // 检查数据库字段 record_date (下划线命名)
      let dateField = record.record_date || record.recordDate
      
      // 如果 record_date 不存在，尝试其他可能的字段名
      if (!dateField) {
        dateField = record.battleDate || record.date || record.createTime || record.createdAt
      }
      
      if (!dateField) return false
      
      // 尝试解析日期
      let recordDate
      if (typeof dateField === 'string') {
        // 如果是YYYY-MM-DD格式字符串，直接创建日期对象
        if (dateField.includes('-') && dateField.length === 10) {
          const parts = dateField.split('-')
          recordDate = new Date(parseInt(parts[0]), parseInt(parts[1]) - 1, parseInt(parts[2]))
        } else if (dateField.includes('T')) {
          // ISO格式字符串
          recordDate = new Date(dateField)
        } else if (dateField.includes(' ') && dateField.includes(':')) {
          // YYYY-MM-DD HH:MM:SS 格式
          recordDate = new Date(dateField)
        } else {
          // 其他格式字符串，尝试直接解析
          recordDate = new Date(dateField)
        }
      } else if (dateField instanceof Date) {
        recordDate = dateField
      } else if (typeof dateField === 'number') {
        // 时间戳
        recordDate = new Date(dateField)
      } else {
        recordDate = new Date(dateField)
      }
      
      // 检查日期是否有效
      if (isNaN(recordDate.getTime())) {
        console.warn('无效的日期格式:', dateField)
        return false
      }
      
      return recordDate >= startDate && recordDate <= inclusiveEndDate
    })
  }
  
  // 排序
  result.sort((a, b) => {
    // 处理可能的字段名差异
    let aValue = a[sortProp.value]
    let bValue = b[sortProp.value]
    
    // 特殊处理一些可能有不同命名的字段
    if (sortProp.value === 'created_at' && a.created_at === undefined) {
      aValue = a.createdAt
      bValue = b.createdAt
    } else if (sortProp.value === 'record_date' && a.record_date === undefined) {
      aValue = a.recordDate
      bValue = b.recordDate
    } else if (sortProp.value === 'member_id' && a.member_id === undefined) {
      aValue = a.memberId
      bValue = b.memberId
    }
    
    if (aValue === bValue) return 0
    
    // 如果是日期字符串，先转换为Date对象再比较
    if (typeof aValue === 'string' && aValue.includes('-') && aValue.length >= 10) {
      aValue = new Date(aValue)
      bValue = new Date(bValue)
    }
    
    if (sortOrder.value === 'ascending') {
      return aValue > bValue ? 1 : -1
    } else {
      return aValue < bValue ? 1 : -1
    }
  })
  
  return result
})

// 当前页显示的记录
const displayRecords = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredRecords.value.slice(start, end)
})

// 统计信息 - 使用筛选后的记录计算所有统计数据
const totalCount = computed(() => filteredRecords.value.length)

// 根据当前筛选条件计算统计信息
const totalKills = computed(() => {
  return filteredRecords.value.reduce((sum, record) => sum + (record.kills || 0), 0)
})

const totalDeaths = computed(() => {
  return filteredRecords.value.reduce((sum, record) => sum + (record.deaths || 0), 0)
})

// 平均KD基于筛选后的记录计算
const avgKdRatio = computed(() => {
  if (filteredRecords.value.length === 0) return 0
  
  // 用筛选后的总击杀/总死亡来计算平均 KD
  if (totalDeaths.value === 0) {
    return totalKills.value > 0 ? totalKills.value : 0
  }
  return totalKills.value / totalDeaths.value
})

// 处理筛选条件变化
function handleFilterChange() {
  currentPage.value = 1
}

// 处理搜索
function handleSearch() {
  currentPage.value = 1
}

// 重置筛选条件
function resetFilters() {
  filters.value = {
    memberId: null,
    dateRange: null
  }
  currentPage.value = 1
  
  // 重置所有计数器
  lastSaturdayClickCount.value = 0
  lastMonthFourthSundayClickCount.value = 0
  lastMonthAllSaturdaysClickCount.value = 0
  lastMonthAllClickCount.value = 0
  
  // 提示用户重置成功
  ElMessage.success('筛选条件已重置')
}

// 快捷日期筛选函数
// 循环点击上周六计数器
const lastSaturdayClickCount = ref(0)

// 循环点击上月第四个周日计数器
const lastMonthFourthSundayClickCount = ref(0)

// 循环点击上月所有周六计数器
const lastMonthAllSaturdaysClickCount = ref(0)

// 循环点击上月所有战绩计数器
const lastMonthAllClickCount = ref(0)

// 筛选本周六
function filterThisWeekSaturday() {
  const today = new Date()
  const currentDay = today.getDay() // 0是周日，6是周六
  const daysUntilSaturday = (6 - currentDay + 7) % 7 // 计算到本周六的天数
  
  const saturday = new Date(today)
  saturday.setDate(today.getDate() + daysUntilSaturday)
  
  // 确保日期从午夜开始，以便正确筛选
  saturday.setHours(0, 0, 0, 0)
  
  // 设置日期范围为本周六当天（确保包含全天记录）
  const endOfDay = new Date(saturday)
  endOfDay.setHours(23, 59, 59, 999)
  
  filters.value.dateRange = [saturday, endOfDay]
  currentPage.value = 1
  
  // 重置上周六点击计数器
  lastSaturdayClickCount.value = 0
  
  // 显示当前选择的日期
  const dateStr = formatDate(saturday)
  ElMessage.success(`已选择本周六 (${dateStr}) 的记录`)
}

// 筛选上周六（支持循环点击）
function filterLastWeekSaturday() {
  const today = new Date()
  const currentDay = today.getDay()
  
  // 每点击一次，往前推一周（7天）
  lastSaturdayClickCount.value++
  
  // 找到最近的周六
  const daysFromLastSaturday = (currentDay + 7 - 6) % 7 || 7
  const lastSaturday = new Date(today)
  lastSaturday.setDate(today.getDate() - daysFromLastSaturday)
  
  // 根据点击次数往前推
  const targetSaturday = new Date(lastSaturday)
  targetSaturday.setDate(lastSaturday.getDate() - (lastSaturdayClickCount.value - 1) * 7)
  
  // 确保日期从午夜开始，到午夜结束
  targetSaturday.setHours(0, 0, 0, 0)
  const endOfDay = new Date(targetSaturday)
  endOfDay.setHours(23, 59, 59, 999)
  
  // 设置日期范围为目标周六当天（确保包含全天记录）
  filters.value.dateRange = [targetSaturday, endOfDay]
  currentPage.value = 1
  
  // 显示当前选择的日期
  const dateStr = formatDate(targetSaturday)
  const weeksAgo = lastSaturdayClickCount.value === 1 ? '上周' : `${lastSaturdayClickCount.value}周前`
  ElMessage.success(`已选择${weeksAgo}六 (${dateStr}) 的记录`)
}

// 筛选本月第四个周日
function filterThisMonthFourthSunday() {
  const today = new Date()
  const currentYear = today.getFullYear()
  const currentMonth = today.getMonth()
  
  // 获取本月第四个周日
  const fourthSunday = getNthWeekdayOfMonth(currentYear, currentMonth, 0, 4) // 0=周日，4=第4个
  
  // 确保日期从午夜开始，到午夜结束
  const startOfDay = new Date(fourthSunday)
  startOfDay.setHours(0, 0, 0, 0)
  const endOfDay = new Date(fourthSunday)
  endOfDay.setHours(23, 59, 59, 999)
  
  // 设置日期范围为本月第四个周日当天（确保包含全天记录）
  filters.value.dateRange = [startOfDay, endOfDay]
  currentPage.value = 1
  
  // 重置上月第四个周日点击计数器
  lastMonthFourthSundayClickCount.value = 0
  
  // 显示当前选择的日期
  const dateStr = formatDate(fourthSunday)
  ElMessage.success(`已选择本月第四个周日 (${dateStr}) 的记录`)
}

// 筛选上月第四个周日（支持循环点击）
function filterLastMonthFourthSunday() {
  const today = new Date()
  
  // 每点击一次，往前推一个月
  lastMonthFourthSundayClickCount.value++
  
  // 计算目标月份
  let targetMonth = today.getMonth() - lastMonthFourthSundayClickCount.value
  let targetYear = today.getFullYear()
  
  // 处理跨年情况
  while (targetMonth < 0) {
    targetMonth += 12
    targetYear -= 1
  }
  
  // 获取目标月份的第四个周日
  const fourthSunday = getNthWeekdayOfMonth(targetYear, targetMonth, 0, 4) // 0=周日，4=第4个
  
  // 确保日期从午夜开始，到午夜结束
  const startOfDay = new Date(fourthSunday)
  startOfDay.setHours(0, 0, 0, 0)
  const endOfDay = new Date(fourthSunday)
  endOfDay.setHours(23, 59, 59, 999)
  
  // 设置日期范围为目标月份第四个周日当天（确保包含全天记录）
  filters.value.dateRange = [startOfDay, endOfDay]
  currentPage.value = 1
  
  // 显示当前选择的日期
  const dateStr = formatDate(fourthSunday)
  const monthName = ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'][targetMonth]
  const monthsAgo = lastMonthFourthSundayClickCount.value === 1 ? '上月' : `${lastMonthFourthSundayClickCount.value}个月前`
  ElMessage.success(`已选择${monthsAgo} (${targetYear}年${monthName}第四个周日 ${dateStr}) 的记录`)
}

// 筛选本月所有周六
function filterAllSaturdaysThisMonth() {
  const today = new Date()
  const currentYear = today.getFullYear()
  const currentMonth = today.getMonth()
  
  // 获取本月所有周六
  const saturdays = getAllWeekdaysOfMonth(currentYear, currentMonth, 6) // 6=周六
  
  if (saturdays.length > 0) {
    // 设置日期范围为本月第一个周六到最后一个周六
    // 确保包含完整日期范围
    const startOfDay = new Date(saturdays[0])
    startOfDay.setHours(0, 0, 0, 0)
    const endOfDay = new Date(saturdays[saturdays.length - 1])
    endOfDay.setHours(23, 59, 59, 999)
    
    filters.value.dateRange = [startOfDay, endOfDay]
    currentPage.value = 1
    
    // 重置上月所有周六点击计数器
    lastMonthAllSaturdaysClickCount.value = 0
    
    // 显示日期范围
    const startDateStr = formatDate(saturdays[0])
    const endDateStr = formatDate(saturdays[saturdays.length - 1])
    ElMessage.success(`已选择本月所有周六 (${startDateStr} 至 ${endDateStr}) 的记录`)
  } else {
    ElMessage.warning('本月没有周六记录')
  }
}

// 筛选本月所有战绩
function filterThisMonthAll() {
  const today = new Date()
  const currentYear = today.getFullYear()
  const currentMonth = today.getMonth()
  
  // 获取本月的第一天和最后一天
  const firstDay = new Date(currentYear, currentMonth, 1)
  const lastDay = new Date(currentYear, currentMonth + 1, 0)
  
  // 确保包含完整日期范围
  const startOfDay = new Date(firstDay)
  startOfDay.setHours(0, 0, 0, 0)
  const endOfDay = new Date(lastDay)
  endOfDay.setHours(23, 59, 59, 999)
  
  filters.value.dateRange = [startOfDay, endOfDay]
  currentPage.value = 1
  
  // 重置上月所有战绩点击计数器
  lastMonthAllClickCount.value = 0
  
  // 显示日期范围
  const startDateStr = formatDate(firstDay)
  const endDateStr = formatDate(lastDay)
  ElMessage.success(`已选择本月所有战绩 (${startDateStr} 至 ${endDateStr})`)
}

// 筛选上月所有周六（支持循环点击）
function filterAllSaturdaysLastMonth() {
  const today = new Date()
  
  // 每点击一次，往前推一个月
  lastMonthAllSaturdaysClickCount.value++
  
  // 计算目标月份
  let targetMonth = today.getMonth() - lastMonthAllSaturdaysClickCount.value
  let targetYear = today.getFullYear()
  
  // 处理跨年情况
  while (targetMonth < 0) {
    targetMonth += 12
    targetYear -= 1
  }
  
  // 获取目标月份所有周六
  const saturdays = getAllWeekdaysOfMonth(targetYear, targetMonth, 6) // 6=周六
  
  if (saturdays.length > 0) {
    // 设置日期范围为目标月份第一个周六到最后一个周六
    // 确保包含完整日期范围
    const startOfDay = new Date(saturdays[0])
    startOfDay.setHours(0, 0, 0, 0)
    const endOfDay = new Date(saturdays[saturdays.length - 1])
    endOfDay.setHours(23, 59, 59, 999)
    
    filters.value.dateRange = [startOfDay, endOfDay]
    currentPage.value = 1
    
    // 显示日期范围
    const startDateStr = formatDate(saturdays[0])
    const endDateStr = formatDate(saturdays[saturdays.length - 1])
    const monthName = ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'][targetMonth]
    const monthsAgo = lastMonthAllSaturdaysClickCount.value === 1 ? '上月' : `${lastMonthAllSaturdaysClickCount.value}个月前`
    ElMessage.success(`已选择${monthsAgo} (${targetYear}年${monthName}所有周六 ${startDateStr} 至 ${endDateStr}) 的记录`)
  } else {
    const monthName = ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'][targetMonth]
    const monthsAgo = lastMonthAllSaturdaysClickCount.value === 1 ? '上月' : `${lastMonthAllSaturdaysClickCount.value}个月前`
    ElMessage.warning(`${monthsAgo} (${targetYear}年${monthName})没有周六记录`)
  }
}

// 筛选上月所有战绩（支持循环点击）
function filterLastMonthAll() {
  const today = new Date()
  
  // 每点击一次，往前推一个月
  lastMonthAllClickCount.value++
  
  // 计算目标月份
  let targetMonth = today.getMonth() - lastMonthAllClickCount.value
  let targetYear = today.getFullYear()
  
  // 处理跨年情况
  while (targetMonth < 0) {
    targetMonth += 12
    targetYear -= 1
  }
  
  // 获取目标月份的第一天和最后一天
  const firstDay = new Date(targetYear, targetMonth, 1)
  const lastDay = new Date(targetYear, targetMonth + 1, 0)
  
  // 确保包含完整日期范围
  const startOfDay = new Date(firstDay)
  startOfDay.setHours(0, 0, 0, 0)
  const endOfDay = new Date(lastDay)
  endOfDay.setHours(23, 59, 59, 999)
  
  filters.value.dateRange = [startOfDay, endOfDay]
  currentPage.value = 1
  
  // 显示日期范围
  const startDateStr = formatDate(firstDay)
  const endDateStr = formatDate(lastDay)
  const monthName = ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'][targetMonth]
  const monthsAgo = lastMonthAllClickCount.value === 1 ? '上月' : `${lastMonthAllClickCount.value}个月前`
  ElMessage.success(`已选择${monthsAgo}所有战绩 (${targetYear}年${monthName} ${startDateStr} 至 ${endDateStr})`)
}

// 辅助函数：获取某个月份的第n个星期几
function getNthWeekdayOfMonth(year, month, weekday, n) {
  // 获取当月第一天
  const firstDay = new Date(year, month, 1)
  const firstDayOfWeek = firstDay.getDay()
  
  // 计算第一个目标星期几的日期
  let daysToAdd = (weekday - firstDayOfWeek + 7) % 7
  let resultDate = new Date(year, month, 1 + daysToAdd)
  
  // 添加 (n-1) 周
  resultDate.setDate(resultDate.getDate() + (n - 1) * 7)
  
  // 确保结果日期仍在同一个月
  if (resultDate.getMonth() !== month) {
    // 如果计算结果超出了当月，返回当月最后一个目标星期几
    return getLastWeekdayOfMonth(year, month, weekday)
  }
  
  return resultDate
}

// 辅助函数：获取某个月份的最后一个星期几
function getLastWeekdayOfMonth(year, month, weekday) {
  // 获取当月最后一天
  const lastDayOfMonth = new Date(year, month + 1, 0)
  const lastDayOfWeek = lastDayOfMonth.getDay()
  
  // 计算最后一个目标星期几的日期
  const daysToSubtract = (lastDayOfWeek - weekday + 7) % 7
  return new Date(year, month, lastDayOfMonth.getDate() - daysToSubtract)
}

// 辅助函数：获取某个月份的所有指定星期几
function getAllWeekdaysOfMonth(year, month, weekday) {
  const result = []
  
  // 获取当月第一天
  const firstDay = new Date(year, month, 1)
  const firstDayOfWeek = firstDay.getDay()
  
  // 计算第一个目标星期几的日期
  let daysToAdd = (weekday - firstDayOfWeek + 7) % 7
  let currentDate = new Date(year, month, 1 + daysToAdd)
  
  // 添加所有符合条件的日期
  while (currentDate.getMonth() === month) {
    result.push(new Date(currentDate))
    currentDate.setDate(currentDate.getDate() + 7) // 加一周
  }
  
  return result
}

// 处理选择变化
function handleSelectionChange(selection) {
  selectedRecords.value = selection
}

// 处理排序变化
function handleSortChange({ prop, order }) {
  sortProp.value = prop
  // Element Plus 的 order 是 'ascending', 'descending' 或 null
  sortOrder.value = order || 'descending'
}

// 处理页码变化
function handleCurrentChange(page) {
  currentPage.value = page
}

// 处理每页显示数量变化
function handleSizeChange(size) {
  pageSize.value = size
  currentPage.value = 1
}

// 显示自定义列选择器
function showColumnSelector() {
  columnSelectorVisible.value = true
}

// 保存列设置
function saveColumnSettings() {
  updateVisibleColumns()
  columnSelectorVisible.value = false
  ElMessage.success('列设置已保存')
}

// 批量删除记录
function batchDeleteRecords() {
  if (selectedRecords.value.length === 0) {
    ElMessage.warning('请先选择要删除的记录')
    return
  }
  
  ElMessageBox.confirm(
    `确定要删除选中的 ${selectedRecords.value.length} 条战绩记录吗？此操作不可恢复！`,
    '批量删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    // 批量删除逻辑：将选中的记录 ID 列表发给父组件处理
    const idsToDelete = selectedRecords.value.map(record => record.id) 
    
    // 注意：这里需要父组件实现一个批量删除的 Emit 事件，或者遍历调用 delete-record
    // 为了保持与原有代码兼容，我们仍然遍历调用 'delete-record'，但这效率较低
    // 更好的做法是新增一个 'batch-delete-records' 事件
    
    selectedRecords.value.forEach(record => {
      emit('delete-record', record)
    })

    // 清空选择
    selectedRecords.value = []
    
    ElMessage.success('批量删除请求已发送')
  }).catch(() => {
    // 用户取消
  })
}

// 导出数据
function exportData() {
  try {
    // 准备导出数据
    const exportData = filteredRecords.value.map(record => {
      return {
        '成员ID': record.member_id || record.memberId,
        '昵称': getMemberNickname(record.member_id || record.memberId),
        '日期': formatDate(record.record_date || record.recordDate),
        '击杀': record.kills,
        '死亡': record.deaths,
        '刨地': record.digs,
        '复活': record.revives,
        'KD': record.kdRatio ? record.kdRatio.toFixed(2) : '-',
        '添加时间': formatDateTime(record.created_at || record.createdAt)
      }
    })
    
    // 转换为CSV格式
    const csv = convertToCSV(exportData)
    
    // 创建下载链接
    // 添加 \ufeff BOM 头确保 Excel 中文不乱码
    const blob = new Blob(['\uFEFF' + csv], { type: 'text/csv;charset=utf-8;' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `战绩记录_${new Date().toISOString().slice(0, 10)}.csv`
    document.body.appendChild(link); // 兼容某些浏览器
    link.click()
    
    // 释放资源
    document.body.removeChild(link);
    URL.revokeObjectURL(url)
    
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  }
}

// 显示个人详情
function showPlayerStats(record) {
  currentMemberId.value = record.member_id || record.memberId
  currentNickname.value = getMemberNickname(record.member_id || record.memberId)
  playerStatsVisible.value = true
}

// 转换为CSV格式
function convertToCSV(data) {
  if (data.length === 0) return ''
  
  // 获取表头
  const headers = Object.keys(data[0])
  
  // 构建CSV内容
  let csv = headers.join(',') + '\n'
  
  data.forEach(row => {
    const values = headers.map(header => {
      let value = row[header] || ''
      // 如果值包含逗号或引号，需要用引号括起来并转义内部引号
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
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stats-row {
  margin-bottom: 20px;
}

/* 统计卡片样式 */
.statistic-card {
  padding: 12px;
  border-radius: 8px;
  transition: all 0.3s ease;
  height: 90px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.statistic-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.statistic-card-blue { background: linear-gradient(135deg, #409EFF, #99CCFF); color: white; }
.statistic-card-green { background: linear-gradient(135deg, #67C23A, #95D475); color: white; }
.statistic-card-orange { background: linear-gradient(135deg, #E6A23C, #EEBE77); color: white; }
.statistic-card-red { background: linear-gradient(135deg, #F56C6C, #F78989); color: white; }

.statistic-icon {
  font-size: 24px;
  margin-right: 8px;
  vertical-align: middle;
}

/* 修改统计卡片的内部样式 */
.statistic-card :deep(.el-statistic__head) {
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
  margin-bottom: 8px;
}

.statistic-card :deep(.el-statistic__content) {
  font-size: 28px;
  font-weight: bold;
  color: white;
}

/* 筛选区域 */
.filter-row {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  flex-wrap: wrap; /* 允许元素在空间不足时换行 */
}

/* 批量操作区域 */
.batch-actions {
  margin-bottom: 10px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 表格容器：用于限制宽度，解决 flex 布局下的溢出问题 */
.table-container {
  width: 100%;
  overflow: hidden; /* 保证容器不会被表格撑开 */
}

/* 修复表格宽度问题 */
.el-table {
  width: 100% !important;
}

.el-table :deep(.el-table__body-wrapper) {
  overflow-x: auto;
}

.el-table :deep(.el-table__header-wrapper) {
  width: 100% !important;
}

.el-table :deep(.el-scrollbar__wrap) {
  width: 100% !important;
}

.el-table :deep(.el-scrollbar__wrap--hidden-default) {
  width: 100% !important;
}

/* 表格字体样式增大 */
.el-table :deep(.el-table__header) th {
  font-size: 15px;
  font-weight: bold;
  padding: 4px 0 !important;
}

.el-table :deep(.el-table__body) td {
  font-size: 15px !important;
  padding: 4px 0 !important;
  height: 28px !important;
  line-height: 28px !important;
}

.el-table :deep(.el-table__row) {
  height: 28px !important;
}

.el-table :deep(.el-table__body-wrapper) {
  font-size: 15px !important;
}

.el-table :deep(.el-table__cell) {
  padding: 4px 0 !important;
  height: 28px !important;
}

.el-table :deep(.el-button--small) {
  padding: 4px 8px !important;
  font-size: 12px !important;
  height: auto !important;
}

.el-table :deep(.el-tag--small) {
  padding: 2px 6px !important;
  font-size: 12px !important;
  height: auto !important;
  line-height: 1.2 !important;
}

/* 操作按钮字体增大 */
.el-table :deep(.el-button--mini) {
  font-size: 13px !important;
  padding: 7px 15px;
}

.el-table :deep(.el-button--small) {
  font-size: 13px !important;
  padding: 7px 15px;
}

/* 通用表格内按钮样式 */
.el-table :deep(.el-button) {
  font-size: 13px !important;
  padding: 7px 15px;
}

/* 针对链接类型的按钮 */
.el-table :deep(.el-button.is-link) {
  font-size: 15px !important;
  font-weight: 500;
  padding: 8px 12px;
}

/* 表格内的所有文本元素 */
.el-table :deep(.el-tag) {
  font-size: 14px !important;
}

.el-pagination {
  margin-top: 20px;
  text-align: right;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .stats-row .el-col {
    margin-bottom: 10px;
  }
  
  /* 筛选行在小屏幕上垂直堆叠 */
  .filter-row {
    display: block;
  }
  
  .filter-row .el-col {
    margin-bottom: 10px;
    width: 100% !important;
  }

  .filter-row .action-buttons {
    display: flex;
    justify-content: space-between;
  }
  
  .filter-row .action-buttons > .el-button {
    flex: 1;
    margin: 0 4px 0 0;
  }
  
  .statistic-card {
    height: 100px;
  }
  
  .statistic-card :deep(.el-statistic__content) {
    font-size: 24px;
  }
  
  .statistic-icon {
    font-size: 20px;
  }
}

/* 快捷筛选样式 */
.quick-filter-row {
  margin-bottom: 15px;
  display: flex;
  align-items: center;
}

.filter-label {
  margin-right: 10px;
  font-weight: bold;
  color: #606266;
}

.quick-filter-row .el-button {
  margin-right: 8px;
  margin-bottom: 5px;
}
</style>
