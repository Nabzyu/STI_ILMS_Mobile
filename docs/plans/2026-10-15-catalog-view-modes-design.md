# Catalog view modes — design

**Date:** 2026-10-15  
**Status:** Approved  
**Audience:** Student / Faculty mobile  
**Screens:** Book catalog, Research catalog  

---

## Goal

Let users switch each catalog between a **list** and a **2-column grid**, with the choice remembered separately per catalog across app restarts.

---

## Decisions (locked)

| Topic | Choice |
|-------|--------|
| Modes | List + 2-column grid only (no 3-col) |
| Prefs | Separate for books vs research |
| Persist | Yes — `SharedPreferences` |
| Default | `LIST` (current UI) |
| Approach | Shared toggle + grid cells + tiny prefs helper |

Out of scope: 3-col grid, shared preference across catalogs, search/filter changes, new API calls.

---

## Architecture

```
CatalogViewMode (LIST | GRID_2)
        ↑
CatalogViewPrefs (read/write by key: books | research)
        ↑
CatalogViewToggle (UI)
        ↑
BookCatalogScreen / ResearchCatalogScreen
  (filters unchanged; results region swaps list vs grid)
```

---

## UI

**Toggle:** Lucide list + grid icons beside the results count row. Selected state uses brand interactive colour. Min 48dp touch targets.

**List:** Keep existing `ElmsListRow` rows (book cover / research file icon).

**Grid (2-col):**
- Books — cover tile, title (max 2 lines), availability badge
- Research — file icon tile, title (max 2 lines), year/category
- Tap opens the same detail route as list
- Cards use existing `ElmsSectionCard` / shape tokens; gaps on the 4dp grid (8–12dp)

Headers, search, and filters stay above; only the results region uses `LazyColumn` or `LazyVerticalGrid`.

---

## Persistence

| Key | Values |
|-----|--------|
| `catalog_view_books` | `list` / `grid_2` |
| `catalog_view_research` | `list` / `grid_2` |

- Prefs file: plain `SharedPreferences` (UI chrome, not secrets) — same style as login hints
- Missing or invalid value → `LIST`
- Toggle: update Compose state first, then write prefs

---

## Errors & testing

- Prefs failures never block catalog load; always fall back to `LIST`
- Existing load / empty / error banners unchanged
- Unit tests: round-trip both keys; invalid → `LIST`
- Manual: toggle both catalogs; kill app; confirm separate memory; light + dark

---

## Related skills

- `@sti-design-system` — tokens and shared components
- `@android-visual-verification` — screenshot confirmation after build
- `@writing-plans` — implementation plan next
