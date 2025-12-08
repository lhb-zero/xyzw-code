<template>
  <div class="member-container">
    <!-- 搜索区域 -->
    <el-card class="search-card" shadow="hover" v-show="showSearch">
      <template #header>
        <div class="card-header">
          <span class="card-title">
            <el-icon><Search /></el-icon>
            筛选查询
          </span>
        </div>
      </template>
      <el-form :model="queryParams" ref="queryRef" :inline="true" label-width="110px">
        <el-form-item label="游戏昵称/ID" prop="gameId">
          <el-input
            v-model="queryParams.gameId"
            placeholder="请输入游戏昵称/ID"
            clearable
            @input="handleInputQuery"
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="原俱乐部名称" prop="server">
          <el-input
            v-model="queryParams.server"
            placeholder="请输入原俱乐部名称"
            clearable
            @input="handleInputQuery"
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="团别" prop="teamGroup">
          <el-select v-model="queryParams.teamGroup" placeholder="请选择团别" clearable style="width: 200px" @change="handleSelectChange">
            <el-option
              v-for="team in teamOptions"
              :key="team.value"
              :label="team.label"
              :value="team.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="加入日期" prop="joinDate">
          <el-date-picker 
            clearable
            v-model="queryParams.joinDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择加入日期"
            style="width: 200px"
            @change="handleDateChange"
          />
        </el-form-item>
        <el-form-item label="四圣数量" prop="fourSacred">
          <el-input
            v-model="queryParams.fourSacred"
            placeholder="请输入四圣数量"
            clearable
            @input="handleInputQuery"
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="红淬炼数量" prop="redRefine">
          <el-select v-model="queryParams.redRefine" placeholder="请选择红淬炼数量范围" clearable style="width: 200px" @change="handleSelectChange">
            <el-option
              v-for="range in redRefineRangeOptions"
              :key="range.value"
              :label="range.label"
              :value="range.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="主C阵容" prop="mainLineup">
          <el-select v-model="queryParams.mainLineup" placeholder="请选择主C阵容" clearable style="width: 200px" @change="handleSelectChange">
            <el-option
              v-for="dict in sys_lineup"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery" class="search-btn">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery" class="reset-btn">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作区域 -->
    <el-card class="operation-card" shadow="hover">
      <el-row :gutter="10" class="mb8">
        <el-col :span="1.5">
          <el-button
            type="primary"
            plain
            icon="Plus"
            @click="handleAdd"
            v-hasPermi="['club:member:add']"
            class="custom-btn"
          >新增成员</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button
            type="success"
            plain
            icon="Edit"
            :disabled="single"
            @click="handleUpdate"
            v-hasPermi="['club:member:edit']"
            class="custom-btn"
          >修改信息</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button
            type="danger"
            plain
            icon="Delete"
            :disabled="multiple"
            @click="handleDelete"
            v-hasPermi="['club:member:remove']"
            class="custom-btn"
          >批量删除</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button
            type="warning"
            plain
            icon="Download"
            @click="handleExport"
            v-hasPermi="['club:member:export']"
            class="custom-btn"
          >导出数据</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button
            type="info"
            plain
            icon="DocumentCopy"
            @click="handleClubDetail"
            class="custom-btn"
          >俱乐部详情</el-button>
        </el-col>
        <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
      </el-row>

      <el-table 
        v-loading="loading" 
        :data="memberList" 
        @selection-change="handleSelectionChange"
        class="custom-table"
        :header-cell-style="{background: '#f5f7fa', color: '#606266', fontWeight: 'bold'}"
        stripe
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="ID" align="center" prop="id" width="80" />
        <el-table-column label="游戏昵称/ID" align="center" prop="gameId" width="160" show-overflow-tooltip>
          <template #default="scope">
            <span class="nickname-text">{{ scope.row.gameId }}</span>
          </template>
        </el-table-column>
        <el-table-column label="战力值" align="center" prop="power" width="130" sortable>
          <template #default="scope">
            <span class="power-highlight">{{ scope.row.power }}</span>
          </template>
        </el-table-column>
        <el-table-column label="原俱乐部" align="center" prop="server" width="150" show-overflow-tooltip />
        <el-table-column label="团别" align="center" prop="teamGroup" width="100">
          <template #default="scope">
            <el-tag 
              :type="getTeamTagType(scope.row.teamGroup)"
              size="small"
              v-if="scope.row.teamGroup"
            >{{ scope.row.teamGroup }}</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="加入日期" align="center" prop="joinDate" width="120" sortable>
          <template #default="scope">
            <span class="date-text">{{ parseTime(scope.row.joinDate, '{y}-{m}-{d}') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="四圣数量" align="center" prop="fourSacred" width="150" sortable>
          <template #default="scope">
            <div class="sacred-container">
              <span class="sacred-highlight">{{ scope.row.fourSacred }}</span>
              <div class="sacred-buttons">
                <el-button 
                  size="small" 
                  type="success" 
                  icon="Top" 
                  circle 
                  @click="incrementFourSacred(scope.row)"
                  v-hasPermi="['club:member:edit']"
                  title="增加四圣数量"
                  class="increment-sacred-btn"
                />
                <el-button 
                  size="small" 
                  type="warning" 
                  icon="Bottom" 
                  circle 
                  @click="decrementFourSacred(scope.row)"
                  v-hasPermi="['club:member:edit']"
                  title="减少四圣数量"
                  class="decrement-sacred-btn"
                />
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="红淬炼" align="center" prop="redRefine" width="150" sortable>
          <template #default="scope">
            <div class="refine-container">
              <span class="refine-highlight">{{ scope.row.redRefine }}</span>
              <div class="refine-buttons">
                <el-button 
                  size="small" 
                  type="success" 
                  icon="Top" 
                  circle 
                  @click="incrementRedRefine(scope.row)"
                  v-hasPermi="['club:member:edit']"
                  title="增加红淬炼"
                  class="increment-btn"
                />
                <el-button 
                  size="small" 
                  type="warning" 
                  icon="Bottom" 
                  circle 
                  @click="decrementRedRefine(scope.row)"
                  v-hasPermi="['club:member:edit']"
                  title="减少红淬炼"
                  class="decrement-btn"
                />
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="主C阵容" align="center" prop="mainLineup" width="140">
          <template #default="scope">
            <span :class="getLineupClass(scope.row.mainLineup)">
              {{ getLineupLabel(scope.row.mainLineup) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="更新时间" align="center" prop="updatedAt" width="120">
          <template #default="scope">
            <span class="date-text">{{ parseTime(scope.row.updatedAt, '{y}-{m}-{d}') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-button 
              link 
              type="primary" 
              icon="Edit" 
              @click="handleUpdate(scope.row)" 
              v-hasPermi="['club:member:edit']"
              class="table-btn"
            >修改</el-button>
            <el-button 
              link 
              type="success" 
              icon="TrendCharts" 
              @click="showPlayerStats(scope.row)" 
              class="table-btn"
            >盐场个人详情</el-button>
            <el-button 
              link 
              type="primary" 
              icon="Delete" 
              @click="handleDelete(scope.row)" 
              v-hasPermi="['club:member:remove']"
              class="table-btn"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <pagination
        v-show="total>0"
        :total="total"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        :page-sizes="[30, 60, 90]"
        @pagination="getList"
        class="custom-pagination"
      />
    </el-card>

    <!-- 添加或修改对话框 -->
    <el-dialog :title="title" v-model="open" width="900px" append-to-body class="custom-dialog">
      <el-form ref="memberRef" :model="form" :rules="rules" label-width="120px" class="dialog-form">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="游戏昵称/ID" prop="gameId">
              <el-input v-model="form.gameId" placeholder="请输入游戏昵称/ID" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="战力值" prop="power">
              <el-input v-model="form.power" placeholder="请输入战力值" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="原俱乐部名称" prop="server">
              <el-input v-model="form.server" placeholder="请输入原俱乐部名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="团别" prop="teamGroup">
              <el-select v-model="form.teamGroup" placeholder="请选择团别" clearable style="width: 100%">
                <el-option
                  v-for="team in teamOptions"
                  :key="team.value"
                  :label="team.label"
                  :value="team.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="加入日期" prop="joinDate">
              <el-date-picker 
                clearable
                v-model="form.joinDate"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择加入日期"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>

        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="四圣数量" prop="fourSacred">
              <el-input v-model="form.fourSacred" placeholder="请输入四圣数量" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="红淬炼数量" prop="redRefine">
              <el-input v-model="form.redRefine" placeholder="请输入红淬炼数量" />
            </el-form-item>
          </el-col>


        </el-row>
        
        <el-form-item label="主C阵容" prop="mainLineup">
          <el-radio-group v-model="form.mainLineup" class="lineup-radio-group">
            <el-radio
              v-for="dict in sys_lineup"
              :key="dict.value"
              :label="dict.value"
            >{{dict.label}}</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="创建时间" prop="createdAt">
              <el-date-picker 
                clearable
                v-model="form.createdAt"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择创建时间"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="更新时间" prop="updatedAt">
              <el-date-picker 
                clearable
                v-model="form.updatedAt"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择更新时间"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-divider content-position="center">盐场战绩记录</el-divider>
        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button type="primary" icon="Plus" @click="handleAddSaltFieldRecord">添加</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="danger" icon="Delete" @click="handleDeleteSaltFieldRecord">删除</el-button>
          </el-col>
        </el-row>
        <el-table 
          :data="saltFieldRecordList" 
          :row-class-name="rowSaltFieldRecordIndex" 
          @selection-change="handleSaltFieldRecordSelectionChange" 
          ref="saltFieldRecord"
          class="sub-table"
        >
          <el-table-column type="selection" width="50" align="center" />
          <el-table-column label="序号" align="center" prop="index" width="50"/>
          <el-table-column label="活动日期" prop="recordDate" width="200">
            <template #default="scope">
              <el-date-picker 
                clearable
                v-model="scope.row.recordDate"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择活动日期"
              />
            </template>
          </el-table-column>
          <el-table-column label="杀敌数" prop="kills" width="130">
            <template #default="scope">
              <el-input v-model="scope.row.kills" placeholder="请输入杀敌数" />
            </template>
          </el-table-column>
          <el-table-column label="死亡数" prop="deaths" width="130">
            <template #default="scope">
              <el-input v-model="scope.row.deaths" placeholder="请输入死亡数" />
            </template>
          </el-table-column>
          <el-table-column label="刨地/刨击数" prop="digs" width="130">
            <template #default="scope">
              <el-input v-model="scope.row.digs" placeholder="请输入刨地/刨击数" />
            </template>
          </el-table-column>
          <el-table-column label="复活丹使用次数" prop="revives" width="150">
            <template #default="scope">
              <el-input v-model="scope.row.revives" placeholder="请输入复活丹使用次数" />
            </template>
          </el-table-column>
          <el-table-column label="KD战损比例" prop="kdRatio" width="130">
            <template #default="scope">
              <el-input v-model="scope.row.kdRatio" placeholder="请输入KD战损比例" />
            </template>
          </el-table-column>
          <el-table-column label="关联OCR识别记录ID" prop="ocrRecordId" width="180">
            <template #default="scope">
              <el-input v-model="scope.row.ocrRecordId" placeholder="请输入关联OCR识别记录ID" />
            </template>
          </el-table-column>
          <el-table-column label="截图存储路径" prop="imageUrl" width="200">
            <template #default="scope">
              <el-input v-model="scope.row.imageUrl" placeholder="请输入截图存储路径" />
            </template>
          </el-table-column>
          <el-table-column label="创建时间" prop="createdAt" width="200">
            <template #default="scope">
              <el-date-picker 
                clearable
                v-model="scope.row.createdAt"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择创建时间"
              />
            </template>
          </el-table-column>
        </el-table>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm" class="submit-btn">确 定</el-button>
          <el-button @click="cancel" class="cancel-btn">取 消</el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 盐场个人详情对话框 -->
    <player-stats-dialog
      v-model:visible="playerStatsVisible"
      :member-id="currentMemberId"
      :nickname="currentMemberName"
      :all-records="allBattleRecords"
    />
  </div>
</template>

<script setup name="Member">
import { ref, reactive, toRefs, getCurrentInstance } from 'vue'
import { listMember, getMember, delMember, addMember, updateMember, updateRedRefine, updateFourSacred } from "@/api/club/member"
import { getBattleRecordList } from "@/api/club/battle"
import PlayerStatsDialog from '@/views/club/battle/components/PlayerStatsDialog.vue'

const { proxy } = getCurrentInstance()
const { sys_lineup } = proxy.useDict('sys_lineup')

// 防抖函数
const debounce = (fn, delay) => {
  let timer = null
  return (...args) => {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => {
      fn.apply(this, args)
    }, delay)
  }
}

const memberList = ref([])
const saltFieldRecordList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const checkedSaltFieldRecord = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")

// 个人详情对话框相关
const playerStatsVisible = ref(false)
const currentMemberId = ref(null)
const currentMemberName = ref('')
const allBattleRecords = ref([])

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 30,
    gameId: null,
    server: null,
    teamGroup: null,
    joinDate: null,
    fourSacred: null,
    redRefine: null,
    mainLineup: null,
  },
  rules: {
    gameId: [
      { required: true, message: "游戏昵称/ID不能为空", trigger: "blur" }
    ],
  }
})

// 团别选项
const teamOptions = ref([
  { value: '1团', label: '1团' },
  { value: '2团', label: '2团' },
  { value: '3团', label: '3团' }
])

// 红淬炼数量范围选项
const redRefineRangeOptions = ref([
  { value: 0, label: '小于30', isMax: true, max: 29 },
  { value: 30, label: '大于等于30', isMin: true },
  { value: 40, label: '大于等于40', isMin: true },
  { value: 50, label: '大于等于50', isMin: true }
])

const { queryParams, form, rules } = toRefs(data)

// 获取阵容标签文本
function getLineupLabel(value) {
  const dict = sys_lineup.value.find(item => item.value === value)
  return dict ? dict.label : '-'
}

// 获取阵容样式类名
function getLineupClass(value) {
  const label = getLineupLabel(value)
  const classMap = {
    '吴国': 'lineup-badge wu-guo',
    '合力赵云': 'lineup-badge heli-zhaoyun',
    '三蜀赵云': 'lineup-badge sanshu-zhaoyun',
    '典韦': 'lineup-badge dianwei',
    '姜维': 'lineup-badge jiangwei',
    '关羽': 'lineup-badge guanyu',
    '司马懿': 'lineup-badge simayi',
    '毒爆': 'lineup-badge dubao'
  }
  return classMap[label] || 'lineup-badge default'
}

// 获取团别标签类型
function getTeamTagType(teamGroup) {
  if (!teamGroup) return 'info'
  const teamTypeMap = {
    '1团': 'primary',
    '2团': 'success',
    '3团': 'warning'
  }
  return teamTypeMap[teamGroup] || 'info'
}

/** 查询俱乐部成员信息列表 */
function getList() {
  loading.value = true
  listMember(queryParams.value).then(response => {
    memberList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

// 取消按钮
function cancel() {
  open.value = false
  reset()
}

// 表单重置
function reset() {
  form.value = {
    id: null,
    gameId: null,
    power: null,
    server: null,
    teamGroup: null,
    joinDate: null,
    fourSacred: null,
    redRefine: null,
    mainLineup: null,
    customField1: null,
    customField2: null,
    createdAt: null,
    updatedAt: null
  }
  saltFieldRecordList.value = []
  proxy.resetForm("memberRef")
}

/** 搜索按钮操作 */
function handleQuery() {
  // 处理红淬炼范围查询参数
  processRedRefineQuery()
  queryParams.value.pageNum = 1
  getList()
}

/** 处理红淬炼范围查询参数 */
function processRedRefineQuery() {
  // 获取选中的选项
  const selectedOption = redRefineRangeOptions.value.find(option => option.value === queryParams.value.redRefine)
  
  if (selectedOption) {
    if (selectedOption.isMin) {
      // 设置最小值（大于等于）
      queryParams.value.redRefineMin = selectedOption.value
      queryParams.value.redRefineMax = selectedOption.max || null
      queryParams.value.redRefineOp = "gte" // 添加操作符标识
    } else if (selectedOption.isMax) {
      // 设置最大值（小于）
      queryParams.value.redRefineMax = selectedOption.max
      queryParams.value.redRefineMin = null
      queryParams.value.redRefineOp = "lt" // 添加操作符标识
    }
  } else {
    // 没有选择任何范围选项，清空范围参数
    queryParams.value.redRefineMin = null
    queryParams.value.redRefineMax = null
    queryParams.value.redRefineOp = null
  }
}

/** 输入框实时查询（防抖处理） */
const handleInputQuery = debounce(() => {
  processRedRefineQuery()
  queryParams.value.pageNum = 1
  getList()
}, 800) // 800毫秒的延迟

/** 下拉选择实时查询 */
function handleSelectChange() {
  processRedRefineQuery()
  queryParams.value.pageNum = 1
  getList()
}

/** 日期选择实时查询 */
function handleDateChange() {
  processRedRefineQuery()
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

// 多选框选中数据
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加俱乐部成员信息"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const _id = row.id || ids.value
  getMember(_id).then(response => {
    form.value = response.data
    saltFieldRecordList.value = response.data.saltFieldRecordList
    open.value = true
    title.value = "修改俱乐部成员信息"
  })
}

/** 显示盐场个人详情 */
async function showPlayerStats(row) {
  try {
    // 获取成员的战绩记录，尝试多种参数组合
    console.log('尝试获取成员战绩，成员ID:', row.id, '成员名称:', row.gameId)
    
    // 先尝试使用 memberId 参数
    let response = await getBattleRecordList({ 
      pageSize: 1000,
      memberId: row.id 
    })
    
    // 如果没有数据，尝试使用 member_id 参数
    if (!response.rows || response.rows.length === 0) {
      console.log('使用memberId未获取到数据，尝试使用member_id参数')
      response = await getBattleRecordList({ 
        pageSize: 1000,
        member_id: row.id 
      })
    }
    
    // 如果还是没有数据，尝试使用成员名称查找
    if (!response.rows || response.rows.length === 0) {
      console.log('使用member_id未获取到数据，尝试使用memberName参数')
      response = await getBattleRecordList({ 
        pageSize: 1000,
        memberName: row.gameId
      })
    }
    
    // 最后尝试使用gameId参数
    if (!response.rows || response.rows.length === 0) {
      console.log('使用memberName未获取到数据，尝试使用gameId参数')
      response = await getBattleRecordList({ 
        pageSize: 1000,
        gameId: row.gameId
      })
    }
    
    console.log('最终获取的战绩数据:', response)
    
    if (response.code === 200) {
      allBattleRecords.value = response.rows || []
      currentMemberId.value = row.id
      currentMemberName.value = row.gameId
      playerStatsVisible.value = true
      
      // 如果仍然没有数据，显示提示
      if (!response.rows || response.rows.length === 0) {
        proxy.$modal.msgWarning('该成员暂无战绩数据')
      }
    } else {
      proxy.$modal.msgError('获取战绩数据失败: ' + (response.msg || '未知错误'))
    }
  } catch (error) {
    console.error('获取战绩数据失败:', error)
    proxy.$modal.msgError('获取战绩数据失败')
  }
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["memberRef"].validate(valid => {
    if (valid) {
      form.value.saltFieldRecordList = saltFieldRecordList.value
      if (form.value.id != null) {
        updateMember(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addMember(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

/** 删除按钮操作 */
function handleDelete(row) {
  const _ids = row.id || ids.value
  proxy.$modal.confirm('是否确认删除俱乐部成员信息编号为"' + _ids + '"的数据项？').then(function() {
    return delMember(_ids)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 俱乐部详情按钮操作 */
function handleClubDetail() {
  proxy.$router.push('/club/detail')
}

/** 盐场战绩记录序号 */
function rowSaltFieldRecordIndex({ row, rowIndex }) {
  row.index = rowIndex + 1
}

/** 盐场战绩记录添加按钮操作 */
function handleAddSaltFieldRecord() {
  let obj = {}
  obj.recordDate = ""
  obj.kills = ""
  obj.deaths = ""
  obj.digs = ""
  obj.revives = ""
  obj.kdRatio = ""
  obj.ocrRecordId = ""
  obj.imageUrl = ""
  obj.dataSource = ""
  obj.createdAt = ""
  saltFieldRecordList.value.push(obj)
}

/** 盐场战绩记录删除按钮操作 */
function handleDeleteSaltFieldRecord() {
  if (checkedSaltFieldRecord.value.length == 0) {
    proxy.$modal.msgError("请先选择要删除的盐场战绩记录数据")
  } else {
    const saltFieldRecords = saltFieldRecordList.value
    const checkedSaltFieldRecords = checkedSaltFieldRecord.value
    saltFieldRecordList.value = saltFieldRecords.filter(function(item) {
      return checkedSaltFieldRecords.indexOf(item.index) == -1
    })
  }
}

/** 复选框选中数据 */
function handleSaltFieldRecordSelectionChange(selection) {
  checkedSaltFieldRecord.value = selection.map(item => item.index)
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('club/member/export', {
    ...queryParams.value
  }, `member_${new Date().getTime()}.xlsx`)
}

/** 增加红淬炼数量 */
function incrementRedRefine(row) {
  const params = {
    id: row.id,
    operation: 'increment'
  }
  
  updateRedRefine(params).then(response => {
    proxy.$modal.msgSuccess("增加成功")
    // 更新当前行数据
    row.redRefine = response.data.redRefine
  }).catch(error => {
    proxy.$modal.msgError("操作失败")
  })
}

/** 减少红淬炼数量 */
function decrementRedRefine(row) {
  // 检查是否可以减少
  if (row.redRefine <= 0) {
    proxy.$modal.msgWarning("红淬炼数量不能小于0")
    return
  }
  
  const params = {
    id: row.id,
    operation: 'decrement'
  }
  
  updateRedRefine(params).then(response => {
    proxy.$modal.msgSuccess("减少成功")
    // 更新当前行数据
    row.redRefine = response.data.redRefine
  }).catch(error => {
    proxy.$modal.msgError("操作失败")
  })
}

/** 增加四圣数量 */
function incrementFourSacred(row) {
  const params = {
    id: row.id,
    operation: 'increment'
  }
  
  updateFourSacred(params).then(response => {
    proxy.$modal.msgSuccess("增加成功")
    // 更新当前行数据
    row.fourSacred = response.data.fourSacred
  }).catch(error => {
    proxy.$modal.msgError("操作失败")
  })
}

/** 减少四圣数量 */
function decrementFourSacred(row) {
  // 检查是否可以减少
  if (row.fourSacred <= 0) {
    proxy.$modal.msgWarning("四圣数量不能小于0")
    return
  }
  
  const params = {
    id: row.id,
    operation: 'decrement'
  }
  
  updateFourSacred(params).then(response => {
    proxy.$modal.msgSuccess("减少成功")
    // 更新当前行数据
    row.fourSacred = response.data.fourSacred
  }).catch(error => {
    proxy.$modal.msgError("操作失败")
  })
}

getList()
</script>

<style scoped lang="scss">
.member-container {
  padding: 20px;
  background: linear-gradient(135deg, #e3f2fd 0%, #f3e5f5 50%, #e8eaf6 100%);
  min-height: calc(100vh - 84px);
}

/* 搜索卡片样式 */
.search-card {
  margin-bottom: 20px;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-2px);
  }
  
  :deep(.el-card__header) {
    background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
    padding: 16px 20px;
    border-bottom: none;
  }
  
  .card-header {
    display: flex;
    align-items: center;
    
    .card-title {
      display: flex;
      align-items: center;
      font-size: 16px;
      font-weight: bold;
      color: #fff;
      
      .el-icon {
        margin-right: 8px;
        font-size: 18px;
      }
    }
  }
  
  :deep(.el-card__body) {
    padding: 20px;
  }
}

/* 操作卡片样式 */
.operation-card {
  border-radius: 12px;
  overflow: hidden;
  
  :deep(.el-card__body) {
    padding: 20px;
  }
}

/* 按钮样式优化 */
.custom-btn {
  border-radius: 8px;
  padding: 10px 20px;
  font-weight: 500;
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  }
}

.search-btn {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  border: none;
  
  &:hover {
    background: linear-gradient(135deg, #00f2fe 0%, #4facfe 100%);
  }
}

.reset-btn {
  &:hover {
    color: #4facfe;
    border-color: #4facfe;
  }
}

/* 表格样式 */
.custom-table {
  border-radius: 8px;
  overflow: hidden;
  margin-top: 20px;
  
  :deep(.el-table__header-wrapper) {
    border-radius: 8px 8px 0 0;
  }
  
  :deep(.el-table__body-wrapper) {
    .el-table__row {
      transition: all 0.3s ease;
      
      &:hover {
        background-color: #f0f5ff !important;
        transform: scale(1.002);
      }
    }
  }
  
  .date-text {
    color: #909399;
    font-size: 13px;
  }
  
  .table-btn {
    font-weight: 500;
    
    &:hover {
      transform: scale(1.1);
    }
  }
}

/* 昵称样式 */
.nickname-text {
  font-weight: 600;
  color: #2c3e50;
}

/* 战力值高亮 */
.power-highlight {
  font-size: 15px;
  font-weight: bold;
  color: #f093fb;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  padding: 4px 8px;
  border-radius: 4px;
  background-color: rgba(240, 147, 251, 0.1);
  display: inline-block;
}

/* 四圣数量高亮 */
.sacred-highlight {
  font-size: 14px;
  font-weight: bold;
  color: #a18cd1;
  background: linear-gradient(135deg, #00aeffff 0%, #0044ffff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  padding: 2px 5px;
  border-radius: 3px;
  background-color: rgba(161, 140, 209, 0.1);
  display: inline-block;
  min-width: 20px;
  text-align: center;
  line-height: 1.2;
}

/* 四圣数量容器 */
.sacred-container {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  height: 100%;
}

/* 四圣按钮组 */
.sacred-buttons {
  display: flex;
  flex-direction: column;
  gap: 1px;
  height: 100%;
  justify-content: center;
}

/* 增加四圣按钮样式 */
.increment-sacred-btn {
  height: 16px;
  width: 16px;
  padding: 0;
  font-size: 8px;
  border: none;
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  line-height: 1;
  margin: 0;
  
  &:hover {
    transform: scale(1.1);
    background: linear-gradient(135deg, #85ce61 0%, #67c23a 100%);
  }
  
  :deep(.el-icon) {
    font-size: 8px;
    line-height: 1;
  }
}

/* 减少四圣按钮样式 */
.decrement-sacred-btn {
  height: 16px;
  width: 16px;
  padding: 0;
  font-size: 8px;
  border: none;
  background: linear-gradient(135deg, #e6a23c 0%, #eebe77 100%);
  line-height: 1;
  margin: 0;
  
  &:hover {
    transform: scale(1.1);
    background: linear-gradient(135deg, #eebe77 0%, #e6a23c 100%);
  }
  
  :deep(.el-icon) {
    font-size: 8px;
    line-height: 1;
  }
}

/* 红淬炼容器 */
.refine-container {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  height: 100%;
}

/* 红淬炼高亮 */
.refine-highlight {
  font-size: 14px;
  font-weight: bold;
  color: #eb3349;
  background: linear-gradient(135deg, #eb3349 0%, #f45c43 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  padding: 2px 5px;
  border-radius: 3px;
  background-color: rgba(235, 51, 73, 0.1);
  display: inline-block;
  min-width: 20px;
  text-align: center;
  line-height: 1.2;
}

/* 红淬炼按钮组 */
.refine-buttons {
  display: flex;
  flex-direction: column;
  gap: 1px;
  height: 100%;
  justify-content: center;
}

/* 增加按钮样式 */
.increment-btn {
  height: 16px;
  width: 16px;
  padding: 0;
  font-size: 8px;
  border: none;
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  line-height: 1;
  margin: 0;
  
  &:hover {
    transform: scale(1.1);
    background: linear-gradient(135deg, #85ce61 0%, #67c23a 100%);
  }
  
  :deep(.el-icon) {
    font-size: 8px;
    line-height: 1;
  }
}

/* 减少按钮样式 */
.decrement-btn {
  height: 16px;
  width: 16px;
  padding: 0;
  font-size: 8px;
  border: none;
  background: linear-gradient(135deg, #e6a23c 0%, #eebe77 100%);
  line-height: 1;
  margin: 0;
  
  &:hover {
    transform: scale(1.1);
    background: linear-gradient(135deg, #eebe77 0%, #e6a23c 100%);
  }
  
  :deep(.el-icon) {
    font-size: 8px;
    line-height: 1;
  }
}

/* 阵容徽章样式 - 优化版 */
.lineup-badge {
  display: inline-block;
  padding: 5px 12px;
  border-radius: 12px;
  font-weight: 600;
  font-size: 13px;
  color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  
  // 吴国 - 鲜艳红色
  &.wu-guo {
    background: linear-gradient(135deg, #ff6b6b 0%, #ff5252 100%);
  }
  
  // 合力赵云 - 橙色（保持）
  &.heli-zhaoyun {
    background: linear-gradient(135deg, #f7971e 0%, #ffd200 100%);
  }
  
  // 三蜀赵云 - 翠绿色
  &.sanshu-zhaoyun {
    background: linear-gradient(135deg, #00d2a0 0%, #00e5b8 100%);
  }
  
  // 司马懿 - 天蓝色（保持）
  &.simayi {
    background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  }
  
  // 典韦 - 深紫蓝
  &.dianwei {
    background: linear-gradient(135deg, #5f72ff 0%, #7b88ff 100%);
  }
  
  // 姜维 - 青绿色
  &.jiangwei {
    background: linear-gradient(135deg, #52c234 0%, #73dd51 100%);
  }
  
  // 关羽 - 青绿色
  &.guanyu {
    background: linear-gradient(135deg, #52c234 0%, #73dd51 100%);
  }
  
  // 毒爆 - 橙色（保持）
  &.dubao {
    background: linear-gradient(135deg, #f7971e 0%, #ffd200 100%);
  }
  
  // 默认
  &.default {
    background: linear-gradient(135deg, #95a5a6 0%, #b2bec3 100%);
  }
}

/* 分页样式 */
.custom-pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  
  :deep(.el-pagination) {
    .btn-next,
    .btn-prev,
    .el-pager li {
      border-radius: 6px;
      transition: all 0.3s ease;
      
      &:hover {
        transform: translateY(-2px);
      }
    }
    
    .el-pager li.active {
      background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
    }
  }
}

/* 对话框样式 */
.custom-dialog {
  :deep(.el-dialog) {
    border-radius: 16px;
    overflow: hidden;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  }
  
  :deep(.el-dialog__header) {
    background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
    padding: 20px;
    margin-right: 0;
    
    .el-dialog__title {
      color: #fff;
      font-size: 18px;
      font-weight: bold;
    }
    
    .el-dialog__headerbtn {
      top: 20px;
      
      .el-dialog__close {
        color: #fff;
        font-size: 20px;
        
        &:hover {
          color: #f0f0f0;
        }
      }
    }
  }
  
  :deep(.el-dialog__body) {
    padding: 30px;
    max-height: 70vh;
    overflow-y: auto;
    
    &::-webkit-scrollbar {
      width: 6px;
    }
    
    &::-webkit-scrollbar-thumb {
      background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
      border-radius: 3px;
    }
    
    &::-webkit-scrollbar-track {
      background: #f5f7fa;
    }
  }
  
  :deep(.el-dialog__footer) {
    padding: 20px 30px;
    background: #f5f7fa;
  }
}

/* 表单样式 */
.dialog-form {
  :deep(.el-form-item__label) {
    font-weight: 500;
    color: #606266;
  }
  
  :deep(.el-input__wrapper) {
    border-radius: 8px;
    transition: all 0.3s ease;
    
    &:hover {
      box-shadow: 0 0 0 1px #4facfe inset;
    }
  }
  
  :deep(.el-date-editor) {
    border-radius: 8px;
  }
  
  :deep(.el-divider__text) {
    background: #fff;
    color: #4facfe;
    font-weight: bold;
    font-size: 15px;
  }
}

/* 阵容单选组 */
.lineup-radio-group {
  :deep(.el-radio) {
    margin-right: 15px;
    margin-bottom: 10px;
  }
}

/* 子表格样式 */
.sub-table {
  margin-top: 15px;
  border-radius: 8px;
  overflow: hidden;
  
  :deep(.el-table__header-wrapper) {
    th {
      background: #f0f5ff !important;
      color: #4facfe;
      font-weight: bold;
    }
  }
}

/* 底部按钮 */
.dialog-footer {
  display: flex;
  justify-content: center;
  gap: 15px;
  
  .submit-btn {
    background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
    border: none;
    padding: 10px 30px;
    border-radius: 8px;
    font-weight: 500;
    
    &:hover {
      background: linear-gradient(135deg, #00f2fe 0%, #4facfe 100%);
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(79, 172, 254, 0.4);
    }
  }
  
  .cancel-btn {
    padding: 10px 30px;
    border-radius: 8px;
    font-weight: 500;
    
    &:hover {
      color: #4facfe;
      border-color: #4facfe;
      transform: translateY(-2px);
    }
  }
}

/* 响应式优化 */
@media screen and (max-width: 768px) {
  .member-container {
    padding: 10px;
  }
  
  .search-card,
  .operation-card {
    margin-bottom: 10px;
  }
}

/* 动画效果 */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.el-card {
  animation: fadeIn 0.5s ease;
}
</style>