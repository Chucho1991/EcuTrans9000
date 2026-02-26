# EcuTrans9000

Aplicación para digitalizar la bitácora de viajes de camiones con gestión operativa y financiera.

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
- `VEHICULOS_MAX_IMAGE_BYTES`, `VEHICULOS_IMPORT_BATCH_SIZE`

Para acceso desde celular por IP, configura `CORS_ALLOWED_ORIGINS` incluyendo tu red local, por ejemplo:
- `http://192.168.*:4200,http://10.*:4200`

## Usuario SUPERADMINISTRADOR inicial
- Username: `admin`
- Password: `Qwerty12345`
- Se asegura automáticamente al iniciar el backend (configurable por variables `BOOTSTRAP_SUPERADMIN_*`).

## Endpoints principales

### Auth y sistema
- `POST /auth/login`
- `GET /dashboard` (solo `SUPERADMINISTRADOR`)
- `GET /api/system/health`

### Módulo Usuarios
- `POST /users` (solo `SUPERADMINISTRADOR`)
- `GET /users` (solo `SUPERADMINISTRADOR`, filtros `rol`, `activo`, `deleted`)
- `GET /users/{id}` (solo `SUPERADMINISTRADOR`)
- `PUT /users/{id}` (solo `SUPERADMINISTRADOR`)
- `POST /users/{id}/soft-delete` (solo `SUPERADMINISTRADOR`)
- `POST /users/{id}/restore` (solo `SUPERADMINISTRADOR`)
- `POST /users/{id}/activate` (solo `SUPERADMINISTRADOR`)
- `POST /users/{id}/deactivate` (solo `SUPERADMINISTRADOR`)
- `DELETE /users/{id}` (deshabilitado: solo se permite eliminación lógica)
- `GET /users/me`
- `PUT /users/me`

### Módulo Vehículos
Base path: `/api/vehiculos`

- `POST /api/vehiculos`
- `PUT /api/vehiculos/{id}`
- `GET /api/vehiculos/{id}`
- `GET /api/vehiculos?page=&size=&q=&estado=&includeDeleted=`
- `POST /api/vehiculos/{id}/activate`
- `POST /api/vehiculos/{id}/deactivate`
- `POST /api/vehiculos/{id}/soft-delete`
- `POST /api/vehiculos/{id}/restore`
- `POST /api/vehiculos/{id}/foto` (multipart/form-data)
- `POST /api/vehiculos/{id}/documento` (multipart/form-data, imagen o PDF)
- `POST /api/vehiculos/{id}/licencia-img` (multipart/form-data, imagen o PDF)
- `GET /api/vehiculos/{id}/foto`
- `GET /api/vehiculos/{id}/documento`
- `GET /api/vehiculos/{id}/licencia-img`
- `GET /api/vehiculos/import/template`
- `POST /api/vehiculos/import/preview?mode=INSERT_ONLY|UPSERT&partialOk=true|false`
- `POST /api/vehiculos/import?mode=INSERT_ONLY|UPSERT&partialOk=true|false`

## Soft delete y auditoría
- Eliminación por defecto lógica (`deleted`, `deleted_at`), sin borrado físico en módulos funcionales.
- Auditoría API en `api_audit_log` (endpoint, request, response, usuario, rol).
- Auditoría de acciones en `action_audit_log` (CREACION, EDICION, ELIMINADO_LOGICO, LOGIN, ELIMINADO_FISICO, IMPORT_CSV).

## Repositorio de imágenes y archivos
- El almacenamiento de `foto`, `documento` y `licencia` del módulo vehículos se realiza en PostgreSQL (`vehiculo_archivos`) como contenido binario.
- El backend mantiene metadatos en `vehiculos` (`foto_path`, `doc_path`, `lic_path`) y el binario en la tabla de archivos.

## Estándar reusable para próximos módulos
- Mantener arquitectura hexagonal completa (`domain`, `application`, `ports`, `adapters`).
- Implementar ciclo funcional completo del catálogo cuando aplique: CRUD + activar/inactivar + soft delete/restore.
- Si el módulo maneja archivos, por defecto almacenar binarios en DB y no en filesystem local del contenedor.
- Para auditoría API, omitir/sanitizar payload binario antes de persistir en `api_audit_log`.
- Incluir importación masiva con `preview`, `partialOk` y errores por fila cuando el dominio lo requiera.
- Mantener coherencia visual: íconos en menú y acciones, tooltips, popups no nativos y estados con color.

## Colección Postman
- Colección oficial: `postman/EcuTrans9000.postman_collection.json`
- Importar en Postman y ejecutar primero `Auth > Login` para poblar token.

## Validación de documentación
- Script de validación: `scripts/validate-documentation.ps1`
- Verifica tres frentes: cobertura básica de Javadocs en tipos públicos, consistencia mínima de colección Postman y temas obligatorios en `README.md`.
- Ejecución:
```bash
./scripts/validate-documentation.ps1
```

## Lineamientos UI obligatorios
- Diseño responsive obligatorio en móvil, tablet y escritorio.
- Formularios y tablas deben adaptarse a la resolución (sin cortes; con scroll horizontal controlado en tablas cuando sea necesario).
- El scroll horizontal de tablas debe aplicarse solo en móvil/tablet; en desktop la tabla debe mostrarse ajustada sin scroll horizontal.
- Los formularios deben mantener proporciones adecuadas al dispositivo para mejorar legibilidad y uso.
- Tema oscuro por defecto y toggle dark/light persistente.
- Botones de acción en listas y navbar como íconos con tooltip.
- Botón de menú lateral en la esquina superior izquierda del navbar.
- Campo `nombres` del usuario autenticado visible en la esquina superior derecha con color distintivo.
- Estados `ACTIVO`, `INACTIVO`, `ELIMINADO` con colores consistentes.
- Validaciones al pie de cada campo obligatorio con nombre del campo y breve descripción.
- En creación, edición, cambio de estado y login, usar popup visual del template.
- No usar popups nativos del navegador (`window.alert`, `window.confirm`, `window.prompt`).

## Levantar con Docker
```bash
cp .env.example .env
docker compose up --build
```

## Acceso desde celular (misma red WiFi)
- Abre en el celular: `http://<IP_DE_TU_PC>:4200`
- El frontend usa automáticamente `http://<IP_DE_TU_PC>:8080` como API.
