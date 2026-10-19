# Reservation preferred day — UX/UI design

**Date:** 2026-10-15  
**Status:** Approved for UI build (Approach A) · mobile UI in progress; calendar API still optional/stub  
**Audience:** Student / Faculty mobile  
**Related:** Existing waitlist reservations (`POST/GET/PUT /api/v1/reservations…`); closed-day logic today is used for **borrow due dates**, not reservations.

---

## Goal

Let students pick a **preferred visit / pickup day** when reserving an unavailable book, and clearly show whether the **library is open that day** — without pretending the copy is guaranteed for that exact day.

Design the mobile UX so it can plug into future APIs with minimal UI churn.

---

## Product decision (recommended)

| Option | Meaning | Verdict |
|--------|---------|---------|
| **A. Preferred day + open/closed** | Student chooses a day they plan to visit; system blocks closed days and shows open hours/status. Waitlist still decides *when* a copy becomes ready. | **Recommended** |
| B. Hard hold for a date | Promise a copy on that calendar day | Reject for now — needs inventory policy the API does not have |
| C. Open-calendar only (no date on reserve) | Separate “Is the library open?” tool | Too weak vs user ask |

**Copy rule:** Prefer “Preferred pickup day” over “Reserve for [date]” so we never imply a guaranteed hold.

---

## Current baseline (do not break)

Today’s waitlist still applies:

1. Reserve only when no copies are available  
2. Join queue → staff marks ready → **pickup deadline** appears  
3. Cancel while pending/approved/ready  

New UI **adds** preferred day + library availability; it does not replace the queue.

---

## Screens & flows

### 1) Book detail — start reserve

**When:** `availableCopies <= 0`  
**CTA:** Keep primary button label **Reserve this title**

**Tap reserve → bottom sheet / full-screen step (not instant API):**

```
Reserve · [Book title]
─────────────────────
Preferred pickup day
  [ calendar / date field ]

Library status for that day
  ● Open — Mon–Sat · closes 5:00 PM     (example)
  ● Closed — Campus holiday             (example)
  ● Checking… / Unable to check         (loading / error)

Note: You’ll join the waiting list. A copy is not guaranteed
on this day. We’ll notify you when it’s ready to claim.

[ Cancel ]              [ Join waiting list ]  (disabled if Closed)
```

**Rules**

- Default preferred day = **next open operating day** (or tomorrow if open unknown until API returns).  
- Closed day → cannot submit; banner explains why.  
- Open / unknown-with-retry → allow submit (unknown shows warning, not hard block — avoids locking users if calendar API is down).  
- Optional: quick chips — Today / Tomorrow / Next Mon (only if that day is open).

### 2) Confirm success

Toast or banner: **Reservation submitted · preferred day Fri, Oct 17**  
Navigate optional deep-link to Reservations list.

### 3) Reservations list (`ReserveBookScreen`) — redesign cards

Keep `PageHeader` + Find a book. Upgrade each card:

```
┌─────────────────────────────────────────┐
│ 📖  [Queued | Ready for pickup | …]     │
│ Introduction to Algorithms              │
│ Requested Oct 14                        │
│                                         │
│ Preferred day     Fri, Oct 17           │
│ Library that day  Open                  │
│ Queue             #2                    │
│ Pickup by         —  (or deadline when  │
│                      status = ready)    │
│                                         │
│                    [ Cancel ]           │
└─────────────────────────────────────────┘
```

**Status mapping (UI)**

| API / local status | Badge | Emphasis |
|--------------------|-------|----------|
| pending / approved | Queued | neutral/warning |
| ready_for_pickup | Ready to claim | success / yellow accent ok for attention |
| cancelled / expired | Cancelled / Expired | muted; hide cancel |

Show **pickup deadline** prominently when ready; keep preferred day as secondary metadata.

### 4) Overview widget

If a reservation exists, show one line under queue:

`Preferred Fri, Oct 17 · Library open`  
or `Preferred Sun · Library closed — pick another day` (deep-link to list to edit — **v2**; v1 can omit edit).

### 5) Edit preferred day (v1.1 optional)

From card overflow/button **Change day** → same sheet as create, PATCH preferred day.  
**v1:** create-only is enough if backend lag; document as follow-up.

---

## Component map (STI design system)

Reuse only existing patterns:

| Piece | Component |
|-------|-----------|
| Screen chrome | `PageHeader`, `PortalButton`, `ElmsBanner`, `ElmsEmptyState`, `ElmsLoadingBlock` |
| Cards | `SectionCard` / `ElmsSectionCard` |
| Status | `StatusBadge` |
| Errors | `userFacingMessage` + `ElmsBannerTone.Error` |
| Date entry | Material3 `DatePicker` / `DatePickerDialog` wrapped in sheet; colors from `colorScheme` |
| Open/closed callout | `ElmsBanner` Info / Warning / Error — no new status colors |

**Do not** invent raw hex; light + dark both required in visual QA.

---

## States to design for

| State | UI |
|-------|----|
| Calendar loading | Skeleton / “Checking library hours…” |
| Day open | Green/success-neutral banner + enable CTA |
| Day closed | Warning/error banner + disabled Join |
| Calendar API missing / 404 | Soft warning: “We couldn’t verify hours. You can still join the queue.” + enable CTA |
| Reserve submit loading | Button `loading` |
| Reserve fail | Banner with `userFacingMessage` |
| Empty list | Existing empty state |
| Ready for pickup | Strong “Claim at counter by …” |

---

## Backend-ready contract (proposed; not implemented)

Design UI against these shapes so wiring is mostly mapping later.

### Check library day

```http
GET /api/v1/library-calendar/day?date=YYYY-MM-DD
```

```json
{
  "success": true,
  "data": {
    "date": "2026-10-17",
    "isOpen": true,
    "reason": null,
    "hoursLabel": "8:00 AM – 5:00 PM",
    "timezone": "Asia/Manila"
  }
}
```

Closed example: `"isOpen": false, "reason": "Campus holiday"`.

*(If backend reuses `library_closed_days` + Sunday rules from due-date logic, mobile does not need to know.)*

### Create reservation (extend existing)

```http
POST /api/v1/reservations/request
```

```json
{
  "bookTitleId": 42,
  "preferredPickupDate": "2026-10-17"
}
```

Server should reject closed days with a stable code, e.g. `LIBRARY_CLOSED_ON_PREFERRED_DATE`.

### List (extend existing items)

Each item adds:

```json
{
  "preferredPickupDate": "2026-10-17",
  "preferredDayIsOpen": true,
  "preferredDayHoursLabel": "8:00 AM – 5:00 PM",
  "pickupDeadline": null
}
```

Mobile DTOs: add fields with `@SerializedName` + camelCase alternates; default null for old servers.

### Optional later

```http
PATCH /api/v1/reservations/{id}/preferred-pickup-date
```

---

## Mobile model sketch (UI-ready)

Extend `Reservation` (conceptual):

- `preferredPickupDate: String?` (display-ready or ISO date)  
- `preferredDayLibraryOpen: Boolean?`  
- `preferredDayHoursLabel: String?`  
- `pickupDeadline: String?` (already on DTO as `pickupDeadline` — surface in UI)

`ReserveRequestDto` gains optional `preferredPickupDate`.

Repository:

- `checkLibraryDay(date: LocalDate): LibraryDayStatus`  
- `reserveBook(book, preferredPickupDate)`  

Until APIs exist: **feature flag or stub** that returns “Unable to check” / disables preferred-day UI — do not fake open/closed.

---

## Information architecture

```
Book detail (unavailable)
    → Preferred-day sheet (calendar + open check)
        → POST reserve
            → Reservations list / Overview widget

Reservations list
    → Cancel (existing)
    → (v1.1) Change preferred day
```

No new bottom-nav tab. Keep drawer **Reservations**.

---

## Accessibility & copy

- Date control must have a visible label (“Preferred pickup day”), not placeholder-only.  
- Closed reason announced in banner text, not color alone.  
- Always include the non-guarantee sentence on the sheet.  
- Counter claim when ready stays the same mental model as returns (desk completes handoff).

---

## Out of scope (this design)

- Guaranteeing a physical copy on the preferred day  
- Staff admin calendar UI  
- Changing queue algorithms  
- Floor-plan / map  
- Push notifications (nice follow-up when ready_for_pickup)

---

## Success criteria

- Student can choose a preferred day and see open/closed before joining the queue  
- Closed days cannot be submitted (when calendar API works)  
- List shows preferred day + queue + pickup deadline when ready  
- UI maps cleanly to the proposed API fields  
- Waitlist semantics remain honest (no false “booked for Friday” language)

---

## Approval checkpoint

Please confirm:

1. **Approach A** (preferred day + open/closed, not hard hold)  
2. **v1** = create sheet + list fields only (no change-day yet)  
3. Soft-allow submit when calendar check fails  

After approval → implementation plan (`writing-plans`) with Compose tasks + DTO stubs + optional mock calendar for previews.
