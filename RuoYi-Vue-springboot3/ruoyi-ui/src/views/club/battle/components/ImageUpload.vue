<template>
  <el-card shadow="always">
    <template #header>
      <div class="card-header">
        <span>盐场战报图片上传</span>
        <el-tag type="info" size="small">已上传 {{ uploadedImages.length }} 张</el-tag>
      </div>
    </template>
    
    <el-upload
      class="upload-demo"
      drag
      action=""
      :http-request="handleUpload"
      :show-file-list="false"
      accept=".jpg,.png,.jpeg"
      :disabled="!ocrStatus.connected"
    >
      <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
      <div class="el-upload__text">
        将盐场战报截图拖到此处，或<em>点击上传</em>
      </div>
      <template #tip>
        <div class="el-upload__tip">
          支持jpg/png文件，且不超过10MB
          <span v-if="!ocrStatus.connected" class="error-tip">
            （请确保OCR服务已启动）
          </span>
        </div>
      </template>
    </el-upload>
    
    <!-- 单张图片展示 -->
    <div v-if="uploadedImages.length > 0" class="uploaded-images">
      <h4>已上传图片</h4>
      <div 
        v-for="(image, index) in uploadedImages" 
        :key="index"
        class="image-container"
      >
        <!-- 识别进度 -->
        <div v-if="image.status === 'processing'" class="image-overlay">
          <el-progress 
            type="circle" 
            :percentage="image.progress || 0" 
            :width="80" 
            :stroke-width="6"
          />
          <span class="progress-text">识别中...</span>
        </div>
        
        <!-- 识别状态图标 -->
        <div v-else-if="image.status === 'matched'" class="image-status success">
          <el-icon :size="28"><SuccessFilled /></el-icon>
        </div>
        
        <div v-else-if="image.status === 'failed'" class="image-status error">
          <el-icon :size="28"><CircleCloseFilled /></el-icon>
        </div>
        
        <!-- 图片预览 -->
        <div class="image-preview-wrapper">
          <img 
            :src="image.imageUrl" 
            class="image-preview" 
            @error="handleImageError($event, image)"
          />
        </div>
        
        <!-- 图片信息和操作按钮 -->
        <div class="image-info">
          <div class="image-details">
            <p class="filename"><strong>文件名：</strong>{{ image.filename }}</p>
            <p><strong>识别状态：</strong>
              <el-tag :type="getStatusType(image.status)" size="small">
                {{ getStatusText(image.status) }}
              </el-tag>
            </p>
            <p v-if="image.processingTime">
              <strong>处理时间：</strong>{{ image.processingTime }}秒
            </p>
          </div>
          
          <div class="image-actions">
            <el-button 
              v-if="image.status === 'matched'" 
              size="small" 
              type="primary"
              @click="$emit('view-ocr-result', index)"
            >
              <el-icon><View /></el-icon> 查看识别结果
            </el-button>
            
            <el-button 
              size="small" 
              @click="openFloatingPreview(index)"
            >
              <el-icon><FullScreen /></el-icon> 预览图片
            </el-button>
            
            <el-button 
              size="small" 
              type="danger"
              @click="deleteImage(index)"
            >
              <el-icon><Delete /></el-icon> 删除图片
            </el-button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 浮动图片预览窗口 -->
    <div v-if="previewVisible" class="floating-preview" 
         :style="{ 
           left: windowPosition.left + 'px', 
           top: windowPosition.top + 'px',
           width: windowSize.width + 'px',
           height: windowSize.height + 'px'
         }">
      <div class="preview-header" @mousedown="startDrag">
        <span class="preview-title">{{ currentImage.filename }}</span>
        <div class="header-controls">
          <el-button circle size="small" @click.stop="zoomIn" title="放大">
            <el-icon><ZoomIn /></el-icon>
          </el-button>
          <el-button circle size="small" @click.stop="zoomOut" title="缩小">
            <el-icon><ZoomOut /></el-icon>
          </el-button>
          <el-button circle size="small" @click.stop="resetZoom" title="重置">
            <el-icon><RefreshRight /></el-icon>
          </el-button>
          <el-button circle size="small" @click.stop="closePreview" title="关闭">
            <el-icon><Close /></el-icon>
          </el-button>
        </div>
      </div>
      
      <div class="preview-content" ref="previewContentRef" @wheel="handleWheel">
        <div class="image-container">
          <img 
            :src="currentImage.imageUrl" 
            class="preview-image"
            :style="{ transform: `scale(${zoomLevel}) translate(${translateX}px, ${translateY}px)` }"
            @mousedown="startImageDrag"
          />
        </div>
      </div>
      
      <div class="preview-footer">
        <div class="resize-handle" @mousedown="startResize"></div>
        <div class="footer-actions">
          <el-button v-if="currentImage.status === 'matched'" type="primary" size="small" @click.stop="$emit('view-ocr-result', currentIndex)">
            <el-icon><View /></el-icon> 查看识别结果
          </el-button>
          <el-button type="danger" size="small" @click.stop="deleteFromPreview">
            <el-icon><Delete /></el-icon> 删除图片
          </el-button>
        </div>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { ref, computed, watch, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  UploadFilled, 
  SuccessFilled, 
  CircleCloseFilled,
  View,
  FullScreen,
  Delete,
  ZoomIn,
  ZoomOut,
  RefreshRight,
  Close
} from '@element-plus/icons-vue'

const props = defineProps({
  uploadedImages: {
    type: Array,
    default: () => []
  },
  ocrStatus: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['handle-upload', 'view-ocr-result', 'delete-image'])

// 浮动预览相关
const previewVisible = ref(false)
const currentIndex = ref(0)
const currentImage = computed(() => {
  return props.uploadedImages[currentIndex.value] || {}
})
const zoomLevel = ref(1)
const translateX = ref(0)
const translateY = ref(0)

// 窗口位置和大小
const windowPosition = ref({ left: 100, top: 100 })
const windowSize = ref({ width: 450, height: 350 })

// 拖拽和缩放状态
const isDragging = ref(false)
const isResizing = ref(false)
const isImageDragging = ref(false)
const dragStart = ref({ x: 0, y: 0 })
const windowStart = ref({ left: 0, top: 0, width: 0, height: 0 })
const imageDragStart = ref({ x: 0, y: 0 })
const imageStart = ref({ x: 0, y: 0 })
const previewContentRef = ref(null)

// 处理图片上传
const handleUpload = (options) => {
  emit('handle-upload', options)
}

// 处理超出上传限制（已移除限制，此函数不再使用）
const handleExceed = () => {
  // 不再限制上传数量
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    'processing': 'warning',
    'matched': 'success',
    'failed': 'danger'
  }
  return typeMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    'processing': '识别中',
    'matched': '已匹配',
    'failed': '识别失败'
  }
  return textMap[status] || '未知状态'
}

// 删除图片
const deleteImage = (index) => {
  // 直接删除，不再显示确认框，由父组件处理确认
  emit('delete-image', index)
}

// 打开浮动预览
const openFloatingPreview = (index) => {
  currentIndex.value = index
  zoomLevel.value = 1
  translateX.value = 0
  translateY.value = 0
  
  // 确保窗口在可见区域内
  const windowWidth = window.innerWidth
  const windowHeight = window.innerHeight
  
  if (windowSize.value.width > windowWidth - 40) {
    windowSize.value.width = Math.min(windowWidth - 40, 800)
  }
  if (windowSize.value.height > windowHeight - 40) {
    windowSize.value.height = Math.min(windowHeight - 40, 600)
  }
  
  if (windowPosition.value.left + windowSize.value.width > windowWidth - 20) {
    windowPosition.value.left = Math.max(20, windowWidth - windowSize.value.width - 20)
  }
  if (windowPosition.value.top + windowSize.value.height > windowHeight - 20) {
    windowPosition.value.top = Math.max(20, windowHeight - windowSize.value.height - 20)
  }
  
  previewVisible.value = true
  // 添加键盘事件监听，确保捕获阶段
  document.addEventListener('keydown', handleKeyDown, true)
}

// 关闭浮动预览
const closePreview = () => {
  previewVisible.value = false
  zoomLevel.value = 1
  translateX.value = 0
  translateY.value = 0
  // 移除键盘事件监听
  document.removeEventListener('keydown', handleKeyDown, true)
}

// 从预览中删除图片
const deleteFromPreview = () => {
  const indexToDelete = currentIndex.value
  closePreview()
  // 直接删除，不再重复确认
  emit('delete-image', indexToDelete)
}

// 放大图片
const zoomIn = () => {
  if (zoomLevel.value < 5) {
    zoomLevel.value += 0.25
  }
}

// 缩小图片
const zoomOut = () => {
  if (zoomLevel.value > 0.5) {
    zoomLevel.value -= 0.25
  }
}

// 重置缩放
const resetZoom = () => {
  zoomLevel.value = 1
  translateX.value = 0
  translateY.value = 0
}

// 处理键盘事件
const handleKeyDown = (event) => {
  // 只有在预览窗口可见时才处理键盘事件
  if (!previewVisible.value) return
  
  console.log('handleKeyDown called, key:', event.key)
  
  switch (event.key) {
    case 'Escape':
      closePreview()
      break
    case '+':
    case '=':
      zoomIn()
      break
    case '-':
    case '_':
      zoomOut()
      break
    case '0':
      resetZoom()
      break
  }
}

// 窗口拖拽
const startDrag = (event) => {
  isDragging.value = true
  dragStart.value = {
    x: event.clientX,
    y: event.clientY
  }
  windowStart.value = { ...windowPosition.value }
  
  const handleMouseMove = (e) => {
    if (!isDragging.value) return
    
    windowPosition.value = {
      left: windowStart.value.left + (e.clientX - dragStart.value.x),
      top: windowStart.value.top + (e.clientY - dragStart.value.y)
    }
  }
  
  const handleMouseUp = () => {
    isDragging.value = false
    document.removeEventListener('mousemove', handleMouseMove)
    document.removeEventListener('mouseup', handleMouseUp)
  }
  
  document.addEventListener('mousemove', handleMouseMove)
  document.addEventListener('mouseup', handleMouseUp)
}

// 窗口大小调整
const startResize = (event) => {
  isResizing.value = true
  dragStart.value = {
    x: event.clientX,
    y: event.clientY
  }
  windowStart.value = { ...windowSize.value }
  
  const handleMouseMove = (e) => {
    if (!isResizing.value) return
    
    windowSize.value = {
      width: Math.max(300, windowStart.value.width + (e.clientX - dragStart.value.x)),
      height: Math.max(200, windowStart.value.height + (e.clientY - dragStart.value.y))
    }
  }
  
  const handleMouseUp = () => {
    isResizing.value = false
    document.removeEventListener('mousemove', handleMouseMove)
    document.removeEventListener('mouseup', handleMouseUp)
  }
  
  document.addEventListener('mousemove', handleMouseMove)
  document.addEventListener('mouseup', handleMouseUp)
}

// 图片拖拽
const startImageDrag = (event) => {
  event.preventDefault()
  isImageDragging.value = true
  imageDragStart.value = {
    x: event.clientX,
    y: event.clientY
  }
  imageStart.value = {
    x: translateX.value,
    y: translateY.value
  }
  
  const handleMouseMove = (e) => {
    if (!isImageDragging.value) return
    
    // 降低拖拽灵敏度，只有移动超过3px才开始移动
    const deltaX = (e.clientX - imageDragStart.value.x) * 0.7
    const deltaY = (e.clientY - imageDragStart.value.y) * 0.7
    
    translateX.value = imageStart.value.x + deltaX
    translateY.value = imageStart.value.y + deltaY
  }
  
  const handleMouseUp = () => {
    isImageDragging.value = false
    document.removeEventListener('mousemove', handleMouseMove)
    document.removeEventListener('mouseup', handleMouseUp)
  }
  
  document.addEventListener('mousemove', handleMouseMove)
  document.addEventListener('mouseup', handleMouseUp)
}

// 鼠标滚轮缩放
const handleWheel = (event) => {
  event.preventDefault()
  event.stopPropagation()
  
  const delta = event.deltaY > 0 ? -0.1 : 0.1
  const newZoom = zoomLevel.value + delta
  
  if (newZoom >= 0.5 && newZoom <= 5) {
    zoomLevel.value = newZoom
  }
}

// 处理图片加载错误
const handleImageError = (event, image) => {
  // 显示占位图片
  event.target.src = '/static/images/image-placeholder.png'
  ElMessage.error('图片加载失败，可能图片已损坏或不存在')
}

// 模拟识别进度
const simulateProgress = () => {
  props.uploadedImages.forEach(image => {
    if (image.status === 'processing') {
      if (!image.progress) {
        image.progress = 0
      }
      if (image.progress < 90) {
        image.progress += Math.random() * 10
      }
    }
  })
}

// 设置定时器，模拟进度更新
let progressTimer = null

// 启动进度模拟
const startProgressSimulation = () => {
  if (progressTimer) {
    clearInterval(progressTimer)
  }
  progressTimer = setInterval(simulateProgress, 500)
}

// 停止进度模拟
const stopProgressSimulation = () => {
  if (progressTimer) {
    clearInterval(progressTimer)
    progressTimer = null
  }
}

// 监听上传图片的变化，如果有正在处理的图片，启动进度模拟
watch(
  () => props.uploadedImages.some(img => img.status === 'processing'),
  (hasProcessing) => {
    if (hasProcessing) {
      startProgressSimulation()
    } else {
      stopProgressSimulation()
    }
  },
  { immediate: true }
)

// 组件卸载时清除定时器和事件监听
onBeforeUnmount(() => {
  stopProgressSimulation()
  document.removeEventListener('keydown', handleKeyDown, true)
})
</script>

<style scoped>
/* 基础样式 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.error-tip {
  color: #F56C6C;
  font-weight: bold;
}

.uploaded-images {
  margin-top: 20px;
}

.uploaded-images h4 {
  margin-bottom: 15px;
  font-size: 16px;
  font-weight: bold;
}

/* 图片容器样式 */
.image-container {
  display: flex;
  margin-bottom: 20px;
  border: 1px solid #EBEEF5;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  transition: box-shadow 0.3s ease;
}

.image-container:hover {
  box-shadow: 0 4px 18px 0 rgba(0, 0, 0, 0.15);
}

.image-preview-wrapper {
  flex: 0 0 40%;
  position: relative;
  background-color: #F5F7FA;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 200px;
}

.image-preview {
  max-width: 100%;
  max-height: 300px;
  object-fit: contain;
  border-radius: 4px;
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: white;
}

.progress-text {
  margin-top: 10px;
  font-size: 14px;
  font-weight: bold;
}

.image-status {
  position: absolute;
  top: 10px;
  right: 10px;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: rgba(255, 255, 255, 0.8);
  z-index: 2;
}

.image-status.success {
  color: #67C23A;
}

.image-status.error {
  color: #F56C6C;
}

.image-info {
  flex: 1;
  padding: 15px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.image-details p {
  margin: 8px 0;
  font-size: 14px;
  line-height: 1.5;
}

.filename {
  word-break: break-all;
}

.image-actions {
  display: flex;
  gap: 10px;
  margin-top: 15px;
  flex-wrap: wrap;
}

/* 浮动预览窗口样式 */
.floating-preview {
  position: fixed;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  z-index: 9999;
  display: flex;
  flex-direction: column;
  min-width: 300px;
  min-height: 200px;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 15px;
  background-color: #F5F7FA;
  border-bottom: 1px solid #EBEEF5;
  border-top-left-radius: 8px;
  border-top-right-radius: 8px;
  cursor: move;
  user-select: none;
}

.preview-title {
  font-weight: bold;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 70%;
}

.header-controls {
  display: flex;
  gap: 5px;
}

.header-controls .el-button {
  padding: 5px;
  background-color: transparent;
  border: none;
}

.header-controls .el-button:hover {
  background-color: rgba(0, 0, 0, 0.05);
}

.preview-content {
  flex: 1;
  overflow: hidden;
  position: relative;
}

.image-container {
  width: 100%;
  height: 100%;
  overflow: hidden;
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #F5F7FA;
}

.preview-image {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  cursor: grab;
  transition: transform 0.1s ease;
}

.preview-image:active {
  cursor: grabbing;
}

.preview-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 15px;
  background-color: #F5F7FA;
  border-top: 1px solid #EBEEF5;
  border-bottom-left-radius: 8px;
  border-bottom-right-radius: 8px;
}

.resize-handle {
  width: 20px;
  height: 20px;
  background-image: linear-gradient(135deg, transparent 50%, #C0C4CC 50%);
  background-size: 8px 8px;
  background-repeat: no-repeat;
  background-position: bottom right;
  cursor: nwse-resize;
  position: absolute;
  bottom: 0;
  right: 0;
}

.footer-actions {
  display: flex;
  gap: 10px;
}

/* 响应式设计 */
/* 响应式设计 */
@media (max-width: 768px) {
  .image-container {
    flex-direction: column;
  }
  
  .image-preview-wrapper {
    flex: 0 0 auto;
    width: 100%;
    min-height: 180px;
  }
  
  .image-info {
    padding: 15px;
  }
  
  .image-actions {
    justify-content: center;
  }
  
  .image-actions .el-button {
    flex: 1;
    margin: 0 5px;
  }
  
  .floating-preview {
    width: 90vw !important;
    height: 70vh !important;
    left: 5vw !important;
    top: 15vh !important;
  }
  
  .preview-title {
    font-size: 14px;
  }
  
  .footer-actions {
    flex-direction: column;
    gap: 5px;
    align-items: center;
  }
  
  .footer-actions .el-button {
    width: 80%;
    max-width: 200px;
  }
}

@media (max-width: 480px) {
  .image-details p {
    font-size: 12px;
  }
  
  .image-actions .el-button {
    font-size: 12px;
  }
  
  .preview-title {
    font-size: 12px;
  }
  
  .header-controls .el-button {
    padding: 3px;
  }
}
</style>