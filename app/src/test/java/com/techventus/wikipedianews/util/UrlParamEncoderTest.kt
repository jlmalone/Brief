package com.techventus.wikipedianews.util

import org.junit.Assert.assertEquals
import org.junit.Test

class UrlParamEncoderTest {

    @Test
    fun testEncode_alphanumeric() {
        assertEquals("abc123", encode("abc123"))
    }

    @Test
    fun testEncode_spaces() {
        assertEquals("Hello+World", encode("Hello World"))
    }

    @Test
    fun testEncode_specialCharacters() {
        assertEquals("%26%2F%3D%3F%23%25", encode("&/=?#%"))
    }

    @Test
    fun testEncode_unicodeCharacters() {
        assertEquals("%E2%82%AC%C3%A9%C3%B1", encode("€éñ"))
    }

    @Test
    fun testEncode_emptyString() {
        assertEquals("", encode(""))
    }

    @Test
    fun testEncode_nullInput() {
        // Test that null input returns an empty string as per the new Kotlin implementation
        assertEquals("", encode(null))
    }

    @Test
    fun testEncode_alreadyEncoded() {
        // Test with a string that might resemble an already encoded string
        // URLEncoder should re-encode the '%' character and '+' character if it's not representing a space
        assertEquals("Hello%2BWorld%2520Test", encode("Hello+World%20Test"))
    }

    @Test
    fun testEncode_mixedContent() {
        assertEquals("Text+with+spaces%2C+special+%26+unicode+%E2%82%AC%C3%A9%C3%B1", encode("Text with spaces, special & unicode €éñ"))
    }
}
