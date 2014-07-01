package de.kisi.android.api.calls;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.util.Log;

import com.google.gson.Gson;
import com.loopj.android.http.JsonHttpResponseHandler;

import de.kisi.android.KisiApplication;
import de.kisi.android.R;
import de.kisi.android.account.KisiAccountManager;
import de.kisi.android.api.KisiAPI;
import de.kisi.android.api.LoginCallback;
import de.kisi.android.db.DataManager;
import de.kisi.android.model.User;

public class LoginCall extends GenericCall {

	private String email;
	private String password;
	
	public LoginCall(String email, String password, final LoginCallback callback) {
		super("users/sign_in", HTTPMethod.POST_NO_AUTH);

		this.email = email;
		this.password = password;
		this.handler = new JsonHttpResponseHandler() {

			public void onSuccess(org.json.JSONObject response) {
				Log.d("LoginCall", "Login success");
				Gson gson = new Gson();
				User user = gson.fromJson(response.toString(), User.class);
				DataManager.getInstance().saveUser(user);
				callback.onLoginSuccess(KisiAPI.getInstance().getUser().getAuthentication_token());
				return;
			}

			public void onFailure(int statusCode, Throwable e, JSONObject response) {
				Log.d("LoginCall", method.toString());
				Log.d("LoginCall", json.toString());
				Log.d("LoginCall", "Login failure");
				String errormessage = null;
				// no network connectivity
				if (statusCode == 0) {
					errormessage = KisiApplication.getInstance().getResources().getString(R.string.no_network);
					callback.onLoginFail(errormessage);
					return;
				}

				if (response != null) {
					try {
						errormessage = response.getString("error");
					} catch (JSONException ej) {
						ej.printStackTrace();
					}
				} else {
					errormessage = "Error!";
				}
				callback.onLoginFail(errormessage);
				return;
			}
		};
		createJson();
	}
	
	@Override
	protected void createJson() {
		super.createJson();
		try {
			JSONObject userJSON = new JSONObject();
			//build user object
			userJSON.put("email", this.email);
			userJSON.put("password", this.password);
			
			//build device object
			JSONObject deviceJSON = new JSONObject();
			String deviceUUID = KisiAccountManager.getInstance().getDeviceUUID(this.email);
			if (deviceUUID != null) {
				deviceJSON.put("uuid", deviceUUID);				
			}
			deviceJSON.put("platform_name", "Android");
			deviceJSON.put("platform_version", Build.VERSION.RELEASE);
			deviceJSON.put("model", Build.MANUFACTURER + " " + Build.MODEL);
			try {
				deviceJSON.put("app_version", KisiApplication.getInstance().getVersion());
			} catch (NameNotFoundException e) {
				//no app version for you then...
			}
			
			this.json.put("user", userJSON);
			this.json.put("device", deviceJSON);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
