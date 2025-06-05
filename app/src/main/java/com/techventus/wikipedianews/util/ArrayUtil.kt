package com.techventus.wikipedianews.util

import java.util.Collection

/**
 * Created by josephmalone on 6/14/15.
 */

fun <T> isNullOrContainsEmpty(array: Array<T?>?): Boolean {
    return array == null || array.isEmpty() || array.any { it == null }
}

fun <T> isNullOrEmpty(collection: Collection<T>?): Boolean {
    return collection == null || collection.isEmpty()
}
