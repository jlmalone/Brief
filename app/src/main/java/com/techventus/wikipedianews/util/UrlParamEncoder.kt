package com.techventus.wikipedianews.util

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Created by josephmalone on 15-07-14.
 */

fun encode(input: String?): String {
    input ?: return ""
    // URLEncoder.encode expects a non-null string.
    // The UnsupportedEncodingException is not expected with StandardCharsets.UTF_8.
    // If it were to occur, it would propagate as an unhandled checked exception from Java.
    // No need to catch it explicitly in Kotlin if we are sure about UTF-8 availability.
    return URLEncoder.encode(input, StandardCharsets.UTF_8.name())
}
