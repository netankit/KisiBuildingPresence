package de.kisi.android.model;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;

/**
 * Data Model : Key The UpdateKeysCall allows to get all data over HTTP from the
 * server end point: places/place_id/keys. The data is in json format, we need
 * specific part of the data and it is defined in this data model. Important
 * information like place_id, issued_to_email, issued_to_id, lock_id and lock
 * are defined and necessary getters and setters are defined for easy access.
 * 
 * @author Ankit
 * 
 */

public class Key {

	private int id;

	@SerializedName("place_id")
	private int place_id;

	@SerializedName("issued_to_email")
	private String issued_to_email;

	@SerializedName("issued_to_id")
	private int issued_to_id;

	@SerializedName("lock_id")
	private int lock_id;

	@DatabaseField(foreign = true, foreignAutoRefresh = true, maxForeignAutoRefreshLevel = 1)
	private Lock lock;

	public String getIssued_to_email() {
		return issued_to_email;
	}

	public void setIssued_to_email(String issued_to_email) {
		this.issued_to_email = issued_to_email;
	}

	public long getId() {
		// TODO Auto-generated method stub
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Lock getLock() {
		return lock;
	}

	public void setLock(Lock lock) {
		this.lock = lock;
	}

	public int getPlace_id() {
		return place_id;
	}

	public void setPlace_id(int place_id) {
		this.place_id = place_id;
	}

	public int getIssued_to_id() {
		return issued_to_id;
	}

	public void setIssued_to_id(int issued_to_id) {
		this.issued_to_id = issued_to_id;
	}

	public int getLock_id() {
		return lock_id;
	}

	public void setLock_id(int lock_id) {
		this.lock_id = lock_id;
	}

}
