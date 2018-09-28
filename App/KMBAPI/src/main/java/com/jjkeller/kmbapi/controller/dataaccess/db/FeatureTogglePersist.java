package com.jjkeller.kmbapi.controller.dataaccess.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.jjkeller.kmbapi.controller.dataaccess.AbstractDBAdapter;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.proxydata.FeatureToggle;

import java.util.List;

public class FeatureTogglePersist <T extends FeatureToggle> extends AbstractDBAdapter<T> {

    ///////////////////////////////////////////////////////////////////////////////////////
    // private members
    ///////////////////////////////////////////////////////////////////////////////////////
    private static final String NAME = "Name";
    private static final String STATE = "State";
    private static final String SQL_SELECT_PRIMARYKEY_COMMAND = "select Key from FeatureToggle where Name=? order by Name asc";
    private static final String SQL_SELECT_COMMAND = "select * from FeatureToggle";

    ///////////////////////////////////////////////////////////////////////////////////////
    // constructors
    ///////////////////////////////////////////////////////////////////////////////////////
    public FeatureTogglePersist(Class<T> clazz, Context ctx) {
        super(clazz, ctx);
        setDbTableName(DB_TABLE_FEATURETOGGLE);
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
        if(data.getName() == null)
            args = new String[]{""};
        else
            args = new String[]{data.getName()};

        return args;
    }

    @Override
    protected String getSelectCommand()
    {
        return SQL_SELECT_COMMAND;
    }

    protected ContentValues PersistContentValues(T data)
    {
        ContentValues content = super.PersistContentValues(data);

        PutValue(content,NAME, data.getName());
        PutValue(content,STATE, data.getState());

        return content ;
    }

    protected T BuildObject(Cursor cursorData)
    {
        T data = super.BuildObject(cursorData);

        data.setName(ReadValue(cursorData, NAME, (String)null));
        data.setState(ReadValue(cursorData, STATE, (boolean)false));

        return data;
    }

    public List<T> FetchList()
    {
        String sql = SQL_SELECT_COMMAND;
        String[] selectionArgs =  new String[]{};

        return ExecuteFetchListRawQuery(sql, selectionArgs);
    }
}
