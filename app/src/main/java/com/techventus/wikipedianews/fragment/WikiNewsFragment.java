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
import com.techventus.wikipedianews.data.HardcodedNewsData;
import com.techventus.wikipedianews.manager.PreferencesManager;
import com.techventus.wikipedianews.logging.Logger;
import com.techventus.wikipedianews.view.LoadingViewFlipper;
import com.techventus.wikipedianews.dialogfragment.NotificationDialogFragment;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

/**
 * Updated WikiNewsFragment with enhanced logging and error handling using Jsoup.
 */
public class WikiNewsFragment extends WikiFragment implements NotificationDialogFragment.NotificationDialogListener {

	private String mPageSource;
	private static final String TAG = WikiNewsFragment.class.getSimpleName();
	private LoadingViewFlipper mLoadingFlipper;
	private RecyclerView mRecyclerView;
	private WikiAdapter mAdapter;
	private RecyclerView.LayoutManager mLayoutManager;
	private ArrayList<WikiData> mData = new ArrayList<>();
	private PreferencesManager mPreferencesManager;

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

		mPreferencesManager = PreferencesManager.getInstance();

		// Decide whether to load hardcoded data or live data
		long expirationTimestamp = mPreferencesManager.getHardcodedDataExpiration();
		long currentTime = new Date().getTime();

		if (expirationTimestamp > 0 && currentTime < expirationTimestamp) {
			Logger.d(TAG, "Using hardcoded data as current time (" + currentTime + ") is before expiration (" + expirationTimestamp + ")");
			loadHardcodedData();
		} else {
			if (expirationTimestamp > 0) {
				Logger.d(TAG, "Hardcoded data expired. Current time: " + currentTime + ", Expiration: " + expirationTimestamp);
			} else {
				Logger.d(TAG, "Hardcoded data not enabled or no expiration set.");
			}
			// Start data fetching for live data
			getData();
		}
	}

	private void loadHardcodedData() {
		Logger.d(TAG, "Loading hardcoded data");
		// Ensure mData is cleared or re-initialized if it could contain old data
		mData.clear();
		ArrayList<WikiData> hardcodedItems = HardcodedNewsData.getHardcodedData();
		if (hardcodedItems != null && !hardcodedItems.isEmpty()) {
			mData.addAll(hardcodedItems); // Add items to existing mData
			mAdapter.updateData(mData); // Update adapter with the new mData
			mLoadingFlipper.showContent();
			Logger.d(TAG, "Hardcoded data loaded successfully");
		} else {
			Logger.e(TAG, "Hardcoded data is null or empty");
			mLoadingFlipper.setError("Failed to load hardcoded data.");
			mLoadingFlipper.showError();
		}
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
	 * Processes the fetched data by parsing HTML content using Jsoup and updating the RecyclerView.
	 */
	private void processData() {
		Logger.d(TAG, "Starting processData with Jsoup");
		try {
			Document doc = Jsoup.parse(mPageSource);

			ArrayList<WikiData> lisArrayList = new ArrayList<>();

			// Parse "Topics in the News"
			try {
				Element topicsSection = doc.selectFirst("div[aria-labelledby=Topics_in_the_news]");
				if (topicsSection != null) {
					Element topicsList = topicsSection.selectFirst("ul");
					if (topicsList != null) {
						lisArrayList.add(new WikiData("Topics in the News", WikiData.DataType.HEADER));
						Elements topicItems = topicsList.select("li");
						for (Element item : topicItems) {
							String html = item.html().trim();
							html = html.replace("a href=\"/", "a href=\"https://en.m.wikipedia.org/");
							lisArrayList.add(new WikiData(html, WikiData.DataType.POST));
							Logger.v(TAG, "Added Topics in the News item: " + html);
						}
					} else {
						Logger.e(TAG, "List items under 'Topics in the news' not found");
					}
				} else {
					Logger.e(TAG, "'Topics in the news' section not found");
				}
			} catch (Exception e) {
				Logger.e(TAG, "Error parsing 'Topics in the news' section: " + e.getMessage(), e);
			}

			// Parse "Ongoing events"
			try {
				Element ongoingSection = doc.selectFirst("div[aria-labelledby=Ongoing_events]");
				if (ongoingSection != null) {
					Element ongoingList = ongoingSection.selectFirst("ul");
					if (ongoingList != null) {
						lisArrayList.add(new WikiData("Ongoing", WikiData.DataType.HEADER));
						Elements ongoingItems = ongoingList.select("li");
						for (Element item : ongoingItems) {
							String html = item.html().trim();
							html = html.replace("a href=\"/", "a href=\"https://en.m.wikipedia.org/");
							lisArrayList.add(new WikiData(html, WikiData.DataType.POST));
							Logger.v(TAG, "Added Ongoing event item: " + html);
						}
					} else {
						Logger.e(TAG, "List items under 'Ongoing events' not found");
					}
				} else {
					Logger.e(TAG, "'Ongoing events' section not found");
				}
			} catch (Exception e) {
				Logger.e(TAG, "Error parsing 'Ongoing events' section: " + e.getMessage(), e);
			}


			// Parse "Recent deaths"
			try {
				Element deathsSection = doc.selectFirst("div[aria-labelledby=Recent_deaths]");
				if (deathsSection != null) {
					Element deathsList = deathsSection.selectFirst("ul");
					if (deathsList != null) {
						lisArrayList.add(new WikiData("Recent Deaths", WikiData.DataType.HEADER));
						Elements deathItems = deathsList.select("li");
						for (Element item : deathItems) {
							String html = item.html().trim();
							html = html.replace("a href=\"/", "a href=\"https://en.m.wikipedia.org/");
							lisArrayList.add(new WikiData(html, WikiData.DataType.POST));
							Logger.v(TAG, "Added Recent Deaths item: " + html);
						}
					} else {
						Logger.e(TAG, "List items under 'Recent deaths' not found");
					}
				} else {
					Logger.e(TAG, "'Recent deaths' section not found");
				}
			} catch (Exception e) {
				Logger.e(TAG, "Error parsing 'Recent deaths' section: " + e.getMessage(), e);
			}

			// Parse "Current events of" sections (e.g., specific days)
			try {
				Elements dayHeaders = doc.select("div.current-events-heading");
				if (dayHeaders != null && !dayHeaders.isEmpty()) {
					for (Element dayHeader : dayHeaders) {

						Element title = dayHeader.selectFirst("span.summary");
						String headerText = title.text().replace(" (Saturday)", "").replace(" (Sunday)", "").replace(" (Monday)", "").replace(" (Tuesday)", "").replace(" (Wednesday)", "").replace(" (Thursday)", "").replace(" (Friday)", "").trim();
						lisArrayList.add(new WikiData(headerText, WikiData.DataType.HEADER));
						Logger.v(TAG, "Added 'Current events of' header: " + headerText);

						Element dayList = dayHeader.parent().selectFirst("div.current-events-content > ul");

						if (dayList != null) {
							Elements dayItems = dayList.select("li");
							for (Element item : dayItems) {
								String html = item.html().trim();
								html = html.replace("a href=\"/", "a href=\"https://en.m.wikipedia.org/");
								lisArrayList.add(new WikiData(html, WikiData.DataType.POST));
								Logger.v(TAG, "Added day item: " + html);
							}
						} else {
							Logger.e(TAG, "List items under 'Current events of " + headerText + "' not found");
						}
					}
				} else {
					Logger.e(TAG, "'Current events of' sections not found");
				}
			} catch (Exception e) {
				Logger.e(TAG, "Error parsing 'Current events of' sections: " + e.getMessage(), e);
			}

			// After parsing all sections, check if any data was collected
			if (lisArrayList.isEmpty()) {
				Logger.e(TAG, "No data parsed from the fetched content");
				mLoadingFlipper.setError("Failed to parse data: No sections found.");
				mLoadingFlipper.showError();
			} else {
				Logger.v(TAG, "Total items parsed: " + lisArrayList.size());
				mAdapter.updateData(lisArrayList);
				mLoadingFlipper.showContent();
				Logger.d(TAG, "processData completed successfully");
			}

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
