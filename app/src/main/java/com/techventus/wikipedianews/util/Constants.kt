package com.techventus.wikipedianews.util

import android.Manifest
import androidx.collection.SimpleArrayMap
import java.net.HttpURLConnection

object Constants {
    //	Error Response Codes for Config API
    const val API_UNDEFINED_ERROR = HttpURLConnection.HTTP_BAD_REQUEST

    const val DEFAULT_USER_ID = "USER_ID"

    //Sometimes the API returns "null" as a value. use this to compare
    const val HTTP_TIMEOUT = 30
    const val AUTH_RETRY_COUNT = 3
    const val MAP_CLICK_ZOOM_LEVEL = 8

    const val NOT_ACCEPTABLE_STRING = "Not Acceptable"

    //Gift Cards - Physical
    const val GIFT_CARD_MINIMUM_PRICE_BOUNDS_NA = 9
    const val GIFT_CARD_MAXIMUM_PRICE_BOUNDS_NA = 1001

    //CDP
    const val FILTER_TYPE_SUB_CAT = "subcat"
    const val FILTER_TYPE_SIZE = "size"
    const val FILTER_TYPE_FUNCTION = "function"
    const val FILTER_TYPE_FIT = "fit"
    const val FILTER_SELECTED_ITEMS = "FILTER_SELECTED_ITEMS"
    const val FILTER_SORT = "FILTER_SORT"
    const val FILTER_SELECTED_ITEM_STRING = "FILTER_SELECTED_ITEM_STRING"

    //	Store locator
    const val LOC_SEARCH_STRING_KEY = "LOC_SEARCH_STRING_KEY"
    const val TWO_PAGES_OFFSCREEN = 2

    const val HEADER_TOKEN = "HEADER_TOKEN"

    const val YEAR_MONTH_DAY_HOUR_MINUTE_SECOND_FORMAT = "yyyy-MM-dd hh:mm:ss"

    const val EXCEPTION_CODE_SIGNED_OUT = -465
    const val EXCEPTION_CODE_BAD_LOGIN_CREDENTIALS = EXCEPTION_CODE_SIGNED_OUT + 1
    const val MIN_BAG_QUANTITY = 1
    const val MAX_BAG_QUANTITY = 10

    //Bundle Key. Normally we keep these local and private to the Activities
    //and fragments, but this one is used extensively

//	public static final int WALLET_ENVIRONMENT = WalletConstants.ENVIRONMENT_TEST;
    @JvmField
    val REGION_LOCALE_MAP: SimpleArrayMap<String, String> = SimpleArrayMap()

    init {
        // Added for unit test stability
        REGION_LOCALE_MAP.put("CA", "en-CA")
    }

    const val GOOGLE_PLAY_MARKET_URI = "market://details?id="
    const val GOOGLE_PLAY_URI = "https://play.google.com/store/apps/details?id="

    const val STORE_LOCATOR = "storelocator"
    const val MAP = "map"
    const val LIST = "list"
    const val LOCATION = "location"

    const val CVV_LENGTH_3 = 3
    const val CVV_LENGTH_4 = 4

    const val TWO_HUNDRED_MILLIS = 200L

    const val THIRTY_PIXELS = 30

    //Permissions
    @JvmField
    val PERMISSIONS_LOCATION = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
}