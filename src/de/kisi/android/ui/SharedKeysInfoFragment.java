package de.kisi.android.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import de.kisi.android.R;
import de.kisi.android.api.KisiAPI;
import de.kisi.android.api.SharedKeyInfoCallback;
import de.kisi.android.model.Key;

/**
 * This fragment extends BaseFragment and implements the SharedKeyInfoCallback,
 * to generate a list of all the shared keys to various user by the user. This
 * fragment is invoked within the KisiMainActivity.
 * 
 * @author Ankit
 */
public class SharedKeysInfoFragment extends BaseFragment implements
		SharedKeyInfoCallback {

	private ListView mListView;

	@Override
	/** 
	 * Creates the default android view
	 */
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

	/**
	 * Invokes the setAdapter with the parameter of a new object of
	 * SharedKeysInfoLogAdapter with keyList as its parameter
	 */
	public void onSharedLogsInfoResult(List<Key> keyList) {
		try {
			mListView.setAdapter(new SharedKeysInfoLogAdapter(keyList));
		} catch (Exception e) {

		}
	}

	class UniqueKey {
		String issued_to_email;
		List<String> locks;
	}

	class SharedKeysInfoLogAdapter extends BaseAdapter {

		private LayoutInflater inflater;
		private List<UniqueKey> unique_keys;

		public SharedKeysInfoLogAdapter(List<Key> events) {
			super();
			HashMap<String, List<String>> hashMap = new HashMap<String, List<String>>();
			for (Key key : events) {
				List<String> lst = hashMap.get(key.getIssued_to_email());
				if (lst == null) {
					lst = new ArrayList<String>();
				}
				lst.add(key.getLock().getName());
				hashMap.put(key.getIssued_to_email(), lst);
			}
			Set<String> keySet = hashMap.keySet();
			unique_keys = new ArrayList<UniqueKey>();
			for (String key_temp : keySet) {
				UniqueKey unkey = new UniqueKey();
				unkey.issued_to_email = key_temp;
				unkey.locks = hashMap.get(key_temp);
				unique_keys.add(unkey);
			}

			inflater = (LayoutInflater) getActivity().getSystemService(
					Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return unique_keys.size();
		}

		@Override
		public Object getItem(int position) {
			return unique_keys.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View vi = convertView;
			if (vi == null)
				vi = inflater.inflate(R.layout.sharedkeyslog_list_item, null);

			TextView userEmailView = (TextView) vi.findViewById(R.id.userEmail);
			userEmailView.setText(unique_keys.get(position).issued_to_email);
			TextView sharedLockNameView = (TextView) vi
					.findViewById(R.id.sharedLockName);
			String new_temp = null;
			for (String item : unique_keys.get(position).locks) {
				if (new_temp != null) {
					new_temp = item + ", " + new_temp;
				} else {
					new_temp = item;
				}

			}
			new_temp = new_temp.trim();
			sharedLockNameView.setText(new_temp);

			ImageView imageView = (ImageView) vi
					.findViewById(R.id.onlineUserGreenDot);
			imageView.invalidate();

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
