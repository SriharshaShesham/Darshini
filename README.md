# Darshini IPTV Player

Darshini is a TV-first IPTV player for Android TV built with Kotlin, Jetpack Compose, Room, Hilt, and Media3. This project is based on the excellent open-source project **Fredolx/fred-tv-mobile** (StreamVault). Special thanks to the original developers for their work.




---

## Features

Darshini keeps everything from the upstream **StreamVault** base and layers the additions and changes below on top of it. This section covers what is unique to Darshini; see **Complete Features** further down for the full combined list.

---

### Navigation & TV UX
- **Collapsible Sidebar & Navigation Switch**: A hamburger toggle collapses the left sidebar to icon-only (72dp) or expands it to full labels (240dp) with a smooth animation; it also auto-expands on focus and collapses when focus moves to content. You can switch the primary navigation between a Top Bar and the Collapsible Sidebar, and the choice persists across screens.
  - **Steps**: **Settings → Interface/Navigation Options** to switch the navigation style.
- **Focus Restore After Back-Navigation**: Open a movie or series and press back — focus returns to the exact poster you opened instead of snapping to the top or the sidebar, with your category rows and scroll position preserved.
- **Category Detail Header + Scroll Restore**: Category grids show a header row (back arrow · category name · on-demand sync icon) in place of the old chip navigation, and pressing back restores your exact scroll position in the preview list.
- **Submit-Only D-pad Search**: Typing only updates the field; the database query runs on DPAD_CENTER/ENTER, so search stays fast and responsive on a remote.

---

### Catalog & Categories
- **Custom Category Ordering & Visibility**: Reorder movie/series/live categories (Up/Down/Top) and hide unwanted ones with an eye icon. Changes auto-save — no Save button.
  - **Steps**: **Settings → UI → Category Control**; pick the **Custom Order** sort mode to activate your ordering.
- **Faster Sync via Hidden Categories**: Hidden categories are skipped during background sync, reducing sync time and data usage.
- **Per-Category On-Demand Sync**: Every Movies/Series category row — and its long-press options dialog — has a **Sync** button that fetches that category's full catalog on demand. Background sync fetches a fast top-items preview first so the app loads quickly after setup.

---

### Playback & Watchlist
- **Next Episode + External Player**: A **Next Episode** button sits beside fast-forward when watching a series, and you can hand playback off to an external player like VLC or MX Player.
  - **Steps**: **Settings → Player Options → External Playback Mode**.
- **VOD Favorites Quick Toggle**: Long-press (hold select) a movie/series card to add or remove it from favorites; favorites are pinned as a dedicated shelf.
- **Remove from Watch History**: Two ways to clear watched items so they stop reappearing:
  - **Long-press** any tile in a **Continue Watching** row (Home, Movies, Series) to remove it.
  - An **Unwatch** button on the movie/series **detail screen**, shown next to Continue/Resume when you have progress. On a movie it clears that title; on a series it clears the **entire series'** history and removes it from Continue Watching.

---

### Personalization & Identity
- **Darshini Branding**: A refreshed identity — a peacock-feather TV launcher icon, a gold "Darshini" wordmark banner, and a white splash screen.
- **Custom App Themes**: **Glass (Dark)** (default) & **Glass (Light)** frosted-glass palettes with spotlight focus outlines, plus solid **Dark**, **Light**, and **System Default**.
  - **Steps**: **Settings → UI → App Theme**.
- **Provider Details Export & Import**: Export a provider's full configuration (passwords decrypted) to a `.json` in your Downloads, then restore it on another device.
  - **Steps**: Export from **Settings → Provider Settings → Export**; restore from **Add Provider → Restore Data**.

---

# Guide to Installing Darshini APK on Fire Stick & Google TV

**Download APK:** [Darshini V1.0.1](https://tinyurl.com/darshiniapp)



## 🔥 PART 1: Amazon Fire Stick Instructions

### Step 1: Enable Developer Options
1. Navigate to **Settings** (the gear icon on the home screen).
2. Select **My Fire TV** > **About**.
3. Highlight the first item (your Fire TV Stick model) and press the center **Select button** on your remote **7 times** until a message at the bottom says: *"No need, you are already a developer."*
4. Press the back button once to return to **My Fire TV**. You will see a new menu item called **Developer Options**.
5. Click **Developer Options** > **Install unknown apps**. *(Leave this screen open for Step 2).*

### Step 2: Install & Authorize the "Downloader" App
1. Press the Home button and go to the **Find / Search** bar on the home screen.
2. Search for **"Downloader"** (the app with the orange logo) and install it.
3. Before opening it, return to **Settings** > **My Fire TV** > **Developer Options** > **Install unknown apps**.
4. Find **Downloader** in the list and toggle it to **ON**.

### Step 3: Download & Install the APK
1. Launch the **Downloader** app and select **Allow** when it requests storage access.
2. Click inside the URL entry box on the Home tab.
3. Type the direct APK URL or your shortened link carefully:
   `https://tinyurl.com/darshiniapp`
4. Click **Go**. The app will download the file from GitHub.
5. Once the download finishes, an installation prompt will overlay the screen. Select **Install**.
6. After installation completes, click **Done**. 
7. Select **Delete** and then **Delete** again to clear out the temporary setup file and save device storage.

### Step 4: Locate Your App
1. Press and hold the **Home** button on your remote, then select **Apps**.
2. Scroll to the bottom of the list to find **Darshini**.
3. Highlight the app, press the **Options button (three horizontal lines)** on your remote, and select **Move to front** to pin it to your home screen.

---

## 📺 PART 2: Google TV Instructions (Chromecast, Sony, TCL, etc.)

### Step 1: Enable Developer Options
1. Click your **Profile/Settings (Gear)** icon in the top-right corner of the screen.
2. Select **System** > **About**.
3. Scroll down to **Android TV OS Build** and click it repeatedly **7 times** until a notification appears saying *"You are now a developer!"*
4. Press the back button to return to the main **Settings** menu.
5. Navigate to **Apps** > **Security & Restrictions** > **Unknown sources**. *(Leave this screen open for Step 2).*

### Step 2: Install & Authorize the "Downloader" App
1. Return to the home screen, go to the **Apps** tab, and select **Search for apps**.
2. Search for **"Downloader"** (the app with the orange logo) and install it.
3. Before opening it, return to **Settings** > **Apps** > **Security & Restrictions** > **Unknown sources**.
4. Find **Downloader** in the list and toggle the switch to **ON**.

### Step 3: Download & Install the APK
1. Open the **Downloader** app and select **Allow** to grant storage permissions.
2. Click inside the box on the Home tab to bring up the keyboard.
3. Type the direct URL or your shortened link:
   `https://tinyurl.com/darshiniapp`
4. Click **Go** to download the package.
5. An installation prompt will automatically pop up. Select **Install**.
6. *Google Play Protect Warning:* If a warning flags it as an unknown developer, click **More Details** > **Install Anyway**.
7. Once installed, select **Done**.
8. Choose **Delete** and confirm **Delete** again to wipe the temporary installer package from your local storage.

### Step 4: Locate Your App
1. Go to **Settings** > **Apps** > **See all apps**.
2. Find **Darshini** in the list to launch it. 
*(Note: If a sideloaded app doesn't natively appear in the regular Google TV launcher grid, you can use a custom launcher like Projectivy Launcher or Sideload Launcher from the Play Store to create a direct shortcut).*

---



## Complete Features

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
- Collapsible left sidebar with hamburger toggle — icon-only at 72dp, full labels at 240dp, animated transition
- Category detail header with back arrow, category name, and per-category sync button; back navigation restores scroll position in the parent list
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
- Remove from watch history: an **Unwatch** button on the movie/series detail screen (movie clears that title; series clears the entire series) plus long-press removal on Continue Watching rows
- In-player episode switching for series
- Automatic next-episode playback
- Per-category on-demand Sync button on every category row and in the category options dialog to fetch the full catalog for a single category without triggering a full provider sync
- Smart two-tier sync: fast background preview (top items per category) keeps initial load quick; full category fetch is available on demand via the Sync button

### Parental Controls And Category Management
- Hide categories completely — hidden categories are excluded from background sync to improve performance and reduce data usage
- Category Control moved to Settings → UI for easier access; supports drag-style reordering with Up/Down/Top buttons that auto-save instantly
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
