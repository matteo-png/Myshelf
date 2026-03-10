# MyShelf

MyShelf est un projet web de gestion d'une collection personnelle d'objets.

Le repository est actuellement organise en deux parties :

- `backend/` : API REST Spring Boot
- `frontend/` : futur frontend Vue.js

## Objectif produit

L'application permet a un utilisateur de :

- creer un compte et se connecter
- gerer des collections personnelles
- ajouter des objets a une collection
- classer ces objets avec des categories, tags et lieux d'achat
- consulter des statistiques de collection
- associer un fichier a un objet, par exemple une facture d'achat

## Stack technique actuelle

- Java 21
- Spring Boot 3
- Spring Web
- Spring Data JPA / Hibernate
- Spring Security
- JWT
- PostgreSQL
- Docker Compose
- Swagger / OpenAPI
- JUnit + Spring Test + Testcontainers

## Demarrage rapide

### Avec Docker

Depuis la racine du projet :

```bash
docker compose up --build
```

Services exposes :

- API : `http://localhost:8080`
- Swagger UI : `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON : `http://localhost:8080/v3/api-docs`
- PostgreSQL : `localhost:5432`

### Backend seul

Le backend se trouve dans `backend/`.

Prerequis :

- Java 21
- Maven ou Maven Wrapper fonctionnel
- PostgreSQL

## Documentation

- Vue technique backend : [docs/backend/technical-overview.md]
- Reference API backend : [docs/backend/api-reference.md]

## Etat du projet

- Backend : en cours de construction
- Frontend : non demarre dans ce repository

