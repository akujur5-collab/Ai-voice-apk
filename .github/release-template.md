# VoiceCraft AI Release Template

## VoiceCraft AI v1.0.1

**VoiceCraft AI**  
AI-powered Text-to-Speech Android application for creators, narrators, and voice-over artists.

---

### 🌟 Features
- **AI Text to Speech**: Studio-quality speech synthesis powered by Google Gemini AI & Google Cloud Text-to-Speech.
- **Hindi Voice Support**: Native Hindi (Devanagari) voices with natural emotional inflections.
- **English Voice Support**: US & UK English voices suited for narration, audiobooks, and commercials.
- **Indian Regional Languages**: Bengali, Marathi, Tamil, Telugu, Gujarati, Punjabi, Kannada, and Malayalam.
- **Voice Selection & 5-Second Auditions**: Browse curated voices and listen to instant 5-second sample previews before full script generation.
- **Audio Format Selection**: Generate and download in **MP3 (Compressed)** or **WAV (Lossless 24kHz PCM Studio Master)**.
- **Audio Playback**: Full-featured player with waveform equalizer, scrub bar, forward/backward seek, and playback rate adjustment.
- **Audio Download**: One-tap export to Android's public `Music/VoiceCraft` folder using scoped storage.
- **Audio Sharing**: Share directly to WhatsApp, Telegram, YouTube, Drive, and email clients.
- **Generation History**: Offline-accessible Room SQLite database preserving all generated projects and audio files.
- **Dark Mode**: Modern Material 3 dynamic theming with complete system dark and light theme support.
- **Creator Studio Tools**: YouTube script builder (Hook, Body, CTA) and Shorts duration estimator (15s, 30s, 45s, 60s).

---

### 📥 Installation Instructions
1. Download the **`VoiceCraft-AI-v1.0.1.apk`** file from the Assets section below.
2. Open the downloaded APK on your Android phone.
3. If Android prompts you with a security warning, tap **Settings** and allow **Install unknown apps** for your browser or file manager.
4. Tap **Install** to proceed with the installation.
5. Open **VoiceCraft AI** and start creating audio!

---

### 📱 Release Specifications
- **App Version**: `1.0.1` (`versionCode: 2`)
- **Release Date**: Current Release
- **Minimum Android Version**: Android 8.0 (Oreo, API level 26)
- **Target Android Version**: Android 15 (API level 36)
- **Supported Architectures**: Universal APK (`arm64-v8a`, `armeabi-v7a`, `x86_64`)
- **Package Name**: `com.aistudio.voicecraftai.vckdpl`

---

### ⚠️ Known Limitations & Notes
- Scoped storage on Android 10+ stores downloaded audio in `Music/VoiceCraft`.
- Online high-definition AI speech requires active internet connectivity; offline fallback utilizes high-quality on-device neural synthesis.
