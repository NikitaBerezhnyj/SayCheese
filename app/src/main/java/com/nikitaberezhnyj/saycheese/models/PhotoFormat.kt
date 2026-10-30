package com.nikitaberezhnyj.saycheese.models

import androidx.annotation.StringRes
import com.nikitaberezhnyj.saycheese.R

enum class PhotoFormat(
    val aspectRatio: Float?,
    @StringRes val labelRes: Int
) {
    RATIO_3_4(3f / 4f, R.string.format_3_4),
    RATIO_1_1(1f, R.string.format_1_1),
    FULL(null, R.string.format_full)
}