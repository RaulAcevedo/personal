package com.jjkeller.kmb.interfaces;

import com.jjkeller.kmbapi.controller.interfaces.IAPIController;


public interface ISubmitLogsViewOnly {
    public interface SubmitLogsViewOnlyFragControllerMethods{
        public IAPIController getMyController();
    }

    public interface SubmitLogsViewOnlyFragActions{
        public void handleSubmitButtonClick();
    }
}

