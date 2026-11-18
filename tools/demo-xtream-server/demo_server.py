#!/usr/bin/env python3
"""Fake Xtream Codes server with fictional demo content.

Used to capture README screenshots without exposing real channel names,
posters, or logos. Every title, channel, logo, and poster is invented and
generated on the fly with Pillow; the video is an abstract ffmpeg test clip.

    python3 demo_server.py [--port 8090]

From the Android emulator the server is reachable at http://10.0.2.2:8090
(username: demo, password: demo).
"""

from __future__ import annotations

import argparse
import base64
import colorsys
import hashlib
import io
import json
import math
import random
import shutil
import subprocess
import time
from datetime import datetime, timedelta, timezone
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from urllib.parse import parse_qs, quote, unquote, urlparse

from PIL import Image, ImageDraw, ImageFilter, ImageFont

ROOT = Path(__file__).resolve().parent
CACHE = ROOT / ".cache"
HLS_DIR = CACHE / "hls"
FONT_DIR = Path("/System/Library/Fonts/Supplemental")
FONTS = {
    "futura": (FONT_DIR / "Futura.ttc", 2),  # Futura Condensed ExtraBold-ish face index
    "didot": (FONT_DIR / "Didot.ttc", 2),
    "rockwell": (FONT_DIR / "Rockwell.ttc", 1),
    "gill": (FONT_DIR / "GillSans.ttc", 1),
    "impact": (FONT_DIR / "Impact.ttf", 0),
    "georgia": (FONT_DIR / "Georgia Bold.ttf", 0),
    "arial": (FONT_DIR / "Arial Bold.ttf", 0),
}

# --------------------------------------------------------------------------
# Fictional catalog
# --------------------------------------------------------------------------

LIVE_CATEGORIES = [
    ("101", "News"),
    ("102", "Sports"),
    ("103", "Movies"),
    ("104", "Kids"),
    ("105", "Music"),
    ("106", "Documentary"),
]

CHANNELS = [
    # (id, name, category, short label for logo)
    (1001, "Aurora News 24", "101", "AN"),
    (1002, "Meridian World", "101", "MW"),
    (1003, "Civic Report HD", "101", "CR"),
    (1004, "Daybreak Business", "101", "DB"),
    (2001, "Summit Sports 1", "102", "S1"),
    (2002, "Summit Sports 2", "102", "S2"),
    (2003, "Pitchside Live", "102", "PL"),
    (2004, "Velocity Motor", "102", "VM"),
    (2005, "Courtline Tennis", "102", "CT"),
    (3001, "Nova Cinema", "103", "NC"),
    (3002, "Reel Classics", "103", "RC"),
    (3003, "Midnight Thriller", "103", "MT"),
    (3004, "Lantern Family", "103", "LF"),
    (4001, "Lumen Kids", "104", "LK"),
    (4002, "Pebble Junior", "104", "PJ"),
    (4003, "Kite Cartoons", "104", "KC"),
    (5001, "Harbor Music", "105", "HM"),
    (5002, "Groove Avenue", "105", "GA"),
    (5003, "Raga Radio TV", "105", "RR"),
    (6001, "Terra Explorer", "106", "TE"),
    (6002, "Deep Blue Planet", "106", "DP"),
    (6003, "History Atlas", "106", "HA"),
]

PROGRAMMES = {
    "101": ["Morning Briefing", "World Desk", "Market Watch", "Evening Headlines", "Global Roundtable",
            "Weather Outlook", "The Policy Hour", "Tech Pulse", "Late Edition"],
    "102": ["Matchday Live", "Goal Rush", "Highlights Tonight", "Inside the Paddock", "Final Whistle",
            "Training Ground", "The Big Rally", "Pre-Game Show", "Classic Replays"],
    "103": ["The Last Lighthouse", "Glass Horizon", "Paper Kingdoms", "Saffron Skies", "Northbound",
            "The Quiet Engine", "Velvet Hour", "Orchard Road", "Silver Lining Express"],
    "104": ["Pip & Pebble", "Sky Builders", "The Puzzle Patrol", "Little Comets", "Story Time Garden",
            "Robo Rangers", "Jungle Junction", "Bubble Lab", "Moonbeam Tales"],
    "105": ["Top 20 Countdown", "Acoustic Sessions", "Night Grooves", "Studio Live", "Classic Hits",
            "Fresh Finds", "Indie Hour", "Chill Lounge", "Dance Floor"],
    "106": ["Wild Coasts", "Secrets of the Reef", "Ancient Roads", "Frozen Frontiers", "Rivers of Life",
            "Lost Cities", "Desert Bloom", "Sky Giants", "The Living Forest"],
}

BLURBS = {
    "101": "Headlines, interviews and analysis from around the world.",
    "102": "Live coverage, expert commentary and all the key moments.",
    "103": "A feature presentation from the channel's film library.",
    "104": "Bright, gentle adventures for younger viewers.",
    "105": "Non-stop music videos and live performances.",
    "106": "Stunning landscapes and the stories behind them.",
}

VOD_CATEGORIES = [
    ("201", "New Releases"),
    ("202", "Action & Adventure"),
    ("203", "Drama"),
    ("204", "Comedy"),
    ("205", "Sci-Fi & Fantasy"),
    ("206", "Family"),
    ("207", "Documentary"),
]

MOVIES = [
    ("The Last Lighthouse", "201", 2026, 8.4, "Drama, Mystery"),
    ("Glass Horizon", "201", 2026, 7.9, "Sci-Fi, Thriller"),
    ("Saffron Skies", "201", 2025, 8.1, "Romance, Drama"),
    ("Northbound", "201", 2026, 7.2, "Adventure"),
    ("The Quiet Engine", "201", 2025, 7.6, "Thriller"),
    ("Velvet Hour", "201", 2026, 6.9, "Crime, Drama"),
    ("Iron Meridian", "202", 2024, 7.4, "Action"),
    ("Storm Runners", "202", 2025, 7.0, "Action, Adventure"),
    ("Edge of Ember", "202", 2023, 6.8, "Action"),
    ("The Cobalt Heist", "202", 2025, 7.7, "Action, Crime"),
    ("Falcon Ridge", "202", 2024, 7.1, "Western, Action"),
    ("Thunder Basin", "202", 2026, 6.6, "Action"),
    ("Paper Kingdoms", "203", 2024, 8.3, "Drama"),
    ("Orchard Road", "203", 2025, 7.8, "Drama, Family"),
    ("A Winter in Lisbon", "203", 2023, 7.5, "Drama, Romance"),
    ("The Weight of Rain", "203", 2025, 8.0, "Drama"),
    ("Small Hours", "203", 2024, 7.3, "Drama"),
    ("Second Verse", "203", 2026, 7.0, "Drama, Music"),
    ("The Neighbours Next Door", "204", 2025, 7.2, "Comedy"),
    ("The Wedding Detour", "204", 2024, 6.7, "Comedy, Romance"),
    ("Office of Lost Socks", "204", 2026, 7.4, "Comedy"),
    ("Uncle Bao's Food Truck", "204", 2025, 7.9, "Comedy, Family"),
    ("Accidental Royals", "204", 2023, 6.5, "Comedy"),
    ("Starfall Protocol", "205", 2026, 8.2, "Sci-Fi"),
    ("The Clockwork Sea", "205", 2025, 7.7, "Fantasy, Adventure"),
    ("Echo Station", "205", 2024, 7.1, "Sci-Fi, Horror"),
    ("Ninth Moon", "205", 2026, 7.8, "Sci-Fi"),
    ("The Lantern Keeper", "205", 2023, 7.3, "Fantasy"),
    ("Kite Over the Canyon", "206", 2025, 7.6, "Family, Adventure"),
    ("Professor Puffin", "206", 2024, 7.0, "Animation, Family"),
    ("The Treehouse Club", "206", 2026, 7.4, "Family"),
    ("Moonbeam Express", "206", 2025, 7.9, "Animation"),
    ("Wild Coasts", "207", 2025, 8.5, "Documentary, Nature"),
    ("Secrets of the Reef", "207", 2024, 8.2, "Documentary"),
    ("Ancient Roads", "207", 2026, 7.9, "Documentary, History"),
    ("Sky Giants", "207", 2023, 8.0, "Documentary"),
]

SERIES_CATEGORIES = [
    ("301", "Trending Series"),
    ("302", "Crime & Thriller"),
    ("303", "Comedy Series"),
    ("304", "Sci-Fi Series"),
    ("305", "Kids Series"),
]

SERIES = [
    ("Copper Valley", "301", 2025, 8.6, "Drama, Western", 3),
    ("The Orchard Files", "301", 2026, 8.3, "Mystery", 2),
    ("Harbour Lights", "301", 2024, 7.9, "Drama", 2),
    ("Signal & Noise", "301", 2026, 8.0, "Thriller", 1),
    ("Night Desk", "302", 2025, 8.1, "Crime", 2),
    ("The Fifth Witness", "302", 2024, 7.8, "Crime, Drama", 3),
    ("Blackwater Bay", "302", 2026, 7.6, "Thriller", 1),
    ("Cold Ledger", "302", 2023, 7.4, "Crime", 2),
    ("Flat 4B", "303", 2025, 7.7, "Sitcom", 4),
    ("The Substitute Chef", "303", 2024, 7.3, "Comedy", 2),
    ("Overtime Heroes", "303", 2026, 7.1, "Comedy", 1),
    ("Orbit Nine", "304", 2026, 8.4, "Sci-Fi", 2),
    ("The Glass Archive", "304", 2025, 8.0, "Sci-Fi, Mystery", 1),
    ("Parallel Street", "304", 2024, 7.5, "Sci-Fi", 3),
    ("Pip & Pebble", "305", 2025, 7.9, "Animation, Kids", 3),
    ("Robo Rangers", "305", 2026, 7.2, "Animation", 2),
    ("Jungle Junction", "305", 2024, 7.0, "Kids", 2),
]

EPISODE_TITLES = [
    "Pilot", "The Long Way Home", "Crossroads", "Undertow", "Paper Trail", "First Light",
    "The Visitor", "Fault Lines", "Homecoming", "Blind Spot", "Turning Tide", "The Reckoning",
]

PLOT = (
    "When an old map surfaces in a seaside town, two estranged siblings set out on a "
    "journey that forces them to confront the secrets their family left behind."
)
CAST = "Alex Rivers, Jordan Vale, Sam Ortega, Priya Natarajan"
DIRECTOR = "Morgan Ellis"


def stable_rng(key: str) -> random.Random:
    return random.Random(int(hashlib.sha256(key.encode()).hexdigest()[:12], 16))


def slug(text: str) -> str:
    return "".join(c.lower() if c.isalnum() else "-" for c in text).strip("-")


def movie_id(index: int) -> int:
    return 7000 + index


def series_id(index: int) -> int:
    return 8000 + index


# --------------------------------------------------------------------------
# Artwork
# --------------------------------------------------------------------------


def font(name: str, size: int) -> ImageFont.FreeTypeFont:
    path, index = FONTS[name]
    try:
        return ImageFont.truetype(str(path), size, index=index)
    except OSError:
        return ImageFont.truetype(str(FONTS["arial"][0]), size)


def palette(rng: random.Random) -> list[tuple[int, int, int]]:
    base = rng.random()
    out = []
    for offset, sat, val in ((0, 0.65, 0.22), (0.08, 0.7, 0.55), (0.5, 0.55, 0.95), (0.12, 0.35, 0.98)):
        r, g, b = colorsys.hsv_to_rgb((base + offset) % 1.0, sat, val)
        out.append((int(r * 255), int(g * 255), int(b * 255)))
    return out


def gradient(size: tuple[int, int], top, bottom) -> Image.Image:
    w, h = size
    img = Image.new("RGB", size)
    draw = ImageDraw.Draw(img)
    for y in range(h):
        t = y / max(1, h - 1)
        draw.line([(0, y), (w, y)], fill=tuple(int(top[i] + (bottom[i] - top[i]) * t) for i in range(3)))
    return img


def scenic_art(key: str, size: tuple[int, int]) -> Image.Image:
    """Abstract landscape: gradient sky, sun/moon, layered hills, glow."""
    rng = stable_rng(key)
    dark, mid, accent, light = palette(rng)
    w, h = size
    img = gradient(size, mid, dark)
    layer = Image.new("RGBA", size, (0, 0, 0, 0))
    d = ImageDraw.Draw(layer)
    # Sun / moon
    r = int(min(w, h) * rng.uniform(0.12, 0.22))
    cx, cy = int(w * (rng.uniform(0.25, 0.75) if h > w else rng.uniform(0.8, 0.9))), int(h * (rng.uniform(0.4, 0.5) if h > w else rng.uniform(0.18, 0.4)))
    d.ellipse([cx - r, cy - r, cx + r, cy + r], fill=accent + (235,))
    glow = layer.filter(ImageFilter.GaussianBlur(r * 0.6))
    img = Image.alpha_composite(img.convert("RGBA"), glow)
    img = Image.alpha_composite(img, layer)
    # Hills
    hills = Image.new("RGBA", size, (0, 0, 0, 0))
    hd = ImageDraw.Draw(hills)
    for i in range(4):
        base_y = h * (0.5 + i * 0.12)
        amp = h * rng.uniform(0.04, 0.1)
        freq = rng.uniform(1.0, 2.6)
        phase = rng.uniform(0, math.tau)
        shade = 0.85 - i * 0.2
        color = tuple(int(c * shade) for c in dark)
        pts = [(x, base_y + amp * math.sin(freq * x / w * math.tau + phase)) for x in range(0, w + 8, 8)]
        hd.polygon(pts + [(w, h), (0, h)], fill=color + (255,))
    img = Image.alpha_composite(img, hills)
    # Stars / particles
    sparks = ImageDraw.Draw(img)
    for _ in range(int(w * h / 9000)):
        x, y = rng.randrange(w), rng.randrange(int(h * 0.45))
        s = rng.choice((1, 1, 2))
        sparks.ellipse([x, y, x + s, y + s], fill=light + (rng.randrange(90, 200),))
    return img


def wrap(text: str, fnt: ImageFont.FreeTypeFont, width: int, draw: ImageDraw.ImageDraw) -> list[str]:
    words, lines, line = text.split(), [], ""
    for word in words:
        trial = f"{line} {word}".strip()
        if draw.textlength(trial, font=fnt) <= width or not line:
            line = trial
        else:
            lines.append(line)
            line = word
    if line:
        lines.append(line)
    return lines


def poster(title: str, year: int | None, size=(400, 600)) -> Image.Image:
    img = scenic_art("poster:" + title, size)
    rng = stable_rng("font:" + title)
    face = rng.choice(["futura", "didot", "rockwell", "impact", "georgia", "gill"])
    w, h = size
    # Darken the top so the title reads; the app draws its own label at the bottom.
    shade = Image.new("RGBA", (w, h // 2), (0, 0, 0, 0))
    mask = Image.linear_gradient("L").rotate(180).resize((w, h // 2))
    shade.putalpha(mask.point(lambda v: int(v * 0.75)))
    img.alpha_composite(Image.composite(Image.new("RGBA", shade.size, (0, 0, 0, 255)), shade, shade.getchannel("A")), (0, 0))
    draw = ImageDraw.Draw(img)
    upper = title.upper() if face in ("futura", "impact", "gill") else title
    size_px = 54
    while size_px > 26:
        fnt = font(face, size_px)
        lines = wrap(upper, fnt, int(w * 0.84), draw)
        if len(lines) <= 3:
            break
        size_px -= 4
    line_h = int(size_px * 1.08)
    y = 112
    for line in lines:
        tw = draw.textlength(line, font=fnt)
        draw.text(((w - tw) / 2 + 2, y + 2), line, font=fnt, fill=(0, 0, 0, 160))
        draw.text(((w - tw) / 2, y), line, font=fnt, fill=(255, 255, 255, 255))
        y += line_h
    return img.convert("RGB")


def backdrop(title: str, size=(1280, 720)) -> Image.Image:
    img = scenic_art("backdrop:" + title, size).convert("RGB")
    return Image.blend(img, Image.new("RGB", size, (8, 10, 20)), 0.35)


def logo(label: str, name: str, size=(256, 256)) -> Image.Image:
    rng = stable_rng("logo:" + name)
    _, mid, accent, light = palette(rng)
    w, h = size
    img = Image.new("RGBA", size, (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    shape = rng.choice(["circle", "rounded", "diamond"])
    pad = 18
    if shape == "circle":
        d.ellipse([pad, pad, w - pad, h - pad], fill=mid + (255,))
        d.ellipse([pad + 18, pad + 18, w - pad - 18, h - pad - 18], outline=accent + (255,), width=8)
    elif shape == "rounded":
        d.rounded_rectangle([pad, pad + 30, w - pad, h - pad - 30], radius=36, fill=mid + (255,))
        d.rectangle([pad, h - pad - 52, w - pad, h - pad - 36], fill=accent + (255,))
    else:
        d.polygon([(w / 2, pad), (w - pad, h / 2), (w / 2, h - pad), (pad, h / 2)], fill=mid + (255,))
    fnt = font("futura", 96)
    tw = d.textlength(label, font=fnt)
    bbox = d.textbbox((0, 0), label, font=fnt)
    th = bbox[3] - bbox[1]
    d.text(((w - tw) / 2, (h - th) / 2 - bbox[1] - 4), label, font=fnt, fill=light + (255,))
    return img


def render(path: Path, builder) -> Path:
    if not path.exists():
        path.parent.mkdir(parents=True, exist_ok=True)
        img = builder()
        if path.suffix == ".png":
            img.save(path, "PNG", optimize=True)
        else:
            img.convert("RGB").save(path, "JPEG", quality=88)
    return path


def image_for(kind: str, name: str) -> Path | None:
    name = unquote(name)
    stem = name.rsplit(".", 1)[0]
    if kind == "logo":
        match = next((c for c in CHANNELS if slug(c[1]) == stem), None)
        if match:
            return render(CACHE / "logo" / f"{stem}.png", lambda: logo(match[3], match[1]))
    if kind == "poster":
        title, year = title_year(stem)
        if title:
            return render(CACHE / "poster" / f"{stem}.jpg", lambda: poster(title, year))
    if kind == "backdrop":
        title, _ = title_year(stem)
        if title:
            return render(CACHE / "backdrop" / f"{stem}.jpg", lambda: backdrop(title))
    if kind == "still":
        return render(CACHE / "still" / f"{stem}.jpg", lambda: backdrop(stem, (640, 360)))
    return None


def title_year(stem: str) -> tuple[str | None, int | None]:
    for title, _cat, year, *_ in MOVIES:
        if slug(title) == stem:
            return title, year
    for title, _cat, year, *_ in SERIES:
        if slug(title) == stem:
            return title, year
    return None, None


# --------------------------------------------------------------------------
# Video
# --------------------------------------------------------------------------


def demo_video() -> Path:
    out = CACHE / "demo.mp4"
    if out.exists():
        return out
    CACHE.mkdir(parents=True, exist_ok=True)
    still = CACHE / "video-still.jpg"
    scenic_art("video:demo", (1920, 1080)).convert("RGB").save(still, "JPEG", quality=92)
    subprocess.run(
        [
            "ffmpeg", "-y", "-loglevel", "error",
            "-loop", "1", "-framerate", "25", "-i", str(still),
            "-f", "lavfi", "-i", "sine=frequency=220:sample_rate=48000",
            "-t", "120", "-filter:a", "volume=0.05",
            "-vf", "zoompan=z='1+0.15*sin(on/3000*PI)':x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)':d=1:s=1280x720:fps=25,format=yuv420p",
            "-c:v", "libx264", "-preset", "veryfast", "-g", "50",
            "-c:a", "aac", "-b:a", "96k", "-movflags", "+faststart", str(out),
        ],
        check=True,
    )
    return out


def start_live_hls() -> subprocess.Popen:
    """One looping live HLS feed shared by every channel."""
    if HLS_DIR.exists():
        shutil.rmtree(HLS_DIR)
    HLS_DIR.mkdir(parents=True)
    proc = subprocess.Popen(
        ["ffmpeg", "-loglevel", "error", "-re", "-stream_loop", "-1", "-i", str(demo_video()),
         "-c", "copy", "-f", "hls", "-hls_time", "4", "-hls_list_size", "6",
         "-hls_flags", "delete_segments+omit_endlist", "-hls_segment_filename", str(HLS_DIR / "seg%05d.ts"),
         str(HLS_DIR / "live.m3u8")],
    )
    for _ in range(100):
        if (HLS_DIR / "live.m3u8").exists():
            break
        time.sleep(0.2)
    return proc


# --------------------------------------------------------------------------
# API payloads
# --------------------------------------------------------------------------


class Catalog:
    def __init__(self, base: str):
        self.base = base

    def img(self, kind: str, name: str, ext: str) -> str:
        return f"{self.base}/img/{kind}/{quote(slug(name))}.{ext}"

    def auth(self) -> dict:
        now = int(time.time())
        host = urlparse(self.base)
        return {
            "user_info": {
                "username": "demo", "password": "demo", "message": "Welcome to the Darshini demo",
                "auth": 1, "status": "Active", "exp_date": str(now + 365 * 86400), "is_trial": "0",
                "active_cons": "0", "created_at": str(now - 86400), "max_connections": "2",
                "allowed_output_formats": ["m3u8", "ts"],
            },
            "server_info": {
                "url": host.hostname, "port": str(host.port or 80), "https_port": "443",
                "server_protocol": "http", "rtmp_port": "1935", "timezone": "UTC",
                "timestamp_now": now, "time_now": datetime.now(timezone.utc).strftime("%Y-%m-%d %H:%M:%S"),
            },
        }

    @staticmethod
    def categories(rows) -> list[dict]:
        return [{"category_id": cid, "category_name": name, "parent_id": 0} for cid, name in rows]

    def live_streams(self, category: str | None) -> list[dict]:
        out = []
        for num, (sid, name, cat, _label) in enumerate(CHANNELS, start=1):
            if category and category != cat:
                continue
            out.append({
                "num": num, "name": name, "stream_type": "live", "stream_id": sid,
                "stream_icon": self.img("logo", name, "png"), "epg_channel_id": f"{slug(name)}.demo",
                "added": "1767225600", "category_id": cat, "category_ids": [int(cat)],
                "custom_sid": "", "tv_archive": 1 if cat in ("101", "102") else 0, "direct_source": "",
                "tv_archive_duration": 3 if cat in ("101", "102") else 0,
            })
        return out

    def vod_streams(self, category: str | None) -> list[dict]:
        out = []
        for i, (title, cat, year, rating, _genre) in enumerate(MOVIES):
            if category and category != cat:
                continue
            out.append({
                "num": i + 1, "name": f"{title} ({year})", "stream_type": "movie", "stream_id": movie_id(i),
                "stream_icon": self.img("poster", title, "jpg"), "rating": f"{rating}",
                "rating_5based": round(rating / 2, 1), "added": str(1767225600 + i * 3600),
                "category_id": cat, "category_ids": [int(cat)], "container_extension": "mp4",
                "custom_sid": "", "direct_source": "", "is_adult": 0,
            })
        return out

    def vod_info(self, vid: int) -> dict:
        idx = vid - 7000
        if not 0 <= idx < len(MOVIES):
            return {"info": [], "movie_data": {}}
        title, cat, year, rating, genre = MOVIES[idx]
        runtime = 88 + (idx * 7) % 50
        return {
            "info": {
                "movie_image": self.img("poster", title, "jpg"), "tmdb_id": "", "plot": PLOT, "cast": CAST,
                "director": DIRECTOR, "genre": genre, "releasedate": f"{year}-0{1 + idx % 9}-15",
                "rating": f"{rating}", "rating_5based": round(rating / 2, 1), "youtube_trailer": "",
                "duration_secs": runtime * 60, "duration": f"{runtime // 60:02d}:{runtime % 60:02d}:00",
                "backdrop_path": [self.img("backdrop", title, "jpg")], "bitrate": 4500,
            },
            "movie_data": {
                "stream_id": vid, "name": f"{title} ({year})", "added": "1767225600", "category_id": cat,
                "container_extension": "mp4", "custom_sid": "", "direct_source": "",
            },
        }

    def series_row(self, i: int) -> dict:
        title, cat, year, rating, genre, _seasons = SERIES[i]
        return {
            "num": i + 1, "name": title, "series_id": series_id(i), "cover": self.img("poster", title, "jpg"),
            "plot": PLOT, "cast": CAST, "director": DIRECTOR, "genre": genre, "releaseDate": f"{year}-03-01",
            "last_modified": str(1767225600 + i * 7200), "rating": f"{rating}",
            "rating_5based": round(rating / 2, 1), "backdrop_path": [self.img("backdrop", title, "jpg")],
            "youtube_trailer": "", "episode_run_time": "45", "category_id": cat, "category_ids": [int(cat)],
        }

    def series(self, category: str | None) -> list[dict]:
        return [self.series_row(i) for i, s in enumerate(SERIES) if not category or category == s[1]]

    def series_info(self, sid: int) -> dict:
        i = sid - 8000
        if not 0 <= i < len(SERIES):
            return {"seasons": [], "info": {}, "episodes": {}}
        title, _cat, year, _rating, _genre, season_count = SERIES[i]
        seasons, episodes = [], {}
        for s in range(1, season_count + 1):
            count = 8 if s < season_count else 6
            seasons.append({
                "season_number": s, "name": f"Season {s}", "episode_count": count,
                "air_date": f"{year - season_count + s}-03-01", "cover": self.img("poster", title, "jpg"),
            })
            eps = []
            for e in range(1, count + 1):
                eid = sid * 1000 + s * 100 + e
                ep_title = EPISODE_TITLES[(e - 1 + s) % len(EPISODE_TITLES)]
                eps.append({
                    "id": str(eid), "episode_num": e, "title": f"{title} - S{s:02d}E{e:02d} - {ep_title}",
                    "container_extension": "mp4", "custom_sid": "", "added": "1767225600", "season": s,
                    "direct_source": "",
                    "info": {
                        "movie_image": f"{self.base}/img/still/{slug(title)}-{s}-{e}.jpg",
                        "plot": f"Episode {e} of season {s}. {PLOT}", "releasedate": f"{year}-04-{e:02d}",
                        "rating": "7.8", "duration_secs": 2700, "duration": "00:45:00", "name": ep_title,
                        "bitrate": 3500,
                    },
                })
            episodes[str(s)] = eps
        return {"seasons": seasons, "info": self.series_row(i), "episodes": episodes}

    # ---- EPG -------------------------------------------------------------

    @staticmethod
    def schedule(channel_index: int, cat: str):
        """Yield (start, stop, title) covering roughly -6h .. +30h, aligned to 30 minutes."""
        now = datetime.now(timezone.utc)
        start = now.replace(minute=0, second=0, microsecond=0) - timedelta(hours=6)
        end = now + timedelta(hours=30)
        titles = PROGRAMMES[cat]
        rng = stable_rng(f"epg:{channel_index}")
        k = channel_index
        t = start
        while t < end:
            length = timedelta(minutes=rng.choice((30, 30, 60, 60, 90, 120)))
            yield t, t + length, titles[k % len(titles)]
            k += 1
            t += length

    def short_epg(self, stream_id: int, limit: int) -> dict:
        idx = next((i for i, c in enumerate(CHANNELS) if c[0] == stream_id), None)
        if idx is None:
            return {"epg_listings": []}
        sid, name, cat, _ = CHANNELS[idx]
        now = datetime.now(timezone.utc)
        listings = []
        for start, stop, title in self.schedule(idx, cat):
            if stop <= now and limit:
                continue
            listings.append({
                "id": str(len(listings) + 1), "epg_id": "1", "lang": "en", "channel_id": f"{slug(name)}.demo",
                "title": base64.b64encode(title.encode()).decode(),
                "description": base64.b64encode(BLURBS[cat].encode()).decode(),
                "start": start.strftime("%Y-%m-%d %H:%M:%S"), "end": stop.strftime("%Y-%m-%d %H:%M:%S"),
                "start_timestamp": int(start.timestamp()), "stop_timestamp": int(stop.timestamp()),
                "now_playing": 1 if start <= now < stop else 0, "has_archive": 0,
            })
            if limit and len(listings) >= limit:
                break
        return {"epg_listings": listings}

    def xmltv(self) -> str:
        fmt = "%Y%m%d%H%M%S +0000"
        esc = lambda s: s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace('"', "&quot;")
        parts = ['<?xml version="1.0" encoding="UTF-8"?>', '<tv generator-info-name="darshini-demo">']
        for _sid, name, _cat, _ in CHANNELS:
            parts.append(
                f'<channel id="{slug(name)}.demo"><display-name>{esc(name)}</display-name>'
                f'<icon src="{esc(self.img("logo", name, "png"))}"/></channel>'
            )
        for idx, (_sid, name, cat, _) in enumerate(CHANNELS):
            for start, stop, title in self.schedule(idx, cat):
                parts.append(
                    f'<programme start="{start.strftime(fmt)}" stop="{stop.strftime(fmt)}" channel="{slug(name)}.demo">'
                    f'<title lang="en">{esc(title)}</title>'
                    f'<desc lang="en">{esc(BLURBS[cat])}</desc></programme>'
                )
        parts.append("</tv>")
        return "\n".join(parts)


# --------------------------------------------------------------------------
# HTTP
# --------------------------------------------------------------------------


class Handler(BaseHTTPRequestHandler):
    protocol_version = "HTTP/1.1"

    def log_message(self, fmt, *args):  # quieter logs
        print(f"{self.address_string()} {self.command} {self.path[:140]}", flush=True)

    def catalog(self) -> Catalog:
        host = self.headers.get("Host") or f"127.0.0.1:{self.server.server_port}"
        return Catalog(f"http://{host}")

    def send_bytes(self, body: bytes, ctype: str, status: int = 200):
        self.send_response(status)
        self.send_header("Content-Type", ctype)
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        if self.command != "HEAD":
            self.wfile.write(body)

    def send_json(self, payload):
        self.send_bytes(json.dumps(payload).encode(), "application/json; charset=utf-8")

    def do_HEAD(self):
        self.do_GET()

    def do_GET(self):
        url = urlparse(self.path)
        q = {k: v[0] for k, v in parse_qs(url.query).items()}
        path = url.path
        cat = self.catalog()
        try:
            if path.endswith("/player_api.php"):
                return self.player_api(cat, q)
            if path.endswith("/xmltv.php"):
                return self.send_bytes(cat.xmltv().encode(), "application/xml; charset=utf-8")
            if path.startswith("/img/"):
                _, _, kind, name = path.split("/", 3)
                file = image_for(kind, name)
                if not file:
                    return self.send_bytes(b"not found", "text/plain", 404)
                ctype = "image/png" if file.suffix == ".png" else "image/jpeg"
                return self.send_bytes(file.read_bytes(), ctype)
            if path.startswith(("/movie/", "/series/")) or (path.startswith("/live/") and path.endswith(".mp4")):
                return self.send_file_ranged(demo_video(), "video/mp4")
            if path.endswith(".m3u8"):
                return self.send_bytes((HLS_DIR / "live.m3u8").read_bytes(), "application/vnd.apple.mpegurl")
            seg = HLS_DIR / Path(path).name
            if path.endswith(".ts") and Path(path).name.startswith("seg") and seg.exists():
                return self.send_bytes(seg.read_bytes(), "video/mp2t")
            if path.startswith("/live/") or path.count("/") == 3:
                return self.stream_live_ts()
            return self.send_bytes(b"not found", "text/plain", 404)
        except (BrokenPipeError, ConnectionResetError):
            pass

    def player_api(self, cat: Catalog, q: dict):
        action = q.get("action")
        category = q.get("category_id") or None
        if not action:
            return self.send_json(cat.auth())
        handlers = {
            "get_live_categories": lambda: cat.categories(LIVE_CATEGORIES),
            "get_vod_categories": lambda: cat.categories(VOD_CATEGORIES),
            "get_series_categories": lambda: cat.categories(SERIES_CATEGORIES),
            "get_live_streams": lambda: cat.live_streams(category),
            "get_vod_streams": lambda: cat.vod_streams(category),
            "get_series": lambda: cat.series(category),
            "get_vod_info": lambda: cat.vod_info(int(q.get("vod_id", 0))),
            "get_series_info": lambda: cat.series_info(int(q.get("series_id", 0))),
            "get_short_epg": lambda: cat.short_epg(int(q.get("stream_id", 0)), int(q.get("limit", 4) or 4)),
            "get_simple_data_table": lambda: cat.short_epg(int(q.get("stream_id", 0)), 0),
        }
        handler = handlers.get(action)
        return self.send_json(handler() if handler else [])

    def send_file_ranged(self, file: Path, ctype: str):
        size = file.stat().st_size
        rng = self.headers.get("Range")
        start, end = 0, size - 1
        status = 200
        if rng and rng.startswith("bytes="):
            a, _, b = rng[6:].partition("-")
            start = int(a) if a else 0
            end = int(b) if b else size - 1
            status = 206
        length = end - start + 1
        self.send_response(status)
        self.send_header("Content-Type", ctype)
        self.send_header("Accept-Ranges", "bytes")
        self.send_header("Content-Length", str(length))
        if status == 206:
            self.send_header("Content-Range", f"bytes {start}-{end}/{size}")
        self.end_headers()
        if self.command == "HEAD":
            return
        with file.open("rb") as fh:
            fh.seek(start)
            remaining = length
            while remaining > 0:
                chunk = fh.read(min(65536, remaining))
                if not chunk:
                    break
                self.wfile.write(chunk)
                remaining -= len(chunk)

    def stream_live_ts(self):
        """Endless MPEG-TS by looping the demo clip in real time."""
        self.send_response(200)
        self.send_header("Content-Type", "video/mp2t")
        self.send_header("Connection", "close")
        self.end_headers()
        if self.command == "HEAD":
            return
        proc = subprocess.Popen(
            ["ffmpeg", "-loglevel", "error", "-re", "-stream_loop", "-1", "-i", str(demo_video()),
             "-c", "copy", "-f", "mpegts", "pipe:1"],
            stdout=subprocess.PIPE,
        )
        try:
            while chunk := proc.stdout.read(32768):
                self.wfile.write(chunk)
        finally:
            proc.kill()
        self.close_connection = True


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--port", type=int, default=8090)
    parser.add_argument("--clean", action="store_true", help="delete generated artwork and video first")
    args = parser.parse_args()
    if args.clean and CACHE.exists():
        shutil.rmtree(CACHE)
    demo_video()
    start_live_hls()
    server = ThreadingHTTPServer(("0.0.0.0", args.port), Handler)
    print(f"Darshini demo Xtream server on http://0.0.0.0:{args.port}  (emulator: http://10.0.2.2:{args.port}, demo/demo)")
    server.serve_forever()


if __name__ == "__main__":
    main()
