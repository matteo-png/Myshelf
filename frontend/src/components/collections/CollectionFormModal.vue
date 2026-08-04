<script setup lang="ts">
import axios from 'axios'
import { computed, reactive, watch } from 'vue'

import FormMessage from '@/components/forms/FormMessage.vue'

import type { ValidationErrorResponse } from '@/types/api'
import type {
  Collection,
  CollectionRequest,
} from '@/types/collections'

const props = defineProps<{
  open: boolean
  collection?: Collection | null
  loading?: boolean
}>()

const emit = defineEmits<{
  close: []
  submit: [data: CollectionRequest]
}>()

const form = reactive({
  name: '',
  description: '',
})

const fieldErrors = reactive<Record<string, string>>({})
const error = reactive({
  message: '',
})

const isEditing = computed(() => Boolean(props.collection))

const title = computed(() => {
  return isEditing.value
    ? 'Modifier la collection'
    : 'Créer une collection'
})

const isFormValid = computed(() => {
  return form.name.trim().length > 0
})

function clearErrors() {
  error.message = ''

  for (const key of Object.keys(fieldErrors)) {
    delete fieldErrors[key]
  }
}

function resetForm() {
  form.name = props.collection?.name ?? ''
  form.description = props.collection?.description ?? ''
  clearErrors()
}

function close() {
  resetForm()
  emit('close')
}

function submit() {
  clearErrors()

  if (!isFormValid.value) {
    fieldErrors.name = 'Le nom de la collection est obligatoire.'
    return
  }

  emit('submit', {
    name: form.name.trim(),
    description: form.description.trim() || null,
  })
}

function setApiError(exception: unknown) {
  clearErrors()

  if (axios.isAxiosError<ValidationErrorResponse>(exception)) {
    const response = exception.response?.data

    if (response?.errors) {
      Object.assign(fieldErrors, response.errors)
    }

    error.message =
      response?.message ??
      'Impossible d’enregistrer la collection.'

    return
  }

  error.message = 'Une erreur inattendue est survenue.'
}

defineExpose({
  setApiError,
})

watch(
  () => [props.open, props.collection],
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
              :message="error.message"
              type="error"
            />

            <div class="mb-3">
              <label
                for="collection-name"
                class="form-label"
              >
                Nom
              </label>

              <input
                id="collection-name"
                v-model="form.name"
                type="text"
                class="form-control"
                :class="{ 'is-invalid': fieldErrors.name }"
                maxlength="255"
                autofocus
              />

              <div
                v-if="fieldErrors.name"
                class="invalid-feedback"
              >
                {{ fieldErrors.name }}
              </div>
            </div>

            <div>
              <label
                for="collection-description"
                class="form-label"
              >
                Description
              </label>

              <textarea
                id="collection-description"
                v-model="form.description"
                class="form-control"
                :class="{
                  'is-invalid': fieldErrors.description,
                }"
                rows="4"
                maxlength="2000"
                placeholder="Décrivez le contenu de cette collection…"
              />

              <div
                v-if="fieldErrors.description"
                class="invalid-feedback"
              >
                {{ fieldErrors.description }}
              </div>

              <div class="form-text text-end">
                {{ form.description.length }} / 2000
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