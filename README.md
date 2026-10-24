# 🛑 Feedbrake (Android)

> **Put the brakes on your infinite scroll.**

---

### 📲 Download APK
👉 **[Download Feedbrake v1.0.1 APK](https://github.com/SarafatAlamIrfan/Feedbrake/releases/download/v1.0.1/Feedbrake-v1.0.1-debug.apk)** (Direct APK Download)  
📦 **[View GitHub Releases](https://github.com/SarafatAlamIrfan/Feedbrake/releases)** | 🚀 **[Alternative Raw APK Link](https://github.com/SarafatAlamIrfan/Feedbrake/raw/apk-download/Feedbrake-v1.0.1-debug.apk)**

---

A digital wellbeing app and live counter for short-form video feeds (**Instagram Reels**, **TikTok**, **YouTube Shorts**, and **Facebook Reels**).

Features an interactive **Dynamic Island** overlay anchored to your front camera punch-hole, an Android **Home Screen Widget**, and an **OLED Dark Mode Dashboard**.

---

## 🌟 Highlights

### 🏝️ 1. Dynamic Island Live Overlay
- **Punch-Hole Cutout Detection**: Uses Android `DisplayCutout` API to dynamically wrap the pill around your front camera cutout.
- **Micro Spring Animations**: Springs open with a +1 count pulse on every reel swipe.
- **Dynamic Dopamine Meter**:
  - 🟢 **0–20 reels**: Calm emerald/cyan (*Mindful*)
  - 🟡 **20–50 reels**: Electric amber (*Caution*)
  - 🔴 **50+ reels**: Burning crimson (*Doomscroll Alert!*)
- **Tap to Expand**: Tap the island while in Instagram/TikTok to reveal elapsed time, current platform, and quick pause controls.

### 📱 2. Multi-Platform Detection Engine
- **Pluggable Strategy Detectors** via `ReelAccessibilityService`:
  - `InstagramDetector`: Tracks Instagram Reels (`com.instagram.android`)
  - `TikTokDetector`: Tracks TikTok vertical feed (`com.zhiliaoapp.musically`)
  - `YouTubeShortsDetector`: Tracks YouTube Shorts (`com.google.android.youtube`)
  - `FacebookDetector`: Tracks Facebook Reels (`com.facebook.katana`)
  - `GenericDetector`: Heuristic fallback for any full-screen vertical snap video feeds.
- **Smart Debounce Engine**: Minimum viewing threshold (1.4s) to eliminate accidental rapid double-swipes.
- **Haptic Feedback**: Subconscious micro-vibration pulse on each reel swipe.

### 🏠 3. Jetpack Glance Home Screen Widget
- Modern Material You widget for your Android launcher.
- Live today's count, status tag (*Mindful*, *Moderate*, *Doomscroll Alert*), and quick glance metrics.

### ⚡ 4. Futuristic OLED Dashboard
- Pure black OLED design with neon cyan/coral accents.
- Animated circular progress dial tracking daily intake against your custom goal (e.g., 50 reels).
- Per-platform breakdown with percentage distribution.
- One-tap permission setup cards for Accessibility and Overlay.

---

## 🛠️ Tech Stack

- **UI**: Jetpack Compose (BOM 2024.06.00) + Material 3
- **Widgets**: Jetpack Glance (`androidx.glance:glance-appwidget`)
- **Overlay**: `SYSTEM_ALERT_WINDOW` + `WindowManager` with `LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS`
- **Detection**: Android `AccessibilityService`
- **Storage**: Reactive StateFlow & Persistent Storage
- **Language**: Kotlin 2.0.0
- **Min SDK**: API 28 (Android 9.0 Pie)
- **Target SDK**: API 34 (Android 14)
