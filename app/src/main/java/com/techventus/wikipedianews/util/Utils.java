package com.techventus.wikipedianews.util;

import android.content.Context;


import org.apache.commons.lang3.StringUtils;


import java.util.HashSet;

import java.util.Set;


/**
 * Created by josephmalone on 6/24/15.
 */
public class Utils {
	private static final Set<String> ROMAN_NUMERALS;

	static
	{
		ROMAN_NUMERALS = new HashSet<>();
		ROMAN_NUMERALS.add("I");
		ROMAN_NUMERALS.add("II");
		ROMAN_NUMERALS.add("III");
		ROMAN_NUMERALS.add("IV");
		ROMAN_NUMERALS.add("V");
		ROMAN_NUMERALS.add("VI");
		ROMAN_NUMERALS.add("VII");
		ROMAN_NUMERALS.add("VIII");
		ROMAN_NUMERALS.add("IX");
		ROMAN_NUMERALS.add("X");

	}




	public static int getPixelSizeFromDP(Context context, float dp)
	{
		float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dp * scale + 0.5f);
	}






	public static String uppercaseWords(final String input)
	{
		if (StringUtils.isEmpty(input))
		{
			return input;
		}
		String[] parts = input.trim().split("\\s+");
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < parts.length; ++i)
		{
			if (i > 0)
			{
				stringBuilder.append(" ");
			}
			String part = parts[i];
			if (isRomanNumeral(part))
			{
				stringBuilder.append(part.toUpperCase());
			}
			else
			{
				if (part.length() > 0) {
					stringBuilder.append(part.substring(0, 1).toUpperCase()).append(part.substring(1).toLowerCase());
				}
			}
		}
		return stringBuilder.toString();
	}

	private static boolean isRomanNumeral(final String in)
	{
		return ROMAN_NUMERALS.contains(in.toUpperCase());
	}





}