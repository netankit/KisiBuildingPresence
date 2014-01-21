package de.kisi.android.model;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;

public class Locator {
	@DatabaseField(id = true)
	private int id;
	
	@DatabaseField
	@SerializedName("place_id")
	private int placeId;
	@DatabaseField(foreign = true, foreignAutoRefresh=true, maxForeignAutoRefreshLevel=1)
	private Place place;
	
	@DatabaseField
	@SerializedName("lock_id")
	private int lockId;
	@DatabaseField(foreign = true, foreignAutoRefresh=true, maxForeignAutoRefreshLevel=1)
	private Lock lock;
	
	
	@DatabaseField
	private String name;

	@DatabaseField
	private boolean enabled;
	
	@DatabaseField
	@SerializedName("suggest_unlock_enabled")
	private boolean suggestUnlockEnabled;
	
	@DatabaseField
	@SerializedName("suggest_unlock_treshold")
	private double suggestUnlockTreshold;
	
	
	@DatabaseField
	@SerializedName("auto_unlock_treshold")
	private double autoUnlockTreshold;
	
	@DatabaseField
	private double latitude;
	
	@DatabaseField
	private double longitude;
	
	@DatabaseField
	@SerializedName("auto_unlock_enabled")
	private boolean autoUnlockEnabled;
	
	@DatabaseField
	private String udid;
	
	@DatabaseField
	private int major;
	
	@DatabaseField
	private int minor;
	
//	@DatabaseField
//	@SerializedName("nfc_identifier")
//	private String nfcIdentifier;
	
	@DatabaseField
	private String type;

	
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getPlaceId() {
		return placeId;
	}

	public void setPlaceId(int placeId) {
		this.placeId = placeId;
	}

	public Place getPlace() {
		return place;
	}

	public void setPlace(Place place) {
		this.place = place;
	}

	public int getLockId() {
		return lockId;
	}

	public void setLockId(int lockId) {
		this.lockId = lockId;
	}

	public Lock getLock() {
		return lock;
	}

	public void setLock(Lock lock) {
		this.lock = lock;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isSuggestUnlockEnabled() {
		return suggestUnlockEnabled;
	}

	public void setSuggestUnlockEnabled(boolean suggestUnlockEnabled) {
		this.suggestUnlockEnabled = suggestUnlockEnabled;
	}

	public double getSuggestUnlockTreshold() {
		return suggestUnlockTreshold;
	}

	public void setSuggestUnlockTreshold(double suggestUnlockTreshold) {
		this.suggestUnlockTreshold = suggestUnlockTreshold;
	}

	public double getAutoUnlockTreshold() {
		return autoUnlockTreshold;
	}

	public void setAutoUnlockTreshold(double autoUnlockTreshold) {
		this.autoUnlockTreshold = autoUnlockTreshold;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public boolean isAutoUnlockEnabled() {
		return autoUnlockEnabled;
	}

	public void setAutoUnlockEnabled(boolean autoUnlockEnabled) {
		this.autoUnlockEnabled = autoUnlockEnabled;
	}

	public String getUdid() {
		return udid;
	}

	public void setUdid(String udid) {
		this.udid = udid;
	}

	public int getMajor() {
		return major;
	}

	public void setMajor(int major) {
		this.major = major;
	}

	public int getMinor() {
		return minor;
	}

	public void setMinor(int minor) {
		this.minor = minor;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	

}
