<script setup lang="ts">
import axios from 'axios'
import { computed, onMounted, ref } from 'vue'

import CategoryFormModal from '@/components/categories/CategoryFormModal.vue'
import ConfirmModal from '@/components/common/ConfirmModal.vue'
import FormMessage from '@/components/forms/FormMessage.vue'

import { useCategoryStore } from '@/stores/category.store'

import type { ValidationErrorResponse } from '@/types/api'
import type {
  Category,
  CategoryRequest,
} from '@/types/category'

const categoryStore = useCategoryStore()

const formModal =
  ref<InstanceType<typeof CategoryFormModal> | null>(null)

const formOpen = ref(false)
const selectedCategory = ref<Category | null>(null)

const deleteModalOpen = ref(false)
const categoryToDelete = ref<Category | null>(null)

const successMessage = ref('')
const localError = ref('')

const deleteMessage = computed(() => {
  if (!categoryToDelete.value) {
    return ''
  }

  return `Voulez-vous vraiment supprimer la catégorie « ${categoryToDelete.value.name} » ?`
})

function formatDate(date: string) {
  return new Intl.DateTimeFormat('fr-FR', {
    dateStyle: 'medium',
  }).format(new Date(date))
}

function clearMessages() {
  successMessage.value = ''
  localError.value = ''
}

function openCreateModal() {
  selectedCategory.value = null
  formOpen.value = true
  clearMessages()
}

function openEditModal(category: Category) {
  selectedCategory.value = category
  formOpen.value = true
  clearMessages()
}

function closeFormModal() {
  formOpen.value = false
  selectedCategory.value = null
}

function askDelete(category: Category) {
  categoryToDelete.value = category
  deleteModalOpen.value = true
  clearMessages()
}

function closeDeleteModal() {
  deleteModalOpen.value = false
  categoryToDelete.value = null
}

async function saveCategory(data: CategoryRequest) {
  try {
    if (selectedCategory.value) {
      await categoryStore.updateCategory(
        selectedCategory.value.id,
        data,
      )

      successMessage.value =
        'La catégorie a bien été modifiée.'
    } else {
      await categoryStore.createCategory(data)

      successMessage.value =
        'La catégorie a bien été créée.'
    }

    closeFormModal()
  } catch (exception) {
    formModal.value?.setApiError(exception)
  }
}

async function deleteCategory() {
  if (!categoryToDelete.value) {
    return
  }

  try {
    await categoryStore.deleteCategory(
      categoryToDelete.value.id,
    )

    successMessage.value =
      'La catégorie a bien été supprimée.'

    closeDeleteModal()
  } catch (exception) {
    if (axios.isAxiosError<ValidationErrorResponse>(exception)) {
      localError.value =
        exception.response?.data?.message ??
        'Impossible de supprimer cette catégorie.'
    } else {
      localError.value =
        'Une erreur inattendue est survenue.'
    }

    closeDeleteModal()
  }
}

onMounted(() => {
  categoryStore.fetchCategories()
})
</script>

<template>
  <section>
    <div
      class="d-flex flex-column flex-md-row align-items-md-center justify-content-between gap-3 mb-4"
    >
      <div>
        <h2 class="h4 mb-1">
          Mes catégories
        </h2>

        <p class="text-secondary mb-0">
          Classez vos objets par type ou par thème.
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

        Nouvelle catégorie
      </button>
    </div>

    <FormMessage
      :message="successMessage"
      type="success"
    />

    <FormMessage
      :message="localError || categoryStore.error || ''"
      type="error"
    />

    <div class="card">
      <div class="card-header">
        <h3 class="card-title">
          Catégories
        </h3>

        <div class="card-tools">
          <span class="badge text-bg-primary">
            {{ categoryStore.totalCategories }}
          </span>
        </div>
      </div>

      <div class="card-body p-0">
        <div
          v-if="categoryStore.loading"
          class="d-flex justify-content-center py-5"
        >
          <div
            class="spinner-border text-primary"
            role="status"
          >
            <span class="visually-hidden">
              Chargement des catégories…
            </span>
          </div>
        </div>

        <div
          v-else-if="categoryStore.categories.length === 0"
          class="text-center py-5 px-3"
        >
          <i
            class="bi bi-folder display-4 text-secondary"
            aria-hidden="true"
          />

          <h3 class="h5 mt-3">
            Aucune catégorie
          </h3>

          <p class="text-secondary">
            Créez une catégorie pour mieux organiser vos objets.
          </p>

          <button
            type="button"
            class="btn btn-primary"
            @click="openCreateModal"
          >
            Créer une catégorie
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
                <th>Créée le</th>
                <th>Dernière modification</th>
                <th class="text-end">Actions</th>
              </tr>
            </thead>

            <tbody>
              <tr
                v-for="category in categoryStore.categories"
                :key="category.id"
              >
                <td>
                  <div class="d-flex align-items-center gap-2">
                    <span
                      class="d-inline-flex align-items-center justify-content-center rounded-circle bg-body-secondary"
                      style="width: 2.25rem; height: 2.25rem"
                    >
                      <i
                        class="bi bi-folder"
                        aria-hidden="true"
                      />
                    </span>

                    <span class="fw-semibold">
                      {{ category.name }}
                    </span>
                  </div>
                </td>

                <td>
                  {{ formatDate(category.createdAt) }}
                </td>

                <td>
                  {{ formatDate(category.updatedAt) }}
                </td>

                <td class="text-end">
                  <div class="btn-group">
                    <RouterLink
                      :to="{
                        path: '/items',
                        query: {
                          categoryId: category.id,
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
                      @click="openEditModal(category)"
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
                      @click="askDelete(category)"
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

    <CategoryFormModal
      ref="formModal"
      :open="formOpen"
      :category="selectedCategory"
      :loading="categoryStore.saving"
      @close="closeFormModal"
      @submit="saveCategory"
    />

    <ConfirmModal
      :open="deleteModalOpen"
      title="Supprimer la catégorie"
      :message="deleteMessage"
      confirm-label="Supprimer"
      :loading="categoryStore.deleting"
      @close="closeDeleteModal"
      @confirm="deleteCategory"
    />
  </section>
</template>