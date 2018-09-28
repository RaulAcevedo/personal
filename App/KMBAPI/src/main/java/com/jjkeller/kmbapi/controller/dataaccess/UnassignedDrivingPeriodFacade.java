package com.jjkeller.kmbapi.controller.dataaccess;

import android.content.Context;

import com.jjkeller.kmbapi.controller.UnclaimedDrivingPeriod;
import com.jjkeller.kmbapi.controller.dataaccess.db.UnassignedDrivingPeriodPersist;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.proxydata.UnassignedDrivingPeriod;

import java.util.Date;
import java.util.List;

public class UnassignedDrivingPeriodFacade extends FacadeBase {

	public UnassignedDrivingPeriodFacade(Context ctx)
	{
		super(ctx);
	}

	public UnassignedDrivingPeriodFacade(Context ctx, User user)
	{
		super(ctx, user);
	}

	public List<UnassignedDrivingPeriod> Fetch()
	{
        UnassignedDrivingPeriodPersist<UnassignedDrivingPeriod> persist = new UnassignedDrivingPeriodPersist<UnassignedDrivingPeriod>(UnassignedDrivingPeriod.class, this.getContext(), this.getCurrentUser());
        return persist.FetchList();
	}
	
	public void MarkAsSubmitted(List<UnassignedDrivingPeriod> periodList)
    {
        UnassignedDrivingPeriodPersist<UnassignedDrivingPeriod> persist = new UnassignedDrivingPeriodPersist<UnassignedDrivingPeriod>(UnassignedDrivingPeriod.class, this.getContext(), this.getCurrentUser());
        persist.MarkAsSubmitted(periodList);
	}

	public void MarkAsClaimed(UnassignedDrivingPeriod periodList)
    {
        UnassignedDrivingPeriodPersist<UnassignedDrivingPeriod> persist = new UnassignedDrivingPeriodPersist<UnassignedDrivingPeriod>(UnassignedDrivingPeriod.class, this.getContext(), this.getCurrentUser());
        persist.MarkAsClaimed(periodList);
	}

	public List<UnassignedDrivingPeriod> FetchUnsubmitted() {
        UnassignedDrivingPeriodPersist<UnassignedDrivingPeriod> persist = new UnassignedDrivingPeriodPersist<UnassignedDrivingPeriod>(UnassignedDrivingPeriod.class, this.getContext(), this.getCurrentUser());
		return persist.FetchAllUnsubmitted();
	}	
	
	public List<UnassignedDrivingPeriod> FetchUnsubmittedByDate(Date date) {
        UnassignedDrivingPeriodPersist<UnassignedDrivingPeriod> persist = new UnassignedDrivingPeriodPersist<UnassignedDrivingPeriod>(UnassignedDrivingPeriod.class, this.getContext(), this.getCurrentUser());
		return persist.FetchUnsubmittedByDate(date);
	}

	public List<UnassignedDrivingPeriod> FetchUnclaimed(){
		UnassignedDrivingPeriodPersist<UnassignedDrivingPeriod> persist = new UnassignedDrivingPeriodPersist<UnassignedDrivingPeriod>(UnassignedDrivingPeriod.class, this.getContext(), this.getCurrentUser());
		return persist.FetchAllUnClaimed();
	}

	public List<UnassignedDrivingPeriod> FetchAllByDate(String startTimeUTC) {
		UnassignedDrivingPeriodPersist<UnassignedDrivingPeriod> persist = new UnassignedDrivingPeriodPersist<UnassignedDrivingPeriod>(UnassignedDrivingPeriod.class, this.getContext(), this.getCurrentUser());
		return persist.FetchAllByDate(startTimeUTC);
	}
	
	public void Save(UnassignedDrivingPeriod udp)
	{
		UnassignedDrivingPeriodPersist<UnassignedDrivingPeriod> persist = new UnassignedDrivingPeriodPersist<UnassignedDrivingPeriod>(UnassignedDrivingPeriod.class, getContext());
		persist.Persist(udp);
	}
	
    public void Save(List<UnassignedDrivingPeriod> periodList)
    {
    	UnassignedDrivingPeriodPersist<UnassignedDrivingPeriod> persist = new UnassignedDrivingPeriodPersist<UnassignedDrivingPeriod>(UnassignedDrivingPeriod.class, getContext());
        // note: need to save these items separately so that they all 
        // are inserted as new items
    	for (UnassignedDrivingPeriod udp : periodList)
        {
            persist.Persist(udp);
        }
    }
    
    public void PurgeOldRecords(Date cutoffDate)
    {
    	UnassignedDrivingPeriodPersist<UnassignedDrivingPeriod> persist = new UnassignedDrivingPeriodPersist<UnassignedDrivingPeriod>(UnassignedDrivingPeriod.class, getContext());
    	persist.PurgeOldRecords(cutoffDate);
    }
    
    public void Delete(UnassignedDrivingPeriod period)
    {
    	UnassignedDrivingPeriodPersist<UnassignedDrivingPeriod> persist = new UnassignedDrivingPeriodPersist<UnassignedDrivingPeriod>(UnassignedDrivingPeriod.class, getContext());
    	persist.Delete(period);
    }
    
}
