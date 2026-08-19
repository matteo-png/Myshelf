<script setup lang="ts">
import { computed } from 'vue'
import { Line } from 'vue-chartjs'

import {
  CategoryScale,
  Chart as ChartJS,
  Legend,
  LinearScale,
  LineElement,
  PointElement,
  Tooltip,
} from 'chart.js'

import type { StatsTimeSeriesPoint } from '@/types/stats'

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Tooltip, Legend)

const props = defineProps<{
  points: StatsTimeSeriesPoint[]
}>()

const chartData = computed(() => ({
  labels: props.points.map((point) => point.period),

  datasets: [
    {
      label: 'Objets acquis',
      data: props.points.map((point) => point.count),
      tension: 0.3,
      borderWidth: 2,
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

  scales: {
    y: {
      beginAtZero: true,

      ticks: {
        precision: 0,
      },
    },
  },
}
</script>

<template>
  <div class="chart-container">
    <Line :data="chartData" :options="chartOptions" />
  </div>
</template>

<style scoped>
.chart-container {
  position: relative;
  min-height: 320px;
}
</style>
