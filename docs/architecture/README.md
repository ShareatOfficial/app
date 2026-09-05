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

La base compartida también está separada en tres módulos Gradle, más dos módulos hoja adicionales: uno para contratos de navegación compartidos y otro para UI compartida:

```text
:shared:domain
:shared:data
:shared:navigation
:shared:designsystem
:shared:ui
```

`:shared:navigation` no aplica plugins Compose y no depende de ningún otro módulo del proyecto. Contiene únicamente contratos de navegación (hoy, el marcador `RequiresLogin`) que varias features y `:shared:ui` necesitan compartir sin crear una dependencia circular entre una feature y el módulo de app. Ver [Navegación](../navigation/README.md). Se mantiene deliberadamente mínimo: no debe crecer para incluir `Navigator`, `NavigationState` ni los `*NavigationImpl`/`*NavigationModule` de cada feature — esos referencian pantallas Compose concretas de cada feature y, si vivieran aquí, recrearían el mismo ciclo de dependencias que este módulo existe para evitar. Ver la sección "Decisión registrada" en [Navegación](../navigation/README.md).

`:shared:designsystem` aplica el plugin Compose pero no depende de `domain`, `data`, Koin ni de ninguna feature — solo de las librerías de Compose. Contiene efectos y componentes visuales puramente de presentación que varias features necesitan por igual (hoy, `Modifier.shimmerEffect` para skeletons de carga). Cualquier `:feature:<nombre>:ui` puede depender de él directamente, igual que ya depende de `:shared:navigation`. Ver [Estados de carga y skeletons](../ui/README.md).

`:shared:ui` es el módulo de composición de la aplicación. Ensambla features, navegación y dependencias para Android, iOS y web, y produce el framework `Shared` que consume iOS.

### `domain`

Contiene:

- entidades y value objects de dominio;
- interfaces de repositorio;
- use cases y reglas de negocio;
- errores y resultados expresados en términos de producto.

No depende de `data`, `ui`, Compose, DTO, almacenamiento ni clientes de red. `:shared:domain` aplica esta misma regla.

Única excepción sobre Koin: un módulo `domain` puede publicar su propio módulo Koin aislado en un paquete `di` que enlaza la interfaz de un use case con su implementación. Las entidades, use cases y repositorios siguen recibiendo sus dependencias por constructor y nunca resuelven nada por sí mismos. `:shared:domain` lo hace en `org.shareat.app.domain.usecase.di.sharedDomainModule`.

Un use case que necesitan varias features vive en `:shared:domain`, no en la feature que lo escribió primero: una feature nunca importa el `domain` de otra. Así, `GetRestaurantsUseCase` (listado de home) y `GetRestaurantUseCase` (detalle) comparten `RestaurantDetailsAssembler` y los mismos repositorios. Cada use case expone un único método público; dos consultas distintas son dos use cases distintos, no dos métodos en una interfaz.

### `data`

Contiene:

- implementaciones mock y remotas de repositorios;
- fuentes de datos y fixtures;
- DTO y modelos de persistencia;
- mappers entre representaciones externas y dominio.

Depende de `domain`. Los DTO y detalles del proveedor no pueden formar parte de la API pública que consume `domain` o `ui`. `data`, incluido `:shared:data`, no aplica plugins de Compose ni declara dependencias o recursos Compose.

### `ui`

Contiene:

- pantallas y componentes propios de la feature;
- ViewModels, estados y efectos;
- interfaces de navegación expresadas como intenciones;

Depende de `domain`; no depende de implementaciones concretas de `data` ni del módulo de app.

Cada feature declara sus propias `NavKey` (sus destinos) junto a su interfaz de navegación. Las implementaciones de esas interfaces y las entradas Koin Navigation 3 viven en `:shared:ui`, que importa las keys de cada feature y conecta cada pantalla con el grafo de la aplicación. Ver [Navegación](../navigation/README.md) para el detalle.

## Dirección de dependencias

```text
 Android / iOS / Web
          |
          v
    :shared:ui --------------> :shared:data
          |                          |
          |                          v
          +------------------> :shared:domain
          |
          +------------------> :feature:<nombre>:ui
                                      |
                                      v
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
- [ ] `:shared:domain` y `:shared:data` no aplican plugins Compose ni contienen imports o recursos Compose.
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
