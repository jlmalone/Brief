package com.techventus.wikipedianews.util;

import android.Manifest;
import androidx.collection.SimpleArrayMap; // Changed import

import java.net.HttpURLConnection;

public class Constants
{
	//	Error Response Codes for Config API
	public static final int API_UNDEFINED_ERROR = HttpURLConnection.HTTP_BAD_REQUEST;

	public static final String DEFAULT_USER_ID = "USER_ID";
	//Sometimes the API returns "null" as a value. use this to compare
	public static final int HTTP_TIMEOUT = 30;
	public static final int AUTH_RETRY_COUNT = 3;
	public static final int MAP_CLICK_ZOOM_LEVEL = 8;

	public static final String NOT_ACCEPTABLE_STRING = "Not Acceptable";

	//Gift Cards - Physical
	public static final int GIFT_CARD_MINIMUM_PRICE_BOUNDS_NA = 9;
	public static final int GIFT_CARD_MAXIMUM_PRICE_BOUNDS_NA = 1001;

	//CDP
	public static final String FILTER_TYPE_SUB_CAT = "subcat";
	public static final String FILTER_TYPE_SIZE = "size";
	public static final String FILTER_TYPE_FUNCTION = "function";
	public static final String FILTER_TYPE_FIT = "fit";
	public static final String FILTER_SELECTED_ITEMS = "FILTER_SELECTED_ITEMS";
	public static final String FILTER_SORT = "FILTER_SORT";
	public static final String FILTER_SELECTED_ITEM_STRING = "FILTER_SELECTED_ITEM_STRING";

	//	Store locator
	public static final String LOC_SEARCH_STRING_KEY = "LOC_SEARCH_STRING_KEY";
	public static final int TWO_PAGES_OFFSCREEN = 2;

	public static final String HEADER_TOKEN = "HEADER_TOKEN";

	public static final String YEAR_MONTH_DAY_HOUR_MINUTE_SECOND_FORMAT = "yyyy-MM-dd hh:mm:ss";

	public static final int EXCEPTION_CODE_SIGNED_OUT = -465;
	public static final int EXCEPTION_CODE_BAD_LOGIN_CREDENTIALS = EXCEPTION_CODE_SIGNED_OUT + 1;
	public static final int MIN_BAG_QUANTITY = 1;
	public static final int MAX_BAG_QUANTITY = 10;

	//Bundle Key. Normally we keep these local and private to the Activities
	//and fragments, but this one is used extensively

//	public static final int WALLET_ENVIRONMENT = WalletConstants.ENVIRONMENT_TEST;

	public static final SimpleArrayMap<String, String> REGION_LOCALE_MAP = new SimpleArrayMap<>();

	static {
		// Added for unit test stability
		REGION_LOCALE_MAP.put("CA", "en-CA");
	}

	public static final String GOOGLE_PLAY_MARKET_URI = "market://details?id=";
	public static final String GOOGLE_PLAY_URI = "https://play.google.com/store/apps/details?id=";

	public static final String STORE_LOCATOR = "storelocator";
	public static final String MAP = "map";
	public static final String LIST = "list";
	public static final String LOCATION = "location";

	public static final int CVV_LENGTH_3 = 3;
	public static final int CVV_LENGTH_4 = 4;

	public static final long TWO_HUNDRED_MILLIS = 200L;

	public static final int THIRTY_PIXELS = 30;

	//Permissions
	public static final String[] PERMISSIONS_LOCATION = {Manifest.permission.ACCESS_FINE_LOCATION};
}