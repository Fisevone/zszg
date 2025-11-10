<template>
  <div class="math-content" v-html="renderedContent"></div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { renderMath } from '@/utils/mathRenderer'

interface Props {
  content: string
}

const props = defineProps<Props>()

const renderedContent = computed(() => {
  return renderMath(props.content || '')
})
</script>

<style scoped>
.math-content {
  line-height: 1.8;
  word-break: break-word;
}

/* KaTeX 公式样式优化 */
.math-content :deep(.katex) {
  font-size: 1.1em;
}

.math-content :deep(.katex-display) {
  margin: 1em 0;
  text-align: center;
}

/* 确保公式在不同背景下都清晰可见 */
.math-content :deep(.katex .mord),
.math-content :deep(.katex .mbin),
.math-content :deep(.katex .mrel) {
  color: inherit;
}
</style>

