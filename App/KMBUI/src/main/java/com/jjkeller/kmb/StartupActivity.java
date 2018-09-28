package com.jjkeller.kmb;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Window;

import com.jjkeller.kmb.share.BaseActivity;

import java.util.List;

//  The purpose of this class is to resolve a defect related to our  
//  "Application Running" icon/notification during it's first run (after install/Open).
//  The icon would start a new instance of the app and this class catches/prevents the issue.
//  Source found at https://github.com/cleverua/android_startup_activity
public class StartupActivity extends Activity {
	BroadcastReceiver mReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Remove Title (was displaying before the animated splash screen)
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		// 3/1/13 AMO: This will only occur on startup if they are using the LockedDown version of the app and they restart their device
		boolean showing = false;
		showing = BaseActivity.IsScreenLocked(this);

		if (showing) {
			try {
				// Use broadcast receiver to determine when the user unlocks the device
				IntentFilter filter = new IntentFilter(Intent.ACTION_USER_PRESENT);
				mReceiver = new receiverScreen();
				registerReceiver(mReceiver, filter);
			} catch (Exception e) {

			}
		} else {
			startUp();
		}
	}

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(mReceiver != null)
            unregisterReceiver(mReceiver);
    }

    // 3/1/13 AMO: Code normally run during onCreate, but wait for the screen unlock before executing
    protected void startUp(){
        Boolean needStart = false;

        needStart = needStartApp();
        
        if (needStart) {
            Intent i = new Intent(StartupActivity.this, SplashScreen.class);
            startActivity(i);
        }
        
        finish();
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // this prevents StartupActivity recreation on Configuration changes
        // (device orientation changes or hardware keyboard open/close).
        // just do nothing on these changes:
        super.onConfigurationChanged(null);
    }
    
    private boolean needStartApp() {
    	boolean needs = false;
        final ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final List<RunningTaskInfo> tasksInfo = am.getRunningTasks(1024);
        
        if (!tasksInfo.isEmpty()) {
            final String ourAppPackageName = getPackageName();
            RunningTaskInfo taskInfo;
            final int size = tasksInfo.size();
            
            for (int i = 0; i < size; i++) {
                taskInfo = tasksInfo.get(i);
                //Log.w("KMB", taskInfo.baseActivity.getPackageName());
                //Log.w("KMB", String.valueOf(taskInfo.numActivities));
                if (ourAppPackageName.equals(taskInfo.baseActivity.getPackageName())) {
                    // continue application start only if there is the only Activity in the task
                    // (BTW in this case this is the StartupActivity)
                	needs = (taskInfo.numActivities == 1);
                }
            }
        }
        
        return needs;
    }
    
    // 3/1/13 AMO: Wait for user interaction before starting app
	public class receiverScreen extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// Screen Unlocked
			if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
				startUp();
			}
		}

	}
}
