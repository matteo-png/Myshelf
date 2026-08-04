<script setup lang="ts">
import { useRouter } from 'vue-router'

import { useAuthStore } from '@/stores/auth.store'

const router = useRouter()
const authStore = useAuthStore()

async function logout() {
  authStore.logout()
  await router.push('/login')
}
</script>

<template>
  <nav class="app-header navbar navbar-expand bg-body">
    <div class="container-fluid">
      <ul class="navbar-nav">
        <li class="nav-item">
          <button
            type="button"
            class="nav-link btn btn-link"
            data-lte-toggle="sidebar"
            aria-label="Afficher ou masquer le menu"
          >
            <i class="bi bi-list" aria-hidden="true" />
          </button>
        </li>

        <li class="nav-item d-none d-md-block">
          <RouterLink
            to="/dashboard"
            class="nav-link"
          >
            Dashboard
          </RouterLink>
        </li>
      </ul>

      <ul class="navbar-nav ms-auto">
        <li class="nav-item dropdown">
          <button
            type="button"
            class="nav-link dropdown-toggle btn btn-link"
            data-bs-toggle="dropdown"
            aria-expanded="false"
          >
            <i class="bi bi-person-circle me-1" aria-hidden="true" />

            <span>
              {{ authStore.displayName ?? 'Mon compte' }}
            </span>
          </button>

          <ul class="dropdown-menu dropdown-menu-end">
            <li>
              <span class="dropdown-item-text">
                Connecté en tant que
                <strong>{{ authStore.displayName }}</strong>
              </span>
            </li>

            <li>
              <hr class="dropdown-divider" />
            </li>

            <li>
              <button
                type="button"
                class="dropdown-item text-danger"
                @click="logout"
              >
                <i class="bi bi-box-arrow-right me-2" aria-hidden="true" />
                Se déconnecter
              </button>
            </li>
          </ul>
        </li>
      </ul>
    </div>
  </nav>
</template>