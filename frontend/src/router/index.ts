import { createRouter, createWebHistory } from 'vue-router'

import { useAuthStore } from '@/stores/auth.store'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),

  routes: [
    {
      path: '/',
      redirect: '/dashboard',
    },

    {
      path: '/',
      component: () => import('@/layouts/AuthLayout.vue'),
      meta: {
        public: true,
      },
      children: [
        {
          path: 'login',
          name: 'login',
          component: () => import('@/views/auth/LoginView.vue'),
        },
        {
          path: 'register',
          name: 'register',
          component: () => import('@/views/auth/RegisterView.vue'),
        },
      ],
    },

    {
      path: '/',
      component: () => import('@/layouts/DefaultLayout.vue'),
      meta: {
        requiresAuth: true,
      },
      children: [
        {
          path: 'dashboard',
          name: 'dashboard',
          component: () => import('@/views/dashboard/DashboardView.vue'),
          meta:{
            title: 'Dashboard',
          }
        },

        {
          path: 'collections',
          name: 'collections',
          component: () => import('@/views/collections/CollectionsView.vue'),
          meta:{
            title: 'collections',
          }
        },

        {
          path: 'items',
          name: 'items',
          component: () => import('@/views/items/ItemsView.vue'),
          meta:{
            title: 'items',
          }
        },

        {
          path: 'categories',
          name: 'categories',
          component: () => import('@/views/categories/CategoriesView.vue'),
          meta:{
            title: 'categories',
          }
        },

        {
          path: 'tags',
          name: 'tags',
          component: () => import('@/views/tags/TagsView.vue'),
          meta:{
            title: 'tags',
          }
        },

        {
          path: 'purchase-places',
          name: 'purchase-places',
          component: () => import('@/views/purchase-places/PurchasePlacesView.vue'),
          meta:{
            title: 'purchase-places',
          }
        },
      ],
    },

    {
      path: '/:pathMatch(.*)*',
      redirect: '/dashboard',
    },
  ],
})

router.beforeEach((to) => {
  const authStore = useAuthStore()

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    return {
      name: 'login',
      query: {
        redirect: to.fullPath,
      },
    }
  }

  if (to.meta.public && authStore.isAuthenticated) {
    return {
      name: 'dashboard',
    }
  }
})

export default router