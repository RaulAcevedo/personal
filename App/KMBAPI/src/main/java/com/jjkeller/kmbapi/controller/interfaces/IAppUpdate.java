package com.jjkeller.kmbapi.controller.interfaces;

import com.jjkeller.kmbapi.controller.share.KmbApplicationException;

import java.io.IOException;

public interface IAppUpdate {
    void addHandler(IAppUpdateHandler handler);
    void shutDown() throws IOException;
    boolean isDownloadInProgress();
    boolean downloadUpdates(IAppUpdateEvent onAppUpdateEvent) throws KmbApplicationException;
    boolean performInstall() throws KmbApplicationException;
    void cancelDownload();
}
