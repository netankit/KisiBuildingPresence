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
import de.kisi.android.api.LogsCallback;
import de.kisi.android.model.Event;
import de.kisi.android.model.Place;

public class GetLogs extends GenericCall {

	public GetLogs(Place place,  final LogsCallback callback) {
		super("places/" + place.getId() + "/events", HTTPMethod.GET);
		
		this.handler = new JsonHttpResponseHandler() {
			
			public void onSuccess(JSONArray jsonArray) {
				List<Event> events = new LinkedList<Event>();
				GsonBuilder gb = new GsonBuilder();
				gb.registerTypeAdapter(Date.class, new DateDeserializer());
				Gson gson = gb.create();
				for(int i = 0; i < jsonArray.length(); i++) {
					Event event = null;
					try {
						event = gson.fromJson(jsonArray.getJSONObject(i).toString(), Event.class);
					} catch (JsonSyntaxException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					events.add(event);
				}
				callback.onLogsResult(events);
			}

			public void onFailure(java.lang.Throwable e, org.json.JSONArray errorResponse) {
				callback.onLogsResult(null);
			}
		};
	}
}
