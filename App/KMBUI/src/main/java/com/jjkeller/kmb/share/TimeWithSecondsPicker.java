package com.jjkeller.kmb.share;

import java.text.DateFormatSymbols;
import java.util.Calendar;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.NumberPicker;
import android.widget.NumberPicker.Formatter;

import com.jjkeller.kmbui.R;

public class TimeWithSecondsPicker extends FrameLayout {

    /**
     * A no-op callback used in the constructor to avoid null checks
     * later in the code.
     */
    private static final OnTimeChangedListener NO_OP_CHANGE_LISTENER = new OnTimeChangedListener() {
        public void onTimeChanged(TimeWithSecondsPicker view, int hourOfDay, int minute, int seconds) {
        }
    };

    public static final Formatter TWO_DIGIT_FORMATTER =
            new Formatter() {

                @Override
                public String format(int value) {
                    return String.format("%02d", value);
                }
            };

    private int mCurrentHour = 0; // 0-23
    private int mCurrentMinute = 0; // 0-59
    private int mCurrentSeconds = 0; // 0-59
    private Boolean mIs24HourView = false;
    private boolean mIsAm;

    private final NumberPicker mHourPicker;
    private final NumberPicker mMinutePicker;
    private final NumberPicker mSecondPicker;
    private final Button mAmPmButton;
    private final String mAmText;
    private final String mPmText;

    private OnTimeChangedListener mOnTimeChangedListener;

    /**
     * The callback interface used to indicate the time has been adjusted.
     */
    public interface OnTimeChangedListener {

        /**
         * @param view The view associated with this listener.
         * @param hourOfDay The current hour.
         * @param minute The current minute.
         * @param seconds The current second.
         */
        void onTimeChanged(TimeWithSecondsPicker view, int hourOfDay, int minute, int seconds);
    }

    public TimeWithSecondsPicker(Context context) {
        this(context, null);
    }

    public TimeWithSecondsPicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimeWithSecondsPicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.time_with_seconds_picker, this, true);

        // hour
        mHourPicker = (NumberPicker)findViewById(R.id.hour);
        mHourPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {

            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                mCurrentHour = newVal;
                if (!mIs24HourView) {
                    // adjust from [1-12] to [0-11] internally, with the times
                    // written "12:xx" being the start of the half-day
                    if (mCurrentHour == 12) {
                        mCurrentHour = 0;
                    }
                    if (!mIsAm) {
                        // PM means 12 hours later than nominal
                        mCurrentHour += 12;
                    }
                }
                onTimeChanged();
            }
        });

        // digits of minute
        mMinutePicker = (NumberPicker)findViewById(R.id.minute);
        mMinutePicker.setMinValue(0);
        mMinutePicker.setMaxValue(59);
        mMinutePicker.setFormatter(TWO_DIGIT_FORMATTER);
        mMinutePicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker spinner, int oldVal, int newVal) {
                mCurrentMinute = newVal;
                onTimeChanged();
            }
        });

        // digits of seconds
        mSecondPicker = (NumberPicker) findViewById(R.id.seconds);
        mSecondPicker.setMinValue(0);
        mSecondPicker.setMaxValue(59);
        mSecondPicker.setFormatter( TWO_DIGIT_FORMATTER);
        mSecondPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {

            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                mCurrentSeconds = newVal;
                onTimeChanged();
            }
        });

        // am/pm
        mAmPmButton = (Button) findViewById(R.id.amPm);

        // now that the hour/minute picker objects have been initialized, set
        // the hour range properly based on the 12/24 hour display mode.
        configurePickerRanges();

        // initialize to current time
        Calendar cal = Calendar.getInstance();
        setOnTimeChangedListener(NO_OP_CHANGE_LISTENER);

        // by default we're not in 24 hour mode
        setCurrentHour(cal.get(Calendar.HOUR_OF_DAY));
        setCurrentMinute(cal.get(Calendar.MINUTE));
        setCurrentSecond(cal.get(Calendar.SECOND));

        mIsAm = (mCurrentHour < 12);

        /* Get the localized am/pm strings and use them in the spinner */
        DateFormatSymbols dfs = new DateFormatSymbols();
        String[] dfsAmPm = dfs.getAmPmStrings();
        mAmText = dfsAmPm[Calendar.AM];
        mPmText = dfsAmPm[Calendar.PM];
        mAmPmButton.setText(mIsAm ? mAmText : mPmText);
        mAmPmButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                requestFocus();
                if (mIsAm) {

                    // Currently AM switching to PM
                    if (mCurrentHour < 12) {
                        mCurrentHour += 12;
                    }
                } else {

                    // Currently PM switching to AM
                    if (mCurrentHour >= 12) {
                        mCurrentHour -= 12;
                    }
                }
                mIsAm = !mIsAm;
                mAmPmButton.setText(mIsAm ? mAmText : mPmText);
                onTimeChanged();
            }
        });

        if (!isEnabled()) {
            setEnabled(false);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        mMinutePicker.setEnabled(enabled);
        mHourPicker.setEnabled(enabled);
        mSecondPicker.setEnabled(enabled);
        mAmPmButton.setEnabled(enabled);
    }

    private static class SavedState extends BaseSavedState {

        private final int mHour;
        private final int mMinute;
        private final int mSecond;

        private SavedState(Parcelable superState, int hour, int minute, int second) {
            super(superState);
            mHour = hour;
            mMinute = minute;
            mSecond = second;
        }

        private SavedState(Parcel in) {
            super(in);
            mHour = in.readInt();
            mMinute = in.readInt();
            mSecond = in.readInt();
        }

        public int getHour() {
            return mHour;
        }

        public int getMinute() {
            return mMinute;
        }

        public int getSecond() {
            return mSecond;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(mHour);
            dest.writeInt(mMinute);
            dest.writeInt(mSecond);
        }

        public static final Creator<SavedState> CREATOR
                = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        return new SavedState(superState, mCurrentHour, mCurrentMinute, mCurrentSeconds);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setCurrentHour(ss.getHour());
        setCurrentMinute(ss.getMinute());
        setCurrentSecond(ss.getSecond());
    }

    public void setOnTimeChangedListener(OnTimeChangedListener onTimeChangedListener) {
        mOnTimeChangedListener = onTimeChangedListener;
    }

    public Integer getCurrentHour() {
        return mCurrentHour;
    }

    public void setCurrentHour(Integer currentHour) {
        this.mCurrentHour = currentHour;
        updateHourDisplay();
    }

    public void setIs24HourView(Boolean is24HourView) {
        if (mIs24HourView != is24HourView) {
            mIs24HourView = is24HourView;
            configurePickerRanges();
            updateHourDisplay();
        }
    }

    public boolean is24HourView() {
        return mIs24HourView;
    }

    public Integer getCurrentMinute() {
        return mCurrentMinute;
    }

    public void setCurrentMinute(Integer currentMinute) {
        this.mCurrentMinute = currentMinute;
        updateMinuteDisplay();
    }

    public Integer getCurrentSeconds() {
        return mCurrentSeconds;
    }

    public void setCurrentSecond(Integer currentSecond) {
        this.mCurrentSeconds = currentSecond;
        updateSecondsDisplay();
    }

    @Override
    public int getBaseline() {
        return mHourPicker.getBaseline();
    }

    private void updateHourDisplay() {
        int currentHour = mCurrentHour;
        if (!mIs24HourView) {
            // convert [0,23] ordinal to wall clock display
            if (currentHour > 12) currentHour -= 12;
            else if (currentHour == 0) currentHour = 12;
        }
        mHourPicker.setValue(currentHour);
        mIsAm = mCurrentHour < 12;
        mAmPmButton.setText(mIsAm ? mAmText : mPmText);
        onTimeChanged();
    }

    private void configurePickerRanges() {
        if (mIs24HourView) {
            mHourPicker.setMinValue(0);
            mHourPicker.setMaxValue(23);
            mHourPicker.setFormatter(TWO_DIGIT_FORMATTER);
            mAmPmButton.setVisibility(View.GONE);
        } else {
            mHourPicker.setMinValue(1);
            mHourPicker.setMaxValue(12);
            mHourPicker.setFormatter(null);
            mAmPmButton.setVisibility(View.VISIBLE);
        }
    }

    private void onTimeChanged() {
        mOnTimeChangedListener.onTimeChanged(this, getCurrentHour(), getCurrentMinute(), getCurrentSeconds());
    }

    private void updateMinuteDisplay() {
        mMinutePicker.setValue(mCurrentMinute);
        mOnTimeChangedListener.onTimeChanged(this, getCurrentHour(), getCurrentMinute(), getCurrentSeconds());
    }

    private void updateSecondsDisplay() {
        mSecondPicker.setValue(mCurrentSeconds);
        mOnTimeChangedListener.onTimeChanged(this, getCurrentHour(), getCurrentMinute(), getCurrentSeconds());
    }
}
