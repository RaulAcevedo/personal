package com.jjkeller.kmb.interfaces;

import com.jjkeller.kmbapi.controller.interfaces.IAPIController;

import java.util.Date;

public interface IDateSelectorFrag {
    public interface DateSelectorControllerMethods{
        public IAPIController getEmployeeLogController();
        public void handleDateChange(Date selectedDate, int position);
        public void handleEditLog();
    }
}
