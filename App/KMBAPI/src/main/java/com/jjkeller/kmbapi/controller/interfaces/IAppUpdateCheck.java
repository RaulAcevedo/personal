package com.jjkeller.kmbapi.controller.interfaces;

import com.jjkeller.kmbapi.controller.share.KmbApplicationException;

public interface IAppUpdateCheck {
    void addHandler(IAppUpdateCheckHandler handler);
    boolean isAppUpdateAvailable(boolean checkForAutoUpdate) throws KmbApplicationException;
}
