package com.jjkeller.kmbapi.controller;

import android.content.Context;
import android.os.Bundle;

import com.google.gson.JsonParseException;
import com.jjkeller.kmbapi.R;
import com.jjkeller.kmbapi.common.TimeKeeper;
import com.jjkeller.kmbapi.controller.EOBR.EobrReader;
import com.jjkeller.kmbapi.controller.dataaccess.EobrDiagnosticCommandFacade;
import com.jjkeller.kmbapi.controller.share.ControllerBase;
import com.jjkeller.kmbapi.controller.utility.RESTWebServiceHelper;
import com.jjkeller.kmbapi.kmbeobr.Enums.EobrReturnCode;
import com.jjkeller.kmbapi.proxydata.EobrDiagnosticCommand;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EobrDiagnosticCommandController extends ControllerBase {

    public EobrDiagnosticCommandController(Context ctx) {
		super(ctx);
	}

	public boolean DownloadPendingEobrDiagnosticCommands() {
		boolean success = false;
		ArrayList<EobrDiagnosticCommand> eobrCommands = null;

		if (this.getIsWebServicesAvailable() && this.IsEobrDeviceOnline()) {
			String serialNum = EobrReader.getInstance().getEobrSerialNumber();
			if (serialNum != null && !serialNum.isEmpty()) {
				try {
					RESTWebServiceHelper rwsh = new RESTWebServiceHelper(getContext());
					eobrCommands = rwsh.GetPendingEOBRDiagnosticCommands(serialNum);
					success = true;
				} catch (IOException | JsonParseException e) {
					this.HandleException(e, this.getContext().getString(R.string.downloadeobrdiagnosiccommandfromdmo));
				}
			}
		}

		if (eobrCommands != null && eobrCommands.size() > 0)
			this.SaveCommands(eobrCommands);

		return success;
	}
	
	// Save the configuration to the local database
	private void SaveCommands(List<EobrDiagnosticCommand> eobrCommands) {
		if (eobrCommands != null) {
			EobrDiagnosticCommandFacade facade = new EobrDiagnosticCommandFacade(this.getContext(), this.getCurrentUser());
			facade.Save(eobrCommands);
		}
	}

	public void ExecutePendingCommands() {
		
		String serialNum = EobrReader.getInstance().getEobrSerialNumber();
		
		// Get list of pending commands
		EobrDiagnosticCommandFacade facade = new EobrDiagnosticCommandFacade(this.getContext(), this.getCurrentUser());
		List<EobrDiagnosticCommand> eobrCommands = facade.FetchAllPendingCommands(serialNum);
		
		// Execute each command
		for (EobrDiagnosticCommand command : eobrCommands) {

			Bundle response = EobrReader.getInstance().SendConsoleCommand(command.getCommand());
			String commandResponse;
			if (response.getInt(this.getContext().getString(R.string.rc)) == EobrReturnCode.S_SUCCESS) {
				commandResponse = response.getString(this.getContext().getString(R.string.returnvalue));
			} else {
				commandResponse = "Command failed with error message: " + response.getInt(this.getContext().getString(R.string.rc));
			}


			// Set Response	
			command.setRespnose(commandResponse);
			command.setResponseTimestamp(TimeKeeper.getInstance().now());
		}
		
		// Save to local DB
		this.SaveCommands(eobrCommands);
	}

	public boolean SubmitEobrDiagnosticCommandsToDMO()
	{
        boolean isSuccessful = false;
        if (this.getIsNetworkAvailable())
        {
            try
            {
                // first fetch all completed commands
            	EobrDiagnosticCommandFacade facade = new EobrDiagnosticCommandFacade(this.getContext(), this.getCurrentUser());
            	List<EobrDiagnosticCommand> completedCommands = facade.FetchCompletedCommands();

                // are there any to send?
                if (completedCommands != null && completedCommands.size() > 0)
                {
                    // there are records to send to DMO
                    for (EobrDiagnosticCommand command : completedCommands)
                    {
            			try
            			{
            				RESTWebServiceHelper rwsh = new RESTWebServiceHelper(getContext());
            				rwsh.SubmitEOBRDiagnosticResults(command);

                            // Delete command from local DB
                            facade.PurgeCommand(command.getPrimaryKey());
            			}
						catch (JsonParseException e)
            			{
            				this.HandleExceptionAndThrow(e, this.getContext().getString(R.string.submiteobrdiagnosticcommandstodmo), this.getContext().getString(R.string.exception_webservicecommerror), this.getContext().getString(R.string.exception_serviceunavailable));
            			}
                    }
                }

                isSuccessful = true;
            }
			catch (Exception ex)
			{
				this.HandleException(ex, this.getContext().getString(R.string.submiteobrdiagnosticcommandstodmo));
            }
        }

        return isSuccessful;
	}

    /**
	 * @return true if the current EOBR instance is ONLINE and false otherwise
	 */
	private boolean IsEobrDeviceOnline()
    {
    	boolean isOnline = false;
        if (EobrReader.getInstance() != null)
        {
            if (EobrReader.getInstance().getCurrentConnectionState() == EobrReader.ConnectionState.ONLINE)
            {
                isOnline = true;
            }
        }
        return isOnline;
    }
}
