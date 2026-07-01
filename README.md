# Darshini IPTV Player

Darshini is a TV-first IPTV player for Android TV built with Kotlin, Jetpack Compose, Room, Hilt, and Media3. This project is based on the excellent open-source project **Fredolx/fred-tv-mobile** (StreamVault). Special thanks to the original developers for their work.




---

## Features

This app includes several custom modifications and enhanced features to deliver a premium, personalized viewing experience along with the  features from the original development from StreamVault.


### 1. Collapsible Sidebar & Navigation Options
- **Hamburger Toggle**: A hamburger icon at the top of the left sidebar lets you manually collapse it to icon-only view (72dp) or expand it to full width (240dp) with a smooth animation. State persists as you navigate between screens.
- **Focus-based Expand/Collapse**: The left sidebar navigation automatically expands to show text labels when focused, and collapses to show icons only when focus moves to content.
- **Top Bar vs. Sidebar Switch**: You can toggle the primary navigation layout between a traditional Top Navigation Bar and the Collapsible Left Sidebar.
  - **Steps**: Go to **Settings** -> **Interface/Navigation Options** and toggle the navigation style.

### 2. Custom App Themes
- **Multiple Color Palettes**: Choose between standard and glass-morphed themes directly within the interface. Supported profiles:
- **Glass (Dark)** (Default) & **Glass (Light)**: Modern frosted glass overlays with spotlight focus outlines.
- **Dark** & **Light**: Traditional solid layout palettes for high-contrast visibility.
- **System Default**: Adapts dynamically to your Android TV's system appearance.
- **Steps**: Go to **Settings** -> **UI** -> **App Theme** to choose your style.

### 2. Custom Playlist & Category Arrangement
- **Manual Ordering**: Arrange the order of your movie, series, and live categories exactly how you want them (e.g., place your preferred languages or categories at the top).
- **Visibility Toggle**: Hide unused or unwanted categories with an eye icon. Changes apply immediately — no Save button required.
- **Sync Performance**: Hidden categories are skipped during background sync, so hiding unused categories reduces sync time and data usage.
- **Category Control in UI Settings**: The Category Control panel has moved to **Settings → UI** so you can access it independently of the provider settings page.
  - **Steps**:
    1. Navigate to **Settings** → **UI** → **Category Control**.
    2. Use the **Up/Down/Top** arrow icons to reorder categories or the **Eye** icon to toggle visibility.
    3. Choose the **Custom Order** sort mode in the category sort settings to activate your custom ordering.

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

### 7. Category Detail Navigation Header
- **Proper Navigation Header**: When browsing a category grid (Movies or Series), a header row replaces the old chip-based navigation. It shows a **back arrow** on the left, the **category name** in the center, and a **sync icon** on the right for on-demand refresh.
- **Scroll Position Restore**: Pressing back from a category grid returns you to exactly where you were in the preview rows list — scroll position is preserved.

### 6. Per-Category On-Demand Sync
- **Sync Button on Category Rows**: Every Movies and Series category row displays a **Sync** button alongside the "See All" button. Pressing it fetches the complete catalog for that category on demand, without waiting for a full provider sync.
- **Smart Background Preview Sync**: The default background sync fetches a fast preview of the top items per category so the app loads quickly after setup. Use the per-category Sync button whenever you want the full listing for a specific category.
- **Category Options Dialog**: Long-press any category header to open its options. A new **Sync Category** action is available there as an alternative to the row button.

### 8. Focus Restore After Opening a Movie/Series
- **Return to Where You Were**: When you open a movie or series from a category row and press back from the detail screen, focus returns to the exact poster you opened — no more snapping to the top or the sidebar. Your paged-in category rows and scroll position are preserved across the trip.

### 9. Remove from Continue Watching
- **Unwatch Shortcut**: Long-press (hold the enter/select button) any tile in a **Continue Watching** row to remove it from your history, so finished or unwanted items stop reappearing. Works on the Home, Movies, and Series screens.

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
