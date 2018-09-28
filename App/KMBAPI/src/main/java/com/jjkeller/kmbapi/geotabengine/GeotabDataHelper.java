package com.jjkeller.kmbapi.geotabengine;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * GeotabDataHelper is a set of ported methods originally created in C#. These methods
 * were provided by the Geotab team as a resource to properly calculate the checksum
 * and construct the full serial number from the base Hardware ID.
 */
    public class GeotabDataHelper {
    //
    //
    // Summary:
    //     false The default serial number prefix for Go2 devices.
    public static final String Go2Prefix = "GT";
    //
    //
    // Summary:
    //     false The default serial number prefix for Go3 devices.
    public static  final String Go3Prefix = "G3";
    //
    //
    // Summary:
    //     false The default serial number prefix for Go4 devices (pre Go4v3).
    public static final String Go4Prefix = "G4";
    //
    // Summary:
    //     false The default product Id for Go4 devices (pre Go4v3).
    public static final int Go4ProductId = 65;
    //
    // Summary:
    //     false The default serial number prefix for Go4v3 devices.
    public static final String Go4V3Prefix = "GV";
    //
    // Summary:
    //     false The default product Id for Go4v3 devices.
    public static final int Go4V3ProductId = 81;
    //
    // Summary:
    //     false The default serial number prefix for Go5 devices.
    public static final String Go5Prefix = "G5";
    //
    // Summary:
    //     false The default product Id for Go5 devices.
    public static final int Go5ProductId = 90;
    //
    // Summary:
    //     false The default serial number prefix for Go6 devices.
    public static final String Go6Prefix = "G6";
    //
    // Summary:
    //     false The default Product Id for Go6 devices.
    public static final int Go6ProductId = 101;
    //
    // Summary:
    //     false The default serial number prefix for Go7 devices.
    public static final String Go7Prefix = "G7";
    //
    // Summary:
    //     false The default Product Id for Go7 devices.
    public static final int Go7ProductId = 105;
    //
    // Summary:
    //     false The default serial number prefix for GoDrive mobile devices.
    public static final String GoDrivePrefix = "GD";
    //
    // Summary:
    //     false The default product Id for GoDrive mobile devices.
    public static final int GoDriveProductId = 256;
    //
    // Summary:
    //     false The maximum id value for devices that are historic
    public static final int HistoricMax = 10000000;

    /// <summary>
    /// Gets the third-party device types.
    /// </summary>
    /// <value>
    /// The third-party device types.
    /// </value>
    public static final HashMap<Integer, String> ThirdPartyDeviceTypes = new HashMap<>();

    //static initializer
    static {
        ThirdPartyDeviceTypes.put(10000, "C1");
        ThirdPartyDeviceTypes.put(10001, "C2");
        ThirdPartyDeviceTypes.put(10002, "C3");
        ThirdPartyDeviceTypes.put(10003, "C4");
        ThirdPartyDeviceTypes.put(10004, "C5");
        ThirdPartyDeviceTypes.put(10005, "C6");
        ThirdPartyDeviceTypes.put(10006, "C7");
        ThirdPartyDeviceTypes.put(10007, "C8");
        ThirdPartyDeviceTypes.put(10008, "C9");
        ThirdPartyDeviceTypes.put(10009, "CA");
        ThirdPartyDeviceTypes.put(10010, "CB");
        ThirdPartyDeviceTypes.put(10011, "CC");
        ThirdPartyDeviceTypes.put(10012, "CD");
        ThirdPartyDeviceTypes.put(10013, "CE");
        ThirdPartyDeviceTypes.put(10014, "CF");
        ThirdPartyDeviceTypes.put(10015, "CG");
        ThirdPartyDeviceTypes.put(10016, "CH");
        ThirdPartyDeviceTypes.put(10017, "CI");
        ThirdPartyDeviceTypes.put(10018, "CJ");
        ThirdPartyDeviceTypes.put(10019, "CK");
        ThirdPartyDeviceTypes.put(10020, "CL");
        ThirdPartyDeviceTypes.put(10021, "CM");
        ThirdPartyDeviceTypes.put(10022, "CN");
        ThirdPartyDeviceTypes.put(10023, "CO");
        ThirdPartyDeviceTypes.put(10024, "CP");
        ThirdPartyDeviceTypes.put(10025, "CQ");
        ThirdPartyDeviceTypes.put(10026, "CR");
        ThirdPartyDeviceTypes.put(10027, "CS");
        ThirdPartyDeviceTypes.put(10028, "CT");
        ThirdPartyDeviceTypes.put(10029, "CU");
        ThirdPartyDeviceTypes.put(10030, "CV");
        ThirdPartyDeviceTypes.put(10031, "CW");
        ThirdPartyDeviceTypes.put(10032, "CX");
        ThirdPartyDeviceTypes.put(10033, "CY");
        ThirdPartyDeviceTypes.put(10034, "CZ");
    }

    /// <summary>Encodes the serial number.</summary>
    /// <param name="productId">The product ID.</param>
    /// <param name="hardwareId">The hardware ID.</param>
    /// <returns>The serial number.</returns>
    public static String EncodeSerialNumber(int productId, int hardwareId) {
        StringBuilder encodedString = new StringBuilder();
        String devicePrefix = GetDevicePrefix(productId);
        if (devicePrefix != null) {
            encodedString.append(devicePrefix);
        }
        int checksum = GetChecksum(devicePrefix, productId, hardwareId);
        encodedString.append(ConvertChecksumToString((byte) checksum));
        encodedString.append(ConvertHardwareIdToString(hardwareId));
        return encodedString.toString();
    }

    /// <summary>Gets the third-party product ID from a serial number or device prefix.</summary>
    /// <param name="value">The serial number or device prefix.</param>
    /// <returns>The corresponding product ID, or 0 if the value is not a valid third-party serial number or device prefix.</returns>
    public static int GetThirdPartyProductId(String value) {
        value = value.toUpperCase();
        Iterator tppIds = ThirdPartyDeviceTypes.entrySet().iterator();
        while (tppIds.hasNext()) {
            Map.Entry pair = (Map.Entry) tppIds.next();
            if (value.startsWith((String) pair.getValue())) {
                return ((Integer) pair.getKey());
            }
        }
        return 0;
    }

    /// <summary>Checks if a serial number or device prefix is a supported third-party device type.</summary>
    /// <param name="value">The serial number or device prefix.</param>
    /// <returns>True if the serial number or device prefix is a supported third-party device type, false otherwise.</returns>
    public static boolean IsThirdPartyDevice(String value) {
        return GetThirdPartyProductId(value) > 0;
    }

    public static int GetHardwareIdFromHexString(String value) {
        return Integer.parseInt(value, 16);
    }

    private static Map.Entry<String, PrefixInfo> CheckDevicePrefix(String serialNumber) {
        PrefixInfo prefixInfo = new PrefixInfo();
        if (serialNumber == null || serialNumber.length() < 2) {
            return new AbstractMap.SimpleEntry<>(serialNumber, null);
        }
        prefixInfo.DevicePrefix = serialNumber.substring(0, 2).toUpperCase();
        if (IsThirdPartyDevice(prefixInfo.DevicePrefix)) {
            // Use third-party checksum
            prefixInfo.IsThirdParty = true;
        } else {
            // This mapping of prefixes to checksum type must match the mapping in serialNoConverter.js
            switch (prefixInfo.DevicePrefix) {
                case Go3Prefix:
                case Go4Prefix:
                case Go4V3Prefix:
                case Go5Prefix:
                case Go6Prefix:
                case Go7Prefix:
                case GoDrivePrefix:
                    // Use Go3 family checksum
                    prefixInfo.IsGo3Family = true;
                    break;
                case Go2Prefix:
                case "G2":
                case "GP":
                case "SA":
                case "SB":
                case "SC":
                    // Use legacy Go2 family checksum
                    break;
                default:
                    // Unknown prefix
                    prefixInfo.DevicePrefix = null;
                    return new AbstractMap.SimpleEntry<>(serialNumber, prefixInfo);
            }
        }
        return new AbstractMap.SimpleEntry<>(serialNumber.substring(2), prefixInfo);
    }

    private static String ConvertChecksumToString(byte checksum) {
        return String.format("%02X", checksum);
    }

    private static String ConvertHardwareIdToString(int hardwareId) {
        return String.format("%08X", hardwareId);
    }

    private static byte GetChecksum(int hardwareId) {
        return (byte) ((hardwareId >> 24) + 3 ^ (hardwareId >> 16) + 2 ^ (hardwareId >> 8) + 1 ^ hardwareId);
    }

    private static byte GetChecksum(String devicePrefix, int productId, int hardwareId) {
        PrefixInfo prefixInfo = null;
        Map.Entry<String, PrefixInfo> returnValues = CheckDevicePrefix(devicePrefix);
        if (returnValues.getValue() != null) {
            prefixInfo = returnValues.getValue();
        }
        if (prefixInfo != null) {
            if (prefixInfo.IsGo3Family) {
                return GetChecksum(hardwareId);
            }
            if (!prefixInfo.IsThirdParty) {
                return GetOldChecksum(hardwareId);
            }
        }
        return GetThirdPartyChecksum(productId, hardwareId);
    }

    private static String GetDevicePrefix(int productId) {
        if (productId <= 0) {
            return null;
        }
        if (productId <= 31 || productId >= 40 && productId <= 55) {
            return Go2Prefix;
        }
        if (productId <= 39 || productId >= 56 && productId < Go4ProductId) {
            return Go3Prefix;
        }
        if (productId >= Go4ProductId && productId < Go4V3ProductId) {
            return Go4Prefix;
        }
        if (productId >= Go4V3ProductId && productId < Go5ProductId) {
            return Go4V3Prefix;
        }
        if (productId >= Go5ProductId && productId < Go6ProductId) {
            return Go5Prefix;
        }
        if (productId >= Go6ProductId && productId < Go7ProductId) {
            return Go6Prefix;
        }
        //TODO: This value is different than the one provided by the Geotab developers
        //in their C# implementation (GoMaxProductId)
        if (productId >= Go7ProductId && productId <= HistoricMax) {
            return Go7Prefix;
        }
        if (productId == GoDriveProductId) {
            return GoDrivePrefix;
        }
        String value = null;
        if (ThirdPartyDeviceTypes.containsKey(productId))
            value = ThirdPartyDeviceTypes.get(productId);
        return value;
    }

    private static byte GetOldChecksum(int hardwareId) {
        String encodedHardwareId = ConvertHardwareIdToString(hardwareId);
        int checksum = 0;
        for (int i = 0; i < encodedHardwareId.length(); i++) {
            checksum = checksum + encodedHardwareId.charAt(i);
        }
        return (byte) checksum;
    }

    private static byte GetThirdPartyChecksum(int productId, int hardwareId) {
        return (byte) (GetChecksum(productId) ^ GetChecksum(hardwareId));
    }
}

class PrefixInfo {
    public String DevicePrefix;
    public boolean IsGo3Family;
    public boolean IsThirdParty;
}