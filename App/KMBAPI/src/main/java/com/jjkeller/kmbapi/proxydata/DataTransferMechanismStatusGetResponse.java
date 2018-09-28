package com.jjkeller.kmbapi.proxydata;

import java.util.Date;

/**
 * Created by klarsen on 3/10/2017.
 */

public class DataTransferMechanismStatusGetResponse extends ProxyBase {
        private Date DateOfNextTransfer;
        private boolean WasSuccessful;

    public Date getDateOfNextTransfer() { return this.DateOfNextTransfer; }
    public void setDateOfNextTransfer(Date value){ this.DateOfNextTransfer = value; }

    public boolean getWasSuccessful() { return this.WasSuccessful; }
    public void setWasSuccessful(boolean value){ this.WasSuccessful = value; }
}
