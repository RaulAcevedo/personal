package com.jjkeller.kmbapi.eobrengine.eobrreader;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.util.Log;

import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.controller.utility.ErrorLogHelper;
import com.jjkeller.kmbapi.eobrengine.CalcCRC;
import com.jjkeller.kmbapi.eobrengine.CommThreadManager;
import com.jjkeller.kmbapi.eobrengine.EUCMDType;
import com.jjkeller.kmbapi.eobrengine.EobrCommunications;
import com.jjkeller.kmbapi.eobrengine.EobrEngineBase;
import com.jjkeller.kmbapi.eobrengine.eobrdatatypes.Eobr_Comm_Response_Packet;
import com.jjkeller.kmbapi.eobrengine.eobrdatatypes.EobrCustomParmPacket;
import com.jjkeller.kmbapi.eobrengine.eobrdatatypes.EobrIntegerPacket;
import com.jjkeller.kmbapi.eobrengine.eobrdatatypes.Eobr_Packet;
import com.jjkeller.kmbapi.eobrengine.eobrdatatypes.EobrStringPacket;
import com.jjkeller.kmbapi.eobrengine.ReceivedPacketHelper.VerifyRxPacketResponseByte;
import com.jjkeller.kmbapi.kmbeobr.Constants;
import com.jjkeller.kmbapi.kmbeobr.Enums.EobrReturnCode;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AndroidBTBase extends EobrEngineBase {

	protected BluetoothAdapter _btAdapter;
	protected BluetoothSocket _localSocket = null;
	protected boolean _isSocketConnected = false;
	protected BluetoothReader bluetoothReader = null;
	
	// Do not use the following 4 properties for anything else except sharing the bluetooth connection information with the 
	// Testharness.  These are only being exposed here so that the TestHarness application can share the Bluetooth information.
	// Currently, the Testharness application communicates with the EOBR via KMBAPI methods and via its own "internal" methods.
	// Because of this, we need to be able to share bluetooth connection information.  For example, the Testharness initially 
	// establishes EOBR communications using the KMBAPI methods.  Although some of the test cases within the Testharness will 
	// continue to use the KMBAPI communication methods to communicate with the EOBR, there are several test cases within the 
	// Testharness that will use its own "internal" methods to communicate with the EOBR.  Because of this, we need to "share", 
	// or continue to use the bluetooth connection information originally established.  If we don't, we run into several errors 
	// and issues. This solution was discussed with Jim M. in June, 2013 and was accepted as the best alternative at this point.
	public BluetoothAdapter getBluetoothAdapter() {
		return _btAdapter;
	}
	
	public BluetoothSocket getBlueToothSocket() {
		return _localSocket; 
	}

	@Override
	public String getCurrentBtAddress() {
		return this.GetActiveDeviceAddress();
	}
	
	public boolean getIsSocketConnected() {
		return _isSocketConnected;
	}

	protected static final String BT_DATA_PREFIX = "##";

	// UUID needed for creating socket in Gingerbread and newer (API 10)
	protected static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	protected abstract BluetoothSocket OpenBluetoothSocket(CommThreadManager ctMgr, BluetoothDevice device);
	protected abstract BluetoothSocket SetupAndOpenBluetoothSocket(CommThreadManager ctMgr, BluetoothAdapter btAdapter, String btAddress);

    protected boolean OpenSocketForDeviceName(String deviceName)
    {
    	boolean bStatus = false;

    	AvailableBtEobrSearch deviceSearch = this.FindAvailableBtEobr(deviceName, null);
    	if (deviceSearch != null) {
    		this.SetupActiveDevice(deviceSearch);

			if (_btAdapter == null)
				_btAdapter = BluetoothAdapter.getDefaultAdapter();

			// if _localSocket is already setup, check if already connected to device we're looking for
			if (_localSocket != null) {
				// if connected to device we're looking for, we're good
				if (_localSocket.getRemoteDevice().getAddress().equals(this.GetActiveDeviceAddress())) {
					bStatus = true;
					_isSocketConnected = true;
				}
				// else, (don't know that this can happen) connected to a different device - close the socket so we can create new socket
				// for the current device we are looking for
				else {
					try {
						_localSocket.close();
					} catch (IOException e) {

						Log.e("UnhandledCatch", e.getMessage() + ": " + Log.getStackTraceString(e));
					}

					_isSocketConnected = false;
					_localSocket = null;
				}
			}

			if (!_isSocketConnected) {
				_localSocket = SetupAndOpenBluetoothSocket(getCommThreadManager(), _btAdapter, this.GetActiveDeviceAddress());

				if (_localSocket != null) {
					bStatus = true;
					_isSocketConnected = true;
				} else {
					bStatus = false;
					_isSocketConnected = false;
				}
			}
		}
    	
    	return bStatus;
    }
	
	CommThreadManager _threadMgr = new CommThreadManager(this);
	public CommThreadManager getCommThreadManager() { return _threadMgr; }
		
    public Bundle GetEobrSerialNumber()
    {
    	int cmdId = EUCMDType.EUCMD_GET_SERIAL_NUMBER;

		Bundle answer = EobrCommunications.Companion.processResponse(_threadMgr, cmdId,
				this.GetActiveDeviceCrc(),
				socketVerifier);
    	
    	return answer;
    }

    public Bundle GetUnitId()
    {
    	int cmdId = EUCMDType.EUCMD_GET_UNIT_ID;

		Bundle answer = EobrCommunications.Companion.processResponse(_threadMgr, cmdId,
				this.GetActiveDeviceCrc(),
				socketVerifier);
    	
    	return answer;
    }

    public int SetUnitId(String unitId)
    {
    	int errorCode;
    	int dataSize = unitId.length();
    	int cmdId = EUCMDType.EUCMD_SET_UNIT_ID;
    	
    	Eobr_Packet eobrPacket = new Eobr_Packet();
		eobrPacket.setCmd((byte)cmdId);
		eobrPacket.setCrc(this.GetActiveDeviceCrc());
		eobrPacket.setLen((byte)dataSize);

		byte[] packetData = new byte[dataSize];
    	for (int i=0; i<dataSize; i++)
    		packetData[i] = (byte) unitId.charAt(i);
    	
    	eobrPacket.setData(packetData);
    	
    	
    	Eobr_Comm_Response_Packet commResponsePacket = new Eobr_Comm_Response_Packet();
        errorCode = EobrCommunications.Companion.validateWithData(_threadMgr, eobrPacket, cmdId,
                this.GetActiveDeviceCrc(), VerifyRxPacketResponseByte.EobrNullPacket,
                commResponsePacket, false, socketVerifier);
    	
    	return errorCode;    	
    }

    public int OpenDevice(String deviceName)    
    {
    	int returnCode = EobrReturnCode.S_DEV_NOT_CONNECTED;
    	boolean bStatus = false;
    	
    	if (this.HasAvailableBtEobrDevices())
    	{
    		bStatus = OpenSocketForDeviceName(deviceName);
    		
    		if (bStatus)
    		{
    			// after creating socket, wait for a tenth of a second
    			// before sending command to eobr
				try {Thread.sleep(500);} catch (InterruptedException e) {
					
		        	Log.e("UnhandledCatch", e.getMessage() + ": " + Log.getStackTraceString(e));
				}
				// Attempt to recover from missing a valid Crc on the Active device
				if (!this.IsActiveDeviceCrcValid()) {
					Bundle bundle = this.GetEobrSerialNumber();
					if (bundle.getInt(Constants.RETURNCODE) == EobrReturnCode.S_SUCCESS) {
						String serialNumber = bundle.getString(Constants.RETURNVALUE);
						short crcValue = (short) CalcCRC.Calculate(serialNumber, serialNumber.length());
						this.GetActiveDevice().setCrc(crcValue);
						String msg = String.format("Invalid CRC detected before opening/initializing a device [AndroidBTBase.OpenDevice(name)].  Successfully recalculated and changed CRC to %d.", this.GetActiveDeviceCrc());
						ErrorLogHelper.RecordMessage(msg);
					}
				}

				returnCode = this.InitializeEobr(deviceName);
    		}
    		else
    		{
    			returnCode = EobrReturnCode.S_DEV_NOT_CONNECTED; 
    		}
    	}
    	else
    	{
    		returnCode = EobrReturnCode.S_DEV_NOT_CONNECTED;
    	}
    	
    	return returnCode;
    }

    public int CloseDevice()
    {
    	int retVal = EobrReturnCode.S_SUCCESS;
    	
    	if (_localSocket != null)
    	{
    		try {
    			_isSocketConnected = false;

				try { _localSocket.getInputStream().close(); } catch (Exception ignored) {}
				try { _localSocket.getOutputStream().close(); } catch (Exception ignored) {}
				_localSocket.close();
				_localSocket = null;
				
				Thread.sleep(5000);
			} catch (IOException e) {
				retVal = EobrReturnCode.S_GENERAL_ERROR;
			} catch (InterruptedException e) {
				Log.e("UnhandledCatch", e.getMessage(), e);
			}
			finally
			{
				// Ensure these variables are cleared even if the close() method throws an exception.
    			_isSocketConnected = false;
				_localSocket = null;
			}
    	}
    	
    	return retVal;
    }

    protected int InitializeEobr(String deviceName)
    {
    	int errorCode;
    	int cmdId = EUCMDType.EUCMD_INITIALIZE;

    	Eobr_Comm_Response_Packet commResponsePacket = new Eobr_Comm_Response_Packet();
		errorCode = EobrCommunications.Companion.validateNoData(_threadMgr, cmdId,
				this.GetActiveDeviceCrc(),
				VerifyRxPacketResponseByte.EobrIntegerPacket, commResponsePacket, socketVerifier);
    	
    	if (errorCode == EobrReturnCode.S_SUCCESS) {
    		EobrIntegerPacket eobrInitializePacket = new EobrIntegerPacket(commResponsePacket.getResponse());
    		
    		if (eobrInitializePacket.getIntegerVal() != 0)
    			errorCode = EobrReturnCode.S_DEV_INTERNAL_ERROR;
    	}    	
    	
    	return errorCode;
    }
    
    public int deleteBTAddress()
    {
    	int retVal = EobrReturnCode.S_SUCCESS;
    	this.initializeConnectedDevices();

    	if (_localSocket != null)
    	{
    		try {
				_localSocket.close();
				_localSocket = null;
				_isSocketConnected = false;
			} catch (IOException e) {
				retVal = EobrReturnCode.S_GENERAL_ERROR;
			}
			finally{
				// Ensure these variables are cleared even if the close() method throws an exception.
				_localSocket = null;
				_isSocketConnected = false;
			}
    	}
    	
    	return retVal;
    }

    /*****************************************************************************
    ** DESCRIPTION:
    ** Sets the debug flags in the EOBRs. 
    **
    ** RETURNS:	S_SUCCESS/S_DEV_NOT_CONNECTED/S_UNKNOWN_ERROR
    **          
    */
    public int SetDebugFlags(int debugFlags)
    {
    	int errorCode;
    	int dataSize = 4;
        int cmdId = EUCMDType.EUCMD_SET_DEBUG_FLAGS;

        // Setup command packet
    	Eobr_Packet eobrPacket = new Eobr_Packet();
		eobrPacket.setCmd((byte)cmdId);
		eobrPacket.setCrc(this.GetActiveDeviceCrc());
		eobrPacket.setLen((byte)dataSize);

		// Arrange the byte into order before send out
		byte[] flagsBytes = new byte[4];
    	for (int i=0; i<4; i++)
    	{
    		flagsBytes[i] = (byte)(debugFlags & 0xFF);    		
    		debugFlags >>= 8;
    	}
        
    	eobrPacket.setData(flagsBytes);

    	
    	Eobr_Comm_Response_Packet commResponsePacket = new Eobr_Comm_Response_Packet();
        errorCode = EobrCommunications.Companion.validateWithData(_threadMgr, eobrPacket, cmdId,
                this.GetActiveDeviceCrc(), VerifyRxPacketResponseByte.EobrNullPacket,
                commResponsePacket, false, socketVerifier);
    	
        return errorCode;
    }
          
    
	protected static final byte[] stringToByteArray(String value) {
		
		int stringLength = value.length();
		byte[] result = new byte[stringLength];
		
		for (int i = 0; i < stringLength; i++)
			result[i] = (byte)value.charAt(i);
		
		return result;
		
	}    
    
    
/*    public int ShutDown()
    {
    	int errorCode;
    	int cmdId = EUCMD_SHUTDOWN;

    	Bundle bundle = _threadMgr.SendCommand(cmdId);

    	if (VerifyCommThreadReturnBundle(bundle))
    	{
        	byte[] response = bundle.getByteArray(COMMTHREAD_RESPONSE);
        	if (response != null)
        	{
        		errorCode = ReceiveStatusResponse(cmdId, response);
        	}
        	else
        	{
        		errorCode = DllErrorCode.S_DEV_NOT_CONNECTED;
        	}    		    		
    	}
    	else
    		errorCode = DllErrorCode.S_DEV_NOT_CONNECTED;
    	
    	return errorCode;
    }*/

    public Bundle GetCustomParameter(int customParameterIndex)
    {
    	int errorCode;
    	int customParameter = 0;
    	int cmdId = EUCMDType.EUCMD_GET_CUSTOM_PARAMETER;
    	
    	Eobr_Packet eobrPacket = new Eobr_Packet();
		eobrPacket.setCmd((byte)cmdId);
		eobrPacket.setCrc(this.GetActiveDeviceCrc());
		eobrPacket.setLen((byte)1);
		eobrPacket.setData(new byte[]{(byte)customParameterIndex});    	
		
		Eobr_Comm_Response_Packet commResponsePacket = new Eobr_Comm_Response_Packet();
        errorCode = EobrCommunications.Companion.validateWithData(_threadMgr, eobrPacket, cmdId,
                this.GetActiveDeviceCrc(), VerifyRxPacketResponseByte.EobrCustomParmPacket,
                commResponsePacket, false, socketVerifier);
    			
    	if (errorCode == EobrReturnCode.S_SUCCESS) {
    		EobrCustomParmPacket customParamPacket = new EobrCustomParmPacket(commResponsePacket.getResponse());
    		
    		if (customParamPacket.getStatus() == 0) {
    			customParameter = customParamPacket.getCustomParam();
    		}
    		else {
    			errorCode = EobrReturnCode.S_GENERAL_ERROR;
    		}
    	}
    	
    	Bundle answer = new Bundle();
    	answer.putInt(Constants.RETURNCODE, errorCode);
    	answer.putInt(Constants.RETURNVALUE, customParameter);
    	
    	return answer;
    }

    public int SetCustomParameter(int customParameter, int customParameterIndex)
    {
    	int errorCode;
    	int cmdId = EUCMDType.EUCMD_SET_CUSTOM_PARAMETER;
    	
    	Eobr_Packet eobrPacket = new Eobr_Packet();
		eobrPacket.setCmd((byte)cmdId);
		eobrPacket.setCrc(this.GetActiveDeviceCrc());
		eobrPacket.setLen((byte)5);	// 4 bytes for the custom parameter, 1 byte for the index.  EOBR allocated 64 bytes of space
									// for custome parameter
   	
    	// Arrange the byte into order before send out
		byte[] customParam = new byte[4];
		customParam = intToByteArray(customParameter);

    	eobrPacket.setData(new byte[]{(byte)customParameterIndex, customParam[0], customParam[1], customParam[2], customParam[3]});
    	    	
    	Eobr_Comm_Response_Packet commResponsePacket = new Eobr_Comm_Response_Packet();
        errorCode = EobrCommunications.Companion.validateWithData(_threadMgr, eobrPacket, cmdId,
                this.GetActiveDeviceCrc(), VerifyRxPacketResponseByte.EobrNullPacket,
                commResponsePacket, false, socketVerifier);
    	
		if (errorCode == 1) {
			errorCode = EobrReturnCode.S_GENERAL_ERROR;
		}    	
    	    	
    	return errorCode;    	
    }
    
    public int PingEobrDevice()
    {
    	int returnCode;
    	int cmdId = EUCMDType.EUCMD_TEST_CONNECTION;

    	    	
    	Eobr_Comm_Response_Packet commResponsePacket = new Eobr_Comm_Response_Packet();
		returnCode = EobrCommunications.Companion.validateNoData(_threadMgr, cmdId,
				this.GetActiveDeviceCrc(),
				VerifyRxPacketResponseByte.EobrNullPacket, commResponsePacket, socketVerifier);
    	    	
    	return returnCode;
    }

    public Bundle GetEngineOffCommsTimeout()
    {
    	int errorCode;
    	int cmdId = EUCMDType.EUCMD_GET_ENGINE_OFF_COMMS_TIMEOUT;
    	Bundle answer = new Bundle();
    	int timeoutInMinutes = 0;    	
    	    	
    	Eobr_Comm_Response_Packet commResponsePacket = new Eobr_Comm_Response_Packet();
		errorCode = EobrCommunications.Companion.validateNoData(_threadMgr, cmdId,
				this.GetActiveDeviceCrc(),
				VerifyRxPacketResponseByte.EobrIntegerPacket, commResponsePacket, socketVerifier);
    	
    	if (errorCode == EobrReturnCode.S_SUCCESS) {
    		EobrIntegerPacket eobrEngineCommsTimeoutPacket = new EobrIntegerPacket(commResponsePacket.getResponse());
           	timeoutInMinutes = eobrEngineCommsTimeoutPacket.getIntegerVal();              	       	       	       	       	       	       	       

            // Byte pattern sent by EOBR to indicate failure
           	// Error happen, size is 1
    		if (eobrEngineCommsTimeoutPacket.getSize() == 1 && timeoutInMinutes == 1)
    			errorCode = EobrReturnCode.S_DEV_INTERNAL_ERROR;        			
    	}
    	
    	answer.putInt(Constants.RETURNCODE, errorCode);
    	answer.putInt(Constants.RETURNVALUE, timeoutInMinutes);
		
    	return answer;    	
    }
    
    public int SetEngineOffCommsTimeout(int timeoutInMinutes)
    {
    	int errorCode;
    	int cmdId = EUCMDType.EUCMD_SET_ENGINE_OFF_COMMS_TIMEOUT;
    	
    	Eobr_Packet eobrPacket = new Eobr_Packet();
		eobrPacket.setCmd((byte)cmdId);
		eobrPacket.setCrc(this.GetActiveDeviceCrc());
		eobrPacket.setLen((byte)4);
   	
    	// Arrange the byte into order before send out
		byte[] timeoutBytes = new byte[4];
    	for (int i=0; i<4; i++)
    	{
    		timeoutBytes[i] = (byte)(timeoutInMinutes & 0xFF);    		
    		timeoutInMinutes >>= 8;
    	}

    	eobrPacket.setData(timeoutBytes);
    	
    	Eobr_Comm_Response_Packet commResponsePacket = new Eobr_Comm_Response_Packet();
        errorCode = EobrCommunications.Companion.validateWithData(_threadMgr, eobrPacket, cmdId,
                this.GetActiveDeviceCrc(), VerifyRxPacketResponseByte.EobrNullPacket,
                commResponsePacket, false, socketVerifier);
		
    	return errorCode;    	
    }

    public int ChangeActiveBusType(int newBusType)
    {
    	int errorCode;
    	int cmdId = EUCMDType.EUCMD_CHANGE_ACTIVE_BUS;
    	
    	Eobr_Packet eobrPacket = new Eobr_Packet();
		eobrPacket.setCmd((byte)cmdId);
		eobrPacket.setCrc(this.GetActiveDeviceCrc());
		eobrPacket.setLen((byte)1);
		eobrPacket.setData(new byte[]{(byte)newBusType});
    	
		Eobr_Comm_Response_Packet commResponsePacket = new Eobr_Comm_Response_Packet();
        errorCode = EobrCommunications.Companion.validateWithData(_threadMgr, eobrPacket, cmdId,
                this.GetActiveDeviceCrc(), VerifyRxPacketResponseByte.EobrNullPacket,
                commResponsePacket, false, socketVerifier);
    	
    	return errorCode;    	   	
    }

    public Bundle GetEOBRDllRevisions() {
    	int errorCode;
    	int cmdId = EUCMDType.EUCMD_GET_EOBR_REVISIONS;
    	Bundle answer = new Bundle();
    	StringBuilder sb = new StringBuilder();
    	String mainFirmwareRevision = "";
    	String usbFirmwareRevision = "";
    	String recordRevision = "";
    	String bootLoaderRevision = "";
    	    	    	
    	Eobr_Comm_Response_Packet commResponsePacket = new Eobr_Comm_Response_Packet();
		errorCode = EobrCommunications.Companion.validateNoData(_threadMgr, cmdId,
				this.GetActiveDeviceCrc(),
				VerifyRxPacketResponseByte.EobrStringPacket, commResponsePacket, socketVerifier);
    	
    	if (errorCode == EobrReturnCode.S_SUCCESS) {
    		EobrStringPacket eobrPacket = EobrStringPacket.build(commResponsePacket.getResponse());
    		
            // EOBR revision is sent using 18 byte data, 5 byte for mainFirmwareRevision,
            // 1 byte for separator, 5 byte for USBFirmwareRevision, 1 byte separator,
            // 5 byte for recordRevision, 1 byte null character.
            // | mainFirmwareRevision | # | USBFirmwareRevision | # | recordRevision | \0 |
    		int revisionSize = eobrPacket.getSize();
    		int i=0;
    		for (i=0; i<revisionSize; i++) {
    			if ((char)eobrPacket.getStringVal()[i] == '#')
    				break;
    			
   				sb.append((char)eobrPacket.getStringVal()[i]);
    		}
    		mainFirmwareRevision = sb.toString();
    		
    		sb = new StringBuilder();
    		for (i=i+1; i<revisionSize; i++) {
    			if ((char)eobrPacket.getStringVal()[i] == '#')
    				break;
    			
    			sb.append((char)eobrPacket.getStringVal()[i]);
    		}
    		usbFirmwareRevision = sb.toString();
    		
    		sb = new StringBuilder();
    		for (i=i+1; i<revisionSize; i++) {
    			if ((char)eobrPacket.getStringVal()[i] == '#')
    				break;
    			
    			sb.append((char)eobrPacket.getStringVal()[i]);
    		}
    		recordRevision = sb.toString();

    		sb = new StringBuilder();
    		for (i=i+1; i<revisionSize; i++) {
    			if ((char)eobrPacket.getStringVal()[i] == '#')
    				break;
    			
    			sb.append((char)eobrPacket.getStringVal()[i]);
    		}    		
    		bootLoaderRevision = sb.toString();    		
    	}
    	
    	answer.putInt(Constants.RETURNCODE, errorCode);
    	answer.putString(Constants.MAINFIRMWAREREVISION, mainFirmwareRevision);
    	answer.putString(Constants.USBFIRMWAREREVISION, usbFirmwareRevision);
    	answer.putString(Constants.RECORDREVISION, recordRevision);
    	answer.putString(Constants.BOOTLOADERREVISION, bootLoaderRevision);
    	answer.putString(Constants.EOBRDLLSREVISION, Constants.EOBR_DLLS_REVISION);
    	
    	return answer;    	   	    	
    }

	protected boolean IsFirmwareVersionSupported(int minSupportedMajor, int minSupportedMinor, int minSupportedPatch) {
		boolean result = false;
		Bundle firmwareVersion = this.GetEOBRDllRevisions();
		if(firmwareVersion.getInt(Constants.RETURNCODE) == EobrReturnCode.S_SUCCESS) {
			String fullVersion = firmwareVersion.getString(Constants.MAINFIRMWAREREVISION);
			if(fullVersion != null) {
				Pattern pattern = Pattern.compile("\\d+\\.\\d+\\.\\d+");
				Matcher matcher = pattern.matcher(fullVersion);
				if(matcher.find()) {
					String[] tokens = matcher.group().split("\\.");
					int major = Integer.parseInt(tokens[0]);
					int minor = Integer.parseInt(tokens[1]);
					int patch = Integer.parseInt(tokens[2]);

					if (major > minSupportedMajor
							|| (major == minSupportedMajor && minor > minSupportedMinor)
							|| (major == minSupportedMajor && minor == minSupportedMinor && patch >= minSupportedPatch)) {
						result = true;
					}
				}
			}
		}
		return result;
	}
    
	protected void closeLocalSocket()
	{
		try {
			if (_localSocket != null)
			{
				_localSocket.close();
				_localSocket = null;
			}
			_isSocketConnected = false;
		} catch (IOException e) {
			_localSocket = null;
			_isSocketConnected = false;
		}
		catch (Throwable ex){
			_localSocket = null;
			_isSocketConnected = false;
		}
	}      
	
	/* This class is responsible for reading from the BluetoothSocket's
	 * underlying InputStream, and shares that data with other threads
	 * through a non-blocking read method. This solves a problem that
	 * was encountered when implementing automatic retries when timeouts
	 * were encountered:
	 * 	- ReceiveDataThread #1 was spawned to wait for data, blocks on InputStream.read
	 * 		and CommThreadManager.WaitForThread detects a timeout.
	 *  - Spawn ReceiveDataThread #2 to wait for data from retry. #1 is still blocking
	 *  	and receives the response... #2 times out.
	 *  
	 *  With this change, #1 times out and is interrupted. The BluetoothReader thread
	 *  it spawned is still around, and is still blocking on the InputStream.read.
	 *  When #2 is spawned, it uses the existing BluetoothReader instance, and reads
	 *  the response from it.  When it succeeds, the BluetoothReader thread finishes,
	 *  and a new instance is spawned with the next command.
	 */
	protected class BluetoothReader extends Thread
	{
		private ByteBuffer byteBuffer;
		private int bytesRead = 0;
		private InputStream in;
		
		private boolean keepReading = true;
		private boolean isDone = false;
		
		private Lock lock = new ReentrantLock();
		private Condition commandWait = lock.newCondition();
		private boolean commandReceived = true;
		
		public BluetoothReader(BluetoothSocket socket)
		{
			byteBuffer = ByteBuffer.allocate(1000);
			
			try {
				in = socket.getInputStream();
			} catch (IOException e) {
				keepReading = false;
				AndroidBTBase.this._isSocketConnected = false;
				
				e.printStackTrace();
			}
		}
		
		public int read(byte[] buffer)
		{
			int retVal = 0;
			
			if(bytesRead > 0)
			{				
				synchronized(byteBuffer)
				{
					retVal = bytesRead;
					
					//set the buffer to the beginning
					byteBuffer.position(0);
					
					int count = bytesRead > buffer.length ? buffer.length : bytesRead;
					
					byteBuffer.get(buffer, 0, count);
					
					//reset things
					byteBuffer.clear();
					bytesRead = 0;
				}
			}
			
			return retVal;
		}
				
		public void run()
		{
			//Log.d("lock", "Thread starting");
			
			while(keepReading)
			{
				//if this is the first time,
				//or if commanded to continue reading
				if(commandReceived)
				{	
					commandReceived = false;
					
					//Log.d("lock", "Waiting for data");
					
					readData();
				}
				else
				{
					lock.lock();
					
					//Log.d("lock", "Waiting for command");
					
					try
					{
						//if this is not the first time through
						//and we haven't been commanded to either
						//continue reading or stop, wait for a command
						while(!commandReceived) //while loop instead of if to prevent spurious wake-ups
						{
							try {
								commandWait.await();
							} catch (InterruptedException e) { e.printStackTrace(); }
						}
						
						//Log.d("lock", String.format("Command received... keep reading: %b", keepReading));
					}
					finally
					{
						lock.unlock();
					}
				}
			}
			
			isDone = true;
		}
		
		public void readData()
		{
			int justRead = 0;
			
			try {
				byte[] bytes = new byte[1000];
				
				justRead = in.read(bytes);
				
				if(justRead > 0)
				{					
					synchronized(byteBuffer)
					{						
						byteBuffer.put(bytes, 0, justRead);
						bytesRead += justRead;
					}
				}
				
			} catch (IOException e) {
				keepReading = false;
				AndroidBTBase.this._isSocketConnected = false;
				
				e.printStackTrace();
			}
		}
		
		public void continueReading()
		{						
			processCommand(true);
		}
		
		public void stopReading()
		{			
			processCommand(false);
		}
		
		private void processCommand(boolean continueReading)
		{
			//Log.d("lock", String.format("Processing command %b", continueReading));
			
			lock.lock();
			try
			{
				keepReading = continueReading;
				commandReceived = true;
				
				commandWait.signal();
			}
			finally
			{
				lock.unlock();
			}
		}
		
		public boolean getIsDone()
		{
			return isDone;
		}
	}

	@Override
	public boolean VerifySocketConnection()
	{
		boolean bStatus = false;

		// if _localSocket is already setup, check if already connected to device we're looking for
		if (_localSocket != null)
		{
			if (_localSocket.getRemoteDevice().getAddress().equals(this.GetActiveDeviceAddress()))
			{
				CloseDevice();

				_localSocket = SetupAndOpenBluetoothSocket(getCommThreadManager(), _btAdapter, this.GetActiveDeviceAddress());

				if (_localSocket != null) {
					bStatus = true;
					_isSocketConnected = true;
					bluetoothReader = null;
					Log.v("BTComm", String.format("BTReconnect:  %s:  VerifySocketConnection successful", DateUtility.getCurrentDateTime().toString()));
				} else {
					bStatus = false;
					_isSocketConnected = false;
					Log.v("BTComm", String.format("BTReconnect:  %s:  VerifySocketConnection failed", DateUtility.getCurrentDateTime().toString()));
				}
			}
		}

		return bStatus;
	}

	protected class ValidatedDevice {
		private String _deviceName;
		private short _deviceCrc;

		protected ValidatedDevice(String name, short crc) {
			_deviceName = name;
			_deviceCrc = crc;
		}

		public String getDeviceName() { return _deviceName; }
		public short getDeviceCrc() { return _deviceCrc; }
	}
}
