# Guía Operativa del Agente

Este archivo define cómo implementar **nuevos módulos** en el proyecto.
Los lineamientos base están en `Inicio.md` y son obligatorios.

## Regla de precedencia
- Si hay conflicto entre este archivo y `Inicio.md`, prevalece `Inicio.md`.

## Convenciones de código
- Backend en arquitectura hexagonal: `domain / application / ports / adapters`.
- No usar entidades JPA dentro de `domain`; mapear explícitamente.
- DTOs solo en `adapters/in`.
- Validar entradas con `jakarta.validation`.
- Usar soft delete por defecto (`activo`, `deleted`).
- Si la política del módulo es “solo eliminación lógica”, no exponer ni usar borrado físico.
- Documentar API en Swagger/OpenAPI.
- En frontend usar Tailwind con lineamientos TailAdmin.
- Centralizar estilos globales y overrides en `frontend/src/styles.css`.
- Mantener siempre una colección Postman actualizada en `/postman`.

## Lineamientos UI obligatorios para módulos nuevos
- Tema oscuro por defecto.
- Diseño responsive obligatorio en móvil, tablet y escritorio.
- Formularios y tablas deben adaptarse al ancho disponible de pantalla.
- El scroll horizontal en tablas debe existir solo para móvil/tablet; en desktop debe evitarse.
- Los formularios deben ajustarse proporcionalmente al tipo de dispositivo (móvil/tablet/escritorio).
- Toggle dark/light con persistencia.
- El tema debe aplicar a todas las pantallas/componentes del módulo.
- Login desacoplado del layout principal (si el módulo toca autenticación).
- Botones de acción de listas en formato ícono + tooltip en hover/focus.
- Si un registro permite activación/inhabilitación, incluir en la lista botón de acción directa para activar o inhabilitar.
- Mostrar solo la acción válida según estado actual (no mostrar acciones contradictorias).
- Navbar con acciones por ícono (tema, perfil, configuración, versión, cerrar sesión, ocultar sidebar).
- El botón de menú lateral debe quedar en el extremo superior izquierdo del navbar.
- Mostrar el campo `nombres` del usuario autenticado en la esquina superior derecha con estilo distintivo.
- Mostrar estados de negocio con color:
  - `ACTIVO` en verde
  - `INACTIVO` en rojo
  - `ELIMINADO` en color de advertencia
- Habilitar/ocultar acciones según estado del registro (no mostrar acciones inválidas).
- En formularios, cuando falte un campo obligatorio, mostrar mensaje al pie del campo.
- El mensaje debe incluir nombre del campo y breve descripción de qué debe llenar.
- Mostrar mensajes específicos para formato inválido (por ejemplo correo).
- En creación, edición, cambio de estado y login, mostrar popup descriptivo de la acción.
- Los popups deben implementarse con componentes del template (modal/dialog), no con popups nativos del navegador.
- Prohibido usar `window.alert`, `window.confirm` o `window.prompt`.

## Plantilla para agregar un nuevo módulo (hexagonal)
1. **Domain**
- Crear entidades/enums/reglas en `backend/src/main/java/.../domain`.

2. **Application**
- Crear casos de uso en `application`.
- Definir puertos `ports/in` y `ports/out`.

3. **Adapters**
- `adapters/in/rest`: controllers, DTOs, validaciones, mappers.
- `adapters/out/persistence`: entidades JPA, repositorios, mappers.

4. **Seguridad**
- Proteger endpoints por rol.
- Si aplica, integrar JWT claims y guards en frontend.

5. **Datos y auditoría**
- Agregar migración Flyway.
- Registrar auditoría API y auditoría de acciones cuando corresponda.

6. **Frontend del módulo**
- Crear rutas y páginas.
- Aplicar lineamientos UI/tema/acciones por estado.
- Integrar servicios HTTP + interceptor + guards.

7. **Documentación**
- Actualizar `README.md` con endpoints, variables y reglas del módulo.
- Actualizar colección Postman en `/postman` con endpoints, payloads y auth vigentes.

8. **Pruebas mínimas**
- Backend: tests de casos críticos del módulo.
- Frontend: validar build y flujos esenciales.

## Checklist para PR
- [ ] Migraciones Flyway incluidas y válidas.
- [ ] Endpoints documentados en Swagger.
- [ ] DTOs con validación.
- [ ] Casos de uso/puertos implementados.
- [ ] Seguridad por rol aplicada.
- [ ] Soft delete aplicado (y delete físico deshabilitado cuando aplique).
- [ ] UI con tema oscuro global + toggle.
- [ ] Acciones con íconos + tooltips y reglas por estado.
- [ ] Botones de activar/inhabilitar presentes en listas cuando aplique (solo acción válida según estado).
- [ ] Formularios con mensajes al pie por campo obligatorio (nombre + breve descripción).
- [ ] Popups descriptivos implementados para creación, edición, cambio de estado y login.
- [ ] Popups implementados con estilo del template (sin `window.alert`/`window.confirm`/`window.prompt`).
- [ ] README actualizado.
- [ ] Colección Postman actualizada en `/postman`.
- [ ] Build/test ejecutados correctamente.
