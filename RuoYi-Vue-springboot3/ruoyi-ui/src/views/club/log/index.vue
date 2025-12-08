<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryFormRef" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="成员ID" prop="memberId">
        <el-input
          v-model="queryParams.memberId"
          placeholder="请输入成员ID"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="日志类型" prop="logType">
        <el-select v-model="queryParams.logType" placeholder="请选择日志类型" clearable>
          <el-option
            v-for="dict in dict.type.club_log_type"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="资源类型" prop="resourceType">
        <el-select v-model="queryParams.resourceType" placeholder="请选择资源类型" clearable>
          <el-option
            v-for="dict in dict.type.club_resource_type"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="变化日期">
        <el-date-picker
          v-model="dateRange"
          style="width: 240px"
          value-format="yyyy-MM-dd"
          type="daterange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
        ></el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" size="small" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" size="small" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Plus"
          size="small"
          @click="handleAdd"
          v-hasPermi="['club:log:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          size="small"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['club:log:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          size="small"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['club:log:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          size="small"
          @click="handleExport"
          v-hasPermi="['club:log:export']"
        >导出</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="info"
          plain
          icon="DataLine"
          size="small"
          @click="showCompareDialog"
          v-hasPermi="['club:log:list']"
        >资源对比</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="logList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="日志ID" align="center" prop="logId" />
      <el-table-column label="成员ID" align="center" prop="memberId" />
      <el-table-column label="日志类型" align="center" prop="logType">
        <template #default="scope">
          <dict-tag :options="dict.type.club_log_type" :value="scope.row.logType"/>
        </template>
      </el-table-column>
      <el-table-column label="资源类型" align="center" prop="resourceType">
        <template #default="scope">
          <dict-tag :options="dict.type.club_resource_type" :value="scope.row.resourceType"/>
        </template>
      </el-table-column>
      <el-table-column label="变化数量" align="center" prop="changeAmount" />
      <el-table-column label="变化前" align="center" prop="beforeAmount" />
      <el-table-column label="变化后" align="center" prop="afterAmount" />
      <el-table-column label="变化时间" align="center" prop="changeTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.changeTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button
            size="small"
            type="text"
            icon="Edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['club:log:edit']"
          >修改</el-button>
          <el-button
            size="small"
            type="text"
            icon="Delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['club:log:remove']"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <pagination
      v-show="total>0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改资源日志对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="成员ID" prop="memberId">
          <el-input v-model="form.memberId" placeholder="请输入成员ID" />
        </el-form-item>
        <el-form-item label="日志类型" prop="logType">
          <el-select v-model="form.logType" placeholder="请选择日志类型">
            <el-option
              v-for="dict in dict.type.club_log_type"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="资源类型" prop="resourceType">
          <el-select v-model="form.resourceType" placeholder="请选择资源类型">
            <el-option
              v-for="dict in dict.type.club_resource_type"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="变化数量" prop="changeAmount">
          <el-input v-model="form.changeAmount" placeholder="请输入变化数量" />
        </el-form-item>
        <el-form-item label="变化前" prop="beforeAmount">
          <el-input v-model="form.beforeAmount" placeholder="请输入变化前" />
        </el-form-item>
        <el-form-item label="变化后" prop="afterAmount">
          <el-input v-model="form.afterAmount" placeholder="请输入变化后" />
        </el-form-item>
        <el-form-item label="变化时间" prop="changeTime">
          <el-date-picker clearable
            v-model="form.changeTime"
            type="date"
            value-format="yyyy-MM-dd"
            placeholder="请选择变化时间">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 资源对比弹窗 -->
    <el-dialog title="资源变化对比" v-model="compareDialogVisible" width="800px" append-to-body>
      <div class="compare-container">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-card shadow="always">
              <template #header>
                <div class="clearfix">
                  <span>选择成员</span>
                </div>
              </template>
              <el-select v-model="compareMemberIds" multiple placeholder="选择要对比的成员" style="width: 100%">
                <el-option
                  v-for="member in memberList"
                  :key="member.memberId"
                  :label="`成员${member.memberId}`"
                  :value="member.memberId"
                ></el-option>
              </el-select>
              <div class="mt-10">
                <el-date-picker
                  v-model="compareDateRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  value-format="yyyy-MM-dd"
                  style="width: 100%">
                </el-date-picker>
              </div>
              <div class="mt-10">
                <el-select v-model="compareResourceType" placeholder="选择资源类型" style="width: 100%">
                  <el-option
                    v-for="dict in dict.type.club_resource_type"
                    :key="dict.value"
                    :label="dict.label"
                    :value="dict.value"
                  ></el-option>
                </el-select>
              </div>
              <div class="mt-20">
                <el-button type="primary" @click="loadCompareData">加载数据</el-button>
              </div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card shadow="always">
              <template #header>
                <div class="clearfix">
                  <span>对比结果</span>
                </div>
              </template>
              <div v-if="compareDataLoaded">
                <div ref="compareChartRef" class="chart-container"></div>
              </div>
              <div v-else class="empty-tip">
                请选择参数并点击加载数据
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </el-dialog>
  </div>
</template>

<script setup name="Log">
import { listLog, getLog, delLog, addLog, updateLog, exportLog, getResourceComparison, getMemberList } from "@/api/club/log";
import * as echarts from 'echarts';
import { ref, reactive, onMounted, nextTick, getCurrentInstance } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';

const { proxy } = getCurrentInstance();

// 遮罩层
const loading = ref(true);
// 选中数组
const ids = ref([]);
// 非单个禁用
const single = ref(true);
// 非多个禁用
const multiple = ref(true);
// 显示搜索条件
const showSearch = ref(true);
// 总条数
const total = ref(0);
// 资源日志表格数据
const logList = ref([]);
// 弹出层标题
const title = ref("");
// 是否显示弹出层
const open = ref(false);
// 日期范围
const dateRange = ref([]);
// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  memberId: null,
  logType: null,
  resourceType: null,
});
// 表单参数
const form = ref({});
// 表单校验
const rules = reactive({
  memberId: [
    { required: true, message: "成员ID不能为空", trigger: "blur" }
  ],
  logType: [
    { required: true, message: "日志类型不能为空", trigger: "change" }
  ],
  resourceType: [
    { required: true, message: "资源类型不能为空", trigger: "change" }
  ],
  changeAmount: [
    { required: true, message: "变化数量不能为空", trigger: "blur" }
  ],
  changeTime: [
    { required: true, message: "变化时间不能为空", trigger: "blur" }
  ],
  createBy: [
    { required: true, message: "创建者不能为空", trigger: "blur" }
  ]
});
// 对比弹窗
const compareDialogVisible = ref(false);
const compareMemberIds = ref([]);
const compareDateRange = ref([]);
const compareResourceType = ref('');
const compareChart = ref(null);
const compareChartInstance = ref(null);
const compareDataLoaded = ref(false);
const memberList = ref([]);

// 日期范围添加函数
function addDateRange(params = {}, dateRange) {
  const search = params;
  if (null != dateRange && '' != dateRange) {
    search.params = typeof (search.params) === 'object' && search.params !== null && !Array.isArray(search.params) ? search.params : {};
    search.params['beginTime'] = dateRange[0];
    search.params['endTime'] = dateRange[1];
  }
  return search;
}

onMounted(() => {
  getList();
  loadMembers();
});

/** 查询资源日志列表 */
function getList() {
  loading.value = true;
  const params = addDateRange(queryParams, dateRange.value);
  listLog(params).then(response => {
    logList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

// 取消按钮
function cancel() {
  open.value = false;
  reset();
}

// 表单重置
function reset() {
  form.value = {
    logId: null,
    memberId: null,
    logType: null,
    resourceType: null,
    changeAmount: null,
    beforeAmount: null,
    afterAmount: null,
    changeTime: null,
    remark: null,
    createBy: null,
    createTime: null,
    updateBy: null,
    updateTime: null
  };
  proxy.resetForm("formRef");
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.pageNum = 1;
  getList();
}

/** 重置按钮操作 */
function resetQuery() {
  dateRange.value = [];
  proxy.resetForm("queryFormRef");
  handleQuery();
}

// 多选框选中数据
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.logId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset();
  open.value = true;
  title.value = "添加资源日志";
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  const logId = row.logId || ids.value
  getLog(logId).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改资源日志";
  });
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    if (valid) {
      if (form.value.logId != null) {
        updateLog(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addLog(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功");
          open.value = false;
          getList();
        });
      }
    }
  });
}

/** 删除按钮操作 */
function handleDelete(row) {
  const logIds = row.logId || ids.value;
  proxy.$modal.confirm('是否确认删除资源日志编号为"' + logIds + '"的数据项？').then(function() {
    return delLog(logIds);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

/** 导出按钮操作 */
function handleExport() {
  const params = addDateRange(queryParams, dateRange.value);
  proxy.download('club/log/export', params, `log_${new Date().getTime()}.xlsx`)
}

// 显示资源对比弹窗
function showCompareDialog() {
  compareDialogVisible.value = true;
  nextTick(() => {
    initCompareChart();
  });
}

// 初始化对比图表
function initCompareChart() {
  if (compareChart.value) {
    compareChartInstance.value = echarts.init(compareChart.value);
    const option = {
      title: {
        text: '资源变化对比'
      },
      tooltip: {
        trigger: 'axis'
      },
      legend: {
        data: []
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        boundaryGap: false
      },
      yAxis: {
        type: 'value'
      },
      series: []
    };
    compareChartInstance.value.setOption(option);
  }
}

// 加载成员列表
function loadMembers() {
  getMemberList().then(response => {
    if (response.code === 200) {
      memberList.value = response.data;
    }
  });
}

// 加载对比数据
function loadCompareData() {
  if (!compareMemberIds.value.length || !compareDateRange.value.length || !compareResourceType.value) {
    ElMessage.error("请选择完整的对比参数");
    return;
  }

  const params = {
    memberIds: compareMemberIds.value.join(','),
    startDate: compareDateRange.value[0],
    endDate: compareDateRange.value[1],
    resourceType: compareResourceType.value
  };

  getResourceComparison(params).then(response => {
    if (response.code === 200) {
      renderCompareChart(response.data);
      compareDataLoaded.value = true;
    }
  });
}

// 渲染对比图表
function renderCompareChart(data) {
  if (!compareChartInstance.value) {
    initCompareChart();
  }

  const dates = [...new Set(data.flatMap(item => item.changeHistory.map(h => h.date)))].sort();
  const series = compareMemberIds.value.map(memberId => {
    const memberData = data.find(item => item.memberId == memberId);
    const changeHistory = memberData ? memberData.changeHistory : [];
    const values = dates.map(date => {
      const record = changeHistory.find(h => h.date === date);
      return record ? record.resourceAmount : 0;
    });

    return {
      name: `成员${memberId}`,
      type: 'line',
      data: values
    };
  });

  const option = {
    title: {
      text: '资源变化对比'
    },
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: compareMemberIds.value.map(id => `成员${id}`)
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: dates
    },
    yAxis: {
      type: 'value',
      name: '数量'
    },
    series: series
  };

  compareChartInstance.value.setOption(option);
}
</script>

<style scoped>
.mt-10 {
  margin-top: 10px;
}

.mt-20 {
  margin-top: 20px;
}

.compare-container {
  padding: 20px;
}

.chart-container {
  width: 100%;
  height: 300px;
}

.empty-tip {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 300px;
  color: #909399;
  font-size: 14px;
}
</style>