package com.jjkeller.kmbapi.controller.utility;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.widget.RemoteViews;

import com.jjkeller.kmbapi.R;
import com.jjkeller.kmbapi.common.TimeKeeper;
import com.jjkeller.kmbapi.configuration.GlobalState;

import java.util.Date;

public class NotificationUtilities {
	public static final int APPRUNNING_ID = 1;
	public static final int HOURSWARNING_ID = 2;
	public static final int EOBRFAILURE_ID = 3;

	/**
		Creates or updates the notification that display KMB Icon in the status bar to indicate
		the application is running.  Clicking the notification will return user to the last place they
		were in the application.
	*/	
	public static void UpdateAppRunningNotification(Context ctx, Class<?> redirectTo, String notificationMsg)
	{
		// Create intent to fire when notification is clicked
		// Should return user to current location in app (where they left off)
		Intent notificationIntent = new Intent(ctx, redirectTo);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        DoNotification(ctx, notificationIntent, APPRUNNING_ID, R.drawable.kmbicon, "KellerMobile", notificationMsg, Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT, 0);
	}
	
	public static Notification GetAppRunningNotification(Context ctx, Class<?> redirectTo, String notificationMsg)
	{
		Intent notificationIntent = new Intent(ctx, redirectTo);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        return BuildNotification(ctx, notificationIntent, APPRUNNING_ID, R.drawable.kmbicon, "KellerMobile", notificationMsg, Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT, 0);
	}
	
	/**
	 * Add/update a notification to the status bar
	 */
	public static void AddNotification(Context ctx, Class<?> redirectTo, int notificationId, int icon, String notificationHeader, String notificationMsg)
	{
		/* TODO This code may need to be updated when final implementation of our notifications is done.
		Will be dependent on what activities we redirect to.  Only tested with RodsEntry so far. */ 
		
		// Create intent to fire when notification is clicked
		Intent notificationIntent = new Intent(ctx, redirectTo);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        DoNotification(ctx, notificationIntent, notificationId, icon, notificationHeader, notificationMsg);
	}

	/**
	 * Add/update a notification to the status bar
	 */
	public static void AddNotification(Context ctx, Class<?> redirectTo, int notificationId, int icon, String notificationHeader, String notificationMsg, int flags, int defaults, int layout)
	{
		/* TODO This code may need to be updated when final implementation of our notifications is done.
		Will be dependent on what activities we redirect to.  Only tested with RodsEntry so far. */ 
		
		// Create intent to fire when notification is clicked
		Intent notificationIntent = new Intent(ctx, redirectTo);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        DoNotification(ctx, notificationIntent, notificationId, icon, notificationHeader, notificationMsg, flags, defaults, layout);
	}

	private static void DoNotification(Context ctx, Intent intent, int notificationId, int icon, String header, String msg)
	{
        DoNotification(ctx, intent, notificationId, icon, header, msg, 0, 0);
	}
	
	private static void DoNotification(Context ctx, Intent intent, int notificationId, int icon, String header, String msg, int flags, int defaults)
	{
		// Do the same as below, but discard the Notification object returned
		NotificationUtilities.BuildNotification(ctx, intent, notificationId, icon, header, msg, flags, defaults);
	}	

	private static Notification BuildNotification(Context ctx, Intent intent, int notificationId, int icon, String header, String msg, int flags, int defaults)
	{
		// Creates text/info displayed in notification and set flags (i.e. ongoing, no clear)
		PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0, intent, 0);
		Notification notification = new Notification.Builder(ctx)
										.setContentIntent(contentIntent)
										.setContentTitle(header)
										.setContentText(msg)
										.setSmallIcon(icon)
										.setWhen(System.currentTimeMillis()).build();

		notification.flags = flags;
		notification.defaults = defaults;

		// Add or update notification
		NotificationManager mNotificationManager = (NotificationManager)ctx.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(notificationId, notification);
		return notification;
	}
	
	private static void DoNotification(Context ctx, Intent intent, int notificationId, int icon, String header, String msg, int flags, int defaults, int layout )
	{
		// Create notification with icon to display		
		Notification notification = new Notification(icon, null, System.currentTimeMillis());

		RemoteViews contentView = new RemoteViews(GlobalState.getInstance().getPackageName(), layout);
		contentView.setImageViewResource(R.id.image, icon);
        contentView.setTextViewText(R.id.title, ctx.getString(R.string.notification_title));
		Date now = TimeKeeper.getInstance().now();
		contentView.setTextViewText(R.id.text, String.format("%s (%s)", msg, DateUtility.getHomeTerminalTime12HourFormat().format(now)));
		notification.contentView = contentView;

        // Creates text/info displayed in notification and set flags (i.e. ongoing, no clear)
		PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0, intent, 0);
		notification.contentIntent = contentIntent;
		notification.flags = flags;
		notification.defaults = defaults;

		// Add or update notification
		NotificationManager mNotificationManager = (NotificationManager)ctx.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(notificationId, notification);
	}	

	/**
	 Removes all notifications from status bar.  Typically would be done when the app is shut down.
	*/
	public static void CancelAllNotifications(Context ctx)
	{
		NotificationManager mNotificationManager = (NotificationManager)ctx.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancelAll();
	}
	
	/**
	 Removes specified notification from status bar.
	*/
	public static void CancelNotification(Context ctx, int notificationId)
	{
		NotificationManager mNotificationManager = (NotificationManager)ctx.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(notificationId);
	}
}
