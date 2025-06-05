package com.techventus.wikipedianews.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by josephmalone on 15-07-14.
 */
public class UrlParamEncoder {

	public static String encode(String input) {
		try {
			return URLEncoder.encode(input, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// This should never happen, as UTF-8 is a standard charset
			throw new RuntimeException("UTF-8 encoding not supported", e);
		}
	}

}