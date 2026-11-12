# Handover: stay signed in + fingerprint unlock

Date: 2026-11-12. Branch: `feat/stay-signed-in` (built on `feat/password-reset`, which is built on `claude/fix-sign-in-flow`). Nothing is merged to `main`; no pull requests are open.

## Decisions from the web team (the user is on the web team)

- Students and faculty only for "stay signed in". Librarians and library desk staff can't sign in to the app in the first release (librarian sign-in parked on `archive/librarian-sign-in`, desk staff on `archive/desk-staff-support`; merged in from `feat/password-reset`).
- Refresh key lifetime: **30 days** from sign-in, not extended by use.
- **One website session and one phone session per account** (a website sign-in must not sign the phone out).
- `LOGIN_DEVICES_ENABLED` stays off. Wrong-portal sign-in stays as it is (counts as a wrong password).
- **Fingerprint unlock: built** (design confirmed 2026-11-12: `androidx.biometric` 1.1.0, ask once after sign-in, lock on launch and after 1 minute away, PIN fallback). Hardware-bound refresh key: **not now** (follow-up).
- Do not push to the server repo (JohnBazty/stiormoclibrary) without the user's explicit permission. Hand over patches instead.

## Done in this branch (app)

- `network/SessionRefreshInterceptor.kt` + tests: refresh on `SESSION_EXPIRED`/`JWT_INVALID`, single-flight, retry once.
- `SessionStore` stores the refresh key (encrypted prefs); `ApiClient.requestRefresh` calls `/auth/refresh`.
- Sign-in sends `client: "mobile"`; repository saves the key, skips the local idle sign-out when a key exists, calls `/auth/logout` on sign-out, and opens offline with the saved identity when a key exists.
- 35 JVM unit tests pass (run outside Gradle in the original session; the Android SDK was not available there).

## Server patches (not applied anywhere yet)

`docs/plans/server-patches/` holds `git format-patch` output against StiOrmocLibrary `main` at `75b4032`:

1. `0001-…activity-401…`: `SESSION_EXPIRED` / `SESSION_UPGRADE_REQUIRED` codes on `/auth/activity` 401s, plus "used by the Android app" comments.
2. `0002-…mobile-refresh-sessions…`: migration `037_mobile_sessions.sql`, per-client session revocation (no `auth_version` bump on a normal sign-in; restoration still bumps), 30-day rotating refresh key with reuse detection, `/auth/refresh`, `/auth/logout`, schema-readiness columns, `login-security.service.test.ts`.

Verified there: `tsc --noEmit` clean, 430/430 API tests pass, and deliberate bugs in the new logic are caught by the tests. Apply with `git am docs/plans/server-patches/*.patch` in a clone of the server repo. The migration must run before the API deploys (schema readiness now requires the new columns).

## Fingerprint unlock (done in this branch, second session)

- `session/AppLock.kt`: when to lock (start once per process, after ≥ 1 minute in the background, never after a fresh password sign-in); `AppLock.shouldOffer`.
- `auth/BiometricUnlock.kt`: `BiometricPrompt` glue, authenticator choice per Android version, error messages.
- `auth/AppLockScreen.kt` + `auth/AppLockGate.kt`: lock screen over the nav graph, auto prompt, "Sign in with password instead" (signs out), "Use fingerprint to unlock?" dialog.
- `SessionStore.appLockChoice` / `setAppLockChoice` (plain prefs `smartlib_app_lock`, per school ID, survives sign-out). `LibraryRepository`: `isAppLocked`, `appLockOfferPending`, `isAppLockTurnedOn`, `onAppBackground`/`onAppForeground`, `unlockApp`, `setAppLockEnabled`.
- `MainActivity` is now a `FragmentActivity`. Side menu has a "Fingerprint unlock On/Off" row.
- Previews: `AppLockScreenPreview`, `…DarkPreview`, `…ErrorPreview` in `ui/preview/ScreenPreviews.kt`.
- New dependency: `androidx.biometric:biometric:1.1.0` (brings `androidx.fragment` 1.2.5 and `appcompat` 1.2.0; adds `USE_BIOMETRIC`/`USE_FINGERPRINT` permissions via manifest merge).

## Verified (second session)

- Android SDK installed (platform 36, build-tools 36.0.0). `./gradlew :app:testDebugUnitTest :app:assembleDebug` passes: **55/55** JVM tests (37 existing, no fixes needed; 18 new in `AppLockTest`, `BiometricUnlockTest`), debug APK builds. After merging `feat/password-reset` (students and faculty only): **56/56**, APK builds. Note: `gradlew` is committed without the executable bit; run it with `sh ./gradlew` or `chmod +x`.

## Not verified yet

- **The lock screen has not been seen on a screen.** No emulator here (no hardware virtualisation), and Robolectric rendering was blocked (large Maven files redirect to a mirror the network blocks). Render the three previews, or run on a phone/emulator with the `android-visual-verification` skill.
- **Fingerprint prompt on a real device**: auto prompt on launch and on return after 1 minute; PIN fallback on Android 8–10 (system PIN screen, `onStop`/`onStart` while locked); rotation while the prompt is open; old `androidx.fragment` 1.2.5 with this activity/Compose stack.
- Anything against a real server.

## Next

- Check the lock screen and prompt on a device (see "Not verified yet").
- Optional hardening (deferred): bind the refresh key to a Keystore key that requires user authentication.
- Optional: hide the app from the Recents preview while fingerprint unlock is on (`FLAG_SECURE` or `setRecentsScreenshotEnabled(false)`).
- Apply the server patches (above) so students/faculty actually get a refresh key; until then the feature never appears.
