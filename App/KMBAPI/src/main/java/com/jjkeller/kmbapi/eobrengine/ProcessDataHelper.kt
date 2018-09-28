package com.jjkeller.kmbapi.eobrengine

import com.jjkeller.kmbapi.common.TabDataConversionUtil
import com.jjkeller.kmbapi.eobrengine.eobrdatatypes.Eobr_Data_GenII
import com.jjkeller.kmbapi.kmbeobr.*
import java.util.*
import kotlin.experimental.and

class ProcessDataHelper {
    companion object {
        fun processHistogramDataResponse(response: ByteArray, histogram: HistogramData) {
            // odometer stored in TAB as (KM * 10) - convert to miles
            val rawOdometer = LittleEndianHelper.getInt(response, 27, 4)
            val histogramOdometer = rawOdometer / 10f / Constants.KILOMETERS_PER_MILE

            // histogram data
            val histData = IntArray(50)
            for (i in 0 until histogram.numBins) {
                histData[i] = LittleEndianHelper.getInt(response, 51 + i * 4, 4)
            }

            histogram.apply {
                recordId = LittleEndianHelper.getInt(response, 2, 4)
                // timestamp stored in TAB as # of milliseconds since 1/1/1970
                timecode = LittleEndianHelper.getLong(response, 6, 8)
                histogramType = response[14].toInt()
                runTime = LittleEndianHelper.getInt(response, 15, 4)
                trips = LittleEndianHelper.getInt(response, 19, 4)
                onTime = LittleEndianHelper.getInt(response, 23, 4)
                odometer = histogramOdometer
                driverId = LittleEndianHelper.getInt(response, 31, 4)
                lowLimit = LittleEndianHelper.getInt(response, 35, 4)
                highLimit = LittleEndianHelper.getInt(response, 39, 4)
                period = LittleEndianHelper.getShort(response, 43, 2).toInt()
                binSize = response[45].toInt()
                numBins = response[46].toInt()
                totalCounts = LittleEndianHelper.getInt(response, 47, 4)
                histogramData = histData
            }
        }

        fun recorderDataToUserData(response: ByteArray, eobrData: Eobr_Data_GenII) {
            val gpsByteBuffer = LittleEndianHelper.getBuffer(response, 19, GpsFix.RecordLength())

            eobrData.apply {
                ignition = response[2]
                recordId = LittleEndianHelper.getInt(response, 3, 4)
                eobrStat = LittleEndianHelper.getInt(response, 7, 4)
                activeBus = LittleEndianHelper.getInt(response, 11, 4)
                diagRecId = LittleEndianHelper.getInt(response, 15, 4)
                gpsFix = GpsFix.FromByteBuffer(gpsByteBuffer)
                obdData = buildObdData(response)
            }
        }

        private fun buildObdData(response: ByteArray): ObdData {
            // vehicle speed stored in TAB as (KPH * 100) - convert to MPH
            val rawVehicleSpeed = LittleEndianHelper.getShort(response, 52, 2)
            val calculatedSpeed = rawVehicleSpeed / 100f * Constants.KPH_TO_MPH
            // odometer stored in TAB as (KM * 10) - convert to miles - 1/10 of a mile precision
            val rawOdometer = LittleEndianHelper.getInt(response, 56, 4)
            val calculatedOdometer = TabDataConversionUtil.convertOdometerReading(rawOdometer)

            // average fuel rate stored in TAB as (MPG * 100)
            val rawAvgFuelRate = LittleEndianHelper.getShort(response, 60, 2)
            var calculatedAvgFuelRate = rawAvgFuelRate.toFloat()
            if (rawAvgFuelRate > 0)
                calculatedAvgFuelRate /= 100f

            // total fuel used stored in TAB as (liters * 100) - convert to gallons
            val rawTotalFuelUsed = LittleEndianHelper.getInt(response, 65, 4)
            var calculatedTotalFuelUsed = rawTotalFuelUsed.toFloat()
            if (rawTotalFuelUsed > 0)
                calculatedTotalFuelUsed /= 100f * Constants.GALLONS_PER_LITER

            // instant fuel economy stored in TAB as (MPG * 100)
            val rawInstantFuelRate = LittleEndianHelper.getShort(response, 71, 2)
            var calculatedInstantFuelRate = rawTotalFuelUsed.toFloat()
            if (rawInstantFuelRate > 0)
                calculatedInstantFuelRate /= 100f

            // SpeedDetail data
            val milli = IntArray(14)
            val speed = FloatArray(14)
            val endOfArrayMarker: Short = -1
            var tempVal: Short

            var i = 0
            while (i < 14 && 102 + i * 4 + 2 <= response.size) {
                tempVal = LittleEndianHelper.getShort(response, 100 + i * 4, 2)
                speed[i] = if (tempVal == endOfArrayMarker) 0.0f else tempVal.toFloat() / 100f * Constants.KPH_TO_MPH

                tempVal = LittleEndianHelper.getShort(response, 102 + i * 4, 2)
                milli[i] = if (tempVal == endOfArrayMarker) 0 else tempVal.toInt()
                i++
            }

            return ObdData().apply {
                // timestamp stored in TAB as # of milliseconds since 1/1/1970
                timeCode = LittleEndianHelper.getLong(response, 43, 8)
                ignition = response[51]
                vehicleSpeed = calculatedSpeed
                // engine speed stored in TAB as RPM
                engineSpeed = LittleEndianHelper.getShort(response, 54, 2)
                odometer = calculatedOdometer
                avgFuelRate = calculatedAvgFuelRate
                cruiseConStat = response[62]
                transRange = LittleEndianHelper.getShort(response, 63, 2)
                totalFuelUsed = calculatedTotalFuelUsed
                brakePress = LittleEndianHelper.getShort(response, 69, 2)
                instFuelRate = calculatedInstantFuelRate
                ptoStatus = response[73]
                coolantPress = LittleEndianHelper.getShort(response, 74, 2)
                coolantLevel = response[76]
                // coolant temp stored in TAB as (degC + 40)
                coolantTemp = response[77]
                transOilLevel = response[78]
                transOilPress = LittleEndianHelper.getShort(response, 79, 2)
                throttlePos = response[81]
                engineLoad = response[82]
                engineOilPress = LittleEndianHelper.getShort(response, 83, 2)
                // engine oil temperature stored in TAB as (degC + 40)
                engineOilTemp = LittleEndianHelper.getShort(response, 85, 2)
                engineOilLevel = response[87]
                parkBrakeState = response[88]
                mil = response[89]
                activeDTCs = response[90]
                pendingDTCs = response[91]
                evalSup = LittleEndianHelper.getInt(response, 92, 4)
                // air flow stored in TAB as (gm/s * 100)
                airFlow = LittleEndianHelper.getShort(response, 96, 2)
                manAbsPress = response[98]
                intakeAirTemp = response[99]
                speedDetailMilli = milli
                speedDetailSpeed = speed
            }
        }

        fun copyEobrDataToStatusRecord(eobrData: StatusRecord, convertData: Eobr_Data_GenII) {
            eobrData.apply {
                isEngineOn = convertData.GetIgnition().toInt() == 1
                recordId = convertData.GetRecordId()
                overallStatus = convertData.GetEOBRStat()
                activeBusType = convertData.GetActiveBus()
                diagRecordId = convertData.GetDiagRecId()
                timestampUtc = Date(convertData.obdData.getTimeCode())
                speedometerReading = convertData.obdData.getVehicleSpeed()
                tachometer = convertData.obdData.getEngineSpeed().toFloat()
                odometerReading = convertData.obdData.getOdometer()
                averageFuelEconomy = convertData.obdData.getAvgFuelRate()
                cruiseControlStatus = convertData.obdData.getCruiseConStat().toInt()
                transmissionRange = convertData.obdData.getTransRange().toInt()
                totalFuelUsed = convertData.obdData.totalFueldUsed
                brakePressure = convertData.obdData.getBrakePress().toFloat()
                instantFuelEconomy = convertData.obdData.getInstFuelRate()
                ptoStatus = convertData.obdData.getPTOStatus().toInt() == 1
                coolantPressure = convertData.obdData.getCoolantPress().toInt()
                coolantLevel = convertData.obdData.getCoolantLevel().toInt()
                coolantTemp = convertData.obdData.getCoolantTemp().toInt()
                transmissionOilLevel = convertData.obdData.getTransOilLevel().toInt()
                transmissionOilPressure = convertData.obdData.getTransOilPress().toInt()
                throttlePos = convertData.obdData.getThrottlePos().toInt()
                engineLoad = convertData.obdData.getEngineLoad().toInt()
                engineOilPressure = convertData.obdData.getEngineOilPress().toInt()
                engineOilTemp = convertData.obdData.getEngineOilTemp().toInt()
                engineOilLevel = convertData.obdData.getEngineOilLevel().toInt()
                parkBrakeState = convertData.obdData.getParkBrakeState().toInt() == 1
                milStatus = convertData.obdData.getMIL().toInt() == 1
                activeDTCs = convertData.obdData.getActiveDTCs().toInt()
                pendingDTCs = convertData.obdData.getPendingDTCs().toInt()
                evalSup = convertData.obdData.getEvalSup()
                airFlow = convertData.obdData.getAirFlow().toInt()
                manAbsPressure = convertData.obdData.getManAbsPress().toInt()
                intakeAirTemp = convertData.obdData.intakeAirTemp.toInt()
                setSpeedDetailMilli(convertData.obdData.getSpeedDetailMilli())
                setSpeedDetailSpeed(convertData.obdData.getSpeedDetailSpeed())
            }

            //If no GPS Timestamp, force to be null, which in turn makes IsGPSLocationValid = false
            if (convertData.gpsFix.getTimeCode() == 0L) {
                eobrData.gpsTimestampUtc = null
            } else {
                eobrData.apply {
                    gpsTimestampUtc = Date(convertData.gpsFix.getTimeCode())
                    gpsLatitude = convertData.gpsFix.getLatitude()
                    gpsLongitude = convertData.gpsFix.getLongitude()
                    gpsDOP = convertData.gpsFix.getDOP()
                    gpsHeading = convertData.gpsFix.getHeading().toInt()
                    gpsSpeed = convertData.gpsFix.getSpeed()
                    gpsAltitude = convertData.gpsFix.getAltitude().toFloat()
                    gpsUncertDistance = convertData.gpsFix.getUncert().toFloat()
                }
            }

        }

        fun processTripReportDataResponse(response: ByteArray, tripReportData: TripReport) {
            // celleration - total speed change during trip - stored in TAB as (KPH * 10) - convert to MPH
            val calculatedCelleration = LittleEndianHelper.getInt(response, 55, 4) / 100f * Constants.KPH_TO_MPH
            // trip fuel - fuel consumed - stored in TAB as (liters * 100) - convert to gallons
            val tripFuelUsed = LittleEndianHelper.getShort(response, 48, 2).toInt() and 0xffff
            var convertedTripFuelUsed = tripFuelUsed.toFloat()
            if (tripFuelUsed > 0)
                convertedTripFuelUsed /= 100f * Constants.GALLONS_PER_LITER

            tripReportData.apply {
                recordId = LittleEndianHelper.getInt(response, 2, 4)
                // timestamp stored in TAB as # of milliseconds since 1/1/1970
                timecode = LittleEndianHelper.getLong(response, 6, 8)
                ignition = response[14]
                // odometer stored in TAB as (KM * 10) - convert to miles
                odometer = TabDataConversionUtil.convertOdometerReading(LittleEndianHelper.getInt(response, 15, 4))
                // run time - trip engine engine run-time (sec)
                runtime = LittleEndianHelper.getInt(response, 19, 4)
                // trip secs - trip time (sec)
                tripSecs = LittleEndianHelper.getShort(response, 23, 2).toInt() and 0xffff
                // trip number
                tripNum = LittleEndianHelper.getShort(response, 25, 2).toInt()
                // trip distance - stored in TAB as (KM * 10) - convert to miles
                tripDist = (LittleEndianHelper.getShort(response, 40, 2).toInt() and 0xffff).toFloat()
                // latitude stored in TAB as # of milliseconds
                latitude = LittleEndianHelper.getInt(response, 29, 4) / Constants.MILLISECONDS_TO_DEGREES
                // longitude stored in TAB as # of milliseconds
                longitude = LittleEndianHelper.getInt(response, 33, 4) / Constants.MILLISECONDS_TO_DEGREES
                // fix uncertainty - distance traveled since last fix - stored in TAB as meters
                fixUncert = (LittleEndianHelper.getShort(response, 37, 2).toInt() and 0xffff).toShort()
                avgHeading = response[39] * 2
                // average speed stored in TAB as (KPH * 10) - convert to MPH
                avgSpeed = LittleEndianHelper.getShort(response, 40, 2) / 100f * Constants.KPH_TO_MPH
                // max speed stored in TAB as (KPH * 10) - convert to MPH
                maxSpeed = LittleEndianHelper.getShort(response, 42, 2) / 100f * Constants.KPH_TO_MPH
                // idle seconds - total time stopped (sec)
                idleSecs = LittleEndianHelper.getShort(response, 44, 2).toInt() and 0xffff
                // halts - # of times vehicle stopped during trip
                halts = LittleEndianHelper.getShort(response, 46, 2).toInt()
                tripFuel = convertedTripFuelUsed
                idleFuelPcnt = response[50]
                ptoFuelPcnt = response[51]
                ptoActivePcnt = response[52]
                // resets - # of total TAB hardware resets
                resets = LittleEndianHelper.getShort(response, 53, 2).toInt()
                celleration = calculatedCelleration
                // driver id
                driverId = LittleEndianHelper.getInt(response, 59, 4)
                // datatimecode stored in TAB as # of milliseconds since 1/1/1970
                dataTimecode = LittleEndianHelper.getLong(response, 63, 8)
                // Max Tach.
                maxEngRPM = LittleEndianHelper.getShort(response, 71, 2).toInt()
                // Avg Tach.
                avgEngRPM = LittleEndianHelper.getShort(response, 73, 2).toInt()
            }

        }
    }
}