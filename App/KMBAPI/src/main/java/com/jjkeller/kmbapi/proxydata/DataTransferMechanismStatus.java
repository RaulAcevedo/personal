package com.jjkeller.kmbapi.proxydata;
import java.util.Date;
/**
 * Created by t000622 - KLarsen on 2/10/2017.
 */

public class DataTransferMechanismStatus  extends ProxyBase {
    private String transferId;
    private Date dateScheduledToTransfer;
    private Date dateTransferred;
    private Date dateOfNextTransfer;
    private boolean wasSuccessful;

    public String getTransferId() { return this.transferId; }
    public void setTransferId(String value){ this.transferId = value; }

    public Date getDateScheduledToTransfer() { return this.dateScheduledToTransfer; }
    public void setDateScheduledToTransfer(Date value){ this.dateScheduledToTransfer = value; }

    public Date getDateTransferred() { return this.dateTransferred; }
    public void setDateTransferred(Date value){ this.dateTransferred = value; }

    public Date getDateOfNextTransfer() { return this.dateOfNextTransfer; }
    public void setDateOfNextTransfer(Date value){ this.dateOfNextTransfer = value; }

    public boolean getWasSuccessful() { return this.wasSuccessful; }
    public void setWasSuccessful(boolean value){ this.wasSuccessful = value; }

}
