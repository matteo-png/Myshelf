# MyShelf

MyShelf est une application web permettant de gérer une collection personnelle d'objets.

Le projet est composé de deux applications :

* **Backend** : API REST développée avec Spring Boot
* **Frontend** : Application développée avec Vue.js

---

# Fonctionnalités

## Authentification

* Création de compte
* Connexion avec JWT
* Endpoints sécurisés avec Spring Security

## Gestion des collections

* Création, modification et suppression de collections
* Consultation de ses collections

## Gestion des objets

* Ajout, modification et suppression d'objets
* Estimation de valeur
* Date d'achat
* URL d'achat
* Statut de l'objet

## Organisation

* Catégories
* Tags
* Lieux d'achat

## Statistiques

* Nombre de collections
* Nombre d'objets
* Nombre de catégories
* Nombre de tags
* Nombre de lieux d'achat
* Valeur totale estimée
* Évolution des acquisitions par mois
* Évolution des acquisitions par année

---

# Stack technique

## Backend

* Java 21
* Spring Boot 3.5
* Spring Security
* Spring Data JPA / Hibernate
* PostgreSQL
* JWT
* Swagger / OpenAPI
* JUnit 5
* Mockito
* Testcontainers

## Frontend

* Vue 3
* TypeScript
* Vite
* Vue Router
* Pinia
* Axios

## Infrastructure

* Docker
* Docker Compose

---

# Structure du projet

```text
myshelf/
├── backend/
├── frontend/
├── docker-compose.yml
└── README.md
```

---

# Lancer le projet

## Prérequis

* Docker Desktop

Depuis la racine du projet :

```bash
docker compose up --build
```

Les services suivants sont démarrés :

| Service     | URL                                   |
| ----------- | ------------------------------------- |
| Frontend    | http://localhost:5173                 |
| Backend API | http://localhost:8080                 |
| Swagger     | http://localhost:8080/swagger-ui.html |
| PostgreSQL  | localhost:5432                        |

---

# Tests

Le backend dispose de plusieurs niveaux de tests :

* Tests unitaires (Mockito)
* Tests des contrôleurs (MockMvc)
* Tests des repositories avec PostgreSQL (Testcontainers)

---

# État du projet

## Backend

* Authentification JWT
* CRUD Collections
* CRUD Items
* CRUD Categories
* CRUD Tags
* CRUD Purchase Places
* Statistiques
* Documentation Swagger
* Tests

## Frontend

En cours de développement.

Les prochaines étapes sont :

* Authentification
* Dashboard
* Gestion des collections
* Gestion des objets
* Statistiques
* Responsive Design

---

## Documentation

* Vue technique backend : [docs/backend/technical-overview.md]
* Reference API backend : [docs/backend/api-reference.md]
