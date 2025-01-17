package com.techventus.wikipedianews.fragment;

import android.os.Bundle;
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

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by josephmalone on 16-08-23.
 */
public class WikiNewsFragment extends WikiFragment
{

	private String mPageSource;
	private static final String TAG = WikiNewsFragment.class.getSimpleName();
	private LoadingViewFlipper mLoadingFlipper;
	private RecyclerView mRecyclerView;
	private WikiAdapter mAdapter;
	private RecyclerView.LayoutManager mLayoutManager;
	private ArrayList<String> mData;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.category_fragment, container, false);
	}

	@Override
	public void onViewCreated(View v, Bundle savedInstanceState)
	{
		super.onViewCreated(v, savedInstanceState);
//		setTitle(R.string.app_name);
		mLoadingFlipper = (LoadingViewFlipper) v;
		Logger.d(TAG, "OnViewCreated");
		//Create an empty adapter.
		mAdapter = new WikiAdapter();//this.getActivity(), null, mNumberColumns);
		mRecyclerView = (RecyclerView) v.findViewById(R.id.recyclerview);
		mRecyclerView.setHasFixedSize(true);
		mLayoutManager = new LinearLayoutManager(getActivity());

		mRecyclerView.setLayoutManager(mLayoutManager);
		mRecyclerView.setAdapter(mAdapter);

		getData();
	}


	private Callback fetchDataCallback = new Callback()
	{
		@Override
		public void onFailure(okhttp3.Call call, IOException e)
		{
			Logger.e(TAG, "getData() onFailure " + e, e);
		}

		@Override
		public void onResponse(okhttp3.Call call, Response response) throws IOException
		{
			mPageSource = response.body().string();
			getActivity().runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					processData();
				}
			});

		}
	};
	private void processData() {
		try
		{
			String sample = mPageSource;

			Logger.v(TAG, "SAMPLE IS " + sample);
			//Everything after In the News
			int topicsIndex = sample.indexOf("Topics in the news");
			if (topicsIndex == -1)
			{
				Logger.e(TAG, "Could not find Topics in the news");
				return;
			}

			String slice1 = sample.substring(topicsIndex);
			Logger.v(TAG, "slice1 after topic =" + slice1);

			int ongoingEventsIndex = slice1.indexOf("Ongoing events</th>");
			if(ongoingEventsIndex == -1)
			{
				Logger.e(TAG, "Could not find Ongoing events");
				return;
			}

			String todayraw = slice1.substring(0, ongoingEventsIndex);
			Logger.v(TAG, "todayraw after Ongoing events =" + todayraw);

			//Break into LI Array
			ArrayList<WikiData> lisArrayList = new ArrayList<>();
			lisArrayList.add(new WikiData("Topics in the News", WikiData.DataType.HEADER));
			String[] lis = todayraw.split("<li>");
			if(lis == null || lis.length <= 1)
			{
				Logger.e(TAG, "Could not find any list items");
				return;
			}

			for (int i = 1; i < lis.length; i++)
			{
				int indexOf = lis[i].indexOf("</li>");
				if (indexOf == -1)
				{
					Logger.v(TAG, "skipping invalid element");
					continue;
				}
				String html = lis[i].substring(0, indexOf);
				html = html.replace("a href=\"/", "a href=\"https://en.m.wikipedia.org/");
				lisArrayList.add(new WikiData(html, WikiData.DataType.POST));
				Logger.v(TAG, "ADD " + html);
			}
			Logger.v(TAG, "Added Topics in the news");


			int ongoingConflictsIndex = sample.indexOf("#Ongoing_conflicts");
			if(ongoingConflictsIndex == -1)
			{
				Logger.e(TAG, "Could not find Ongoing conflicts section");
				return;
			}

			String todayOnging = sample.substring(ongoingEventsIndex, ongoingConflictsIndex);
			Logger.v(TAG, "todayOnging after Ongoing events =" + todayOnging);

			String[] lisOutgoing = todayOnging.split("<li>");

			lisArrayList.add(new WikiData("Ongoing", WikiData.DataType.HEADER));
			if(lisOutgoing == null || lisOutgoing.length <= 1)
			{
				Logger.e(TAG, "Could not find any list items for Ongoing");
				return;
			}
			for (int i = 1; i < lisOutgoing.length; i++) {
				int endIndex = lisOutgoing[i].indexOf("</li>");
				if (endIndex == -1)
				{
					Logger.v(TAG, "skipping invalid element for ongoing");
					continue;
				}
				String html = lisOutgoing[i].substring(0, endIndex);
				html = html.replace("a href=\"/", "a href=\"https://en.m.wikipedia.org/");
				lisArrayList.add(new WikiData(html, WikiData.DataType.POST));
				Logger.v(TAG, "ADD ongoing " + html);
			}
			Logger.v(TAG, "Added Ongoing events");


			int recentDeathsIndex = slice1.indexOf("Recent deaths");
			if(recentDeathsIndex == -1)
			{
				Logger.e(TAG, "Could not find Recent deaths");
				return;
			}
			int tableEndIndex = slice1.indexOf("</table>");
			if(tableEndIndex == -1)
			{
				Logger.e(TAG, "Could not find end of table");
				return;
			}


			lisArrayList.add(new WikiData("Recent Deaths", WikiData.DataType.HEADER));
			String recentDeaths = slice1.substring(recentDeathsIndex, tableEndIndex);
			Logger.v(TAG, "recentDeaths after Recent deaths = " + recentDeaths);


			String[] lisDeaths = recentDeaths.split("<li>");
			if(lisDeaths == null || lisDeaths.length <= 1)
			{
				Logger.e(TAG, "Could not find list elements for recent deaths");
				return;
			}

			for (int i = 1; i < lisDeaths.length; i++) {

				int endIndex = lisDeaths[i].indexOf("</li>");
				if (endIndex == -1)
				{
					Logger.v(TAG, "skipping invalid element for deaths");
					continue;
				}
				String html = lisDeaths[i].substring(0, endIndex);
				html = html.replace("a href=\"/", "a href=\"https://en.m.wikipedia.org/");
				lisArrayList.add(new WikiData(html, WikiData.DataType.POST));
				Logger.v(TAG, "ADD deaths " + html);
			}
			Logger.v(TAG, "Added Recent Deaths");

			String[] days = slice1.split("style=\"display:none;\">Current events of</span> ");
			if(days == null || days.length <= 1)
			{
				Logger.e(TAG, "Could not find Current events of section");
				return;
			}

			for (int i = 1; i < days.length; i++)
			{
				int spanIndex = days[i].indexOf("<span style=\"display:none\">");

				if(spanIndex == -1)
				{
					Logger.v(TAG, "skipping invalid day element");
					continue;
				}

				String header = days[i].substring(0, spanIndex);

				lisArrayList.add(new WikiData(header, WikiData.DataType.HEADER));
				Logger.v(TAG, "header ="+ header);

				String[] day = days[i].split("<li>");
				if (day == null || day.length <= 1)
				{
					Logger.e(TAG, "Could not find list items for specific days");
					return;
				}
				for (int j = 1; j < day.length; j++) {
					int val = day[j].indexOf("</li>");
					if (val == -1) {
						Logger.v(TAG, "skipping invalid list element in a day");
						continue;
					}
					String html = day[j].substring(0, val);
					html = html.replace("a href=\"/", "a href=\"https://en.m.wikipedia.org/");
					lisArrayList.add(new WikiData(html, WikiData.DataType.POST));
					Logger.v(TAG, "ADD day " + html);
				}

			}
			Logger.v(TAG, "Added daily events");

			Logger.v(TAG, "LIS SIZE " + lisArrayList.size());
			mAdapter.updateData(lisArrayList);
			mAdapter.notifyDataSetChanged();
			mLoadingFlipper.showContent();

		}
		catch (StringIndexOutOfBoundsException e)
		{
			Logger.e(TAG, "processData() StringIndexOutOfBoundsException : " + e, e);
		}
		catch (Exception e) {
			Logger.e(TAG, "processData() Exception : " + e, e);
		}
	}
	private void getData()
	{
		OkHttpClient okHttpClient = new OkHttpClient();
		Request request = new Request.Builder().url("https://en.m.wikipedia.org/wiki/Portal:Current_events").build();
		Logger.d(TAG, "Making HTTP call to " + request.url());

		okHttpClient.newCall(request).enqueue(fetchDataCallback);
	}
}