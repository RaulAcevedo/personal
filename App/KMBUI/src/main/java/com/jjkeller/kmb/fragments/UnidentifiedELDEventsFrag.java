package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;

import com.jjkeller.kmb.interfaces.IUnidentifiedELDEvents;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbui.R;

public class UnidentifiedELDEventsFrag extends BaseFragment
{
	private static final String TAG = "UnidentifiedELDEventsFrag Activity";
	private static final String BUNDLE_SELECTALL_ISCHECKED = "selectAllIsChecked";

	private IUnidentifiedELDEvents.UnidentifiedELDEventsFragActions _actionsListener;

	private ListView _listUnassignedEmployeeEvents;
	private Button _btnClaim;
	private CheckBox _chkSelectAll;
	private boolean _initialSelectAllCheckedState = false;
	
	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);

        try {
        	_actionsListener = (IUnidentifiedELDEvents.UnidentifiedELDEventsFragActions) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement UnidentifiedELDEventsFragActions");
        }
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
        // Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.f_unidentifiedeventlogs, container, false);
		findControls(v);

		if(savedInstanceState != null) /* orientation change */ {
			_initialSelectAllCheckedState = savedInstanceState.getBoolean(BUNDLE_SELECTALL_ISCHECKED);
		}

		return v;
	}

	/**
	 * Called after onRestoreInstanceState(Bundle), onRestart(), or onPause(), for your fragment to start interacting with the user.
	 */
	@Override
	public void onResume() {
		super.onResume();

		_chkSelectAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
				_actionsListener.handleSelectAllClicked(compoundButton, b);
			}
		});
	}

	/**
	 * Called as part of the lifecycle when an fragment is going into the background, but has not (yet) been killed.
	 */
	@Override
	public void onPause() {
		super.onPause();

		_chkSelectAll.setOnCheckedChangeListener(null);
	}

	/**
	 * Called during screen rotation to persist values so the screen can be re-created.
	 */
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putBoolean(BUNDLE_SELECTALL_ISCHECKED, _chkSelectAll.isChecked());

		super.onSaveInstanceState(outState);
	}

	protected void findControls(View v)
	{
		//find the Claim button and attach listener
		_btnClaim = (Button) v.findViewById(R.id.uee_btn_claim);
		_btnClaim.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				_actionsListener.handleClaimButtonClicked(v);
			}
		});

		_chkSelectAll = (CheckBox) v.findViewById(R.id.uee_cbo_claimall);
		_chkSelectAll.setChecked(_initialSelectAllCheckedState);

		// find the listview and attach the adapter to populate with UEE records.  the click event is handled in the parent activity.
		_listUnassignedEmployeeEvents = (ListView) v.findViewById(R.id.uee_list_records);
	}

	public ListView getEventsListView()
	{
		return _listUnassignedEmployeeEvents;
	}
	public Button getClaimButtonView()
	{
		return _btnClaim;
	}

}