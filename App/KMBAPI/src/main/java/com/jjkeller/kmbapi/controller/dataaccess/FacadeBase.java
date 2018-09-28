package com.jjkeller.kmbapi.controller.dataaccess;

import android.content.Context;

import com.jjkeller.kmbapi.controller.share.User;

public class FacadeBase {

	private User user;
	private Context ctx;
	
	public FacadeBase(Context ctx)
	{
		this.ctx = ctx;
	}
	
	public FacadeBase(Context ctx, User user)
	{
		this.user = user;
		this.ctx = ctx;
	}
	
	public Context getContext()
	{
		return this.ctx;		
	}
	public void setContext(Context ctx)
	{
		this.ctx = ctx;
	}
	
	public User getCurrentUser()
	{
		return this.user;
	}
	public void setCurrentUser(User user)
	{
		this.user = user;
	}
}
