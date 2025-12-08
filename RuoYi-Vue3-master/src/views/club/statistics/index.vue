<template>
  <div class="app-container">
    <el-row :gutter="20">
      <!-- 总览数据卡片 -->
      <el-col :span="6" v-for="(item, index) in overviewData" :key="index">
        <el-card shadow="hover" class="overview-card">
          <div class="card-content">
            <div class="card-icon" :style="{ backgroundColor: item.color }">
              <el-icon><component :is="item.icon" /></el-icon>
            </div>
            <div class="card-info">
              <div class="card-title">{{ item.title }}</div>
              <div class="card-value">{{ item.value }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-20">
      <!-- 成员战绩排名 -->
      <el-col :span="12">
        <el-card shadow="always">
          <template #header>
            <div class="card-header">
              <span>成员战绩排名</span>
              <el-select v-model="rankingType" size="small" @change="refreshRanking">
                <el-option label="杀敌数" value="kills" />
                <el-option label="死亡数" value="deaths" />
                <el-option label="刨击数" value="digs" />
                <el-option label="KD比例" value="kd" />
              </el-select>
            </div>
          </template>
          
          <div ref="rankingChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      
      <!-- 战绩趋势图 -->
      <el-col :span="12">
        <el-card shadow="always">
          <template #header>
            <div class="card-header">
              <span>战绩趋势图</span>
              <el-button-group size="small">
                <el-button :type="trendType === 'kills' ? 'primary' : ''" @click="changeTrendType('kills')">杀敌</el-button>
                <el-button :type="trendType === 'deaths' ? 'primary' : ''" @click="changeTrendType('deaths')">死亡</el-button>
                <el-button :type="trendType === 'kd' ? 'primary' : ''" @click="changeTrendType('kd')">KD</el-button>
              </el-button-group>
            </div>
          </template>
          
          <div ref="trendChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" class="mt-20">
      <!-- 数据分布饼图 -->
      <el-col :span="12">
        <el-card shadow="always">
          <template #header>
            <div class="card-header">
              <span>战绩数据分布</span>
              <el-select v-model="distributionType" size="small" @change="refreshDistribution">
                <el-option label="成员贡献占比" value="member" />
                <el-option label="日期活动量" value="date" />
              </el-select>
            </div>
          </template>
          
          <div ref="distributionChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      
      <!-- 详细数据表格 -->
      <el-col :span="12">
        <el-card shadow="always">
          <template #header>
            <div class="card-header">
              <span>详细数据</span>
              <el-button size="small" @click="refreshData">刷新</el-button>
            </div>
          </template>
          
          <el-table :data="detailData" style="width: 100%" max-height="400">
            <el-table-column prop="memberId" label="成员ID" width="80" />
            <el-table-column prop="totalKills" label="总杀敌" width="80" />
            <el-table-column prop="totalDeaths" label="总死亡" width="80" />
            <el-table-column prop="totalDigs" label="总刨击" width="80" />
            <el-table-column prop="totalRevives" label="总复活" width="80" />
            <el-table-column prop="avgKdRatio" label="平均KD" width="100">
              <template #default="scope">
                {{ scope.row.avgKdRatio ? scope.row.avgKdRatio.toFixed(2) : '0.00' }}
              </template>
            </el-table-column>
            <el-table-column prop="recordCount" label="记录数" width="80" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="Statistics">
import { ref, onMounted, nextTick, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { 
  getOverview, 
  getRanking, 
  getTimelineStats 
} from '@/api/club/statistics'
import { 
  Document, 
  Trophy, 
  Warning, 
  Aim 
} from '@element-plus/icons-vue'

// 图表DOM引用
const rankingChartRef = ref(null)
const trendChartRef = ref(null)
const distributionChartRef = ref(null)

// 图表实例
let rankingChartInstance = null
let trendChartInstance = null
let distributionChartInstance = null

// 数据状态
const overviewData = ref([])
const detailData = ref([])
const rankingType = ref('kills')
const trendType = ref('kills')
const distributionType = ref('member')

// 初始化总览数据
const initOverviewData = () => {
  overviewData.value = [
    {
      title: '总记录数',
      value: 0,
      icon: 'Document',
      color: '#409EFF'
    },
    {
      title: '总杀敌数',
      value: 0,
      icon: 'Trophy',
      color: '#67C23A'
    },
    {
      title: '总死亡数',
      value: 0,
      icon: 'Warning',
      color: '#E6A23C'
    },
    {
      title: '平均KD',
      value: '0.00',
      icon: 'Aim',
      color: '#F56C6C'
    }
  ]
}

// 获取总览数据
const fetchOverview = async () => {
  try {
    const response = await getOverview()
    if (response.code === 200) {
      const data = response.data
      
      // 更新总览数据
      overviewData.value[0].value = data.totalRecords
      overviewData.value[1].value = data.totalKills
      overviewData.value[2].value = data.totalDeaths
      overviewData.value[3].value = data.avgKdRatio.toFixed(2)
    }
  } catch (error) {
    console.error('获取总览数据失败:', error)
  }
}

// 初始化排名图表
const initRankingChart = () => {
  rankingChartInstance = echarts.init(rankingChartRef.value)
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    xAxis: {
      type: 'category',
      data: []
    },
    yAxis: {
      type: 'value'
    },
    series: [{
      data: [],
      type: 'bar',
      itemStyle: {
        color: '#409EFF'
      }
    }]
  }
  
  rankingChartInstance.setOption(option)
}

// 刷新排名图表
const refreshRanking = async () => {
  try {
    const response = await getRanking({ type: rankingType.value, limit: 10 })
    if (response.code === 200) {
      const data = response.data.rankings
      
      const names = data.map(item => `成员${item.memberId}`)
      const values = data.map(item => {
        if (rankingType.value === 'kd') {
          return item.avgKdRatio ? item.avgKdRatio.toFixed(2) : 0
        }
        return item[`total${rankingType.value.charAt(0).toUpperCase() + rankingType.value.slice(1)}`] || 0
      })
      
      rankingChartInstance.setOption({
        xAxis: { data: names },
        series: [{ data: values }]
      })
      
      // 更新详细数据
      detailData.value = data
    }
  } catch (error) {
    console.error('获取排名数据失败:', error)
  }
}

// 初始化趋势图表
const initTrendChart = () => {
  trendChartInstance = echarts.init(trendChartRef.value)
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: []
    },
    yAxis: {
      type: 'value'
    },
    series: [{
      data: [],
      type: 'line',
      smooth: true,
      itemStyle: {
        color: '#67C23A'
      }
    }]
  }
  
  trendChartInstance.setOption(option)
}

// 切换趋势类型
const changeTrendType = (type) => {
  trendType.value = type
  refreshTrendChart()
}

// 刷新趋势图表
const refreshTrendChart = async () => {
  try {
    const response = await getTimelineStats()
    if (response.code === 200) {
      const data = response.data.timeline
      
      const dates = data.map(item => item.date)
      const values = data.map(item => {
        if (trendType.value === 'kd') {
          return item.totalDeaths > 0 ? (item.totalKills / item.totalDeaths).toFixed(2) : item.totalKills
        }
        return item[`total${trendType.value.charAt(0).toUpperCase() + trendType.value.slice(1)}`] || 0
      })
      
      trendChartInstance.setOption({
        xAxis: { data: dates },
        series: [{ data: values }]
      })
    }
  } catch (error) {
    console.error('获取趋势数据失败:', error)
  }
}

// 初始化分布图表
const initDistributionChart = () => {
  distributionChartInstance = echarts.init(distributionChartRef.value)
  const option = {
    tooltip: {
      trigger: 'item'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        type: 'pie',
        radius: '50%',
        data: [],
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
  
  distributionChartInstance.setOption(option)
}

// 刷新分布图表
const refreshDistribution = async () => {
  try {
    let data = []
    
    if (distributionType.value === 'member') {
      const response = await getRanking({ type: 'kills', limit: 10 })
      if (response.code === 200) {
        data = response.data.rankings.map(item => ({
          name: `成员${item.memberId}`,
          value: item.totalKills
        }))
      }
    } else if (distributionType.value === 'date') {
      const response = await getTimelineStats()
      if (response.code === 200) {
        data = response.data.timeline.map(item => ({
          name: item.date,
          value: item.recordCount
        }))
      }
    }
    
    distributionChartInstance.setOption({
      series: [{ data }]
    })
  } catch (error) {
    console.error('获取分布数据失败:', error)
  }
}

// 刷新所有数据
const refreshData = async () => {
  await fetchOverview()
  await refreshRanking()
  await refreshTrendChart()
  await refreshDistribution()
}

// 窗口大小变化时重新调整图表大小
const resizeCharts = () => {
  if (rankingChartInstance) rankingChartInstance.resize()
  if (trendChartInstance) trendChartInstance.resize()
  if (distributionChartInstance) distributionChartInstance.resize()
}

// 页面挂载后初始化
onMounted(async () => {
  initOverviewData()
  await fetchOverview()
  
  // 等待DOM渲染完成后初始化图表
  await nextTick()
  
  // 初始化图表
  initRankingChart()
  initTrendChart()
  initDistributionChart()
  
  // 加载数据
  await refreshRanking()
  await refreshTrendChart()
  await refreshDistribution()
  
  // 监听窗口大小变化
  window.addEventListener('resize', resizeCharts)
})

// 组件卸载时清理
onUnmounted(() => {
  window.removeEventListener('resize', resizeCharts)
  
  if (rankingChartInstance) rankingChartInstance.dispose()
  if (trendChartInstance) trendChartInstance.dispose()
  if (distributionChartInstance) distributionChartInstance.dispose()
})
</script>

<style scoped>
.mt-20 {
  margin-top: 20px;
}

.overview-card {
  margin-bottom: 20px;
}

.card-content {
  display: flex;
  align-items: center;
}

.card-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  color: white;
  font-size: 24px;
}

.card-info {
  flex: 1;
}

.card-title {
  font-size: 14px;
  color: #606266;
  margin-bottom: 5px;
}

.card-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container {
  width: 100%;
  height: 300px;
}
</style>