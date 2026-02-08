Actúa siempre como arquitecto senior fullstack y programador cuidadoso. Cada vez que se cree un sistema en este repositorio, estos lineamientos aplican por defecto (obligatorios), salvo que se indique explícitamente lo contrario.

ALCANCE DE ESTOS LINEAMIENTOS
- Aplican para todo sistema nuevo y para todo módulo nuevo dentro de sistemas existentes.
- `agent.md` debe implementarlos como guía operativa (pasos concretos), sin contradecir este documento.
- Si aparece un conflicto entre documentos, prevalece `Inicio.md`.

OBJETIVO BASE
Construir sistemas web empresariales en monorepo, con backend seguro, frontend mantenible y despliegue reproducible.

STACK Y ARQUITECTURA ESTÁNDAR
1) Backend
- Java 17 + Spring Boot 3
- API REST
- Arquitectura hexagonal (domain / application / ports / adapters)
- Spring Security + JWT
- BCrypt para hash de contraseñas
- Flyway para migraciones
- springdoc-openapi (Swagger UI)

2) Frontend
- Angular estable
- Tailwind (estilo base tomado del template TailAdmin cuando aplique)
- Auth con JWT (interceptor + guards + almacenamiento local seguro)

3) Base de datos
- PostgreSQL

4) Infraestructura
- docker-compose (postgres + backend + frontend)
- `.env.example` con variables claras

LINEAMIENTOS UI/UX OBLIGATORIOS (TEMPLATE)
1) Login independiente
- La pantalla de login debe ser independiente del layout principal de la aplicación.
- Debe ser responsive, accesible (labels, estados de error, aria cuando corresponda), segura y fácil de mantener.
- Debe seguir la línea visual propuesta por TailAdmin.

2) Navbar y menú lateral
- Navbar con botones de acción por íconos (no texto fijo), con tooltip en hover/focus.
- Navbar debe incluir: cambiar tema, cerrar sesión, perfil, configuración, versión, ocultar menú lateral.
- El botón de menú lateral debe ubicarse en la esquina superior izquierda del navbar.
- Debe mostrarse el campo `nombres` del usuario autenticado en la esquina superior derecha con color/estilo distintivo.
- El menú lateral debe poder ocultarse/mostrarse desde el navbar.

3) Botones de acción en listas
- En tablas/listados, las acciones deben mostrarse como íconos.
- El texto descriptivo de la acción aparece en tooltip al pasar el mouse o al focus.
- Siempre que un registro permita activar o inhabilitar, la lista debe incluir botón de acción directa para ese cambio de estado.
- El botón debe mostrarse según el estado actual (mostrar solo la acción válida: activar o inhabilitar).
- El scroll horizontal en tablas debe habilitarse de forma natural solo en móvil/tablet; en desktop la tabla debe verse ajustada sin scroll horizontal.

4) Tema global
- El tema debe aplicar a todos los componentes/pantallas del sistema.
- La aplicación debe ser responsive en móvil, tablet y escritorio.
- Formularios y tablas deben adaptarse a la resolución: sin cortes visuales y con scroll horizontal controlado cuando aplique.
- Los formularios deben tener proporciones y distribución de campos según el dispositivo, priorizando legibilidad en equipos externos a PC.
- El tema por defecto es oscuro.
- Debe existir toggle dark/light y persistencia de preferencia (por ejemplo en localStorage).
- Estados visuales de negocio (por ejemplo ACTIVO/INACTIVO/ELIMINADO) deben mostrarse con códigos de color consistentes.
- Acciones deben habilitarse/ocultarse según estado del registro (no mostrar acciones inválidas).

5) Validaciones de formularios
- Cuando un campo obligatorio no sea llenado, debe mostrarse mensaje al pie del campo.
- El mensaje debe incluir el nombre del campo y una breve descripción de lo que se debe ingresar.
- Para campos con formato (por ejemplo correo), mostrar mensaje específico de formato inválido al pie.

6) Popups de acciones críticas
- Toda acción de creación, edición, cambio de estado o login debe mostrar un popup descriptivo.
- El popup debe indicar claramente la acción que se va a ejecutar y/o su resultado.
- El popup debe usar el estilo del template (TailAdmin/Tailwind), no popups nativos del navegador.
- No usar `window.alert`, `window.confirm` ni `window.prompt`.

LINEAMIENTOS DE DATOS
A) Eliminación lógica (soft delete)
- Por defecto NO se elimina físicamente.
- Soft delete permitido según rol definido por reglas del módulo.
- Eliminación física debe estar protegida y ser excepcional.
- Si la política del sistema define “solo eliminación lógica”, la eliminación física debe quedar deshabilitada también en backend y frontend.

B) Auditoría de consumo API (PostgreSQL)
Tabla `api_audit_log`:
- fecha_hora
- endpoint
- request_json
- response_json
- usuario
- rol_usuario
Debe existir filtro/interceptor global para request/response con truncamiento configurable de payload.

C) Auditoría de acciones (PostgreSQL)
Tabla `action_audit_log`:
- fecha_hora
- usuario
- rol_usuario
- modulo_afectado
- tipo_modificacion (CREACION, EDICION, ELIMINADO_LOGICO, LOGIN, ELIMINADO_FISICO)
- id_registro
- nombre_tabla
Registrar login exitoso y operaciones CRUD relevantes.

MÓDULO BASE (USUARIOS) - REGLAS POR DEFECTO
Roles iniciales:
- SUPERADMINISTRADOR
- REGISTRADOR

Usuario base (bootstrap):
- Username: `admin`
- Password: `Qwerty12345`
- Debe asegurarse al iniciar backend (preferiblemente configurable vía variables de entorno).

Campos de usuario:
- id (UUID)
- nombres
- correo (único)
- username (único)
- password_hash
- rol
- activo
- deleted
- deleted_at
- deleted_by
- created_at
- updated_at

Capacidades mínimas:
- Login: `POST /auth/login`
- Perfil propio: `GET /users/me`, `PUT /users/me`
- Gestión admin: crear/listar/ver/editar/soft-delete/restore/delete físico protegido
- Dashboard protegido por rol administrador

SEGURIDAD Y AUTORIZACIÓN
- JWT firmado con `JWT_SECRET` de entorno.
- Roles en claims y autorización por endpoint/guard.
- CORS explícito para frontend.
- Endpoints sensibles protegidos por rol.

ESTRUCTURA DEL REPO (MONOREPO)
/backend
/frontend
/docker-compose.yml
/.env.example
/README.md
/agent.md

BACKEND HEXAGONAL (OBLIGATORIO)
- domain: entidades + enums + reglas de dominio
- application: casos de uso
- ports/in y ports/out
- adapters/in: controllers + DTOs + mappers
- adapters/out: persistencia JPA + integraciones
- config: security, jwt, cors, swagger, bootstrap
No mezclar entidades JPA con modelos de dominio.

DOCUMENTACIÓN OBLIGATORIA
README.md debe incluir:
- cómo levantar con Docker
- urls (frontend, backend, swagger)
- variables de entorno
- endpoints principales
- reglas de soft delete y auditoría
- referencia a la colección Postman oficial en `/postman`

COLECCIÓN POSTMAN (OBLIGATORIA)
- Mantener una colección Postman actualizada en el directorio `/postman`.
- Cada cambio de endpoints, payloads, auth o reglas de negocio debe reflejarse en la colección.
- La colección debe incluir variables mínimas (`baseUrl`, `token`, ids de prueba) y flujo de login.
- No cerrar tareas de API sin actualizar la colección.

agent.md debe incluir:
- convenciones de código
- plantilla para nuevo módulo hexagonal
- checklist de PR

CRITERIOS DE CALIDAD
- Código compila y corre.
- `docker compose up --build` funcional.
- Migraciones Flyway correctas.
- Pruebas mínimas para casos críticos de auth/usuarios.
- Evitar stubs en módulo funcional objetivo; placeholders solo en módulos no priorizados.

ENTREGABLE
- Todos los archivos necesarios y completos.
- Rutas y puertos coherentes.
- Nombres claros y buenas prácticas.
