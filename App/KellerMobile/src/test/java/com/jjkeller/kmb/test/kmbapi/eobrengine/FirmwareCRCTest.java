package com.jjkeller.kmb.test.kmbapi.eobrengine;

import com.jjkeller.kmbapi.controller.utility.FileUtility;
import com.jjkeller.kmbapi.eobrengine.FirmwareCRC;

import junit.framework.Assert;

import org.codehaus.plexus.util.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;

public class FirmwareCRCTest {
    private static final String TESTFILE = "./src/test/java/com/jjkeller/kmb/test/kmbapi/eobrengine/gen2_6.88.100.bin";

    @Test
    public void testCalculateCRCFromByteArray() throws Exception{
        int expectedCrc = -2041687555;
        File file = FileUtils.getFile(TESTFILE);
        FileInputStream fin = new FileInputStream(file);
        byte[] firmwareImage = FileUtility.InputStreamToBytes(fin);

        boolean isHashEqual = FirmwareCRC.isHashEqual(firmwareImage, expectedCrc);

        Assert.assertTrue(isHashEqual);
    }
}
