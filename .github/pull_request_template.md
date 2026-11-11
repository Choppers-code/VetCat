## Qué cambia

<!-- Una o dos líneas. El detalle va en el issue. -->

## Issue

Closes #

## Checklist

- [ ] Compila y las pruebas pasan
- [ ] La entrada se valida en el backend, no solo en la app
- [ ] Respeta el `@PreAuthorize` del rol que corresponde
- [ ] No rompe el aislamiento entre clínicas (si toca datos de clínica)
- [ ] Migración versionada con Flyway (si cambia el esquema)
- [ ] Contrato OpenAPI actualizado (si cambia la API)
- [ ] Sin secretos, `.env`, keystores ni credenciales
- [ ] Documentación actualizada si cambia una decisión

## Riesgo y reversión

<!-- Qué puede romper y cómo se deshace. "Nada" no es una respuesta válida. -->
