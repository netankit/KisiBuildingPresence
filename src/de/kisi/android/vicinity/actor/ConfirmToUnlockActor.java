package de.kisi.android.vicinity.actor;

import de.kisi.android.vicinity.LockInVicinityActorInterface;
import de.kisi.android.vicinity.LockInVicinityDisplayManager;

/**
 * The ConformToUnlockActor is a simple passthrough of the signal.
 * It sends a Message to the DisplayManager that a Vicinity of a Place has
 * been entered. The Lock should only open when the User confirm this signal.
 * 
 * @author Thomas H�rmann
 *
 */
public class ConfirmToUnlockActor implements LockInVicinityActorInterface {

	@Override
	public void actOnEntry(int placeID, int lockId) {
		LockInVicinityDisplayManager.getInstance().notifyOnEntry(placeID,lockId);
	}

	@Override
	public void actOnExit(int placeID, int lockId) {
		LockInVicinityDisplayManager.getInstance().notifyOnExit(placeID,lockId);
	}

}
