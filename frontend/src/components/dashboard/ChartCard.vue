<script setup lang="ts">
defineProps<{
  title: string
  hasData: boolean
  emptyMessage?: string
}>()
</script>

<template>
  <div class="card h-100">
    <div class="card-header">
      <h2 class="card-title mb-0">
        {{ title }}
      </h2>
    </div>

    <div class="card-body">
      <div
        v-if="!hasData"
        class="d-flex flex-column align-items-center justify-content-center text-center text-secondary h-100 py-5"
      >
        <i class="bi bi-bar-chart display-5 mb-3" aria-hidden="true" />

        <p class="mb-0">
          {{ emptyMessage ?? 'Pas encore assez de données.' }}
        </p>
      </div>

      <slot v-else />
    </div>
  </div>
</template>
