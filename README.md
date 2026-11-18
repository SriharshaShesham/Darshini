# Darshini

A TV-first IPTV player for Android TV and Fire TV, built for the D-pad. It supports Xtream Codes, Stalker Portal, M3U and Jellyfin, and plays live TV, movies and series with a full TV guide. Based on [Fredolx/fred-tv-mobile](https://github.com/Fredolx/fred-tv-mobile) (StreamVault).

## Screenshots

![Darshini slideshow](docs/screenshots/slideshow.webp)

<details>
<summary><b>Browse all screenshots</b></summary>
<br>

| | | |
|---|---|---|
| ![Home](docs/screenshots/home.jpg)<br>Home | ![Live TV](docs/screenshots/live-tv.jpg)<br>Live TV | ![Guide](docs/screenshots/guide.jpg)<br>TV Guide |
| ![Player](docs/screenshots/player.jpg)<br>Player | ![Movies](docs/screenshots/movies.jpg)<br>Movies | ![Movie details](docs/screenshots/movie-details.jpg)<br>Movie details |
| ![Series](docs/screenshots/series.jpg)<br>Series | ![Series details](docs/screenshots/series-details.jpg)<br>Series details | ![Episodes](docs/screenshots/series-episodes.jpg)<br>Episodes |
| ![Search](docs/screenshots/search.jpg)<br>Search | ![Continue watching](docs/screenshots/continue-watching.jpg)<br>Continue watching | ![Favorites](docs/screenshots/channel-favorite.jpg)<br>Mark as favorite |
| ![Add provider](docs/screenshots/add-provider.jpg)<br>Add provider | ![Provider settings](docs/screenshots/provider-settings.jpg)<br>Provider settings | ![Backup](docs/screenshots/backup-restore.jpg)<br>Google Drive backup |
| ![Category controls](docs/screenshots/category-controls.jpg)<br>Hide / show categories | ![Settings](docs/screenshots/settings.jpg)<br>Settings | ![Sidebar](docs/screenshots/sidebar.jpg)<br>Collapsible sidebar |

</details>

<sub>All content shown is fictional demo data from [`tools/demo-xtream-server`](tools/demo-xtream-server/).</sub>

## Features

- **Providers:** Xtream Codes, Stalker Portal, M3U (URL or file), Jellyfin with Quick Connect, and combined M3U profiles.
- **Live TV:** favorites, recents, custom groups, pinned categories, channel numbers, numeric remote entry and live preview.
- **Guide:** a full EPG grid, an overlay guide, XMLTV sources, manual EPG matching and catch-up.
- **Playback:**
  - timeshift of up to 30 minutes, multi-view and Cast
  - subtitle and audio track selection
  - FFmpeg audio fallback (AC-3, DTS, TrueHD)
  - handoff to an external player (VLC or MX Player)
- **Movies & series:**
  - Continue Watching, resume from the last position, and Unwatch to clear history
  - Next Episode and autoplay
  - favorites by long-press
  - Sync any single category on demand
- **Categories:** reorder, hide or show, and PIN-lock categories. Hidden categories are skipped during sync.
- **Recording:** scheduled DVR, offline VOD downloads and program reminders.
- **Backup:**
  - Google Drive sync
  - export and import of settings and favorites
  - export and import of provider details
- **TV UX:**
  - a collapsible sidebar or a top bar
  - focus restored after you go back
  - search that only runs when you submit
  - remappable color buttons
  - Glass, Dark and Light themes

## Installation

**APK:** [tinyurl.com/darshiniapp](https://tinyurl.com/darshiniapp)

**Fire TV Stick**
1. Turn on developer mode: **Settings → My Fire TV → About**, then press Select on the device name 7 times.
2. Install **Downloader** from the app store, then enable it under **Settings → My Fire TV → Developer Options → Install unknown apps**.
3. Open Downloader, enter `https://tinyurl.com/darshiniapp`, and choose **Install**. When it finishes, delete the downloaded file.

**Google TV / Android TV**
1. Turn on developer mode: **Settings → System → About**, then press **Android TV OS Build** 7 times.
2. Install **Downloader** from the Play Store, then enable it under **Settings → Apps → Security & Restrictions → Unknown sources**.
3. Open Downloader, enter `https://tinyurl.com/darshiniapp`, and choose **Install**. If Play Protect warns you, choose **More details → Install anyway**.

**Build from source** (requires JDK 17 and the Android SDK)
```bash
./gradlew assembleRelease
```

---

<sub>Darshini is an IPTV client, not a content provider. Use only sources you are authorized to access. See [LICENSE](LICENSE).</sub>
