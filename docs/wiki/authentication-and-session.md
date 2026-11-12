---
title: Authentication and session
tags: [auth, security, session]
---

# Authentication and session

Back to [index](README.md). Related: [Networking and API](networking-and-api.md), [Error handling](error-handling.md).

## Roles

`auth/UserType.kt` maps the server's role string:

| Server role | `UserType` | Label | `isStaff` |
|---|---|---|---|
| `Admin`, `Librarian`, `System Administrator` | `ADMIN` | Librarian | yes |
| `Faculty` | `FACULTY` | Faculty | no |
| anything else / null | `STUDENT` | Student | no |

Covered by `UserTypeTest`.

## Sign in — one form, two server portals

The API has separate **user** and **staff** login portals; the app shows a single form and works out which one to use (`LibraryRepository.login`):

1. Normalise the school ID (trim, upper-case).
2. Try the portal remembered for that ID (`SessionStore.preferredLoginPortal`), default `user`.
3. If that fails with `INVALID_CREDENTIALS`, try the other portal. (Admins are rejected by the user portal, so first-time librarians fall through to `staff`.)
4. If the second attempt fails, surface its error only when it is `TOO_MANY_LOGIN_ATTEMPTS` or `ACCOUNT_DEACTIVATED`; otherwise re-throw the first error.

Each attempt (`authenticatePortal`):
- clears the stored session and in-memory data first, so no stale `Authorization` header is sent;
- `POST api/v1/auth/login` with `{school_id, password, portal}`;
- requires a non-blank `token` and an account id, else throws;
- rejects a non-staff role coming back from the `staff` portal with `ADMIN_ROLE_REQUIRED`;
- saves token + identity, sets `session`, and remembers the portal for that school ID (this hint survives logout).

Client-side validation in `LoginScreen`: school ID must be **exactly 11 digits**; password required. Server field errors (`school_id`, `password`) are shown under the fields. On success the user goes to `staff_home` or `dashboard/{type}` and the login screen is removed from the back stack.

## Registration (students only)

`RegisterScreen` → `LibraryRepository.registerStudent` → `POST api/v1/auth/register`.

Client checks: 11-digit student ID; first/last name; email matching the STI student domain (`name@stu.sti.edu.ph`); program/strand; year level; password ≥ 8 chars; confirmation matches. Program options and year levels come from `auth/ProgramOptions.kt`:
- Senior High strands STEM, ABM, TVL → Grade 11–12
- College BSIT, BSTM, BSHM → 1st–4th Year

Registration does **not** sign the user in; on success it returns to `login`. Server field errors map back onto the form.

## Session storage — `session/SessionStore.kt`

- Stores `token`, `account_id`, `school_id`, `full_name`, `role` in `EncryptedSharedPreferences` (`smartlib_session`, AES-256 GCM/SIV via `MasterKey`).
- **Fallback:** if the encrypted store cannot be created, it logs a warning and uses plain `smartlib_session_fallback` prefs. The token is then stored unencrypted on that device.
- Writes use `commit()` so the token is readable before the very next request.
- `getSession()` returns null unless token, account id ≥ 0, school ID and role are all present.
- Login-portal hints live in a separate plain prefs file `smartlib_login_hints` (school ID → `user`/`staff`).

## Restore, expiry and logout

- **Restore:** at launch, `restoreSession()` calls `/auth/me`; on success it refreshes stored identity, on any `ApiException` it clears everything and returns false.
- **Server-side invalidation:** `ApiClient.unwrap` clears the stored session when the error code is `ACCOUNT_DEACTIVATED`, `ACCOUNT_ACCESS_REVOKED`, `JWT_REQUIRED` or `JWT_INVALID`. The in-memory `session` is *not* reset at that point, so the user stays on the current screen until they navigate or restart.
- **Logout:** clears stored session, all in-memory lists and the cart; navigation pops to `login`.
- There is no token refresh flow; `expiresIn` from the login response is ignored.
