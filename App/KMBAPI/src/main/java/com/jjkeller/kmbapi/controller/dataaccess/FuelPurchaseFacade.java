package com.jjkeller.kmbapi.controller.dataaccess;

import android.content.Context;

import com.jjkeller.kmbapi.controller.dataaccess.db.FuelPurchasePersist;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.proxydata.FuelPurchase;

import java.util.Date;
import java.util.List;

public class FuelPurchaseFacade extends FacadeBase {

	public FuelPurchaseFacade(Context ctx, User user) {
		super(ctx, user);
	}

	public List<FuelPurchase> FetchAllUnsubmitted()
	{
		FuelPurchasePersist<FuelPurchase> persist = new FuelPurchasePersist<FuelPurchase>(FuelPurchase.class, this.getContext());
		return persist.FetchAllUnsubmitted();
	}

	public void MarkAsSubmitted(List<FuelPurchase> unSubmittedItems) {
		FuelPurchasePersist<FuelPurchase> persist = new FuelPurchasePersist<FuelPurchase>(FuelPurchase.class, this.getContext());
		persist.MarkAsSubmitted(unSubmittedItems);
	}

    public void Save(FuelPurchase fuelPurchase)
    {
    	FuelPurchasePersist<FuelPurchase> persist = new FuelPurchasePersist<FuelPurchase>(FuelPurchase.class, getContext());
    	persist.Persist(fuelPurchase);
    }
    
    public void PurgeOldRecords(Date cutoffDate)
    {
    	FuelPurchasePersist<FuelPurchase> persist = new FuelPurchasePersist<FuelPurchase>(FuelPurchase.class, getContext());
    	persist.PurgeOldRecords(cutoffDate);
    }
}
