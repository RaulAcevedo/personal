package com.jjkeller.kmbapi.kmbeobr;

public class Enums {
	
	public enum CommunicationsMode
	{
		Unknown,
		USB,
		Bluetooth
	}
		
	public class DeviceErrorFlags
	{
		public static final int None = 0x00000000;
		public static final int RTC = 0x00000001;
		public static final int ExternalFlash = 0x00000002;
		public static final int JBus = 0x00000004;
		public static final int GPS = 0x00000008;
		public static final int InternalFlash = 0x00000020;
		public static final int InternalEOBR = 0x00000080;
		public static final int Speedometer = 0x00000200;
		public static final int Odometer = 0x00000400;
		public static final int MemFullEOBR = 0x00000800;
		public static final int RTCBattery = 0x00001000;
		public static final int Bluetooth = 0x00002000;
		public static final int USB = 0x00004000;
		public static final int JbusDiagMemFull = 0x00008000;
		public static final int JbusDiagBadBlock = 0x00010000;
		public static final int TemperatureWarning = 0x00020000;
		public static final int TemperatureShutdown = 0x00040000;
	}
	
	/// <summary>
    /// Enumeration of all return codes coming back from IEobrDevice methods 
    /// </summary>
	public class EobrReturnCode {
		public static final int S_ABORTED = -1;    // we've aborted the operation
		public static final int S_SUCCESS = 0;    // Function runs successfully
		public static final int S_INVALID_DATE_TIME = 3;    // Date time parameter is over bound, or year is smaller than 2000 or larger than 2099
		public static final int S_NO_DATA = 4;    // No data retrieved from device
		public static final int S_FUNC_NOT_IMPLEMENTED = 8;    // Function not implemented
		public static final int S_DEV_INTERNAL_ERROR = 9;    // One or more EOBR components have failed
		public static final int S_DEV_NOT_CONNECTED = 10;   // There is a high probability that the EOBR device is not connected.
		public static final int S_NO_HISTORICAL_DATA = 11;   // No Historical data in EOBR
		public static final int S_COMMS_BUSY = 12;   // Communications channel is busy with another command
		public static final int S_DATA_OUT_OF_BOUND = 13;   // Data entered is not supported by the function
		public static final int S_NO_SERIAL_DRIVER = 50;   // The device is not using the necessary serial USB driver
		public static final int S_WRONG_COMMAND_BACK = 100;
		public static final int S_WRONG_SIGNATURE_CRC = 200;
		public static final int S_GENERAL_ERROR = 255;   // Unknown error
	}
	
	/**
	 * Enumerate all of the messages that can be sent to the EobrService
	 * @author temp451
	 *
	 */
	public class EobrServiceMessages {	
	    public static final int MSG_REGISTER_CLIENT = 1;
	    public static final int MSG_UNREGISTER_CLIENT = 2;
	    public static final int MSG_SUSPEND_READING = 4;
	    public static final int MSG_RESUME_READING = 5;
	    public static final int MSG_SHUTDOWN = 6;
	    public static final int MSG_READANDPUBLISHHISTORICAL = 7;
	    public static final int MSG_HEARTBEAT = 8;	
	    public static final int MSG_IGNORERESUMEON = 9;
	    public static final int MSG_IGNORERESUMEOFF = 10;
	    public static final int MSG_FORCE_IMMEDIATE_READ = 11;
	    public static final int MSG_READANDPUBLISHHISTORICAL_GEN2 = 12;
	    public static final int MSG_READHISTORYONTIMERPOP = 13;
	}
}
