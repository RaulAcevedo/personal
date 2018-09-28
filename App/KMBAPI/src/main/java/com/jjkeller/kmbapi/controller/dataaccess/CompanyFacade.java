package com.jjkeller.kmbapi.controller.dataaccess;

import android.content.Context;

import com.jjkeller.kmbapi.controller.dataaccess.db.CompanyPersist;
import com.jjkeller.kmbapi.proxydata.CompanyConfigSettings;

public class CompanyFacade extends FacadeBase {
	
	public CompanyFacade(Context ctx)
	{
		super(ctx);
	}
	
	public CompanyConfigSettings Fetch()
	{
		CompanyPersist<CompanyConfigSettings> persist = new CompanyPersist<CompanyConfigSettings>(CompanyConfigSettings.class, this.getContext());
		return persist.Fetch();
	}
	
	public void Save(CompanyConfigSettings companyConfigSettings)
	{
		CompanyPersist<CompanyConfigSettings> persist = new CompanyPersist<CompanyConfigSettings>(CompanyConfigSettings.class, this.getContext());
		persist.Persist(companyConfigSettings);
	}
}
