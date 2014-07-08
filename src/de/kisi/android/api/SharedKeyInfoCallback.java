package de.kisi.android.api;

import java.util.List;

import de.kisi.android.model.Key;

public interface SharedKeyInfoCallback {
	public void onSharedLogsInfoResult(List<Key> events);

}
