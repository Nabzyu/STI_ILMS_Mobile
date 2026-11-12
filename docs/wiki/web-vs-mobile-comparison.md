---
title: Web system vs mobile app
tags: [comparison, api, gaps]
---

# Web system vs mobile app

Back to [index](README.md). Related: [Networking and API](networking-and-api.md), [Authentication and session](authentication-and-session.md), [Known gaps](known-gaps.md).

Compares this app with the web system it talks to, **JohnBazty/StiOrmocLibrary** (Express + TypeScript API in `apps/api`, React web app in `apps/web`, Supabase Postgres), at commit `75b4032` (2026-11-12). The comparison comes from reading the server code.

**Update 2026-11-12:** sections 4 and 5 were re-checked against web commit `2dacbde` (only a cron timezone fix since `75b4032`, no route changes) and app branch `feat/october-features` @ `82e18b5`, which holds PR #1 plus the October feature work and is not merged to `main` yet. On the live server, all 12 auth routes the app calls answer (none 404), and activation is open. The sign-in steps that need a real account were not tested.

Server references are paths in the web repo; mobile references are in this repo.

## Verdict

The mobile app was written against an older version of the server's `/api/v1` contract. Most data endpoints still match field for field, but the **sign-in and registration flows have moved on**, and those breaks block the app before any data screen loads.

## 1. Blockers — the app cannot get past sign-in

> **Status (branch `claude/fix-sign-in-flow`):** items marked ✅ (B1, B2, B4, M1, M2, M4) are fixed in the app. B5 (⏸) is deliberately not shipped: librarian and desk-staff accounts use the website for now, and the full support is kept on branch `archive/desk-staff-support`. The rows below still describe the original problem; [Authentication and session](authentication-and-session.md) describes the new behaviour. None of it has been tested against the live server yet. B3 and M5 are fixed on branch `feat/activate-account` (roster "Activate account" flow). M3 is now handled as a normal sign-out; M6–M8 are unchanged.

| # | Server now | Mobile does | Effect |
|---|---|---|---|
| B1 ✅ | **Students and faculty always get an emailed 6-digit login code.** `POST /api/v1/auth/login` returns `{ otpRequired, challengeId, emailHint, expiresIn: 600, resendAfter: 60 }` with **no token**. The token comes from `POST /api/v1/auth/login/verify` with `{ challenge_id, code }` (`apps/api/src/modules/auth/login-security.service.ts` lines 152–178, `jwt-auth.routes.ts` line 132). | Expects `data.token` straight from `/login`; throws "The server returned an invalid access token." (`data/LibraryRepository.kt` `authenticatePortal`). | **No student or faculty can sign in.** Their code email is still sent each attempt. |
| B2 ✅ | **Sessions expire after 10 minutes without activity.** Every `/api/v1` request checks `library_login_sessions.last_activity_at` (`jwt-auth.middleware.ts` lines 57–65). Only `POST /api/v1/auth/activity` refreshes it, and it returns a renewed token. The web app calls it from `apps/web/src/features/auth/SessionActivity.tsx`. | Never calls `/auth/activity`. | Even with B1 fixed, every request fails with `401 SESSION_EXPIRED` 10 minutes after login. The app doesn't clear its session for that code, so it keeps failing until the user restarts. |
| B3 ✅ | **The old registration endpoint is retired.** `POST /api/v1/auth/register` always returns `410 REGISTRATION_FLOW_REPLACED` (`jwt-auth.routes.ts` line 119). Signup now goes through roster registration: `GET /auth/roster-registration`, then `POST /auth/roster-registration/{begin,verify,complete}`. | `RegisterScreen` posts to `/auth/register`. | Registration always fails. |
| B4 ✅ | **School IDs are 3–50 letters, digits, `.`, `_` or `-`** (`account-auth.validation.ts` line 7). The combined Librarian/Admin login is `ADMIN-PORTAL-001` (web `README.MD`). | Login and register screens require **exactly 11 digits**. | **The librarian cannot sign in from the app.** The form rejects the ID before sending it. |
| B5 ⏸ | Staff-portal roles are `Admin`, `Librarian` and **`Staff`** (`jwt-auth.service.ts` line 15). | `UserType.fromServerRole("Staff")` → `STUDENT`, and the staff portal then rejects it with `ADMIN_ROLE_REQUIRED`. | Desk staff accounts cannot sign in. |

## 2. Contract mismatches that cause wrong behaviour

| # | Topic | Server | Mobile | Effect |
|---|---|---|---|---|
| M1 ✅ | Lockout code | Wrong passwords → `401 INVALID_CREDENTIALS` ("Wrong credentials." plus remaining-attempt count). After 8 in 10 minutes → `429 LOGIN_PAUSED`. Also `LOGIN_CODE_WAIT`, `LOGIN_EMAIL_MISSING`, `LOGIN_CHANGED` (`login-security.service.ts`). | Knows `TOO_MANY_LOGIN_ATTEMPTS` only. | Lockout messages are only shown when the server's own text passes the app's filter. The portal fallback doesn't treat `LOGIN_PAUSED` as final. |
| M2 ✅ | Attempt budget | Each failed portal check counts as a wrong attempt (role/portal mismatch also throws `INVALID_CREDENTIALS`, `jwt-auth.service.ts` line 94). | Tries the `user` portal, then the `staff` portal, on each sign-in. | One wrong password from the app uses **2 of the 8 attempts**. A librarian's first sign-in on a device also burns one. |
| M3 | Single session | Every successful login revokes the account's other sessions (`library_login_sessions … revoked_at=NOW()`, `login-security.service.ts` ~line 84). | Assumes its token stays valid. | Signing in on the web logs the phone out, and the reverse. The app sees `ACCOUNT_ACCESS_REVOKED` or `SESSION_EXPIRED`. |
| M4 ✅ | Session error codes | Also sends `SESSION_EXPIRED`, `SESSION_UPGRADE_REQUIRED`, `JWT_ROLE_FORBIDDEN`. | Clears the session only for `ACCOUNT_DEACTIVATED`, `ACCOUNT_ACCESS_REVOKED`, `JWT_REQUIRED`, `JWT_INVALID`. | Expired sessions aren't cleared. The user is stuck on error banners until relaunch. |
| M5 ✅ | School email domain | `@ormoc.sti.edu.ph` (`account-auth.validation.ts` line 8). | Register form requires `@stu.sti.edu.ph`. | Would block registration even after B3 is fixed. |
| M6 | Borrow limit | Set by the librarian's borrowing policy (`student_max_active_books`, `faculty_max_active_books`), counting active loans. Exposed via `GET /api/v1/borrow/cart-state`. | Hard-coded: students 2 in the cart, faculty unlimited (`LibraryRepository.cartLimit()`). Ignores current loans. Since `feat/october-features` the app calls `cart-state`, but only to drop titles already requested or borrowed. | The app can let a student build a cart the server will refuse, or block one the policy allows. |
| M7 | Device origin check | With `LOGIN_DEVICES_ENABLED=true`, `/auth/login` rejects requests whose `Origin` / `Sec-Fetch-Site` isn't the website (`403 DEVICE_ORIGIN`). | Native app; sends neither header. | Probably fine today: a missing header passes. Worth confirming the deployed flag and keeping in mind if the check gets stricter. |
| M8 | Page size | Catalogue `limit` is capped at **100**, default 24 (`book-catalog.validation.ts` line 53). | Sends 50. | Fine now. No paging in the app (already in [Known gaps](known-gaps.md)). |

## 3. Data endpoints — these still line up

The app's other calls exist on the server with matching paths, methods and role guards (`apps/api/src/app.js` lines 101–155). The response field names the app reads are present in the server code (e.g. print requests use `request_id`, `file_name`, `job_status`, `calculated_cost`):

`GET /auth/me` · `GET /catalog/books` · `GET /catalog/books/{id}` · `POST /catalog/books/{id}/reservations` · `GET /catalog/research` · `GET /borrowing/history` · `POST /borrow/submit-request` · `GET /reservations` · `PUT /reservations/{id}/cancel` · `GET /fines/me` · `GET /notifications` · `GET /printing/requests` · `GET /attendance/pass` · `GET /attendance/pass.png`

The field match was checked by name only, not against a recorded live response.

## 4. Features the server supports but the app lacks

State on `feat/october-features` @ `82e18b5`. The October work closed most of the earlier list: printing submit (quote, multipart upload, cancel, receipts with PDF), server read-state and delete for notifications, server clearance (`/clearance/me`, lost-book report), `/dashboard` summary (occupancy, announcements), My Books, `cart-state`, `reservations/request`, fines receipts with PDF, profile and avatar, floor plan and book location, and password recovery. The return-request screen was removed.

Still missing:

| Server has | Web uses it in | App today | Note |
|---|---|---|---|
| `GET /borrowing/in-library` | `features/circulation/InLibraryHistory.tsx` | Nothing | In-library (on-site) use history. |
| `GET /printing/requests/{id}/invoice[/pdf]` | `features/printing/PrintingInvoice.tsx` | Receipts only | Invoice download for a print request. |
| `GET /catalog/categories` | `features/catalog/CategoryFilterSearchBar.tsx` | Builds categories from the books already loaded (`BookCatalogScreen.kt`) | With Load more paging, categories on unloaded pages are missing from the filter. |
| `GET /catalog/research/{id}` | research detail | Looks the item up in the loaded list | An item not yet loaded cannot open. |
| Borrowing-policy limits in `GET /borrow/cart-state` | cart | Hard-coded limit | See M6. |
| Full librarian/staff API under `/api/v1/admin/*` | ~35 librarian, staff and admin routes | Staff home placeholders | Out of the first release (web team decision below). |

## 5. Screen coverage side by side

Web routes come from `apps/web/src/App.tsx` (student and faculty portals are almost identical; printing is student-only in both). Mobile column is `feat/october-features`.

| Web (student / faculty) | Mobile |
|---|---|
| dashboard | overview from `/dashboard`, with occupancy and announcements (faculty overview scrolls, pull-to-refresh) |
| catalog, library | book catalogue with list/grid and Load more, detail with physical copies and barcode |
| research | research catalogue with list/grid and Load more, detail |
| cart | book cart, drops titles already requested or borrowed |
| borrowing | borrowing history with Load more |
| my-books | My Books |
| reservations | reservations (request, cancel) |
| attendance | My QR attendance |
| printing (student) | printing: quote, upload, cancel, history, receipts with PDF (no invoices) |
| fines | fines, receipts with PDF |
| clearance | clearance from `/clearance/me`, deadline, lost-book report |
| notifications | notifications: mark read, mark all read, delete, library hours and closures |
| profile | My profile with avatar |
| floor-plan | floor plan and book location |
| in-library history (part of borrowing) | — |
| forgot-password, register | forgot password, Activate account (roster) |
| librarian / staff / admin portals (~35 routes) | staff home placeholder |

## Suggested order (for discussion)

1. ✅ **Sign-in:** add the emailed-code step (B1), keep the session alive or re-login on `SESSION_EXPIRED` (B2, M4), relax the ID rule (B4), map `Staff` (B5), and handle `LOGIN_PAUSED` without double-counting attempts (M1, M2). Nothing else is testable until this works.
2. ✅ **Registration:** move to roster registration with the `@ormoc.sti.edu.ph` domain (B3, M5), or hand users to the website.
3. ✅ **Portal parity** (October work): printing, notifications, clearance, dashboard, My Books, profile, floor plan. Merge `feat/october-features` to `main`.
4. **Remaining gaps:** borrow limits from `cart-state` (M6), server categories, research detail by id, print invoices, in-library history.
5. **Live test** with a real student and faculty account: sign-in code, idle expiry, single session (M3), and the October screens.

## Web team decisions (2026-11-12)

- **Native use of `/auth/login/verify` and `/auth/activity`:** allowed. The server routes get a comment saying the Android app depends on them.
- **`LOGIN_DEVICES_ENABLED`:** stays off. The app cannot complete the device-switch step or send the browser device cookie.
- **Bare 401s from `/auth/activity`:** will carry `SESSION_EXPIRED` (and `SESSION_UPGRADE_REQUIRED` for the no-session-id case). The app works with or without the codes.
- **Separate code for wrong-portal sign-in:** no, leave as is. The server answers `INVALID_CREDENTIALS` and counts the attempt; the app relies on the remembered portal and the librarian switch.
- **Librarians and library staff:** not in the first app release (the staff side of the app is placeholders). Librarian sign-in is parked on `archive/librarian-sign-in`, desk staff on `archive/desk-staff-support`.
- **Stay signed in on the phone:** to be a server refresh token for students and faculty only (design pending).
