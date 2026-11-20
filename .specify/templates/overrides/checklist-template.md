# [TIPO DE LISTA] Lista de verificación: [NOMBRE DE LA FUNCIONALIDAD]

**Propósito**: [Descripción breve de lo que cubre esta lista]
**Creada**: [FECHA]
**Funcionalidad**: [Enlace a spec.md u otra documentación relevante]

**Nota**: Esta lista personalizada la genera el comando `/speckit-checklist` (en OpenCode, `/speckit.checklist`) según el contexto y los requisitos de la funcionalidad.
**Responsable de la revisión**: Esta lista es un artefacto de revisión de la calidad de los requisitos y le pertenece a quien revisa. Marca un elemento con `[x]` solo cuando quien revisa determine que se cumple el criterio de calidad.
**Significado de la marca**: `[x]` significa que el criterio se revisó y se cumple en cuanto a calidad de requisitos. No significa que el trabajo de implementación esté terminado.

<!--
  ============================================================================
  IMPORTANTE: Los elementos de abajo son EJEMPLOS ilustrativos.

  El comando /speckit-checklist DEBE reemplazarlos con elementos reales según:
  - La solicitud específica de la lista
  - Los requisitos de spec.md
  - El contexto técnico de plan.md
  - Los detalles de implementación de tasks.md

  NO conserves estos ejemplos en la lista generada.
  ============================================================================
-->

## [Categoría 1]

- [ ] CHK001 Primer elemento con una acción clara
- [ ] CHK002 Segundo elemento
- [ ] CHK003 Tercer elemento

## [Categoría 2]

- [ ] CHK004 Otro elemento de la categoría
- [ ] CHK005 Elemento con criterios específicos
- [ ] CHK006 Último elemento de esta categoría

## Notas

- Marca los elementos con `[x]` solo cuando la revisión confirme que se cumple el criterio de calidad del requisito
- Deja sin marcar los elementos que aún requieran aclaración, corrección o evaluación de quien revisa
- `/speckit-implement` lee el estado de las casillas como una puerta y no debe modificar las marcas
- `checklists/requirements.md` tiene su propio ciclo de vida, que mantienen `/speckit-specify` y `/speckit-clarify`
- Agrega comentarios o hallazgos en línea
- Enlaza a recursos o documentación relevante
- Los elementos se numeran de forma consecutiva para referenciarlos con facilidad
