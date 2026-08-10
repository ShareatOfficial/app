# Pruebas y repositorios mock

## Objetivo

Empezar el desarrollo con datos mock realistas y proteger las fronteras de arquitectura con pruebas unitarias JUnit para repositorios, use cases y ViewModels.

## Repositorios mock

La interfaz del repositorio vive en `domain`; la implementación mock vive en `data`.

```kotlin
interface RestaurantRepository {
    suspend fun restaurants(): Result<List<Restaurant>>
}

class MockRestaurantRepository(
    private val scenario: RestaurantScenario,
) : RestaurantRepository {
    override suspend fun restaurants(): Result<List<Restaurant>> =
        scenario.result()
}
```

Los escenarios deben ser explícitos y deterministas. Como mínimo:

- éxito con datos representativos;
- resultado vacío;
- latencia controlable;
- error recuperable;
- offline;
- contenido deshabilitado o no disponible;
- datos límite definidos por el contrato.

No se usan esperas reales ni aleatoriedad en pruebas. Un scheduler, clock o fuente de escenarios se inyecta cuando sea necesario.

## Realismo de los fixtures

- Los fixtures siguen el modelo y ejemplos acordados en #35.
- DTO, IDs, timestamps, nullability, paginación y errores se actualizan con el contrato decidido en #34.
- `data` mapea los fixtures a dominio igual que lo hará la fuente remota.
- La UI nunca consume JSON, DTO ni clases específicas de Firebase, AWS u otro proveedor.

## Qué probar

### Repositorios

- mapeo de DTO/fixture a dominio;
- propagación o traducción de errores;
- comportamiento de caché y reintento cuando se definan;
- todos los escenarios mock públicos;
- el mismo contrato observable para implementación mock y real.

### Use cases

- reglas de negocio y validación;
- permisos o precondiciones del lado cliente cuando correspondan;
- combinación y transformación de repositorios;
- casos límite sin arrancar Koin ni Compose.

### ViewModels

- estado inicial;
- transiciones loading, empty, content y error;
- reintentos y prevención de acciones duplicadas;
- efectos de una sola ejecución;
- llamadas a navegación mediante interfaces fake.

## Convenciones

- Usar JUnit como runner de las pruebas unitarias de repositorios, use cases y ViewModels.
- Preferir fakes pequeños y legibles frente a mocks de interacción excesiva.
- Seguir Given/When/Then o nombres que expresen escenario y resultado.
- Evitar verificar detalles internos; probar resultados, estados y efectos observables.
- Añadir una prueba de regresión antes o junto a cada corrección de bug.

## Estructura sugerida

```text
feature/<nombre>/domain/src/commonTest/...
feature/<nombre>/data/src/commonTest/...
feature/<nombre>/ui/src/commonTest/...
```

Si una prueba necesita una API exclusiva de JVM/Android, se ubica en el source set de host correspondiente sin mover lógica multiplataforma fuera de `commonMain`.

## Trabajo relacionado

- Contrato de backend y comportamiento de mocks: [#34](https://github.com/ShareatOfficial/app/issues/34)
- Modelo y fixtures: [#35](https://github.com/ShareatOfficial/app/issues/35)
- Estados de autorización: [#33](https://github.com/ShareatOfficial/app/issues/33)
