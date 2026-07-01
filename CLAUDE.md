# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Darshini is a TV-first IPTV player for Android TV, forked from Fredolx/fred-tv-mobile (StreamVault). Package ID is `tv.darshini.app`. Built with Kotlin + Jetpack Compose, Room (SQLite), Hilt (DI), Retrofit, and Media3 (ExoPlayer). Min API 25, targets Android TV / D-pad navigation first.

## Build Commands

```bash
./gradlew assembleDebug           # Debug APK
./gradlew assembleRelease         # Release APK (requires keystore.properties)
./gradlew app:installDebug        # Build + install to connected device/emulator
./gradlew testDebugUnitTest       # Unit tests
./gradlew app:connectedAndroidTest  # Instrumented tests (requires device)
./gradlew koverHtmlReport         # Coverage report
```

Run a single test class:
```bash
./gradlew testDebugUnitTest --tests "tv.darshini.data.SomeTest"
```

## Module Architecture

Four Gradle modules with strict layering — `domain` has no Android or data dependencies:

```
app  →  data  →  domain
 ↓                  ↑
player  ────────────┘
```

- **`domain/`** — Pure Kotlin. All models (`Movie`, `Channel`, `Series`, `Provider`, etc.), repository interfaces, manager interfaces, and use cases. No Room, no Retrofit, no Android SDK imports. This is the source of truth for contracts.
- **`data/`** — Implements domain interfaces. Contains Room database (`StreamVaultDatabase`, v61), DAOs, entity mappers, network clients (Retrofit + OkHttp), and sync logic (`SyncManager`, `SyncManagerXtreamFetcher`). Provider type implementations live here: `XtreamProvider`, `StalkerProvider`, `JellyfinProvider`.
- **`app/`** — Android UI. Hilt DI wiring (`di/` package), Compose screens, ViewModels, `AppNavigation.kt` (single `NavHost` entry point), and platform integrations (Cast, TvInput, plugins, recordings).
- **`player/`** — Media3/ExoPlayer wrapper (`PlayerEngine` interface → `Media3PlayerEngine`). Handles live retry logic, HLS vs MPEG-TS stream type detection, FFmpeg decoder fallback for AC-3/DTS/TrueHD, timeshift buffering, and subtitle rendering.

## Key Architecture Patterns

**Navigation**: Single-Activity with Compose Navigation. All routes are string constants in `Routes` object inside `AppNavigation.kt`. Player is launched via `PlayerNavigationRequest` (a `Serializable` passed through the nav graph) rather than individual query params.

**ViewModels**: Hilt-injected, one per screen. `MoviesViewModel` and `SeriesViewModel` share the bulk of their logic through free functions in `app/ui/screens/vod/` (`VodBrowseStateHelpers.kt`, `VodCatalogBuilders.kt`, etc.) — these are not classes, just stateless extension functions called from both ViewModels.

**Sync**: `SyncManager` (data layer) handles all provider sync. It is orchestrated by `ProviderRepositoryImpl`. The heavy Xtream-specific fetch logic is split into `SyncManagerXtreamFetcher` (network) and uses streaming batch inserts to avoid buffering whole catalogs. A two-table staging pattern (e.g. `MovieImportStageEntity` → `MovieEntity`) is used to atomically swap catalog data.

**Database**: Room with 61 migrations. Schema JSON exported to `data/schemas/`. FTS tables exist for channels, movies, and series (`ChannelFtsEntity`, `MovieFtsEntity`, `SeriesFtsEntity`) — FTS search must fall back to `LIKE` when FTS returns empty (search is submit-only; local state is used for typing, DB is queried only on submit).

**Category display**: Categories have a `customOrder`, `isHidden`, and `isPinned` flag. The `applyProviderCategoryDisplayPreferences` function (in `app/ui/model/`) merges the persisted user ordering/visibility preferences onto the category list before it reaches the UI.

**Provider credentials**: Passwords are AES-encrypted at rest via `CredentialCrypto` in the data layer. The only path that ever exposes cleartext is `getAllProviderCredentials()` in `ProviderRepository`, used solely for Drive backup sync.

**Plugin system**: Third-party plugins are separate APKs discovered via `tv.darshini.plugin.API` intent action. Communication is Android `Messenger` IPC through `PluginMessengerClient`. See `docs/PLUGIN_API.md` for the contract.

## TV / D-pad Specifics

- Use `onPreviewKeyEvent` (not `onKeyEvent`) to intercept DPAD_CENTER/ENTER at the composable level before the system handles them.
- Search inputs use a submit-only pattern: typing updates local `MutableState`, the DB query fires only on DPAD_CENTER/ENTER, not on every keystroke.
- Focus restoration after back-navigation is managed manually with `FocusRequester`; rely on saved category ID state, not Compose's automatic focus restoration.
- The emulator must be locked to `ROTATION_270` for live TV debugging — see `AGENTS.md` for the exact `adb` commands.



## Graphify Knowledge Graph

This project has a graphify index at `graphify-out/`. Before answering architecture or cross-module questions, read `graphify-out/GRAPH_REPORT.md`. Use `graphify query`, `graphify path`, or `graphify explain` for cross-module relationship questions instead of grep.

## Dev Seeding

Debug builds support seeding a real provider from `local.properties` (see `docs/DEV_SEEDING.md`). Release builds never ship these values — the build script injects empty strings for all seeding fields in the release config.


## Testing

The testing should always be manual. Just ask the user to test and confirm.