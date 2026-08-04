<script setup lang="ts">
withDefaults(
  defineProps<{
    open: boolean
    title?: string
    message: string
    confirmLabel?: string
    loading?: boolean
    variant?: 'danger' | 'primary'
  }>(),
  {
    title: 'Confirmation',
    confirmLabel: 'Confirmer',
    loading: false,
    variant: 'danger',
  },
)

defineEmits<{
  close: []
  confirm: []
}>()
</script>

<template>
  <div
    v-if="open"
    class="modal fade show d-block"
    tabindex="-1"
    role="dialog"
    aria-modal="true"
    @click.self="$emit('close')"
  >
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content">
        <div class="modal-header">
          <h2 class="modal-title fs-5">
            {{ title }}
          </h2>

          <button
            type="button"
            class="btn-close"
            aria-label="Fermer"
            :disabled="loading"
            @click="$emit('close')"
          />
        </div>

        <div class="modal-body">
          <p class="mb-0">
            {{ message }}
          </p>
        </div>

        <div class="modal-footer">
          <button
            type="button"
            class="btn btn-secondary"
            :disabled="loading"
            @click="$emit('close')"
          >
            Annuler
          </button>

          <button
            type="button"
            class="btn"
            :class="`btn-${variant}`"
            :disabled="loading"
            @click="$emit('confirm')"
          >
            <span
              v-if="loading"
              class="spinner-border spinner-border-sm me-2"
              aria-hidden="true"
            />

            {{ loading ? 'Suppression…' : confirmLabel }}
          </button>
        </div>
      </div>
    </div>
  </div>

  <div
    v-if="open"
    class="modal-backdrop fade show"
  />
</template>