package de.kisi.android.api;

import java.util.List;

import de.kisi.android.model.Key;

/**
 * Callback to invoke the onSharedLogsInfoResult function within the
 * SharedKeysInfoFragment.java
 * 
 * @author Ankit
 * 
 */
public interface SharedKeyInfoCallback {
	public void onSharedLogsInfoResult(List<Key> events);

}
