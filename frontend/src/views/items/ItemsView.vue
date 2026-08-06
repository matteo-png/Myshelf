<script setup lang="ts">
import axios from 'axios'
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import ConfirmModal from '@/components/common/ConfirmModal.vue'
import FormMessage from '@/components/forms/FormMessage.vue'
import ItemFormModal from '@/components/items/ItemFormModal.vue'

import { referenceDataService } from '@/services/reference-data.service'
import { useItemStore } from '@/stores/item.store'

import type { ValidationErrorResponse } from '@/types/api'
import type { Category } from '@/types/category'
import type { Collection } from '@/types/collections'
import {
  ITEM_STATUSES,
  type Item,
  type ItemRequest,
  type ItemStatus,
} from '@/types/items'
import type { PurchasePlace } from '@/types/purchase-place'
import type { Tag } from '@/types/tags'

const statusLabels: Record<ItemStatus, string> = {
  ACTIVE: 'Actif',
  SOLD: 'Vendu',
  LOST: 'Perdu',
  GIVEN: 'Donné',
  OTHER: 'Autre',
}
const searchQuery = ref('')
const selectedCategoryId = ref<number | null>(null)
const selectedTagId = ref<number | null>(null)
const selectedPurchasePlaceId = ref<number | null>(null)
const selectedStatus = ref<ItemStatus | null>(null)

const route = useRoute()
const router = useRouter()
const itemStore = useItemStore()

const collections = ref<Collection[]>([])
const categories = ref<Category[]>([])
const tags = ref<Tag[]>([])
const purchasePlaces = ref<PurchasePlace[]>([])

const referencesLoading = ref(false)

const selectedCollectionId = ref<number | null>(
  route.query.collectionId
    ? Number(route.query.collectionId)
    : null,
)

const formModal = ref<InstanceType<typeof ItemFormModal> | null>(null)
const formOpen = ref(false)
const selectedItem = ref<Item | null>(null)

const deleteModalOpen = ref(false)
const itemToDelete = ref<Item | null>(null)

const successMessage = ref('')
const localError = ref('')

const deleteMessage = computed(() => {
  if (!itemToDelete.value) {
    return ''
  }

  return `Voulez-vous vraiment supprimer l’objet « ${itemToDelete.value.name} » ?`
})

function formatDate(date: string | null) {
  if (!date) {
    return 'Non renseignée'
  }

  return new Intl.DateTimeFormat('fr-FR', {
    dateStyle: 'medium',
  }).format(new Date(`${date}T00:00:00`))
}

function formatCurrency(value: number | null) {
  if (value === null || value === undefined) {
    return '—'
  }

  return new Intl.NumberFormat('fr-FR', {
    style: 'currency',
    currency: 'EUR',
  }).format(value)
}

function openCreateModal() {
  selectedItem.value = null
  formOpen.value = true
  successMessage.value = ''
  localError.value = ''
}

function openEditModal(item: Item) {
  selectedItem.value = item
  formOpen.value = true
  successMessage.value = ''
  localError.value = ''
}

function closeFormModal() {
  formOpen.value = false
  selectedItem.value = null
}

function askDelete(item: Item) {
  itemToDelete.value = item
  deleteModalOpen.value = true
  successMessage.value = ''
  localError.value = ''
}

function closeDeleteModal() {
  deleteModalOpen.value = false
  itemToDelete.value = null
}

async function loadReferenceData() {
  referencesLoading.value = true

  try {
    const [
      collectionsResponse,
      categoriesResponse,
      tagsResponse,
      placesResponse,
    ] = await Promise.all([
      referenceDataService.getCollections(),
      referenceDataService.getCategories(),
      referenceDataService.getTags(),
      referenceDataService.getPurchasePlaces(),
    ])

    collections.value = collectionsResponse.data
    categories.value = categoriesResponse.data
    tags.value = tagsResponse.data
    purchasePlaces.value = placesResponse.data
  } catch {
    localError.value =
      'Impossible de charger les données nécessaires au formulaire.'
  } finally {
    referencesLoading.value = false
  }
}

async function changeCollectionFilter() {
  await router.replace({
    query: selectedCollectionId.value
      ? {
          collectionId: String(selectedCollectionId.value),
        }
      : {},
  })

  await itemStore.fetchItems(selectedCollectionId.value)
}

async function saveItem(data: ItemRequest) {
  try {
    if (selectedItem.value) {
      await itemStore.updateItem(selectedItem.value.id, data)
      successMessage.value = 'L’objet a bien été modifié.'
    } else {
      await itemStore.createItem(data)
      successMessage.value = 'L’objet a bien été ajouté.'
    }

    closeFormModal()

    if (
      selectedCollectionId.value &&
      data.collectionId !== selectedCollectionId.value
    ) {
      await itemStore.fetchItems(selectedCollectionId.value)
    }
  } catch (exception) {
    formModal.value?.setApiError(exception)
  }
}

async function deleteItem() {
  if (!itemToDelete.value) {
    return
  }

  try {
    await itemStore.deleteItem(itemToDelete.value.id)
    successMessage.value = 'L’objet a bien été supprimé.'
    closeDeleteModal()
  } catch (exception) {
    if (axios.isAxiosError<ValidationErrorResponse>(exception)) {
      localError.value =
        exception.response?.data?.message ??
        'Impossible de supprimer cet objet.'
    } else {
      localError.value =
        'Une erreur inattendue est survenue.'
    }

    closeDeleteModal()
  }
}

watch(
  () => route.query.collectionId,
  (collectionId) => {
    selectedCollectionId.value = collectionId
      ? Number(collectionId)
      : null
  },
)

onMounted(async () => {
  await Promise.all([
    itemStore.fetchItems(selectedCollectionId.value),
    loadReferenceData(),
  ])
})


const filteredItems = computed(() => {
  const search = searchQuery.value.trim().toLocaleLowerCase('fr-FR')

  const selectedTag = selectedTagId.value
    ? tags.value.find((tag) => tag.id === selectedTagId.value)
    : null

  return itemStore.items.filter((item) => {
    const matchesSearch =
      !search ||
      item.name.toLocaleLowerCase('fr-FR').includes(search) ||
      item.description?.toLocaleLowerCase('fr-FR').includes(search)

    const matchesCategory =
      selectedCategoryId.value === null ||
      item.categoryId === selectedCategoryId.value

    const matchesPurchasePlace =
      selectedPurchasePlaceId.value === null ||
      item.purchasePlaceId === selectedPurchasePlaceId.value

    const matchesStatus =
      selectedStatus.value === null ||
      item.status === selectedStatus.value

    /*
     * ItemResponse contient les noms des tags et non leurs identifiants.
     * On récupère donc le tag sélectionné dans les références,
     * puis on compare son nom avec item.tags.
     */
    const matchesTag =
      selectedTag === null ||
      item.tags.includes(selectedTag.name)

    return (
      matchesSearch &&
      matchesCategory &&
      matchesPurchasePlace &&
      matchesStatus &&
      matchesTag
    )
  })
})

const hasActiveFilters = computed(() => {
  return (
    searchQuery.value.trim() !== '' ||
    selectedCollectionId.value !== null ||
    selectedCategoryId.value !== null ||
    selectedTagId.value !== null ||
    selectedPurchasePlaceId.value !== null ||
    selectedStatus.value !== null
  )
})

function resetFilters() {
  searchQuery.value = ''
  selectedCategoryId.value = null
  selectedTagId.value = null
  selectedPurchasePlaceId.value = null
  selectedStatus.value = null

  if (selectedCollectionId.value !== null) {
    selectedCollectionId.value = null
    void changeCollectionFilter()
  }
}

</script>

<template>
  <section>
    <div
      class="d-flex flex-column flex-md-row align-items-md-center justify-content-between gap-3 mb-4"
    >
      <div>
        <h2 class="h4 mb-1">
          Mes objets
        </h2>

        <p class="text-secondary mb-0">
          Consultez et gérez tous les objets de vos collections.
        </p>
      </div>

      <button
        type="button"
        class="btn btn-primary"
        :disabled="referencesLoading || collections.length === 0"
        @click="openCreateModal"
      >
        <i class="bi bi-plus-circle me-2" />
        Ajouter un objet
      </button>
    </div>

    <FormMessage
      :message="successMessage"
      type="success"
    />

    <FormMessage
      :message="localError || itemStore.error || ''"
      type="error"
    />

    <div
      v-if="collections.length === 0 && !referencesLoading"
      class="alert alert-warning"
    >
      Vous devez créer une collection avant de pouvoir ajouter un objet.

      <RouterLink
        to="/collections"
        class="alert-link"
      >
        Créer une collection
      </RouterLink>
    </div>

    <div class="card">
      <div class="card-header">
        <div
          class="d-flex flex-column flex-md-row align-items-md-center justify-content-between gap-3"
        >
          <h3 class="card-title mb-0">
            Objets
          </h3>

        <div class="d-flex align-items-center gap-2">
          <label for="items-search" class="form-label">
            Recherche
          </label>

          <div class="input-group">
            <span class="input-group-text">
              <i class="bi bi-search" aria-hidden="true" />
            </span>

            <input
              id="items-search"
              v-model="searchQuery"
              type="search"
              class="form-control"
              placeholder="Nom..."
            />
          </div>
        </div>
        
          <div class="d-flex align-items-center gap-2">
            <label
              for="items-collection-filter"
              class="form-label mb-0"
            >
              Collection
            </label>

            <select
              id="items-collection-filter"
              v-model="selectedCollectionId"
              class="form-select"
              @change="changeCollectionFilter"
            >
              <option :value="null">
                Toutes les collections
              </option>

              <option
                v-for="collection in collections"
                :key="collection.id"
                :value="collection.id"
              >
                {{ collection.name }}
              </option>
            </select> 

            <span class="badge text-bg-primary">
              {{ filteredItems.length }}
            </span>
          </div>
        </div>
      </div>

      <div class="card-body p-0">
        <div
          v-if="itemStore.loading"
          class="d-flex justify-content-center py-5"
        >
          <div class="spinner-border text-primary">
            <span class="visually-hidden">
              Chargement des objets…
            </span>
          </div>
        </div>

        <div
          v-else-if="filteredItems.length === 0"
          class="text-center py-5 px-3"
        >
          <i class="bi bi-box-seam display-4 text-secondary" />

          <h3 class="h5 mt-3">
            Aucun objet
          </h3>

          <p class="text-secondary">
            Ajoutez votre premier objet à une collection.
          </p>

          <button
            v-if="collections.length > 0"
            type="button"
            class="btn btn-primary"
            @click="openCreateModal"
          >
            Ajouter un objet
          </button>
        </div>

        <div
          v-else
          class="table-responsive"
        >

        <div class="card mb-4">
  <div class="card-header">
    <div class="d-flex align-items-center justify-content-between">
      <h3 class="card-title mb-0">
        <i class="bi bi-funnel me-2" aria-hidden="true" />
        Filtres
      </h3>

      <button
        v-if="hasActiveFilters"
        type="button"
        class="btn btn-sm btn-outline-secondary"
        @click="resetFilters"
      >
        <i class="bi bi-x-circle me-1" aria-hidden="true" />
        Réinitialiser
      </button>
    </div>
  </div>

  <div class="card-body">
    <div class="row g-3">

      <div class="col-xl-4 col-md-6">
        <label for="items-category-filter" class="form-label">
          Catégorie
        </label>

        <select
          id="items-category-filter"
          v-model="selectedCategoryId"
          class="form-select"
        >
          <option :value="null">
            Toutes les catégories
          </option>

          <option
            v-for="category in categories"
            :key="category.id"
            :value="category.id"
          >
            {{ category.name }}
          </option>
        </select>
      </div>

      <div class="col-xl-4 col-md-6">
        <label for="items-tag-filter" class="form-label">
          Tag
        </label>

        <select
          id="items-tag-filter"
          v-model="selectedTagId"
          class="form-select"
        >
          <option :value="null">
            Tous les tags
          </option>

          <option
            v-for="tag in tags"
            :key="tag.id"
            :value="tag.id"
          >
            {{ tag.name }}
          </option>
        </select>
      </div>

      <div class="col-xl-4 col-md-6">
        <label for="items-place-filter" class="form-label">
          Lieu d’achat
        </label>

        <select
          id="items-place-filter"
          v-model="selectedPurchasePlaceId"
          class="form-select"
        >
          <option :value="null">
            Tous les lieux d’achat
          </option>

          <option
            v-for="place in purchasePlaces"
            :key="place.id"
            :value="place.id"
          >
            {{ place.name }}
          </option>
        </select>
      </div>

      <div class="col-xl-4 col-md-6">
        <label for="items-status-filter" class="form-label">
          Statut
        </label>

        <select
          id="items-status-filter"
          v-model="selectedStatus"
          class="form-select"
        >
          <option :value="null">
            Tous les statuts
          </option>

          <option
            v-for="status in ITEM_STATUSES"
            :key="status"
            :value="status"
          >
            {{ statusLabels[status] }}
          </option>
        </select>
      </div>
    </div>
  </div>
</div>
          <table class="table table-hover align-middle mb-0">
            <thead>
              <tr>
                <th>Nom</th>
                <th>Collection</th>
                <th>Catégorie</th>
                <th>Valeur</th>
                <th>Date d’achat</th>
                <th>Tags</th>
                <th class="text-end">Actions</th>
              </tr>
            </thead>

            <tbody>
              <tr
                v-for="item in filteredItems"
                :key="item.id"
              >
                <td>
                  <div class="fw-semibold">
                    {{ item.name }}
                  </div>

                  <div
                    v-if="item.purchasePlaceName"
                    class="small text-secondary"
                  >
                    <i class="bi bi-shop me-1" />
                    {{ item.purchasePlaceName }}
                  </div>
                </td>

                <td>
                  {{ item.collectionName }}
                </td>

                <td>
                  <span
                    v-if="item.categoryName"
                    class="badge text-bg-secondary"
                  >
                    {{ item.categoryName }}
                  </span>

                  <span v-else class="text-secondary">
                    —
                  </span>
                </td>

                <td>
                  {{ formatCurrency(item.estimatedValue) }}
                </td>

                <td>
                  {{ formatDate(item.purchaseDate) }}
                </td>

                <td>
                  <div class="d-flex flex-wrap gap-1">
                    <span
                      v-for="tag in item.tags"
                      :key="tag"
                      class="badge text-bg-info"
                    >
                      {{ tag }}
                    </span>

                    <span
                      v-if="item.tags.length === 0"
                      class="text-secondary"
                    >
                      —
                    </span>
                  </div>
                </td>

                <td class="text-end">
                  <div class="btn-group">
                    <a
                      v-if="item.purchaseUrl"
                      :href="item.purchaseUrl"
                      target="_blank"
                      rel="noopener noreferrer"
                      class="btn btn-sm btn-outline-primary"
                      title="Voir le lien d’achat"
                    >
                      <i class="bi bi-box-arrow-up-right" />
                    </a>

                    <button
                      type="button"
                      class="btn btn-sm btn-outline-secondary"
                      title="Modifier"
                      @click="openEditModal(item)"
                    >
                      <i class="bi bi-pencil" />
                    </button>

                    <button
                      type="button"
                      class="btn btn-sm btn-outline-danger"
                      title="Supprimer"
                      @click="askDelete(item)"
                    >
                      <i class="bi bi-trash" />
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <ItemFormModal
      ref="formModal"
      :open="formOpen"
      :item="selectedItem"
      :collections="collections"
      :categories="categories"
      :tags="tags"
      :purchase-places="purchasePlaces"
      :loading="itemStore.saving"
      @close="closeFormModal"
      @submit="saveItem"
    />

    <ConfirmModal
      :open="deleteModalOpen"
      title="Supprimer l’objet"
      :message="deleteMessage"
      confirm-label="Supprimer"
      :loading="itemStore.deleting"
      @close="closeDeleteModal"
      @confirm="deleteItem"
    />
  </section>
</template>