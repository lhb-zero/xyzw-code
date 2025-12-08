<template>
  <div class="player-header">
    <div class="player-info-section">
      <el-avatar :size="80" class="player-avatar">
        {{ getPlayerInitial(playerData.nickname) }}
      </el-avatar>
      <div class="player-info">
        <h2>{{ playerData.nickname }}</h2>
        <p class="player-id">ID: {{ playerData.memberId }}</p>
        <div class="stats-summary">
          <el-statistic title="总战绩数" :value="totalBattles" />
          <el-statistic title="平均KD" :value="avgKD" :precision="2" />
          <el-statistic title="最高KD" :value="maxKD" :precision="2" />
        </div>
      </div>
    </div>
    <div class="filter-section">
      <TimeRangeFilter
        :timeRange="timeRange"
        :lastWeekCount="lastWeekCount"
        :lastMonthCount="lastMonthCount"
        :fourthSundaysCount="fourthSundaysCount"
        :customDateRange="customDateRange"
        @update:timeRange="$emit('update:timeRange', $event)"
        @update:lastWeekCount="$emit('update:lastWeekCount', $event)"
        @update:lastMonthCount="$emit('update:lastMonthCount', $event)"
        @update:fourthSundaysCount="$emit('update:fourthSundaysCount', $event)"
        @update:customDateRange="$emit('update:customDateRange', $event)"
        @timeRangeChange="handleTimeRangeChange"
      />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import TimeRangeFilter from './TimeRangeFilter.vue'

const props = defineProps({
  playerData: {
    type: Object,
    default: () => ({})
  },
  filteredRecords: {
    type: Array,
    default: () => []
  },
  timeRange: {
    type: String,
    default: 'thisWeek'
  },
  lastWeekCount: {
    type: Number,
    default: 0
  },
  lastMonthCount: {
    type: Number,
    default: 0
  },
  fourthSundaysCount: {
    type: Number,
    default: 0
  },
  customDateRange: {
    type: Array,
    default: () => null
  }
})

const emit = defineEmits(['update:timeRange', 'update:lastWeekCount', 'update:lastMonthCount', 'update:fourthSundaysCount', 'update:customDateRange', 'timeRangeChange'])

// 计算属性
const totalBattles = computed(() => {
  return props.filteredRecords.length
})

const avgKD = computed(() => {
  if (props.filteredRecords.length === 0) return 0
  const totalKD = props.filteredRecords.reduce((sum, record) => sum + (record.kdRatio || 0), 0)
  return totalKD / props.filteredRecords.length
})

const maxKD = computed(() => {
  if (props.filteredRecords.length === 0) return 0
  return Math.max(...props.filteredRecords.map(record => record.kdRatio || 0))
})

// 获取玩家姓名首字母
function getPlayerInitial(name) {
  if (!name) return 'U'
  return name.charAt(0).toUpperCase()
}

// 处理时间范围变化
function handleTimeRangeChange(range) {
  emit('timeRangeChange', range)
}
</script>

<style scoped>
.player-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20px;
  margin-bottom: 20px;
}

.player-info-section {
  display: flex;
  align-items: center;
  gap: 20px;
}

.player-avatar {
  background-color: #409EFF;
  color: white;
  font-size: 24px;
  font-weight: bold;
  flex-shrink: 0;
}

.player-info {
  flex: 1;
}

.player-info h2 {
  margin: 0 0 5px 0;
  color: #303133;
}

.player-id {
  margin: 0 0 15px 0;
  color: #909399;
  font-size: 14px;
}

.stats-summary {
  display: flex;
  gap: 30px;
}

.stats-summary :deep(.el-statistic) {
  text-align: center;
}

.stats-summary :deep(.el-statistic__head) {
  color: #909399;
  font-size: 14px;
}

.stats-summary :deep(.el-statistic__content) {
  color: #409EFF;
  font-weight: bold;
}

.filter-section {
  flex-shrink: 0;
}

@media (max-width: 768px) {
  .player-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .stats-summary {
    flex-wrap: wrap;
    gap: 15px;
  }
  
  .filter-section {
    align-items: flex-start;
    width: 100%;
  }
}
</style>