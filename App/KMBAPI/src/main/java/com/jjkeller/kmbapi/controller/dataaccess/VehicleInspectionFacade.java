package com.jjkeller.kmbapi.controller.dataaccess;

import android.content.Context;

import com.jjkeller.kmbapi.controller.dataaccess.db.VehicleInspectionPersist;
import com.jjkeller.kmbapi.proxydata.EobrConfiguration;
import com.jjkeller.kmbapi.proxydata.VehicleInspection;
import com.jjkeller.kmbapi.proxydata.VehicleInspectionList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VehicleInspectionFacade extends FacadeBase {

	public VehicleInspectionFacade(Context ctx)
	{
		super(ctx);
	}
	
	public void Save(VehicleInspection vehicleInspection)
	{
		VehicleInspectionPersist<VehicleInspection> persist = new VehicleInspectionPersist<VehicleInspection>(VehicleInspection.class, this.getContext(), vehicleInspection.getIsPoweredUnit());
		persist.Persist(vehicleInspection);
	}
	
    public VehicleInspection FetchRecentTractorInspectionForCurrentUser(EobrConfiguration eobr)
    {
    	VehicleInspectionPersist<VehicleInspection> persist = new VehicleInspectionPersist<VehicleInspection>(VehicleInspection.class, this.getContext(), true);
        return persist.FetchRecentTractorForUser(this.getCurrentUser(), eobr.getSerialNumber());
    }
    
    public VehicleInspection FetchRecentTractorPreInspectionForCurrentUser(EobrConfiguration eobr)
    {
    	VehicleInspectionPersist<VehicleInspection> persist = new VehicleInspectionPersist<VehicleInspection>(VehicleInspection.class, this.getContext(), true);
    	return persist.FetchRecentTractorPreInspectionForUser(this.getCurrentUser(), eobr.getSerialNumber());
    }
	
    public VehicleInspection FetchRecentTrailerInspectionForCurrentUser(String trailerNumber)
    {
    	VehicleInspectionPersist<VehicleInspection> persist = new VehicleInspectionPersist<VehicleInspection>(VehicleInspection.class, this.getContext(), false);
        return persist.FetchRecentTrailerForUser(this.getCurrentUser(), trailerNumber);
    }

	public VehicleInspection FetchRecentTrailerPreInspectionForCurrentUser(String trailerNumber)
	{
		VehicleInspectionPersist<VehicleInspection> persist = new VehicleInspectionPersist<VehicleInspection>(VehicleInspection.class, this.getContext(), false);
		return persist.FetchRecentTrailerPreInspectionForUser(this.getCurrentUser(), trailerNumber);
	}

	public List<VehicleInspection> FetchAllUnsubmitted() {
    	VehicleInspectionPersist<VehicleInspection> persist = new VehicleInspectionPersist<VehicleInspection>(VehicleInspection.class, this.getContext(), true);
    	return persist.FetchAllUnsubmitted();
	}
	
	public void MarkAsSubmitted(VehicleInspectionList inspectionList) {
    	VehicleInspectionPersist<VehicleInspection> persist = new VehicleInspectionPersist<VehicleInspection>(VehicleInspection.class, this.getContext(), true);
    	
    	List<VehicleInspection> unSubmittedItems = new ArrayList<VehicleInspection>(Arrays.asList(inspectionList.getInspectionList()));
		persist.MarkAsSubmitted(unSubmittedItems);
	}
}
