# Plan

Active work items. Completed phases are in git history.

---

## Open

### Phase 7 — Sync completeness investigation
**Status**: Not started

Sync is not syncing everything. Needs investigation:
- Understand the distinction between "sync" and "indexing" (`XtreamIndexJob` / `XtreamContentIndexEntity`) and when each runs.
- Identify which content types or categories are being dropped or skipped.
- Determine if the staging→swap pattern is the cause (partial stage completion?).
- Key files: `SyncManager.kt`, `SyncManagerXtreamFetcher.kt`, `XtreamIndexDaos.kt`, `CatalogSyncDao.kt`.

---

### Phase 8 — Lazy/paginated category sync with per-category "Load All" button
**Status**: Not started

Default sync (from Settings) should fetch only the top 20 items per category (Movies, Series, Live). A "Load All" / sync button should appear inside the "See All" category view to fetch the full category on demand.

Design questions to resolve:
- Can the Xtream API return a capped/paginated result per category, or must we fetch all and truncate locally?
- Where does the per-category full-sync button live: inside `MoviesScreen`, `SeriesScreen`?
- Does this require a new `syncSingleCategory` call path (already exists in `ProviderRepository`) or a new DAO query?
- Key files: `ProviderRepository.kt` (`syncSingleCategory`), `SyncManager.kt`, `MoviesViewModel.kt`, `SeriesViewModel.kt`, `MoviesScreen.kt`, `SeriesScreen.kt`.

---

### Phase 9 — App-wide performance pass
**Status**: Not started

Reported symptom: general UI slowness. Areas to audit:
- Overly broad `Flow` collectors causing unnecessary recompositions (check `distinctUntilChanged` / `distinctUntilChangedBy` usage in ViewModels).
- Debounce timings on search and scroll listeners.
- Heavy DB queries running on the main thread or without indexing (enable `SlowQueryLoggingOpenHelperFactory` to surface these).
- Key files: `MoviesViewModel.kt`, `SeriesViewModel.kt`, `Daos.kt`, `SlowQueryLoggingOpenHelperFactory.kt`.
