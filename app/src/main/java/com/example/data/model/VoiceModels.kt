package com.example.data.model

data class Language(
    val code: String,
    val name: String,
    val localizedName: String,
    val flagEmoji: String,
    val sampleText: String
)

data class Voice(
    val id: String,
    val name: String,
    val languageCode: String,
    val gender: String,
    val description: String,
    val geminiVoiceName: String,
    val samplePreviewText: String,
    val isRecommendedForHindi: Boolean = false,
    val googleCloudVoiceName: String = "",
    val ssmlGender: String = "SSML_VOICE_GENDER_UNSPECIFIED"
)

enum class VoiceStyle(
    val displayName: String,
    val description: String,
    val promptInstruction: String,
    val rateMultiplier: Float = 1.0f,
    val pitchMultiplier: Float = 1.0f
) {
    NATURAL("Natural", "Everyday conversational tone", "Speak in a natural, friendly conversational tone.", 1.0f, 1.0f),
    NARRATOR("Narrator", "Clear, measured narrative cadence", "Speak like a documentary narrator with clear enunciation and measured pacing.", 0.95f, 0.98f),
    STORYTELLING("Storytelling", "Expressive & dramatic storytelling", "Speak like a captivating storyteller with expressive emotion and pauses for suspense.", 0.92f, 1.02f),
    PROFESSIONAL("Professional", "Crisp, business-grade delivery", "Speak in a crisp, confident, and professional broadcast tone.", 1.02f, 1.0f),
    FRIENDLY("Friendly", "Warm and welcoming demeanor", "Speak in a warm, welcoming, and smiling friendly voice.", 1.05f, 1.05f),
    NEWS("News", "Authoritative, fast, objective delivery", "Speak like a seasoned news anchor with authoritative cadence and clear articulation.", 1.1f, 0.98f),
    CALM("Calm", "Soothing, gentle, relaxed flow", "Speak in a soft, calm, and soothing manner with gentle breaths.", 0.88f, 0.95f),
    ENERGETIC("Energetic", "High-energy, punchy, exciting", "Speak with high energy, enthusiasm, and punchy excitement.", 1.15f, 1.08f),
    MOTIVATIONAL("Motivational", "Inspiring, powerful, uplifting", "Speak like an inspiring motivational speaker with strong conviction and uplifting power.", 1.05f, 1.04f),
    DEVOTIONAL("Devotional", "Reverent, spiritual, peaceful", "Speak in a peaceful, reverent, and sacred devotional cadence.", 0.85f, 0.96f),
    EDUCATIONAL("Educational", "Structured, clear, tutorial pace", "Speak clearly like a patient teacher, emphasizing key concepts and pausing slightly after sentences.", 0.95f, 1.0f),
    ADVERTISEMENT("Advertisement", "Persuasive, attention-grabbing", "Speak like a charismatic commercial voiceover, highlighting benefits with engaging excitement.", 1.12f, 1.06f)
}

enum class ShortsPreset(val seconds: Int, val label: String, val approxWords: Int, val approxChars: Int) {
    FIFTEEN(15, "15s (Shorts)", 35, 220),
    THIRTY(30, "30s (Reels)", 75, 450),
    FORTY_FIVE(45, "45s (Video)", 115, 680),
    SIXTY(60, "60s (Max)", 155, 920)
}

data class SampleScript(
    val title: String,
    val category: String,
    val languageCode: String,
    val text: String
)
