package de.kisi.android.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import de.kisi.android.R;
import de.kisi.android.api.KisiAPI;
import de.kisi.android.model.Place;


public class PlaceNotificationSettingsFragment extends BaseFragment implements OnClickListener {

	private ListView mListView;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout =  inflater.inflate(R.layout.place_notification_settings, container, false);
        mListView = (ListView) layout.findViewById(R.id.place_notification_listview);
        mListView.setAdapter(new PlaceNotificationAdapter(getActivity()));
        

		return layout;
    }
	
	
	class PlaceNotificationAdapter extends BaseAdapter {
		
		private LayoutInflater inflater;
		private Context context;
		
		public PlaceNotificationAdapter(Context context) {
			super();
			this.context = context;
			inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		@Override
		public int getCount() {
			return KisiAPI.getInstance().getPlaces().length;
		}

		@Override
		public Place getItem(int position) {
			return KisiAPI.getInstance().getPlaceAt(position);
		}

		@Override
		public long getItemId(int position) {
			return KisiAPI.getInstance().getPlaceAt(position).getId();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View vi = convertView;
	        if (vi == null)
	            vi = inflater.inflate(R.layout.place_notification_item, null);
	        final Place p = this.getItem(position);
	        TextView placeName = (TextView) vi.findViewById(R.id.place_name_notification);
	        placeName.setText(p.getName());
	        TextView defaultSetting = (TextView) vi.findViewById(R.id.notification_default_setting);
	        defaultSetting.setText(context.getResources().getString(R.string.default_settings) + ": " +
	        		Boolean.toString(p.isSuggestUnlock()));
	        Switch placeSwitch = (Switch) vi.findViewById(R.id.place_switch_notification);
	        placeSwitch.setChecked(p.getNotificationEnabled());
	        placeSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					p.setNotificationEnabled(isChecked);
				} 
				
			});
	        return vi;
		}
		
		
		
	}
	
	
	
	
	//listener for the backbutton of the action bar
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
       switch (item.getItemId()) {
        case android.R.id.home:
            getActivity().onBackPressed();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
	}

	@Override
	public void onClick(View v) {
		getActivity().onBackPressed();
	}

	@Override
	public String getName(){
		return "Notification Settings";
	}

	@Override
	public int getMenuId() {
		return R.menu.notification_setting;
	}

}