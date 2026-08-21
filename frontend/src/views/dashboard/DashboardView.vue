<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'

import DistributionChart from '@/components/dashboard/DistributionChart.vue'
import MonthlyAcquisitionsChart from '@/components/dashboard/MonthlyAcquisitionsChart.vue'
import StatCard from '@/components/dashboard/StatCard.vue'
import FormMessage from '@/components/forms/FormMessage.vue'
import CollectionValueChart from '@/components/dashboard/CollectionValueChart.vue'
import StatusChart from '@/components/dashboard/StatusChart.vue'
import YearlyAcquisitionsChart from '@/components/dashboard/YearlyAcquisitionsChart.vue'
import { useAuthStore } from '@/stores/auth.store'
import { useStatsStore } from '@/stores/stats.store'
import type { StatsGroup, StatsTimeSeriesPoint } from '@/types/stats'
import RecentItems from '@/components/dashboard/RecentItems.vue'
import ChartCard from '@/components/dashboard/ChartCard.vue'

const authStore = useAuthStore()
const statsStore = useStatsStore()

const currentYear = new Date().getFullYear()
const selectedYear = ref(currentYear)

const availableYears = computed(() => {
  const years = statsStore.itemsByYear
    .map((point) => Number(point.period))
    .filter((year) => Number.isInteger(year))

  if (!years.includes(currentYear)) {
    years.push(currentYear)
  }

  return years.sort((first, second) => second - first)
})

const formattedTotalValue = computed(() => {
  return new Intl.NumberFormat('fr-FR', {
    style: 'currency',
    currency: 'EUR',
  }).format(statsStore.overview.totalEstimatedValue ?? 0)
})

async function changeYear() {
  await statsStore.fetchItemsByMonth(selectedYear.value)
}

onMounted(async () => {
  await statsStore.fetchDashboard(selectedYear.value)
})

function hasGroupData(items: StatsGroup[]) {
  return items.some((item) => item.count > 0)
}

function hasTimeSeriesData(items: StatsTimeSeriesPoint[]) {
  return items.some((item) => item.count > 0)
}

const topCategories = computed(() =>
  [...statsStore.itemsByCategory].sort((a, b) => b.count - a.count).slice(0, 5),
)

const topPurchasePlaces = computed(() =>
  [...statsStore.itemsByPurchasePlace].sort((a, b) => b.count - a.count).slice(0, 5),
)

const topCollectionsByValue = computed(() =>
  [...statsStore.itemsByCollection].sort((a, b) => b.totalValue - a.totalValue).slice(0, 5),
)

const topCollections = computed(() =>
  [...statsStore.itemsByCollection].sort((a, b) => b.count - a.count).slice(0, 5),
)
</script>

<template>
  <section>
    <div class="alert alert-primary d-flex align-items-center">
      <i class="bi bi-person-check fs-4 me-3" aria-hidden="true" />

      <div>
        <strong>Bienvenue {{ authStore.displayName }}.</strong>

        <div>Voici un aperçu de votre collection personnelle.</div>
      </div>
    </div>

    <FormMessage :message="statsStore.error ?? ''" type="error" />

    <div v-if="statsStore.loading" class="d-flex justify-content-center py-5">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden"> Chargement du tableau de bord… </span>
      </div>
    </div>

    <template v-else>
      <div class="row">
        <div class="col-lg-4 col-md-6">
          <StatCard
            title="Collections"
            :value="statsStore.overview.collectionsCount"
            icon="bi-collection"
            variant="primary"
            route="/collections"
            link-label="Voir les collections"
          />
        </div>

        <div class="col-lg-4 col-md-6">
          <StatCard
            title="Objets"
            :value="statsStore.overview.itemsCount"
            icon="bi-box-seam"
            variant="success"
            route="/items"
            link-label="Voir les objets"
          />
        </div>

        <div class="col-lg-4 col-md-6">
          <StatCard
            title="Valeur totale estimée"
            :value="formattedTotalValue"
            icon="bi-currency-euro"
            variant="info"
          />
        </div>

        <div class="col-lg-4 col-md-6">
          <StatCard
            title="Catégories"
            :value="statsStore.overview.categoriesCount"
            icon="bi-folder"
            variant="warning"
            route="/categories"
            link-label="Voir les catégories"
          />
        </div>

        <div class="col-lg-4 col-md-6">
          <StatCard
            title="Tags"
            :value="statsStore.overview.tagsCount"
            icon="bi-tags"
            variant="danger"
            route="/tags"
            link-label="Voir les tags"
          />
        </div>

        <div class="col-lg-4 col-md-6">
          <StatCard
            title="Lieux d'achat"
            :value="statsStore.overview.purchasePlacesCount"
            icon="bi-shop"
            variant="secondary"
            route="/purchase-places"
            link-label="Voir les lieux d'achat"
          />
        </div>
      </div>

      <div class="card mb-4">
        <div class="card-header">
          <div
            class="d-flex flex-column flex-md-row align-items-md-center justify-content-between gap-3"
          >
            <!-- <div>
              <h2 class="card-title mb-0">Acquisitions par mois</h2>
            </div> -->

            <div>
              <label for="dashboard-year" class="visually-hidden"> Année </label>

              <select
                id="dashboard-year"
                v-model.number="selectedYear"
                class="form-select"
                @change="changeYear"
              >
                <option v-for="year in availableYears" :key="year" :value="year">
                  {{ year }}
                </option>
              </select>
            </div>
          </div>
        </div>
        <ChartCard
          title="Acquisitions par mois"
          :has-data="hasTimeSeriesData(statsStore.itemsByMonth)"
          empty-message="Aucune acquisition pour cette année."
        >
          <MonthlyAcquisitionsChart :points="statsStore.itemsByMonth" />
        </ChartCard>
      </div>

      <div class="row">
        <div class="col-xl-6">
          <div class="card mb-4">
            <div class="card-header">
              <h2 class="card-title mb-0">Objets par collection</h2>
            </div>

            <div class="card-body">
              <DistributionChart label="Objets" :items="topCollections" />
            </div>
          </div>
        </div>

        <div class="col-xl-6">
          <div class="card mb-4">
            <ChartCard
              title="Top 5 catégories"
              :has-data="hasGroupData(topCategories)"
              empty-message="Aucune catégorie à afficher."
            >
              <DistributionChart label="Objets" :items="topCategories" />
            </ChartCard>
          </div>
        </div>
      </div>

      <div class="row">
        <div class="col-xl-6">
          <div class="card mb-4">
            <ChartCard
              title="Répartition par statut"
              :has-data="hasGroupData(statsStore.itemsByStatus)"
              empty-message="Aucun statut à analyser pour le moment."
            >
              <StatusChart :items="statsStore.itemsByStatus" />
            </ChartCard>
          </div>
        </div>

        <div class="col-xl-6">
          <div class="card mb-4">
            <ChartCard
              title="Collections les plus valorisées"
              :has-data="hasGroupData(topCollectionsByValue)"
              empty-message="Aucune valeur estimée disponible."
            >
              <CollectionValueChart :items="topCollectionsByValue" />
            </ChartCard>
          </div>
        </div>
      </div>

      <div class="row">
        <div class="col-xl-6">
          <div class="card mb-4">
            <ChartCard
              title="Top 5 lieux d’achat"
              :has-data="hasGroupData(topPurchasePlaces)"
              empty-message="Aucun lieu d’achat à afficher."
            >
              <DistributionChart label="Objets" :items="topPurchasePlaces" />
            </ChartCard>
          </div>
        </div>

        <div class="col-xl-6">
          <div class="card mb-4">
            <div class="card-header">
              <h2 class="card-title mb-0">Évolution annuelle</h2>
            </div>

            <div class="card-body">
              <YearlyAcquisitionsChart :points="statsStore.itemsByYear" />
            </div>
          </div>
        </div>

        <div class="card mb-4">
          <div class="card-header d-flex align-items-center justify-content-between">
            <h2 class="card-title mb-0">Derniers objets ajoutés</h2>

            <RouterLink to="/items" class="btn btn-sm btn-outline-primary ms-auto">
              Voir tous les objets
              <i class="bi bi-arrow-right ms-1" aria-hidden="true" />
            </RouterLink>
          </div>

          <div class="card-body">
            <RecentItems :items="statsStore.recentItems" />
          </div>
        </div>
      </div>
    </template>
  </section>
</template>
