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
- Todo tipo público del backend debe incluir Javadoc (`/** ... */`) y no se considera cerrada una tarea de backend si faltan Javadocs.
- Usar soft delete por defecto (`activo`, `deleted`).
- Si la política del módulo es “solo eliminación lógica”, no exponer ni usar borrado físico.
- Documentar API en Swagger/OpenAPI.
- En frontend usar Tailwind con lineamientos TailAdmin.
- Centralizar estilos globales y overrides en `frontend/src/styles.css`.
- Estandarizar patrones visuales repetidos con utilidades (`@utility`) y reutilizarlas en todos los módulos.
- Todo cambio funcional, de flujo, reglas, contratos, catálogos reutilizables o convenciones visibles debe reflejarse en los archivos `.md` aplicables (`README.md`, guías operativas, documentación del módulo y similares).
- Mantener siempre una colección Postman actualizada en `/postman`.
- Si el módulo requiere almacenamiento de archivos, usar persistencia en DB por defecto (tabla de binarios + metadatos), salvo instrucción explícita contraria.
- En auditoría de API, no persistir payload binario crudo; registrar marcador/sanitizar contenido para evitar errores de encoding.

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
- Todo catálogo con alto volumen de registros usado en filtros o formularios debe incluir buscador integrado para filtrar al menos por nombre y documento; en vehículos, también por placa y chofer cuando aplique.
- En creación, edición, cambio de estado y login, mostrar popup descriptivo de la acción.
- Los popups deben implementarse con componentes del template (modal/dialog), no con popups nativos del navegador.
- Los formularios de creación y edición deben renderizarse en popup modal, no embebidos inline dentro de la página.
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
- Si hay archivos adjuntos:
  - definir tabla de archivos (binario + `content_type` + `file_name` + `size` + `created_at/updated_at`);
  - exponer descarga autenticada;
  - evitar guardar binarios en `api_audit_log`.

6. **Frontend del módulo**
- Crear rutas y páginas.
- Aplicar lineamientos UI/tema/acciones por estado.
- Integrar servicios HTTP + interceptor + guards.

7. **Documentación**
- Actualizar `README.md` con endpoints, variables y reglas del módulo.
- Actualizar también los demás archivos `.md` afectados por el cambio funcional o técnico, aunque no se modifiquen endpoints.
- Actualizar colección Postman en `/postman` con endpoints, payloads y auth vigentes.

8. **Pruebas mínimas**
- Backend: tests de casos críticos del módulo.
- Frontend: validar compilación y flujos esenciales.

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
- [ ] Formularios de creación y edición presentados como popup modal consistente con el patrón del sistema.
- [ ] README actualizado.
- [ ] Archivos `.md` aplicables actualizados según el alcance del cambio.
- [ ] Colección Postman actualizada en `/postman`.
- [ ] Build/test ejecutados correctamente.

## Estado actual del proyecto
- Módulo `USERS`: implementado con CRUD, soft delete, estados, perfil y auditoría; formularios de creación/edición en popup modal.
- Módulo `VEHICULOS`: implementado con CRUD, activar/inactivar, soft delete/restore, carga de imágenes/documentos, almacenamiento de archivos en DB (`vehiculo_archivos`), carga masiva Excel con `preview` e `import` y formularios de creación/edición en popup modal.
- Módulo `CLIENTES`: implementado con CRUD, activar/inactivar, soft delete/restore, logo empresarial almacenado en DB dentro de `clientes`, campo `direccion`, carga masiva Excel con `preview`, `import`, plantilla y ejemplo, y formularios de creación/edición en popup modal.
- Módulo `BITACORA`: implementado con CRUD, soft delete/restore, filtros por rango de fechas, exportación Excel basada en plantilla corporativa, importación Excel con plantilla simple y ejemplo, y formularios de creación/edición en popup modal.
- Módulo `DESCUENTOS DE VIAJES`: implementado con CRUD, activar/inactivar, soft delete/restore, asociación de chofer mediante vehículo, importación Excel por placa, formularios de creación/edición en popup modal y habilitación operativa por acceso dinámico de módulo; el eliminado lógico sigue reservado a `SUPERADMINISTRADOR`.
- Módulo `CONSULTA POR PLACAS`: implementado con selección obligatoria de chofer por placa, rango de fechas obligatorio, filtro para aplicar o no retención del 1%, selección de descuentos activos por motivo asociados al chofer, recalculo visual en vivo según viajes marcados y exportación Excel limitada a los viajes seleccionados con total descuentos y detalle `Fecha - Motivo - Monto`.
- Swagger/OpenAPI y colección Postman deben mantenerse sincronizados ante cada cambio de endpoint o payload.

## Habilidades
Una habilidad es un conjunto de instrucciones locales guardadas en un archivo `SKILL.md`. A continuación se lista lo disponible en esta sesión.

### Habilidades disponibles
- `skill-creator`: guía para crear habilidades efectivas. Úsala cuando se solicite crear o actualizar una habilidad que extienda las capacidades de Codex con flujos o conocimiento especializado. (archivo: `/opt/codex/skills/.system/skill-creator/SKILL.md`)
- `skill-installer`: instala habilidades en `$CODEX_HOME/skills` desde opciones curadas o desde una ruta de repositorio GitHub. Úsala cuando se solicite listar o instalar habilidades. (archivo: `/opt/codex/skills/.system/skill-installer/SKILL.md`)

### Cómo usar habilidades
- Descubrimiento: la lista anterior define las habilidades disponibles en la sesión; el contenido está en las rutas indicadas.
- Reglas de activación: si el usuario nombra una habilidad (con `$SkillName` o texto normal) o la tarea coincide claramente con su descripción, se debe usar esa habilidad en ese turno.
- Habilidad faltante o bloqueada: si una habilidad solicitada no existe o no puede leerse, informarlo brevemente y continuar con la mejor alternativa.
- Uso progresivo:
  1. Abrir el `SKILL.md` de la habilidad elegida y leer solo lo necesario para ejecutar la tarea.
  2. Si el `SKILL.md` referencia carpetas como `references/`, cargar únicamente los archivos necesarios.
  3. Si existen `scripts/`, preferir ejecutarlos o ajustarlos en vez de reescribir bloques grandes.
  4. Si existen `assets/` o plantillas, reutilizarlos antes de crear contenido desde cero.
- Coordinación:
  - Si aplican varias habilidades, usar el conjunto mínimo y definir el orden.
  - Anunciar en una línea qué habilidad(es) se usan y por qué.
- Higiene de contexto:
  - Mantener el contexto corto; resumir en lugar de pegar bloques extensos.
  - Evitar abrir referencias profundas innecesarias.
  - Si hay variantes, elegir solo la referencia relevante y dejar constancia.
- Seguridad y fallback: si una habilidad no puede aplicarse limpiamente, indicar el bloqueo y continuar con el mejor enfoque alternativo.
