package com.jjkeller.kmbapi.controller.dataaccess.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.jjkeller.kmbapi.controller.dataaccess.AbstractDBAdapter;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.proxydata.EldRegistrationInfo;

import java.util.Date;
import java.util.List;

public class EldRegistrationInfoPersist<T extends EldRegistrationInfo>extends AbstractDBAdapter<T> {
    ///////////////////////////////////////////////////////////////////////////////////////
    // private members
    ///////////////////////////////////////////////////////////////////////////////////////

    private String fmcsaRegistrationId;
    private String fmcsaProviderName;
    private String name;
    private String eldIdentifier;
    private String firmwareType;
    private String minAppVersion;
    private String maxAppVersion;

    private static final String FMCSAREGISTRATIONID = "FmcsaRegistrationId";
    private static final String FMCSAPROVIDERNAME = "FmcsaProviderName";
    private static final String NAME = "Name";
    private static final String ELDIDENTIFIER = "EldIdentifier";
    private static final String FIRMWARETYPE = "FirmwareType";
    private static final String MINAPPVERSION = "MinAppVersion";
    private static final String MAXAPPVERSION = "MaxAppVersion";
    private static final String CHANGEDATE = "ChangeDate";

    private static final String SQL_SELECT_PRIMARYKEY_COMMAND = "select [Key] from [EldRegistrationInfo] where Key=?";
    private static final String SQL_SELECT_COMMAND = "select * from [EldRegistrationInfo] order by Key asc";
    private static final String SQL_SELECT_OLDESTCHANGEDATE_COMMAND = "select * from [EldRegistrationInfo] order by CHANGEDATE asc LIMIT 1";
    private static final String SQL_SELECT_COMMAND_BY_TYPE_VERSION = "select * from [EldRegistrationInfo] where FirmwareType=? and MinAppVersion <= ?  and (MaxAppVersion >= ? or MaxAppVersion IS NULL) and Name like '%Android%' order by MaxAppVersion";
    private static final String SQL_SELECT_COMMAND_DEFAULT = "select * from [EldRegistrationInfo] where FirmwareType=? and Name like '%Android%' order by MaxAppVersion";

    ///////////////////////////////////////////////////////////////////////////////////////
    // constructors
    ///////////////////////////////////////////////////////////////////////////////////////
    public EldRegistrationInfoPersist(Class<T> clazz, Context ctx, User user)
    {
        super(clazz, ctx, user);

        setDbTableName(DB_TABLE_ELDREGISTRATIONINFO);
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
    protected String getSelectCommand()
    {
        return SQL_SELECT_COMMAND;
    }

    @Override
    protected String[] getSelectPrimaryKeyArgs(T data) {
        return new String[]{Long.toString(data.getPrimaryKey())};
    }

    @Override
    protected T BuildObject(Cursor cursorData)
    {
        T data = super.BuildObject(cursorData);

        data.setFmcsaRegistrationId(ReadValue(cursorData, FMCSAREGISTRATIONID, (String)null));
        data.setFmcsaProviderName(ReadValue(cursorData, FMCSAPROVIDERNAME, (String)null));
        data.setName(ReadValue(cursorData, NAME, (String)null));
        data.setEldIdentifier(ReadValue(cursorData, ELDIDENTIFIER, (String)null));
        data.setFirmwareType(ReadValue(cursorData, FIRMWARETYPE, (String)null));
        data.setMinAppVersion(ReadValue(cursorData, MINAPPVERSION, (String)null));
        data.setMaxAppVersion(ReadValue(cursorData, MAXAPPVERSION, (String)null));
        data.setChangeDate(ReadValue(cursorData, CHANGEDATE, (Date) null, DateUtility.getHomeTerminalSqlDateTimeFormat()));
        return data;
    }

    @Override
    public ContentValues PersistContentValues(T data)
    {
        ContentValues content = super.PersistContentValues(data);

        PutValue(content, FMCSAREGISTRATIONID, data.getFmcsaRegistrationId());
        PutValue(content, FMCSAPROVIDERNAME, data.getFmcsaProviderName());
        PutValue(content, NAME, data.getName());
        PutValue(content, ELDIDENTIFIER, data.getEldIdentifier());
        PutValue(content, FIRMWARETYPE, data.getFirmwareType());
        PutValue(content, MINAPPVERSION, data.getMinAppVersion());
        PutValue(content, MAXAPPVERSION, data.getMaxAppVersion());
        PutValue(content, CHANGEDATE, DateUtility.getCurrentDateTimeUTC(), DateUtility.getHomeTerminalSqlDateTimeFormat());

        return content;
    }

    public List<T> FetchList()
    {
        String sql = SQL_SELECT_COMMAND;
        String[] selectionArgs =  new String[]{};

        return ExecuteFetchListRawQuery(sql, selectionArgs);
    }

    public Date FetchOldestChangeDate()
    {
        String sql = SQL_SELECT_OLDESTCHANGEDATE_COMMAND;
        String[] selectionArgs =  new String[]{};

        T eldRegistrationInfo = ExecuteFetchRawQuery(sql, selectionArgs);

        if (eldRegistrationInfo == null) return null;

        return eldRegistrationInfo.getChangeDate();
    }

    public List<T> fetchRegistrationInfoByTypeAndVersion(String type, String version)
    {
        String sql = SQL_SELECT_COMMAND_BY_TYPE_VERSION;
        String[] selectionArgs =  new String[]{type, version, version};

        return ExecuteFetchListRawQuery(sql, selectionArgs);
    }

    public List<T> fetchDefaultRegistrationInfoByType(String type)
    {
        String sql = SQL_SELECT_COMMAND_DEFAULT;
        String[] selectionArgs =  new String[]{type};

        return ExecuteFetchListRawQuery(sql, selectionArgs);
    }
}