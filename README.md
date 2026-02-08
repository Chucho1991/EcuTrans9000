# EcuTrans9000

Aplicacion para digitalizar la bitácora de viajes de camiones para registrar información operativa y financiera, autocompletar datos del vehículo, gestionar estados de facturación/pago y calcular totales por placa

## Servicios
- Frontend (Angular): http://localhost:4200
- Backend API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api-docs

## Requisitos
- Docker + Docker Compose

## Variables de entorno
Revisar `.env.example` para valores por defecto:
- `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, `POSTGRES_URL`
- `JWT_SECRET`, `JWT_EXPIRATION_MINUTES`
- `AUDIT_MAX_PAYLOAD_SIZE`
- `CORS_ALLOWED_ORIGINS`
- `BOOTSTRAP_SUPERADMIN_USERNAME`, `BOOTSTRAP_SUPERADMIN_PASSWORD`
- `BOOTSTRAP_SUPERADMIN_NOMBRES`, `BOOTSTRAP_SUPERADMIN_CORREO`

## Usuario SUPERADMINISTRADOR inicial
- Username: `admin`
- Password: `Qwerty12345`
- Se asegura automaticamente al iniciar el backend (configurable por variables `BOOTSTRAP_SUPERADMIN_*`).

## Endpoints principales (Modulo Usuarios)
- `POST /auth/login`
- `GET /dashboard` (solo `SUPERADMINISTRADOR`)
- `POST /users` (solo `SUPERADMINISTRADOR`)
- `GET /users` (solo `SUPERADMINISTRADOR`, con filtros `rol`, `activo`, `deleted`)
- `GET /users/{id}` (solo `SUPERADMINISTRADOR`)
- `PUT /users/{id}` (solo `SUPERADMINISTRADOR`)
- `POST /users/{id}/soft-delete` (solo `SUPERADMINISTRADOR`)
- `POST /users/{id}/restore` (solo `SUPERADMINISTRADOR`)
- `POST /users/{id}/activate` (solo `SUPERADMINISTRADOR`)
- `POST /users/{id}/deactivate` (solo `SUPERADMINISTRADOR`)
- `DELETE /users/{id}` (deshabilitado: solo se permite eliminacion logica)
- `GET /users/me`
- `PUT /users/me`

## Colección Postman
- Colección oficial: `postman/EcuTrans9000.postman_collection.json`
- Importar en Postman y ejecutar primero `Auth > Login` para poblar el token de la colección.

## Lineamientos UI obligatorios
- Tema oscuro por defecto y toggle dark/light persistente.
- Botones de acción en listas y navbar como íconos con tooltip.
- Estados `ACTIVO`, `INACTIVO`, `ELIMINADO` con colores consistentes.
- Validaciones al pie de cada campo obligatorio con nombre del campo y breve descripción.
- En creación, edición, cambio de estado y login, usar popup visual del template.
- No usar popups nativos del navegador (`window.alert`, `window.confirm`, `window.prompt`).

## Levantar con Docker
```bash
cp .env.example .env
docker compose up --build
```
