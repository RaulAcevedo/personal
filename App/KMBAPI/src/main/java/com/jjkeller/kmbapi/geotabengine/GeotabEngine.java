package com.jjkeller.kmbapi.geotabengine;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import com.jjkeller.kmbapi.CodeBlocks;
import com.jjkeller.kmbapi.HosMessageProcessor.interfaces.IHOSMessage;
import com.jjkeller.kmbapi.common.LogCat;
import com.jjkeller.kmbapi.common.TimeKeeper;
import com.jjkeller.kmbapi.common.WatchdogTimer;
import com.jjkeller.kmbapi.configuration.AppSettings;
import com.jjkeller.kmbapi.configuration.FirmwareUpdate;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.EOBR.EobrReader;
import com.jjkeller.kmbapi.controller.GeotabController;
import com.jjkeller.kmbapi.controller.dataaccess.FacadeFactory;
import com.jjkeller.kmbapi.controller.interfaces.IFacadeFactory;
import com.jjkeller.kmbapi.controller.utility.ErrorLogHelper;
import com.jjkeller.kmbapi.enums.DatabusTypeEnum;
import com.jjkeller.kmbapi.enums.USBAccessoryConnectionStatus;
import com.jjkeller.kmbapi.eobrengine.EobrDeviceDescriptor;
import com.jjkeller.kmbapi.eobrengine.IEobrEngine;
import com.jjkeller.kmbapi.featuretoggle.IFeatureToggleService;
import com.jjkeller.kmbapi.firmwareupgrade.FirmwareUpdateBroadcaster;
import com.jjkeller.kmbapi.geotabengine.interfaces.IGeotabMessageProcessor;
import com.jjkeller.kmbapi.kmbeobr.BundleBuilder;
import com.jjkeller.kmbapi.kmbeobr.Constants;
import com.jjkeller.kmbapi.kmbeobr.DriveData;
import com.jjkeller.kmbapi.kmbeobr.DriveDataTypeEnum;
import com.jjkeller.kmbapi.kmbeobr.Enums;
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
import com.jjkeller.kmbapi.kmbeobr.ThresholdTypeEnum;
import com.jjkeller.kmbapi.kmbeobr.Thresholds;
import com.jjkeller.kmbapi.kmbeobr.TripDistanceHours;
import com.jjkeller.kmbapi.kmbeobr.TripReport;
import com.jjkeller.kmbapi.kmbeobr.VehicleLocation;
import com.jjkeller.kmbapi.proxydata.EobrConfiguration;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class GeotabEngine implements IEobrEngine, IGeotabListener {
    private static final String TAG = "GeotabEngine";

    private GeotabUsbService _usbService;
    private Context _context;

    private IGeotabMessageProcessor _messageProcessor;
    private Thresholds _defaultThresholds, _driverThresholds;
    private IFeatureToggleService _featureToggleService;
    private IFacadeFactory _facadeFactory;

    private GeotabDataEnhanced _lastGeotabData;
    private String _unitId;
    private CountDownLatch _receivedDataLatch;
    private CountDownLatch _serviceConnectedLatch;
    private WatchdogTimer _watchdogTimer;
    private AppSettings _appSettings;
    private Set<Long> _generatedTimecodes;

    private boolean _hasSubmittedConnectionToGeotab;

    private int _messageCount;
    private boolean _stallData = false;

    ServiceConnection _serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogCat.getInstance().d(TAG, "GeotabUsbService connected");

            GeotabUsbService.ServiceBinder binder = (GeotabUsbService.ServiceBinder) service;
            _usbService = binder.getService();
            _usbService.addHosListener(GeotabEngine.this);

            _serviceConnectedLatch.countDown();
            _context.unbindService(this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogCat.getInstance().d(TAG, "GeotabUsbService disconnected");
        }
    };

    public GeotabEngine(final Context ctx, Thresholds thresholds) {
        this(ctx, thresholds, GlobalState.getInstance().getFeatureService());
    }

    public GeotabEngine(final Context ctx, final Thresholds thresholds, final IFeatureToggleService featureToggleService) {
        this(ctx, new GeotabMessageProcessor(featureToggleService, thresholds), featureToggleService, null, null, null, null);
    }

    public GeotabEngine(final Context ctx, IGeotabMessageProcessor messageProcessor, IFeatureToggleService featureToggleService, GeotabUsbService usbService, GeotabController geotabController, IFacadeFactory facadeFactory, AppSettings appSettings)
    {
        _hasSubmittedConnectionToGeotab = false;
        _context = ctx;
        _usbService = usbService;
        _messageProcessor = messageProcessor;
        _featureToggleService = featureToggleService;
        _watchdogTimer = new WatchdogTimer(30, TimeUnit.SECONDS, new Runnable() {
            @Override
            public void run() {
                String message = "Geotab watchdog timer expired (stopped receiving messages), closing connection.";

                LogCat.getInstance().e(TAG, message);
                ErrorLogHelper.RecordMessage(message);

                CloseDevice();
            }
        });

        final GeotabController geoController = geotabController == null ? new GeotabController(ctx) : geotabController;
        _facadeFactory = facadeFactory == null ? FacadeFactory.GetInstance() : facadeFactory;

        _appSettings = appSettings == null ? GlobalState.getInstance().getAppSettings(_context) : appSettings;

        _messageProcessor.setGeotabDataProcessed(new CodeBlocks.Action2<IHOSMessage, ArrayList<EventRecord>>() {
            @Override
            public void execute(IHOSMessage hosMessage, ArrayList<EventRecord> eventRecords) {

                for(EventRecord eventRecord : eventRecords) {
                    long timecode = eventRecord.getTimecode();

                    /*
                        The TAB never creates multiple events with the same timecode - they're always
                        separated by at least a millisecond.  Having multiple events with the same
                        timecode would result in losing events due to the way the reference timestamps work.

                        It's possible that the state machine could create multiple events from the same message,
                        in which case the timecodes would be the same.  We need to compensate here.

                        It's also possible that at a later time we create an event with a timecode we've used
                        previously - so store the timecodes so we can refer to them later. (Under AOBRD when
                        we finally create a DRIVE_ON we give it the timecode from when the vehicle started moving).
                     */

                    if(_generatedTimecodes == null)
                        _generatedTimecodes = new HashSet<>();

                    while(_generatedTimecodes.contains(timecode)) {
                        timecode += 1;
                    }

                    _generatedTimecodes.add(timecode);
                    eventRecord.setTimecode(timecode);
                }

                geoController.SaveEventsAndHosDataForDriver(_messageProcessor.getVehicleId(), eventRecords, hosMessage);
            }
        });

        if(_usbService == null) {
            _serviceConnectedLatch = new CountDownLatch(1);

            LogCat.getInstance().d(TAG, "Binding to GeotabUsbService");
            Intent intent = new Intent(ctx, GeotabUsbService.class);

            //start the service to keep it running beyond the lifecycle of one activity
            ctx.startService(intent);

            //bind to it so we can tell when it's alive
            ctx.bindService(intent, _serviceConnection, Context.BIND_AUTO_CREATE);
        } else {
            _usbService.addHosListener(this);
        }
    }

    @Override

    public void receiveGeotabData(GeotabDataEnhanced data) {
        _messageCount++;

        //conditionally skip message processing to simulate data stalling
        // from the GO7 without it actually disconnecting
        if(_featureToggleService.getGeotabInjectDataStallsEnabled()) {
            if(_messageCount > 125) {
                LogCat.getInstance().v(TAG, "Message count exceeded threshold, injecting data stream 'stall'");
                _stallData = true;
            }

            if(_stallData)
                return;
        }

        if(data != null) {
            LogCat.getInstance().v(TAG, data.toString());

            if(data.isDateTimeValid()) {
                _watchdogTimer.reset();

                _lastGeotabData = data;

                if (_receivedDataLatch != null)
                    _receivedDataLatch.countDown();

                IHOSMessage message = data.toHOSMessage();
                String serialNumber = data.getVehicleId();
                updateAndTrackTimestamp(message, serialNumber);


                _messageProcessor.setVehicleId(_lastGeotabData.getVehicleId());

                // jpp Inform the Geotab web service that the driver has changed
                if (!_hasSubmittedConnectionToGeotab) {
                    SubmitGeotabDriverChange();
                    _hasSubmittedConnectionToGeotab = true;
                }


                _messageProcessor.processHosMessage(message);
            }
        }
    }

    private void updateAndTrackTimestamp(IHOSMessage message, String serialNumber) {
        // Set timestamp fields
        message.setOriginalTimestampUtc(message.getTimestampUtc());
        message.setTimestampUtc(TimeKeeper.getInstance().getCurrentDateTime());

        long originalTimestamp = message.getOriginalTimestampUtc() == null ? 0 : message.getOriginalTimestampUtc().getMillis();
        long currentTimestamp = message.getTimestampUtc() == null ? 0 : message.getTimestampUtc().getMillis();

        // Track offset from DMO
        long offset = currentTimestamp - originalTimestamp;
        EobrConfiguration config = _facadeFactory.getEobrConfigurationFacade(_context).Fetch(serialNumber);
        if(config != null) {
            long previousOffset = config.getClockSyncOffset() == null ? 0 : config.getClockSyncOffset();

            if (offset - previousOffset != 0) {
                config.setClockSyncOffset(offset);
                config.setClockSyncDateUTC(TimeKeeper.getInstance().getCurrentDateTime());
                _facadeFactory.getEobrConfigurationFacade(_context).Save(serialNumber, config);
            }
        }
    }

    public static boolean IsDeviceAttached(Context ctx) {
        return GeotabUsbService.USBAccessoryAttached(ctx);
    }

    public void addListener(IGeotabListener listener) {
        if(_usbService != null)
            _usbService.addHosListener(listener);
    }

    public void removeListener(IGeotabListener listener) {
        if(_usbService != null)
            _usbService.removeHosListener(listener);
    }

    public Bundle GetVin() {
        return null;
    }

    public void ClearState() {
        _generatedTimecodes.clear();
        _unitId = "";
        _defaultThresholds = null;
        _driverThresholds = null;

        _messageProcessor.clearAllEobrData();
    }

    //region IEobrEngine
    @Override
    public BluetoothAdapter getBluetoothAdapter() {
        return null;
    }

    @Override
    public BluetoothSocket getBlueToothSocket() {
        return null;
    }

    @Override
    public String getCurrentBtAddress() {
        return null;
    }

    @Override
    public boolean getIsSocketConnected() {
        if(_usbService != null)
            return _usbService.USBAccessoryConnected();

        return false;
    }

    @Override
    public void ClearActiveDeviceCrc() {
        //intentionally left empty
    }

    @Override
    public void initializeConnectedDevices() {
        //intentionally left empty
    }

    @Override
    public int deleteBTAddress() {
        return 0;
    }

    @Override
    public EobrDeviceDescriptor[] getDiscoveredDeviceList() {
        return null;
    }

    @Override
    public void SetupActiveDevice(String deviceName, String btAddress, int eobrGen, short crc) {

    }

    @Override
    public String GetActiveDeviceAddress() {
        return null;
    }

    @Override
    public int searchForEobrDevices(List<BluetoothDevice> devices, String companyPasskey, String serialNumber) {
        return Enums.EobrReturnCode.S_SUCCESS;
    }

    @Override
    public int searchForEobrDevice(String companyPasskey, BluetoothDevice btDevice) {
        return Enums.EobrReturnCode.S_SUCCESS;
    }

    @Override
    public int OpenDevice(String deviceName) {
        try {
            _messageCount = 0;
            _stallData = false;

            LogCat.getInstance().d(TAG, "Waiting for GeotabUsbService to start");

            //if the service has been recently started, wait for it to connect
            boolean serviceStarted = _serviceConnectedLatch.await(10, TimeUnit.SECONDS);
            if (serviceStarted) {
                LogCat.getInstance().d(TAG, "Service started.");

                if(_usbService.USBAccessoryAttached()) {
                    //reset this latch - we need to wait for first data again
                    _receivedDataLatch = new CountDownLatch(1);

                    USBAccessoryConnectionStatus status = _usbService.open();

                    if (status == USBAccessoryConnectionStatus.CONNECTED) {
                        LogCat.getInstance().d(TAG, "Waiting for first valid data.");

                        //wait for the first receipt of data
                        boolean receivedData = _receivedDataLatch.await(10, TimeUnit.SECONDS);

                        if (receivedData) {
                            LogCat.getInstance().d(TAG, "Received first valid data.");

                            _messageProcessor.resetVehicleState();

                            //reset the timecodes
                            _generatedTimecodes = new HashSet<>();

                            //the data must flow
                            _watchdogTimer.start();

                            //refresh history from encompass
                            GeotabHistoryDownloader _geotabHistoryDownloader = new GeotabHistoryDownloader(_context, _appSettings, _lastGeotabData.getVehicleId());

                            _geotabHistoryDownloader.download();
                            _geotabHistoryDownloader.startTimer();

                            return EobrReturnCode.S_SUCCESS;
                        } else
                            LogCat.getInstance().e(TAG, "Timed out waiting for first data after connection.");
                    } else {
                        LogCat.getInstance().e(TAG, String.format("Device did not connect with status %s", status.name()));
                    }
                } else {
                    LogCat.getInstance().d(TAG, "No USB accessory is attached.");
                }
            } else {
                LogCat.getInstance().e(TAG, "Timed out waiting for service to start before opening device.");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return EobrReturnCode.S_DEV_NOT_CONNECTED;
    }

    @Override
    public int CloseDevice() {

        // Set the potential driving stop timestamp, this will dismiss clocks or re-enable controls
        // if the vehicle is in motion and the device gets disconnected 
        GlobalState.getInstance().setPotentialDrivingStopTimestamp(TimeKeeper.getInstance().getCurrentDateTime().toDate());

        GeotabController geotabController = new GeotabController(_context);
        clearErrorsUponDisconnection(_lastGeotabData, geotabController);

        if(_watchdogTimer != null)
            _watchdogTimer.stop();

        if(_usbService != null)
            _usbService.close();

        if(_hasSubmittedConnectionToGeotab) {
            geotabController.SubmitGeotabApiDriverChange(true);
            _hasSubmittedConnectionToGeotab = false;
        }

        return EobrReturnCode.S_SUCCESS;
    }

    private void clearErrorsUponDisconnection(GeotabDataEnhanced lastGeotabData, GeotabController geotabController){
        if(lastGeotabData == null) return;  // No action to take if last data isn't initialized

        HOSMessage lastHOSMessage = lastGeotabData.toHOSMessage();
        boolean isInVSSGPSErrorState = (!lastHOSMessage.isGpsValid() || !lastHOSMessage.isOdometerFromEngine() || !lastHOSMessage.isSpeedFromEngine());
        if(isInVSSGPSErrorState){
            List<EventRecord> listOfClearEvents = new ArrayList<EventRecord>();

            if(!lastHOSMessage.isOdometerFromEngine() || !lastHOSMessage.isSpeedFromEngine()){
                EventRecord clearErrorEventRecord = new EventRecord();
                clearErrorEventRecord.setTimecode(_lastGeotabData.getDatetime());
                clearErrorEventRecord.setEventType(EventTypeEnum.ERROR);
                clearErrorEventRecord.setEventData(0);

                listOfClearEvents.add(clearErrorEventRecord);
            }

            if(!lastHOSMessage.isGpsValid()){
                EventRecord clearGpsErrorEventRecord = new EventRecord();
                clearGpsErrorEventRecord.setTimecode(_lastGeotabData.getDatetime());
                clearGpsErrorEventRecord.setEventType(EventTypeEnum.GPS);
                clearGpsErrorEventRecord.setEventData(0);

                listOfClearEvents.add(clearGpsErrorEventRecord);
            }

            geotabController.SaveEventsAndHosDataForDriver(_lastGeotabData.getVehicleId(), listOfClearEvents, lastHOSMessage);

        }
    }

    @Override
    public int PingEobrDevice() {
        if(getIsSocketConnected())
            return EobrReturnCode.S_SUCCESS;
        else
            return EobrReturnCode.S_DEV_NOT_CONNECTED;
    }

    @Override
    public Bundle GetEobrSerialNumber() {
        int returnCode = getBasicReturnCode();

        //the vehicle ID is the last part of the serial number
        //For the serial number G7-A82-0D9-1B4C
        //G7 is the device
        //A8 is a checksum
        //20D91B4C is the vehicle ID
        Bundle bundle = BundleBuilder
                .withReturnCode(returnCode)
                .withReturnValue(String.valueOf(_lastGeotabData.getVehicleId()))
                .build();

        return bundle;
    }

    /*
        ToDo: Implement clock synchronization.
        For now, we're just going to return the last value we got from Geotab.
        Eventually we'll want to do some validation and come up with some sort of offset
        from DMO server time.
    */
    @Override
    public Bundle GetClockUTC() {
        long time = TimeKeeper.getInstance().now().getTime();

        if(_lastGeotabData != null)
            time = _lastGeotabData.getDatetime();

        int returnCode = getBasicReturnCode();

        Bundle bundle = BundleBuilder
                .withReturnCode(returnCode)
                .withReturnValue(time)
                .build();

        return bundle;
    }

    @Override
    public EobrResponse<Date> GetGPSTimestamp() {
        return null;
    }

    @Override
    public int SetClockUTC(Date newClock) {
        if(!getIsSocketConnected())
            return EobrReturnCode.S_DEV_NOT_CONNECTED;
        
        return EobrReturnCode.S_SUCCESS;
    }

    @Override
    public Bundle GetCompanyPasskey()
    {
        Bundle bundle = BundleBuilder
                .withReturnCode(EobrReturnCode.S_FUNC_NOT_IMPLEMENTED)
                .build();

        return bundle;
    }

    @Override
    public int SetCompanyPasskey(String passkey)
    {
        return EobrReturnCode.S_FUNC_NOT_IMPLEMENTED;
    }

    @Override
    public Bundle GetCustomParameter(int customParameterIndex) {
        Bundle bundle = BundleBuilder
                .withReturnCode(EobrReturnCode.S_FUNC_NOT_IMPLEMENTED)
                .build();

        return bundle;
    }

    @Override
    public int SetCustomParameter(int customParameter, int customParameterIndex) {
        return EobrReturnCode.S_FUNC_NOT_IMPLEMENTED;
    }

    //TODO: Implement calibration and offsets for real, just defaulting to 0 and faking a successful set
    @Override
    public Bundle GetEobrOdometerOffset() {
        int returnCode = getBasicReturnCode();
        
        Bundle bundle = BundleBuilder
                .withReturnCode(returnCode)
                .withReturnValue(0.0f)
                .build();

        return bundle;
    }

    @Override
    public int SetEobrOdometerOffset(float offset) {
        if(!getIsSocketConnected())
            return EobrReturnCode.S_DEV_NOT_CONNECTED;

        return EobrReturnCode.S_SUCCESS;
    }

    @Override
    public Bundle GetOdometerCalibration() {
        String serialnumber = GlobalState.getInstance().getCurrentEobrSerialNumber();
        EobrConfiguration config = FacadeFactory.GetInstance().getEobrConfigurationFacade(_context).Fetch(serialnumber);
        int returnCode = getBasicReturnCode();
        Bundle bundle = null;

        if(config != null){
            float odometerOffset = config.getDasboardOdometer() - config.getEobrOdometer();
            bundle = BundleBuilder
                    .withReturnCode(returnCode)
                    .withValue(Constants.OFFSETPARAM, odometerOffset)
                    .withValue(Constants.MULTIPLIERPARAM, 0.0f)
                    .build();
        }else{
            bundle = BundleBuilder
                    .withReturnCode(returnCode)
                    .withValue(Constants.OFFSETPARAM, 0.0f)
                    .withValue(Constants.MULTIPLIERPARAM, 0.0f)
                    .build();
        }
        return bundle;
    }

    @Override
    public int SetOdometerCalibration(float offset, float multiplier) {
        if(!getIsSocketConnected())
            return EobrReturnCode.S_DEV_NOT_CONNECTED;

        return EobrReturnCode.S_SUCCESS;
    }

    @Override
    public Bundle GetUnitId() {
        //This is only ever called by Technician_GetUniqueIdentifier - returning the Geotab VehicleId

        if(getIsSocketConnected() && _lastGeotabData != null) {
            if(_unitId != null && _unitId != "")
            return BundleBuilder
                    .withReturnCode(EobrReturnCode.S_SUCCESS)
                    .withReturnValue(_unitId)
                    .build();
            else
                return BundleBuilder
                        .withReturnCode(EobrReturnCode.S_SUCCESS)
                    .withReturnValue(_lastGeotabData.getVehicleId())
                    .build();
        } else {
            return BundleBuilder
                    .withReturnCode(EobrReturnCode.S_DEV_NOT_CONNECTED)
                    .build();
        }
    }

    @Override
    public int SetUnitId(String unitId) {
        if(!getIsSocketConnected())
            return EobrReturnCode.S_DEV_NOT_CONNECTED;
        _unitId = unitId;
        //we have no control over the Geotab VehicleId
        return EobrReturnCode.S_SUCCESS;
    }

    @Override
    public int GetEobrData(StatusRecord statusRec, StatusRecordQueryMethodEnum queryMethod, int recordId, Date timeCode, StatusRecordMotionOptionEnum motionOption, boolean resetReferenceTimestampToCurrent) {
        if(!getIsSocketConnected())
            return EobrReturnCode.S_DEV_NOT_CONNECTED;

        //the motionOption is only utilized by GenI, so ignore for now.
        long timestamp = timeCode != null ? timeCode.getTime() : 0;

        _messageProcessor.getEobrData(_context, statusRec, queryMethod, recordId, timestamp, resetReferenceTimestampToCurrent);

        return EobrReturnCode.S_SUCCESS;
    }

    @Override
    public Bundle GetEngineOffCommsTimeout() {
        int returnCode = getBasicReturnCode();
        
        Bundle bundle = BundleBuilder
                .withReturnCode(returnCode)
                .withReturnValue(0)
                .build();

        return bundle;
    }

    @Override
    public int SetEngineOffCommsTimeout(int timeoutInMinutes) {
        if(!getIsSocketConnected())
            return EobrReturnCode.S_DEV_NOT_CONNECTED;

        //irrelevant for Geotab
        return EobrReturnCode.S_SUCCESS;
    }

    @Override
    public Bundle ReadDataCollectionRate() {
        int returnCode = getBasicReturnCode();
        
        //always 2 seconds for Geotab
        Bundle bundle = BundleBuilder
                .withReturnCode(returnCode)
                .withReturnValue(2)
                .build();

        return bundle;
    }

    @Override
    public int ChangeDataCollectionRate(int newDataRate) {
        if(!getIsSocketConnected())
            return EobrReturnCode.S_DEV_NOT_CONNECTED;

        //can't be changed for Geotab
        return EobrReturnCode.S_SUCCESS;
    }

    @Override
    public Bundle GetReferenceTimestamp() {
        int returnCode = getBasicReturnCode();
        
        EobrReferenceTimestamps timestamps = _messageProcessor.getReferenceTimestamp();

        Bundle bundle = BundleBuilder
                .withReturnCode(returnCode)
                .withReturnValue(timestamps.getEobrReferenceTime())
                .withValue(Constants.EOBRREFTIME, timestamps.getEobrReferenceTime())
                .withValue(Constants.EVENTREFTIME, timestamps.getEventReferenceTime())
                .withValue(Constants.HISTREFTIME, timestamps.getHistogramReferenceTime())
                .withValue(Constants.TRIPREFTIME, timestamps.getTripReferenceTime())
                .withValue(Constants.DTCREFTIME, timestamps.getDtcReferenceTime())
                .build();

        return bundle;
    }

    @Override
    public Bundle GetDistHours(long timecode) {
        int returnCode = getBasicReturnCode();
        
        TripDistanceHours distanceHours = _messageProcessor.getTripDistanceHours(timecode);

        Bundle bundle = BundleBuilder
                .withReturnCode(EobrReturnCode.S_SUCCESS)
                .withValue(Constants.TRIPDISTANCE, 0)
                .withValue(Constants.TRIPRUNTIME, 0)
                .build();

        return bundle;
    }

    @Override
    public Bundle GetActiveBusType() {
        //we don't have visibility into the GO7's databus type
        // 10/14/16 JMoen- Using J1708 to prevent EobrConfig submit failure to Encompass.
        // Value is the default for a "dummy" ELD created by the web service

        int returnCode = getBasicReturnCode();
        
        Bundle bundle = BundleBuilder
                .withReturnCode(returnCode)
                .withReturnValue(DatabusTypeEnum.J1708)
                .build();

        return bundle;
    }

    @Override
    public int ChangeActiveBusType(int newBusType) {
        if(!getIsSocketConnected())
            return EobrReturnCode.S_DEV_NOT_CONNECTED;

        //can't change the bus type for Geotab
        return EobrReturnCode.S_SUCCESS;
    }

    @Override
    public Bundle GetEOBRDllRevisions() {
        int returnCode = getBasicReturnCode();
        
        Bundle bundle = BundleBuilder
                .withReturnCode(returnCode)
                .withValue(Constants.MAINFIRMWAREREVISION, Constants.GEOTAB_FIRMWARE_REVISION)
                .withValue(Constants.USBFIRMWAREREVISION, Constants.GEOTAB_FIRMWARE_REVISION)
                .withValue(Constants.RECORDREVISION, Constants.GEOTAB_FIRMWARE_REVISION)
                .withValue(Constants.BOOTLOADERREVISION, Constants.GEOTAB_FIRMWARE_REVISION)
                .withValue(Constants.EOBRDLLSREVISION, Constants.EOBR_DLLS_REVISION)
                .build();

        return bundle;
    }

    @Override
    public int SetDebugFlags(int debugFlags) {
        if(!getIsSocketConnected())
            return EobrReturnCode.S_DEV_NOT_CONNECTED;

        return EobrReturnCode.S_SUCCESS;
    }

    @Override
    public Bundle SendConsoleCommandToDevice(String command) {
        int returnCode = getBasicReturnCode();
        
        return BundleBuilder
                .withReturnCode(returnCode)
                .withReturnValue("")
                .build();
    }

    @Override
    public Bundle SendConsoleCommandToDeviceWithNoRetry(String command) {
        return SendConsoleCommandToDevice(command);
    }

    @Override
    public boolean SetSelfTest() {
        return true;
    }

    @Override
    public Bundle GetSelfTest() {
        int returnCode = getBasicReturnCode();
        
        return BundleBuilder
                .withReturnCode(returnCode)
                .withReturnValue(0)
                .build();
    }

    @Override
    public int ClearAllRecordData(int clearFlags) {
        _messageProcessor.clearAllRecordData(clearFlags);

        return EobrReturnCode.S_SUCCESS;
    }

    @Override
    public int DownloadFirmwareUpdate(InputStream firmwareUpdateFile, com.jjkeller.kmbapi.eobrengine.Enums.FirmwareUpgradeTypeEnum firmwareUpgradeType, FirmwareUpdateBroadcaster broadcaster, FirmwareUpdate firmwareUpdateConfig) {
        return EobrReturnCode.S_FUNC_NOT_IMPLEMENTED;
    }

    @Override
    public Bundle GetThresholdValues(int thresholdType) {
        Thresholds thresholds;

        if(thresholdType == 0)
            thresholds = _driverThresholds;
        else
            thresholds = _defaultThresholds;

        if(thresholds == null || !getIsSocketConnected()) {
            return BundleBuilder
                    .withReturnCode(EobrReturnCode.S_DEV_NOT_CONNECTED)
                    .build();
        }

        BundleBuilder builder = thresholds.toBundleBuilder();

        return builder
                .withReturnCode(EobrReturnCode.S_SUCCESS)
                .build();
    }

    @Override
    public Bundle SetThresholdValues(int rpmThreshold, float speedThreshold, float hardBrakeThreshold, float driveStartDistance, int driveStopTime, int eventBlanking, String driverId, float driveStartSpeed) {
        Thresholds thresholds = new Thresholds(rpmThreshold, speedThreshold, hardBrakeThreshold, driveStartDistance, driveStartSpeed, driveStopTime, driverId, eventBlanking);

        if(thresholds.getThresholdType() == ThresholdTypeEnum.DEFAULT)
            _defaultThresholds = thresholds;
        else
            _driverThresholds = thresholds;

        _messageProcessor.setThresholdValues(thresholds);

        int returnCode = getBasicReturnCode();
        
        Bundle bundle = BundleBuilder
                .withReturnCode(returnCode)
                .withValue(Constants.DRIVERIDCRC, thresholds.getDriverIdCRC())
                .build();

        return bundle;
    }

    @Override
    public int GetEventData(EventRecord eventRecordData, StatusRecordQueryMethodEnum queryMethod, int recordId, long timeCode, EventTypeEnum eventType, boolean resetReferenceTimestampToCurrent) {
        if(!getIsSocketConnected())
            return EobrReturnCode.S_DEV_NOT_CONNECTED;

        _messageProcessor.getEventData(_context, eventRecordData, queryMethod, recordId, timeCode, eventType, resetReferenceTimestampToCurrent);

        return EobrReturnCode.S_SUCCESS;
    }

    @Override
    public int GetEventData(EventRecord eventRecordData, StatusRecordQueryMethodEnum queryMethod, int recordId, long timeCode, EventTypeEnum eventType, boolean resetReferenceTimestampToCurrent, int eventMask)
    {
        if(!getIsSocketConnected())
            return EobrReturnCode.S_DEV_NOT_CONNECTED;

        _messageProcessor.getEventData(_context, eventRecordData, queryMethod, recordId, timeCode, eventType, resetReferenceTimestampToCurrent, eventMask);

        return EobrReturnCode.S_SUCCESS;
    }

    @Override
    public int GetTripData(TripReport tripReport, StatusRecordQueryMethodEnum queryMethod, int recordId, long timeCode, boolean resetReferenceTimestampToCurrent) {
        if(!getIsSocketConnected()) {
            return EobrReturnCode.S_DEV_NOT_CONNECTED;
        }
        EventRecord driverEvent = new EventRecord();
        int returnCode = EobrReader.getInstance().GetLastEventOfType(driverEvent, new EventTypeEnum(EventTypeEnum.DRIVER));
        if (returnCode == EobrReturnCode.S_SUCCESS) {
            if (_lastGeotabData != null) {
                _messageProcessor.setVehicleId(_lastGeotabData.getVehicleId());
                _messageProcessor.getTripData(_context, tripReport, queryMethod, recordId, timeCode, resetReferenceTimestampToCurrent);
            }
        } else {
            ErrorLogHelper.RecordMessage(String.format("GetTripData failed to get last driver event with code %d", returnCode));
        }
        return returnCode;
    }

    @Override
    public int GetHistogramData(HistogramData histogramData, StatusRecordQueryMethodEnum queryMethod, int recordId, long timeCode, HistogramTypeEnum histogramType, boolean setRefTime) {
        if(!getIsSocketConnected())
            return EobrReturnCode.S_DEV_NOT_CONNECTED;

        _messageProcessor.getHistogramData(_context, histogramData, queryMethod, recordId, timeCode, histogramType, setRefTime);

        return EobrReturnCode.S_SUCCESS;
    }

    @Override
    public int GetJBusDiagnosticDataFromDevice(JbusDiagnosticData diagnosticData, StatusRecordQueryMethodEnum queryMethod, int recordId, long timestamp, boolean setRefTime) {
        //This type of data is also known as "DTC" data.  Implement, potentially, at some point in the future
        if(!getIsSocketConnected())
            return EobrReturnCode.S_DEV_NOT_CONNECTED;

        return EobrReturnCode.S_SUCCESS;
    }

    @Override
    public Bundle GetConsoleLog(Date startDate, Date endDate) {
        int returnCode = getBasicReturnCode();
        
        return BundleBuilder
                .withReturnCode(returnCode)
                .withReturnValue("")
                .build();
    }

    @Override
    public int GetEobrGeneration() {
        return Constants.GENERATION_GEOTAB;
    }

    @Override
    public void ClearAllEobrData() {
        _messageProcessor.clearAllEobrData();
    }

    @Override
    public boolean IsJJK(Context ctx) { return false; }

    @Override
    public FirmwareUpgradeRequestResult RequestFirmwareUpgrade(long firmwarePatchId) {
        return new FirmwareUpgradeRequestResult(EobrReturnCode.S_FUNC_NOT_IMPLEMENTED);
    }

    @Override
    public FirmwareUpgradeStatusResult GetFirmwareUpgradeStatus() {
        return new FirmwareUpgradeStatusResult(EobrReturnCode.S_FUNC_NOT_IMPLEMENTED);
    }

    @Override
    public EobrResponse<DriveData> GetDriveData(DriveDataTypeEnum typeEnum, long timeCode, short timeStep, short maxUncertainty) {

        EobrResponse response = new EobrResponse<DriveData>(EobrReturnCode.S_SUCCESS);
        DriveData driveData = new DriveData();
        driveData.setVehicleLocations(new ArrayList<VehicleLocation>());
        response.setData(driveData);
        return response;
    }

    @Override
    public int SetReferenceTimestamps(EobrReferenceTimestamps timestamps) {
        if(!getIsSocketConnected())
            return EobrReturnCode.S_DEV_NOT_CONNECTED;
        
        _messageProcessor.setReferenceTimestamp(timestamps);

        return EobrReturnCode.S_SUCCESS;
    }

    @Override
    public boolean IsGetDriveDataSupported() {
        return true;
    }

    @Override
    public boolean IsGetEventDataEventMaskSupported() {
        return true;
    }

    @Override
    public int SetIsEldMandate(boolean isEldMandate) {
        if(!getIsSocketConnected())
            return EobrReturnCode.S_DEV_NOT_CONNECTED;

        //TODO: Implement the ability for the HosMessageProcessor to switch ELD mandate state
        //independent of the feature toggle it reads on initialization

        return EobrReturnCode.S_SUCCESS;
    }

    @Override
    public int SetDisableReadEldVin(boolean isEldReadingVin) {
        if(!getIsSocketConnected())
            return EobrReturnCode.S_DEV_NOT_CONNECTED;

        //we have no control over whether or not the GO7 device
        //reads the vehicle VIN
        return EobrReturnCode.S_SUCCESS;
    }

    @Override
    public Bundle GetDisableReadEldVin() {
        int returnCode = getBasicReturnCode();
        
        //assume VIN reading isn't disabled
        return BundleBuilder
                .withReturnCode(returnCode)
                .withReturnValue(false)
                .build();
    }



    @Override
    public EobrResponse<StatusBuffer> GetStatusBuffer() {
        EobrResponse<StatusBuffer> result = new EobrResponse<>(getBasicReturnCode());
        StatusBuffer statusBuffer = new StatusBuffer();

        statusBuffer.setLastEobrId(GeotabConstants.MOST_RECENT_RECORD_ID);
        result.setData(statusBuffer);

        return result;
    }

    @Override
    public int GetDriverEvent(EventRecord eventData, long startTimeCode, long endTimeCode, int eventMask, boolean includeEventsWithoutDriverId) {
        return EobrReturnCode.S_FUNC_NOT_IMPLEMENTED;
    }

    @Override
    public Bundle GetDriverCount(long startTimeCode, long endTimeCode) {
        int returnCode = getBasicReturnCode();

        Bundle bundle = BundleBuilder
                .withReturnCode(returnCode)
                .withReturnValue(0)
                .build();

        return bundle;
    }

    //endregion
    
    private int getBasicReturnCode() {
        return getIsSocketConnected() ? EobrReturnCode.S_SUCCESS : EobrReturnCode.S_DEV_NOT_CONNECTED;
    }

    public Bundle GetEobrHardware() {
        Bundle b = new Bundle();
        b.putInt(Constants.RETURNCODE,EobrReturnCode.S_FUNC_NOT_IMPLEMENTED);
        return b;
    }

    public void SubmitGeotabDriverChange() {

        String employeeCode = GlobalState.getInstance()
                .getCurrentDesignatedDriver()
                .getCredentials()
                .getEmployeeCode();

        GeotabController geotabController = new GeotabController(_context);
        geotabController.SubmitGeotabDriverChangeTaskRun(_messageProcessor.getVehicleId(), employeeCode);
    }

}
