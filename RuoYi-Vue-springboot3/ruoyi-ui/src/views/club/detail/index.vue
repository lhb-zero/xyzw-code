<template>
  <div class="club-detail-container">
    <!-- 页面标题和团队选择 -->
    <div class="page-header">
      <div class="page-title">
        <span class="title-icon">🏆</span>
        <span class="title-text">俱乐部数据中心</span>
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
        <div class="quick-actions">
          <div class="action-card" @click="goToMemberList">
            <div class="action-icon">👥</div>
            <div class="action-text">
              <div class="action-title">成员管理</div>
              <div class="action-desc">查看和管理俱乐部成员</div>
            </div>
            <div class="action-arrow">→</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 核心统计卡片 -->
    <div class="stats-grid" v-loading="overviewLoading">
      <div class="stat-card">
        <div class="stat-icon team-icon">👥</div>
        <div class="stat-content">
          <div class="stat-value">{{ teamOverview.memberCount || 0 }}</div>
          <div class="stat-label">团队人数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon power-icon">⚔️</div>
        <div class="stat-content">
          <div class="stat-value">{{ formatPower(teamOverview.totalPower || 0) }}</div>
          <div class="stat-label">总战力</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon refine-icon">🔥</div>
        <div class="stat-content">
          <div class="stat-value">{{ formatNumber(teamOverview.totalRedRefine || 0) }}</div>
          <div class="stat-label">总红淬炼</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon avg-icon">📊</div>
        <div class="stat-content">
          <div class="stat-value">{{ formatNumber(teamOverview.avgRedRefine || 0) }}</div>
          <div class="stat-label">平均实力</div>
        </div>
      </div>
    </div>

    <!-- 数据可视化区域 - 2x2网格布局 -->
    <div class="charts-grid">
      <!-- 第一行：阵容分布 + 战力分布 -->
      <div class="charts-row">
        <div class="chart-card" v-loading="lineupChartLoading">
          <div class="chart-header">
            <div class="chart-title">🛡️ 阵容分布</div>
          </div>
          <div class="chart-container" ref="lineupDistributionChart"></div>
        </div>

        <div class="chart-card" v-loading="powerChartLoading">
          <div class="chart-header">
            <div class="chart-title">⚡ 战力分布</div>
          </div>
          <div class="chart-container" ref="powerDistributionChart"></div>
        </div>
      </div>

      <!-- 第二行：红淬炼分布 + 四圣分布 -->
      <div class="charts-row">
        <div class="chart-card" v-loading="refineChartLoading">
          <div class="chart-header">
            <div class="chart-title">🔥 红淬炼分布</div>
          </div>
          <div class="chart-container" ref="redRefineDistributionChart"></div>
        </div>

        <div class="chart-card" v-loading="fourSacredChartLoading">
          <div class="chart-header">
            <div class="chart-title">⭐ 四圣分布</div>
          </div>
          <div class="chart-container" ref="fourSacredChart"></div>
        </div>
      </div>
    </div>

    <!-- 方案预览区域 - 成员展示 -->
    <div class="member-preview-section">
      <div class="section-header">
        <div class="section-title">👥 Top10 - 红淬数展示</div>
        <div class="section-actions">
          <el-button-group>
            <el-button :type="displayMode === 'cards' ? 'primary' : ''" size="small" @click="displayMode = 'cards'">
              卡片模式
            </el-button>
            <el-button :type="displayMode === 'table' ? 'primary' : ''" size="small" @click="displayMode = 'table'">
              表格模式
            </el-button>
          </el-button-group>
        </div>
      </div>

      <!-- 卡片模式展示 -->
      <div v-if="displayMode === 'cards'" class="member-cards-grid" v-loading="memberListLoading">
      <div v-for="(member, index) in topMembers" :key="member.gameId" class="member-card" :class="getCardPowerClass(member.redRefine)">
        <div class="member-rank">
          <span class="rank-number" :class="getRankClass(index)">{{ index + 1 }}</span>
        </div>
        <div class="member-avatar">
          <div class="avatar-placeholder" :class="getLineupColorClass(member.mainLineup)">
            {{ getLineupShortName(member.mainLineup) }}
          </div>
        </div>
        <div class="member-info">
          <div class="member-name">{{ member.gameId }}</div>
          <div class="member-stats">
            <div class="stat-item">
              <span class="stat-label">战力</span>
              <span class="stat-value power-stat">{{ formatPower(member.power || 0) }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">红</span>
              <span class="stat-value red-stat">{{ formatNumber(member.redRefine || 0) }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">四圣</span>
              <span class="stat-value sacred-stat">{{ member.fourSacred || 0 }}</span>
            </div>
          </div>
          <div class="member-lineup">
            <el-tag :type="getLineupTagType(member.mainLineup)" size="small">
              {{ getLineupLabel(member.mainLineup) }}
            </el-tag>
          </div>
        </div>
      </div>
      </div>

      <!-- 表格模式展示（简化版） -->
      <div v-else class="member-table-preview" v-loading="memberListLoading">
        <el-table :data="topMembers" stripe class="preview-table">
          <el-table-column label="排名" width="80" align="center">
            <template #default="scope">
              <span class="rank-badge" :class="getRankClass(scope.$index)">
                {{ scope.$index + 1 }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="gameId" label="游戏昵称/ID" />
          <el-table-column prop="power" label="战力值" sortable>
            <template #default="scope">
              <span class="power-value">{{ formatPower(scope.row.power || 0) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="redRefine" label="红淬炼" sortable>
            <template #default="scope">
              <span class="refine-value">{{ formatNumber(scope.row.redRefine || 0) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="fourSacred" label="四圣" sortable>
            <template #default="scope">
              <span class="fourSacred-value">{{ scope.row.fourSacred || 0 }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="mainLineup" label="主C阵容">
            <template #default="scope">
              <el-tag :type="getLineupTagType(scope.row.mainLineup)" size="small">
                {{ getLineupLabel(scope.row.mainLineup) }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 底部操作区 -->
      <div class="preview-actions">
        <div class="action-info">
          <span class="info-text">当前显示前 {{ topMembers.length }} 名核心成员</span>
          <span class="divider">|</span>
          <span class="info-text">团队总计 {{ total }} 人</span>
        </div>
        <div class="action-buttons">
          <el-button type="primary" @click="goToMemberList">
            <el-icon><User /></el-icon>
            查看完整成员列表
          </el-button>
          <el-button @click="exportTeamData">
            <el-icon><Download /></el-icon>
            导出数据
          </el-button>
        </div>
      </div>
    </div>

    <!-- 成员详情对话框 -->
    <el-dialog v-model="memberDetailVisible" :title="memberDetailTitle" width="600px">
      <el-descriptions v-if="currentMember" :column="2" border>
        <el-descriptions-item label="游戏昵称/ID">{{ currentMember.gameId }}</el-descriptions-item>
        <el-descriptions-item label="战力值">{{ formatPower(currentMember.power || 0) }}</el-descriptions-item>
        <el-descriptions-item label="原俱乐部">{{ currentMember.server }}</el-descriptions-item>
        <el-descriptions-item label="团别">{{ currentMember.teamGroup }}</el-descriptions-item>
        <el-descriptions-item label="四圣数量">{{ currentMember.fourSacred }}</el-descriptions-item>
        <el-descriptions-item label="红淬炼">{{ formatNumber(currentMember.redRefine || 0) }}</el-descriptions-item>
        <el-descriptions-item label="主C阵容">{{ getLineupLabel(currentMember.mainLineup) }}</el-descriptions-item>
        <el-descriptions-item label="加入日期">{{ parseTime(currentMember.joinDate, '{y}-{m}-{d}') }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup name="ClubDetail">
import { ref, reactive, onMounted, nextTick, getCurrentInstance, computed } from 'vue'
const { proxy } = getCurrentInstance()
import { getDetail, getPowerDistribution, getRedRefineDistribution, getFourSacredDistribution } from "@/api/club/detail"
import { listMember } from "@/api/club/member"
import * as echarts from 'echarts'
import { User, TrendCharts, Star, DataAnalysis, Download } from '@element-plus/icons-vue'

// 团别选项
const teamOptions = ref([
  { value: '1团', label: '1团' },
  { value: '2团', label: '2团' },
  { value: '3团', label: '3团' }
])

// 选中的团
const selectedTeam = ref('1团')

// 团队概览数据
const teamOverview = ref({})
const overviewLoading = ref(false)

// 成员列表数据
const memberList = ref([])
const total = ref(0)
const memberListLoading = ref(false)

// 成员详情
const memberDetailVisible = ref(false)
const memberDetailTitle = ref('')
const currentMember = ref(null)

// 显示模式：'cards' | 'table'
const displayMode = ref('cards')

// 核心成员（前10名，按红淬炼数量排序）
const topMembers = computed(() => {
  return (memberList.value || [])
    .slice(0, 10)
    .sort((a, b) => (b.redRefine || 0) - (a.redRefine || 0))
})

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 30,
  teamGroup: selectedTeam.value
})

// 图表加载状态
const lineupChartLoading = ref(false)
const powerChartLoading = ref(false)
const refineChartLoading = ref(false)
const fourSacredChartLoading = ref(false)

// 图表实例
const lineupDistributionChart = ref(null)
const powerDistributionChart = ref(null)
const redRefineDistributionChart = ref(null)
const fourSacredChart = ref(null)

let lineupChartInstance = null
let powerChartInstance = null
let redRefineChartInstance = null
let fourSacredChartInstance = null

// 格式化数字
function formatNumber(num) {
  if (num === null || num === undefined) return '0'
  return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

// 格式化战力值，添加"亿"单位
function formatPower(power) {
  if (power === null || power === undefined) return '0'
  return power.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',') + '亿'
}

// 获取阵容标签
function getLineupLabel(value) {
  if (!value) return '-'
  const lineupMap = {
    '0': '吴国',
    '1': '合力赵云',
    '2': '三蜀赵云',
    '3': '典韦',
    '4': '姜维',
    '5': '关羽',
    '6': '司马懿',
    '7': '毒爆'
  }
  return lineupMap[value] || value
}

// 获取阵容标签类型
function getLineupTagType(value) {
  if (!value) return 'info'
  const typeMap = {
    '0': 'danger',
    '1': 'warning',
    '2': 'success',
    '3': 'info',
    '4': 'primary',
    '5': 'primary',
    '6': 'danger',
    '7': 'warning'
  }
  return typeMap[value] || 'info'
}

// 获取排名样式类
function getRankClass(index) {
  if (index === 0) return 'rank-gold'
  if (index === 1) return 'rank-silver'
  if (index === 2) return 'rank-bronze'
  return 'rank-normal'
}

// 获取阵容简称
function getLineupShortName(value) {
  if (!value) return '无'
  const shortNameMap = {
    '0': '吴国',
    '1': '赵云', 
    '2': '蜀',
    '3': '典韦',
    '4': '姜维',
    '5': '关羽',
    '6': '司马',
    '7': '毒爆'
  }
  return shortNameMap[value] || value
}

// 根据红淬炼数量获取头像样式类
function getAvatarClass(redRefine) {
  const refine = parseInt(redRefine) || 0
  if (refine >= 50) return 'avatar-high'
  if (refine >= 40) return 'avatar-medium'
  return 'avatar-low'
}

// 根据阵容获取颜色类
function getLineupColorClass(value) {
  if (!value) return 'lineup-gray'
  const colorMap = {
    '0': 'lineup-red',    // 吴国 - 红色
    '1': 'lineup-green',   // 赵云 - 绿色
    '2': 'lineup-green',   // 蜀 - 绿色
    '3': 'lineup-blue',    // 典韦 - 蓝色
    '4': 'lineup-green',   // 姜维 - 绿色
    '5': 'lineup-green',   // 关羽 - 绿色
    '6': 'lineup-blue',    // 司马 - 蓝色
    '7': 'lineup-gold'     // 毒爆 - 金色
  }
  return colorMap[value] || 'lineup-gray'
}

// 根据红淬炼数量获取卡片样式类
function getCardPowerClass(redRefine) {
  const refine = parseInt(redRefine) || 0
  if (refine >= 50) return 'card-high'
  if (refine >= 40) return 'card-medium'
  return 'card-low'
}

// 团别切换
function handleTeamChange(team) {
  selectedTeam.value = team
  queryParams.teamGroup = team
  queryParams.pageNum = 1
  loadTeamData()
  getMemberList()
}

// 加载团队数据
function loadTeamData() {
  overviewLoading.value = true
  getDetail(selectedTeam.value).then(response => {
    teamOverview.value = response.data
    overviewLoading.value = false
    
    // 更新图表
    updateLineupChart(response.data.lineupStats)
  }).catch(() => {
    overviewLoading.value = false
  })
  
  // 加载战力分布数据
  loadPowerDistribution()
  
  // 加载红淬炼分布数据
  loadRefineDistribution()
  
  // 加载四圣数量分布数据
  loadFourSacredDistribution()
}

// 更新阵容分布图
function updateLineupChart(data) {
  lineupChartLoading.value = true
  nextTick(() => {
    if (!lineupChartInstance) {
      lineupChartInstance = echarts.init(lineupDistributionChart.value)
    }
    
    const option = {
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c} ({d}%)'
      },
      legend: {
        orient: 'vertical',
        left: 10
      },
      series: [
        {
          name: '阵容分布',
          type: 'pie',
          radius: ['50%', '70%'],
          center: ['60%', '50%'],
          data: data ? data.map(item => ({
            value: item.count,
            name: item.lineupLabel,
            label: {
              show: true,
              position: 'outside',
              formatter: '{b}\n{c}人 ({d}%)',
              fontSize: 12,
              fontWeight: 'bold'
            },
            labelLine: {
              show: true,
              length: 15,
              length2: 10
            }
          })) : [],
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }
      ]
    }
    
    lineupChartInstance.setOption(option)
    lineupChartLoading.value = false
  })
}

// 加载战力分布数据
function loadPowerDistribution() {
  powerChartLoading.value = true
  getPowerDistribution(selectedTeam.value).then(response => {
    nextTick(() => {
      if (!powerChartInstance) {
        powerChartInstance = echarts.init(powerDistributionChart.value)
      }
      
      const data = response.data || []
      const categories = data.map(item => item.teamGroup)
      const values = data.map(item => item.memberCount)
      
      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: categories
        },
        yAxis: {
          type: 'value',
          name: '人数'
        },
        series: [
          {
            name: '战力分布',
            type: 'bar',
            data: values,
            itemStyle: {
              color: '#409EFF'
            },
            label: {
              show: true,
              position: 'top',
              formatter: '{c}',
              fontSize: 14,
              fontWeight: 'bold',
              color: '#333'
            }
          }
        ]
      }
      
      powerChartInstance.setOption(option)
      powerChartLoading.value = false
    })
  }).catch(() => {
    powerChartLoading.value = false
  })
}

// 加载红淬炼分布数据
function loadRefineDistribution() {
  refineChartLoading.value = true
  getRedRefineDistribution(selectedTeam.value).then(response => {
    nextTick(() => {
      if (!redRefineChartInstance) {
        redRefineChartInstance = echarts.init(redRefineDistributionChart.value)
      }
      
      const data = response.data || []
      const categories = data.map(item => item.teamGroup)
      const values = data.map(item => item.memberCount)
      
      // 创建渐变色数组，红淬炼数量越多颜色越深
      const colorGradient = categories.map((category, index) => {
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
                      <div style="color: #666;">人数: <span style="color: #ff4757; font-weight: bold;">${data.value}</span>人</div>
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
          data: categories,
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
          name: '人数',
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
            name: '红淬炼分布',
            type: 'bar',
            data: values.map((value, index) => ({
              value: value,
              itemStyle: {
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                  { offset: 0, color: colorGradient[index] },
                  { offset: 1, color: colorGradient[index] + '99' } // 添加透明度
                ]),
                borderRadius: [6, 6, 0, 0],
                shadowColor: colorGradient[index] + '33',
                shadowBlur: 10,
                shadowOffsetY: 4
              }
            })),
            label: {
              show: true,
              position: 'top',
              formatter: '{c}',
              fontSize: 13,
              fontWeight: '600',
              color: '#ff4757',
              padding: [4, 0, 0, 0]
            },
            emphasis: {
              itemStyle: {
                shadowColor: colorGradient,
                shadowBlur: 15,
                shadowOffsetY: 6
              }
            }
          }
        ]
      }
      
      redRefineChartInstance.setOption(option)
      refineChartLoading.value = false
    })
  }).catch(() => {
    refineChartLoading.value = false
  })
}

// 手动重新加载四圣数量分布图表，确保颜色正确
function refreshFourSacredChart() {
  if (fourSacredChartInstance) {
    const option = fourSacredChartInstance.getOption()
    if (option && option.series && option.series[0]) {
      // 强制重新渲染图表，确保颜色正确
      fourSacredChartInstance.setOption(option, true)
    }
  }
}

// 加载四圣数量分布数据
function loadFourSacredDistribution() {
  fourSacredChartLoading.value = true
  getFourSacredDistribution(selectedTeam.value).then(response => {
    nextTick(() => {
      // 如果图表实例已存在，先销毁再重新创建
      if (fourSacredChartInstance) {
        fourSacredChartInstance.dispose()
      }
      fourSacredChartInstance = echarts.init(fourSacredChart.value)
      
      const data = response.data || []
      console.log('四圣数量分布数据:', data) // 调试日志，检查数据是否正确
      
      // 确保数据是有效的
      if (!Array.isArray(data) || data.length === 0) {
        console.error('四圣数量分布数据无效:', data)
        fourSacredChartLoading.value = false
        return
      }
      
      const categories = data.map(item => item.teamGroup || '')
      const values = data.map(item => item.memberCount || 0)
      
      // 为四圣数量分布创建渐变色，数量越多颜色越深
      const itemColors = categories.map(category => {
        if (!category) return '#67C23A' // 默认绿色
        if (category === '无') return '#E4E7ED' // 灰色
        if (category === '1') return '#B3E19D' // 浅绿色
        if (category === '2') return '#95D475' // 中浅绿色
        if (category === '3') return '#67C23A' // 中绿色
        if (category === '4') return '#529B2E' // 中深绿色
        if (category === '5') return '#409EFF' // 蓝色
        if (category === '6') return '#E6A23C' // 橙色
        if (category === '7') return '#F56C6C' // 红色
        if (category === '8') return '#BB1E1E' // 深红色
        if (category === '8+') return '#7D1313' // 极深红色
        return '#67C23A' // 默认绿色
      })
      
      console.log('四圣数量分布类别:', categories)
      console.log('四圣数量分布值:', values)
      console.log('四圣数量分布颜色:', itemColors)
      
      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          },
          formatter: function(params) {
            const category = params[0].axisValue
            const value = params[0].value
            const color = category === '无' ? 'gray' : 
                         category === '8+' ? 'gold' : 
                         parseInt(category) >= 5 ? 'purple' : 'green'
            
            let rarityText = ''
            if (category === '无') {
              rarityText = '<span style="color:gray">无四圣</span>'
            } else if (category === '8+') {
              rarityText = '<span style="color:gold;font-weight:bold">极其稀有 (8+)</span>'
            } else {
              const num = parseInt(category)
              if (num >= 7) {
                rarityText = `<span style="color:red;font-weight:bold">极其稀有 (${num}四圣)</span>`
              } else if (num >= 5) {
                rarityText = `<span style="color:purple;font-weight:bold">非常稀有 (${num}四圣)</span>`
              } else if (num >= 3) {
                rarityText = `<span style="color:green">${num}四圣</span>`
              } else {
                rarityText = `<span style="color:blue">${num}四圣</span>`
              }
            }
            
            return `${rarityText}<br/>人数: ${value}人`
          }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          top: '15%', // 增加顶部空间以容纳更长的标签
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: categories,
          axisLabel: {
            interval: 0, // 显示所有标签
            rotate: 0,   // 不旋转标签
            formatter: function(value) {
              // 为高数量值添加特殊标记
              if (value === '8+') return '8+⭐'
              if (value === '8') return '8⭐'
              if (value === '7') return '7⭐'
              if (value === '6') return '6⭐'
              if (value === '5') return '5⭐'
              if (value === '4') return '4⭐'
              return value
            }
          }
        },
        yAxis: {
          type: 'value',
          name: '人数',
          minInterval: 1 // 确保y轴最小刻度为整数
        },
        // 直接设置每个数据项的颜色和值，确保颜色正确显示
        series: [
          {
            name: '四圣数量分布',
            type: 'bar',
            data: categories.map((category, index) => {
              return {
                value: values[index],
                itemStyle: {
                  color: itemColors[index] || '#67C23A', // 使用预计算的颜色，提供默认值
                  borderWidth: 1,
                  borderColor: '#fff'
                },
                label: {
                  show: true,
                  position: 'top',
                  formatter: '{c}',
                  fontSize: 14,
                  fontWeight: 'bold',
                  color: '#333'
                }
              };
            }),
            // 为高数量的柱状图添加高亮效果
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowColor: 'rgba(0, 0, 0, 0.5)',
                borderWidth: 2
              }
            },
            // 标记最大值
            markPoint: {
              data: [
                { type: 'max', name: '最大值' }
              ],
              label: {
                formatter: '{b}: {c}',
                fontSize: 12,
                fontWeight: 'bold'
              }
            }
          }
        ]
      }
      
      try {
        fourSacredChartInstance.setOption(option, true) // 添加true参数，确保完全重新渲染图表
      } catch (error) {
        console.error('设置四圣数量分布图表选项时出错:', error)
        // 尝试使用简单的选项重新设置图表
        fourSacredChartInstance.setOption({
          xAxis: { data: categories },
          series: [{
            data: values,
            itemStyle: { color: '#67C23A' }
          }]
        }, true)
      }
      fourSacredChartLoading.value = false
    })
  }).catch(error => {
    console.error('加载四圣数量分布数据失败:', error)
    fourSacredChartLoading.value = false
  })
}

// 获取成员列表
function getMemberList() {
  memberListLoading.value = true
  listMember(queryParams).then(response => {
    memberList.value = response.rows
    total.value = response.total
    memberListLoading.value = false
  }).catch(() => {
    memberListLoading.value = false
  })
}

// 显示成员详情
function showMemberDetail(member) {
  currentMember.value = member
  memberDetailTitle.value = `成员详情 - ${member.gameId}`
  memberDetailVisible.value = true
}

// 导出团队数据
function exportTeamData() {
  // 导出当前团队的成员数据
  const queryParams = {
    teamGroup: selectedTeam.value
  }
  proxy.download('club/member/export', queryParams, `${selectedTeam.value}_成员数据_${new Date().getTime()}.xlsx`)
}

// 跳转到成员管理页面
function goToMemberList() {
  proxy.$router.push('/club/member')
}

// 调整图表大小
function resizeCharts() {
  nextTick(() => {
    lineupChartInstance?.resize()
    powerChartInstance?.resize()
    redRefineChartInstance?.resize()
    if (fourSacredChartInstance) {
      fourSacredChartInstance.resize()
      // 在调整大小时确保颜色正确显示
      try {
        const option = fourSacredChartInstance.getOption()
        if (option && option.series && option.series[0]) {
          // 触发图表重新渲染，确保颜色正确
          fourSacredChartInstance.setOption(option, true)
        }
      } catch (error) {
        console.error('调整四圣数量分布图表大小时出错:', error)
      }
    }
  })
}

// 页面挂载后加载数据
onMounted(() => {
  loadTeamData()
  getMemberList()
  
  // 监听窗口大小变化，调整图表大小
  window.addEventListener('resize', resizeCharts)
})
</script>

<style scoped lang="scss">
.club-detail-container {
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

  .quick-actions {
    display: flex;
    gap: 12px;
  }

  .action-card {
    display: flex;
    align-items: center;
    padding: 12px 16px;
    background: white;
    border: 2px solid #e2e8f0;
    border-radius: 12px;
    cursor: pointer;
    transition: all 0.3s ease;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    position: relative;
    overflow: hidden;

    &:hover {
      border-color: #3b82f6;
      transform: translateY(-2px);
      box-shadow: 0 4px 20px rgba(59, 130, 246, 0.15);

      &::before {
        transform: scaleX(1);
      }
    }

    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      height: 3px;
      background: linear-gradient(90deg, #3b82f6, #8b5cf6);
      transform: scaleX(0);
      transition: transform 0.3s ease;
    }

    .action-icon {
      font-size: 24px;
      margin-right: 12px;
      width: 40px;
      height: 40px;
      background: linear-gradient(135deg, #f8fafc, #e2e8f0);
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
      border: 1px solid #e2e8f0;
    }

    .action-text {
      flex: 1;

      .action-title {
        font-size: 16px;
        font-weight: 600;
        color: #1e293b;
        margin-bottom: 2px;
      }

      .action-desc {
        font-size: 12px;
        color: #64748b;
        line-height: 1.4;
      }
    }

    .action-arrow {
      font-size: 18px;
      color: #3b82f6;
      font-weight: 600;
      margin-left: 8px;
    }
  }
}

/* 统计卡片网格 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
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
      width: 64px;
      height: 64px;
      border-radius: 16px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 20px;
      font-size: 28px;
      background: linear-gradient(135deg, #f8fafc, #e2e8f0);
      border: 2px solid #e2e8f0;

      &.team-icon {
        background: linear-gradient(135deg, #dbeafe, #bfdbfe);
        border-color: #93c5fd;
      }

      &.power-icon {
        background: linear-gradient(135deg, #dcfce7, #bbf7d0);
        border-color: #86efac;
      }

      &.refine-icon {
        background: linear-gradient(135deg, #fef3c7, #fde68a);
        border-color: #fcd34d;
      }

      &.avg-icon {
        background: linear-gradient(135deg, #fce7f3, #fbcfe8);
        border-color: #f9a8d4;
      }
    }

    .stat-content {
      flex: 1;

      .stat-value {
        font-size: 32px;
        font-weight: 700;
        color: #0f172a;
        margin-bottom: 4px;
        line-height: 1.2;
      }

      .stat-label {
        font-size: 14px;
        color: #64748b;
        font-weight: 500;
      }
    }
  }
}

/* 图表网格布局 */
.charts-grid {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 20px;

  .charts-row {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 16px;
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
      height: 280px;
      padding: 16px;
    }
  }
}

/* 成员预览区域 */
.member-preview-section {
  background: white;
  border-radius: 16px;
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
      :deep(.el-button-group) {
        .el-button {
          border-radius: 6px;
          
          &.el-button--primary {
            background: #3b82f6;
            border-color: #3b82f6;
          }
        }
      }
    }
  }

  /* 成员卡片网格 */
  .member-cards-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 16px;
    padding: 20px;

    .member-card {
      display: flex;
      align-items: center;
      padding: 16px;
      background: #f8fafc;
      border: 2px solid #e2e8f0;
      border-radius: 12px;
      transition: all 0.3s ease;
      position: relative;

      &:hover {
        border-color: #3b82f6;
        transform: translateY(-2px);
        box-shadow: 0 8px 25px rgba(59, 130, 246, 0.15);
      }

      // 高实力卡片效果
      &.card-high {
        background: linear-gradient(145deg, #fff5f5, #fff0f0);
        border-color: #fecaca;
        box-shadow: 0 4px 20px rgba(251, 146, 60, 0.15);
        
        &:hover {
          border-color: #fb923c;
          box-shadow: 0 8px 30px rgba(251, 146, 60, 0.25);
        }
      }

      // 中等实力卡片效果
      &.card-medium {
        background: linear-gradient(145deg, #f0f9ff, #e0f2fe);
        border-color: #bae6fd;
        box-shadow: 0 4px 15px rgba(59, 130, 246, 0.1);
        
        &:hover {
          border-color: #38bdf8;
          box-shadow: 0 8px 25px rgba(59, 130, 246, 0.2);
        }
      }

      // 一般实力卡片效果
      &.card-low {
        background: #f8fafc;
        border-color: #e2e8f0;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
      }

      .member-rank {
        position: absolute;
        top: -8px;
        left: 16px;

        .rank-number {
          display: inline-flex;
          align-items: center;
          justify-content: center;
          width: 28px;
          height: 28px;
          border-radius: 50%;
          font-size: 12px;
          font-weight: 700;
          color: white;
          
          &.rank-gold {
            background: linear-gradient(135deg, #FFD700, #FFA500);
            box-shadow: 0 2px 8px rgba(255, 215, 0, 0.4);
          }
          
          &.rank-silver {
            background: linear-gradient(135deg, #C0C0C0, #808080);
            box-shadow: 0 2px 8px rgba(192, 192, 192, 0.4);
          }
          
          &.rank-bronze {
            background: linear-gradient(135deg, #CD7F32, #8B4513);
            box-shadow: 0 2px 8px rgba(205, 127, 50, 0.4);
          }
          
          &.rank-normal {
            background: linear-gradient(135deg, #64748b, #475569);
            box-shadow: 0 2px 8px rgba(100, 116, 139, 0.3);
          }
        }
      }

      .member-avatar {
        margin-right: 12px;
        margin-top: 8px;

        .avatar-placeholder {
          width: 48px;
          height: 48px;
          border-radius: 50%;
          color: white;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 14px;
          font-weight: 700;
          border: 2px solid rgba(255, 255, 255, 0.9);
          box-shadow: 0 3px 8px rgba(0, 0, 0, 0.2);
          
          // 阵容专属配色（固定色系，不受实力影响）
          &.lineup-red {
            // 吴国 - 经典红
            background: linear-gradient(145deg, #e74c3c, #c0392b);
          }
          
          &.lineup-green {
            // 赵云、蜀、姜维、关羽 - 翡翠绿
            background: linear-gradient(145deg, #27ae60, #229954);
          }
          
          &.lineup-blue {
            // 典韦、司马 - 深邃蓝
            background: linear-gradient(145deg, #3498db, #2980b9);
          }
          
          &.lineup-gold {
            // 毒爆 - 尊贵金
            background: linear-gradient(145deg, #f39c12, #d68910);
          }
          
          &.lineup-gray {
            // 默认 - 优雅灰
            background: linear-gradient(145deg, #7f8c8d, #5d6d7e);
          }
        }
      }

      .member-info {
        flex: 1;
        margin-top: 4px;

        .member-name {
          font-size: 16px;
          font-weight: 600;
          color: #1e293b;
          margin-bottom: 8px;
        }

        .member-stats {
          display: flex;
          gap: 12px;
          margin-bottom: 8px;

          .stat-item {
            display: flex;
            flex-direction: column;
            align-items: center;
            gap: 2px;
            font-size: 12px;
            min-width: 45px;

            .stat-label {
              font-size: 12px;
              color: #64748b;
              font-weight: 500;
              line-height: 1;
            }

            .stat-value {
              font-weight: 600;
              color: #374151;
              font-size: 15px;
              line-height: 1.2;

              &.power-stat {
                color: #059669;
                font-size: 14px;
              }

              &.red-stat {
                color: #dc2626;
                font-size: 16px;
                font-weight: 700;
              }

              &.sacred-stat {
                color: #7c3aed;
                font-size: 16px;
              }
            }
          }
        }

        .member-lineup {
          :deep(.el-tag) {
            border-radius: 6px;
            font-weight: 500;
            border: none;
          }
        }
      }
    }
  }

  /* 表格预览模式 */
  .member-table-preview {
    padding: 20px;

    :deep(.preview-table) {
      .el-table__header {
        th {
          background: #f8fafc;
          border-bottom: 2px solid #e2e8f0;
          font-weight: 600;
          color: #374151;
          font-size: 14px;
        }
      }

      .el-table__row {
        &:hover {
          background: #f8fafc;
        }

        td {
          border-bottom: 1px solid #f1f5f9;
          padding: 12px 0;
        }
      }

      .rank-badge {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        width: 24px;
        height: 24px;
        border-radius: 50%;
        font-size: 11px;
        font-weight: 700;
        color: white;
        
        &.rank-gold {
          background: linear-gradient(135deg, #FFD700, #FFA500);
        }
        
        &.rank-silver {
          background: linear-gradient(135deg, #C0C0C0, #808080);
        }
        
        &.rank-bronze {
          background: linear-gradient(135deg, #CD7F32, #8B4513);
        }
        
        &.rank-normal {
          background: linear-gradient(135deg, #64748b, #475569);
        }
      }

      .power-value {
        color: #059669;
        font-weight: 600;
        font-size: 15px;
      }

      .refine-value {
        color: #dc2626;
        font-weight: 600;
        font-size: 15px;
      }

      .fourSacred-value {
        color: #7c3aed;
        font-weight: 600;
        font-size: 15px;
      }

      .el-tag {
        border-radius: 6px;
        font-weight: 500;
        border: none;
      }
    }
  }

  /* 底部操作区 */
  .preview-actions {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 20px;
    background: #f8fafc;
    border-top: 1px solid #e2e8f0;

    .action-info {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 14px;
      color: #64748b;

      .info-text {
        font-weight: 500;
      }

      .divider {
        color: #cbd5e1;
      }
    }

    .action-buttons {
      display: flex;
      gap: 12px;

      .el-button {
        border-radius: 8px;
        font-weight: 500;

        &.el-button--primary {
          background: #3b82f6;
          border-color: #3b82f6;

          &:hover {
            background: #2563eb;
            border-color: #2563eb;
          }
        }
      }
    }
  }
}

/* 响应式设计 */
@media screen and (max-width: 1200px) {
  .charts-grid {
    .charts-row {
      grid-template-columns: 1fr;
    }
  }
}

@media screen and (max-width: 768px) {
  .club-detail-container {
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

    .quick-actions {
      justify-content: center;

      .action-card {
        width: 100%;
        padding: 16px 20px;
        justify-content: center;
        text-align: center;

        .action-icon {
          margin-right: 0;
          margin-bottom: 8px;
        }

        .action-text {
          text-align: center;

          .action-desc {
            display: none;
          }
        }

        .action-arrow {
          display: none;
        }
      }
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
        font-size: 24px;
      }
    }
  }

  .chart-card .chart-container {
    height: 240px;
    padding: 12px;
  }

  .member-preview-section {
    .section-header {
      flex-direction: column;
      gap: 12px;
      align-items: stretch;
    }

    .member-cards-grid {
      grid-template-columns: 1fr;
      gap: 12px;
      padding: 16px;

      .member-card {
        padding: 12px;

        .member-avatar .avatar-placeholder {
          width: 40px;
          height: 40px;
          font-size: 16px;
        }

        .member-info .member-name {
          font-size: 15px;
        }

        .member-info .member-stats {
          gap: 8px;
        }
      }
    }

    .member-table-preview {
      padding: 16px;
    }

    .preview-actions {
      flex-direction: column;
      gap: 12px;
      align-items: stretch;

      .action-info {
        justify-content: center;
        text-align: center;
      }

      .action-buttons {
        justify-content: center;
      }
    }
  }
}

/* 打印样式优化 */
@media print {
  .club-detail-container {
    padding: 16px;
    background: white;
  }

  .page-header,
  .section-header {
    box-shadow: none;
    border: 1px solid #ddd;
  }

  .stats-grid .stat-card,
  .charts-grid .chart-card,
  .member-list-section {
    box-shadow: none;
    border: 1px solid #ddd;
  }
}
</style>