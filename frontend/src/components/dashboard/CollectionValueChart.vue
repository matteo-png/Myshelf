<script setup lang="ts">
import { computed } from 'vue'
import { Bar } from 'vue-chartjs'

import { BarElement, CategoryScale, Chart as ChartJS, Legend, LinearScale, Tooltip } from 'chart.js'

import type { StatsGroup } from '@/types/stats'

ChartJS.register(CategoryScale, LinearScale, BarElement, Tooltip, Legend)

const props = defineProps<{
  items: StatsGroup[]
}>()

const chartData = computed(() => ({
  labels: props.items.map((item) => item.label),

  datasets: [
    {
      label: 'Valeur estimée',
      data: props.items.map((item) => item.totalValue),

      backgroundColor: '#198754',
      borderColor: '#198754',

      borderWidth: 1,
      borderRadius: 5,
    },
  ],
}))

const chartOptions = {
  responsive: true,
  maintainAspectRatio: false,

  plugins: {
    legend: {
      display: false,
    },

    tooltip: {
      callbacks: {
        label(context: { raw: unknown }) {
          const value = Number(context.raw)

          return new Intl.NumberFormat('fr-FR', {
            style: 'currency',
            currency: 'EUR',
          }).format(value)
        },
      },
    },
  },

  scales: {
    y: {
      beginAtZero: true,

      ticks: {
        callback(value: string | number) {
          return `${value} €`
        },
      },
    },
  },
}
</script>

<template>
  <div class="chart-container">
    <Bar :data="chartData" :options="chartOptions" />
  </div>
</template>

<style scoped>
.chart-container {
  position: relative;
  min-height: 320px;
}
</style>
