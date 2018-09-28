package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.jjkeller.kmb.interfaces.IExemptLogType.ExemptLogTypeFragActions;
import com.jjkeller.kmb.interfaces.IExemptLogType.ExemptLogTypeFragControllerMethods;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.LogEntryController;
import com.jjkeller.kmbapi.enums.ExemptLogTypeEnum;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbui.R;

import org.joda.time.DateTime;

public class ExemptLogTypeFrag extends BaseFragment{
	ExemptLogTypeFragActions actionsListener;
	ExemptLogTypeFragControllerMethods controlListener;
	
	private RadioGroup _radioExemptLogTypeGroup;
	private Button _btnOK;
	private EmployeeLog _employeeLog = null;
	private boolean _isNewLog = false;

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		// Get the current Employee Log
		//_employeeLog = controlListener.getMyController().GetEmployeeLog(DateTime.now().toDate());
		_employeeLog = controlListener.getMyController().GetLocalEmployeeLog(DateTime.now().toDate());

        // Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.f_exemptlogtype, container, false);
		findControls(v);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
	}
	
	protected void findControls(View v){	
		_radioExemptLogTypeGroup = (RadioGroup) v.findViewById(R.id.radExemptLogTypeGroup);
		
		_btnOK = (Button)v.findViewById(R.id.btnOK);
		
		_btnOK.setOnClickListener(
	            new OnClickListener() {
	            	public void onClick(View v) {
	            		actionsListener.handleOKButtonClick();
	            	}
	            });

		RadioButton radioStandardGrid =(RadioButton)_radioExemptLogTypeGroup.findViewById(R.id.radio_login_standardgridlog);
		RadioButton radioExempt =(RadioButton)_radioExemptLogTypeGroup.findViewById(R.id.radio_login_exemptlog);
		RadioButton radioExemptELDUse =(RadioButton)_radioExemptLogTypeGroup.findViewById(R.id.radio_login_exemptfromELDuse);
		TextView lblExemptLogMsg = (TextView)v.findViewById(R.id.lbllogtype_exemptmsg);

		// set visibility of Exempt from ELD use button
		boolean isELDMandateEnabled = GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled();
		boolean isExemptFromEldUse = controlListener.getMyLoginController().getCurrentUser().getExemptFromEldUse();

		if(_employeeLog != null && _employeeLog.getIsExemptFromELDUse())
		{
			lblExemptLogMsg.setVisibility(View.VISIBLE);
			radioStandardGrid.setVisibility(View.INVISIBLE);
			radioStandardGrid.setHeight(0);
			radioExempt.setVisibility(View.INVISIBLE);
			radioExempt.setHeight(0);
			radioExemptELDUse.setVisibility(View.INVISIBLE);
			radioExemptELDUse.setHeight(0);
			_btnOK.setVisibility(getView().INVISIBLE);
			_btnOK.setHeight(0);
		}
		else {
			lblExemptLogMsg.setVisibility(View.INVISIBLE);
			lblExemptLogMsg.setHeight(0);
			if (isELDMandateEnabled && isExemptFromEldUse && _employeeLog != null &&  _employeeLog.getExemptLogType().getValue() == ExemptLogTypeEnum.UNDEFINED) {
				radioExemptELDUse.setVisibility(View.VISIBLE);
			} else {
				radioExemptELDUse.setVisibility(View.INVISIBLE);
				radioExemptELDUse.setHeight(0);
			}

			// set visibility of Mobile Exempt Log Allowed button

			boolean isMobileExemptLogAllowed = controlListener.getMyLoginController().getCurrentUser().getIsMobileExemptLogAllowed();

			if (isMobileExemptLogAllowed) {
				radioExempt.setVisibility(View.VISIBLE);
			} else {
				radioExempt.setVisibility(View.INVISIBLE);
				radioExempt.setHeight(0);
			}
		}
	}
	
	@Override
    public void onAttach(Activity activity) {
		super.onAttach(activity);
        try {
        	actionsListener = (ExemptLogTypeFragActions) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement ExemptLogTypeFragActions");
        }
        
        try{
        	controlListener = (ExemptLogTypeFragControllerMethods) activity;
        } catch (ClassCastException e) {
        	throw new ClassCastException(activity.toString() + " must implement ExemptLogTypeFragControllerMethods");
        }
    }
	
	public int getDesignatedLogTypeRadioButtonIndex(){
		// get selected radio button from radioGroup
		int selectedId = _radioExemptLogTypeGroup.getCheckedRadioButtonId();
		
		// get index of radio button from radioGroup
		int index = _radioExemptLogTypeGroup.indexOfChild(_radioExemptLogTypeGroup.findViewById(selectedId));
		
		// index == 0 is Standard Grid Log
		// index == 1 is Exempt Grid Log
		// index == 2 is Exempt from ELD use
		return index;
	}
}
