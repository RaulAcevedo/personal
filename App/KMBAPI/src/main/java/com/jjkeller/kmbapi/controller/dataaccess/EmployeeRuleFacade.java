package com.jjkeller.kmbapi.controller.dataaccess;

import android.content.Context;

import com.jjkeller.kmbapi.controller.dataaccess.db.EmployeeRulePersist;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.proxydata.EmployeeRule;

public class EmployeeRuleFacade extends FacadeBase{
	
	public EmployeeRuleFacade(Context ctx, User user)
	{
		super(ctx, user);
	}
	
	public EmployeeRule Fetch()
	{
		EmployeeRulePersist<EmployeeRule> persist = new EmployeeRulePersist<EmployeeRule>(EmployeeRule.class, this.getContext(), this.getCurrentUser());
		return persist.Fetch();
	}
	
	public void Save(EmployeeRule empRuleData)
	{
		EmployeeRulePersist<EmployeeRule> persist = new EmployeeRulePersist<EmployeeRule>(EmployeeRule.class, this.getContext(), this.getCurrentUser());
		persist.Persist(empRuleData);
	}
}
