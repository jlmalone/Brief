package com.techventus.wikipedianews.fragment;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import com.techventus.wikipedianews.R;
import com.techventus.wikipedianews.WikiAdapter;
import com.techventus.wikipedianews.WikiData;
import com.techventus.wikipedianews.logging.Logger;
import com.techventus.wikipedianews.view.LoadingViewFlipper;
import com.techventus.wikipedianews.dialogfragment.NotificationDialogFragment;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Updated WikiNewsFragment with enhanced logging and error handling.
 */
public class WikiNewsFragment extends WikiFragment implements NotificationDialogFragment.NotificationDialogListener {

	private String mPageSource;
	private static final String TAG = WikiNewsFragment.class.getSimpleName();
	private LoadingViewFlipper mLoadingFlipper;
	private RecyclerView mRecyclerView;
	private WikiAdapter mAdapter;
	private RecyclerView.LayoutManager mLayoutManager;
	private ArrayList<WikiData> mData;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Logger.d(TAG, "onCreateView called");
		return inflater.inflate(R.layout.category_fragment, container, false);
	}

	@Override
	public void onViewCreated(View v, Bundle savedInstanceState) {
		super.onViewCreated(v, savedInstanceState);
		Logger.d(TAG, "onViewCreated called");

		// Initialize LoadingViewFlipper
		mLoadingFlipper = v.findViewById(R.id.loading_view_flipper);
		Logger.d(TAG, "Initialized LoadingViewFlipper");

		// Set Retry Callback to handle retry actions from the error view
		mLoadingFlipper.setRetryCallback(new LoadingViewFlipper.RetryCallback() {
			@Override
			public void onRetry() {
				Logger.d(TAG, "RetryCallback triggered");
				getData(); // Re-initiate data fetching
			}
		});

		// Initialize RecyclerView and Adapter
		mAdapter = new WikiAdapter();
		mRecyclerView = mLoadingFlipper.findViewById(R.id.recyclerview_content); // Corrected ID
		if (mRecyclerView == null) {
			Logger.e(TAG, "RecyclerView with ID 'recyclerview_content' not found");
			mLoadingFlipper.showError("Internal error: RecyclerView not found.");
			return;
		}
		mRecyclerView.setHasFixedSize(true);
		mLayoutManager = new LinearLayoutManager(getActivity());

		mRecyclerView.setLayoutManager(mLayoutManager);
		mRecyclerView.setAdapter(mAdapter);
		Logger.d(TAG, "RecyclerView and Adapter set up");

		// Start data fetching
		getData();
	}

	/**
	 * Callback for handling network responses.
	 */
	private Callback fetchDataCallback = new Callback() {
		@Override
		public void onFailure(okhttp3.Call call, IOException e) {
			Logger.e(TAG, "getData() onFailure: " + e.getMessage(), e);
			if (getActivity() != null) {
				getActivity().runOnUiThread(() -> {
					mLoadingFlipper.setError("Failed to load data. Please check your connection.");
					mLoadingFlipper.showError();
				});
			}
		}

		@Override
		public void onResponse(okhttp3.Call call, Response response) throws IOException {
			if (!response.isSuccessful()) {
				Logger.e(TAG, "getData() onResponse unsuccessful: " + response.code());
				if (getActivity() != null) {
					getActivity().runOnUiThread(() -> {
						mLoadingFlipper.setError("Server error: " + response.code());
						mLoadingFlipper.showError();
					});
				}
				return;
			}
			mPageSource = response.body().string();
			Logger.d(TAG, "Received data from network");
			if (getActivity() != null) {
				// Use a lambda to reference the outer class's processData method
				getActivity().runOnUiThread(() -> processData());

				// Alternatively, use a method reference:
				// getActivity().runOnUiThread(WikiNewsFragment.this::processData);
			}
		}
	};

	/**
	 * Processes the fetched data by parsing HTML content and updating the RecyclerView.
	 */
	private void processData() {
		Logger.d(TAG, "Starting processData");
		try {
			String sample = mPageSource;
			Logger.v(TAG, "SAMPLE LENGTH: " + (sample != null ? sample.length() : "null"));

			// Find "Topics in the news"
			int topicsIndex = sample.indexOf("Topics in the news");
			if (topicsIndex == -1) {
				Logger.e(TAG, "Could not find 'Topics in the news'");
				mLoadingFlipper.setError("Failed to parse data: 'Topics in the news' section missing.");
				mLoadingFlipper.showError();
				return;
			}
			String slice1 = sample.substring(topicsIndex);
			Logger.v(TAG, "slice1 after 'Topics in the news' = " +
					(slice1.length() > 200 ? slice1.substring(0, 200) + "..." : slice1));

			// Find "Ongoing events</th>"
			int ongoingEventsIndex = slice1.indexOf("Ongoing events</th>");
			if (ongoingEventsIndex == -1) {
				Logger.e(TAG, "Could not find 'Ongoing events' section");
				mLoadingFlipper.setError("Failed to parse data: 'Ongoing events' section missing.");
				mLoadingFlipper.showError();
				return;
			}
			String todayraw = slice1.substring(0, ongoingEventsIndex);
			Logger.v(TAG, "todayraw after 'Ongoing events' = " +
					(todayraw.length() > 200 ? todayraw.substring(0, 200) + "..." : todayraw));

			// Initialize data list
			ArrayList<WikiData> lisArrayList = new ArrayList<>();
			lisArrayList.add(new WikiData("Topics in the News", WikiData.DataType.HEADER));

			// Parse list items for "Topics in the News"
			String[] lis = todayraw.split("<li>");
			if (lis == null || lis.length <= 1) {
				Logger.e(TAG, "Could not find any list items for 'Topics in the News'");
				mLoadingFlipper.setError("Failed to parse data: No list items found for 'Topics in the News'.");
				mLoadingFlipper.showError();
				return;
			}

			for (int i = 1; i < lis.length; i++) {
				int indexOf = lis[i].indexOf("</li>");
				if (indexOf == -1) {
					Logger.v(TAG, "Skipping invalid element in 'Topics in the News'");
					continue;
				}
				String html = lis[i].substring(0, indexOf).trim();
				html = html.replace("a href=\"/", "a href=\"https://en.m.wikipedia.org/");
				lisArrayList.add(new WikiData(html, WikiData.DataType.POST));
				Logger.v(TAG, "Added Topics in the News item: " + html);
			}
			Logger.v(TAG, "Added 'Topics in the News' items");

			// Find "Ongoing_conflicts"
			int ongoingConflictsIndex = slice1.indexOf("#Ongoing_conflicts");
			if (ongoingConflictsIndex == -1) {
				Logger.e(TAG, "Could not find 'Ongoing_conflicts' section");
				mLoadingFlipper.setError("Failed to parse data: 'Ongoing conflicts' section missing.");
				mLoadingFlipper.showError();
				return;
			}

			String todayOngoing = slice1.substring(ongoingEventsIndex, ongoingConflictsIndex);
			Logger.v(TAG, "todayOngoing after 'Ongoing events' = " +
					(todayOngoing.length() > 200 ? todayOngoing.substring(0, 200) + "..." : todayOngoing));

			// Parse list items for "Ongoing events"
			String[] lisOutgoing = todayOngoing.split("<li>");
			lisArrayList.add(new WikiData("Ongoing", WikiData.DataType.HEADER));
			if (lisOutgoing == null || lisOutgoing.length <= 1) {
				Logger.e(TAG, "Could not find any list items for 'Ongoing'");
				mLoadingFlipper.setError("Failed to parse data: No list items found for 'Ongoing'.");
				mLoadingFlipper.showError();
				return;
			}
			for (int i = 1; i < lisOutgoing.length; i++) {
				int endIndex = lisOutgoing[i].indexOf("</li>");
				if (endIndex == -1) {
					Logger.v(TAG, "Skipping invalid element in 'Ongoing events'");
					continue;
				}
				String html = lisOutgoing[i].substring(0, endIndex).trim();
				html = html.replace("a href=\"/", "a href=\"https://en.m.wikipedia.org/");
				lisArrayList.add(new WikiData(html, WikiData.DataType.POST));
				Logger.v(TAG, "Added Ongoing event item: " + html);
			}
			Logger.v(TAG, "Added 'Ongoing' items");

			// Find "Recent deaths"
			int recentDeathsIndex = slice1.indexOf("Recent deaths");
			if (recentDeathsIndex == -1) {
				Logger.e(TAG, "Could not find 'Recent deaths' section");
				mLoadingFlipper.setError("Failed to parse data: 'Recent deaths' section missing.");
				mLoadingFlipper.showError();
				return;
			}

			// Find end of table
			int tableEndIndex = slice1.indexOf("</table>", recentDeathsIndex);
			if (tableEndIndex == -1) {
				Logger.e(TAG, "Could not find end of 'Recent deaths' table");
				mLoadingFlipper.setError("Failed to parse data: 'Recent deaths' table end missing.");
				mLoadingFlipper.showError();
				return;
			}

			String recentDeaths = slice1.substring(recentDeathsIndex, tableEndIndex);
			Logger.v(TAG, "recentDeaths after 'Recent deaths' = " +
					(recentDeaths.length() > 200 ? recentDeaths.substring(0, 200) + "..." : recentDeaths));

			lisArrayList.add(new WikiData("Recent Deaths", WikiData.DataType.HEADER));

			// Parse list items for "Recent deaths"
			String[] lisDeaths = recentDeaths.split("<li>");
			if (lisDeaths == null || lisDeaths.length <= 1) {
				Logger.e(TAG, "Could not find list elements for 'Recent deaths'");
				mLoadingFlipper.setError("Failed to parse data: No list items found for 'Recent deaths'.");
				mLoadingFlipper.showError();
				return;
			}

			for (int i = 1; i < lisDeaths.length; i++) {
				int endIndex = lisDeaths[i].indexOf("</li>");
				if (endIndex == -1) {
					Logger.v(TAG, "Skipping invalid element in 'Recent deaths'");
					continue;
				}
				String html = lisDeaths[i].substring(0, endIndex).trim();
				html = html.replace("a href=\"/", "a href=\"https://en.m.wikipedia.org/");
				lisArrayList.add(new WikiData(html, WikiData.DataType.POST));
				Logger.v(TAG, "Added Recent Deaths item: " + html);
			}
			Logger.v(TAG, "Added 'Recent Deaths' items");

			// Parse "Current events of" sections
			String[] days = slice1.split("style=\"display:none;\">Current events of</span> ");
			if (days == null || days.length <= 1) {
				Logger.e(TAG, "Could not find 'Current events of' sections");
				mLoadingFlipper.setError("Failed to parse data: 'Current events of' sections missing.");
				mLoadingFlipper.showError();
				return;
			}

			for (int i = 1; i < days.length; i++) {
				int spanIndex = days[i].indexOf("<span style=\"display:none\">");
				if (spanIndex == -1) {
					Logger.v(TAG, "Skipping invalid 'Current events of' day element");
					continue;
				}

				String header = days[i].substring(0, spanIndex).trim();
				lisArrayList.add(new WikiData(header, WikiData.DataType.HEADER));
				Logger.v(TAG, "Added 'Current events of' header: " + header);

				String[] dayItems = days[i].split("<li>");
				if (dayItems == null || dayItems.length <= 1) {
					Logger.e(TAG, "Could not find list items for specific day: " + header);
					mLoadingFlipper.setError("Failed to parse data: No list items found for day '" + header + "'.");
					mLoadingFlipper.showError();
					return;
				}

				for (int j = 1; j < dayItems.length; j++) {
					int val = dayItems[j].indexOf("</li>");
					if (val == -1) {
						Logger.v(TAG, "Skipping invalid list element in day: " + header);
						continue;
					}
					String html = dayItems[j].substring(0, val).trim();
					html = html.replace("a href=\"/", "a href=\"https://en.m.wikipedia.org/");
					lisArrayList.add(new WikiData(html, WikiData.DataType.POST));
					Logger.v(TAG, "Added day item: " + html);
				}
			}
			Logger.v(TAG, "Added daily events");

			Logger.v(TAG, "Total items parsed: " + lisArrayList.size());
			mAdapter.updateData(lisArrayList);
			mLoadingFlipper.showContent();
			Logger.d(TAG, "processData completed successfully");

		} catch (StringIndexOutOfBoundsException e) {
			Logger.e(TAG, "processData() StringIndexOutOfBoundsException: " + e.getMessage(), e);
			mLoadingFlipper.setError("Failed to parse data due to unexpected format.");
			mLoadingFlipper.showError();
		} catch (Exception e) {
			Logger.e(TAG, "processData() Exception: " + e.getMessage(), e);
			mLoadingFlipper.setError("An unexpected error occurred while processing data.");
			mLoadingFlipper.showError();
		}
	}

	/**
	 * Initiates data fetching from the network.
	 */
	private void getData() {
		Logger.d(TAG, "Initiating data fetch");
		OkHttpClient okHttpClient = new OkHttpClient.Builder()
				.connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
				.readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
				.writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
				.build();
		Request request = new Request.Builder().url("https://en.m.wikipedia.org/wiki/Portal:Current_events").build();
		Logger.d(TAG, "Making HTTP call to " + request.url());

		mLoadingFlipper.showLoading();
		okHttpClient.newCall(request).enqueue(fetchDataCallback);
	}

	/**
	 * Implementation of NotificationDialogListener interface methods
	 */
	@Override
	public void onPositiveButtonClicked(Intent intent) {
		// Handle positive button click
		Logger.d(TAG, "Positive button clicked with intent: " + intent);
		// Perform desired action, e.g., retry fetching data
		getData();
	}

	@Override
	public void onNegativeButtonClicked(Intent intent) {
		// Handle negative button click
		Logger.d(TAG, "Negative button clicked with intent: " + intent);
		// Perform desired action, e.g., dismiss the dialog
	}
}
