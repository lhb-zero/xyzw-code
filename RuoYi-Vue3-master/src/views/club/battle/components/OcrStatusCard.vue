<template>
  <el-card class="ocr-status-card" shadow="hover">
    <!-- OCR服务状态窗口 -->
    <template #header>
      <div class="card-header">
        <span>OCR服务状态</span>
        <el-button size="small" @click="$emit('refresh-status')">刷新状态</el-button>
      </div>
    </template>
    
    <el-row :gutter="20">
      <el-col :span="8">
        <div class="status-item">
          <div class="status-label">服务连通性</div>
          <div class="status-value">
            <el-tag :type="ocrStatus.connected ? 'success' : 'danger'">
              {{ ocrStatus.connected ? '已连接' : '未连接' }}
            </el-tag>
          </div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="status-item">
          <div class="status-label">OCR引擎状态</div>
          <div class="status-value">
            <el-tag :type="ocrStatus.healthy ? 'success' : 'warning'">
              {{ ocrStatus.healthy ? '已初始化' : '未初始化' }}
            </el-tag>
          </div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="status-item">
          <div class="status-label">操作</div>
          <div class="status-value">
            <el-button size="mini" type="primary" @click="$emit('init-ocr')" :loading="ocrInitializing">
              初始化OCR
            </el-button>
          </div>
        </div>
      </el-col>
    </el-row>
  </el-card>
</template>

<script setup>
const props = defineProps({
  ocrStatus: {
    type: Object,
    required: true
  },
  ocrInitializing: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['refresh-status', 'init-ocr'])
</script>

<style scoped>
.ocr-status-card {
  margin-bottom: 20px;
}

.status-item {
  text-align: center;
}

.status-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.status-value {
  font-size: 16px;
  font-weight: bold;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>