# STI ILMS Mobile — Global Search Design

**Status:** Approved 2026-10-13  
**Scope:** Drawer search icon → dedicated Search screen with book + research predictions (local-first, then API).

## Decisions

| Topic | Choice |
|-------|--------|
| Entry | Drawer search icon (was no-op) |
| Prediction targets | Books and research |
| Suggestion source | C — local filter first, debounced API refresh |
| UI shape | Dedicated Search screen (stacked NavHost route) |

## Flow

1. User opens drawer → taps Search → drawer closes → `DashboardRoute.Search` opens (stacked, like Notifications).
2. Search field is focused; keyboard opens.
3. Empty query: hint only (“Search books and research”) — no full catalog dump.
4. Typing: predictions appear under the field (local filter of loaded `books` + `research`).
5. After ~300ms pause on the same query: `loadBooks(query)` + `loadResearch(query)`, then rebuild predictions.
6. Tap row → book detail or research detail; Back returns to Search with query kept.
7. Back from Search → previous screen.

## Predictions

- Match on book: title, author, ISBN; research: title, author, category/department.
- Cap ~8 rows; books first, then research.
- Row: type label (Book / Research), title, subtitle (author).
- API failure after local hits: keep local rows + small error banner.
- No matches: empty state + optional “Browse catalog”.

## UI

- Portal components: `PortalField`, list rows (≥48dp), `ElmsEmptyState`, `ElmsBanner`.
- Title “Search” + Back control.
- Existing light/dark tokens only.

## Out of scope

- App-screen shortcuts (Fines, Reservations, etc.).
- Search inside the drawer sheet itself.
- Changing Books/Research catalog screen search fields (unchanged).
