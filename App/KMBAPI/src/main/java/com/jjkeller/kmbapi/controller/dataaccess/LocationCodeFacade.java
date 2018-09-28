package com.jjkeller.kmbapi.controller.dataaccess;

import android.content.Context;

import com.jjkeller.kmbapi.controller.dataaccess.db.LocationCodePersist;
import com.jjkeller.kmbapi.proxydata.LocationCode;

import java.util.List;

public class LocationCodeFacade extends FacadeBase {

	public LocationCodeFacade(Context ctx)
	{
		super(ctx);
	}
	
    public List<LocationCode> FetchForUser()
    {
        LocationCodePersist<LocationCode> persist = new LocationCodePersist<LocationCode>(LocationCode.class, this.getContext());
        return persist.FetchList();
    }

    public void SaveToUser(List<LocationCode> locationCodeList)
    {
        LocationCodePersist<LocationCode> persist = new LocationCodePersist<LocationCode>(LocationCode.class, this.getContext());
        persist.Persist(locationCodeList);
    }
}
