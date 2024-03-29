package de.kisi.android.api;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONObject;

import android.content.Intent;
import android.widget.Toast;
import de.kisi.android.KisiApplication;
import de.kisi.android.R;
import de.kisi.android.account.KisiAccountManager;
import de.kisi.android.api.calls.CreateGatewayCall;
import de.kisi.android.api.calls.CreateNewKeyCall;
import de.kisi.android.api.calls.GenericCall;
import de.kisi.android.api.calls.GetLogs;
import de.kisi.android.api.calls.UpdateKeysCall;
import de.kisi.android.api.calls.LocatorBoundaryCrossingCall;
import de.kisi.android.api.calls.LocatorBoundaryCrossingCall.BoundaryCrossing;
import de.kisi.android.api.calls.LoginCall;
import de.kisi.android.api.calls.LogoutCall;
import de.kisi.android.api.calls.RegisterCall;
import de.kisi.android.api.calls.UnlockCall;
import de.kisi.android.api.calls.UpdateLocatorsCall;
import de.kisi.android.api.calls.UpdateLocksCall;
import de.kisi.android.api.calls.UpdatePlacesCall;
import de.kisi.android.api.calls.VersionCheckCall;
import de.kisi.android.db.DataManager;
import de.kisi.android.model.Locator;
import de.kisi.android.model.Lock;
import de.kisi.android.model.Place;
import de.kisi.android.model.User;
import de.kisi.android.notifications.NotificationManager;
import de.kisi.android.ui.KisiMainActivity;
import de.kisi.android.vicinity.LockInVicinityDisplayManager;
import de.kisi.android.vicinity.manager.BluetoothLEManager;

public class KisiAPI {

	private boolean loginInProgress = false;
	private final LinkedList<GenericCall> callQueue = new LinkedList<GenericCall>();

	// -------------------- Singleton Stuff: --------------------
	private static KisiAPI instance;

	public static KisiAPI getInstance() {
		if (instance == null)
			instance = new KisiAPI();
		return instance;
	}

	private KisiAPI() {
	}

	// -------------------- CALLS: --------------------
	public void createGateway(JSONObject blinkUpResponse) {
		this.sendCall(new CreateGatewayCall(blinkUpResponse));
	}

	public void getLatestVerion(final VersionCheckCallback callback) {
		this.sendCall(new VersionCheckCall(callback));
	}

	public void updateLocators(final Place place) {
		this.sendCall(new UpdateLocatorsCall(place));
	}

	public void updateLocks(final Place place,
			final OnPlaceChangedListener listener) {
		this.sendCall(new UpdateLocksCall(place, listener));
	}

	public boolean createNewKey(Place place, String email, List<Lock> locks) {
		this.sendCall(new CreateNewKeyCall(place, email, locks));
		return true;
	}

	private void sendCall(GenericCall call) {
		if (!loginInProgress || (call instanceof LoginCall)
				|| (call instanceof RegisterCall)
				|| (call instanceof LogoutCall)) {
			call.send();
		} else if (loginInProgress) {
			synchronized (callQueue) {
				callQueue.add(call);
			}
		}

	}

	private void processCallQueue() {
		synchronized (callQueue) {
			for (GenericCall call : callQueue) {
				call.send();
			}
			callQueue.clear();
		}
	}

	public synchronized void login(String email, String password,
			final LoginCallback callback) {
		loginInProgress = true;
		LoginCall loginCall = new LoginCall(email, password,
				new LoginCallback() {

					@Override
					public void onLoginSuccess(String authtoken) {

						loginInProgress = false;
						if (callback != null) {
							callback.onLoginSuccess(authtoken);
						}
						processCallQueue();
					}

					@Override
					public void onLoginFail(String errormessage) {
						loginInProgress = false;
						if (callback != null) {
							callback.onLoginFail(errormessage);
						}
						callQueue.clear();
					}
				});
		sendCall(loginCall);
	}

	public void logout() {
		new LogoutCall().send();
		if (KisiAPI.getInstance().getUser() != null)
			KisiAccountManager.getInstance().deleteAccountByName(
					KisiAPI.getInstance().getUser().getEmail());
		clearCache();
		BluetoothLEManager.getInstance().stopService();
		LockInVicinityDisplayManager.getInstance().update();
		NotificationManager.removeAllNotification();
	}

	public void showLoginScreen() {
		logout();
		Intent login = new Intent(KisiApplication.getInstance(),
				KisiMainActivity.class);
		login.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_CLEAR_TASK
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		KisiApplication.getInstance().startActivity(login);

		Toast.makeText(
				KisiApplication.getInstance(),
				KisiApplication.getInstance().getResources()
						.getString(R.string.automatic_relogin_failed),
				Toast.LENGTH_LONG).show();
	}

	public void updatePlaces(final OnPlaceChangedListener listener) {
		if (getUser() == null)
			return;

		new UpdatePlacesCall(listener).send();
	}

	/**
	 * Checks if the place is owned by the user or just shared
	 * 
	 * @param place
	 *            Place to be checked
	 * @return true if user is owner, false if someone else shares this place
	 *         with the user
	 */
	public boolean userIsOwner(Place place) {
		if (place == null || getUser() == null) {
			return false;
		}
		return place.getOwnerId() == this.getUser().getId();
	}

	/**
	 * Send a request to the server to unlock this lock. The callback will run
	 * on another Thread, so do no direct UI modifications in there
	 * 
	 * @param lock
	 *            The lock that should be unlocked
	 * @param callback
	 *            Callback object for feedback, or null if no feedback is
	 *            requested
	 */
	public void unlock(Lock lock, final UnlockCallback callback,
			String trigger, boolean automatic_unlock) {
		new UnlockCall(lock, callback, trigger, automatic_unlock).send();
	}

	public void crossBoundary(Locator locator, BoundaryCrossing action) {
		this.sendCall(new LocatorBoundaryCrossingCall(locator, action));
	}

	// Ankit
	// Dummy Data Added to the server at :
	// https://kisi.de/places/538/members/present
	public void sendDummyData(Locator locator, BoundaryCrossing action) {
		this.sendCall(new LocatorBoundaryCrossingCall(locator, action));
	}

	// Ankit --End

	public void getSharedKeysLogsInfo(Place place,
			final SharedKeyInfoCallback callback) {
		new UpdateKeysCall(place, callback).send();
	}

	public void getLogs(Place place, final LogsCallback callback) {
		new GetLogs(place, callback).send();
	}

	// Registration: Registers user by sending in a JSON object with user
	// information.
	public void register(String first_name, String last_name,
			String user_email, String password, Boolean terms_and_conditions,
			final RegisterCallback callback) {

		this.sendCall(new RegisterCall(first_name, last_name, user_email,
				password, terms_and_conditions, callback));
	}

	// -------------------- DATA: --------------------
	public User getUser() {
		return DataManager.getInstance().getUser();
	}

	public Place[] getPlaces() {
		// Ankit -- Made changes here! Delete them soon!
		// Locator locator;
		// BoundaryCrossing action;
		// sendDummyData(locator, action);
		return DataManager.getInstance().getAllPlaces().toArray(new Place[0]);
	}

	public Place getPlaceAt(int index) {
		Place[] places = DataManager.getInstance().getAllPlaces()
				.toArray(new Place[0]);
		if (places != null && index >= 0 && index < places.length)
			return places[index];
		return null;
	}

	public Place getPlaceById(int num) {
		Place[] places = DataManager.getInstance().getAllPlaces()
				.toArray(new Place[0]);
		for (Place p : places)
			if (p.getId() == num)
				return p;
		return null;
	}

	public void clearCache() {
		DataManager.getInstance().deleteDB();
	}

	public Lock getLockById(Place place, int lockId) {
		List<Lock> locks = place.getLocks();
		if (locks == null)
			return null;
		for (Lock lock : locks) {
			if (lock.getId() == lockId) {
				return lock;
			}
		}
		return null;
	}

	public Lock getLockById(int lockId) {
		Place[] places = KisiAPI.getInstance().getPlaces();
		if (places == null)
			return null;

		for (Place place : places) {
			for (Lock lock : place.getLocks()) {
				if (lock.getId() == lockId) {
					return lock;
				}
			}
		}
		return null;
	}

	// -------------------- OTHER: --------------------

	public void refresh(OnPlaceChangedListener listener) {
		DataManager.getInstance().deletePlaceLockLocatorFromDB();
		this.updatePlaces(listener);
	}

}
