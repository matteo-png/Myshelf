<script setup lang="ts">
import type { Item } from '@/types/items'

defineProps<{
  items: Item[]
}>()

function formatDate(date: string) {
  return new Intl.DateTimeFormat('fr-FR', {
    dateStyle: 'medium',
  }).format(new Date(date))
}

function formatCurrency(value: number | null) {
  if (value === null) {
    return '—'
  }

  return new Intl.NumberFormat('fr-FR', {
    style: 'currency',
    currency: 'EUR',
  }).format(value)
}
</script>

<template>
  <div v-if="items.length === 0" class="text-center py-5 text-secondary">
    <i class="bi bi-box-seam display-5" aria-hidden="true" />

    <p class="mt-3 mb-0">Aucun objet ajouté pour le moment.</p>
  </div>

  <div v-else class="list-group list-group-flush">
    <RouterLink
      v-for="item in items"
      :key="item.id"
      :to="{
        path: '/items',
        query: {
          collectionId: item.collectionId,
        },
      }"
      class="list-group-item list-group-item-action px-0"
    >
      <div class="d-flex justify-content-between gap-3">
        <div class="overflow-hidden">
          <div class="fw-semibold text-truncate">
            {{ item.name }}
          </div>

          <small class="text-secondary">
            {{ item.collectionName }}
            <template v-if="item.categoryName"> · {{ item.categoryName }} </template>
          </small>
        </div>

        <div class="text-end flex-shrink-0">
          <div class="fw-semibold">
            {{ formatCurrency(item.estimatedValue) }}
          </div>

          <small class="text-secondary">
            {{ formatDate(item.createdAt) }}
          </small>
        </div>
      </div>
    </RouterLink>
  </div>
</template>
