# Catalog book cart FAB — design

**Date:** 2026-10-16  
**Status:** Approved  
**Audience:** Student / Faculty mobile  
**Related:** Existing in-memory cart (`LibraryRepository.cart`), `BookCartScreen`, `DashboardRoute.BookCart`

---

## Goal

Give catalog browsers a always-visible floating cart control with a count badge, without changing cart rules or navigation.

---

## Decisions

| Choice | Decision |
|--------|----------|
| Where | `BookCatalogScreen` only (list + grid) |
| Visibility | Always shown; badge when `cart.size > 0` |
| Badge | Existing `ElmsCountBadge` |
| Results-bar Cart | Remove; FAB is the only catalog cart entry |
| Approach | Box overlay + Material `FloatingActionButton` (not nested Scaffold, not shared component) |

---

## Behavior

- FAB bottom-end, 16dp from edges; icon `Lucide.ShoppingCart`.
- Colors from `MaterialTheme.colorScheme` (`primary` / `onPrimary`).
- Tap → existing `onCartClick` (opens `BookCartScreen`).
- Badge = `repository.cart.size`; hidden at 0 via `ElmsCountBadge`.
- List/grid bottom content padding ~72dp so last rows clear the FAB.
- A11y: `contentDescription` `"Book cart"` or `"Book cart, N items"` when N > 0.

---

## Out of scope

- Book detail, drawer, borrow submit flow, cart limit messaging, new routes/APIs.

---

## Check

- Empty cart: FAB visible, no badge; opens empty cart.
- Add from detail, return to catalog: badge matches count.
- Last list/grid item not under FAB.
- Light and dark.
