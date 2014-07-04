package de.kisi.android.api.calls;

import org.json.JSONArray;

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
				responseString = responseString.replaceAll("\"notify_on_entry\":null", "\"notify_on_entry\":true");
				responseString = responseString.replaceAll("\"notify_on_exit\":null", "\"notify_on_exit\":true");
				Gson gson = new Gson();
				Locator[] locators = gson.fromJson(responseString, Locator[].class);
				try {// Prevent App from crashing when closing during a
						// refresh
					for (Locator l : locators) {
						l.setLock(KisiAPI.getInstance().getLockById(KisiAPI.getInstance().getPlaceById(l.getPlaceId()), l.getLockId()));
						l.setPlace(KisiAPI.getInstance().getPlaceById(l.getPlaceId()));
					}
					DataManager.getInstance().saveLocators(locators);
					OnPlaceChangedEventHandler.getInstance().notifyAllOnPlaceChangedListener();
				} catch (NullPointerException e) {
				}
			}
		};
	}
}
