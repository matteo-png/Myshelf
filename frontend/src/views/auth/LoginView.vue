<script setup lang="ts">
import axios from 'axios'
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'
import type { ValidationErrorResponse } from '@/types/api'
import PasswordInput from '@/components/forms/PasswordInput.vue'
import FormMessage from '@/components/forms/FormMessage.vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const form = reactive({
  email: '',
  password: '',
})

const error = ref('')

const registrationSuccess = computed(() => {
  return route.query.registered === 'true'
})

const isFormValid = computed(() => {
  return form.email.trim() !== '' && form.password !== ''
})

async function submit() {
  error.value = ''

  if (!isFormValid.value) {
    error.value = 'Veuillez renseigner votre email et votre mot de passe.'
    return
  }

  try {
    await authStore.login({
      email: form.email.trim(),
      password: form.password,
    })

    await router.push('/dashboard')
  } catch (exception) {
    if (axios.isAxiosError<ValidationErrorResponse>(exception)) {
      error.value =
        exception.response?.data?.message ??
        'Email ou mot de passe incorrect.'
      return
    }

    error.value = 'Une erreur inattendue est survenue.'
  }
}
</script>

<template>
  <section>
    <div class="text-center mb-4">
      <h1 class="h3 fw-bold mb-2">
        Connexion
      </h1>

      <p class="text-secondary mb-0">
        Connectez-vous pour accéder à vos collections.
      </p>
    </div>

    <FormMessage
      v-if="registrationSuccess"
      message="Votre compte a bien été créé. Vous pouvez maintenant vous connecter."
      type="success"
    />

    <form @submit.prevent="submit">
      <div class="mb-3">
        <label for="email" class="form-label">
          Adresse email
        </label>

        <div class="input-group">
          <span class="input-group-text">
            <i class="bi bi-envelope" aria-hidden="true" />
          </span>

          <input
            id="email"
            v-model.trim="form.email"
            type="email"
            class="form-control"
            placeholder="nom@exemple.com"
            autocomplete="email"
            required
          />
        </div>
      </div>

      <PasswordInput
        id="password"
        v-model="form.password"
        label="Mot de passe"
        autocomplete="current-password"
        required
      />

      <FormMessage
        :message="error"
        type="error"
      />

      <button
        type="submit"
        class="btn btn-primary w-100 py-2"
        :disabled="authStore.loading || !isFormValid"
      >
        <span
          v-if="authStore.loading"
          class="spinner-border spinner-border-sm me-2"
          aria-hidden="true"
        />

        {{ authStore.loading ? 'Connexion en cours...' : 'Se connecter' }}
      </button>
    </form>

    <div class="text-center mt-4">
      <span class="text-secondary">
        Vous n’avez pas encore de compte ?
      </span>

      <RouterLink to="/register" class="ms-1 fw-semibold">
        Créer un compte
      </RouterLink>
    </div>
  </section>
</template>