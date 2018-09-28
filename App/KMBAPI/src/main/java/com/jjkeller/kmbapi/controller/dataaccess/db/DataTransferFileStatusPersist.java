package com.jjkeller.kmbapi.controller.dataaccess.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.jjkeller.kmbapi.controller.dataaccess.AbstractDBAdapter;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.enums.DataTransferFileStatusEnum;
import com.jjkeller.kmbapi.enums.RoadsideDataTransferMethodEnum;
import com.jjkeller.kmbapi.proxydata.DataTransferFileStatus;
import com.jjkeller.kmbapi.proxydata.DataTransferMechanismStatus;

import java.util.Date;

public class DataTransferFileStatusPersist<T extends DataTransferFileStatus>extends AbstractDBAdapter<T> {
    ///////////////////////////////////////////////////////////////////////////////////////
    // private members
    ///////////////////////////////////////////////////////////////////////////////////////

    private final String FILENAME = "Filename";
    private final String CREATEDATE = "CreateDate";
    private final String USERKEY = "UserKey";
    private final String ATTEMPTCOUNT = "AttemptCount";
    private final String STATUS = "Status";
    private final String ROADSIDEDATATRANSFERMETHOD = "RoadsideDataTransferMethod";
    private final String WASNOTIFICATIONDISPLAYED = "WasNotificationDisplayed";

    private static final String SQL_SELECT_PRIMARYKEY_COMMAND = "select [Key] from [DataTransferFileStatus] where Filename=?";
    private static final String SQL_SELECT_CURRENT_TRANSFER = "select * from DataTransferFileStatus order by [Key] DESC LIMIT 1";
    private static final String SQL_SELECT_LATEST_BY_USER_KEY = "select * from [DataTransferFileStatus] where UserKey=? AND RoadsideDataTransferMethod = ? AND Status IN (0,1,8) order by [Key] DESC LIMIT 1";

    ///////////////////////////////////////////////////////////////////////////////////////
    // constructors
    ///////////////////////////////////////////////////////////////////////////////////////
    public DataTransferFileStatusPersist(Class<T> clazz, Context ctx, User user)
    {
        super(clazz, ctx, user);

        setDbTableName(DB_TABLE_DATATRANSFERFILESTATUS);
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
        return SQL_SELECT_CURRENT_TRANSFER;
    }

    @Override
    protected String[] getSelectPrimaryKeyArgs(T data) {
        return new String[]{data.getFileName()};
    }

    @Override
    protected T BuildObject(Cursor cursorData)
    {
        T data = super.BuildObject(cursorData);

        data.setFileName(ReadValue(cursorData, FILENAME, (String)null));
        data.setCreateDate(ReadValue(cursorData, CREATEDATE, (Date) null, DateUtility.getHomeTerminalSqlDateTimeFormat()));
        data.setUserKey(ReadValue(cursorData, USERKEY, 0));
        data.setAttemptCount(ReadValue(cursorData, ATTEMPTCOUNT, 0));
        data.getStatus().setValue(ReadValue(cursorData, STATUS, DataTransferFileStatusEnum.UNKNOWN));
        data.getRoadsideDataTransferMethod().setValue(ReadValue(cursorData, ROADSIDEDATATRANSFERMETHOD, RoadsideDataTransferMethodEnum.EMAIL));
        data.setWasNotificationDisplayed(ReadValue(cursorData, WASNOTIFICATIONDISPLAYED, false));

        return data;
    }

    @Override
    public ContentValues PersistContentValues(T data)
    {
        ContentValues content = super.PersistContentValues(data);

        PutValue(content, FILENAME, data.getFileName());
        PutValue(content, CREATEDATE, DateUtility.getHomeTerminalSqlDateTimeFormat().format(data.getCreateDate()));
        long userKey = this.getCurrentUser().getCredentials().getPrimaryKey();
        if (!data.isPrimaryKeySet()) {
            PutValue(content, USERKEY, userKey);
        }
        PutValue(content, ATTEMPTCOUNT, data.getAttemptCount());
        PutValue(content, STATUS, data.getStatus().getValue());
        PutValue(content, ROADSIDEDATATRANSFERMETHOD, data.getRoadsideDataTransferMethod().getValue());
        PutValue(content, WASNOTIFICATIONDISPLAYED, data.getWasNotificationDisplayed());

        return content;
    }

    public DataTransferFileStatus FetchLatestByTransferMethod(RoadsideDataTransferMethodEnum dataTransferMethod) {
        String userKeyStr = String.valueOf(this.getCurrentUser().getCredentials().getPrimaryKey());
        String dataTransferMethodStr = String.valueOf(dataTransferMethod.getValue());

        String[] selectionArgs = new String[]{userKeyStr, dataTransferMethodStr};
        return ExecuteFetchRawQuery(SQL_SELECT_LATEST_BY_USER_KEY, selectionArgs);
    }
}
