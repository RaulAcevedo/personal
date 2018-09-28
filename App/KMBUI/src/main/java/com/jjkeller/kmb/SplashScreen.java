package com.jjkeller.kmb;

import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.VideoView;

import com.actionbarsherlock.view.Menu;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.EOBR.FirmwareImageMover;
import com.jjkeller.kmbapi.controller.SystemStartupController;
import com.jjkeller.kmbapi.controller.interfaces.ISystemStartupProgress;
import com.jjkeller.kmbapi.controller.utility.NotificationUtilities;
import com.jjkeller.kmbui.R;

import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

public class SplashScreen extends BaseActivity implements OnCompletionListener {
	private static final String TAG = SplashScreen.class.getSimpleName();
    
	public static final String SKIP_SPLASH_ANIMATION_EXTRA_KEY = "SkipSplash";
	private static final int SPLASH_DISPLAY_LENGTH = 2000;

    private boolean _skipSplashAnimation;
    private View _splashAnimationWrapper;
    private VideoView _splashAnimation;
    private View _splashContentWrapper;
	private TextView _tvlblversionnumber;
	private TextView _tvlblmessage;
	
	private ISystemStartupProgress _onSystemStartupProgress;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Log.v(TAG, "Initialization of Crashlytics");
		//Fabric.with(this, new Crashlytics());
		setContentView(R.layout.splash);

		new FirmwareImageMover().moveBundledFirmwareToInternalDir(this.getApplicationContext());

		LockScreenRotation();

		// Add icon to status bar to indicate the app is running
		NotificationUtilities.UpdateAppRunningNotification(this, StartupActivity.class, "Application is running.");

		if (getIntent() != null)
			_skipSplashAnimation = getIntent().getBooleanExtra(SKIP_SPLASH_ANIMATION_EXTRA_KEY, false);
		
		this.findActivityControls();
	}

	@Override
	protected void onResume()
	{		
		super.onResume();
		
		this.loadControls();
	}
	
	@Override
	public void Return()
	{
		// Remove notifications when exiting app
		NotificationUtilities.CancelAllNotifications(this);

		Process.killProcess(Process.myPid());

		this.finish();
	}

	@Override
	protected void findActivityControls()
	{
		_splashAnimationWrapper = this.findViewById(R.id.splash_animation_wrapper);
		_splashAnimation = (VideoView)this.findViewById(R.id.splash_animation);
		_splashContentWrapper = this.findViewById(R.id.splash_content_wrapper);
		_tvlblversionnumber = (TextView)this.findViewById(R.id.lblversionnumber);
		_tvlblmessage = (TextView)this.findViewById(R.id.lblsplashmessage);
	}
    
	@Override
	protected void loadControls()
	{
		try
		{
			String version = GlobalState.getInstance().getPackageVersionName();
			_tvlblversionnumber.setText(version);
		}
		catch (Exception e)
		{
			this.Return();
		}

		// hide the splash animation when showing Feature Toggle (aka debug functions)
		if(GlobalState.getInstance().getAppSettings(this).getShowFeatureToggles()) {
			_skipSplashAnimation = true;
		}

		if (_splashAnimation != null && !_skipSplashAnimation)
		{
			// First show the video
			String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.splash_animation;
			_splashAnimation.setVideoPath(videoPath);
			_splashAnimation.setOnCompletionListener(this);
			_splashAnimation.start();
		}
		else
		{
			// Just start the startup process
			startSystemStartupProcess();
		}
	}

	public void onCompletion(MediaPlayer mediaPlayer)
	{
		// The splash video has completed its playback, so show the startup process
		startSystemStartupProcess();
	}
	
	private void startSystemStartupProcess()
	{
		if (_splashAnimationWrapper != null)
			_splashAnimationWrapper.setVisibility(View.GONE);
		if (_splashContentWrapper != null)
			_splashContentWrapper.setVisibility(View.VISIBLE);
		new Handler().postDelayed(SystemStartupProcessTask, SPLASH_DISPLAY_LENGTH);
	}
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		return true;
	}
    
    protected void LaunchKMB()
    {
    	if (GlobalState.getInstance().getCompanyConfigSettings(this) == null)
    	{
            this.finish();

    		/* Company config not created, so display activation screen */
            this.startActivity(Activation.class);
    	}
    	else
    	{
    		String message = String.format("%s %s '%s'", this.getString(R.string.actionbar_title), this.getString(R.string.mainappstartuplabel), _tvlblversionnumber.getText());
    		com.jjkeller.kmbapi.controller.utility.ErrorLogHelper.RecordMessage(this, message); 

            OnSystemStartupProgress onSystemStartupProgress = new OnSystemStartupProgress();
            this.setOnSystemStartupProgress(onSystemStartupProgress);
        	
            boolean isDatabaseOk = SplashScreen.this.getMyController().PerformSystemStartup_CheckDatabase(SplashScreen.this._onSystemStartupProgress);	            
        	if(isDatabaseOk)
        	{            	
	    		// Company config does exist, company already activated,
	    		// finish splash activity and launch login screen
	    		SplashScreen.this.finish();

	            Intent loginIntent = new Intent(SplashScreen.this, com.jjkeller.kmb.Login.class);
	            SplashScreen.this.startActivity(loginIntent);		            
        	}
    	}
    }

	protected SystemStartupController getMyController()
	{
		return (SystemStartupController)this.getController();
	}

	@Override
	protected void InitController()
	{
		this.setController(new SystemStartupController(this));
	}

	public void setOnSystemStartupProgress(ISystemStartupProgress systemStartupProgress)
	{
		this._onSystemStartupProgress = systemStartupProgress;
	}

	public class OnSystemStartupProgress implements ISystemStartupProgress
	{
		public void onProgressChanged(String message)
		{
			_tvlblmessage.setText(message);
		}
	}

	private Runnable SystemStartupProcessTask = new Runnable()
	{
		public void run()
		{
			SplashScreen.this.LaunchKMB();
		}
	};
}
