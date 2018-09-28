package com.jjkeller.kmb.test.kmbapi.eobrengine;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;

import com.jjkeller.kmb.test.KMBRoboElectricTestRunner;
import com.jjkeller.kmbapi.configuration.FirmwareUpdate;
import com.jjkeller.kmbapi.eobrengine.Enums.FirmwareUpgradeTypeEnum;
import com.jjkeller.kmbapi.eobrengine.EobrDeviceDescriptor;
import com.jjkeller.kmbapi.eobrengine.EobrEngineBase;
import com.jjkeller.kmbapi.eobrengine.eobrdatatypes.Eobr_Data_Packet;
import com.jjkeller.kmbapi.eobrengine.eobrdatatypes.Eobr_Data_Packet_GenII;
import com.jjkeller.kmbapi.eobrengine.eobrdatatypes.Eobr_FW_Block_Packet;
import com.jjkeller.kmbapi.eobrengine.eobrdatatypes.Eobr_Packet;
import com.jjkeller.kmbapi.eobrengine.eobrdatatypes.Eobr_Driver_Event_Packet;
import com.jjkeller.kmbapi.firmwareupgrade.FirmwareUpdateBroadcaster;
import com.jjkeller.kmbapi.kmbeobr.DriveData;
import com.jjkeller.kmbapi.kmbeobr.DriveDataTypeEnum;
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

import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

@RunWith(KMBRoboElectricTestRunner.class)
public class EobrEngineBaseTests extends TestCase {
    @Test
    public void testReadBtName_NoActiveEobrs_ReturnEmptyString() {
        TestableEobrEngine reader = new TestableEobrEngine();

        // No Data in array
        String name = reader.ReadBtName(0);

        Assert.assertTrue(name != null);
    }

    @Test
    public void testReadBtAddress_NoActiveEobrs_ReturnEmptyString() {
        TestableEobrEngine reader = new TestableEobrEngine();

        // No Data in array
        String name = reader.ReadBtAddress(0);

        Assert.assertTrue(name != null);
    }

    @Test
    public void testReadEobrCRC_NoActiveEobrs_ReturnEmptyString() {
        TestableEobrEngine reader = new TestableEobrEngine();

        // No Data in array
        short result = reader.ReadEobrCRC(0);

        Assert.assertTrue(result == 0);
    }

    @Test
    public void testReadEobrGen_NoActiveEobrs_ReturnEmptyString() {
        TestableEobrEngine reader = new TestableEobrEngine();

        // No Data in array
        int result = reader.ReadEobrGen(0);

        Assert.assertTrue(result == 0);
    }

    @Test
    public void testGetDiscoveredDeviceList_NoActiveEobrs_ReturnEmptyArray() {
        TestableEobrEngine reader = new TestableEobrEngine();

        // No Data in array
        EobrDeviceDescriptor[] result = reader.getDiscoveredDeviceList();

        Assert.assertTrue(result.length == 0);
    }

    private class TestableEobrEngine extends EobrEngineBase {

        public String ReadBtAddress(int index) {
            return super.ReadBtAddress(index);
        }

        public short ReadEobrCRC(int index) {
            return super.ReadEobrCRC(index);
        }

        public int ReadEobrGen(int index) {
            return super.ReadEobrGen(index);
        }

        @Override
        public BluetoothAdapter getBluetoothAdapter() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public BluetoothSocket getBlueToothSocket() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getCurrentBtAddress() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean getIsSocketConnected() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public int deleteBTAddress() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int searchForEobrDevices(List<BluetoothDevice> devices,
                                        String companyPasskey, String serialNumber) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int searchForEobrDevice(String companyPasskey,
                                       BluetoothDevice btDevice) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int OpenDevice(String deviceName) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int CloseDevice() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int PingEobrDevice() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public Bundle GetEobrSerialNumber() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Bundle GetClockUTC() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public EobrResponse<Date> GetGPSTimestamp() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public int SetClockUTC(Date newClock) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public Bundle GetCompanyPasskey() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public int SetCompanyPasskey(String passkey) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public Bundle GetCustomParameter(int customParameterIndex) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public int SetCustomParameter(int customParameter,
                                      int customParameterIndex) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public Bundle GetEobrOdometerOffset() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public int SetEobrOdometerOffset(float offset) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public Bundle GetUnitId() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public int SetUnitId(String unitId) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int GetEobrData(StatusRecord statusRec,
                               StatusRecordQueryMethodEnum queryMethod, int recordId,
                               Date timeCode, StatusRecordMotionOptionEnum motionOption,
                               boolean resetReferenceTimestampToCurrent) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public Bundle GetEngineOffCommsTimeout() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public int SetEngineOffCommsTimeout(int timeoutInMinutes) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public Bundle ReadDataCollectionRate() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public int ChangeDataCollectionRate(int newDataRate) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public Bundle GetReferenceTimestamp() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Bundle GetDistHours(long timecode) {
            return null;
        }


        @Override
        public Bundle GetActiveBusType() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public int ChangeActiveBusType(int newBusType) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public Bundle GetEOBRDllRevisions() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public int SetDebugFlags(int debugFlags) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public Bundle SendConsoleCommandToDevice(String command) {
            // TODO Auto-generated method stub
            return new Bundle();
        }

        @Override
        public Bundle SendConsoleCommandToDeviceWithNoRetry(String command) {
            // TODO Auto-generated method stub
            return new Bundle();
        }

        @Override
        public boolean SetSelfTest() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public Bundle GetSelfTest() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public int ClearAllRecordData(int clearFlags) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int DownloadFirmwareUpdate(InputStream firmwareUpdateFile,
                                          FirmwareUpgradeTypeEnum firmwareUpgradeType,
                                          FirmwareUpdateBroadcaster broadcaster,
                                           FirmwareUpdate firmwareUpdateConfig) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public Bundle GetThresholdValues(int thresholdType) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Bundle SetThresholdValues(int rpmThreshold,
                                         float speedThreshold, float hardBrakeThreshold,
                                         float driveStartDistance, int driveStopTime, int eventBlanking,
                                         String driverId, float driveStartSpeed) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public int GetEventData(EventRecord eventRecordData,
                                StatusRecordQueryMethodEnum queryMethod, int recordId,
                                long timeCode, EventTypeEnum eventType,
                                boolean resetReferenceTimestampToCurrent) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int GetEventData(EventRecord eventRecordData,
                                StatusRecordQueryMethodEnum queryMethod, int recordId,
                                long timeCode, EventTypeEnum eventType,
                                boolean resetReferenceTimestampToCurrent, int eventMask) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int GetTripData(TripReport eventData,
                               StatusRecordQueryMethodEnum queryMethod, int recordId,
                               long timeCode, boolean resetReferenceTimestampToCurrent) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int GetHistogramData(HistogramData histogramData,
                                    StatusRecordQueryMethodEnum queryMethod, int recordId,
                                    long timeCode, HistogramTypeEnum histogramType,
                                    boolean setRefTime) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int GetJBusDiagnosticDataFromDevice(
                JbusDiagnosticData diagnosticData,
                StatusRecordQueryMethodEnum queryMethod, int recordId,
                long timestamp, boolean setRefTime) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public Bundle GetConsoleLog(Date startDate, Date endDate) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean IsJJK(Context ctx) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public int GetEobrGeneration() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public void ClearAllEobrData() {
            // TODO Auto-generated method stub

        }

        @Override
        public byte[] receiveBulkData(boolean eobrData) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean sendBulkData(Eobr_Packet eobrPacket, int packetSize) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean sendBulkData(Eobr_FW_Block_Packet eobrPacket,
                                    int packetSize) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean sendEobrDataPacketBulkData(Eobr_Data_Packet dataPacket) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean sendEobrDataPacketBulkData(
                Eobr_Data_Packet_GenII dataPacket) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public FirmwareUpgradeRequestResult RequestFirmwareUpgrade(
                long firmwarePatchId) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public FirmwareUpgradeStatusResult GetFirmwareUpgradeStatus() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public EobrResponse<DriveData> GetDriveData(DriveDataTypeEnum typeEnum,
                                                    long timeCode, short timeStep, short maxUncertainty) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public int SetReferenceTimestamps(EobrReferenceTimestamps timestamps) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public boolean IsGetDriveDataSupported() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean IsGetEventDataEventMaskSupported() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public int SetIsEldMandate(boolean isEldMandate) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int SetDisableReadEldVin(boolean isEldReadingVin) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public Bundle GetDisableReadEldVin() {
            // TODO Auto-generated method stub
            return null;
        }


        @Override
        public EobrResponse<StatusBuffer> GetStatusBuffer() {
            return null;
        }

        @Override
        public boolean VerifySocketConnection() {
            return true;
        }

        @Override
        public boolean sendEobrFirmwareBlockPacket(Eobr_FW_Block_Packet blockPacket) {
            return true;
        }

        @Override
        public Bundle GetVin() {
            return null;
        }

        public Bundle GetEobrHardware(){
            return  null;
        }

        @Override
        public int GetDriverEvent(EventRecord eventData, long startTimeCode, long endTimeCode, int eventMask, boolean includeEventsWithoutDriverId) {
            return 0;
        }

        @Override
        public boolean sendEobrDriverEventPacket(Eobr_Driver_Event_Packet driverPacket){
            return false;
        }

        @Override
        public Bundle GetDriverCount(long startTimeCode, long endTimeCode) {
            return null;
        }
    }
}
