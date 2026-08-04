<script setup lang="ts">
withDefaults(
  defineProps<{
    message?: string
    type?: 'error' | 'success' | 'info'
  }>(),
  {
    message: '',
    type: 'info',
  },
)

const alertClass = {
  error: 'alert-danger',
  success: 'alert-success',
  info: 'alert-info',
}
</script>

<template>
  <div
    v-if="message"
    class="alert"
    :class="alertClass[type]"
    :role="type === 'error' ? 'alert' : 'status'"
  >
    <i
      class="bi me-2"
      :class="{
        'bi-exclamation-triangle': type === 'error',
        'bi-check-circle': type === 'success',
        'bi-info-circle': type === 'info',
      }"
      aria-hidden="true"
    />

    {{ message }}
  </div>
</template>