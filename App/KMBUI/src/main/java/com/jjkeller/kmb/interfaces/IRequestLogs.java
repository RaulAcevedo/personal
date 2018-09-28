package com.jjkeller.kmb.interfaces;

import com.jjkeller.kmbapi.controller.interfaces.IAPIController;

/**
 * Created by t000253 on 2/1/2016.
 */
public interface IRequestLogs {
    public interface RequestLogsFragControllerMethods{
        public IAPIController getMyController();
    }

    public interface RequestLogsFragActions{
        public void handleSubmitButtonClick();
        public void handleRememberMe(boolean isChecked);
    }
}
