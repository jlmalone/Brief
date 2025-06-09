package com.techventus.wikipedianews.util

import android.content.Context
import org.apache.commons.lang3.StringUtils
import java.util.Locale

/**
 * Created by josephmalone on 6/24/15.
 */
object Utils {
    private val ROMAN_NUMERALS: Set<String> = setOf(
        "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"
    )

    @JvmStatic
    fun getPixelSizeFromDP(context: Context, dp: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }

    @JvmStatic
    fun uppercaseWords(input: String?): String? {
        if (StringUtils.isEmpty(input)) {
            return input
        }
        val parts = input!!.trim().split("\\s+".toRegex()).toTypedArray()
        val stringBuilder = StringBuilder()
        for (i in parts.indices) {
            if (i > 0) {
                stringBuilder.append(" ")
            }
            val part = parts[i]
            if (isRomanNumeral(part)) {
                stringBuilder.append(part.uppercase(Locale.getDefault()))
            } else {
                if (part.isNotEmpty()) {
                    stringBuilder.append(part.substring(0, 1).uppercase(Locale.getDefault()))
                        .append(part.substring(1).lowercase(Locale.getDefault()))
                }
            }
        }
        return stringBuilder.toString()
    }

    private fun isRomanNumeral(input: String): Boolean {
        return ROMAN_NUMERALS.contains(input.uppercase(Locale.getDefault()))
    }
}