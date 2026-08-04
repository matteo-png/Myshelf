<script setup lang="ts">
import axios from 'axios'
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'

import FormMessage from '@/components/forms/FormMessage.vue'
import PasswordInput from '@/components/forms/PasswordInput.vue'
import { useAuthStore } from '@/stores/auth.store'
import type { ValidationErrorResponse } from '@/types/api'

const router = useRouter()
const authStore = useAuthStore()

const form = reactive({
  displayName: '',
  email: '',
  password: '',
  confirmPassword: '',
})

const fieldErrors = reactive<Record<string, string>>({})
const error = ref('')
const success = ref('')

const passwordErrors = computed(() => {
  const errors: string[] = []

  if (form.password.length < 8) {
    errors.push('Le mot de passe doit contenir au moins 8 caractères.')
  }

  if (!/[A-Z]/.test(form.password)) {
    errors.push('Le mot de passe doit contenir une majuscule.')
  }

  if (!/[a-z]/.test(form.password)) {
    errors.push('Le mot de passe doit contenir une minuscule.')
  }

  if (!/[0-9]/.test(form.password)) {
    errors.push('Le mot de passe doit contenir un chiffre.')
  }

  return errors
})

const passwordsMatch = computed(() => {
  return form.password === form.confirmPassword
})

const isFormValid = computed(() => {
  return (
    form.displayName.trim() !== '' &&
    form.email.trim() !== '' &&
    form.password !== '' &&
    form.confirmPassword !== '' &&
    passwordErrors.value.length === 0 &&
    passwordsMatch.value
  )
})

function clearErrors() {
  error.value = ''

  for (const key of Object.keys(fieldErrors)) {
    delete fieldErrors[key]
  }
}

async function submit() {
  clearErrors()
  success.value = ''

  if (!isFormValid.value) {
    error.value = 'Veuillez corriger les erreurs du formulaire.'
    return
  }

  try {
    await authStore.register({
      displayName: form.displayName.trim(),
      email: form.email.trim(),
      password: form.password,
    })

    //success.value = 'Votre compte a bien été créé.'

    //window.setTimeout(() => {
      router.push({
        name: 'login',
        query: { registered: 'true' },
      })
    //}, 1500)
  } catch (exception) {
    if (axios.isAxiosError<ValidationErrorResponse>(exception)) {
      const response = exception.response?.data

      if (response?.errors) {
        Object.assign(fieldErrors, response.errors)
        error.value = response.message
        return
      }

      error.value = response?.message ?? 'Impossible de créer le compte.'
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
        Créer un compte
      </h1>

      <p class="text-secondary mb-0">
        Commencez à organiser vos collections avec MyShelf.
      </p>
    </div>

    <form @submit.prevent="submit">
      <div class="mb-3">
        <label for="displayName" class="form-label">
          Nom affiché
        </label>

        <div class="input-group">
          <span class="input-group-text">
            <i class="bi bi-person" aria-hidden="true" />
          </span>

          <input
            id="displayName"
            v-model.trim="form.displayName"
            type="text"
            class="form-control"
            :class="{ 'is-invalid': fieldErrors.displayName }"
            placeholder=""
            autocomplete="name"
            required
          />

          <div
            v-if="fieldErrors.displayName"
            class="invalid-feedback"
          >
            {{ fieldErrors.displayName }}
          </div>
        </div>
      </div>

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
            :class="{ 'is-invalid': fieldErrors.email }"
            placeholder="nom@exemple.com"
            autocomplete="email"
            required
          />

          <div
            v-if="fieldErrors.email"
            class="invalid-feedback"
          >
            {{ fieldErrors.email }}
          </div>
        </div>
      </div>

      <PasswordInput
        id="password"
        v-model="form.password"
        label="Mot de passe"
        autocomplete="new-password"
        required
        :error="fieldErrors.password"
      />

      <div
        v-if="form.password"
        class="card bg-light border-0 mb-3"
      >
        <div class="card-body py-3">
          <p class="small fw-semibold mb-2">
            Le mot de passe doit contenir :
          </p>

          <ul class="small mb-0 ps-3">
            <li :class="form.password.length >= 8 ? 'text-success' : 'text-secondary'">
              Au moins 8 caractères
            </li>

            <li :class="/[A-Z]/.test(form.password) ? 'text-success' : 'text-secondary'">
              Une majuscule
            </li>

            <li :class="/[a-z]/.test(form.password) ? 'text-success' : 'text-secondary'">
              Une minuscule
            </li>

            <li :class="/[0-9]/.test(form.password) ? 'text-success' : 'text-secondary'">
              Un chiffre
            </li>
          </ul>
        </div>
      </div>

      <PasswordInput
        id="confirmPassword"
        v-model="form.confirmPassword"
        label="Confirmer le mot de passe"
        autocomplete="new-password"
        required
        :error="
          form.confirmPassword && !passwordsMatch
            ? 'Les mots de passe ne correspondent pas.'
            : ''
        "
      />

      <FormMessage
        :message="error"
        type="error"
      />

      <FormMessage
        :message="success"
        type="success"
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

        {{ authStore.loading ? 'Création en cours...' : 'Créer mon compte' }}
      </button>
    </form>

    <div class="text-center mt-4">
      <span class="text-secondary">
        Vous avez déjà un compte ?
      </span>

      <RouterLink to="/login" class="ms-1 fw-semibold">
        Se connecter
      </RouterLink>
    </div>
  </section>
</template>
