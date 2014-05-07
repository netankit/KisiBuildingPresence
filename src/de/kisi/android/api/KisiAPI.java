package de.kisi.android.api;


import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.TextHttpResponseHandler;

import de.kisi.android.KisiApplication;
import de.kisi.android.KisiMain;
import de.kisi.android.R;
import de.kisi.android.account.KisiAccountManager;
import de.kisi.android.account.KisiAuthenticator;
import de.kisi.android.db.DataManager;
import de.kisi.android.model.Locator;
import de.kisi.android.model.Lock;
import de.kisi.android.model.Place;
import de.kisi.android.model.User;
import de.kisi.android.rest.KisiRestClient;
import de.kisi.android.vicinity.LockInVicinityDisplayManager;
import de.kisi.android.vicinity.manager.BluetoothLEManager;
import de.kisi.android.vicinity.manager.GeofenceManager;

import com.google.gson.Gson;


public class KisiAPI {

	private static KisiAPI instance;  
	
	
	private List<OnPlaceChangedListener> registeredOnPlaceChangedListener = new LinkedList<OnPlaceChangedListener>();
	private List<OnPlaceChangedListener> unregisteredOnPlaceChangedListener = new LinkedList<OnPlaceChangedListener>();
	private List<OnPlaceChangedListener> newregisteredOnPlaceChangedListener = new LinkedList<OnPlaceChangedListener>();

	private Context context;
	
	private boolean oldAuthToken = true;
	private Boolean reloginSuccess = false;
	
	public static KisiAPI getInstance(){
		if(instance == null)
			instance = new KisiAPI(KisiApplication.getApplicationInstance());
		return instance;
	}

	private KisiAPI(Context context){
		this.context = context;
	}
	
	
	public void login(String login, String password, final LoginCallback callback){
		Log.i("KisiAPI","login");
		synchronized(reloginSuccess){
		
			if(reloginSuccess){
				callback.onLoginSuccess(KisiAPI.getInstance().getUser().getAuthentication_token());
				return;
			}
			
		JSONObject login_data = new JSONObject();
		JSONObject login_user = new JSONObject();
		try {
			login_data.put("email", login);
			login_data.put("password", password);
			login_user.put("user", login_data);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}

		KisiRestClient.getInstance().postWithoutAuthToken("users/sign_in", login_user,  new JsonHttpResponseHandler() {
			
			 public void onSuccess(org.json.JSONObject response) {
				 Log.i("KisiAPI","login success");
				oldAuthToken = false;
				Gson gson = new Gson();
				User user = gson.fromJson(response.toString(), User.class);
				DataManager.getInstance().saveUser(user);
				callback.onLoginSuccess(KisiAPI.getInstance().getUser().getAuthentication_token());
				return;
			}
			
			 public void onFailure(int statusCode, Throwable e, JSONObject response) {
					Log.i("KisiAPI","login fail");
				 String errormessage = null;
				 //no network connectivity
				 if(statusCode == 0) {
					 errormessage = context.getResources().getString(R.string.no_network);
					 callback.onLoginFail(errormessage);
					 return;
				 }
	
				 if(response != null) {
					try {
						errormessage = response.getString("error");
					} catch (JSONException ej) {
						ej.printStackTrace();
					}
				}
				else {
					errormessage = "Error!";
				}
				callback.onLoginFail(errormessage);
				return;
			};
			
		});
		
		}
		

	}
	
	public void clearCache() {		
		DataManager.getInstance().deleteDB();
	}
	
	
	public void logout(){
		if(KisiAPI.getInstance().getUser()!=null && KisiAPI.getInstance().getUser().getEmail()!=null){
			KisiRestClient.getInstance().delete("/users/sign_out",  new TextHttpResponseHandler() {
				public void onSuccess(String msg) {
				}
			});
			KisiAccountManager.getInstance().deleteAccountByName(KisiAPI.getInstance().getUser().getEmail());
			clearCache();
			BluetoothLEManager.getInstance().stopService();
			LockInVicinityDisplayManager.getInstance().update();
		}
	}
	
	/**
	 * Get all available Places for the User
	 * 
	 * @return Array of all Places the user has access to
	 */
	public Place[] getPlaces(){
		return DataManager.getInstance().getAllPlaces().toArray(new Place[0]);
	}
	
	public Place getPlaceAt(int index){
		Place[] places = DataManager.getInstance().getAllPlaces().toArray(new Place[0]);
		if(places != null && index>=0 && index<places.length)
			return places[index];
		return null;
	}
	
	public Place getPlaceById(int num){
		Place[]  places = DataManager.getInstance().getAllPlaces().toArray(new Place[0]);
		for(Place p : places)
			if(p.getId() == num)
				return p;
		return null;
	}
	
	
	public User getUser() {
		return 	DataManager.getInstance().getUser();
	}

	/**
	 * 
	 * @param listener
	 */
	public void updatePlaces(final OnPlaceChangedListener listener) {
		if(getUser() == null)
			return;

		
		KisiRestClient.getInstance().get("places",  new JsonHttpResponseHandler() { 
			
			public void onSuccess(JSONArray response) {
				Gson gson = new Gson();
				Place[]  pl = gson.fromJson(response.toString(), Place[].class);
				DataManager.getInstance().savePlaces(pl);
				//update locks for places
				for(Place p: pl) {
					KisiAPI.getInstance().updateLocks(p, listener);
				}
			}
			
		});
	}
	
	
	public void updateLocks(final Place place, final OnPlaceChangedListener listener) {	
		KisiRestClient.getInstance().get("places/" + String.valueOf(place.getId()) + "/locks",  new JsonHttpResponseHandler() { 
			
			public void onSuccess(JSONArray response) {
				Gson gson = new Gson();
				Lock[] locks = gson.fromJson(response.toString(), Lock[].class);
				for(Lock l: locks) {
					l.setPlace(instance.getPlaceById(l.getPlaceId()));
				}
				DataManager.getInstance().saveLocks(locks);
				listener.onPlaceChanged(getPlaces());
				notifyAllOnPlaceChangedListener();
				//get also locators for this place
				KisiAPI.getInstance().updateLocators(place);
			}
		});		
	}
	
	//helper method for updateLocators
	public Lock getLockById(Place place, int lockId){ 
		List<Lock> locks  = place.getLocks();
		if(locks == null)
			return null;
		for(Lock l: locks) {
			if(l.getId() == lockId) {
				return l;
			}
		}
		return null;
	}
	
	
	public Lock getLockById(int lockId) {
		Place[] places = this.getPlaces();
		if(places == null)
			return null;
		
		for(Place p: places){
			for(Lock l: p.getLocks()) {
				if(l.getId() == lockId){
					return l;
				}
			}
		}
		return null;
	}
	
	
	public void updateLocators(final Place place) {
		KisiRestClient.getInstance().get("places/" + String.valueOf(place.getId()) + "/locators", new JsonHttpResponseHandler() {
			
			public void onSuccess(JSONArray response) {
				Gson gson = new Gson();
				Locator[] locators = gson.fromJson(response.toString(), Locator[].class);
				try{//Prevent App from crashing when closing during a refresh
					for(Locator l: locators) {
						l.setLock(KisiAPI.getInstance().getLockById(KisiAPI.getInstance().getPlaceById(l.getPlaceId()), l.getLockId()));
						l.setPlace(KisiAPI.getInstance().getPlaceById(l.getPlaceId()));
					}
					DataManager.getInstance().saveLocators(locators);
					notifyAllOnPlaceChangedListener();
				}catch(NullPointerException e){}
			}
			
		});
	}
	

	public boolean createNewKey(Place p, String email, List<Lock> locks, final Activity activity) {

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
		String url = "places/" + String.valueOf(p.getId()) + "/keys";
		
		KisiRestClient.getInstance().post(url, data, new JsonHttpResponseHandler() {
			
			public void onSuccess(JSONObject data) {
				try {
					Toast.makeText(
							activity,
							String.format(context.getResources().getString(R.string.share_success),
								data.getString("issued_to_email")),
							Toast.LENGTH_LONG).show();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			
		});	
		return true;
	}
	/**
	 * Register for interest in any changes in the Places.
	 * The listener is fired when a Place was added or deleted
	 * or any change occur within the Place like a Lock or Locator has been added. 
	 * 
	 * Sometimes on a total refresh there can be a lot of PlaceChanges in a short
	 * period of time, so make sure that the client can handle this.
	 * @param listener
	 */
	public void registerOnPlaceChangedListener(OnPlaceChangedListener listener){
		if(listener != null)
			newregisteredOnPlaceChangedListener.add(listener);
	}
	/**
	 * Unregister for the listener registered in registerOnPlaceChangedListener()
	 * @param listener
	 */
	public void unregisterOnPlaceChangedListener(OnPlaceChangedListener listener){
		if(listener != null)
			unregisteredOnPlaceChangedListener.add(listener);
	}
	
	/**
	 * Notifies all the registered OnPlaceChangedListener that some Data regarding
	 * the Places has changed.
	 */
	private void notifyAllOnPlaceChangedListener(){
		// This have to be done this way, lists are not allowed to be modified 
		// during a foreach loop
		for(OnPlaceChangedListener listener : unregisteredOnPlaceChangedListener)
			registeredOnPlaceChangedListener.remove(listener);
		registeredOnPlaceChangedListener.addAll(newregisteredOnPlaceChangedListener);
		newregisteredOnPlaceChangedListener.clear();
		for(OnPlaceChangedListener listener : registeredOnPlaceChangedListener)
			listener.onPlaceChanged(getPlaces());
	}
	
	/**
	 * Checks if the place is owned by the user or just shared
	 * 
	 * @param place Place to be checked
	 * @return true if user is owner, false if someone else shares this place with the user
	 */
	public boolean userIsOwner(Place place){
		return place.getOwnerId()==this.getUser().getId();
	}
	
	/**
	 * Send a request to the server to unlock this lock. The callback will
	 * run on another Thread, so do no direct UI modifications in there
	 * 
	 * @param lock The lock that should be unlocked
	 * @param callback Callback object for feedback, or null if no feedback is requested
	 */
	public void unlock(final Lock lock, final UnlockCallback callback){
		Log.i("KisiAPI","unlock");
        JSONObject location =  generateJSONLocation();
		JSONObject data = new JSONObject();
		try {
			data.put("location", location);
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		String url = String.format("places/%d/locks/%d/access", lock.getPlaceId(), lock.getId());
		
		KisiRestClient.getInstance().post(url, data,  new JsonHttpResponseHandler() {
			
			public void onSuccess(JSONObject response) {
				String message = null;
				if(response.has("notice")) {
					try {
						message = response.getString("notice");
					} catch (JSONException e) {
						e.printStackTrace();
					}	
				}
				Log.i("KisiAPI","unlock success");
				if(callback != null)
					callback.onUnlockSuccess(message);
			}
			
			public void onFailure(int statusCode, Throwable e, JSONObject errorResponse) {
				Log.i("KisiAPI","unlock fail");
				//statusCode  == 0: no network connectivity
				String errormessage = null;
				if(statusCode == 0) {
					 errormessage = context.getResources().getString(R.string.no_network);
					 if(callback != null)
						 callback.onUnlockFail(errormessage);
					 return;
				 }
				Log.i("KisiAPI","statusCode: "+statusCode);
				Log.i("KisiAPI","old_auth_token: "+oldAuthToken);
				if(statusCode!=401){
					Log.i("KisiAPI","unlock fail show error");

					if(errorResponse != null) {
						if(errorResponse.has("alert")) {
							try {
								errormessage = errorResponse.getString("alert");
							} catch (JSONException je) {
								e.printStackTrace();
							}
						}else if(errorResponse.has("error")) {
							try {
								errormessage = errorResponse.getString("error");
							} catch (JSONException je) {
								e.printStackTrace();
							}
						}
					} else {
						errormessage = "Unknown Error!";
					}
					if(callback != null)
						callback.onUnlockFail(errormessage);
				}else{
					if(oldAuthToken==false)
						showLoginScreen();
					else{ // retry
						Log.i("KisiAPI","unlock start retry");

						AccountManager mAccountManager = AccountManager.get(KisiApplication.getApplicationInstance());
						Account availableAccounts[] = mAccountManager.getAccountsByType(KisiAuthenticator.ACCOUNT_TYPE);
						Account acc = availableAccounts[0];
						for(Account a :availableAccounts)
								if(a.name.equals(getUser().getEmail()))
										acc = a;
						String password = mAccountManager.getPassword(acc);
						KisiAPI.getInstance().login(acc.name, password, new LoginCallback(){

							@Override
							public void onLoginSuccess(String authtoken) {
								Log.i("KisiAPI","unlock again");
								unlock(lock,callback);
							}

							@Override
							public void onLoginFail(String errormessage) {
								if(!KisiApplication.getApplicationInstance().getResources().getString(R.string.no_network).equals(errormessage))
									showLoginScreen();
							}
							
						});
					}
				}
			}	
		});
	}
	
	private void showLoginScreen(){
		KisiAPI.getInstance().logout();
		Intent login = new Intent(KisiApplication.getApplicationInstance(), KisiMain.class);
		login.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | 
                Intent.FLAG_ACTIVITY_CLEAR_TASK |
                Intent.FLAG_ACTIVITY_NEW_TASK);				
		KisiApplication.getApplicationInstance().startActivity(login);

		Toast.makeText(KisiApplication.getApplicationInstance(), KisiApplication.getApplicationInstance().getResources().getString(R.string.automatic_relogin_failed), Toast.LENGTH_LONG).show();
	}
	
	
	public void refresh(OnPlaceChangedListener listener) {
		DataManager.getInstance().deletePlaceLockLocatorFromDB();
		this.updatePlaces(listener);
	}
	
	

	public void createGateway(JSONObject blinkUpResponse) {	
		String agentUrl = null;
		String impeeId = null;
		String planId = null;
		try {
			agentUrl = blinkUpResponse.getString("agent_url");
			impeeId = blinkUpResponse.getString("impee_id");
			planId = blinkUpResponse.getString("plan_id");
		} catch (JSONException e2) {
			e2.printStackTrace();
		} 
		//impeeId contains white spaces in the end, remove them
        if (impeeId != null) 
            impeeId = impeeId.trim();

        JSONObject location = generateJSONLocation();
    	JSONObject gateway = new JSONObject();
		try {
			gateway.put("name", "Gateway");
			gateway.put("uri", agentUrl);
			gateway.put("plattform_name", "Android");
			gateway.put("plattform_version", Build.VERSION.RELEASE);
			gateway.put("blinked_up", true);
			gateway.put("ei_impee_id", impeeId);
			gateway.put("device_model", Build.MANUFACTURER + " " + Build.MODEL);
			gateway.put("location", location);
		} catch (JSONException e) {
			e.printStackTrace();
		}
    	
		
		JSONObject data = new JSONObject();
		
		try {
			data.put("gateway", gateway);
			data.put("ei_plan_id", planId);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	
		//TODO: Implement a proper handler
		KisiRestClient.getInstance().post("gateways", data, new JsonHttpResponseHandler() {});	
		
	}

	private JSONObject generateJSONLocation() {
		JSONObject location = new JSONObject();
		Location currentLocation = GeofenceManager.getInstance().getLocation();
		try {
    		if(currentLocation != null) {
    			location.put("latitude", currentLocation.getLatitude());
    			location.put("longitude", currentLocation.getLongitude());
    			location.put("horizontalAccuracy", currentLocation.getAccuracy());
    			location.put("age", (System.currentTimeMillis() - currentLocation.getTime())/1000.0);
    		} else { 
	 			location.put("error:", "Location data not accessible");
    		}
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
        return location;
	}
	
	
}
