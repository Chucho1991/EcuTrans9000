# Estado de Documentación

Fecha de validación: 2026-04-06  
Comando ejecutado: `./scripts/validate-documentation.ps1`

## Resultado
- Javadocs: pendiente.
  - Los tipos públicos nuevos incorporados para `Configuración de accesos por rol` quedaron documentados.
  - El validador sigue reportando deuda histórica en otros tipos públicos del backend fuera del alcance de este ajuste documental.
- Postman: OK.
  - Archivo presente: `postman/EcuTrans9000.postman_collection.json`
  - Variables mínimas requeridas por el validador actual: completas.
  - Endpoints críticos históricos requeridos por el validador actual: completos.
  - Cobertura operativa incluida: `Configuración de accesos`, `Clientes` con logo/importación/tabla de equivalencia y `Consulta por placas`.
- README: OK.
  - Temas obligatorios detectados: completos.
  - README actualizado para reflejar `Configuración de accesos`, `Clientes`, `Vehículos`, `Bitácora`, `Descuentos de viajes` y `Consulta por placas`, incluyendo permisos dinámicos por rol, reglas de importación, almacenamiento binario, tabla de equivalencia por destino, descuentos por chofer, formularios de creación/edición en popup modal y exportación financiera por placa con retención definida por viaje, descuentos por motivo y viajes seleccionados.
  - Regla operativa documentada: clientes con viajes pendientes no pueden inactivarse/eliminarse y choferes con pago transportista pendiente tampoco pueden inactivarse/eliminarse.
- Agent/OpenAPI: OK.
  - `AGENTS.md` refleja el estado actual de `CLIENTES`, `VEHICULOS`, `BITACORA`, `DESCUENTOS DE VIAJES` y `CONSULTA POR PLACAS`, además del patrón obligatorio de formularios de creación/edición en popup modal.
  - `OpenApiConfig` describe el alcance actual: configuración de accesos por rol, CRUD, soft delete, binarios en PostgreSQL, importación/exportación Excel operativa, descuentos por chofer y reportes por placa.

## Cobertura operativa actual
- La colección Postman incluye autenticación, usuarios, sistema, configuración de accesos, vehículos, clientes, bitácora, descuentos de viajes y consulta por placas.
- README documenta:
  - módulo `Configuración de accesos` con permisos persistidos por `rol + módulo`,
  - módulo `Clientes` con CRUD, logo en BD, campo `direccion`, importación Excel y tabla de equivalencia por destino,
  - módulo `Vehículos` con binarios en PostgreSQL,
  - módulo `Bitácora` con importación/exportación Excel, filtros por rango de fechas y formularios de creación/edición en popup modal,
  - módulo `Descuentos de viajes` con CRUD, fecha de aplicación informativa, importación Excel por placa, estados, formularios en popup modal y borrado lógico restringido a `SUPERADMINISTRADOR`,
  - módulo `Consulta por placas` con placa/chofer obligatorio, fechas obligatorias, retención opcional, selección de descuentos por motivo, recalculo por viajes marcados y exportación Excel del subconjunto seleccionado.
- Swagger/OpenAPI declara el alcance actual del backend incluyendo configuración de accesos, clientes, vehículos, bitácora, descuentos de viajes y consulta por placas.

## Acción recomendada
- Ejecutar este validador en cada PR de backend/documentación.
- Mantener sincronizadas colección Postman y validación cuando cambien variables de colección, endpoints o flujos binarios.
- Volver a ejecutar la validación tras cada alta de módulo o cambio de payload.
