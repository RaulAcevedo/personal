package com.jjkeller.kmbapi.controller.dataaccess;

import android.content.Context;

import com.jjkeller.kmbapi.controller.dataaccess.LocationDBAdapter.LocationDBLocation;

import java.util.List;

public class LocationDBFacade extends FacadeBase {

	public LocationDBFacade(Context ctx)
	{
		super(ctx);
	}

	public List<LocationDBLocation> FetchList(double lat, double lon)
	{
		List<LocationDBLocation> list = null;
		LocationDBAdapter dbAdapter = new LocationDBAdapter();
		
		list = dbAdapter.FetchList(this.getContext(), lat, lon);
		
		return list;		
	}
}
