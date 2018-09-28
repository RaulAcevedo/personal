package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.widget.Button;
import android.widget.DatePicker;

import com.jjkeller.kmb.interfaces.IBaseDialogFragment.BaseDialogFragmentActions;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.utility.DateUtility;

import java.text.ParseException;
import java.util.Calendar;

public class DatePickerDialogFrag extends DialogFragment{

	BaseDialogFragmentActions _baseListener;
	Button mDateDialogButton;
	public DatePickerDialogFrag(){}
	public DatePickerDialogFrag(Button btn){
		mDateDialogButton = btn;
	}
	
	private DatePickerDialog.OnDateSetListener mDateSetListener = 
			new DatePickerDialog.OnDateSetListener() {				
				public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
					Calendar cal = Calendar.getInstance();
					cal.setTime(DateUtility.CurrentHomeTerminalTime(GlobalState.getInstance().getCurrentUser()));
					cal.set(Calendar.YEAR, year);
					cal.set(Calendar.MONTH, monthOfYear);
					cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
					updateDateDisplay(cal);
	                _baseListener.UnlockScreenRotation();
	                DatePickerDialogFrag.this.dismiss();
				}
			};
	
	public Dialog onCreateDialog(Bundle savedInstanceState){
		if(mDateDialogButton != null){
			Calendar valDateDialog = Calendar.getInstance();
			try {
				valDateDialog.setTime(DateUtility.getDateFormat().parse(mDateDialogButton.getText().toString()));
			} catch (ParseException e) {
				// Allow dialog to display default time if parse exception occurs
				
	        	Log.e("UnhandledCatch", e.getMessage() + ": " + Log.getStackTraceString(e));
			}	
			
			_baseListener.LockScreenRotation();
			DatePickerDialog dialog = new DatePickerDialog(getActivity(), mDateSetListener, 
					valDateDialog.get(Calendar.YEAR), valDateDialog.get(Calendar.MONTH), valDateDialog.get(Calendar.DAY_OF_MONTH));
			
			// Add Cancel button logic to unlock screen
			dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog, int which)
			    {
			        if (which == DialogInterface.BUTTON_NEGATIVE)
			        {
			        	_baseListener.UnlockScreenRotation();
			        	DatePickerDialogFrag.this.dismiss();
			        }
			    }
			});
			
			return dialog;
		}		
		return new DatePickerDialog(getActivity(), mDateSetListener, 0, 0, 0);
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
    
	protected void updateDateDisplay(Calendar c) {
		_baseListener.updateDateDisplay(mDateDialogButton, c);
	}
}