package com.jjkeller.kmbapi.controller;

import com.jjkeller.kmbapi.controller.abstracts.AppUpdateFactoryBase;
import com.jjkeller.kmbapi.controller.interfaces.IAppUpdate;
import com.jjkeller.kmbapi.controller.interfaces.IAppUpdateCheck;

// Java 1.7 does not support interfaces with static methods, so we implement as a singleton instead.
// When we go to Java 1.8, these can be likely converted to static methods implementing
// IAppUpdateFactory directly, rather than extending AppUpdateFactoryBase.
public class AppUpdateFactory extends AppUpdateFactoryBase {
    @Override
    public IAppUpdate getAppUpdateComponent() { return new DummyAppUpdate(); }

    @Override
    public IAppUpdateCheck getAppUpdateCheckComponent() { return new DummyAppUpdateCheck(); }

    @Override
    public boolean areAppUpdateChecksEnabled() { return false; }

    @Override
    public boolean areAppUpdatesEnabled() { return false; }
}
