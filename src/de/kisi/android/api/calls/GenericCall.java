package de.kisi.android.api.calls;

import org.json.JSONArray;
import org.json.JSONObject;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.util.Log;

import com.loopj.android.http.JsonHttpResponseHandler;

import de.kisi.android.KisiApplication;
import de.kisi.android.R;
import de.kisi.android.account.KisiAuthenticator;
import de.kisi.android.api.KisiAPI;
import de.kisi.android.api.LoginCallback;
import de.kisi.android.rest.KisiRestClient;

public abstract class GenericCall {

	protected JsonHttpResponseHandler handler;
	protected String path;
	protected JSONObject json = new JSONObject();
	protected HTTPMethod method;

	private boolean createJsonCalled = false;
	
	protected GenericCall(String path, HTTPMethod method) {
		this.path = path;
		this.method = method;
	}


	protected void createJson() {
		createJsonCalled = true;
	}

	public final void send() {
		json = new JSONObject();
		createJson();
		if(!createJsonCalled)
			throw new RuntimeException("Overwritten createJson() must call super.createJson()");
		final GenericCall call = this;
		JsonHttpResponseHandler realHandler = new JsonHttpResponseHandler() {

			@Override
			public void onSuccess(JSONObject response) {
				Log.d("GenericCall", "call success: " + response.toString());
				if (call.handler != null) {
					call.handler.onSuccess(response);
				}
			}
			
			@Override
			public void onSuccess(JSONArray response) {
				Log.d("GenericCall", "call success: " + response);
				if (call.handler != null) {
					call.handler.onSuccess(response);
				}
			}	


			public void onFailure(int statusCode, Throwable e, JSONObject response) {
				Log.d("GenericCall", "call failed: "+response);
				
				if (checkOptimisticSignIn(statusCode))
					if (call.handler != null)
						call.handler.onFailure(statusCode, e, response);

			}
			
			public void onFailure(int statusCode, Throwable e, JSONArray response) {
				Log.d("GenericCall", "call failed: "+response.toString());
				
				if (checkOptimisticSignIn(statusCode))
					if (call.handler != null)
						call.handler.onFailure(statusCode, e, response);
				
			}


			private void showLoginScreen() {
				KisiAPI.getInstance().showLoginScreen();
			}

			private boolean checkOptimisticSignIn(int statusCode) {
				if (statusCode != 401) 
					return true;// only check 401

				if (call instanceof LoginCall)
					return true;// do nothing


				AccountManager mAccountManager = AccountManager.get(KisiApplication.getInstance());
				Account availableAccounts[] = mAccountManager.getAccountsByType(KisiAuthenticator.ACCOUNT_TYPE);
				if(availableAccounts.length==0){
					showLoginScreen();
					return true;
				}

				Account acc = availableAccounts[0];
				for (Account a : availableAccounts)
					if (a.name.equals(KisiAPI.getInstance().getUser().getEmail()))
						acc = a;

				String password = mAccountManager.getPassword(acc);
				KisiAPI.getInstance().login(acc.name, password, new LoginCallback() {

					@Override
					public void onLoginSuccess(String authtoken) {
						call.send();
					}

					@Override
					public void onLoginFail(String errormessage) {
						if (!KisiApplication.getInstance().getResources().getString(R.string.no_network).equals(errormessage))
							showLoginScreen();
					}

				});
				return false;
			}


		};

		switch (this.method) {
		case GET:
			KisiRestClient.getInstance().get(path, realHandler);
			break;
		case POST:
			KisiRestClient.getInstance().post(path, json, realHandler);
			break;
		case DELETE:
			KisiRestClient.getInstance().delete(path,  realHandler);
			break;
		case POST_NO_AUTH:
			KisiRestClient.getInstance().postNoAuth(path, json, realHandler);
			break;
		default:
			throw new RuntimeException("Unsupported HttpMethod");
		}
	}
}
