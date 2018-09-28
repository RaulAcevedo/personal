package com.jjkeller.kmbapi.controller.dataaccess;

import android.content.Context;

import com.jjkeller.kmbapi.controller.dataaccess.db.EobrConfigurationPersist;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.proxydata.EobrConfiguration;

import java.util.List;

public class EobrConfigurationFacade extends FacadeBase{

	public EobrConfigurationFacade(Context ctx) {
		super(ctx);
	}
	
	public EobrConfigurationFacade(Context ctx, User user){
		super(ctx, user);
	}

	public void Save(String eobrSerialNumber, EobrConfiguration eobrConfig)
	{
		EobrConfigurationPersist<EobrConfiguration> persist = new EobrConfigurationPersist<EobrConfiguration>(EobrConfiguration.class, this.getContext(), this.getCurrentUser(), eobrSerialNumber);
		persist.Persist(eobrConfig);
	}

	public EobrConfiguration Fetch(String eobrSerialNumber)
	{
		EobrConfigurationPersist<EobrConfiguration> persist = new EobrConfigurationPersist<EobrConfiguration>(EobrConfiguration.class, this.getContext(), this.getCurrentUser(), eobrSerialNumber);
		List<EobrConfiguration> list = persist.FetchList();
		if(list != null && list.size() > 0) return list.get(0);
		else return null;
	}
	
	public List<EobrConfiguration> FetchAllUnsubmitted()
	{
		EobrConfigurationPersist<EobrConfiguration> persist = new EobrConfigurationPersist<EobrConfiguration>(EobrConfiguration.class, this.getContext(), this.getCurrentUser());
        return persist.FetchAllUnsubmitted();
	}

	public List<EobrConfiguration> FetchAll()
	{
		EobrConfigurationPersist<EobrConfiguration> persist = new EobrConfigurationPersist<EobrConfiguration>(EobrConfiguration.class, this.getContext(), this.getCurrentUser());
        return persist.FetchAll();
	}
	
	public void MarkAsSubmitted(EobrConfiguration eobrConfig)
	{
		EobrConfigurationPersist<EobrConfiguration> persist = new EobrConfigurationPersist<EobrConfiguration>(EobrConfiguration.class, this.getContext());
        persist.MarkAsSubmitted(eobrConfig);
	}

	/**
	 * When not connected to an ELD, return the mostly recently connected EOBR Config
	 */
	public EobrConfiguration FetchMostRecentlyConnectedEobr()
	{
		EobrConfigurationPersist<EobrConfiguration> persist = new EobrConfigurationPersist<EobrConfiguration>(EobrConfiguration.class, this.getContext());
		return persist.FetchMostRecentlyConnectedEobr();
	}
}
