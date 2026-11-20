# ADR-0008 · notificaciones: bandeja y push

| | |
|---|---|
| **Estado** | Aceptada |
| **Fecha** | 2026-11-20 |
| **Área** | 1 · Arquitectura, con el área 5 |
| **Issue** | #76 |
| **Decide** | La bandeja dentro de la app es la fuente de verdad de las notificaciones y el push con Firebase Cloud Messaging es el canal de entrega, detrás de un adaptador |

## Contexto

Los requerimientos (`docs/requerimientos/10_NOT_notificaciones.md`) piden que `veterinario` y `cliente` reciban avisos de citas, solicitudes y recordatorios, también con la app cerrada. El alcance excluye WhatsApp y SMS, y deja el push dentro.

## Opciones consideradas

| Opción | Ventaja | Costo |
|---|---|---|
| **Bandeja como fuente de verdad y push como entrega, con Firebase Cloud Messaging** | El aviso nunca se pierde y llega con la app cerrada | Depende de un servicio externo y de internet |
| Solo bandeja | Sin dependencias externas | El usuario no se entera hasta abrir la app, y el equipo quiere avisos con la app cerrada |
| WhatsApp o SMS | Llega a cualquier teléfono | Fuera de alcance: costo y proveedor |

## Decisión

1. **Toda notificación se guarda en una bandeja** por usuario y clínica, con retención de 90 días, y se envía además por push.
2. El envío va detrás de un **adaptador** (patrón Adapter, `Tecnologias_y_patrones.md` §2). El backend no llama a Firebase desde un módulo de negocio.
3. **Si el servicio de push falla, la notificación sigue en la bandeja.**
4. **El texto del push no incluye datos clínicos.**
5. Los tokens de dispositivo se guardan por usuario y se borran al cerrar sesión.
6. Los trabajos programados (recordatorios, vencimiento de planes) reciben el tenant de forma explícita (`Decisiones_tecnicas.md` §2).
7. Las credenciales de Firebase son un secreto fuera del repositorio.

## Consecuencias

### Se gana

- Una sola fuente de verdad, y un canal de entrega que se puede cambiar sin tocar los módulos.
- El usuario recibe el aviso aunque la app esté cerrada.

### Se paga

- Una dependencia de un servicio externo y de internet.
- Credenciales que custodiar y un proyecto de Firebase que mantener.

### Queda prohibido a partir de ahora

- Enviar datos clínicos en el texto de un push.
- Llamar a Firebase desde un módulo de negocio sin pasar por el adaptador.
- Guardar las credenciales de Firebase en el repositorio.

## Cómo se hace cumplir

| Mecanismo | Qué garantiza | Estado |
|---|---|---|
| Prueba con el adaptador de push caído | Que la notificación queda en la bandeja | Fase 8 |
| Prueba del texto del push | Que no incluye datos clínicos | Fase 8 |
| Revisión de PR | Que ningún módulo llama a Firebase directamente | Activo |

## Referencias

- `docs/Decisiones_tecnicas.md` §9.
- `docs/requerimientos/10_NOT_notificaciones.md`.
- `docs/Despliegue_y_operacion.md` §10 y §11.
