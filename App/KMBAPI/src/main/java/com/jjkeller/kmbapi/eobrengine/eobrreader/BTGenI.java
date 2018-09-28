package com.jjkeller.kmbapi.eobrengine.eobrreader;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;

import com.jjkeller.kmbapi.configuration.FirmwareUpdate;
import com.jjkeller.kmbapi.eobrengine.CalcCRC;
import com.jjkeller.kmbapi.eobrengine.CommThreadManager;
import com.jjkeller.kmbapi.eobrengine.EUCMDType;
import com.jjkeller.kmbapi.eobrengine.Enums;
import com.jjkeller.kmbapi.eobrengine.EobrCommunications;
import com.jjkeller.kmbapi.eobrengine.LittleEndianHelper;
import com.jjkeller.kmbapi.eobrengine.ReceivedPacketHelper;
import com.jjkeller.kmbapi.eobrengine.eobrdatatypes.EobrBytePacket;
import com.jjkeller.kmbapi.eobrengine.eobrdatatypes.EobrClockPacket;
import com.jjkeller.kmbapi.eobrengine.eobrdatatypes.EobrIntegerPacket;
import com.jjkeller.kmbapi.eobrengine.eobrdatatypes.EobrTimestamp;
import com.jjkeller.kmbapi.eobrengine.eobrdatatypes.Eobr_Comm_Response_Packet;
import com.jjkeller.kmbapi.eobrengine.eobrdatatypes.Eobr_Data;
import com.jjkeller.kmbapi.eobrengine.eobrdatatypes.Eobr_Data_Packet;
import com.jjkeller.kmbapi.eobrengine.eobrdatatypes.Eobr_Data_Packet_GenII;
import com.jjkeller.kmbapi.eobrengine.eobrdatatypes.Eobr_Driver_Event_Packet;
import com.jjkeller.kmbapi.eobrengine.eobrdatatypes.Eobr_FW_Block_Packet;
import com.jjkeller.kmbapi.eobrengine.eobrdatatypes.Eobr_Packet;
import com.jjkeller.kmbapi.eobrengine.eobrreader.exceptions.EobrException;
import com.jjkeller.kmbapi.eobrengine.ReceivedPacketHelper.VerifyRxPacketResponseByte;
import com.jjkeller.kmbapi.firmwareupgrade.FirmwareUpdateBroadcaster;
import com.jjkeller.kmbapi.kmbeobr.Constants;
import com.jjkeller.kmbapi.kmbeobr.DriveData;
import com.jjkeller.kmbapi.kmbeobr.DriveDataTypeEnum;
import com.jjkeller.kmbapi.kmbeobr.Enums.EobrReturnCode;
import com.jjkeller.kmbapi.kmbeobr.EobrReferenceTimestamps;
import com.jjkeller.kmbapi.kmbeobr.EobrResponse;
import com.jjkeller.kmbapi.kmbeobr.EventRecord;
import com.jjkeller.kmbapi.kmbeobr.EventTypeEnum;
import com.jjkeller.kmbapi.kmbeobr.FirmwareUpgradeRequestResult;
import com.jjkeller.kmbapi.kmbeobr.FirmwareUpgradeStatusResult;
import com.jjkeller.kmbapi.kmbeobr.HistogramData;
import com.jjkeller.kmbapi.kmbeobr.HistogramTypeEnum;
import com.jjkeller.kmbapi.kmbeobr.JbusDiagnosticData;
import com.jjkeller.kmbapi.kmbeobr.StatusBuffer;
import com.jjkeller.kmbapi.kmbeobr.StatusRecord;
import com.jjkeller.kmbapi.kmbeobr.StatusRecordMotionOptionEnum;
import com.jjkeller.kmbapi.kmbeobr.StatusRecordQueryMethodEnum;
import com.jjkeller.kmbapi.kmbeobr.TripReport;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

public class BTGenI extends AndroidBTBase {

	private int bootloaderAddrBase;

	public int searchForEobrDevices(List<BluetoothDevice> devices, String companyPasskey, String serialNumber)
	{
    	int retValue;

   		_companyPasskey = companyPasskey;
   		retValue = this.SearchBluetoothThread(devices, serialNumber);
    	
    	return retValue;
	}

    // 2/3/2012 - TCH - pass in BTAdapter so we don't have to try and get the
    // adapter here in the case of the app process being killed and we are relogging in.
    // In that scenario, this code is being executed on a background thread and the
    // getDefaultAdapter method needs to be run on the UI thread
	public int searchForEobrDevice(String companyPasskey, BluetoothDevice btDevice)
	{
		_companyPasskey = companyPasskey;
		int retValue = EobrReturnCode.S_SUCCESS;

		// Get a BluetoothSocket for a connection with the given Bluetooth address
		try
		{
			ValidatedDevice device = checkValidDevice(btDevice, null);
			if (device != null)
			{
				AvailableBtEobrSearch deviceSearch = this.AddAvailableBtEobr(device.getDeviceName(), btDevice.getAddress(), device.getDeviceCrc(), 1);
				this.SetupActiveDevice(deviceSearch);
			}
			else
			{
				this.ClearActiveDevice();
				retValue = EobrReturnCode.S_DEV_NOT_CONNECTED;
			}
		}
		catch (IllegalArgumentException ex)
		{
			retValue = EobrReturnCode.S_DEV_NOT_CONNECTED;
			_isSocketConnected = false;
		}

		return retValue;
	}

    private int SearchBluetoothThread(List<BluetoothDevice> devices, String serialNumber)
    {
		_btAdapter = BluetoothAdapter.getDefaultAdapter();
   	
    	int retValue = EobrReturnCode.S_SUCCESS;
    	
    	// if any devices discovered, determine if they are eobrs - 
    	// connect to each device and get serial number and passkey
    	if (devices != null && devices.size() > 0)
    	{
			Log.v("Comm", String.format("devices discovered: %s", devices));
    		for (BluetoothDevice btDevice : devices) {
    			ValidatedDevice eobrDevice = checkValidDevice(btDevice, serialNumber);
				if (eobrDevice != null)
				{
					this.AddAvailableBtEobr(eobrDevice.getDeviceName(), btDevice.getAddress(), eobrDevice.getDeviceCrc(), 1);
				}
    		}    		
    	}		    	
    	else
    	{
    		retValue = EobrReturnCode.S_DEV_NOT_CONNECTED;
    	}
    	
    	return retValue;
    }
    
    private ValidatedDevice checkValidDevice(BluetoothDevice device, String serialNumberToFind)
    {
    	ValidatedDevice result = null;
    	
		_localSocket = OpenBluetoothSocket(getCommThreadManager(), device);
		if (_localSocket != null)
		{
			_isSocketConnected = true;

			Bundle bundle = GetEobrSerialNumber();
			if (bundle.getInt(Constants.RETURNCODE) == EobrReturnCode.S_SUCCESS)
			{
				// if serial number is entered, we're provisioning a new EOBR.
				boolean serialNumberValid = false;
				String serialNumber = bundle.getString(Constants.RETURNVALUE);
				if (serialNumberToFind != null && serialNumberToFind.length() > 0)
				{
					if (serialNumber.endsWith(serialNumberToFind))
					{
						serialNumberValid = true;
					}
				}
				else
				{
					// not provisioning a new eobr, so don't need to check serial number
					serialNumberValid = true;
				}

				if (serialNumberValid)
				{
					short crcValue = (short)CalcCRC.Calculate(serialNumber, serialNumber.length());
					this.SetHandshakeCrc(crcValue);

					bundle.clear();
					bundle = this.GetCompanyPasskey();
					if (bundle.getInt(Constants.RETURNCODE) == EobrReturnCode.S_SUCCESS)
					{
						if (this.FilterEobrByPasskey(_companyPasskey, bundle.getString(Constants.RETURNVALUE)))
						{
							bundle.clear();
							bundle = GetUnitId();

							if (bundle.getInt(Constants.RETURNCODE) == EobrReturnCode.S_SUCCESS)
							{
								String deviceName = bundle.getString(Constants.RETURNVALUE);
								result = new ValidatedDevice(deviceName, crcValue);
							}
						}
					}

					this.ClearHandshakeCrc();
				}
			}

			closeLocalSocket();
		}
		
		return result;
    }

	@Override
	protected BluetoothSocket OpenBluetoothSocket(CommThreadManager ctMgr, BluetoothDevice device)
	{
		BluetoothSocket socket = null;

    	// Get a BluetoothSocket for a connection with the
    	// given BluetoothDevice
        try {
        	// Gingerbread method call to get insecure socket (2.3.3 - API 10)
        	//socket = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
			Log.v("Comm", String.format("About to connect socket to address: %s name: %s", device.getAddress(), device.getName()));
                	
            // Reflection method call to get insecure socket prior to Gingerbread (2.3.2 and prior - back to 2.0 I believe)
        	Method m = device.getClass().getMethod("createInsecureRfcommSocket", new Class[] {int.class});
          	socket = (BluetoothSocket) m.invoke(device, 1);            	                	
          	
        	// Open socket - Note:  This is a blocking call
    		if (!ctMgr.ConnectToBTSocket(socket))
    		{
   				socket = null;
    		}
			
			Log.v("Comm", "BTGenI.OpenBluetoothSocket AfterConnectToBTSocket");
                	
        } catch (IOException e) {    				
			socket = null;
        } catch (NoSuchMethodException e) {
			socket = null;
		} catch (IllegalAccessException e) {
			socket = null;
		} catch (NullPointerException e) {
			// Reflection method does not work for Samsung devices returns NullPointerException - 
			// try a different method to connect to the socket
			socket = CreateRfcommSocketToServiceRecord(ctMgr, device, -1, MY_UUID, true);
		} catch (InvocationTargetException e) {
			// Reflection method does not work for LG phones returns InvocationTargetException - 
			// try a different method to connect to the socket
			socket = CreateRfcommSocketToServiceRecord(ctMgr, device, -1, MY_UUID, true);
		}                      

		return socket;
    }

    @Override
	protected BluetoothSocket SetupAndOpenBluetoothSocket(CommThreadManager ctMgr, BluetoothAdapter btAdapter, String btAddress)
    {
		BluetoothSocket socket = null;
		BluetoothDevice device = null;
		
    	// Get a BluetoothSocket for a connection with the given Bluetooth address
        try {
        	device = btAdapter.getRemoteDevice(btAddress);
        	
        	// Gingerbread method call to get insecure socket (2.3.3 - API 10)
        	//socket = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
			Log.v("Comm", String.format("About to connect (SetupAndOpen) socket to address: %s name: %s", device.getAddress(), device.getName()));
                	
            // Reflection method call to get insecure socket prior to Gingerbread (2.3.2 and prior - back to 2.0 I believe)
        	Method m = device.getClass().getMethod("createInsecureRfcommSocket", new Class[] {int.class});
          	socket = (BluetoothSocket) m.invoke(device, 1);            	
                	
        	// Open socket - Note:  This is a blocking call
    		if (!ctMgr.ConnectToBTSocket(socket))
    		{
   				socket = null;
    		}
          	
			Log.v("Comm", "BTGenI.SetupAndOpenBluetoothSocket AfterConnectToBTSocket");
        } catch (IOException e) {    				
           	socket = null;
        } catch (NoSuchMethodException e) {
			socket = null;
		} catch (IllegalAccessException e) {
			socket = null;
		} catch (NullPointerException e) {
			// Reflection method does not work for Samsung devices returns NullPointerException - 
			// try a different method to connect to the socket
			if (device != null)
				socket = CreateRfcommSocketToServiceRecord(ctMgr, device, -1, MY_UUID, true);
		} catch (InvocationTargetException e) {
			// Reflection method does not work for LG phones returns InvocationTargetException - 
			// try a different method to connect to the socket
			if (device != null)
				socket = CreateRfcommSocketToServiceRecord(ctMgr, device, -1, MY_UUID, true);
		}                       

		return socket;
    }

	private BluetoothSocket CreateRfcommSocketToServiceRecord(CommThreadManager ctMgr, BluetoothDevice device, int port, UUID uuid, boolean encrypt) 
	{
        BluetoothSocket socket = null;

		try {
        	Constructor<BluetoothSocket> constructor = BluetoothSocket.class.getDeclaredConstructor(
        			int.class, int.class, boolean.class, boolean.class, BluetoothDevice.class, int.class, ParcelUuid.class);
        	if(constructor == null)
        		return null;

        	constructor.setAccessible(true);

        	Field f_rfcomm_type = BluetoothSocket.class.getDeclaredField("TYPE_RFCOMM");
    		f_rfcomm_type.setAccessible(true);

    		int rfcomm_type = (Integer)f_rfcomm_type.get(null);
        	
    		socket = constructor.newInstance(new Object[] { rfcomm_type, -1, false, true, device, port, uuid != null ? new ParcelUuid(uuid) : null} );
    		if (!ctMgr.ConnectToBTSocket(socket))
    		{
   				socket = null;
    		}
    		
			Log.v("Comm", "EobrBTHelper.CreateRfcommSocketToServiceRecord AfterConnectToBTSocket");

        	return socket;        
    	} catch (IOException e) {
    		return null;
    	} catch (NoSuchMethodException e) {
    		return null;
    	} catch (NoSuchFieldException e) {
    		return null;
    	} catch(IllegalAccessException e) {
    		return null;
    	} catch(InstantiationException e) {
    		return null;
    	} catch(InvocationTargetException e) {
    		return null;
    	}
	}
	    
    // Inherited from IEobrEngine
    public Bundle GetReferenceTimestamp()
    {
    	int cmdId = EUCMDType.EUCMD_GET_REF_TIMESTAMP;
    	int errorCode = EobrReturnCode.S_SUCCESS;
    	Bundle answer = new Bundle();
    	Date referenceTime = null;
    	
    	
    	Eobr_Comm_Response_Packet commResponsePacket = new Eobr_Comm_Response_Packet();
        errorCode = EobrCommunications.Companion.validateNoData(_threadMgr, cmdId,
                this.GetActiveDeviceCrc(),
                VerifyRxPacketResponseByte.EobrClockPacket, commResponsePacket, socketVerifier);
    	
    	if (errorCode == EobrReturnCode.S_SUCCESS) {
    		EobrClockPacket eobrGetClockPacket = new EobrClockPacket(commResponsePacket.getResponse());
    		
            // Byte pattern sent by EOBR to indicate GetRefTimestamp failure
            // Error happen, size is 1
    		if (eobrGetClockPacket.getSize() == 1 && eobrGetClockPacket.getYear() == 1)
    			errorCode = EobrReturnCode.S_DEV_INTERNAL_ERROR;
    		else {
    			// if reference time not set on eobr (1/1/0001 12:00 AM)
    			// set all values to -1
   			 	if (eobrGetClockPacket.getYear() == 0) {
   			 		referenceTime = null;
   			 	}
   			 	else {
   	                Calendar cal = Calendar.getInstance();
   	                cal.setTimeZone(TimeZone.getTimeZone("GMT"));
   	            	cal.set(Calendar.YEAR, eobrGetClockPacket.getYear() + 2000);
   	            	cal.set(Calendar.MONTH, eobrGetClockPacket.getMonth() - 1);
   	            	cal.set(Calendar.DAY_OF_MONTH, eobrGetClockPacket.getDay());
   	            	cal.set(Calendar.HOUR_OF_DAY, eobrGetClockPacket.getHour());
   	            	cal.set(Calendar.MINUTE, eobrGetClockPacket.getMinute());
   	            	cal.set(Calendar.SECOND, eobrGetClockPacket.getSecond());

   	            	referenceTime = cal.getTime();
		       	}
    		}
		}    	
    	
		answer.putInt(Constants.RETURNCODE, errorCode);
    	if (referenceTime != null)
    		answer.putLong(Constants.RETURNVALUE, referenceTime.getTime());
    	
    	return answer;
    }

	@Override
	public Bundle GetDistHours(long timecode) {
		Bundle bundle = new Bundle();
		bundle.putInt(Constants.RETURNCODE, EobrReturnCode.S_FUNC_NOT_IMPLEMENTED);
		bundle.putInt(Constants.RETURNVALUE, 0);
		return bundle;
	}

	public Bundle GetVin() {
		return null;
	}

    public Bundle GetCompanyPasskey()
    {
    	int cmdId = EUCMDType.EUCMD_GET_COMPANY_PASSKEY;
        Bundle answer = EobrCommunications.Companion.processResponse(_threadMgr, cmdId,
                this.GetActiveDeviceCrc(),
                socketVerifier);
    	return answer;
    }

    public int SetCompanyPasskey(String passkey)
    {
    	int errorCode;
    	int cmdId = EUCMDType.EUCMD_SET_COMPANY_PASSKEY;
    	int dataSize = passkey.length();

    	Eobr_Packet eobrPacket = new Eobr_Packet();
		eobrPacket.setCmd((byte)cmdId);
		eobrPacket.setCrc(this.GetActiveDeviceCrc());
		eobrPacket.setLen((byte)dataSize);

		byte[] packetData = new byte[dataSize];
    	for (int i=0; i<dataSize; i++)
    		packetData[i] = (byte) passkey.charAt(i);
    	
    	eobrPacket.setData(packetData);
    	
    	Eobr_Comm_Response_Packet commResponsePacket = new Eobr_Comm_Response_Packet();
        errorCode = EobrCommunications.Companion.validateWithData(_threadMgr, eobrPacket, cmdId,
                this.GetActiveDeviceCrc(), VerifyRxPacketResponseByte.EobrNullPacket,
                commResponsePacket, false, socketVerifier);
    	
    	return errorCode;    	
    }

    public Bundle GetClockUTC()
    {
    	int errorCode;
    	int cmdId = EUCMDType.EUCMD_GET_CLOCK_UTC;
    	Bundle answer = new Bundle();
    	Date eobrClockUtc = null;
    	
    	Eobr_Comm_Response_Packet commResponsePacket = new Eobr_Comm_Response_Packet();
        errorCode = EobrCommunications.Companion.validateNoData(_threadMgr, cmdId,
                this.GetActiveDeviceCrc(),
                VerifyRxPacketResponseByte.EobrClockPacket, commResponsePacket, socketVerifier);
    	
    	if (errorCode == EobrReturnCode.S_SUCCESS) {
    		EobrClockPacket eobrGetClockPacket = new EobrClockPacket(commResponsePacket.getResponse());
    		
            // Byte pattern sent by EOBR to indicate RTC failure
            // Getclock size 1 is error
    		if (eobrGetClockPacket.getSize() == 1 && eobrGetClockPacket.getYear() == 1)
    			errorCode = EobrReturnCode.S_DEV_INTERNAL_ERROR;
    		else {
                Calendar cal = Calendar.getInstance();
                cal.setTimeZone(TimeZone.getTimeZone("GMT"));
            	cal.set(Calendar.YEAR, eobrGetClockPacket.getYear() + 2000);
            	cal.set(Calendar.MONTH, eobrGetClockPacket.getMonth() - 1);
            	cal.set(Calendar.DAY_OF_MONTH, eobrGetClockPacket.getDay());
            	cal.set(Calendar.HOUR_OF_DAY, eobrGetClockPacket.getHour());
            	cal.set(Calendar.MINUTE, eobrGetClockPacket.getMinute());
            	cal.set(Calendar.SECOND, eobrGetClockPacket.getSecond());

            	eobrClockUtc = cal.getTime();
    		}
    	}    	
    	
    	answer.putInt(Constants.RETURNCODE, errorCode);
    	if (eobrClockUtc != null)
    		answer.putLong(Constants.RETURNVALUE, eobrClockUtc.getTime());
    	
    	return answer;    	
    }

	public EobrResponse<Date> GetGPSTimestamp()
	{
		return null;
	}

    public int SetClockUTC(Date newClock)    
    {
    	int returnCode;
    	int cmdId = EUCMDType.EUCMD_SET_CLOCK_UTC;
  
		Calendar cal = Calendar.getInstance();
		cal.setTimeZone(TimeZone.getTimeZone("GMT"));
		cal.setTime(newClock);
		
		EobrTimestamp eobrTimestamp = new EobrTimestamp(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));

    	if (eobrTimestamp.year < EOBR_MIN_YEAR || eobrTimestamp.year > EOBR_MAX_YEAR)
    		returnCode = EobrReturnCode.S_INVALID_DATE_TIME;
    	else
    	{
    		Eobr_Packet eobrPacket = new Eobr_Packet();
    		eobrPacket.setCmd((byte)cmdId);
    		eobrPacket.setCrc(this.GetActiveDeviceCrc());
    		eobrPacket.setLen((byte)EUTIMESTAMP_SIZE); 

    		byte[] data = new byte[EUTIMESTAMP_SIZE];
    		
    		int year = 0;
        	if (eobrTimestamp.getYear() < EOBR_MIN_YEAR)
        		year = EOBR_MIN_YEAR_OFFSET;
        	else if (eobrTimestamp.getYear() > EOBR_MAX_YEAR)
        		year = EOBR_MAX_YEAR_OFFSET;
        	else
        		year = eobrTimestamp.getYear() - 2000;
        	
        	data[0] = (byte)year;
        	data[1] = (byte)(eobrTimestamp.getMonth() + 1);
        	data[2] = (byte)eobrTimestamp.getDay();
        	data[3] = (byte)eobrTimestamp.getHour();
        	data[4] = (byte)eobrTimestamp.getMinute();
        	data[5] = (byte)eobrTimestamp.getSecond();
        	
        	eobrPacket.setData(data);
        	
        	Eobr_Comm_Response_Packet commResponsePacket = new Eobr_Comm_Response_Packet();
            returnCode =
                    EobrCommunications.Companion.validateWithData(_threadMgr, eobrPacket, cmdId,
                            this.GetActiveDeviceCrc(), VerifyRxPacketResponseByte.EobrNullPacket,
                            commResponsePacket, false, socketVerifier);
    	}
    	
    	return returnCode;
    }

    public Bundle GetActiveBusType()
    {
    	int errorCode;
    	int cmdId = EUCMDType.EUCMD_GET_ACTIVE_BUS;
    	Bundle answer = new Bundle();
    	int currentBusType = 0;
    	
    	Eobr_Comm_Response_Packet commResponsePacket = new Eobr_Comm_Response_Packet();
        errorCode = EobrCommunications.Companion.validateNoData(_threadMgr, cmdId,
                this.GetActiveDeviceCrc(),
                VerifyRxPacketResponseByte.EobrBytePacket, commResponsePacket, socketVerifier);
    	
    	if (errorCode == EobrReturnCode.S_SUCCESS) {
    		EobrBytePacket eobrBusTypePacket = new EobrBytePacket(commResponsePacket.getResponse());
    		currentBusType = eobrBusTypePacket.getByteVal();
    		
            // Byte pattern sent by EOBR to indicate failure
           	// Error happen, size is 1
    		if (eobrBusTypePacket.getSize() == 0xFF && currentBusType == 1)
    			errorCode = EobrReturnCode.S_DEV_INTERNAL_ERROR;        			
    	}
    	
    	answer.putInt(Constants.RETURNCODE, errorCode);
    	answer.putInt(Constants.RETURNVALUE, currentBusType);
    	    	
    	return answer;    	
    }

	public int ClearAllRecordData(int clearFlags){
    	int errorCode;
    	int cmdId = EUCMDType.EUCMD_CLEAR_RECORD_DATA;
    	int dataSize = 1;

    	Eobr_Packet eobrPacket = new Eobr_Packet();
		eobrPacket.setCmd((byte)cmdId);
		eobrPacket.setCrc(this.GetActiveDeviceCrc());
		eobrPacket.setLen((byte)dataSize);

		byte[] packetData = new byte[dataSize];
		packetData[0] = (byte)clearFlags;
    	
    	eobrPacket.setData(packetData);
    	
    	Eobr_Comm_Response_Packet commResponsePacket = new Eobr_Comm_Response_Packet();
        errorCode = EobrCommunications.Companion.validateWithData(_threadMgr, eobrPacket, cmdId,
                this.GetActiveDeviceCrc(), VerifyRxPacketResponseByte.EobrNullPacket,
                commResponsePacket, false, socketVerifier);
    	    	
    	return errorCode;    	
	}
    
    public int GetEobrData(StatusRecord statusRec, StatusRecordQueryMethodEnum queryMethod, int recordId, Date timeCode, StatusRecordMotionOptionEnum motionOption, boolean resetReferenceTimestampToCurrent)
    {
    	int cmdId = EUCMDType.EUCMD_GET_EOBR_DATA;
    	int returnCode;
		Eobr_Data convertData = new Eobr_Data();
		EobrTimestamp eobrTimestamp;
		byte method;
		byte year = (byte)0;
		byte month = (byte)0;
		byte day = (byte)0;
		byte hour = (byte)0;
		byte minute = (byte)0;
		byte second = (byte)0;
		
		// if querying by timestamp, setup eobrtimestamp from timecode passed in
     	if(queryMethod.getValue() == StatusRecordQueryMethodEnum.TIMESTAMP)
     	{     		
     		// if querymethod is timestamp and timestamp is null, then get the oldest
     		// record - use recordid method with recordId value = 0
     		if (timeCode == null)
     		{
     			method = (byte)RECORD_ID_TYPE;
     			recordId = 0;
     		}
     		// else setup the eobr timestamp from timecode specified
     		else
     		{
     			method = (byte)TIMESTAMP_TYPE;
     			
	     		Calendar cal = Calendar.getInstance();
	     		cal.setTimeZone(TimeZone.getTimeZone("GMT"));		
	     		cal.setTime(timeCode);
			
	     		eobrTimestamp = new EobrTimestamp(cal.get(Calendar.YEAR)-2000, cal.get(Calendar.MONTH)+1, cal.get(Calendar.DATE), cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
	     		year = (byte)eobrTimestamp.getYear();
	     		month = (byte)eobrTimestamp.getMonth();
	     		day = (byte)eobrTimestamp.getDay();
	     		hour = (byte)eobrTimestamp.getHour();
	     		minute = (byte)eobrTimestamp.getMinute();
	     		second = (byte)eobrTimestamp.getSecond();
     		}
     	}
     	else
     	{
     		method = (byte)RECORD_ID_TYPE;
     	}
     		
     	Eobr_Data_Packet eobrDataPacket = new Eobr_Data_Packet();
    	eobrDataPacket.setCmd((byte)cmdId);
    	eobrDataPacket.setLen((byte)13);  //sizeof(EOBR_GET_DATA_PACKET) - 4  //minus bCmd, wCrc, bLen
    	eobrDataPacket.setCrc(this.GetActiveDeviceCrc());
   		eobrDataPacket.setMethod(method);
    	eobrDataPacket.setRecordIdUnion(recordId);
    	eobrDataPacket.setYearUnion(year);
    	eobrDataPacket.setMonthUnion(month);
    	eobrDataPacket.setDayUnion(day);
    	eobrDataPacket.setHour(hour);
    	eobrDataPacket.setMinute(minute);
    	eobrDataPacket.setSecond(second);
    	
    	if (motionOption.getValue() == StatusRecordMotionOptionEnum.NEXTRECORD)
    		eobrDataPacket.setTimeOrMotionOption((byte)SPECIFIED_TIMESTAMP);
    	else
    		eobrDataPacket.setTimeOrMotionOption((byte)NEXT_MOTION);

   		eobrDataPacket.setRefTimestampOption((byte)(resetReferenceTimestampToCurrent?1:0));
   		
   		
   		Eobr_Comm_Response_Packet commResponsePacket = new Eobr_Comm_Response_Packet();
		returnCode = EobrCommunications.Companion.validateGenI(_threadMgr, eobrDataPacket, cmdId,
				this.GetActiveDeviceCrc(), commResponsePacket, resetReferenceTimestampToCurrent,
				socketVerifier);
   		
		if (returnCode == EobrReturnCode.S_SUCCESS) {
			this.RecorderDataToUserData(commResponsePacket.getResponse(), convertData);
			this.CopyEobrDataToStatusRecord(statusRec, convertData);
		}
   		
    	return returnCode;
    }
        
    public Bundle ReadDataCollectionRate()
    {
    	int errorCode;
    	int cmdId = EUCMDType.EUCMD_GET_DATA_RATE;
    	Bundle answer = new Bundle();
    	int dataRate = 0;
    	
    	Eobr_Comm_Response_Packet commResponsePacket = new Eobr_Comm_Response_Packet();
        errorCode = EobrCommunications.Companion.validateNoData(_threadMgr, cmdId,
                this.GetActiveDeviceCrc(),
                VerifyRxPacketResponseByte.EobrIntegerPacket, commResponsePacket, socketVerifier);
    	
    	if (errorCode == EobrReturnCode.S_SUCCESS) {
    		EobrIntegerPacket eobrDataCollectionRatePacket = new EobrIntegerPacket(commResponsePacket.getResponse());
           	dataRate = eobrDataCollectionRatePacket.getIntegerVal();              	       	       	       	       	       	       	       

            // Byte pattern sent by EOBR to indicate failure
           	// Error happen, size is 1
    		if (eobrDataCollectionRatePacket.getSize() == 1 && dataRate == 1)
    			errorCode = EobrReturnCode.S_DEV_INTERNAL_ERROR;              		
    	}
    	
    	answer.putInt(Constants.RETURNCODE, errorCode);
    	answer.putInt(Constants.RETURNVALUE, dataRate);
    	
    	return answer;    	
    }

    public int ChangeDataCollectionRate(int newDataRate)
    {
    	int errorCode;
    	int cmdId = EUCMDType.EUCMD_CHANGE_DATA_RATE;
    	
    	Eobr_Packet eobrPacket = new Eobr_Packet();
		eobrPacket.setCmd((byte)cmdId);
		eobrPacket.setCrc(this.GetActiveDeviceCrc());
		eobrPacket.setLen((byte)4);
		eobrPacket.setData(intToByteArray(newDataRate));
		
		Eobr_Comm_Response_Packet commResponsePacket = new Eobr_Comm_Response_Packet();
        errorCode = EobrCommunications.Companion.validateWithData(_threadMgr, eobrPacket, cmdId,
                this.GetActiveDeviceCrc(), VerifyRxPacketResponseByte.EobrNullPacket,
                commResponsePacket, false, socketVerifier);
    	    	
    	return errorCode;    	   	
    }

	/// <summary>
	/// This function doesn't exist for Gen I - set return code value to S_FUNC_NOT_IMPLEMENTED
	/// </summary>
	public Bundle GetEobrOdometerOffset()
	{
		Bundle bundle = new Bundle();
		
    	bundle.putInt(Constants.RETURNCODE, EobrReturnCode.S_FUNC_NOT_IMPLEMENTED);
    	bundle.putFloat(Constants.RETURNVALUE, 0F);
    	
    	return bundle;
	}

	/// <summary>
	/// This function doesn't exist for Gen I - set return code value to S_FUNC_NOT_IMPLEMENTED
	/// </summary>
    public int SetEobrOdometerOffset(float offset)
    {
    	return EobrReturnCode.S_FUNC_NOT_IMPLEMENTED;
    }
    
	/// <summary>
	/// This function doesn't exist for Gen I - set return code value to S_FUNC_NOT_IMPLEMENTED
	/// </summary>    
    public Bundle SendConsoleCommandToDevice(String command) {
    	Bundle bundle = new Bundle();
    	bundle.putInt(Constants.RETURNCODE, EobrReturnCode.S_FUNC_NOT_IMPLEMENTED);
    	bundle.putInt(Constants.RETURNVALUE, 0);
    	return bundle;
    }
    
	/// <summary>
	/// This function doesn't exist for Gen I - set return code value to S_FUNC_NOT_IMPLEMENTED
	/// </summary>    
    public Bundle SendConsoleCommandToDeviceWithNoRetry(String command) {
    	Bundle bundle = new Bundle();
    	bundle.putInt(Constants.RETURNCODE, EobrReturnCode.S_FUNC_NOT_IMPLEMENTED);
    	bundle.putInt(Constants.RETURNVALUE, 0);
    	return bundle;
    }
    
	public boolean SetSelfTest()
	{
		return false;
	}

	public Bundle GetSelfTest()
	{
		Bundle bundle = new Bundle();
    	bundle.putInt(Constants.RETURNCODE, EobrReturnCode.S_FUNC_NOT_IMPLEMENTED);
    	bundle.putInt(Constants.RETURNVALUE, 0);
    	return bundle;
	}
    
	public boolean sendBulkData(Eobr_Packet eobrPacket, int packetSize) {

		boolean retVal = false;
		//Log.v("Comm", "sendBulkData: Starting.");
		
		if (!_isSocketConnected || _localSocket == null)
		{
			if (this.HasActiveDevice())
			{
				_localSocket = this.SetupAndOpenBluetoothSocket(getCommThreadManager(),_btAdapter, this.GetActiveDeviceAddress());
				if (_localSocket != null){
					Log.v("Comm", "sendBulkData: socket connected.");
					_isSocketConnected = true;
				}
			}
		}

		if (_isSocketConnected && _localSocket != null)
		{
			OutputStream out = null;
			try {
				out = _localSocket.getOutputStream();
				//Log.v("Comm", "sendBulkData: got outputstream.");

			    // Send "##" first so that ST knows we are sending valid data
				byte[] bt_data_prefix = BT_DATA_PREFIX.getBytes();
				out.write(bt_data_prefix);
				out.flush();
	
				byte[] message = new byte[packetSize];
				message[0] = eobrPacket.getCmd();
				byte[] crc = shortToByteArray(eobrPacket.getCrc());
				message[1] = crc[0]; // first byte of crc
				message[2] = crc[1]; // second byte of crc
				message[3] = eobrPacket.getLen();
				
				if (packetSize > (EOBR_PACKET_SIZE - EOBR_PAYLOAD_SIZE))
				{
					for (int i=0; i<packetSize - (EOBR_PACKET_SIZE - EOBR_PAYLOAD_SIZE); i++)
					{
						message[i+4] = eobrPacket.getData()[i];
					}
				}
				
				out.write(message);
				out.flush();
				retVal = true;
			}
			catch (IOException e) {	
				Log.e("Comm", "Error on sending BT sendBulkData");
				Log.e("Comm", e.getMessage());
				_isSocketConnected = false;
				retVal = false;
			}
		}
		
		return retVal;
	}
	
    // Inherited from IEobrEngine
	public boolean sendBulkData(Eobr_FW_Block_Packet eobrPacket, int packetSize) {

		boolean retVal = false;
		//Log.v("Comm", String.format("sendBulkData: Starting cmd: %s", eobrPacket.getCmd()));
		
		if (!_isSocketConnected || _localSocket == null)
		{
			if (this.HasActiveDevice())
			{
				_localSocket = this.SetupAndOpenBluetoothSocket(getCommThreadManager(),_btAdapter, this.GetActiveDeviceAddress());
				if (_localSocket != null){
					Log.v("Comm", "sendBulkData: socket connected.");
					_isSocketConnected = true;
				}
			}
		}

		if (_isSocketConnected && _localSocket != null)
		{
			OutputStream out = null;
			try {
				out = _localSocket.getOutputStream();
				//Log.v("Comm", "sendBulkData: got outputstream.");

			    // Send "##" first so that ST knows we are sending valid data
		    	byte[] bt_data_prefix = BT_DATA_PREFIX.getBytes();
				out.write(bt_data_prefix);
				out.flush();
	
				byte[] message = new byte[packetSize];
				message[0] = eobrPacket.getCmd();
				byte[] crc = shortToByteArray(eobrPacket.getCrc());
				message[1] = crc[0]; // first byte of crc
				message[2] = crc[1]; // second byte of crc
				message[3] = eobrPacket.getLen();
				message[4] = eobrPacket.getFWType();				
				
				for (int i=5; i<eobrPacket.getLen() + EOBR_FW_PACKET_SIZE - EOBR_FW_PAYLOAD_SIZE; i++)
				{
					int dataIndex = i-(EOBR_FW_PACKET_SIZE - EOBR_FW_PAYLOAD_SIZE);
					if(dataIndex < eobrPacket.getData().length){
						message[i] = eobrPacket.getData()[dataIndex];
					}
				}
				
				out.write(message);
				out.flush();
				retVal = true;
			}
			catch (IOException e) {	
				Log.e("Comm", "Error on sending BT sendBulkData");
				Log.e("Comm", e.getMessage());
				_isSocketConnected = false;
				retVal = false;
			}
		}
		
		return retVal;
	}
	
	public boolean sendEobrDataPacketBulkData(Eobr_Data_Packet_GenII dataPacket)
	{
		return false;
	}
	
	public boolean sendEobrDataPacketBulkData(Eobr_Data_Packet dataPacket)
	{
		boolean retVal = false;

		if (!_isSocketConnected || _localSocket == null)
		{
			_localSocket = this.SetupAndOpenBluetoothSocket(getCommThreadManager(), _btAdapter, this.GetActiveDeviceAddress());
			if (_localSocket != null)
				_isSocketConnected = true;
		}

		if (_isSocketConnected && _localSocket != null)
		{
			OutputStream out = null;
			try {
				out = _localSocket.getOutputStream();
	
			    // Send "##" first so that ST knows we are sending valid data
		    	byte[] bt_data_prefix = BT_DATA_PREFIX.getBytes();
				out.write(bt_data_prefix);
				out.flush();

				byte[] message = new byte[dataPacket.getLen() + 4];
				message[0] = dataPacket.getCmd();
				byte[] crc = shortToByteArray(dataPacket.getCrc());
				message[1] = crc[0]; // first byte of crc
				message[2] = crc[1]; // second byte of crc
				message[3] = dataPacket.getLen();
				message[4] = dataPacket.getMethod();
				byte[] recordId = intToByteArray(dataPacket.getRecordIdUnion());
				message[5] = recordId[0];
				message[6] = recordId[1];
				message[7] = recordId[2];
				message[8] = recordId[3];
				message[9] = dataPacket.getYearUnion();
				message[10] = dataPacket.getMonthUnion();
				message[11] = dataPacket.getDayUnion();
				message[12] = dataPacket.getHour();
				message[13] = dataPacket.getMinute();
				message[14] = dataPacket.getSecond();
				message[15] = dataPacket.getTimeOrMotionOption();
				message[16] = dataPacket.getRefTimestampOption();
				
				out.write(message);
				out.flush();
				retVal = true;
			}
			catch (IOException e) {
				_isSocketConnected = false;
				retVal = false;
			}
		}
		
		return retVal;		
	}

	public boolean sendEobrDriverEventPacket(Eobr_Driver_Event_Packet driverPacket)
	{
		return false;
	}

	///////////////////////////////////////////////////////////
	// TEMPORARY - boolean parameter is temporary - needed for 
	// usb simulated data - not used for bluetooth
	///////////////////////////////////////////////////////////	
    // Inherited from IEobrEngine
	
	private Object btReaderLock = new Object();

	public byte[] receiveBulkData(boolean eobrData)
	{
    	boolean retVal = false;
    	int bytesRead = 0;
    	int totalBytesRead = 0;
    	byte[] buffer = new byte[RECEIVED_PACKET_SIZE];
    	byte[] response = new byte[RECEIVED_PACKET_SIZE];

    	boolean interrupted = false;
    	
		if (!_isSocketConnected || _localSocket == null)
		{
			_localSocket = this.SetupAndOpenBluetoothSocket(getCommThreadManager(), _btAdapter, this.GetActiveDeviceAddress());
			if (_localSocket != null)
				_isSocketConnected = true;
		}

		boolean done = false;
		
		synchronized(btReaderLock)
		{
			if (_isSocketConnected)
			{
				if(bluetoothReader == null || bluetoothReader.getIsDone())
				{
					bluetoothReader = new BluetoothReader(_localSocket);
					bluetoothReader.start();
				}
				
				while (!done && !interrupted)
				{	
					if(Thread.currentThread().isInterrupted())
					{
						interrupted = true;
						break;
					}
					
					bytesRead = bluetoothReader.read(buffer);
					
					if (bytesRead <= 0)
					{
						try {
							Thread.sleep(5);
						} catch (InterruptedException e) {
							interrupted = true;
						}
					}
					else if (bytesRead > 0)
					{
						for (int i=0; i<bytesRead; i++)
						{
							if (i+totalBytesRead < RECEIVED_PACKET_SIZE)
								response[i+totalBytesRead] = buffer[i];
						}
						totalBytesRead += bytesRead;
						
						if (!done && totalBytesRead >= 128) 
						{
							done = true;
							retVal = true;					
						}
						
						if(!done)
							bluetoothReader.continueReading();
					}
				}
				
				if (!retVal && totalBytesRead > 0)
					retVal = true;
			}
			
			if(done)
			{
				//stop waiting for more data - we don't want to accidentally
				//pick up data from the next command
				bluetoothReader.stopReading();
				
				//this method is ending normally.  If it was interrupted due to a timeout
				//then the bluetoothReader thread, which is still blocking on the read,
				//will still be around for the next ReceiveDataThread.	
				bluetoothReader = null;
			}
		}
		
		return response;
	}

	/**
	 * GetThresholdValues not implemented in Gen I.
	 */
    public Bundle GetThresholdValues(int thresholdType)
    {
		Bundle bundle = new Bundle();		
    	bundle.putInt(Constants.RETURNCODE, EobrReturnCode.S_FUNC_NOT_IMPLEMENTED);
    	
    	return bundle;
    }
    
    public Bundle SetThresholdValues(int rpmThreshold, float speedThreshold, float hardBrakeThreshold, float driveStartDistance, int driveStopTime, int eventBlanking, String driverId, float driveStartSpeed)
    {
		Bundle bundle = new Bundle();		
    	bundle.putInt(Constants.RETURNCODE, EobrReturnCode.S_SUCCESS);
    	return bundle;
    }
    
    public int DownloadFirmwareUpdate(InputStream firmwareUpdateFile, Enums.FirmwareUpgradeTypeEnum firmwareUpgradeType, FirmwareUpdateBroadcaster broadcaster, FirmwareUpdate firmwareUpdateConfig)
    {
    	int errorCode = EobrReturnCode.S_DEV_NOT_CONNECTED;
    	
    	int firmwareUpgrade = FW_ST_MICRO;    	
    	if(firmwareUpgradeType == Enums.FirmwareUpgradeTypeEnum.BOOTLOADER)    		
    		firmwareUpgrade = FW_BOOTLOADER;

    	if(firmwareUpgradeType == Enums.FirmwareUpgradeTypeEnum.APP)    		
    		firmwareUpgrade = FW_ST_MICRO;

    	// Begin Firmware Upgrade
    	errorCode = this.FW_BeginFirmwareDownload(FW_UPGRADE_BEGIN, firmwareUpgrade);
    	
        if (errorCode == EobrReturnCode.S_SUCCESS)
        {
        	this.CloseDevice();
        	
        	//Delay for 25 seconds to meet ST 15seconds powerup.
        	try {
    			Thread.sleep(25000);
    		} catch (InterruptedException e) {
    			
            	Log.e("UnhandledCatch", e.getMessage() + ": " + Log.getStackTraceString(e));
    		}
        	
        	this.OpenCurrentSocket();
        	
        	try{
	            // if it is bootloader it will return start address for which bootloader, 
	            // if App then erase the block in external flash for upcoming firmware to be write there.               
	        	errorCode = this.FW_GetFirmwareToBeDownload(firmwareUpgrade);
	        	
	            if (errorCode == EobrReturnCode.S_SUCCESS)
	            {
		        	if(firmwareUpgradeType == Enums.FirmwareUpgradeTypeEnum.BOOTLOADER)   { 	
		        		// Start Downloading Process
		        		errorCode = this.FW_FirmwareDownloadProcess_BootLoader(firmwareUpdateFile);
		        	}	
		        	else {
		        		// Application upgrade (ST, EZHost)	        		
	                	// Start Downloading Process
	                	errorCode = this.FW_FirmwareDownloadProcess_Application(firmwareUpdateFile);
		                	
		                if (errorCode == EobrReturnCode.S_SUCCESS)
		                {
		                    // Start OTG Download Process
		                	errorCode = FW_GetFirmwareToBeDownload(FW_EZ_HOST);  // Tell ST now is the time for OTG, erase OTG block.
		                    if(errorCode == EobrReturnCode.S_SUCCESS)
		                    {
		                    	errorCode = FW_FirmwareDownloadProcess_OTG(firmwareUpdateFile);
		                    }
		                } 
		        	}
	            }
        	}
        	finally
        	{
        	}
        	
            if (errorCode == EobrReturnCode.S_SUCCESS)
            {
            	// End Firmware Upgrade
            	errorCode = this.FW_BeginFirmwareDownload(FW_UPGRADE_END, firmwareUpgrade);
            }
            
            if (errorCode == EobrReturnCode.S_SUCCESS)
            {
            	// End Firmware Upgrade with a reset
            	this.FW_BeginFirmwareDownload(FW_UPGRADE_RESET, firmwareUpgrade);
            	// the reset is always successful
            	errorCode = EobrReturnCode.S_SUCCESS;            	
            }
            
        }
        
    	return errorCode;
    }

    public int GetEobrGeneration()
    {
    	return 1;
    }

    private int FW_BeginFirmwareDownload(int specialFirmwareCmdId, int firmwareUpgradeType)
    {
    	int errorCode;
    	int cmdId = EUCMDType.EUCMD_START_FW_UPGRADE;
    	
    	Log.v("Comm", "FW_BeginFirmwareDownload");
    	Eobr_Packet eobrPacket = new Eobr_Packet();
		eobrPacket.setCmd((byte)cmdId);
		eobrPacket.setCrc(this.GetActiveDeviceCrc());
		eobrPacket.setLen((byte)2);
		eobrPacket.setData(new byte[]{(byte)specialFirmwareCmdId, (byte)firmwareUpgradeType});
		
		Eobr_Comm_Response_Packet commResponsePacket = new Eobr_Comm_Response_Packet();
        errorCode = EobrCommunications.Companion.validateWithData(_threadMgr, eobrPacket, cmdId,
                this.GetActiveDeviceCrc(), VerifyRxPacketResponseByte.EobrNullPacket,
                commResponsePacket, false, socketVerifier);
    			    
    	Log.v("Comm", String.format("FW_BeginFirmwareDownload complete errorCode: %s", errorCode));
    	
    	return errorCode;    	   	
    }

    private int FW_GetFirmwareToBeDownload(int firmwareUpgradeType)
    {
    	int errorCode;
    	int cmdId = EUCMDType.EUCMD_GET_FW_IMAGE_REQ;
    	
    	Log.v("Comm", String.format("FW_GetFirmwareToBeDownload"));

    	Eobr_Packet eobrPacket = new Eobr_Packet();
		eobrPacket.setCmd((byte)cmdId);
		eobrPacket.setCrc(this.GetActiveDeviceCrc());
		eobrPacket.setLen((byte)1);
		eobrPacket.setData(new byte[]{(byte)firmwareUpgradeType});
				
    	Eobr_Comm_Response_Packet commResponsePacket = new Eobr_Comm_Response_Packet();
        errorCode = EobrCommunications.Companion.validateWithData(_threadMgr, eobrPacket, cmdId,
                this.GetActiveDeviceCrc(), VerifyRxPacketResponseByte.EobrNullPacket,
                commResponsePacket, false, socketVerifier);
    	
    	if (errorCode == EobrReturnCode.S_SUCCESS) {
	    	byte[] response = commResponsePacket.getResponse();
	    	
	    	// The return data should be in size of 4 bytes.
		    if ((int)response[1] != MAX_ADDRESS_SIZE) {
		        errorCode = EobrReturnCode.S_DEV_INTERNAL_ERROR;
		    }
		    else {		    	
		    	// note: the base address for the bootloader will be at either 0x2000 or 0x8000		    	
			    bootloaderAddrBase = DEFAULT_RESET_ADDRESS;
			    bootloaderAddrBase = (int)response[2] & 0xFF;        // First byte of LSB
			    bootloaderAddrBase += ((int)response[3] & 0xFF)<<SECOND_SHIFT;  // Second byte(shift 8bits to store as a 32bits)
			    bootloaderAddrBase += ((int)response[4] & 0xFF)<<THIRD_SHIFT; // Third byte(shift 16bits to store as a 32bits)
			    bootloaderAddrBase += ((int)response[5] & 0xFF)<<FOURTH_SHIFT; // MSB(shift 24bits to store as a 32bits)
		
		        if ((errorCode == EobrReturnCode.S_SUCCESS) && (((bootloaderAddrBase != DEFAULT_RESET_ADDRESS) && (firmwareUpgradeType == FW_BOOTLOADER)) ||
		              ((bootloaderAddrBase == DEFAULT_RESET_ADDRESS) && ((firmwareUpgradeType == FW_ST_MICRO)||(firmwareUpgradeType == FW_EZ_HOST))))) {		        	
		          	errorCode = EobrReturnCode.S_SUCCESS;
			    }
		        else {
					errorCode = ReceivedPacketHelper.Companion.getStatus(cmdId, response);
		        }
		    }    	
    	} 
		    	
    	Log.v("Comm", String.format("FW_GetFirmwareToBeDownload complete errorCode: %s", errorCode));

    	return errorCode;    	   	
    }

    private int FW_FirmwareDownloadProcess_BootLoader(InputStream firmwareUpdateFile)
    {
    	int errorCode = EobrReturnCode.S_GENERAL_ERROR;
    	int cmdId = EUCMDType.EUCMD_FW_IMAGE_BLOCK;
    	
    	Log.v("Comm", String.format("FW_FirmwareDownloadProcess_BootLoader"));

        int pos = bootloaderAddrBase;
        
    	Eobr_FW_Block_Packet eobrPacket = new Eobr_FW_Block_Packet();
		eobrPacket.setCmd((byte)cmdId);
		eobrPacket.setCrc(this.GetActiveDeviceCrc());
		eobrPacket.setLen((byte)(EOBR_FW_BOOTLOADER_BLOCK_CODE_SIZE+EOBR_FW_BLOCK_CODE_ADDRESS_SIZE+EOBR_FW_BLOCK_CODE_TYPE_SIZE));
		eobrPacket.setFWType((byte)FW_BOOTLOADER);
    	
		BufferedInputStream inputStream = null;
		try{	
			firmwareUpdateFile.reset();
			inputStream = new BufferedInputStream(firmwareUpdateFile);
			
	    	Log.v("Comm", String.format("skipping to starting point: pos: 0x%x", pos+INPUTFILE_OFFSET));
			inputStream.skip(pos+INPUTFILE_OFFSET);
			
		    for (int i=0; i < BOOTLOADER_FW_SIZE; i++)
		    {		    	
		    	byte[] posAddrVal = intToByteArray(pos);
		    	byte[] buffer = new byte[EOBR_FW_BOOTLOADER_BLOCK_CODE_SIZE + EOBR_FW_BLOCK_CODE_ADDRESS_SIZE];
		    	buffer[0] = posAddrVal[0];
		    	buffer[1] = posAddrVal[1];
		    	buffer[2] = posAddrVal[2];
		    	buffer[3] = posAddrVal[3];
		    	int bytesRead = inputStream.read(buffer, EOBR_FW_BLOCK_CODE_ADDRESS_SIZE, EOBR_FW_BOOTLOADER_BLOCK_CODE_SIZE);
		    	
		    	if(bytesRead > 0)
		    	{
			    	eobrPacket.setData(buffer);
			    	if(this.sendBulkData(eobrPacket, EOBR_FW_PACKET_SIZE))
			    	{
				    	//Log.v("Comm", String.format("about to receiveBulkData", errorCode));
				    	byte[] response = this.receiveBulkData(true);
				    	//Log.v("Comm", String.format("received %s", response.length));

						errorCode = ReceivedPacketHelper.Companion.getStatus(cmdId, response);
				    	//Log.v("Comm", String.format("sendBulkData errorCode: %s", errorCode));

			    		if(errorCode != EobrReturnCode.S_SUCCESS)
			    			break;
			    	}
			    	else 
			    	{
			    		errorCode = EobrReturnCode.S_DEV_INTERNAL_ERROR;
			    		break;
			    	}
		    	}
		    	else {
		    		errorCode = EobrReturnCode.S_DEV_INTERNAL_ERROR;
		    		break;
		    	}
		    	pos = pos + bytesRead;
		    }		    
		}
		catch(Exception e){
	    	Log.e("Comm", String.format("Error occurred"));
			e.printStackTrace();
			errorCode = EobrReturnCode.S_DEV_INTERNAL_ERROR;
		}		
		
    	Log.v("Comm", String.format("FW_FirmwareDownloadProcess_BootLoader complete errorCode: %s", errorCode));

    	return errorCode;    	   	
    }

    private int FW_FirmwareDownloadProcess_Application(InputStream firmwareUpdateFile)
    {
    	int errorCode = EobrReturnCode.S_GENERAL_ERROR;
    	int cmdId = EUCMDType.EUCMD_FW_IMAGE_BLOCK;
    	
    	Log.d("Comm", String.format("FW_FirmwareDownloadProcess_Application"));

        int pos = APPBASE_addr;     
        
    	Eobr_FW_Block_Packet eobrPacket = new Eobr_FW_Block_Packet();
		eobrPacket.setCmd((byte)cmdId);
		eobrPacket.setCrc(this.GetActiveDeviceCrc());
		eobrPacket.setLen((byte)(EOBR_FW_BLOCK_CODE_SIZE+EOBR_FW_BLOCK_CODE_ADDRESS_SIZE+EOBR_FW_BLOCK_CODE_TYPE_SIZE));
		eobrPacket.setFWType((byte)FW_ST_MICRO);
    	
		BufferedInputStream inputStream = null;
		try{		
			firmwareUpdateFile.reset();
			inputStream = new BufferedInputStream(firmwareUpdateFile);	
			
	    	Log.v("Comm", String.format("skipping to starting point: pos: 0x%x", pos+INPUTFILE_OFFSET));
			inputStream.skip(pos+INPUTFILE_OFFSET);
			
		    for (int i=0; i < APP_FW_SIZE; i++)
		    {
		    	//Log.v("Comm", String.format("preparing block: %s", i));
		    	
		    	byte[] posAddrVal = intToByteArray(pos);
		    	byte[] buffer = new byte[EOBR_FW_BLOCK_CODE_SIZE + EOBR_FW_BLOCK_CODE_ADDRESS_SIZE];
		    	buffer[0] = posAddrVal[0];
		    	buffer[1] = posAddrVal[1];
		    	buffer[2] = posAddrVal[2];
		    	buffer[3] = posAddrVal[3];
		    	int bytesRead = inputStream.read(buffer, EOBR_FW_BLOCK_CODE_ADDRESS_SIZE, EOBR_FW_BLOCK_CODE_SIZE);
		    	
		    	if(bytesRead > 0)
		    	{
			    	eobrPacket.setData(buffer);
			    	if(this.sendBulkData(eobrPacket, EOBR_FW_PACKET_SIZE))
			    	{
				    	//Log.v("Comm", String.format("about to receiveBulkData", errorCode));
				    	byte[] response = this.receiveBulkData(true);
				    	//Log.v("Comm", String.format("received %s", response.length));

						errorCode = ReceivedPacketHelper.Companion.getStatus(cmdId, response);
				    	//Log.v("Comm", String.format("sendBulkData errorCode: %s", errorCode));

			    		if(errorCode != EobrReturnCode.S_SUCCESS)
			    			break;
			    	}
			    	else 
			    	{
			    		errorCode = EobrReturnCode.S_DEV_INTERNAL_ERROR;
			    		break;
			    	}
		    	}
		    	else {
		    		errorCode = EobrReturnCode.S_DEV_INTERNAL_ERROR;
		    		break;
		    	}
		    	pos = pos + bytesRead;
		    }		    
		} 
		catch(Exception e){
	    	Log.e("Comm", String.format("Error occurred"));
			e.printStackTrace();
			errorCode = EobrReturnCode.S_DEV_INTERNAL_ERROR;			
		}
		
    	Log.d("Comm", String.format("FW_FirmwareDownloadProcess_Application complete errorCode: %s", errorCode));

    	return errorCode;    	   	
    }

    public int FW_FirmwareDownloadProcess_OTG(InputStream firmwareUpdateFile)
    {
    	int errorCode = EobrReturnCode.S_GENERAL_ERROR;
    	int cmdId = EUCMDType.EUCMD_FW_IMAGE_BLOCK;

    	Log.d("Comm", String.format("FW_FirmwareDownloadProcess_OTG"));

        int pos = OTG1BASE_addr;    
        
    	Eobr_FW_Block_Packet eobrPacket = new Eobr_FW_Block_Packet();
		eobrPacket.setCmd((byte)cmdId);
		eobrPacket.setCrc(this.GetActiveDeviceCrc());
		eobrPacket.setLen((byte)(EOBR_FW_BLOCK_CODE_SIZE+EOBR_FW_BLOCK_CODE_ADDRESS_SIZE+EOBR_FW_BLOCK_CODE_TYPE_SIZE));
		eobrPacket.setFWType((byte)FW_EZ_HOST);
		
		BufferedInputStream inputStream = null;
		try{	
			firmwareUpdateFile.reset();
			inputStream = new BufferedInputStream(firmwareUpdateFile);

	    	Log.v("Comm", String.format("skipping to starting point: pos: 0x%x", pos+INPUTFILE_OFFSET));
			inputStream.skip(pos+INPUTFILE_OFFSET);

		    for (int i=0; i < OTG_FW_SIZE; i++)
		    {		    	
		    	byte[] posAddrVal = intToByteArray(pos);
		    	byte[] buffer = new byte[EOBR_FW_BLOCK_CODE_SIZE + EOBR_FW_BLOCK_CODE_ADDRESS_SIZE];
		    	buffer[0] = posAddrVal[0];
		    	buffer[1] = posAddrVal[1];
		    	buffer[2] = posAddrVal[2];
		    	buffer[3] = posAddrVal[3];
		    	int bytesRead = inputStream.read(buffer, EOBR_FW_BLOCK_CODE_ADDRESS_SIZE, EOBR_FW_BLOCK_CODE_SIZE);
		    	
		    	if(bytesRead > 0)
		    	{
			    	//Log.v("Comm", String.format("bytes read: %s", bytesRead));
			    	eobrPacket.setData(buffer);
			    	if(this.sendBulkData(eobrPacket, EOBR_FW_PACKET_SIZE))
			    	{
				    	//Log.v("Comm", String.format("about to receiveBulkData", errorCode));
				    	byte[] response = this.receiveBulkData(true);
				    	//Log.v("Comm", String.format("received %s", response.length));

						errorCode = ReceivedPacketHelper.Companion.getStatus(cmdId, response);
				    	//Log.v("Comm", String.format("sendBulkData errorCode: %s", errorCode));

			    		if(errorCode != EobrReturnCode.S_SUCCESS)
			    			break;
			    	}
			    	else 
			    	{
			    		errorCode = EobrReturnCode.S_DEV_INTERNAL_ERROR;
			    		break;
			    	}
		    	}
		    	else {
		    		errorCode = EobrReturnCode.S_DEV_INTERNAL_ERROR;
		    		break;
		    	}
		    	pos = pos + bytesRead;
		    }		   
		}
		catch(Exception e){
	    	Log.e("Comm", String.format("Error occurred"));
			e.printStackTrace();
			errorCode = EobrReturnCode.S_DEV_INTERNAL_ERROR;
		}
		
    	Log.d("Comm", String.format("FW_FirmwareDownloadProcess_OTG complete errorCode: %s", errorCode));

    	return errorCode;    	   	
    }

	/// <summary>
	/// These functions doesn't exist for Gen I - set return code value to S_FUNC_NOT_IMPLEMENTED
	/// </summary>
    public int GetEventData(EventRecord eventRecordData, StatusRecordQueryMethodEnum queryMethod, int recordId, long timeCode, EventTypeEnum eventType, boolean resetReferenceTimestampToCurrent)
    {
    	return EobrReturnCode.S_FUNC_NOT_IMPLEMENTED;
    }
	public int GetEventData(EventRecord eventRecordData, StatusRecordQueryMethodEnum queryMethod, int recordId, long timeCode, EventTypeEnum eventType, boolean resetReferenceTimestampToCurrent, int eventMask)
	{
		return EobrReturnCode.S_FUNC_NOT_IMPLEMENTED;
	}
    public int GetTripData(TripReport eventData, StatusRecordQueryMethodEnum queryMethod, int recordId, long timeCode, boolean resetReferenceTimestampToCurrent)
    {
    	return EobrReturnCode.S_FUNC_NOT_IMPLEMENTED;
    }
    public int GetHistogramData(HistogramData histogramData, StatusRecordQueryMethodEnum queryMethod, int recordId, long timeCode, HistogramTypeEnum histogramType, boolean setRefTime)
    {
    	return EobrReturnCode.S_FUNC_NOT_IMPLEMENTED;
    }
    public int GetJBusDiagnosticDataFromDevice(JbusDiagnosticData diagnosticData, StatusRecordQueryMethodEnum queryMethod, int recordId, long timestamp, boolean setRefTime)
    {
    	return EobrReturnCode.S_FUNC_NOT_IMPLEMENTED;
    }
	public Bundle GetConsoleLog(Date startDate, Date endDate)
	{
		Bundle bundle = new Bundle();
		bundle.putInt(Constants.RETURNCODE, EobrReturnCode.S_FUNC_NOT_IMPLEMENTED);
		
		return bundle;
	}
	public boolean IsJJK(Context ctx)
	{
		return false;
	}
	public FirmwareUpgradeRequestResult RequestFirmwareUpgrade(long firmwarePatchId)
	{
		return new FirmwareUpgradeRequestResult(EobrReturnCode.S_FUNC_NOT_IMPLEMENTED);
	}
	
	public FirmwareUpgradeStatusResult GetFirmwareUpgradeStatus()
	{
		return new FirmwareUpgradeStatusResult(EobrReturnCode.S_FUNC_NOT_IMPLEMENTED);
	}
	
    private void OpenCurrentSocket()
    {
		if (!_isSocketConnected || _localSocket == null)
		{
			if (this.HasActiveDevice())
			{
				_localSocket = this.SetupAndOpenBluetoothSocket(getCommThreadManager(),_btAdapter, this.GetActiveDeviceAddress());
				if (_localSocket != null){
					Log.v("Comm", "sendBulkData: socket connected.");
					_isSocketConnected = true;
				}
			}
		}    	
    }


    private void RecorderDataToUserData(byte[] response, Eobr_Data eobrData)
    {
       	eobrData.setRecordID(LittleEndianHelper.Companion.getInt(response, 47, 4));

    	eobrData.gpsPosition = eobrData.new GPS_POSITION();

       	int gpsDay = ByteResponseHelper.Companion.getIntegerFromCharResponse(response, 34, 35);
       	int gpsMonth = ByteResponseHelper.Companion.getIntegerFromCharResponse(response, 36, 37);
       	int gpsYear = ByteResponseHelper.Companion.getIntegerFromCharResponse(response, 38, 39);

    	int gpsHour = ByteResponseHelper.Companion.getIntegerFromCharResponse(response, 2, 3);
       	int gpsMinute = ByteResponseHelper.Companion.getIntegerFromCharResponse(response, 4, 5);
       	int gpsSecond = ByteResponseHelper.Companion.getIntegerFromCharResponse(response, 6, 7);
       	
       	if (gpsDay >= 0 && gpsMonth >= 0 && gpsYear >=0 && gpsHour >= 0 && gpsMinute >= 0 && gpsSecond >= 0)
       	{
           	gpsYear += 2000;
           	gpsMonth -= 1;  // when month is assigned to Date, it is based on 0-11, decrement by 1 so it converts properly
       		eobrData.gpsPosition.setGpsTimestamp(new EobrTimestamp(gpsYear, gpsMonth, gpsDay, gpsHour, gpsMinute, gpsSecond));
       	}
       	else
       		eobrData.gpsPosition.setGpsTimestamp(null);

       	float latitudeDegrees = ByteResponseHelper.Companion.getFloatFromResponse(response, 12, 13);
       	float latitudeMinutes = ByteResponseHelper.Companion.getFloatFromResponse(response, 14, 20);
       	if (latitudeDegrees >= 0 && latitudeMinutes >= 0)
       		eobrData.gpsPosition.setLatitude(latitudeDegrees + (latitudeMinutes / MINUTE_PER_DEG));
       	else
       		eobrData.gpsPosition.setLatitude(0);
       	
       	eobrData.gpsPosition.setNorthSouthInd((char)response[21]);
       	
       	float longitudeDegrees = ByteResponseHelper.Companion.getFloatFromResponse(response, 22, 24);
       	float longitudeMinutes = ByteResponseHelper.Companion.getFloatFromResponse(response, 25, 31);
       	if (longitudeDegrees >= 0 && longitudeMinutes >= 0)
       		eobrData.gpsPosition.setLongitude(longitudeDegrees + (longitudeMinutes / MINUTE_PER_DEG));
       	else
       		eobrData.gpsPosition.setLongitude(0);
       	
       	eobrData.gpsPosition.setEastWestInd((char)response[32]);
       	eobrData.gpsPosition.setPosFixIndicator((char)response[33]);       	

       	eobrData.setEngineUseIndicator(response[46]);

       	eobrData.setOverallStatus(LittleEndianHelper.Companion.getInt(response, 51, 4));

       	eobrData.setActiveBusType(response[55]);
       	if (eobrData.getActiveBusType() == BUSTYPE_J1708)
       	{          	
           	eobrData.odometer = eobrData.new ODOMETER();
       		if ((eobrData.getOverallStatus() & STATUS_ODOMETER) == 0)
       		{
               	int totalVehicleDistance = LittleEndianHelper.Companion.getInt(response, 60, 4);

               	int tachometer = LittleEndianHelper.Companion.getInt(response, 56, 4);
               	if (totalVehicleDistance == DEFAULT_TRUCK_ODOMETER)
       				eobrData.odometer.setTotalTripDistance(ERROR_ODOMETER);
       			else
       				eobrData.odometer.setTotalTripDistance((float)totalVehicleDistance * J1708_MILES_PER_BIT);
       			
       			if (tachometer == DEFAULT_ENGINE_RPM)
       				eobrData.odometer.setTachometer(ERROR_ENGINE_RPM);
       			else
       	           	eobrData.odometer.setTachometer((float)tachometer * J1708_RPM_PER_BIT);       				
       		}
       		else
       		{
   				eobrData.odometer.setTotalTripDistance(ERROR_ODOMETER);
   				eobrData.odometer.setTachometer(ERROR_ENGINE_RPM);
       		}
       		       		
           	if ((eobrData.getOverallStatus() & STATUS_SPEEDOMETER) == 0)
           	{
               	int speedometer = LittleEndianHelper.Companion.getShort(response, 70, 2);

               	if (speedometer == DEFAULT_TRUCK_VELOCITY)
           			eobrData.setSpeedometer(ERROR_VELOCITY);
           		else
           			eobrData.setSpeedometer((float)speedometer * J1708_MPH_PER_BIT);
           	}
           	else
           	{
       			eobrData.setSpeedometer(ERROR_VELOCITY);           		
           	}
           	
           	if ((eobrData.getOverallStatus() & STATUS_FUEL) == 0)
           	{
               	int instFuelEconomy = LittleEndianHelper.Companion.getShort(response, 72, 2);
               	
               	if (instFuelEconomy == DEFAULT_FUEL_ECONOMY)
               		eobrData.setInstFuelEconomy(ERROR_FUEL);
               	else
               		eobrData.setInstFuelEconomy((float)instFuelEconomy * J1708_FUEL_ECON_PER_BIT);

               	int avgFuelEconomy = LittleEndianHelper.Companion.getShort(response, 74, 2);

               	if (avgFuelEconomy == DEFAULT_FUEL_ECONOMY)
               		eobrData.setAvgFuelEconomy(ERROR_FUEL);
               	else
               		eobrData.setAvgFuelEconomy((float)avgFuelEconomy * J1708_FUEL_ECON_PER_BIT);

               	int totalFuelUsed = LittleEndianHelper.Companion.getInt(response, 81, 4);

               	if (totalFuelUsed == DEFAULT_TOTAL_FUEL)
               		eobrData.setTotalFuelUsed(ERROR_FUEL);
               	else
               		eobrData.setTotalFuelUsed((float)totalFuelUsed * J1708_TOTAL_FUEL_PER_BIT);
           	}
           	else
           	{
           		eobrData.setInstFuelEconomy(ERROR_FUEL);
           		eobrData.setAvgFuelEconomy(ERROR_FUEL);
           		eobrData.setTotalFuelUsed(ERROR_FUEL);
           	}
           	
           	if ((eobrData.getOverallStatus() & STATUS_CRUISE_CONTROL) == 0)
           	{
               	char cruiseControlStatus = (char)response[76];

               	if (cruiseControlStatus == DEFAULT_CRUISE_CONTROL_STATUS)
           			eobrData.setCruiseControlStatus(ERROR_CRUISE_CONTROL);
           		else
           			eobrData.setCruiseControlStatus(cruiseControlStatus);
           	}
           	else
           	{
           		eobrData.setCruiseControlStatus(ERROR_CRUISE_CONTROL);
           	}

           	if ((eobrData.getOverallStatus() & STATUS_TRANSMISSION) == 0)
           	{
               	eobrData.transmissionRangeAttained = eobrData.new TRANSMISSION_RANGE();
               	eobrData.transmissionRangeSelected = eobrData.new TRANSMISSION_RANGE();

               	char firstChar = (char)response[79];
           		char secondChar = (char)response[80];           		
           		if (firstChar + secondChar == DEFAULT_TRANSMISSION)
           		{
           			eobrData.transmissionRangeAttained.setFirstChar(ERROR_TRANSMISSION);
           			eobrData.transmissionRangeAttained.setSecondChar(ERROR_TRANSMISSION);
           		}
           		else
           		{
           			eobrData.transmissionRangeAttained.setFirstChar(firstChar);
           			eobrData.transmissionRangeAttained.setSecondChar(secondChar);
           		}
           		
           		firstChar = (char)response[77];
           		secondChar = (char)response[78];
           		if (firstChar + secondChar == DEFAULT_TRANSMISSION)
           		{
           			eobrData.transmissionRangeSelected.setFirstChar(ERROR_TRANSMISSION);
           			eobrData.transmissionRangeSelected.setSecondChar(ERROR_TRANSMISSION);
           		}
           		else
           		{
           			eobrData.transmissionRangeSelected.setFirstChar(firstChar);
           			eobrData.transmissionRangeSelected.setSecondChar(secondChar);
           		}
           	}
           	else
           	{
       			eobrData.transmissionRangeAttained.setFirstChar(ERROR_TRANSMISSION);
       			eobrData.transmissionRangeAttained.setSecondChar(ERROR_TRANSMISSION);

       			eobrData.transmissionRangeSelected.setFirstChar(ERROR_TRANSMISSION);
       			eobrData.transmissionRangeSelected.setSecondChar(ERROR_TRANSMISSION);
           	}
           	
           	if ((eobrData.getOverallStatus() & STATUS_BRAKE) == 0)
           	{
               	int brakePressure = response[85];
           		if (brakePressure == DEFAULT_BRAKE_PRESSURE)
           		{
           			eobrData.setBrakePressure(ERROR_BRAKE);           			
           		}
           		else
           		{
           			eobrData.setBrakePressure((float)brakePressure * J1708_BRAKE_PRESSURE_PER_BIT);
           		}
           	}
           	else
           	{
       			eobrData.setBrakePressure(ERROR_BRAKE);           			           		
           	}
       	}
       	else if (eobrData.getActiveBusType() == BUSTYPE_J1939)
       	{
           	eobrData.odometer = eobrData.new ODOMETER();
       		if ((eobrData.getOverallStatus() & STATUS_ODOMETER) == 0)
       		{
               	int totalTripDistance = LittleEndianHelper.Companion.getInt(response, 60, 4);

               	int tachometer = LittleEndianHelper.Companion.getInt(response, 56, 4);

       			if (totalTripDistance == DEFAULT_TRUCK_ODOMETER)
       				eobrData.odometer.setTotalTripDistance(ERROR_ODOMETER);
       			else
       				eobrData.odometer.setTotalTripDistance((float)totalTripDistance * DISTANCE_KM_PER_BIT * Constants.MILES_PER_KILOMETER);

       			if (tachometer == DEFAULT_ENGINE_RPM)
       				eobrData.odometer.setTachometer(ERROR_ENGINE_RPM);
       			else
       	           	eobrData.odometer.setTachometer((float)tachometer * J1939_RPM_PER_BIT);       				
       		}
       		else
       		{
   				eobrData.odometer.setTotalTripDistance(ERROR_ODOMETER);
   				eobrData.odometer.setTachometer(ERROR_ENGINE_RPM);
       		}

           	if ((eobrData.getOverallStatus() & STATUS_SPEEDOMETER) == 0)
           	{
               	int speedometer = LittleEndianHelper.Companion.getShort(response, 72, 2);

               	if (speedometer == DEFAULT_TRUCK_VELOCITY)
           			eobrData.setSpeedometer(ERROR_VELOCITY);
           		else
           			eobrData.setSpeedometer((float)speedometer * WHEEL_SPEED_KPH_PER_BIT * Constants.KPH_TO_MPH);
           	}
           	else
           	{
       			eobrData.setSpeedometer(ERROR_VELOCITY);           		
           	}

           	if ((eobrData.getOverallStatus() & STATUS_FUEL) == 0)
           	{
               	int instFuelEconomy = LittleEndianHelper.Companion.getShort(response, 74, 2);
               	
               	if (instFuelEconomy == DEFAULT_FUEL_ECONOMY)
               		eobrData.setInstFuelEconomy(ERROR_FUEL);
               	else
               		eobrData.setInstFuelEconomy((float)instFuelEconomy * J1939_FUEL_ECONOMY_KM_PER_L_PER_BIT * KPL_TO_MPG);

               	int avgFuelEconomy = LittleEndianHelper.Companion.getShort(response, 76, 2);

               	if (avgFuelEconomy == DEFAULT_FUEL_ECONOMY)
               		eobrData.setAvgFuelEconomy(ERROR_FUEL);
               	else
               		eobrData.setAvgFuelEconomy((float)avgFuelEconomy * J1939_FUEL_ECONOMY_KM_PER_L_PER_BIT * KPL_TO_MPG);

               	int totalFuelUsed = LittleEndianHelper.Companion.getInt(response, 83, 4);

               	if (totalFuelUsed == DEFAULT_TOTAL_FUEL)
               		eobrData.setTotalFuelUsed(ERROR_FUEL);
               	else
               		eobrData.setTotalFuelUsed((float)totalFuelUsed * J1939_TOTAL_FUEL_L_PER_BIT * GALLONS_PER_LITER);
           	}
           	else
           	{
           		eobrData.setInstFuelEconomy(ERROR_FUEL);
           		eobrData.setAvgFuelEconomy(ERROR_FUEL);
           		eobrData.setTotalFuelUsed(ERROR_FUEL);
           	}

           	if ((eobrData.getOverallStatus() & STATUS_CRUISE_CONTROL) == 0)
           	{
               	char cruiseControlStatus = (char)response[78];

               	if (cruiseControlStatus == DEFAULT_CRUISE_CONTROL_STATUS)
           			eobrData.setCruiseControlStatus(ERROR_CRUISE_CONTROL);
           		else
           			eobrData.setCruiseControlStatus(cruiseControlStatus);
           	}
           	else
           	{
           		eobrData.setCruiseControlStatus(ERROR_CRUISE_CONTROL);
           	}

           	if ((eobrData.getOverallStatus() & STATUS_TRANSMISSION) == 0)
           	{
               	eobrData.transmissionRangeAttained = eobrData.new TRANSMISSION_RANGE();
               	eobrData.transmissionRangeSelected = eobrData.new TRANSMISSION_RANGE();

               	char firstChar = (char)response[81];
           		char secondChar = (char)response[82];           		
           		if (firstChar + secondChar == DEFAULT_TRANSMISSION)
           		{
           			eobrData.transmissionRangeAttained.setFirstChar(ERROR_TRANSMISSION);
           			eobrData.transmissionRangeAttained.setSecondChar(ERROR_TRANSMISSION);
           		}
           		else
           		{
           			eobrData.transmissionRangeAttained.setFirstChar(firstChar);
           			eobrData.transmissionRangeAttained.setSecondChar(secondChar);
           		}
           		
           		firstChar = (char)response[79];
           		secondChar = (char)response[80];
           		if (firstChar + secondChar == DEFAULT_TRANSMISSION)
           		{
           			eobrData.transmissionRangeSelected.setFirstChar(ERROR_TRANSMISSION);
           			eobrData.transmissionRangeSelected.setSecondChar(ERROR_TRANSMISSION);
           		}
           		else
           		{
           			eobrData.transmissionRangeSelected.setFirstChar(firstChar);
           			eobrData.transmissionRangeSelected.setSecondChar(secondChar);
           		}
           	}
           	else
           	{
       			eobrData.transmissionRangeAttained.setFirstChar(ERROR_TRANSMISSION);
       			eobrData.transmissionRangeAttained.setSecondChar(ERROR_TRANSMISSION);

       			eobrData.transmissionRangeSelected.setFirstChar(ERROR_TRANSMISSION);
       			eobrData.transmissionRangeSelected.setSecondChar(ERROR_TRANSMISSION);
           	}
 
           	if ((eobrData.getOverallStatus() & STATUS_BRAKE) == 0)
           	{
               	int brakePressure = response[87];
           		if (brakePressure == DEFAULT_BRAKE_PRESSURE)
           		{
           			eobrData.setBrakePressure(ERROR_BRAKE);           			
           		}
           		else
           		{
           			eobrData.setBrakePressure((float)brakePressure * J1939_BRAKE_PRESSURE_kPA_PER_BIT * PSI_PER_kPA);
           		}
           	}
           	else
           	{
       			eobrData.setBrakePressure(ERROR_BRAKE);           			           		
           	}
       	}
       	else
       	{
           	eobrData.odometer = eobrData.new ODOMETER();
           	eobrData.odometer.setTotalTripDistance(DEFAULT_TOTAL_DIS);
           	eobrData.odometer.setTachometer(ERROR_ENGINE_RPM);
  			eobrData.setSpeedometer(ERROR_VELOCITY);
       		eobrData.setInstFuelEconomy(ERROR_FUEL);
       		eobrData.setAvgFuelEconomy(ERROR_FUEL);
       		eobrData.setTotalFuelUsed(ERROR_FUEL);
  			eobrData.setCruiseControlStatus(ERROR_CRUISE_CONTROL);
           	eobrData.transmissionRangeAttained = eobrData.new TRANSMISSION_RANGE();
      		eobrData.transmissionRangeAttained.setFirstChar(ERROR_TRANSMISSION);
  			eobrData.transmissionRangeAttained.setSecondChar(ERROR_TRANSMISSION);
            eobrData.transmissionRangeSelected = eobrData.new TRANSMISSION_RANGE();
   			eobrData.transmissionRangeSelected.setFirstChar(ERROR_TRANSMISSION);
   			eobrData.transmissionRangeSelected.setSecondChar(ERROR_TRANSMISSION);
   			eobrData.setBrakePressure(ERROR_BRAKE);           			
       	}
       	
       	int timestampYear = response[40];
       	
       	if (timestampYear == 0)
       		eobrData.setTimestamp(null);
       	else
       	{
	       	timestampYear += 2000;
	       	int timestampMonth = response[41];
	       	timestampMonth -= 1; // when month is assigned to Date, it is based on 0-11, decrement by 1 so it converts properly
	       	int timestampDay = response[42];
	       	int timestampHour = response[43];
	       	int timestampMinute = response[44];
	       	int timestampSecond = response[45];       	
	       	eobrData.setTimestamp(new EobrTimestamp(timestampYear, timestampMonth, timestampDay, timestampHour, timestampMinute, timestampSecond));             	       	
       	}

       	// diagnostic data is after the bus data and bus data is different
       	// size for 1708 and 1939 bus
       	if (eobrData.getActiveBusType() == BUSTYPE_J1708)
       	{
       		eobrData.setDiagnosticData_dwAddress(LittleEndianHelper.Companion.getInt(response, 86, 4));
       		eobrData.setDiagnosticData_bReserved(response[90]);
       		eobrData.setDiagnosticData_bReserved1(response[91]);
       	}
       	else if (eobrData.getActiveBusType() == BUSTYPE_J1939)
       	{
       		eobrData.setDiagnosticData_dwAddress(LittleEndianHelper.Companion.getInt(response, 88, 4));
       		eobrData.setDiagnosticData_bReserved(response[92]);
       		eobrData.setDiagnosticData_bReserved1(response[93]);
       	}
       	else
       	{
       		eobrData.setDiagnosticData_dwAddress(0);
       		eobrData.setDiagnosticData_bReserved((byte)0);
       		eobrData.setDiagnosticData_bReserved1((byte)0);
       	}
    }

//    private int CheckEOBRReturnStatus(byte[] response)
//    {
//    	int statusCode = EobrReturnCode.S_SUCCESS;
//    	
//    	if (response[41] == 0xFF)
//    	{
//    		if (response[42] == 0x01)
//    			statusCode = EobrReturnCode.S_NO_DATA;
//    		else if (response[42] == 0x02)
//    			statusCode = EobrReturnCode.S_NO_HISTORICAL_DATA;
//    	}
//    	
//    	return statusCode;
//    }

    /// <summary>
    /// Convert EOBR data into object of StatusRecord. 
    /// </summary>
    /// <param name="eobrData">an object of StatusRecord</param>
    /// <param name="convertData">EOBR_DATA data structure</param>
    private void CopyEobrDataToStatusRecord(StatusRecord eobrData, Eobr_Data convertData)
    {
        if (convertData.engineUseIndicator > 0)
            eobrData.setIsEngineOn(true);
        else
            eobrData.setIsEngineOn(false);

        eobrData.setRecordId(convertData.recordID);
        eobrData.setOverallStatus(convertData.overallStatus);
        eobrData.setActiveBusType(convertData.activeBusType);
        eobrData.setOdometerReading(convertData.odometer.totalTripDistance);
        eobrData.setTachometer(convertData.odometer.tachometer);
        eobrData.setSpeedometerReading(convertData.speedometer);
        eobrData.setInstantFuelEconomy(convertData.instFuelEconomy);
        eobrData.setAverageFuelEconomy(convertData.avgFuelEconomy);

        if (convertData.cruiseControlStatus == (char)0 ||
        	convertData.cruiseControlStatus == (char)1)
        {
        	eobrData.setCruiseControlStatus((int)convertData.cruiseControlStatus);
        }

        eobrData.setTransmissionRangeAttained(eobrData.new TransmissionRange(convertData.transmissionRangeAttained.firstChar, 
                                                                   convertData.transmissionRangeAttained.secondChar));
        eobrData.setTransmissionRangeSelected(eobrData.new TransmissionRange(convertData.transmissionRangeSelected.firstChar,
                                                                   convertData.transmissionRangeSelected.secondChar));

        eobrData.setTotalFuelUsed(convertData.totalFuelUsed);
        eobrData.setBrakePressure(convertData.brakePressure);
        eobrData.setDiagnosticData_dwAddress(convertData.diagnosticData_dwAddress);
        eobrData.setDiagnosticData_bReserved(convertData.diagnosticData_bReserved);

        //We don't want to throw an exception if we don't have to
        eobrData.setTimestampUtc(null);
        if (convertData.timestamp != null &&
        	(convertData.timestamp.year > 0) &&
        	(convertData.timestamp.month > -1) &&
        	(convertData.timestamp.day > 0))
        {
            try
            {
            	Calendar cal = Calendar.getInstance();
                cal.setTimeZone(TimeZone.getTimeZone("GMT"));
            	
            	cal.set(Calendar.YEAR, convertData.timestamp.getYear());
            	cal.set(Calendar.MONTH, convertData.timestamp.getMonth());
            	cal.set(Calendar.DAY_OF_MONTH, convertData.timestamp.getDay());
            	cal.set(Calendar.HOUR_OF_DAY, convertData.timestamp.getHour());
            	cal.set(Calendar.MINUTE, convertData.timestamp.getMinute());
            	cal.set(Calendar.SECOND, convertData.timestamp.getSecond());
            	cal.set(Calendar.MILLISECOND, 0);
            	
                eobrData.setTimestampUtc(cal.getTime());
            }
            catch(Exception ex)  //if DateTime is not valid, an exception will be thrown.
            {
                eobrData.setTimestampUtc(null);
            }
        }

        //We don't want to throw an exception if we don't have to
        eobrData.setGpsTimestampUtc(null);
        if (convertData.gpsPosition.gpsTimestamp != null &&
        	(convertData.gpsPosition.gpsTimestamp.year > 0) &&
        	(convertData.gpsPosition.gpsTimestamp.month > -1) &&
        	(convertData.gpsPosition.gpsTimestamp.day > 0))
        {
            try
            {
            	Calendar cal = Calendar.getInstance();
            	cal.setTimeZone(TimeZone.getTimeZone("GMT"));
            	
            	cal.set(Calendar.YEAR, convertData.gpsPosition.gpsTimestamp.getYear());
            	cal.set(Calendar.MONTH, convertData.gpsPosition.gpsTimestamp.getMonth());
            	cal.set(Calendar.DAY_OF_MONTH, convertData.gpsPosition.gpsTimestamp.getDay());
            	cal.set(Calendar.HOUR_OF_DAY, convertData.gpsPosition.gpsTimestamp.getHour());
            	cal.set(Calendar.MINUTE, convertData.gpsPosition.gpsTimestamp.getMinute());
            	cal.set(Calendar.SECOND, convertData.gpsPosition.gpsTimestamp.getSecond());
            	cal.set(Calendar.MILLISECOND, 0);
            	
                eobrData.setGpsTimestampUtc(cal.getTime());
            }
            catch(Exception ex)
            {
                eobrData.setGpsTimestampUtc(null);
            }
        }

        eobrData.setGpsLatitude(convertData.gpsPosition.latitude);
        eobrData.setNorthSouthInd(convertData.gpsPosition.northSouthInd);
        eobrData.setGpsLongitude(convertData.gpsPosition.longitude);
        eobrData.setEastWestInd(convertData.gpsPosition.eastWestInd);
        eobrData.setPosFixIndicator(convertData.gpsPosition.posFixIndicator);
    }

    
    private boolean FilterEobrByPasskey(String passkey, String passkeyRetrieved)
    {
    	String allDevices = "All Devices";
    	
    	if (passkey.equals(allDevices))
    		return true;
    	else if (passkey.equals(passkeyRetrieved))
    		return true;
    	else
    		return false;
    }
   
	public void ClearAllEobrData() throws EobrException {
		int rc = ClearAllRecordData(0x01);
		rc = ClearAllRecordData(0x04);
		rc = ClearAllRecordData(0x08);
		
	 EobrException eobrEx =	EobrException.getEobrExceptionFromEobrReturnCodeValue(rc);
	 if(eobrEx != null)
		 throw eobrEx; 
	}
	
	public EobrResponse<DriveData> GetDriveData(DriveDataTypeEnum typeEnum, long timeCode, short timeStep, short maxUncertainty)
	{
		return new EobrResponse<DriveData>(EobrReturnCode.S_FUNC_NOT_IMPLEMENTED);
	}
	
	public int SetReferenceTimestamps(EobrReferenceTimestamps timestamps)
	{
		return EobrReturnCode.S_FUNC_NOT_IMPLEMENTED;
	}

	public boolean IsGetDriveDataSupported()
	{
		return false;
	}

	public boolean IsGetEventDataEventMaskSupported() { return false; }
	
	public int SetIsEldMandate(boolean isEldMandate)
	{
		return EobrReturnCode.S_FUNC_NOT_IMPLEMENTED;
	}

	public int SetDisableReadEldVin(boolean isEldReadingVin) {
		return EobrReturnCode.S_FUNC_NOT_IMPLEMENTED;
	}

	public Bundle GetDisableReadEldVin()
	{
		Bundle bundle = null;
		return bundle;
	}

	@Override
	public EobrResponse<StatusBuffer> GetStatusBuffer() {
		return new EobrResponse<>(EobrReturnCode.S_FUNC_NOT_IMPLEMENTED);
	}

	public boolean sendEobrFirmwareBlockPacket(Eobr_FW_Block_Packet packet){
			throw new UnsupportedOperationException("This is not implemented for Gen1... try gen2 out, I hear it is cool!");
	}

	public Bundle GetEobrHardware() {
		Bundle b = new Bundle();
		b.putInt(Constants.RETURNCODE,EobrReturnCode.S_FUNC_NOT_IMPLEMENTED);
		return b;
	}

	@Override
	public int GetDriverEvent(EventRecord eventData, long startTimeCode, long endTimeCode, int eventMask, boolean includeEventsWithoutDriverId) {
		return EobrReturnCode.S_FUNC_NOT_IMPLEMENTED;
	}

	@Override
	public Bundle GetDriverCount(long startTimeCode, long endTimeCode)
	{
		Bundle bundle = null;
		return bundle;
	}
}
