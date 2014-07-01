package de.kisi.android.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;

import de.kisi.android.KisiApplication;
import de.kisi.android.api.KisiAPI;
import de.kisi.android.vicinity.LockInVicinityDisplayManager;




//TODO: tk: libraries like Lombok simplify with automating getter & setter creation
//@DatabaseTable
public class Place {
	@DatabaseField(id = true, index=true)
	private int id;
	@DatabaseField
	private String name;
	@ForeignCollectionField(eager=false)
    private ForeignCollection<Lock> locks;	
	private boolean locksLoaded;
	@DatabaseField
	private double latitude;
	@DatabaseField
	private double longitude;
	@SerializedName("street_name")
	private String streetName;
	@SerializedName("street_number")
	private String streetNumber;
	private String zip;
	private String city;
	private String state;
	private String country;
	private String additionalInformation; 
	@DatabaseField
	@SerializedName("user_id")
	private int ownerId;
	@DatabaseField
	@SerializedName("suggest_unlock")
	private boolean suggestUnlock;

	@ForeignCollectionField(eager=false)
    private ForeignCollection<Locator> locators;	
	
//	@SerializedName("updated_at")
//	private Date updatedAt;

	@DatabaseField
	@SerializedName("locator_id")
	private int defaultLocatorID;
	
	public Place () {}
	
	
	public int getId() {
		return id;
	}
	public String getName() {
		return name;
	}

	public boolean areLocksLoaded(){
		return locksLoaded;
	}
	
	//TODO: clean this up
	public List<Lock> getLocks() {
		Lock[] lockArray = locks.toArray(new Lock[0]);
		List<Lock> result = new ArrayList<Lock>();
		for(Lock l: lockArray) {
			result.add(l);
		}
		return result;
	}
	
	public Lock getLockById(int lockId) {
		for(Lock l:locks)
			if(l.getId()==lockId)
				return l;
		return null;
	}

	public List<Locator> getLocators() {
		KisiAPI.getInstance().updateLocators(this);
		List<Locator> result = Arrays.asList(locators.toArray(new Locator[0]));
		if (result.size() == 0) { 
			Locator[] locatorArray = locators.toArray(new Locator[0]);
			result = new ArrayList<Locator>();
			for(Locator l: locatorArray) {
				result.add(l);
			}
		}
		return result;
	}
	
	public Locator getDefaultLocator() {
		List<Locator> locatorsList = getLocators();
		for (Locator locator : locatorsList) {
			if (locator.getId() == defaultLocatorID) {
				return locator;
			}
		}
		return null;
	}

	public double getLatitude() {
//		return getDefaultLocator().getLatitude();
		return latitude;
	}

	public double getLongitude() {
//		return getDefaultLocator().getLongitude();
		return longitude;
	}

	
	public String getAddress() {
		return getStreetName() + ", " + getCity();
	}
	
	public String getFullAddress() {
		return getStreet() + "\n" + 
			getZip() + " " + getCity() + "\n" +
			getCountry();
	}
	
	public String getStreet() {
		return streetName + " " + streetNumber;
	}
	
	public String getStreetName() {
		return streetName;
	}

	public String getStreetNumber() {
		return streetNumber;
	}

	public String getZip() {
		return zip;
	}

	public String getCity() {
		return city;
	}

	public String getCountry() {
		return country;
	}

	public String getState() {
		return state;
	}

	public String getAdditionalInformation() {
		return additionalInformation;
	}

	public int getOwnerId() {
		return ownerId;
	}

	
	public boolean getNotificationEnabled() {
		Context context = KisiApplication.getInstance().getApplicationContext();
		SharedPreferences prefs = context.getSharedPreferences("userconfig", Context.MODE_PRIVATE);
		//check if there is already a value for enabling/disabling notifications is already set
		//otherwise use the one from the server
		if(prefs.contains(generateSharedPreferencesKey())) {
			return prefs.getBoolean(generateSharedPreferencesKey(), true);
		} else {
			return this.isSuggestUnlock();
		}	
	}
	
	public void setNotificationEnabled(boolean value) {
		Context context = KisiApplication.getInstance().getApplicationContext();
		SharedPreferences prefs = context.getSharedPreferences("userconfig", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(generateSharedPreferencesKey(), value);
		editor.commit();
		LockInVicinityDisplayManager.getInstance().update();
	}
	
	private String generateSharedPreferencesKey() {
		if(KisiAPI.getInstance().getUser() != null) {
			return this.id + "-" + KisiAPI.getInstance().getUser().getId();
		}
		else {
			return "";
		}
	}

	/**
	 * Checks if the place is owned by the user or just shared
	 * 
	 * @param place Place to be checked
	 * @return true if user is owner, false if someone else shares this place with the user
	 */
	public boolean userIsOwner(){
		return getOwnerId() == KisiAPI.getInstance().getUser().getId();
	}
	

	public boolean isSuggestUnlock() {
		return suggestUnlock;
	}


	public void setSuggestUnlock(boolean suggestUnlock) {
		this.suggestUnlock = suggestUnlock;
	}

	@Override
	public String toString(){
		return "Place: "+getId();
	}


	public int getDefaultLocatorID() {
		return defaultLocatorID;
	}
}
