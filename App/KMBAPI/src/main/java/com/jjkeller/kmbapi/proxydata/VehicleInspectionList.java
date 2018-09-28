package com.jjkeller.kmbapi.proxydata;

import java.util.ArrayList;
import java.util.Arrays;





public class VehicleInspectionList extends ProxyBase {

	///////////////////////////////////////////////////////////////////////////////////////
	// private members
	///////////////////////////////////////////////////////////////////////////////////////
    private VehicleInspection[] inspectionList;
    
	///////////////////////////////////////////////////////////////////////////////////////
	// public get/set methods
	///////////////////////////////////////////////////////////////////////////////////////
    public VehicleInspection[] getInspectionList()
    {
    	return inspectionList;
    }
    public void setInspectionList(VehicleInspection[] inspectionList)
    {
    	this.inspectionList = inspectionList;
    }

   
	///////////////////////////////////////////////////////////////////////////////////////
	// Custom methods
	///////////////////////////////////////////////////////////////////////////////////////
    public boolean IsEmpty()
    {
    	return (this.inspectionList == null || this.inspectionList.length == 0);
    }
        
    /// <summary>
    /// Add the defect to the list if it's not already in the list
    /// </summary>
    /// <param name="driver"></param>
    public void Add(VehicleInspection inspection)
    {
        if (inspectionList == null)
        {
            // nothing in the list yet
            // create a list of 1, and add it
        	inspectionList = new VehicleInspection[1];
        	inspectionList[0] = inspection;
        }
        else
        {
        	ArrayList<VehicleInspection> list = new ArrayList<VehicleInspection>(Arrays.asList(inspectionList));
            list.add(inspection);

            // recreate the list
            inspectionList = list.toArray(new VehicleInspection[list.size()]);
        }
    }
    

}
