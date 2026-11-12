---
title: Architecture
tags: [architecture]
---

# Architecture

Back to [index](README.md).

## Layers

```
MainActivity ──► AppNavGraph (top level)          navigation/
                   ├─ LoginScreen / RegisterScreen  auth/
                   ├─ StaffHomeScreen               dashboard/staff/
                   └─ DashboardScreen ──► inner NavHost ──► feature screens  dashboard/**
                                │
                 rememberLibraryRepository()        data/RepositoryProvider.kt
                                │
                        LibraryRepository           data/LibraryRepository.kt
                         │             │
                 ApiClient.api     SessionStore     network/ , session/
                 (Retrofit)        (EncryptedSharedPreferences)
                         │
              https://sti-ormoc-smart-library.vercel.app/api/v1/...
```

- **UI** — Compose screens. They hold only local UI state (`remember { mutableStateOf(...) }`) and call suspend functions on the repository from `rememberCoroutineScope()` or `LaunchedEffect`.
- **State + data** — `LibraryRepository` is both the data source and the state holder. It exposes `mutableStateOf` / `mutableStateListOf` fields (`session`, `books`, `cart`, `fines`, …) so any screen that reads them recomposes when they change. See [Data layer](data-layer.md).
- **Network** — `ApiClient` singleton (Retrofit + OkHttp + Gson) and the `LibraryApi` interface. See [Networking and API](networking-and-api.md).
- **Persistence** — `SessionStore` keeps the JWT and basic identity in encrypted shared preferences. Nothing else is cached on disk; all lists are re-fetched. See [Authentication and session](authentication-and-session.md).

## Object lifetime and wiring

`STIOrmocLibraryApp.onCreate()` (`app/src/main/java/com/example/stismartlib/STIOrmocLibraryApp.kt`):

1. creates `SessionStore(this)`
2. calls `ApiClient.initialize(sessionStore)` so the auth interceptor can read the token
3. creates the single `LibraryRepository(sessionStore)`

`ProvideLibraryRepository { }` (in `data/RepositoryProvider.kt`) puts that instance into `LocalLibraryRepository`; screens call `rememberLibraryRepository()`. Previews swap in a seeded in-memory repository through the same local (see [UI and design system](ui-design-system.md#previews)).

## Source layout

Directories do **not** match package names — e.g. `app/src/main/java/auth/LoginScreen.kt` is in package `com.example.stismartlib.ui.auth`. Search by file name, not by package path.

| Directory (`app/src/main/java/`) | Package | Contents |
|---|---|---|
| `com/example/stismartlib/` | `com.example.stismartlib` | `MainActivity`, `STIOrmocLibraryApp` |
| `com/example/stismartlib/ui/theme/` | `…ui.theme` | colours, typography, theme, theme mode |
| `navigation/` | `…navigation` | `Screen`, `AppNavGraph` |
| `auth/` | `…ui.auth` | login, register, `UserType`, `ProgramOption` |
| `dashboard/` | `…ui.dashboard.*` | dashboard shell, inner routes, all feature screens |
| `components/` | `…ui.components` | shared `Portal*` / `Elms*` components, `AppDialog` |
| `data/`, `data/models/` | `…data`, `…data.models` | repository, provider, domain models |
| `network/`, `network/dto/` | `…network`, `…network.dto` | Retrofit client, API, DTOs, `ApiException` |
| `session/` | `…session` | `SessionStore` |
| `ui/feedback`, `ui/format`, `ui/splash`, `ui/preview` | `…ui.*` | error copy, date formatting, preview splash, previews |

## Design choices worth knowing

- **Single shared repository, no ViewModels.** Simple, but state survives only as long as the process and is shared by every screen. Configuration changes are fine because the repository lives in the `Application`.
- **Server-driven data, no offline cache.** Every list is replaced wholesale on load.
- **Role decides the shell.** Staff go to `StaffHomeScreen`; everyone else to `DashboardScreen` with a role-specific overview and drawer.
