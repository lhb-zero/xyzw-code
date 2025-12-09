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
      <!-- 第一行：成员排名趋势图 + 战绩时间趋势图 -->
      <div class="charts-row">
        <div class="chart-card" v-loading="rankingLoading">
          <div class="chart-header">
            <div class="chart-title">🏆 成员战绩排名</div>
            <el-select v-model="rankingType" size="small" @change="refreshRankingData">
              <el-option label="杀敌数" value="kills" />
              <el-option label="死亡数" value="deaths" />
              <el-option label="刨击数" value="digs" />
              <el-option label="KD比例" value="kd" />
            </el-select>
          </div>
          <div class="chart-container" ref="rankingChartRef"></div>
        </div>

        <div class="chart-card" v-loading="trendLoading">
          <div class="chart-header">
            <div class="chart-title">📈 战绩时间趋势</div>
            <el-button-group size="small">
              <el-button :type="trendType === 'kills' ? 'primary' : ''" @click="changeTrendType('kills')">杀敌</el-button>
              <el-button :type="trendType === 'deaths' ? 'primary' : ''" @click="changeTrendType('deaths')">死亡</el-button>
              <el-button :type="trendType === 'kd' ? 'primary' : ''" @click="changeTrendType('kd')">KD</el-button>
            </el-button-group>
          </div>
          <div class="chart-container" ref="trendChartRef"></div>
        </div>
      </div>

      <!-- 第二行：俱乐部贡献榜（复活丹使用最多） -->
      <div class="charts-row">
        <div class="chart-card" v-loading="contributionLoading">
          <div class="chart-header">
            <div class="chart-title">💊 俱乐部贡献榜（复活丹使用最多）</div>
            <div class="chart-subtitle">复活是需要花费金币的，使用越多说明对俱乐部贡献越大</div>
          </div>
          <div class="chart-container" ref="contributionChartRef"></div>
        </div>
      </div>
    </div>

    <!-- 详细数据表格 -->
    <div class="data-table-section">
      <div class="section-header">
        <div class="section-title">📋 详细战绩数据</div>
        <div class="section-actions">
          <el-button size="small" @click="refreshData">
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
          stripe 
          style="width: 100%" 
          max-height="500"
          :default-sort="{ prop: 'totalKills', order: 'descending' }"
        >
          <el-table-column prop="gameId" label="成员" width="120" sortable>
            <template #default="scope">
              <div class="member-cell">
                <el-avatar :size="32" :src="scope.row.avatar">
                  {{ scope.row.gameId ? scope.row.gameId.charAt(0).toUpperCase() : 'U' }}
                </el-avatar>
                <span>{{ scope.row.gameId || '未知' }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="totalKills" label="总杀敌" width="100" sortable />
          <el-table-column prop="totalDeaths" label="总死亡" width="100" sortable />
          <el-table-column prop="totalDigs" label="总刨击" width="100" sortable />
          <el-table-column prop="totalRevives" label="总复活" width="100" sortable />
          <el-table-column prop="avgKdRatio" label="平均KD" width="100" sortable>
            <template #default="scope">
              <span :class="getKdClass(scope.row.avgKdRatio)">
                {{ scope.row.avgKdRatio ? scope.row.avgKdRatio.toFixed(2) : '0.00' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="recordCount" label="记录数" width="100" sortable />
          <el-table-column prop="avgKills" label="场均杀敌" width="100" sortable>
            <template #default="scope">
              {{ scope.row.avgKills ? scope.row.avgKills.toFixed(1) : '0.0' }}
            </template>
          </el-table-column>
          <el-table-column prop="lastActiveTime" label="最后活跃" width="120" sortable>
            <template #default="scope">
              {{ parseTime(scope.row.lastActiveTime, '{m}-{d}') }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" fixed="right">
            <template #default="scope">
              <el-button 
                type="text" 
                size="small" 
                @click="viewMemberDetail(scope.row)"
              >
                详情
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
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup name="BattleStatistics">
import { ref, reactive, onMounted, onUnmounted, nextTick, getCurrentInstance } from 'vue'
import { Refresh, Download } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { getOverview, getRanking, getTimelineStats, getMemberList, getDataSourceStats } from "@/api/club/statistics"
import { parseTime } from '@/utils/ruoyi'

const { proxy } = getCurrentInstance()

// 团别选项
const teamOptions = ref([
  { value: '1团', label: '1团' },
  { value: '2团', label: '2团' },
  { value: '3团', label: '3团' }
])

// 选中的团
const selectedTeam = ref('1团')

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
const rankingType = ref('kills')
const trendType = ref('kills')
const distributionType = ref('member')

// 加载状态
const rankingLoading = ref(false)
const trendLoading = ref(false)
const contributionLoading = ref(false)
const tableLoading = ref(false)

// 图表DOM引用
const rankingChartRef = ref(null)
const trendChartRef = ref(null)
const contributionChartRef = ref(null)

// 图表实例
let rankingChartInstance = null
let trendChartInstance = null
let contributionChartInstance = null

// 表格数据
const tableData = ref([])
const total = ref(0)

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 20,
  teamGroup: selectedTeam.value
})

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
  refreshTrendData()
}

// 刷新排名数据
function refreshRankingData() {
  loadRankingData()
}

// 加载俱乐部贡献榜数据（复活丹使用最多）
function loadContributionData() {
  contributionLoading.value = true
  getRanking({
    teamGroup: selectedTeam.value,
    type: 'revives',
    limit: 10,
    startDate: dateRange.value[0],
    endDate: dateRange.value[1]
  }).then(response => {
    if (response && response.code === 200) {
      console.log('Contribution data:', response)
      nextTick(() => {
        if (!contributionChartInstance && contributionChartRef.value) {
          console.log('Initializing contribution chart')
          contributionChartInstance = echarts.init(contributionChartRef.value)
        } else if (contributionChartRef.value) {
          console.log('Updating existing contribution chart')
        } else {
          console.error('Contribution chart container not found')
        }
        
        const responseData = response.data || {}
        const data = responseData.rankings || []
        const names = data.map(item => item.gameId || `成员${item.memberId}`)
        const values = data.map(item => item.totalRevives || 0)
        
        // 创建渐变色数组
        const colorGradient = values.map((value, index) => {
          const colors = [
            '#e74c3c', // 鲜红
            '#c0392b', // 深红
            '#e67e22', // 橙色
            '#f39c12', // 黄色
            '#27ae60', // 绿色
            '#16a085', // 青色
            '#2980b9', // 蓝色
            '#8e44ad', // 紫色
            '#34495e'  // 灰色
          ];
          return colors[index % colors.length];
        });
        
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
              fontSize: 12
            },
            axisLine: {
              lineStyle: {
                color: '#e0e0e0'
              }
            }
          },
          series: [
            {
              name: '复活丹使用数量',
              type: 'bar',
              data: values.map((value, index) => ({
                value: value,
                itemStyle: {
                  color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
                    { offset: 0, color: colorGradient[index] },
                    { offset: 1, color: colorGradient[index] + '99' } // 添加透明度
                  ]),
                  borderRadius: [0, 6, 6, 0],
                  shadowColor: colorGradient[index] + '33',
                  shadowBlur: 10,
                  shadowOffsetX: 4
                }
              })),
              label: {
                show: true,
                position: 'right',
                formatter: '{c}',
                fontSize: 12,
                fontWeight: '600',
                color: '#e74c3c'
              }
            }
          ]
        }
        
        contributionChartInstance.setOption(option)
        contributionLoading.value = false
      })
    } else {
      console.error('Error response:', response)
      proxy.$modal.msgError(response.msg || '获取贡献榜数据失败')
      contributionLoading.value = false
    }
  }).catch(error => {
    console.error('Error loading contribution data:', error)
    proxy.$modal.msgError('获取贡献榜数据失败')
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

// 查看成员详情
function viewMemberDetail(member) {
  // 这里可以跳转到成员详情页或打开详情对话框
  proxy.$modal.msgSuccess(`查看成员 ${member.gameId} 的详细战绩`)
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
      proxy.$modal.msgError(response.msg || '获取概览数据失败')
    }
    overviewLoading.value = false
  }).catch(error => {
    console.error('Error loading overview data:', error)
    proxy.$modal.msgError('获取概览数据失败')
    overviewLoading.value = false
  })
}

// 加载排名数据
function loadRankingData() {
  rankingLoading.value = true
  getRanking({
    teamGroup: selectedTeam.value,
    type: rankingType.value,
    limit: 10,
    startDate: dateRange.value[0],
    endDate: dateRange.value[1]
  }).then(response => {
    if (response && response.code === 200) {
      console.log('Ranking data:', response)
      nextTick(() => {
        if (!rankingChartInstance && rankingChartRef.value) {
          console.log('Initializing ranking chart')
          rankingChartInstance = echarts.init(rankingChartRef.value)
        } else if (rankingChartRef.value) {
          console.log('Updating existing ranking chart')
        } else {
          console.error('Ranking chart container not found')
        }
      
      const responseData = response.data || {}
      const data = responseData.rankings || []
      const names = data.map(item => item.gameId || `成员${item.memberId}`)
      const values = data.map(item => {
        if (rankingType.value === 'kd') {
          return item.avgKdRatio ? parseFloat(item.avgKdRatio).toFixed(2) : 0
        }
        return item[`total${rankingType.value.charAt(0).toUpperCase() + rankingType.value.slice(1)}`] || 0
      })
      
      // 创建渐变色数组
      const colorGradient = values.map((value, index) => {
        const colors = [
          '#ff6b6b', // 浅红
          '#ff5252', // 中红
          '#ff4757', // 深红
          '#ee5a6f', // 玫瑰红
          '#c44569', // 深玫瑰
          '#f8b500', // 金黄红
          '#ff6348', // 番茄红
          '#e74c3c', // 鲜红
          '#c0392b'  // 深红
        ];
        return colors[index % colors.length];
      });
      
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
                        ${rankingType.value === 'kd' ? 'KD比例' : rankingType.value === 'kills' ? '杀敌数' : 
                          rankingType.value === 'deaths' ? '死亡数' : '刨击数'}: 
                        <span style="color: #ff4757; font-weight: bold;">${data.value}</span>
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
          name: rankingType.value === 'kd' ? 'KD比例' : rankingType.value === 'kills' ? '杀敌数' : 
                rankingType.value === 'deaths' ? '死亡数' : '刨击数'
        },
        yAxis: {
          type: 'category',
          data: names,
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
        series: [
          {
            name: rankingType.value === 'kd' ? 'KD比例' : rankingType.value === 'kills' ? '杀敌数' : 
                  rankingType.value === 'deaths' ? '死亡数' : '刨击数',
            type: 'bar',
            data: values.map((value, index) => ({
              value: value,
              itemStyle: {
                color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
                  { offset: 0, color: colorGradient[index] },
                  { offset: 1, color: colorGradient[index] + '99' } // 添加透明度
                ]),
                borderRadius: [0, 6, 6, 0],
                shadowColor: colorGradient[index] + '33',
                shadowBlur: 10,
                shadowOffsetX: 4
              }
            })),
            label: {
              show: true,
              position: 'right',
              formatter: '{c}',
              fontSize: 12,
              fontWeight: '600',
              color: '#ff4757'
            }
          }
        ]
      }
      
        rankingChartInstance.setOption(option)
        rankingLoading.value = false
      })
    } else {
      console.error('Error response:', response)
      proxy.$modal.msgError(response.msg || '获取排名数据失败')
      rankingLoading.value = false
    }
  }).catch(error => {
    console.error('Error loading ranking data:', error)
    proxy.$modal.msgError('获取排名数据失败')
    rankingLoading.value = false
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
    console.log('Timeline data:', response)
    nextTick(() => {
      if (!trendChartInstance && trendChartRef.value) {
        console.log('Initializing trend chart')
        trendChartInstance = echarts.init(trendChartRef.value)
      } else if (trendChartRef.value) {
        console.log('Updating existing trend chart')
      } else {
        console.error('Trend chart container not found')
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
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: 'rgba(41, 128, 185, 0.4)' },
                { offset: 1, color: 'rgba(41, 128, 185, 0.05)' }
              ])
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
  }).catch(() => {
    trendLoading.value = false
  })
}

// 加载分布数据
function loadDistributionData() {
  distributionLoading.value = true
  
  if (distributionType.value === 'member') {
    // 成员贡献分布
    getRanking({
      teamGroup: selectedTeam.value,
      type: 'kills',
      limit: 10,
      startDate: dateRange.value[0],
      endDate: dateRange.value[1]
  }).then(response => {
    console.log('Distribution data:', response)
    nextTick(() => {
      if (!distributionChartInstance && distributionChartRef.value) {
        console.log('Initializing distribution chart')
        distributionChartInstance = echarts.init(distributionChartRef.value)
      } else if (distributionChartRef.value) {
        console.log('Updating existing distribution chart')
      } else {
        console.error('Distribution chart container not found')
      }
      
        const responseData = response.data || {}
        const data = responseData.rankings || []
        const pieData = data.map(item => ({
          name: item.gameId || `成员${item.memberId}`,
          value: item.totalKills || 0
        }))
        
        const option = {
          tooltip: {
            trigger: 'item',
            formatter: '{a} <br/>{b}: {c} ({d}%)'
          },
          legend: {
            orient: 'vertical',
            left: 'left'
          },
          series: [
            {
              name: '成员贡献分布',
              type: 'pie',
              radius: ['50%', '70%'],
              avoidLabelOverlap: false,
              itemStyle: {
                borderRadius: 10,
                borderColor: '#fff',
                borderWidth: 2
              },
              label: {
                show: false,
                position: 'center'
              },
              emphasis: {
                label: {
                  show: true,
                  fontSize: '20',
                  fontWeight: 'bold'
                }
              },
              labelLine: {
                show: false
              },
              data: pieData
            }
          ]
        }
        
        distributionChartInstance.setOption(option)
        distributionLoading.value = false
      })
    }).catch(() => {
      distributionLoading.value = false
    })
  } else {
    // 日期分布
    getTimelineStats({
      teamGroup: selectedTeam.value,
      startDate: dateRange.value[0],
      endDate: dateRange.value[1]
  }).then(response => {
    console.log('Distribution data:', response)
    nextTick(() => {
      if (!distributionChartInstance && distributionChartRef.value) {
        console.log('Initializing distribution chart')
        distributionChartInstance = echarts.init(distributionChartRef.value)
      } else if (distributionChartRef.value) {
        console.log('Updating existing distribution chart')
      } else {
        console.error('Distribution chart container not found')
      }
      
        const responseData = response.data || {}
        const data = responseData.timeline || []
        const pieData = data.map(item => ({
          name: item.date,
          value: item.recordCount || 0
        }))
        
        const option = {
          tooltip: {
            trigger: 'item',
            formatter: '{a} <br/>{b}: {c} ({d}%)'
          },
          legend: {
            orient: 'vertical',
            left: 'left'
          },
          series: [
            {
              name: '日期分布',
              type: 'pie',
              radius: ['50%', '70%'],
              avoidLabelOverlap: false,
              itemStyle: {
                borderRadius: 10,
                borderColor: '#fff',
                borderWidth: 2
              },
              label: {
                show: false,
                position: 'center'
              },
              emphasis: {
                label: {
                  show: true,
                  fontSize: '20',
                  fontWeight: 'bold'
                }
              },
              labelLine: {
                show: false
              },
              data: pieData
            }
          ]
        }
        
        distributionChartInstance.setOption(option)
        distributionLoading.value = false
      })
    }).catch(() => {
      distributionLoading.value = false
    })
  }
}

// 加载数据来源统计
function loadDataSourceData() {
  dataSourceLoading.value = true
  getDataSourceStats({
    teamGroup: selectedTeam.value,
    startDate: dateRange.value[0],
    endDate: dateRange.value[1]
  }).then(response => {
    console.log('Data source data:', response)
    nextTick(() => {
      if (!dataSourceChartInstance && dataSourceChartRef.value) {
        console.log('Initializing data source chart')
        dataSourceChartInstance = echarts.init(dataSourceChartRef.value)
      } else if (dataSourceChartRef.value) {
        console.log('Updating existing data source chart')
      } else {
        console.error('Data source chart container not found')
      }
      
      const data = response.data || []
      const pieData = data.map(item => ({
        name: item.dataSource === 'ocr' ? 'OCR识别' : '手动输入',
        value: item.count || 0
      }))
      
      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c} ({d}%)'
        },
        color: ['#3498db', '#e74c3c'],
        series: [
          {
            name: '数据来源',
            type: 'pie',
            radius: '60%',
            center: ['50%', '50%'],
            itemStyle: {
              borderRadius: 10,
              borderColor: '#fff',
              borderWidth: 2
            },
            label: {
              formatter: '{b}: {c} ({d}%)',
              fontSize: 14
            },
            data: pieData
          }
        ]
      }
      
      dataSourceChartInstance.setOption(option)
      dataSourceLoading.value = false
    })
  }).catch(() => {
    dataSourceLoading.value = false
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
    console.log('Member list data:', response)
    tableData.value = response.rows || []
    total.value = response.total || 0
    tableLoading.value = false
  }).catch(error => {
    console.error('Error loading member list:', error)
    tableLoading.value = false
  })
}

// 加载所有数据
function loadAllData() {
  loadOverviewData()
  loadRankingData()
  loadTrendData()
  loadContributionData()
  loadTableData()
}

// 刷新所有数据
function refreshData() {
  loadAllData()
}

// 调整图表大小
function resizeCharts() {
  nextTick(() => {
    rankingChartInstance?.resize()
    trendChartInstance?.resize()
    contributionChartInstance?.resize()
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
  
  if (rankingChartInstance) rankingChartInstance.dispose()
  if (trendChartInstance) trendChartInstance.dispose()
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

    &:last-child {
      margin-bottom: 0;
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
      justify-content: space-between;
      align-items: center;

      .chart-title {
        font-size: 16px;
        font-weight: 600;
        color: #1e293b;
        display: flex;
        align-items: center;
        gap: 8px;
      }
    }

    .chart-container {
      height: 300px;
      padding: 16px;
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
    padding: 16px 20px;
    border-bottom: 1px solid #f1f5f9;

    .section-title {
      font-size: 18px;
      font-weight: 600;
      color: #1e293b;
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .section-actions {
      display: flex;
      gap: 8px;
    }
  }

  .table-container {
    padding: 20px;

    .member-cell {
      display: flex;
      align-items: center;
      gap: 8px;

      .el-avatar {
        background: linear-gradient(135deg, #3b82f6, #8b5cf6);
      }
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
    padding: 16px 20px;
    display: flex;
    justify-content: flex-end;
    border-top: 1px solid #f1f5f9;
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