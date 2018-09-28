package com.jjkeller.kmbapi.featuretoggle;

import android.content.Context;
import android.content.SharedPreferences;

import com.jjkeller.kmbapi.R;
import com.jjkeller.kmbapi.configuration.AppSettings;
import com.jjkeller.kmbapi.configuration.GlobalState;

public class FeatureToggle<T> implements IFeatureToggle {

	public FeatureToggle(Class<T> type, AppSettings appSettings) {
		this.type = type;
		_appSettings = appSettings;
	}

	private AppSettings _appSettings = null;
	public AppSettings getAppSettings()
	{
		return this._appSettings;
	}
	
	private final Class<T> type;
	public String Name() {
		return type.getSimpleName();
	}

	public boolean IsEnabled() {
		boolean result = _appSettings == null ? false : _appSettings.getIsFeatureEnabled(this.Name());
		
		// lookup the user's saved preference for this toggle,
		Context ctx = GlobalState.getInstance().getApplicationContext();
		SharedPreferences userPref = ctx.getSharedPreferences(ctx.getString(R.string.sharedpreferencefile), 0);
		if(userPref.contains(this.Name())){
			// if the toggle exists in preferences, then use it
			result = userPref.getBoolean(this.Name(), false);			
		}
						
		return result;
	}

	public void setIsEnabled(boolean isEnabled){
		// save the toggle value to the user's saved preference for this toggle,
		Context ctx = GlobalState.getInstance().getApplicationContext();
		SharedPreferences userPref = ctx.getSharedPreferences(ctx.getString(R.string.sharedpreferencefile), 0);
		userPref.edit().putBoolean(this.Name(), isEnabled).commit();
	}
}
