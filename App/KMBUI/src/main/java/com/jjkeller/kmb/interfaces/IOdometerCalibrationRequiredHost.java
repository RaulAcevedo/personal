package com.jjkeller.kmb.interfaces;

import com.jjkeller.kmb.share.BaseActivity;

public interface IOdometerCalibrationRequiredHost {
	
	public BaseActivity getActivity();
	
	public void OnOdometerCalibrationRequired(boolean calibrationRequired);
}
