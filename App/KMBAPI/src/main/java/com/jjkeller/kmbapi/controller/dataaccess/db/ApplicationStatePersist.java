package com.jjkeller.kmbapi.controller.dataaccess.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.jjkeller.kmbapi.controller.dataaccess.AbstractDBAdapter;
import com.jjkeller.kmbapi.proxydata.ApplicationStateSettings;

public class ApplicationStatePersist<T extends ApplicationStateSettings> extends AbstractDBAdapter<T> {

    ///////////////////////////////////////////////////////////////////////////////////////
    // private members
    ///////////////////////////////////////////////////////////////////////////////////////
    private static final String SEQUENCEID = "EventSequenceId";
    private static final String SQL_SELECT_PRIMARYKEY_COMMAND = "select Key from ApplicationState";
    private static final String SQL_SELECT_COMMAND = "select * from ApplicationState";

    ///////////////////////////////////////////////////////////////////////////////////////
    // constructors
    ///////////////////////////////////////////////////////////////////////////////////////
    public ApplicationStatePersist(Class<T> clazz, Context ctx) {
        super(clazz, ctx);

        setDbTableName(DB_TABLE_ApplicationState);
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
        return null;

    }
    @Override
    protected String getSelectCommand() {
        return SQL_SELECT_COMMAND;
    }

    @Override
    protected String[] getSelectArgs() {
        return null;
    }

    @Override
    protected T BuildObject(Cursor cursorData) {
        T data = super.BuildObject(cursorData);

        data.setEventSequenceId(ReadValue(cursorData, SEQUENCEID, (int)0));

        return (T) data;
    }

    @Override
    protected ContentValues PersistContentValues(T data) {
        ContentValues content = super.PersistContentValues(data);

        PutValue(content, SEQUENCEID, data.getEventSequenceId());

        return content;
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // custom methods
    ///////////////////////////////////////////////////////////////////////////////////////
}
