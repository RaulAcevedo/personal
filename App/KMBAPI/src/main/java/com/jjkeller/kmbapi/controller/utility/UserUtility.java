package com.jjkeller.kmbapi.controller.utility;

import android.content.Context;

import com.jjkeller.kmbapi.controller.EmployeeRuleController;
import com.jjkeller.kmbapi.controller.dataaccess.UserFacade;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.proxydata.EmployeeRule;

/**
 * Common methods related to a User
 */
public class UserUtility {

    /**
     * Hydrate team driver user
     */
    public static User getUserForKMBUserName(Context context, String kmbUserName){
        User user = new User();
        UserFacade userFacade = new UserFacade(context, user);
        user.setCredentials(userFacade.Fetch(kmbUserName));

        EmployeeRuleController ruleController = new EmployeeRuleController(context);
        EmployeeRule employeeRule = ruleController.EmployeeRuleForUser(user);
        ruleController.TransferEmployeeRuleToUser(user, employeeRule);

        return user;
    }
}