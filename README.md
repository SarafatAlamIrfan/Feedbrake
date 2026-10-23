# 🎬 Reel Counter (Android)

A digital wellbeing app and live counter for short-form video feeds (**Instagram Reels**, **TikTok**, **YouTube Shorts**, and **Facebook Reels**).

Features an interactive **Dynamic Island** overlay anchored to your front camera punch-hole, an Android **Home Screen Widget**, and an **OLED Dark Mode Dashboard**.

---

## 🌟 Highlights

### 🏝️ 1. Dynamic Island Live Overlay
- **Punch-Hole Cutout Detection**: Uses Android DisplayCutout API to dynamically wrap the pill around your front camera cutout.
- **Micro Spring Animations**: Springs open with a +1 count pulse on every reel swipe.
- **Dynamic Dopamine Meter**:
  - 🟢 **0–15 reels**: Calm emerald/cyan (*Mindful*)
  - 🟡 **15–35 reels**: Electric amber (*Caution*)
  - 🔴 **50+ reels**: Burning crimson (*Doomscroll Alert!*)
- **Tap to Expand**: Tap the island while in Instagram/TikTok to reveal elapsed time, current platform, and quick pause controls.

### 📱 2. Multi-Platform Detection Engine
- **Pluggable Strategy Detectors** via ReelAccessibilityService:
  - InstagramDetector: Tracks Instagram Reels (com.instagram.android)
  - TikTokDetector: Tracks TikTok vertical feed (com.zhiliaoapp.musically)
  - YouTubeShortsDetector: Tracks YouTube Shorts (com.google.android.youtube)
  - FacebookDetector: Tracks Facebook Reels (com.facebook.katana)
  - GenericDetector: Heuristic fallback for any full-screen vertical snap video feeds.
- **Smart Debounce Engine**: Minimum viewing threshold (1.4s) to eliminate accidental rapid double-swipes or bouncy scrolling.
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

## 🛠️ Architecture & Tech Stack

- **UI**: Jetpack Compose (BOM 2024.06.00) + Material 3
- **Widgets**: Jetpack Glance (ndroidx.glance:glance-appwidget)
- **Overlay**: SYSTEM_ALERT_WINDOW + WindowManager with LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
- **Detection**: Android AccessibilityService
- **Database**: Room Database with Kotlin Coroutines & Flow
- **Language**: Kotlin 2.0.0
- **Min SDK**: API 28 (Android 9.0 Pie - required for hardware DisplayCutout APIs)
- **Target SDK**: API 34 (Android 14)

---

## 🚀 How to Run

1. Open **Android Studio**.
2. Select **Open** and choose D:\Personal projects\ReelCounter.
3. Let Gradle sync and download dependencies.
4. Connect an Android phone (or launch an Android emulator with API 28+).
5. Click **Run** (Shift + F10).
6. In the app:
   - Tap **Enable** on *Accessibility Service* and turn on **Reel Counter**.
   - Tap **Grant** on *Dynamic Island Overlay* and allow **Display over other apps**.
7. Open Instagram Reels, TikTok, or YouTube Shorts — your Dynamic Island will appear around the camera cutout with live counting!
