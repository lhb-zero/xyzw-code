<template>
  <el-card shadow="always">
    <template #header>
      <div class="card-header">
        <span>智能识别结果</span>
        <div>
          <el-button size="small" @click="$emit('show-batch-date-dialog')">
            一键设置时间
          </el-button>
          <el-button size="small" @click="$emit('refresh-results')">
            <el-icon><refresh /></el-icon>
            清空数据
          </el-button>
          <el-button size="small" type="primary" @click="$emit('batch-confirm')" :disabled="matchedResults.length === 0">
            批量确认
          </el-button>
        </div>
      </div>
    </template>
    
    <el-table :data="matchedResults" style="width: 100%" max-height="500" :default-sort="{prop: 'id', order: 'descending'}">
      <el-table-column label="来源" width="70">
        <template #default="scope">
          <el-tag :type="scope.row.isManualInput ? 'success' : 'primary'" size="mini">
            {{ scope.row.isManualInput ? '手动' : 'OCR' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="昵称" width="100" prop="nickname">
        <template #default="scope">
          <el-tag size="mini">{{ scope.row.nickname }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="匹配成员" width="120">
        <template #default="scope">
          <div v-if="scope.row.matchedMember">
            <el-tag type="success" size="mini">成员{{ scope.row.matchedMember.id }}</el-tag>
            <div class="member-info">{{ scope.row.matchedMember.gameId || scope.row.memberName }}</div>
            <el-button size="mini" type="text" @click="$emit('show-member-selector', scope.row)">
              重新匹配
            </el-button>
          </div>
          <div v-else>
            <el-tag type="danger" size="mini">未匹配</el-tag>
            <el-button size="mini" type="text" @click="$emit('show-member-selector', scope.row)">
              手动选择
            </el-button>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="kills" label="击杀" width="70" sortable />
      <el-table-column prop="digs" label="刨地" width="70" sortable />
      <el-table-column prop="deaths" label="死亡" width="70" sortable />
      <el-table-column prop="revives" label="复活" width="70" sortable />
      <el-table-column prop="kdRatio" label="KD" width="80" sortable>
        <template #default="scope">
          {{ scope.row.kdRatio ? scope.row.kdRatio.toFixed(2) : '-' }}
        </template>
      </el-table-column>
      <el-table-column label="操作">
        <template #default="scope">
          <el-button size="mini" @click="$emit('edit-result', scope.row)">编辑</el-button>
          <el-button size="mini" type="success" @click="$emit('confirm-result', scope.row)">
            确认
          </el-button>
          <el-button size="mini" type="danger" @click="$emit('delete-result', scope.row)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { Refresh } from '@element-plus/icons-vue'

const props = defineProps({
  matchedResults: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits([
  'show-batch-date-dialog',
  'refresh-results',
  'batch-confirm',
  'show-member-selector',
  'edit-result',
  'confirm-result',
  'delete-result'
])
</script>

<style scoped>
.member-info {
  font-size: 12px;
  color: #606266;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>