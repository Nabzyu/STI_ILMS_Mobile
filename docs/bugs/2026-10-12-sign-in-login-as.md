# Bug Report: Mobile sign-in fails — API requires `login_as`, app sends `portal`

**Status:** Fixed in app (sends `login_as` Student/Faculty/Admin with role fallback; auth DTOs use snake_case `@SerializedName`).

## Description

STI ILMS Mobile sign-in calls `POST /api/v1/auth/login` with `school_id`, `password`, and `portal` (`"user"` / `"staff"`). The hosted API rejects that body because it requires `login_as` set to a valid role (`Student`, `Faculty`, or `Admin`). Sign-in fails validation before credentials are checked, so Student/Faculty/Librarian cannot log in against the current backend.

## Environment

- **Device**: Android emulator / physical device (any)
- **OS**: Android (app package `com.example.stismartlib`)
- **App**: STI ILMS Mobile — login via `LibraryRepository.authenticatePortal`
- **API base**: `https://sti-ormoc-smart-library.vercel.app/` (`BuildConfig.API_BASE_URL`)
- **Endpoint**: `POST /api/v1/auth/login`
- **User Role/Permissions**: Unauthenticated (sign-in screen)

## Severity

**Critical** — Blocks all sign-in against the live API; no authenticated features are reachable.

## Steps to Reproduce

1. Open the app and go to Sign in.
2. Enter any 11-digit school ID and a password.
3. Tap **Sign in**.
4. Observe the error banner / failed login.

**API reproduction (verified 2026-10-12):**

```http
POST https://sti-ormoc-smart-library.vercel.app/api/v1/auth/login
Content-Type: application/json

{"school_id":"00000000000","password":"wrong","portal":"user"}
```

## Expected Behavior

- Request includes a valid `login_as` role (and any other fields the API still accepts).
- Wrong password → `INVALID_CREDENTIALS`.
- Correct credentials → `success: true` with token + user; app stores session and navigates to Dashboard / Staff home.

## Actual Behavior

API returns HTTP 422:

```json
{
  "success": false,
  "code": "AUTH_VALIDATION_FAILED",
  "message": "Please correct the highlighted fields.",
  "details": {
    "errors": {
      "login_as": "Select a valid login role."
    }
  }
}
```

The app’s user/staff portal fallback never runs, because it only retries on `INVALID_CREDENTIALS`, not `AUTH_VALIDATION_FAILED`.

## Evidence (live API probes)

| Request body | Result |
|--------------|--------|
| `portal: "user"` only (matches app) | `AUTH_VALIDATION_FAILED` — `login_as` required |
| `login_as: "user"` or `"staff"` | Same validation failure |
| `login_as: "Student"` or `"Faculty"` | Validation OK → `INVALID_CREDENTIALS` for bad password |
| `login_as: "Admin"` | Accepted as a role (rate-limited during probing) |

## Console / Error Logs

App maps field errors only for `school_id` and `password` on the login screen, so `login_as` surfaces as a generic banner: “Please correct the highlighted fields.”

Relevant code:

- [`app/src/main/java/data/LibraryRepository.kt`](../../app/src/main/java/data/LibraryRepository.kt) — `authenticatePortal` sets `portal`, never `loginAs`
- [`app/src/main/java/network/dto/AuthDtos.kt`](../../app/src/main/java/network/dto/AuthDtos.kt) — `LoginRequest` has both `portal` and `@SerializedName("login_as") loginAs`, but `loginAs` is unused
- [`app/src/main/java/auth/LoginScreen.kt`](../../app/src/main/java/auth/LoginScreen.kt) — field error keys `school_id` / `password` only

## Additional Notes

### Secondary risk (after login starts succeeding)

`LoginUser` and `MeUser` use camelCase properties (`schoolId`, `fullName`, `accountId`) **without** `@SerializedName`. Other DTOs (register, profile) use snake_case. If the login success payload uses `school_id` / `full_name` / `account_id`, the app may still fail with “Missing account id” or blank display name after a “successful” HTTP response.

### Rate limiting

The login endpoint is rate-limited (`8-in-15min`). Failed probes can return `TOO_MANY_LOGIN_ATTEMPTS` even when validating the fix.

## Possible Fix

1. Send `login_as` with role values the API accepts: `Student`, `Faculty`, `Admin` (not `user`/`staff`).
2. Adjust unified sign-in fallback to try roles in sequence on `INVALID_CREDENTIALS` (e.g. remembered role → Student → Faculty → Admin), instead of `portal` user/staff only.
3. Keep or drop `portal` only after confirming whether the backend still reads it; today it is insufficient alone.
4. Add `@SerializedName` on `LoginUser` / `MeUser` / `LoginData` snake_case fields to match the rest of the API.
5. Optionally surface `details.errors.login_as` on the login UI during development; production users should not need to pick a role if fallback handles it.
