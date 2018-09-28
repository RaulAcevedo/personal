package com.jjkeller.kmbapi.controller.share;

import android.content.Context;
import android.util.Log;

import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.configuration.KmbUserInfo;
import com.jjkeller.kmbapi.configuration.LoginCredentials;
import com.jjkeller.kmbapi.controller.LogEntryController;
import com.jjkeller.kmbapi.controller.interfaces.IController;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.controller.utility.ErrorLogHelper;
import com.jjkeller.kmbapi.controller.utility.NetworkUtilities;

import java.util.ArrayList;
import java.util.Date;

public class ControllerBase
	implements IController
{
	protected ControllerBase(Context ctx)
	{
		_ctx = ctx;
	}

	// Context
	private Context _ctx = null;
	public Context getContext()
	{
		return this._ctx;
	}

	// User
	public User getCurrentUser()
	{
		return GlobalState.getInstance().getCurrentUser();
	}

	public void setCurrentUser(User currentUser)
	{
		GlobalState.getInstance().setCurrentUser(currentUser);
	}

	public ArrayList<User> getLoggedInUserList()
	{
		return GlobalState.getInstance().getLoggedInUserList();
	}

	public void setLoggedInUserList(ArrayList<User> loggedInUserList)
	{
		GlobalState.getInstance().setLoggedInUserList(loggedInUserList);
	}

	// Miscellaneous
	public boolean getIsNetworkAvailable()
	{
		return NetworkUtilities.VerifyNetworkConnection(this._ctx);
	}

	// Miscellaneous
	public boolean getIsWebServicesAvailable()
	{
		return NetworkUtilities.VerifyWebServiceConnection(this._ctx);
	}

	/**
	 * Answer the current clock time, in the user's home terminal time zone.
	 */
	public Date getCurrentClockHomeTerminalTime()
	{
        return DateUtility.CurrentHomeTerminalTime(this.getCurrentUser());
	}

	public Date getCurrentClockHomeTerminalTime(User user)
	{
		return DateUtility.CurrentHomeTerminalTime(user);
	}

	/**
	 * The user which is the currently designated driver of the vehicle.
	 * Note: this is managed in state data.
	 */
	public User getCurrentDesignatedDriver()
	{
		// fetch from state
		return GlobalState.getInstance().getCurrentDesignatedDriver();
	}

	public void setCurrentDesignatedDriver(User user)
	{
		// save to state
		GlobalState.getInstance().setCurrentDesignatedDriver(user);
	}

	public void UpdateLogCheckerWebServiceCredentials(LoginCredentials credentials)
	{
		if (credentials != null)
		{
			KmbUserInfo kmbUserInfo = new KmbUserInfo();

			kmbUserInfo.setKmbUsername(credentials.getUsername());
			kmbUserInfo.setKmbPassword(credentials.getPassword());
			kmbUserInfo.setDmoEmployeeId(credentials.getEmployeeId());

			GlobalState.getInstance().setKmbUserInfo(kmbUserInfo);

			GlobalState.getInstance().getCurrentUser().setCredentials(credentials);
		}
	}

	/**
	 * Switch the context of the app to this user.
	 */
	protected void SwitchUserContext(User usr)
	{
		// found the user to switch to
		this.setCurrentUser(usr);

		// register the Home Terminal Timezone with the DateUtilities so that all Date operations will be correct
        DateUtility.setHomeTerminalTimeDateFormatTimeZone(usr.getHomeTerminalTimeZone().toTimeZone());
		
		// have the effected controllers change context
		LogEntryController rodsController = new LogEntryController(getContext());
		rodsController.SwitchUserContext();
	}

	@Override
	public void HandleException(Exception ex)
	{
		this.HandleException(ex, "");
	}

	@Override
	public void HandleException(Exception ex, String tag)
	{
		Log.e(tag, ex.toString());
		ErrorLogHelper.RecordException(this.getContext(), ex);
	}

	@Override
	public void HandleExceptionAndThrow(Exception ex, String tag, String caption, String displayMessage) throws KmbApplicationException
	{
		Log.e(tag, ex.toString());
		ErrorLogHelper.RecordException(this.getContext(), ex);

		KmbApplicationException kae = new KmbApplicationException();
		kae.setCaption(caption);
		kae.setDisplayMessage(displayMessage);
		throw kae;
	}
}
