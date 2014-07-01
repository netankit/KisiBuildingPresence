package de.kisi.android.api.calls;

import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.loopj.android.http.JsonHttpResponseHandler;

import de.kisi.android.KisiApplication;
import de.kisi.android.R;
import de.kisi.android.api.RegisterCallback;

public class RegisterCall extends GenericCall {

	private String user_email; 
	private String first_name;
	private String last_name;
	private String password;
	private Boolean terms_and_conditions;
	
	public RegisterCall(
			String first_name, 
			String last_name, 
			String user_email, 
			String password, 
			Boolean terms_and_conditions,  
			final RegisterCallback callback) {
		
		super("users", HTTPMethod.POST_NO_AUTH);
		
		this.first_name = first_name;
		this.last_name = last_name;
		this.user_email = user_email;
		this.password = password;
		this.terms_and_conditions = terms_and_conditions;
		
		this.handler = new JsonHttpResponseHandler() {

			public void onSuccess(JSONObject response) {
				callback.onRegisterSuccess();
			}

			public void onFailure(int statusCode, Throwable e,
					JSONObject response) {
				String errormessage = KisiApplication.getInstance().getResources()
						.getString(R.string.unknown_error);
				// no network connectivity
				if (statusCode == 0) {
					errormessage = KisiApplication.getInstance().getResources().getString(
							R.string.no_network);
					callback.onRegisterFail(errormessage);
					return;
				}

				if (response != null) {
					try{
						JSONObject errors = response.getJSONObject("errors");
						Iterator<?> it = errors.keys();
						
						while(it.hasNext()){
							String key = (String) it.next();
							Gson gson = new Gson();
							String[] errorMessages = gson.fromJson(errors.getJSONArray(key).toString(), 
									String[].class);
							for(String msg : errorMessages) {
								errormessage = errormessage + key + " " + msg + '\n';
							}							
						};
					}catch (JSONException ej) {
						ej.printStackTrace();
						try {
							errormessage = response.getString("error");
						} catch (JSONException ej1) {
							ej1.printStackTrace();
						}
					}
				}
				callback.onRegisterFail(errormessage);
			};
		};
		createJson();
	}

	@Override
	protected void createJson() {
		super.createJson();
		try {
			//build user object
			JSONObject userJSON = new JSONObject();
			userJSON.put("first_name", first_name);
			userJSON.put("last_name", last_name);
			userJSON.put("email", user_email);
			userJSON.put("password", password);
			userJSON.put("terms_and_conditions", terms_and_conditions == true?"1":"0");
			this.json.put("user", userJSON);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
	}
}
