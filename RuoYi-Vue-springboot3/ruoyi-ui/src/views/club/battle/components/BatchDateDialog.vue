<template>
  <!-- 一键设置时间组件 -->
  <el-dialog title="一键设置时间" v-model="dialogVisible" width="400px" @close="handleClose">
    <el-form label-width="80px">
      <el-form-item label="选择日期">
<el-date-picker
  v-model="selectedDate"
  type="date"
  placeholder="选择日期"
  format="YYYY-MM-DD"
  value-format="YYYY-MM-DD"
  style="width: 100%">
</el-date-picker>
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="applyBatchDate" :disabled="!selectedDate">
          应用到所有记录
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:visible', 'apply-batch-date'])

const dialogVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

const selectedDate = ref('')

// 当对话框打开时，默认设置为当前日期
watch(() => props.visible, (newVal) => {
  if (newVal) {
    selectedDate.value = new Date().toISOString().split('T')[0]
  }
})

const handleClose = () => {
  emit('update:visible', false)
}

const applyBatchDate = () => {
  if (!selectedDate.value) {
    ElMessage.warning('请选择日期')
    return
  }
  
  emit('apply-batch-date', selectedDate.value)
  dialogVisible.value = false
}
</script>

<style scoped>
.dialog-footer {
  text-align: right;
}
</style>