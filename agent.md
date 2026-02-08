# Convenciones de código
- Mantener arquitectura hexagonal en backend: domain / application / ports / adapters.
- No usar entidades JPA en domain, siempre mapear.
- DTOs solo en adapters/in.
- Validar entrada con `jakarta.validation`.
- Control de estados en los regitros que se guardan por formulario (en BDD booleano activo, en front Activo/Inactivo)
- Control de eliminación en los regitros que se guardan por formulario (en BDD booleano deleted, en front botón de elimando con confirmación vía popup)
- Documentar cada método con Javadoc en Java y con Compodoc en Angular.
- En el frontend usar utilidades de Tailwind para estilos nuevos y centralizar overrides globales en `frontend/src/styles.css`.
- El paquete de íconos los debe tomar de tailadmin adjunto en la carpeta /libs
- Cuando el proyecto se declare culminado, eliminar los logs agregados para depuración.
- Cuando el proyecto se declare culminado, eliminar la configuración temporal de debug (puertos/env vars).
- En formularios, mostrar validaciones en rojo y mensajes claros (requerido, correo inválido, contraseñas no coinciden).
- En listas, usar íconos en las acciones y mostrar el nombre de la acción al pasar el mouse (tooltip).
- Todas las pantallas por defecto tienen que llevar el estilo oscuro de la plantilla tailadmin

# Cómo agregar un nuevo módulo (plantilla hexagonal)
1. **Domain**: crear entidad/enum en `backend/src/main/java/com/ecutrans/domain`.
2. **Ports**:
   - `ports/in`: interfaz de caso de uso.
   - `ports/out`: interfaz de repositorio/servicios externos.
3. **Application**: servicio en `application/usecase` implementando el puerto.
4. **Adapters**:
   - `adapters/in/rest`: controller + DTOs + mapper.
   - `adapters/out/persistence`: repositorios JPA y mappers.
5. **Migraciones**: agregar script en `resources/db/migration`.
6. **Tests**: agregar al menos 1 test unitario por caso de uso.

# Checklist para PR
- [ ] Migraciones Flyway actualizadas.
- [ ] DTOs y validaciones creadas.
- [ ] Use cases con puertos in/out.
- [ ] Tests mínimos añadidos.
- [ ] README actualizado si aplica.
