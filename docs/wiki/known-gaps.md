---
title: Known gaps
tags: [gaps, todo]
---

# Known gaps

Back to [index](README.md). Things found while reading the code that a new contributor should know. Each one is a fact about the current code, not a decided fix. For mismatches with the server, see [Web system vs mobile app](web-vs-mobile-comparison.md); it lists the features the app still lacks compared with the web.

Updated 2026-11-12 for branch `feat/october-features`: print submit, mark-as-read, the faculty overview, drawer search and catalogue/history paging are now real, and the local-only return-request screen was removed. Branch `feat/web-parity-gaps` then closed the detail-by-id, category filter and hard-coded cart limit items.

## Features that look done but are local only

- **Staff home** — all three counter tools are "Soon" placeholders.

## Behaviour to be aware of

- **Refresh stops at the first failure** — `refreshStudentData()` is sequential. Receipts, schedule and dashboard summary are wrapped in `runCatching`, but if books, research, history, reservations, fines, notifications, print history or the attendance pass fail, every later list stays empty and the error banner shows.
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
