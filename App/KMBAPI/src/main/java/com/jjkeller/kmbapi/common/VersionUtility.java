package com.jjkeller.kmbapi.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Jamie on 5/15/17.
 */

public class VersionUtility {

    //compares version numbers such as 6.88.100 to 6.89.00 and outputs 1, 0 or -1 to denote >, < or ==
    public static int compareVersions(String version1, String version2){
        String[] levels1 = version1.split("\\.");
        String[] levels2 = version2.split("\\.");

        int length = Math.max(levels1.length, levels2.length);
        return compareVersions(levels1, levels2, length);
    }

    public static int compareMajorMinorVersions(String version1, String version2) {
        String[] levels1 = version1.split("\\.");
        String[] levels2 = version2.split("\\.");

        int majorMinorDepth = 2; // Major.Minor
        return compareVersions(levels1, levels2, majorMinorDepth);

    }

    private static int compareVersions(String[] levels1, String[] levels2, int compareDepth) {
        for (int i = 0; i < compareDepth; i++){
            Integer v1 = i < levels1.length ? Integer.parseInt(levels1[i]) : 0;
            Integer v2 = i < levels2.length ? Integer.parseInt(levels2[i]) : 0;
            int compare = v1.compareTo(v2);
            if (compare != 0){
                return compare;
            }
        }
        return 0;
    }

    public static ParsedVersion parseVersionString(String version) {
        ParsedVersion parsed = new ParsedVersion();
        Pattern pattern = Pattern.compile("\\d+\\.\\d+\\.\\d+");
        Matcher matcher = pattern.matcher(version);
        if (matcher.find()) {
            String[] tokens = matcher.group().split("\\.");
            parsed.setMajor(Integer.parseInt(tokens[0]));
            parsed.setMinor(Integer.parseInt(tokens[1]));
            parsed.setPatch(Integer.parseInt(tokens[2]));
        }
        return parsed;
    }

    public static String getImageFileName(int generation, int majorVersion, int minorVersion, int patchVersion) {
        final String FIRMWARE_IMAGE_NAME_FORMAT = "gen%s_%d.%d.%d.bin";

        return String.format(FIRMWARE_IMAGE_NAME_FORMAT, generation, majorVersion, minorVersion, patchVersion);
    }

    public static String getVersionString(int majorVersion, int minorVersion, int patchVersion) {
        final String VERSION_FORMAT = "%d.%d.%d";

        return String.format(VERSION_FORMAT, majorVersion, minorVersion, patchVersion);
    }

    public static class ParsedVersion {
        private int major;
        public int getMajor() {
            return major;
        }
        public void setMajor(int major) {
            this.major = major;
        }

        private int minor;
        public int getMinor() {
            return minor;
        }
        public void setMinor(int minor) {
            this.minor = minor;
        }

        private int patch;
        public int getPatch() {
            return patch;
        }
        public void setPatch(int patch) {
            this.patch = patch;
        }
    }
}
