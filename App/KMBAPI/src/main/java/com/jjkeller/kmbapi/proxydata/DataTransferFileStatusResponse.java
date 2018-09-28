package com.jjkeller.kmbapi.proxydata;

import com.jjkeller.kmbapi.enums.DataTransferFileStatusEnum;

public class DataTransferFileStatusResponse extends ProxyBase {

    private DataTransferFileStatusEnum status;
    private String[] errorMessageLineItems;

    public DataTransferFileStatusEnum getStatus() { return this.status; }
    public void setStatus(DataTransferFileStatusEnum value){ this.status = value; }

    public String[] getErrorMessageLineItems() { return this.errorMessageLineItems; }
    public void setErrorMessageLineItems(String[] value){ this.errorMessageLineItems = value; }

}
