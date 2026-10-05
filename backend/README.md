# VoiceCraft AI - Secure TTS Proxy Backend

This service provides a secure, token-protecting backend proxy between the VoiceCraft AI Android app and Google's official Gemini Text-to-Speech API (`gemini-2.5-flash-preview-tts`).

## Security Architecture

```
[Android Mobile App]
        ↓ (POST /api/tts)
[VoiceCraft Backend Proxy] (Validates text, language, style, protects API keys)
        ↓
[Google Gemini AI Audio API] (gemini-2.5-flash-preview-tts)
        ↓
[Playable Audio Response]
```

## Setup & Deployment

1. **Install Dependencies:**
   ```bash
   npm install
   ```

2. **Configure Environment:**
   Create a `.env` file in `/backend`:
   ```env
   PORT=8080
   GOOGLE_API_KEY=your_google_ai_or_gemini_api_key
   ```

3. **Start the Server:**
   ```bash
   npm start
   ```

4. **Deploy to Google Cloud Run:**
   ```bash
   gcloud run deploy voicecraft-tts-backend \
     --source . \
     --platform managed \
     --region asia-southeast1 \
     --allow-unauthenticated \
     --set-env-vars GOOGLE_API_KEY=your_key_here
   ```

5. **Connect Android App:**
   In VoiceCraft AI on Android:
   Go to **Settings** → **Backend Service & API Key** → Enter your Cloud Run URL (e.g. `https://your-cloud-run.app/api/tts`).
