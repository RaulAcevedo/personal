package com.jjkeller.kmb.share;

import android.content.Context;
import android.content.Intent;

import com.jjkeller.kmb.Login;
import com.jjkeller.kmb.RodsEntry;
import com.jjkeller.kmb.RptDailyHours;
import com.jjkeller.kmb.RptDutyStatus;
import com.jjkeller.kmb.RptGridImage;
import com.jjkeller.kmb.RptLogDetail;
import com.jjkeller.kmb.SubmitLogsViewOnly;
import com.jjkeller.kmb.ViewClocks;
import com.jjkeller.kmb.ViewHours;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.LoginController;
import com.jjkeller.kmbui.R;

import java.util.Arrays;
import java.util.Comparator;

public class ViewOnlyModeNavHandler
{
	private ViewOnlyModeActivity _currentActivity;
	private static Context _context;
	
	public ViewOnlyModeNavHandler(Context context)
	{
		_context = context;
	}
	
	public void setCurrentActivity(ViewOnlyModeActivity currentActivity)
	{
		_currentActivity = currentActivity;
	}
	
	public Intent handleMenuItemSelected(int itemPosition)
	{		
		if(GlobalState.getInstance().getIsViewOnlyMode())
		{
			Intent intent;
			ViewOnlyModeActivity selectedActivity = ViewOnlyModeActivity.getViewOnlyModeActivityFromIndex(itemPosition);
			
			if(_currentActivity != selectedActivity)
			{				
				if(selectedActivity == ViewOnlyModeActivity.EXIT)
				{
					LoginController loginController = new LoginController(_context);
					loginController.PerformReadOnlyLogout();
					GlobalState.getInstance().setIsViewOnlyMode(false);
					
					if(GlobalState.getInstance().getLoggedInUserList().size() > 0)
					{
						//there's another user logged in... go back to rods after logging out
						intent = new Intent(_context, RodsEntry.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					} else
					{
						intent = new Intent(_context, Login.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					}
				} else
				{
					intent = new Intent(_context, selectedActivity.activityClass);
				}
				
				return intent;
			}
			else
				return null;
		}
		else
			return null;
	}

	public String getActivityMenuItemList(String standardList)
	{
		if(GlobalState.getInstance().getIsViewOnlyMode())
			return ViewOnlyModeActivity.getActivityList();
		else
			return standardList;
	}
	
	public boolean getIsViewOnlyMode()
	{
		return GlobalState.getInstance().getIsViewOnlyMode();
	}
	
	public ViewOnlyModeActivity getCurrentActivity()
	{
		return _currentActivity;
	}
	
	public enum ViewOnlyModeActivity
	{
		VIEWGRID(0, _context.getString(R.string.viewonlymode_viewgrid), RptGridImage.class),
		VIEWHOURS(1, _context.getString(R.string.viewonlymode_viewhours), ViewHours.class),
		VIEWCLOCKS(2, _context.getString(R.string.viewonlymode_viewclocks), ViewClocks.class),
        VIEWRECAPINFO (3,_context.getString(R.string.viewonlymode_viewrecapinfo ), RptDailyHours.class ),
		SUMMARY(4, _context.getString(R.string.viewonlymode_summary), RptDutyStatus.class),
		DETAIL(5, _context.getString(R.string.viewonlymode_detail), RptLogDetail.class),
		EXIT(6, _context.getString(R.string.viewonlymode_exit), null),
		SUBMITLOGS(7, _context.getString(R.string.viewonlymode_submitlogs), SubmitLogsViewOnly.class);
		
		private final int index;
		private final String activityName;
		private final Class<?> activityClass;
		
		ViewOnlyModeActivity(int index, String name, Class<?> myClass)
		{
			this.index = index;
			this.activityName = name;
			this.activityClass = myClass;
		}
		
		public int index()
		{
			return index;
		}
		
		public String activityName()
		{
			return activityName;
		}
		
		public Class<?> activityClass()
		{
			return activityClass;
		}
		
		public static ViewOnlyModeActivity getViewOnlyModeActivityFromIndex(int index)
		{
			for(ViewOnlyModeActivity a : ViewOnlyModeActivity.values()) {
				if(a.index() == index)
					return a;
			}
			
			return null;
		}
		
		public static String getActivityList()
		{
			ViewOnlyModeActivity[] values = ViewOnlyModeActivity.values();
			
			Arrays.sort(values, new Comparator<ViewOnlyModeActivity>()
			{
				public int compare(ViewOnlyModeActivity v1, ViewOnlyModeActivity v2)
				{				
					//the indices should never be the same
					if(v1.index() < v2.index())
						return -1;
					else return 1;
				}
			});
			
			StringBuilder activityList = new StringBuilder();
			
			for(ViewOnlyModeActivity activity : values)
				activityList.append(activity.activityName()).append(',');
			
			//delete the last comma
			activityList.setLength(activityList.length() - 1);
			
			return activityList.toString();
		}
	}
}
