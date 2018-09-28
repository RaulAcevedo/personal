package com.jjkeller.kmbapi.controller.dataaccess;

import android.content.Context;

import com.jjkeller.kmbapi.controller.dataaccess.db.EobrDiagnosticCommandPersist;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.proxydata.EobrDiagnosticCommand;

import java.util.List;

public class EobrDiagnosticCommandFacade extends FacadeBase{

	public EobrDiagnosticCommandFacade(Context ctx, User user)
	{
		super(ctx, user);
	}

    public List<EobrDiagnosticCommand> FetchAllPendingCommands(String serialNumber)
    {
        EobrDiagnosticCommandPersist<EobrDiagnosticCommand> persist = new EobrDiagnosticCommandPersist<EobrDiagnosticCommand>(EobrDiagnosticCommand.class, getContext());
        return persist.FetchAllPendingCommands(serialNumber);
    }
    
    public void Save(List<EobrDiagnosticCommand> eobrCommands)
    {
    	EobrDiagnosticCommandPersist<EobrDiagnosticCommand> persist = new EobrDiagnosticCommandPersist<EobrDiagnosticCommand>(EobrDiagnosticCommand.class, getContext());
    	persist.Save(eobrCommands);
    }
    
    public void PurgeCommand(long key)
    {
    	EobrDiagnosticCommandPersist<EobrDiagnosticCommand> persist = new EobrDiagnosticCommandPersist<EobrDiagnosticCommand>(EobrDiagnosticCommand.class, getContext());
    	persist.PurgeCommand(key);
    }
    
    public List<EobrDiagnosticCommand> FetchCompletedCommands()
    {
        EobrDiagnosticCommandPersist<EobrDiagnosticCommand> persist = new EobrDiagnosticCommandPersist<EobrDiagnosticCommand>(EobrDiagnosticCommand.class, this.getContext());
        return persist.FetchCompletedCommands();
    }
}
