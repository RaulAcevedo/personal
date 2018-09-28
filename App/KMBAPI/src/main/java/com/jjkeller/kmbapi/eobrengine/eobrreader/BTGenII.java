package com.jjkeller.kmbapi.eobrengine.eobrreader;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.jjkeller.kmbapi.R;
import com.jjkeller.kmbapi.configuration.FirmwareUpdate;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.eobrengine.CalcCRC;
import com.jjkeller.kmbapi.eobrengine.CommThreadManager;
import com.jjkeller.kmbapi.eobrengine.EUCMDType;
import com.jjkeller.kmbapi.eobrengine.Enums.FirmwareUpgradeTypeEnum;
import com.jjkeller.kmbapi.eobrengine.EobrCommunications;
import com.jjkeller.kmbapi.eobrengine.LittleEndianHelper;
import com.jjkeller.kmbapi.eobrengine.ProcessDataHelper;
import com.jjkeller.kmbapi.eobrengine.eobrdatatypes.EobrBytePacket;
import com.jjkeller.kmbapi.eobrengine.eobrdatatypes.EobrIntegerPacket;
import com.jjkeller.kmbapi.eobrengine.eobrdatatypes.EobrJbusDiagDataDtcInfo;
import com.jjkeller.kmbapi.eobrengine.eobrdatatypes.EobrJbusDiagDataPacket;
import com.jjkeller.kmbapi.eobrengine.eobrdatatypes.EobrShortPacket;
import com.jjkeller.kmbapi.eobrengine.eobrdatatypes.EobrStringPacket;
import com.jjkeller.kmbapi.eobrengine.eobrdatatypes.Eobr_Comm_Response_Packet;
import com.jjkeller.kmbapi.eobrengine.eobrdatatypes.Eobr_Data_GenII;
import com.jjkeller.kmbapi.eobrengine.eobrdatatypes.Eobr_Data_Packet;
import com.jjkeller.kmbapi.eobrengine.eobrdatatypes.Eobr_Data_Packet_GenII;
import com.jjkeller.kmbapi.eobrengine.eobrdatatypes.Eobr_Driver_Event_Packet;
import com.jjkeller.kmbapi.eobrengine.eobrdatatypes.Eobr_FW_Block_Packet;
import com.jjkeller.kmbapi.eobrengine.eobrdatatypes.Eobr_Packet;
import com.jjkeller.kmbapi.eobrengine.eobrreader.exceptions.EobrException;
import com.jjkeller.kmbapi.eobrengine.ReceivedPacketHelper.VerifyRxPacketResponseByte;
import com.jjkeller.kmbapi.firmwareupgrade.FirmwareUpdateBroadcaster;
import com.jjkeller.kmbapi.kmbeobr.Constants;
import com.jjkeller.kmbapi.kmbeobr.DTCInformation;
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
import com.jjkeller.kmbapi.realtime.MalfunctionManager;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author jld5296
 *
 */
public class BTGenII extends AndroidBTBase {
	protected static final int EOBR_FW_BLOCK_CODE_SIZE_SMALL = 48;
	protected static final int EOBR_FW_BLOCK_CODE_SIZE_LARGE = 250;	
		
	protected static final byte[] EUCMD_BITMASK_DEFAULT = new byte[]{
		    0x7f, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xc8, (byte)0xff, (byte)0xc0,
		    (byte)0xfc, 0x00, 0x00, 0x00, 0x00, 0x00, (byte)0xfc, 0x00,
		    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
		    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01,
	};

	protected byte[] currentEucmdBitmask = new byte[32];
	
	//bitmask values for enable/disable reading vehicle vin
	private static final int FLAG_CUSTPARM10_ReadVehicleVin = 1<<0;   // 0x00000001
    private static final int FLAG_CUSTPARM10_SampleBit2 = 1<<1;       // 0x00000010
    private static final int FLAG_CUSTPARM10_SampleBit3 = 1<<2;       // 0x00000100

	private boolean isIgnitionTripReportSupported = false;
	private boolean isEldMandateMode = false;

	public int searchForEobrDevices(List<BluetoothDevice> devices, String companyPasskey, String serialNumber)
	{
    	int retValue = EobrReturnCode.S_SUCCESS;

   		_companyPasskey = companyPasskey;
   		retValue = this.SearchBluetoothThread(devices, serialNumber);
    	
    	return retValue;
	}

	public int searchForEobrDevice(String companyPasskey, BluetoothDevice btDevice)
	{
		int retValue = EobrReturnCode.S_SUCCESS;
		
		_companyPasskey = companyPasskey;
		if (_btAdapter == null) 
			_btAdapter = BluetoothAdapter.getDefaultAdapter();

		// Get a BluetoothSocket for a connection with the given Bluetooth address
		try
		{
			ValidatedDevice device = checkValidDevice(btDevice);
			if (device != null)
			{
				AvailableBtEobrSearch deviceSearch = this.AddAvailableBtEobr(device.getDeviceName(), btDevice.getAddress(), device.getDeviceCrc(), 2);
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
    		for (BluetoothDevice btDevice : devices)
    		{
    			ValidatedDevice eobrDevice = checkValidDevice(btDevice);
				if (eobrDevice != null)
				{
					this.AddAvailableBtEobr(eobrDevice.getDeviceName(), btDevice.getAddress(), eobrDevice.getDeviceCrc(), 2);
				}
    		}    		
    	}		    	
    	else
    	{
    		retValue = EobrReturnCode.S_DEV_NOT_CONNECTED;
    	}
    	
    	return retValue;
    }

	private ValidatedDevice checkValidDevice(BluetoothDevice device)
	{
		ValidatedDevice result = null;

		_localSocket = OpenBluetoothSocket(getCommThreadManager(), device);
		if (_localSocket != null)
		{
			_isSocketConnected = true;
			
			try {
				Thread.sleep(500);

				Bundle bundle = this.GetEobrSerialNumber();
				if (bundle.getInt(Constants.RETURNCODE) == EobrReturnCode.S_SUCCESS) {
					String serialNumber = bundle.getString(Constants.RETURNVALUE);
					short crcValue = (short)CalcCRC.Calculate(serialNumber, serialNumber.length());
					this.SetHandshakeCrc(crcValue);

					bundle.clear();
					bundle = this.GetUnitId();

					if (bundle.getInt(Constants.RETURNCODE) == EobrReturnCode.S_SUCCESS) {
						String deviceName = bundle.getString(Constants.RETURNVALUE);
						result = new ValidatedDevice(deviceName, crcValue);
					}
					this.ClearHandshakeCrc();
				}

				Thread.sleep(500);
			} catch (InterruptedException e) {
				Log.e("checkValidDevice", "Error getting the device name");
			}
			
			closeLocalSocket();
			try {
				Thread.sleep(100); // Give both sides time to clean up resources
			} catch (InterruptedException e) {
				Log.v("UnhandledException", "", e);
			}
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
            Method m = device.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
            socket = (BluetoothSocket)m.invoke(device, Integer.valueOf(1));

			Log.v("Comm", String.format("About to connect socket to address: %s name: %s", device.getAddress(), device.getName()));
                	
        	// Open socket - Note:  This is a blocking call
    		if (!ctMgr.ConnectToBTSocket(socket))
    		{
   				socket = null;
    		}
			
			Log.v("Comm", "EobrBTHelper.OpenBluetoothSocket AfterConnectToBTSocket");
                	
        } catch (IOException e) {
            socket = null;
		} catch (NoSuchMethodException e) {
			socket = null;
		} catch (IllegalAccessException e) {
			socket = null;
		} catch (NullPointerException e) {
			// Reflection method does not work for Samsung devices returns NullPointerException
			socket = this.OpenSocketWithoutReflection(ctMgr, device);
		} catch (InvocationTargetException e) {
			socket = this.OpenSocketWithoutReflection(ctMgr, device);
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

            Method m = device.getClass().getMethod("createRfcommSocket", new Class[] { int.class});
            socket = (BluetoothSocket)m.invoke(device, Integer.valueOf(1));
    		
    		btAdapter.cancelDiscovery();

   			Log.v("Comm", String.format("About to connect socket to address: %s name: %s", device.getAddress(), device.getName()));

           	// Open socket - Note:  This is a blocking call
       		if (!ctMgr.ConnectToBTSocket(socket))
       		{
  				socket = null;
       		}

   			Log.v("Comm", "EobrBTHelper.OpenBluetoothSocket AfterConnectToBTSocket");
                    	
         } catch (IOException e) {
   	 		socket = null;
        }  catch (NoSuchMethodException e) {
        	socket = null;
        } catch (IllegalAccessException e) {
        	socket = null;
		} catch (NullPointerException e) {
			// Reflection method does not work for Samsung devices returns NullPointerException
			socket = this.OpenSocketWithoutReflection(ctMgr, device);
		} catch (InvocationTargetException e) {
			socket = this.OpenSocketWithoutReflection(ctMgr, device);
		} 

		return socket;
    }

	protected BluetoothSocket OpenSocketWithoutReflection(CommThreadManager ctMgr, BluetoothDevice device)
    {
		BluetoothSocket socket = null;

		if (device != null)
		{
			// Get a BluetoothSocket for a connection with the given Bluetooth address
			try {
				socket = device.createRfcommSocketToServiceRecord(MY_UUID);
				Log.v("Comm", String.format("About to connect socket to address: %s name: %s", device.getAddress(), device.getName()));

				// Open socket - Note:  This is a blocking call
				if (!ctMgr.ConnectToBTSocket(socket))
				{
					socket = null;
				}
    			
				Log.v("Comm", "EobrBTHelper.OpenBluetoothSocket AfterConnectToBTSocket");                    
			} catch (IOException e) {   
				socket = null;
			}
		} 

		return socket;
    }

    public Bundle GetCompanyPasskey()
    {
    	Bundle answer = new Bundle();
    	answer.putInt(Constants.RETURNCODE, EobrReturnCode.S_FUNC_NOT_IMPLEMENTED);
    	
       	return answer;
    }

    public int SetCompanyPasskey(String passkey)
    {
    	return EobrReturnCode.S_FUNC_NOT_IMPLEMENTED;
    }
    
    public Bundle GetReferenceTimestamp()
    {
    	int cmdId = EUCMDType.EUCMD_GET_REF_TIMESTAMP;
    	Bundle answer = new Bundle();
    	long eobrRefTime = 0;
    	long eventRefTime = 0;
    	long histRefTime = 0;
    	long tripRefTime = 0;
    	long dtcRefTime = 0;
    	int errorCode = EobrReturnCode.S_SUCCESS;
    	
		Eobr_Packet eobrPacket = new Eobr_Packet();
		eobrPacket.setCmd((byte)cmdId);
		eobrPacket.setCrc(this.GetActiveDeviceCrc());
		eobrPacket.setLen((byte)0);

		Eobr_Comm_Response_Packet commResponsePacket = new Eobr_Comm_Response_Packet();
		errorCode = EobrCommunications.Companion.validateWithData(_threadMgr, eobrPacket, cmdId,
				this.GetActiveDeviceCrc(), VerifyRxPacketResponseByte.EobrStringPacket,
				commResponsePacket, false, socketVerifier);
    	
    	if (errorCode == EobrReturnCode.S_SUCCESS) {
    		EobrStringPacket eobrStringPacket = EobrStringPacket.build(commResponsePacket.getResponse());
    		
            // Byte pattern sent by EOBR to indicate GetRefTimestamp failure
            // Error happen, size is 1
    		if (eobrStringPacket.getSize() == 1) {
    			errorCode = EobrReturnCode.S_DEV_INTERNAL_ERROR;
    		}
    		else {
    			eobrRefTime = LittleEndianHelper.Companion.getLong(eobrStringPacket.getStringVal(), 0, 8);
                eventRefTime = LittleEndianHelper.Companion.getLong(eobrStringPacket.getStringVal(), 8, 8);
                histRefTime = LittleEndianHelper.Companion.getLong(eobrStringPacket.getStringVal(), 16, 8);
                tripRefTime = LittleEndianHelper.Companion.getLong(eobrStringPacket.getStringVal(), 24, 8);
                dtcRefTime = LittleEndianHelper.Companion.getLong(eobrStringPacket.getStringVal(), 32, 8);
    			
            	answer.putLong(Constants.RETURNVALUE, eobrRefTime);
    			answer.putLong(Constants.EOBRREFTIME, eobrRefTime);
    			answer.putLong(Constants.EVENTREFTIME, eventRefTime);
    			answer.putLong(Constants.HISTREFTIME, histRefTime);
    			answer.putLong(Constants.TRIPREFTIME, tripRefTime);
    			answer.putLong(Constants.DTCREFTIME, dtcRefTime);
    		}
    	}    	
    	
		answer.putInt(Constants.RETURNCODE, errorCode);
    	return answer;
    }

	@Override
	public Bundle GetDistHours(long timecode) {
		int cmdId = EUCMDType.EUCMD_GET_DIST_HRS;
		Bundle answer = new Bundle();
//		char status = 0;
		int tripDist = 0;
		int tripRunTime = 0;
		int errorCode = EobrReturnCode.S_SUCCESS;

		ByteBuffer payload = ByteBuffer.allocate(8);
		payload.order(ByteOrder.LITTLE_ENDIAN);
		payload.putLong(timecode);

		Eobr_Packet eobrPacket = new Eobr_Packet();
		eobrPacket.setCmd((byte)cmdId);
		eobrPacket.setCrc(this.GetActiveDeviceCrc());
		eobrPacket.setLen((byte)payload.capacity());
		eobrPacket.setData(payload.array());

		Eobr_Comm_Response_Packet commResponsePacket = new Eobr_Comm_Response_Packet();
		errorCode = EobrCommunications.Companion.validateWithData(_threadMgr, eobrPacket, cmdId,
				this.GetActiveDeviceCrc(), VerifyRxPacketResponseByte.EobrStringPacket,
				commResponsePacket, false, socketVerifier);

		if (errorCode == EobrReturnCode.S_SUCCESS) {
			EobrStringPacket eobrStringPacket = EobrStringPacket.build(commResponsePacket.getResponse());

			if (eobrStringPacket.getSize() == 1) {
				errorCode = EobrReturnCode.S_DEV_INTERNAL_ERROR;
			}
			else {
				float rawTripDist = LittleEndianHelper.Companion.getFloat(
						eobrStringPacket.getStringVal(), 1,4);
				tripDist = (int)(Math.round(((rawTripDist / 10f) * Constants.MILES_PER_KILOMETER) * 10f) / 10f);

				float rawTripRunTime = LittleEndianHelper.Companion.getFloat(
						eobrStringPacket.getStringVal(), 5,4);
				tripRunTime = (int)Math.round(rawTripRunTime / 60.0);
				answer.putInt(Constants.TRIPDISTANCE, tripDist);
				answer.putInt(Constants.TRIPRUNTIME, tripRunTime);
			}
		}

		answer.putInt(Constants.RETURNCODE, errorCode);
		return answer;
	}

    public int GetEobrData(StatusRecord statusRec, StatusRecordQueryMethodEnum queryMethod, int recordId, Date timeCode, StatusRecordMotionOptionEnum motionOption, boolean resetReferenceTimestampToCurrent)
    {
    	int cmdId = EUCMDType.EUCMD_GET_EOBR_DATA;
    	int retVal;
		Eobr_Data_GenII eobrData = new Eobr_Data_GenII();

    	Eobr_Data_Packet_GenII eobrDataPacket = new Eobr_Data_Packet_GenII();
    	eobrDataPacket.setCmd((byte)cmdId);
    	eobrDataPacket.setLen((byte)15);  //sizeof(EOBR_GET_DATA_PACKET_GENII) - 4  //minus bCmd, wCrc, bLen
    	eobrDataPacket.setCrc(this.GetActiveDeviceCrc());
    	if(queryMethod.getValue() == StatusRecordQueryMethodEnum.RECORDID)
    		eobrDataPacket.setMethod((byte)RECORD_ID_TYPE);
    	else
    		eobrDataPacket.setMethod((byte)TIMESTAMP_TYPE);
    	eobrDataPacket.setRecordId(recordId);	// eobrDataPacket.setRecordId(NEWEST_RECORD_ID);
    	
    	// time code only provided for historical data or next motion change data
    	if (timeCode != null)
    	{
    		eobrDataPacket.setTimecode(timeCode.getTime());
    	}
    	
    	if (motionOption.getValue() == StatusRecordMotionOptionEnum.NEXTRECORD)
    		eobrDataPacket.setMotionOption((byte)SPECIFIED_TIMESTAMP);
    	else
    		eobrDataPacket.setMotionOption((byte)NEXT_MOTION);
    	
   		eobrDataPacket.setRefTimestampOption((byte)(resetReferenceTimestampToCurrent?1:0));
    	
   		Eobr_Comm_Response_Packet commResponsePacket = new Eobr_Comm_Response_Packet();
		retVal = EobrCommunications.Companion.validate(_threadMgr, eobrDataPacket, cmdId,
				this.GetActiveDeviceCrc(), commResponsePacket, socketVerifier);
   		
   		if (retVal == EobrReturnCode.S_SUCCESS) {
   		    ProcessDataHelper.Companion.recorderDataToUserData(commResponsePacket.getResponse(), eobrData);
   		    ProcessDataHelper.Companion.copyEobrDataToStatusRecord(statusRec, eobrData);
   		}
   		
    	return retVal;
    }
    
	public int ClearAllRecordData(int clearFlags)
	{
    	int errorCode;
    	int cmdId = EUCMDType.EUCMD_CLEAR_RECORD_DATA;
    	int dataSize = 0;

    	Eobr_Packet eobrPacket = new Eobr_Packet();
		eobrPacket.setCmd((byte)cmdId);
		eobrPacket.setCrc(this.GetActiveDeviceCrc());
		eobrPacket.setLen((byte)dataSize);

		byte[] packetData = new byte[dataSize];
    	
    	eobrPacket.setData(packetData);
    	
    	Eobr_Comm_Response_Packet commResponsePacket = new Eobr_Comm_Response_Packet();
		errorCode = EobrCommunications.Companion.validateWithData(_threadMgr, eobrPacket, cmdId,
				this.GetActiveDeviceCrc(), VerifyRxPacketResponseByte.EobrNullPacket,
				commResponsePacket, false, socketVerifier);
    	
    	return errorCode;    			
	}
		
	
	/**
	 * Clears status buffer including odometer and odometer offset
	 * @return error code
	 * @throws EobrException 
	 */
	private void clearStatusBufferData() throws EobrException
	{    	
    	int cmdId = EUCMDType.EUCMD_RESET_STATUS_BUFFER;
    	int dataSize = 0;

    	Eobr_Packet eobrPacket = new Eobr_Packet();
		eobrPacket.setCmd((byte)cmdId);
		eobrPacket.setCrc(this.GetActiveDeviceCrc());
		eobrPacket.setLen((byte)dataSize);
		
		byte[] packetData = new byte[dataSize];
    	
    	eobrPacket.setData(packetData);		
    	
    	Eobr_Comm_Response_Packet commResponsePacket = new Eobr_Comm_Response_Packet();
		int errorCode = EobrCommunications.Companion.validateWithData(_threadMgr, eobrPacket,
		cmdId, this.GetActiveDeviceCrc(), VerifyRxPacketResponseByte.EobrBytePacket,
				commResponsePacket, false, socketVerifier);
    	
    	// HACK: EOBR returns 255 even though Status Buffer reset occurs
    	if(errorCode == 255) 
    		return; 
    	
    	EobrException eobrException = EobrException.getEobrExceptionFromEobrReturnCodeValue(errorCode);
    	if(eobrException != null)
    		throw eobrException; 
	}
	
	private void clearHistogramData(int histType) throws EobrException
	{
    	
    	int cmdId = EUCMDType.EUCMD_CLEAR_HISTOGRAM;
    	int dataSize = 1;

    	Eobr_Packet eobrPacket = new Eobr_Packet();
		eobrPacket.setCmd((byte)cmdId);
		eobrPacket.setCrc(this.GetActiveDeviceCrc());
		eobrPacket.setLen((byte)dataSize);
		
		byte[] packetData = new byte[dataSize];
    	packetData[0] = (byte)histType; 
    	
    	eobrPacket.setData(packetData);
		
    	Eobr_Comm_Response_Packet commResponsePacket = new Eobr_Comm_Response_Packet();
		int errorCode = EobrCommunications.Companion.validateWithData(_threadMgr, eobrPacket,
				cmdId, this.GetActiveDeviceCrc(), VerifyRxPacketResponseByte.EobrBytePacket,
				commResponsePacket, false, socketVerifier);

    	EobrException eobrException = EobrException.getEobrExceptionFromEobrReturnCodeValue(errorCode); 
    	if(eobrException != null)
    		throw eobrException;  			
	}
	//region ISendReceiveBulkData

	public boolean sendBulkData(Eobr_Packet eobrPacket, int packetSize) {

		boolean retVal = false;
		
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

				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
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

	public boolean sendEobrDataPacketBulkData(Eobr_Data_Packet dataPacket)
	{
		return false;
	}
	
	public boolean sendEobrDataPacketBulkData(Eobr_Data_Packet_GenII dataPacket)
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
	
				int messageLength = dataPacket.getLen() + 4;
				byte[] message = new byte[messageLength];
				message[0] = dataPacket.getCmd();
				byte[] crc = shortToByteArray(dataPacket.getCrc());
				message[1] = crc[0]; // first byte of crc
				message[2] = crc[1]; // second byte of crc
				message[3] = dataPacket.getLen();
				message[4] = dataPacket.getMethod();
				byte[] recordId = intToByteArray(dataPacket.getRecordId());
				message[5] = recordId[0];
				message[6] = recordId[1];
				message[7] = recordId[2];
				message[8] = recordId[3];
				
				byte[] timestamp;
				
				if(dataPacket.getCmd() == EUCMDType.EUCMD_GET_CONSOLE_LOG)
				{
					long secondsSince1970 = dataPacket.getTimecode() / 1000;
					timestamp = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(secondsSince1970).array();
					message[9] = timestamp[0];
					message[10] = timestamp[1];
					message[11] = timestamp[2];
					message[12] = timestamp[3];
					
				} else
				{
					timestamp = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(dataPacket.getTimecode()).array();
					message[9] = timestamp[0];
					message[10] = timestamp[1];
					message[11] = timestamp[2];
					message[12] = timestamp[3];
					message[13] = timestamp[4];
					message[14] = timestamp[5];
					message[15] = timestamp[6];
					message[16]	= timestamp[7];					
				}
				
				// If writing for GetEobrData method, include MotionOption and RefTimestampOption
				if (dataPacket.getCmd() == EUCMDType.EUCMD_GET_EOBR_DATA)
				{
					message[17] = dataPacket.getMotionOption();
					message[18] = dataPacket.getRefTimestampOption();
				}
				// Else, if writing for event record data, include the event type
				else if (dataPacket.getCmd() == EUCMDType.EUCMD_GET_EVENT_DATA ||
						dataPacket.getCmd() == EUCMDType.EUCMD_GET_HISTOGRAM)
				{
					message[17] = dataPacket.getEventType();
					message[18] = dataPacket.getRefTimestampOption();

					// Do I need eventMask here?
					if (dataPacket.getCmd() == EUCMDType.EUCMD_GET_EVENT_DATA && dataPacket.getLen() == 19) {
						byte[] eventMask = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putInt(dataPacket.getEventMask()).array();
						message[19] = eventMask[0];
						message[20] = eventMask[1];
						message[21] = eventMask[2];
						message[22] = eventMask[3];
					}
				}
				else if (dataPacket.getCmd() == EUCMDType.EUCMD_GET_TRIP_REPORT) {
					message[17] = dataPacket.getRefTimestampOption();
				}
				else if (dataPacket.getCmd() == EUCMDType.EUCMD_GET_JBUS_DIAG_DATA) {
					message[17] = dataPacket.getRefTimestampOption();
				}
				
				out.write(message);
				out.flush();
				retVal = true;

				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
			}
			catch (IOException e) {
				_isSocketConnected = false;
				retVal = false;
			}
		}
		
		return retVal;		
	}

	//endregion

	private Object btReaderLock = new Object();

	public byte[] receiveBulkData(boolean eobrData)
	{
    	boolean retVal = false;
    	int bytesRead = 0;
    	int totalBytesRead = 0;
    	byte[] buffer = new byte[RECEIVED_PACKET_SIZE];
    	byte[] response = new byte[RECEIVED_PACKET_SIZE];

    	boolean interrupted = false;
    	
    	int len = -1;
    	
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

				while (!done && !interrupted && bluetoothReader != null)
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
								response[i + totalBytesRead] = buffer[i];
							}
						totalBytesRead += bytesRead;				
	
						if (len == -1 && totalBytesRead > 1)
						{
							len = (int)response[1] & 0xff;							
						}
						
						if (!done && len >= 0 && totalBytesRead >= len+2)
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

	public boolean sendBulkData(Eobr_FW_Block_Packet eobrPacket, int packetSize)
	{
		boolean retVal = false;

		if (!_isSocketConnected || _localSocket == null)
		{
			if (this.HasActiveDevice())
			{
				_localSocket = this.SetupAndOpenBluetoothSocket(getCommThreadManager(), _btAdapter, this.GetActiveDeviceAddress());
				if (_localSocket != null)
				{
					_isSocketConnected = true;
				}
			}
		}

		if (_isSocketConnected && _localSocket != null)
		{
			try
			{
				OutputStream out = _localSocket.getOutputStream();

				byte[] message = new byte[packetSize];
				message[0] = eobrPacket.getCmd();
				byte[] crc = shortToByteArray(eobrPacket.getCrc());
				message[1] = crc[0]; // first byte of crc
				message[2] = crc[1]; // second byte of crc
				message[3] = eobrPacket.getLen();
				message[4] = eobrPacket.getFWType();

				// write out the FW Block Address and then all the bytes of the ImageBlock
				int dataOffset = EOBR_FW_PACKET_SIZE - EOBR_FW_PAYLOAD_SIZE;
				int payloadByteCount = EOBR_FW_BLOCK_CODE_SIZE + EOBR_FW_BLOCK_CODE_ADDRESS_SIZE + EOBR_FW_BLOCK_CODE_TYPE_SIZE + dataOffset;
				for (int i = dataOffset; i < payloadByteCount; i++)			
				{
					int dataIndex = i - dataOffset;
					if (dataIndex < eobrPacket.getData().length)
					{
						message[i] = eobrPacket.getData()[dataIndex];
					}
				}

				out.write(message);
				out.flush();
				retVal = true;
			}
			catch (IOException e)
			{
				Log.e("Comm", "Error on sending BT sendBulkData", e);
				_isSocketConnected = false;
				retVal = false;
			}
		}

		return retVal;
	}

	public boolean sendEobrDriverEventPacket(Eobr_Driver_Event_Packet driverPacket)
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

				int messageLength = driverPacket.getLen() + 4;
				byte[] message = new byte[messageLength];
				message[0] = driverPacket.getCmd();

				byte[] crc = shortToByteArray(driverPacket.getCrc());
				message[1] = crc[0]; // first byte of crc
				message[2] = crc[1]; // second byte of crc
				message[3] = driverPacket.getLen();

				byte[] startTimestamp;
				startTimestamp = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(driverPacket.getStartTimeCode()).array();
				message[4] = startTimestamp[0];
				message[5] = startTimestamp[1];
				message[6] = startTimestamp[2];
				message[7] = startTimestamp[3];
				message[8] = startTimestamp[4];
				message[9] = startTimestamp[5];
				message[10] = startTimestamp[6];
				message[11]	= startTimestamp[7];

				byte[] endTimestamp;
				endTimestamp = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(driverPacket.getEndTimeCode()).array();
				message[12] = endTimestamp[0];
				message[13] = endTimestamp[1];
				message[14] = endTimestamp[2];
				message[15] = endTimestamp[3];
				message[16] = endTimestamp[4];
				message[17] = endTimestamp[5];
				message[18] = endTimestamp[6];
				message[19]	= endTimestamp[7];

				// Check if Get_Driver_Event or Get_Driver_Count
				if (driverPacket.getLen() == 21)
				{
					byte[] eventMask = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(driverPacket.getEventMask()).array();
					message[20] = eventMask[0];
					message[21] = eventMask[1];
					message[22] = eventMask[2];
					message[23] = eventMask[3];

					message[24] = driverPacket.getSearchMethod();
				}

				out.write(message);
				out.flush();
				retVal = true;

				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
			}
			catch (IOException e) {
				_isSocketConnected = false;
				retVal = false;
			}
		}

		return retVal;
	}

    public int SetClockUTC(Date newClock)    
    {
    	int errorCode;
    	int cmdId = EUCMDType.EUCMD_SET_CLOCK_UTC;
  
    	Calendar cal = Calendar.getInstance();
    	cal.setTimeZone(TimeZone.getTimeZone("GMT"));
    	cal.setTime(newClock);    	
    	
    	if (cal.get(Calendar.YEAR) < EOBR_MIN_YEAR || cal.get(Calendar.YEAR) > EOBR_MAX_YEAR)
    		errorCode = EobrReturnCode.S_INVALID_DATE_TIME;
    	else
    	{
    		Eobr_Packet eobrPacket = new Eobr_Packet();
    		eobrPacket.setCmd((byte)cmdId);
    		eobrPacket.setCrc(this.GetActiveDeviceCrc());
    		eobrPacket.setLen((byte)EUTIMESTAMP_SIZE_SECONDS_EPOCH);

    		byte[] data;
    		
    		Integer dateSeconds = Integer.valueOf((String.valueOf(cal.getTimeInMillis() / 1000)));
    		data = intToByteArray(dateSeconds);
    		
        	eobrPacket.setData(data);
        	
        	Eobr_Comm_Response_Packet commResponsePacket = new Eobr_Comm_Response_Packet();
			errorCode = EobrCommunications.Companion.validateWithData(_threadMgr, eobrPacket,
					cmdId, this.GetActiveDeviceCrc(), VerifyRxPacketResponseByte.EobrNullPacket,
					commResponsePacket, false, socketVerifier);
    	}
    	
    	return errorCode;
    }
	
    public Bundle GetClockUTC()
    {
    	int returnCode;
    	int cmdId = EUCMDType.EUCMD_GET_CLOCK_UTC;
    	Bundle answer = new Bundle();
    	Date eobrClock = null;
    	
		Eobr_Packet eobrPacket = new Eobr_Packet();
		eobrPacket.setCmd((byte)cmdId);
		eobrPacket.setCrc(this.GetActiveDeviceCrc());
		eobrPacket.setLen((byte)0); 
		
		Eobr_Comm_Response_Packet commResponsePacket = new Eobr_Comm_Response_Packet();
		returnCode = EobrCommunications.Companion.validateWithData(_threadMgr, eobrPacket, cmdId,
				this.GetActiveDeviceCrc(), VerifyRxPacketResponseByte.EobrStringPacket,
				commResponsePacket, false, socketVerifier);
    	
    	if (returnCode == EobrReturnCode.S_SUCCESS) {
    		// convert UNIX Epoch seconds into milliseconds, then date
			int rawEobrMilliseconds = LittleEndianHelper.Companion.getInt(
					commResponsePacket.getResponse(), 2,4);
			long convertedEobrMilliseconds = Integer.valueOf(rawEobrMilliseconds).longValue();
			eobrClock = new Date(convertedEobrMilliseconds * 1000);
    	}
    	
    	answer.putInt(Constants.RETURNCODE, returnCode);
    	if (eobrClock != null)
    		answer.putLong(Constants.RETURNVALUE, eobrClock.getTime());
    	
    	return answer;    	
    }

	public EobrResponse<Date> GetGPSTimestamp()
	{
		int returnCode;
		int cmdId = EUCMDType.EUCMD_GET_CLOCK_GPS;
		Date gpsTime = null;

		Eobr_Packet eobrPacket = new Eobr_Packet();
		eobrPacket.setCmd((byte)cmdId);
		eobrPacket.setCrc(this.GetActiveDeviceCrc());
		eobrPacket.setLen((byte)0);

		Eobr_Comm_Response_Packet commResponsePacket = new Eobr_Comm_Response_Packet();
		returnCode = EobrCommunications.Companion.validateWithData(_threadMgr, eobrPacket, cmdId,
				this.GetActiveDeviceCrc(), VerifyRxPacketResponseByte.EobrStringPacket,
				commResponsePacket, false, socketVerifier);

		EobrResponse<Date> response = new EobrResponse<Date>(returnCode);

		if (returnCode == EobrReturnCode.S_SUCCESS) {
			// convert UNIX Epoch seconds into milliseconds, then date
			int rawGpsMilliseconds = LittleEndianHelper.Companion.getInt(
					commResponsePacket.getResponse(), 2,4);
			long convertedGpsMilliseconds = Integer.valueOf(rawGpsMilliseconds).longValue();
			if(convertedGpsMilliseconds != 0)
				gpsTime = new Date(convertedGpsMilliseconds * 1000);
		}

		if (gpsTime != null)
			response.setData(gpsTime);

		return response;
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
				VerifyRxPacketResponseByte.EobrStringPacket, commResponsePacket, socketVerifier);
    	
    	if (errorCode == EobrReturnCode.S_SUCCESS) {
    		EobrStringPacket eobrBusTypePacket = EobrStringPacket.build(commResponsePacket.getResponse());
    		
    		// NOTE:  response contains 2 bytes - first byte is Target Bus Type (what we previously set)
    		// second byte is the current active bus type, that's what we are interested in.
    		currentBusType = eobrBusTypePacket.getStringVal()[1];
    		
            // Byte pattern sent by EOBR to indicate failure
           	// Error happen, size is 1
    		if (eobrBusTypePacket.getSize() == 0xFF && currentBusType == 1)
    			errorCode = EobrReturnCode.S_DEV_INTERNAL_ERROR;        			
    	}    	
    	
    	answer.putInt(Constants.RETURNCODE, errorCode);
    	answer.putInt(Constants.RETURNVALUE, currentBusType);
    	    	
    	return answer;    	
    }

	public Bundle GetVin()
	{
		int cmdId = EUCMDType.EUCMD_GET_DEV_INFO_ON_JBUS;
		Bundle answer = new Bundle();
		String devInfo = "";
		String vin = "";

		//Leaving make and model in the code for easy future reference
		//String make = "";
		//String model = "";
		Eobr_Packet eobrPacket = new Eobr_Packet();
		eobrPacket.setCmd((byte)cmdId);
		eobrPacket.setCrc(this.GetActiveDeviceCrc());
		eobrPacket.setLen((byte)1);

		Eobr_Comm_Response_Packet commResponsePacket = new Eobr_Comm_Response_Packet();
		int retVal = EobrCommunications.Companion.validateWithData(_threadMgr, eobrPacket, cmdId,
				this.GetActiveDeviceCrc(), VerifyRxPacketResponseByte.EobrStringPacket,
				commResponsePacket, false, socketVerifier);

		if (retVal == EobrReturnCode.S_SUCCESS) {
			EobrStringPacket eobrStrPacket = EobrStringPacket.build(commResponsePacket.getResponse());

			if(eobrStrPacket == null) {
				retVal = EobrReturnCode.S_GENERAL_ERROR;
			} else {
				try {
					devInfo = new String(eobrStrPacket.getStringVal(), "US-ASCII");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				if (devInfo.length() > 0) {
					//Leaving make and model in the code for easy future reference
					//make = devInfo.substring(1, 6).trim();
					//model = devInfo.substring(6, 31).trim();
					vin = devInfo.substring(31).trim();
					if(vin.length() != 17) {
						vin = "";
					}
				}
			}
		}

		answer.putInt(Constants.RETURNCODE, retVal);
		answer.putString(Constants.RETURNVALUE, vin);

		return answer;
	}

    public Bundle GetEobrOdometerOffset()
    {
    	int errorCode;
    	int cmdId = EUCMDType.EUCMD_GET_ODOMETER_OFFSET;
    	Bundle answer = new Bundle();
    	float offset = 0;    	
    	
    	Eobr_Comm_Response_Packet commResponsePacket = new Eobr_Comm_Response_Packet();
		errorCode = EobrCommunications.Companion.validateNoData(_threadMgr, cmdId,
				this.GetActiveDeviceCrc(),
				VerifyRxPacketResponseByte.EobrIntegerPacket, commResponsePacket, socketVerifier);
    	
    	if (errorCode == EobrReturnCode.S_SUCCESS) {
    		EobrIntegerPacket offsetPacket = new EobrIntegerPacket(commResponsePacket.getResponse());
    		
       		// get and convert to mi.
       		offset = (float)Math.round((offsetPacket.getIntegerVal() / Constants.MILES_TO_METERS) * 10) / 10;
        		
               // Byte pattern sent by EOBR to indicate failure
       		if (offsetPacket.getSize() == 0xFF) {
       			errorCode = EobrReturnCode.S_DEV_INTERNAL_ERROR;
       		}
    	}    	
    	
    	answer.putInt(Constants.RETURNCODE, errorCode);
    	answer.putFloat(Constants.RETURNVALUE, offset);
    	    	
    	return answer;    	
    }

	public Bundle GetConsoleLog(Date startDate, Date endDate)
	{
		Bundle returnBundle = new Bundle();
		
		int nextRecordId;
		int finalRecordId;
		
		StringBuilder consoleLog = new StringBuilder();
		
		//get the first segment of the console log
		Bundle startBundle = getConsoleLog(startDate);
		
		if(startBundle.getInt(Constants.RETURNCODE) == EobrReturnCode.S_SUCCESS)
		{
			//get the ID of the next console log segment
			nextRecordId = startBundle.getInt(NEXTCONSOLELOGRECORDID);
						
			//capture the first segment of the console log
			consoleLog.append(startBundle.getString(Constants.RETURNVALUE));
			
			//find the record ID of the console log at the requested end date
			Bundle endBundle = getConsoleLog(endDate);
			
			if(endBundle.getInt(Constants.RETURNCODE) == EobrReturnCode.S_SUCCESS)
			{
				finalRecordId = endBundle.getInt(NEXTCONSOLELOGRECORDID);
				
				//keep getting the console log 250 bytes at a time
				//until we hit the requested end
				while(nextRecordId < finalRecordId)
				{				
					//get the next segment of the console log
					Bundle bundle = getConsoleLog(nextRecordId);
					
					if(bundle.getInt(Constants.RETURNCODE) == EobrReturnCode.S_SUCCESS)
					{
						//keep track of what the next segment is
						nextRecordId = bundle.getInt(NEXTCONSOLELOGRECORDID);
						
						//save this segment
						consoleLog.append(bundle.getString(Constants.RETURNVALUE));
					}
					else
					{
						returnBundle.putInt(Constants.RETURNCODE,  bundle.getInt(Constants.RETURNCODE));
						break;
					}
				}
				
				if(!returnBundle.containsKey(Constants.RETURNCODE))
				{
					returnBundle.putInt(Constants.RETURNCODE, EobrReturnCode.S_SUCCESS);
					returnBundle.putString(Constants.RETURNVALUE, consoleLog.toString());
				}
			} else
			{
				returnBundle.putInt(Constants.RETURNCODE, endBundle.getInt(Constants.RETURNCODE));
			}
		} else
		{
			returnBundle.putInt(Constants.RETURNCODE, startBundle.getInt(Constants.RETURNCODE));
		}

		return returnBundle;
	}

	
    /**
     * Gets the first 250 bytes of the console log at (approximately) the specified timestamp
     * 
     * @param timestamp
     * @return bundle with return code, the record ID for the next 250 bytes of the log, and the 250 bytes itself
     */
    private Bundle getConsoleLog(Date timestamp)
    {	
    	return getConsoleLog(new StatusRecordQueryMethodEnum(StatusRecordQueryMethodEnum.TIMESTAMP), 0, timestamp);
    }
    
    /**
     * Gets the first 250 bytes of the console log at the specified record ID
     * 
     * @param recordId
     * @return bundle with return code, the record ID for the next 250 bytes of the log, and the 250 bytes itself
     */
    private Bundle getConsoleLog(int recordId)
    {   	
    	return getConsoleLog(new StatusRecordQueryMethodEnum(StatusRecordQueryMethodEnum.RECORDID), recordId, null);
    }
	
    
	/**
	 * Gets the first 250 bytes of the console log at the specified timestamp or record ID
	 * 
	 * @param queryMethod
	 * @param recordId
	 * @param timestamp
	 * @return bundle with return code, the record ID for the next 250 bytes of the log, and the 250 bytes itself
	 */
	private Bundle getConsoleLog(StatusRecordQueryMethodEnum queryMethod, int recordId, Date timestamp)
	{		
	   	int retVal;
    	int cmdId = EUCMDType.EUCMD_GET_CONSOLE_LOG;
    	
    	Eobr_Data_Packet_GenII eobrDataPacket = new Eobr_Data_Packet_GenII();
    	eobrDataPacket.setCmd((byte)cmdId);
    	eobrDataPacket.setLen((byte)9);
    	eobrDataPacket.setCrc(this.GetActiveDeviceCrc());
    	eobrDataPacket.setRecordId(recordId);
    	
    	if(queryMethod.getValue() == StatusRecordQueryMethodEnum.RECORDID)
    		eobrDataPacket.setMethod((byte)RECORD_ID_TYPE);
    	else
    		eobrDataPacket.setMethod((byte)TIMESTAMP_TYPE);
    	
    	if (timestamp != null)
    	{
    		eobrDataPacket.setTimecode(timestamp.getTime());
    	}
    	    	    	
   		Eobr_Comm_Response_Packet commResponsePacket = new Eobr_Comm_Response_Packet();
		retVal = EobrCommunications.Companion.validate(_threadMgr, eobrDataPacket, cmdId,
				this.GetActiveDeviceCrc(), commResponsePacket, socketVerifier);
    	
   		String consoleLog = null;
   		int nextConsoleLogRecordId = 0;   		
   		
    	if (retVal == EobrReturnCode.S_SUCCESS) {
    		EobrStringPacket eobrStrPacket = EobrStringPacket.build(commResponsePacket.getResponse());
    		
    		if(eobrStrPacket == null)
    			retVal = EobrReturnCode.S_GENERAL_ERROR;
    		else 
    		{
               	nextConsoleLogRecordId = LittleEndianHelper.Companion.getInt(eobrStrPacket.getStringVal(), 0, 4);
               	
               	if(eobrStrPacket.getSize() > 4) {
    			 	StringBuilder consoleLogSb = new StringBuilder();	
    			 	
		    		for (int i = 4; i < eobrStrPacket.getSize(); i++)
		    			consoleLogSb.append((char)eobrStrPacket.getStringVal()[i]);
		    		
		    		consoleLog = consoleLogSb.toString();
               	}
    		}
    	}    	
    	
    	Bundle answer = new Bundle();
    	answer.putInt(Constants.RETURNCODE, retVal);
    	answer.putInt(NEXTCONSOLELOGRECORDID, nextConsoleLogRecordId);
    	answer.putString(Constants.RETURNVALUE, consoleLog);
    	    	
    	return answer; 
	}
    
    public int SetEobrOdometerOffset(float offset)
    {
    	int errorCode;
    	int cmdId = EUCMDType.EUCMD_SET_ODOMETER_OFFSET;
    	
    	Eobr_Packet eobrPacket = new Eobr_Packet();
		eobrPacket.setCmd((byte)cmdId);
		eobrPacket.setCrc(this.GetActiveDeviceCrc());
		eobrPacket.setLen((byte)4);
		eobrPacket.setData(intToByteArray((int)(offset * Constants.MILES_TO_METERS)));

		Eobr_Comm_Response_Packet commResponsePacket = new Eobr_Comm_Response_Packet();
		errorCode = EobrCommunications.Companion.validateWithData(_threadMgr, eobrPacket, cmdId,
				this.GetActiveDeviceCrc(), VerifyRxPacketResponseByte.EobrNullPacket,
				commResponsePacket, false, socketVerifier);
    	
    	return errorCode;    	   	
    }
    
    public Bundle SendConsoleCommandToDevice(String command)
    {
    	return this.PerformSendConsoleCommandToDevice(command, true);
    }
    
    public Bundle SendConsoleCommandToDeviceWithNoRetry(String command)
    {
    	return this.PerformSendConsoleCommandToDevice(command, false);
    }
    
    private Bundle PerformSendConsoleCommandToDevice(String command, boolean retryIfNotSuccessful)
    {
    	Bundle answer = new Bundle();
    	int errorCode;
    	int cmdId = EUCMDType.EUCMD_CONSOLE_COMMAND;
    	StringBuilder consoleLogBuilder = new StringBuilder();
    	
		byte[] data = stringToByteArray(command);
    	
    	Eobr_Packet eobrPacket = new Eobr_Packet();
		eobrPacket.setCmd((byte)cmdId);
		eobrPacket.setCrc(this.GetActiveDeviceCrc());
		eobrPacket.setLen((byte)data.length);
		eobrPacket.setData(data);
    	
    	Eobr_Comm_Response_Packet commResponsePacket = new Eobr_Comm_Response_Packet();
    	if(retryIfNotSuccessful)
			errorCode = EobrCommunications.Companion.validateWithData(_threadMgr, eobrPacket,
			cmdId, this.GetActiveDeviceCrc(), VerifyRxPacketResponseByte.EobrStringPacket,
					commResponsePacket, false, socketVerifier);
    	else
			errorCode = EobrCommunications.Companion.validateWithData(_threadMgr, eobrPacket,
			cmdId, this.GetActiveDeviceCrc(), VerifyRxPacketResponseByte.EobrStringPacket,
					commResponsePacket, false, socketVerifier, 1);
    		
		if (errorCode == EobrReturnCode.S_SUCCESS)
		{
			EobrStringPacket eobrStrPacket = EobrStringPacket.build(commResponsePacket.getResponse());
			if (eobrStrPacket == null)
			{
				errorCode = EobrReturnCode.S_GENERAL_ERROR;
			}
			else
			{
				
				int nextRecordId;
				final int FINAL_RECORD_ID = 0xFFFFFFFF;

				nextRecordId = LittleEndianHelper.Companion.getInt(eobrStrPacket.getStringVal(), 0, 4);

				if (eobrStrPacket.getSize() > 4)
				{
					for (int i = 4; i < eobrStrPacket.getSize(); i++)
						consoleLogBuilder.append((char) eobrStrPacket.getStringVal()[i]);
				}
				
				// Keep getting the console log 250 bytes at a time until we hit the end
				while (nextRecordId != FINAL_RECORD_ID)
				{				
					// Get the next segment of the console log
					Bundle bundle = getConsoleLog(nextRecordId);
					errorCode = bundle.getInt(Constants.RETURNCODE);
					if(errorCode == EobrReturnCode.S_SUCCESS)
					{
						// Keep track of what the next segment is
						nextRecordId = bundle.getInt(NEXTCONSOLELOGRECORDID);
						
						if(nextRecordId != FINAL_RECORD_ID)
							// Save this segment
							consoleLogBuilder.append(bundle.getString(Constants.RETURNVALUE));
					}
					else
					{
						break;
					}
				}
			}
		}
    	    	
		answer.putInt(Constants.RETURNCODE, errorCode);
    	answer.putString(Constants.RETURNVALUE, consoleLogBuilder.toString());
		
    	return answer;
    }
    
	public boolean SetSelfTest()
	{
    	int cmdId = EUCMDType.EUCMD_SET_SELF_TEST;
    	int errorCode;
    	    	
    	Eobr_Comm_Response_Packet commResponsePacket = new Eobr_Comm_Response_Packet();
		errorCode = EobrCommunications.Companion.validateNoData(_threadMgr, cmdId,
				this.GetActiveDeviceCrc(),
				VerifyRxPacketResponseByte.EobrNullPacket, commResponsePacket, socketVerifier);
    	    	
    	return (errorCode == EobrReturnCode.S_SUCCESS);
	}

	public Bundle GetSelfTest()
	{
    	int cmdId = EUCMDType.EUCMD_GET_SELF_TEST;
    	int errorCode;
    	int testResult = 1;
    	Bundle answer = new Bundle();

    	Eobr_Comm_Response_Packet commResponsePacket = new Eobr_Comm_Response_Packet();
		errorCode = EobrCommunications.Companion.validateNoData(_threadMgr, cmdId,
				this.GetActiveDeviceCrc(),
				VerifyRxPacketResponseByte.EobrShortPacket, commResponsePacket, socketVerifier);
    	    
    	if (errorCode == EobrReturnCode.S_SUCCESS) {
    		EobrShortPacket selfTestPacket = new EobrShortPacket(commResponsePacket.getResponse());
    		testResult = selfTestPacket.getShortVal();
    	}    	
    	
    	answer.putInt(Constants.RETURNCODE, errorCode);
    	answer.putInt(Constants.RETURNVALUE, testResult);
    	    	
    	return answer;  
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
    		
   			dataRate = dataRate / 1000;
    	}    	
    	
    	answer.putInt(Constants.RETURNCODE, errorCode);
    	answer.putInt(Constants.RETURNVALUE, dataRate);
    	
    	return answer;    	
    }

    public int ChangeDataCollectionRate(int newDataRate)
    {
    	int errorCode;
    	int cmdId = EUCMDType.EUCMD_CHANGE_DATA_RATE;

    	// Gen II stores in milliseconds
		newDataRate = newDataRate * 1000;

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

    // returns a bundle containing the following:
    //	- rpm threshold set in TAB
    //	- speed threshold set in TAB - returns miles/hour (Note: TAB stores in KPH * 100
    //	- hard brake threshold set in TAB - returns miles/hour/second (Note: TAB stores in KPH * 100/second
    //	- drive start distance threshold set in TAB - returns miles (Note: TAB stores in KM * 100)
    //	- drive stop time threshold
    //	- event blanking threshold
    /**
     *  returns a bundle containing the data listed below for the driver threshold values
     *  or the default threshold values:
     *  @param thresholdType - 0 to retrieve driver threshold settings
     *  				  	-1 (0xffffffff) to retrieve default threshold settings
     *  @returns bundle containig the following:
     *	- rpm threshold set in TAB
     *	- speed threshold set in TAB - returns miles/hour (Note: TAB stores in KPH * 100
     *	- hard brake threshold set in TAB - returns miles/hour/second (Note: TAB stores in KPH * 100/second
     *	- drive start distance threshold set in TAB - returns miles (Note: TAB stores in KM * 100)
     *	- drive stop time threshold
     *	- event blanking threshold
     */
    public Bundle GetThresholdValues(int thresholdType)
    {
		boolean needsPreMandateDataSize;
    	int errorCode;
    	int cmdId = EUCMDType.EUCMD_GET_THRESHOLDS;
    	Bundle answer = new Bundle();
    	int rpmThreshold = 0;
    	float speedThreshold = 0;
    	float hardBrakeThreshold = 0;
    	float driveStartDistanceThreshold = 0;
    	int driveStopTimeThreshold = 0;
    	int eventBlankingThreshold = 0;
    	int driverIdCRC = 0;
    	int dataSize = 4;
		float driveStartSpeed = 0;

		needsPreMandateDataSize = !supportsDriveStartSpeed();
    	
    	Eobr_Packet eobrPacket = new Eobr_Packet();
		eobrPacket.setCmd((byte)cmdId);
		eobrPacket.setCrc(this.GetActiveDeviceCrc());
		eobrPacket.setLen((byte)dataSize);

		byte[] packetData = new byte[dataSize];

		// add thresholdType value to packetdata
		byte[] value = new byte[4];
		value = intToByteArray(thresholdType);
		packetData[0] = value[0];
		packetData[1] = value[1];
		packetData[2] = value[2];
		packetData[3] = value[3];

		eobrPacket.setData(packetData);
    	
		Eobr_Comm_Response_Packet commResponsePacket = new Eobr_Comm_Response_Packet();
		errorCode = EobrCommunications.Companion.validateWithData(_threadMgr, eobrPacket, cmdId,
				this.GetActiveDeviceCrc(), VerifyRxPacketResponseByte.EobrStringPacket,
				commResponsePacket, false, socketVerifier);
    	
    	if (errorCode == EobrReturnCode.S_SUCCESS) {
    		ByteBuffer buffer;
    		EobrStringPacket eobrResponsePacket = EobrStringPacket.build(commResponsePacket.getResponse());

    		// Threshold data is sent in 10 bytes - 2 bytes for each threshold value
    		// Get RPM Threshold
           	rpmThreshold = (int)(LittleEndianHelper.Companion.getShort(eobrResponsePacket.getStringVal(), 0, 2) + 0.5);
           	// Get Speedometer Threshold - stored as KM * 100 in TAB
           	speedThreshold = (float)Math.round((((float)LittleEndianHelper.Companion.getShort(eobrResponsePacket.getStringVal(), 2, 2) / 100f) / Constants.MILES_TO_KILOMETERS) * 10) / 10;
            // Get Hardbrake Threshold - stored as KM * 100 in TAB
           	hardBrakeThreshold = (float)Math.round((((float)LittleEndianHelper.Companion.getShort(eobrResponsePacket.getStringVal(), 4, 2) / 100f) / Constants.MILES_TO_KILOMETERS) * 10) /10;
           	// Get DriveStartDistance Threshold - stored as KM * 100 in TAB
           	driveStartDistanceThreshold = (float)Math.round((((float)LittleEndianHelper.Companion.getShort(eobrResponsePacket.getStringVal(), 6, 2) / 100f) / Constants.MILES_TO_KILOMETERS) * 10) / 10;
    		// Get DriveStopTime Threshold - convert seconds to minutes
           	driveStopTimeThreshold = (int)((LittleEndianHelper.Companion.getShort(eobrResponsePacket.getStringVal(), 8, 2) / 60) + 0.5);
           	// Get RPM Threshold
           	eventBlankingThreshold = (int)(LittleEndianHelper.Companion.getShort(eobrResponsePacket.getStringVal(), 10, 2) + 0.5);
           	// Get DriverIdCRC
           	driverIdCRC = LittleEndianHelper.Companion.getInt(eobrResponsePacket.getStringVal(), 12, 4);
           	// Get DriveStartSpeed
			if (!needsPreMandateDataSize) {
				driveStartSpeed = (float) Math.round((((float) LittleEndianHelper.Companion.getShort(eobrResponsePacket.getStringVal(), 16, 2) / 100f) / Constants.MILES_TO_KILOMETERS) * 10) / 10;
			}
    	}    	
    	
    	answer.putInt(Constants.RETURNCODE, errorCode);
    	answer.putInt(Constants.RPMTHRESHOLD, rpmThreshold);
    	answer.putFloat(Constants.SPEEDTHRESHOLD, speedThreshold);
    	answer.putFloat(Constants.HARDBRAKETHRESHOLD, hardBrakeThreshold);
    	answer.putFloat(Constants.DRIVESTARTDISTANCETHRESHOLD, driveStartDistanceThreshold);
    	answer.putInt(Constants.DRIVESTOPTIMETHRESHOLD, driveStopTimeThreshold);
    	answer.putInt(Constants.EVENTBLANKINGTHRESHOLD, eventBlankingThreshold);
		answer.putFloat(Constants.DRIVESTARTSPEED, driveStartSpeed);
    	answer.putInt(Constants.DRIVERIDCRC, driverIdCRC);
    	
    	return answer;    	   	    	
    }

    // Set Threshold values in eobr so threshold event data can be generated in the eobr
    // Note:  	speedThreshold value passed into this method should be in Miles - value passed to TAB is converted to KM (KPH)
    //			hardBrakeThreshold value passed into this method should be in Miles - value passed to TAB is converted to KM (KPH/sec)
    //			driveStartDistance value passed into this method should be in Miles - value passed to TAB is converted to KM
    //public Bundle SetThresholdValues(int rpmThreshold, int speedThreshold, int hardBrakeThreshold, float driveStartDistance, int driveStopTime, int eventBlanking, String driverId)
    public Bundle SetThresholdValues(int rpmThreshold, float speedThreshold, float hardBrakeThreshold, float driveStartDistance, int driveStopTime, int eventBlanking, String driverId, float driveStartSpeed)
    {
		boolean needsPreMandateDataSize = false;
    	int errorCode;
    	int dataSize;
    	int cmdId = EUCMDType.EUCMD_SET_THRESHOLDS;
        int driverIdCrc = 0;

		needsPreMandateDataSize = !supportsDriveStartSpeed();

		if (needsPreMandateDataSize)
			dataSize = 16;
		else
			dataSize = 18;
        
    	Eobr_Packet eobrPacket = new Eobr_Packet();
		eobrPacket.setCmd((byte)cmdId);
		eobrPacket.setCrc(this.GetActiveDeviceCrc());
		eobrPacket.setLen((byte)dataSize);

		byte[] packetData = new byte[dataSize];

		// add rpm threshold to packetdata
		byte[] value = new byte[2];
		value = shortToByteArray((short)rpmThreshold);
		packetData[0] = value[0];
		packetData[1] = value[1];
		
		// add speed threshold to packetdata
		// value needs to be converted to KM and multiplied by 100 (per protocol 
		// doc) - send the resulting int value to the TAB
		value = new byte[2];
		short convertedValue = (short)(speedThreshold * Constants.MILES_TO_KILOMETERS * 100);
		value = shortToByteArray(convertedValue);
		packetData[2] = value[0];
		packetData[3] = value[1];
		
		// add hardbrake threshold to packetdata
		// value needs to be converted to KM and multiplied by 100 (per protocol 
		// doc) - send the resulting int value to the TAB		
		value = new byte[2];
		convertedValue = (short)(hardBrakeThreshold * Constants.MILES_TO_KILOMETERS * 100);
		value = shortToByteArray(convertedValue);
		packetData[4] = value[0];
		packetData[5] = value[1];

		// add drive start threshold to packetdata
		// value needs to be converted to KM and multiplied by 100 (per protocol 
		// doc) - send the resulting int value to the TAB
		value = new byte[2];
		convertedValue = (short)(driveStartDistance * Constants.MILES_TO_KILOMETERS * 100);
		value = shortToByteArray(convertedValue);
		packetData[6] = value[0];
		packetData[7] = value[1];

		// add drive stop threshold to packetdata - convert minutes to seconds
		value = new byte[2];
		value = shortToByteArray((short)(driveStopTime * 60));
		packetData[8] = value[0];
		packetData[9] = value[1];
		
		// add event blanking value to packetdata
		value = new byte[2];
		value = shortToByteArray((short)eventBlanking);
		packetData[10] = value[0];
		packetData[11] = value[1];
		
		// if driverId is specified, calc CRC and add value to packetData
		if(driverId.length() > 0) {
			value = new byte[4];
			driverIdCrc = CalcCRC.Calculate(driverId, driverId.length());
			value = intToByteArray(driverIdCrc);
			packetData[12] = value[0];
			packetData[13] = value[1];
			packetData[14] = value[2];
			packetData[15] = value[3];
		}
		// else setting default threshold values - set driver id to 0xffffffff
		else
		{
			packetData[12] = (byte)0xff;
			packetData[13] = (byte)0xff;
			packetData[14] = (byte)0xff;
			packetData[15] = (byte)0xff;
		}
		if (!needsPreMandateDataSize) {
			value = new byte[2];
			convertedValue = (short) (driveStartSpeed * Constants.MILES_TO_KILOMETERS * 100);
			value = shortToByteArray(convertedValue);
			packetData[16] = value[0];
			packetData[17] = value[1];
		}
    	eobrPacket.setData(packetData);
        	
    	Eobr_Comm_Response_Packet commResponsePacket = new Eobr_Comm_Response_Packet();
		errorCode = EobrCommunications.Companion.validateWithData(_threadMgr, eobrPacket, cmdId,
				this.GetActiveDeviceCrc(), VerifyRxPacketResponseByte.EobrNullPacket,
				commResponsePacket, false, socketVerifier);
    	
    	Bundle response = new Bundle();
    	response.putInt(Constants.RETURNCODE, errorCode);
    	response.putInt(Constants.DRIVERIDCRC, driverIdCrc);
    	
    	return response;    	
    }

	private boolean supportsDriveStartSpeed() {
		Bundle firmwareVersion = this.GetEOBRDllRevisions();

		if(firmwareVersion.getInt(Constants.RETURNCODE) == EobrReturnCode.S_SUCCESS) {

			//starting in 6.88, the TAB became capable of accepting
			//a new "drive start speed" for the mandate.
			double minVersion = 6.88;

			String revision = firmwareVersion.getString(Constants.MAINFIRMWAREREVISION);

			if(revision != null) {
				Pattern pattern = Pattern.compile("\\d+\\.\\d+");
				Matcher matcher = pattern.matcher(revision);

				if(matcher.find()) {
					double currentVersion = Double.parseDouble(matcher.group());

					return currentVersion >= minVersion;
				}
			}
		}

		return false;
	}

	public int DownloadFirmwareUpdate(InputStream firmwareUpdateFile, FirmwareUpgradeTypeEnum firmwareUpgradeType, FirmwareUpdateBroadcaster broadcaster, FirmwareUpdate firmwareUpdateConfig)
	{
		// Only allow application firmware upgrades
		if (firmwareUpgradeType != FirmwareUpgradeTypeEnum.APP)
			return EobrReturnCode.S_FUNC_NOT_IMPLEMENTED;

		int errorCode;

		final int upgradeTypeValue = FW_APPLICATION;

		errorCode = setFirmwareUpgradeMode(FW_UPGRADE_BEGIN, upgradeTypeValue);
		if (errorCode == EobrReturnCode.S_SUCCESS)
		{
			try {
				EOBR_FW_BLOCK_CODE_SIZE = EOBR_FW_BLOCK_CODE_SIZE_SMALL;					

				// determine whether the large packet sizes will work
				if(firmwareUpdateConfig != null && firmwareUpdateConfig.getSupportsLargeBlockDownload())
					EOBR_FW_BLOCK_CODE_SIZE = EOBR_FW_BLOCK_CODE_SIZE_LARGE;
					
				EOBR_FW_PAYLOAD_SIZE = EOBR_FW_BLOCK_CODE_SIZE + EOBR_FW_BLOCK_CODE_ADDRESS_SIZE;
				EOBR_FW_PACKET_SIZE = EOBR_FW_PAYLOAD_SIZE + 5;
				
				errorCode = sendFirmwareImageToDevice(firmwareUpdateFile, upgradeTypeValue, broadcaster);
			} catch (Exception e){
				Log.e("firmwareUpdate", "Error sending firmware to the device..", e);
			}

			if (errorCode == EobrReturnCode.S_SUCCESS)
			{
				// End the firmware upgrade
				errorCode = setFirmwareUpgradeMode(FW_UPGRADE_END, upgradeTypeValue);
			}

			if (errorCode == EobrReturnCode.S_SUCCESS)
			{
				// Reset the EOBR
				errorCode = setFirmwareUpgradeMode(FW_UPGRADE_RESET, upgradeTypeValue);
				try {
					Thread.sleep(15000);
				} catch (InterruptedException e) {
					Log.e("UnhandledCatch", e.getMessage() + ": " + Log.getStackTraceString(e));
				}
			}
		}

		return errorCode;
	}

	/**
     * Sets the firmware upgrade mode for the given upgrade type.
	 * @param upgradeMode The mode to set it to. Should be FW_UPGRADE_BEGIN, FW_UPGRADE_END, or FW_UPGRADE_RESET.
	 * @param upgradeType The upgrade type for which to set the mode. Should be FW_APPLICATION or FW_BOOTLOADER.
	 * @return The return code returned from the EOBR
	 */
	private int setFirmwareUpgradeMode(int upgradeMode, int upgradeType)
	{
		int errorCode;
		int cmdId = EUCMDType.EUCMD_START_FW_UPGRADE;

		Eobr_Packet eobrPacket = new Eobr_Packet();
		eobrPacket.setCmd((byte)cmdId);
		eobrPacket.setCrc(this.GetActiveDeviceCrc());
		eobrPacket.setLen((byte)2);
		eobrPacket.setData(new byte[] { (byte)upgradeMode, (byte)upgradeType });
		
		Eobr_Comm_Response_Packet commResponsePacket = new Eobr_Comm_Response_Packet();
		errorCode = EobrCommunications.Companion.validateWithData(_threadMgr, eobrPacket, cmdId,
				this.GetActiveDeviceCrc(), VerifyRxPacketResponseByte.EobrNullPacket,
				commResponsePacket, false, socketVerifier);
    	
		return errorCode;
	}

    /**
     * Sends an open input stream of a new firmware image to the device.
     * @param firmwareImageStream The open input stream of the new firmware image. The stream will be left open.
	 * @param upgradeType The upgrade type that should be updated. Should be FW_APPLICATION or FW_BOOTLOADER.
	 * @param broadcaster An optional listener that will get progress updates as the firmware gets updated
     * @return The return code from the EOBR
     */
	private int sendFirmwareImageToDevice(InputStream firmwareImageStream, int upgradeType, FirmwareUpdateBroadcaster broadcaster)
	{
		int cmdId = EUCMDType.EUCMD_FW_IMAGE_BLOCK;
		int errorCode = EobrReturnCode.S_SUCCESS;

		Eobr_FW_Block_Packet eobrPacket = new Eobr_FW_Block_Packet();
		eobrPacket.setCmd((byte)cmdId);
		eobrPacket.setCrc(this.GetActiveDeviceCrc());
		
		eobrPacket.setLen((byte)(EOBR_FW_BLOCK_CODE_SIZE + EOBR_FW_BLOCK_CODE_ADDRESS_SIZE + EOBR_FW_BLOCK_CODE_TYPE_SIZE));
		eobrPacket.setFWType((byte)upgradeType);

		try
		{
			// Get all the bytes for the new firmware image
			byte[] firmwareImageBytes = readEntireInputStream(firmwareImageStream);

			// Send the entire image in blocks
			int eobrAddress = 0x0; // Start at address 0x0
			for (int imagePosition = 0; imagePosition < firmwareImageBytes.length; imagePosition += EOBR_FW_BLOCK_CODE_SIZE)
			{
				byte[] buffer = new byte[EOBR_FW_BLOCK_CODE_SIZE + EOBR_FW_BLOCK_CODE_ADDRESS_SIZE];
				byte[] eobrAddressBytes = intToByteArray(eobrAddress);
				buffer[0] = eobrAddressBytes[0];
				buffer[1] = eobrAddressBytes[1];
				buffer[2] = eobrAddressBytes[2];
				buffer[3] = eobrAddressBytes[3];
				int imageByteCountToCopy = Math.min(EOBR_FW_BLOCK_CODE_SIZE, firmwareImageBytes.length - imagePosition);
				System.arraycopy(firmwareImageBytes, imagePosition, buffer, EOBR_FW_BLOCK_CODE_ADDRESS_SIZE, imageByteCountToCopy);

				eobrPacket.setData(buffer);

				Eobr_Comm_Response_Packet responsePacket = new Eobr_Comm_Response_Packet();
				EobrCommunications.Companion.sendAndConfirmFirmwarePacket(getCommThreadManager(), this.GetActiveDeviceCrc(),

						eobrPacket,
						responsePacket, socketVerifier);

				errorCode = responsePacket.getReturnCode();
				if(errorCode != EobrReturnCode.S_SUCCESS){
					broadcaster.onFirmwareUpdateFinished(false);
					break;
				}

				if (broadcaster != null)
					broadcaster.onDownloadFirmwareProgress((int)((double)imagePosition / (double)firmwareImageBytes.length * 100.0));
				eobrAddress += EOBR_FW_BLOCK_CODE_SIZE;
			}
			
			if (broadcaster != null)
				broadcaster.onDownloadFirmwareProgress(100);
		}
		catch (Exception e)
		{
			Log.e("FirmwareUpdate", "Error updating firmware", e);
			errorCode = EobrReturnCode.S_DEV_INTERNAL_ERROR;
		}

		return errorCode;
	}



	public boolean sendEobrFirmwareBlockPacket(Eobr_FW_Block_Packet fwBlockPacket){
		return this.sendBulkData(fwBlockPacket, EOBR_FW_PACKET_SIZE);
	}
	
	private byte[] readEntireInputStream(InputStream inputStream) throws IOException
	{
		byte[] firmwareImageBytes = null;
		
		BufferedInputStream bufferedInputStream = null;
		ByteArrayOutputStream firmwareImageByteStream = null;
		try
		{
			bufferedInputStream = new BufferedInputStream(inputStream);
			firmwareImageByteStream = new ByteArrayOutputStream();
			
			int bytesRead;
			byte[] buffer = new byte[16384];
			while ((bytesRead = bufferedInputStream.read(buffer, 0, buffer.length)) > 0)
			{
				firmwareImageByteStream.write(buffer, 0, bytesRead);
			}
			firmwareImageByteStream.flush();
			
			firmwareImageBytes = firmwareImageByteStream.toByteArray();
		}
		finally
		{
			if (firmwareImageByteStream != null)
				firmwareImageByteStream.close();
		}
		
		return firmwareImageBytes;
	}

	private Bundle setFileTransferControl(int contentType, int contentLength, int checksum, int transferMode, int openMode, String fileName)
	{
		Bundle answer;
		int errorCode;
		int cmdId = EUCMDType.EUCMD_FILE_XFER_CONTROL;
		int blockSize = EOBR_FILE_XFER_BLOCK_CODE_SIZE;
		byte[] filenameBytes = stringToByteArray(fileName);
		
		Eobr_Packet eobrPacket = new Eobr_Packet();
		eobrPacket.setCmd((byte)cmdId);
		eobrPacket.setCrc(this.GetActiveDeviceCrc());
		
		byte[] buffer = new byte[filenameBytes.length + 12];
		buffer[0] = (byte)contentType;

		// next 4 bytes are for the contentLength
		byte[] contentLengthBytes = intToByteArray(contentLength);
		buffer[1] = contentLengthBytes[0];
		buffer[2] = contentLengthBytes[1];
		buffer[3] = contentLengthBytes[2];
		buffer[4] = contentLengthBytes[3];
		
		buffer[5] = (byte)blockSize;

		// next 4 bytes are for the checksum
		byte[] checksumBytes = intToByteArray(checksum);
		buffer[6] = checksumBytes[0];
		buffer[7] = checksumBytes[1];
		buffer[8] = checksumBytes[2];
		buffer[9] = checksumBytes[3];
		
		buffer[10] = (byte)transferMode;
		buffer[11] = (byte)openMode;
		System.arraycopy(filenameBytes, 0, buffer, 12, filenameBytes.length);		
		eobrPacket.setData(buffer);
		eobrPacket.setLen((byte)buffer.length);
		
		Eobr_Comm_Response_Packet commResponsePacket = new Eobr_Comm_Response_Packet();
		errorCode = EobrCommunications.Companion.validateWithData(_threadMgr, eobrPacket, cmdId,
				this.GetActiveDeviceCrc(), VerifyRxPacketResponseByte.EobrBytePacket,
				commResponsePacket, false, socketVerifier);
    	
    	answer = new Bundle();
    	answer.putInt(Constants.RETURNCODE, errorCode);
    	
    	if (errorCode == EobrReturnCode.S_SUCCESS) {
    		ByteBuffer respBuffer;
    		EobrStringPacket eobrResponsePacket = EobrStringPacket.build(commResponsePacket.getResponse());
    		byte[] responseBytes = eobrResponsePacket.getStringVal();
    		
    		// Get status - 1 byte
    		int eobrStatus = (int)responseBytes[0];
			// Get content length - 4 bytes
           	int eobrContentLength = LittleEndianHelper.Companion.getInt(responseBytes, 1, 4);
           	// Get checksum - 4 bytes
            int eobrChecksum = LittleEndianHelper.Companion.getInt(responseBytes, 5, 4);
           	
           	// TODO: Change these to use R values, not hardcoded strings
        	answer.putInt("status", eobrStatus);
        	answer.putInt("contentLength", eobrContentLength);
        	answer.putInt("checksum", eobrChecksum);
    	}    
    
    	return answer;    	   	    	
	}

	public int GetEventData(EventRecord eventData, StatusRecordQueryMethodEnum queryMethod, int recordId, long timeCode, EventTypeEnum eventType, boolean resetReferenceTimestampToCurrent, int eventMask)
	{
		int cmdId = EUCMDType.EUCMD_GET_EVENT_DATA;
		int retVal;

		Eobr_Data_Packet_GenII eobrEventPacket = new Eobr_Data_Packet_GenII();
		eobrEventPacket.setCmd((byte) cmdId);
		eobrEventPacket.setCrc( this.GetActiveDeviceCrc());

		// If eventMask is supplied, then we will vary how we format the command
		if (eventMask >= 0) {
			eobrEventPacket.setLen((byte) 19);
			eobrEventPacket.setEventType((byte) EventTypeEnum.ANYTYPE);
			eobrEventPacket.setEventMask(eventMask);
		}
		else {
			eobrEventPacket.setLen((byte) 15);
			eobrEventPacket.setEventType((byte) eventType.getValue());
		}

		if (queryMethod.getValue() == StatusRecordQueryMethodEnum.RECORDID) {
			eobrEventPacket.setMethod((byte) RECORD_ID_TYPE);
			eobrEventPacket.setRecordId(recordId);
			eobrEventPacket.setTimecode(0);
		}
		else {
			eobrEventPacket.setMethod((byte) TIMESTAMP_TYPE);
			eobrEventPacket.setRecordId(0);
			eobrEventPacket.setTimecode(timeCode);
		}

		eobrEventPacket.setRefTimestampOption((byte)(resetReferenceTimestampToCurrent?1:0));

    	Eobr_Comm_Response_Packet commResponsePacket = new Eobr_Comm_Response_Packet();
		retVal = EobrCommunications.Companion.validate(_threadMgr, eobrEventPacket, cmdId,
				this.GetActiveDeviceCrc(), commResponsePacket, socketVerifier);
   		
   		if (retVal == EobrReturnCode.S_SUCCESS) {
   			byte[] response = commResponsePacket.getResponse();

			eventData.setRecordId(LittleEndianHelper.Companion.getInt(response, 2, 4));
			// timestamp stored in TAB as # of milliseconds since 1/1/1970
			eventData.setTimecode(LittleEndianHelper.Companion.getLong(response, 6, 8));
			eventData.setEventType(response[14]);
			eventData.setEventData(LittleEndianHelper.Companion.getInt(response, 15, 4));
			eventData.setDriverId(LittleEndianHelper.Companion.getInt(response, 19, 4));
			eventData.setEobrId(LittleEndianHelper.Companion.getInt(response, 23, 4));

   			// Get associated trip report data for Drive Start/End events.
			if(eventData != null && eventData.getEventData() > 0 && (eventData.getEventType() == EventTypeEnum.DRIVESTART || eventData.getEventType() == EventTypeEnum.DRIVEEND)) {
				TripReport tripData = new TripReport();
				int lookupRecordId = eventData.getEventData();

				retVal = this.GetTripData(tripData, new StatusRecordQueryMethodEnum(StatusRecordQueryMethodEnum.RECORDID), lookupRecordId, -1, false);
				this.PrepareForDataRecordingMalfunctionCheck(lookupRecordId, tripData.getRecordId());
				eventData.setTripReportData(tripData);
			}

			// Get associated trip report data for Ignition On/Off events.
			if (isIgnitionTripReportSupported) {
				if(eventData != null && eventData.getEventData() > 0 && (eventData.getEventType() == EventTypeEnum.IGNITIONON || eventData.getEventType() == EventTypeEnum.IGNITIONOFF)) {
					TripReport tripData = new TripReport();
					int lookupRecordId = eventData.getEventData();

					retVal = this.GetTripData(tripData, new StatusRecordQueryMethodEnum(StatusRecordQueryMethodEnum.RECORDID), lookupRecordId, -1, false);
					this.PrepareForDataRecordingMalfunctionCheck(lookupRecordId, tripData.getRecordId());
					eventData.setTripReportData(tripData);
				}
			}
		}

		return retVal;
	}

	private void PrepareForDataRecordingMalfunctionCheck(int queryRecordId, int resultRecordId){
		if (isEldMandateMode) {
			Integer statusBufferNumberOfTrips = null;

			// If the lookup failed for a valid queryRecordId, then we need to go get the number of trips from the status buffer to help determine if a malfunction should be created or not
			if (queryRecordId != 0 && resultRecordId <= 0) {
				StatusBuffer sb = this.GetStatusBuffer().getData();
				statusBufferNumberOfTrips = sb.getNumberOfTrips();
			}
			MalfunctionManager.getInstance().checkDataRecordingMalfunction(queryRecordId, resultRecordId, statusBufferNumberOfTrips);
		}
	}

	public int GetEventData(EventRecord eventData, StatusRecordQueryMethodEnum queryMethod, int recordId, long timeCode, EventTypeEnum eventType, boolean resetReferenceTimestampToCurrent)
	{
		return GetEventData(eventData, queryMethod, recordId, timeCode, eventType, resetReferenceTimestampToCurrent, -1);
	}
	
	public int GetTripData(TripReport eventData, StatusRecordQueryMethodEnum queryMethod, int recordId, long timeCode, boolean resetReferenceTimestampToCurrent)
	{
		int cmdId = EUCMDType.EUCMD_GET_TRIP_REPORT;

		Eobr_Data_Packet_GenII eobrEventPacket = new Eobr_Data_Packet_GenII();
		eobrEventPacket.setCmd((byte) cmdId);
		eobrEventPacket.setLen((byte) 14);
		eobrEventPacket.setCrc(this.GetActiveDeviceCrc());
		if (queryMethod.getValue() == StatusRecordQueryMethodEnum.RECORDID)
			eobrEventPacket.setMethod((byte) RECORD_ID_TYPE);
		else
			eobrEventPacket.setMethod((byte) TIMESTAMP_TYPE);
		eobrEventPacket.setRecordId(recordId);
		eobrEventPacket.setRefTimestampOption((byte)(resetReferenceTimestampToCurrent?1:0));

    	// time code only provided for historical data or next motion change data
    	if (timeCode > 0)
    	{
    		eobrEventPacket.setTimecode(timeCode);
    	}
    	
    	Eobr_Comm_Response_Packet commResponsePacket = new Eobr_Comm_Response_Packet();
		int retVal = EobrCommunications.Companion.validate(_threadMgr, eobrEventPacket, cmdId,
				this.GetActiveDeviceCrc(), commResponsePacket, socketVerifier);
   		
   		if (retVal == EobrReturnCode.S_SUCCESS) {
   		    ProcessDataHelper.Companion.processTripReportDataResponse(commResponsePacket.getResponse(), eventData);
   		}else{
			Log.e("BTGenII", "Error Reading Trip Data!");
		}

		return retVal;
	}

	public int GetDriverEvent(EventRecord eventData, long startTimeCode, long endTimeCode, int eventMask, boolean includeEventsWithoutDriverId)
	{
		int cmdId = EUCMDType.EUCMD_GET_DRIVER_EVENT;
		int retVal;

		Eobr_Driver_Event_Packet eobrDriverPacket = new Eobr_Driver_Event_Packet();
		eobrDriverPacket.setCmd((byte) cmdId);
		eobrDriverPacket.setCrc(this.GetActiveDeviceCrc());
		eobrDriverPacket.setLen((byte) 21);
		eobrDriverPacket.setEventMask(eventMask);
		eobrDriverPacket.setStartTimeCode(startTimeCode);
		eobrDriverPacket.setEndTimeCode(endTimeCode);
		if (includeEventsWithoutDriverId) {
			eobrDriverPacket.setSearchMethod((byte) 1);
		} else {
			eobrDriverPacket.setSearchMethod((byte) 0);
		}

		Eobr_Comm_Response_Packet commResponsePacket = new Eobr_Comm_Response_Packet();
		retVal =
				EobrCommunications.Companion.sendAndConfirmDriverEvent(_threadMgr,
				eobrDriverPacket,
						cmdId, this.GetActiveDeviceCrc(), commResponsePacket, socketVerifier);

		if (retVal == EobrReturnCode.S_SUCCESS) {
			byte[] response = commResponsePacket.getResponse();

			// Check the "Status" included in the command response
			if (LittleEndianHelper.Companion.getInt(response, 2, 1) == 1) retVal = EobrReturnCode.S_GENERAL_ERROR;

			eventData.setRecordId(LittleEndianHelper.Companion.getInt(response, 3, 4));
			// timestamp stored in TAB as # of milliseconds since 1/1/1970
			eventData.setTimecode(LittleEndianHelper.Companion.getLong(response, 7, 8));
			eventData.setEventType(response[15]);
			eventData.setEventData(LittleEndianHelper.Companion.getInt(response, 16, 4));
			eventData.setDriverId(LittleEndianHelper.Companion.getInt(response, 20, 4));
			eventData.setEobrId(LittleEndianHelper.Companion.getInt(response, 24, 4));
		}

		return retVal;
	}

	public Bundle GetDriverCount(long startTimeCode, long endTimeCode)
	{
		int cmdId = EUCMDType.EUCMD_GET_DRIVER_COUNT;
		int returnCode;
		int eventCount = 0;
		int[] driverIds = new int[16];

		Eobr_Driver_Event_Packet eobrDriverPacket = new Eobr_Driver_Event_Packet();
		eobrDriverPacket.setCmd((byte) cmdId);
		eobrDriverPacket.setCrc(this.GetActiveDeviceCrc());
		eobrDriverPacket.setLen((byte) 16);
		eobrDriverPacket.setStartTimeCode(startTimeCode);
		eobrDriverPacket.setEndTimeCode(endTimeCode);

		Eobr_Comm_Response_Packet commResponsePacket = new Eobr_Comm_Response_Packet();
		returnCode =
				EobrCommunications.Companion.sendAndConfirmDriverEvent(_threadMgr,
				eobrDriverPacket,
						cmdId, this.GetActiveDeviceCrc(), commResponsePacket, socketVerifier);

		if (returnCode == EobrReturnCode.S_SUCCESS) {
			byte[] response = commResponsePacket.getResponse();

			// Check the "Status" included in the command response
			if (LittleEndianHelper.Companion.getInt(response, 2, 1) == 1) returnCode = EobrReturnCode.S_GENERAL_ERROR;
			eventCount = LittleEndianHelper.Companion.getShort(response, 3, 2);

			short endOfArrayMarker = -1;
			int tempVal;
			for (int i=0; i < 16; i++)
			{
				tempVal = LittleEndianHelper.Companion.getInt(response, 5 + (i*4), 4);
				driverIds[i] = (tempVal == endOfArrayMarker ? 0 : tempVal);
			}
		}

		Bundle answer = new Bundle();
		answer.putInt(Constants.RETURNCODE, returnCode);
		answer.putIntArray(Constants.DRIVERIDS, driverIds);
		answer.putInt(Constants.RETURNVALUE, eventCount);

		return answer;
	}

	public int GetHistogramData(HistogramData histogramData, StatusRecordQueryMethodEnum queryMethod, int recordId, long timeCode, HistogramTypeEnum histogramType, boolean setRefTime)
    {
    	int cmdId = EUCMDType.EUCMD_GET_HISTOGRAM;
    	int retVal;
   	
    	Eobr_Data_Packet_GenII eobrHistogramReportPacket = new Eobr_Data_Packet_GenII();
    	eobrHistogramReportPacket.setCmd((byte)cmdId);
    	eobrHistogramReportPacket.setLen((byte)15);
    	eobrHistogramReportPacket.setCrc(this.GetActiveDeviceCrc());
    	
    	if(queryMethod.getValue() == StatusRecordQueryMethodEnum.RECORDID)
    		eobrHistogramReportPacket.setMethod((byte)RECORD_ID_TYPE);
    	else
    		eobrHistogramReportPacket.setMethod((byte)TIMESTAMP_TYPE);
    	
    	eobrHistogramReportPacket.setRecordId(recordId);	
    	eobrHistogramReportPacket.setRefTimestampOption((byte)(setRefTime == true ? 1 : 0));
    	eobrHistogramReportPacket.setEventType((byte)histogramType.getValue());
    	
    	// time code only provided if retrieving data by timestamp
    	if (timeCode > 0)
    	{
    		eobrHistogramReportPacket.setTimecode(timeCode);
    	}
    	
    	Eobr_Comm_Response_Packet commResponsePacket = new Eobr_Comm_Response_Packet();
		retVal = EobrCommunications.Companion.validate(_threadMgr, eobrHistogramReportPacket,
					cmdId, this.GetActiveDeviceCrc(), commResponsePacket, socketVerifier);
    	
    	if(retVal == EobrReturnCode.S_SUCCESS) {
    		ProcessDataHelper.Companion.processHistogramDataResponse(commResponsePacket.getResponse(), histogramData);
    	}

    	return retVal;
    }
	
    public int GetJBusDiagnosticDataFromDevice(JbusDiagnosticData diagnosticData, StatusRecordQueryMethodEnum queryMethod, int recordId, long timestamp, boolean setRefTime)
	{
    	int cmdId = EUCMDType.EUCMD_GET_JBUS_DIAG_DATA;
    	int retVal;
    	
    	// Create the command packet  
    	Eobr_Data_Packet_GenII eobrDataPacket = new Eobr_Data_Packet_GenII();
    	eobrDataPacket.setCmd((byte)cmdId);
    	eobrDataPacket.setLen((byte)14);
    	eobrDataPacket.setCrc(this.GetActiveDeviceCrc());
    	eobrDataPacket.setRecordId(recordId);
    	eobrDataPacket.setRefTimestampOption((byte)(setRefTime == true ? 1 : 0));
    	
    	if(queryMethod.getValue() == StatusRecordQueryMethodEnum.RECORDID)
    		eobrDataPacket.setMethod((byte)RECORD_ID_TYPE);
    	else
    		eobrDataPacket.setMethod((byte)TIMESTAMP_TYPE);
    	
    	if (timestamp > 0)
    	{
    		eobrDataPacket.setTimecode(timestamp);
    	}
    	    	    	
   		Eobr_Comm_Response_Packet commResponsePacket = new Eobr_Comm_Response_Packet();
		retVal = EobrCommunications.Companion.validate(_threadMgr, eobrDataPacket, cmdId,
				this.GetActiveDeviceCrc(), commResponsePacket, socketVerifier);
		
    	if(retVal == EobrReturnCode.S_SUCCESS) {
     		EobrJbusDiagDataPacket responsePacket = new EobrJbusDiagDataPacket(commResponsePacket.getResponse());
     													//this.PopulateHistoricalJBusDiagDataPacket(commResponsePacket.getResponse());

     		// Fill in the results
     		diagnosticData.setAssociatedEobrRecordId(responsePacket.getAssociatedEobrRecordId());
     		diagnosticData.setTimestamp(new Date(responsePacket.getTimecode()));
     		diagnosticData.setRecordId(responsePacket.getRecordId());
 			
 			ArrayList<DTCInformation> dtcList = new ArrayList<DTCInformation>();
 			EobrJbusDiagDataDtcInfo[] responseDTCList = responsePacket.getDtcList();
 			for (EobrJbusDiagDataDtcInfo dtcInfo : responseDTCList)
 			{
 				DTCInformation dtc = new DTCInformation();
 				dtc.setType(dtcInfo.getType());
 				dtc.setSource(dtcInfo.getSource());
 				dtc.setDtc(dtcInfo.getDTC());
 				dtcList.add(dtc);
 			}
 			diagnosticData.setDTCList(dtcList);
    	}
    	
    	return retVal;
	}
    
    public int GetEobrGeneration()
    {
    	return 2;
    }

	public void ClearAllEobrData(){							
		clearVehicleSpeedHistogramData();
		clearEngineSpeedHistogramData();
		clearEngineLoadHistogramData();
		clearGpsDopHistogramData();
		clearGpsSecondsToFirstFixHistogramData();
		clearAllRecordData(); 
		clearStatusBufferData();	
	}

	private void clearAllRecordData(){
		int rc = ClearAllRecordData(0x00);
		EobrException eobrException = EobrException.getEobrExceptionFromEobrReturnCodeValue(rc);
		if(eobrException != null)
			throw eobrException;
	}

	private void clearGpsSecondsToFirstFixHistogramData(){		
		clearHistogramData(GenIIBase.HistogramType.TAB_HIST_GPS_SECS);
	}

	private void clearGpsDopHistogramData(){
		clearHistogramData(GenIIBase.HistogramType.TAB_HIST_GPS_DOP);
	}

	private void clearEngineLoadHistogramData(){
		
		clearHistogramData(GenIIBase.HistogramType.TAB_HIST_LOAD);
	}

	private void clearEngineSpeedHistogramData(){
		clearHistogramData(GenIIBase.HistogramType.TAB_HIST_RPM);
	}

	private void clearVehicleSpeedHistogramData(){		  
		clearHistogramData(GenIIBase.HistogramType.TAB_HIST_VSS);
	}

	public Bundle GetEobrHardware() {
	
		int errorCode = EobrReturnCode.S_FUNC_NOT_IMPLEMENTED;
		int cmdId = EUCMDType.EUCMD_GET_EOBR_HARDWARE;
		Bundle answer = new Bundle();
		boolean isJJK = false;

		// 2014.09.17 sjn - The Get_Eobr_Hardware command is not implemented on all Gen 2 ELDs.
		//                  Verify that the command is supported before sending it.
		//                  The IsCommandSupported design is also not implemented universally yet.
		//                  If the IsCommandSupported=false then read the firmware version and only send the command on BTE firmware   
		//                  The issue is that the current JJK Gen2 firmware in prod (6.60) does not support this command 
		//                  and there are many attempts to send this command. Each time it is sent to 6.60, it fails.   
		//                  This is a workaround to eliminate the failures form 6.60.	
		boolean sendCommand = this.IsCommandSupported(EUCMDType.EUCMD_GET_EOBR_HARDWARE);
		
		if(sendCommand == false){
			Bundle firmwareVersion = this.GetEOBRDllRevisions();
			if( firmwareVersion.getInt(Constants.RETURNCODE) == EobrReturnCode.S_SUCCESS
					&& firmwareVersion.containsKey(Constants.MAINFIRMWAREREVISION)
					&& firmwareVersion.getString(Constants.MAINFIRMWAREREVISION).startsWith("2."))
			{
				sendCommand = true;
			}
		}
		
		if(sendCommand)
		{
			// The EUCMD_GET_EOBR_HARDWARE command is implemented in the JJK ELD as of FW 6.70
			Eobr_Comm_Response_Packet commResponsePacket = new Eobr_Comm_Response_Packet();
			errorCode = EobrCommunications.Companion.validateNoData(_threadMgr, cmdId,
					this.GetActiveDeviceCrc(),
					VerifyRxPacketResponseByte.EobrStringPacket, commResponsePacket,
					socketVerifier);
	
			if (errorCode == EobrReturnCode.S_SUCCESS)
			{
				EobrStringPacket eobrHardwarePacket = EobrStringPacket.build(commResponsePacket.getResponse());
	
	    		/*This information is returned as a single variable-length, NULL-terminated, ASCII string
	    		  with multiple data fields, separated by hash marks (#).
	    		  
	    		  The returned data fields include the following:
	    			(1)	Hardware maker (e.g., "Networkfleet")
	    			(2)	Hardware model (e.g., "BTE")
	    			(3)	Hardware PCB version (e.g., "1.0")
	
	    			The following is a sample returned "Info" string:
	    			    "Networkfleet#BTE#1.0"
	    		*/
	    		
				// eobrHardware contains information about the EOBR device
				String eobrHardware = new String(eobrHardwarePacket.getStringVal());				
				if (eobrHardware.length() > 0)
				{
					String[] info = eobrHardware.split("#");
					String maker = info[0];
					// Not being used currently
					String model = info[1];
					// Not being used currently
					String version = info[2];

					answer.putString(EOBR_HARDWARE_MANUFACTURER, maker);
					answer.putString(EOBR_HARDWARE_MODEL, model);
					answer.putString(EOBR_HARDWARE_VERSION, version);

					if(maker.equalsIgnoreCase("JJKeller"))
						isJJK = true;
					else if(maker.equalsIgnoreCase("Networkfleet"))
						isJJK = false;
				}
			}	
		}
		
		answer.putInt(Constants.RETURNCODE, errorCode);
		answer.putBoolean(Constants.RETURNVALUE, isJJK);

		
		return answer;
	}
	
	public FirmwareUpgradeRequestResult RequestFirmwareUpgrade(long firmwarePatchId)
	{
		int cmdId = EUCMDType.EUCMD_GET_EOBR_UPGRADE_REQ;
		int returnCode;
		
		Eobr_Packet request = new Eobr_Packet();
		request.setCmd((byte)cmdId);
		request.setCrc(this.GetActiveDeviceCrc());
		request.setLen((byte)4);
		
		//the 32-bit patch ID is stored as a long because Java lacks unsigned types
		//and we needed to be able to treat it as its actual positive value.
		//but here the bits are just bits
		request.setData(intToByteArray((int)firmwarePatchId));
		
		Eobr_Comm_Response_Packet response = new Eobr_Comm_Response_Packet();
		returnCode = EobrCommunications.Companion.validateWithData(_threadMgr, request, cmdId,
				this.GetActiveDeviceCrc(), VerifyRxPacketResponseByte.EobrBytePacket,
				response, false, socketVerifier);
		
		FirmwareUpgradeRequestResult result = new FirmwareUpgradeRequestResult(returnCode);
		
		if(returnCode == EobrReturnCode.S_SUCCESS)
		{
			EobrBytePacket packet = new EobrBytePacket(response.getResponse());
			
			result.setStatus(packet.getByteVal());
		}
		
		return result;
	}
	
	public FirmwareUpgradeStatusResult GetFirmwareUpgradeStatus()
	{
		int cmdId = EUCMDType.EUCMD_GET_EOBR_UPGRADE_STATUS;
		int returnCode;
			
		Eobr_Comm_Response_Packet responsePacket = new Eobr_Comm_Response_Packet();
		returnCode = EobrCommunications.Companion.validateNoData(_threadMgr, cmdId,
				this.GetActiveDeviceCrc(),
				VerifyRxPacketResponseByte.EobrStringPacket, responsePacket, socketVerifier);
		
		FirmwareUpgradeStatusResult result = new FirmwareUpgradeStatusResult(returnCode);
		
		if(returnCode == EobrReturnCode.S_SUCCESS)
		{
    		result.setStagedFirmwarePatchId(LittleEndianHelper.Companion.getInt(responsePacket.getResponse(), 2, 4) & 0xFFFFFFFFL);
			result.setRequestedFirmwarePatchId(LittleEndianHelper.Companion.getInt(responsePacket.getResponse(), 6, 4) & 0xFFFFFFFFL);
    		result.setCurrentFirmwarePatchId(LittleEndianHelper.Companion.getInt(responsePacket.getResponse(), 10, 4) & 0xFFFFFFFFL);
		}
		
		return result;
	}
	
	@Override
    protected int InitializeEobr(String deviceName)
    {
    	int errorCode;
    	int cmdId = EUCMDType.EUCMD_INITIALIZE;

    	Eobr_Comm_Response_Packet commResponsePacket = new Eobr_Comm_Response_Packet();
		errorCode = EobrCommunications.Companion.validateNoData(_threadMgr, cmdId,
				this.GetActiveDeviceCrc(),
				VerifyRxPacketResponseByte.EobrIntegerPacket, commResponsePacket, socketVerifier);
    	
    	if (errorCode == EobrReturnCode.S_SUCCESS) {
    		int status;
    		byte[] response = commResponsePacket.getResponse();
    		
    		status = new EobrIntegerPacket(response).getIntegerVal();
    		
    		//current FW versions will respond to this command
    		//with a bitmask representing the commands it supports
    		if(response[1] == 36)
    			System.arraycopy(response, 6, currentEucmdBitmask, 0, 32); 
    		else   			
    			currentEucmdBitmask = EUCMD_BITMASK_DEFAULT;
    		
    		if (status != 0)
    			errorCode = EobrReturnCode.S_DEV_INTERNAL_ERROR;

			isIgnitionTripReportSupported = IsIgnitionTripReportSupported();
    	}    	
    	
    	return errorCode;
    }

	public EobrResponse<DriveData> GetDriveData(DriveDataTypeEnum typeEnum, long timeCode, short timeStep, short maxUncertainty) {
    	int errorCode;
    	int dataSize = 15;
    	short minStop = 0; //set to 0 to disable period merging in the ELD
    	int cmdId = EUCMDType.EUCMD_GET_DRIVE_DATA;
    	
    	Eobr_Packet eobrPacket = new Eobr_Packet();
		eobrPacket.setCmd((byte)cmdId);
		eobrPacket.setCrc(this.GetActiveDeviceCrc());
		eobrPacket.setLen((byte)dataSize);
		
		ByteBuffer data = ByteBuffer.allocate(dataSize);
		data.order(ByteOrder.LITTLE_ENDIAN);
		data.put((byte)(typeEnum == DriveDataTypeEnum.DRIVEPERIOD ? 0 : 1));
		data.putLong(timeCode);
		data.putShort(timeStep);
		data.putShort(minStop);
		data.putShort(maxUncertainty);
		
		byte[] bytes = new byte[dataSize];
		data.position(0);
		data.get(bytes);
		eobrPacket.setData(bytes);

		Eobr_Comm_Response_Packet commResponsePacket = new Eobr_Comm_Response_Packet();
		// extendBarrierTime indicates to use 15s threshold for send command with data
		errorCode = EobrCommunications.Companion.validateWithData(_threadMgr, eobrPacket, cmdId,
				this.GetActiveDeviceCrc(), VerifyRxPacketResponseByte.EobrCustomParmPacket,
				commResponsePacket, true, socketVerifier);
    	
    	EobrResponse<DriveData> result = new EobrResponse<DriveData>(errorCode);
    	
    	if(result.getReturnCode() == EobrReturnCode.S_SUCCESS)
    	{
    		byte[] response = commResponsePacket.getResponse();

    		ByteBuffer payload = ByteBuffer.wrap(response, 2, response[1] & 0xFF);
    		
    		DriveData driveData = DriveData.FromByteBuffer(payload);
    		result.setData(driveData);
    	}
    	    	
    	return result;  
	}

	public int SetReferenceTimestamps(EobrReferenceTimestamps timestamps){
    	int errorCode;
    	int dataSize = 40;
    	int cmdId = EUCMDType.EUCMD_SET_REF_TIMESTAMP;
    	
    	Eobr_Packet eobrPacket = new Eobr_Packet();
		eobrPacket.setCmd((byte)cmdId);
		eobrPacket.setCrc(this.GetActiveDeviceCrc());
		eobrPacket.setLen((byte)dataSize);
		
		ByteBuffer data = ByteBuffer.allocate(dataSize);
		data.order(ByteOrder.LITTLE_ENDIAN);
		data.putLong(timestamps.getEobrReferenceTime());
		data.putLong(timestamps.getEventReferenceTime());
		data.putLong(timestamps.getHistogramReferenceTime());
		data.putLong(timestamps.getTripReferenceTime());
		data.putLong(timestamps.getDtcReferenceTime());
		
		byte[] bytes = new byte[dataSize];
		data.position(0);
		data.get(bytes);
		
		eobrPacket.setData(bytes);

		Eobr_Comm_Response_Packet commResponsePacket = new Eobr_Comm_Response_Packet();
		errorCode = EobrCommunications.Companion.validateWithData(_threadMgr, eobrPacket, cmdId,
				this.GetActiveDeviceCrc(), VerifyRxPacketResponseByte.EobrBytePacket,
				commResponsePacket, false, socketVerifier);
    	
    	if(errorCode == EobrReturnCode.S_SUCCESS)
    	{
    		EobrBytePacket packet = new EobrBytePacket(commResponsePacket.getResponse());
    		
    		//0 is success
    		if(packet.getByteVal() != 0)
    			errorCode = EobrReturnCode.S_GENERAL_ERROR;
    	}
    	    	
    	return errorCode;  
	}

	public boolean IsGetDriveDataSupported()
	{
		return IsCommandSupported(EUCMDType.EUCMD_GET_DRIVE_DATA);
	}

	public boolean IsGetEventDataEventMaskSupported() {
		boolean result = false;
		// We don't currently support this on the BTE (Gen2 & !isJJK)
		if (IsJJK(GlobalState.getInstance().getApplicationContext())) {
			// Starting in 6.89.5, the TAB became capable of accepting the "EventMask"
			// in the "Cmd_EUCMD_GET_EVENT_DATA" command for the mandate.
			// the mandate firmware version was set to 6.88.110 (which corresponds to 6.89.34)
			result = this.IsFirmwareVersionSupported(6, 88, 110);
		}
		return result;
	}


	public boolean IsIgnitionTripReportSupported() {
		boolean result = false;
		// We don't currently support this on the BTE (Gen2 & !isJJK)
		if (IsJJK(GlobalState.getInstance().getApplicationContext())) {
			// Starting in 6.89.14, the TAB associated trip records to ignition on events
			// the mandate firmware version was set to 6.88.110 (which corresponds to 6.89.34)
			result = this.IsFirmwareVersionSupported(6, 88, 110);
		}

		return result;
	}

	public boolean IsJJK(Context ctx) {
		boolean isJJK = true;
		Bundle bundle = GetEobrHardware();
		if (bundle != null)
		{
			int status = bundle.getInt(ctx.getString(R.string.rc));

			// if return code is successful, then the ELD is NOT a JJK ELD
			if (status == EobrReturnCode.S_SUCCESS && bundle.containsKey(ctx.getString(R.string.returnvalue)))
				isJJK = bundle.getBoolean(ctx.getString(R.string.returnvalue));
		}

		return isJJK;
	}


	public boolean IsCommandSupported(int command) {
		int valid = 0;
		
		if(command < 256)
		{
			byte b = currentEucmdBitmask[command / 8];
			
			valid = (b >> (7 - (command & 7))) & 1;
		}
		
		return valid > 0;
	}
	
	public int SetIsEldMandate(boolean isEldMandate)
	{
		isEldMandateMode = isEldMandate;
		return SetCustomParameter(isEldMandate ? 1 : 0, CUSTOM_PARAMETER_ELDMANDATE);
	}

	public int SetDisableReadEldVin(boolean isEldReadingVin)
	{
		int answer = EobrReturnCode.S_GENERAL_ERROR;

        // first we need to read the current parameter value
        Bundle bundle = this.GetCustomParameter(CUSTOM_PARAMETER_ELDREADVIN);

		if (bundle != null && bundle.containsKey(Constants.RETURNCODE)) {
			int rc = bundle.getInt(Constants.RETURNCODE);
			if (rc == EobrReturnCode.S_SUCCESS) {
				// now we need to set the bit correctly
				int parmValue = bundle.getInt(Constants.RETURNVALUE);
				if (isEldReadingVin)
					// turn on the bit
					parmValue = parmValue | FLAG_CUSTPARM10_ReadVehicleVin;
				else
					// turn off the bit
					parmValue = parmValue & ~FLAG_CUSTPARM10_ReadVehicleVin;

				answer = this.SetCustomParameter(parmValue, CUSTOM_PARAMETER_ELDREADVIN);
			}
		}

		return answer;
	}

	public Bundle GetDisableReadEldVin()
	{
		Bundle answer = new Bundle();

        // setup the answer with default values
        answer.putBoolean(Constants.RETURNVALUE, false);
        answer.putInt(Constants.RETURNCODE, EobrReturnCode.S_DEV_INTERNAL_ERROR);

        Bundle bundle = this.GetCustomParameter(CUSTOM_PARAMETER_ELDREADVIN);

		if (bundle != null && bundle.containsKey(Constants.RETURNCODE)) {
			int rc = bundle.getInt(Constants.RETURNCODE);
			answer.putInt(Constants.RETURNCODE, rc);
			if (rc == EobrReturnCode.S_SUCCESS) {
				int parmValue = bundle.getInt(Constants.RETURNVALUE);
				if (0 != (parmValue & FLAG_CUSTPARM10_ReadVehicleVin)) {
					// the bit is turned on, so the return value is true
					// THIS MEANS READING OF VIN IS DISABLED!!!!!
					answer.putBoolean(Constants.RETURNVALUE, true);
				}
			}
		}

		return answer;
	}

	@Override
	public EobrResponse<StatusBuffer> GetStatusBuffer() {
		int cmdId = EUCMDType.EUCMD_GET_STATUS_BUFFER;

		Eobr_Packet eobrPacket = new Eobr_Packet();
		eobrPacket.setCmd((byte) cmdId);
		eobrPacket.setCrc(this.GetActiveDeviceCrc());
		eobrPacket.setLen((byte) 0);

		Eobr_Comm_Response_Packet commResponsePacket = new Eobr_Comm_Response_Packet();
		int errorCode = EobrCommunications.Companion.validateWithData(_threadMgr, eobrPacket,
				cmdId, this.GetActiveDeviceCrc(), VerifyRxPacketResponseByte.EobrCustomParmPacket,
				commResponsePacket, false, socketVerifier);

		EobrResponse<StatusBuffer> result = new EobrResponse<>(errorCode);
		if (result.getReturnCode() == EobrReturnCode.S_SUCCESS) {
			byte[] response = commResponsePacket.getResponse();
			int responsePayloadLength = response[1] & 0xFF;
			ByteBuffer payload = ByteBuffer.wrap(response, 2, responsePayloadLength);

			StatusBuffer statusBuffer = StatusBuffer.FromByteBuffer(payload);
			result.setData(statusBuffer);
		}
		return result;
	}

}
