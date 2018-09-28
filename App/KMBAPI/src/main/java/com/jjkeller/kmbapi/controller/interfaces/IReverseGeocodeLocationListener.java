package com.jjkeller.kmbapi.controller.interfaces;

import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.GpsLocation;


public interface IReverseGeocodeLocationListener
{
     void onResult(EmployeeLog log, GpsLocation location);
}