package de.kisi.android.vicinity.manager;

import de.kisi.android.KisiApplication;
import de.kisi.android.api.KisiAPI;
import de.kisi.android.model.Locator;
import de.kisi.android.model.Lock;
import de.kisi.android.model.Place;
import de.kisi.android.ui.KisiMainActivity;
import de.kisi.android.vicinity.LockInVicinityActorFactory;
import de.kisi.android.vicinity.LockInVicinityActorInterface;
import android.app.Activity;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;

public class NFCReceiver extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Finish the activity at the start to avoid ugly animations
        finish();

		try{
	        // Read the first record which contains the NFC data
			Intent intent = getIntent();
	        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
	        NdefRecord relayRecord = ((NdefMessage)rawMsgs[0]).getRecords()[0];

	        // Get the Data on the Tag
	        String nfcData = new String(relayRecord.getPayload());
	        
	        // Display the data on the tag for debuging
	        //Toast.makeText(this, nfcData, Toast.LENGTH_SHORT).show();
	        
	        
	        // Test all locators for equality to the data string
	        boolean foundLock = false;
	        for(Place place : KisiAPI.getInstance().getPlaces()){
	        	for(Lock lock : place.getLocks()){
	        		for(Locator locator : lock.getLocators()){
	        			if (nfcData.equals(locator.getTag())){
	        		        // get actor for NFC
	        				LockInVicinityActorInterface actor = LockInVicinityActorFactory.getActor(locator);
	        				// act
	        				actor.actOnEntry(locator);
	        				foundLock = true;
	        			}
	        		}
	        	}
	        }
	
	        if (!foundLock){
	    		Intent i = new Intent(KisiApplication.getInstance(), KisiMainActivity.class);
	    		i.putExtra("Type", "nfcNoLock");
	    		startActivity(i);
	        }
	    }catch(Exception e){
	    }
	}

	
}