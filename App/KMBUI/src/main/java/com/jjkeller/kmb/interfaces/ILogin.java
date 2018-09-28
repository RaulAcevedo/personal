package com.jjkeller.kmb.interfaces;

import com.jjkeller.kmbapi.controller.share.User;

import java.util.Date;

public interface ILogin {
    public interface LoginFragActions {
        public void handleLoginSoloButtonClick();
        public void handleLoginTeamButtonClick();
        public void handleCancelButtonClick();
        public void handleLoginViewOnlyButtonClick();
        public void handleFeatureToggleButtonClick();
    }
    public interface LoginControllerMethods {
        public User getCurrentUser();
        public Date getCurrentClockHomeTerminalTime();
        public void handleConfigurationChangedState();
    }
}
