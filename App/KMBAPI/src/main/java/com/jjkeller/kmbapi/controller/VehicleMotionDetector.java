package com.jjkeller.kmbapi.controller;

import android.content.Context;
import android.util.Log;

import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.controller.utility.ErrorLogHelper;
import com.jjkeller.kmbapi.kmbeobr.StatusRecord;
import com.jjkeller.kmbapi.proxydata.GpsLocation;

import java.util.Date;

public class VehicleMotionDetector {

	private Context _ctx; 
	public VehicleMotionDetector(Context ctx)
	{
		this._ctx = ctx;
	}
	
    /// <summary>
    /// The current status record being processed from the EOBR
    /// </summary>
    private StatusRecord _currentStatus = null;

    /// <summary>
    /// The current user logged into the system
    /// </summary>
    private User _currentUser = null;

    /// <summary>
    /// Timestamp when vehicle motion was first detected for this period
    /// </summary>
    private Date _potentialStartTimestamp = null;

    /// <summary>
    /// Timestamp when vehicle motion was first detected for this period and
    /// the driving period start has been confirmed
    /// </summary>
    private Date _confirmedStartTimestamp = null;

    /// <summary>
    /// Odometer value when vehicle motion was first detected
    /// </summary>
    private float _drivingPeriodStartOdometer = -1.0F;

    /// <summary>
    /// Location value when driving period starts
    /// </summary>
    private GpsLocation _drivingPeriodStartLocation = null;

    /// <summary>
    /// Timestamp when vehicle motion first was stopped
    /// </summary>
    private Date _potentialStopTimestamp = null;

    /// <summary>
    /// Timestamp when vehicle motion first was stopped, after a confirmation
    /// that the driving period was ended
    /// </summary>
    private Date _confirmedStopTimestamp = null;

    /// <summary>
    /// Location value when driving period stops
    /// </summary>
    private GpsLocation _drivingPeriodStopLocation = null;

    /// <summary>
    /// Odometer value when driving period stops
    /// </summary>
    private float _drivingPeriodStopOdometer = -1.0F;

    /// <summary>
    /// Tracks whether the conditions for a confirmed driving period have been net
    /// </summary>
    private boolean _isConfirmedStart = false;

    /// <summary>
    /// Tracks whether the conditions for a confirmed stop period have been met
    /// </summary>
    private boolean _isConfirmedStop = false;

    /// <summary>
    /// Enables whether automatic confirmation of a stop period occurs.
    /// This is used by the "manually  extend driving" segment design that a driver
    /// will use when in a traffic jam, and a change to driving status should be 
    /// skipped once a confirmed stop occurs.
    /// </summary>
    private boolean _enableStopConfirmation = true;
    
    /// <summary>
    /// Process the current status record from the EOBR.
    /// This EOBR record should not be a failure record.
    /// Depending on the state of the vehicle, try to confirm
    /// either the start or end of a driving period.
    /// </summary>
    /// <param name="user"></param>
    /// <param name="currentStatus"></param>
    public void ProcessStatusRecord(User user, StatusRecord currentStatus)
    {
        Log.v("LogEvent", String.format(">>VMD pStart:{%s} cStart:{%s} odom:{%f} pStop:{%s} cStop:{%s} isConfStart:{%s} isConfStop:{%s}", _potentialStartTimestamp, _confirmedStartTimestamp, _drivingPeriodStartOdometer, _potentialStopTimestamp, _confirmedStopTimestamp, (_isConfirmedStart? "true":"false"), (_isConfirmedStop? "true":"false")));

        _currentStatus = currentStatus;
        _currentUser = user;

        if (currentStatus != null && !currentStatus.IsEmpty())
        {
            // this is a valid status record

            if (_isConfirmedStart)
            {
                // when previously a confirmed start occurs, need to start over again
                // to look for a new start
                this.ResetStartPeriodParms();
            }
            if (_isConfirmedStop)
            {
                // when previously a confirmed stop occurs, need to start over again
                // looking for a stop
                this.ResetStopPeriodParms();
            }

            // are there any failures reported in it
            if (!currentStatus.IsSignificantDeviceFailureDetected())
            {
                // no failures reported on it
                // attempt to confirm either the start, or end, of a driving segment
                boolean isVehicleMoving = this.getIsVehicleInMotion();

                this.ConfirmStartOfDrivingPeriod(isVehicleMoving);
                this.ConfirmEndOfDrivingPeriod(isVehicleMoving);
            }
            else
            {
                // there are significant failures reported from the EOBR
                // need to end a driving segment that may be in process
                // This is a "forced" stop because of the failure.
                // Emulate as if the truck has stopped and manually 
                // mark the end of the driving period

                this.ConfirmEndOfDrivingPeriod(false);
                this.MarkEndOfDrivingPeriod();
            }
        }
        else
        {
            this.ResetStartPeriodParms();
            this.ResetStopPeriodParms();
        }
    }

    /// <summary>
    /// Answer if the last processed status record resulted in a confirmed
    /// Driving period.
    /// </summary>
    public boolean getIsConfirmedDrivingPeriodStart()
    {
        return _isConfirmedStart;
    }

    /// <summary>
    /// Answer the timestamp of when the vehicle first started moving,
    /// at the beginning of the confirmed driving period.   
    /// The timestamp will be in UTC.
    /// </summary>
    public Date getConfirmedDrivingStartTimestamp()
    {
        return _confirmedStartTimestamp;
    }

    /// <summary>
    /// Answer the GPS location of the vehicle when first started moving
    /// </summary>
    public GpsLocation getDrivingPeriodStartLocation()
    {
        return _drivingPeriodStartLocation;
    }

    /// <summary>
    /// Answer the odometer of the vehicle when first started moving
    /// </summary>
    public float getDrivingPeriodStartOdometer()
    {
        return _drivingPeriodStartOdometer;
    }

    /// <summary>
    /// Answer if the last the processed status record resulting in a
    /// confirmed stop of the vehicle
    /// </summary>
    public boolean getIsConfirmedDrivingPeriodStop()
    {
        return _isConfirmedStop;
    }

    /// <summary>
    /// Answer the timestamp when the vehicle first came to rest, for
    /// the confirmed stop of the vehicle.
    /// The timestamp will be in UTC.
    /// </summary>
    public Date getConfirmedDrivingStopTimestamp()
    {
        return _confirmedStopTimestamp;
    }

    /// <summary>
    /// Answer the timestamp when the vehicle first came to rest
    /// This may not become a confirmed stop of the vehicle.
    /// The timestamp will be in UTC.
    /// </summary>
    public Date getPotentialDrivingStartTimestamp()
    {
        return _potentialStartTimestamp;
    }
    
    /// <summary>
    /// Answer the timestamp when the vehicle first came to rest
    /// This may not become a confirmed stop of the vehicle.
    /// The timestamp will be in UTC.
    /// </summary>
    public Date getPotentialDrivingStopTimestamp()
    {
        return _potentialStopTimestamp;
    }

    /// <summary>
    /// Answer the GPS location of the vehicle when it stop moving
    /// </summary>
    public GpsLocation getDrivingPeriodStopLocation()
    {
        return _drivingPeriodStopLocation;
    }

    /// <summary>
    /// Answer the odometer of the vehicle when it stopped moving
    /// </summary>
    public float getDrivingPeriodStopOdometer()
    {
        return _drivingPeriodStopOdometer;
    }

    /// <summary>
    /// Answer if there is any movement at all detected by the vehicle.
    /// The vehicle is defined to be in motion whent the engine is on, 
    /// and there is some valid speed reported by the speedometer.
    /// </summary>
    public boolean getIsVehicleInMotion()
    {
        boolean inMotion = false;

        // check that the engine is on, and that the EOBR is functioning normally
        if (_currentStatus != null && _currentStatus.getIsEngineRunning() && !_currentStatus.IsSignificantDeviceFailureDetected() )
        {
            // is there some speed detected which would indicate that the 
            // vehicle is moving
            inMotion = _currentStatus.getSpeedometerReading() > 0;
        }

        return inMotion;
    }

    /// <summary>
    /// Disable the function that confirms the stop periods.
    /// This is used so that once a driving period is entered, it 
    /// can not be automatically stopped.
    /// Note: this is just a temporary setting and will become reenabled
    /// when vehicle motion is detected again.
    /// </summary>
    public void DisableStopPeriodConfirmation()
    {
        _isConfirmedStop = false;
        _enableStopConfirmation = false;
    }
    
    /// <summary>
    /// Attempt to confirm the start of the driving period.
    /// This is done using the currently logged in user's rule for 
    /// the distance required for the vehicle to travel before indicating
    /// the start of a driving period.
    /// </summary>
    private void ConfirmStartOfDrivingPeriod(boolean isVehicleInMotion)
    {
        // not already in a confirmed driving period
        if (isVehicleInMotion && _potentialStartTimestamp == null)
        {
            // the vehicle just started moving right now, keep track of when it started moving
            // this is a potential start of a driving period
            // reset both the location and odometer
            _potentialStartTimestamp = _currentStatus.getTimestampUtc();
            _drivingPeriodStartOdometer = -1.0F;
            _drivingPeriodStartLocation = null;

            // whenever we start moving again, reset the auto-stop confirmation
            // auto-stop confirmation is turned off when the driver extends the driving period
            _enableStopConfirmation = true;
        }

        // has there been the potential start of a driving period previously
        if (_potentialStartTimestamp != null)
        {
            // ensure that a valid odometer is recorded as the start of this potential driving period
            if (_drivingPeriodStartOdometer <= 0)
            {
                _drivingPeriodStartOdometer = _currentStatus.getOdometerReadingMI();
            }

            // ensure that a valid GPS location is recorded as the start of this potential driving period
            if (_drivingPeriodStartLocation == null || _drivingPeriodStartLocation.IsEmpty())
            {
                if (_currentStatus.IsGpsLocationValid())
                {
                    try
                    {
                        // get the GPS position from the status record
                        // if the GPS data is valid, then keep track of it for use later
                        GpsLocation gpsLoc = new GpsLocation(_currentStatus.getGpsTimestampUtc(), _currentStatus.getGpsLatitude(), _currentStatus.getNorthSouthInd(), _currentStatus.getGpsLongitude(), _currentStatus.getEastWestInd());
                        _drivingPeriodStartLocation = gpsLoc;
                    }
                    catch (Exception excp)
                    {
                        // some aspect of the GPS data is invalid, record the error
                        ErrorLogHelper.RecordException(_ctx, excp);
                    }
                }
            }

            // is there enough movement to confirm the driving period
            if (this.HasVehicleDrivenToConfirmStart())
            {
                // yes, so mark it as the start of a driving period
                this.MarkStartOfDrivingPeriod();
            }
        }

        if (!isVehicleInMotion)
        {
            // If there is a pending driving start that's occurred and
            // the vehicle has come to a confirmed stop, then the pending 
            // driving start is ignored.
            // In order to declare a "full stop", compare the time stopped 
            // against the user's rule for how long the vehicle needs to 
            // be stopped before the end of a driving period                
            if (_potentialStopTimestamp != null && _potentialStartTimestamp != null)
            {
                if (this.HasVehicleStoppedToConfirmStop())
                {
                    _potentialStartTimestamp = null;
                }
            }
        }

    }

    /// <summary>
    /// Attempt to confirm the end of a driving period.  
    /// This is done using the currently logged in user's rule for
    /// how much time needs to elapse while the vehicle is at rest. 
    /// </summary>
    private void ConfirmEndOfDrivingPeriod(boolean isVehicleInMotion)
    {
        if (_enableStopConfirmation)
        {
            // the auto-stop feature is enabled
            if (!isVehicleInMotion && _potentialStopTimestamp == null)
            {
                // the first time a record has been processed where the vehicle is stopped
                // this is the spot where a potential stop may occur
                // reset both the location and odometer
                _potentialStopTimestamp = _currentStatus.getTimestampUtc();
                _drivingPeriodStopOdometer = -1.0F;
                _drivingPeriodStopLocation = null;
            }

            // have we recorded a potential stop period previously
            if (_potentialStopTimestamp != null)
            {
                // ensure that a valid odometer is recorded as the stop of this potential driving period
                if (_drivingPeriodStopOdometer <= 0)
                {
                    _drivingPeriodStopOdometer = _currentStatus.getOdometerReadingMI();
                }

                // ensure that a valid GPS location is recorded as the end of this potential driving period
                if (_drivingPeriodStopLocation == null || _drivingPeriodStopLocation.IsEmpty())
                {
                    if (_currentStatus.IsGpsLocationValid())
                    {
                        try
                        {
                            // get the GPS position from the status record
                            // if the GPS data is valid, then keep track of it for use later
                            GpsLocation gpsLoc = new GpsLocation(_currentStatus.getGpsTimestampUtc(), _currentStatus.getGpsLatitude(), _currentStatus.getNorthSouthInd(), _currentStatus.getGpsLongitude(), _currentStatus.getEastWestInd());
                            _drivingPeriodStopLocation = gpsLoc;
                        }
                        catch (Exception excp)
                        {
                            // some aspect of the GPS data is invalid, record the error
                            ErrorLogHelper.RecordException(_ctx, excp);
                        }
                    }
                }

                // has it been stopped long enough to confirm the stop period
                if (this.HasVehicleStoppedToConfirmStop())
                {
                    // yes, so mark the end of the driving period
                    this.MarkEndOfDrivingPeriod();
                }
            }

            if (isVehicleInMotion)
            {
                // the vehicle is moving, so reset the potential stop time
                // the vehicle needs to stop, and stay stopped for a length of time
                // in order to confirm a stop
                // once the vehicle moves, there is no more chance to confirm a stop
                _potentialStopTimestamp = null;
            }
        }
    }
    
    /// <summary>
    /// Answer if the vehicle has driven far enough to confirm the start of the 
    /// driving period.
    /// The actual distance driven is compared to the current user's rule for the 
    /// driving distance needed to mark the start of a driving period.
    /// If the distance driven is more than the rule, then return true.
    /// </summary>
    /// <returns></returns>
    private boolean HasVehicleDrivenToConfirmStart()
    {
        boolean hasDrivenFarEnough = false;

        // is there enough movement to confirm the driving period
        // compare the distance driven against the user's rule for how far 
        // the vehicle needs to be driven before the start of a driving period
        if (_currentStatus.getOdometerReadingMI() > 0F && _drivingPeriodStartOdometer > 0F)
        {
            // both odometer readings are valid, calc the distance driven so far
            float drivingDistance = _currentStatus.getOdometerReadingMI() - _drivingPeriodStartOdometer;
            if (drivingDistance >= _currentUser.getDrivingStartDistanceMiles())
            {
                // yes, enough distance has been travelled to confirm 
                // this as a driving period
                hasDrivenFarEnough = true;
            }
        }

        return hasDrivenFarEnough;
    }

    /// <summary>
    /// Answer if the vehicle has been stopped long enough to confirm the end of the
    /// driving period.
    /// Compare the actual amount of time that the vehicle has stopped against
    /// the current user's rule for the driving period stop time.
    /// If the actual stop time is more than the driver's rule, then return true.
    /// </summary>
    /// <returns></returns>
    private boolean HasVehicleStoppedToConfirmStop()
    {
        boolean hasStoppedEnough = false;

        // has it been stopped long enough to confirm the stop period
        // compare the time stopped against the user's rule for how long 
        // the vehicle needs to be stopped before the end of a driving period
        long stopTime = _currentStatus.getTimestampUtc().getTime() - _potentialStopTimestamp.getTime();
        double stopTimeTotalMinutes = stopTime / 60000; // Convert milliseconds to Minutes (1000*60)
        if (stopTimeTotalMinutes >= (double)_currentUser.getDrivingStopTimeMinutes())
        {
            // yes, the vehicle has stopped long enough to 
            // confirm it as a stop period
            hasStoppedEnough = true;
        }
        return hasStoppedEnough;
    }
    
    /// <summary>
    /// The driving period needs to be marked as started.  
    /// This means that the driving period has been confirmed to have begun.
    /// </summary>
    private void MarkStartOfDrivingPeriod()
    {
        _isConfirmedStart = true;
        _confirmedStartTimestamp = _potentialStartTimestamp;
    }

    /// <summary>
    /// The driving period needs to be marked as ended.  
    /// This means that the driving period has completed.
    /// </summary>
    private void MarkEndOfDrivingPeriod()
    {
        _isConfirmedStop = true;
        _confirmedStopTimestamp = _potentialStopTimestamp;

        // once the end of a driving period is marked, and confirmed
        // verify that a potential start of a new driving period is after the
        // confirmed stop of this driving period.
        if (_potentialStartTimestamp != null && _potentialStartTimestamp.compareTo(_confirmedStopTimestamp) < 0)
        {
            this.ResetStartPeriodParms();

            // sjn 04/25/08
            // attempt to confirm the start of a new period because
            // there is a confirmed stop, but the potential start occurs before
            // the confirmed stop time.   
            // Intent of this is to check if the current record being processed
            // will be used to both confirm a stop *and* potentially start another driving period.
            this.ConfirmStartOfDrivingPeriod(this.getIsVehicleInMotion());
        }

    }

    /// <summary>
    /// Reset the parameters surrounding the start of the driving period
    /// because a vehicular stop was confirmed.
    /// </summary>
    private void ResetStartPeriodParms()
    {
        _drivingPeriodStartOdometer = -1.0F;
        _potentialStartTimestamp = null;
        _confirmedStartTimestamp = null;
        _isConfirmedStart = false;
        _drivingPeriodStartLocation = null;
    }

    /// <summary>
    /// Reset the parameters surrounding the end of the driving period,
    /// because the vehicle started moving again.
    /// </summary>
    private void ResetStopPeriodParms()
    {
        _drivingPeriodStopOdometer = -1.0F;
        _potentialStopTimestamp = null;
        _confirmedStopTimestamp = null;
        _isConfirmedStop = false;
        _drivingPeriodStopLocation = null;

        // always enable the stop period confirmation, by default
        _enableStopConfirmation = true;
    }    
}
