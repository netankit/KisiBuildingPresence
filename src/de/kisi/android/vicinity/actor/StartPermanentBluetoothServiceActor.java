package de.kisi.android.vicinity.actor;

import android.content.pm.PackageManager;
import de.kisi.android.KisiApplication;
import de.kisi.android.api.KisiAPI;
import de.kisi.android.model.Locator;
import de.kisi.android.model.Place;
import de.kisi.android.notifications.NotificationManager;
import de.kisi.android.vicinity.LockInVicinityActorInterface;
import de.kisi.android.vicinity.manager.BluetoothLEManager;

/**
 * This Actor starts the BLE Service in foreground mode 
 */
public class StartPermanentBluetoothServiceActor implements LockInVicinityActorInterface{

	@Override
	public void actOnEntry(Locator locator) {
		// Start BLE and make sure it runs in foreground mode
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2 && 
				KisiApplication.getInstance().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {	
			Place place = KisiAPI.getInstance().getPlaceById(locator.getPlaceId());
			for(Locator l: place.getLocators()) {
				if(l.getType().equals("BLE")) {
					NotificationManager.getOrCreateBLEButtonNotification(KisiApplication.getInstance(), locator.getPlace(),"BLE");
					BluetoothLEManager.getInstance().startService(true);
					return;
				}
			}
		}
	}

	@Override
	public void actOnExit(Locator locator) {
		// Stop the BLE Service, because it needs a lot of power
		// It is very unlikely that an iBeacon is outside of a Geofence, so 
		// we do not need to run the service in Background mode
		BluetoothLEManager.getInstance().stopService();//.startService(false);
		NotificationManager.notifyBLEButtonNotificationDeleted("BLE");
	}

}
