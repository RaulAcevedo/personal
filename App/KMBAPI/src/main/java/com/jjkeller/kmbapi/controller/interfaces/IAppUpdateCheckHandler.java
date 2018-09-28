package com.jjkeller.kmbapi.controller.interfaces;

import com.jjkeller.kmbapi.controller.share.KmbApplicationException;
import com.jjkeller.kmbapi.proxydata.ApplicationUpdateInfo;

public interface IAppUpdateCheckHandler extends IController {
    void cleanUp() throws KmbApplicationException;
    String getPackageName();
    void setApplicationUpdateInfo(ApplicationUpdateInfo applicationUpdateInfo);
    ApplicationUpdateInfo getApplicationUpdateInfo();
}
