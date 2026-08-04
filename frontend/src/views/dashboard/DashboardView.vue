<script setup lang="ts">
import { useAuthStore } from '@/stores/auth.store'

const authStore = useAuthStore()
</script>

<template>
  <section>
    <div class="alert alert-primary">
      Bienvenue {{ authStore.displayName }} dans MyShelf.
    </div>

    <div class="row">
      <div class="col-lg-3 col-6">
        <div class="small-box text-bg-primary">
          <div class="inner">
            <h3>0</h3>
            <p>Objets</p>
          </div>

          <div class="small-box-icon">
            <i class="bi bi-box-seam" />
          </div>

          <RouterLink
            to="/items"
            class="small-box-footer link-light"
          >
            Voir les objets
            <i class="bi bi-arrow-right-circle ms-1" />
          </RouterLink>
        </div>
      </div>

      <div class="col-lg-3 col-6">
        <div class="small-box text-bg-success">
          <div class="inner">
            <h3>0</h3>
            <p>Collections</p>
          </div>

          <div class="small-box-icon">
            <i class="bi bi-collection" />
          </div>

          <RouterLink
            to="/collections"
            class="small-box-footer link-light"
          >
            Voir les collections
            <i class="bi bi-arrow-right-circle ms-1" />
          </RouterLink>
        </div>
      </div>
    </div>
  </section>
</template>