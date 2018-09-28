package com.jjkeller.kmb;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.LoginController;
import com.jjkeller.kmbapi.controller.utility.NetworkUtilities;
import com.jjkeller.kmbui.R;

public class IncomingReceiver extends BroadcastReceiver {  

	private static final String CHECKACTIVATION_ACTION = "com.jjkeller.kmb.checkactivation";
	private static final String ACTIVATE_ACTION = "com.jjkeller.kmb.activate";
	private static final String CHECKLOGIN_ACTION = "com.jjkeller.kmb.checklogin";

	
	public boolean getIsNetworkAvailable(Context ctx)
	{
		return NetworkUtilities.VerifyNetworkConnection(ctx);
	}
	
	@Override 
	public void onReceive(Context context, Intent intent) {  
		Bundle results = getResultExtras(true);
		
		if(!NetworkUtilities.VerifyNetworkConnection(context))
		{
			results.putBoolean(context.getString(R.string.isnetworkavailable), false);
		}
		else
		{
			if(intent.getAction().equals(CHECKACTIVATION_ACTION)){
				new CheckActivation(context).execute();
			
			}
			else if (intent.getAction().equals(ACTIVATE_ACTION)){
				String activationCode = intent.getExtras().getString(context.getApplicationContext().getString(R.string.activationcode));
				new Activate(context).execute(activationCode);

			}
			else if (intent.getAction().equals(CHECKLOGIN_ACTION)) {
				LoginController loginController = new LoginController(context);
				if(loginController.getLoggedInUserList().size()>0){
					results.putBoolean(context.getString(R.string.isloggedin), true);
				}
				else{
					results.putBoolean(context.getString(R.string.isloggedin), false);
				}
					
			}
		}
	}  
	
	private class CheckActivation extends AsyncTask<Void, Void, Boolean> {
		Context ctx;
		
		public CheckActivation(Context ctx)
		{
			this.ctx = ctx;
		}
		
		protected void onPreExecute()
		{
		}
		
		protected Boolean doInBackground(Void... params) {
			
	    	if (GlobalState.getInstance().getCompanyConfigSettings(ctx) == null) {
	    		return false;
	    	}
	    	else {
	    		return true;
	    	}
			
		}

	}
	
	private class Activate extends AsyncTask<String, Void, Boolean> {
		Context ctx;
		String failureMessage = "";
		
		public Activate(Context ctx)
		{
			this.ctx = ctx;
		}
		
		protected void onPreExecute()
		{
		}
		
		protected Boolean doInBackground(String... arg0) {
			boolean isActivated = false;
			LoginController loginController = new LoginController(ctx);
			String activationCode = arg0[0];
			try{
				boolean success = loginController.DownloadCompanyConfigSettings(activationCode);
				if(success){
					if(GlobalState.getInstance().getCompanyConfigSettings(ctx) == null){
						isActivated = false;
						failureMessage = ctx.getString(R.string.invalid_activationcode);
					}
					else{
						isActivated = true;
					}
				}
			} catch(Exception e){
				isActivated = false;
				failureMessage = ctx.getString(R.string.msg_activationfailed);
			}
			
			return isActivated;
		}

	}
	
}