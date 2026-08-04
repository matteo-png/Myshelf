<script setup lang="ts">
import { computed } from 'vue'
import { Bar } from 'vue-chartjs'

import {
  BarElement,
  CategoryScale,
  Chart as ChartJS,
  Legend,
  LinearScale,
  Title,
  Tooltip,
} from 'chart.js'

import type { StatsGroup } from '@/types/stats'

ChartJS.register(
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend,
)

const props = defineProps<{
  label: string
  items: StatsGroup[]
}>()

const chartData = computed(() => ({
  labels: props.items.map((item) => item.label),

  datasets: [
    {
      label: props.label,
      data: props.items.map((item) => item.count),
      borderWidth: 1,
    },
  ],
}))

const chartOptions = {
  indexAxis: 'y' as const,
  responsive: true,
  maintainAspectRatio: false,

  plugins: {
    legend: {
      display: false,
    },
  },

  scales: {
    x: {
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
    <Bar
      :data="chartData"
      :options="chartOptions"
    />
  </div>
</template>

<style scoped>
.chart-container {
  position: relative;
  min-height: 320px;
}
</style>