package de.kisi.android.api.calls;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.loopj.android.http.JsonHttpResponseHandler;

import de.kisi.android.api.DateDeserializer;
import de.kisi.android.api.SharedKeyInfoCallback;
import de.kisi.android.model.Key;
import de.kisi.android.model.Place;

/**
 * This API Call allows to get all data over HTTP from the server end point:
 * places/place_id/members_present. The data is in Json format and thus we use
 * GSON library to parse the data and then invoke the relevant callback.
 * 
 * @currentState: Dormant!
 * @author Ankit
 * 
 */

public class UpdatePresenceCall extends GenericCall {

	public UpdatePresenceCall(Place place, final SharedKeyInfoCallback callback) {
		super("places/" + place.getId() + "/members/present", HTTPMethod.GET);

		this.handler = new JsonHttpResponseHandler() {

			public void onSuccess(JSONArray jsonArray) {
				List<Key> events = new LinkedList<Key>();
				GsonBuilder gb = new GsonBuilder();
				gb.registerTypeAdapter(Date.class, new DateDeserializer());
				Gson gson = gb.create();
				// String msg = "## JSON Array Length: " + jsonArray.length();
				// Log.v(path, msg);
				System.out.println();
				for (int i = 0; i < jsonArray.length(); i++) {
					Key event = null;
					try {
						// TODO: Create a new Data Model for this class and
						// replace with Keys.class
						event = gson.fromJson(jsonArray.getJSONObject(i)
								.toString(), Key.class);
						// Log.v(path, jsonArray.getJSONObject(i).toString());
					} catch (JsonSyntaxException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					events.add(event);
				}
				callback.onSharedLogsInfoResult(events);
			}

			public void onFailure(java.lang.Throwable e,
					org.json.JSONArray errorResponse) {
				callback.onSharedLogsInfoResult(null);
			}
		};
	}
}
