package com.jjkeller.kmbapi.kmbeobr;

import junit.framework.Assert;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.nio.ByteBuffer;

@RunWith(JUnit4.class)
public class StatusBufferTest {

    @Test
    public void FromByteBuffer_should_read_correctly() throws Exception {
        /*
          Cmd: 0x30
          Len: 152
          TabStatus:
          Timecode: 0x00000159e196ad33 (2017-01-27 20:21:05.459)
          Resets: 7
          CumUptime: 9620 s
          Uptime: 2025 s
          Trips: 5
          RunTime: 5584 s
          OnTime: 2002 s
          OffTime: 0 s
          Odometer: 48.9 km
          CmdCount: 65
          TxPkts: 1896
          RxPkts: 96
          TxBytes: 139024
          RxBytes: 720
          CumAir: 87923.67 gm
          EobrStat: 0x00000000
          DriverId: 0x0000c782
          ActiveBus: 9
          LastEobrId: 0x5001dd1a
          EobrRefTime:  0x00000159e1966ca1 (2017-01-27 20:20:48.929)
          EventRefTime: 0x00000159e191f0a8 (2017-01-27 20:15:55.048)
          HistRefTime:  0x00000159e191ef89 (2017-01-27 20:15:54.761)
          TripRefTime:  0x00000159e191f0a0 (2017-01-27 20:15:55.040)
          DtcRefTime:   0x00000159e1835fc3 (2017-01-27 20:00:00.451)
          SynOdometer: 48900.000 m
          OdoOffset: 0 m
          CumIMAP: 426235492
          TotalFuelUsed: 489.00 L
          EngOnTime: 5579 s
         */
        ByteBuffer buffer = ByteBuffer.wrap(new byte[] {
                (byte) 0x30, (byte) 0x98,
                (byte) 0x33, (byte) 0xAD, (byte) 0x96, (byte) 0xE1, (byte) 0x59, (byte) 0x01, (byte) 0x00, (byte) 0x00, // timecode
                (byte) 0x07, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x94, (byte) 0x25, (byte) 0x00, (byte) 0x00, // resets, cumulative uptime seconds
                (byte) 0xE9, (byte) 0x07, (byte) 0x00, (byte) 0x00, (byte) 0x05, (byte) 0x00, (byte) 0x00, (byte) 0x00, // uptime seconds, number of trips
                (byte) 0xD0, (byte) 0x15, (byte) 0x00, (byte) 0x00, (byte) 0xD2, (byte) 0x07, (byte) 0x00, (byte) 0x00, // run time seconds, on time seconds
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xE9, (byte) 0x01, (byte) 0x00, (byte) 0x00, // off time, odometer km * 10
                (byte) 0x41, (byte) 0x00, (byte) 0x00, (byte) 0x00, // command count
                (byte) 0x68, (byte) 0x07, (byte) 0x00, (byte) 0x00, (byte) 0x60, (byte) 0x00, (byte) 0x00, (byte) 0x00, // BT tx packets, BT rx packets
                (byte) 0x10, (byte) 0x1F, (byte) 0x02, (byte) 0x00, (byte) 0xD0, (byte) 0x02, (byte) 0x00, (byte) 0x00, // BT tx bytes, BT rx bytes
                (byte) 0x2F, (byte) 0x29, (byte) 0x86, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // cumulative MAF grams * 100
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x82, (byte) 0xC7, (byte) 0x00, (byte) 0x00, // EOBR status, driver ID
                (byte) 0x09, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x1A, (byte) 0xDD, (byte) 0x01, (byte) 0x50, // active bus type, last EOBR ID
                (byte) 0xA1, (byte) 0x6C, (byte) 0x96, (byte) 0xE1, (byte) 0x59, (byte) 0x01, (byte) 0x00, (byte) 0x00, // start reference timestamps
                (byte) 0xA8, (byte) 0xF0, (byte) 0x91, (byte) 0xE1, (byte) 0x59, (byte) 0x01, (byte) 0x00, (byte) 0x00,
                (byte) 0x89, (byte) 0xEF, (byte) 0x91, (byte) 0xE1, (byte) 0x59, (byte) 0x01, (byte) 0x00, (byte) 0x00,
                (byte) 0xA0, (byte) 0xF0, (byte) 0x91, (byte) 0xE1, (byte) 0x59, (byte) 0x01, (byte) 0x00, (byte) 0x00,
                (byte) 0xC3, (byte) 0x5F, (byte) 0x83, (byte) 0xE1, (byte) 0x59, (byte) 0x01, (byte) 0x00, (byte) 0x00, // last reference timestamp
                (byte) 0xA0, (byte) 0x27, (byte) 0xEA, (byte) 0x02, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // synthetic odometer * 1000
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // odometer offset
                (byte) 0x64, (byte) 0xD6, (byte) 0x67, (byte) 0x19, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // cumulative IMAP
                (byte) 0x04, (byte) 0xBF, (byte) 0x00, (byte) 0x00, (byte) 0xCB, (byte) 0x15, (byte) 0x00, (byte) 0x00, // total fuel used * 100, engine on time
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // extra bytes
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00
        }, 2, 152);

        StatusBuffer result = StatusBuffer.FromByteBuffer(buffer);

        Assert.assertNotNull(result);
        Assert.assertEquals(new DateTime(2017, 1, 27, 20, 21, 5, 459, DateTimeZone.UTC).getMillis(), result.getTimecode());
        Assert.assertEquals(7, result.getResets());
        Assert.assertEquals(9620, result.getCumulativeUptimeSeconds());
        Assert.assertEquals(2025, result.getUptimeSeconds());
        Assert.assertEquals(5, result.getNumberOfTrips());
        Assert.assertEquals(5584, result.getRunTimeSeconds());
        Assert.assertEquals(2002, result.getIgnitionOnTimeSeconds());
        Assert.assertEquals(0, result.getIgnitionOffTimeSeconds());
        Assert.assertEquals(48.9f, result.getOdometerKilometers());
        Assert.assertEquals(65, result.getTotalConsoleCommandCount());
        Assert.assertEquals(1896, result.getBluetoothPacketTxCount());
        Assert.assertEquals(96, result.getBluetoothPacketRxCount());
        Assert.assertEquals(139024, result.getBluetoothPacketTxByteCount());
        Assert.assertEquals(720, result.getBluetoothPacketRxByteCount());
        Assert.assertEquals(87923.67, result.getCumulativeMAFGrams());
        Assert.assertEquals(0, result.getOverallComponentStatus());
        Assert.assertEquals(0x0000c782, result.getDriverId());
        Assert.assertEquals(9, result.getActiveBusType());
        Assert.assertEquals(0x5001dd1a, result.getLastEobrId());
        Assert.assertEquals(0x159e1966ca1L, result.getEobrReferenceTimestamp());
        Assert.assertEquals(0x159e191f0a8L, result.getEventReferenceTimestamp());
        Assert.assertEquals(0x159e191ef89L, result.getHistogramReferenceTimestamp());
        Assert.assertEquals(0x159e191f0a0L, result.getTripReferenceTimestamp());
        Assert.assertEquals(0x159e1835fc3L, result.getDtcReferenceTimestamp());
        Assert.assertEquals(48900.0, result.getSyntheticOdometerMeters());
        Assert.assertEquals(0, result.getOdometerOffsetMeters());
        Assert.assertEquals(426235492, result.getCumulativeIMAP());
        Assert.assertEquals(489.0, result.getTotalFuelUsedLiters());
        Assert.assertEquals(5579, result.getEngineOnTimeSeconds());
    }

    @Test
    public void FromByteBuffer_should_read_unsigned_values_correctly() throws Exception {
        ByteBuffer buffer = ByteBuffer.wrap(new byte[] {
                (byte) 0x30, (byte) 0x98,
                (byte) 0x33, (byte) 0xAD, (byte) 0x96, (byte) 0xE1, (byte) 0x59, (byte) 0x01, (byte) 0x00, (byte) 0x00, // timecode
                (byte) 0x07, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x94, (byte) 0x25, (byte) 0x00, (byte) 0x00, // resets, cumulative uptime seconds
                (byte) 0xE9, (byte) 0x07, (byte) 0x00, (byte) 0x00, (byte) 0x05, (byte) 0x00, (byte) 0x00, (byte) 0x00, // uptime seconds, number of trips
                (byte) 0xD0, (byte) 0x15, (byte) 0x00, (byte) 0x00, (byte) 0xD2, (byte) 0x07, (byte) 0x00, (byte) 0x00, // run time seconds, on time seconds
                (byte) 0xFF, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x2D, (byte) 0x31, (byte) 0x01, // off time, odometer km * 10
                (byte) 0x41, (byte) 0x00, (byte) 0x00, (byte) 0x00, // command count
                (byte) 0x68, (byte) 0x07, (byte) 0x00, (byte) 0x00, (byte) 0x60, (byte) 0x00, (byte) 0x00, (byte) 0x00, // BT tx packets, BT rx packets
                (byte) 0x56, (byte) 0x34, (byte) 0x12, (byte) 0xFF, (byte) 0xA0, (byte) 0xCB, (byte) 0xED, (byte) 0xFF, // BT tx bytes, BT rx bytes
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x00, (byte) 0x00, // cumulative MAF grams * 100
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x82, (byte) 0xC7, (byte) 0x00, (byte) 0x00, // EOBR status, driver ID
                (byte) 0x09, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x1A, (byte) 0xDD, (byte) 0x01, (byte) 0x50, // active bus type, last EOBR ID
                (byte) 0xA1, (byte) 0x6C, (byte) 0x96, (byte) 0xE1, (byte) 0x59, (byte) 0x01, (byte) 0x00, (byte) 0x00, // start reference timestamps
                (byte) 0xA8, (byte) 0xF0, (byte) 0x91, (byte) 0xE1, (byte) 0x59, (byte) 0x01, (byte) 0x00, (byte) 0x00,
                (byte) 0x89, (byte) 0xEF, (byte) 0x91, (byte) 0xE1, (byte) 0x59, (byte) 0x01, (byte) 0x00, (byte) 0x00,
                (byte) 0xA0, (byte) 0xF0, (byte) 0x91, (byte) 0xE1, (byte) 0x59, (byte) 0x01, (byte) 0x00, (byte) 0x00,
                (byte) 0xC3, (byte) 0x5F, (byte) 0x83, (byte) 0xE1, (byte) 0x59, (byte) 0x01, (byte) 0x00, (byte) 0x00, // last reference timestamp
                (byte) 0xCB, (byte) 0x84, (byte) 0xC5, (byte) 0xAB, (byte) 0x80, (byte) 0x25, (byte) 0x00, (byte) 0x00, // synthetic odometer * 1000
                (byte) 0xFF, (byte) 0xFF, (byte) 0x0F, (byte) 0x00, // odometer offset
                (byte) 0xD2, (byte) 0x92, (byte) 0x0B, (byte) 0x6E, (byte) 0x3D, (byte) 0x24, (byte) 0x01, (byte) 0x00, // cumulative IMAP
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x00, (byte) 0xCB, (byte) 0x15, (byte) 0x00, (byte) 0x00, // total fuel used * 100, engine on time
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // extra bytes
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00
        }, 2, 152);

        StatusBuffer result = StatusBuffer.FromByteBuffer(buffer);

        Assert.assertNotNull(result);
        Assert.assertEquals(new DateTime(2017, 1, 27, 20, 21, 5, 459, DateTimeZone.UTC).getMillis(), result.getTimecode());
        Assert.assertEquals(7, result.getResets());
        Assert.assertEquals(9620, result.getCumulativeUptimeSeconds());
        Assert.assertEquals(2025, result.getUptimeSeconds());
        Assert.assertEquals(5, result.getNumberOfTrips());
        Assert.assertEquals(5584, result.getRunTimeSeconds());
        Assert.assertEquals(2002, result.getIgnitionOnTimeSeconds());
        Assert.assertEquals(255, result.getIgnitionOffTimeSeconds());
        Assert.assertEquals(2_000_000.0f, result.getOdometerKilometers());
        Assert.assertEquals(65, result.getTotalConsoleCommandCount());
        Assert.assertEquals(1896, result.getBluetoothPacketTxCount());
        Assert.assertEquals(96, result.getBluetoothPacketRxCount());
        Assert.assertEquals(4_279_383_126L, result.getBluetoothPacketTxByteCount());
        Assert.assertEquals(4_293_774_240L, result.getBluetoothPacketRxByteCount());
        Assert.assertEquals(2_814_749_767_106.55, result.getCumulativeMAFGrams());
        Assert.assertEquals(-1, result.getOverallComponentStatus());
        Assert.assertEquals(0x0000c782, result.getDriverId());
        Assert.assertEquals(9, result.getActiveBusType());
        Assert.assertEquals(0x5001dd1a, result.getLastEobrId());
        Assert.assertEquals(0x159e1966ca1L, result.getEobrReferenceTimestamp());
        Assert.assertEquals(0x159e191f0a8L, result.getEventReferenceTimestamp());
        Assert.assertEquals(0x159e191ef89L, result.getHistogramReferenceTimestamp());
        Assert.assertEquals(0x159e191f0a0L, result.getTripReferenceTimestamp());
        Assert.assertEquals(0x159e1835fc3L, result.getDtcReferenceTimestamp());
        Assert.assertEquals(41_234_567_890.123, result.getSyntheticOdometerMeters());
        Assert.assertEquals(1_048_575, result.getOdometerOffsetMeters());
        Assert.assertEquals(321_321_234_567_890L, result.getCumulativeIMAP());
        Assert.assertEquals(167_772.15, result.getTotalFuelUsedLiters());
        Assert.assertEquals(5579, result.getEngineOnTimeSeconds());
    }
}
