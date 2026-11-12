---
title: Networking and API
tags: [network, api]
---

# Networking and API

Back to [index](README.md). Related: [Data layer](data-layer.md), [Error handling](error-handling.md).

## Base URL

`BuildConfig.API_BASE_URL`, set in `app/build.gradle.kts` for `defaultConfig`, `debug` and `release` — all three are currently `https://sti-ormoc-smart-library.vercel.app/` (the hosted web app). `network/ApiConfig.kt` just exposes it; `ApiConfigTest` asserts the value and the trailing slash.

To point at a local server, change the `buildConfigField` for `debug`. `res/xml/network_security_config.xml` already allows cleartext HTTP to `10.0.2.2` (emulator host), `localhost` and `127.0.0.1`; everything else must be HTTPS.

## Client — `network/ApiClient.kt`

Singleton `object`, built lazily:
- **OkHttp** with 30 s connect/read/write timeouts.
- **Auth interceptor:** adds `Accept: application/json` to every request and `Authorization: Bearer <token>` when a token exists — except for sign-in paths (`isAuthHandshakePath`: login, code verify/resend, password recovery, roster registration), where any `Authorization` header is removed.
- **Logging interceptor** at `BASIC` (method, URL, status, timing — no bodies or headers). It is active in release builds too.
- **Retrofit** with the Gson converter. Gson uses default field naming, so response DTOs expect **camelCase** keys unless a field has `@SerializedName`; request bodies use explicit `snake_case` names.

## Response envelope

Every JSON endpoint returns

```json
{ "success": true, "message": "...", "code": "...", "data": { }, "details": { "errors": { "field": "msg" } }, "meta": { } }
```

(`network/dto/AuthDtos.kt` → `ApiEnvelope<T>`, `ApiErrorDetails`).

`ApiClient.unwrap(response)` returns `data` only when HTTP is 2xx, `success == true` and `data != null`. Otherwise it parses the error body and throws `ApiException(message, code, fieldErrors, httpStatus)` — see [Error handling](error-handling.md). Because `data` must be non-null, an endpoint that succeeds with `data: null` is treated as a failure.

## Endpoints (`network/LibraryApi.kt`)

All paths are relative to the base URL.

| Method | Path | Used by | Returns (`data`) |
|---|---|---|---|
| POST | `api/v1/auth/login` | login | `LoginData` (token, user) |
| GET | `api/v1/auth/roster-registration` | activation open? | `RosterStatusData` |
| POST | `api/v1/auth/roster-registration/{begin,verify,complete}` | account activation | `RosterChallengeData` / `RosterGrantData` / `RosterCompleteData` |
| GET | `api/v1/auth/me` | session restore | `MeUser` |
| GET | `api/v1/catalog/books?q&page=1&limit=50` | book catalogue | `List<BookCatalogItemDto>` |
| GET | `api/v1/catalog/books/{titleId}` | *declared, not called* | `BookCatalogItemDto` |
| POST | `api/v1/catalog/books/{titleId}/reservations` | reserve from book detail | `ReserveResultDto` |
| GET | `api/v1/catalog/research?q&page=1&limit=50` | research catalogue | `List<ResearchCatalogItemDto>` |
| GET | `api/v1/borrowing/history?page=1&limit=50` | history, overview, clearance | `BorrowingHistoryDataDto` |
| POST | `api/v1/borrow/submit-request` | cart checkout (`{title_ids}`) | `BorrowRequestReceiptDto` |
| GET | `api/v1/reservations` | reservations | `List<ReservationItemDto>` |
| PUT | `api/v1/reservations/{reservationId}/cancel` | cancel reservation | any |
| GET | `api/v1/fines/me?status=all&period=all&page=1&limit=50` | fines, clearance | `FineListDto` |
| GET | `api/v1/notifications?limit=100` | notifications, badges | `NotificationListDto` |
| GET | `api/v1/printing/requests` | print history | `List<PrintRequestDto>` |
| GET | `api/v1/attendance/pass` | attendance pass | `AttendancePassDto` |
| GET | `api/v1/attendance/pass.png` | QR image (raw bytes, no envelope) | PNG |

Paging: every list call asks for page 1 with a fixed limit (50 or 100). There is no "load more", so accounts with more items than the limit see only the first page.

DTOs are in `network/dto/LibraryDtos.kt` and `AuthDtos.kt`; every field is nullable and defaulted, and the repository fills in fallbacks when mapping. See [Data layer](data-layer.md#mapping).
