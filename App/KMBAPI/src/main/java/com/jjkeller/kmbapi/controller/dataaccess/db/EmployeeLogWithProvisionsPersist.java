package com.jjkeller.kmbapi.controller.dataaccess.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.jjkeller.kmbapi.controller.dataaccess.AbstractDBAdapter;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.EmployeeLogWithProvisions;
import com.jjkeller.kmbapi.proxydata.GpsLocation;
import com.jjkeller.kmbapi.proxydata.Location;

import java.util.Date;
import java.util.List;

/**
 * Created by jar5943 on 4/21/2016.
 */
public class EmployeeLogWithProvisionsPersist<T extends EmployeeLogWithProvisions> extends AbstractDBAdapter<T>{
    ///////////////////////////////////////////////////////////////////////////////////////
    // private members
    ///////////////////////////////////////////////////////////////////////////////////////
    private long _employeeLogKey;

    private static final String EMPLOYEELOGKEY= "EmployeeLogKey";
    private static final String EMPLOYEELOGELDEVENTKEY = "EmployeeLogEldEventKey";
    private static final String STARTTIME = "StartTime";
    private static final String STARTODOMETER = "StartOdometer";
    private static final String STARTLOCATIONNAME = "StartLocationName";
    private static final String STARTLATITUDEDEGREES = "StartLatitudeDegrees";
    private static final String STARTLONGITUDEDEGREES = "StartLongitudeDegrees";
    private static final String ENDTIME = "EndTime";
    private static final String ENDODOMETER = "EndOdometer";
    private static final String ENDLOCATIONNAME = "EndLocationName";
    private static final String ENDLATITUDEDEGREES = "EndLatitudeDegrees";
    private static final String ENDLONGITUDEDEGREES = "EndLongitudeDegrees";
    private static final String TOTALDISTANCE = "TotalDistance";
    private static final String TRACTORNUMBER = "TractorNumber";
    private static final String EMPLOYEECODE = "EmployeeCode";
    private static final String LOGDATE = "LogDate";
    private static final String ISSUBMITTED = "IsSubmitted";
    private static final String PROVISIONTYPEENUM = "ProvisionTypeEnum";

    private static final String SQL_SELECT_PRIMARYKEY_COMMAND = "select [Key] from EmployeeLogWithProvisions where EmployeeLogKey=? AND StartTime=? AND ProvisionTypeEnum=?";
    private static final String SQL_SELECT_COMMAND = "select * from EmployeeLogWithProvisions where EmployeeLogKey=? AND ProvisionTypeEnum=? order by EmployeeLogWithProvisions.StartTime, EmployeeLogWithProvisions.EndTime";
    private static final String SQL_SELECT_MOSTRECENT_FORLOG_COMMAND = "select * from EmployeeLogWithProvisions where EmployeeLogKey=? AND ProvisionTypeEnum=? order by EmployeeLogWithProvisions.StartTime DESC LIMIT 1";
    private static final String SQL_SELECT_UNSUBMITTED_LIST = "select EmployeeLogWithProvisions.*, EmployeeLog.LogDate as LogDate, [User].EmployeeCode as EmployeeCode "
            +" from EmployeeLogWithProvisions "
            +" LEFT JOIN EmployeeLog ON EmployeeLog.Key = EmployeeLogWithProvisions.EmployeeLogKey "
            +" LEFT JOIN [User] ON EmployeeLog.UserKey = [User].[Key] "
            +" where EmployeeLogWithProvisions.IsSubmitted = 0 AND EmployeeLog.LogSourceStatusEnum = 2  AND ProvisionTypeEnum=?";
    private static final String SQL_UPDATE_SUBMITTED = "update EmployeeLogWithProvisions set IsSubmitted = 1 where Key=?";
    private static final String SQL_SELECT_FOR_LOGELDEVENT = "SELECT * FROM EmployeeLogWithProvisions WHERE EmployeeLogKey=? AND EmployeeLogEldEventKey = ? LIMIT 1";
    private static final String SQL_SELECT_LAST_LOGWITHPROVISIONS = "SELECT * FROM EmployeeLogWithProvisions ORDER BY [Key] DESC LIMIT 1";
    private static final String SQL_UPDATE_LOGELDEVENTKEY = "UPDATE EmployeeLogWithProvisions SET EmployeeLogEldEventKey = ? WHERE [Key] = ?";


    ///////////////////////////////////////////////////////////////////////////////////////
    // constructors
    ///////////////////////////////////////////////////////////////////////////////////////

    public EmployeeLogWithProvisionsPersist(Class<T> clazz, Context ctx) {
        super(clazz, ctx);

        setDbTableName(DB_TABLE_EMPLOYEELOGWITHPROVISIONS);
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // @Override methods
    ///////////////////////////////////////////////////////////////////////////////////////
    @Override
    public String getSelectPrimaryKeyCommand() {
        return SQL_SELECT_PRIMARYKEY_COMMAND;
    }

    @Override
    protected String[] getSelectPrimaryKeyArgs(T data) {
        String [] args;
        if(data.getStartTime() == null)
            args = new String[]{Long.toString(this._employeeLogKey), ""};
        else
            args = new String[]{Long.toString(this._employeeLogKey), DateUtility.getHomeTerminalSqlDateTimeFormat().format(data.getStartTime()), Integer.toString(data.getProvisionTypeEnum())};

        return args;
    }

    @Override
    protected String getSelectCommand()
    {
        return SQL_SELECT_COMMAND;
    }

    @Override
    protected String getSelectUnsubmittedCommand()
    {
        return SQL_SELECT_UNSUBMITTED_LIST;
    }

    protected ContentValues PersistContentValues(T data)
    {
        ContentValues content = super.PersistContentValues(data);

        PutValue(content, EMPLOYEELOGKEY, _employeeLogKey);
        PutValue(content, EMPLOYEELOGELDEVENTKEY, data.getEmployeeLogEldEventId());
        PutValue(content, STARTTIME, data.getStartTime(), DateUtility.getHomeTerminalSqlDateTimeFormat());
        PutValue(content, ENDTIME, data.getEndTime(), DateUtility.getHomeTerminalSqlDateTimeFormat());
        PutValue(content, PROVISIONTYPEENUM, data.getProvisionTypeEnum());

        Location location = data.getStartLocation();
        if(location != null){
            PutValue(content, STARTODOMETER, location.getOdometerReading());
            if(location.getName() != null && location.getName().length() > 0)
                PutValue(content, STARTLOCATIONNAME, location.getName());
            else
                PutValue(content, STARTLOCATIONNAME, null);
            if(location.getGpsInfo() != null){
                PutValue(content, STARTLATITUDEDEGREES, location.getGpsInfo().getLatitudeDegrees());
                PutValue(content, STARTLONGITUDEDEGREES, location.getGpsInfo().getLongitudeDegrees());
            }
            else{
                PutValue(content, STARTLATITUDEDEGREES, null);
                PutValue(content, STARTLONGITUDEDEGREES, null);
            }
        }
        else
        {
            PutValue(content, STARTODOMETER, null);
            PutValue(content, STARTLOCATIONNAME, null);
            PutValue(content, STARTLATITUDEDEGREES, null);
            PutValue(content, STARTLONGITUDEDEGREES, null);
        }

        location = data.getEndLocation();
        if(location != null){
            PutValue(content, ENDODOMETER, location.getOdometerReading());
            if(location.getName() != null && location.getName().length() > 0)
                PutValue(content, ENDLOCATIONNAME, location.getName());
            else
                PutValue(content, ENDLOCATIONNAME, null);
            if(location.getGpsInfo() != null){
                PutValue(content, ENDLATITUDEDEGREES, location.getGpsInfo().getLatitudeDegrees());
                PutValue(content, ENDLONGITUDEDEGREES, location.getGpsInfo().getLongitudeDegrees());
            }
            else{
                PutValue(content, ENDLATITUDEDEGREES, null);
                PutValue(content, ENDLONGITUDEDEGREES, null);
            }
        }
        else
        {
            PutValue(content, ENDODOMETER, null);
            PutValue(content, ENDLOCATIONNAME, null);
            PutValue(content, ENDLATITUDEDEGREES, null);
            PutValue(content, ENDLONGITUDEDEGREES, null);
        }

        PutValue(content, TOTALDISTANCE, data.getTotalDistance());
        PutValue(content, TRACTORNUMBER, data.getTractorNumber());
        PutValue(content, ISSUBMITTED, 0);

        return content;
    }

    protected T BuildObject(Cursor cursorData)
    {
        T data = super.BuildObject(cursorData);

        data.setEmployeeLogEldEventId(ReadValue(cursorData, EMPLOYEELOGELDEVENTKEY, 0));

        data.setStartTime(ReadValue(cursorData, STARTTIME, (Date)null, DateUtility.getHomeTerminalSqlDateTimeFormat()));
        data.setEndTime(ReadValue(cursorData, ENDTIME, (Date)null, DateUtility.getHomeTerminalSqlDateTimeFormat()));
        data.setProvisionTypeEnum(ReadValue(cursorData, PROVISIONTYPEENUM, 0));

        Location loc = new Location();
        loc.setName(ReadValue(cursorData, STARTLOCATIONNAME, (String)null));
        loc.setOdometerReading(ReadValue(cursorData, STARTODOMETER, (float)-1));
        GpsLocation gpsLoc = new GpsLocation();
        gpsLoc.setLatitudeDegrees(ReadValue(cursorData, STARTLATITUDEDEGREES, (float)0));
        gpsLoc.setLongitudeDegrees(ReadValue(cursorData, STARTLONGITUDEDEGREES, (float)0));
        loc.setGpsInfo(gpsLoc);
        data.setStartLocation(loc);

        loc = new Location();
        loc.setName(ReadValue(cursorData, ENDLOCATIONNAME, (String)null));
        loc.setOdometerReading(ReadValue(cursorData, ENDODOMETER, (float)-1));
        gpsLoc = new GpsLocation();
        gpsLoc.setLatitudeDegrees(ReadValue(cursorData, ENDLATITUDEDEGREES, (float)0));
        gpsLoc.setLongitudeDegrees(ReadValue(cursorData, ENDLONGITUDEDEGREES, (float)0));
        loc.setGpsInfo(gpsLoc);
        data.setEndLocation(loc);

        data.setTotalDistance(ReadValue(cursorData, TOTALDISTANCE, -1));
        data.setTractorNumber(ReadValue(cursorData, TRACTORNUMBER, (String)null));

        // used only for submit to the webapi
        data.setEmployeeCode(ReadValue(cursorData, EMPLOYEECODE, (String)null));
        data.setLogDate(ReadValue(cursorData, LOGDATE, (Date)null, DateUtility.getHomeTerminalSqlDateFormat()));

        return data;
    }

    public void Save(T employeeLogWithProvisions, EmployeeLog empLog){
        _employeeLogKey = empLog.getPrimaryKey();
        this.Persist((T)employeeLogWithProvisions);
    }

    /// <summary>
    /// Returns the most recent EmployeeLogWithProvision that is related to the Employee Log and the ProvisionType that are passed in
    /// </summary>
    /// <returns>EmployeeLogWithProvisions</returns>
    public T FetchMostRecentForLog(EmployeeLog empLog, int provisionTypeEnum)
    {
        T item = ExecuteFetchRawQuery(SQL_SELECT_MOSTRECENT_FORLOG_COMMAND, new String[]{Long.toString(empLog.getPrimaryKey()), Integer.toString(provisionTypeEnum)});
        return item;
    }

    /**
     * Returns an EmployeeLogWithProvisions record which is related to a EmployeeLogEldEvent
     *
     * @param logEldEventKey The event key
     * @return The matching record
     */
    public T FetchForLogEldEvent(EmployeeLog empLog, long logEldEventKey)
    {
        T item = ExecuteFetchRawQuery(SQL_SELECT_FOR_LOGELDEVENT, new String[]{Long.toString(empLog.getPrimaryKey()), Long.toString(logEldEventKey)});
        return item;
    }

    public List<T> FetchAllUnsubmittedByProvisionType(int empLogWithProvisionTypeEnum) {
        List<T> list = ExecuteFetchListRawQuery(String.format(getSelectUnsubmittedCommand(), getDbTableName()), new String[]{Integer.toString(empLogWithProvisionTypeEnum)});

        return list;
    }

    public T FetchLastLogWithProvisions()
    {
        T item = ExecuteFetchRawQuery(SQL_SELECT_LAST_LOGWITHPROVISIONS, null);

        return item;
    }

    public void UpdateLogEldEventKey(int logWithProvisionKey, int logEldEventKey){
        ExecuteQuery(SQL_UPDATE_LOGELDEVENTKEY, new String[]{Integer.toString(logEldEventKey), Integer.toString(logWithProvisionKey)});
    }
}
