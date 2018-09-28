package com.jjkeller.kmb.interfaces;

import com.jjkeller.kmbapi.controller.LogEntryController;

import java.util.Date;


public interface ILogout {
    interface LogoutFragActions {
        void handleLogoutButtonClick();
        void handleCancelButtonClick();
        void handleDutyStatusTimeSelect();
        void handleSubmitButtonClick();
    }
    interface LogoutControllerMethods {
        Date getCurrentClockHomeTerminalTime();
        boolean canUseOffDutyWellSite();
        boolean ShouldShowManualLocation();
        LogEntryController getMyLogEntryController();
    }
}
