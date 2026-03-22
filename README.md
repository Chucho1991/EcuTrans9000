# EcuTrans9000

Plataforma para digitalizar la operación de transporte: control de usuarios, bitácora de viajes, catálogo de vehículos/clientes y consulta financiera por placa.

## Servicios
- Frontend (Angular): `http://localhost:4200`
- Backend API (Spring Boot): `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api-docs`

## Requisitos
- Docker + Docker Compose

## Variables de entorno
Tomar como base `.env.example`.

Variables clave:
- Base de datos: `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, `POSTGRES_URL`
- Seguridad: `JWT_SECRET`, `JWT_EXPIRATION_MINUTES`, `CORS_ALLOWED_ORIGINS`
- Auditoría: `AUDIT_MAX_PAYLOAD_SIZE`
- Bootstrap administrador: `BOOTSTRAP_SUPERADMIN_*`
- Límites de archivos/importación: `VEHICULOS_*`, `CLIENTES_*`

## Inicio rápido
```bash
cp .env.example .env
docker compose up --build
```

## Usuario inicial
El backend asegura un usuario `SUPERADMINISTRADOR` al iniciar:
- Username: `admin`
- Password: `Qwerty12345`

## Endpoints principales

### Auth y sistema
- `POST /auth/login`
- `GET /dashboard` (solo `SUPERADMINISTRADOR`)
- `GET /api/system/health`

### Usuarios (`/users`)
- CRUD administrativo (sin hard delete)
- Estados: activar, desactivar, soft-delete, restore
- Perfil autenticado: `GET /users/me`, `PUT /users/me`

### Configuración de accesos por rol (`/settings/module-access`)
- `GET /settings/module-access/me`
- `GET /settings/module-access` (solo `SUPERADMINISTRADOR`)
- `PUT /settings/module-access/{role}` (solo `SUPERADMINISTRADOR`)

### Módulo Vehículos (`/api/vehiculos`)
- CRUD operativo + estados
- Campo adicional `cuentaBancaria` disponible en formulario, detalle, listado e importación Excel
- Carga/lectura de foto, documento y licencia
- Importación masiva (`preview` y `import`) y plantillas

### Módulo Clientes (`/clientes`)
- CRUD operativo + estados
- Logo empresarial en base de datos
- Importación masiva (`preview` y `import`) y plantillas
- Flag `Aplica Tabla de Equivalencia` por cliente
- Tabla de equivalencia por destino con carga Excel (`DESTINO`, `VALOR DESTINO`, `COSTO CHOFER`)
- Edición manual de equivalencias desde el detalle del cliente

### Módulo Bitácora (`/api/bitacora/viajes`)
- CRUD operativo + borrado lógico/restauración
- Exportación Excel
- Importación masiva con plantilla
- Campo obligatorio `costoChofer` en creación, edición e importación Excel
- Si el cliente seleccionado tiene tabla de equivalencia, el formulario cambia `Destino` a selección por opciones y autocompleta `valor` y `costoChofer`
- En la plantilla de importación, la columna `E` (`Documento cliente`) se genera con formato texto para conservar ceros a la izquierda y documentos numéricos largos

### Módulo Placas (`/api/placas`)
- Consulta por placa, código de viaje, estado de pago al chofer y rango de fechas
- Exportación financiera por placa

## Soft delete y auditoría
- Soft delete por defecto en módulos funcionales.
- Auditoría API en `api_audit_log`.
- Auditoría de acciones (creación, edición, estado, login, restauración, etc.) en `action_audit_log`.

## Colección Postman
- Archivo oficial: `postman/EcuTrans9000.postman_collection.json`
- Ejecutar primero `Auth > Login` para poblar token.
- Variables incluidas: `baseUrl`, `token`, `targetUserId`, `targetVehiculoId`, `targetClienteId`, `targetBitacoraId`.

## Validación de documentación
Script:
```bash
./scripts/validate-documentation.ps1
```
Valida Javadocs públicos, cobertura base de Postman y secciones obligatorias del README.

## Lineamientos UI
- Diseño responsive en móvil/tablet/escritorio.
- Tema oscuro por defecto con toggle persistente.
- Uso de popups visuales del template (sin `window.alert/confirm/prompt`).
- Estados visuales consistentes (`ACTIVO`, `INACTIVO`, `ELIMINADO`).
- Los catálogos grandes usados en filtros y formularios deben ofrecer buscador integrado.
- En clientes, el buscador de catálogo debe permitir filtrar por documento y nombre.
- En vehículos, el buscador de catálogo debe permitir filtrar por placa, documento y chofer.

## Mantenimiento de documentación
- Todo cambio funcional, de flujo, reglas de negocio, contratos, patrones reutilizables o convenciones visibles debe actualizar los archivos `.md` aplicables.
- Como mínimo, evaluar `README.md`, guías operativas del repositorio y documentación del módulo afectado en cada implementación.

## Acceso desde celular (misma red)
- Abrir: `http://<IP_DE_TU_PC>:4200`
- Consumir API: `http://<IP_DE_TU_PC>:8080`
