<template>
  <el-dialog title="OCR识别结果" v-model="dialogVisible" width="60%" @close="handleClose">
    <div class="ocr-result-container">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="文件名">{{ currentOcrResult.filename }}</el-descriptions-item>
        <el-descriptions-item label="处理时间">{{ currentOcrResult.processingTime }}秒</el-descriptions-item>
      </el-descriptions>
      
      <div class="mt-20">
        <h4>识别文本</h4>
        <el-input type="textarea" :rows="10" v-model="currentOcrResult.text" readonly></el-input>
      </div>
      
      <div class="mt-20">
        <h4>识别行</h4>
        <el-tag 
          v-for="(line, index) in currentOcrResult.lines" 
          :key="index" 
          class="line-tag"
        >
          {{ line }}
        </el-tag>
      </div>
    </div>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="dialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="$emit('reprocess-image')">重新处理</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  currentOcrResult: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['update:visible', 'reprocess-image'])

const dialogVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

const handleClose = () => {
  emit('update:visible', false)
}
</script>

<style scoped>
.mt-20 {
  margin-top: 20px;
}

.ocr-result-container {
  max-height: 500px;
  overflow-y: auto;
}

.line-tag {
  margin: 5px;
}

.dialog-footer {
  text-align: right;
}
</style>