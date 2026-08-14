# Documentación técnica de Shareat

Esta carpeta es la fuente de verdad para explicar las decisiones técnicas y cómo aplicarlas. Los tickets y ADR deciden; los `README.md` de esta carpeta convierten esas decisiones en convenciones y ejemplos reutilizables por el equipo.

## Guías

| Área | Documento | Contenido |
| --- | --- | --- |
| Producto | [Definición del producto](./product-definition.md) | Alcance, reglas y flujos del MVP |
| Arquitectura | [Arquitectura por feature](./architecture/README.md) | Módulos `data`, `domain` y `ui`, dependencias y flujo de datos |
| Datos | [Modelo de dominio](./data-model/README.md) | Entidades, relaciones y contratos de repositorio del MVP |
| Navegación | [Navegación](./navigation/README.md) | Contratos por pantalla, `NavKey` y entry builders |
| Inyección | [Koin](./dependency-injection/README.md) | Composition root, módulos y selección de repositorios |
| Calidad | [Pruebas y mocks](./testing/README.md) | Repositorios mock y pruebas de repositorios, use cases y ViewModels |

## Convención para nuevas guías

Cada tema técnico tendrá su propia carpeta `docs/<tema>/README.md`. Una guía debe incluir, como mínimo:

1. objetivo y alcance;
2. decisión vigente y responsabilidades por módulo;
3. patrón de implementación con un ejemplo pequeño;
4. reglas que se puedan revisar en una pull request;
5. estrategia de pruebas;
6. decisiones pendientes y enlaces a tickets o ADR relacionados.

Las guías son documentación viva. Cualquier cambio de arquitectura debe actualizar el ADR o ticket que toma la decisión y el `README.md` afectado en la misma entrega.
