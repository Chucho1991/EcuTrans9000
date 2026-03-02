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
- `CLIENTES_MAX_LOGO_BYTES`, `CLIENTES_IMPORT_BATCH_SIZE`

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

### Módulo Clientes
Base path: `/clientes`

- `POST /clientes`
- `GET /clientes`
- `GET /clientes/{id}`
- `PUT /clientes/{id}`
- `PATCH /clientes/{id}/toggle-activo`
- `DELETE /clientes/{id}` (solo `SUPERADMINISTRADOR`, borrado lógico)
- `PATCH /clientes/{id}/restore` (solo `SUPERADMINISTRADOR`)
- `DELETE /clientes/{id}/force` (solo `SUPERADMINISTRADOR`, borrado físico)
- `POST /clientes/{id}/logo` (multipart/form-data, logo JPG/PNG/WEBP almacenado en DB)
- `GET /clientes/{id}/logo`
- `GET /clientes/import/template`
- `GET /clientes/import/template/example`
- `POST /clientes/import/preview?mode=INSERT_ONLY|UPSERT&partialOk=true|false`
- `POST /clientes/import?mode=INSERT_ONLY|UPSERT&partialOk=true|false`

Reglas operativas vigentes del módulo:
- `documento` único en todo el catálogo.
- Un cliente inactivo o eliminado no puede asociarse a nuevos viajes.
- El logo de empresa se almacena en PostgreSQL dentro de la tabla `clientes`.
- El catálogo incluye el campo `direccion`.
- La importación masiva es por Excel `.xlsx`, no CSV.
- La plantilla de importación usa los encabezados `tipo_documento,documento,nombre,direccion,descripcion,activo`.
- Existe descarga de plantilla vacía y plantilla con ejemplo en Excel.

### Módulo Bitácora
Base path: `/api/bitacora/viajes`

- `POST /api/bitacora/viajes`
- `PUT /api/bitacora/viajes/{id}`
- `GET /api/bitacora/viajes/{id}`
- `GET /api/bitacora/viajes?page=&size=&q=&vehiculoId=&clienteId=&fechaDesde=&fechaHasta=&includeDeleted=`
- `DELETE /api/bitacora/viajes/{id}` (solo `SUPERADMINISTRADOR`, borrado lógico)
- `PATCH /api/bitacora/viajes/{id}/restore` (solo `SUPERADMINISTRADOR`)
- `GET /api/bitacora/viajes/export?q=&vehiculoId=&clienteId=&fechaDesde=&fechaHasta=`
- `GET /api/bitacora/viajes/import/template`
- `GET /api/bitacora/viajes/import/template/example`
- `POST /api/bitacora/viajes/import/preview?mode=INSERT_ONLY|UPSERT&partialOk=true|false`
- `POST /api/bitacora/viajes/import?mode=INSERT_ONLY|UPSERT&partialOk=true|false`

Reglas operativas vigentes del módulo:
- La importación masiva es por Excel `.xlsx`, no CSV.
- La plantilla de importación usa `Placa` para vehículo y `Documento cliente` para cliente.
- La importación genera automáticamente `numeroViaje`.
- El reporte Excel usa la plantilla corporativa del módulo y el título `BITACORA VIAJES <anio>` toma el año del rango filtrado (`fechaDesde`/`fechaHasta`).
- En exportación, los checks operativos salen como `✓` para `SI` y `X` para `NO`.

### Módulo Consulta por placas
Base path: `/api/placas`

- `GET /api/placas/consulta?placa=&fechaDesde=&fechaHasta=`
- `GET /api/placas/consulta/export?placa=&fechaDesde=&fechaHasta=`

Reglas operativas vigentes del módulo:
- La consulta toma los registros del módulo `Bitácora`.
- La pantalla inicia con lista vacía y solo consulta al usar el filtro.
- El filtro principal es `placa`; adicionalmente acepta rango `fechaDesde` y `fechaHasta`.
- El resumen financiero calcula automáticamente:
  - `Valor Factura` como suma de `valor`,
  - `Retención 1%` sobre el total facturado,
  - `Comisión administrativa 6%` sobre el total facturado,
  - `Pago Total = Valor Factura - Retención 1% - Comisión 6% - Anticipos`.
- La exportación Excel genera un reporte financiero por placa con estilo corporativo y logo institucional.

## Soft delete y auditoría
- Eliminación por defecto lógica (`deleted`, `deleted_at`), sin borrado físico en módulos funcionales.
- Auditoría API en `api_audit_log` (endpoint, request, response, usuario, rol).
- Auditoría de acciones en `action_audit_log` (CREACION, EDICION, CAMBIO_ESTADO, ELIMINADO_LOGICO, RESTAURACION, LOGIN, ELIMINADO_FISICO, IMPORT_CSV).

## Repositorio de imágenes y archivos
- El almacenamiento de `foto`, `documento` y `licencia` del módulo vehículos se realiza en PostgreSQL (`vehiculo_archivos`) como contenido binario.
- El backend mantiene metadatos en `vehiculos` (`foto_path`, `doc_path`, `lic_path`) y el binario en la tabla de archivos.
- El módulo clientes almacena el logo empresarial directamente en la tabla `clientes` (`logo_file_name`, `logo_content_type`, `logo_contenido`).

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
- Variables operativas incluidas: `baseUrl`, `token`, `targetUserId`, `targetVehiculoId`, `targetClienteId`, `targetBitacoraId`.
- Cobertura actual: auth, sistema, usuarios, vehículos, clientes, bitácora y consulta por placas.

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
