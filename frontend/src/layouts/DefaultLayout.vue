<script setup lang="ts">
import { ref } from 'vue'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppSidebar from '@/components/layout/AppSidebar.vue'
const sidebarOpen = ref(true)
</script>

<template>
  <div class="app-wrapper">
    <AppHeader />
    <AppSidebar 
    :open="sidebarOpen"
      @close="sidebarOpen = false"
    />

    <main class="app-main">
      <div class="app-content-header">
        <div class="container-fluid">
          <div class="row">
            <div class="col-sm-6">
              <h1 class="mb-0">
                {{ $route.meta.title ?? 'MyShelf' }}
              </h1>
            </div>

            <div class="col-sm-6">
              <ol class="breadcrumb float-sm-end mb-0">
                <li class="breadcrumb-item">
                  <RouterLink to="/dashboard">
                    Accueil
                  </RouterLink>
                </li>

                <li
                  v-if="$route.meta.title"
                  class="breadcrumb-item active"
                  aria-current="page"
                >
                  {{ $route.meta.title }}
                </li>
              </ol>
            </div>
          </div>
        </div>
      </div>

      <div class="app-content">
        <div class="container-fluid">
          <RouterView />
        </div>
      </div>
    </main>

    <footer class="app-footer">
      <div class="float-end d-none d-sm-inline">
        Gestion de collections personnelles
      </div>

      <strong>MyShelf</strong>
    </footer>
  </div>
</template>