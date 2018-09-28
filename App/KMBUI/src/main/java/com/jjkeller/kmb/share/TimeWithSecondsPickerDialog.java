package com.jjkeller.kmb.share;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;

import android.view.View;
import android.view.Window;

import com.jjkeller.kmbui.R;

public class TimeWithSecondsPickerDialog extends AlertDialog implements OnClickListener {
	public interface OnTimeWithSecondsSetListener {
		void onTimeWithSecondsSet(TimeWithSecondsPicker view, int hourOfDay, int minute, int seconds);
	}

	private static final int POSITIVE_BUTTON = BUTTON_POSITIVE;
	private static final int NEGATIVE_BUTTON = BUTTON_NEGATIVE;

	private static final String HOUR = "hour";
	private static final String MINUTE = "minute";
	private static final String SECONDS = "seconds";
	private static final String IS_24_HOUR = "is24hour";

	private final TimeWithSecondsPicker mTimeWithSecondsPicker;
	private final OnTimeWithSecondsSetListener mCallback;
	private final Calendar mCalendar;
	private final java.text.DateFormat mDateFormat;

	int mInitialHourOfDay;
	int mInitialMinute;
	int mInitialSeconds;
	boolean mIs24HourView;

	public TimeWithSecondsPickerDialog(Context context, OnTimeWithSecondsSetListener callBack, int hourOfDay, int minute, int seconds, boolean is24HourView) {
		super(context);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		mCallback = callBack;
		mInitialHourOfDay = hourOfDay;
		mInitialMinute = minute;
		mInitialSeconds = seconds;
		mIs24HourView = is24HourView;

		mDateFormat = new SimpleDateFormat("hh:mm:ss aa");
		mCalendar = Calendar.getInstance();

		setButton(POSITIVE_BUTTON, context.getText(R.string.btnok), this);
		setButton(NEGATIVE_BUTTON, context.getText(R.string.cancellabel), (OnClickListener) null);
		//setIcon(android.R.drawable.ic_dialog_time);

		View view = getLayoutInflater().inflate(R.layout.time_with_seconds_picker_dialog, null);
		setView(view);
		mTimeWithSecondsPicker = (TimeWithSecondsPicker) view.findViewById(R.id.timePicker);

		// initialize state
		mTimeWithSecondsPicker.setCurrentHour(mInitialHourOfDay);
		mTimeWithSecondsPicker.setCurrentMinute(mInitialMinute);
		mTimeWithSecondsPicker.setCurrentSecond(mInitialSeconds);
		mTimeWithSecondsPicker.setIs24HourView(mIs24HourView);
	}

	public void onClick(DialogInterface dialog, int which) {
		if (mCallback != null) {
			mTimeWithSecondsPicker.clearFocus();
			mCallback.onTimeWithSecondsSet(mTimeWithSecondsPicker, mTimeWithSecondsPicker.getCurrentHour(), mTimeWithSecondsPicker.getCurrentMinute(), mTimeWithSecondsPicker.getCurrentSeconds());
		}
	}

	@Override
	public Bundle onSaveInstanceState()
	{
		Bundle bundle = super.onSaveInstanceState();
		bundle.putInt(HOUR, mTimeWithSecondsPicker.getCurrentHour());
		bundle.putInt(MINUTE, mTimeWithSecondsPicker.getCurrentMinute());
		bundle.putInt(SECONDS, mTimeWithSecondsPicker.getCurrentSeconds());
		bundle.putBoolean(IS_24_HOUR, mTimeWithSecondsPicker.is24HourView());
		return bundle;
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState)
	{
		super.onRestoreInstanceState(savedInstanceState);
		int hour = savedInstanceState.getInt(HOUR);
		int minute = savedInstanceState.getInt(MINUTE);
		int seconds = savedInstanceState.getInt(SECONDS);
		mTimeWithSecondsPicker.setCurrentHour(hour);
		mTimeWithSecondsPicker.setCurrentMinute(minute);
		mTimeWithSecondsPicker.setCurrentSecond(seconds);
		mTimeWithSecondsPicker.setIs24HourView(savedInstanceState.getBoolean(IS_24_HOUR));
	}
}