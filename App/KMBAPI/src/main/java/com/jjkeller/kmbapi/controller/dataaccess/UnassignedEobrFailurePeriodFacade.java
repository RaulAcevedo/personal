package com.jjkeller.kmbapi.controller.dataaccess;

import android.content.Context;

import com.jjkeller.kmbapi.controller.dataaccess.db.UnassignedDrivingPeriodPersist;
import com.jjkeller.kmbapi.controller.dataaccess.db.UnassignedEobrFailurePeriodPersist;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.proxydata.UnassignedDrivingPeriod;
import com.jjkeller.kmbapi.proxydata.UnassignedEobrFailurePeriod;

import java.util.Date;
import java.util.List;

public class UnassignedEobrFailurePeriodFacade extends FacadeBase {

	public UnassignedEobrFailurePeriodFacade(Context ctx) {
		super(ctx);
	}

	public UnassignedEobrFailurePeriodFacade(Context ctx, User user) {
		super(ctx, user);
	}

	public List<UnassignedEobrFailurePeriod> FetchUnsubmitted()
	{
		UnassignedEobrFailurePeriodPersist<UnassignedEobrFailurePeriod> persist = new UnassignedEobrFailurePeriodPersist<UnassignedEobrFailurePeriod>(UnassignedEobrFailurePeriod.class, this.getContext(), this.getCurrentUser());
        return persist.FetchAllUnsubmitted();
	}

	public void MarkAsSubmitted(List<UnassignedEobrFailurePeriod> unassignedEobrFailurePeriodList)
	{
		UnassignedEobrFailurePeriodPersist<UnassignedEobrFailurePeriod> persist = new UnassignedEobrFailurePeriodPersist<UnassignedEobrFailurePeriod>(UnassignedEobrFailurePeriod.class, this.getContext(), this.getCurrentUser());
        persist.MarkAsSubmitted(unassignedEobrFailurePeriodList);
	}

    public void Save(List<UnassignedEobrFailurePeriod> periodList)
    {
		UnassignedEobrFailurePeriodPersist<UnassignedEobrFailurePeriod> persist = new UnassignedEobrFailurePeriodPersist<UnassignedEobrFailurePeriod>(UnassignedEobrFailurePeriod.class, this.getContext(), this.getCurrentUser());
        // note: need to save these items separately so that they all 
        // are inserted as new items
    	for (UnassignedEobrFailurePeriod ufp : periodList)
        {
            persist.Persist(ufp);
        }
    }
    
    public void PurgeOldRecords(Date cutoffDate)
    {
    	UnassignedDrivingPeriodPersist<UnassignedDrivingPeriod> persist = new UnassignedDrivingPeriodPersist<UnassignedDrivingPeriod>(UnassignedDrivingPeriod.class, getContext());
    	persist.PurgeOldRecords(cutoffDate);
    }
    
}
