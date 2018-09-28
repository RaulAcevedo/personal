package com.jjkeller.kmbapi.proxydata;

public class LogRemarkItem extends ProxyBase {

	private String name;
	private int itemEnum = -1;
	private String lkupLogRemarkId;
	private boolean isActive = true;
	
	///////////////////////////////////////////////////////////////////////////////////////
	// public get/set methods
	///////////////////////////////////////////////////////////////////////////////////////
    public String getName()
    {
    	return this.name;
    }
    public void setName(String value)
    {
    	this.name = value;
    }
    
    public int getItemEnum()
    {
    	return this.itemEnum;
    }
    public void setItemEnum(int value)
    {
    	this.itemEnum = value;
    }
    
    public String getLkupLogRemarkId()
    {
    	return this.lkupLogRemarkId;
    }
    public void setLkupLogRemarkId(String value)
    {
    	this.lkupLogRemarkId = value;
    } 
    
    public boolean getIsActive()
    {
    	return this.isActive;
    }
    public void setIsActive(boolean value)
    {
    	this.isActive = value;
    } 
    public String toString()
    {
 		return this.getName();
 	}
}

