<template>
  <div class="battle-statistics-container">
    <!-- 页面标题和团队选择 -->
    <div class="page-header">
      <div class="page-title">
        <span class="title-icon">⚔️</span>
        <span class="title-text">战绩数据看板</span>
      </div>
      <div class="header-actions">
        <div class="team-tabs">
          <div 
            v-for="team in teamOptions" 
            :key="team.value"
            :class="['team-tab', { active: selectedTeam === team.value }]"
            @click="handleTeamChange(team.value)"
          >
            {{ team.label }}
          </div>
        </div>
        <div class="date-range-selector">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            :shortcuts="dateShortcuts"
            size="default"
            @change="handleDateChange"
          />
        </div>
      </div>
    </div>
    
    <!-- 快捷筛选区域 -->
    <div class="quick-filter-section">
      <div class="quick-filter-title">快捷筛选:</div>
      <div class="quick-filter-buttons">
        <el-button size="small" @click="applyQuickFilter('thisSaturday')">本周六</el-button>
        <el-button size="small" @click="applyQuickFilter('lastSaturday')">上周六</el-button>
        <el-button size="small" @click="applyQuickFilter('fourthSundayThisMonth')">本月第四个周日</el-button>
        <el-button size="small" @click="applyQuickFilter('fourthSundayLastMonth')">上月第四个周日</el-button>
        <el-button size="small" @click="applyQuickFilter('allSaturdaysThisMonth')">本月所有周六</el-button>
        <el-button size="small" @click="applyQuickFilter('allSaturdaysLastMonth')">上月所有周六</el-button>
        <el-button size="small" @click="applyQuickFilter('thisMonth')">本月所有战绩</el-button>
        <el-button size="small" @click="applyQuickFilter('lastMonth')">上月所有战绩</el-button>
        <el-button size="small" type="warning" @click="resetFilters">重置</el-button>
      </div>
    </div>

    <!-- 核心统计卡片 -->
    <div class="stats-grid" v-loading="overviewLoading">
      <div class="stat-card">
        <div class="stat-icon records-icon">📊</div>
        <div class="stat-content">
          <div class="stat-value">{{ formatNumber(overviewData.totalRecords || 0) }}</div>
          <div class="stat-label">总记录数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon kills-icon">⚔️</div>
        <div class="stat-content">
          <div class="stat-value">{{ formatNumber(overviewData.totalKills || 0) }}</div>
          <div class="stat-label">总杀敌数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon deaths-icon">💀</div>
        <div class="stat-content">
          <div class="stat-value">{{ formatNumber(overviewData.totalDeaths || 0) }}</div>
          <div class="stat-label">总死亡数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon kd-icon">📈</div>
        <div class="stat-content">
          <div class="stat-value">{{ overviewData.avgKdRatio ? overviewData.avgKdRatio.toFixed(2) : '0.00' }}</div>
          <div class="stat-label">平均KD比</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon digs-icon">⛏️</div>
        <div class="stat-content">
          <div class="stat-value">{{ formatNumber(overviewData.totalDigs || 0) }}</div>
          <div class="stat-label">总刨击数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon revives-icon">💊</div>
        <div class="stat-content">
          <div class="stat-value">{{ formatNumber(overviewData.totalRevives || 0) }}</div>
          <div class="stat-label">总复活数</div>
        </div>
      </div>
    </div>

    <!-- 数据可视化区域 -->
    <div class="charts-section">
      <!-- 第一行：击杀排行 + KD比例排行 -->
      <div class="charts-row">
        <div class="chart-card" v-loading="killsKingLoading">
          <div class="chart-header">
            <div class="chart-title">👑 击杀排行榜</div>
            <div class="chart-subtitle">
              <span>展示团队中杀敌数最多的成员</span>
              <span class="data-count">{{ killsKingLimit === 0 ? '全部成员' : `前${killsKingLimit}名` }}</span>
            </div>
            <div class="chart-controls">
              <el-select v-model="killsKingLimit" size="small" @change="refreshKillsKingData">
                <el-option label="前10名" :value="10" />
                <el-option label="前20名" :value="20" />
                <el-option label="前30名" :value="30" />
                <el-option label="全部成员" :value="0" />
              </el-select>
            </div>
          </div>
          <div class="chart-container" :class="{ expanded: killsKingLimit === 0 }" ref="killsKingChartRef"></div>
        </div>

        <div class="chart-card" v-loading="kdRankingLoading">
          <div class="chart-header">
            <div class="chart-title">📈 KD比例排行榜</div>
            <div class="chart-subtitle">
              <span>展示团队中KD比例最高的成员</span>
              <span class="data-count">{{ kdRankingLimit === 0 ? '全部成员' : `前${kdRankingLimit}名` }}</span>
            </div>
            <div class="chart-controls">
              <el-select v-model="kdRankingLimit" size="small" @change="refreshKdRankingData">
                <el-option label="前10名" :value="10" />
                <el-option label="前20名" :value="20" />
                <el-option label="前30名" :value="30" />
                <el-option label="全部成员" :value="0" />
              </el-select>
            </div>
          </div>
          <div class="chart-container" :class="{ expanded: kdRankingLimit === 0 }" ref="kdRankingChartRef"></div>
        </div>
      </div>

      <!-- 第二行：刨地排行 + 死亡数排行 -->
      <div class="charts-row">
        <div class="chart-card" v-loading="digsKingLoading">
          <div class="chart-header">
            <div class="chart-title">⛏️ 刨地排行榜</div>
            <div class="chart-subtitle">
              <span>展示团队中刨击数最多的成员</span>
              <span class="data-count">{{ digsKingLimit === 0 ? '全部成员' : `前${digsKingLimit}名` }}</span>
            </div>
            <div class="chart-controls">
              <el-select v-model="digsKingLimit" size="small" @change="refreshDigsKingData">
                <el-option label="前10名" :value="10" />
                <el-option label="前20名" :value="20" />
                <el-option label="前30名" :value="30" />
                <el-option label="全部成员" :value="0" />
              </el-select>
            </div>
          </div>
          <div class="chart-container" :class="{ expanded: digsKingLimit === 0 }" ref="digsKingChartRef"></div>
        </div>

        <div class="chart-card" v-loading="deathsRankingLoading">
          <div class="chart-header">
            <div class="chart-title">💀 死亡数排行榜</div>
            <div class="chart-subtitle">
              <span>展示团队中死亡数最多的成员</span>
              <span class="data-count">{{ deathsRankingLimit === 0 ? '全部成员' : `前${deathsRankingLimit}名` }}</span>
            </div>
            <div class="chart-controls">
              <el-select v-model="deathsRankingLimit" size="small" @change="refreshDeathsRankingData">
                <el-option label="前10名" :value="10" />
                <el-option label="前20名" :value="20" />
                <el-option label="前30名" :value="30" />
                <el-option label="全部成员" :value="0" />
              </el-select>
            </div>
          </div>
          <div class="chart-container" :class="{ expanded: deathsRankingLimit === 0 }" ref="deathsRankingChartRef"></div>
        </div>
      </div>

      <!-- 第三行：俱乐部贡献榜 + 战绩时间趋势 -->
      <div class="charts-row">
        <div class="chart-card" v-loading="contributionLoading">
          <div class="chart-header">
            <div class="chart-title">💊 俱乐部贡献榜（复活丹使用最多）</div>
            <div class="chart-subtitle">
              <span>复活是需要花费金币的，使用越多说明对俱乐部贡献越大</span>
              <span class="data-count">{{ contributionLimit === 0 ? '全部成员' : `前${contributionLimit}名` }}</span>
            </div>
            <div class="chart-controls">
              <el-select v-model="contributionLimit" size="small" @change="refreshContributionData">
                <el-option label="前10名" :value="10" />
                <el-option label="前20名" :value="20" />
                <el-option label="前30名" :value="30" />
                <el-option label="全部成员" :value="0" />
              </el-select>
            </div>
          </div>
          <div class="chart-container" :class="{ expanded: contributionLimit === 0 }" ref="contributionChartRef"></div>
        </div>

        <div class="chart-card" v-loading="trendLoading">
          <div class="chart-header">
            <div class="chart-title">📊 战绩时间趋势</div>
            <div class="chart-subtitle">
              <span>展示团队战绩随时间变化</span>
              <span class="data-count">趋势分析</span>
            </div>
            <el-button-group size="small">
              <el-button :type="trendType === 'kills' ? 'primary' : ''" @click="changeTrendType('kills')">杀敌</el-button>
              <el-button :type="trendType === 'deaths' ? 'primary' : ''" @click="changeTrendType('deaths')">死亡</el-button>
              <el-button :type="trendType === 'kd' ? 'primary' : ''" @click="changeTrendType('kd')">KD</el-button>
            </el-button-group>
          </div>
          <div class="chart-container" ref="trendChartRef"></div>
        </div>
      </div>
    </div>

    <!-- 详细数据表格 -->
    <div class="data-table-section">
      <div class="section-header">
        <div class="header-left">
          <div class="section-title">📋 详细战绩数据</div>
          <div class="section-subtitle">共 <span class="highlight-count">{{ total }}</span> 名成员</div>
        </div>
        <div class="section-actions">
          <el-button size="small" @click="refreshData" :loading="tableLoading">
            <el-icon><Refresh /></el-icon>
            刷新数据
          </el-button>
          <el-button type="primary" size="small" @click="exportData">
            <el-icon><Download /></el-icon>
            导出数据
          </el-button>
        </div>
      </div>
      
      <div class="table-container" v-loading="tableLoading">
        <el-table 
          :data="tableData" 
          style="width: 100%" 
          :default-sort="{ prop: 'totalKills', order: 'descending' }"
          :header-cell-style="{ background: '#f8fafc', color: '#475569', fontWeight: '600' }"
          :row-class-name="getRowClassName"
        >
          <el-table-column type="index" label="#" width="60" align="center" fixed />
          
          <el-table-column prop="gameId" label="成员" min-width="12%" sortable fixed>
            <template #default="scope">
              <div class="member-cell">
                <el-avatar :size="36" :src="scope.row.avatar" class="member-avatar">
                  {{ scope.row.gameId ? scope.row.gameId.charAt(0).toUpperCase() : 'U' }}
                </el-avatar>
                <div class="member-info">
                  <div class="member-name">{{ scope.row.gameId || '未知' }}</div>
                  <div class="member-stats">记录 {{ scope.row.recordCount || 0 }} 场</div>
                </div>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column prop="totalKills" label="总杀敌" min-width="10%" sortable align="center">
            <template #default="scope">
              <span class="stat-value kills-value">{{ formatNumber(scope.row.totalKills || 0) }}</span>
            </template>
          </el-table-column>
          
          <el-table-column prop="totalDeaths" label="总死亡" min-width="10%" sortable align="center">
            <template #default="scope">
              <span class="stat-value deaths-value">{{ formatNumber(scope.row.totalDeaths || 0) }}</span>
            </template>
          </el-table-column>
          
          <el-table-column prop="avgKdRatio" label="KD比例" min-width="9%" sortable align="center">
            <template #default="scope">
              <span :class="['kd-value', getKdClass(scope.row.avgKdRatio)]">
                {{ scope.row.avgKdRatio ? scope.row.avgKdRatio.toFixed(2) : '0.00' }}
              </span>
            </template>
          </el-table-column>
          
          <el-table-column prop="totalDigs" label="总刨击" min-width="10%" sortable align="center">
            <template #default="scope">
              <span class="stat-value digs-value">{{ formatNumber(scope.row.totalDigs || 0) }}</span>
            </template>
          </el-table-column>
          
          <el-table-column prop="totalRevives" label="总复活" min-width="10%" sortable align="center">
            <template #default="scope">
              <span class="stat-value revives-value">{{ formatNumber(scope.row.totalRevives || 0) }}</span>
            </template>
          </el-table-column>
          
          <el-table-column prop="avgKills" label="场均杀敌" min-width="10%" sortable align="center">
            <template #default="scope">
              <span class="avg-stat">{{ scope.row.avgKills ? scope.row.avgKills.toFixed(1) : '0.0' }}</span>
            </template>
          </el-table-column>
          
          <el-table-column prop="lastActiveTime" label="最后活跃" min-width="11%" sortable align="center">
            <template #default="scope">
              <span class="time-cell">{{ parseTime(scope.row.lastActiveTime, '{y}-{m}-{d}') }}</span>
            </template>
          </el-table-column>
          
          <el-table-column label="操作" min-width="8%" fixed="right" align="center">
            <template #default="scope">
              <el-button 
                type="primary"
                link
                size="small" 
                @click="viewMemberDetail(scope.row)"
              >
                查看详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="queryParams.pageNum"
            v-model:page-size="queryParams.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            background
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </div>
    
    <!-- 盐场个人战绩详情弹窗 -->
    <PlayerStatsDialog
      v-model:visible="playerDialogVisible"
      :member-id="selectedMemberId"
      :nickname="selectedMemberName"
      :all-records="allBattleRecords"
    />
  </div>
</template>

<script setup name="BattleStatistics">
import { ref, reactive, onMounted, onUnmounted, nextTick, getCurrentInstance } from 'vue'
import { Refresh, Download } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import PlayerStatsDialog from '@/views/club/battle/components/PlayerStatsDialog.vue'

import { getOverview, getRanking, getTimelineStats, getMemberList, getMemberBattleDetail } from "@/api/club/statistics"
import { parseTime } from '@/utils/ruoyi'
import { getThisSaturday, getLastSaturday, getFourthSundayOfMonth, getFourthSundayOfLastMonth, 
         getAllSaturdaysOfMonth, getAllSaturdaysOfLastMonth, getThisMonthBattleRange, 
         getLastMonthBattleRange, formatDateToYYYYMMDD, getLastWeekRange, getThisWeekRange } from '@/utils/dateFilters'

const { proxy } = getCurrentInstance()

// 团别选项
const teamOptions = ref([
  { value: '1团', label: '1团' },
  { value: '2团', label: '2团' },
  { value: '3团', label: '3团' }
])

// 选中的团
const selectedTeam = ref('1团')

// 循环查询偏移量状态
const filterOffsets = ref({
  lastSaturday: 0,           // 上周六偏移量
  fourthSundayLastMonth: 0,  // 上月第四个周日偏移量
  allSaturdaysLastMonth: 0,  // 上月所有周六偏移量
  lastMonth: 0               // 上月所有战绩偏移量
})

// 日期范围
const dateRange = ref([])
const defaultEndDate = new Date()
const defaultStartDate = new Date()
defaultStartDate.setDate(defaultStartDate.getDate() - 7) // 默认显示最近7天
dateRange.value = [
  parseTime(defaultStartDate, '{y}-{m}-{d}'),
  parseTime(defaultEndDate, '{y}-{m}-{d}')
]

// 日期快捷选项
const dateShortcuts = [
  {
    text: '最近一周',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
      return [start, end]
    },
  },
  {
    text: '最近一个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
      return [start, end]
    },
  },
  {
    text: '最近三个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 90)
      return [start, end]
    },
  },
]

// 总览数据
const overviewData = ref({})
const overviewLoading = ref(false)

// 图表类型选择
const trendType = ref('kills')

// 图表显示数量限制（默认显示全部成员）
const killsKingLimit = ref(0)
const kdRankingLimit = ref(0)
const digsKingLimit = ref(0)
const deathsRankingLimit = ref(0)
const contributionLimit = ref(0)

// 加载状态
const trendLoading = ref(false)
const contributionLoading = ref(false)
const killsKingLoading = ref(false)
const kdRankingLoading = ref(false)
const digsKingLoading = ref(false)
const deathsRankingLoading = ref(false)
const tableLoading = ref(false)

// 图表DOM引用
const trendChartRef = ref(null)
const contributionChartRef = ref(null)
const killsKingChartRef = ref(null)
const kdRankingChartRef = ref(null)
const digsKingChartRef = ref(null)
const deathsRankingChartRef = ref(null)

// 图表实例
let trendChartInstance = null
let contributionChartInstance = null
let killsKingChartInstance = null
let kdRankingChartInstance = null
let digsKingChartInstance = null
let deathsRankingChartInstance = null

// 表格数据
const tableData = ref([])
const total = ref(0)

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 20,
  teamGroup: selectedTeam.value
})

// 盐场个人战绩详情弹窗相关
const playerDialogVisible = ref(false)
const selectedMemberId = ref(null)
const selectedMemberName = ref('')
const allBattleRecords = ref([])

// 格式化数字
function formatNumber(num) {
  if (num === null || num === undefined) return '0'
  return parseFloat(num).toLocaleString()
}

// 根据KD比例获取样式类
function getKdClass(kdRatio) {
  if (!kdRatio) return ''
  const ratio = parseFloat(kdRatio)
  if (ratio >= 2.0) return 'kd-excellent'
  if (ratio >= 1.5) return 'kd-good'
  if (ratio >= 1.0) return 'kd-normal'
  return 'kd-poor'
}

// 根据KD比例获取Tag类型
function getKdTagType(kdRatio) {
  if (!kdRatio) return 'info'
  const ratio = parseFloat(kdRatio)
  if (ratio >= 2.0) return 'success'  // 绿色 - 优秀
  if (ratio >= 1.5) return ''          // 蓝色 - 良好
  if (ratio >= 1.0) return 'warning'   // 黄色 - 一般
  return 'danger'                      // 红色 - 较差
}

// 获取表格行类名
function getRowClassName({ rowIndex }) {
  return rowIndex % 2 === 0 ? 'even-row' : 'odd-row'
}

// 快捷筛选功能
function applyQuickFilter(filterType) {
  let startDate, endDate;
  
  // 增加偏移量并计算对应的日期
  switch (filterType) {
    case 'thisSaturday':
      startDate = getThisSaturday();
      endDate = new Date(startDate);
      // 重置偏移量
      filterOffsets.value.lastSaturday = 0;
      break;
    case 'lastSaturday':
      // 增加偏移量
      filterOffsets.value.lastSaturday += 1;
      // 计算上周六日期（考虑偏移量）
      const baseLastSaturday = getLastSaturday();
      startDate = new Date(baseLastSaturday);
      startDate.setDate(baseLastSaturday.getDate() - (filterOffsets.value.lastSaturday - 1) * 7);
      endDate = new Date(startDate);
      break;
    case 'fourthSundayThisMonth':
      startDate = getFourthSundayOfMonth();
      endDate = new Date(startDate);
      // 重置偏移量
      filterOffsets.value.fourthSundayLastMonth = 0;
      break;
    case 'fourthSundayLastMonth':
      // 增加偏移量
      filterOffsets.value.fourthSundayLastMonth += 1;
      // 计算上月第四个周日（考虑偏移量）
      let targetMonthOffset = -1 - (filterOffsets.value.fourthSundayLastMonth - 1);
      const today = new Date();
      let fsTargetYear = today.getFullYear();
      let fsTargetMonth = today.getMonth() + targetMonthOffset;
      
      // 处理跨年情况
      while (fsTargetMonth < 0) {
        fsTargetMonth += 12;
        fsTargetYear -= 1;
      }
      while (fsTargetMonth > 11) {
        fsTargetMonth -= 12;
        fsTargetYear += 1;
      }
      
      // 获取目标月份的第一天
      const firstDay = new Date(fsTargetYear, fsTargetMonth, 1);
      // 获取目标月份第一个周日
      const firstSunday = new Date(firstDay);
      firstSunday.setDate(firstDay.getDate() + (7 - firstDay.getDay()) % 7);
      
      // 获取第四个周日
      const targetSunday = new Date(firstSunday);
      targetSunday.setDate(firstSunday.getDate() + 21); // 3周后
      
      // 如果第四个周日超出目标月份，则返回最后一个周日
      if (targetSunday.getMonth() !== fsTargetMonth) {
        const lastDay = new Date(fsTargetYear, fsTargetMonth + 1, 0); // 目标月份最后一天
        const lastSunday = new Date(lastDay);
        lastSunday.setDate(lastDay.getDate() - lastDay.getDay());
        startDate = lastSunday;
      } else {
        startDate = targetSunday;
      }
      endDate = new Date(startDate);
      break;
    case 'allSaturdaysThisMonth':
      // 对于本月所有周六，设置日期范围为本月1号到月末
      const thisMonthSaturdays = getAllSaturdaysOfMonth();
      if (thisMonthSaturdays.length > 0) {
        startDate = new Date(thisMonthSaturdays[0]);
        const today = new Date();
        const lastSaturday = new Date(thisMonthSaturdays[thisMonthSaturdays.length - 1]);
        // 如果最后一个周六在今天之后，则结束日期为今天
        endDate = lastSaturday > today ? today : lastSaturday;
      } else {
        // 如果本月还没有周六，则使用默认范围
        const today = new Date();
        startDate = new Date(today.getFullYear(), today.getMonth(), 1);
        endDate = today;
      }
      // 重置偏移量
      filterOffsets.value.allSaturdaysLastMonth = 0;
      break;
    case 'allSaturdaysLastMonth':
      // 增加偏移量
      filterOffsets.value.allSaturdaysLastMonth += 1;
      // 计算上月所有周六（考虑偏移量）
      let saturdayMonthOffset = -1 - (filterOffsets.value.allSaturdaysLastMonth - 1);
      const saturdayToday = new Date();
      let saTargetYear = saturdayToday.getFullYear();
      let saTargetMonth = saturdayToday.getMonth() + saturdayMonthOffset;
      
      // 处理跨年情况
      while (saTargetMonth < 0) {
        saTargetMonth += 12;
        saTargetYear -= 1;
      }
      while (saTargetMonth > 11) {
        saTargetMonth -= 12;
        saTargetYear += 1;
      }
      
      // 获取目标月份第一天
      const saturdayFirstDay = new Date(saTargetYear, saTargetMonth, 1);
      // 获取目标月份最后一个日期
      const saturdayLastDay = new Date(saTargetYear, saTargetMonth + 1, 0);
      
      const saturdays = [];
      // 从目标月份第一天开始查找周六
      const saturdayCurrentDate = new Date(saturdayFirstDay);
      
      // 找到目标月份第一个周六
      while (saturdayCurrentDate.getDay() !== 6 && saturdayCurrentDate <= saturdayLastDay) {
        saturdayCurrentDate.setDate(saturdayCurrentDate.getDate() + 1);
      }
      
      // 收集所有周六
      while (saturdayCurrentDate <= saturdayLastDay) {
        saturdays.push(new Date(saturdayCurrentDate));
        saturdayCurrentDate.setDate(saturdayCurrentDate.getDate() + 7);
      }
      
      if (saturdays.length > 0) {
        startDate = new Date(saturdays[0]);
        endDate = new Date(saturdays[saturdays.length - 1]);
      } else {
        // 如果目标月份没有周六，则使用该月份范围
        startDate = saturdayFirstDay;
        endDate = saturdayLastDay;
      }
      break;
    case 'thisMonth':
      const thisMonthRange = getThisMonthBattleRange();
      startDate = thisMonthRange.startDate;
      endDate = thisMonthRange.endDate;
      // 重置偏移量
      filterOffsets.value.lastMonth = 0;
      break;
    case 'lastMonth':
      // 增加偏移量
      filterOffsets.value.lastMonth += 1;
      // 计算上月范围（考虑偏移量）
      let monthOffset = -1 - (filterOffsets.value.lastMonth - 1);
      const monthToday = new Date();
      let lmTargetYear = monthToday.getFullYear();
      let lmTargetMonth = monthToday.getMonth() + monthOffset;
      
      // 处理跨年情况
      while (lmTargetMonth < 0) {
        lmTargetMonth += 12;
        lmTargetYear -= 1;
      }
      while (lmTargetMonth > 11) {
        lmTargetMonth -= 12;
        lmTargetYear += 1;
      }
      
      // 目标月份第一天
      startDate = new Date(lmTargetYear, lmTargetMonth, 1);
      // 目标月份最后一天
      endDate = new Date(lmTargetYear, lmTargetMonth + 1, 0);
      break;
    default:
      return;
  }
  
  // 设置日期范围并触发数据刷新
  dateRange.value = [
    formatDateToYYYYMMDD(startDate),
    formatDateToYYYYMMDD(endDate)
  ];
  
  // 触发数据刷新
  handleDateChange();
}

// 重置筛选条件
function resetFilters() {
  // 重置所有偏移量
  filterOffsets.value.lastSaturday = 0;
  filterOffsets.value.fourthSundayLastMonth = 0;
  filterOffsets.value.allSaturdaysLastMonth = 0;
  filterOffsets.value.lastMonth = 0;
  
  // 重置日期范围为默认值（最近7天）
  const defaultEndDate = new Date();
  const defaultStartDate = new Date();
  defaultStartDate.setDate(defaultStartDate.getDate() - 7);
  
  dateRange.value = [
    formatDateToYYYYMMDD(defaultStartDate),
    formatDateToYYYYMMDD(defaultEndDate)
  ];
  
  // 触发数据刷新
  handleDateChange();
}

// 团别切换
function handleTeamChange(team) {
  selectedTeam.value = team
  queryParams.teamGroup = team
  queryParams.pageNum = 1
  loadAllData()
}

// 日期范围变化
function handleDateChange(dates) {
  queryParams.pageNum = 1
  loadAllData()
}

// 切换趋势类型
function changeTrendType(type) {
  trendType.value = type
  loadTrendData()
}

// 刷新KD排名数据
function refreshKdRankingData() {
  if (kdRankingChartInstance) {
    kdRankingChartInstance.dispose();
    kdRankingChartInstance = null;
  }
  loadKdRankingData()
}

// 刷新死亡数排名数据
function refreshDeathsRankingData() {
  if (deathsRankingChartInstance) {
    deathsRankingChartInstance.dispose();
    deathsRankingChartInstance = null;
  }
  loadDeathsRankingData()
}

// 刷新击杀王数据
function refreshKillsKingData() {
  // 如果图表实例已存在，先销毁它，确保重新创建时能应用新的高度设置
  if (killsKingChartInstance) {
    killsKingChartInstance.dispose();
    killsKingChartInstance = null;
  }
  loadKillsKingData()
}

// 刷新刨地王数据
function refreshDigsKingData() {
  // 如果图表实例已存在，先销毁它，确保重新创建时能应用新的高度设置
  if (digsKingChartInstance) {
    digsKingChartInstance.dispose();
    digsKingChartInstance = null;
  }
  loadDigsKingData()
}

// 刷新贡献榜数据
function refreshContributionData() {
  // 如果图表实例已存在，先销毁它，确保重新创建时能应用新的高度设置
  if (contributionChartInstance) {
    contributionChartInstance.dispose();
    contributionChartInstance = null;
  }
  loadContributionData()
}

// 加载KD排行榜数据
function loadKdRankingData() {
  kdRankingLoading.value = true
  const limitValue = kdRankingLimit.value === 0 ? 999 : kdRankingLimit.value
  getRanking({
    teamGroup: selectedTeam.value,
    type: 'kd',
    limit: limitValue,
    startDate: dateRange.value[0],
    endDate: dateRange.value[1]
  }).then(response => {
    if (response && response.code === 200) {
      console.log('KD ranking data:', response)
      nextTick(() => {
        if (kdRankingChartInstance) {
          kdRankingChartInstance.dispose();
        }
        
        if (kdRankingChartRef.value) {
          console.log('Initializing KD ranking chart')
          kdRankingChartInstance = echarts.init(kdRankingChartRef.value)
        } else {
          console.error('KD ranking chart container not found')
        }
        
        const responseData = response.data || {}
        const data = responseData.rankings || []
        data.sort((a, b) => (parseFloat(b.avgKdRatio) || 0) - (parseFloat(a.avgKdRatio) || 0))
        const names = data.map(item => item.gameId || `成员${item.memberId}`)
        const values = data.map(item => item.avgKdRatio ? parseFloat(item.avgKdRatio).toFixed(2) : 0)
        
        // 使用统一的蓝色系渐变（第一名最深，最后一名最浅）
        const baseColor = '#3b82f6';
        const getColorByRank = (index, total) => {
          // 从深蓝色到浅蓝色的渐变
          const ratio = index / Math.max(total - 1, 1);
          const colors = [
            '#1e40af', // 深蓝
            '#1e3a8a', // 更深蓝
            '#1d4ed8', // 蓝
            '#2563eb', // 中蓝
            '#3b82f6', // 基础蓝
            '#60a5fa', // 浅蓝
            '#93c5fd', // 更浅蓝
            '#bfdbfe'  // 最浅蓝
          ];
          const colorIndex = Math.floor(ratio * (colors.length - 1));
          return colors[colorIndex];
        };
        
        const totalItems = names.length;
        const calculatedHeight = Math.min(600, Math.max(400, totalItems * 25));
        
        if (kdRankingChartRef.value) {
          kdRankingChartRef.value.style.height = `${calculatedHeight}px`;
        }
        
        const option = {
          tooltip: {
            trigger: 'axis',
            axisPointer: {
              type: 'shadow'
            },
            formatter: function(params) {
              const data = params[0];
              return `<div style="padding: 8px;">
                        <div style="font-weight: bold; margin-bottom: 4px;">${data.name}</div>
                        <div style="color: #666;">
                          KD比例: 
                          <span style="color: #3b82f6; font-weight: bold;">${data.value}</span>
                        </div>
                        <div style="color: #999; font-size: 12px; margin-top: 4px;">
                          📈 KD比例越高，战斗效率越强
                        </div>
                      </div>`;
            }
          },
          grid: {
            left: '3%',
            right: '4%',
            bottom: '3%',
            top: '10%',
            containLabel: true
          },
          xAxis: {
            type: 'value',
            name: 'KD比例',
            nameTextStyle: {
              color: '#666',
              fontWeight: 500
            }
          },
          yAxis: {
            type: 'category',
            data: names,
            axisLabel: {
              color: '#666',
              fontWeight: 500,
              fontSize: 12,
              interval: 0
            },
            axisLine: {
              lineStyle: {
                color: '#e0e0e0'
              }
            },
            inverse: true
          },
          series: [
            {
              name: 'KD比例',
              type: 'bar',
              data: values.map((value, index) => {
                const itemColor = getColorByRank(index, values.length);
                return {
                  value: value,
                  itemStyle: {
                    color: {
                      type: 'linear',
                      x: 0, y: 0, x2: 1, y2: 0,
                      colorStops: [
                        { offset: 0, color: itemColor },
                        { offset: 1, color: itemColor + 'CC' }
                      ]
                    },
                    borderRadius: [0, 6, 6, 0],
                    shadowColor: itemColor + '33',
                    shadowBlur: 10,
                    shadowOffsetX: 4
                  }
                }
              }),
              label: {
                show: true,
                position: 'right',
                formatter: '{c}',
                fontSize: 12,
                fontWeight: '600',
                color: '#3b82f6'
              }
            }
          ]
        }
        
        kdRankingChartInstance.setOption(option)
        
        setTimeout(() => {
          kdRankingChartInstance.resize();
        }, 100);
        
        if (kdRankingLimit.value === 0) {
          const chartContainer = kdRankingChartRef.value;
          const existingInfo = chartContainer.querySelector('.data-info');
          if (existingInfo) {
            existingInfo.remove();
          }
          
          const dataInfo = document.createElement('div');
          dataInfo.className = 'data-info';
          dataInfo.style.cssText = 'text-align: center; color: #64748b; font-size: 12px; margin-top: 8px;';
          dataInfo.textContent = `共显示 ${names.length} 名成员，可滚动查看全部数据`;
          chartContainer.appendChild(dataInfo);
        } else {
          const chartContainer = kdRankingChartRef.value;
          const existingInfo = chartContainer.querySelector('.data-info');
          if (existingInfo) {
            existingInfo.remove();
          }
        }
        
        kdRankingLoading.value = false
      })
    } else {
      console.error('Error response:', response)
      proxy.$modal.msgError(response?.msg || '获取KD排名数据失败')
    }
  }).catch(error => {
    console.error('Error loading KD ranking data:', error)
    proxy.$modal.msgError('获取KD排名数据失败')
  }).finally(() => {
    kdRankingLoading.value = false
  })
}

// 加载死亡数排行榜数据
function loadDeathsRankingData() {
  deathsRankingLoading.value = true
  const limitValue = deathsRankingLimit.value === 0 ? 999 : deathsRankingLimit.value
  getRanking({
    teamGroup: selectedTeam.value,
    type: 'deaths',
    limit: limitValue,
    startDate: dateRange.value[0],
    endDate: dateRange.value[1]
  }).then(response => {
    if (response && response.code === 200) {
      console.log('Deaths ranking data:', response)
      nextTick(() => {
        if (deathsRankingChartInstance) {
          deathsRankingChartInstance.dispose();
        }
        
        if (deathsRankingChartRef.value) {
          console.log('Initializing deaths ranking chart')
          deathsRankingChartInstance = echarts.init(deathsRankingChartRef.value)
        } else {
          console.error('Deaths ranking chart container not found')
        }
        
        const responseData = response.data || {}
        const data = responseData.rankings || []
        data.sort((a, b) => (b.totalDeaths || 0) - (a.totalDeaths || 0))
        const names = data.map(item => item.gameId || `成员${item.memberId}`)
        const values = data.map(item => item.totalDeaths || 0)
        
        // 使用统一的紫色系渐变（第一名最深，最后一名最浅）
        const baseColor = '#8b5cf6';
        const getColorByRank = (index, total) => {
          const ratio = index / Math.max(total - 1, 1);
          const colors = [
            '#581c87', // 深紫
            '#6b21a8', // 更深紫
            '#7c3aed', // 紫
            '#8b5cf6', // 基础紫
            '#a78bfa', // 浅紫
            '#c4b5fd', // 更浅紫
            '#ddd6fe', // 最浅紫
            '#ede9fe'  // 极浅紫
          ];
          const colorIndex = Math.floor(ratio * (colors.length - 1));
          return colors[colorIndex];
        };
        
        const totalItems = names.length;
        const calculatedHeight = Math.min(600, Math.max(400, totalItems * 25));
        
        if (deathsRankingChartRef.value) {
          deathsRankingChartRef.value.style.height = `${calculatedHeight}px`;
        }
        
        const option = {
          tooltip: {
            trigger: 'axis',
            axisPointer: {
              type: 'shadow'
            },
            formatter: function(params) {
              const data = params[0];
              return `<div style="padding: 8px;">
                        <div style="font-weight: bold; margin-bottom: 4px;">${data.name}</div>
                        <div style="color: #666;">
                          死亡数: 
                          <span style="color: #ef4444; font-weight: bold;">${data.value}</span>
                        </div>
                        <div style="color: #999; font-size: 12px; margin-top: 4px;">
                          💀 死亡数反映参与度和战斗积极性
                        </div>
                      </div>`;
            }
          },
          grid: {
            left: '3%',
            right: '4%',
            bottom: '3%',
            top: '10%',
            containLabel: true
          },
          xAxis: {
            type: 'value',
            name: '死亡数',
            nameTextStyle: {
              color: '#666',
              fontWeight: 500
            }
          },
          yAxis: {
            type: 'category',
            data: names,
            axisLabel: {
              color: '#666',
              fontWeight: 500,
              fontSize: 12,
              interval: 0
            },
            axisLine: {
              lineStyle: {
                color: '#e0e0e0'
              }
            },
            inverse: true
          },
          series: [
            {
              name: '死亡数',
              type: 'bar',
              data: values.map((value, index) => {
                const itemColor = getColorByRank(index, values.length);
                return {
                  value: value,
                  itemStyle: {
                    color: {
                      type: 'linear',
                      x: 0, y: 0, x2: 1, y2: 0,
                      colorStops: [
                        { offset: 0, color: itemColor },
                        { offset: 1, color: itemColor + 'CC' }
                      ]
                    },
                    borderRadius: [0, 6, 6, 0],
                    shadowColor: itemColor + '33',
                    shadowBlur: 10,
                    shadowOffsetX: 4
                  }
                }
              }),
              label: {
                show: true,
                position: 'right',
                formatter: '{c}',
                fontSize: 12,
                fontWeight: '600',
                color: '#8b5cf6'
              }
            }
          ]
        }
        
        deathsRankingChartInstance.setOption(option)
        
        setTimeout(() => {
          deathsRankingChartInstance.resize();
        }, 100);
        
        if (deathsRankingLimit.value === 0) {
          const chartContainer = deathsRankingChartRef.value;
          const existingInfo = chartContainer.querySelector('.data-info');
          if (existingInfo) {
            existingInfo.remove();
          }
          
          const dataInfo = document.createElement('div');
          dataInfo.className = 'data-info';
          dataInfo.style.cssText = 'text-align: center; color: #64748b; font-size: 12px; margin-top: 8px;';
          dataInfo.textContent = `共显示 ${names.length} 名成员，可滚动查看全部数据`;
          chartContainer.appendChild(dataInfo);
        } else {
          const chartContainer = deathsRankingChartRef.value;
          const existingInfo = chartContainer.querySelector('.data-info');
          if (existingInfo) {
            existingInfo.remove();
          }
        }
        
        deathsRankingLoading.value = false
      })
    } else {
      console.error('Error response:', response)
      proxy.$modal.msgError(response?.msg || '获取死亡数排名数据失败')
    }
  }).catch(error => {
    console.error('Error loading deaths ranking data:', error)
    proxy.$modal.msgError('获取死亡数排名数据失败')
  }).finally(() => {
    deathsRankingLoading.value = false
  })
}

// 加载击杀王数据
function loadKillsKingData() {
  killsKingLoading.value = true
  // 根据选择的limit值决定实际限制数，如果为0则获取全部成员
  const limitValue = killsKingLimit.value === 0 ? 999 : killsKingLimit.value
  getRanking({
    teamGroup: selectedTeam.value,
    type: 'kills',
    limit: limitValue,
    startDate: dateRange.value[0],
    endDate: dateRange.value[1]
  }).then(response => {
    if (response && response.code === 200) {
      console.log('Kills king data:', response)
      nextTick(() => {
        // 每次都重新创建图表实例，确保高度设置正确应用
        if (killsKingChartInstance) {
          killsKingChartInstance.dispose();
        }
        
        if (killsKingChartRef.value) {
          console.log('Initializing kills king chart')
          killsKingChartInstance = echarts.init(killsKingChartRef.value)
        } else {
          console.error('Kills king chart container not found')
        }
        
        const responseData = response.data || {}
        const data = responseData.rankings || []
        // 降序排序（从高到低）
        data.sort((a, b) => (b.totalKills || 0) - (a.totalKills || 0))
        const names = data.map(item => item.gameId || `成员${item.memberId}`)
        const values = data.map(item => item.totalKills || 0)
        
        // 使用统一的红色系渐变（第一名最深，最后一名最浅）
        const baseColor = '#ef4444';
        const getColorByRank = (index, total) => {
          const ratio = index / Math.max(total - 1, 1);
          const colors = [
            '#991b1b', // 深红
            '#b91c1c', // 更深红
            '#dc2626', // 红
            '#ef4444', // 基础红
            '#f87171', // 浅红
            '#fca5a5', // 更浅红
            '#fecaca', // 最浅红
            '#fee2e2'  // 极浅红
          ];
          const colorIndex = Math.floor(ratio * (colors.length - 1));
          return colors[colorIndex];
        };
        
        const totalItems = names.length;
        const calculatedHeight = Math.min(600, Math.max(400, totalItems * 25));
        
        // 更新容器高度
        if (killsKingChartRef.value) {
          killsKingChartRef.value.style.height = `${calculatedHeight}px`;
        }
        
        const option = {
          tooltip: {
            trigger: 'axis',
            axisPointer: {
              type: 'shadow'
            },
            formatter: function(params) {
              const data = params[0];
              return `<div style="padding: 8px;">
                        <div style="font-weight: bold; margin-bottom: 4px;">${data.name}</div>
                        <div style="color: #666;">
                          击杀数: 
                          <span style="color: #e74c3c; font-weight: bold;">${data.value}</span>
                        </div>
                        <div style="color: #999; font-size: 12px; margin-top: 4px;">
                          👑 击杀数越多，战斗能力越强
                        </div>
                      </div>`;
            }
          },
          grid: {
            left: '3%',
            right: '4%',
            bottom: '3%',
            top: '10%',
            containLabel: true
          },
          xAxis: {
            type: 'value',
            name: '击杀数',
            nameTextStyle: {
              color: '#666',
              fontWeight: 500
            }
          },
          yAxis: {
            type: 'category',
            data: names,
            axisLabel: {
              color: '#666',
              fontWeight: 500,
              fontSize: 12,
              interval: 0
            },
            axisLine: {
              lineStyle: {
                color: '#e0e0e0'
              }
            },
            inverse: true // 降序排列（从高到低，数值大的在上方）
          },
          series: [
            {
              name: '击杀数',
              type: 'bar',
              data: values.map((value, index) => {
                const itemColor = getColorByRank(index, values.length);
                return {
                  value: value,
                  itemStyle: {
                    color: {
                      type: 'linear',
                      x: 0, y: 0, x2: 1, y2: 0,
                      colorStops: [
                        { offset: 0, color: itemColor },
                        { offset: 1, color: itemColor + 'CC' }
                      ]
                    },
                    borderRadius: [0, 6, 6, 0],
                    shadowColor: itemColor + '33',
                    shadowBlur: 10,
                    shadowOffsetX: 4
                  }
                }
              }),
              label: {
                show: true,
                position: 'right',
                formatter: '{c}',
                fontSize: 12,
                fontWeight: '600',
                color: '#ef4444'
              }
            }
          ]
        }
        
        killsKingChartInstance.setOption(option)
        
        // 强制重新渲染图表，确保高度变更生效
        setTimeout(() => {
          killsKingChartInstance.resize();
        }, 100);
        
        // 在图表标题下方添加数据量提示
        if (killsKingLimit.value === 0) {
          const chartContainer = killsKingChartRef.value;
          const existingInfo = chartContainer.querySelector('.data-info');
          if (existingInfo) {
            existingInfo.remove();
          }
          
          const dataInfo = document.createElement('div');
          dataInfo.className = 'data-info';
          dataInfo.style.cssText = 'text-align: center; color: #64748b; font-size: 12px; margin-top: 8px;';
          dataInfo.textContent = `共显示 ${names.length} 名成员，可滚动查看全部数据`;
          chartContainer.appendChild(dataInfo);
        } else {
          const chartContainer = killsKingChartRef.value;
          const existingInfo = chartContainer.querySelector('.data-info');
          if (existingInfo) {
            existingInfo.remove();
          }
        }
        killsKingLoading.value = false
      })
    } else {
      console.error('Error response:', response)
      proxy.$modal.msgError(response?.msg || '获取击杀王数据失败')
    }
  }).catch(error => {
    console.error('Error loading kills king data:', error)
    proxy.$modal.msgError('获取击杀王数据失败')
  }).finally(() => {
    killsKingLoading.value = false
  })
}

// 加载刨地王数据
function loadDigsKingData() {
  digsKingLoading.value = true
  // 根据选择的limit值决定实际限制数，如果为0则获取全部成员
  const limitValue = digsKingLimit.value === 0 ? 999 : digsKingLimit.value
  getRanking({
    teamGroup: selectedTeam.value,
    type: 'digs',
    limit: limitValue,
    startDate: dateRange.value[0],
    endDate: dateRange.value[1]
  }).then(response => {
    if (response && response.code === 200) {
      console.log('Digs king data:', response)
      nextTick(() => {
        // 每次都重新创建图表实例，确保高度设置正确应用
        if (digsKingChartInstance) {
          digsKingChartInstance.dispose();
        }
        
        if (digsKingChartRef.value) {
          console.log('Initializing digs king chart')
          digsKingChartInstance = echarts.init(digsKingChartRef.value)
        } else {
          console.error('Digs king chart container not found')
        }
        
        const responseData = response.data || {}
        const data = responseData.rankings || []
        // 降序排序（从高到低）
        data.sort((a, b) => (b.totalDigs || 0) - (a.totalDigs || 0))
        const names = data.map(item => item.gameId || `成员${item.memberId}`)
        const values = data.map(item => item.totalDigs || 0)
        
        // 使用统一的橙色系渐变（第一名最深，最后一名最浅）
        const baseColor = '#f59e0b';
        const getColorByRank = (index, total) => {
          const ratio = index / Math.max(total - 1, 1);
          const colors = [
            '#b45309', // 深橙
            '#d97706', // 更深橙
            '#f59e0b', // 基础橙
            '#fbbf24', // 浅橙
            '#fcd34d', // 更浅橙
            '#fde68a', // 最浅橙
            '#fef3c7', // 极浅橙
            '#fffbeb'  // 超浅橙
          ];
          const colorIndex = Math.floor(ratio * (colors.length - 1));
          return colors[colorIndex];
        };
        
        const totalItems = names.length;
        const calculatedHeight = Math.min(600, Math.max(400, totalItems * 25));
        
        // 更新容器高度
        if (digsKingChartRef.value) {
          digsKingChartRef.value.style.height = `${calculatedHeight}px`;
        }
        
        const option = {
          tooltip: {
            trigger: 'axis',
            axisPointer: {
              type: 'shadow'
            },
            formatter: function(params) {
              const data = params[0];
              return `<div style="padding: 8px;">
                        <div style="font-weight: bold; margin-bottom: 4px;">${data.name}</div>
                        <div style="color: #666;">
                          刨击数: 
                          <span style="color: #f39c12; font-weight: bold;">${data.value}</span>
                        </div>
                        <div style="color: #999; font-size: 12px; margin-top: 4px;">
                          ⛏️ 刨击数越多，资源收集能力越强
                        </div>
                      </div>`;
            }
          },
          grid: {
            left: '3%',
            right: '4%',
            bottom: '3%',
            top: '10%',
            containLabel: true
          },
          xAxis: {
            type: 'value',
            name: '刨击数',
            nameTextStyle: {
              color: '#666',
              fontWeight: 500
            }
          },
          yAxis: {
            type: 'category',
            data: names,
            axisLabel: {
              color: '#666',
              fontWeight: 500,
              fontSize: 12,
              interval: 0
            },
            axisLine: {
              lineStyle: {
                color: '#e0e0e0'
              }
            },
            inverse: true // 降序排列（从高到低，数值大的在上方）
          },
          series: [
            {
              name: '刨击数',
              type: 'bar',
              data: values.map((value, index) => {
                const itemColor = getColorByRank(index, values.length);
                return {
                  value: value,
                  itemStyle: {
                    color: {
                      type: 'linear',
                      x: 0, y: 0, x2: 1, y2: 0,
                      colorStops: [
                        { offset: 0, color: itemColor },
                        { offset: 1, color: itemColor + 'CC' }
                      ]
                    },
                    borderRadius: [0, 6, 6, 0],
                    shadowColor: itemColor + '33',
                    shadowBlur: 10,
                    shadowOffsetX: 4
                  }
                }
              }),
              label: {
                show: true,
                position: 'right',
                formatter: '{c}',
                fontSize: 12,
                fontWeight: '600',
                color: '#f59e0b'
              }
            }
          ]
        }
        
        digsKingChartInstance.setOption(option)
        
        // 强制重新渲染图表，确保高度变更生效
        setTimeout(() => {
          digsKingChartInstance.resize();
        }, 100);
        
        // 在图表标题下方添加数据量提示
        if (digsKingLimit.value === 0) {
          const chartContainer = digsKingChartRef.value;
          const existingInfo = chartContainer.querySelector('.data-info');
          if (existingInfo) {
            existingInfo.remove();
          }
          
          const dataInfo = document.createElement('div');
          dataInfo.className = 'data-info';
          dataInfo.style.cssText = 'text-align: center; color: #64748b; font-size: 12px; margin-top: 8px;';
          dataInfo.textContent = `共显示 ${names.length} 名成员，可滚动查看全部数据`;
          chartContainer.appendChild(dataInfo);
        } else {
          const chartContainer = digsKingChartRef.value;
          const existingInfo = chartContainer.querySelector('.data-info');
          if (existingInfo) {
            existingInfo.remove();
          }
        }
        digsKingLoading.value = false
      })
    } else {
      console.error('Error response:', response)
      proxy.$modal.msgError(response?.msg || '获取刨地王数据失败')
    }
  }).catch(error => {
    console.error('Error loading digs king data:', error)
    proxy.$modal.msgError('获取刨地王数据失败')
  }).finally(() => {
    digsKingLoading.value = false
  })
}

// 加载俱乐部贡献榜数据（复活丹使用最多）
function loadContributionData() {
  contributionLoading.value = true
  // 根据选择的limit值决定实际限制数，如果为0则获取全部成员
  const limitValue = contributionLimit.value === 0 ? 999 : contributionLimit.value
  getRanking({
    teamGroup: selectedTeam.value,
    type: 'revives',
    limit: limitValue,
    startDate: dateRange.value[0],
    endDate: dateRange.value[1]
  }).then(response => {
    if (response && response.code === 200) {
      console.log('Contribution data:', response)
      nextTick(() => {
        // 每次都重新创建图表实例，确保高度设置正确应用
        if (contributionChartInstance) {
          contributionChartInstance.dispose();
        }
        
        if (contributionChartRef.value) {
          console.log('Initializing contribution chart')
          contributionChartInstance = echarts.init(contributionChartRef.value)
        } else {
          console.error('Contribution chart container not found')
        }
        
        const responseData = response.data || {}
        const data = responseData.rankings || []
        // 降序排序（从高到低）
        data.sort((a, b) => (b.totalRevives || 0) - (a.totalRevives || 0))
        const names = data.map(item => item.gameId || `成员${item.memberId}`)
        const values = data.map(item => item.totalRevives || 0)
        
        // 使用统一的粉色系渐变（第一名最深，最后一名最浅）
        const baseColor = '#ec4899';
        const getColorByRank = (index, total) => {
          const ratio = index / Math.max(total - 1, 1);
          const colors = [
            '#9f1239', // 深粉
            '#be123c', // 更深粉
            '#e11d48', // 粉
            '#ec4899', // 基础粉
            '#f472b6', // 浅粉
            '#f9a8d4', // 更浅粉
            '#fbcfe8', // 最浅粉
            '#fce7f3'  // 极浅粉
          ];
          const colorIndex = Math.floor(ratio * (colors.length - 1));
          return colors[colorIndex];
        };
        
        const totalItems = names.length;
        const calculatedHeight = Math.min(600, Math.max(400, totalItems * 25));
        
        // 更新容器高度
        if (contributionChartRef.value) {
          contributionChartRef.value.style.height = `${calculatedHeight}px`;
        }
        
        const option = {
          tooltip: {
            trigger: 'axis',
            axisPointer: {
              type: 'shadow'
            },
            formatter: function(params) {
              const data = params[0];
              return `<div style="padding: 8px;">
                        <div style="font-weight: bold; margin-bottom: 4px;">${data.name}</div>
                        <div style="color: #666;">
                          复活丹使用数量: 
                          <span style="color: #e74c3c; font-weight: bold;">${data.value}</span>
                        </div>
                        <div style="color: #999; font-size: 12px; margin-top: 4px;">
                          💊 复活需要花费金币，使用越多贡献越大
                        </div>
                      </div>`;
            }
          },
          grid: {
            left: '3%',
            right: '4%',
            bottom: '3%',
            top: '10%',
            containLabel: true
          },
          xAxis: {
            type: 'value',
            name: '复活丹使用数量',
            nameTextStyle: {
              color: '#666',
              fontWeight: 500
            }
          },
          yAxis: {
            type: 'category',
            data: names,
            axisLabel: {
              color: '#666',
              fontWeight: 500,
              fontSize: 12,
              interval: 0
            },
            axisLine: {
              lineStyle: {
                color: '#e0e0e0'
              }
            },
            inverse: true // 降序排列（从高到低，数值大的在上方）
          },
          series: [
            {
              name: '复活丹使用数量',
              type: 'bar',
              data: values.map((value, index) => {
                const itemColor = getColorByRank(index, values.length);
                return {
                  value: value,
                  itemStyle: {
                    color: {
                      type: 'linear',
                      x: 0, y: 0, x2: 1, y2: 0,
                      colorStops: [
                        { offset: 0, color: itemColor },
                        { offset: 1, color: itemColor + 'CC' }
                      ]
                    },
                    borderRadius: [0, 6, 6, 0],
                    shadowColor: itemColor + '33',
                    shadowBlur: 10,
                    shadowOffsetX: 4
                  }
                }
              }),
              label: {
                show: true,
                position: 'right',
                formatter: '{c}',
                fontSize: 12,
                fontWeight: '600',
                color: '#ec4899'
              }
            }
          ]
        }
        
        contributionChartInstance.setOption(option)
        
        // 强制重新渲染图表，确保高度变更生效
        setTimeout(() => {
          contributionChartInstance.resize();
        }, 100);
        
        // 在图表标题下方添加数据量提示
        if (contributionLimit.value === 0) {
          const chartContainer = contributionChartRef.value;
          const existingInfo = chartContainer.querySelector('.data-info');
          if (existingInfo) {
            existingInfo.remove();
          }
          
          const dataInfo = document.createElement('div');
          dataInfo.className = 'data-info';
          dataInfo.style.cssText = 'text-align: center; color: #64748b; font-size: 12px; margin-top: 8px;';
          dataInfo.textContent = `共显示 ${names.length} 名成员，可滚动查看全部数据`;
          chartContainer.appendChild(dataInfo);
        } else {
          const chartContainer = contributionChartRef.value;
          const existingInfo = chartContainer.querySelector('.data-info');
          if (existingInfo) {
            existingInfo.remove();
          }
        }
        contributionLoading.value = false
      })
    } else {
      console.error('Error response:', response)
      proxy.$modal.msgError(response?.msg || '获取贡献榜数据失败')
    }
  }).catch(error => {
    console.error('Error loading contribution data:', error)
    proxy.$modal.msgError('获取贡献榜数据失败')
  }).finally(() => {
    contributionLoading.value = false
  })
}

// 分页大小变化
function handleSizeChange(val) {
  queryParams.pageSize = val
  queryParams.pageNum = 1
  loadTableData()
}

// 当前页变化
function handleCurrentChange(val) {
  queryParams.pageNum = val
  loadTableData()
}

// 查看成员详惁 - 打开盐场个人战绩详情弹窗
async function viewMemberDetail(row) {
  try {
    // 调用API获取该成员的所有战绩记录
    const response = await getMemberBattleDetail({
      memberId: row.memberId,
      teamGroup: selectedTeam.value,
      startDate: dateRange.value[0],
      endDate: dateRange.value[1]
    })
    
    if (response.code === 200) {
      selectedMemberId.value = row.memberId
      selectedMemberName.value = row.gameId
      allBattleRecords.value = response.data || []
      playerDialogVisible.value = true
      
      // 如果没有数据，给出提示
      if (!response.data || response.data.length === 0) {
        proxy.$modal.msgWarning('该成员在当前时间范围内暂无战绩数据')
      }
    } else {
      proxy.$modal.msgError('获取成员战绩数据失败: ' + (response.msg || '未知错误'))
    }
  } catch (error) {
    console.error('获取成员战绩数据失败:', error)
    proxy.$modal.msgError('获取成员战绩数据失败')
  }
}

// 导出数据
function exportData() {
  proxy.download('club/statistics/export', {
    teamGroup: selectedTeam.value,
    startDate: dateRange.value[0],
    endDate: dateRange.value[1]
  }, `${selectedTeam.value}_战绩数据_${parseTime(new Date(), '{y}{m}{d}')}.xlsx`)
}

// 加载总览数据
function loadOverviewData() {
  overviewLoading.value = true
  getOverview({
    teamGroup: selectedTeam.value,
    startDate: dateRange.value[0],
    endDate: dateRange.value[1]
  }).then(response => {
    if (response && response.code === 200) {
      overviewData.value = response.data || {}
      console.log('Overview data:', response)
    } else {
      console.error('Error response:', response)
      proxy.$modal.msgError(response?.msg || '获取概览数据失败')
    }
  }).catch(error => {
    console.error('Error loading overview data:', error)
    proxy.$modal.msgError('获取概览数据失败')
  }).finally(() => {
    overviewLoading.value = false
  })
}

// 加载趋势数据
function loadTrendData() {
  trendLoading.value = true
  getTimelineStats({
    teamGroup: selectedTeam.value,
    startDate: dateRange.value[0],
    endDate: dateRange.value[1]
  }).then(response => {
    if (response && response.code === 200) {
      console.log('Timeline data:', response)
      nextTick(() => {
        if (!trendChartInstance && trendChartRef.value) {
          console.log('Initializing trend chart')
          trendChartInstance = echarts.init(trendChartRef.value)
        } else if (trendChartRef.value) {
          console.log('Updating existing trend chart')
        } else {
          console.error('Trend chart container not found')
          trendLoading.value = false
          return
        }
        
        const responseData = response.data || {}
        const data = responseData.timeline || []
      const dates = data.map(item => item.date)
      const values = data.map(item => {
        if (trendType.value === 'kd') {
          return item.totalDeaths > 0 ? (item.totalKills / item.totalDeaths).toFixed(2) : 0
        }
        return item[`total${trendType.value.charAt(0).toUpperCase() + trendType.value.slice(1)}`] || 0
      })
      
      const option = {
        tooltip: {
          trigger: 'axis',
          formatter: function(params) {
            const data = params[0];
            return `<div style="padding: 8px;">
                      <div style="font-weight: bold; margin-bottom: 4px;">${data.name}</div>
                      <div style="color: #666;">
                        ${trendType.value === 'kd' ? '平均KD' : trendType.value === 'kills' ? '杀敌数' : 
                          trendType.value === 'deaths' ? '死亡数' : '刨击数'}: 
                        <span style="color: #2980b9; font-weight: bold;">${data.value}</span>
                      </div>
                    </div>`;
          }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          top: '10%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: dates,
          axisLabel: {
            color: '#666',
            fontWeight: 500,
            fontSize: 12
          },
          axisLine: {
            lineStyle: {
              color: '#e0e0e0'
            }
          }
        },
        yAxis: {
          type: 'value',
          name: trendType.value === 'kd' ? 'KD比例' : trendType.value === 'kills' ? '杀敌数' : 
                trendType.value === 'deaths' ? '死亡数' : '刨击数',
          nameTextStyle: {
            color: '#666',
            fontWeight: 500
          },
          axisLabel: {
            color: '#666'
          },
          splitLine: {
            lineStyle: {
              color: '#f0f0f0'
            }
          }
        },
        series: [
          {
            name: trendType.value === 'kd' ? '平均KD' : trendType.value === 'kills' ? '杀敌数' : 
                  trendType.value === 'deaths' ? '死亡数' : '刨击数',
            type: 'line',
            data: values,
            smooth: true,
            itemStyle: {
              color: '#2980b9'
            },
            areaStyle: {
              color: {
                type: 'linear',
                x: 0,
                y: 0,
                x2: 0,
                y2: 1,
                colorStops: [
                  { offset: 0, color: 'rgba(41, 128, 185, 0.4)' },
                  { offset: 1, color: 'rgba(41, 128, 185, 0.05)' }
                ]
              }
            },
            lineStyle: {
              width: 3,
              shadowColor: 'rgba(41, 128, 185, 0.3)',
              shadowBlur: 10,
              shadowOffsetY: 5
            }
          }
        ]
      }
      
        trendChartInstance.setOption(option)
        trendLoading.value = false
      })
    } else {
      console.error('Error response:', response)
      proxy.$modal.msgError(response?.msg || '获取趋势数据失败')
      trendLoading.value = false
    }
  }).catch(error => {
    console.error('Error loading trend data:', error)
    proxy.$modal.msgError('获取趋势数据失败')
    trendLoading.value = false
  })
}



// 加载表格数据
function loadTableData() {
  tableLoading.value = true
  getMemberList({
    ...queryParams,
    startDate: dateRange.value[0],
    endDate: dateRange.value[1]
  }).then(response => {
    if (response && response.code === 200) {
      console.log('Member list data:', response)
      tableData.value = response.rows || []
      total.value = response.total || 0
    } else {
      console.error('Error response:', response)
      proxy.$modal.msgError(response?.msg || '获取成员列表失败')
    }
  }).catch(error => {
    console.error('Error loading member list:', error)
    proxy.$modal.msgError('获取成员列表失败')
  }).finally(() => {
    tableLoading.value = false
  })
}

// 加载所有数据
function loadAllData() {
  loadOverviewData()
  loadKillsKingData()
  loadKdRankingData()
  loadDigsKingData()
  loadDeathsRankingData()
  loadContributionData()
  loadTrendData()
  loadTableData()
}

// 刷新所有数据
function refreshData() {
  loadAllData()
}

// 调整图表大小
function resizeCharts() {
  nextTick(() => {
    // 调整所有图表实例大小
    const chartInstances = [
      trendChartInstance,
      killsKingChartInstance,
      kdRankingChartInstance,
      digsKingChartInstance,
      deathsRankingChartInstance,
      contributionChartInstance
    ]
    
    chartInstances.forEach(instance => {
      if (instance) {
        instance.resize()
      }
    })
  })
}

// 页面挂载后加载数据
onMounted(() => {
  console.log('Statistics component mounted')
  
  // 延迟执行以确保DOM完全加载
  setTimeout(() => {
    loadAllData()
  }, 500)
  
  // 监听窗口大小变化，调整图表大小
  window.addEventListener('resize', resizeCharts)
})

// 组件卸载时清理
onUnmounted(() => {
  window.removeEventListener('resize', resizeCharts)
  
  if (trendChartInstance) trendChartInstance.dispose()
  if (killsKingChartInstance) killsKingChartInstance.dispose()
  if (kdRankingChartInstance) kdRankingChartInstance.dispose()
  if (digsKingChartInstance) digsKingChartInstance.dispose()
  if (deathsRankingChartInstance) deathsRankingChartInstance.dispose()
  if (contributionChartInstance) contributionChartInstance.dispose()
})
</script>

<style scoped lang="scss">
.battle-statistics-container {
  padding: 16px;
  background: #f8fafc;
  min-height: calc(100vh - 84px);
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* 快捷筛选区域 */
.quick-filter-section {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid #e2e8f0;
  padding: 16px 20px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.quick-filter-title {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
  white-space: nowrap;
}

.quick-filter-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

:deep(.quick-filter-buttons .el-button) {
  padding: 6px 12px;
  font-size: 12px;
  border-radius: 6px;
  background: #f1f5f9;
  border-color: #e2e8f0;
  color: #475569;
  
  &:hover {
    background: #e2e8f0;
    border-color: #cbd5e1;
    color: #1e293b;
  }
  
  &:focus {
    background: #e2e8f0;
    border-color: #94a3b8;
    color: #1e293b;
  }
}

:deep(.quick-filter-buttons .el-button--warning) {
  background: #fef3c7;
  border-color: #fde68a;
  color: #92400e;
  
  &:hover {
    background: #fde68a;
    border-color: #fcd34d;
    color: #92400e;
  }
  
  &:focus {
    background: #fde68a;
    border-color: #fbbf24;
    color: #92400e;
  }
}

/* 页面头部 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 16px 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid #e2e8f0;

  .page-title {
    display: flex;
    align-items: center;
    gap: 12px;

    .title-icon {
      font-size: 28px;
    }

    .title-text {
      font-size: 20px;
      font-weight: 600;
      color: #1e293b;
    }
  }

  .header-actions {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .team-tabs {
    display: flex;
    gap: 4px;
    background: #f1f5f9;
    padding: 4px;
    border-radius: 12px;

    .team-tab {
      padding: 8px 20px;
      border-radius: 8px;
      font-size: 14px;
      font-weight: 500;
      color: #64748b;
      cursor: pointer;
      transition: all 0.2s ease;
      border: 2px solid transparent;

      &:hover {
        color: #334155;
        background: rgba(59, 130, 246, 0.1);
      }

      &.active {
        background: #3b82f6;
        color: white;
        border-color: #2563eb;
        font-weight: 600;
      }
    }
  }

  .date-range-selector {
    :deep(.el-date-editor) {
      border-radius: 8px;
    }
  }
}

/* 统计卡片网格 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 16px;
  margin-bottom: 20px;

  .stat-card {
    display: flex;
    align-items: center;
    padding: 20px;
    background: white;
    border-radius: 12px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
    border: 1px solid #e2e8f0;
    transition: all 0.3s ease;
    position: relative;
    overflow: hidden;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
    }

    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      height: 4px;
      background: linear-gradient(90deg, #3b82f6, #8b5cf6);
    }

    .stat-icon {
      width: 56px;
      height: 56px;
      border-radius: 16px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 16px;
      font-size: 24px;
      background: linear-gradient(135deg, #f8fafc, #e2e8f0);
      border: 2px solid #e2e8f0;

      &.records-icon {
        background: linear-gradient(135deg, #dbeafe, #bfdbfe);
        border-color: #93c5fd;
      }

      &.kills-icon {
        background: linear-gradient(135deg, #dcfce7, #bbf7d0);
        border-color: #86efac;
      }

      &.deaths-icon {
        background: linear-gradient(135deg, #fee2e2, #fecaca);
        border-color: #fca5a5;
      }

      &.kd-icon {
        background: linear-gradient(135deg, #e0e7ff, #c7d2fe);
        border-color: #a5b4fc;
      }

      &.digs-icon {
        background: linear-gradient(135deg, #fef3c7, #fde68a);
        border-color: #fcd34d;
      }

      &.revives-icon {
        background: linear-gradient(135deg, #e9d5ff, #d8b4fe);
        border-color: #c084fc;
      }
    }

    .stat-content {
      flex: 1;

      .stat-value {
        font-size: 24px;
        font-weight: 700;
        color: #0f172a;
        margin-bottom: 4px;
        line-height: 1.2;
      }

      .stat-label {
        font-size: 13px;
        color: #64748b;
        font-weight: 500;
      }
    }
  }
}

/* 图表区域 */
.charts-section {
  margin-bottom: 20px;

  .charts-row {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 16px;
    margin-bottom: 16px;
    align-items: start; /* 确保行高由内容决定，而不是强制拉伸 */

    &:last-child {
      margin-bottom: 0;
    }
    
    &.contribution-row {
      grid-template-columns: 1fr;
    }
  }

  .chart-card {
    background: white;
    border-radius: 12px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
    border: 1px solid #e2e8f0;
    overflow: hidden;

    .chart-header {
      padding: 16px 20px;
      border-bottom: 1px solid #f1f5f9;
      display: flex;
      flex-direction: column;
      gap: 4px;

      .chart-title {
        font-size: 16px;
        font-weight: 600;
        color: #1e293b;
        display: flex;
        align-items: center;
        gap: 8px;
      }
      
      .chart-subtitle {
        font-size: 12px;
        color: #64748b;
        margin-top: 2px;
        display: flex;
        justify-content: space-between;
        align-items: center;
      }
      
      .data-count {
        font-size: 11px;
        color: #94a3b8;
        background: #f1f5f9;
        padding: 2px 8px;
        border-radius: 12px;
      }
      
      .chart-controls {
        display: flex;
        gap: 12px;
        margin-top: 8px;
        
        .el-select {
          width: 120px;
        }
      }
    }

    .chart-container {
      min-height: 300px;
      padding: 16px;
      transition: all 0.3s ease;
      overflow-y: auto;
      scrollbar-width: thin;
      scrollbar-color: #cbd5e1 #f1f5f9;
      
      &::-webkit-scrollbar {
        width: 6px;
      }
      
      &::-webkit-scrollbar-track {
        background: #f1f5f9;
        border-radius: 3px;
      }
      
      &::-webkit-scrollbar-thumb {
        background: #cbd5e1;
        border-radius: 3px;
        
        &:hover {
          background: #94a3b8;
        }
      }
    }
  }
}

/* 数据表格区域 */
.data-table-section {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid #e2e8f0;
  overflow: hidden;

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px 24px;
    border-bottom: 2px solid #f1f5f9;
    background: linear-gradient(135deg, #f8fafc 0%, #ffffff 100%);

    .header-left {
      display: flex;
      align-items: baseline;
      gap: 12px;
    }

    .section-title {
      font-size: 20px;
      font-weight: 700;
      color: #1e293b;
      display: flex;
      align-items: center;
      gap: 8px;
    }
    
    .section-subtitle {
      font-size: 14px;
      color: #64748b;
      font-weight: 500;
      
      .highlight-count {
        color: #3b82f6;
        font-weight: 700;
        font-size: 16px;
      }
    }

    .section-actions {
      display: flex;
      gap: 10px;
    }
  }

  .table-container {
    padding: 16px;

    // 表格行样式
    :deep(.even-row) {
      background-color: #f9fafb;
    }

    :deep(.odd-row) {
      background-color: #ffffff;
    }

    :deep(.el-table) {
      font-size: 13px;
      
      th {
        font-weight: 600;
        text-transform: uppercase;
        font-size: 11px;
        letter-spacing: 0.5px;
      }
      
      td {
        padding: 8px 0;
      }
      
      .el-table__body tr:hover > td {
        background-color: #eff6ff !important;
      }
    }

    .member-cell {
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 4px 0;

      .member-avatar {
        width: 30px;
        height: 30px;
        line-height: 30px;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        font-weight: 700;
        font-size: 12px;
        box-shadow: 0 1px 4px rgba(102, 126, 234, 0.3);
      }
      
        .member-info {
          display: flex;
          flex-direction: column;
          gap: 1px;
        }
        
        .member-name {
          font-weight: 600;
          color: #1e293b;
          font-size: 13px;
        }
        
        .member-stats {
          font-size: 10px;
          color: #94a3b8;
        }
      }
    
    .stat-value {
      font-weight: 600;
      font-size: 13px;
      
      &.kills-value {
        color: #dc2626;
      }
      
      &.deaths-value {
        color: #7c3aed;
      }
      
      &.digs-value {
        color: #d97706;
      }
      
      &.revives-value {
        color: #be185d;
      }
    }
    
    .kd-value {
      font-weight: 700;
      font-size: 13px;
    }
    
    .avg-stat {
      font-weight: 600;
      color: #3b82f6;
      font-size: 13px;
    }
    
    .time-cell {
      color: #64748b;
      font-size: 12px;
    }

    .kd-excellent {
      color: #22c55e;
      font-weight: 700;
    }

    .kd-good {
      color: #84cc16;
      font-weight: 600;
    }

    .kd-normal {
      color: #3b82f6;
      font-weight: 500;
    }

    .kd-poor {
      color: #ef4444;
      font-weight: 600;
    }
  }

  .pagination-container {
    padding: 12px 16px;
    display: flex;
    justify-content: flex-end;
    border-top: 2px solid #f1f5f9;
    background: #f8fafc;
    
    :deep(.el-pagination) {
      .btn-prev,
      .btn-next,
      .el-pager li {
        font-weight: 600;
      }
    }
  }
}

/* 响应式设计 */
@media screen and (max-width: 1200px) {
  .charts-section .charts-row {
    grid-template-columns: 1fr;
  }
}

@media screen and (max-width: 768px) {
  .battle-statistics-container {
    padding: 12px;
  }

  .page-header {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;

    .header-actions {
      flex-direction: column;
      gap: 12px;
    }

    .team-tabs {
      justify-content: center;
    }
  }

  .quick-filter-section {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }

  .quick-filter-title {
    text-align: center;
  }

  .quick-filter-buttons {
    justify-content: center;
  }

  .stats-grid {
    grid-template-columns: 1fr;
    gap: 12px;

    .stat-card {
      padding: 16px;

      .stat-icon {
        width: 48px;
        height: 48px;
        font-size: 20px;
      }

      .stat-content .stat-value {
        font-size: 20px;
      }
    }
  }
}
</style>