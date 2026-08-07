<script setup lang="ts">
import axios from 'axios'
import { computed, reactive, ref, watch } from 'vue'

import FormMessage from '@/components/forms/FormMessage.vue'

import type { ValidationErrorResponse } from '@/types/api'
import {
  PURCHASE_PLACE_TYPES,
  type PurchasePlace,
  type PurchasePlaceRequest,
  type PurchasePlaceType,
} from '@/types/purchase-place'

const purchasePlaceTypeLabels: Record<PurchasePlaceType, string> = {
  MAGASIN: 'Magasin',
  MARKETPLACE: 'Marketplace',
  ONLINE: 'Boutique en ligne',
  OTHER: 'Autre',
}

const props = withDefaults(
  defineProps<{
    open: boolean
    purchasePlace?: PurchasePlace | null
    loading?: boolean
  }>(),
  {
    purchasePlace: null,
    loading: false,
  },
)

const emit = defineEmits<{
  close: []
  submit: [data: PurchasePlaceRequest]
}>()

const form = reactive<{
  name: string
  type: PurchasePlaceType | ''
  websiteUrl: string
}>({
  name: '',
  type: '',
  websiteUrl: '',
})

const fieldErrors = reactive<Record<string, string>>({})
const globalError = ref('')

const isEditing = computed(() => Boolean(props.purchasePlace))

const title = computed(() =>
  isEditing.value
    ? 'Modifier le lieu d’achat'
    : 'Créer un lieu d’achat',
)

const isFormValid = computed(() => form.name.trim().length > 0)

function clearErrors() {
  globalError.value = ''

  for (const key of Object.keys(fieldErrors)) {
    delete fieldErrors[key]
  }
}

function resetForm() {
  form.name = props.purchasePlace?.name ?? ''
  form.type = props.purchasePlace?.type ?? ''
  form.websiteUrl = props.purchasePlace?.websiteUrl ?? ''

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
    fieldErrors.name = 'Le nom du lieu d’achat est obligatoire.'
    return
  }

  emit('submit', {
    name: form.name.trim(),
    type: form.type || null,
    websiteUrl: form.websiteUrl.trim() || null,
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
      'Impossible d’enregistrer le lieu d’achat.'

    return
  }

  globalError.value = 'Une erreur inattendue est survenue.'
}

defineExpose({
  setApiError,
})

watch(
  () => [props.open, props.purchasePlace],
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

            <div class="mb-3">
              <label for="purchase-place-name" class="form-label">
                Nom
              </label>

              <input
                id="purchase-place-name"
                v-model="form.name"
                type="text"
                class="form-control"
                :class="{ 'is-invalid': fieldErrors.name }"
                maxlength="255"
                placeholder="Exemple : Fnac"
                autofocus
              />

              <div
                v-if="fieldErrors.name"
                class="invalid-feedback"
              >
                {{ fieldErrors.name }}
              </div>
            </div>

            <div class="mb-3">
              <label for="purchase-place-type" class="form-label">
                Type
              </label>

                <select
                id="purchase-place-type"
                v-model="form.type"
                class="form-select"
                :class="{ 'is-invalid': fieldErrors.type }"
                >
                <option value="">
                    Aucun type
                </option>

                <option
                    v-for="type in PURCHASE_PLACE_TYPES"
                    :key="type"
                    :value="type"
                >
                    {{ purchasePlaceTypeLabels[type] }}
                </option>
                </select>

                <div
                v-if="fieldErrors.type"
                class="invalid-feedback"
                >
                {{ fieldErrors.type }}
                </div>

              <div
                v-if="fieldErrors.type"
                class="invalid-feedback"
                >
                {{ fieldErrors.type }}
              </div>
            </div>

            <div>
              <label for="purchase-place-url" class="form-label">
                Site internet
              </label>

              <input
                id="purchase-place-url"
                v-model="form.websiteUrl"
                type="url"
                class="form-control"
                :class="{ 'is-invalid': fieldErrors.websiteUrl }"
                placeholder="https://www.fnac.com"
              />

              <div
                v-if="fieldErrors.websiteUrl"
                class="invalid-feedback"
              >
                {{ fieldErrors.websiteUrl }}
              </div>

              <div class="form-text">
                L’adresse générale du vendeur, et non le lien d’un produit.
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