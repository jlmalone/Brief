package com.techventus.wikipedianews.util

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P]) // Use a consistent SDK for reliable test runs
class UtilsTest {

    @Test
    fun getPixelSizeFromDP_withStandardDensity() {
        // Robolectric provides a real Context with predictable properties.
        val context = ApplicationProvider.getApplicationContext<Context>()
        // Default density is often 1.0, so 10dp should equal 10px.
        // This test verifies the conversion logic works as expected.
        val expectedPixels = (10 * context.resources.displayMetrics.density + 0.5f).toInt()
        assertEquals(expectedPixels.toLong(), Utils.getPixelSizeFromDP(context, 10f).toLong())
    }

    @Test
    fun getPixelSizeFromDP_withZeroDP() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertEquals(0, Utils.getPixelSizeFromDP(context, 0f).toLong())
    }

    // --- Tests for uppercaseWords ---
    @Test
    fun uppercaseWords_handlesNullAndEmpty() {
        assertEquals(null, Utils.uppercaseWords(null))
        assertEquals("", Utils.uppercaseWords(""))
        assertEquals("", Utils.uppercaseWords("   ")) // Should trim to empty
    }

    @Test
    fun uppercaseWords_simpleCase() {
        assertEquals("Hello World", Utils.uppercaseWords("hello world"))
    }

    @Test
    fun uppercaseWords_mixedCase() {
        assertEquals("Hello World", Utils.uppercaseWords("hELLo wORLd"))
    }

    @Test
    fun uppercaseWords_handlesExtraSpaces() {
        assertEquals("Hello World", Utils.uppercaseWords("  hello   world  "))
    }

    @Test
    fun uppercaseWords_handlesRomanNumerals() {
        assertEquals("Chapter VI", Utils.uppercaseWords("chapter vi"))
        assertEquals("Part IX", Utils.uppercaseWords("PART IX"))
        assertEquals("Title I", Utils.uppercaseWords("title i"))
    }

    @Test
    fun uppercaseWords_mixedContent() {
        assertEquals("Part IV Section A", Utils.uppercaseWords("part iv section a"))
    }

    @Test
    fun uppercaseWords_doesNotMistakeWordsForNumerals() {
        assertEquals("Veni Vidi Vici", Utils.uppercaseWords("veni vidi vici"))
    }
}