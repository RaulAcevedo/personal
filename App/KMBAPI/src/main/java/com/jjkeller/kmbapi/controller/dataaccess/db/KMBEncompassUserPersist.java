package com.jjkeller.kmbapi.controller.dataaccess.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.jjkeller.kmbapi.controller.KMBEncompassUser;
import com.jjkeller.kmbapi.controller.dataaccess.AbstractDBAdapter;

/**
 * Created by eth6134 on 6/8/16.
 */
public class KMBEncompassUserPersist<T extends KMBEncompassUser> extends AbstractDBAdapter<T> {


    private static final String USERID = "UserId";
    private static final String LASTNAME = "LastName";
    private static final String FIRSTNAME = "FirstName";
    private static final String USERNAME = "UserName";

    private static final String SQL_SELECT_PRIMARYKEY_COMMAND = "select Key from KMBEncompassUser where UserId=?";
    private static final String SQL_SELECT_COMMAND = "select * from KMBEncompassUser";

    ///////////////////////////////////////////////////////////////////////////////////////
    // constructors
    ///////////////////////////////////////////////////////////////////////////////////////
    public KMBEncompassUserPersist(Class<T> clazz, Context ctx)
    {
        super(clazz, ctx);

        setDbTableName(DB_TABLE_KMBENCOMPASSUSER);
    }
    ///////////////////////////////////////////////////////////////////////////////////////
    // @Override methods
    ///////////////////////////////////////////////////////////////////////////////////////
    @Override
    public String getSelectPrimaryKeyCommand()
    {
        return SQL_SELECT_PRIMARYKEY_COMMAND;
    }

    @Override
    protected String[] getSelectPrimaryKeyArgs(T data) {
        return new String[]{data.getUserId()};
    }

    @Override
    protected String getSelectCommand()
    {
        return SQL_SELECT_COMMAND;
    }

    @Override
    protected T BuildObject(Cursor cursorData)
    {
        T data = super.BuildObject(cursorData);

        data.setUserId(ReadValue(cursorData, USERID, (String)null));
        data.setLastName(ReadValue(cursorData, LASTNAME, (String)null));
        data.setFirstName(ReadValue(cursorData, FIRSTNAME, (String)null));
        data.setUserName(ReadValue(cursorData, USERNAME, (String)null));
        return (T) data;
    }

    @Override
    protected ContentValues PersistContentValues(T data)
    {
        ContentValues content = super.PersistContentValues(data);

        PutValue(content, USERID, data.getUserId());
        PutValue(content, LASTNAME, data.getLastName());
        PutValue(content, FIRSTNAME, data.getFirstName());
        PutValue(content, USERNAME, data.getUserName());

        return content;
    }


}
