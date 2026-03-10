# Backend Technical Overview

## Purpose

Le backend MyShelf est une API REST securisee qui permet a chaque utilisateur de gerer ses propres donnees de collection.

Le perimetre fonctionnel actuel couvre :

- authentification utilisateur
- gestion des collections
- gestion des items
- gestion des categories
- gestion des tags
- gestion des lieux d'achat
- statistiques agregees
- stockage de fichier attache a un item

## Technology Stack

- Java 21
- Spring Boot 3.5
- Spring MVC
- Spring Data JPA / Hibernate
- Spring Security
- JWT avec `jjwt`
- PostgreSQL
- Swagger / OpenAPI avec `springdoc`
- Lombok
- Tests Spring Boot + MockMvc + Testcontainers

## Repository Structure

```text
backend/
  src/main/java/com/myshelf/apiMyshelf/
    controller/
    dto/
    model/
    repository/
    security/
    service/
    config/
  src/main/resources/
  src/test/java/com/myshelf/apiMyshelf/
```

## Application Architecture

L'architecture suit une separation classique en couches :

- `controller/` : expose les endpoints REST
- `service/` : contient les regles metier
- `repository/` : acces aux donnees avec Spring Data JPA
- `model/` : entites JPA persistantes
- `dto/` : objets d'entree/sortie de l'API
- `security/` : JWT, filtre de securite, user details, configuration Spring Security
- `config/` : configuration technique transversale, par exemple OpenAPI

Flux general d'une requete securisee :

1. le client envoie un JWT dans le header `Authorization`
2. `JwtAuthenticationFilter` valide le token
3. Spring Security alimente le `SecurityContext`
4. `CurrentUserService` resout l'utilisateur courant depuis son email
5. les services metier filtrent les donnees par proprietaire

## Domain Model

### BaseEntity

Superclasse commune avec :

- `id`
- `createdAt`
- `updatedAt`

### User

Represente un utilisateur authentifiable.

Champs principaux :

- `email`
- `passwordHash`
- `displayName`

Relations :

- `collections`
- `categories`
- `tags`
- `purchasePlaces`

### Collection

Collection personnelle appartenant a un utilisateur.

Champs principaux :

- `name`
- `description`
- `owner`

### Item

Objet appartenant a une collection.

Champs principaux :

- `name`
- `description`
- `collection`
- `category`
- `purchasePlace`
- `estimatedValue`
- `purchaseDate`
- `purchaseUrl`
- `status`
- `tags`

Gestion de fichier associe :

- `fileName`
- `fileContentType`
- `fichierUrl`

Note :

`fichierUrl` stocke l'identifiant interne du fichier. L'URL publique de telechargement est exposee via le DTO de reponse.

### Category

Categorie d'item liee a un utilisateur.

### Tag

Tag d'item lie a un utilisateur.

### PurchasePlace

Lieu d'achat associe a un utilisateur.

Champs principaux :

- `name`
- `type`
- `websiteUrl`

### Enumerations

- `ItemStatus`
- `PurchasePlaceType`

## Security

La securite est stateless.

Comportement actuel :

- `/api/auth/**` est public
- `/swagger-ui.html`, `/swagger-ui/**` et `/v3/api-docs/**` sont publics
- toutes les autres routes exigent un JWT valide

Elements principaux :

- `SecurityConfig` : configuration Spring Security
- `JwtAuthenticationFilter` : extraction et validation du token
- `JwtService` : generation et verification des JWT
- `CustomUserDetailsService` : chargement du user Spring Security
- `CurrentUserService` : resolution du user applicatif courant

Limitation actuelle importante :

La cle JWT est actuellement codee en dur dans `JwtService`. Pour un environnement reel, elle devrait etre externalisee via une variable d'environnement ou une configuration securisee.

## Persistence

Le backend utilise PostgreSQL avec Hibernate/JPA.

Configuration actuelle :

- profil local minimal dans `application.properties`
- profil Docker dans `application-docker.properties`
- `ddl-auto=update` dans Docker

Point d'attention :

Pour un projet amene a evoluer, il faudra envisager l'introduction d'un outil de migration type Flyway ou Liquibase.

## File Storage For Item Attachments

Les fichiers associes aux items sont stockes sur disque via `ItemFileStorageService`.

Configuration :

- local : `uploads/item-files`
- Docker : `/app/uploads/item-files`

Contraintes actuelles :

- taille max fichier : `10MB`
- taille max requete multipart : `10MB`

Cycle de vie :

- a la creation d'un item, un fichier peut etre envoye en multipart
- a la mise a jour, le fichier peut etre remplace
- un fichier peut etre retire sans supprimer l'item
- a la suppression de l'item, le fichier est supprime du stockage

## API Documentation

Swagger / OpenAPI est disponible a l'execution :

- UI : `http://localhost:8080/swagger-ui.html`
- spec JSON : `http://localhost:8080/v3/api-docs`

## Testing Strategy

Le projet contient plusieurs niveaux de tests :

- tests controleurs avec `MockMvc`
- tests services
- tests repositories
- tests d'integration avec PostgreSQL via Testcontainers

Le support de fichier sur `Item` dispose aussi de tests dedies au niveau service et controller.

## Current Backend Scope

Le backend couvre deja le coeur du produit, mais plusieurs sujets restent typiquement a prevoir :

- vraie strategie CORS pour le futur frontend Vue.js
- gestion centralisee des exceptions
- migrations de base de donnees
- externalisation des secrets
- validation metier plus fine
- pagination, tri et filtres
- gestion de roles si le produit evolue

