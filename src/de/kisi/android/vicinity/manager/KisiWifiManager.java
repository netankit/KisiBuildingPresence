package de.kisi.android.vicinity.manager; 

import de.kisi.android.KisiApplication;
import android.content.Context;
import android.net.wifi.WifiManager;


//this class checks if the device is a nexus 4 and turn off the wifi while the scan for the BLE beacons is running
//this is needed because the use of bluetooth is interfering with the wifi and is sometimes preventing that data is transmitted
//this error only happens as far as yet know on the Nexus 4 & 7 see Bug #497
public class KisiWifiManager {

	private static KisiWifiManager  instance;
	
	private boolean previousWifiState;
	private WifiManager mWifiManager;
	
	public static KisiWifiManager getInstance() {
		if(instance == null) {
			instance = new KisiWifiManager();
		}
		return instance;
	}

	private KisiWifiManager() {
		mWifiManager = (WifiManager) KisiApplication.getInstance().getSystemService(Context.WIFI_SERVICE); 
	}
	
	public void turnOffWifi() {
		
		previousWifiState = mWifiManager.isWifiEnabled();
		if(previousWifiState == false) {
			return;
		} else {
			//check if the devices is a Nexus 4 
			if(!android.os.Build.MODEL.equals("Nexus 4")) {
				return;
			} else {
				mWifiManager.setWifiEnabled(false);
			}
		}
	}
	
	public void turnOnWifi() {
		//check if the devices is a Nexus 4 
		if(!android.os.Build.MODEL.equals("Nexus 4")) {
			return;
		}
		
		if(previousWifiState == false) {
			return;
		} else {
			mWifiManager.setWifiEnabled(true);
		}

	}
	
}
