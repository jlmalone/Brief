package com.techventus.wikipedianews.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import com.techventus.wikipedianews.WikiApplication;
import com.techventus.wikipedianews.manager.WikiCookieManager;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

/**
 * Created by josephmalone on 6/24/15.
 */
public class Utils
{

	private static final int MIN_PASSWORD_LENGTH = 8;
	private static final Set<String> ROMAN_NUMERALS;
	private static final int MAX_IMAGE_WIDTH_WIFI = 1080;
	private static final int MAX_IMAGE_WIDTH_3G = 720;
	private static final String GMT = "GMT";
	private static final String UTC_FORMAT_STRING = "yyyy-MM-dd HH:mm:ss.SSS";
	private static final String TEL = "tel:";

	public static long getUTCTimestampInMillis(String inventoryTimestamp)
	{
		try
		{
			SimpleDateFormat sdf = new SimpleDateFormat(UTC_FORMAT_STRING);
			sdf.setTimeZone(TimeZone.getTimeZone(GMT));
			Date d = sdf.parse(inventoryTimestamp.substring(0, UTC_FORMAT_STRING.length()).replace('T', ' '));
			return d.getTime();
		}
		catch (ParseException e)
		{
			//			Crittercism.logHandledException(e);

		}
		return -1;
	}

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


	public static int getPixelScreenHeight(Context context)
	{
		return context.getResources().getDisplayMetrics().heightPixels;
	}

	public static int getPixelScreenWidth(Context context)
	{
		return context.getResources().getDisplayMetrics().widthPixels;
	}

	public static int getPixelSizeFromDP(Context context, float dp)
	{
		float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dp * scale + 0.5f);
	}

	public static float getDPFromPixelSize(Context context, int pixelSize)
	{
		float scale = context.getResources().getDisplayMetrics().density;
		return (pixelSize / scale);
	}


	public static int getPixelSizeFromSP(Context context, float dp)
	{
		float scale = context.getResources().getDisplayMetrics().scaledDensity;
		return (int) (dp * scale + 0.5f);
	}


	public static float getSPFromPixelSize(Context context, int pixelSize)
	{
		float scale = context.getResources().getDisplayMetrics().scaledDensity;
		return (pixelSize / scale);
	}

	public static boolean isAtLeastSDK(int sdkVersionChecked)
	{
		return Build.VERSION.SDK_INT >= sdkVersionChecked;
	}

	public static String getPlatformVersion()
	{
		return Build.VERSION.RELEASE;
	}

	/**
	 * Returns whether the device is currently in portrait or landscape orientation.
	 *
	 * @param activity the activity from which we will obtain device orientation info
	 * @return true if in landscape orientation, else false for portrait.
	 */
	@SuppressWarnings("deprecation")
	public static boolean isDeviceInLandscapeOrientation(Activity activity)
	{
		Display display = activity.getWindow().getWindowManager().getDefaultDisplay();
		int width = display.getWidth();
		int height = display.getHeight();
		return width > height;
	}

	/**
	 * Returns whether the device is currently in its default or natural orientation (based on the device form factor).
	 *
	 * @param activity the activity from which we will obtain device orientation info
	 * @return true if in default orientation, else false.
	 */
	@SuppressWarnings("deprecation")
	public static boolean isDeviceInDefaultOrientation(Activity activity)
	{
		Display display = activity.getWindow().getWindowManager().getDefaultDisplay();
		return display.getRotation() == Surface.ROTATION_0;
	}

	/**
	 * Formats a phone number based on the current locale
	 *
	 * @param unformattedPhoneNumber the raw phone number
	 * @return the formatted phone number per the current locale
	 */
	@SuppressWarnings("deprecation")
	public static String formatPhoneNumber(final String unformattedPhoneNumber)
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
		{
			String countryCode = Locale.getDefault().getCountry();
			if (!TextUtils.isEmpty(countryCode))
			{
				return PhoneNumberUtils.formatNumber(unformattedPhoneNumber, countryCode);
			}
		}
		return PhoneNumberUtils.formatNumber(unformattedPhoneNumber);
	}


	public static boolean hasConnectivity()
	{
		ConnectivityManager cm = (ConnectivityManager) WikiApplication.getInstance().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
	}

	public static boolean isNonZero(Double number)
	{
		if (number != null && number != 0d)
		{
			return true;
		}
		return false;
	}

	public static String getDateStringForConfigHeader(String format)
	{
		TimeZone timeZone = TimeZone.getTimeZone("UTC");
		Calendar calendar = Calendar.getInstance(timeZone, Locale.ENGLISH);
		Date date = calendar.getTime();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format, Locale.ENGLISH);
		simpleDateFormat.setTimeZone(timeZone);
		return unixTimeFormat(date.getTime() / 1000L, simpleDateFormat);
	}

	private static String unixTimeFormat(long unixtime, SimpleDateFormat formatter)
	{
		return formatter.format(new Date(unixtime * 1000L));
	}

	//Taking in dateStr of type 2015-07-15 11:59:07.0
	public static String formatDateString(String dateStr, String dateFormat)
	{
		if (TextUtils.isEmpty(dateStr))
		{
			return "";
		}
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, Integer.parseInt(dateStr.substring(0, 4)));
		cal.set(Calendar.MONTH, Integer.parseInt(dateStr.substring(5, 5 + 2)) - 1);
		cal.set(Calendar.DATE, Integer.parseInt(dateStr.substring(8, 8 + 2)));
		Date date = cal.getTime();
		return unixTimeFormat(date.getTime() / 1000L, new SimpleDateFormat(dateFormat));
	}


	public static String formatBirthday(final String birthday)
	{
		if (!TextUtils.isEmpty(birthday))
		{
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
			try
			{
				Date date = simpleDateFormat.parse(birthday);
				return new SimpleDateFormat("MMM, d").format(date);
			}
			catch (ParseException ignore)
			{
			}
		}
		return "";
	}


	public static void showKeyboard(final View view)
	{
		InputMethodManager inputMethodManager = (InputMethodManager) WikiApplication.getInstance().getApplicationContext().getSystemService(
				Context.INPUT_METHOD_SERVICE);
		inputMethodManager.showSoftInput(view, 0);
	}

	public static void hideKeyboard(final View view)
	{
		InputMethodManager inputMethodManager = (InputMethodManager) WikiApplication.getInstance().getApplicationContext().getSystemService(
				Context.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}


	public static boolean isValidPassword(final CharSequence password)
	{
		boolean hasUppercase = false;
		boolean hasNumber = false;
		boolean hasSpecial = false;
		if (password.length() >= MIN_PASSWORD_LENGTH)
		{
			for (int i = 0; i < password.length(); ++i)
			{
				char ch = password.charAt(i);
				if (Character.isDigit(ch))
				{
					hasNumber = true;
				}
				else if (Character.isUpperCase(ch))
				{
					hasUppercase = true;
				}
				else if (!Character.isLowerCase(ch))
				{
					hasSpecial = true;
					break;
				}
			}
		}
		return hasUppercase && hasNumber && !hasSpecial;
	}

	public static String uppercaseWords(final String input)
	{
		if (TextUtils.isEmpty(input))
		{
			return input;
		}
		StringBuilder stringBuilder = new StringBuilder();
		String[] parts = input.split(" ");
		for (int i = 0; i < parts.length; ++i)
		{
			if (i > 0)
			{
				stringBuilder.append(" ");
			}
			if (parts[i].length() > 1 && !isRomanNumeral(parts[i]))
			{
				stringBuilder.append(parts[i].substring(0, 1).toUpperCase()).append(parts[i].substring(1).toLowerCase());
			}
			else
			{
				stringBuilder.append(parts[i].toUpperCase());
			}
		}
		return stringBuilder.toString();
	}

	public static void launchPlayStoreForUpdate()
	{
		final Context context = WikiApplication.getInstance().getApplicationContext();
		final String appPackageName = context.getPackageName();
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.GOOGLE_PLAY_MARKET_URI + appPackageName));
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		try
		{
			context.startActivity(intent);
		}
		catch (android.content.ActivityNotFoundException anfe)
		{
			intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.GOOGLE_PLAY_URI + appPackageName));
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
		}
	}


	//TODO Consider making a Permission Manager Class for things like this.
	//Returns true if we already have permission otherwise will show the dialog and return false


	private static boolean isRomanNumeral(final String in)
	{
		return ROMAN_NUMERALS.contains(in.toUpperCase());
	}

	public static String convert24to12(String time)
	{
		if (TextUtils.isEmpty(time))
		{
			return null;
		}
		String convertedTime = "";
		try
		{
			SimpleDateFormat displayFormat = new SimpleDateFormat("hh:mma", Locale.US);
			SimpleDateFormat parseFormat = new SimpleDateFormat("HH:mm", Locale.US);
			Date date = parseFormat.parse(time);
			convertedTime = displayFormat.format(date);
		}
		catch (final ParseException e)
		{
			e.printStackTrace();
		}
		return convertedTime.toLowerCase();
		//Output will be 10:23am
	}


	public static boolean exceedsUpdateWindow(long windowInterval, long lastCheck)
	{
		long currentTime = System.currentTimeMillis();
		if (currentTime - lastCheck > windowInterval || lastCheck - currentTime > windowInterval)
		{
			return true;
		}
		return false;
	}


	public enum Units
	{
		METRIC,
		IMPERIAL
	}

	public static Units getUnitType()
	{
		String countryCode = Locale.getDefault().getCountry();
		if ("US".equals(countryCode))
		{
			return Units.IMPERIAL; // USA
		}
		if ("UK".equals(countryCode))
		{
			return Units.IMPERIAL; // UK
		}
		if ("LR".equals(countryCode))
		{
			return Units.IMPERIAL; // liberia
		}
		if ("MM".equals(countryCode))
		{
			return Units.IMPERIAL; // burma
		}
		return Units.METRIC;
	}

	public static double kmToMi(double km)
	{
		return km * 0.621371;
	}

	public static float dpToPx(int dp)
	{
		Resources r = WikiApplication.getInstance().getApplicationContext().getResources();
		return r.getDimensionPixelSize(dp);
	}

	public static boolean isGoogleMapsInstalled()
	{
		try
		{
			ApplicationInfo info = WikiApplication.getInstance().getApplicationContext().getPackageManager().getApplicationInfo("com.google.android.apps" +
					".maps",
					0);
			return true;
		}
		catch (PackageManager.NameNotFoundException e)
		{
			return false;
		}
	}
//
//	public static String buildTimes(String open, String close)
//	{
//		String open12hr = Utils.convert24to12(open);
//		String close12hr = Utils.convert24to12(close);
//
//		if (open12hr != null && close12hr != null)
//		{
//			return open12hr + " " + WikiApplication.getInstance().getApplicationContext().getString(R.string.day_separator) + " " + close12hr;
//		}
//		else
//		{
//			return WikiApplication.getInstance().getApplicationContext().getString(R.string.store_closed);
//		}
//	}

//
//	public static String toSentenceCase(String inputString)
//	{
//		StringBuilder result = new StringBuilder();
//		if (inputString.length() == 0)
//		{
//			return result.toString();
//		}
//		char firstChar = inputString.charAt(0);
//		char firstCharToUpperCase = Character.toUpperCase(firstChar);
//		result.append(firstCharToUpperCase);
//		boolean terminalCharacterEncountered = false;
//		Set<Character> terminalCharacters = Sets.newHashSet('.', '?', '!');
//
//		for (int i = 1; i < inputString.length(); i++)
//		{
//			char currentChar = inputString.charAt(i);
//			if (terminalCharacterEncountered)
//			{
//				if (currentChar == ' ')
//				{
//					result.append(currentChar);
//				}
//				else
//				{
//					char currentCharToUpperCase = Character.toUpperCase(currentChar);
//					result.append(currentCharToUpperCase);
//					terminalCharacterEncountered = false;
//				}
//			}
//			else
//			{
//				char currentCharToLowerCase = Character.toLowerCase(currentChar);
//				result.append(currentCharToLowerCase);
//			}
//			if (terminalCharacters.contains(currentChar))
//			{
//				terminalCharacterEncountered = true;
//			}
//		}
//		return result.toString();
//	}


	public static String formatCreditCardString(String CCNumber)
	{
		if (TextUtils.isEmpty(CCNumber))
		{
			return CCNumber;
		}
		StringBuilder output = new StringBuilder();
		CCNumber = CCNumber.replaceAll("(?i)x", "\u2022");
		for (int i = 0; i < CCNumber.length(); i++)
		{
			if (i > 1 && (i % 4) == 0 && i != (CCNumber.length() - 1))
			{
				output.append(" ");
			}
			output.append(CCNumber.charAt(i));
		}
		return output.toString();
	}

	public static String unformatCreditCardString(final String input)
	{
		if (TextUtils.isEmpty(input))
		{
			return input;
		}
		StringBuilder output = new StringBuilder();
		for (int i = 0; i < input.length(); ++i)
		{
			char val = input.charAt(i);
			if (Character.isDigit(val) || val == '\u2022')
			{
				output.append(val);
			}
		}
		return output.toString();
	}

	public static String unformatPhoneNumberString(final String input)
	{
		if (TextUtils.isEmpty(input))
		{
			return input;
		}
		StringBuilder output = new StringBuilder();
		for (int i = 0; i < input.length(); ++i)
		{
			char val = input.charAt(i);
			if (Character.isDigit(val))
			{
				output.append(val);
			}
		}
		return output.toString();
	}


	public static boolean isCreditCardExpired(int month, int year)
	{
		//Calender.getInstance.get(Calender.MONTH) returns months 0 - 11; Jan being 0
		if ((month < (Calendar.getInstance().get(Calendar.MONTH) + 1) && (year <= Calendar.getInstance().get(Calendar.YEAR))) ||
				(year < Calendar.getInstance().get(Calendar.YEAR)))
		{
			return true;
		}
		return false;
	}


	/**
	 * After a Sign In user Change, fix the cookies based on the User Profile
	 */
	public static void regionChangeCookiePopulate(String region, String locale, String isLoggedIn)
	{
		WikiCookieManager.getInstance().clearCookies();
		WikiCookieManager.getInstance().setCookie("Country", region);
		WikiCookieManager.getInstance().setCookie("isLoggedin", isLoggedIn);
		if (StringUtils.isNotEmpty(region))
		{
			if (StringUtils.isNotEmpty(locale))
			{
				WikiCookieManager.getInstance().setCookie("UsrLocale", locale);
			}
			WikiCookieManager.getInstance().setCookie("UsrLocale", Constants.REGION_LOCALE_MAP.get(region));
		}
		WikiCookieManager.getInstance().setUpdated(true);

		WikiCookieManager.getInstance().saveCookiesIfNeeded();
	}
	//
	//	public static int getImageWidth(int viewWidth)
	//	{
	//		ConnectivityManager connectivityManager = (ConnectivityManager) WikiApplication.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
	//		Network[] network = connectivityManager.getAllNetworks();
	//		boolean wifiOn = false;
	//		boolean dataOn = false;
	//		if (network != null && network.length > 0)
	//		{
	//			outer:
	//			for (int i = 0; i < network.length; ++i)
	//			{
	//				NetworkInfo networkInfo = connectivityManager.getNetworkInfo(network[i]);
	//				if (networkInfo.isConnectedOrConnecting())
	//				{
	//					switch (networkInfo.getType())
	//					{
	//						case ConnectivityManager.TYPE_WIFI:
	//							wifiOn = true;
	//							break outer;
	//						case ConnectivityManager.TYPE_MOBILE:
	//							dataOn = true;
	//							break;
	//					}
	//				}
	//			}
	//		}
	//		if (wifiOn)
	//		{
	//			return viewWidth > 0 ? Math.min(MAX_IMAGE_WIDTH_WIFI, viewWidth) : MAX_IMAGE_WIDTH_WIFI;
	//		}
	//		else
	//		{
	//			return viewWidth > 0 ? Math.min(MAX_IMAGE_WIDTH_3G, viewWidth) : MAX_IMAGE_WIDTH_3G;
	//		}
	//	}

	public static String optimizeUrl(
			final String url, int requiredWidth)
	{
		// if the URL has a specifier for the size, remove it and add our own based on the specified width
		int index = url.indexOf("?");
		return (index > 0 ? url.substring(0, index) : url) + "?wid=" + requiredWidth;
	}

	public static String optimizeUrl(
			final String url, int requiredWidth, int requiredHeight)
	{
		return optimizeUrl(url, requiredWidth, requiredHeight, false);
	}

	public static String optimizeUrl(final String url, int requiredWidth, int requiredHeight, boolean cropAndAlign)
	{
		// if the URL has a specifier for the size, remove it and add our own based on the width
		// and height specified
		int index = url.indexOf("?");
		String optimized = (index > 0 ? url.substring(0, index) : url) + "?wid=" + requiredWidth + "&hei=" + requiredHeight;
		return cropAndAlign ? optimized + "&fit=crop&align=0,0" : optimized;
	}

}
