package de.kisi.android.vicinity.manager;

import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;

import android.app.IntentService;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.radiusnetworks.ibeacon.IBeacon;
import com.radiusnetworks.ibeacon.IBeaconConsumer;
import com.radiusnetworks.ibeacon.IBeaconManager;
import com.radiusnetworks.ibeacon.MonitorNotifier;
import com.radiusnetworks.ibeacon.RangeNotifier;
import com.radiusnetworks.ibeacon.Region;

import de.kisi.android.api.KisiAPI;
import de.kisi.android.api.OnPlaceChangedEventHandler;
import de.kisi.android.api.OnPlaceChangedListener;
import de.kisi.android.api.calls.LocatorBoundaryCrossingCall.BoundaryCrossing;
import de.kisi.android.model.Locator;
import de.kisi.android.model.Place;
import de.kisi.android.notifications.NotificationInformation;
import de.kisi.android.notifications.NotificationManager;
import de.kisi.android.vicinity.LockInVicinityActorFactory;
import de.kisi.android.vicinity.LockInVicinityActorInterface;

public class BluetoothLEService extends IntentService implements IBeaconConsumer{

	public BluetoothLEService(){
		super("BluetoothLEService");
	}

	/**
	 * This class handles multiple onPlaceChanged requests, to detect
	 * wheter there was a data change needed by this framework or not  
	 */
	private class RegionContainer{
		Hashtable<Integer,Region> hashtable = new Hashtable<Integer,Region>();
		Hashtable<Region,Integer> keys = new Hashtable<Region,Integer>();
		
		/**
		 * Returns a list of all currently registered Regions 
		 */
		public Collection<Region> values(){
			LinkedList<Region> regions = new LinkedList<Region>();
			for(Region r : hashtable.values())
				regions.add(r);
			return regions;
		}
		/**
		 * Fast check whether a key is already registered or not
		 */
		public boolean containsKey(Integer key){
			return hashtable.containsKey(key);
		}
		/**
		 * A new Region has been registered 
		 */
		public void put(Integer key, Region value){
			hashtable.put(key, value);
			keys.put(value, key);
		}
		/**
		 * Returns the corresponding Region to the key 
		 */
		public Region get(Integer key){
			return hashtable.get(key);
		}
		/**
		 * Region has been deleted
		 */
		public void remove(Region region){
			Integer key = keys.get(region);
			keys.remove(region);
			hashtable.remove(key);
		}
	}
	private final IBinder mBinder = new LocalBinder();
	private RegionContainer regions = new RegionContainer();
	public class LocalBinder extends Binder {
		public BluetoothLEService getService(){
			return BluetoothLEService.this;
		}
    }		

	private IBeaconManager iBeaconManager;
	
	@Override
	public IBinder onBind(Intent arg0) {
		//iBeaconManager = IBeaconManager.getInstanceForApplication(getApplicationContext());
		//iBeaconManager.bind(this);
		return mBinder;
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
	}
	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// check if foreground mode is requested
		if(intent == null) {
			return -1;
		}
		if(intent.getExtras()!=null && intent.getExtras().getBoolean("foreground", false)){
			// foreground mode needs a permanent notification to run on level 1
			// if the notification is not valid, e.g. no notification shows up
			// the service will run on background mode
			//
			// i also noticed that the notification should be created with the service context 
			// and not the the application context, thats why the service is a parameter of the 
			// method
			NotificationInformation notification = NotificationManager.getOrCreateBLEServiceNotification(this,"BLE");
			if(notification != null){
				startForeground(notification.notificationId, notification.notification);
			}
		}else{
			try{
				stopForeground(true);
				NotificationManager.notifyBLEServiceNotificationDeleted("BLE");
			}catch(Exception e){
				// There might be an exception when we are already in background mode
				// but i didn't find a mechansim to check
			}
		}
		
		// bind the iBeacon Service to the BLE Service
		iBeaconManager = IBeaconManager.getInstanceForApplication(getApplicationContext());
		iBeaconManager.bind(this);
	    return START_STICKY;
	}
	
	@Override
	public void onDestroy (){
		try{
			stopForeground(true);
		}catch(Exception e){
		}
		iBeaconManager = IBeaconManager.getInstanceForApplication(getApplicationContext());
		iBeaconManager.unBind(this);
		NotificationManager.notifyBLEServiceNotificationDeleted("BLE");
	}

	// protect the actor for permanent fireing
	// those are only used in the ranging
	private static HashSet<Integer> unlockLocatorId = new HashSet<Integer>();
			
	/**
	 * Connection succeeded, so we can start to set our notifier
	 * @see com.radiusnetworks.ibeacon.IBeaconConsumer#onIBeaconServiceConnect()
	 */
	@Override
	public void onIBeaconServiceConnect() {
		Log.i("BLE","connect");
		
		// the range notifier is currently not used because of a
		// bug in the iBeacon framework, but the RangeNotifier works :)
		iBeaconManager.setRangeNotifier(new RangeNotifier(){

			@Override
			public void didRangeBeaconsInRegion(Collection<IBeacon> iBeacons,
					Region region) {
				// get best result of all beacons 
				// this should always only be one beacon
				Integer maxRssi = null;
				for(IBeacon beacon:iBeacons){
					if(maxRssi == null)
						maxRssi = beacon.getRssi();
					else
						maxRssi = Math.max(beacon.getRssi(), maxRssi);
				}
				
				if(maxRssi == null) // there is no beacon
					return;
				
				// get information about the region
				String beaconId[] = region.getUniqueId().split(" ");
				int placeId = Integer.parseInt(beaconId[1]);
				int lockId = Integer.parseInt(beaconId[3]);
				int locatorId = Integer.parseInt(beaconId[5]);
				Locator locator;
				
				try{
					locator = KisiAPI.getInstance().getPlaceById(placeId).getLockById(lockId).getLocatorById(locatorId);
					LockInVicinityActorInterface actor = LockInVicinityActorFactory.getActor(locator);
					if(unlockLocatorId.contains(locatorId) && locator.getSuggestUnlockTreshold()<maxRssi){
						unlockLocatorId.remove(locatorId);
						actor.actOnExit(locator);
						Place placeToCross = KisiAPI.getInstance().getPlaceById(placeId);
		        		Locator defaultLocator = placeToCross.getDefaultLocator();
		            	if (defaultLocator.isNotifyOnExit()) {
		        			KisiAPI.getInstance().crossBoundary(defaultLocator, BoundaryCrossing.EXIT);
		        		}
					}
					if(!unlockLocatorId.contains(locatorId) && locator.getSuggestUnlockTreshold()>maxRssi){
						unlockLocatorId.add(locatorId);
						actor.actOnEntry(locator);
						Place placeToCross = KisiAPI.getInstance().getPlaceById(placeId);
	            		KisiAPI.getInstance().updateLocators(placeToCross);
	            		Locator defaultLocator = placeToCross.getDefaultLocator();
	            		if (defaultLocator.isNotifyOnEntry()) {
	            			KisiAPI.getInstance().crossBoundary(defaultLocator, BoundaryCrossing.ENTER);
	            		}
					}
				}catch(NullPointerException e){
					// Do nothing if there is no such locator
					// this is also a protection of old locators that are deleted on the server, but
					// not removed on the iBeacon list
				}

				/*
				for(IBeacon b:iBeacons){
					Log.i("BLE","Rssi "+b.getRssi());
					Log.i("BLE","Acc "+b.getAccuracy());
				}
				*/
			}});
		
		
		iBeaconManager.setMonitorNotifier(new MonitorNotifier(){

			@Override
			public void didEnterRegion(Region region) {
				actForRegion(region,true);
			}
			
			@Override
			public void didExitRegion(Region region) {
				actForRegion(region,false);
			}
			
			private void actForRegion(Region region,boolean entered){ 
				LockInVicinityActorInterface actor;
				
				// Get Required Data from the Region
				String beaconId[] = region.getUniqueId().split(" ");
				int placeId = Integer.parseInt(beaconId[1]);
				int lockId = Integer.parseInt(beaconId[3]);
				int locatorId = Integer.parseInt(beaconId[5]);
				
				try{
					Locator locator = KisiAPI.getInstance().getPlaceById(placeId).getLockById(lockId).getLocatorById(locatorId);

					actor = LockInVicinityActorFactory.getActor(locator);
					
					if(entered)
						actor.actOnEntry(locator);
					else
						actor.actOnExit(locator);
					
					// refresh time for shut down 
					BluetoothLEManager.getInstance().resetShutDownTime();
					
				}catch(NullPointerException e){
					// Do nothing if there is no such locator
					// this is also a protection of old locators that are deleted on the server, but
					// not removed on the iBeacon list
				}

				
			}


			@Override
			public void didDetermineStateForRegion(int state,
					Region region) {
				// TODO Auto-generated method stub
				
			}
			
		});
		OnPlaceChangedEventHandler.getInstance().registerOnPlaceChangedListener(new OnPlaceChangedListener(){

			@Override
			public void onPlaceChanged(Place[] newPlaces) {
				registerPlaces(newPlaces);
				
			}
		});
		registerPlaces(KisiAPI.getInstance().getPlaces());
	}
	
	private void registerPlaces(Place[] places){
        try {
        	// Get Registered Regions
        	Collection<Region> curRegions = regions.values();
        	for(Place p :places){
        		for(Locator locator:p.getLocators()){
        			if(locator.getType()!=null && locator.getType().equals("BLE")){
        				if(regions.containsKey(locator.getId())){
        					// Remove Region from Delete list
        					curRegions.remove(regions.get(locator.getId()));
        				}else{
        					// Create Region object
        					int major = locator.getMajor();
        					int minor = locator.getMinor();
        					Region region = null;
        					// use default KISI Uuid if the Uuid of the locator is null
        					if(locator.getUuid() == null  || locator.getUuid() == "") {
        						region = new Region("Place: "+locator.getPlaceId()+" Lock: "+locator.getLockId()+" Locator: "+locator.getId(), "DE9D14A1-1C16-4114-9B68-3B2435C6B99A", major, minor);
        					}
        					else {
        						region = new Region("Place: "+locator.getPlaceId()+" Lock: "+locator.getLockId()+" Locator: "+locator.getId(), locator.getUuid(), major, minor);
        					}
        					// Remove those 2 lines when activate ranging again
        					// For now only use monitoring
        					if(locator.isSuggestUnlockEnabled() || locator.isAutoUnlockEnabled())
        						iBeaconManager.startMonitoringBeaconsInRegion(region);
        					
        					// Ranging does not work reliable, sometimes it throws a nullPointerException
        					// For now we stay at monitoring only
        					/* 
        					if((locator.isSuggestUnlockEnabled() && locator.getSuggestUnlockTreshold() == 0) ||
        							(locator.isAutoUnlockEnabled() && locator.getAutoUnlockTreshold() == 0)	)
        						iBeaconManager.startMonitoringBeaconsInRegion(region);
        					if((locator.isSuggestUnlockEnabled() && locator.getSuggestUnlockTreshold() != 0) ||
        							(locator.isAutoUnlockEnabled() && locator.getAutoUnlockTreshold() != 0)	)
        						iBeaconManager.startRangingBeaconsInRegion(region);
        					 */
        					regions.put(locator.getId(), region);
        				}
        			}
        		}
        	}
        	
        	// Delete Regions that not in the list anymore
        	for(Region region : curRegions){
        		iBeaconManager.stopMonitoringBeaconsInRegion(region);
				//iBeaconManager.stopRangingBeaconsInRegion(region);
        		regions.remove(region);
        	}
        } catch (RemoteException e) {   }			
	}

}
