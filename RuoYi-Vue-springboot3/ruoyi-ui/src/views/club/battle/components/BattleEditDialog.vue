<template>
  <teleport to="body">
    <!-- 编辑已确认战绩窗口 -->
    <div v-show="dialogVisible" class="battle-edit-drawer-overlay" @click.self="handleClose">
      <div class="battle-edit-drawer" :class="{ 'is-open': dialogVisible }">
        <div class="battle-edit-drawer__header">
          <div class="battle-edit-drawer__title">{{ title }}</div>
          <button class="battle-edit-drawer__close" @click="handleClose">
            <el-icon><Close /></el-icon>
          </button>
        </div>
        <div class="battle-edit-drawer__body">
          <el-form ref="battleEditForm" :model="editingBattle" :rules="battleRules" label-width="80px">
            <el-form-item label="昵称" prop="nickname">
              <el-input v-model="editingBattle.nickname" />
            </el-form-item>
            <el-form-item label="成员ID" prop="memberId">
              <el-input v-model="editingBattle.memberId" type="number" />
            </el-form-item>
            <el-form-item label="日期" prop="recordDate">
              <el-date-picker
                ref="datePickerRef"
                v-model="editingBattle.recordDate"
                type="date"
                placeholder="选择日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 100%">
              </el-date-picker>
              <div class="mt-5">
                <el-button size="mini" type="text" @click="setThisWeekSaturday">设为本周六</el-button>
              </div>
            </el-form-item>
            <el-form-item label="击杀数" prop="kills">
              <el-input v-model="editingBattle.kills" type="number" />
            </el-form-item>
            <el-form-item label="死亡数" prop="deaths">
              <el-input v-model="editingBattle.deaths" type="number" />
            </el-form-item>
            <el-form-item label="刨地数" prop="digs">
              <el-input v-model="editingBattle.digs" type="number" />
            </el-form-item>
            <el-form-item label="复活数" prop="revives">
              <el-input v-model="editingBattle.revives" type="number" />
            </el-form-item>
          </el-form>
        </div>
        <div class="battle-edit-drawer__footer">
          <el-button @click="handleClose">取消</el-button>
          <el-button v-if="isPendingEdit" type="info" @click="savePendingEdit">暂存</el-button>
          <el-button type="primary" @click="saveBattle">保存</el-button>
        </div>
      </div>
    </div>
  </teleport>
</template>

<script setup>
import { computed, ref, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Close } from '@element-plus/icons-vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    default: '编辑战绩'
  },
  editingBattle: {
    type: Object,
    default: () => ({})
  },
  isPendingEdit: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:visible', 'save-battle', 'save-pending-edit', 'save-pending-result', 'update:date'])

// 日期选择器引用
const datePickerRef = ref(null)

const dialogVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

const battleEditForm = ref(null)

// 监听击杀和死亡数变化，自动计算KD比率
watch(() => [props.editingBattle.kills, props.editingBattle.deaths], ([kills, deaths]) => {
  if (kills !== null && deaths !== null) {
    if (deaths > 0) {
      props.editingBattle.kdRatio = kills / deaths
    } else {
      // 死亡数为0时，直接使用击杀数作为KD比率
      props.editingBattle.kdRatio = kills
    }
  }
}, { immediate: true })

// 战绩编辑表单规则
const battleRules = {
  nickname: [
    { required: true, message: '昵称不能为空', trigger: 'blur' }
  ],
  memberId: [
    { required: true, message: '成员ID不能为空', trigger: 'blur' }
  ],
  recordDate: [
    { required: true, message: '日期不能为空', trigger: 'blur' }
  ],
  kills: [
    { required: true, message: '击杀数不能为空', trigger: 'blur' }
  ],
  deaths: [
    { required: true, message: '死亡数不能为空', trigger: 'blur' }
  ],
  digs: [
    { required: true, message: '刨地数不能为空', trigger: 'blur' }
  ],
  revives: [
    { required: true, message: '复活数不能为空', trigger: 'blur' }
  ]
}

const handleClose = () => {
  emit('update:visible', false)
}

// 设置本周六日期
const setThisWeekSaturday = () => {
  const today = new Date()
  const currentDay = today.getDay() // 0是周日，6是周六
  const daysUntilSaturday = (6 - currentDay + 7) % 7 // 计算到本周六的天数
  
  const saturday = new Date(today)
  saturday.setDate(today.getDate() + daysUntilSaturday)
  
  // 转换为YYYY-MM-DD格式
  const saturdayStr = `${saturday.getFullYear()}-${(saturday.getMonth() + 1).toString().padStart(2, '0')}-${saturday.getDate().toString().padStart(2, '0')}`
  
  console.log('设置本周六日期:', saturdayStr)
  
  // 创建一个全新的对象来强制Vue响应
  const newEditingBattle = {
    ...props.editingBattle,
    recordDate: saturdayStr
  }
  
  // 通过emit通知父组件更新整个editingBattle对象
  emit('update:editingBattle', newEditingBattle)
  emit('update:date', saturdayStr)
}

const savePendingEdit = () => {
  battleEditForm.value.validate(async (valid) => {
    if (!valid) {
      return
    }
    
    // 计算KD比率
    let kdRatio = 0.0
    const kills = parseInt(props.editingBattle.kills) || 0
    const deaths = parseInt(props.editingBattle.deaths) || 0
    
    if (deaths > 0) {
      kdRatio = kills / deaths
    } else {
      // 死亡数为0时，直接使用击杀数作为KD比率
      kdRatio = kills
    }
    
    // 准备数据
    const pendingData = {
      id: props.editingBattle.pendingId,
      memberId: props.editingBattle.memberId,
      memberName: props.editingBattle.memberName || `成员${props.editingBattle.memberId}`,
      kills: kills,
      deaths: deaths,
      digging: parseInt(props.editingBattle.digs) || 0,
      revives: parseInt(props.editingBattle.revives) || 0,
      kdRatio: kdRatio,
      recordDate: props.editingBattle.recordDate
    }
    
    emit('save-pending-edit', pendingData)
  })
}

const saveBattle = () => {
  battleEditForm.value.validate(async (valid) => {
    if (!valid) {
      return
    }
    
    try {
      // 计算KD比例
      const battleData = { ...props.editingBattle }
      if (battleData.kills && battleData.deaths) {
        if (battleData.deaths > 0) {
          battleData.kdRatio = battleData.kills / battleData.deaths
        } else {
          // 死亡数为0时，直接使用击杀数作为KD比率
          battleData.kdRatio = battleData.kills
        }
      }
      
      // 如果是暂存记录编辑，使用新的保存方法
      if (props.isPendingEdit) {
        emit('save-pending-result', battleData)
      } else {
        emit('save-battle', battleData)
      }
    } catch (error) {
      console.error('保存战绩失败:', error)
      ElMessage.error('保存战绩失败')
    }
  })
}
</script>

<style>
/* 抽屉式编辑器样式 */
.battle-edit-drawer-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 2000;
  display: flex;
  justify-content: flex-end;
}

.battle-edit-drawer {
  width: 500px;
  max-width: 100%;
  height: 100vh;
  background-color: #fff;
  box-shadow: -5px 0 15px rgba(0, 0, 0, 0.2);
  display: flex;
  flex-direction: column;
  transform: translateX(100%);
  transition: transform 0.3s ease-in-out;
}

.battle-edit-drawer.is-open {
  transform: translateX(0);
}

.battle-edit-drawer__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #e4e7ed;
  background-color: #fff;
}

.battle-edit-drawer__title {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.battle-edit-drawer__close {
  border: none;
  background: none;
  cursor: pointer;
  padding: 5px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
}

.battle-edit-drawer__close:hover {
  color: #409eff;
}

.battle-edit-drawer__body {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

.battle-edit-drawer__footer {
  padding: 15px 20px;
  border-top: 1px solid #e4e7ed;
  background-color: #fff;
  text-align: right;
}
</style>

<style scoped>
.mt-5 {
  margin-top: 5px;
}
</style>