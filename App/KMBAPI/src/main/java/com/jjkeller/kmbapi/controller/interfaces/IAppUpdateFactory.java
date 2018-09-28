package com.jjkeller.kmbapi.controller.interfaces;

public interface IAppUpdateFactory {
    IAppUpdate getAppUpdateComponent();
    IAppUpdateCheck getAppUpdateCheckComponent();

    boolean areAppUpdateChecksEnabled();
    boolean areAppUpdatesEnabled();
}
