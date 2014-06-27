package de.kisi.android.ui;

import de.kisi.android.model.Place;
import android.app.Fragment;
import android.util.Log;

public abstract class BaseFragment extends Fragment{

	protected Place place;

	protected void setPlace(Place place){
		this.place = place;
	}

	protected Place getPlace(){
		return place;
	}
	
	public abstract String getName();
	public abstract int getMenuId();
}
