package de.kisi.android.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import de.kisi.android.R;
import de.kisi.android.api.KisiAPI;
import de.kisi.android.model.Lock;
import de.kisi.android.model.Place;

public class ShareKeyFragment extends BaseFragment {

	private View view;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		view = inflater.inflate(R.layout.share_key_activity, container, false);
		buildShareDialog(place);
		return view;
	}

	private void buildShareDialog(final Place place) {
		final List<Lock> locks = place.getLocks();
		LinearLayout linearLayout = (LinearLayout) view
				.findViewById(R.id.place_linear_layout);

		final List<Lock> sendlocks = new ArrayList<Lock>();

		// Getting px form Scale-independent Pixels
		Resources r = getResources();
		int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
				350, r.getDisplayMetrics());
		int height = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_SP, 85, r.getDisplayMetrics());
		int margin = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_SP, 15, r.getDisplayMetrics());

		LinearLayout.LayoutParams layoutParams;
		final EditText emailInput = (EditText) view
				.findViewById(R.id.shareEmailInput);

		layoutParams = new LinearLayout.LayoutParams(width, height);
		layoutParams.setMargins(margin, margin, margin, margin);
		for (final Lock lock : locks) {

			LayoutInflater li = (LayoutInflater) getActivity()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			CheckBox checkBox = (CheckBox) li.inflate(R.layout.share_checkbox,
					null);
			checkBox.setText(lock.getName());
			checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					if (isChecked) {
						sendlocks.add(lock);
					} else {
						sendlocks.remove(lock);
					}
				}

			});
			// final Button button = new Button(this);
			// button.setText(lock.getName());
			// button.setTypeface(font);
			// button.setGravity(Gravity.CENTER);
			// button.setWidth(width);
			// button.setHeight(height);
			// button.setTextColor(Color.DKGRAY);
			// button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
			// button.setCompoundDrawablesWithIntrinsicBounds(uncheckedIcon,
			// null, null, null);
			// button.setVisibility(View.VISIBLE);
			// button.setOnClickListener(new OnClickListener(){
			// private boolean checked = false;
			// @Override
			// public void onClick(View v) {
			// if (checked){
			// button.setCompoundDrawablesWithIntrinsicBounds(uncheckedIcon,
			// null, null, null);
			// sendlocks.remove(lock);
			// checked = false;
			// }else{
			// button.setCompoundDrawablesWithIntrinsicBounds(checkedIcon, null,
			// null, null);
			// sendlocks.add(lock);
			// checked = true;
			// }
			// }});
			//
			// linearLayout.addView(button, layoutParams);
			linearLayout.addView(checkBox, layoutParams);
		}

		Button submit = new Button(getActivity());
		submit.setText(getResources().getString(R.string.share_submit_button));
		submit.setTextColor(Color.DKGRAY);
		submit.setTextSize(25);
		layoutParams = new LinearLayout.LayoutParams(width,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.setMargins(margin, margin, margin, margin);
		submit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (sendlocks.isEmpty()) {
					Toast.makeText(getActivity().getApplicationContext(),
							R.string.share_error, Toast.LENGTH_LONG).show();
					return;
				}
				String email = emailInput.getText().toString();
				if (email.isEmpty()) {
					Toast.makeText(getActivity().getApplicationContext(),
							R.string.share_error_empty_email, Toast.LENGTH_LONG)
							.show();
					return;
				}
				KisiAPI.getInstance().createNewKey(place, email, sendlocks);
				getActivity().onBackPressed();
			}
		});
		linearLayout.addView(submit, layoutParams);

	}

	@Override
	public String getName() {
		return place.getName();
	}

	@Override
	public int getMenuId() {
		return R.menu.share_key;
	}

}
