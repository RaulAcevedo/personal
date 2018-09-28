package com.jjkeller.kmb.interfaces;

import android.widget.Button;

import java.util.Calendar;


public interface IBaseDialogFragment {
    public interface BaseDialogFragmentActions {
        public abstract void LockScreenRotation();
        public abstract void UnlockScreenRotation();
        public void updateDateDisplay(Button dateDialogButton, Calendar c);
        public void updateTimeDisplay(Button timeDialogButton, Calendar c);
    }
}
