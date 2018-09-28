package com.jjkeller.kmbapi.controller.dataaccess;

import android.content.Context;

import com.jjkeller.kmbapi.controller.dataaccess.db.RoutePositionPersist;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.proxydata.RoutePosition;

import java.util.Date;
import java.util.List;

public class RoutePositionFacade extends FacadeBase{

	public RoutePositionFacade(Context ctx) {
		super(ctx);
	}

	public RoutePositionFacade(Context ctx, User user) {
		super(ctx, user);
	}

    public List<RoutePosition> FetchAllUnsubmitted()
    {
        RoutePositionPersist<RoutePosition> persist = new RoutePositionPersist<RoutePosition>(RoutePosition.class, this.getContext());
        return persist.FetchAllUnsubmitted();
    }
    
    public void MarkAsSubmitted(List<RoutePosition> routePositionList)
    {
        RoutePositionPersist<RoutePosition> persist = new RoutePositionPersist<RoutePosition>(RoutePosition.class, this.getContext());
        persist.MarkAsSubmitted(routePositionList);
    }
    
    public void Save(List<RoutePosition> list)
    {
    	RoutePositionPersist<RoutePosition> persist = new RoutePositionPersist<RoutePosition>(RoutePosition.class, getContext());
    	
    	for(RoutePosition route : list)
    		persist.Persist(route);
    }
    
    public void Save(RoutePosition routePosition)
    {
    	RoutePositionPersist<RoutePosition> persist = new RoutePositionPersist<RoutePosition>(RoutePosition.class, getContext());
    	persist.Persist(routePosition);
    }
    
    public void PurgeOldRecords(Date cutoffDate)
    {
    	RoutePositionPersist<RoutePosition> persist = new RoutePositionPersist<RoutePosition>(RoutePosition.class, getContext());
    	persist.PurgeOldRecords(cutoffDate);
    }
}
