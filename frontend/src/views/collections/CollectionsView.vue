<script setup lang="ts">
import axios from 'axios'
import { computed, onMounted, ref } from 'vue'

import CollectionFormModal from '@/components/collections/CollectionFormModal.vue'
import ConfirmModal from '@/components/common/ConfirmModal.vue'
import FormMessage from '@/components/forms/FormMessage.vue'

import { useCollectionStore } from '@/stores/collection.store'

import type { ValidationErrorResponse } from '@/types/api'
import type {
  Collection,
  CollectionRequest,
} from '@/types/collections'

const collectionStore = useCollectionStore()

const formModal = ref<InstanceType<typeof CollectionFormModal> | null>(null)

const formOpen = ref(false)
const selectedCollection = ref<Collection | null>(null)

const deleteModalOpen = ref(false)
const collectionToDelete = ref<Collection | null>(null)

const successMessage = ref('')
const localError = ref('')

const deleteMessage = computed(() => {
  if (!collectionToDelete.value) {
    return ''
  }

  const itemCount = collectionToDelete.value.itemCount

  if (itemCount > 0) {
    return `La collection « ${collectionToDelete.value.name} » contient ${itemCount} objet(s). Sa suppression peut également affecter ces objets. Voulez-vous continuer ?`
  }

  return `Voulez-vous vraiment supprimer la collection « ${collectionToDelete.value.name} » ?`
})

function formatDate(date: string) {
  return new Intl.DateTimeFormat('fr-FR', {
    dateStyle: 'medium',
  }).format(new Date(date))
}

function openCreateModal() {
  selectedCollection.value = null
  formOpen.value = true
  successMessage.value = ''
  localError.value = ''
}

function openEditModal(collection: Collection) {
  selectedCollection.value = collection
  formOpen.value = true
  successMessage.value = ''
  localError.value = ''
}

function closeFormModal() {
  formOpen.value = false
  selectedCollection.value = null
}

function askDelete(collection: Collection) {
  collectionToDelete.value = collection
  deleteModalOpen.value = true
  successMessage.value = ''
  localError.value = ''
}

function closeDeleteModal() {
  deleteModalOpen.value = false
  collectionToDelete.value = null
}

async function saveCollection(data: CollectionRequest) {
  try {
    if (selectedCollection.value) {
      await collectionStore.updateCollection(
        selectedCollection.value.id,
        data,
      )

      successMessage.value =
        'La collection a bien été modifiée.'
    } else {
      await collectionStore.createCollection(data)

      successMessage.value =
        'La collection a bien été créée.'
    }

    closeFormModal()
  } catch (exception) {
    formModal.value?.setApiError(exception)
  }
}

async function deleteCollection() {
  if (!collectionToDelete.value) {
    return
  }

  try {
    await collectionStore.deleteCollection(
      collectionToDelete.value.id,
    )

    successMessage.value =
      'La collection a bien été supprimée.'

    closeDeleteModal()
  } catch (exception) {
    if (axios.isAxiosError<ValidationErrorResponse>(exception)) {
      localError.value =
        exception.response?.data?.message ??
        'Impossible de supprimer la collection.'
    } else {
      localError.value =
        'Une erreur inattendue est survenue.'
    }

    closeDeleteModal()
  }
}

onMounted(() => {
  collectionStore.fetchCollections()
})
</script>

<template>
  <section>
    <div
      class="d-flex flex-column flex-md-row align-items-md-center justify-content-between gap-3 mb-4"
    >
      <div>
        <h2 class="h4 mb-1">
          Mes collections
        </h2>

        <p class="text-secondary mb-0">
          Organisez vos objets dans différentes collections.
        </p>
      </div>

      <button
        type="button"
        class="btn btn-primary"
        @click="openCreateModal"
      >
        <i
          class="bi bi-plus-circle me-2"
          aria-hidden="true"
        />

        Nouvelle collection
      </button>
    </div>

    <FormMessage
      :message="successMessage"
      type="success"
    />

    <FormMessage
      :message="localError || collectionStore.error || ''"
      type="error"
    />

    <div class="card">
      <div class="card-header">
        <h3 class="card-title">
          Collections
        </h3>

        <div class="card-tools">
          <span class="badge text-bg-primary">
            {{ collectionStore.totalCollections }}
          </span>
        </div>
      </div>

      <div class="card-body p-0">
        <div
          v-if="collectionStore.loading"
          class="d-flex justify-content-center py-5"
        >
          <div
            class="spinner-border text-primary"
            role="status"
          >
            <span class="visually-hidden">
              Chargement des collections…
            </span>
          </div>
        </div>

        <div
          v-else-if="collectionStore.collections.length === 0"
          class="text-center py-5 px-3"
        >
          <i
            class="bi bi-collection display-4 text-secondary"
            aria-hidden="true"
          />

          <h3 class="h5 mt-3">
            Aucune collection
          </h3>

          <p class="text-secondary">
            Créez votre première collection pour commencer à ajouter des objets.
          </p>

          <button
            type="button"
            class="btn btn-primary"
            @click="openCreateModal"
          >
            Créer une collection
          </button>
        </div>

        <div
          v-else
          class="table-responsive"
        >
          <table class="table table-hover align-middle mb-0">
            <thead>
              <tr>
                <th>Nom</th>
                <th>Description</th>
                <th class="text-center">Objets</th>
                <th>Créée le</th>
                <th class="text-end">Actions</th>
              </tr>
            </thead>

            <tbody>
              <tr
                v-for="collection in collectionStore.collections"
                :key="collection.id"
              >
                <td>
                  <div class="fw-semibold">
                    {{ collection.name }}
                  </div>
                </td>

                <td>
                  <span
                    v-if="collection.description"
                    class="text-secondary"
                  >
                    {{ collection.description }}
                  </span>

                  <span
                    v-else
                    class="text-secondary fst-italic"
                  >
                    Aucune description
                  </span>
                </td>

                <td class="text-center">
                  <span class="badge text-bg-info">
                    {{ collection.itemCount }}
                  </span>
                </td>

                <td>
                  {{ formatDate(collection.createdAt) }}
                </td>

                <td class="text-end">
                  <div class="btn-group">
                    <RouterLink
                      :to="{
                        path: '/items',
                        query: {
                          collectionId: collection.id,
                        },
                      }"
                      class="btn btn-sm btn-outline-primary"
                      title="Voir les objets"
                    >
                      <i
                        class="bi bi-eye"
                        aria-hidden="true"
                      />
                    </RouterLink>

                    <button
                      type="button"
                      class="btn btn-sm btn-outline-secondary"
                      title="Modifier"
                      @click="openEditModal(collection)"
                    >
                      <i
                        class="bi bi-pencil"
                        aria-hidden="true"
                      />
                    </button>

                    <button
                      type="button"
                      class="btn btn-sm btn-outline-danger"
                      title="Supprimer"
                      @click="askDelete(collection)"
                    >
                      <i
                        class="bi bi-trash"
                        aria-hidden="true"
                      />
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <CollectionFormModal
      ref="formModal"
      :open="formOpen"
      :collection="selectedCollection"
      :loading="collectionStore.saving"
      @close="closeFormModal"
      @submit="saveCollection"
    />

    <ConfirmModal
      :open="deleteModalOpen"
      title="Supprimer la collection"
      :message="deleteMessage"
      confirm-label="Supprimer"
      :loading="collectionStore.deleting"
      @close="closeDeleteModal"
      @confirm="deleteCollection"
    />
  </section>
</template>