package com.jjkeller.kmb.test;

import org.junit.runners.model.InitializationError;
import org.robolectric.AndroidManifest;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.res.Fs;
import org.robolectric.res.ResourcePath;

import java.util.List;

public class KMBRoboElectricTestRunner extends RobolectricTestRunner {

    /**
     * Creates a runner to run {@code testClass}. Looks in your working directory for your AndroidManifest.xml file
     * and res directory by default. Use the {@link Config} annotation to configure.
     *
     * @param testClass the test class to be run
     * @throws InitializationError if junit says so
     */
    public KMBRoboElectricTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    @Override
    protected AndroidManifest getAppManifest(Config config) {
        String appRoot = "../KellerMobile/src/main/";
        String manifestPath = appRoot + "AndroidManifest.xml";
        String resDir = appRoot + "res";
        String assetsDir = appRoot + "assets";

        return new AndroidManifest(Fs.fileFromPath(manifestPath), Fs.fileFromPath(resDir), Fs.fileFromPath(assetsDir)) {
            @Override
            public List<ResourcePath> getIncludedResourcePaths() {
                List<ResourcePath> paths = super.getIncludedResourcePaths();
                paths.add(new ResourcePath(getRClass(), getPackageName(), Fs.fileFromPath("../KellerMobile/src/main/res"), getAssetsDirectory()));
                paths.add(new ResourcePath(getRClass(), getPackageName(), Fs.fileFromPath("../KMBUI/src/main/res"), getAssetsDirectory()));
                paths.add(new ResourcePath(getRClass(), getPackageName(), Fs.fileFromPath("../KMBAPI/src/main/res"), getAssetsDirectory()));
                paths.add(new ResourcePath(getRClass(), getPackageName(), Fs.fileFromPath("../KellerMobile/build/intermediates/exploded-aar/com.actionbarsherlock/actionbarsherlock/4.4.0/res"), getAssetsDirectory()));
                return paths;
            }

            @Override
            public String getVersionName() {
                return "1.0.0.0";
            }
        };
    }
}
