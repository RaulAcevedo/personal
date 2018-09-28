package com.jjkeller.kmbapi.controller.EOBR;

import android.content.Context;

import com.jjkeller.kmbapi.proxydata.FirmwareVersion;

interface IFirmwareImageDownloader {
    void downloadImageIfNotOnDevice(Context context, FirmwareVersion firmwareVersion);
}
