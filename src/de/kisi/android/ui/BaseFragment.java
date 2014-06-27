package de.kisi.android.ui;

import de.kisi.android.model.Place;
import android.app.Fragment;
import android.util.Log;

public abstract class BaseFragment extends Fragment{

	protected Place place;
	protected static int ids = 0;
	protected int id;
	public BaseFragment(){
		id = ids++;
		Log.i("fragment","id "+id);
	}
	public void setPlace(Place place){
		this.place = place;
	}

	public Place setPlace(){
		return place;
	}
	
	public abstract String getName();
	public abstract int getMenuId();
}
