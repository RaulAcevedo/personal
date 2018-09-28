package com.jjkeller.kmbapi.controller.dataaccess.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.jjkeller.kmbapi.controller.dataaccess.AbstractDBAdapter;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.proxydata.DrivingEventReassignmentMapping;

import java.util.Date;
import java.util.List;

/**
 * Created by bja6001 on 2/23/17.
 */

public class EventReassignmentPersist<T extends DrivingEventReassignmentMapping> extends AbstractDBAdapter<T> {
    //Column Name Definitions
    public static final String RELATEDEVENT = "RelatedEvent";
    public static final String DRIVERTOASSIGNEVENTTOID = "DriverToAssignEventToId";
    public static final String EVENTCOMMENT = "EventComment";
    public static final String ISSUBMITTED = "IsSubmitted";
    //SQL Query Definitions
    private static final String SQL_SELECT_PRIMARYKEY_COMMAND = "SELECT * FROM " + DB_TABLE_DRIVINGEVENTREASSIGNMENTMAPPING + " WHERE Key=?";
    private static final String SQL_SELECT_NATURALKEY_COMMAND = "SELECT * FROM " + DB_TABLE_DRIVINGEVENTREASSIGNMENTMAPPING + " WHERE " + RELATEDEVENT + "=? AND " + DRIVERTOASSIGNEVENTTOID + "=?";
    private static final String SQL_SELECT_RELATEDEVENT_COMMAND = "SELECT * FROM " + DB_TABLE_DRIVINGEVENTREASSIGNMENTMAPPING + " WHERE " + RELATEDEVENT + "=?";
    private static final String SQL_SELECT_DRIVERTOASSIGNEVENTTOID_COMMAND = "SELECT * FROM " + DB_TABLE_DRIVINGEVENTREASSIGNMENTMAPPING + " WHERE " + DRIVERTOASSIGNEVENTTOID + "=?";
    private static final String SQL_SELECT_FETCHALLUNSUBMITTED_COMMAND = "SELECT * FROM " + DB_TABLE_DRIVINGEVENTREASSIGNMENTMAPPING + " WHERE IsSubmitted=0";

    public EventReassignmentPersist(Class<T> clazz, Context ctx) {
        super(clazz, ctx);
        setDbTableName(DB_TABLE_DRIVINGEVENTREASSIGNMENTMAPPING);
    }

    //Override for AbstractDBAdapter
    @Override
    public String getSelectPrimaryKeyCommand() {
        return SQL_SELECT_PRIMARYKEY_COMMAND;
    }

    //Override for AbstractDBAdapter implementation of PersistBase
    @Override
    protected ContentValues PersistContentValues(T data) {
        ContentValues values = super.PersistContentValues(data);
        PutValue(values, RELATEDEVENT, data.getRelatedEvent());
        PutValue(values, DRIVERTOASSIGNEVENTTOID, data.getDriverToAssignEventTo());
        PutValue(values, EVENTCOMMENT, data.getEventComment());
        PutValue(values, ISSUBMITTED, data.getIsSubmitted());
        return values;
    }

    //Override for AbstractDBAdapter
    @Override
    protected String[] getSelectPrimaryKeyArgs(T data) {
        return new String[]{Long.toString(data.getPrimaryKey())};
    }

    //Override for AbstractDBAdapter
    @Override
    protected String getSelectCommand() {
        return SQL_SELECT_PRIMARYKEY_COMMAND;
    }

    //Override for PersistBase
    @Override
    protected T BuildObject(Cursor cursorData) {
        DrivingEventReassignmentMapping data = super.BuildObject(cursorData);
        data.setRelatedEvent(ReadValue(cursorData, RELATEDEVENT, (Integer) null));
        data.setDriverToAssignEventTo(ReadValue(cursorData, DRIVERTOASSIGNEVENTTOID, (String) null));
        data.setEventComment(ReadValue(cursorData, EVENTCOMMENT, (String) null));
        data.setIsSubmitted(ReadValue(cursorData, ISSUBMITTED, false));
        return (T) data;
    }

    public List<T> FetchByNaturalKey(int relatedEvent, String driverToAssignEventToId) {
        //create args
        String[] selectionArgs = new String[]{Integer.toString(relatedEvent),driverToAssignEventToId};
        return ExecuteFetchListRawQuery(SQL_SELECT_NATURALKEY_COMMAND, selectionArgs);
    }

    public List<T> FetchByPrimaryKey(int recordId) {
        String[] selectionArgs = new String[]{Integer.toString(recordId)};
        return ExecuteFetchListRawQuery(SQL_SELECT_PRIMARYKEY_COMMAND, selectionArgs);
    }

    public List<T> FetchByRelatedEvent(int relatedEvent) {
        String[] selectionArgs = new String[]{Integer.toString(relatedEvent)};
        return ExecuteFetchListRawQuery(SQL_SELECT_RELATEDEVENT_COMMAND, selectionArgs);
    }

    public List<T> FetchByDriverEventReassignmentId(String driverReassignmentId) {
        String[] selectionArgs = new String[]{driverReassignmentId};
        return ExecuteFetchListRawQuery(SQL_SELECT_DRIVERTOASSIGNEVENTTOID_COMMAND, selectionArgs);
    }

    public List<T> FetchAllUnsubmitted()
    {
        return ExecuteFetchListRawQuery(SQL_SELECT_FETCHALLUNSUBMITTED_COMMAND, null);
    }
}
