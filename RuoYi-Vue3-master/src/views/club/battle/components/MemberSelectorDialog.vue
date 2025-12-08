<template>
  <el-dialog title="选择俱乐部成员" v-model="dialogVisible" width="50%" @close="handleClose">
    <div class="member-selector-container">
      <el-input 
        v-model="memberSearchText" 
        placeholder="搜索成员昵称或游戏ID" 
        prefix-icon="Search"
        clearable
      ></el-input>
      
      <div class="member-list mt-10">
        <el-radio-group v-model="selectedMemberId">
          <div 
            v-for="member in filteredMembers" 
            :key="member.id" 
            class="member-item"
          >
            <el-radio :label="member.id">
              <div class="member-info">
                <span class="member-id">成员{{ member.id }}</span>
                <span class="member-nickname">{{ member.gameId }}</span>
                <span class="member-server">- {{ member.server }}</span>
              </div>
            </el-radio>
          </div>
        </el-radio-group>
      </div>
    </div>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmSelection" :disabled="!selectedMemberId">
          确认选择
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  clubMembers: {
    type: Array,
    default: () => []
  },
  currentMatchingResult: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:visible', 'confirm-selection'])

const dialogVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

const memberSearchText = ref('')
const selectedMemberId = ref(null)

// 计算属性：过滤后的成员列表
const filteredMembers = computed(() => {
  if (!memberSearchText.value) {
    return props.clubMembers
  }
  
  const searchText = memberSearchText.value.toLowerCase()
  return props.clubMembers.filter(member => 
    member.gameId.toLowerCase().includes(searchText) || 
    `member${member.id}`.toLowerCase().includes(searchText)
  )
})

const handleClose = () => {
  memberSearchText.value = ''
  selectedMemberId.value = null
  emit('update:visible', false)
}

const confirmSelection = () => {
  if (!selectedMemberId.value) {
    ElMessage.warning('请选择一个成员')
    return
  }
  
  const member = props.clubMembers.find(m => m.id === selectedMemberId.value)
  emit('confirm-selection', {
    member: member,
    currentMatchingResult: props.currentMatchingResult
  })
  
  memberSearchText.value = ''
  selectedMemberId.value = null
  dialogVisible.value = false
}

// 当对话框打开时，如果有当前匹配结果，初始化选中状态
watch(() => props.visible, (newVal) => {
  if (newVal && props.currentMatchingResult) {
    selectedMemberId.value = props.currentMatchingResult.matchedMember?.id || null
  }
})
</script>

<style scoped>
.mt-10 {
  margin-top: 10px;
}

.member-selector-container {
  max-height: 400px;
}

.member-list {
  max-height: 300px;
  overflow-y: auto;
  border: 1px solid #DCDFE6;
  border-radius: 4px;
  padding: 10px;
}

.member-item {
  display: block;
  width: 100%;
  padding: 8px 0;
  border-bottom: 1px solid #EBEEF5;
}

.member-info {
  display: flex;
  align-items: center;
}

.member-id {
  font-weight: bold;
  margin-right: 8px;
}

.member-nickname {
  color: #409EFF;
  margin-right: 8px;
}

.member-server {
  color: #909399;
}

.dialog-footer {
  text-align: right;
}
</style>