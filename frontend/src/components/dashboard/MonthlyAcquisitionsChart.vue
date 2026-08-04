<script setup lang="ts">
import { computed } from 'vue'
import { Line } from 'vue-chartjs'

import {
  CategoryScale,
  Chart as ChartJS,
  Filler,
  Legend,
  LinearScale,
  LineElement,
  PointElement,
  Title,
  Tooltip,
} from 'chart.js'

import type { StatsTimeSeriesPoint } from '@/types/stats'

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  Filler,
)

const props = defineProps<{
  points: StatsTimeSeriesPoint[]
}>()

const monthFormatter = new Intl.DateTimeFormat('fr-FR', {
  month: 'short',
})

const chartData = computed(() => ({
  labels: props.points.map((point) => {
    const [year, month] = point.period.split('-')

    return monthFormatter.format(
      new Date(Number(year), Number(month) - 1, 1),
    )
  }),

  datasets: [
    {
      label: 'Nombre d’objets achetés',
      data: props.points.map((point) => point.count),
      tension: 0.3,
      fill: true,
      borderWidth: 2,
    },
  ],
}))

const chartOptions = {
  responsive: true,
  maintainAspectRatio: false,

  plugins: {
    legend: {
      display: true,
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
    <Line
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