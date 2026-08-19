<script setup lang="ts">
import { computed } from 'vue'
import { Doughnut } from 'vue-chartjs'

import { ArcElement, Chart as ChartJS, Legend, Tooltip } from 'chart.js'

import type { StatsGroup } from '@/types/stats'

ChartJS.register(ArcElement, Tooltip, Legend)

const props = defineProps<{
  items: StatsGroup[]
}>()

const statusLabels: Record<string, string> = {
  ACTIVE: 'Actif',
  SOLD: 'Vendu',
  LOST: 'Perdu',
  GIVEN: 'Donné',
  OTHER: 'Autre',
}

const chartData = computed(() => ({
  labels: props.items.map((item) => statusLabels[item.label] ?? item.label),

  datasets: [
    {
      data: props.items.map((item) => item.count),
    },
  ],
}))

const chartOptions = {
  responsive: true,
  maintainAspectRatio: false,

  plugins: {
    legend: {
      position: 'bottom' as const,
    },
  },
}
</script>

<template>
  <div class="chart-container">
    <Doughnut :data="chartData" :options="chartOptions" />
  </div>
</template>

<style scoped>
.chart-container {
  position: relative;
  min-height: 320px;
}
</style>
