package com.jjkeller.kmbapi.controller.dataaccess;

import android.content.Context;
import android.util.Log;

import com.jjkeller.kmbapi.controller.dataaccess.db.EmployeeLogRevisionPersist;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.proxydata.EmployeeLogRevision;

import java.util.List;
import java.util.TimeZone;

public class EmployeeLogRevisionFacade extends FacadeBase{
	
	public EmployeeLogRevisionFacade(Context ctx)
	{
		super(ctx);
	}

	public EmployeeLogRevisionFacade(Context ctx, User user)
	{
		super(ctx, user);
	}

	public void Save(EmployeeLogRevision empLogRevisionData)
	{		
		Log.d("EmpLogRev", String.format("EmpLogRevisionFacade.Save empLogDate: %s empCode: %s empHomeTerm: %s Tz.default: %s", 
				empLogRevisionData.getEmployeeLogDate(), this.getCurrentUser().getCredentials().getEmployeeCode(), this.getCurrentUser().getHomeTerminalTimeZone().toDMOEnum(), TimeZone.getDefault().getDisplayName()));
		EmployeeLogRevisionPersist<EmployeeLogRevision> persist = new EmployeeLogRevisionPersist<EmployeeLogRevision>(EmployeeLogRevision.class, this.getContext(), this.getCurrentUser());
		persist.Persist(empLogRevisionData);
	}
	
	
    /// <summary>
    /// </summary>
    /// <returns></returns>
    public List<EmployeeLogRevision> FetchUnsubmittedWithLimit()
    {
    	EmployeeLogRevisionPersist<EmployeeLogRevision> persist = new EmployeeLogRevisionPersist<EmployeeLogRevision>(EmployeeLogRevision.class, this.getContext(), this.getCurrentUser());
        return persist.FetchAllUnsubmitted();
    }
    
	public void MarkSubmitted(List<EmployeeLogRevision> items)
	{
		EmployeeLogRevisionPersist<EmployeeLogRevision> persist = new EmployeeLogRevisionPersist<EmployeeLogRevision>(EmployeeLogRevision.class, this.getContext(), this.getCurrentUser());		
		persist.MarkAsSubmitted(items);
	}
}
