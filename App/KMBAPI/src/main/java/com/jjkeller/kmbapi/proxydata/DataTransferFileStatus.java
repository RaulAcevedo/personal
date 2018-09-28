package com.jjkeller.kmbapi.proxydata;

import com.jjkeller.kmbapi.enums.DataTransferFileStatusEnum;
import com.jjkeller.kmbapi.enums.RoadsideDataTransferMethodEnum;
import java.util.Date;

public class DataTransferFileStatus extends ProxyBase {

    private String fileName;
    private Date createDate;
    private int userKey;
    private int attemptCount;
    private DataTransferFileStatusEnum status = new DataTransferFileStatusEnum(DataTransferFileStatusEnum.UNKNOWN);
    private RoadsideDataTransferMethodEnum roadsideDataTransferMethod = new RoadsideDataTransferMethodEnum(RoadsideDataTransferMethodEnum.EMAIL);
    private Boolean wasNotificationDisplayed;

    public String getFileName() { return this.fileName; }
    public void setFileName(String value){ this.fileName = value; }

    public Date getCreateDate() { return this.createDate; }
    public void setCreateDate(Date value){ this.createDate = value; }

    public int getUserKey() { return this.userKey; }
    public void setUserKey(int value){ this.userKey = value; }

    public int getAttemptCount() { return this.attemptCount; }
    public void setAttemptCount(int value){ this.attemptCount = value; }

    public DataTransferFileStatusEnum getStatus() { return this.status; }
    public void setStatus(DataTransferFileStatusEnum value){ this.status = value; }

    public RoadsideDataTransferMethodEnum getRoadsideDataTransferMethod() { return this.roadsideDataTransferMethod; }
    public void setRoadsideDataTransferMethod(RoadsideDataTransferMethodEnum value){ this.roadsideDataTransferMethod = value; }

    public Boolean getWasNotificationDisplayed() { return this.wasNotificationDisplayed; }
    public void setWasNotificationDisplayed(Boolean value){ this.wasNotificationDisplayed = value; }
}
