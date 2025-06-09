package com.techventus.wikipedianews.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Arrays

class ArrayUtilTest {

    @Test
    fun testIsNullOrContainsEmpty_nullArray() {
        assertTrue(isNullOrContainsEmpty(null as Array<String?>?))
    }

    @Test
    fun testIsNullOrContainsEmpty_emptyArray() {
        assertTrue(isNullOrContainsEmpty(emptyArray<String?>()))
    }

    @Test
    fun testIsNullOrContainsEmpty_arrayWithNullElements() {
        assertTrue(isNullOrContainsEmpty(arrayOf<String?>(null, "test")))
        assertTrue(isNullOrContainsEmpty(arrayOf<String?>("test", null)))
        assertTrue(isNullOrContainsEmpty(arrayOf<String?>(null, null)))
    }

    @Test
    fun testIsNullOrContainsEmpty_arrayWithNonNullElements() {
        assertFalse(isNullOrContainsEmpty(arrayOf("a", "b")))
    }

    @Test
    fun testIsNullOrContainsEmpty_arrayWithMixOfNullAndNonNullElements() {
        assertTrue(isNullOrContainsEmpty(arrayOf<String?>("a", null, "c")))
    }

    @Test
    fun testIsNullOrContainsEmpty_arrayWithEmptyString() {
        // Current implementation of isNullOrContainsEmpty does not check for empty strings,
        // it only checks for null objects. This test reflects that.
        // If the requirement was to check for empty strings as well, this test would fail
        // and the method would need to be updated.
        assertFalse(isNullOrContainsEmpty(arrayOf("")))
        assertFalse(isNullOrContainsEmpty(arrayOf("a", "")))
    }


    // Tests for isNullOrEmpty(collection: Collection<T>?)

    @Test
    fun testIsNullOrEmpty_nullCollection() {
        assertTrue(isNullOrEmpty(null as Collection<String>?))
    }

    @Test
    fun testIsNullOrEmpty_emptyArrayList() {
        assertTrue(isNullOrEmpty(ArrayList<String>()))
    }

    @Test
    fun testIsNullOrEmpty_emptyHashSet() {
        assertTrue(isNullOrEmpty(HashSet<String>()))
    }

    @Test
    fun testIsNullOrEmpty_collectionWithElements() {
        assertFalse(isNullOrEmpty(Arrays.asList("a", "b")))
        assertFalse(isNullOrEmpty(HashSet(Arrays.asList("a", "b"))))
    }

    @Test
    fun testIsNullOrEmpty_collectionWithNullElements() {
        // isNullOrEmpty only checks if the collection itself is null or empty,
        // not the contents of the collection.
        assertFalse(isNullOrEmpty(Arrays.asList(null, "a")))
        assertFalse(isNullOrEmpty(java.util.Collections.singletonList(null as String?)))
    }
}
