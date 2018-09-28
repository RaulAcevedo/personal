package com.jjkeller.kmbapi.geotabengine;

import android.util.Log;

import com.jjkeller.kmbapi.common.LogCat;
import com.jjkeller.kmbapi.common.TabDataConversionUtil;
import com.jjkeller.kmbapi.controller.utility.ErrorLogHelper;
import com.jjkeller.kmbapi.kmbeobr.Constants;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.*;

class ThirdPartyMessage
{
    public String Name;
    public byte MessageType;
    public byte[] Command;

    ThirdPartyMessage(String sName, byte bType, byte[] abCommand)
    {
        Name = sName;
        MessageType = bType;
        Command = abCommand;
    }
}

public class ThirdParty {
    private static final String TAG = "Geotab ThirdParty";	// Used for error logging

    private static GeotabDataEnhanced _geotabData = new GeotabDataEnhanced();
    private static GeotabData _geotabDataOld = new GeotabData();

    private static final byte MESSAGE_HANDSHAKE = 1;
    private static final byte MESSAGE_ACK = 2;
    private static final byte MESSAGE_HOS_MOTION_DATA = 0x20;
    private static final byte MESSAGE_ENHANCED_HOS_MOTION_DATA = 0x21;
    private static final byte MESSAGE_CONFIRMATION = (byte) 0x81;
    private static final byte MESSAGE_STATUS_DATA = (byte) 0x80;
    private static final byte MESSAGE_HOS_DATA_ACKNOWLEDGEMENT = (byte) 0x84;
    private static final byte TP_FREE_FORMAT_DATA = (byte) 0x82;
    private static final byte TP_DEVICE_INFO_RECEIVED = (byte) 0x83;
    private static final byte TP_HOS_ACK = (byte) 0x84;
    private static final byte MESSAGE_SYNC = 0x55;
    private static final byte[] HOS_ID = new byte[] { 0x03, 0x10, 0x00, 0x00 };
    private static final byte[] ENHANCED_HOS_ID = new byte[] { 0x2D, 0x10, 0x00, 0x00 };
    private static final byte[] HOS_WITH_ACK_ID = new byte[] { 0x1C, 0x10, 0x00, 0x00 };

    static final ThirdPartyMessage[] THIRD_PARTY_MESSAGE_DEFINEs = new ThirdPartyMessage[]
            {
                    new ThirdPartyMessage("-BYPASS-", (byte)0, null),
                    new ThirdPartyMessage("STATUS: OUTSIDE TEMPERATURE", MESSAGE_STATUS_DATA, new byte[] { 0x35, 0x00 }),		// 53
                    new ThirdPartyMessage("STATUS: ENGINE WARNING LIGHT", MESSAGE_STATUS_DATA, new byte[] { 0x24, 0x00 }),		// 36
                    new ThirdPartyMessage("STATUS: PARK BRAKE", MESSAGE_STATUS_DATA, new byte[] { 0x31, 0x00 }),				// 49
                    new ThirdPartyMessage("FREE FORMAT", TP_FREE_FORMAT_DATA, null),
                    new ThirdPartyMessage("DEVICE INFO", TP_DEVICE_INFO_RECEIVED, null),
                    new ThirdPartyMessage("HOS ACK", TP_HOS_ACK, null),
            };

    private enum State
    {
        SEND_SYNC,
        WAIT_FOR_HANDSHAKE,
        SEND_CONFIRMATION,
        PRE_IDLE,
        IDLE,
        WAIT_FOR_ACK,
        ACK_HOS_DATA,
    }

    private final Lock _lock = new ReentrantLock();
    private final Condition _event = _lock.newCondition();

    private byte[] _message;
    private boolean _ackReceived, _handshakeReceived, _messageToSend, _needToAcknowledgeHosData;

    private GeotabUsbService _accessoryControl;
    // private Context _context;
    private StateMachine _stateMachine;

    // Constructor
    public ThirdParty(GeotabUsbService accessory)
    {
        _handshakeReceived = false;
        _ackReceived = false;
        _messageToSend = false;

        _accessoryControl = accessory;
        // _context = context;						// Context is needed for showToastFromThread

        _stateMachine = new StateMachine();
        new Thread(_stateMachine).start();		// Run as a separate thread
    }

    // State machine to handle the third party protocol
    private class StateMachine implements Runnable {
        private State eState = State.SEND_SYNC;
        private AtomicBoolean fRunning = new AtomicBoolean(true);

        public void run() {
            while (fRunning.get()) {
                _lock.lock();        // The lock is needed for await and atomic access to flags/buffers

                try {
                    LogCat.getInstance().v(TAG, eState.toString());
                    switch (eState) {
                        case SEND_SYNC: {
                            byte[] abMessage = new byte[]{MESSAGE_SYNC};
                            _accessoryControl.write(abMessage);
                            eState = State.WAIT_FOR_HANDSHAKE;
                            break;
                        }
                        case WAIT_FOR_HANDSHAKE: {
                            // Waits for the handshake message or resends sync every 1s
                            _event.await(1000, TimeUnit.MILLISECONDS);

                            if (_handshakeReceived) {
                                eState = State.SEND_CONFIRMATION;
                            } else {
                                eState = State.SEND_SYNC;
                            }
                            break;
                        }
                        case SEND_CONFIRMATION: {
                            byte[] abMessage = BuildMessage(MESSAGE_CONFIRMATION, ENHANCED_HOS_ID);
                            _accessoryControl.write(abMessage);
                            eState = State.PRE_IDLE;
                            break;
                        }
                        case PRE_IDLE: {
                            _handshakeReceived = false;
                            _ackReceived = false;
                            _messageToSend = false;
                            eState = State.IDLE;
                            break;
                        }
                        case IDLE: {
                            // Sleep and wait for a handshake or a message to send
                            _event.await();

                            if (_handshakeReceived) {
                                eState = State.SEND_CONFIRMATION;
                            } else if (_messageToSend) {
                                _accessoryControl.write(_message);
                                eState = State.WAIT_FOR_ACK;
                            } else if(_needToAcknowledgeHosData) {
                                _needToAcknowledgeHosData = false;
                                eState = State.ACK_HOS_DATA;
                            }
                            break;
                        }
                        case WAIT_FOR_ACK: {
                            // Wait for the ack or reset after 5s
                            _event.await(5000, TimeUnit.MILLISECONDS);

                            if (_ackReceived) {
                                eState = State.PRE_IDLE;
                            } else {
                                eState = State.SEND_SYNC;
                            }
                            break;
                        }
                        case ACK_HOS_DATA: {
                            byte[] abMessage = BuildMessage(MESSAGE_HOS_DATA_ACKNOWLEDGEMENT, new byte[] {});
                            _accessoryControl.write(abMessage);
                            eState = State.PRE_IDLE;
                            break;
                        }
                        default: {
                            eState = State.SEND_SYNC;
                            break;
                        }
                    }

                } catch (InterruptedException e) {
                    LogCat.getInstance().e(TAG, "State Machine encountered an error: " + Log.getStackTraceString(e));
                } finally {
                    _lock.unlock();
                }
            }
        }

        // Stop the thread
        public void close()
        {
            Log.i(TAG, "Shutting down third party SM");

            _lock.lock();
            try
            {
                _geotabData = new GeotabDataEnhanced();
                fRunning.set(false);
                _handshakeReceived = false;
                _ackReceived = false;
                _messageToSend = false;
                _event.signal();
            }
            finally
            {
                _lock.unlock();
            }
        }

    }

    // Signal the state machine to stop
    public void close()
    {
        if (_stateMachine != null)
            _stateMachine.close();
    }

    // Encapsulate a message to be sent
    public void TxMessage(byte bType, byte[] abData)
    {
        _lock.lock();
        try
        {
            _message = BuildMessage(bType, abData);
            _messageToSend = true;
            _event.signal();
        }
        finally
        {
            _lock.unlock();
        }
    }

    // Checks if a received message matches the expected third party format
    public void RxMessage(byte[] abData)
    {
        // Check length
        if (abData == null || abData.length < 6)
            return;

        // Check structure
        byte bSTX = abData[0];
        byte bLength = abData[2];
        byte bETX = abData[abData.length - 1];

        if (bSTX != 0x02 || bETX != 0x03)
            return;

        // Check checksum
        byte[] abChecksum = new byte[] { abData[abData.length - 3], abData[abData.length - 2] };
        byte[] abCalcChecksum = CalcChecksum(abData, bLength + 3);

        if (!Arrays.equals(abChecksum, abCalcChecksum))
            return;

        byte bType = abData[1];

        switch (bType)
        {
            case MESSAGE_HANDSHAKE:
                Log.v(TAG, "Received MESSAGE_HANDSHAKE");

                _lock.lock();
                try
                {
                    _handshakeReceived = true;
                    _event.signal();
                }
                finally
                {
                    _lock.unlock();
                }
                break;

            case MESSAGE_ACK:
                Log.v(TAG, "Received MESSAGE_ACK");

                _lock.lock();
                try
                {
                    _ackReceived = true;
                    _event.signal();
                }
                finally
                {
                    _lock.unlock();
                }
                break;

            case MESSAGE_HOS_MOTION_DATA:
                Log.v(TAG, "Received MESSAGE_HOS_MOTION_DATA");

                _lock.lock();

                try {
                    _needToAcknowledgeHosData = true;
                    synchronized (_geotabData){
                        _geotabDataOld = new GeotabData(abData);
                    }

                    _event.signal();
                }
                finally {
                    _lock.unlock();
                }

                break;

            case MESSAGE_ENHANCED_HOS_MOTION_DATA:
                Log.v(TAG, "Received MESSAGE_ENHANCED_HOS_MOTION_DATA");

                _lock.lock();

                try {
                    _needToAcknowledgeHosData = true;
                    synchronized (_geotabData) {
                        _geotabData = new GeotabDataEnhanced(abData);
                    }

                    _event.signal();
                }
                finally {
                    _lock.unlock();
                }

                break;
        }
    }

    // Assemble a third party message
    private byte[] BuildMessage(byte bType, byte[] abData)
    {
        byte[] abMessage = new byte[abData.length + 6];

        abMessage[0] = 0x02;
        abMessage[1] = bType;
        abMessage[2] = (byte) abData.length;

        System.arraycopy(abData, 0, abMessage, 3, abData.length);

        int iLengthUpToChecksum = abData.length + 3;
        byte abCalcChecksum[] = CalcChecksum(abMessage, iLengthUpToChecksum);
        System.arraycopy(abCalcChecksum, 0, abMessage, iLengthUpToChecksum, 2);

        abMessage[abMessage.length - 1] = 0x03;

        return abMessage;
    }

    // Calculate the Fletcher's checksum over the given bytes
    private byte[] CalcChecksum(byte[] abData, int iLength)
    {
        byte[] abChecksum = new byte[] { 0x00, 0x00 };

        for (int i = 0; i < iLength; i++)
        {
            abChecksum[0] += abData[i];
            abChecksum[1] += abChecksum[0];
        }

        return abChecksum;
    }

    public void ExtractHOSData(byte[] abData) {
        ByteBuffer abConvert;

        byte[] abDateTime = new byte[4];
        System.arraycopy(abData, 3, abDateTime, 0, abDateTime.length);
        abConvert = ByteBuffer.wrap(abDateTime).order(java.nio.ByteOrder.LITTLE_ENDIAN);
        int iDateTime = abConvert.getInt();
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        c.clear();
        c.set(2002, Calendar.JANUARY, 1);        // (Units given in seconds since Jan 1, 2002)
        c.add(Calendar.SECOND, iDateTime);
        _geotabData.setDatetime(c.getTime().getTime()); // Convert to milliseconds since epoch

        byte[] abLatitude = new byte[4];
        System.arraycopy(abData, 7, abLatitude, 0, abLatitude.length);
        abConvert = ByteBuffer.wrap(abLatitude).order(java.nio.ByteOrder.LITTLE_ENDIAN);
        int iLatitude = abConvert.getInt();
        _geotabData.setLatitude((float)iLatitude  / 10000000f);    // (Units given in 10^-7)

        byte[] abLogitude = new byte[4];
        System.arraycopy(abData, 11, abLogitude, 0, abLogitude.length);
        abConvert = ByteBuffer.wrap(abLogitude).order(java.nio.ByteOrder.LITTLE_ENDIAN);
        int iLogitude = abConvert.getInt();
        _geotabData.setLongitude((float)iLogitude / 10000000f);    // (Units given in 10^-7)

        //A byte according to the language spec represents a value between −128 - 127.
        //Since we don't care about negative values when it comes to speed, we need to convert it to unsigned byte, increasing the max value to 255
        int kph = (int)abData[15] & 0xFF; //Converting to Unsigned
        float mph = kph * Constants.MILES_PER_KILOMETER;
        _geotabData.setSpeedometer(mph);

        byte[] abPRM = new byte[2];
        System.arraycopy(abData, 16, abPRM, 0, abPRM.length);
        abConvert = ByteBuffer.wrap(abPRM).order(java.nio.ByteOrder.LITTLE_ENDIAN);
        _geotabData.setTachometer(abConvert.getShort() / 4f); // Convert to RPM (Units given in 0.25)

        byte[] abOdometer = new byte[4];
        System.arraycopy(abData, 18, abOdometer, 0, abOdometer.length);
        abConvert = ByteBuffer.wrap(abOdometer).order(java.nio.ByteOrder.LITTLE_ENDIAN);
        float odometer = TabDataConversionUtil.convertOdometerReading(abConvert.getInt());
        _geotabData.setOdometer(odometer);

        _geotabData.setStatus(abData[22]);
    }

    public void ExtractEnhancedHOSData(byte[] abData) {
        //extract the basic stuff
        ExtractHOSData(abData);

        ByteBuffer abConvert;

        byte[] abTripOdometer = new byte[4];
        System.arraycopy(abData, 23, abTripOdometer, 0, abTripOdometer.length);
        abConvert = ByteBuffer.wrap(abTripOdometer).order(java.nio.ByteOrder.LITTLE_ENDIAN);
        float tripOdometer = abConvert.getInt() / 10f; // Convert to km (Units given in 0.1/km)
        tripOdometer *= Constants.MILES_PER_KILOMETER; //convert to miles
        _geotabData.setTripOdometer(tripOdometer);

        byte[] abEngineHours = new byte[4];
        System.arraycopy(abData, 27, abEngineHours, 0, abEngineHours.length);
        abConvert = ByteBuffer.wrap(abEngineHours).order(java.nio.ByteOrder.LITTLE_ENDIAN);
        _geotabData.setEngineHours(abConvert.getInt() / 10f); // Convert to hours (Units given in 0.1/hr)

        byte[] abTripEngineHours = new byte[4];
        System.arraycopy(abData, 31, abTripEngineHours, 0, abTripEngineHours.length);
        abConvert = ByteBuffer.wrap(abTripEngineHours).order(java.nio.ByteOrder.LITTLE_ENDIAN);
        _geotabData.setTripEngineSeconds(abConvert.getInt()); // Units given in seconds

        byte[] abVehicleId = new byte[4];
        System.arraycopy(abData, 35, abVehicleId, 0, abVehicleId.length);
        abConvert = ByteBuffer.wrap(abVehicleId).order(java.nio.ByteOrder.LITTLE_ENDIAN);

        //Transform serial number into full serial number (Get prefix and checksum)
        GeotabDataHelper helper = new GeotabDataHelper();
        Integer hardwareId = helper.GetHardwareIdFromHexString(String.format("%08X",abConvert.getInt()));
        String fullVehicleId = helper.EncodeSerialNumber(helper.Go7ProductId, hardwareId);
        _geotabData.setVehicleId(fullVehicleId);
    }

    public synchronized GeotabDataEnhanced getGeotabData()
    {
        return _geotabData;
    }
}
