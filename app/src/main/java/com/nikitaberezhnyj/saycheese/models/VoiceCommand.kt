package com.nikitaberezhnyj.saycheese.models

import androidx.annotation.StringRes
import com.nikitaberezhnyj.saycheese.R

enum class VoiceCommand(
    val phrase: String,
    @field:StringRes val labelRes: Int
) {
    CHEESE("cheese", R.string.speech_heard_cheese),
    TIMER("timer", R.string.speech_heard_timer)
}