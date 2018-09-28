package com.jjkeller.kmb.test.kmbapi.calcengine.Federal;

import com.jjkeller.kmbapi.controller.ILogCheckerComplianceDatesController;

import java.util.Date;

class MockLogCheckerComplianceDatesControllerAllInactive implements ILogCheckerComplianceDatesController {

    public boolean IsLogCheckerComplianceDateActive(int complianceDatesType, Date dateToCheck, boolean dateRangeDefinesActivePeriod) {
        return !dateRangeDefinesActivePeriod;
    }
}
