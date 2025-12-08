<template>
  <el-card shadow="always" class="manual-input-card">
    <template #header>
      <div class="card-header">
        <span>手动输入战绩数据</span>
        <div>
          <el-button size="small" @click="clearInput">清空</el-button>
          <el-button size="small" type="primary" @click="parseAndPreview">解析并预览</el-button>
        </div>
      </div>
    </template>
    
    <div class="input-section" :class="{ 'collapsed': isInputCollapsed }">
      <div v-show="!isInputCollapsed">
        <div class="input-tips">
          <el-alert
            title="输入提示"
            type="info"
            show-icon
            :closable="false"
          >
            <p>1. 支持直接粘贴战绩文本或HTML表格</p>
            <p>2. 文本格式示例：昵称 击杀 刨地 死亡 复活 KD</p>
            <p>3. 表格格式：可直接从游戏截图复制HTML表格</p>
          </el-alert>
        </div>
      </div>
      
      <!-- 输入框控制按钮 -->
      <div class="input-toggle" v-if="parsedData.length > 0">
        <el-button size="small" type="text" @click="toggleInputBox">
          {{ isInputCollapsed ? '展开输入框' : '收起输入框' }}
        </el-button>
      </div>
      
      <div class="input-wrapper" v-show="!isInputCollapsed">
        <el-input
          v-model="inputText"
          type="textarea"
          :rows="15"
          placeholder="请粘贴战绩数据...&#10;例如：&#10;千寻、庆庆 38 28 16 10 2.38&#10;千寻、小八 37 28 15 9 2.47"
          class="input-textarea"
        />
        
        <div class="example-section">
          <el-collapse>
            <el-collapse-item title="查看输入示例">
              <div class="example-content">
                <h4>文本格式示例：</h4>
                <pre class="example-text">{{ textExample }}</pre>
                
                <h4>HTML表格格式示例：</h4>
                <pre class="example-text">{{ htmlExample }}</pre>
              </div>
            </el-collapse-item>
          </el-collapse>
        </div>
      </div>
    </div>
    
    <!-- 解析结果预览 -->
    <el-card v-if="parsedData.length > 0" shadow="hover" class="preview-section-card">
      <template #header>
        <div class="card-header">
          <span>解析结果预览 ({{ parsedData.length }} 条)</span>
        </div>
      </template>
      
      <el-table :data="parsedData" style="width: 100%" max-height="500" stripe>
        <el-table-column prop="nickname" label="昵称" min-width="20%" />
        <el-table-column prop="kills" label="击杀" min-width="16%" />
        <el-table-column prop="digs" label="刨地" min-width="16%" />
        <el-table-column prop="deaths" label="死亡" min-width="16%" />
        <el-table-column prop="revives" label="复活" min-width="16%" />
        <el-table-column prop="kdRatio" label="KD" min-width="16%">
          <template #default="scope">
            <el-tag :type="getKdTagType(scope.row.kdRatio)">
              {{ scope.row.kdRatio ? scope.row.kdRatio.toFixed(2) : '-' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      
      <el-collapse v-if="validationResult.warnings.length > 0" class="validation-section">
        <el-collapse-item title="查看数据验证警告">
          <el-alert
            title="数据验证警告"
            type="warning"
            show-icon
            :closable="false"
          >
            <ul>
              <li v-for="(warning, index) in validationResult.warnings" :key="index">
                {{ warning }}
              </li>
            </ul>
          </el-alert>
        </el-collapse-item>
      </el-collapse>
      
      <div class="action-buttons">
        <el-button icon="Edit" @click="editData">编辑数据</el-button>
        <el-button type="primary" icon="Check" @click="confirmData">确认并添加到战绩</el-button>
      </div>
    </el-card>
    
    <!-- 编辑数据对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑战绩数据"
      width="80%"
    >
      <el-table :data="editableData" style="width: 100%" max-height="60vh">
        <el-table-column label="昵称" min-width="100">
          <template #default="scope">
            <el-input v-model="scope.row.nickname" size="small" />
          </template>
        </el-table-column>
        <el-table-column label="击杀" width="140">
          <template #default="scope">
            <el-input-number v-model="scope.row.kills" size="small" :min="0" />
          </template>
        </el-table-column>
        <el-table-column label="刨地" width="140">
          <template #default="scope">
            <el-input-number v-model="scope.row.digs" size="small" :min="0" />
          </template>
        </el-table-column>
        <el-table-column label="死亡" width="140">
          <template #default="scope">
            <el-input-number v-model="scope.row.deaths" size="small" :min="0" />
          </template>
        </el-table-column>
        <el-table-column label="复活" width="140">
          <template #default="scope">
            <el-input-number v-model="scope.row.revives" size="small" :min="0" />
          </template>
        </el-table-column>
        <el-table-column label="KD" width="140">
          <template #default="scope">
            <el-input-number 
              v-model="scope.row.kdRatio" 
              size="small" 
              :min="0" 
              :step="0.01" 
              :precision="2"
              @change="recalculateKd(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="150">
          <template #default="scope">
            <el-button size="small" type="danger" @click="removeRow(scope.$index)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveEditedData">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { parseManualInputData, validateParsedData } from './utils/dataParser'

const emit = defineEmits(['data-confirmed'])

// 响应式数据
const inputText = ref('')
const parsedData = ref([])
const validationResult = reactive({ valid: true, errors: [], warnings: [] })
const editDialogVisible = ref(false)
const editableData = ref([])
const isInputCollapsed = ref(false)

// 示例文本
const textExample = `总计30人    416    786    296    124    1.41
千寻、庆庆    38    28    16    10    2.38
千寻、小八    37    28    15    9    2.47
千寻、哇哇叫    33    18    14    8    2.36
千寻、星辰    25    22    8    2    3.13`

// 示例HTML
const htmlExample = `<table border=1 style='margin: auto; width: max-content;'>
<tr><td style='text-align: center;'>昵称</td><td style='text-align: center;'>击杀</td><td style='text-align: center;'>创地</td><td style='text-align: center;'>死亡</td><td style='text-align: center;'>复活</td><td style='text-align: center;'>K/D比</td></tr>
<tr><td style='text-align: center;'>总计30人</td><td style='text-align: center;'>416</td><td style='text-align: center;'>786</td><td style='text-align: center;'>296</td><td style='text-align: center;'>124</td><td style='text-align: center;'>1.41</td></tr>
</table>`

// 清空输入
function clearInput() {
  inputText.value = ''
  parsedData.value = []
  isInputCollapsed.value = false
}

// 解析并预览数据
function parseAndPreview() {
  if (!inputText.value.trim()) {
    ElMessage.warning('请输入战绩数据')
    return
  }
  
  try {
    // 解析数据
    const data = parseManualInputData(inputText.value)
    
    // 验证数据
    const validation = validateParsedData(data)
    Object.assign(validationResult, validation)
    
    if (!validation.valid) {
      ElMessage.error('数据解析失败：' + validation.errors.join(', '))
      return
    }
    
    parsedData.value = data
    
    // 收起输入框
    isInputCollapsed.value = true
    
    if (validation.warnings.length > 0) {
      ElMessage.warning('数据解析成功，但有一些警告')
    } else {
      ElMessage.success('数据解析成功')
    }
  } catch (error) {
    console.error('解析数据时出错:', error)
    ElMessage.error('解析数据时出错：' + error.message)
  }
}

// 切换输入框展开/收起状态
function toggleInputBox() {
  isInputCollapsed.value = !isInputCollapsed.value
}

// 获取KD值的标签类型
function getKdTagType(kdRatio) {
  if (!kdRatio || kdRatio === 0) return 'info'
  if (kdRatio < 1) return 'danger'
  if (kdRatio < 2) return 'warning'
  if (kdRatio < 3) return 'success'
  return 'primary'
}

// 编辑数据
function editData() {
  if (parsedData.value.length === 0) {
    ElMessage.warning('没有可编辑的数据')
    return
  }
  
  // 深拷贝数据以避免直接修改
  editableData.value = JSON.parse(JSON.stringify(parsedData.value))
  editDialogVisible.value = true
}

// 重新计算KD
function recalculateKd(row) {
  if (row.deaths > 0) {
    row.kdRatio = parseFloat((row.kills / row.deaths).toFixed(2))
  } else if (row.kills > 0) {
    row.kdRatio = row.kills // 如果死亡为0，KD等于击杀数
  } else {
    row.kdRatio = 0
  }
}

// 删除行
function removeRow(index) {
  editableData.value.splice(index, 1)
}

// 保存编辑后的数据
function saveEditedData() {
  parsedData.value = JSON.parse(JSON.stringify(editableData.value))
  editDialogVisible.value = false
  ElMessage.success('数据已保存')
}

// 确认数据
function confirmData() {
  if (parsedData.value.length === 0) {
    ElMessage.warning('没有可确认的数据')
    return
  }
  
  // 再次验证数据
  const validation = validateParsedData(parsedData.value)
  Object.assign(validationResult, validation)
  
  if (!validation.valid) {
    ElMessage.error('数据验证失败：' + validation.errors.join(', '))
    return
  }
  
  // 发送数据到父组件
  emit('data-confirmed', parsedData.value)
  
  // 清空输入和解析结果
  clearInput()
  ElMessage.success('数据已添加到战绩列表')
}
</script>

<style scoped>
.manual-input-card {
  margin-bottom: 20px;
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  border-radius: 8px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #f0f2f5;
  padding: 10px 15px;
  margin: -20px -20px 15px -20px;
  border-radius: 8px 8px 0 0;
  font-weight: bold;
  color: #303133;
  border-bottom: 2px solid #e4e7ed;
}

.input-section {
  margin-top: 15px;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.input-section.collapsed {
  padding: 10px 15px;
  min-height: auto;
}

.input-tips {
  margin-bottom: 15px;
}

.input-toggle {
  margin: 10px 0;
  text-align: center;
  padding: 8px 0;
  background-color: #e6f7ff;
  border-radius: 4px;
  border: 1px dashed #91d5ff;
  cursor: pointer;
  transition: all 0.2s ease;
}

.input-toggle:hover {
  background-color: #bae7ff;
  border-color: #69c0ff;
}

.input-wrapper {
  animation: fadeIn 0.3s ease-in-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}

.input-textarea {
  margin-bottom: 15px;
  border-radius: 6px;
}

.example-section {
  margin-top: 15px;
}

.example-content {
  padding: 10px;
  background-color: #f9f9f9;
  border-radius: 4px;
  border: 1px solid #e0e0e0;
}

.example-text {
  background-color: #fff;
  padding: 10px;
  border-radius: 4px;
  border: 1px solid #eaeaea;
  white-space: pre-wrap;
  word-break: break-all;
  font-family: monospace;
  font-size: 12px;
  line-height: 1.5;
  color: #606266;
  margin: 10px 0;
  max-height: 200px;
  overflow-y: auto;
  box-shadow: inset 0 1px 3px rgba(0,0,0,0.05);
}

.preview-section-card {
  margin-top: 20px;
  background: linear-gradient(135deg, #ffffff 0%, #f5f7fa 100%);
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
}

.preview-section-card .card-header {
  background-color: #e9f5ff;
  border-bottom: 2px solid #c3e1ff;
}

.validation-section {
  margin: 15px 0;
}

.action-buttons {
  margin-top: 20px;
  text-align: right;
  padding: 15px 0 5px;
  border-top: 1px dashed #e0e0e0;
}

.action-buttons .el-button {
  margin-left: 10px;
  border-radius: 6px;
  padding: 8px 15px;
  font-weight: 500;
  transition: all 0.2s ease;
}

.action-buttons .el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
}

/* 表格样式增强 */
.el-table {
  border-radius: 6px;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0,0,0,0.05);
}

.el-table th {
  background-color: #f5f7fa;
  font-weight: 600;
  color: #303133;
}

/* 标签样式增强 */
.el-tag {
  border-radius: 12px;
  font-weight: 500;
  padding: 0 8px;
}
</style>