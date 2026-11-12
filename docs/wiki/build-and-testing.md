---
title: Build and testing
tags: [build, testing]
---

# Build and testing

Back to [index](README.md).

## Build

- Gradle Kotlin DSL with a version catalog (`gradle/libs.versions.toml`). Single module `:app`.
- AGP 9.0.1, Kotlin 2.0.21 with the Compose compiler plugin, Compose BOM 2024.09.00.
- `compileSdk`/`targetSdk` 36, `minSdk` 26, Java 11.
- `buildConfig = true` for `API_BASE_URL` (see [Networking and API](networking-and-api.md#base-url)).
- Release: `isMinifyEnabled = false`; no signing config in the repo.

```bash
./gradlew :app:assembleDebug
```

```bash
./gradlew :app:testDebugUnitTest
```

## Main dependencies

| Area | Library |
|---|---|
| UI | Compose UI / Material 3, material-icons-extended, Lucide icons |
| Navigation | navigation-compose 2.8.9 |
| Startup | core-splashscreen |
| Network | Retrofit 2.11 + Gson converter, OkHttp 4.12 + logging interceptor |
| Async | kotlinx-coroutines-android 1.9 |
| Storage | androidx.security:security-crypto 1.1.0-alpha06 |
| Camera / scanning | CameraX 1.4 (core, camera2, lifecycle, view), ML Kit barcode-scanning 17.2 — **declared but not used in code yet** |

## Permissions

`INTERNET`, `CAMERA`. The manifest marks `android.hardware.camera` as **required**, so the Play Store would hide the app from devices without a camera even though no screen uses the camera yet.

## Tests

Unit tests only (`app/src/test/java/com/example/stismartlib/`):
- `ApiConfigTest` — base URL ends with `/` and equals the hosted web API.
- `UserTypeTest` — server-role mapping, including admin aliases and null.

No instrumented tests exist, though the Espresso / Compose UI test dependencies are declared. The repository, mapping and error sanitising have no tests.

## Visual verification

UI changes are checked by rendering previews on an emulator through `PreviewActivity`; see `.claude/skills/android-visual-verification/SKILL.md` and [UI and design system](ui-design-system.md#previews).
