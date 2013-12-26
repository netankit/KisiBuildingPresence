/**
 * Radius Networks, Inc.
 * http://www.radiusnetworks.com
 * 
 * @author David G. Young
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.radiusnetworks.ibeacon;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.radiusnetworks.ibeacon.client.RangingTracker;
import com.radiusnetworks.ibeacon.service.IBeaconData;
import com.radiusnetworks.ibeacon.service.IBeaconService;
import com.radiusnetworks.ibeacon.service.RangingData;
import com.radiusnetworks.ibeacon.service.RegionData;
import com.radiusnetworks.ibeacon.service.StartRMData;

import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

/**
 * An class used to set up interaction with iBeacons from an <code>Activity</code> or <code>Service</code>.
 * This class is used in conjunction with <code>IBeaconConsumer</code> interface, which provides a callback 
 * when the <code>IBeaconService</code> is ready to use.  Until this callback is made, ranging and monitoring 
 * of iBeacons is not possible.
 * 
 * In the example below, an Activity implements the <code>IBeaconConsumer</code> interface, binds
 * to the service, then when it gets the callback saying the service is ready, it starts ranging.
 * 
 *  <pre><code>
 *  public class RangingActivity extends Activity implements IBeaconConsumer {
 *  	protected static final String TAG = "RangingActivity";
 *  	private IBeaconManager iBeaconManager = IBeaconManager.getInstanceForApplication(this);
 *  	 {@literal @}Override
 *  	protected void onCreate(Bundle savedInstanceState) {
 *  		super.onCreate(savedInstanceState);
 *  		setContentView(R.layout.activity_ranging);
 *  		iBeaconManager.bind(this);
 *  	}
 *  	 {@literal @}Override 
 *  	protected void onDestroy() {
 *  		super.onDestroy();
 *  		iBeaconManager.unBind(this);
 *  	}
 *  	 {@literal @}Override
 *  	public void onIBeaconServiceConnect() {
 *  		iBeaconManager.setRangeNotifier(new RangeNotifier() {
 *        	 {@literal @}Override 
 *        	public void didRangeBeaconsInRegion(Collection<IBeacon> iBeacons, Region region) {
 *     			if (iBeacons.size() > 0) {
 *	      			Log.i(TAG, "The first iBeacon I see is about "+iBeacons.iterator().next().getAccuracy()+" meters away.");		
 *     			}
 *        	}
 *  		});
 *  		
 *  		try {
 *  			iBeaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
 *  		} catch (RemoteException e) {	}
 *  	}
 *  }
 *  </code></pre>
 * 
 * @author David G. Young
 *
 */
public class IBeaconManager {
	private static final String TAG = "IBeaconManager";
	private Context context;
	private static IBeaconManager client = null;
	private Map<IBeaconConsumer,ConsumerInfo> consumers = new HashMap<IBeaconConsumer,ConsumerInfo>();
	private Messenger serviceMessenger = null;
	protected RangeNotifier rangeNotifier = null;
    protected MonitorNotifier monitorNotifier = null;
    protected RangingTracker rangingTracker = new RangingTracker();

    /**
     * The default duration in milliseconds of the bluetooth scan cycle
     */
    public static final long DEFAULT_FOREGROUND_SCAN_PERIOD = 1100;
    /**
     * The default duration in milliseconds spent not scanning between each bluetooth scan cycle
     */
    public static final long DEFAULT_FOREGROUND_BETWEEN_SCAN_PERIOD = 0;
    /**
     * The default duration in milliseconds of the bluetooth scan cycle when no ranging/monitoring clients are in the foreground
     */
    public static final long DEFAULT_BACKGROUND_SCAN_PERIOD = 10000;
    /**
     * The default duration in milliseconds spent not scanning between each bluetooth scan cycle when no ranging/monitoring clients are in the foreground
     */
    public static final long DEFAULT_BACKGROUND_BETWEEN_SCAN_PERIOD = 5*60*1000;

    private long foregroundScanPeriod = DEFAULT_FOREGROUND_SCAN_PERIOD;
    private long foregroundBetweenScanPeriod = DEFAULT_FOREGROUND_BETWEEN_SCAN_PERIOD;
    private long backgroundScanPeriod = DEFAULT_BACKGROUND_SCAN_PERIOD;
    private long backgroundBetweenScanPeriod = DEFAULT_BACKGROUND_BETWEEN_SCAN_PERIOD;

    /**
     * Sets the duration in milliseconds of each Bluetooth LE scan cycle to look for iBeacons
     * @param p
     */
    public void setForegroundScanPeriod(long p) {
        foregroundBetweenScanPeriod = p;
    }
    /**
     * Sets the duration in milliseconds to wait between each bluetooth scan cycle used to look for iBeacons
     * @param p
     */
    public void setForegroundBetweenScanPeriod(long p) {
        foregroundBetweenScanPeriod = p;
    }
    /**
     * Sets the duration in milliseconds of each Bluetooth LE scan cycle to look for iBeacons when no ranging/monitoring clients are in the foreground
     * @param p
     */
    public void setBackgroundScanPeriod(long p) {
        backgroundScanPeriod = p;
    }
    /**
     * Sets the duration in milliseconds spent not scanning between each Bluetooth LE scan cycle when no ranging/monitoring clients are in the foreground
     * @param p
     */
    public void setBackgroundBetweenScanPeriod(long p) {
        backgroundBetweenScanPeriod = p;
    }

	/**
	 * An accessor for the singleton instance of this class.  A context must be provided, but if you need to use it from a non-Activity
	 * or non-Service class, you can attach it to another singleton or a subclass of the Android Application class.
	 */
	public static IBeaconManager getInstanceForApplication(Context context) {
		if (!isInstantiated()) {
			Log.d(TAG, "IBeaconManager instance craetion");
			client = new IBeaconManager(context);
		}
		return client;
	}
	
	private IBeaconManager(Context context) {
		this.context = context;
	}
	/**
	 * Check if Bluetooth LE is supported by this Android device, and if so, make sure it is enabled.
	 * Throws a BleNotAvailableException if Bluetooth LE is not supported.  (Note: The Android emulator will do this)
	 * @return false if it is supported and not enabled
	 */
	public boolean checkAvailability() {
		if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			throw new BleNotAvailableException("Bluetooth LE not supported by this device"); 
		}		
		else {
			if (((BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter().isEnabled()){
				return true;
			}
		}	
		return false;
	}
	/**
	 * Binds an Android <code>Activity</code> or <code>Service</code> to the <code>IBeaconService</code>.  The 
	 * <code>Activity</code> or <code>Service</code> must implement the <code>IBeaconConsuemr</code> interface so
	 * that it can get a callback when the service is ready to use.
	 * 
	 * @param consumer the <code>Activity</code> or <code>Service</code> that will receive the callback when the service is ready.
	 */
	public void bind(IBeaconConsumer consumer) {
		if (consumers.keySet().contains(consumer)) {
			Log.i(TAG, "This consumer is already bound");					
		}
		else {
			Log.i(TAG, "This consumer is not bound.  binding: "+consumer);	
			consumers.put(consumer, new ConsumerInfo());
			Intent intent = new Intent(consumer.getApplicationContext(), IBeaconService.class);
			consumer.bindService(intent, iBeaconServiceConnection, Context.BIND_AUTO_CREATE);
			Log.i(TAG, "consumer count is now:"+consumers.size());
            setBackgroundMode(consumer, false); // if we just bound, we assume we are not in the background.
		}
	}
	
	/**
	 * Unbinds an Android <code>Activity</code> or <code>Service</code> to the <code>IBeaconService</code>.  This should
	 * typically be called in the onDestroy() method.
	 * 
	 * @param consumer the <code>Activity</code> or <code>Service</code> that no longer needs to use the service.
	 */
	public void unBind(IBeaconConsumer consumer) {
		if (consumers.keySet().contains(consumer)) {
			Log.i(TAG, "Unbinding");			
			consumer.unbindService(iBeaconServiceConnection);
			consumers.remove(consumer);
		}
		else {
			Log.i(TAG, "This consumer is not bound to: "+consumer);
			Log.i(TAG, "Bound consumers: ");
			for (int i = 0; i < consumers.size(); i++) {
				Log.i(TAG, " "+consumers.get(i));
			}
		}
	}

    /**
     * Tells you if the passed iBeacon consumer is bound to the service
     * @param consumer
     * @return
     */
    public boolean isBound(IBeaconConsumer consumer) {
        return consumers.keySet().contains(consumer);
    }

    /**
     * This method notifies the iBeacon service that the IBeaconConsumer is either moving to background mode or foreground mode
     * When in background mode, BluetoothLE scans to look for iBeacons are executed less frequently in order to save battery life
     * The specific scan rates for background and foreground operation are set by the defaults below, but may be customized.
     * Note that when multiple IBeaconConsumers exist, all must be in background mode for the the background scan periods to be used
     * When ranging in the background, the time between updates will be much less fequent than in the foreground.  Updates will come
     * every time interval equal to the sum total of the BackgroundScanPeriod and the BackgroundBetweenScanPeriod
     * All IBeaconConsumers are by default treated as being in foreground mode unless this method is explicitly called indicating
     * otherwise.
     *
     * @see #DEFAULT_FOREGROUND_SCAN_PERIOD
     * @see #DEFAULT_FOREGROUND_BETWEEN_SCAN_PERIOD;
     * @see #DEFAULT_BACKGROUND_SCAN_PERIOD;
     * @see #DEFAULT_BACKGROUND_BETWEEN_SCAN_PERIOD;
     * @see #setForegroundScanPeriod(long p)
     * @see #setForegroundBetweenScanPeriod(long p)
     * @see #setBackgroundScanPeriod(long p)
     * @see #setBackgroundBetweenScanPeriod(long p)
     * @param consumer
     * @param backgroundMode true indicates the iBeaconConsumer is in the background
     * returns true if background mode is successfully set
     */
    public boolean setBackgroundMode(IBeaconConsumer consumer, boolean backgroundMode) {
        try {
            ConsumerInfo consumerInfo = consumers.get(consumer);
            consumerInfo.isInBackground = backgroundMode;
            consumers.put(consumer,consumerInfo);
            setScanPeriods();
            return true;
        }
        catch (RemoteException e) {
            Log.e(TAG, "Failed to set background mode", e);
            return false;
        }
    }

	/**
	 * Specifies a class that should be called each time the <code>IBeaconService</code> gets ranging
	 * data, which is nominally once per second when iBeacons are detected.
	 *  
	 * @see RangeNotifier 
	 * @param notifier
	 */
	public void setRangeNotifier(RangeNotifier notifier) {
		rangeNotifier = notifier;
	}

	/**
	 * Specifies a class that should be called each time the <code>IBeaconService</code> gets sees
	 * or stops seeing a Region of iBeacons.
	 *  
	 * @see MonitorNotifier 
	 * @see #startMonitoringBeaconsInRegion(Region region)
	 * @see Region 
	 * @param notifier
	 */
	public void setMonitorNotifier(MonitorNotifier notifier) {
		monitorNotifier = notifier;
	}
	
	/**
	 * Tells the <code>IBeaconService</code> to start looking for iBeacons that match the passed
	 * <code>Region</code> object, and providing updates on the estimated distance very seconds while
	 * iBeacons in the Region are visible.  Note that the Region's unique identifier must be retained to
	 * later call the stopRangingBeaconsInRegion method.
	 *  
	 * @see IBeaconManager#setRangeNotifier(RangeNotifier)
	 * @see IBeaconManager#stopRangingBeaconsInRegion(Region region)
	 * @see RangeNotifier 
	 * @see Region 
	 * @param region
	 */
	public void startRangingBeaconsInRegion(Region region) throws RemoteException {
        if (serviceMessenger == null) {
            throw new RemoteException("The IBeaconManager is not bound to the service.  Call iBeaconManager.bind(IBeaconConsumer consumer) and wait for a callback to onIBeaconServiceConnect()");
        }
		Message msg = Message.obtain(null, IBeaconService.MSG_START_RANGING, 0, 0);
		StartRMData obj = new StartRMData(new RegionData(region), callbackPackageName(), this.getScanPeriod(), this.getBetweenScanPeriod() );
		msg.obj = obj;
		serviceMessenger.send(msg);
	}
	/**
	 * Tells the <code>IBeaconService</code> to stop looking for iBeacons that match the passed
	 * <code>Region</code> object and providing distance information for them.
	 *  
	 * @see #setMonitorNotifier(MonitorNotifier notifier)
	 * @see #startMonitoringBeaconsInRegion(Region region)
	 * @see MonitorNotifier 
	 * @see Region 
	 * @param region
	 */
	public void stopRangingBeaconsInRegion(Region region) throws RemoteException {
        if (serviceMessenger == null) {
            throw new RemoteException("The IBeaconManager is not bound to the service.  Call iBeaconManager.bind(IBeaconConsumer consumer) and wait for a callback to onIBeaconServiceConnect()");
        }
		Message msg = Message.obtain(null, IBeaconService.MSG_STOP_RANGING, 0, 0);
		StartRMData obj = new StartRMData(new RegionData(region), callbackPackageName(),this.getScanPeriod(), this.getBetweenScanPeriod() );
		msg.obj = obj;
		serviceMessenger.send(msg);
	}
	/**
	 * Tells the <code>IBeaconService</code> to start looking for iBeacons that match the passed
	 * <code>Region</code> object.  Note that the Region's unique identifier must be retained to
	 * later call the stopMonitoringBeaconsInRegion method.
	 *  
	 * @see IBeaconManager#setMonitorNotifier(MonitorNotifier)
	 * @see IBeaconManager#stopMonitoringBeaconsInRegion(Region region)
	 * @see MonitorNotifier 
	 * @see Region 
	 * @param region
	 */
	public void startMonitoringBeaconsInRegion(Region region) throws RemoteException {
        if (serviceMessenger == null) {
            throw new RemoteException("The IBeaconManager is not bound to the service.  Call iBeaconManager.bind(IBeaconConsumer consumer) and wait for a callback to onIBeaconServiceConnect()");
        }
		Message msg = Message.obtain(null, IBeaconService.MSG_START_MONITORING, 0, 0);
		StartRMData obj = new StartRMData(new RegionData(region), callbackPackageName(),this.getScanPeriod(), this.getBetweenScanPeriod()  );
		msg.obj = obj;
		serviceMessenger.send(msg);
	}
	/**
	 * Tells the <code>IBeaconService</code> to stop looking for iBeacons that match the passed
	 * <code>Region</code> object.  Note that the Region's unique identifier is used to match it to
	 * and existing monitored Region.
	 *  
	 * @see IBeaconManager#setMonitorNotifier(MonitorNotifier)
	 * @see IBeaconManager#startMonitoringBeaconsInRegion(Region region)
	 * @see MonitorNotifier 
	 * @see Region 
	 * @param region
	 */
	public void stopMonitoringBeaconsInRegion(Region region) throws RemoteException {
        if (serviceMessenger == null) {
            throw new RemoteException("The IBeaconManager is not bound to the service.  Call iBeaconManager.bind(IBeaconConsumer consumer) and wait for a callback to onIBeaconServiceConnect()");
        }
		Message msg = Message.obtain(null, IBeaconService.MSG_STOP_MONITORING, 0, 0);
		StartRMData obj = new StartRMData(new RegionData(region), callbackPackageName(),this.getScanPeriod(), this.getBetweenScanPeriod() );
		msg.obj = obj;
		serviceMessenger.send(msg);
	}

    public void setScanPeriods() throws RemoteException {
        if (serviceMessenger == null) {
            throw new RemoteException("The IBeaconManager is not bound to the service.  Call iBeaconManager.bind(IBeaconConsumer consumer) and wait for a callback to onIBeaconServiceConnect()");
        }
        Message msg = Message.obtain(null, IBeaconService.MSG_SET_SCAN_PERIODS, 0, 0);
        StartRMData obj = new StartRMData(this.getScanPeriod(), this.getBetweenScanPeriod());
        msg.obj = obj;
        serviceMessenger.send(msg);
    }
	
	private String callbackPackageName() {
		String packageName = context.getPackageName();
		Log.d(TAG, "callback packageName: "+packageName);
		return packageName;
	}

	private ServiceConnection iBeaconServiceConnection = new ServiceConnection() {
		// Called when the connection with the service is established
	    public void onServiceConnected(ComponentName className, IBinder service) {
	    	Log.d(TAG,  "we have a connection to the service now");
	        serviceMessenger = new Messenger(service);
	        Iterator<IBeaconConsumer> consumerIterator = consumers.keySet().iterator();
	        while (consumerIterator.hasNext()) {
	        	IBeaconConsumer consumer = consumerIterator.next();
	        	Boolean alreadyConnected = consumers.get(consumer).isConnected;
	        	if (!alreadyConnected) {		        	
		        	consumer.onIBeaconServiceConnect();
                    ConsumerInfo consumerInfo = consumers.get(consumer);
                    consumerInfo.isConnected = true;
		        	consumers.put(consumer,consumerInfo);
	        	}
	        }
	    }

	    // Called when the connection with the service disconnects unexpectedly
	    public void onServiceDisconnected(ComponentName className) {
	        Log.e(TAG, "onServiceDisconnected");
	    }
	};	

    /**
     * @see #monitorNotifier
     * @return monitorNotifier
     */
	public MonitorNotifier getMonitoringNotifier() {
		return this.monitorNotifier;		
	}	
	/**
	 * @see #rangeNotifier
	 * @return rangeNotifier
	 */
	public RangeNotifier getRangingNotifier() {
		return this.rangeNotifier;		
	}	
    /**
     * Determines if the singleton has been constructed already.  Useful for not overriding settings set declaratively in XML
     * @return true, if the class has been constructed
     */
	public static boolean isInstantiated() {
		return (client != null);
	}

    private class ConsumerInfo {
        public boolean isConnected = false;
        public boolean isInBackground = false;
    }

    private boolean isInBackground() {
        boolean background = true;
        for (IBeaconConsumer consumer : consumers.keySet()) {
            if (!consumers.get(consumer).isInBackground) {
                background = false;
            }
        }
        return background;
    }

    private long getScanPeriod() {
        if (isInBackground()) {
            return backgroundScanPeriod;
        }
        else {
            return foregroundScanPeriod;
        }
    }
    private long getBetweenScanPeriod() {
        if (isInBackground()) {
            return backgroundBetweenScanPeriod;
        }
        else {
            return foregroundBetweenScanPeriod;
        }
    }

}
