# 手动输入战绩数据组件

## 功能概述

ManualInput 组件允许用户直接输入战绩数据，而不仅仅依赖OCR识别。这提供了以下优势：

1. **数据来源多样性**：用户可以从多种来源复制战绩数据
2. **处理OCR识别困难**：当图片质量不佳或格式特殊时，可以手动输入
3. **灵活性**：支持文本和HTML表格两种输入格式

## 使用方法

### 1. 文本格式输入

支持自由文本格式，例如：
```
昵称 击杀 刨地 死亡 复活 KD
千寻、庆庆 38 28 16 10 2.38
千寻、小八 37 28 15 9 2.47
```

### 2. HTML表格格式输入

直接粘贴HTML表格代码：
```html
<table border=1 style='margin: auto; width: max-content;'>
<tr><td>昵称</td><td>击杀</td><td>刨地</td><td>死亡</td><td>复活</td><td>K/D比</td></tr>
<tr><td>千寻、庆庆</td><td>38</td><td>28</td><td>16</td><td>10</td><td>2.38</td></tr>
</table>
```

## 数据解析规则

1. **表头识别**：自动识别常见表头关键词
2. **字段映射**：将不同名称的字段映射到标准字段
3. **数据验证**：检查数据合理性和完整性
4. **KD计算**：当KD值为0但有击杀和死亡数据时，自动计算KD

## 数据编辑功能

解析后的数据可以进行编辑：

1. **修改数值**：直接修改击杀、死亡等数值
2. **自动重算KD**：修改击杀或死亡后自动重算KD值
3. **删除条目**：可以删除不需要的条目

## 集成方式

ManualInput 组件通过以下事件与父组件交互：

- `data-confirmed`: 确认数据后触发，传递解析后的数据

## 示例代码

```vue
<template>
  <manual-input
    @data-confirmed="handleManualInputData"
  />
</template>

<script setup>
import ManualInput from './components/ManualInput.vue'

function handleManualInputData(data) {
  // 处理手动输入的数据
  console.log('手动输入的数据:', data)
}
</script>
```

## 注意事项

1. **数据格式**：确保数据格式正确，否则可能导致解析失败
2. **特殊字符**：某些特殊字符可能影响解析结果
3. **数据验证**：系统会进行基本验证，但仍需用户确认数据正确性