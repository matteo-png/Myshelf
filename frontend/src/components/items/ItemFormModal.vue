<script setup lang="ts">
import type { ValidationErrorResponse } from '@/types/api'
import type { Category } from '@/types/category'
import type { Collection } from '@/types/collections'
import { ITEM_STATUSES, type Item, type ItemRequest, type ItemStatus } from '@/types/items'
import type { PurchasePlace } from '@/types/purchase-place'
import type { Tag } from '@/types/tags'
import axios from 'axios'
import { computed, reactive, ref, watch } from 'vue'

const selectedFile = ref<File | null>(null)
const removeExistingFile = ref(false)

const props = withDefaults(
  defineProps<{
    open: boolean
    item?: Item | null
    collections: Collection[]
    categories: Category[]
    tags: Tag[]
    purchasePlaces: PurchasePlace[]
    loading?: boolean
  }>(),
  {
    item: null,
    loading: false,
  },
)
const statusLabels: Record<ItemStatus, string> = {
  ACTIVE: 'Actif',
  SOLD: 'Vendu',
  LOST: 'Perdu',
  GIVEN: 'Donné',
  OTHER: 'Autre',
}
const emit = defineEmits<{
  close: []
  submit: [data: ItemRequest, file: File | null]
}>()

const form = reactive({
  collectionId: '',
  categoryId: '',
  purchasePlaceId: '',

  name: '',
  description: '',
  estimatedValue: '',
  purchaseDate: '',
  purchaseUrl: '',
  status: 'ACTIVE' as ItemStatus,

  tagIds: [] as number[],
})

const fieldErrors = reactive<Record<string, string>>({})
const globalError = ref('')

const isEditing = computed(() => Boolean(props.item))

const title = computed(() => (isEditing.value ? 'Modifier l’objet' : 'Ajouter un objet'))

const isFormValid = computed(() => {
  return Number(form.collectionId) > 0 && form.name.trim().length > 0
})

function clearErrors() {
  globalError.value = ''

  for (const key of Object.keys(fieldErrors)) {
    delete fieldErrors[key]
  }
}

function resetForm() {
  selectedFile.value = null
  removeExistingFile.value = false

  form.collectionId = props.item?.collectionId ? String(props.item.collectionId) : ''

  form.categoryId = props.item?.categoryId ? String(props.item.categoryId) : ''

  form.purchasePlaceId = props.item?.purchasePlaceId ? String(props.item.purchasePlaceId) : ''

  form.name = props.item?.name ?? ''
  form.description = props.item?.description ?? ''

  form.estimatedValue =
    props.item?.estimatedValue !== null && props.item?.estimatedValue !== undefined
      ? String(props.item.estimatedValue)
      : ''

  form.purchaseDate = props.item?.purchaseDate ?? ''
  form.purchaseUrl = props.item?.purchaseUrl ?? ''
  form.status = props.item?.status ?? 'ACTIVE'

  form.tagIds = props.item
    ? props.tags.filter((tag) => props.item?.tags.includes(tag.name)).map((tag) => tag.id)
    : []

  clearErrors()
}

function selectFile(event: Event) {
  const input = event.target as HTMLInputElement

  selectedFile.value = input.files?.[0] ?? null

  if (selectedFile.value) {
    removeExistingFile.value = false
  }
}

function removeFile() {
  selectedFile.value = null
  removeExistingFile.value = true
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
    fieldErrors.name = 'Le nom de l’objet est obligatoire.'
  }

  if (!Number(form.collectionId)) {
    fieldErrors.collectionId = 'La collection est obligatoire.'
  }

  if (!isFormValid.value) {
    return
  }

  emit(
    'submit',
    {
      collectionId: Number(form.collectionId),

      categoryId: form.categoryId ? Number(form.categoryId) : null,

      purchasePlaceId: form.purchasePlaceId ? Number(form.purchasePlaceId) : null,

      name: form.name.trim(),
      description: form.description.trim() || null,

      estimatedValue: form.estimatedValue !== '' ? Number(form.estimatedValue) : null,

      purchaseDate: form.purchaseDate || null,
      purchaseUrl: form.purchaseUrl.trim() || null,

      status: form.status,
      tagIds: [...form.tagIds],

      removeFile: removeExistingFile.value,
    },

    selectedFile.value,
  )
}

function setApiError(exception: unknown) {
  clearErrors()

  if (axios.isAxiosError<ValidationErrorResponse>(exception)) {
    const response = exception.response?.data

    if (response?.errors) {
      Object.assign(fieldErrors, response.errors)
    }

    globalError.value = response?.message ?? 'Impossible d’enregistrer cet objet.'

    return
  }

  globalError.value = 'Une erreur inattendue est survenue.'
}

defineExpose({
  setApiError,
})

watch(
  () => [props.open, props.item, props.tags],
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
    <div class="modal-dialog modal-dialog-centered modal-xl">
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
            <FormMessage :message="globalError" type="error" />

            <div class="row">
              <div class="col-md-8 mb-3">
                <label for="item-name" class="form-label"> Nom </label>

                <input
                  id="item-name"
                  v-model="form.name"
                  type="text"
                  class="form-control"
                  :class="{ 'is-invalid': fieldErrors.name }"
                  maxlength="255"
                />

                <div v-if="fieldErrors.name" class="invalid-feedback">
                  {{ fieldErrors.name }}
                </div>
              </div>

              <div class="col-md-4 mb-3">
                <label for="item-status" class="form-label"> Statut </label>

                <select id="item-status" v-model="form.status" class="form-select">
                  <option v-for="status in ITEM_STATUSES" :key="status" :value="status">
                    {{ statusLabels[status] }}
                  </option>
                </select>
              </div>
            </div>

            <div class="row">
              <div class="col-md-4 mb-3">
                <label for="item-collection" class="form-label"> Collection </label>

                <select
                  id="item-collection"
                  v-model="form.collectionId"
                  class="form-select"
                  :class="{
                    'is-invalid': fieldErrors.collectionId,
                  }"
                >
                  <option value="">Sélectionner une collection</option>

                  <option
                    v-for="collection in collections"
                    :key="collection.id"
                    :value="String(collection.id)"
                  >
                    {{ collection.name }}
                  </option>
                </select>

                <div v-if="fieldErrors.collectionId" class="invalid-feedback">
                  {{ fieldErrors.collectionId }}
                </div>
              </div>

              <div class="col-md-4 mb-3">
                <label for="item-category" class="form-label"> Catégorie </label>

                <select id="item-category" v-model="form.categoryId" class="form-select">
                  <option value="">Aucune catégorie</option>

                  <option
                    v-for="category in categories"
                    :key="category.id"
                    :value="String(category.id)"
                  >
                    {{ category.name }}
                  </option>
                </select>
              </div>

              <div class="col-md-4 mb-3">
                <label for="item-purchase-place" class="form-label"> Lieu d’achat </label>

                <select id="item-purchase-place" v-model="form.purchasePlaceId" class="form-select">
                  <option value="">Aucun lieu d’achat</option>

                  <option v-for="place in purchasePlaces" :key="place.id" :value="String(place.id)">
                    {{ place.name }}
                  </option>
                </select>
              </div>
            </div>

            <div class="mb-3">
              <label for="item-description" class="form-label"> Description </label>

              <textarea
                id="item-description"
                v-model="form.description"
                class="form-control"
                rows="4"
                maxlength="2000"
              />
            </div>

            <div class="row">
              <div class="col-md-4 mb-3">
                <label for="item-estimated-value" class="form-label"> Valeur estimée </label>

                <div class="input-group">
                  <input
                    id="item-estimated-value"
                    v-model="form.estimatedValue"
                    type="number"
                    class="form-control"
                    min="0"
                    step="0.01"
                  />

                  <span class="input-group-text"> € </span>
                </div>
              </div>

              <div class="col-md-4 mb-3">
                <label for="item-purchase-date" class="form-label"> Date d’achat </label>

                <input
                  id="item-purchase-date"
                  v-model="form.purchaseDate"
                  type="date"
                  class="form-control"
                />
              </div>

              <div class="col-md-4 mb-3">
                <label for="item-purchase-url" class="form-label"> URL d’achat </label>

                <input
                  id="item-purchase-url"
                  v-model="form.purchaseUrl"
                  type="url"
                  class="form-control"
                  placeholder="https://..."
                />
              </div>
              <div class="mb-3">
                <label for="item-file" class="form-label"> Fichier </label>

                <input id="item-file" type="file" class="form-control" @change="selectFile" />

                <div class="form-text">
                  Ajoutez une facture, un certificat, une image ou un autre document lié à l’objet.
                </div>

                <div v-if="selectedFile" class="alert alert-info mt-3 mb-0">
                  <i class="bi bi-paperclip me-2" aria-hidden="true" />

                  {{ selectedFile.name }}
                </div>

                <div
                  v-else-if="item?.fileName && !removeExistingFile"
                  class="alert alert-secondary mt-3 mb-0"
                >
                  <div class="d-flex align-items-center justify-content-between gap-3">
                    <span>
                      <i class="bi bi-file-earmark me-2" aria-hidden="true" />

                      {{ item.fileName }}
                    </span>

                    <button type="button" class="btn btn-sm btn-outline-danger" @click="removeFile">
                      Retirer
                    </button>
                  </div>
                </div>

                <div v-if="removeExistingFile" class="alert alert-warning mt-3 mb-0">
                  Le fichier actuel sera supprimé lors de l’enregistrement.
                </div>
              </div>
            </div>

            <div>
              <label class="form-label"> Tags </label>

              <div v-if="tags.length === 0" class="text-secondary">Aucun tag disponible.</div>

              <div v-else class="d-flex flex-wrap gap-3">
                <div v-for="tag in tags" :key="tag.id" class="form-check">
                  <input
                    :id="`item-tag-${tag.id}`"
                    v-model="form.tagIds"
                    type="checkbox"
                    class="form-check-input"
                    :value="tag.id"
                  />

                  <label :for="`item-tag-${tag.id}`" class="form-check-label">
                    {{ tag.name }}
                  </label>
                </div>
              </div>
            </div>
          </div>

          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" :disabled="loading" @click="close">
              Annuler
            </button>

            <button type="submit" class="btn btn-primary" :disabled="loading || !isFormValid">
              <span
                v-if="loading"
                class="spinner-border spinner-border-sm me-2"
                aria-hidden="true"
              />

              {{ loading ? 'Enregistrement…' : isEditing ? 'Enregistrer' : 'Ajouter' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>

  <div v-if="open" class="modal-backdrop fade show" />
</template>
