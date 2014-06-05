package de.kisi.android.api.calls;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.JsonHttpResponseHandler;

import android.app.Activity;
import android.widget.Toast;
import de.kisi.android.KisiApplication;
import de.kisi.android.R;
import de.kisi.android.model.Lock;
import de.kisi.android.model.Place;

public class CreateNewKeyCall extends GenericCall {

	
	private String email;
	private List<Lock> locks;

	public CreateNewKeyCall(Place place, String email, List<Lock> locks, final Activity activity) {
		super("places/" + place.getId() + "/keys", HTTPMethod.POST);
		
		this.email = email;
		this.locks = locks;
		this.handler = new JsonHttpResponseHandler() {
			
			public void onSuccess(JSONObject data) {
				try {
					Toast.makeText(
							activity,
							String.format(KisiApplication.getInstance().getResources().getString(R.string.share_success),
								data.getString("issued_to_email")),
							Toast.LENGTH_LONG).show();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			
		};
		
		
	}
	
	@Override
	protected void createJson() {
		JSONArray lock_ids = new JSONArray();
		for (Lock l : locks) {
			lock_ids.put(l.getId());
		}
		JSONObject key = new JSONObject();
		JSONObject data = new JSONObject();
		//changed in the API from assignee_email to issued_to_email
		try {
			key.put("lock_ids", lock_ids);
			key.put("issued_to_email", email);
			data.put("key", key);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		this.json=data;
	}

}
