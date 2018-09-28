package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.widget.Button;
import android.widget.TimePicker;

import com.jjkeller.kmb.interfaces.IBaseDialogFragment.BaseDialogFragmentActions;
import com.jjkeller.kmbapi.common.TimeKeeper;
import com.jjkeller.kmbapi.controller.utility.DateUtility;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

public class TimePickerDialogFrag extends DialogFragment {

	BaseDialogFragmentActions _baseListener;
    Button mTimeDialogButton;     
    public TimePickerDialogFrag(){ }       
    public TimePickerDialogFrag(Button btn){
    	mTimeDialogButton = btn;
    }       
    
    private TimePickerDialog.OnTimeSetListener mTimeSetListener =
        new TimePickerDialog.OnTimeSetListener() {
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            	Calendar cal = Calendar.getInstance();
            	cal.setTimeZone(DateUtility.getHomeTerminalDateFormat().getTimeZone());
            	cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
            	cal.set(Calendar.MINUTE, minute);
                updateTimeDisplay(cal);
                _baseListener.UnlockScreenRotation();
                TimePickerDialogFrag.this.dismiss();
            }
        };

    public Dialog onCreateDialog(Bundle savedInstanceState){
		if(mTimeDialogButton != null)
		{
			Date valTimeDialog = TimeKeeper.getInstance().now();
			try {
				valTimeDialog = DateUtility.getHomeTerminalTime12HourFormat().parse(mTimeDialogButton.getText().toString());
			} catch (ParseException e) {
				
	        	Log.e("UnhandledCatch", e.getMessage() + ": " + Log.getStackTraceString(e));
			}

			Calendar cal = Calendar.getInstance();
			cal.setTimeZone(DateUtility.getHomeTerminalDateFormat().getTimeZone());
			cal.setTime(valTimeDialog);
			
			_baseListener.LockScreenRotation();
			TimePickerDialog dialog = new TimePickerDialog(getActivity(),mTimeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false);
			
			// Add Cancel button logic to unlock screen
			dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog, int which)
			    {
			        if (which == DialogInterface.BUTTON_NEGATIVE)
			        {
			        	_baseListener.UnlockScreenRotation();
		                TimePickerDialogFrag.this.dismiss();
			        }
			    }
			});
			
			return dialog;
		}
		return new TimePickerDialog(getActivity(), mTimeSetListener, 0, 0, false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
        	_baseListener = (BaseDialogFragmentActions) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement BaseFragmentActions");
        }
	}
    protected void updateTimeDisplay(Calendar c) {
		_baseListener.updateTimeDisplay(mTimeDialogButton, c);
    }
}
