package de.kisi.android.ui;

import de.kisi.android.R;
import de.kisi.android.api.KisiAPI;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class LockListFragment extends BaseFragment{


	private ListView mLockList;
	private LockListAdapter mLockListAdapter;
	private boolean viewCreated = false;
	
	private boolean unlockInProgress = false;
	private boolean highlightInProgress = false;
	private int lockId;
	private String sender;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		mLockList = (ListView) inflater.inflate(R.layout.lock_list, container, false);

		mLockListAdapter = new LockListAdapter(getActivity(), place);
		mLockList.setAdapter(mLockListAdapter);
		mLockList.setOnItemClickListener(new LockListOnItemClickListener(place));
		viewCreated = true;
		if(unlockInProgress){
			performItemClick(lockId,sender);
		}
		if(highlightInProgress){
			highlightButton(lockId);
		}
		return mLockList;
	}

	@Override
	public String getName() {
		if(place!=null)
			return place.getName();
		return "";
	}

	@Override
	public int getMenuId() {
		if (KisiAPI.getInstance().userIsOwner(place)){
			return R.menu.main_place;
		}else{
			return R.menu.main_place_no_share;
		}
	}

	public void performItemClick(int lockId, String sender){
		if(viewCreated){
			int mActivePosition = mLockListAdapter.getItemPosition(lockId);
			mLockListAdapter.setTrigger(sender);
			mLockList.invalidate();
			// + 2 cause there are 2 elements before the places start in the ListView (TextView and the divider)
			//TODO: review this code: +2 caused an indexOutOfBoundsException so i removed it
			mLockList.performItemClick(mLockList.getAdapter().getView(mActivePosition, null, null), mActivePosition,
					mLockList.getAdapter().getItemId(mActivePosition));
		}else{
			unlockInProgress = true;
			this.lockId = lockId;
			this.sender = sender;
		}

	}
	public void highlightButton(int lockId){
		if(viewCreated){
			mLockListAdapter.addSuggestedNFC(lockId);
			mLockList.invalidate();
		}else{
			highlightInProgress = true;
			this.lockId = lockId;
		}

	}
}
