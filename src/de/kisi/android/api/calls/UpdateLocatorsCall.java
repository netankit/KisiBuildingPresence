package de.kisi.android.api.calls;

import org.json.JSONArray;

import android.util.Log;

import com.google.gson.Gson;
import com.loopj.android.http.JsonHttpResponseHandler;

import de.kisi.android.api.KisiAPI;
import de.kisi.android.api.OnPlaceChangedEventHandler;
import de.kisi.android.api.calls.LocatorBoundaryCrossingCall.BoundaryCrossing;
import de.kisi.android.db.DataManager;
import de.kisi.android.model.Locator;
import de.kisi.android.model.Place;

public class UpdateLocatorsCall extends GenericCall {

	public UpdateLocatorsCall(final Place place) {
		super("places/" + place.getId() + "/locators", HTTPMethod.GET);
		
		handler = new JsonHttpResponseHandler() {
			
			public void onSuccess(JSONArray response) {
				String responseString = response.toString();
				if (place.getId() == 607) {
					Log.d("UpdateLocatorsCall", "onSuccess"+place.getId());
					responseString = "[{\"id\":178,\"place_id\":607,\"lock_id\":null,\"name\":\"Shared Locator\",\"enabled\":true,\"suggest_unlock_enabled\":true,\"suggest_unlock_treshold\":null,\"auto_unlock_treshold\":null,\"latitude\":48.1443,\"longitude\":11.5609,\"created_at\":\"2014-07-15T14:47:19.000Z\",\"updated_at\":\"2014-07-15T14:48:09.000Z\",\"auto_unlock_enabled\":false,\"major\":null,\"minor\":null,\"uuid\":null,\"tag\":null,\"suggest\":null,\"execute\":null,\"suggest_radius\":null,\"execute_radius\":null,\"notify_on_entry\":null,\"notify_on_exit\":null,\"type\":\"GPS\"}]";
				}
				responseString = responseString.replaceAll("\"notify_on_entry\":null", "\"notify_on_entry\":true");
				responseString = responseString.replaceAll("\"notify_on_exit\":null", "\"notify_on_exit\":true");
				Gson gson = new Gson();
				Locator[] locators = gson.fromJson(responseString, Locator[].class);
				try {// Prevent App from crashing when closing during a
						// refresh
					for (Locator l : locators) {
						l.setLock(KisiAPI.getInstance().getLockById(KisiAPI.getInstance().getPlaceById(l.getPlaceId()), l.getLockId()));
						l.setPlace(KisiAPI.getInstance().getPlaceById(l.getPlaceId()));
						KisiAPI.getInstance().crossBoundary(l, BoundaryCrossing.ENTER);
					}
					DataManager.getInstance().saveLocators(locators);
					OnPlaceChangedEventHandler.getInstance().notifyAllOnPlaceChangedListener();
				} catch (NullPointerException e) {
				}
			}
		};
	}
}
