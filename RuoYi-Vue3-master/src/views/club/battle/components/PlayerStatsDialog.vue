<template>
  <el-dialog
    v-model="visible"
    title="盐场个人战绩详情"
    width="90%"
    :before-close="handleClose"
    class="player-stats-dialog"
  >
    <div class="player-stats-container" v-if="playerData">
      <!-- 玩家基本信息 -->
      <PlayerHeader
        :player-data="playerData"
        :filtered-records="filteredRecords"
        v-model:time-range="timeRange"
        v-model:last-week-count="lastWeekCount"
        v-model:last-month-count="lastMonthCount"
        v-model:fourth-sundays-count="fourthSundaysCount"
        v-model:custom-date-range="customDateRange"
        @time-range-change="handleTimeRangeChange"
      />

      <el-divider />

      <!-- 空数据提示 -->
      <div v-if="filteredRecords.length === 0" class="empty-data-container">
        <el-empty description="当前时间范围内暂无战绩数据">
          <template #image>
            <el-icon size="80" color="#909399"><DocumentRemove /></el-icon>
          </template>
          <template #description>
            <p>{{ getEmptyDataMessage() }}</p>
          </template>
          <el-button type="primary" @click="handleTimeRangeChange('all')">查看全部数据</el-button>
        </el-empty>
      </div>

      <!-- 图表区域 -->
      <div class="charts-section" v-if="filteredRecords.length > 0">
        <!-- 趋势图 -->
        <el-card class="chart-card">
          <template #header>
            <div class="chart-header">
              <div class="chart-title-section">
                <span>战绩趋势</span>
                <el-tooltip content="展示KD值、击杀数或死亡数随时间的变化趋势，帮助了解玩家表现的波动情况" placement="top">
                  <el-icon class="help-icon"><QuestionFilled /></el-icon>
                </el-tooltip>
              </div>
              <el-radio-group v-model="trendMetric" size="small">
                <el-radio-button label="kd">KD趋势</el-radio-button>
                <el-radio-button label="kills">击杀趋势</el-radio-button>
                <el-radio-button label="deaths">死亡趋势</el-radio-button>
                <el-radio-button label="comprehensive">综合趋势</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <TrendChart :records="filteredRecords" :trend-metric="trendMetric" />
        </el-card>

        <!-- 新增图表行 -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-card class="chart-card">
              <template #header>
                <div class="chart-header">
                  <div class="chart-title-section">
                    <span>战绩总览</span>
                    <el-tooltip content="统计选定时间范围内的总击杀、死亡、刨地和复活数量，直观展示玩家的整体战绩表现" placement="top">
                      <el-icon class="help-icon"><QuestionFilled /></el-icon>
                    </el-tooltip>
                    <span class="time-range-label">({{ getTimeRangeLabel() }})</span>
                  </div>
                </div>
              </template>
              <BarChart :records="filteredRecords" :time-range-label="getTimeRangeLabel()" />
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card class="chart-card">
              <template #header>
                <div class="chart-header">
                  <div class="chart-title-section">
                    <span>战斗效率分析</span>
                    <el-tooltip content="展示每场战斗的平均击杀、死亡、刨地和复活数据，通过平均值评估玩家的综合战斗效率和稳定性" placement="top">
                      <el-icon class="help-icon"><QuestionFilled /></el-icon>
                    </el-tooltip>
                    <span class="time-range-label">({{ getTimeRangeLabel() }})</span>
                  </div>
                </div>
              </template>
              <EfficiencyChart :records="filteredRecords" :time-range-label="getTimeRangeLabel()" />
            </el-card>
          </el-col>
        </el-row>

        <!-- 第二行图表 -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-card class="chart-card">
              <template #header>
                <div class="chart-header">
                  <span>战斗力趋势</span>
                  <el-tooltip content="综合KD值、击杀数、复活和刨地值计算的综合战斗力指数(0-100)，分数越高表现越佳，用于评估玩家整体战斗能力的变化趋势" placement="top">
                    <el-icon class="help-icon"><QuestionFilled /></el-icon>
                  </el-tooltip>
                </div>
              </template>
              <CombatPowerChart :records="filteredRecords" />
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card class="chart-card">
              <template #header>
                <div class="chart-header">
                  <div class="chart-title-section">
                    <span>与平均值对比</span>
                    <el-tooltip content="对比个人数据与全员平均值，蓝色代表个人数据，橙色代表全员平均水平，帮助评估个人在群体中的相对表现" placement="top">
                      <el-icon class="help-icon"><QuestionFilled /></el-icon>
                    </el-tooltip>
                  </div>
                  <el-select v-model="avgComparisonRange" size="small" @change="updateAvgComparisonData">
                    <el-option label="本周" value="thisWeek" />
                    <el-option label="上周" value="lastWeek" />
                    <el-option label="本月" value="thisMonth" />
                    <el-option label="上月" value="lastMonth" />
                    <el-option label="本季度" value="thisQuarter" />
                    <el-option label="今年" value="thisYear" />
                    <el-option label="全部" value="all" />
                  </el-select>
                </div>
              </template>
              <AvgComparisonChart 
                :records="filteredRecords" 
                :server-avg-data="serverAvgData"
                :avg-comparison-range="avgComparisonRange"
              />
            </el-card>
          </el-col>
        </el-row>
        <!-- 原有图表行 -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-card class="chart-card">
              <template #header>
                <div class="chart-header">
                  <div class="chart-title-section">
                    <span>KD与击杀数关系</span>
                    <el-tooltip content="散点图展示击杀数与KD值的关系，绿色点表示KD≥1的高效战斗，红色点表示KD<1的战斗，帮助识别高效率战斗记录" placement="top">
                      <el-icon class="help-icon"><QuestionFilled /></el-icon>
                    </el-tooltip>
                    <span class="time-range-label">({{ getTimeRangeLabel() }})</span>
                  </div>
                </div>
              </template>
              <ScatterChart :records="filteredRecords" :time-range-label="getTimeRangeLabel()" />
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card class="chart-card">
              <template #header>
                <div class="chart-header">
                  <div class="chart-title-section">
                    <span>KD分布</span>
                    <el-tooltip content="将KD值分为不同区间展示分布情况，帮助了解玩家在不同KD区间的战绩数量和占比" placement="top">
                      <el-icon class="help-icon"><QuestionFilled /></el-icon>
                    </el-tooltip>
                    <span class="time-range-label">({{ getTimeRangeLabel() }})</span>
                  </div>
                </div>
              </template>
              <HistogramChart :records="filteredRecords" :time-range-label="getTimeRangeLabel()" />
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 详细数据表格 -->
      <DataTable 
        :records="filteredRecords"
        :player-data="playerData"
      />
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue'
import { DocumentRemove, QuestionFilled } from '@element-plus/icons-vue'
import { getServerAverages } from '@/api/club/battle'

// 导入组件
import PlayerHeader from './subcomponents/PlayerHeader.vue'
import DataTable from './subcomponents/DataTable.vue'
import TrendChart from './charts/TrendChart.vue'
import BarChart from './charts/BarChart.vue'
import HistogramChart from './charts/HistogramChart.vue'
import EfficiencyChart from './charts/EfficiencyChart.vue'
import ScatterChart from './charts/ScatterChart.vue'
import CombatPowerChart from './charts/CombatPowerChart.vue'
import AvgComparisonChart from './charts/AvgComparisonChart.vue'

// 导入工具函数
import {
  filterRecordsByTimeRange,
  getTimeRangeLabel as getTimeRangeLabelUtil,
  getEmptyDataMessage as getEmptyDataMessageUtil,
  calculateServerAveragesFromData
} from './utils/dataProcessing'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  memberId: {
    type: [Number, String],
    required: true
  },
  nickname: {
    type: String,
    default: ''
  },
  allRecords: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:visible'])

// 响应式数据
const visible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

const playerData = ref(null)
const timeRange = ref('thisWeek')
const customDateRange = ref(null)
const trendMetric = ref('kd')
const avgComparisonRange = ref('thisMonth')
const serverAvgData = ref({
  kd: 0,
  kills: 0,
  deaths: 0,
  digs: 0,
  revives: 0,
  loading: false
})

// 循环计数器
const lastWeekCount = ref(0)
const lastMonthCount = ref(0)
const fourthSundaysCount = ref(0)

// 计算属性
const filteredRecords = computed(() => {
  if (!playerData.value || !playerData.value.records) return []
  
  // 确保记录日期字段正确
  let records = [...playerData.value.records].map(record => ({
    ...record,
    record_date: record.record_date || record.recordDate || record.created_at || record.createdAt
  }))
  
  return filterRecordsByTimeRange(
    records,
    timeRange.value,
    customDateRange.value,
    lastWeekCount.value,
    lastMonthCount.value,
    fourthSundaysCount.value
  )
})

// 监听visible变化，初始化数据
watch(() => props.visible, (newVal) => {
  if (newVal) {
    initializePlayerData()
    nextTick(() => {
      updateAvgComparisonData()
    })
  }
})

// 初始化玩家数据
function initializePlayerData() {
  const playerRecords = props.allRecords.filter(record => 
    record.member_id === props.memberId || record.memberId === props.memberId
  )
  
  playerData.value = {
    memberId: props.memberId,
    nickname: props.nickname,
    records: playerRecords
  }
}

// 获取时间范围标签
function getTimeRangeLabel() {
  return getTimeRangeLabelUtil(
    timeRange.value,
    lastWeekCount.value,
    lastMonthCount.value,
    fourthSundaysCount.value,
    customDateRange.value
  )
}

// 获取空数据提示消息
function getEmptyDataMessage() {
  return getEmptyDataMessageUtil(
    timeRange.value,
    lastWeekCount.value,
    lastMonthCount.value,
    fourthSundaysCount.value
  )
}

// 处理时间范围变化
function handleTimeRangeChange(range) {
  timeRange.value = range
}

// 获取全员平均值数据
async function updateAvgComparisonData() {
  // 设置加载状态
  serverAvgData.value.loading = true
  
  try {
    // 调用API获取全员平均值数据
    const response = await getServerAverages(avgComparisonRange.value)
    
    if (response.data && response.data.code === 200) {
      serverAvgData.value = {
        ...response.data.data,
        loading: false
      }
    } else {
      // 如果API调用失败，使用本地计算的数据
      throw new Error('API返回错误')
    }
  } catch (error) {
    console.error('获取全员平均值数据失败:', error)
    
    // 使用本地计算的数据作为后备
    const localAverages = calculateServerAveragesFromData(props.allRecords, avgComparisonRange.value)
    serverAvgData.value = {
      ...localAverages,
      loading: false
    }
  }
}

// 关闭对话框
function handleClose() {
  visible.value = false
}
</script>

<style scoped>
.player-stats-dialog :deep(.el-dialog) {
  max-height: 90vh;
  overflow: auto;
}

.player-stats-dialog :deep(.el-dialog__body) {
  padding: 20px;
  max-height: calc(90vh - 120px);
  overflow: auto;
}

.player-stats-container {
  width: 100%;
}

.charts-section {
  margin-bottom: 20px;
}

.empty-data-container {
  margin: 20px 0;
  padding: 40px;
  text-align: center;
  background-color: #f9f9f9;
  border-radius: 8px;
  border: 1px dashed #d9d9d9;
}

.chart-card {
  margin-bottom: 20px;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.chart-title-section {
  display: flex;
  align-items: center;
}

.help-icon {
  color: #909399;
  cursor: pointer;
  font-size: 16px;
  margin-left: 8px;
}

.help-icon:hover {
  color: #409EFF;
}

.time-range-label {
  color: #409EFF;
  font-size: 14px;
  font-weight: 500;
  margin-left: 8px;
}
</style>