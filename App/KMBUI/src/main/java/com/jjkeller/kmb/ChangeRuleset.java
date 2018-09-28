package com.jjkeller.kmb;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.actionbarsherlock.view.Menu;
import com.jjkeller.kmb.fragments.ChangeRulesetFrag;
import com.jjkeller.kmb.interfaces.IChangeRuleset.ChangeRulesetFragActions;
import com.jjkeller.kmb.interfaces.IChangeRuleset.ChangeRulesetFragControllerMethods;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmbapi.calcengine.Enums.RuleTypeEnum;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.ExemptLogValidationController;
import com.jjkeller.kmbapi.controller.interfaces.IAPIController;
import com.jjkeller.kmbapi.controller.share.MandateObjectFactory;
import com.jjkeller.kmbapi.enums.RuleSetTypeEnum;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbui.R;

public class ChangeRuleset extends BaseActivity 
							implements ChangeRulesetFragActions, ChangeRulesetFragControllerMethods{
	ChangeRulesetFrag _contentFrag;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.baselayout);

		mFetchLocalDataTask = new FetchLocalDataTask(this.getClass().getSimpleName());
		mFetchLocalDataTask.execute();
	}

	@Override
	protected void loadControls()
	{
		super.loadControls();
		loadContentFragment(new ChangeRulesetFrag());
	}
	
	@Override
	public void setFragments()
	{
		super.setFragments();

		Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
		_contentFrag = (ChangeRulesetFrag)f;
	}	

	@Override
	protected void InitController() {
		IAPIController ctrl = MandateObjectFactory.getInstance(this,GlobalState.getInstance().getFeatureService()).getCurrentEventController();

		this.setController(ctrl);
	}
	
	public IAPIController getMyController()
	{
		return (IAPIController) this.getController();
	}	
	
	@Override
	protected void Return(boolean success) {
		if(success)
		{
			this.finish();

			/* Display rodsentry activity */
			this.startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
		}
		else{ 
			if(!_contentFrag.getRulesetCombinationAllowed())
				this.ShowMessage(this, getString(R.string.msg_ruleset_invalidrulesetcombination));
		}
	}
	
	public void handleCancelButtonClick(){
		Return();
	}
	
	public void handleOKButtonClick() {

		RuleSetTypeEnum newRuleset = RuleSetTypeEnum.valueOf(this, (String)_contentFrag.getRulesetSpinner().getSelectedItem());
		if(!getMyController().IsUSOilFieldOffDutyStatusInLog() || newRuleset.isAnyOilFieldRuleset())
		{
			EmployeeLog currentLog = GlobalState.getInstance().getCurrentEmployeeLog();
			if (getMyController().IsLogExemptEligible(currentLog))
			{
				this.ShowConfirmationMessage(this, R.string.lblexemptchangerulesettitle, getString(R.string.lblexemptchangerulesetmessage), 
						R.string.oklabel, new ShowMessageClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								super.onClick(dialog, id);
								dialog.dismiss();
								mSaveLocalDataTask = new SaveLocalDataTask(this.getClass().getSimpleName());
								mSaveLocalDataTask.execute();
							}
						}, 
						R.string.cancellabel, new ShowMessageClickListener());
			}
			else
			{
				mSaveLocalDataTask = new SaveLocalDataTask(this.getClass().getSimpleName());
				mSaveLocalDataTask.execute();	
			}
		}
		else
		{
			this.ShowMessage(this, this.getResources().getString(R.string.msg_ruleset_cannotbechanged));
		}
	}
	
	@Override
	protected boolean saveData()
	{
		boolean isSuccessful = false;
		RuleTypeEnum ruleTypeEnum;

		int selectedRuletype = (int)_contentFrag.getRuletypeSpinner().getSelectedItemId();
		if(selectedRuletype == 1)
			ruleTypeEnum = RuleTypeEnum.CDOnly;
		else if(selectedRuletype == 2)
			ruleTypeEnum = RuleTypeEnum.USOnly;
		else
			ruleTypeEnum = RuleTypeEnum.Both;
		
		RuleSetTypeEnum newRuleset = RuleSetTypeEnum.valueOf(this, (String)_contentFrag.getRulesetSpinner().getSelectedItem());
		
		if(ruleTypeEnum != RuleTypeEnum.Both)
		{
			if(getMyController().isRulesetCombinationAllowed(newRuleset)){
				getMyController().ChangeRulesetOfEntireLog(ruleTypeEnum, newRuleset);
				isSuccessful = true;
			}
			else{
				_contentFrag.setRulesetCombinationAllowed(false);
				isSuccessful = false;
			}
		}
		else
		{
			getMyController().ChangeRulesetOfEntireLog(ruleTypeEnum, newRuleset);
			isSuccessful = true;
		}
		
		EmployeeLog currentLog = GlobalState.getInstance().getCurrentEmployeeLog();
		ExemptLogValidationController ctrlr = new ExemptLogValidationController(this);
		ctrlr.PerformCompleteValidationForCurrentLog(currentLog, true);
		
		return isSuccessful;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		this.CreateOptionsMenu(menu, false);
		return true;
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		if(_contentFrag.getRuletypeSpinner() != null)
			outState.putInt(getResources().getString(R.string.state_ruletype), (int)_contentFrag.getRuletypeSpinner().getSelectedItemId());
		if(_contentFrag.getRulesetSpinner() != null)
			outState.putInt(getResources().getString(R.string.state_ruleset), (int)_contentFrag.getRulesetSpinner().getSelectedItemId());
		super.onSaveInstanceState(outState);
		
	}

	
}
