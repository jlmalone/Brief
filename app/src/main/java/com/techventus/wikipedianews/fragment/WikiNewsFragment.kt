package com.techventus.wikipedianews.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.techventus.wikipedianews.R
import com.techventus.wikipedianews.WikiAdapter
import com.techventus.wikipedianews.WikiData
import com.techventus.wikipedianews.dialogfragment.NotificationDialogFragment
import com.techventus.wikipedianews.logging.Logger
import com.techventus.wikipedianews.view.LoadingViewFlipper
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.jsoup.Jsoup
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Updated WikiNewsFragment with enhanced logging and error handling using Jsoup.
 */
class WikiNewsFragment : WikiFragment(), NotificationDialogFragment.NotificationDialogListener {

    private var mPageSource: String? = null
    private lateinit var mLoadingFlipper: LoadingViewFlipper
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mAdapter: WikiAdapter
    private lateinit var mLayoutManager: RecyclerView.LayoutManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Logger.d(TAG, "onCreateView called")
        return inflater.inflate(R.layout.category_fragment, container, false)
    }

    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
        super.onViewCreated(v, savedInstanceState)
        Logger.d(TAG, "onViewCreated called")

        // Initialize LoadingViewFlipper
        mLoadingFlipper = v.findViewById(R.id.loading_view_flipper)
        Logger.d(TAG, "Initialized LoadingViewFlipper")

        // Set Retry Callback to handle retry actions from the error view
        mLoadingFlipper.setRetryCallback(object : LoadingViewFlipper.RetryCallback {
            override fun onRetry() {
                Logger.d(TAG, "RetryCallback triggered")
                getData() // Re-initiate data fetching
            }
        })

        // Initialize RecyclerView and Adapter
        mAdapter = WikiAdapter()
        mRecyclerView = mLoadingFlipper.findViewById(R.id.recyclerview_content) // Corrected ID
        // It's good practice to check if findViewById returned null, though with non-null assertion it would crash earlier.
        // However, since mLoadingFlipper itself could be null if the outer view (v) didn't contain it,
        // a more robust check would involve checking mLoadingFlipper first.
        // Given the current structure, if mRecyclerView is null, it implies a layout issue.
        mRecyclerView.setHasFixedSize(true)
        mLayoutManager = LinearLayoutManager(activity)

        mRecyclerView.layoutManager = mLayoutManager
        mRecyclerView.adapter = mAdapter
        Logger.d(TAG, "RecyclerView and Adapter set up")

        // Start data fetching
        getData()
    }

    /**
     * Callback for handling network responses.
     */
    private val fetchDataCallback: Callback = object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            Logger.e(TAG, "getData() onFailure: " + e.message, e)
            activity?.runOnUiThread {
                mLoadingFlipper.setError("Failed to load data. Please check your connection.")
                mLoadingFlipper.showError()
            }
        }

        override fun onResponse(call: Call, response: Response) {
            if (!response.isSuccessful) {
                Logger.e(TAG, "getData() onResponse unsuccessful: " + response.code)
                activity?.runOnUiThread {
                    mLoadingFlipper.setError("Server error: " + response.code)
                    mLoadingFlipper.showError()
                }
                return
            }
            mPageSource = response.body?.string() // Use safe call for body
            Logger.d(TAG, "Received data from network")
            activity?.runOnUiThread { processData() }
        }
    }

    /**
     * Processes the fetched data by parsing HTML content using Jsoup and updating the RecyclerView.
     */
    private fun processData() {
        Logger.d(TAG, "Starting processData with Jsoup")
        try {
            val doc = Jsoup.parse(mPageSource ?: "") // Provide a default empty string if mPageSource is null

            val lisArrayList = ArrayList<WikiData>()

            // Helper function to parse sections
            fun parseSection(sectionLabel: String, headerTitle: String) {
                try {
                    val section = doc.selectFirst("div[aria-labelledby=$sectionLabel]")
                    if (section != null) {
                        val list = section.selectFirst("ul")
                        if (list != null) {
                            lisArrayList.add(WikiData(headerTitle, WikiData.DataType.HEADER))
                            val items = list.select("li")
                            for (item in items) {
                                var html = item.html().trim()
                                html = html.replace("a href=\"/", "a href=\"https://en.m.wikipedia.org/")
                                lisArrayList.add(WikiData(html, WikiData.DataType.POST))
                                Logger.v(TAG, "Added $headerTitle item: $html")
                            }
                        } else {
                            Logger.e(TAG, "List items under '$headerTitle' not found")
                        }
                    } else {
                        Logger.e(TAG, "'$headerTitle' section not found")
                    }
                } catch (e: Exception) {
                    Logger.e(TAG, "Error parsing '$headerTitle' section: " + e.message, e)
                }
            }

            parseSection("Topics_in_the_news", "Topics in the News")
            parseSection("Ongoing_events", "Ongoing")
            parseSection("Recent_deaths", "Recent Deaths")

            // Parse "Current events of" sections (e.g., specific days)
            try {
                val dayHeaders = doc.select("div.current-events-heading")
                if (dayHeaders.isNotEmpty()) {
                    for (dayHeader in dayHeaders) {
                        val titleElement = dayHeader.selectFirst("span.summary")
                        titleElement?.let {
                            val headerText = it.text().replace(Regex(" \\(.*?\\)"), "").trim()
                            lisArrayList.add(WikiData(headerText, WikiData.DataType.HEADER))
                            Logger.v(TAG, "Added 'Current events of' header: $headerText")

                            val dayList = dayHeader.parent()?.selectFirst("div.current-events-content > ul")
                            if (dayList != null) {
                                val dayItems = dayList.select("li")
                                for (item in dayItems) {
                                    var html = item.html().trim()
                                    html = html.replace("a href=\"/", "a href=\"https://en.m.wikipedia.org/")
                                    lisArrayList.add(WikiData(html, WikiData.DataType.POST))
                                    Logger.v(TAG, "Added day item: $html")
                                }
                            } else {
                                Logger.e(TAG, "List items under 'Current events of $headerText' not found")
                            }
                        }
                    }
                } else {
                    Logger.e(TAG, "'Current events of' sections not found")
                }
            } catch (e: Exception) {
                Logger.e(TAG, "Error parsing 'Current events of' sections: " + e.message, e)
            }

            // After parsing all sections, check if any data was collected
            if (lisArrayList.isEmpty()) {
                Logger.e(TAG, "No data parsed from the fetched content")
                mLoadingFlipper.setError("Failed to parse data: No sections found.")
                mLoadingFlipper.showError()
            } else {
                Logger.v(TAG, "Total items parsed: " + lisArrayList.size)
                mAdapter.updateData(lisArrayList)
                mLoadingFlipper.showContent()
                Logger.d(TAG, "processData completed successfully")
            }

        } catch (e: Exception) {
            Logger.e(TAG, "processData() Exception: " + e.message, e)
            mLoadingFlipper.setError("An unexpected error occurred while processing data.")
            mLoadingFlipper.showError()
        }
    }

    /**
     * Initiates data fetching from the network.
     */
    private fun getData() {
        Logger.d(TAG, "Initiating data fetch")
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
        val request = Request.Builder().url("https://en.m.wikipedia.org/wiki/Portal:Current_events").build()
        Logger.d(TAG, "Making HTTP call to " + request.url)

        mLoadingFlipper.showLoading()
        okHttpClient.newCall(request).enqueue(fetchDataCallback)
    }

    /**
     * Implementation of NotificationDialogListener interface methods
     */
    override fun onPositiveButtonClicked(intent: Intent) {
        // Handle positive button click
        Logger.d(TAG, "Positive button clicked with intent: $intent")
        // Perform desired action, e.g., retry fetching data
        getData()
    }

    override fun onNegativeButtonClicked(intent: Intent) {
        // Handle negative button click
        Logger.d(TAG, "Negative button clicked with intent: $intent")
        // Perform desired action, e.g., dismiss the dialog
    }

    companion object {
        private val TAG = WikiNewsFragment::class.java.simpleName
    }
}
