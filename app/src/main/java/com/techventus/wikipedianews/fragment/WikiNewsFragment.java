package com.techventus.wikipedianews.fragment;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
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
		setTitle(R.string.app_name);
		mLoadingFlipper = (LoadingViewFlipper) v;
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
		public void onFailure(Request request, IOException e)
		{

		}

		@Override
		public void onResponse(Response response) throws IOException
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

	private void processData()
	{
		String sample = mPageSource;

		Logger.v(TAG, "SAMPLE IS " + sample);
		//Everything after In the News
		String slice1 = sample.substring(sample.indexOf("Topics in the news"));
		slice1 = slice1.substring(0, slice1.indexOf("Ongoing " + "events"));

		String todayraw = slice1.substring(0, slice1.indexOf("<table class=\"infobox\""));

		//Break into LI Array
		ArrayList<WikiData> lisArrayList = new ArrayList<>();
//		lisArrayList.add(new WikiData("Topics in the News", WikiData.DataType.HEADER));
		String[] lis = todayraw.split("<li>");
		for (int i = 1; i < lis.length; i++)
		{
			int indexOf = lis[i].indexOf("</li>");
			if (lis[i].length() < 4 || (indexOf <= 0 || indexOf > lis[i].length()))
			{
				continue;
			}
			String html = lis[i].substring(0, indexOf);
			html = html.replace("a href=\"/", "a href=\"https://en.m.wikipedia.org/");
//			lisArrayList.add(new WikiData(html, WikiData.DataType.POST));
			Logger.v(TAG, "ADD " + html);
		}

		lisArrayList.add(new WikiData("Ongoing", WikiData.DataType.HEADER));
		String todayOnging = sample.substring(sample.indexOf("Ongoing events</th>"), sample.indexOf("#Ongoing_conflicts"));

		String[] lisOutgoing = todayOnging.split("<li>");

		for (int i = 1; i < lisOutgoing.length; i++)
		{
			String html = lisOutgoing[i].substring(0, lisOutgoing[i].indexOf("</li>"));
			html = html.replace("a href=\"/", "a href=\"https://en.m.wikipedia.org/");
			lisArrayList.add(new WikiData(html, WikiData.DataType.POST));
			Logger.v(TAG, "ADD " + html);
		}


		lisArrayList.add(new WikiData("Recent Deaths", WikiData.DataType.HEADER));
		String recentDeaths = slice1.substring(slice1.indexOf("Recent deaths"), slice1.indexOf("</table>"));
		String[] lisDeaths = recentDeaths.split("<li>");

		for (int i = 1; i < lisDeaths.length; i++)
		{
			String html = lisDeaths[i].substring(0, lisDeaths[i].indexOf("</li>"));
			html = html.replace("a href=\"/", "a href=\"https://en.m.wikipedia.org/");
			lisArrayList.add(new WikiData(html, WikiData.DataType.POST));
			Logger.v(TAG, "ADD " + html);
		}

		String[] days = slice1.split("style=\"display:none;\">Current events of</span> ");
		for (int i = 1; i < days.length; i++)
		{
			String header = days[i].substring(0, days[i].indexOf("<span style=\"display:none\">"));
			lisArrayList.add(new WikiData(header, WikiData.DataType.HEADER));


			String[] day = days[i].split("<li>");

			if (day != null && day.length > 0)
			{
				for (int j = 1; j < day.length; j++)
				{
					int val = day[j].indexOf("</li>");
					if (val == -1)
					{
						continue;
					}
					String html = day[j].substring(0, val);
					html = html.replace("a href=\"/", "a href=\"https://en.m.wikipedia.org/");
					lisArrayList.add(new WikiData(html, WikiData.DataType.POST));
					Logger.v(TAG, "ADD " + html);
				}
			}

		}
		String slice2 = slice1.substring(slice1.indexOf("style=\"display:none;\">Current events of</span> "));

		Logger.v(TAG, "LIS SIZE " + lisArrayList.size());
		mAdapter.updateData(lisArrayList);
		mAdapter.notifyDataSetChanged();
		mLoadingFlipper.showContent();
	}


	private void getData()
	{

		OkHttpClient okHttpClient = new OkHttpClient();
		Request request = new Request.Builder().url("https://en.m.wikipedia.org/wiki/Portal:Current_events").build();
		okHttpClient.newCall(request).enqueue(fetchDataCallback);

	}
}
