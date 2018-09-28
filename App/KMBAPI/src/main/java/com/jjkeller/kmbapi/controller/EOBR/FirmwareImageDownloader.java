package com.jjkeller.kmbapi.controller.EOBR;

import android.content.Context;

import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.share.KmbApplicationException;
import com.jjkeller.kmbapi.controller.utility.ErrorLogHelper;
import com.jjkeller.kmbapi.controller.utility.RESTWebServiceHelper;
import com.jjkeller.kmbapi.eobrengine.FirmwareCRC;
import com.jjkeller.kmbapi.proxydata.FirmwareVersion;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FirmwareImageDownloader implements IFirmwareImageDownloader {

    @Override
    public void downloadImageIfNotOnDevice(Context context, FirmwareVersion firmwareVersion) {
        File firmwareImageFile = getFirmwareImageFile(context, firmwareVersion);

        if (firmwareImageFile.exists())
            return;

        try {
            byte[] firmwareImage = downloadFirmwareImage(firmwareVersion, context);
            writeFirmwareImage(firmwareImage, firmwareVersion, context);
        } catch (Exception e) {
            ErrorLogHelper.RecordException(context, e);
        }
    }

    private byte[] downloadFirmwareImage(FirmwareVersion firmwareVersion, Context context) throws IOException, KmbApplicationException {
        RESTWebServiceHelper restWebServiceHelper = new RESTWebServiceHelper(context);
        byte[] firmwareImage;
        firmwareImage = restWebServiceHelper.DownloadFirmwareUpdate(firmwareVersion.getId());

        boolean hashMatchesExpected = FirmwareCRC.isHashEqual(firmwareImage, firmwareVersion.getCrc());
        if (!hashMatchesExpected) {
            String imageDoesNotMatch = String.format("Downloaded firmware image for %s, %d does not match expected CRC %d",
                    firmwareVersion.getVersionString(), FirmwareCRC.calculateHash(firmwareImage), firmwareVersion.getCrc());
            throw new KmbApplicationException(imageDoesNotMatch);
        }

        return firmwareImage;
    }

    private void writeFirmwareImage(byte[] firmwareImage, FirmwareVersion firmwareVersion, Context context) throws IOException {
        File firmwareImageFile = getFirmwareImageFile(context, firmwareVersion);

        BufferedOutputStream bufferedWriter;

        bufferedWriter = new BufferedOutputStream(new FileOutputStream(firmwareImageFile));
        bufferedWriter.write(firmwareImage);
        bufferedWriter.close();
    }

    private File getFirmwareImageFile(Context context, FirmwareVersion firmwareVersion) {
        File firmwareImagesFolder = new File(context.getFilesDir(), "/" + GlobalState.FIRMWARE_IMAGE_DIRECTORY);
        String imageFileName = firmwareVersion.getImageFileName();
        return new File(firmwareImagesFolder, imageFileName);
    }
}
