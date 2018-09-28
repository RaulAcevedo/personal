package com.jjkeller.kmb;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.jjkeller.kmb.adapters.UnidentifiedELDEventsReviewListAdapter;
import com.jjkeller.kmb.fragments.UnidentifiedELDEventsReviewFrag;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmbapi.common.JsonUtil;
import com.jjkeller.kmbapi.common.LogCat;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.ControllerFactory;
import com.jjkeller.kmbapi.controller.EmployeeLogEldMandateController;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.employeelogeldevents.Enums;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;
import com.jjkeller.kmbui.R;

import java.util.Date;
import java.util.List;

public class UnidentifiedELDEventsReview extends BaseActivity {

	private static final String TAG = "UnidentifiedELDEventsReview Activity";

    public static final String BUNDLE_EVENTS_TO_REVIEW = "eventsToReview";
    public static final String BUNDLE_REVIEW_HEADER = "REVIEW_HEADER";
    public static final String BUNDLE_REVIEW_INSTRUCTIONS = "REVIEW_INSTRUCTIONS";
	public static final String BUNDLE_FROM_SCREEN = "FROM_SCREEN";

    ProgressDialog pd;
    private UnidentifiedELDEventsReviewFrag contentFrag;
	private List<EmployeeLogEldEvent> eventsToReview;
	private UnidentifiedELDEventsReviewListAdapter eventsToReviewAdapter;

    private String header;
    private String instruction;
	private String fromScreen;

	private EmployeeLogEldEvent previousEvent = null;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.baselayout);

		if (savedInstanceState != null){
            String eventJson = savedInstanceState.getString(BUNDLE_EVENTS_TO_REVIEW);

            eventsToReview = JsonUtil.getGson().fromJson(eventJson, JsonUtil.TYPE_LIST_OF_EMPLOYEE_LOG_ELD_EVENT);
            header = savedInstanceState.getString(BUNDLE_REVIEW_HEADER);
            instruction = savedInstanceState.getString(BUNDLE_REVIEW_INSTRUCTIONS);
			fromScreen = savedInstanceState.getString(BUNDLE_FROM_SCREEN);
        } else {
            String eventJson = getIntent().getStringExtra(BUNDLE_EVENTS_TO_REVIEW);

            eventsToReview = JsonUtil.getGson().fromJson(eventJson, JsonUtil.TYPE_LIST_OF_EMPLOYEE_LOG_ELD_EVENT);
            header = getIntent().getStringExtra(BUNDLE_REVIEW_HEADER);
            instruction = getIntent().getStringExtra(BUNDLE_REVIEW_INSTRUCTIONS);
			fromScreen = getIntent().getStringExtra(BUNDLE_FROM_SCREEN);
        }

        loadControls();

	}

    @Override
    protected void onResume() {
        //This is overridden in BaseActivity.java.  We want the default Android behaviour
        super.onResume();
    }

    @Override
    protected void onPause() {
        //This is overridden in BaseActivity.java.  We want the default Android behaviour
        super.onPause();
    }

    @Override
	protected void onSaveInstanceState(Bundle outState) {
        if (eventsToReviewAdapter != null) {
            outState.putString(BUNDLE_EVENTS_TO_REVIEW, JsonUtil.getGson().toJson(eventsToReviewAdapter.getEvents()));
        } else {
            outState.putString(BUNDLE_EVENTS_TO_REVIEW, JsonUtil.getGson().toJson(eventsToReview));
        }
        outState.putString(BUNDLE_REVIEW_HEADER, header);
        outState.putString(BUNDLE_REVIEW_INSTRUCTIONS, instruction);
		outState.putString(BUNDLE_FROM_SCREEN, fromScreen);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void InitController() { }

	@Override
	protected void loadControls() {
		super.loadControls();
		loadContentFragment(new UnidentifiedELDEventsReviewFrag());
	}

	@Override
	public void setFragments() {
		super.setFragments();

		// set the fragment which holds the expandablelistview displaying all the records
		Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
		contentFrag = (UnidentifiedELDEventsReviewFrag) f;

		// set the list adapter
		eventsToReviewAdapter = new UnidentifiedELDEventsReviewListAdapter(this, eventsToReview);
		contentFrag.getEventsListView().setAdapter(eventsToReviewAdapter);
		contentFrag.getEventsListView().setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
			int previousItem = -1;

			@Override
			public void onGroupExpand(int groupPosition) {
				if (groupPosition != previousItem) {
					contentFrag.getEventsListView().collapseGroup(previousItem);
				}
				previousItem = groupPosition;
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		return super.onOptionsItemSelected(item);
	}

	@Override
	public String getActivityMenuItemList()
	{
		return getString(R.string.unidentified_events_review_btn_done);
	}

	@Override
	public void onNavItemSelected(int menuItem)
	{
		// DONE is the only action
		finish();
		this.startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
	}


	public void populateEvent(final int groupPosition){
		if(previousEvent != null) {
			View v = getGroupViewByPosition(groupPosition);
			populateFieldIfVisible((EditText) v.findViewById(R.id.editLocation), previousEvent.getDriversLocationDescription());
			populateFieldIfVisible((EditText) v.findViewById(R.id.editUnitNumber), previousEvent.getTractorNumber());
			populateFieldIfVisible((EditText) v.findViewById(R.id.editTrailerInfo), previousEvent.getTrailerNumber());
			populateFieldIfVisible((EditText) v.findViewById(R.id.editShipmentInfo), previousEvent.getShipmentInfo());
		}
	}

	private void populateFieldIfVisible(EditText editText, String value){
		if(editText != null && editText.getVisibility() == View.VISIBLE){
			editText.setText(value);
		}
	}

	private View getGroupViewByPosition(int groupPosition){
		return contentFrag.getEventsListView().getChildAt(groupPosition + 1);
	}

	public boolean saveEvent(int groupPosition, EmployeeLogEldEvent event){

        String sLocation = event.getDriversLocationDescription(), sUnitNumber = event.getTractorNumber(), sTrailerInfo = event.getTrailerNumber(), sShipmentInfo = event.getShipmentInfo();
        boolean hasErrors = false;

        for (int i = 0; i < contentFrag.getEventsListView().getChildCount(); i++) {
            View v = contentFrag.getEventsListView().getChildAt(i);


            //Find, validate LOCATION
            EditText location = (EditText) v.findViewById(R.id.editLocation);

            if(location != null && location.getVisibility() == View.VISIBLE){
                String locationString = location.getText().toString().trim();
                if (locationString.length() < 5) {
                    location.setError(Html.fromHtml(getResources().getString(R.string.msgactuallocationminimumlengtherror)));
                    hasErrors = true;
                } else {
                    sLocation = locationString;
                }
            }

			//Find, validate UNIT NUMBER
			EditText unitNumber = (EditText) v.findViewById(R.id.editUnitNumber);
			if(unitNumber != null && unitNumber.getVisibility() == View.VISIBLE){
				if(unitNumber.getText().toString().trim().length() > 0)
					sUnitNumber = unitNumber.getText().toString();
			}

			//Find, validate TRAILER INFO
			EditText trailerInfo = (EditText) v.findViewById(R.id.editTrailerInfo);
			if(trailerInfo != null && trailerInfo.getVisibility() == View.VISIBLE){
				if(trailerInfo.getText().toString().trim().length() > 0)
					sTrailerInfo = trailerInfo.getText().toString();
			}

			//Find, validate SHIPMENT INFO
			EditText shipmentInfo = (EditText) v.findViewById(R.id.editShipmentInfo);
			if(shipmentInfo != null && shipmentInfo.getVisibility() == View.VISIBLE){
				if(shipmentInfo.getText().toString().trim().length() > 0)
					sShipmentInfo = shipmentInfo.getText().toString();
			}

			Button prevButton = (Button) v.findViewById(R.id.btnPopulate);
			if(prevButton != null && (!hasErrors || previousEvent != null)) {
				prevButton.setVisibility(View.VISIBLE);
			}
		}

        if(hasErrors) {
            return false;
        }

        previousEvent = new EmployeeLogEldEvent();
		previousEvent.setDriversLocationDescription(sLocation);
		previousEvent.setTractorNumber(sUnitNumber);
		previousEvent.setTrailerNumber(sTrailerInfo);
		previousEvent.setShipmentInfo(sShipmentInfo);


		// data is valid, set event values
		event.setDriversLocationDescription(sLocation);
		event.setTractorNumber(sUnitNumber);
		event.setTrailerNumber(sTrailerInfo);
		event.setShipmentInfo(sShipmentInfo);

		// save event
		try{

			LogCat.getInstance().d(TAG, "Updating event "+event.getEventDateTime()
					+ " with unit number "+event.getTractorNumber()
					+ " and location "+event.getLocation().getName()
					+ " and trailer info "+event.getTrailerNumber()
					+ " and shipment info "+event.getShipmentInfo());

			GlobalState globalState = GlobalState.getInstance();
			EmployeeLogEldMandateController mandateController = ControllerFactory.getInstance().getEmployeeLogEldMandateController();
			User currentUser = globalState.getCurrentUser();

            Date logEndTime = mandateController.findEndTimeForEvent(event, currentUser);

			if(fromScreen != null && fromScreen.equalsIgnoreCase("EDITLOG"))
				mandateController.saveEldEvent(event, Enums.SpecialDrivingCategory.None, logEndTime, Enums.ActionInitiatingSaveEnum.EditLog);
			else
				mandateController.saveEldEvent(event, Enums.SpecialDrivingCategory.None, logEndTime, Enums.ActionInitiatingSaveEnum.ClaimUnidentifiedEvent);
			Toast.makeText(getApplicationContext(), "Event updated.", Toast.LENGTH_LONG).show();

		}catch (Throwable throwable) {
			Toast.makeText(getApplicationContext(), "Event was not save", Toast.LENGTH_LONG).show();
			LogCat.getInstance().e(TAG, throwable.getMessage() + ": " + Log.getStackTraceString(throwable));
			return false;
		}

		//clean up the expandablelistview
		contentFrag.getEventsListView().collapseGroup(groupPosition);
		eventsToReviewAdapter.removeGroup(groupPosition);
		eventsToReviewAdapter.notifyDataSetChanged();
		
		// if this is the last event saved, call finish() to move onto the next screen
		if(eventsToReviewAdapter.getGroupCount() == 0) {
			finish();
		}

		return true;
	}

}