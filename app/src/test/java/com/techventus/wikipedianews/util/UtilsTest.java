package com.techventus.wikipedianews.util;

import static org.junit.Assert.assertEquals;

import android.content.Context;
import android.os.Build;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.P) // Use a consistent SDK for reliable test runs
public class UtilsTest {


    @Test
    public void getPixelSizeFromDP_withStandardDensity() {
        // Robolectric provides a real Context with predictable properties.
        Context context = ApplicationProvider.getApplicationContext();
        // Default density is often 1.0, so 10dp should equal 10px.
        // This test verifies the conversion logic works as expected.
        int expectedPixels = (int) (10 * context.getResources().getDisplayMetrics().density + 0.5f);
        assertEquals(expectedPixels, Utils.getPixelSizeFromDP(context, 10f));
    }

    @Test
    public void getPixelSizeFromDP_withZeroDP() {
        Context context = ApplicationProvider.getApplicationContext();
        assertEquals(0, Utils.getPixelSizeFromDP(context, 0f));
    }

    // --- Tests for uppercaseWords ---

    @Test
    public void uppercaseWords_handlesNullAndEmpty() {
        assertEquals(null, Utils.uppercaseWords(null));
        assertEquals("", Utils.uppercaseWords(""));
        assertEquals("", Utils.uppercaseWords("   ")); // Should trim to empty
    }

    @Test
    public void uppercaseWords_simpleCase() {
        assertEquals("Hello World", Utils.uppercaseWords("hello world"));
    }

    @Test
    public void uppercaseWords_mixedCase() {
        assertEquals("Hello World", Utils.uppercaseWords("hELLo wORLd"));
    }

    @Test
    public void uppercaseWords_handlesExtraSpaces() {
        assertEquals("Hello World", Utils.uppercaseWords("  hello   world  "));
    }

    @Test
    public void uppercaseWords_handlesRomanNumerals() {
        assertEquals("Chapter VI", Utils.uppercaseWords("chapter vi"));
        assertEquals("Part IX", Utils.uppercaseWords("PART IX"));
        assertEquals("Title I", Utils.uppercaseWords("title i"));
    }

    @Test
    public void uppercaseWords_mixedContent() {
        assertEquals("Part IV Section A", Utils.uppercaseWords("part iv section a"));
    }

    @Test
    public void uppercaseWords_doesNotMistakeWordsForNumerals() {
        assertEquals("Veni Vidi Vici", Utils.uppercaseWords("veni vidi vici"));
    }
}