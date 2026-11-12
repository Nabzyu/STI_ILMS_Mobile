---
title: Error handling
tags: [errors]
---

# Error handling

Back to [index](README.md). Related: [Networking and API](networking-and-api.md).

## `ApiException`

`network/ApiException.kt`: `message`, `code` (default `REQUEST_FAILED`), `fieldErrors: Map<String, String>`, `httpStatus`.

Thrown by `ApiClient.unwrap` and by the repository for local problems (empty cart, missing token, invalid id).

## How `unwrap` builds the exception

1. Message: the error envelope's `message` (or the success body's), passed through `sanitizeUserMessage`, which drops anything that looks internal (contains "exception", "stack", "sql", "at com.", "traceback", `{` or `</`).
2. If nothing usable is left, a status-based default: 401 → "Invalid student ID or password.", 403 → account deactivated, 429 → too many attempts, otherwise "The request could not be completed."
3. Code: envelope `code`, else 429 → `TOO_MANY_LOGIN_ATTEMPTS`, 401 → `INVALID_CREDENTIALS`, else `REQUEST_FAILED`.
4. Field errors: `details.errors`.
5. Codes `ACCOUNT_DEACTIVATED`, `ACCOUNT_ACCESS_REVOKED`, `JWT_REQUIRED`, `JWT_INVALID` clear the stored session.

## User-facing copy — `ui/feedback/UserFacingErrors.kt`

`userFacingMessage(error, fallback)` is what screens show:

| Error | Message |
|---|---|
| `INVALID_CREDENTIALS` | Invalid student ID or password. |
| `ACCOUNT_DEACTIVATED` | Your account is currently deactivated. Please coordinate with the campus librarian. |
| `TOO_MANY_LOGIN_ATTEMPTS` | Too many login attempts. Please wait 15 minutes and try again. |
| `ADMIN_ROLE_REQUIRED` | This sign-in is for Librarian / Admin accounts only. |
| other `ApiException` | sanitised server message, else `fallback` |
| `IOException` (offline, timeout) | Unable to reach the library service. Check your internet connection and try again. |
| anything else | `fallback` ("Something went wrong. Please try again.") |

The second sanitiser is stricter than the one in `ApiClient` — it also rejects "bearer", "unauthorized", "authorization" and 5xx status numbers.

## Patterns on screens

- Forms (login, register) catch `ApiException`, show a banner and map `fieldErrors` onto fields by API key (`school_id`, `password`, `email`, …).
- List screens show `ElmsLoadingBlock` while loading an empty list, an `ElmsBanner` with retry when `lastError` is set, and `ElmsEmptyState` when the list is empty.
- One-off actions on detail screens use `Toast`.
- Background loads are wrapped in `runCatching`, so failures there are silent apart from `lastError`.
