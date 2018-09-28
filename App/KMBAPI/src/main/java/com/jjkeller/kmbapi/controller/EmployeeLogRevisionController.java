package com.jjkeller.kmbapi.controller;

import android.content.Context;

import com.google.gson.JsonSyntaxException;
import com.jjkeller.kmbapi.controller.dataaccess.EmployeeLogRevisionFacade;
import com.jjkeller.kmbapi.controller.share.ControllerBase;
import com.jjkeller.kmbapi.controller.utility.ErrorLogHelper;
import com.jjkeller.kmbapi.controller.utility.RESTWebServiceHelper;
import com.jjkeller.kmbapi.enums.EmployeeLogRevisionTypeEnum;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.EmployeeLogRevision;

import java.io.IOException;
import java.util.List;

public class EmployeeLogRevisionController extends ControllerBase {

	public EmployeeLogRevisionController(Context ctx)
	{
		super(ctx);
	}
	
	public void CreateRevisionFor(EmployeeLog empLog, int revisionType)
	{
		EmployeeLogRevision empLogRevision = new EmployeeLogRevision();
		empLogRevision.setEmployeeLogDate(empLog.getLogDate());
		empLogRevision.setEmployeeCode(this.getCurrentUser().getCredentials().getEmployeeCode());
		empLogRevision.setRevisionType(new EmployeeLogRevisionTypeEnum(revisionType));
		
		String debugMessage = String.format("EmployeeLogRevision created for logDate: {%s} employeeCode: %s revisionType %s.", empLogRevision.getEmployeeLogDate(), empLogRevision.getEmployeeCode(), empLogRevision.getRevisionType().toDMOEnum() );
        ErrorLogHelper.RecordMessage(this.getContext(), debugMessage);
        
        EmployeeLogRevisionFacade facade = new EmployeeLogRevisionFacade(this.getContext(), this.getCurrentUser());
        facade.Save(empLogRevision);
        
	}
	
	public boolean SubmitAllEmployeeLogRevisionItemsToDMO()
	{
        boolean isSuccesful = false;

        if (this.getIsNetworkAvailable())
        {
            try
            {
                // first fetch all unsubmitted records
            	EmployeeLogRevisionFacade facade = new EmployeeLogRevisionFacade(this.getContext());
                List<EmployeeLogRevision> unSubmittedItems = facade.FetchUnsubmittedWithLimit();

                // are there any to send?
                if (unSubmittedItems != null && unSubmittedItems.size() > 0)
                {
                    // second, attempt to send the entire list to DMO                     
                	EmployeeLogRevision[] listToSend = unSubmittedItems.toArray(new EmployeeLogRevision[unSubmittedItems.size()]);
                                   
                	RESTWebServiceHelper rwsh = new RESTWebServiceHelper(getContext());
                	rwsh.SubmitEmployeeLogRevisions(listToSend);                	

                    // third, mark all as submitted successfully
                    facade.MarkSubmitted(unSubmittedItems);
                }

                isSuccesful = true;
            }
			catch (JsonSyntaxException jse)
			{
				this.HandleException(jse);			
			}
    		catch (IOException ioe) {
    			this.HandleException(ioe);			
    		}
        }

        return isSuccesful;
	}
}
