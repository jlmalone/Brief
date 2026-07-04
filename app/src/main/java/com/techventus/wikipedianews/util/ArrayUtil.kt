package com.techventus.wikipedianews.util

//import java.util.Collection


fun <T> isNullOrContainsEmpty(array: Array<T?>?): Boolean {
    return array == null || array.isEmpty() || array.any { it == null }
}

fun <T> isNullOrEmpty(collection: Collection<T>?): Boolean {
    return collection == null || collection.isEmpty()
}
