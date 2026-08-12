# Arquitectura por feature

## Objetivo

Permitir que varias features evolucionen en paralelo y que la fuente de datos pueda cambiar de mocks al backend real sin modificar casos de uso, ViewModels o pantallas.

## Estructura

Cada nueva feature se divide en tres módulos Gradle:

```text
:feature:<nombre>:domain
:feature:<nombre>:data
:feature:<nombre>:ui
```

El módulo de composición de la aplicación es `:shared` en la estructura actual. Ensambla features, navegación y dependencias para Android, iOS y web.

### `domain`

Contiene:

- entidades y value objects de dominio;
- interfaces de repositorio;
- use cases y reglas de negocio;
- errores y resultados expresados en términos de producto.

No depende de `data`, `ui`, Koin, DTO, almacenamiento ni clientes de red.

### `data`

Contiene:

- implementaciones mock y remotas de repositorios;
- fuentes de datos y fixtures;
- DTO y modelos de persistencia;
- mappers entre representaciones externas y dominio.

Depende de `domain`. Los DTO y detalles del proveedor no pueden formar parte de la API pública que consume `domain` o `ui`.

### `ui`

Contiene:

- pantallas y componentes propios de la feature;
- ViewModels, estados y efectos;
- interfaces de navegación expresadas como intenciones;

Depende de `domain`; no depende de implementaciones concretas de `data` ni del módulo de app.

Las keys, implementaciones de las interfaces y entradas Koin Navigation 3 viven en `:shared`, que conecta cada pantalla con el grafo de la aplicación.

## Dirección de dependencias

```text
                    :shared (composition root)
                       /                 \
                      v                   v
          :feature:<nombre>:ui   :feature:<nombre>:data
                      \                   /
                       v                 v
                    :feature:<nombre>:domain
```

Una dependencia entre features debe pasar por una API explícita. No se importan implementaciones internas de otra feature para reutilizar una pantalla, un repositorio o un ViewModel.

## Flujo de datos

```text
Pantalla -> ViewModel -> Use case -> Repository (interfaz)
                                      |
                                      +-> MockRepository
                                      +-> RemoteRepository (futuro)
```

`data` transforma fixtures o respuestas remotas a modelos de dominio antes de completar la llamada al repositorio. La UI transforma dominio a estado de presentación cuando lo necesita.

## Checklist de una nueva feature

- [ ] Los tres módulos están incluidos en `settings.gradle.kts`.
- [ ] `domain` no conoce frameworks ni infraestructura.
- [ ] `ui` solo depende de `domain` y librerías de presentación.
- [ ] `data` implementa interfaces definidas por `domain`.
- [ ] La app ensambla navegación y dependencias.
- [ ] Existen pruebas de repositorio, use cases y ViewModels.
- [ ] La guía afectada se actualiza si aparece un patrón nuevo.

## Trabajo relacionado

- Backend y contratos mock: [#34](https://github.com/ShareatOfficial/app/issues/34)
- Modelo de datos: [#35](https://github.com/ShareatOfficial/app/issues/35)
- Autenticación y autorización: [#33](https://github.com/ShareatOfficial/app/issues/33)
- Implementación de la base compartida: [#9](https://github.com/ShareatOfficial/app/issues/9)
