package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.jjkeller.kmb.interfaces.IChangeRuleset.ChangeRulesetFragActions;
import com.jjkeller.kmb.interfaces.IChangeRuleset.ChangeRulesetFragControllerMethods;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbapi.controller.abstracts.APIControllerBase;
import com.jjkeller.kmbapi.enums.RuleSetTypeEnum;
import com.jjkeller.kmbui.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChangeRulesetFrag extends BaseFragment{
	ChangeRulesetFragActions actionsListener;
	ChangeRulesetFragControllerMethods controlListener;	
	
	private Spinner _spinnerRuleset;
	private Spinner _spinnerRuletype;
	ArrayAdapter<CharSequence> _ruleTypeAdapter;
	private boolean _isBothInternationalRulesetsAvailable;
	private Bundle _savedInstanceState;
	private boolean _isRulesetCombinationAllowed = true;
	private TextView _lblNewRuleset;
	private Button _btnOK;
	private Button _btnCancel;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        // Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.f_changeruleset, container, false);
		findControls(v);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		this.loadControls(savedInstanceState);
	}
	
	private void loadControls(Bundle savedInstanceState){
		if(savedInstanceState != null)
			_savedInstanceState = savedInstanceState;
		
		_isBothInternationalRulesetsAvailable = ((APIControllerBase)controlListener.getMyController()).getCurrentUser().AreBothInternationalRulesetsAvailable(getActivity());
		
		if(!_isBothInternationalRulesetsAvailable){
			_spinnerRuletype.setVisibility(View.GONE);
			
			_lblNewRuleset.setText(getString(R.string.cr_lblnewrulesetforentirelog));
			
			List<String> rulesetAvailList = new ArrayList<String>();
			for(RuleSetTypeEnum item : ((APIControllerBase)controlListener.getMyController()).getCurrentUser().getAvailableRulesets())
			{
				if(item.getString(getActivity()).length() > 0)
					rulesetAvailList.add(item.getString(getActivity()));
			}
			Collections.sort(rulesetAvailList);
			
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.kmb_spinner_item, rulesetAvailList);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	
			_spinnerRuleset.setAdapter(adapter);
			if(savedInstanceState != null)
				_spinnerRuleset.setSelection(savedInstanceState.getInt(getResources().getString(R.string.state_ruleset)));
			else
				_spinnerRuleset.setSelection(adapter.getPosition(((APIControllerBase)controlListener.getMyController()).getCurrentUser().getRulesetTypeEnum().getString(getActivity())));

		}		
		else
		{
			_ruleTypeAdapter = ArrayAdapter.createFromResource(
	                getActivity(), R.array.cr_RulesetType_Array, R.layout.kmb_spinner_item);
			_ruleTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			_spinnerRuletype.setAdapter(_ruleTypeAdapter);
			if(_savedInstanceState != null && _savedInstanceState.containsKey(getResources().getString(R.string.state_ruletype))){			
				_spinnerRuletype.setSelection(_savedInstanceState.getInt(getResources().getString(R.string.state_ruletype)));
			}
			_spinnerRuletype.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				public void onItemSelected(AdapterView<?> parent, View v, int position, long id) 
				{
					String[] rulesetTypeArray = getResources().getStringArray(R.array.cr_RulesetType_Array);
					String selectedRuletype = (String)getRuletypeSpinner().getSelectedItem();
					
					List<String> rulesetAvailList = new ArrayList<String>();
					
					for(RuleSetTypeEnum item : ((APIControllerBase)controlListener.getMyController()).getCurrentUser().getAvailableRulesets())
					{
						if(item.getString(getActivity()).length() > 0){
							if(selectedRuletype.contains(rulesetTypeArray[1]) && item.isCanadianRuleset()){
								rulesetAvailList.add(item.getString(getActivity()));
							}
							else if(selectedRuletype.contains(rulesetTypeArray[2]) && !item.isCanadianRuleset()){
								rulesetAvailList.add(item.getString(getActivity()));
							}
							else if(selectedRuletype.contains(rulesetTypeArray[0]))
							{
								rulesetAvailList.add(item.getString(getActivity()));
							}
						}
					}
					Collections.sort(rulesetAvailList);
					
					ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.kmb_spinner_item, rulesetAvailList);
					adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

					getRulesetSpinner().setAdapter(adapter);
					getRulesetSpinner().setSelection(adapter.getPosition(((APIControllerBase)controlListener.getMyController()).getCurrentUser().getRulesetTypeEnum().getString(getActivity())));

					if(getSavedInstance() != null && getSavedInstance().containsKey(getResources().getString(R.string.state_ruleset))){			
						getRulesetSpinner().setSelection(getSavedInstance().getInt(getResources().getString(R.string.state_ruleset)));
					}		
				}
				
				public void onNothingSelected(AdapterView<?> arg0) {
					// TODO Auto-generated method stub
					
				}	
			}); 
		}
	}
	
	protected void findControls(View v){
		_spinnerRuletype = (Spinner)v.findViewById(R.id.cr_spnRuletype);
		_spinnerRuleset = (Spinner)v.findViewById(R.id.cr_spnRuleset);		
		_lblNewRuleset = (TextView)v.findViewById(R.id.cr_lblNewRuleset);
		_btnOK = (Button)v.findViewById(R.id.cr_btnOk);
		_btnCancel = (Button)v.findViewById(R.id.cr_btnCancel);
		
		_btnOK.setOnClickListener(
				new OnClickListener() {
	            	public void onClick(View v) {
	            		actionsListener.handleOKButtonClick();
	            	}
	            });

		_btnCancel.setOnClickListener(
				new OnClickListener() {
	            	public void onClick(View v) {
	            		actionsListener.handleCancelButtonClick();
	            	}
	            });		
	}
	
	@Override
    public void onAttach(Activity activity) {
		super.onAttach(activity);
        try {
        	actionsListener = (ChangeRulesetFragActions) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement ChangeRulesetFragActions");
        }
        
        try{
        	controlListener = (ChangeRulesetFragControllerMethods) activity;
        } catch (ClassCastException e) {
        	throw new ClassCastException(activity.toString() + " must implement ChangeRulesetFragControllerMethods");
        }
    }
	
	public Spinner getRulesetSpinner(){
		if(_spinnerRuleset == null)
			_spinnerRuleset = (Spinner)getView().findViewById(R.id.cr_spnRuleset);
		return _spinnerRuleset;
	}
	
	public Spinner getRuletypeSpinner(){
		if(_spinnerRuletype == null)
			_spinnerRuletype = (Spinner)getView().findViewById(R.id.cr_spnRuletype);
		return _spinnerRuletype;
	}
	
	public void setRulesetCombinationAllowed(boolean flag){
		_isRulesetCombinationAllowed = flag;
	}
	
	public boolean getRulesetCombinationAllowed(){
		return _isRulesetCombinationAllowed;
	}
	
	public Bundle getSavedInstance(){
		return _savedInstanceState;
	}
}
