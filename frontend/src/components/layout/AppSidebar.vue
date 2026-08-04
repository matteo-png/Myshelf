<script setup lang="ts">
interface NavigationItem {
  label: string
  route: string
  icon: string
}

const navigationItems: NavigationItem[] = [
  {
    label: 'Dashboard',
    route: '/dashboard',
    icon: 'bi-speedometer2',
  },
  {
    label: 'Collections',
    route: '/collections',
    icon: 'bi-collection',
  },
  {
    label: 'Objets',
    route: '/items',
    icon: 'bi-box-seam',
  },
  {
    label: 'Catégories',
    route: '/categories',
    icon: 'bi-folder',
  },
  {
    label: 'Tags',
    route: '/tags',
    icon: 'bi-tags',
  },
  {
    label: "Lieux d'achat",
    route: '/purchase-places',
    icon: 'bi-shop',
  },
]
</script>

<template>
  <aside
    class="app-sidebar bg-body-secondary shadow"
    data-bs-theme="dark"
  >
    <div class="sidebar-brand">
      <RouterLink
        to="/dashboard"
        class="brand-link"
      >
        <span class="brand-text fw-light">
          <i class="bi bi-bookshelf me-2" aria-hidden="true" />
          MyShelf
        </span>
      </RouterLink>
    </div>

    <div class="sidebar-wrapper">
      <nav class="mt-2">
        <ul
          class="nav sidebar-menu flex-column"
          data-lte-toggle="treeview"
          role="menu"
          data-accordion="false"
        >
          <li
            v-for="item in navigationItems"
            :key="item.route"
            class="nav-item"
          >
            <RouterLink
              :to="item.route"
              class="nav-link"
            >
              <i
                class="nav-icon bi"
                :class="item.icon"
                aria-hidden="true"
              />

              <p>{{ item.label }}</p>
            </RouterLink>
          </li>
        </ul>
      </nav>
    </div>
  </aside>
</template>