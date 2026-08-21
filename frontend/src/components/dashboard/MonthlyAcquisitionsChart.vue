<script setup lang="ts">
import { computed } from 'vue'
import { Line } from 'vue-chartjs'
import { CHART_PRIMARY, CHART_PRIMARY_BACKGROUND } from '@/constants/chart-colors'
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

    return monthFormatter.format(new Date(Number(year), Number(month) - 1, 1))
  }),

  datasets: [
    {
      label: 'Nombre d’objets achetés',
      data: props.points.map((point) => point.count),

      borderColor: CHART_PRIMARY,
      backgroundColor: CHART_PRIMARY_BACKGROUND,

      fill: true,
      tension: 0.35,

      pointBackgroundColor: CHART_PRIMARY,
      pointBorderColor: '#ffffff',
      pointBorderWidth: 2,
      pointRadius: 4,
      pointHoverRadius: 6,

      borderWidth: 3,
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
    <Line :data="chartData" :options="chartOptions" />
  </div>
</template>

<style scoped>
.chart-container {
  position: relative;
  min-height: 320px;
}
</style>
