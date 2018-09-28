package com.jjkeller.kmbapi.proxydata;

import com.jjkeller.kmbapi.enums.InspectionDefectType;

import java.util.ArrayList;
import java.util.Arrays;

public class DefectList extends ProxyBase {

	///////////////////////////////////////////////////////////////////////////////////////
	// private members
	///////////////////////////////////////////////////////////////////////////////////////
    private VehicleInspectionDefect[] defectList;
    
	///////////////////////////////////////////////////////////////////////////////////////
	// public get/set methods
	///////////////////////////////////////////////////////////////////////////////////////
    public VehicleInspectionDefect[] getDefectList()
    {
    	return defectList;
    }
    public void setDefectList(VehicleInspectionDefect[] defectList)
    {
    	this.defectList = defectList;
    }
    
    

	///////////////////////////////////////////////////////////////////////////////////////
	// Custom methods
	///////////////////////////////////////////////////////////////////////////////////////
    public boolean IsEmpty()
    {
    	return (this.defectList == null || this.defectList.length == 0);
    }
    
    /// <summary>
    /// Answer if the defect is contained in the list
    /// </summary>
    /// <param name="defect"></param>
    /// <returns></returns>
    public boolean Contains(InspectionDefectType defectToLocate)
    {
        boolean contains = false;
        if(defectList != null && defectList.length > 0)
        {
            for (int index = 0; index< defectList.length; index++)
            {
            	VehicleInspectionDefect currDefect = defectList[index];
                if (currDefect.getInspectionDefectType().getValue() == defectToLocate.getValue()) 
                { 
                    contains = true;
                    break;
                }
            }
        }
        return contains;
    }
    
    /// <summary>
    /// Add the defect to the list if it's not already in the list
    /// </summary>
    /// <param name="driver"></param>
    public void Add(InspectionDefectType defect)
    {
        if (this.Contains(defect)) return;
        if (defectList == null)
        {
            // nothing in the list yet
            // create a list of 1, and add it
        	defectList = new VehicleInspectionDefect[1];
        	VehicleInspectionDefect newDefect = new VehicleInspectionDefect();
        	newDefect.setInspectionDefectType(defect);
        	defectList[0] = newDefect;
        }
        else
        {
        	ArrayList<VehicleInspectionDefect> list = new ArrayList<VehicleInspectionDefect>(Arrays.asList(defectList));
        	VehicleInspectionDefect newDefect = new VehicleInspectionDefect();
        	newDefect.setInspectionDefectType(defect);
            list.add(newDefect);

            // recreate the list
            defectList = list.toArray(new VehicleInspectionDefect[list.size()]);
        }
    }
    
    /// <summary>
    /// Remove the defect from the list
    /// </summary>
    /// <param name="defect"></param>
    public void Remove(InspectionDefectType defect)
    {
        if (defectList != null && defectList.length > 0)
        {
            VehicleInspectionDefect itemToRemove = null;
            for (int index = 0; index< defectList.length; index++)
            {
            	VehicleInspectionDefect currDefect = defectList[index];
                if (currDefect.getInspectionDefectType() == defect)
                {
                    itemToRemove = currDefect;
                    break;
                }
            }
            if (itemToRemove != null)
            {
            	ArrayList<VehicleInspectionDefect> list = new ArrayList<VehicleInspectionDefect>(Arrays.asList(defectList));
                list.remove(itemToRemove);

                // recreate the list
                defectList = list.toArray(new VehicleInspectionDefect[list.size()]);
            }
        }
    }
    
    public void ClearAllDefects(){
    	defectList = null;
    }
    
    public int[] GetSerializableDefectList() {
    	int[] defects = null;
        if (defectList != null && defectList.length > 0)
        {     
        	defects = new int[defectList.length];
            for (int index = 0; index< defectList.length; index++)
            {
            	VehicleInspectionDefect currDefect = defectList[index];
            	defects[index] = currDefect.getInspectionDefectType().getValue();
            }
        }
        return defects;
    }
    public void PutSerializableDefectList(int[] lstOfDefects) {
    	if(lstOfDefects == null){ 
    		defectList = null;
    	} else {
	    	defectList = new VehicleInspectionDefect[lstOfDefects.length];
	    	for(int index = 0; index < lstOfDefects.length; index++){
	    		VehicleInspectionDefect newDefect = new VehicleInspectionDefect();
	    		newDefect.setInspectionDefectType(new InspectionDefectType(lstOfDefects[index]));
	    		defectList[index] = newDefect;
	    	}
    	}   	
    }
}
