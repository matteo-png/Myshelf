<script setup lang="ts">
import axios from 'axios'
import { computed, onMounted, ref } from 'vue'

import ConfirmModal from '@/components/common/ConfirmModal.vue'
import FormMessage from '@/components/forms/FormMessage.vue'
import PurchasePlaceFormModal from '@/components/purchase-places/PurchasePlaceFormModal.vue'

import { usePurchasePlaceStore } from '@/stores/purchase-place.store'

import type { ValidationErrorResponse } from '@/types/api'
import type {
  PurchasePlace,
  PurchasePlaceRequest,
  PurchasePlaceType,
} from '@/types/purchase-place'

const purchasePlaceStore = usePurchasePlaceStore()

const purchasePlaceTypeLabels: Record<PurchasePlaceType, string> = {
  MAGASIN: 'Magasin',
  MARKETPLACE: 'Marketplace',
  ONLINE: 'Boutique en ligne',
  OTHER: 'Autre',
}

const formModal =
  ref<InstanceType<typeof PurchasePlaceFormModal> | null>(null)

const formOpen = ref(false)
const selectedPurchasePlace = ref<PurchasePlace | null>(null)

const deleteModalOpen = ref(false)
const purchasePlaceToDelete = ref<PurchasePlace | null>(null)

const successMessage = ref('')
const localError = ref('')

const deleteMessage = computed(() => {
  if (!purchasePlaceToDelete.value) {
    return ''
  }

  const place = purchasePlaceToDelete.value

  if (place.itemCount > 0) {
    return `Le lieu d’achat « ${place.name} » est utilisé par ${place.itemCount} objet(s). Vous devez d’abord modifier ces objets avant de pouvoir supprimer ce lieu d’achat.`
  }

  return `Voulez-vous vraiment supprimer le lieu d’achat « ${place.name} » ?`
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
  selectedPurchasePlace.value = null
  formOpen.value = true
  clearMessages()
}

function openEditModal(place: PurchasePlace) {
  selectedPurchasePlace.value = place
  formOpen.value = true
  clearMessages()
}

function closeFormModal() {
  formOpen.value = false
  selectedPurchasePlace.value = null
}

function askDelete(place: PurchasePlace) {
  clearMessages()

  if (place.itemCount > 0) {
    localError.value =
      `Impossible de supprimer « ${place.name} » : ce lieu d’achat est utilisé par ${place.itemCount} objet(s).`

    return
  }

  purchasePlaceToDelete.value = place
  deleteModalOpen.value = true
}

function closeDeleteModal() {
  deleteModalOpen.value = false
  purchasePlaceToDelete.value = null
}

async function savePurchasePlace(data: PurchasePlaceRequest) {
  try {
    if (selectedPurchasePlace.value) {
      await purchasePlaceStore.updatePurchasePlace(
        selectedPurchasePlace.value.id,
        data,
      )

      successMessage.value =
        'Le lieu d’achat a bien été modifié.'
    } else {
      await purchasePlaceStore.createPurchasePlace(data)

      successMessage.value =
        'Le lieu d’achat a bien été créé.'
    }

    closeFormModal()
  } catch (exception) {
    formModal.value?.setApiError(exception)
  }
}

async function deletePurchasePlace() {
  if (!purchasePlaceToDelete.value) {
    return
  }

  try {
    await purchasePlaceStore.deletePurchasePlace(
      purchasePlaceToDelete.value.id,
    )

    successMessage.value =
      'Le lieu d’achat a bien été supprimé.'

    closeDeleteModal()
  } catch (exception) {
    if (axios.isAxiosError<ValidationErrorResponse>(exception)) {
      localError.value =
        exception.response?.data?.message ??
        'Impossible de supprimer ce lieu d’achat.'
    } else {
      localError.value =
        'Une erreur inattendue est survenue.'
    }

    closeDeleteModal()
  }
}

onMounted(() => {
  purchasePlaceStore.fetchPurchasePlaces()
})
</script>

<template>
  <section>
    <div
      class="d-flex flex-column flex-md-row align-items-md-center justify-content-between gap-3 mb-4"
    >
      <div>
        <h2 class="h4 mb-1">
          Mes lieux d’achat
        </h2>

        <p class="text-secondary mb-0">
          Gérez les boutiques, vendeurs et lieux où vous achetez vos objets.
        </p>
      </div>

      <button
        type="button"
        class="btn btn-primary"
        @click="openCreateModal"
      >
        <i class="bi bi-plus-circle me-2" aria-hidden="true" />
        Nouveau lieu d’achat
      </button>
    </div>

    <FormMessage
      :message="successMessage"
      type="success"
    />

    <FormMessage
      :message="localError || purchasePlaceStore.error || ''"
      type="error"
    />

    <div class="card">
      <div class="card-header">
        <h3 class="card-title">
          Lieux d’achat
        </h3>

        <div class="card-tools">
          <span class="badge text-bg-primary">
            {{ purchasePlaceStore.totalPurchasePlaces }}
          </span>
        </div>
      </div>

      <div class="card-body p-0">
        <div
          v-if="purchasePlaceStore.loading"
          class="d-flex justify-content-center py-5"
        >
          <div class="spinner-border text-primary" role="status">
            <span class="visually-hidden">
              Chargement…
            </span>
          </div>
        </div>

        <div
          v-else-if="purchasePlaceStore.purchasePlaces.length === 0"
          class="text-center py-5 px-3"
        >
          <i
            class="bi bi-shop display-4 text-secondary"
            aria-hidden="true"
          />

          <h3 class="h5 mt-3">
            Aucun lieu d’achat
          </h3>

          <p class="text-secondary">
            Ajoutez les boutiques ou vendeurs auprès desquels vous achetez vos objets.
          </p>

          <button
            type="button"
            class="btn btn-primary"
            @click="openCreateModal"
          >
            Créer un lieu d’achat
          </button>
        </div>

        <div v-else class="table-responsive">
          <table class="table table-hover align-middle mb-0">
            <thead>
              <tr>
                <th>Nom</th>
                <th>Type</th>
                <th>Site</th>
                <th class="text-center">Objets</th>
                <th>Créé le</th>
                <th class="text-end">Actions</th>
              </tr>
            </thead>

            <tbody>
              <tr
                v-for="place in purchasePlaceStore.purchasePlaces"
                :key="place.id"
              >
                <td>
                  <div class="d-flex align-items-center gap-2">
                    <i
                      class="bi bi-shop text-secondary"
                      aria-hidden="true"
                    />

                    <span class="fw-semibold">
                      {{ place.name }}
                    </span>
                  </div>
                </td>

                <td>
                    <span
                        v-if="place.type"
                        class="badge text-bg-secondary"
                    >
                        {{ purchasePlaceTypeLabels[place.type] }}
                    </span>

                    <span
                        v-else
                        class="text-secondary"
                    >
                        —
                    </span>
                </td>

                <td>
                  <a
                    v-if="place.websiteUrl"
                    :href="place.websiteUrl"
                    target="_blank"
                    rel="noopener noreferrer"
                  >
                    Visiter
                    <i
                      class="bi bi-box-arrow-up-right ms-1"
                      aria-hidden="true"
                    />
                  </a>

                  <span v-else class="text-secondary">
                    —
                  </span>
                </td>

                <td class="text-center">
                  <span class="badge text-bg-secondary">
                    {{ place.itemCount }}
                  </span>
                </td>

                <td>
                  {{ formatDate(place.createdAt) }}
                </td>

                <td class="text-end">
                  <div class="btn-group">
                    <RouterLink
                      :to="{
                        path: '/items',
                        query: {
                          purchasePlaceId: place.id,
                        },
                      }"
                      class="btn btn-sm btn-outline-primary"
                      title="Voir les objets"
                    >
                      <i class="bi bi-eye" aria-hidden="true" />
                    </RouterLink>

                    <button
                      type="button"
                      class="btn btn-sm btn-outline-secondary"
                      title="Modifier"
                      @click="openEditModal(place)"
                    >
                      <i class="bi bi-pencil" aria-hidden="true" />
                    </button>

                    <button
                        type="button"
                        class="btn btn-sm btn-outline-danger"
                        :title="
                            place.itemCount > 0
                            ? 'Ce lieu est utilisé par des objets'
                            : 'Supprimer'
                        "
                        @click="askDelete(place)"
                        >
                        <i class="bi bi-trash" aria-hidden="true" />
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <PurchasePlaceFormModal
      ref="formModal"
      :open="formOpen"
      :purchase-place="selectedPurchasePlace"
      :loading="purchasePlaceStore.saving"
      @close="closeFormModal"
      @submit="savePurchasePlace"
    />

    <ConfirmModal
      :open="deleteModalOpen"
      title="Supprimer le lieu d’achat"
      :message="deleteMessage"
      confirm-label="Supprimer"
      :loading="purchasePlaceStore.deleting"
      @close="closeDeleteModal"
      @confirm="deletePurchasePlace"
    />
  </section>
</template>