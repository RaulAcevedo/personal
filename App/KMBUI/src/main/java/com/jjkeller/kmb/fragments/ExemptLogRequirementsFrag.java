package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.jjkeller.kmb.interfaces.IExemptLogRequirements.ExemptLogRequirementsFragActions;
import com.jjkeller.kmb.interfaces.IExemptLogRequirements.ExemptLogRequirementsFragControllerMethods;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmb.share.BulletedListTextFormatter;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.enums.ExemptLogTypeEnum;
import com.jjkeller.kmbui.R;

public class ExemptLogRequirementsFrag extends BaseFragment{
	ExemptLogRequirementsFragActions actionsListener;
	ExemptLogRequirementsFragControllerMethods controlListener;
	
	private Button _btnyes;
	private Button _btnno;
	private TextView _lblexempttype_title;
	private TextView _lblexemptexception_bulletlist1;
	private TextView _lblexemptexception_bulletlist2;
	private TextView _lblexemptexception_bulletlist3;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        // Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.f_exemptlogrequirements, container, false);
		findControls(v);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
	}
	
	protected void findControls(View v){
		_btnyes = (Button)v.findViewById(R.id.btnyes);
		_btnno = (Button)v.findViewById(R.id.btnno);
		_lblexempttype_title = (TextView)v.findViewById(R.id.lblexempttype_title);
		_lblexemptexception_bulletlist1 = (TextView)v.findViewById(R.id.exemptexception_bulletlist1);
		_lblexemptexception_bulletlist2 = (TextView)v.findViewById(R.id.exemptexception_bulletlist2);
		_lblexemptexception_bulletlist3 = (TextView)v.findViewById(R.id.exemptexception_bulletlist3);
		
		ExemptLogTypeEnum logtype = GlobalState.getInstance().getCurrentUser().getExemptLogType();
		
		BulletedListTextFormatter.createBulletedList(this.getActivity(), _lblexemptexception_bulletlist1, R.string.lblexemptexception_bullet1);
		
		if(logtype.getValue() == ExemptLogTypeEnum.EXEMPTLOGTYPE100AIRMILE)
		{
			_lblexempttype_title.setText(R.string.login_exemptlogtype100);
			BulletedListTextFormatter.createBulletedList(this.getActivity(), _lblexemptexception_bulletlist2, R.string.lblexemptexception_bullet2_100);
			BulletedListTextFormatter.createBulletedList(this.getActivity(), _lblexemptexception_bulletlist3, R.string.lblexemptexception_bullet3_100);
		} else {
			_lblexempttype_title.setText(R.string.login_exemptlogtype150);
			BulletedListTextFormatter.createBulletedList(this.getActivity(), _lblexemptexception_bulletlist2, R.string.lblexemptexception_bullet2_150);
			BulletedListTextFormatter.createBulletedList(this.getActivity(), _lblexemptexception_bulletlist3, R.string.lblexemptexception_bullet3_150);
		}
		
		_btnyes.setOnClickListener(
	            new OnClickListener() {
	            	public void onClick(View v) {
	            		actionsListener.handleYesButtonClick();
	            	}
	            });
		_btnno.setOnClickListener(
	            new OnClickListener() {
	            	public void onClick(View v) {
	            		actionsListener.handleNoButtonClick();
	            	}
	            });
	}
	
	@Override
    public void onAttach(Activity activity) {
		super.onAttach(activity);
        try {
        	actionsListener = (ExemptLogRequirementsFragActions) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement ExemptLogRequirementsFragActions");
        }
        
        try{
        	controlListener = (ExemptLogRequirementsFragControllerMethods) activity;
        } catch (ClassCastException e) {
        	throw new ClassCastException(activity.toString() + " must implement ExemptLogRequirementsFragControllerMethods");
        }
    }
}
