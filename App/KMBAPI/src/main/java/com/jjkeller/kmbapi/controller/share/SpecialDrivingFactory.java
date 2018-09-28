package com.jjkeller.kmbapi.controller.share;

import com.android.internal.util.Predicate;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.configuration.UserState;
import com.jjkeller.kmbapi.controller.LogHyrailController;
import com.jjkeller.kmbapi.controller.LogNonRegulatedDrivingController;
import com.jjkeller.kmbapi.controller.LogPersonalConveyanceController;
import com.jjkeller.kmbapi.controller.interfaces.ISpecialDrivingController;
import com.jjkeller.kmbapi.enums.EmployeeLogProvisionTypeEnum;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by ief5781 on 10/26/16.
 */

public class SpecialDrivingFactory {
    private static HashMap<EmployeeLogProvisionTypeEnum, ISpecialDrivingController> _controllers;

    static {
        _controllers = new LinkedHashMap<>();

        //the order in which these controllers are added is the order in which we'll iterate over them
        //in getControllerPassingTest
        _controllers.put(EmployeeLogProvisionTypeEnum.PERSONALCONVEYANCE, new LogPersonalConveyanceController(GlobalState.getInstance().getApplicationContext()));
        _controllers.put(EmployeeLogProvisionTypeEnum.HYRAIL, new LogHyrailController(GlobalState.getInstance().getApplicationContext()));
        _controllers.put(EmployeeLogProvisionTypeEnum.NONREGULATED, new LogNonRegulatedDrivingController(GlobalState.getInstance().getApplicationContext()));
    }

    public static ISpecialDrivingController getControllerForDrivingCategory(EmployeeLogProvisionTypeEnum drivingCategory) {
        return _controllers.get(drivingCategory);
    }

    public static ISpecialDrivingController getControllerInDrivingSegment() {
        return getControllerPassingTest(new Predicate<ISpecialDrivingController>() {
            @Override
            public boolean apply(ISpecialDrivingController specialDrivingController) {
                return specialDrivingController.getIsInSpecialDrivingSegment();
            }
        });
    }

    public static ISpecialDrivingController getControllerInDutyStatus() {
        return getControllerPassingTest(new Predicate<ISpecialDrivingController>() {
            @Override
            public boolean apply(ISpecialDrivingController specialDrivingController) {
                return specialDrivingController.getIsInSpecialDutyStatus();
            }
        });
    }

    public static ISpecialDrivingController getControllerInDutyStatusButNotDrivingSegment() {
        return getControllerPassingTest(new Predicate<ISpecialDrivingController>() {
            @Override
            public boolean apply(ISpecialDrivingController specialDrivingController) {
                return specialDrivingController.getIsInSpecialDutyStatus() && !specialDrivingController.getIsInSpecialDrivingSegment();
            }
        });
    }

    public static ISpecialDrivingController getControllerPassingTest(Predicate<ISpecialDrivingController> predicate) {
        for(ISpecialDrivingController controller : _controllers.values()) {
            if(predicate.apply(controller))
                return controller;
        }

        return null;
    }

    public static ISpecialDrivingController getController(UserState userState) {
        ISpecialDrivingController controller = null;

        if(userState.getIsInPersonalConveyanceDutyStatus())
            controller = _controllers.get(EmployeeLogProvisionTypeEnum.PERSONALCONVEYANCE);
        else if(userState.getIsInHyrailDutyStatus())
            controller = _controllers.get(EmployeeLogProvisionTypeEnum.HYRAIL);
        else if(userState.getIsInNonRegDrivingDutyStatus())
            controller = _controllers.get((EmployeeLogProvisionTypeEnum.NONREGULATED));

        if(controller != null && controller.getIsInSpecialDrivingSegment())
            return controller;

        return null;
    }

    public static Collection<ISpecialDrivingController> getAllControllers() {
        return _controllers.values();
    }
}
