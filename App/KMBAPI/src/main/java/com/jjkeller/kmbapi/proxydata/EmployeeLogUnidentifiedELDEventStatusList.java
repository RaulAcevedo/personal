package com.jjkeller.kmbapi.proxydata;

/**
 * Created by Kris Larsen on 7/17/2017.
 */

public class EmployeeLogUnidentifiedELDEventStatusList extends ProxyBase {
    ///////////////////////////////////////////////////////////////////////////////////////
    // private members
    ///////////////////////////////////////////////////////////////////////////////////////
    private EmployeeLogUnidentifiedELDEventStatus[] EmployeeLogUnidentifiedELDEventStatuses;

    ///////////////////////////////////////////////////////////////////////////////////////
    // public get/set methods
    ///////////////////////////////////////////////////////////////////////////////////////
    public EmployeeLogUnidentifiedELDEventStatus[] GetEmployeeLogUnidentifiedELDEventStatusList()
    {
        return EmployeeLogUnidentifiedELDEventStatuses;
    }
    public void settEmployeeLogUnidentifiedELDEventStatusList(EmployeeLogUnidentifiedELDEventStatus[] employeeLogUnidentifiedELDEventStatuses)
    {
        this.EmployeeLogUnidentifiedELDEventStatuses = employeeLogUnidentifiedELDEventStatuses;
    }
}
