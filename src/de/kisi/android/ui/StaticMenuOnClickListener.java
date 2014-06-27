package de.kisi.android.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.electricimp.blinkup.BlinkupController;
import com.electricimp.blinkup.BlinkupController.ServerErrorHandler;

import de.kisi.android.KisiApplication;
import de.kisi.android.R;
import de.kisi.android.api.KisiAPI;

public class StaticMenuOnClickListener implements OnClickListener {
	private KisiMainActivity activity;
	
	public StaticMenuOnClickListener(KisiMainActivity activity){
		this.activity = activity;
	}
	
	@Override
	public void onClick(View v) {			

		activity.closeDrawer();

		switch(v.getId()) {
			case R.id.refreshButton:
				KisiAPI.getInstance().refresh(activity);
			return;
		

		case R.id.setup_kisi_button:

			BlinkupController blinkup = BlinkupController.getInstance();
			blinkup.intentBlinkupComplete = new Intent(activity, BlinkupCompleteActivity.class);

			if (KisiAPI.getInstance().getUser().getEiPlanId() != null) {
				blinkup.setPlanID(KisiAPI.getInstance().getUser().getEiPlanId());
			}
			blinkup.selectWifiAndSetupDevice(activity, KisiMainActivity.IMP_API_KEY, new ServerErrorHandler() {
						@Override
						public void onError(String errorMsg) {
							Toast.makeText(KisiApplication.getInstance(), errorMsg, Toast.LENGTH_SHORT).show();
						}
					});
			return ;

		case R.id.logout_button:
			activity.logout();
			return;
			
			
		case R.id.about_button:
			AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
			alertDialog.setTitle(R.string.kisi);
			String versionName = null;
			try {
				versionName = KisiApplication.getInstance().getPackageManager().getPackageInfo(KisiApplication.getInstance().getPackageName(), 0).versionName;
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
			alertDialog.setMessage(activity.getResources().getString(R.string.version) + versionName);
			alertDialog.show();
			return;
			
			
		case R.id.notification_settings_button:
			activity.setActiveView(v);

			activity.setFragment(new PlaceNotificationSettingsFragment());
			return;
			

		default:
			return;
		}
	}

}


