package com.jjkeller.kmbapi.controller.abstracts;

import com.jjkeller.kmbapi.controller.AppUpdateFactory;
import com.jjkeller.kmbapi.controller.interfaces.IAppUpdateFactory;

public abstract class AppUpdateFactoryBase implements IAppUpdateFactory {
    private static IAppUpdateFactory _instance;
    public static IAppUpdateFactory getInstance() {
        if (_instance == null)
            _instance = new AppUpdateFactory();
        return _instance;
    }
}
