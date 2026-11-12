---
title: Known gaps
tags: [gaps, todo]
---

# Known gaps

Back to [index](README.md). Things found while reading the code that a new contributor should know. Each one is a fact about the current code, not a decided fix.

## Features that look done but are local only

- **Print request submit** (`dashboard/printing/PrintingServiceScreen.kt`) — sets a local `submitted` flag; there is no create-print-request endpoint in `LibraryApi`.
- **Return request** (`dashboard/returnbook/ReturnBookScreen.kt`) — confirm dialog then a success dialog; nothing is sent to the server.
- **Mark all notifications as read** (`dashboard/notifications/NotificationsScreen.kt`) — edits the in-memory list; the next refresh restores unread state.
- **Staff home** — all three counter tools are "Soon" placeholders.
- **Faculty overview** — a welcome card only, unlike the full student overview.
- **Drawer search button** — no-op ("search reserved for future").

## Behaviour to be aware of

- **Refresh stops at the first failure** — `refreshStudentData()` is sequential, so if any endpoint fails (for example, if printing or attendance were unavailable for a faculty account) every later list stays empty and sets the error banner.
- **First page only** — every list asks for page 1 (limit 50/100) with no paging UI.
- **Detail screens need the list** — book/research detail look up the loaded list instead of calling `GET catalog/books/{titleId}`.
- **Login ID rule** — the form requires an 11-digit numeric ID for everyone, including librarians; if staff IDs differ, they cannot sign in from the app.
- **401 without a code** is mapped to `INVALID_CREDENTIALS` ("Invalid student ID or password.") on any endpoint and does not clear the session. Correct behaviour depends on the server sending `JWT_INVALID`/`JWT_REQUIRED` codes.
- **Session cleared but UI stays** — when `unwrap` clears the stored session on a JWT/deactivation code, the in-memory session and current screen remain until navigation or restart.
- **Theme choice is not persisted** across app restarts.
- **Messages and Notifications top-bar icons** go to the same screen and show the same count.
- `QrLoginScreen` exists but is only used by previews.

## Security notes

- Fallback to **unencrypted** shared preferences for the JWT when `EncryptedSharedPreferences` cannot be created (logged as a warning).
- `android:allowBackup="true"` and `res/xml/backup_rules.xml` / `data_extraction_rules.xml` are still the Android Studio templates, so shared preferences (session files and login hints) are eligible for backup. The encrypted file is useless without the device's Keystore key, but the plain fallback file and login-hint file would restore as-is.
- HTTP logging at `BASIC` level is on in release builds (URLs and status codes in logcat, no bodies or tokens).
- `security-crypto` is an alpha version.

## Build / dependency notes

- CameraX and ML Kit barcode scanning are dependencies but unused; camera is declared as a required hardware feature.
- No instrumented tests; repository and mapping logic are untested.
- Source directories don't match package names (see [Architecture](architecture.md#source-layout)).
