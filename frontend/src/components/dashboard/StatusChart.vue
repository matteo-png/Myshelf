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

const statusColors: Record<string, string> = {
  ACTIVE: '#198754',
  SOLD: '#0d6efd',
  LOST: '#dc3545',
  GIVEN: '#ffc107',
  OTHER: '#6c757d',
}

const chartData = computed(() => ({
  labels: props.items.map((item) => statusLabels[item.label] ?? item.label),

  datasets: [
    {
      data: props.items.map((item) => item.count),

      backgroundColor: props.items.map((item) => statusColors[item.label] ?? '#6c757d'),

      borderWidth: 2,
      borderColor: '#ffffff',
      hoverOffset: 6,
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
