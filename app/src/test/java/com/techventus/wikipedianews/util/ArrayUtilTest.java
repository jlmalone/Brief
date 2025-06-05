package com.techventus.wikipedianews.util;

import org.junit.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ArrayUtilTest {

    // Tests for isNullOrContainsEmpty(T[] array)

    @Test
    public void testIsNullOrContainsEmpty_nullArray() {
        assertTrue(ArrayUtil.isNullOrContainsEmpty(null));
    }

    @Test
    public void testIsNullOrContainsEmpty_emptyArray() {
        assertTrue(ArrayUtil.isNullOrContainsEmpty(new String[0]));
    }

    @Test
    public void testIsNullOrContainsEmpty_arrayWithNullElements() {
        assertTrue(ArrayUtil.isNullOrContainsEmpty(new String[]{null, "test"}));
        assertTrue(ArrayUtil.isNullOrContainsEmpty(new String[]{"test", null}));
        assertTrue(ArrayUtil.isNullOrContainsEmpty(new String[]{null, null}));
    }

    @Test
    public void testIsNullOrContainsEmpty_arrayWithNonNullElements() {
        assertFalse(ArrayUtil.isNullOrContainsEmpty(new String[]{"a", "b"}));
    }

    @Test
    public void testIsNullOrContainsEmpty_arrayWithMixOfNullAndNonNullElements() {
        assertTrue(ArrayUtil.isNullOrContainsEmpty(new String[]{"a", null, "c"}));
    }

    @Test
    public void testIsNullOrContainsEmpty_arrayWithEmptyString() {
        // Current implementation of isNullOrContainsEmpty does not check for empty strings,
        // it only checks for null objects. This test reflects that.
        // If the requirement was to check for empty strings as well, this test would fail
        // and the method would need to be updated.
        assertFalse(ArrayUtil.isNullOrContainsEmpty(new String[]{""}));
        assertFalse(ArrayUtil.isNullOrContainsEmpty(new String[]{"a", ""}));
    }


    // Tests for isNullOrEmpty(Collection<T> collection)

    @Test
    public void testIsNullOrEmpty_nullCollection() {
        assertTrue(ArrayUtil.isNullOrEmpty(null));
    }

    @Test
    public void testIsNullOrEmpty_emptyArrayList() {
        assertTrue(ArrayUtil.isNullOrEmpty(new ArrayList<String>()));
    }

    @Test
    public void testIsNullOrEmpty_emptyHashSet() {
        assertTrue(ArrayUtil.isNullOrEmpty(new HashSet<String>()));
    }

    @Test
    public void testIsNullOrEmpty_collectionWithElements() {
        assertFalse(ArrayUtil.isNullOrEmpty(Arrays.asList("a", "b")));
        assertFalse(ArrayUtil.isNullOrEmpty(new HashSet<>(Arrays.asList("a", "b"))));
    }

    @Test
    public void testIsNullOrEmpty_collectionWithNullElements() {
        // isNullOrEmpty only checks if the collection itself is null or empty,
        // not the contents of the collection.
        assertFalse(ArrayUtil.isNullOrEmpty(Arrays.asList(null, "a")));
        assertFalse(ArrayUtil.isNullOrEmpty(Collections.singletonList(null)));
    }
}
