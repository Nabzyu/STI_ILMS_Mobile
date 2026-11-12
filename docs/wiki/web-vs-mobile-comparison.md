---
title: Web system vs mobile app
tags: [comparison, api, gaps]
---

# Web system vs mobile app

Back to [index](README.md). Related: [Networking and API](networking-and-api.md), [Authentication and session](authentication-and-session.md), [Known gaps](known-gaps.md).

Compares this app with the web system it talks to, **JohnBazty/StiOrmocLibrary** (Express + TypeScript API in `apps/api`, React web app in `apps/web`, Supabase Postgres), at commit `75b4032` (2026-11-12). The comparison comes from reading the server code. The live server at `sti-ormoc-smart-library.vercel.app` could not be reached from the review environment, so nothing here was tested against production. If the deployed build is older than `main`, some of these breaks may not have reached production yet.

Server references are paths in the web repo; mobile references are in this repo.

## Verdict

The mobile app was written against an older version of the server's `/api/v1` contract. Most data endpoints still match field for field, but the **sign-in and registration flows have moved on**, and those breaks block the app before any data screen loads.

## 1. Blockers — the app cannot get past sign-in

> **Status (branch `claude/fix-sign-in-flow`):** items marked ✅ (B1, B2, B4, M1, M2, M4) are fixed in the app. B5 (⏸) is deliberately not shipped: desk-staff accounts get a "use the website" message, and the full support is kept on branch `archive/desk-staff-support`. The rows below still describe the original problem; [Authentication and session](authentication-and-session.md) describes the new behaviour. None of it has been tested against the live server yet. B3, M3 (now handled as a normal sign-out) and M5–M8 are unchanged.

| # | Server now | Mobile does | Effect |
|---|---|---|---|
| B1 ✅ | **Students and faculty always get an emailed 6-digit login code.** `POST /api/v1/auth/login` returns `{ otpRequired, challengeId, emailHint, expiresIn: 600, resendAfter: 60 }` with **no token**. The token comes from `POST /api/v1/auth/login/verify` with `{ challenge_id, code }` (`apps/api/src/modules/auth/login-security.service.ts` lines 152–178, `jwt-auth.routes.ts` line 132). | Expects `data.token` straight from `/login`; throws "The server returned an invalid access token." (`data/LibraryRepository.kt` `authenticatePortal`). | **No student or faculty can sign in.** Their code email is still sent each attempt. |
| B2 ✅ | **Sessions expire after 10 minutes without activity.** Every `/api/v1` request checks `library_login_sessions.last_activity_at` (`jwt-auth.middleware.ts` lines 57–65). Only `POST /api/v1/auth/activity` refreshes it, and it returns a renewed token. The web app calls it from `apps/web/src/features/auth/SessionActivity.tsx`. | Never calls `/auth/activity`. | Even with B1 fixed, every request fails with `401 SESSION_EXPIRED` 10 minutes after login. The app doesn't clear its session for that code, so it keeps failing until the user restarts. |
| B3 | **The old registration endpoint is retired.** `POST /api/v1/auth/register` always returns `410 REGISTRATION_FLOW_REPLACED` (`jwt-auth.routes.ts` line 119). Signup now goes through roster registration: `GET /auth/roster-registration`, then `POST /auth/roster-registration/{begin,verify,complete}`. | `RegisterScreen` posts to `/auth/register`. | Registration always fails. |
| B4 ✅ | **School IDs are 3–50 letters, digits, `.`, `_` or `-`** (`account-auth.validation.ts` line 7). The combined Librarian/Admin login is `ADMIN-PORTAL-001` (web `README.MD`). | Login and register screens require **exactly 11 digits**. | **The librarian cannot sign in from the app.** The form rejects the ID before sending it. |
| B5 ⏸ | Staff-portal roles are `Admin`, `Librarian` and **`Staff`** (`jwt-auth.service.ts` line 15). | `UserType.fromServerRole("Staff")` → `STUDENT`, and the staff portal then rejects it with `ADMIN_ROLE_REQUIRED`. | Desk staff accounts cannot sign in. |

## 2. Contract mismatches that cause wrong behaviour

| # | Topic | Server | Mobile | Effect |
|---|---|---|---|---|
| M1 ✅ | Lockout code | Wrong passwords → `401 INVALID_CREDENTIALS` ("Wrong credentials." plus remaining-attempt count). After 8 in 10 minutes → `429 LOGIN_PAUSED`. Also `LOGIN_CODE_WAIT`, `LOGIN_EMAIL_MISSING`, `LOGIN_CHANGED` (`login-security.service.ts`). | Knows `TOO_MANY_LOGIN_ATTEMPTS` only. | Lockout messages are only shown when the server's own text passes the app's filter. The portal fallback doesn't treat `LOGIN_PAUSED` as final. |
| M2 ✅ | Attempt budget | Each failed portal check counts as a wrong attempt (role/portal mismatch also throws `INVALID_CREDENTIALS`, `jwt-auth.service.ts` line 94). | Tries the `user` portal, then the `staff` portal, on each sign-in. | One wrong password from the app uses **2 of the 8 attempts**. A librarian's first sign-in on a device also burns one. |
| M3 | Single session | Every successful login revokes the account's other sessions (`library_login_sessions … revoked_at=NOW()`, `login-security.service.ts` ~line 84). | Assumes its token stays valid. | Signing in on the web logs the phone out, and the reverse. The app sees `ACCOUNT_ACCESS_REVOKED` or `SESSION_EXPIRED`. |
| M4 ✅ | Session error codes | Also sends `SESSION_EXPIRED`, `SESSION_UPGRADE_REQUIRED`, `JWT_ROLE_FORBIDDEN`. | Clears the session only for `ACCOUNT_DEACTIVATED`, `ACCOUNT_ACCESS_REVOKED`, `JWT_REQUIRED`, `JWT_INVALID`. | Expired sessions aren't cleared. The user is stuck on error banners until relaunch. |
| M5 | School email domain | `@ormoc.sti.edu.ph` (`account-auth.validation.ts` line 8). | Register form requires `@stu.sti.edu.ph`. | Would block registration even after B3 is fixed. |
| M6 | Borrow limit | Set by the librarian's borrowing policy (`student_max_active_books`, `faculty_max_active_books`), counting active loans. Exposed via `GET /api/v1/borrow/cart-state`. | Hard-coded: students 2 in the cart, faculty unlimited. Ignores current loans. | The app can let a student build a cart the server will refuse, or block one the policy allows. |
| M7 | Device origin check | With `LOGIN_DEVICES_ENABLED=true`, `/auth/login` rejects requests whose `Origin` / `Sec-Fetch-Site` isn't the website (`403 DEVICE_ORIGIN`). | Native app; sends neither header. | Probably fine today: a missing header passes. Worth confirming the deployed flag and keeping in mind if the check gets stricter. |
| M8 | Page size | Catalogue `limit` is capped at **100**, default 24 (`book-catalog.validation.ts` line 53). | Sends 50. | Fine now. No paging in the app (already in [Known gaps](known-gaps.md)). |

## 3. Data endpoints — these still line up

The app's other calls exist on the server with matching paths, methods and role guards (`apps/api/src/app.js` lines 101–155). The response field names the app reads are present in the server code (e.g. print requests use `request_id`, `file_name`, `job_status`, `calculated_cost`):

`GET /auth/me` · `GET /catalog/books` · `GET /catalog/books/{id}` · `POST /catalog/books/{id}/reservations` · `GET /catalog/research` · `GET /borrowing/history` · `POST /borrow/submit-request` · `GET /reservations` · `PUT /reservations/{id}/cancel` · `GET /fines/me` · `GET /notifications` · `GET /printing/requests` · `GET /attendance/pass` · `GET /attendance/pass.png`

The field match was checked by name only, not against a recorded live response.

## 4. Features the server supports but the app fakes or lacks

This revises three items in [Known gaps](known-gaps.md):

| App today | Server has | Note |
|---|---|---|
| Print request submit is local-only | `GET /printing/service-status`, `/pricing`, `/printers/availability`; `POST /printing/quote`; **`POST /printing/requests`** (multipart file upload; server counts pages and prices); `PUT /printing/requests/{id}/cancel`; invoices and receipts | Real feature to wire up. Needs a file picker. |
| "Mark all as read" is local-only | **`PATCH /notifications/read-all`**, `PATCH /notifications/{id}/read`, `DELETE /notifications[/{id}]` | Simple to wire. |
| Return request is local-only | Returns are **librarian-only**: `PUT /api/v1/admin/borrowing/{transactionId}/return` (desk permission) | The app is right not to call anything. The screen should say it's a reminder, not a "request submitted". |
| Clearance computed on the phone | `GET /clearance/me`, `/clearance/schedule`, `POST /clearance/lost-books/{transactionId}/report` | Server is the source of truth. The app's rule (no loans + no unpaid fines) may differ. |
| Overview assembled from 8 calls | `GET /dashboard` (student/faculty summary) | Could replace much of `refreshStudentData()`. |
| — | `GET /borrowing/my-books`, `GET /borrowing/in-library`, `GET /borrow/cart-state`, `POST /reservations/request`, `GET /fines/terms`, `/fines/receipts[/{id}]`, `GET/PATCH /profile/me`, profile avatar, `GET /floor-plan`, password recovery (`/auth/password-recovery/{request,verify,reset}`; ✅ added on branch `feat/password-reset`) | Not in the app. |
| Staff home is placeholders | Full librarian/staff API under `/api/v1/admin/*` (circulation desk, attendance scanning, reservations queue, printing desk, fines, inventory, reports) | Large. Separate decision. |

## 5. Screen coverage side by side

The web routes come from `apps/web/src` (student and faculty portals are almost identical; printing is student-only in both).

| Web (student / faculty) | Mobile |
|---|---|
| dashboard | overview (full for students, welcome card for faculty) |
| catalog | book catalogue + detail |
| research | research catalogue + detail |
| cart | book cart |
| borrowing, my-books | borrowing history (no separate "my books") |
| reservations | reservations |
| attendance | My QR attendance |
| printing (student) | printing (student), submit not wired |
| fines | fines |
| clearance | clearance (computed locally) |
| notifications | notifications |
| profile | — |
| floor-plan | — |
| library (in-library) | — |
| forgot-password | ✅ forgot password (branch `feat/password-reset`) |
| librarian / staff / admin portals (~35 routes) | staff home placeholder |

## Suggested order (for discussion, nothing changed yet)

1. ✅ **Sign-in:** add the emailed-code step (B1), keep the session alive or re-login on `SESSION_EXPIRED` (B2, M4), relax the ID rule (B4), map `Staff` (B5), and handle `LOGIN_PAUSED` without double-counting attempts (M1, M2). Nothing else is testable until this works.
2. **Registration:** move to roster registration with the `@ormoc.sti.edu.ph` domain (B3, M5), or hand users to the website.
3. **Quick wins:** server read-state for notifications, server clearance, borrow limits from `cart-state`.
4. **Printing submit**, then profile and the rest of section 4.

Before step 1, confirm with the web team that a native client may use `/auth/login/verify` and `/auth/activity`, since those routes are written with the browser in mind.
