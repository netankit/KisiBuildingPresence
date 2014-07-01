package de.kisi.android.ui;

import de.kisi.android.KisiApplication;
import de.kisi.android.R;
import de.kisi.android.api.KisiAPI;
import de.kisi.android.api.UnlockCallback;
import de.kisi.android.model.Lock;
import de.kisi.android.model.Place;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.Toast;

public class LockListOnItemClickListener implements OnItemClickListener {

	private Place place;
	private static long delay = 1500;
	
	
	public LockListOnItemClickListener(Place place) {
		this.place = place;
	}

	
	@Override
	public void  onItemClick(final AdapterView<?> parent,final View view, final int position, long id) {
		final ProgressDialog progressDialog = new ProgressDialog(parent.getContext());
		progressDialog.setMessage(KisiApplication.getInstance().getString(R.string.opening));
		progressDialog.setCancelable(false);
		progressDialog.show();
		final Lock lock = place.getLocks().get(position);
		Log.d("LockListOnItemClick", String.valueOf(parent.getAdapter().getCount()));
		Log.d("LockListOnItemClick", "Position: "+position);
		Log.d("LockListOnItemClick", ((Button)view).getText().toString());
		
		LockListAdapter adapter = (LockListAdapter)parent.getAdapter();
		String trigger = adapter.getTrigger(); 


		String buttonTrigger = "manual";
		boolean automatic = false;
		if("NFC".equals(trigger)){
			buttonTrigger = "NFC";
			automatic = true;
		}
		if("BLE".equals(trigger))
			buttonTrigger = "beacon";
		if("geofence".equals(trigger))
			buttonTrigger = "geofence";
		if(adapter.isSuggestedNFC(lock.getId())){
			buttonTrigger = "NFC";
			automatic = false;
		}
		
		
		
		
		KisiAPI.getInstance().unlock(lock, new UnlockCallback(){
			
			
			@Override
			public void onUnlockSuccess(String message) {
				progressDialog.dismiss();
				Toast.makeText(KisiApplication.getInstance(), message, Toast.LENGTH_SHORT).show();
				final Button currentButton = (Button) view;
				parent.invalidate();
				currentButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.kisi_lock_open, 0, 0, 0);
				currentButton.setBackgroundColor(Color.parseColor("#99CC00"));
				Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					public void run() {

						// after delay back to old design re-enable click
						currentButton.setBackgroundColor(KisiApplication.getInstance().getResources().getColor(R.color.kisi_color));
						currentButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.kisi_lock, 0, 0, 0);

					}
				}, delay);	
			}

			@Override
			public void onUnlockFail(String alertMsg) {
				progressDialog.dismiss();
				Toast.makeText(KisiApplication.getInstance(), alertMsg, Toast.LENGTH_SHORT).show();
				Button button = (Button)view ;
				final Button currentButton = button;
				parent.invalidate();
				// save button design
				final String currentText = (String) currentButton.getText();
				// change to failure design
				currentButton.setBackgroundColor(Color.RED);
				currentButton.setText(alertMsg);

				Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					public void run() {

						// after delay back to old design re-enable click
						currentButton.setBackgroundColor(KisiApplication.getInstance().getResources().getColor(R.color.kisi_color));
						currentButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.kisi_lock, 0, 0, 0);
						currentButton.setText(currentText);
					}
				}, delay);

			}				
			},buttonTrigger, automatic);

	
	}
	

}
