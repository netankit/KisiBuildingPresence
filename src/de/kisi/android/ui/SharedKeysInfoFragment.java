package de.kisi.android.ui;

import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import de.kisi.android.R;
import de.kisi.android.api.KisiAPI;
import de.kisi.android.api.SharedKeyInfoCallback;
import de.kisi.android.model.Key;

public class SharedKeysInfoFragment extends BaseFragment implements
		SharedKeyInfoCallback {

	private ListView mListView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.sharedkeysloginfo, container,
				false);
		mListView = (ListView) view
				.findViewById(R.id.place_sharedkeylog_listview);
		KisiAPI.getInstance().getSharedKeysLogsInfo(place, this);
		return view;
	}

	public void onSharedLogsInfoResult(List<Key> events) {
		try {
			mListView.setAdapter(new SharedKeysInfoLogAdapter(events));
		} catch (Exception e) {

		}
	}

	class SharedKeysInfoLogAdapter extends BaseAdapter {

		private List<Key> events;
		private LayoutInflater inflater;

		public SharedKeysInfoLogAdapter(List<Key> events) {
			super();
			this.events = events;
			inflater = (LayoutInflater) getActivity().getSystemService(
					Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return events.size();
		}

		@Override
		public Object getItem(int position) {
			return events.get(position);
		}

		@Override
		public long getItemId(int position) {
			return events.get(position).getId();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View vi = convertView;
			if (vi == null)
				vi = inflater.inflate(R.layout.sharedkeyslog_list_item, null);
			TextView userEmailView = (TextView) vi.findViewById(R.id.userEmail);
			userEmailView.setText(events.get(position).getIssued_to_email());
			Log.v("Test Path Email View", events.get(position)
					.getIssued_to_email());
			TextView sharedLockNameView = (TextView) vi
					.findViewById(R.id.sharedLockName);
			sharedLockNameView
					.setText(events.get(position).getLock().getName());
			return vi;
		}
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		if (place != null)
			return place.getName();
		return "";
	}

	@Override
	public int getMenuId() {
		return R.menu.share_key_info;
	}

}
