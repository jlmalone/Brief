package com.techventus.wikipedianews;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.techventus.wikipedianews.logging.Logger;
import com.techventus.wikipedianews.util.ArrayUtil;
import com.techventus.wikipedianews.view.WikiHeaderViewHolder;
import com.techventus.wikipedianews.view.WikiViewHolder;
import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;

/**
 * Created by josephmalone on 16-08-23.
 */
public class WikiAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
	private static final String TAG = WikiAdapter.class.getSimpleName();
	private ArrayList<WikiData> mData = new ArrayList<>();

	public WikiAdapter()
	{

	}

	public void updateData(ArrayList<WikiData> items)
	{
		mData = items;
	}


	@Override
	public int getItemViewType(int position)
	{

		//		return super.getItemViewType(position);
		return mData.get(position).getType().ordinal();

	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		if (viewType == WikiData.DataType.POST.ordinal())
		{
			View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.wiki_item, parent, false);
			return new WikiViewHolder(parent.getContext(), view);
		}
		else
		{
			View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.wiki_header, parent, false);
			return new WikiViewHolder(parent.getContext(), view);
		}
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
	{
		final View itemView = holder.itemView;
		WikiHeaderViewHolder vh = (WikiHeaderViewHolder) holder;

		Logger.v(TAG, "bind view "+mData.get(position).getText());


		if(getItemViewType(position)== WikiData.DataType.HEADER.ordinal())
		{
			((WikiHeaderViewHolder)holder).bindItem(mData.get(position).getText());
		}
		else if (getItemViewType(position)==WikiData.DataType.POST.ordinal())
		{
			((WikiViewHolder)holder).bindItem(mData.get(position).getText());

		}

		vh.bindItem(mData.get(position).getText());
	}

	@Override
	public int getItemCount()
	{
		if (ArrayUtil.isNullOrEmpty(mData))
		{
			return 0;
		}
		else
		{
			return mData.size();
		}
	}
}
