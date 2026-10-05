# Release Notes Template - VoiceCraft AI

Use this template when publishing a new version tag (e.g., `v1.0.0`, `v1.0.1`, `v1.1.0`).

---

## [v1.0.0] - Official Release

### 🚀 What's New
- **Multi-Language Speech Synthesis**: Natural voice generation in Hindi, English (US/UK), Bengali, Marathi, Tamil, Telugu, Gujarati, Punjabi, Kannada, and Malayalam.
- **5-Second Voice Auditions**: Instant narrator preview clips for every voice catalog entry with local audio caching.
- **MP3 & WAV Format Selection**: Export either universal compressed MP3 or studio-master lossless 24kHz PCM WAV.
- **YouTube Shorts Script Builder**: Guided Hook, Main Narrative, and CTA inputs with word/duration estimation.
- **Audio Storage & Sharing**: Scoped MediaStore export directly into `Music/VoiceCraft` and Android native share sheet integration.
- **Project History**: Room database integration for offline recall, playback, and management.

### 🐛 Bug Fixes
- Fixed UTF-8 character preservation across multi-chunk Devanagari Hindi text scripts.
- Resolved audio player duration formatting and scrub position synchronization.
- Fixed scoped storage compatibility on Android 10+ (API 29–36).

### ⚡ Performance Improvements
- Added local memory and disk caching for narrator preview samples, reducing latency to < 50ms for repeat auditions.
- Optimized text chunking algorithm to split along natural sentence punctuation boundaries.
- Streamlined incremental build and APK size optimizations.

### ⚠️ Known Issues
- Very long scripts (> 5,000 words) may require several seconds to synthesize across multiple chunk requests.
- First-time background initialization of on-device neural TTS engine requires ~1-2 seconds on older devices.
