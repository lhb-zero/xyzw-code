<template>
  <div class="date-filter">
    <el-radio-group v-model="localTimeRange" @change="handleTimeRangeChange">
      <el-radio-button label="thisWeek">本周</el-radio-button>
      <el-radio-button label="lastWeek">上周</el-radio-button>
      <el-radio-button label="thisMonth">本月</el-radio-button>
      <el-radio-button label="lastMonth">上月</el-radio-button>
      <el-radio-button label="fourthSundays">每月第四周日</el-radio-button>
      <el-radio-button label="all">全部时间</el-radio-button>
    </el-radio-group>
    <div class="cycle-indicator" v-if="['lastWeek', 'lastMonth', 'fourthSundays'].includes(localTimeRange)">
      <el-tag size="small" type="info">{{ getCycleIndicatorText() }}</el-tag>
      <div class="cycle-buttons">
        <el-button-group size="small">
          <el-button @click="previousCycle" :disabled="!canGoPrevious()">
            <el-icon><ArrowLeft /></el-icon>
            上一个
          </el-button>
          <el-button @click="nextCycle">
            下一个
            <el-icon><ArrowRight /></el-icon>
          </el-button>
        </el-button-group>
      </div>
    </div>
    <el-date-picker
      v-if="localTimeRange === 'custom'"
      :model-value="customDateRange"
      @update:model-value="handleCustomDateChange"
      type="daterange"
      range-separator="至"
      start-placeholder="开始日期"
      end-placeholder="结束日期"
    />
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import { getCycleIndicatorText as getCycleIndicatorTextUtil, canGoPrevious as canGoPreviousUtil } from '../utils/dateUtils'

const props = defineProps({
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

// 本地状态
const localTimeRange = computed({
  get: () => props.timeRange,
  set: (value) => emit('update:timeRange', value)
})

// 获取循环指示文本
function getCycleIndicatorText() {
  switch (localTimeRange.value) {
    case 'lastWeek':
      return `上周${props.lastWeekCount > 0 ? `(-${props.lastWeekCount})` : ''}`
    case 'lastMonth':
      return `上月${props.lastMonthCount > 0 ? `(-${props.lastMonthCount})` : ''}`
    case 'fourthSundays':
      return `每月第四周日${props.fourthSundaysCount > 0 ? `(-${props.fourthSundaysCount})` : ''}`
    default:
      return ''
  }
}

// 检查是否可以向前循环
function canGoPrevious() {
  if (localTimeRange.value === 'lastWeek') return props.lastWeekCount > 0
  if (localTimeRange.value === 'lastMonth') return props.lastMonthCount > 0
  if (localTimeRange.value === 'fourthSundays') return props.fourthSundaysCount > 0
  return false
}

// 处理时间范围变化
function handleTimeRangeChange(range) {
  localTimeRange.value = range
  
  // 重置所有计数器
  if (range !== 'lastWeek') emit('update:lastWeekCount', 0)
  if (range !== 'lastMonth') emit('update:lastMonthCount', 0)
  if (range !== 'fourthSundays') emit('update:fourthSundaysCount', 0)
  
  // 触发父组件更新
  emit('timeRangeChange', range)
}

// 上一个周期
function previousCycle() {
  if (localTimeRange.value === 'lastWeek' && props.lastWeekCount > 0) {
    emit('update:lastWeekCount', props.lastWeekCount - 1)
  } else if (localTimeRange.value === 'lastMonth' && props.lastMonthCount > 0) {
    emit('update:lastMonthCount', props.lastMonthCount - 1)
  } else if (localTimeRange.value === 'fourthSundays' && props.fourthSundaysCount > 0) {
    emit('update:fourthSundaysCount', props.fourthSundaysCount - 1)
  }
}

// 下一个周期
function nextCycle() {
  if (localTimeRange.value === 'lastWeek') {
    emit('update:lastWeekCount', props.lastWeekCount + 1)
  } else if (localTimeRange.value === 'lastMonth') {
    emit('update:lastMonthCount', props.lastMonthCount + 1)
  } else if (localTimeRange.value === 'fourthSundays') {
    emit('update:fourthSundaysCount', props.fourthSundaysCount + 1)
  }
}

// 处理自定义日期变化
function handleCustomDateChange(dates) {
  emit('update:customDateRange', dates)
  emit('timeRangeChange', 'custom')
}
</script>

<style scoped>
.date-filter {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 10px;
}

.cycle-indicator {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
  margin-top: 5px;
}

.cycle-buttons {
  margin-top: 5px;
}
</style>