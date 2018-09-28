package com.jjkeller.kmbapi.proxydata;

import java.util.Date;

/**
 * Created by t000253 on 2/1/2016.
 */
public class EmployeeLogReportRequest extends ProxyBase{

    ///////////////////////////////////////////////////////////////////////////////////////
    // private members
    ///////////////////////////////////////////////////////////////////////////////////////

    private String email;
    private Date beginDate;
    private Date endDate;

    ///////////////////////////////////////////////////////////////////////////////////////
    // public get/set methods
    ///////////////////////////////////////////////////////////////////////////////////////

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public Date getBeginDate(){ return beginDate; }

    public void setBeginDate(Date beginDate){
        this.beginDate = beginDate;
    }

    public Date getEndDate(){ return endDate; }

    public void setEndDate(Date endDate){
        this.endDate = endDate;
    }
}
