<script setup lang="ts">
import axios from 'axios'
import { computed, reactive, ref, watch } from 'vue'

import FormMessage from '@/components/forms/FormMessage.vue'

import type { ValidationErrorResponse } from '@/types/api'
import type {
  Tag,
  TagRequest,
} from '@/types/tags'

const props = withDefaults(
  defineProps<{
    open: boolean
    tag?: Tag | null
    loading?: boolean
  }>(),
  {
    tag: null,
    loading: false,
  },
)

const emit = defineEmits<{
  close: []
  submit: [data: TagRequest]
}>()

const form = reactive({
  name: '',
})

const fieldErrors = reactive<Record<string, string>>({})
const globalError = ref('')

const isEditing = computed(() => Boolean(props.tag))

const title = computed(() => {
  return isEditing.value
    ? 'Modifier le tag'
    : 'Créer un tag'
})

const isFormValid = computed(() => {
  return form.name.trim().length > 0
})

function clearErrors() {
  globalError.value = ''

  for (const key of Object.keys(fieldErrors)) {
    delete fieldErrors[key]
  }
}

function resetForm() {
  form.name = props.tag?.name ?? ''
  clearErrors()
}

function close() {
  if (props.loading) {
    return
  }

  resetForm()
  emit('close')
}

function submit() {
  clearErrors()

  if (!form.name.trim()) {
    fieldErrors.name = 'Le nom du tag est obligatoire.'
    return
  }

  emit('submit', {
    name: form.name.trim(),
  })
}

function setApiError(exception: unknown) {
  clearErrors()

  if (axios.isAxiosError<ValidationErrorResponse>(exception)) {
    const response = exception.response?.data

    if (response?.errors) {
      Object.assign(fieldErrors, response.errors)
    }

    globalError.value =
      response?.message ??
      'Impossible d’enregistrer le tag.'

    return
  }

  globalError.value = 'Une erreur inattendue est survenue.'
}

defineExpose({
  setApiError,
})

watch(
  () => [props.open, props.tag],
  () => {
    if (props.open) {
      resetForm()
    }
  },
  {
    immediate: true,
  },
)
</script>

<template>
  <div
    v-if="open"
    class="modal fade show d-block"
    tabindex="-1"
    role="dialog"
    aria-modal="true"
    @click.self="close"
  >
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content">
        <form @submit.prevent="submit">
          <div class="modal-header">
            <h2 class="modal-title fs-5">
              {{ title }}
            </h2>

            <button
              type="button"
              class="btn-close"
              aria-label="Fermer"
              :disabled="loading"
              @click="close"
            />
          </div>

          <div class="modal-body">
            <FormMessage
              :message="globalError"
              type="error"
            />

            <div>
              <label
                for="tag-name"
                class="form-label"
              >
                Nom
              </label>

              <input
                id="tag-name"
                v-model="form.name"
                type="text"
                class="form-control"
                :class="{ 'is-invalid': fieldErrors.name }"
                maxlength="255"
                placeholder="Exemple : Rare"
                autofocus
              />

              <div
                v-if="fieldErrors.name"
                class="invalid-feedback"
              >
                {{ fieldErrors.name }}
              </div>
            </div>
          </div>

          <div class="modal-footer">
            <button
              type="button"
              class="btn btn-secondary"
              :disabled="loading"
              @click="close"
            >
              Annuler
            </button>

            <button
              type="submit"
              class="btn btn-primary"
              :disabled="loading || !isFormValid"
            >
              <span
                v-if="loading"
                class="spinner-border spinner-border-sm me-2"
                aria-hidden="true"
              />

              {{
                loading
                  ? 'Enregistrement…'
                  : isEditing
                    ? 'Enregistrer'
                    : 'Créer'
              }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>

  <div
    v-if="open"
    class="modal-backdrop fade show"
  />
</template>