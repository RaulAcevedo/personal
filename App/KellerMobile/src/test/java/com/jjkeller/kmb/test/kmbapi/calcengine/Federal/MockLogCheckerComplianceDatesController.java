package com.jjkeller.kmb.test.kmbapi.calcengine.Federal;

import com.jjkeller.kmbapi.controller.ILogCheckerComplianceDatesController;

import java.util.Date;

class MockLogCheckerComplianceDatesController implements ILogCheckerComplianceDatesController {

    public boolean IsLogCheckerComplianceDateActive(int complianceDatesType, Date dateToCheck, boolean dateRangeDefinesActivePeriod) {
        Date complianceStartDate = new Date(Date.parse("12/16/2014"));
        Date complianceEndDate = new Date(Date.parse("03/16/2015"));

        boolean isActive;
        if (complianceStartDate.compareTo(dateToCheck) < 0) {
            if (complianceEndDate.compareTo(dateToCheck) > 0)
                isActive = dateRangeDefinesActivePeriod;
            else
                isActive = !dateRangeDefinesActivePeriod;
        } else {
            isActive = !dateRangeDefinesActivePeriod;
        }

        return isActive;
    }
}
