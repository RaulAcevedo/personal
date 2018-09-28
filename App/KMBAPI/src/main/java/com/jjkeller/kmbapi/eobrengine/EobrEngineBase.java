package com.jjkeller.kmbapi.eobrengine;

import android.os.Bundle;

import com.jjkeller.kmbapi.eobrengine.eobrdatatypes.ConnectedBluetoothDevice;
import com.jjkeller.kmbapi.eobrengine.eobrdatatypes.Eobr_Driver_Event_Packet;
import com.jjkeller.kmbapi.kmbeobr.Constants;
import com.jjkeller.kmbapi.kmbeobr.Enums.EobrReturnCode;

import java.util.ArrayList;
import java.util.List;

public abstract class EobrEngineBase implements IEobrEngine, ISendReceiveBulkData {

    /// <summary>
    /// Restrict the constructor so that external use is through the EobrEngineFactory
    /// </summary>
	protected EobrEngineBase(){}
	
	private ConnectedBluetoothDevice[] _availableBtEobrList = new ConnectedBluetoothDevice[MAX_DEVICES];
	private int _availableBTEobrQuantity = 0;
	private int _activeBTDeviceIndex = -1;

	protected static final String COMMTHREAD_RESPONSE = "RESPONSE";
	protected static final String COMMTHREAD_RETURNCODE = "RETURNCODE";

	protected static final int EOBR_PACKET_SIZE = 64;
	protected static int EOBR_FW_PACKET_SIZE = 61;
	protected static final int EUTIMESTAMP_SIZE = 6;
	protected static final int EUTIMESTAMP_SIZE_SECONDS_EPOCH = 4;

	protected static final int EOBR_GET_CLOCK_PACKET_SIZE = 6;
	
	public static final int EOBR_PAYLOAD_SIZE = 60;
	protected static int EOBR_FW_PAYLOAD_SIZE = 56;
	protected static final int RECEIVED_PACKET_SIZE = 258;

	protected static final int RECORD_ID_TYPE = 0;
	protected static final int TIMESTAMP_TYPE = 1;   // query the data given a timestamp
	protected static final int NEWEST_RECORD_ID = 0xFFFFFFFF;   // Newest record shall be returned
	protected static final int SPECIFIED_TIMESTAMP = 0;
	protected static final int NO_RESET_REF_TIMESTAMP = 0;   // No reset reference timestamp
	protected static final int NEXT_MOTION = 1;

	protected static final float J1708_MPH_PER_BIT = 0.5F;	//according to section A.84 of the J1708 specification.
	protected static final float J1708_MILES_PER_BIT = 0.1F;	//according to section A.244 and A.245 of the J1708 specification.
	protected static final float J1708_RPM_PER_BIT = 0.25F;	//according to section A.190 of the J1708 specification
	protected static final float J1708_FUEL_ECON_PER_BIT = 0.00390625F;	// 1/256 mpg section A.184 and A.185
	protected static final float J1708_TOTAL_FUEL_PER_BIT = 0.125F;		// gallons section A.250
	protected static final float J1708_BRAKE_PRESSURE_PER_BIT = 0.6F;		// lbf / in^2 (foot pounds per square inch) Section A.116
	protected static final float DISTANCE_KM_PER_BIT = 0.125F;
	protected static final float WHEEL_SPEED_KPH_PER_BIT = 0.00390625F; 	// (1/256)
	
	protected static final float KPL_TO_MPG = 2.35214583F;
	protected static final float GALLONS_PER_LITER = 0.2641720F;
	protected static final float PSI_PER_kPA = 0.14504F;

	protected static final float J1939_RPM_PER_BIT = 0.125F;
	protected static final float J1939_FUEL_ECONOMY_KM_PER_L_PER_BIT = 0.001953125F;
	protected static final float J1939_TOTAL_FUEL_L_PER_BIT = 0.5F;
	protected static final int J1939_BRAKE_PRESSURE_kPA_PER_BIT = 4;

	protected static final int BUSTYPE_J1708 = 1;
	protected static final int BUSTYPE_J1939 = 2;
	
	protected static final int STATUS_ODOMETER = 0x00000400;
	protected static final int STATUS_SPEEDOMETER = 0x00000200;
	protected static final int STATUS_FUEL = 0x00080000;
	protected static final int STATUS_CRUISE_CONTROL = 0x00100000;
	protected static final int STATUS_TRANSMISSION = 0x00200000;
	protected static final int STATUS_BRAKE = 0x00400000;

	protected static final int DEFAULT_TOTAL_DIS = 0;
	protected static final int DEFAULT_TRUCK_ODOMETER = 0xFFFFFFFF;
	protected static final int DEFAULT_ENGINE_RPM = 0x0000FFFF;
	protected static final int DEFAULT_TRUCK_VELOCITY = 0xFFFF;
	protected static final int DEFAULT_FUEL_ECONOMY = 0xFFFF;
	protected static final int DEFAULT_TOTAL_FUEL = 0xFFFFFFFF;
	protected static final int DEFAULT_CRUISE_CONTROL_STATUS = 0xFF;
	protected static final int DEFAULT_TRANSMISSION = 0xFFFF;
	protected static final int DEFAULT_BRAKE_PRESSURE = 0xFF;

	protected static final float ERROR_ODOMETER = -1;
	protected static final float ERROR_ENGINE_RPM = -1;
	protected static final float ERROR_VELOCITY = -1;
	protected static final float ERROR_FUEL = -1;
	protected static final char ERROR_CRUISE_CONTROL = (char)-1;
	protected static final char ERROR_TRANSMISSION = (char)-1;
	protected static final float ERROR_BRAKE = -1;

	// The Firmware Upgrade Start and End commands
	protected static final int FW_UPGRADE_BEGIN = 1;
	protected static final int FW_UPGRADE_END = 0;
	protected static final int FW_UPGRADE_RESET = 2;

	// Firmware type to be upgraded.
	protected static final int FW_ST_MICRO = 1;
	protected static final int FW_EZ_HOST = 2;
	protected static final int FW_BOOTLOADER = 0;
	protected static final int FW_APPLICATION = 1;
	
	protected static final int DEFAULT_RESET_ADDRESS = 0;   // Initialize ST reset address to 0
	
	// APP Firmware Total Packet size
	protected static final int APP_FW_SIZE = 3151; //3151 * 52 bytes per packet
	protected static final int BOOTLOADER_FW_SIZE = 512;  //512 * 48 bytes per packet

	// APP Firmware Total Packet size
//	protected static final int APP_FW_CHECKSUM_VECTOR_SIZE = 157;

	// OTG Firmware Total Packet size
	protected static final int OTG_FW_SIZE = (630+1);

	// EOBR APP checksum
//	protected static final int CHECKSUM_APP1	= 0x00002000;
//	protected static final int CHECKSUM_APP2  = 0x00004000;
//	protected static final int APP1BASE_addr  = 0x00008000;
//	protected static final int APP2BASE_addr  = 0x00020000;

	protected static final int APPBASE_addr    = 0x00010000;

	protected static final int OTG1BASE_addr    = 0x00040000;

	// Firmware Upgrade Defines
	protected static final int SECOND_SHIFT      = (8);
	protected static final int THIRD_SHIFT       = (16);
	protected static final int FOURTH_SHIFT      = (24);
//	private static final int DELAY_25SEC       = (25000);
	protected static final int MAX_ADDRESS_SIZE  = (4);

	// Binary File offset
	protected static final int INPUTFILE_OFFSET = 0x1A;

	public static int EOBR_FW_BLOCK_CODE_SIZE = 52;
	protected static final int EOBR_FW_BOOTLOADER_BLOCK_CODE_SIZE = 48;
	public static final int EOBR_FW_BLOCK_CODE_ADDRESS_SIZE = 4;
	protected static final int EOBR_FW_BLOCK_CODE_TYPE_SIZE = 1;
	
	public static final int EOBR_FILE_XFER_BLOCK_CODE_SIZE = 250;
	
	protected static final int MAX_DEVICES = 16;  		// maximum bluetooth devices will discover - taken from win mobile code

	protected static final String YEAR = "Year";
	protected static final String MONTH = "Month";
	protected static final String DAY = "Day";
	protected static final String HOUR = "Hour";
	protected static final String MINUTE = "Minute";
	protected static final String SECOND = "Second";
	protected static final String NEXTCONSOLELOGRECORDID = "NextConsoleLogRecordId";

	private static final int OFFSET_INDEX = 0;
    private static final int MULTIPLIER_INDEX = 1;
    private static final int ODOMETER_OFFSET_5BIT_FRACTION = 32;
    private static final int ODOMETER_MULTIPLIER_22BIT_FRACTION = 4194304;

    private static final double ODOMETER_OFFSET_LOWER_BOUND = -67108863.96875d;
    private static final double ODOMETER_OFFSET_UPPER_BOUND = 67108863.96875d;
    private static final double ODOMETER_MULTIPLIER_LOWER_BOUND = 0d;
    private static final double ODOMETER_MULTIPLIER_UPPER_BOUND = 1023.9999997d;

	public static final String EOBR_HARDWARE_MANUFACTURER = "HardwareManufacturer";
	public static final String EOBR_HARDWARE_MODEL = "HardwareModel";
	public static final String EOBR_HARDWARE_VERSION = "HardwareVersion";
	
	protected String _companyPasskey = null;

	protected static final int CUSTOM_PARAMETER_ELDREADVIN = 10;
	protected static final int CUSTOM_PARAMETER_ELDMANDATE = 11;

	public abstract boolean VerifySocketConnection();

	private enum OdometerCalibrationDataParameter
	{
		OFFSET,
		MULTIPLIER
	}

	private short _handshakeCrc = 0;
	protected boolean HasHandshakeCrc() { return _handshakeCrc != 0; }
	protected void SetHandshakeCrc(short crc) { _handshakeCrc = crc; }
	protected void ClearHandshakeCrc() { _handshakeCrc = 0; }

    public void initializeConnectedDevices()
    {
		for (int i=0; i<MAX_DEVICES; i++)
		{
			ConnectedBluetoothDevice dev = new ConnectedBluetoothDevice();
			dev.setName("");

			_availableBtEobrList[i] = dev;
		}

		_availableBTEobrQuantity = 0;
		this.ClearActiveDevice();
		ClearHandshakeCrc();
    }

    public EobrDeviceDescriptor[] getDiscoveredDeviceList()
    {
    	String deviceName = null;
		List<EobrDeviceDescriptor> devices = new ArrayList<EobrDeviceDescriptor>();

		for (int i=0; i<MAX_DEVICES; i++)
		{
			deviceName = this.ReadBtName(i);
			
			if (deviceName != null && !deviceName.equals(""))
			{
				EobrDeviceDescriptor desc = new EobrDeviceDescriptor(deviceName, this.ReadBtAddress(i), this.ReadEobrGen(i), this.ReadEobrCRC(i));
				devices.add(desc);
			}
		}
				
		return devices.toArray(new EobrDeviceDescriptor[devices.size()]);		
    }

	private boolean IsMatchingBtEobr(String name, String address, ConnectedBluetoothDevice device){
		boolean result = address != null && !address.isEmpty() && device.getAddress().equals(address);
		if (!result) {
			result = name != null && !name.isEmpty() && device.getName().equals(name);
		}
		return result;
	}

	protected AvailableBtEobrSearch FindAvailableBtEobr(String name, String address) {
		AvailableBtEobrSearch result = null;
		for (int i = 0; i < _availableBTEobrQuantity; i++){
			if (this.IsMatchingBtEobr(name, address, _availableBtEobrList[i])) {
				result = new AvailableBtEobrSearch(_availableBtEobrList[i], i);
				break;
			}
		}
		return result;
	}

	protected AvailableBtEobrSearch AddAvailableBtEobr(String name, String address, short crc, int eobrGen) {
		AvailableBtEobrSearch result = null;
    	if (_availableBTEobrQuantity < MAX_DEVICES)
		{
			result = this.FindAvailableBtEobr(name, address);
			if (result == null){
				_availableBtEobrList[_availableBTEobrQuantity].setName(name);
				_availableBtEobrList[_availableBTEobrQuantity].setAddress(address);
				_availableBtEobrList[_availableBTEobrQuantity].setCrc(crc);
				_availableBtEobrList[_availableBTEobrQuantity].setEobrGen(eobrGen);
				_availableBTEobrQuantity++;

				int index = _availableBTEobrQuantity - 1;
				result = new AvailableBtEobrSearch(_availableBtEobrList[index], index);
			}
		}
		return result;
	}

	protected boolean HasAvailableBtEobrDevices() { return _availableBTEobrQuantity > 0; }

	protected boolean HasActiveDevice () { return _activeBTDeviceIndex >= 0;	}

	protected ConnectedBluetoothDevice GetActiveDevice()
	{
		ConnectedBluetoothDevice result = null;
		if (this.HasActiveDevice())
		{
			result = _availableBtEobrList[_activeBTDeviceIndex];
		}
		return result;
	}

	// This method should never be used in "real life".  This is here solely for the purpose of an admin function added to help reproduce "Bad CRC!" issues in PBI 63446.
	// Menu > System Menu > File > Admin > Debug Clear Active Device CRC (button)
	public void ClearActiveDeviceCrc()
	{
		if (HasActiveDevice()) {
			GetActiveDevice().setCrc((short)0);
		}
	}

	protected void ClearActiveDevice()
	{
		_activeBTDeviceIndex = -1;
	}

	protected void SetupActiveDevice(AvailableBtEobrSearch deviceSearch)
	{
		_activeBTDeviceIndex = deviceSearch.getDeviceIndex();
	}

	public void SetupActiveDevice(String deviceName, String btAddress, int eobrGen, short crc)
	{
		AvailableBtEobrSearch deviceSearch = this.AddAvailableBtEobr(deviceName, btAddress, crc, eobrGen);
		_activeBTDeviceIndex = deviceSearch.getDeviceIndex();
	}

    public String GetActiveDeviceAddress()
    {
    	return this.HasActiveDevice() ? this.GetActiveDevice().getAddress() : null;
    }

    public boolean IsActiveDeviceCrcValid()
	{
		return CRCHelper.Companion.isValidCRCValue(this.GetActiveDeviceCrc());
	}

	public short GetActiveDeviceCrc()
	{
		short result = 0;
		if (this.HasActiveDevice())
		{
			result = this.GetActiveDevice().getCrc();
		}
		else if (this.HasHandshakeCrc())
		{
			result = _handshakeCrc;
		}
		return result;
	}
    
    public Bundle GetOdometerCalibration()
    {
		int returnCode = 0;
		int tempOffset = 0;
		int tempMultiplier = 0;
		float offset = 0F;
		float multiplier = 0F;
		
		Bundle bundle = null;

		if (_availableBtEobrList[0].getEobrGen() == 1)
		{
			bundle = this.GetCustomParameter(OFFSET_INDEX);
			if (bundle != null && bundle.containsKey(Constants.RETURNCODE))
				returnCode = bundle.getInt(Constants.RETURNCODE);
			else
				returnCode = EobrReturnCode.S_DEV_INTERNAL_ERROR;
			
			if (returnCode == EobrReturnCode.S_SUCCESS)
			{
				tempOffset = bundle.getInt(Constants.RETURNVALUE);
				
				bundle.clear();
				bundle = this.GetCustomParameter(MULTIPLIER_INDEX);
				if (bundle != null && bundle.containsKey(Constants.RETURNCODE))
					returnCode = bundle.getInt(Constants.RETURNCODE);
				else
					returnCode = EobrReturnCode.S_DEV_INTERNAL_ERROR;
				
				if (returnCode == EobrReturnCode.S_SUCCESS)
					tempMultiplier = bundle.getInt(Constants.RETURNVALUE);
			}
						
			if (returnCode == EobrReturnCode.S_SUCCESS)
			{
				offset = this.RevertToUserValue(tempOffset, ODOMETER_OFFSET_5BIT_FRACTION, OdometerCalibrationDataParameter.OFFSET);				
				multiplier = this.RevertToUserValue(tempMultiplier, ODOMETER_MULTIPLIER_22BIT_FRACTION, OdometerCalibrationDataParameter.MULTIPLIER);
			}
			else
			{
				offset = 0F;
				multiplier = 0F;
			}
		}
		// else, gen II
		else
		{
			bundle = this.GetEobrOdometerOffset();
			returnCode = bundle.getInt(Constants.RETURNCODE);
			if (returnCode == EobrReturnCode.S_SUCCESS)
				offset = bundle.getFloat(Constants.RETURNVALUE);
		}

		// if offset is defined, round to 10th of a mile
		if (offset != 0)
			offset = Math.round(offset*10f)/10f;

		bundle.clear();
		bundle.putInt(Constants.RETURNCODE, returnCode);
		bundle.putFloat(Constants.OFFSETPARAM, offset);
		bundle.putFloat(Constants.MULTIPLIERPARAM, multiplier);
		
		return bundle;
    }
    
	public int SetOdometerCalibration(float offset, float multiplier)
	{
		int returnCode = 0;
		int convertedOffset = 0;
		int convertedMultiplier = 0;

		if (offset != 0)
			offset = Math.round(offset*10f)/10f;

		if (_availableBtEobrList[0].getEobrGen() == 1)
		{
			if (this.ValidateValueBeforeStore(offset, ODOMETER_OFFSET_LOWER_BOUND, ODOMETER_OFFSET_UPPER_BOUND))
			{
				if (this.ValidateValueBeforeStore(multiplier, ODOMETER_MULTIPLIER_LOWER_BOUND, ODOMETER_MULTIPLIER_UPPER_BOUND))
				{
					convertedOffset = ConvertToEOBRValue(offset, ODOMETER_OFFSET_5BIT_FRACTION);
					convertedMultiplier = ConvertToEOBRValue(multiplier, ODOMETER_MULTIPLIER_22BIT_FRACTION);
				}
				else
				{
					returnCode = EobrReturnCode.S_DATA_OUT_OF_BOUND;
				}
			}
			else
			{
				returnCode = EobrReturnCode.S_DATA_OUT_OF_BOUND;
			}
        
			if (returnCode == EobrReturnCode.S_SUCCESS)
			{
				returnCode = this.SetCustomParameter(convertedOffset, OFFSET_INDEX);
        		if (returnCode == EobrReturnCode.S_SUCCESS)
        		{
        			returnCode = this.SetCustomParameter(convertedMultiplier, MULTIPLIER_INDEX);
        		}        		
        	}        	
		}
		// Gen II
		else
		{
			returnCode = this.SetEobrOdometerOffset(offset);			
		}

		return returnCode;
	}
	
    public String ReadBtName(int index)
    {    	
    	if(index < 0)
    		return ""; 
    	
    	if(index > MAX_DEVICES)
    		return ""; 
    
    	if(_availableBtEobrList[index] == null)
    		return "";    	
    	
    	return _availableBtEobrList[index].getName();
    }
    
    protected String ReadBtAddress(int index)
    {
    	if(index < 0)
    		return ""; 
    	
    	if(index > MAX_DEVICES)
    		return ""; 
    
    	if(_availableBtEobrList[index] == null)
    		return "";    	 
    	
    	return _availableBtEobrList[index].getAddress();
    }

    protected int ReadEobrGen(int index)
    {
    	if(index < 0)
    		return 0; 
    	
    	if(index > MAX_DEVICES)
    		return 0; 
    
    	if(_availableBtEobrList[index] == null)
    		return 0;    	 
    	
    	
    	return _availableBtEobrList[index].getEobrGen();
    }

    protected short ReadEobrCRC(int index)
    {
    	if(index < 0)
    		return 0; 
    	
    	if(index > MAX_DEVICES)
    		return 0; 
    
    	if(_availableBtEobrList[index] == null)
    		return 0;    	 
    	
    	
    	return _availableBtEobrList[index].getCrc();
    }
    
	protected static final byte[] intToByteArray(int value)
	{
		//NOTE:  Data on eobr is LITTLE_ENDIAN
		return new byte[] {
				(byte) value,		
				(byte)(value >>> 8),
				(byte)(value >>> 16),
				(byte)(value >>> 24)};
	}

	protected static final byte[] shortToByteArray(short value)
	{
		//NOTE:  Data on eobr is LITTLE_ENDIAN
		return new byte[] {
				(byte) value,		
				(byte)(value >>> 8)};
	}

	protected EobrCommunications.IVerifySocketConnection socketVerifier = new EobrCommunications.IVerifySocketConnection() {
        @Override
        public boolean verify() {
            return VerifySocketConnection();
        }
    };

	private boolean ValidateValueBeforeStore(double varToCompare, double referenceVarLowerBound, double referenceVarUpperBound)
    {
        boolean retCode;

        if (varToCompare <= referenceVarUpperBound && varToCompare >= referenceVarLowerBound)
        {
            retCode = true;
        }
        else
        {
            retCode = false;
        }

        return retCode;
    }

    /// <summary>
    /// This function will take the unsigned 32 bit value and convert to double value.
    /// Specially used for Odometer calibration data.
    /// </summary>
    /// <param name="varToRevert">Variable to be convert to double</param>
    /// <param name="bitForFraction">2 to the power of bit of fraction supported</param>
    /// <param name="odometerParameter">specified which odomoter parameter to convert</param>
    /// <returns>userValue</returns>
    private float RevertToUserValue(int varToRevert, int bitForFraction, OdometerCalibrationDataParameter odometerParameter)
    {
        boolean isNegative = false;
        float userValue = 0;

        // Parse the 32 bit representation to convert to double data type.
        if (odometerParameter == OdometerCalibrationDataParameter.OFFSET) // Now only Offset support negative.
        {
            if ((varToRevert & 0x80000000) == 0x80000000) // check to see is it negative, MSB is set if negative.
            {
                isNegative = true;
                varToRevert = varToRevert & 0x7FFFFFFF;  // mask off the positive/ negative bit.
            }
        }

        userValue = ((float)varToRevert / (bitForFraction));   // convert from 32 bit EOBR value to double.

        if (isNegative == true)
        {
            userValue = userValue * -1;
        }

        return userValue;
    }

    /// <summary>
    /// This function will convert the double parameter into unsigned 32bit variable according to the bit
    /// of fraction used. Specially used for Odometer calibration data.
    /// </summary>
    /// <param name="varToConvert">the double variable which is to be convert</param>
    /// <param name="bitForFraction">2 to the power of bit of fraction supported</param>
    /// <returns>valueToStoreInEOBR</returns>
    private int ConvertToEOBRValue(double varToConvert, int bitForFraction)
    {
        int valueToStoreInEOBR = 0;
        int valueBeforeDot = 0;
        double valueAfterDot = 0;
        boolean isNegative = false;
        double tempValue = 0;       
      
        if (varToConvert < 0) // check to see if it is negative value
        {
            isNegative = true;
            varToConvert = varToConvert * -1;            // convert to positive
        }

        tempValue = varToConvert * (bitForFraction);     // convert to EOBRValue,32bit

        valueBeforeDot = (int)tempValue;              // take the value before dot
        valueAfterDot = tempValue - valueBeforeDot;      // take the value after dot, fraction value

        if (valueAfterDot >= 0.5)                        // if the fraction >= 0.5 increment the value by 1
        {
            valueBeforeDot = valueBeforeDot + 1;
        }

        valueToStoreInEOBR = valueBeforeDot;

        if (isNegative == true)
        {
            valueToStoreInEOBR = valueToStoreInEOBR | 0x80000000;
        }

        return valueToStoreInEOBR;
    }

	protected class AvailableBtEobrSearch {
		private ConnectedBluetoothDevice _device;
		private int _deviceIndex;

		protected AvailableBtEobrSearch(ConnectedBluetoothDevice device, int index) {
			_device = device;
			_deviceIndex = index;
		}

		public ConnectedBluetoothDevice getDevice() { return _device; }
		public int getDeviceIndex() { return _deviceIndex; }
	}
}
