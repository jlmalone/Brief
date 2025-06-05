package com.techventus.wikipedianews.util;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class UrlParamEncoderTest {

    @Test
    public void testEncode_alphanumeric() {
        assertEquals("abc123", UrlParamEncoder.encode("abc123"));
    }

    @Test
    public void testEncode_spaces() {
        assertEquals("Hello+World", UrlParamEncoder.encode("Hello World"));
    }

    @Test
    public void testEncode_specialCharacters() {
        assertEquals("%26%2F%3D%3F%23%25", UrlParamEncoder.encode("&/=?#%"));
    }

    @Test
    public void testEncode_unicodeCharacters() {
        assertEquals("%E2%82%AC%C3%A9%C3%B1", UrlParamEncoder.encode("€éñ"));
    }

    @Test
    public void testEncode_emptyString() {
        assertEquals("", UrlParamEncoder.encode(""));
    }

    @Test
    public void testEncode_alreadyEncoded() {
        // Test with a string that might resemble an already encoded string
        // URLEncoder should re-encode the '%' character
        assertEquals("Hello%2BWorld%2520Test", UrlParamEncoder.encode("Hello+World%20Test"));
    }

    @Test
    public void testEncode_mixedContent() {
        assertEquals("Text+with+spaces%2C+special+%26+unicode+%E2%82%AC%C3%A9%C3%B1", UrlParamEncoder.encode("Text with spaces, special & unicode €éñ"));
    }
}
