package com.jjkeller.kmb.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.widget.Button;

import com.jjkeller.kmb.interfaces.IBaseDialogFragment.BaseDialogFragmentActions;
import com.jjkeller.kmb.share.TimeWithSecondsPicker;
import com.jjkeller.kmbapi.common.TimeKeeper;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmb.share.TimeWithSecondsPickerDialog;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeWithSecondsPickerDialogFrag extends DialogFragment {

	BaseDialogFragmentActions _baseListener;
    Button mTimeDialogButton;
    public TimeWithSecondsPickerDialogFrag(){ }
    @SuppressLint("ValidFragment")
	public TimeWithSecondsPickerDialogFrag(Button btn){
    	mTimeDialogButton = btn;
    }

    private TimeWithSecondsPickerDialog.OnTimeWithSecondsSetListener mTimeWithSecondsSetListener =
        new TimeWithSecondsPickerDialog.OnTimeWithSecondsSetListener() {
            public void onTimeWithSecondsSet(TimeWithSecondsPicker view, int hourOfDay, int minute, int second) {
            	Calendar cal = Calendar.getInstance();
            	cal.setTimeZone(DateUtility.getHomeTerminalDateFormat().getTimeZone());
            	cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
            	cal.set(Calendar.MINUTE, minute);
				cal.set(Calendar.SECOND, second);
                updateTimeDisplay(cal);
                _baseListener.UnlockScreenRotation();
                TimeWithSecondsPickerDialogFrag.this.dismiss();
            }
        };

    public Dialog onCreateDialog(Bundle savedInstanceState){
		if(mTimeDialogButton != null)
		{

			Date valTimeDialog = TimeKeeper.getInstance().midnight();
			try {
				valTimeDialog = DateUtility.getHomeTerminalTime12HourFormatWithSeconds().parse(mTimeDialogButton.getText().toString());
			} catch (ParseException e) {
				
	        	Log.e("UnhandledCatch", e.getMessage() + ": " + Log.getStackTraceString(e));
			}

			Calendar cal = Calendar.getInstance();
			cal.setTimeZone(DateUtility.getHomeTerminalDateFormat().getTimeZone());
			cal.setTime(valTimeDialog);
			
			_baseListener.LockScreenRotation();
			TimeWithSecondsPickerDialog dialog = new TimeWithSecondsPickerDialog(getActivity(),mTimeWithSecondsSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND), false);
			
			// Add Cancel button logic to unlock screen
			dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog, int which)
			    {
			        if (which == DialogInterface.BUTTON_NEGATIVE)
			        {
			        	_baseListener.UnlockScreenRotation();
		                TimeWithSecondsPickerDialogFrag.this.dismiss();
			        }
			    }
			});
			
			return dialog;
		}
		return new TimeWithSecondsPickerDialog(getActivity(), mTimeWithSecondsSetListener, 0, 0, 0, false);
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
