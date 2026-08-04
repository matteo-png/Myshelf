<script setup lang="ts">
import { ref } from 'vue'

withDefaults(
  defineProps<{
    id: string
    label?: string
    autocomplete?: string
    required?: boolean
    error?: string
  }>(),
  {
    label: 'Mot de passe',
    autocomplete: 'current-password',
    required: false,
    error: '',
  },
)

const model = defineModel<string>({ required: true })
const visible = ref(false)
</script>

<template>
  <div class="mb-3">
    <label :for="id" class="form-label">
      {{ label }}
    </label>

    <div class="input-group">
      <span class="input-group-text">
        <i class="bi bi-lock" aria-hidden="true" />
      </span>

      <input
        :id="id"
        v-model="model"
        :type="visible ? 'text' : 'password'"
        :autocomplete="autocomplete"
        :required="required"
        class="form-control"
        :class="{ 'is-invalid': error }"
        :aria-invalid="Boolean(error)"
      />

      <button
        type="button"
        class="btn btn-outline-secondary"
        :aria-label="visible ? 'Masquer le mot de passe' : 'Afficher le mot de passe'"
        @click="visible = !visible"
      >
        <i
          class="bi"
          :class="visible ? 'bi-eye-slash' : 'bi-eye'"
          aria-hidden="true"
        />
      </button>

      <div v-if="error" class="invalid-feedback">
        {{ error }}
      </div>
    </div>
  </div>
</template>