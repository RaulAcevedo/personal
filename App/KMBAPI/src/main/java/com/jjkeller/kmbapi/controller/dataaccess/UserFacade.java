package com.jjkeller.kmbapi.controller.dataaccess;

import android.content.Context;

import com.jjkeller.kmbapi.configuration.LoginCredentials;
import com.jjkeller.kmbapi.controller.dataaccess.db.UserPersist;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.controller.utility.DateUtility;

public class UserFacade extends FacadeBase{
	
	public UserFacade(Context ctx, User user)
	{
		super(ctx, user);
	}
	
	public LoginCredentials Fetch(String username)
	{
		UserPersist<LoginCredentials> persist = new UserPersist<LoginCredentials>(LoginCredentials.class, this.getContext(), this.getCurrentUser());
		return persist.Fetch(username);
	}
	
	public void Save(LoginCredentials credentials)
	{
		UserPersist<LoginCredentials> persist = new UserPersist<LoginCredentials>(LoginCredentials.class, this.getContext(), this.getCurrentUser());		
		persist.Persist(credentials);
	}

	public void Logout()
	{
		this.getCurrentUser().getCredentials().setLastLogoutTimestampUtc(DateUtility.getCurrentDateTimeUTC());		
		this.Save(this.getCurrentUser().getCredentials());
	}

	public LoginCredentials IsAuthenticated(String username, String password)
	{
		LoginCredentials credentials = this.Fetch(username);
		if (credentials != null)
		{
			if (credentials.getPassword().compareTo(password) == 0)
			{
				credentials.setLastLoginTimestampUtc(DateUtility.getCurrentDateTimeUTC());
				this.Save(credentials);
			}
			else
			{
				credentials = null;
			}
		}
		
		return credentials;
	}
}
