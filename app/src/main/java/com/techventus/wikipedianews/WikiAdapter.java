package com.techventus.wikipedianews;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.techventus.wikipedianews.logging.Logger;
import com.techventus.wikipedianews.view.WikiHeaderViewHolder;
import com.techventus.wikipedianews.view.WikiViewHolder;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**
 * Updated WikiAdapter with proper ViewHolder handling and enhanced logging.
 */
public class WikiAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
	private static final String TAG = WikiAdapter.class.getSimpleName();
	private ArrayList<WikiData> mData = new ArrayList<>();

	public WikiAdapter() {
		Logger.d(TAG, "WikiAdapter initialized");
	}

	public void updateData(ArrayList<WikiData> items) {
		Logger.d(TAG, "Updating adapter data with " + (items != null ? items.size() : "null") + " items");
		mData = items != null ? items : new ArrayList<>();
		notifyDataSetChanged();
	}

	@Override
	public int getItemViewType(int position) {
		int viewType = mData.get(position).getType().ordinal();
		Logger.v(TAG, "getItemViewType for position " + position + ": " + viewType);
		return viewType;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		Logger.d(TAG, "onCreateViewHolder called with viewType: " + viewType);
		LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		if (viewType == WikiData.DataType.POST.ordinal()) {
			View view = inflater.inflate(R.layout.wiki_item, parent, false);
			Logger.v(TAG, "Inflated wiki_item layout for POST view type");
			return new WikiViewHolder(parent.getContext(), view);
		} else {
			View view = inflater.inflate(R.layout.wiki_header, parent, false);
			Logger.v(TAG, "Inflated wiki_header layout for HEADER view type");
			return new WikiHeaderViewHolder(parent.getContext(), view);
		}
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		WikiData data = mData.get(position);
		Logger.v(TAG, "onBindViewHolder called for position " + position + " with data: " + data.getText());

		try {
			if (getItemViewType(position) == WikiData.DataType.HEADER.ordinal()) {
				WikiHeaderViewHolder headerHolder = (WikiHeaderViewHolder) holder;
				headerHolder.bindItem(data.getText());
				Logger.v(TAG, "Bound HEADER data at position " + position);
			} else if (getItemViewType(position) == WikiData.DataType.POST.ordinal()) {
				WikiViewHolder postHolder = (WikiViewHolder) holder;
				postHolder.bindItem(data.getText());
				Logger.v(TAG, "Bound POST data at position " + position);
			}
		} catch (ClassCastException e) {
			Logger.e(TAG, "ViewHolder casting error at position " + position, e);
		} catch (Exception e) {
			Logger.e(TAG, "Unexpected error in onBindViewHolder at position " + position, e);
		}
	}

	@Override
	public int getItemCount() {
		int count = (mData == null || mData.isEmpty()) ? 0 : mData.size();
		Logger.v(TAG, "getItemCount: " + count);
		return count;
	}
}