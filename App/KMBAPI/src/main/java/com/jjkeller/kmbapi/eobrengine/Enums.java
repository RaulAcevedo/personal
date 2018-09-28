package com.jjkeller.kmbapi.eobrengine;

public class Enums {

	public class DllErrorCode
	{
		public static final int S_SUCCESS = 0;
		public static final int S_DEV_FLASH_FAILURE = 2;
		public static final int S_INVALID_DATE_TIME = 3;
		public static final int S_NO_DATA = 4;
		public static final int S_DEV_NO_RESPONSE = 5;
		public static final int S_FUNC_NOT_IMPLEMENTED = 8;
		public static final int S_DEV_INTERNAL_ERROR = 9;
		public static final int S_DEV_NOT_CONNECTED = 10;
		public static final int S_NO_HIST_DATA = 11;
		public static final int S_COMMS_BUSY = 12;
		public static final int S_NO_SERIAL_DRIVER = 50;
		public static final int S_WRONG_COMMAND_BACK = 100;
		public static final int S_WRONG_SIGNATURE_CRC = 200;
		public static final int S_UNKNOWN_ERROR = 255;
	}

    /// <summary>
    /// Possible types of firmware update available
    /// </summary>
    public enum FirmwareUpgradeTypeEnum { APP, BOOTLOADER }
}
