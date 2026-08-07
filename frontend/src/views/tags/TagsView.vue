<script setup lang="ts">
import axios from 'axios'
import { computed, onMounted, ref } from 'vue'

import ConfirmModal from '@/components/common/ConfirmModal.vue'
import FormMessage from '@/components/forms/FormMessage.vue'
import TagFormModal from '@/components/tags/TagFormModal.vue'

import { useTagStore } from '@/stores/tag.store'

import type { ValidationErrorResponse } from '@/types/api'
import type {
  Tag,
  TagRequest,
} from '@/types/tags'

const tagStore = useTagStore()

const formModal =
  ref<InstanceType<typeof TagFormModal> | null>(null)

const formOpen = ref(false)
const selectedTag = ref<Tag | null>(null)

const deleteModalOpen = ref(false)
const tagToDelete = ref<Tag | null>(null)

const successMessage = ref('')
const localError = ref('')

const deleteMessage = computed(() => {
  if (!tagToDelete.value) {
    return ''
  }

  const itemCount = tagToDelete.value.itemCount

  if (itemCount > 0) {
    return `Le tag « ${tagToDelete.value.name} » est associé à ${itemCount} objet(s). Il sera retiré de ces objets avant suppression. Voulez-vous continuer ?`
  }

  return `Voulez-vous vraiment supprimer le tag « ${tagToDelete.value.name} » ?`
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
  selectedTag.value = null
  formOpen.value = true
  clearMessages()
}

function openEditModal(tag: Tag) {
  selectedTag.value = tag
  formOpen.value = true
  clearMessages()
}

function closeFormModal() {
  formOpen.value = false
  selectedTag.value = null
}

function askDelete(tag: Tag) {
  tagToDelete.value = tag
  deleteModalOpen.value = true
  clearMessages()
}

function closeDeleteModal() {
  deleteModalOpen.value = false
  tagToDelete.value = null
}

async function saveTag(data: TagRequest) {
  try {
    if (selectedTag.value) {
      await tagStore.updateTag(
        selectedTag.value.id,
        data,
      )

      successMessage.value =
        'Le tag a bien été modifié.'
    } else {
      await tagStore.createTag(data)

      successMessage.value =
        'Le tag a bien été créé.'
    }

    closeFormModal()
  } catch (exception) {
    formModal.value?.setApiError(exception)
  }
}

async function deleteTag() {
  if (!tagToDelete.value) {
    return
  }

  try {
    await tagStore.deleteTag(tagToDelete.value.id)

    successMessage.value =
      'Le tag a bien été supprimé.'

    closeDeleteModal()
  } catch (exception) {
    if (axios.isAxiosError<ValidationErrorResponse>(exception)) {
      localError.value =
        exception.response?.data?.message ??
        'Impossible de supprimer ce tag.'
    } else {
      localError.value =
        'Une erreur inattendue est survenue.'
    }

    closeDeleteModal()
  }
}

onMounted(() => {
  tagStore.fetchTags()
})
</script>

<template>
  <section>
    <div
      class="d-flex flex-column flex-md-row align-items-md-center justify-content-between gap-3 mb-4"
    >
      <div>
        <h2 class="h4 mb-1">
          Mes tags
        </h2>

        <p class="text-secondary mb-0">
          Ajoutez des étiquettes personnalisées à vos objets.
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

        Nouveau tag
      </button>
    </div>

    <FormMessage
      :message="successMessage"
      type="success"
    />

    <FormMessage
      :message="localError || tagStore.error || ''"
      type="error"
    />

    <div class="card">
      <div class="card-header">
        <h3 class="card-title">
          Tags
        </h3>

        <div class="card-tools">
          <span class="badge text-bg-primary">
            {{ tagStore.totalTags }}
          </span>
        </div>
      </div>

      <div class="card-body p-0">
        <div
          v-if="tagStore.loading"
          class="d-flex justify-content-center py-5"
        >
          <div
            class="spinner-border text-primary"
            role="status"
          >
            <span class="visually-hidden">
              Chargement des tags…
            </span>
          </div>
        </div>

        <div
          v-else-if="tagStore.tags.length === 0"
          class="text-center py-5 px-3"
        >
          <i
            class="bi bi-tags display-4 text-secondary"
            aria-hidden="true"
          />

          <h3 class="h5 mt-3">
            Aucun tag
          </h3>

          <p class="text-secondary">
            Créez un tag pour identifier rapidement vos objets.
          </p>

          <button
            type="button"
            class="btn btn-primary"
            @click="openCreateModal"
          >
            Créer un tag
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
                <th class="text-center">Objets</th>
                <th>Créé le</th>
                <th>Dernière modification</th>
                <th class="text-end">Actions</th>
              </tr>
            </thead>

            <tbody>
              <tr
                v-for="tag in tagStore.tags"
                :key="tag.id"
              >
                <td>
                  <span class="badge rounded-pill text-bg-info fs-6">
                    <i
                      class="bi bi-tag me-1"
                      aria-hidden="true"
                    />

                    {{ tag.name }}
                  </span>
                </td>

                <td class="text-center">
                  <span class="badge text-bg-secondary">
                    {{ tag.itemCount }}
                  </span>
                </td>

                <td>
                  {{ formatDate(tag.createdAt) }}
                </td>

                <td>
                  {{ formatDate(tag.updatedAt) }}
                </td>

                <td class="text-end">
                  <div class="btn-group">
                    <RouterLink
                      :to="{
                        path: '/items',
                        query: {
                          tagId: tag.id,
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
                      @click="openEditModal(tag)"
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
                      @click="askDelete(tag)"
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

    <TagFormModal
      ref="formModal"
      :open="formOpen"
      :tag="selectedTag"
      :loading="tagStore.saving"
      @close="closeFormModal"
      @submit="saveTag"
    />

    <ConfirmModal
      :open="deleteModalOpen"
      title="Supprimer le tag"
      :message="deleteMessage"
      confirm-label="Supprimer"
      :loading="tagStore.deleting"
      @close="closeDeleteModal"
      @confirm="deleteTag"
    />
  </section>
</template>