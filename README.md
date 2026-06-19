# Darshini IPTV Player

Darshini is a TV-first IPTV player for Android TV built with Kotlin, Jetpack Compose, Room, Hilt, and Media3. 

It is designed for large playlists, remote-friendly browsing, fast provider switching, and a polished living-room playback experience. Darshini supports `M3U` playlists, `Xtream Codes`, `Stalker Portal`, and `Jellyfin` providers, with dedicated flows for `Live TV`, `Movies`, and `Series`.

Built for Android TV first, Darshini focuses on the things generic IPTV apps usually get wrong: D-pad navigation, quick channel movement, large-library organization, and a player that still feels good to use from the couch. Phone and tablet installs are also supported, but the primary UX target is TV.

---

## Custom Extensions & Modded Features

This version includes several custom modifications and enhanced features to deliver a premium, personalized viewing experience:

### 1. Collapsible Sidebar & Navigation Options
- **Focus-based Expand/Collapse**: The left sidebar navigation automatically expands to show text labels when focused, and collapses to show icons only when focus moves to content.
- **Top Bar vs. Sidebar Switch**: You can toggle the primary navigation layout between a traditional Top Navigation Bar and the Collapsible Left Sidebar.
  - **Steps**: Go to **Settings** -> **Interface/Navigation Options** and toggle the navigation style.

### 2. Custom Playlist & Category Arrangement
- **Manual Ordering**: Arrange the order of your movie, series, and live categories exactly how you want them (e.g., place your preferred languages or categories at the top).
- **Visibility Toggle**: Hide unused or unwanted categories with an eye icon.
  - **Steps**:
    1. Navigate to **Settings** and select **Movie Categories**, **Live TV Categories**, or **Series Categories**.
    2. Choose the **Custom Order** option in the sort/arrangement window.
    3. Use the **Up/Down/Top** arrow icons to change the priority order and the **Eye** icon to toggle visibility.

### 3. Provider Details Export & Import
- **Decrypted Export**: Export your complete provider configurations (with passwords securely decrypted) to reuse them on other devices.
  - **Steps**:
    1. Go to **Settings** -> **Provider Settings** and select the provider you want to back up.
    2. Click the **Export** button.
    3. The file will be saved directly to your `/sdcard/Download/` folder (or app-private folders as fallback) as a `.json` file.
- **Restore / Import**:
  - **Steps**:
    1. Go to **Add Provider** -> **Restore Data**.
    2. Choose the exported JSON file from your `Download` directory to restore your login credentials instantly.

### 4. VOD Favorites Quick Toggle
- **Long-Click Shortcut**: Easily add or remove movie/series cards from your favorites by holding the enter/select button (long-click).
- **Favorites Shelf**: Your favorites are pinned unconditionally as a dedicated row for quick access.

### 5. Playback Enhancements
- **Next Episode Shortcut**: Added a "Next Episode" button directly next to the fast forward button in the player control bar when watching series.
- **External Player Support**: Option to switch the built-in media player with external players like VLC or MX Player.
  - **Steps**: Toggle the Player settings under **Settings** -> **Player Options** -> **External Playback Mode**.

---

## Features

### Provider Support
- `Xtream Codes` API integration
- `Stalker Portal` support
- `Jellyfin` media servers with direct library sync and TV-friendly Quick Connect support
- `M3U` playlists from URLs plus local files
- Separate onboarding and sync flows for live channels, movies, series, and guide data
- Fast switching between providers with provider-scoped settings
- Combined M3U profiles for merging multiple M3U providers into a single Live TV source

### Navigation And TV UX
- Designed for Android TV and D-pad navigation first
- Fast channel browsing with large-playlist friendly layouts
- Numeric remote input for direct channel entry
- Configurable startup landing screen so the app can open Home, Live TV, Movies, Series, Guide, Downloads, Plugins, or Settings first
- Colored remote button remapping with global defaults plus playback and live-browse overrides
- Preview mode while browsing channels
- TV-friendly search and text-entry flows

### Live TV And Channel Management
- Favorites and recently watched channels
- Custom groups for personal channel collections
- Pinned categories surfaced near the top of the live guide rail
- Long-press live categories for actions like pin, hide, lock or unlock, and custom-group management
- Channel reordering for favorites and custom groups
- Channel numbering modes by group or across the full provider lineup

### Guide, Search, And Playback
- Full EPG grid view
- Transparent overlay guide over live playback
- Program search inside the guide
- XMLTV guide support with built-in EPG source management
- Manual EPG match overrides and source-priority controls from inside Settings and Guide flows
- Provider archive or catch-up support when the source exposes replay streams
- Live rewind or timeshift playback with up to 30 minutes of buffer
- Global search across live TV, movies, and series
- Multi-view for watching multiple live streams at once
- Player controls for subtitles, audio tracks, aspect ratio, playback speed, video quality, Cast, and external-player handoff

### Recording And Playback
- Scheduled and background DVR recording for live channels
- Offline VOD downloads with grouped episode handling and completed-file local playback
- Program reminders from guide entries
- App-managed default recording folder with optional custom storage selection
- In-app playback for completed recordings
- Bundled Media3 FFmpeg audio fallback for unsupported audio codecs such as AC-3, E-AC-3, DTS, MP2, and TrueHD

### Movies And Series
- Two VOD layouts: Modern shelf-based browsing or Classic left-sidebar category browsing
- Detailed info pages for movies and series
- Continue watching, playback history, and detail-screen resume actions with saved position context
- In-player episode switching for series
- Automatic next-episode playback

### Parental Controls
- Hide categories completely
- Lock categories behind a PIN
- Option to hide locked content from browsing views
- Adult-category detection using provider flags and category naming heuristics

---

## Project Structure

- `app/` Android app UI, navigation, dependency injection, and Android TV integrations
- `data/` Room database, sync, parsing, provider implementations, and repositories
- `domain/` models, repository contracts, managers, and use cases
- `player/` playback abstraction and Media3 player implementation
- `docs/` architecture notes, plugin API docs, and image assets

---

## Build

### Requirements
- Android Studio
- Android SDK
- JDK 17 or another Gradle-supported JDK 17 runtime
- Android NDK only if you want to rebuild the bundled Media3 FFmpeg extension locally

### Useful commands
```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run unit tests
./gradlew testDebugUnitTest
```

---

## Notes
- Darshini is an IPTV client, not a content provider.
- Use only playlists, streams, and guide sources you are authorized to access.
- Local configuration and signing files are excluded from git.

---

## License & Credits

- **Original Base App**: This project is based on the excellent open-source project **Fredolx/fred-tv-mobile** (StreamVault). Special thanks to the original developers for their work.
- **License**: Any usage, modification, and distribution of the modified codebase must comply with the terms defined in the local LICENSE file.
