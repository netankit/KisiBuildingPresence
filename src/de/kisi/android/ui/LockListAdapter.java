package de.kisi.android.ui;

import java.util.HashSet;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import de.kisi.android.R;
import de.kisi.android.model.Lock;
import de.kisi.android.model.Place;


public class LockListAdapter extends BaseAdapter {

	private Context mContext;
	private Place place;
	private String trigger;
	private HashSet<Integer> suggestedNFC;

	public LockListAdapter(Context context, Place place) {
		this.mContext = context;
		this.place = place;
		suggestedNFC = new HashSet<Integer>();
	}
	
	@Override
	public int getCount() {
		if(place != null) {
			List<Lock> locks = place.getLocks();	
			if(locks != null) {
				return locks.size();
			}
		}
		return 0;
	}
	public String getTrigger(){
		String result = trigger;
		trigger = null;
		return result;
	}
	public void setTrigger(String t){
		trigger = t;
	}
	public boolean isSuggestedNFC(int lockId){
		return suggestedNFC.contains(lockId);
	}
	public void clearSuggestedNFC(){
		suggestedNFC.clear();
	}
	public void addSuggestedNFC(int lockId){
		suggestedNFC.add(lockId);
	}
	
	@Override
	public Object getItem(int position) {
		if(place != null) {
			List<Lock> locks = place.getLocks();	
			if(locks != null) {
				return locks.get(position);
			}
		}
		return null;
	}

	@Override
	public long getItemId(int position) {;
		if(place != null) {
			List<Lock> locks = place.getLocks();	
			if(locks != null && locks.size()>position) {
				return locks.get(position).getId();
			}
		}
		return 0;
	}
	
	
	public int getItemPosition(long id) {
		if(place != null) {
			List<Lock> locks = place.getLocks();	
			for(int position = 0; position < locks.size(); position++) {
				if(locks.get(position).getId() == (int) id) {
					return position;
				}
					
			}
		}
		return -1;
		
	}
	

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Lock lock = place.getLocks().get(position);
		Button button;
		if(convertView == null) {
				LayoutInflater li = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				button = (Button) li.inflate(R.layout.lock_button, parent, false);
		}
		else {
			button = (Button) convertView;
		}
		button.setText(lock.getName());
		
		if(suggestedNFC.contains(lock.getId()))
			button.setBackgroundColor(Color.BLACK);
		else
			button.setBackgroundColor(mContext.getResources().getColor(R.color.kisi_color));

		//disable the clickability of the buttons so that the OnItemClickListner of the ListView handels the clicks 
		button.setFocusable(false);
		button.setClickable(false);
		
		
		return button;
	}
	
	
	public Place getPlace() {
		return place;
	}

}
