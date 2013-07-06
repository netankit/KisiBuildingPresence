package com.example.kisibox;

import java.util.List;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;

import com.example.kisibox.model.Location;
import com.manavo.rest.RestCallback;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.Window;
import android.widget.TextView;


public class KisiMain extends FragmentActivity {

	public SparseArray<Location> locations;
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);

		setContentView(R.layout.kisi_main);

		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.window_title);

		TextView title = (TextView) findViewById(R.id.title);

		title.setText("Your Locations");

		initializePager();

	}

	public void initializePager() {
		updateLocations();
	}

	private void updateLocations() {
		KisiApi api = new KisiApi(this);

		api.setCallback(new RestCallback() {
			public void success(Object obj) {
				JSONArray data = (JSONArray)obj;

				setupView(data);
			}

		});
		api.get("locations");
		
	}
	
	private void setupView(JSONArray locations_json) {
		
		List<Fragment> fragments = new Vector<Fragment>(); 
		locations = new SparseArray<Location>();
		
		try {
			for (int i=0; i<locations_json.length(); i++) {
				Location location = new Location(locations_json.getJSONObject(i));
				locations.put(location.getId(), location);
				fragments.add(LocationViewFragment.newInstance(location.getId()));
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		FragmentManager fm = getSupportFragmentManager();
		ViewPager pager = (ViewPager) findViewById(R.id.pager);
		MyFragmentPagerAdapter pagerAdapter = new MyFragmentPagerAdapter(fm, fragments);
		pager.setAdapter(pagerAdapter);
	}
}
