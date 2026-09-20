# Localización (i18n)

## Objetivo

Que cualquier texto que ve una persona usuaria pueda cambiar de idioma sin tocar código Kotlin, y que añadir un idioma nuevo sea añadir un fichero, no recorrer pantallas.

## Decisión vigente

- Todo texto de interfaz vive en `src/commonMain/composeResources/values*/strings.xml` del módulo `:feature:<nombre>:ui` (o `:shared:ui`) que lo pinta. Ningún literal de interfaz se escribe en Kotlin.
- `values/strings.xml` es **inglés** y actúa de fallback: un dispositivo en un idioma sin traducción (francés, alemán…) verá inglés. `values-es/strings.xml` es el español. Los dos ficheros contienen exactamente las mismas claves.
- Las claves llevan el prefijo de su feature (`login_`, `settings_`, `restaurant_home_`, `nav_`…) para que no colisionen al leerlas y para localizar de un vistazo a qué pantalla pertenecen.
- El plural se resuelve con `<plurals>` y `pluralStringResource`, nunca concatenando una `s` ni con un `if (count == 1)`.
- Los valores interpolados usan posicionales (`%1$s`, `%1$d`), de modo que una traducción pueda reordenarlos.

## Textos que produce un ViewModel

Un ViewModel no construye cadenas de interfaz: **expone un tipo, no un mensaje**. Los errores de repositorio se mapean a un `enum` (o `sealed interface` cuando el error lleva parámetros, como el día de la semana en `SettingsError.InvalidOpeningTime`) que vive en el módulo `ui`, y un fichero `Labels.kt` junto a los composables lo traduce a `Res.string.*`:

```kotlin
// LoginViewModel.kt — sin texto
private fun RepositoryError.toLoginError(): LoginError = when (this) {
    RepositoryError.Offline -> LoginError.OFFLINE
    RepositoryError.Unauthenticated -> LoginError.UNAUTHENTICATED
    // ...
}

// components/Labels.kt — el único sitio que conoce el texto
@Composable
internal fun LoginError.label(): String = stringResource(
    when (this) {
        LoginError.OFFLINE -> Res.string.login_error_offline
        LoginError.UNAUTHENTICATED -> Res.string.login_error_session
        // ...
    },
)
```

Esto mantiene los tests de ViewModel independientes del idioma (`assertEquals(LoginError.OFFLINE, state.error)` en vez de comparar una frase) y evita que `commonMain` sin Compose tenga que resolver recursos.

Lo mismo aplica a cualquier otro texto derivado de estado: `RestaurantCardUiState.ratingLabel` es `null` cuando no hay valoraciones y la UI decide qué pintar; `LastActivityReviewUiState.type` es un `LastActivityTargetType`, no `"Plato"`.

## Qué **no** se traduce

- Datos de negocio que llegan del backend (nombre del restaurante, descripción de un plato, comentario de una reseña).
- Cadenas de terceros ya localizadas por su SDK: los mensajes de error, títulos de producto y precios de RevenueCat se muestran tal cual.
- Fixtures y datos de `@Preview` (`FakeShareatData`, `MockRestaurants`, `RestaurantPreviewData`): son contenido de ejemplo, no interfaz.
- Endónimos de un selector de idioma (`English (US)`, `Español`, `Français`), que se escriben siempre en su propio idioma.
- Nombres de marca y de producto (`Shareat`, `Shareat Unlimited`), aunque vivan en `strings.xml` con el mismo valor en los dos idiomas para que no queden literales sueltos.

## Selector de idioma en Ajustes

Ajustes (cliente y restaurante) ofrece **Idioma del sistema / English / Español**. La elección es una
preferencia del dispositivo, no del perfil: se guarda en almacenamiento local, no en la cuenta.

- `AppLanguage` y `AppLanguageRepository` viven en `:shared:domain`; `:shared:data` implementa la
  persistencia (`AppLanguageStorage`) y la aplicación al locale de la plataforma (`AppLanguageApplier`).
- `:feature:settings:ui` sólo ve los casos de uso `ObserveAppLanguageUseCase`,
  `GetAppLanguageSupportUseCase` y `SelectAppLanguageUseCase`.
- Construir el repositorio vuelve a aplicar la elección guardada, de modo que el locale ya es
  correcto cuando el primer composable resuelve un recurso.

### Por qué hay un `key(appLanguage)` en `App()`

Compose Multiplatform 1.11.1 **no** permite redirigir `stringResource` a un idioma elegido:
`LocalComposeEnvironment` es `internal` en la librería y `Locale.current` es un valor estático de
plataforma, no un `CompositionLocal`. La única palanca es cambiar el locale de la plataforma y
reconstruir el árbol, que es lo que hace `key(appLanguage) { AppContent(...) }`. El estado de
navegación se iza *por encima* de ese `key` para que cambiar de idioma no vacíe la pila de vuelta.

`AppLanguageSelectionSupport` describe qué puede hacer cada plataforma, y la UI muestra la nota
correspondiente bajo el selector:

| Plataforma | Soporte | Mecanismo |
| --- | --- | --- |
| Android | `IMMEDIATE` | `LocaleList.setDefault(...)`; los recursos resuelven contra el locale del proceso, así que no hace falta recrear la Activity |
| iOS | `NEXT_LAUNCH` | se escribe `AppleLanguages` en `NSUserDefaults`; `NSLocale.preferredLanguages` se resuelve una vez por proceso |
| Web | `UNSUPPORTED` | `Locale.current` lee `navigator.languages` y una página no puede sobrescribirlo; la fila queda deshabilitada |

Si una futura versión de Compose Multiplatform publica una API para proveer el `ResourceEnvironment`,
esta pieza se simplifica a un `CompositionLocal` y el `key` desaparece.

## Añadir un idioma

1. Copiar `values/strings.xml` a `values-<código>/strings.xml` en cada módulo que tenga textos.
2. Traducir los valores sin tocar las claves.
3. Compose Multiplatform resuelve el idioma del dispositivo automáticamente; no hay que registrar el idioma en ningún sitio.

## Reglas revisables en PR

- Ningún `Text("…")`, `label = "…"`, `placeholder = "…"` ni `contentDescription = "…"` con literal en un composable de producción.
- Ningún ViewModel, mapper ni ui state con un `String` de interfaz: se expone el tipo y `Labels.kt` lo traduce.
- Una clave nueva se añade a `values/` **y** a `values-es/` en el mismo commit; un fichero con más claves que el otro es un fallo de revisión.
- Los plurales usan `<plurals>`; las interpolaciones usan posicionales.
- Un módulo que estrena `composeResources` añade `implementation(libs.compose.components.resources)` y `androidResources { enable = true }` a su `build.gradle.kts`.

## Estrategia de pruebas

Los tests de ViewModel comparan el tipo de error (`assertEquals(HomeError.OFFLINE, content.error)`), nunca el texto. `TopLevelNavigationItemsTest` compara `StringResource` en vez de etiquetas, de modo que la prueba no se rompe al traducir.

## Pendiente

- No hay comprobación automática de que `values/` y `values-es/` tengan las mismas claves; hoy es una regla de revisión.
