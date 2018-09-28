package com.jjkeller.kmbapi.controller.interfaces;

import com.jjkeller.kmbapi.proxydata.ApplicationUpdateInfo;

public interface IAppUpdateHandler extends IController {
    ApplicationUpdateInfo getApplicationUpdateInfo();
    String getPackageName();
}
