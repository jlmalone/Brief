package com.techventus.wikipedianews.util;

import java.util.Collection;

/**
 * Created by josephmalone on 6/14/15.
 */
public class ArrayUtil
{
	public static <T> boolean isNullOrContainsEmpty(T[] array)
	{
		if (array == null || array.length == 0)
		{
			return true;
		}

		for (T val : array)
		{
			if (val == null)
			{
				return true;
			}
		}

		return false;
	}

	public  static <T> boolean isNullOrEmpty(Collection<T> collection)
	{
		return collection == null || collection.isEmpty();
	}
}
