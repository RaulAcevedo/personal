package com.jjkeller.kmbapi.controller.EOBR;

import android.content.Context;

import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.utility.ErrorLogHelper;
import com.jjkeller.kmbapi.controller.utility.FileUtility;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FirmwareImageMover {
    public void moveBundledFirmwareToInternalDir(Context context) {
        File firmwareImagesFolder = new File(context.getFilesDir(), GlobalState.FIRMWARE_IMAGE_DIRECTORY);
        if (!firmwareImagesFolder.exists())
        {
            firmwareImagesFolder.mkdirs();
        }

        try {
            String[] bundledImages = context.getAssets().list(GlobalState.FIRMWARE_IMAGE_DIRECTORY);
            String baseAssetPath = GlobalState.FIRMWARE_IMAGE_DIRECTORY + "/";

            for (String image : bundledImages) {
                File destinationImage = new File(firmwareImagesFolder, image);
                if(!destinationImage.exists()) {
                    InputStream in = context.getAssets().open(baseAssetPath + image);
                    OutputStream out = new FileOutputStream(destinationImage);
                    FileUtility.CopyFile(in, out);
                    in.close();
                    out.close();
                }
            }
        } catch (IOException e) {
            ErrorLogHelper.RecordException(context, e);
        }
    }
}
