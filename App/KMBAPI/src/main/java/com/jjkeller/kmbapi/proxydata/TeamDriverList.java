package com.jjkeller.kmbapi.proxydata;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;


public class TeamDriverList extends ProxyBase {
    @Override
    public String toString() {
        return "TeamDriverList{" +
                "teamDrivers=" + Arrays.toString(teamDrivers) +
                '}';
    }

    ///////////////////////////////////////////////////////////////////////////////////////
	// private members
	///////////////////////////////////////////////////////////////////////////////////////
	private TeamDriver[] teamDrivers = null;

	///////////////////////////////////////////////////////////////////////////////////////
	// public get/set methods
	///////////////////////////////////////////////////////////////////////////////////////
	public TeamDriver[] getTeamDriverList()
    {
    	return teamDrivers;
    }
    public void setTeamDriverList(TeamDriver[] teamDrivers)
    {
    	this.teamDrivers = teamDrivers;
    }
    


	///////////////////////////////////////////////////////////////////////////////////////
	// Custom methods
	///////////////////////////////////////////////////////////////////////////////////////
    
    public void Add(TeamDriver driver)
    {
    	if (this.teamDrivers == null)
    	{
    		// no drivers in the list yet
    		// create a list of 1, and add it
    		this.teamDrivers = new TeamDriver[1];
    		this.teamDrivers[0] = driver;
    	}
    	else
    	{
    		// there are drivers already
            List<TeamDriver> list = new LinkedList<TeamDriver>(Arrays.asList(this.teamDrivers));
            list.add(driver);
            
            this.teamDrivers = list.toArray(new TeamDriver[list.size()]);
    	}
    }
    
    public boolean IsEmpty()
    {
    	return (this.teamDrivers == null || this.teamDrivers.length == 0);
    }
    
    /// <summary>
    /// Answer if there is a team driver present at the time specified
    /// </summary>
    public boolean IsTeamDriverPresent(Date timestamp)
    {
    	boolean answer = false;
    	
    	if (!this.IsEmpty())
    	{
    		for (TeamDriver drv : this.teamDrivers)
    		{
                // check if a team driver was active at the timestamp
                // does it start before the timestamp and end after it
    			if (drv.getStartTime().compareTo(timestamp) <= 0)
    			{
    				if (drv.getEndTime() == null || timestamp.compareTo(drv.getEndTime()) <= 0)
    				{
    					answer = true;
    				}
    			}
    		}
    	}
    	
    	return answer;
    }
}
