package com.jjkeller.kmb;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.jjkeller.kmb.fragments.UnidentifiedELDEventsFrag;
import com.jjkeller.kmb.interfaces.IUnidentifiedELDEvents;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmbapi.common.JsonUtil;
import com.jjkeller.kmbapi.common.LogCat;
import com.jjkeller.kmbapi.common.TimeKeeper;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.ControllerFactory;
import com.jjkeller.kmbapi.controller.EmployeeLogEldMandateController;
import com.jjkeller.kmbapi.controller.LogEntryController;
import com.jjkeller.kmbapi.controller.dataaccess.EmployeeLogEldEventFacade;
import com.jjkeller.kmbapi.controller.interfaces.IAPIController;
import com.jjkeller.kmbapi.controller.share.KmbApplicationException;
import com.jjkeller.kmbapi.controller.share.MandateObjectFactory;
import com.jjkeller.kmbapi.controller.share.SelectedRecord;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.eldmandate.EventDataDiagnosticsChecker;
import com.jjkeller.kmbapi.employeelogeldevents.Enums;
import com.jjkeller.kmbapi.enums.UnidentifiedEldEventStatus;
import com.jjkeller.kmbapi.geotabengine.GeotabHistoryDownloader;
import com.jjkeller.kmbapi.kmbeobr.UnidentifiedPairedEvents;
import com.jjkeller.kmbapi.proxydata.EldEventAdapterList;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;
import com.jjkeller.kmbui.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class UnidentifiedELDEvents extends BaseActivity implements IUnidentifiedELDEvents.UnidentifiedELDEventsFragActions {

	private static final String TAG = "UnidentifiedELDEvents Activity";

    private static final String BUNDLE_SAVED_SELECTED_KEYS = "savedSelectedKeys";

	private UnidentifiedELDEventsFrag _contentFrag;
	private IAPIController _controllerEmp;
	List<SelectedRecord<EmployeeLogEldEvent>> _adapterEvents;
    List<EmployeeLogEldEvent> claimedEventsToReview;
	List<Date> unclaimedEventDates = new ArrayList<>();
	private boolean _showAllUnsubmitted;
	private boolean _returnToLogout;
	ProgressDialog pd;
    private HashSet<String> saveStateSelectedKeys;
    private UnidentifiedEmployeeEventsListAdapter eventsAdapter;

    @Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.baselayout);

        saveStateSelectedKeys = new HashSet<>();

		_controllerEmp = MandateObjectFactory.getInstance(this, GlobalState.getInstance().getFeatureService()).getCurrentEventController();

		if (savedInstanceState != null) {
			_showAllUnsubmitted = savedInstanceState.getBoolean("showAllUnsubmitted");
            _returnToLogout = savedInstanceState.getBoolean("returnToLogout");
            saveStateSelectedKeys = JsonUtil.getGson().fromJson(savedInstanceState.getString(BUNDLE_SAVED_SELECTED_KEYS, ""), JsonUtil.TYPE_SET_OF_STRINGS);
		}
		else {
			_showAllUnsubmitted = this.getIntent().getBooleanExtra(getString(R.string.parm_unidentifiedeldeventsshowallunsubmitted), false);
            _returnToLogout = this.getIntent().getBooleanExtra(getString(R.string.parm_returntologout), false);
		}

		loadContentFragment(new UnidentifiedELDEventsFrag());

		// kickoff background thread to load data
		mFetchLocalDataTask = new FetchLocalDataTask(this.getClass().getSimpleName());
		mFetchLocalDataTask.execute();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
	}

	@Override
	protected void onPause()
	{
		super.onPause();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putBoolean("showAllUnsubmitted", _showAllUnsubmitted);
        outState.putBoolean("returnToLogout", _returnToLogout);

        if (eventsAdapter != null) {
            outState.putString(BUNDLE_SAVED_SELECTED_KEYS, JsonUtil.getGson().toJson(eventsAdapter.getSelectedKeys()));
        } else {
            outState.putString(BUNDLE_SAVED_SELECTED_KEYS, JsonUtil.getGson().toJson(saveStateSelectedKeys));
        }

		super.onSaveInstanceState(outState);
	}

	@Override
	protected void InitController() { }

	@Override
	/*
	 * Called from BaseActivity FetchLocalDataTask::onPostExecute
	 */
	protected void loadControls() {
		super.loadControls();

		// set the events adapter
        eventsAdapter = new UnidentifiedEmployeeEventsListAdapter(this, _adapterEvents, _contentFrag.getClaimButtonView(), saveStateSelectedKeys);
		_contentFrag.getEventsListView().setAdapter(eventsAdapter);
		_contentFrag.getClaimButtonView().setEnabled(! eventsAdapter.getSelectedKeys().isEmpty());
	}

	@Override
	public void setFragments() {
		super.setFragments();

		// set the UEE fragment which holds the listview displaying all the records
		Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
		_contentFrag = (UnidentifiedELDEventsFrag) f;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		return true;
	}

	@Override
	public void onBackPressed() {
		// We only allow use of the back button when coming from the menu & showing all unsubmitted events
		if (_showAllUnsubmitted) {
			super.onBackPressed();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		return super.onOptionsItemSelected(item);
	}

	@Override
	public String getActivityMenuItemList()
	{
		return getString(R.string.wifi_settings_action_items);
	}

	@Override
	public void onNavItemSelected(int menuItem)
	{
		// DONE is the only action so update log(s) and finish the activity

        for( SelectedRecord selectedRecord : _adapterEvents){
            selectedRecord.setIsSelected(false);
        }

		// kickoff background thread to save data
		mSaveLocalDataTask = new SaveLocalDataTask(this.getClass().getSimpleName(), getString(R.string.uee_mark_as_reviewed));
		mSaveLocalDataTask.execute();

		this.startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
	}

	@Override
	protected void Return(boolean success)
	{
		PerformUnidentifiedDrivingDataDiagnosticTest();

		if (success) {

			// upon Bluetooth reconnect, determine if engine cycle's claimed while disconnected that would end Special Driving (Yard Move or Personal Conveyance)
			// user must Claim engine cycle's because even though the cycle's occurred probably with the same ELD, we can't assume until the driver claims them
			if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled() && !_showAllUnsubmitted /*not initiated from System Menu*/) {
				if (GlobalState.getInstance().getIsInYardMoveDrivingSegment()) {
					GlobalState.getInstance().getEobrService().getBluetoothDrivingManager().onUnidentifiedEldEventsClaimed(GlobalState.getInstance().getIsInYardMoveDrivingSegment(), GlobalState.getInstance().getIsInPersonalConveyanceDrivingSegment());
					finishActivity();
				}
				else if (GlobalState.getInstance().getIsInPersonalConveyanceDrivingSegment()) {
					// prompt the user to continue in Personal Conveyance
					// need an Activity inorder to create a Dialog so prompt the user here
					final LogEntryController logEntryController = new LogEntryController(getApplicationContext());
					Date lastIgnitionOn = logEntryController.hasIngitionOffOnCycleOccuredSince(GlobalState.getInstance().getEobrService().getBluetoothDrivingManager().getPotentialDrivingStopTimestamp());

					// engine cycle for mandate defines ignition cycle as Off -> ON
					if (lastIgnitionOn != null) {

						DialogInterface.OnClickListener onYesHandler = new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								// End Yard Move
								GlobalState.getInstance().getEobrService().getBluetoothDrivingManager().onUnidentifiedEldEventsClaimed(GlobalState.getInstance().getIsInYardMoveDrivingSegment(), GlobalState.getInstance().getIsInPersonalConveyanceDrivingSegment());
								finishActivity();
							}
						};

						DialogInterface.OnClickListener onNoHandler = new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								// do nothing - remain in Yard Move
								finishActivity();
							}
						};

						this.ShowConfirmationMessage(this, getString(R.string.msg_endpersonalconveyance), onYesHandler, onNoHandler);
					}
					else {
						finishActivity();
					}
				}
				else {
					finishActivity();
				}
			}
			else {
				finishActivity();
			}
		}
		else {
			// refresh the list to remove successful saves or successful IsReviewed - only
			// items left should be Drives that would overlap existing automatic driving period
			eventsAdapter.notifyDataSetChanged();

			// format list of events that cannot be claimed
			StringBuilder sb = new StringBuilder();
			sb.append(this.getString(R.string.claim_unidentified_overlap_automatic_driving)).append('\n').append('\n');

			for (SelectedRecord<EmployeeLogEldEvent> selectedRecordEvent : _adapterEvents) {
				sb.append(selectedRecordEvent.getFormattedDateTime()).append('\n');
			}

			AlertDialog.Builder builder= new AlertDialog.Builder(this);
			builder.setMessage(sb.toString());
			builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
				}
			});
			builder.show();
		}
	}

    private void finishActivity() {
        if (claimedEventsToReview.size() > 0) {
            String eventsJson = JsonUtil.getGson().toJson(claimedEventsToReview);

            Bundle bundle = new Bundle();
            bundle.putString(UnidentifiedELDEventsReview.BUNDLE_EVENTS_TO_REVIEW, eventsJson);
            bundle.putString(UnidentifiedELDEventsReview.BUNDLE_REVIEW_HEADER, getString(R.string.unidentified_eld_events_review_header));
            bundle.putString(UnidentifiedELDEventsReview.BUNDLE_REVIEW_INSTRUCTIONS, getString(R.string.unidentified_eld_events_review_instructions));
            bundle.putString(UnidentifiedELDEventsReview.BUNDLE_FROM_SCREEN, "UNIDENTIFIED");

            startActivity(UnidentifiedELDEventsReview.class, bundle);
            finish();
        } else {
            //If there are no events selected to review, then start go back to RODS.
            this.startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
            finish();
        }
    }

    /**
	 * A list adapter for the unidentified ELD events
	 */
	private static class UnidentifiedEmployeeEventsListAdapter extends ArrayAdapter<SelectedRecord<EmployeeLogEldEvent>>
	{
		Context _context;
		private Button claimButton;

        private HashSet<String> selectedKeys;

		public UnidentifiedEmployeeEventsListAdapter(Context context, List<SelectedRecord<EmployeeLogEldEvent>> events, Button button, HashSet<String> selectedKeys)
		{
			super(context, R.layout.unidentifiedemployeeevent_list_item, events);
			_context = context;
			this.claimButton = button;
            this.selectedKeys = selectedKeys;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			ViewHolder holder;

			if (convertView == null)
			{
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.unidentifiedemployeeevent_list_item, parent, false);

				holder = new ViewHolder();
				holder.dateTimeView = (TextView) convertView.findViewById(R.id.uee_item_datetime);
				holder.eventTypeView = (TextView) convertView.findViewById(R.id.uee_item_eventtype);
				holder.unitNumberView = (TextView) convertView.findViewById(R.id.uee_item_unitnumber);
				holder.distanceView = (TextView) convertView.findViewById(R.id.uee_item_distance);
				holder.claim = (CheckBox) convertView.findViewById(R.id.uee_cbo_claim);
				holder.claim.setOnClickListener( new View.OnClickListener() {
					public void onClick(View v) {
						CheckBox cb = (CheckBox) v ;
						SelectedRecord record = (SelectedRecord) cb.getTag();
						EmployeeLogEldEvent evt = (EmployeeLogEldEvent)record.getRecord();
						record.setIsSelected(cb.isChecked());
						if(cb.isChecked()){
							selectedKeys.add(String.valueOf(evt.getPrimaryKey()));
						}else{
							selectedKeys.remove(String.valueOf(evt.getPrimaryKey()));
						}
						claimButton.setEnabled(selectedKeys.size()>0);
					}
				});

				convertView.setTag(holder);
			}else {
				holder = (ViewHolder) convertView.getTag();
			}

			SelectedRecord record = getItem(position);
			EmployeeLogEldEvent event = (EmployeeLogEldEvent) record.getRecord();

			holder.dateTimeView.setText(record.getFormattedDateTime());
			holder.eventTypeView.setText(event.getCompositeEventCodeType(event.getEventType(),event.getEventCode()));
			boolean isSelected = selectedKeys.contains(String.valueOf(event.getPrimaryKey()));
			holder.claim.setChecked(isSelected);
			record.setIsSelected(isSelected);
			holder.claim.setTag(record);

			try{
				holder.unitNumberView.setText(event.getTractorNumber().toString());
			}catch(Exception ex){
				holder.unitNumberView.setText("Unavailable");
			}

			holder.distanceView.setText(record.getFormattedDistance(this.getContext()));

			return convertView;
		}

        public HashSet<String> getSelectedKeys(){
            return selectedKeys;
        }

        public void setSelectedKeys(HashSet<String> selectedKeys) {
			this.selectedKeys = selectedKeys;
		}

		static class ViewHolder {
			private TextView dateTimeView;
			private TextView eventTypeView;
			private TextView unitNumberView;
			private TextView distanceView;
			private CheckBox claim;
		}

	}

	/**
	 * Claim button click
	 */
	public void handleClaimButtonClicked(View view)
	{

		// still need to kickoff background thread to save data as reviewed
		mSaveLocalDataTask = new SaveLocalDataTask(this.getClass().getSimpleName());
		mSaveLocalDataTask.execute();
	}

	/**
	 * SelectAll/UnselectAll checkbox click
	 */
	public void handleSelectAllClicked(View view, boolean checked)
	{
		// disable master Select All to prevent fast fingers
		view.setEnabled(false);

		HashSet<String> selectedKeys = new HashSet<>();

		for (int i = 0; i < _adapterEvents.size(); i++) {
			SelectedRecord<EmployeeLogEldEvent> item = _adapterEvents.get(i);
			item.setIsSelected(checked);

			if (checked) {
				selectedKeys.add(String.valueOf(item.getRecord().getPrimaryKey()));
			}
		}

		eventsAdapter.setSelectedKeys(selectedKeys);

		// refresh the list to reflect IsSelected state
		eventsAdapter.notifyDataSetChanged();

		// enable/disable Claim button if anything is selected
		_contentFrag.getClaimButtonView().setEnabled(!selectedKeys.isEmpty());

		view.setEnabled(true);
	}

	/**
	 * Called from BaseActivity FetchLocalDataTask so data is loaded on AsyncTask
	 */
	@Override
	protected void loadData() {

		if (GlobalState.getInstance().getCompanyConfigSettings(getApplicationContext()).getIsGeotabEnabled() && _showAllUnsubmitted) {
			try {
				GeotabHistoryDownloader _geotabHistoryDownloader = new GeotabHistoryDownloader(getApplicationContext(), GlobalState.getInstance().getAppSettings(getApplicationContext()),  GlobalState.getInstance().getCurrentEobrSerialNumber());
				//Setting downloaded History to Reviewed due to the way this screen selects the Unidentified Events query when viewed for the menu
				_geotabHistoryDownloader.downloadHistory(true);

			} catch (KmbApplicationException e) {
				Log.e("GeoTab", e.getMessage(), e);
			}
		}

		// query Unidentified Events and pair the Drive and corresponding end of drive OnDuty
        List<UnidentifiedPairedEvents> unidentifiedEventPairs = _controllerEmp.LoadUnidentifiedEldEventPairs(_showAllUnsubmitted);

        _adapterEvents = new ArrayList<>();

        for (UnidentifiedPairedEvents u : unidentifiedEventPairs){
			// Add the events to Claim to the list adapter presented to the user to see
			_adapterEvents.add(new SelectedRecord<>(u.startEvent, u.endEvent));
        }
	}

	/**
	 * SaveLocalDataTask doInBackground call to save data.
	 */
	@Override
	protected boolean saveData() {
        GlobalState globalState = GlobalState.getInstance();

        EmployeeLogEldMandateController mandateController = ControllerFactory.getInstance().getEmployeeLogEldMandateController();
        User currentUser = globalState.getCurrentUser();
        EmployeeLogEldEventFacade eventFacade = new EmployeeLogEldEventFacade(this, globalState.getCurrentUser());

        claimedEventsToReview = new LinkedList<>();
		List<SelectedRecord<EmployeeLogEldEvent>> eventsThatWillInvalidateAutomaticDriveTimeWithSameEobrSerialNumber = new ArrayList<>();

		// events to Claim are displayed in the List as the most recent to oldest -- but when actually saving the data, we want to process them
		// in the Date order they occurred, so iterate in reverse so we save oldest to newest
		for (int i = _adapterEvents.size() - 1; i >= 0; i--) {
			SelectedRecord<EmployeeLogEldEvent> selectedRecordEvent = _adapterEvents.get(i);

			EmployeeLogEldEvent event = selectedRecordEvent.getRecord();
            EmployeeLogEldEvent endEvent = selectedRecordEvent.getEndEvent();

			List<EmployeeLogEldEvent> savedEvents = new ArrayList<EmployeeLogEldEvent>();
            try {
				if (selectedRecordEvent.getIsSelected()) {
                    if (endEvent != null) {
                        Date endDateTime = endEvent.getStartTime();
                        List<EmployeeLogEldEvent> returnEvent = mandateController.assignEldEventToUser(event, endDateTime, currentUser);
                        if (returnEvent.size() == 1 && returnEvent.get(0) == null) {
                            // assignEldEventToUser returns null if Claiming will overlap automatically previously recorded driving time
							eventsThatWillInvalidateAutomaticDriveTimeWithSameEobrSerialNumber.add(selectedRecordEvent);
                            continue;
                        } else {
                            savedEvents.addAll(returnEvent);
                        }

                        boolean shouldAddEndEvent = true;
                        EmployeeLog empLog = mandateController.GetLocalEmployeeLog(currentUser, endEvent.getEventDateTime());
                        if (empLog != null) {
                            EmployeeLogEldEvent[] logEventArray = empLog.getEldEventList().getActiveEldEventList(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus);
                            for (EmployeeLogEldEvent eventInLog : logEventArray){
								// set the endEvent event code/type with the last known code/type in the EmployeeLog (make sure it's a dutystatuschange and NOT driving(3), otherwise keep default
								if(eventInLog.getEventCode() != 3
										&& eventInLog.getEventDateTime().compareTo(endEvent.getEventDateTime()) == -1){
									endEvent.setEventCode(eventInLog.getEventCode());
									endEvent.setEventType(eventInLog.getEventType());
								}
                                if (eventInLog.getEventDateTime().equals(endEvent.getEventDateTime())){
                                    shouldAddEndEvent = false;
                                }
                            }
                        }

                        if (shouldAddEndEvent) {
                            savedEvents.addAll(mandateController.assignEldEventToUser(endEvent, null, currentUser));
                        } else {
                            endEvent.setIsReviewed(true);
                            endEvent.setUnidentifiedEventStatus(UnidentifiedEldEventStatus.CLAIMED);
                            endEvent.setEventRecordStatus(Enums.EmployeeLogEldEventRecordStatus.InactiveChanged.getValue());
                            eventFacade.Update(endEvent);
                        }

						if (savedEvents.size() == 2
								&& savedEvents.get(0) == null
								&& savedEvents.get(1).getEventDateTime().compareTo(endEvent.getEventDateTime()) == 0) {
							event.setEventRecordStatus(Enums.EmployeeLogEldEventRecordStatus.InactiveChanged.getValue());
							event.setUnidentifiedEventStatus(UnidentifiedEldEventStatus.CLAIMED);
							eventFacade.Update(event);
						}
                    } else {
						savedEvents.addAll(mandateController.assignEldEventToUser(event, null, currentUser));
                    }
					for(EmployeeLogEldEvent savedEvent : savedEvents){
						if (hasMissingData(savedEvent) && savedEvent.getEventRecordStatus() != Enums.EmployeeLogEldEventRecordStatus.InactiveChanged.getValue()){
							claimedEventsToReview.add(savedEvent);
						}
					}
                    selectedRecordEvent.setIsSelected(false);
                } else {
					unclaimedEventDates.add(event.getEventDateTime());
                    event.setIsReviewed(true);
                    eventFacade.Update(event);
                    if (endEvent != null){
                        endEvent.setIsReviewed(true);
                        eventFacade.Update(endEvent);
					}
                }

            } catch (Throwable throwable) {
                LogCat.getInstance().e(TAG, "Error updating Unidentified Employee Event(s)", throwable);
                return false;
            }
        }

		// Inactivate Unidentified Drive and corresponding end OnDuty events that would invalidate existing Automatic Drive events with the SAME EobrSerialNumber
		if (!eventsThatWillInvalidateAutomaticDriveTimeWithSameEobrSerialNumber.isEmpty()) {

			EmployeeLogEldEvent[] overlapEventsToInvalidate = new EmployeeLogEldEvent[eventsThatWillInvalidateAutomaticDriveTimeWithSameEobrSerialNumber.size() * 2];	// single array entry contains BOTH Drive and corresponding OnDuty event

			// Inactivate the Drive event
			int nextEntryIndex = 0;
			Date logRemarkDateTime = TimeKeeper.getInstance().getCurrentDateTime().toDate();
			for (int i = 0; i < eventsThatWillInvalidateAutomaticDriveTimeWithSameEobrSerialNumber.size(); i++) {
				SelectedRecord<EmployeeLogEldEvent> selectedRecord = eventsThatWillInvalidateAutomaticDriveTimeWithSameEobrSerialNumber.get(i);

				overlapEventsToInvalidate[nextEntryIndex] = selectedRecord.getRecord();	// Drive event
				overlapEventsToInvalidate[nextEntryIndex].setEventRecordStatus(EmployeeLogEldEvent.RECORD_STATUS_INACTIVE_CHANGED);
				overlapEventsToInvalidate[nextEntryIndex].setLogRemarkDateTime(logRemarkDateTime);
				overlapEventsToInvalidate[nextEntryIndex].setLogRemark(getString(R.string.unidentified_inactivated_because_overlaps_automatic_drive));
				nextEntryIndex++;

				// Inactivate the OnDuty event
				overlapEventsToInvalidate[nextEntryIndex] = selectedRecord.getEndEvent();	// OnDuty event
				overlapEventsToInvalidate[nextEntryIndex].setEventRecordStatus(EmployeeLogEldEvent.RECORD_STATUS_INACTIVE_CHANGED);
				overlapEventsToInvalidate[nextEntryIndex].setLogRemarkDateTime(logRemarkDateTime);
				overlapEventsToInvalidate[nextEntryIndex].setLogRemark(getString(R.string.unidentified_inactivated_because_overlaps_automatic_drive));
				nextEntryIndex++;
			}

			eventFacade.SaveListInSingleTransaction(overlapEventsToInvalidate);
		}

		return true;
	}

	private void PerformUnidentifiedDrivingDataDiagnosticTest(){

		if(!_showAllUnsubmitted)
		{
			//only need to run after reading history.
			if(unclaimedEventDates.size() > 0){
				Collections.sort(unclaimedEventDates);
				//Per mandate, test to see if we need to add a unidentified driving data diagnostic event. Test needs to happen after any unidentified events are added.
				EmployeeLogEldMandateController mandateController = ControllerFactory.getInstance().getEmployeeLogEldMandateController();
				mandateController.TrySetUnidentifiedDrivingTimeDataDiagnosticEvent(unclaimedEventDates.get(0));
			}
		}else{
			//Per mandate, test to see if we need to remove an unidentified driving data diagnostic event.
			EmployeeLogEldMandateController mandateController = ControllerFactory.getInstance().getEmployeeLogEldMandateController();
			mandateController.TryClearUnidentifiedDrivingTimeDataDiagnosticEvent();
		}
	}

	private static boolean hasMissingData(EmployeeLogEldEvent event){
		if (event == null){
			return false;
		}

		// do not need to check for missing data on PowerUp/Down events
		if (event.getEventType() == Enums.EmployeeLogEldEventType.EnginePowerUpPowerDown) {
			return false;
		}

		return (new EventDataDiagnosticsChecker().new DutyStatusChangeChecker().isDriversLocationDescriptionMissing(event));
	}
}