package de.kisi.android.api.calls;

import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;

import com.loopj.android.http.JsonHttpResponseHandler;

import de.kisi.android.model.Locator;

public class LocatorBoundaryCrossingCall extends LocatableCall {

	public enum BoundaryCrossing {
		EXIT,ENTER;
		
		@Override
		public String toString(){
			return this.name().toLowerCase(Locale.ENGLISH);
		}
	}
	
	public LocatorBoundaryCrossingCall(final Locator locator, BoundaryCrossing action) {
		super(String.format(Locale.ENGLISH, "places/%d/locators/%d/%s", 
				locator.getPlaceId(), 
				locator.getId(),
				action), 
			HTTPMethod.POST);
		
		handler = new JsonHttpResponseHandler() {

			@Override
			public void onSuccess(JSONObject response) {
				int i = 0;
				i++;
				Log.d("LocatorBoundaryCrossingCall", "onSuccess"+locator.getId());
			}
			
			@Override
			public void onSuccess(JSONArray response) {
				int i = 0;
				i++;
				Log.d("LocatorBoundaryCrossingCall", "onSuccess"+locator.getId());
			}	

			@Override
			public void onFailure(int statusCode, Throwable e, JSONObject response) {
				int i = 0;
				i++;
				Log.d("LocatorBoundaryCrossingCall", "onSuccess"+locator.getId());

			}
			
			@Override
			public void onFailure(int statusCode, Throwable e, JSONArray response) {
				int i = 0;
				i++;
				Log.d("LocatorBoundaryCrossingCall", "onSuccess"+locator.getId());
				
			}
		};
		
	}
	
}
