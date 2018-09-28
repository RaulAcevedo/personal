package com.jjkeller.kmbapi.controller;

import android.content.Context;

import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.jjkeller.kmbapi.R;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.dataaccess.FeatureToggleFacade;
import com.jjkeller.kmbapi.controller.dataaccess.db.FeatureTogglePersist;
import com.jjkeller.kmbapi.controller.share.ControllerBase;
import com.jjkeller.kmbapi.controller.share.KmbApplicationException;
import com.jjkeller.kmbapi.controller.utility.RESTWebServiceHelper;
import com.jjkeller.kmbapi.featuretoggle.FeatureToggleService;
import com.jjkeller.kmbapi.proxydata.FeatureToggle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FeatureToggleController extends ControllerBase {

	public FeatureToggleController(Context ctx){
		super(ctx);
	}

	public void SetSelectiveFeatureTogglesEnabled(boolean isEnabled)
	{
		GlobalState.getInstance().getFeatureService().setSelectiveFeatureTogglesEnabled(isEnabled);
	}

	public void SetEldMandateEnabled(boolean isEnabled)
	{
		GlobalState.getInstance().getFeatureService().setIsEldMandateEnabled(isEnabled);
	}
	
	public void SetIgnoreServerTime(boolean isEnabled)
	{
		GlobalState.getInstance().getFeatureService().setIgnoreServerTime(isEnabled);
	}
	
	public void SetShowDebugFunctions(boolean isEnabled)
	{
		GlobalState.getInstance().getFeatureService().setShowDebugFunctions(isEnabled);
	}
	
	public void SetUseCloudServices(boolean isEnabled)
	{
		GlobalState.getInstance().getFeatureService().setUseCloudServices(isEnabled);
	}
	
	public void SetDefaultTripInformation(boolean isEnabled) 
	{
		GlobalState.getInstance().getFeatureService().setDefaultTripInformation(isEnabled);
	}
	
	public void SetAlkCopilotEnabled(boolean isEnabled)
	{
		GlobalState.getInstance().getFeatureService().setAlkCopilotEnabled(isEnabled);
	}
	
	public void SetPersonalConveyanceEnabled(boolean isEnabled)
	{
		GlobalState.getInstance().getFeatureService().setPersonalConveyanceEnabled(isEnabled);
	}

	public void SetIgnoreFirmwareUpdate(boolean isEnabled)
	{
		GlobalState.getInstance().getFeatureService().setIgnoreFirmwareUpdate(isEnabled);
	}
	
	public void SetForceComplianceTabletMode(boolean isEnabled)
	{
		GlobalState.getInstance().getFeatureService().setForceComplianceTabletMode(isEnabled);
	}

	public void SetSetMobileStartTimestampToNull(boolean isEnabled)
	{
		GlobalState.getInstance().getFeatureService().setSetMobileStartTimestampToNull(isEnabled);
	}

	public void SetHyrailEnabled(boolean isEnabled)
	{
		GlobalState.getInstance().getFeatureService().setHyrailEnabled(isEnabled);
	}

	public void SetNonRegDrivingEnabled(boolean isEnabled)
	{
		GlobalState.getInstance().getFeatureService().setNonRegDrivingEnabled(isEnabled);
	}

	public void SetGeotabInjectDataStallsEnabled(boolean isEnabled)
	{
		GlobalState.getInstance().getFeatureService().setGeotabInjectDataStallsEnabled(isEnabled);
	}

	public void SetForceCrashesEnabled(boolean isEnabled)
	{
		GlobalState.getInstance().getFeatureService().setIsForceCrashesEnabled(isEnabled);
	}

	public void DownloadAndApplyFeatureToggles(String companyId) throws KmbApplicationException
	{
		ArrayList<FeatureToggle> featureToggleList;

		try
		{
			RESTWebServiceHelper rwsh = new RESTWebServiceHelper(getContext());
			featureToggleList = rwsh.DownloadFeatureToggles(companyId);

            if (featureToggleList.size() > 0) {
                //Apply the downloaded FeatureToggles and then Save to table for offline logins.
                this.ApplyFeatureToggles(featureToggleList);
    			this.SaveFeatureToggles(featureToggleList);
            }
		}
		catch (JsonSyntaxException jse)
		{
			this.HandleExceptionAndThrow(jse, this.getContext().getString(R.string.downloadfeaturetoggles), this.getContext().getString(R.string.exception_webservicecommerror), this.getContext().getString(R.string.exception_serviceunavailable));
		}
		catch (JsonParseException e)
		{
			// when connected to a network, but unable to get to webservice "e" is null at times
			if (e == null)
				e = new JsonParseException(JsonParseException.class.getName());
			this.HandleExceptionAndThrow(e, this.getContext().getString(R.string.downloadfeaturetoggles), this.getContext().getString(R.string.exception_webservicecommerror), this.getContext().getString(R.string.exception_serviceunavailable));
		}
		catch (IOException ioe)
		{
			this.HandleExceptionAndThrow(ioe, this.getContext().getString(R.string.downloadfeaturetoggles), this.getContext().getString(R.string.exception_webservicecommerror), this.getContext().getString(R.string.exception_serviceunavailable));
		}
	}

	private void SaveFeatureToggles(ArrayList<FeatureToggle> featureToggleList) {
		FeatureToggleFacade facade = new FeatureToggleFacade(this.getContext(), getCurrentUser());
		facade.Save(featureToggleList);
	}

	private void ApplyFeatureToggles(List<FeatureToggle> featureToggleList){
		
		for(FeatureToggle ft : featureToggleList)
		{
			switch (ft.getName())
			{
				case "AlkCopilotEnabled":
					this.SetAlkCopilotEnabled(ft.getState());
					break;
				case "DefaultTripInformation":
					this.SetDefaultTripInformation(ft.getState());
					break;
				case "ELDMandate":
					this.SetEldMandateEnabled(ft.getState());
					break;
				case "ForceComplianceTabletMode":
					this.SetForceComplianceTabletMode(ft.getState());
					break;
				case "ForceCrashes":
					this.SetForceCrashesEnabled(ft.getState());
					break;
				case "GeotabInjectDataStallsEnabled":
					this.SetGeotabInjectDataStallsEnabled(ft.getState());
					break;
				case "HyrailEnabled":
					this.SetHyrailEnabled(ft.getState());
					break;
				case "IgnoreFirmwareUpdate":
					this.SetIgnoreFirmwareUpdate(ft.getState());
					break;
				case "IgnoreServerTime":
					this.SetIgnoreServerTime(ft.getState());
					break;
				case "NonRegDrivingEnabled":
					this.SetNonRegDrivingEnabled(ft.getState());
					break;
				case "PersonalConveyanceEnabled":
					this.SetPersonalConveyanceEnabled(ft.getState());
					break;
				case "SelectiveFeatureTogglesEnabled":
					this.SetSelectiveFeatureTogglesEnabled(ft.getState());
					break;
				case "SetMobileStartTimestampToNull":
					this.SetSetMobileStartTimestampToNull(ft.getState());
					break;
				case "ShowDebugFunctions":
					this.SetShowDebugFunctions(ft.getState());
					break;
				case "UseCloudServices":
					this.SetUseCloudServices(ft.getState());
					break;
			}
		}
	}


}
