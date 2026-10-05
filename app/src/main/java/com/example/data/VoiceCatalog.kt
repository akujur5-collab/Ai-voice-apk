package com.example.data

import com.example.data.model.Language
import com.example.data.model.SampleScript
import com.example.data.model.Voice

object VoiceCatalog {

    val supportedLanguages: List<Language> = listOf(
        Language("hi-IN", "Hindi", "हिन्दी", "🇮🇳", "नमस्ते, VoiceCraft AI में आपका स्वागत है।"),
        Language("en-US", "English (US)", "English (US)", "🇺🇸", "Welcome to VoiceCraft AI text-to-speech studio."),
        Language("en-GB", "English (UK)", "English (UK)", "🇬🇧", "Greetings, welcome to VoiceCraft AI audio studio."),
        Language("bn-IN", "Bengali", "বাংলা", "🇮🇳", "নমস্কার, ভয়েসক্ল্যাক্ট এআই-তে আপনাকে স্বাগতম।"),
        Language("mr-IN", "Marathi", "मराठी", "🇮🇳", "नमस्कार, व्हॉईसक्राफ्ट एआय मध्ये आपले स्वागत आहे."),
        Language("ta-IN", "Tamil", "தமிழ்", "🇮🇳", "வணக்கம், வாய்ஸ்கிராஃப்ட் ஏஐக்கு தங்களை வரவேற்கிறோம்."),
        Language("te-IN", "Telugu", "తెలుగు", "🇮🇳", "నమస్కారం, వాయిస్‌క్రాఫ్ట్ ఏఐ కి స్వాగతం."),
        Language("gu-IN", "Gujarati", "ગુજરાતી", "🇮🇳", "નમસ્તે, વૉઇસક્રાફ્ટ AI માં આપનું સ્વાગત છે."),
        Language("pa-IN", "Punjabi", "ਪੰਜਾਬੀ", "🇮🇳", "ਸਤਿ ਸ੍ਰੀ ਅਕਾਲ, ਵਾਇਸਕ੍ਰਾਫਟ AI ਵਿੱਚ ਤੁਹਾਡਾ ਸੁਆਗਤ ਹੈ।"),
        Language("kn-IN", "Kannada", "ಕನ್ನಡ", "🇮🇳", "ನಮಸ್ಕಾರ, ವಾಯ್ಸ್‌ಕ್ರಾಫ್ಟ್ ಎಐ ಗೆ ಸುಸ್ವಾಗತ."),
        Language("ml-IN", "Malayalam", "മലയാളം", "🇮🇳", "നമസ്കാരം, വോയ്സ്ക്രാഫ്റ്റ് എഐയിലേക്ക് സ്വാഗതം.")
    )

    val supportedVoices: List<Voice> = listOf(
        // Hindi Voices
        Voice(
            id = "hi_in_kore",
            name = "Aanya (Warm & Expressive)",
            languageCode = "hi-IN",
            gender = "Female",
            description = "Natural Hindi voice with warm inflection, ideal for YouTube shorts and stories.",
            geminiVoiceName = "Kore",
            googleCloudVoiceName = "hi-IN-Neural2-A",
            ssmlGender = "FEMALE",
            samplePreviewText = "नमस्ते, यह मेरी AI आवाज़ का छोटा सा नमूना है।",
            isRecommendedForHindi = true
        ),
        Voice(
            id = "hi_in_charon",
            name = "Aarav (Deep Narrator)",
            languageCode = "hi-IN",
            gender = "Male",
            description = "Deep, resonant Hindi narrator voice suited for documentaries, motivation and history.",
            geminiVoiceName = "Charon",
            googleCloudVoiceName = "hi-IN-Neural2-B",
            ssmlGender = "MALE",
            samplePreviewText = "नमस्ते, यह मेरी AI आवाज़ का छोटा सा नमूना है।",
            isRecommendedForHindi = true
        ),
        Voice(
            id = "hi_in_aoede",
            name = "Diya (Melodic & Soft)",
            languageCode = "hi-IN",
            gender = "Female",
            description = "Gentle, melodic cadence great for devotional content, poetry, and bedtime tales.",
            geminiVoiceName = "Aoede",
            googleCloudVoiceName = "hi-IN-Neural2-C",
            ssmlGender = "FEMALE",
            samplePreviewText = "नमस्ते, यह मेरी AI आवाज़ का छोटा सा नमूना है।",
            isRecommendedForHindi = true
        ),
        Voice(
            id = "hi_in_fenrir",
            name = "Kabir (Bold & Motivational)",
            languageCode = "hi-IN",
            gender = "Male",
            description = "Authoritative, commanding tone perfect for inspirational videos and advertisements.",
            geminiVoiceName = "Fenrir",
            googleCloudVoiceName = "hi-IN-Neural2-D",
            ssmlGender = "MALE",
            samplePreviewText = "नमस्ते, यह मेरी AI आवाज़ का छोटा सा नमूना है।",
            isRecommendedForHindi = true
        ),
        Voice(
            id = "hi_in_puck",
            name = "Rohan (Youthful & Energetic)",
            languageCode = "hi-IN",
            gender = "Male",
            description = "Fast, energetic voice tailored for tech reviews, vlogs, and YouTube Shorts hooks.",
            geminiVoiceName = "Puck",
            googleCloudVoiceName = "hi-IN-Wavenet-B",
            ssmlGender = "MALE",
            samplePreviewText = "नमस्ते, यह मेरी AI आवाज़ का छोटा सा नमूना है。"
        ),

        // English (US) Voices
        Voice(
            id = "en_us_kore",
            name = "Kore (Warm Female)",
            languageCode = "en-US",
            gender = "Female",
            description = "Friendly and natural American accent with balanced warmth and clarity.",
            geminiVoiceName = "Kore",
            googleCloudVoiceName = "en-US-Journey-F",
            ssmlGender = "FEMALE",
            samplePreviewText = "Hello, this is a preview of my AI voice."
        ),
        Voice(
            id = "en_us_charon",
            name = "Charon (Documentary Male)",
            languageCode = "en-US",
            gender = "Male",
            description = "Deep and trustworthy baritone voice, ideal for professional narration.",
            geminiVoiceName = "Charon",
            googleCloudVoiceName = "en-US-Neural2-D",
            ssmlGender = "MALE",
            samplePreviewText = "Hello, this is a preview of my AI voice."
        ),
        Voice(
            id = "en_us_aoede",
            name = "Aoede (Expressive Female)",
            languageCode = "en-US",
            gender = "Female",
            description = "Engaging, conversational tone great for audiobooks, education, and social clips.",
            geminiVoiceName = "Aoede",
            googleCloudVoiceName = "en-US-Neural2-F",
            ssmlGender = "FEMALE",
            samplePreviewText = "Hello, this is a preview of my AI voice."
        ),
        Voice(
            id = "en_us_fenrir",
            name = "Fenrir (Bold Male)",
            languageCode = "en-US",
            gender = "Male",
            description = "Strong, impactful voice for promos, workouts, and cinematic trailers.",
            geminiVoiceName = "Fenrir",
            googleCloudVoiceName = "en-US-Neural2-A",
            ssmlGender = "MALE",
            samplePreviewText = "Hello, this is a preview of my AI voice."
        ),
        Voice(
            id = "en_us_puck",
            name = "Puck (Energetic Male)",
            languageCode = "en-US",
            gender = "Male",
            description = "Bright and lively tone designed for fast-paced reels and tutorials.",
            geminiVoiceName = "Puck",
            googleCloudVoiceName = "en-US-Journey-O",
            ssmlGender = "MALE",
            samplePreviewText = "Hello, this is a preview of my AI voice."
        ),

        // English (UK) Voices
        Voice(
            id = "en_gb_olivia",
            name = "Olivia (British Female)",
            languageCode = "en-GB",
            gender = "Female",
            description = "Articulate British female voice with sophisticated diction and polite elegance.",
            geminiVoiceName = "Aoede",
            googleCloudVoiceName = "en-GB-Neural2-A",
            ssmlGender = "FEMALE",
            samplePreviewText = "Hello, this is a preview of my AI voice."
        ),
        Voice(
            id = "en_gb_arthur",
            name = "Arthur (British Male)",
            languageCode = "en-GB",
            gender = "Male",
            description = "Refined British narrator voice suitable for documentaries and long-form literature.",
            geminiVoiceName = "Charon",
            googleCloudVoiceName = "en-GB-Neural2-B",
            ssmlGender = "MALE",
            samplePreviewText = "Hello, this is a preview of my AI voice."
        ),

        // Bengali Voices
        Voice(
            id = "bn_in_shreya",
            name = "Shreya (Bengali Female)",
            languageCode = "bn-IN",
            gender = "Female",
            description = "Sweet and expressive Bengali voice suitable for literature, culture and news.",
            geminiVoiceName = "Kore",
            googleCloudVoiceName = "bn-IN-Wavenet-A",
            ssmlGender = "FEMALE",
            samplePreviewText = "নমস্কার, এটি আমার এআই কণ্ঠস্বরের একটি নমুনা।"
        ),
        Voice(
            id = "bn_in_anirban",
            name = "Anirban (Bengali Male)",
            languageCode = "bn-IN",
            gender = "Male",
            description = "Clear and articulate Bengali male voice for narration and educational reels.",
            geminiVoiceName = "Charon",
            googleCloudVoiceName = "bn-IN-Wavenet-B",
            ssmlGender = "MALE",
            samplePreviewText = "নমস্কার, এটি আমার এআই কণ্ঠস্বরের একটি নমুনা।"
        ),

        // Marathi Voices
        Voice(
            id = "mr_in_swara",
            name = "Swara (Marathi Female)",
            languageCode = "mr-IN",
            gender = "Female",
            description = "Traditional and pleasant Marathi voice for devotional songs, stories and news.",
            geminiVoiceName = "Aoede",
            googleCloudVoiceName = "mr-IN-Wavenet-A",
            ssmlGender = "FEMALE",
            samplePreviewText = "नमस्कार, हा माझ्या एआय आवाजाचा एक लहान नमुना आहे."
        ),
        Voice(
            id = "mr_in_tanmay",
            name = "Tanmay (Marathi Male)",
            languageCode = "mr-IN",
            gender = "Male",
            description = "Solid and confident Marathi male voice for explanatory videos and ads.",
            geminiVoiceName = "Fenrir",
            googleCloudVoiceName = "mr-IN-Wavenet-B",
            ssmlGender = "MALE",
            samplePreviewText = "नमस्कार, हा माझ्या एआय आवाजाचा एक लहान नमुना आहे."
        ),

        // Tamil Voices
        Voice(
            id = "ta_in_ananya",
            name = "Ananya (Tamil Female)",
            languageCode = "ta-IN",
            gender = "Female",
            description = "Crisp and fluent Tamil female voice with standard pronunciation.",
            geminiVoiceName = "Kore",
            googleCloudVoiceName = "ta-IN-Wavenet-A",
            ssmlGender = "FEMALE",
            samplePreviewText = "வணக்கம், இது எனது ஏஐ குரலின் மாதிரி ஆகும்."
        ),
        Voice(
            id = "ta_in_karthik",
            name = "Karthik (Tamil Male)",
            languageCode = "ta-IN",
            gender = "Male",
            description = "Energetic Tamil male voice for podcasts, news, and cinema updates.",
            geminiVoiceName = "Puck",
            googleCloudVoiceName = "ta-IN-Wavenet-B",
            ssmlGender = "MALE",
            samplePreviewText = "வணக்கம், இது எனது ஏஐ குரலின் மாதிரி ஆகும்."
        ),

        // Telugu Voices
        Voice(
            id = "te_in_keerthi",
            name = "Keerthi (Telugu Female)",
            languageCode = "te-IN",
            gender = "Female",
            description = "Harmonious Telugu voice for narration, devotional content, and ads.",
            geminiVoiceName = "Aoede",
            googleCloudVoiceName = "te-IN-Standard-A",
            ssmlGender = "FEMALE",
            samplePreviewText = "నమస్కారం, ఇది నా ఏఐ వాయిస్ నమూనా."
        ),
        Voice(
            id = "te_in_prasad",
            name = "Prasad (Telugu Male)",
            languageCode = "te-IN",
            gender = "Male",
            description = "Deep Telugu voice for documentaries and political commentary.",
            geminiVoiceName = "Charon",
            googleCloudVoiceName = "te-IN-Standard-B",
            ssmlGender = "MALE",
            samplePreviewText = "నమస్కారం, ఇది నా ఏఐ వాయిస్ నమూనా."
        ),

        // Gujarati Voices
        Voice(
            id = "gu_in_priti",
            name = "Priti (Gujarati Female)",
            languageCode = "gu-IN",
            gender = "Female",
            description = "Cheerful Gujarati female voice for commerce, stories, and social clips.",
            geminiVoiceName = "Kore",
            googleCloudVoiceName = "gu-IN-Wavenet-A",
            ssmlGender = "FEMALE",
            samplePreviewText = "નમસ્તે, આ મારા AI અવાજનો એક નાનો નમૂનો છે."
        ),

        // Punjabi Voices
        Voice(
            id = "pa_in_simran",
            name = "Simran (Punjabi Female)",
            languageCode = "pa-IN",
            gender = "Female",
            description = "Vibrant Punjabi voice with genuine regional tone and warmth.",
            geminiVoiceName = "Kore",
            googleCloudVoiceName = "pa-IN-Wavenet-A",
            ssmlGender = "FEMALE",
            samplePreviewText = "ਸਤਿ ਸ੍ਰੀ ਅਕਾਲ, ਇਹ ਮੇਰੀ AI ਆਵਾਜ਼ ਦਾ ਇੱਕ ਨਮੂਨਾ ਹੈ।"
        ),

        // Kannada Voices
        Voice(
            id = "kn_in_chaitra",
            name = "Chaitra (Kannada Female)",
            languageCode = "kn-IN",
            gender = "Female",
            description = "Clear Kannada female voice tailored for storytelling and educational tutorials.",
            geminiVoiceName = "Aoede",
            googleCloudVoiceName = "kn-IN-Wavenet-A",
            ssmlGender = "FEMALE",
            samplePreviewText = "ನಮಸ್ಕಾರ, ಇದು ನನ್ನ ಎಐ ಧ್ವನಿಯ ಕಿರು ಮಾದರಿಯಾಗಿದೆ."
        ),

        // Malayalam Voices
        Voice(
            id = "ml_in_arya",
            name = "Arya (Malayalam Female)",
            languageCode = "ml-IN",
            gender = "Female",
            description = "Authentic Malayalam female voice with natural prosody and cadence.",
            geminiVoiceName = "Kore",
            googleCloudVoiceName = "ml-IN-Wavenet-A",
            ssmlGender = "FEMALE",
            samplePreviewText = "നമസ്കാരം, ഇത് എന്റെ എഐ ശബ്ദത്തിന്റെ ഒരു സാമ്പിൾ ആണ്."
        )
    )

    val sampleScripts: List<SampleScript> = listOf(
        SampleScript(
            title = "YouTube Video Hook (Hindi)",
            category = "YouTube",
            languageCode = "hi-IN",
            text = "नमस्ते दोस्तों! क्या आप जानते हैं कि दुनिया का 90 प्रतिशत डेटा पिछले सिर्फ दो सालों में बना है? आज के वीडियो में हम जानेंगे वो रहस्य जो आपकी सोचने का नज़रिया बदल देगा। वीडियो को अंत तक ज़रूर देखिए!"
        ),
        SampleScript(
            title = "YouTube Video Hook (English)",
            category = "YouTube",
            languageCode = "en-US",
            text = "Did you know that ninety percent of all digital data in the world was created in just the last two years? Today, we're diving into five mind-blowing AI breakthroughs that will transform how you work and create."
        ),
        SampleScript(
            title = "Daily Motivation (Hindi)",
            category = "Motivation",
            languageCode = "hi-IN",
            text = "याद रखिए, मुश्किल वक्त हर किसी की ज़िंदगी में आता है। लेकिन विजेता वही बनता है जो हर चुनौती को अवसर में बदल दे। आज का दिन आपका है, उठिए और अपनी मंज़िल की ओर पहला कदम बढ़ाइए।"
        ),
        SampleScript(
            title = "Daily Motivation (English)",
            category = "Motivation",
            languageCode = "en-US",
            text = "Your only limit is you. The distance between your dreams and your reality is called action. Take the first step today, even if it feels small, because consistency creates miracles."
        ),
        SampleScript(
            title = "Historical Story (Hindi)",
            category = "Story",
            languageCode = "hi-IN",
            text = "यह कहानी है एक ऐसे महान राजा की, जिसने अपनी प्रजा की रक्षा के लिए अपना सब कुछ दांव पर लगा दिया था। रात का सन्नाटा था और किले की दीवारें गवाह थीं उस ऐतिहासिक फैसले की..."
        ),
        SampleScript(
            title = "Fable Narration (English)",
            category = "Story",
            languageCode = "en-US",
            text = "Once upon a time in a tranquil forest nestled between emerald mountains, an ancient whispering oak guarded the secrets of the forgotten kingdom. Travelers spoke of its magical glow when twilight fell."
        ),
        SampleScript(
            title = "Tech News Flash (Hindi)",
            category = "News",
            languageCode = "hi-IN",
            text = "आज की बड़ी टेक खबर! आर्टिफिशियल इंटेलिजेंस की दुनिया में एक नया कीर्तिमान स्थापित हुआ है। वैज्ञानिकों ने एक ऐसा मॉडल पेश किया है जो रियल-टाइम में मानवीय भावनाओं को समझ सकता है।"
        ),
        SampleScript(
            title = "Daily News Brief (English)",
            category = "News",
            languageCode = "en-US",
            text = "Good morning. In global markets today, tech indices rallied following breakthroughs in renewable energy infrastructure and chip manufacturing efficiency."
        ),
        SampleScript(
            title = "Educational Science (Hindi)",
            category = "Education",
            languageCode = "hi-IN",
            text = "प्रकाश संश्लेषण यानी Photosynthesis वह अद्भुत जैविक प्रक्रिया है जिसके द्वारा हरे पौधे सूर्य के प्रकाश की ऊर्जा को रासायनिक ऊर्जा में बदलते हैं और हमें प्राणवायु ऑक्सीजन प्रदान करते हैं।"
        ),
        SampleScript(
            title = "Devotional Shloka & Reflection (Hindi)",
            category = "Bible/Devotional",
            languageCode = "hi-IN",
            text = "कर्मण्येवाधिकारस्ते मा फलेषु कदाचन। ईश्वर पर अटूट विश्वास रखें और निष्काम भाव से अपने कर्म करते चलें। मन की शांति ही जीवन का सबसे बड़ा धन है।"
        ),
        SampleScript(
            title = "Peaceful Reflection (English)",
            category = "Bible/Devotional",
            languageCode = "en-US",
            text = "The Lord is my shepherd; I shall not want. He makes me lie down in green pastures, He leads me beside still waters, and He restores my soul."
        ),
        SampleScript(
            title = "Product Launch Commercial (Hindi)",
            category = "Advertisement",
            languageCode = "hi-IN",
            text = "क्या आप भी अपनी आवाज़ को देना चाहते हैं एक नया जादू? पेश है VoiceCraft AI! एक क्लिक में अपने शब्दों को बनाएं सबसे प्रभावशाली ऑडियो। आज ही डाउनलोड करें!"
        ),
        SampleScript(
            title = "SaaS Launch Commercial (English)",
            category = "Advertisement",
            languageCode = "en-US",
            text = "Stop spending thousands on studio voice talent. Unlock hyper-realistic AI voices in over ten languages with VoiceCraft AI. Turn your scripts into studio audio in seconds."
        )
    )

    fun getVoicesForLanguage(languageCode: String): List<Voice> {
        val list = supportedVoices.filter { it.languageCode == languageCode }
        return if (list.isNotEmpty()) list else supportedVoices.filter { it.languageCode == "en-US" }
    }

    fun getVoiceById(id: String): Voice {
        return supportedVoices.firstOrNull { it.id == id } ?: supportedVoices.first()
    }

    fun getLanguageByCode(code: String): Language {
        return supportedLanguages.firstOrNull { it.code == code } ?: supportedLanguages.first()
    }
}
