# Demo Xtream server

A tiny fake Xtream Codes provider with **fictional** content — invented channel names,
movie and series titles, guide listings, and artwork generated on the fly with Pillow.
It exists so README screenshots never show real providers, channels, posters or logos.

## Requirements

- Python 3.10+ with Pillow (`pip install pillow`)
- `ffmpeg` on `PATH` (builds the looping demo video and live HLS feed)
- macOS system fonts (`/System/Library/Fonts/Supplemental`) for poster titles

## Run

```bash
python3 tools/demo-xtream-server/demo_server.py          # port 8090
python3 tools/demo-xtream-server/demo_server.py --clean  # regenerate artwork/video
```

In the app, add an **Xtream Codes** provider:

| Field    | Value                                        |
|----------|----------------------------------------------|
| Server   | `http://10.0.2.2:8090` (Android emulator)    |
| Username | `demo`                                       |
| Password | `demo`                                       |

On a real device use your computer's LAN IP instead of `10.0.2.2`.

## What it serves

- `player_api.php` — auth, live/VOD/series categories and streams, `get_vod_info`,
  `get_series_info`, `get_short_epg`, `get_simple_data_table`
- `xmltv.php` — a 36-hour guide for all 22 channels
- `/img/...` — generated posters, backdrops, episode stills and channel logos
- `/movie/...`, `/series/...` — a 2-minute demo clip (supports range requests)
- `/live/....m3u8` — one shared looping live HLS feed

Generated files live in `.cache/` (git-ignored).

## Capturing screenshots

```bash
adb exec-out screencap -p > shot.png
```

Final README screenshots live in `docs/screenshots/` as 1920×1080 JPEGs.
