package de.kisi.android.ui;

import java.util.Stack;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.commonsware.cwac.merge.MergeAdapter;
import com.electricimp.blinkup.BlinkupController;
import com.newrelic.agent.android.NewRelic;

import de.kisi.android.BaseActivity;
import de.kisi.android.R;
import de.kisi.android.account.KisiAuthenticator;
import de.kisi.android.api.KisiAPI;
import de.kisi.android.api.OnPlaceChangedListener;
import de.kisi.android.model.Place;

public class KisiMainActivity extends BaseActivity implements OnPlaceChangedListener{

	public static final String IMP_API_KEY = "08a6dd6db0cd365513df881568c47a1c";
	public static final String NEW_RELIC_API_KEY = "AAe80044cf73854b68f6e83881c9e61c0df9d92e56";



	private Stack<BaseFragment> fragmentStack = new Stack<BaseFragment>();
	private boolean fragmentStackInvalid = false;
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private DrawerListAdapter mDrawerListAdapter;
	//private ListView mLockList;
	//private LockListAdapter mLockListAdapter; 
	private ActionBarDrawerToggle mDrawerToggle;
	private int selectedPosition = 0;

	private MergeAdapter  mMergeAdapter;

	private Place mPlace = null;


	private TextView accountName;
	private TextView title;

	// just choose a random value
	// TODO: change this later
	public static int LOGIN_REQUEST_CODE = 5;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		NewRelic.withApplicationToken(NEW_RELIC_API_KEY).start(this.getApplication());

		setContentView(R.layout.navigation_drawer_layout);

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);
		//mLockList = (ListView) findViewById(R.id.lock_list);
		mDrawerList.setFocusableInTouchMode(false);
		mMergeAdapter = new MergeAdapter();
		mDrawerListAdapter = new DrawerListAdapter(this);

		buildTopDivider();

		mMergeAdapter.addAdapter(mDrawerListAdapter);

		buildStaticMenuItems();

		mDrawerList.setAdapter(mMergeAdapter);
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
		getActionBar().setCustomView(R.layout.abs_layout);
		getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_USE_LOGO | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_HOME_AS_UP);		
		
		title = (TextView) getActionBar().getCustomView().findViewById(R.id.actionbar_title);
		
		
		mDrawerToggle = new ActionBarDrawerToggle( this,  mDrawerLayout, R.drawable.ic_drawer,  R.string.place_overview,  R.string.kisi ) {
			public void onDrawerClosed(View view) {
				super.onDrawerClosed(view);
				String newTitle = fragmentStack.peek().getName();
				//getActionBar().setTitle(newTitle);
				title.setText(newTitle);
				invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
			}

	            public void onDrawerOpened(View drawerView) {
	                super.onDrawerOpened(drawerView);
	                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
	            }
		 };
		 
		mDrawerLayout.setDrawerListener(mDrawerToggle);	 


		AccountManager mAccountManager = AccountManager.get(this);
		if(KisiAPI.getInstance().getUser()==null){
			if(mAccountManager.getAccountsByType(KisiAuthenticator.ACCOUNT_TYPE).length==1){
				Account account = mAccountManager.getAccountsByType(KisiAuthenticator.ACCOUNT_TYPE)[0];
				mAccountManager.removeAccount(account, null, null);
			}
			Intent login = new Intent(this, AccountPickerActivity.class);
			startActivityForResult(login, LOGIN_REQUEST_CODE);
		}
		else {
			setUiIntoStartState();
		}
	}


	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onBackPressed() {
		if(fragmentStack.size()<=1){
			super.onBackPressed();
			return;
		}
		if (fragmentStack.pop() instanceof LockListFragment){
			super.onBackPressed();
			return;
		}
		setFragment(fragmentStack.pop());
	}

	@Override
	public void onPause(){
		super.onPause();
		closeDrawer();

	}

	protected void closeDrawer(){
		mDrawerLayout.closeDrawer(mDrawerList);
	}

	protected void setFragment(BaseFragment fragment){
		if (fragment == null){
			return;
		}

		closeDrawer();

		if (fragmentStackInvalid){
			fragmentStack.clear();
		}else{
			if (fragmentStack.size()>0){
				BaseFragment oldFragment = fragmentStack.peek();
				if (oldFragment.getClass().equals(fragment.getClass())){
					if( oldFragment.place== null && fragment.place == null){
						return;
					}else if (oldFragment.place!= null && fragment.place != null && oldFragment.place.getId() == fragment.place.getId()){
						return;
					}
				}
			}
		}

		//getActionBar().setTitle(fragment.getName());
		title.setText(fragment.getName());

		FragmentManager fragmentManager = getFragmentManager();
		fragmentStack.push(fragment);
		fragmentManager.beginTransaction()
		.replace(R.id.main_content, fragment)
		.commit();
		invalidateOptionsMenu();
	}


	@Override
	protected void onStart() {
		super.onStart();
		if(getIntent().hasExtra("Type")) {
			handleIntent(getIntent());
			getIntent().removeExtra("Type");
		}
	}


	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		if(intent.hasExtra("Type")) {
			handleIntent(intent);
			intent.removeExtra("Type");
		}
		invalidateOptionsMenu();
	}

	private class DrawerItemClickListener implements ListView.OnItemClickListener {
		@SuppressWarnings("rawtypes")
		@Override
		public void onItemClick(AdapterView parent, View view, int position, long id) {
			selectItem(position, id);
		}
	}

	private void setUiIntoStartState() {
		Place place = KisiAPI.getInstance().getPlaceAt(0);
		accountName.setText(KisiAPI.getInstance().getUser() == null ?  " " : KisiAPI.getInstance().getUser().getEmail() );
		if(place != null) {
			// - 2 cause there are 2 elements before the places start in the ListView (TextView and the divider)
			selectItem(selectedPosition + 2 , mDrawerListAdapter.getItemId(selectedPosition));
        	if (place != null){
        		String newTitle = place.getName();
        		title.setText(newTitle);
        	}
		}
		else {
			KisiAPI.getInstance().updatePlaces(this);
		}
	}


	private LockListFragment selectItem(int position, long id) {
		Place place = KisiAPI.getInstance().getPlaceById((int) id);
		if(place != null) {
			title.setText(place.getName());
	    	mPlace = place;
		}
		// - 2 cause there are 2 elements before the places start in the ListView (TextView and the divider)
		selectedPosition = position - 2;
		mDrawerListAdapter.selectItem(position - 2);
		LockListFragment fragment = new LockListFragment();
		fragment.setPlace(place);
		mPlace = place;
		setFragment(fragment);
		
		// Highlight the selected item, update the title, and close the drawer and check if the position is available 
		// + 2 cause there are 2 elements before the places start in the ListView (TextView and the divider)
		if( position < mDrawerListAdapter.getCount() + 2) {
			mDrawerList.setItemChecked(position, true);
		}
		else {
			mDrawerList.setItemChecked(2, true);
		}
		closeDrawer();
		return (LockListFragment) fragmentStack.peek();
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Pass the event to ActionBarDrawerToggle, if it returns
		// true, then it has handled the app icon touch event
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		// Handle your other action bar items...
		// get all places
		Place[] places = KisiAPI.getInstance().getPlaces();
		Place place;
		if(item.getItemId() == R.id.share || item.getItemId() == R.id.share_actionbar_button) {
			// check if user has a place
			if (places.length == 0) {
				Toast.makeText(this, R.string.share_empty_place_error, Toast.LENGTH_LONG).show();
				return false;
			}

			place = mPlace; 

			ShareKeyFragment fragment = new ShareKeyFragment();
			fragment.setPlace(place);
			setFragment(fragment);

			return true;
		}
		else if(item.getItemId() == R.id.showLog) {
			// check if user has a place
			if (places.length == 0) {
				Toast.makeText(this, R.string.log_empty_place_error, Toast.LENGTH_LONG).show();
				return false;
			}
			place = mPlace;// mLockListAdapter.getPlace();

			LogInfoFragment fragment = new LogInfoFragment();
			fragment.setPlace(place);
			setFragment(fragment);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void refreshViews() {
		mDrawerListAdapter.notifyDataSetChanged();
		mDrawerLayout.invalidate();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_place_no_share, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu){
		MenuInflater inflater = getMenuInflater();
		menu.clear();
		if(fragmentStack.size()>0){
			inflater.inflate(fragmentStack.peek().getMenuId(), menu);
		}
		return true;
	}


	
	private void handleIntent(Intent intent) {
		// No extras, nothing to do
		if (intent.getExtras() == null)
			return;
		String type = intent.getStringExtra("Type");
		if (type.equals("unlock"))
			handleUnlockIntent(intent);
		if (type.equals("highlight"))
			handleHighlightIntent(intent);
		if (type.equals("nfcNoLock"))
			handleNFCNoLockIntent();
	}

	private void handleNFCNoLockIntent(){
		AlertDialog alertDialog = new AlertDialog.Builder(this).setPositiveButton(getResources().getString(R.string.ok),null).create();
		alertDialog.setTitle(R.string.restricted_access);
		alertDialog.setMessage(getResources().getString(R.string.no_access_to_lock));
		alertDialog.show();
	}


	private void handleUnlockIntent(Intent intent) {
		String sender = intent.getStringExtra("Sender");
		if (intent.getExtras() == null){
			return;
		}

		int placeId = intent.getIntExtra("Place", -1);
		for (int j = 0; j < KisiAPI.getInstance().getPlaces().length; j++) {
			if (KisiAPI.getInstance().getPlaces()[j].getId() == placeId) {
				LockListFragment fragment = selectItem(j+2, placeId);
				int lockId = intent.getIntExtra("Lock", -1);
				//check if there is a lockId in the intent and then unlock the right lock
				if(lockId != -1) {
					fragment.performItemClick(lockId, sender);
				}
			}
		}

	}

	private void handleHighlightIntent(Intent intent){
		if (intent.getExtras() == null){
			return;
		}
		
		int placeId = intent.getIntExtra("Place", -1);
		for (int j = 0; j < KisiAPI.getInstance().getPlaces().length; j++) {
			if (KisiAPI.getInstance().getPlaces()[j].getId() == placeId) {
				LockListFragment fragment = selectItem(j+2, placeId);
				int lockId = intent.getIntExtra("Lock", -1);
				//check if there is a lockId in the intent and then unlock the right lock
				if(lockId != -1) {
					fragment.highlightButton(lockId);
				}
			}
		}
	}

	// callback for blinkup and login
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == LOGIN_REQUEST_CODE) {
			switch(resultCode){
			case AccountPickerActivity.LOGIN_FAILED:
				KisiAPI.getInstance().logout();
				Intent login = new Intent(this, AccountPickerActivity.class);
				startActivityForResult(login, LOGIN_REQUEST_CODE);
				return;

			case AccountPickerActivity.LOGIN_OPTIMISTIC_SUCCESS:
				Log.i("onActivityResult", "LOGIN_OPTIMISTIC_SUCCESS");
				KisiAPI.getInstance().updatePlaces(this);
				return;
			case AccountPickerActivity.LOGIN_REAL_SUCCESS:
				Log.i("onActivityResult", "LOGIN_REAL_SUCCESS");
				KisiAPI.getInstance().updatePlaces(this);
				setUiIntoStartState();
				return;

			case AccountPickerActivity.LOGIN_CANCELED:
				finish();
			}
		} else {
			BlinkupController.getInstance().handleActivityResult(this,requestCode, resultCode, data);
		}
	}



	public void logout() {
		KisiAPI.getInstance().logout();
		Intent login = new Intent(this, AccountPickerActivity.class);
		startActivityForResult(login, LOGIN_REQUEST_CODE);
	}

	@Override
	public void onPlaceChanged(Place[] newPlaces) {
		fragmentStackInvalid = true;
		refreshViews();
		setUiIntoStartState();
	}


	//these methods fill the drawer with its static elements


	private void buildTopDivider() {
		LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		final TextView places = (TextView) li.inflate(R.layout.drawer_list_section_item, null);
		places.setText(getResources().getText(R.string.place_overview));
		mMergeAdapter.addView(places);

		final View divider =  (View) li.inflate(R.layout.drawer_list_divider, null);
		mMergeAdapter.addView(divider);
	}




	private void buildStaticMenuItems() {

		LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		final TextView settings = (TextView) li.inflate(R.layout.drawer_list_section_item, null);
		settings.setText(getResources().getText(R.string.settings));
		mMergeAdapter.addView(settings);

		final View divider =  (View) li.inflate(R.layout.drawer_list_divider, null);
		mMergeAdapter.addView(divider);

		StaticMenuOnClickListener listener =  new StaticMenuOnClickListener(this);

		final TextView refreshButton = (TextView) li.inflate(R.layout.drawer_list_item, null);
		refreshButton.setId(R.id.refreshButton);
		refreshButton.setText(getResources().getText(R.string.refresh));
		refreshButton.setClickable(true);
		refreshButton.setOnClickListener(listener);
		mMergeAdapter.addView(refreshButton);

		addDivider();

		final TextView setup_kisi_button = (TextView) li.inflate(R.layout.drawer_list_item, null);
		setup_kisi_button.setId(R.id.setup_kisi_button);
		setup_kisi_button.setText(getResources().getText(R.string.setup));
		setup_kisi_button.setClickable(true);
		setup_kisi_button.setOnClickListener(listener);
		mMergeAdapter.addView(setup_kisi_button);

		addDivider();

		final TextView notification = (TextView) li.inflate(R.layout.drawer_list_item, null);
		notification.setId(R.id.notification_settings_button);
		notification.setText(getResources().getText(R.string.notification_settings));
		notification.setClickable(true);
		notification.setOnClickListener(listener);
		mMergeAdapter.addView(notification);

		addDivider();

		final TextView about = (TextView) li.inflate(R.layout.drawer_list_item, null);
		about.setId(R.id.about_button);
		about.setText(getResources().getText(R.string.about));
		about.setClickable(true);
		about.setOnClickListener(listener);
		mMergeAdapter.addView(about);

		final TextView account = (TextView) li.inflate(R.layout.drawer_list_section_item, null);
		account.setText(getResources().getText(R.string.account));
		mMergeAdapter.addView(account);

		final View divider2 =  (View) li.inflate(R.layout.drawer_list_divider, null);
		mMergeAdapter.addView(divider2);


		accountName = (TextView) li.inflate(R.layout.drawer_list_item, null);
		accountName.setText(KisiAPI.getInstance().getUser() == null ?  " " : KisiAPI.getInstance().getUser().getEmail() );
		accountName.setTextColor(Color.GRAY);
		mMergeAdapter.addView(accountName);

		addDivider();

		final TextView logout = (TextView) li.inflate(R.layout.drawer_list_item, null);	
		logout.setId(R.id.logout_button);
		logout.setText(getResources().getText(R.string.logout));
		logout.setClickable(true);
		logout.setOnClickListener(listener);
		mMergeAdapter.addView(logout);




	}

	private void addDivider() {
		LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View divider =  (View) li.inflate(R.layout.drawer_list_small_divider, null);
		mMergeAdapter.addView(divider);
	}

	protected void setActiveView(View view){
	}

}
