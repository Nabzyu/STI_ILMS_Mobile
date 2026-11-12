---
title: STI ILMS Mobile — Wiki
tags: [index]
---

# STI ILMS Mobile — Wiki

Android client for the **STI College Ormoc Integrated Library Management System**. Students, faculty and librarians sign in with their school ID; students and faculty get a library portal (catalogue, borrowing, reservations, fines, attendance QR, printing), librarians get a placeholder staff home. All data comes from the hosted web API at `https://sti-ormoc-smart-library.vercel.app/`.

Written from the source on branch `claude/trusting-carson-rryhyc` (Nov 2026). Links are relative markdown so the pages work in Obsidian, on GitHub and in an IDE.

## Map of content

| Page | What it covers |
|---|---|
| [Architecture](architecture.md) | Layers, packages, how the pieces depend on each other |
| [Startup and navigation](startup-and-navigation.md) | Cold start, splash, the two nav graphs, routes |
| [Authentication and session](authentication-and-session.md) | Login with portal switch, registration, token storage, 30-day stay signed in, fingerprint unlock, logout |
| [Networking and API](networking-and-api.md) | Retrofit client, interceptors, envelope unwrapping, endpoint table |
| [Data layer](data-layer.md) | `LibraryRepository`, domain models, DTO→model mapping, cart rules |
| [Screens](screens.md) | Every screen, who sees it, what it reads and does |
| [UI and design system](ui-design-system.md) | Theme, colour tokens, shared components, previews |
| [Error handling](error-handling.md) | `ApiException`, message sanitising, user-facing copy |
| [Build and testing](build-and-testing.md) | Gradle setup, dependencies, tests, visual verification |
| [Known gaps](known-gaps.md) | Features that are UI-only, unused dependencies, sharp edges |
| [Web system vs mobile app](web-vs-mobile-comparison.md) | Where the app's API use has drifted from the StiOrmocLibrary server |

## Quick facts

- **Language / UI:** Kotlin, Jetpack Compose, Material 3, Navigation Compose, Lucide icons.
- **Module:** single `:app` module, `applicationId` / namespace `com.example.stismartlib`.
- **SDK:** minSdk 26, target/compile 36, Java 11 bytecode.
- **No DI framework, no ViewModels.** One `LibraryRepository` instance, created in the `Application`, exposed to Compose through a `CompositionLocal`. Screens read Compose state on the repository directly.
- **Roles:** `UserType.STUDENT`, `FACULTY`, `ADMIN` (shown as "Librarian").
- **Other docs:** [UI/UX improvement plan](../plans/2026-10-12-ui-ux-improvements.md).
