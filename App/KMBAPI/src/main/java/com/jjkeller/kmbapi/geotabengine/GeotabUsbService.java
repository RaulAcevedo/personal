package com.jjkeller.kmbapi.geotabengine;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.jjkeller.kmbapi.controller.utility.ErrorLogHelper;
import com.jjkeller.kmbapi.enums.USBAccessoryConnectionStatus;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


// From AccessoryControl
@TargetApi(12)
public class GeotabUsbService extends Service
{
    private static final String TAG = "GeotabUSB";
    private static final String MANUFACTURER = "Geotab";
    private static final String MODEL = "IOX USB";

    // --- Member variables
    private UsbManager _usbManager;
    private USBReceiver _usbReceiver;
    private ThirdParty _thirdParty;

    private boolean  _connectionOpen;
    private ParcelFileDescriptor _parcelFileDescriptor;
    private FileOutputStream _outputStream;
    private FileInputStream _inputStream;
    private final Lock _lock = new ReentrantLock();
    private final Condition _receiverEnded = _lock.newCondition();

    //region: broadcast receiver stuff
    private HandlerThread _broadcastThread;
    private Handler _broadcastHandler;
    private Thread _usbReceiverThread;
    private static final String ACTION_USB_PERMISSION_RESULT = "com.jjkeller.kmbapi.GeotabUsbService.ActionUSBPermissionResult";
    private final Intent _permissionResultIntent = new Intent(ACTION_USB_PERMISSION_RESULT);
    private CountDownLatch _permissionRequestLatch;
    //endregion

    //region: broadcast intents
    private final Intent _permissionDeniedIntent = new Intent(GeotabBroadcasts.ON_PERMISSION_DENIED);
    private final Intent _permissionGrantedIntent = new Intent(GeotabBroadcasts.ON_PERMISSION_GRANTED);
    private final Intent _permissionTimeoutIntent = new Intent(GeotabBroadcasts.ON_PERMISSION_TIMEOUT);
    //endregion

    //region: Service
    private final IBinder _binder = new ServiceBinder();
    public class ServiceBinder extends Binder {
        public GeotabUsbService getService() {
            return GeotabUsbService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Service bound.");

        return _binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "Service created.");

        //receive broadcasts in a different thread so that
        //we can wait for the permission request
        _broadcastThread = new HandlerThread("GeotabUsbService.BroadcastReceiverThread");
        _broadcastThread.start();
        _broadcastHandler = new Handler(_broadcastThread.getLooper());

        _connectionOpen = false;

        _usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        Log.d(TAG, "Recieved Service");

        IntentFilter filter = new IntentFilter();
        filter.addAction(GeotabBroadcasts.ON_DEVICE_DETACHED);
        filter.addAction(ACTION_USB_PERMISSION_RESULT);

        registerReceiver(_broadcastReceiver, filter, null, _broadcastHandler);
        Log.d(TAG, "Registered Receiver");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "Service destroyed.");

        unregisterReceiver(_broadcastReceiver);
        close();
    }
    //endregion

    private final BroadcastReceiver _broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "Received broadcast: " + action);

            if(action.equals(ACTION_USB_PERMISSION_RESULT)) {
                try {
                    /*
                        Sleep this thread for 5 seconds after getting permissions for reading USB accessory to prevent
                        the user disconnecting the device while opening a valid USB connection.
                        The reason it works is because this permission gets invalidated as soon the device is disconnected, so, waiting some seconds
                        after getting the permission will invalidate ite and it won't attempt to open the USB accessory.
                     */
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    ErrorLogHelper.RecordException(context, e);
                    e.printStackTrace();
                }

                if(_permissionRequestLatch != null)
                    _permissionRequestLatch.countDown();
            }
            else if(action.equals(UsbManager.ACTION_USB_ACCESSORY_DETACHED))
            {
                close();
            }
        }
    };



    // This thread receives messages from the USB Accessory
    private class USBReceiver implements Runnable {
        private AtomicBoolean fRunning = new AtomicBoolean(true);

        // Constructor
        USBReceiver() {
            _thirdParty = new ThirdParty(GeotabUsbService.this);
        }

        public void run() {
            int iNumberOfBytesRead = 0;
            byte[] abBuffer = new byte[512];    // max is [16384]

            Log.i(TAG, "Receiver thread started");

            try {
                while (fRunning.get()) {
                    // Note: Read blocks until one byte has been read, the end of the source stream is detected or an exception is thrown
                    iNumberOfBytesRead = _inputStream.read(abBuffer);

                    if (fRunning.get() && (iNumberOfBytesRead > 0)) {
                        byte[] abMessage = new byte[iNumberOfBytesRead];
                        System.arraycopy(abBuffer, 0, abMessage, 0, abMessage.length);

                        if(_thirdParty != null) {
                            _thirdParty.RxMessage(abMessage);

                            final StringBuffer sDisplay = convertToString(abMessage);
                            Log.v(TAG, "Raw data: " + sDisplay.toString());

                            //this was being ran on the UI thread but that doesn't seem like a great idea.
                            //Running in a separate Runnable in case this takes a while so that we can
                            //continue to process incoming data.
                            Runnable sendData = new Runnable() {
                                @Override
                                public void run() {
                                    if (_thirdParty != null) {
                                        GeotabDataEnhanced data = _thirdParty.getGeotabData();
                                        Log.v(TAG, String.format("getGeotabData : \n%s", data.toString()));

                                        for (int i = 0; i < listeners.size(); i++) {
                                            listeners.get(i).receiveGeotabData(data);
                                        }
                                    }
                                }
                            };

                            new Thread(sendData).start();
                        }
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "Couldn't read from input stream: " + Log.getStackTraceString(e));
                close();
            }

            _lock.lock();

            try {

            } finally {
                _lock.unlock();
            }
        }

        // Shutdown the receiver and third party threads
        public void close()
        {
            fRunning.set(false);

            if (_thirdParty != null)
            {
                _thirdParty.close();
                _thirdParty = null;
            }
        }
    }

    // --- Implementation
    private List<IGeotabListener> listeners = new ArrayList<IGeotabListener>();
    public void addHosListener(IGeotabListener listener)
    {
        listeners.add(listener);
    }
    public void removeHosListener(IGeotabListener listener) { listeners.remove(listener); }

    // Same as AccessoryControl.java open()
    public USBAccessoryConnectionStatus open()
    {
        if (_connectionOpen)
            return USBAccessoryConnectionStatus.CONNECTED;

        UsbAccessory[] accList = _usbManager.getAccessoryList();    // The accessory list only returns 1 entry

        if (accList != null && accList.length > 0) {
            UsbAccessory accessory = accList[0];
            // If permission has been granted, try to establish the connection
            boolean permission = _usbManager.hasPermission(accessory);

            Log.d(TAG, String.format("Attempting to open device. Has permission: %b", permission));

            if (permission)
                return open(accessory);

            if(_permissionRequestLatch == null) {
                _permissionRequestLatch = new CountDownLatch(1);

                Log.d(TAG, "Requesting permission");

                //request permission and wait for the user to grant permissions
                PendingIntent permissionIntent = PendingIntent.getBroadcast(GeotabUsbService.this, 0, _permissionResultIntent, 0);
                _usbManager.requestPermission(accList[0], permissionIntent);

                try {
                    boolean requestAcknowledged = _permissionRequestLatch.await(30, TimeUnit.SECONDS);

                    //they granted or denied permission
                    if(requestAcknowledged) {
                        if(_usbManager.hasPermission(accessory)) {
                            Log.d(TAG, String.format("User granted permission"));
                            sendBroadcast(_permissionGrantedIntent);

                            return open(accessory);
                        } else {
                            Log.e(TAG, String.format("User denied permission"));
                            sendBroadcast(_permissionDeniedIntent);

                            return USBAccessoryConnectionStatus.PERMISSION_DENIED;
                        }
                    } else {
                        //they didn't acknowledge the permission request
                        Log.e(TAG, String.format("Permission request timed out"));
                        sendBroadcast(_permissionTimeoutIntent);

                        return USBAccessoryConnectionStatus.PERMISSION_TIMEOUT;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finally {
                    _permissionRequestLatch = null;
                }
            }
        }

        return USBAccessoryConnectionStatus.NO_ACCESSORY;
    }

    public static boolean USBAccessoryAttached(Context ctx) {
        return USBAccessoryAttached((UsbManager)ctx.getSystemService(Context.USB_SERVICE));
    }

    public static boolean USBAccessoryAttached(UsbManager usbManager){
        boolean isAccessoryConnected = false;

        UsbAccessory[] accList = usbManager.getAccessoryList();
        if (accList != null && accList.length > 0) {
            isAccessoryConnected = true;
        }
        return isAccessoryConnected;
    }

    public boolean USBAccessoryAttached() {
        return USBAccessoryAttached(_usbManager);
    }

    public boolean USBAccessoryConnected() {
        return _connectionOpen;
    }

    public USBAccessoryConnectionStatus open(UsbAccessory accessory)
    {
        if (_connectionOpen)
            return USBAccessoryConnectionStatus.CONNECTED;

        // Check if the accessory is supported by this app
        if (!MANUFACTURER.equals(accessory.getManufacturer()) || !MODEL.equals(accessory.getModel())) {
            Log.e(TAG, "Attempted to connect to an unknown accessory: " + accessory.getManufacturer() + " " + accessory.getModel());
            return USBAccessoryConnectionStatus.UNKNOWN_ACCESSORY;
        }

        // Open read/write streams for the accessory
        _parcelFileDescriptor = _usbManager.openAccessory(accessory);
        Log.i(TAG, "Connected to Accessory: " + accessory.getManufacturer() + " " + accessory.getModel());

        if (_parcelFileDescriptor != null) {
            FileDescriptor fd = _parcelFileDescriptor.getFileDescriptor();
            _outputStream = new FileOutputStream(fd);
            _inputStream = new FileInputStream(fd);

            _connectionOpen = true;

            _usbReceiver = new USBReceiver();
            _usbReceiverThread = new Thread(_usbReceiver);
            _usbReceiverThread.start();

            return USBAccessoryConnectionStatus.CONNECTED;
        }


        Log.e(TAG, "Couldn't get parcel description from device.");
        return USBAccessoryConnectionStatus.NO_PARCEL;
    }

    // End and shutdown the communication with the accessory
    public void appIsClosing()
    {
        if (!_connectionOpen)
            return;

        _usbReceiver.close();

        _lock.lock();
        try
        {
            // Wait up to 100ms for the receiver thread to gracefully close the link
            _receiverEnded.await(100, TimeUnit.MILLISECONDS);
        }
        catch (InterruptedException e)
        {
            Log.w(TAG, "Exception in disconnect timeout", e);
        }
        finally
        {
            _lock.unlock();
        }
    }

    public void close()
    {
        if(!_connectionOpen)
            return;

        _connectionOpen = false;

        // End the receiver thread
        _usbReceiver.close();
        Log.i(TAG, "Receiver Thread closed");

        try
        {
            _inputStream.close();
            Log.i(TAG, "Input Stream closed");
        }
        catch (Exception e)
        {
            Log.e(TAG, "Exception when closing Input Stream: " + Log.getStackTraceString(e));
        }

        try
        {
            _outputStream.close();
            Log.i(TAG, "Output Stream closed");
        }
        catch (Exception e)
        {
            Log.e(TAG, "Exception when closing Output Stream: " + Log.getStackTraceString(e));
        }

        try
        {
            _parcelFileDescriptor.close();
            Log.i(TAG, "File Descriptor closed");
        }
        catch (Exception e)
        {
            Log.e(TAG, "Exception when closing File Descriptor: " + Log.getStackTraceString(e));
        }
    }

    // Converts a byte array to a string
    private StringBuffer convertToString(byte[] abIn)
    {
        StringBuffer sData = new StringBuffer();

        for (byte anAbIn : abIn) {
            if ((anAbIn >> 4) == 0)
                sData.append('0');

            sData.append(Integer.toHexString(anAbIn & 0xFF).toUpperCase(Locale.US) + " ");
        }

        return sData;
    }

    public void write(byte[] data)
    {
        if (!_connectionOpen)
            return;

        try
        {
            // Lock the output stream for the write operation
            synchronized (_outputStream)
            {
                _outputStream.write(data);
            }
        }
        catch (IOException e)
        {
            Log.e(TAG, "Couldn't write to output stream: " + Log.getStackTraceString(e));
            close();
        }
    }
}
