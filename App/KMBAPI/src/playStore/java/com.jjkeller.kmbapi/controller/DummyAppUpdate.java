package com.jjkeller.kmbapi.controller;

import com.jjkeller.kmbapi.controller.interfaces.IAppUpdate;
import com.jjkeller.kmbapi.controller.interfaces.IAppUpdateEvent;
import com.jjkeller.kmbapi.controller.interfaces.IAppUpdateHandler;
import com.jjkeller.kmbapi.controller.share.KmbApplicationException;

import java.io.IOException;

public class DummyAppUpdate implements IAppUpdate {
    @Override
    public void addHandler(IAppUpdateHandler handler) { }

    @Override
    public void shutDown() throws IOException { }

    @Override
    public boolean isDownloadInProgress() { return false; }

    @Override
    public boolean performInstall() throws KmbApplicationException { return true; }

    @Override
    public void cancelDownload() { }

    @Override
    public boolean downloadUpdates(IAppUpdateEvent onAppUpdateEvent) throws KmbApplicationException { return true; }
}
