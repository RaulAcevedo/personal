package com.jjkeller.kmb.share;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.Button;

import com.jjkeller.kmb.interfaces.IBaseFragment.BaseFragmentActions;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.share.KmbApplicationException;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.controller.utility.EmployeeLogUtilities;
import com.jjkeller.kmbapi.employeelogeldevents.EmployeeLogEldEventCode;
import com.jjkeller.kmbapi.kmbeobr.EventTypeEnum;
import com.jjkeller.kmbapi.proxydata.EldEventAdapterList;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;

import java.util.Calendar;

public abstract class BaseFragment extends Fragment {

	BaseFragmentActions _baseListener;
	
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
        	_baseListener = (BaseFragmentActions) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement BaseFragmentActions");
        }
	}
    
	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		this.setFragments();
	}
	
    protected void setFragments()
    {
    	_baseListener.setFragments();
    }
    
    protected void startActivity(Class<?> c, Bundle extras)
    {
    	_baseListener.handleActivityStart(c, extras);
    }
    
    protected void updateTimeDisplay(Button timeDialogButton, Calendar c) {
    	timeDialogButton.setText(DateUtility.getHomeTerminalTime12HourFormat().format(c.getTime()));
    }

    protected void updateDateDisplay(Button dateDialogButton, Calendar c) {
    	dateDialogButton.setText(DateUtility.getHomeTerminalDateFormat().format(c.getTime()));
    }

   public void ShowMessage(String message)
    {
    	((BaseActivity)getActivity()).ShowMessage(getActivity(), message);
    }

	protected void ShowToastMessage(String message)
	{
		((BaseActivity)getActivity()).showMsg(message);
	}

	protected void ShowTimePickerDialog(Button btn)
	{
		((BaseActivity)getActivity()).ShowTimePickerDialog(btn);
	}

	protected void ShowTimeWithSecondsPickerDialog(Button btn)
	{
		((BaseActivity)getActivity()).ShowTimeWithSecondsPickerDialog(btn);
	}

	protected void ShowDatePickerDialog(Button btn)
	{
		((BaseActivity)getActivity()).ShowDatePickerDialog(btn);
	}

	public void HandleFragmentException(KmbApplicationException kae)
	{
		((BaseActivity)getActivity()).HandleException(kae);
	}
	
	public void ToggleDOTClocks_NightMode()
	{
		BaseActivity.ToggleDOTClocks_NightMode();
	}
	
	protected boolean getDOTClocks_NightMode()
	{
		return BaseActivity.getDOTClocks_NightMode();
	}

	protected boolean getIsExemptFromELDUse()
	{
		EmployeeLog currentlog = GlobalState.getInstance().getCurrentEmployeeLog();
		boolean isMandate = GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled();
		return currentlog != null && isMandate ? currentlog.getIsExemptFromELDUse() : false;
	}

	protected String getLastEnteredManualLocation(EmployeeLog currentlog)
	{
		if(currentlog != null)
		{
			EmployeeLogEldEvent lastDutyStatusEventInLog = EmployeeLogUtilities.GetLastEventInLog(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus, currentlog);

			if(lastDutyStatusEventInLog != null)
			{
				return lastDutyStatusEventInLog.getEventCode() != EmployeeLogEldEventCode.DutyStatus_Driving ? lastDutyStatusEventInLog.getDriversLocationDescription() : null;
			}
		}

		return null;
	}
}
