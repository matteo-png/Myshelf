<template>
  <div class="auth-page">
    <div class="auth-container">
      <div class="auth-brand">
        <RouterLink to="/login" class="auth-logo">
          <i class="bi bi-bookshelf me-2" aria-hidden="true" />
          MyShelf
        </RouterLink>

        <p class="auth-subtitle">
          Gérez simplement vos collections personnelles.
        </p>
      </div>

      <div class="card auth-card shadow">
        <div class="card-body p-4 p-md-5">
          <RouterView />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.auth-page {
  display: flex;
  min-height: 100vh;
  align-items: center;
  justify-content: center;
  padding: 2rem 1rem;
  background:
    radial-gradient(circle at top left, rgb(13 110 253 / 18%), transparent 35%),
    linear-gradient(135deg, #1f252b 0%, #111827 100%);
}

.auth-container {
  width: 100%;
  max-width: 460px;
}

.auth-brand {
  margin-bottom: 1.5rem;
  text-align: center;
}

.auth-logo {
  color: white;
  font-size: 2.25rem;
  font-weight: 700;
  text-decoration: none;
}

.auth-logo:hover {
  color: #dbeafe;
}

.auth-subtitle {
  margin-top: 0.5rem;
  margin-bottom: 0;
  color: rgb(255 255 255 / 70%);
}

.auth-card {
  overflow: hidden;
  border: 0;
  border-radius: 1rem;
  background: white;
}
</style>