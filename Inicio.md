Actúa como un arquitecto senior fullstack y como un programador cuidadoso. Construye el proyecto "EcuTrans9000" (app para digitalizar la bitácora de viajes de camiones para registrar información operativa y financiera, autocompletar datos del vehículo, gestionar estados de facturación/pago y calcular totales por placa). Debes ENTREGAR código funcional, iniciando con el MÓDULO 1: GESTIÓN DE USUARIOS (COMPLETO). Los demás módulos (Vehículos, Clientes, Bitácora de viajes, Consulta por placas) solo deben quedar como estructura y rutas placeholder.

STACK Y ARQUITECTURA
1) Backend:
- Java 17 + Spring Boot 3
- API REST
- Arquitectura hexagonal (domain / application / ports / adapters)
- Spring Security + JWT
- BCrypt para hash de contraseñas
- Flyway para migraciones
- springdoc-openapi (Swagger UI)

2) Frontend:
- Angular (usar la versión más reciente estable)
- Angular Material (UI simple)
- Auth con JWT (interceptor, guard, almacenamiento seguro)
- Módulos: auth, users (completo), dashboard (solo para SUPERADMIN/ADMIN), placeholders para otros módulos

3) Bases de datos:
- PostgreSQL: usuarios + auditorías + datos

4) Docker:
- docker-compose: postgres + mongo + backend + frontend
- .env.example y variables claras

LINEAMIENTOS DE DATOS
A) Eliminación lógica (soft delete):
- Por defecto, NO se elimina físicamente.
- Soft delete permitido por SUPERADMINISTRADOR y ADMINISTRADOR.
- Solo SUPERADMINISTRADOR puede eliminar físicamente (en esta etapa: implementar endpoint de borrado físico pero protegido y advertido; NO usarlo por defecto).

B) Auditoría de consumo API (PostgreSQL):
Tabla api_audit_log con:
- fecha_hora (timestamp)
- endpoint (varchar)
- request_json (JSONB o TEXT)
- response_json (JSONB o TEXT)
- usuario (varchar)
- rol_usuario (varchar)
Implementar un filtro/interceptor global que registre request/response (cuidar tamaño: si es muy grande, truncar a un límite configurable).

C) Auditoría de acciones (PostgreSQL) para login y creación/modificación:
Tabla action_audit_log con:
- fecha_hora
- usuario
- rol_usuario
- modulo_afectado
- tipo_modificacion (CREACION, EDICION, ELIMINADO_LOGICO, LOGIN, ELIMINADO_FISICO)
- id_registro
- nombre_tabla
Registrar:
- LOGIN exitoso
- CREATE/UPDATE/SOFT_DELETE/DELETE físico en usuarios

MÓDULO 1: GESTIÓN DE USUARIOS (COMPLETO)
Roles iniciales:
- SUPERADMINISTRADOR
- REGISTRADOR

Datos de usuario:
- id (UUID)
- nombres
- correo (único)
- username (único)
- password_hash
- rol (enum)
- activo (bool)
- deleted (bool)
- deleted_at
- deleted_by
- created_at
- updated_at

Reglas funcionales:
1) Registro/creación de usuario:
- Solo SUPERADMINISTRADOR pueden crear usuarios.
- Usuario génerico SUPERADMINISTRADOR: admin, Contraseña: Qwerty12345.
- Validar email y username únicos.
- Password requiere confirmación (password + confirmPassword) en request.
- Guardar con BCrypt.

2) Login:
- Endpoint /auth/login con username/email + password.
- Retornar JWT + info básica del usuario (id, nombres, username, rol).
- Registrar action_audit_log tipo LOGIN.

3) Autogestión de perfil:
- Un usuario puede actualizar SOLO: nombres, correo, username y password.
- Endpoint /users/me GET y PUT.
- Para cambio de password exigir password + confirmPassword.

4) Gestión administrativa:
- Listar usuarios (con paginación y filtros: rol, activo, deleted).
- Ver detalle por id.
- Editar usuario (admin puede editar: nombres, correo, username, rol, activo; pero NO ver password).
- Soft delete: /users/{id}/soft-delete (solo SUPERADMIN)
- Restore (opcional útil): /users/{id}/restore (solo SUPERADMIN)
- Delete físico: /users/{id} DELETE (solo SUPERADMINISTRADOR) — implementar pero enfatizar que es excepcional.

5) Panel general:
- Endpoint /dashboard solo SUPERADMINISTRADOR (puede devolver métricas dummy por ahora).

SEGURIDAD Y AUTORIZACIÓN
- JWT firmado con secret de env var.
- Roles en claims.
- Guards en Angular por rol.
- Endpoints protegidos según reglas.
- CORS configurado para frontend.

ESTRUCTURA DEL REPO (MONOREPO)
/backend
/frontend
/docker-compose.yml
/.env.example
/README.md
/agent.md

BACKEND: HEXAGONAL (OBLIGATORIO)
- domain: entidades + enums + validaciones de dominio
- application: use cases (CreateUser, UpdateUser, Login, SoftDeleteUser, ListUsers, GetMe, UpdateMe)
- ports/in: interfaces de casos de uso
- ports/out: repositorios (UserRepositoryPort, AuditRepositoryPort)
- adapters/in: controllers REST + DTOs + mappers
- adapters/out: JPA Postgres (users, auditorías) + Mongo config repositorio
- config: security, jwt, swagger, cors
NO mezcles JPA entities directamente en domain: usa modelos de dominio y mappers.

FRONTEND ANGULAR (MÓDULO USERS COMPLETO)
- Pantallas:
  - Login
  - Dashboard (solo admin/superadmin)
  - Users:
    - listado con tabla (paginación)
    - crear usuario
    - editar usuario
    - ver detalle
    - soft delete / restore
  - Perfil:
    - ver y editar mis datos
    - cambiar password
- Interceptor JWT
- Guard por autenticación y rol
- Manejo de errores (toasts/snackbar)

DOCUMENTACIÓN
README.md:
- cómo levantar con Docker
- urls (frontend, swagger)
- variables de entorno
- endpoints principales del módulo usuarios
- explicación de soft delete y auditorías

agent.md:
- convenciones de código
- cómo agregar un nuevo módulo (plantilla hexagonal)
- checklist para PR

REQUISITOS DE CALIDAD
- Código compila y corre.
- docker compose up levanta todo.
- Migraciones Flyway crean tablas.
- Pruebas mínimas: al menos tests unitarios básicos para use cases clave (CreateUser, Login) y/o integración para controller.
- No dejes endpoints “stub” para usuarios: el módulo de usuarios debe estar completo y funcional.
- Los demás módulos solo placeholders (sin lógica).

ENTREGAR:
- Todos los archivos necesarios, completos (no vacíos).
- Asegura que las rutas y puertos sean coherentes.
- Usa buenas prácticas y nombres claros.

NO implementes los demás módulos; solo crea carpetas, rutas y componentes vacíos con “Coming soon”. El ÚNICO módulo funcional en esta etapa es Usuarios (incluye login).

