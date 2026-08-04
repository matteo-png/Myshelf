<script setup lang="ts">
withDefaults(
  defineProps<{
    title: string
    value: string | number
    icon: string
    variant?: 'primary' | 'success' | 'warning' | 'danger' | 'info' | 'secondary'
    route?: string
    linkLabel?: string
  }>(),
  {
    variant: 'primary',
    route: undefined,
    linkLabel: 'Voir plus',
  },
)
</script>

<template>
  <div class="small-box" :class="`text-bg-${variant}`">
    <div class="inner">
      <h3>{{ value }}</h3>

      <p>{{ title }}</p>
    </div>

    <div class="small-box-icon">
      <i class="bi" :class="icon" aria-hidden="true" />
    </div>

    <RouterLink
      v-if="route"
      :to="route"
      class="small-box-footer link-light"
    >
      {{ linkLabel }}

      <i
        class="bi bi-arrow-right-circle ms-1"
        aria-hidden="true"
      />
    </RouterLink>
  </div>
</template>