# Inyección de dependencias con Koin

## Objetivo

Usar Koin para ensamblar implementaciones en el borde de la aplicación y permitir seleccionar repositorios mock o remotos sin cambiar dominio o UI.

## Principios

- Las entidades, use cases y repositorios de `domain` permanecen libres de Koin y de cualquier service locator: reciben sus dependencias por constructor, nunca las resuelven ellos mismos.
- `domain` puede publicar un módulo Koin propio (en un paquete `di` separado, p. ej. `org.shareat.feature.<name>.domain.di`) que enlaza sus interfaces de use case con su implementación (`factory<XxxUseCase> { XxxUseCaseImpl(...) }`). Esto mantiene a `ui` agnóstico de qué clase implementa cada use case — `ui` solo depende de la interfaz.
- `data` publica las definiciones de sus repositorios y fuentes de datos.
- `ui` publica las definiciones de sus ViewModels, e incluye (`includes(...)`) el módulo Koin de `domain` cuando ese feature expone uno, en vez de instanciar la implementación del use case directamente.
- `:shared:ui` actúa como composition root: inicia Koin una vez, combina módulos y elige el modo de datos del entorno.
- No se obtiene una dependencia globalmente desde una entidad, use case o repositorio.

## Módulos de datos

```kotlin
val fakeDataModule = module { /* deterministic repositories */ }
fun supabaseDataModule(config: SupabaseConfig) = module { /* runtime repositories */ }
```

`sharedModule` usa Supabase en Android, iOS y web. `previewSharedModule` usa los fakes y se selecciona explícitamente con `initKoin(useFakeData = true)` para demos, previews y pruebas.

## Composition root

La app decide qué módulos cargar. La selección mock/remoto debe proceder de configuración de entorno, no de condiciones repartidas por pantallas o repositorios.

```kotlin
modules(sharedModule, platformModule)
```

Cada plataforma llama a una inicialización compartida. Android aporta almacenamiento de sesión cifrado mediante Keystore; iOS aporta Keychain; web usa el almacenamiento del navegador bajo CSP restrictiva.

## Reglas de scopes

- `single`: clientes, almacenamiento, fuentes compartidas y repositorios sin estado de pantalla.
- `factory`: use cases ligeros y objetos sin identidad compartida.
- `viewModel`: ViewModels ligados al ciclo de vida soportado por Compose Multiplatform.
- Un scope adicional necesita una vida útil explícita y una prueba que demuestre su cierre.

## Sobrescrituras para pruebas

Las pruebas unitarias construyen la clase directamente con fakes. Koin se reserva para pruebas de wiring que comprueban que el grafo completo arranca y resuelve todas las dependencias.

No se debe arrancar un contenedor global de Koin para probar una regla de dominio o una transición aislada del ViewModel.

## Checklist

- [x] Todas las dependencias obligatorias entran por constructor.
- [x] Solo el composition root selecciona fake o Supabase.
- [x] `ui` incluye el módulo `di` de `domain` en vez de referenciar la clase `XxxUseCaseImpl` directamente.
- [x] Existe una prueba de resolución para los grafos de datos.
- [x] Los scopes tienen una vida útil justificada.

## Referencias

- [Koin para Compose y Compose Multiplatform](https://insert-koin.io/docs/reference/koin-compose/compose/)
- [ViewModels multiplataforma con Koin](https://insert-koin.io/docs/reference/koin-core/viewmodel/)
