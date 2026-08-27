# Tests and mock repositories

## Goal

Start development against realistic mock data, and protect architecture boundaries with JUnit unit tests for repositories, use cases, and ViewModels.

## Mock repositories

The repository interface lives in `domain`; the mock implementation lives in `data`.

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

Scenarios must be explicit and deterministic. At minimum, cover:

- success with representative data;
- empty result;
- controllable latency;
- recoverable error;
- offline;
- disabled/unavailable content;
- boundary data defined by the contract.

No real waiting, no randomness in tests. Inject a scheduler, clock, or scenario source when needed.

## Fixture realism

- Fixtures follow the agreed model and examples (see `data-model.md` / issue #35).
- DTOs, IDs, timestamps, nullability, pagination, and errors match the agreed contract (issue #34).
- `data` maps fixtures to domain the same way the remote source eventually will.
- The UI never consumes JSON, DTOs, or provider-specific classes (Firebase, AWS, Supabase types, etc.) directly.

## What to test, by layer

### Repositories

- fixture/DTO → domain mapping;
- error propagation or translation;
- cache/retry behavior once defined;
- every public mock scenario;
- the same observable contract for both the mock and real implementation.

### Use cases

- business rules and validation;
- client-side permissions/preconditions where relevant;
- combining/transforming repositories;
- edge cases, without starting Koin or Compose.

### ViewModels

- initial state;
- loading / empty / content / error transitions;
- retries and duplicate-action prevention;
- one-shot effects;
- navigation calls, via fake navigation interfaces (see `navigation.md`).

## Conventions

- JUnit is the runner for repository/use case/ViewModel unit tests.
- Prefer small, readable fakes over heavily-interaction-verifying mocks.
- Follow Given/When/Then or names that state the scenario and the expected result.
- Don't assert internal details — assert observable results, states, and effects.
- Add a regression test alongside (or before) each bug fix.

## Suggested layout

```
shared/data/src/commonTest/...
feature/<name>/domain/src/commonTest/...
feature/<name>/data/src/commonTest/...
feature/<name>/ui/src/commonTest/...
```

A test that needs a JVM/Android-only API goes in that host source set — don't move multiplatform logic out of `commonMain` to work around it.

## Local Supabase

```bash
npm install
npx supabase start
npx supabase db reset --local
npx supabase test db
npx supabase db lint --local --schema private,public --level warning --fail-on warning
```

`supabase/tests` holds pgTAP tests for schema, constraints, indexes, grants, RLS, and Storage. `supabase/seed.sql` is local-only and provides a public catalogue for the JVM contract test of the real repositories. To run that contract test: export `SHAREAT_SUPABASE_URL` and `SHAREAT_SUPABASE_PUBLISHABLE_KEY` from `npx supabase status -o env`, then run `./gradlew :shared:data:jvmTest`.

Seeds are never included in `db push`; never create fake accounts or restaurants on the hosted project.

## Related tickets

- Backend contract and mock behavior: [#34](https://github.com/ShareatOfficial/app/issues/34)
- Model and fixtures: [#35](https://github.com/ShareatOfficial/app/issues/35)
- Authorization states: [#33](https://github.com/ShareatOfficial/app/issues/33)
