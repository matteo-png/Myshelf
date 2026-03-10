# Backend API Reference

## Base URL

Par defaut en local :

```text
http://localhost:8080
```

## Authentication

### Public endpoints

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /swagger-ui.html`
- `GET /swagger-ui/**`
- `GET /v3/api-docs/**`

### Protected endpoints

Toutes les autres routes necessitent :

```http
Authorization: Bearer <token>
```

## Auth

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/me`

## Collections

- `GET /api/collections`
- `GET /api/collections/{id}`
- `POST /api/collections`
- `PUT /api/collections/{id}`
- `DELETE /api/collections/{id}`

## Items

### Standard JSON endpoints

- `GET /api/items`
- `GET /api/items/{id}`
- `PUT /api/items/{id}`
- `DELETE /api/items/{id}`

### File-related endpoints

- `GET /api/items/{id}/file`
- `POST /api/items` en `multipart/form-data`
- `PUT /api/items/{id}` en `multipart/form-data`

### Query parameters

`GET /api/items`

- `collectionId` optionnel pour filtrer les items d'une collection

### Multipart contract

Pour creer ou mettre a jour un item avec fichier :

- part `item` : JSON correspondant a `ItemRequest`
- part `file` : fichier optionnel

### Remove attached file without deleting item

Utiliser `PUT /api/items/{id}` avec :

```json
{
  "removeFile": true
}
```

Important :

Le endpoint `PUT` agit actuellement comme une mise a jour complete de l'objet. Il faut donc renvoyer aussi les autres champs necessaires a conserver.

## Categories

- `GET /api/categories`
- `GET /api/categories/{id}`
- `POST /api/categories`
- `PUT /api/categories/{id}`
- `DELETE /api/categories/{id}`

## Tags

- `GET /api/tags`
- `GET /api/tags/{id}`
- `POST /api/tags`
- `PUT /api/tags/{id}`
- `DELETE /api/tags/{id}`

## Purchase Places

- `GET /api/purchase-places`
- `GET /api/purchase-places/{id}`
- `POST /api/purchase-places`
- `PUT /api/purchase-places/{id}`
- `DELETE /api/purchase-places/{id}`

## Stats

- `GET /api/stats/overview`
- `GET /api/stats/items-by-collection`
- `GET /api/stats/items-by-category`
- `GET /api/stats/items-by-purchase-place`
- `GET /api/stats/items-by-status`
- `GET /api/stats/items-by-year`
- `GET /api/stats/items-by-month?year=YYYY`

## Configuration Notes

Configuration notable actuelle :

- Swagger UI : `/swagger-ui.html`
- OpenAPI JSON : `/v3/api-docs`
- stockage des fichiers item configurable avec `app.item-files.root`
- taille max multipart : `10MB`

