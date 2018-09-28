package com.jjkeller.kmb.share;

import android.content.Context;
import android.os.AsyncTask;

import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.interfaces.IAPIController;
import com.jjkeller.kmbapi.controller.share.MandateObjectFactory;

public class MobileDeviceHandler
{
	private static Context _context;

	public MobileDeviceHandler(Context context)
	{
		_context = context;
	}

	public void UploadMobileDeviceInfo()
	{
		mUploadMobileDeviceInfoTask = new UploadMobileDeviceInfo();
		mUploadMobileDeviceInfoTask.execute();
	}

	private static UploadMobileDeviceInfo mUploadMobileDeviceInfoTask;
	private class UploadMobileDeviceInfo extends AsyncTask<Void, Void, Boolean>
	{
		@Override
		protected Boolean doInBackground(Void... params)
		{
			try
			{
				IAPIController logController = MandateObjectFactory.getInstance(_context,GlobalState.getInstance().getFeatureService()).getCurrentEventController();
				logController.SubmitMobileDeviceInfo();
				return true;
			}
			catch (Exception e)
			{
				return false;
			}
		}
	}
}