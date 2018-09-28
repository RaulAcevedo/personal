package com.jjkeller.kmb.share;

import android.content.Context;
import android.content.Intent;
import android.os.Process;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.jjkeller.kmb.CrashDialog;
import com.jjkeller.kmb.RodsEntry;
import com.jjkeller.kmb.crashlytics.Utils;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.utility.ErrorLogHelper;
import com.jjkeller.kmbapi.controller.utility.NotificationUtilities;
import com.jjkeller.kmbui.R;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Locale;


public class CustomGlobalExceptionHandler implements UncaughtExceptionHandler {

    private UncaughtExceptionHandler defaultUEH;

    private Context mContext;
    private static final String spacechar = " ";

    public CustomGlobalExceptionHandler(Context ctx) {
        mContext = ctx;

        this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
    }

    public void uncaughtException(Thread t, Throwable e) {

        Utils.LogCrashlytics(e);

        Log.e("KMB", "Unhandled exception", e);

        ErrorLogHelper.RecordMessage(mContext, "Unhandled exception has occurred, abort the app.");
        ErrorLogHelper.RecordException(mContext, e);

        // Create a message to display to the user when an unhandled exception occurs
        StackTraceElement[] stackTraceArray = e.getStackTrace();
        StringBuilder sb = new StringBuilder();

        sb.append(String.format(Locale.getDefault(), "%s%s%s",
                mContext.getResources().getString(R.string.msg_unhandledexceptionheader),
                mContext.getResources().getString(R.string.newline),
                mContext.getResources().getString(R.string.newline)));

        sb.append(String.format(Locale.getDefault(), "%s%s",
                mContext.getResources().getString(R.string.msg_unhandledexceptioncause),
                spacechar));

        if (e.getCause() != null)
            sb.append(String.format(Locale.getDefault(), "%s", e.getCause().toString()));
        else if (e.getMessage() != null)
            sb.append(String.format(Locale.getDefault(), "%s", e.getMessage()));
        else
            sb.append(String.format(Locale.getDefault(), "%s", e.toString()));

        sb.append(String.format(Locale.getDefault(), "%s%s",
                mContext.getResources().getString(R.string.newline),
                mContext.getResources().getString(R.string.newline)));

        sb.append(String.format(Locale.getDefault(), "%s%s",
                mContext.getResources().getString(R.string.msg_unhandledexceptionlocation),
                spacechar));

        if (stackTraceArray.length > 0)
            sb.append(String.format(Locale.getDefault(), "%s%s",
                    mContext.getResources().getString(R.string.newline),
                    stackTraceArray[0].toString()));
        if (stackTraceArray.length > 1)
            sb.append(String.format(Locale.getDefault(), "%s%s",
                    mContext.getResources().getString(R.string.newline),
                    stackTraceArray[1].toString()));

        sb.append(String.format(Locale.getDefault(), "%s%s%s",
                mContext.getResources().getString(R.string.newline),
                mContext.getResources().getString(R.string.newline),
                mContext.getResources().getString(R.string.msg_unhandledexceptionfooter)));

        try {
            // Remove notifications when exiting app
            NotificationUtilities.CancelAllNotifications(mContext);

            // 9/19/12 JHM - Added code to gracefully handle stopping the EobrService
            // and removal of the notification required for the Foreground behavior.
            GlobalState.getInstance().setIsCrashDetected(true);
            if (GlobalState.getInstance().getEobrService() != null) {
                GlobalState.getInstance().getEobrService().stopForeground(true);
                GlobalState.getInstance().getEobrService().stopSelf();
            }

			/* End rodsentry activity */
            Intent killRodsEntry = new Intent(mContext, RodsEntry.class);
            killRodsEntry.putExtra(mContext.getString(R.string.exit), true);
            killRodsEntry.putExtra(mContext.getString(R.string.crash_exit), true);
            killRodsEntry.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            mContext.startActivity(killRodsEntry);

            // Show the new activity, which is our dialog with the error information for the user
            Intent intent = new Intent(mContext, CrashDialog.class);
            intent.putExtra(mContext.getResources().getString(R.string.extra_crashmsg), sb.toString());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  // 9/19/12 JHM - Start new task
            mContext.startActivity(intent);

        } catch (Throwable excp) {
            // 4/26/11 JHM - Pass handling back to system default if our custom dialog fails
            defaultUEH.uncaughtException(t, excp);
        } finally {
            Process.killProcess(Process.myPid());
        }
    }

}
