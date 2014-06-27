package de.kisi.android.ui;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import de.kisi.android.R;
import de.kisi.android.api.KisiAPI;
import de.kisi.android.api.LogsCallback;
import de.kisi.android.model.Event;
import de.kisi.android.model.Place;

public class LogInfoFragment extends BaseFragment implements LogsCallback{

	
	private ListView mListView;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.loginfo, container, false);
        mListView =  (ListView)view.findViewById(R.id.place_notification_listview);
		KisiAPI.getInstance().getLogs(place, this);
		return view;
    }

	@Override
	public void onLogsResult(List<Event> events) {
		try{
			mListView.setAdapter(new LogAdapter(events));
		}catch(Exception e){
			
		}
	}
	
	
	class LogAdapter extends BaseAdapter {

		private List<Event> events;
		private LayoutInflater inflater;
		
		
		public LogAdapter(List<Event> events) {
			super();
			this.events = events;
			inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

		@SuppressWarnings("deprecation")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View vi = convertView;
	        if (vi == null)
	            vi = inflater.inflate(R.layout.loginfo_list_item, null);
	        TextView eventTextView = (TextView) vi.findViewById(R.id.logEventTextView);
	        eventTextView.setText(events.get(position).getMessage());
	        TextView dateTextView = (TextView) vi.findViewById(R.id.logDateTextVIew);
	        SimpleDateFormat df = new SimpleDateFormat();
	        dateTextView.setText(df.format(events.get(position).getCreatedAt()));
	        return vi;
		}
		
	}


	@Override
	public String getName() {
		if(place!=null)
			return place.getName();
		return "";
	}

	@Override
	public int getMenuId() {
		return R.menu.log_info;
	}
	
}
