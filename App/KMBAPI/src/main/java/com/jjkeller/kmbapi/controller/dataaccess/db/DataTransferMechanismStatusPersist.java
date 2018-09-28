package com.jjkeller.kmbapi.controller.dataaccess.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.jjkeller.kmbapi.controller.dataaccess.AbstractDBAdapter;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.proxydata.DataTransferMechanismStatus;

import java.util.Date;
import java.util.List;
/*import java.util.List;*/

/**
 * Created by KLarsen on 2/10/2017.
 */

public class DataTransferMechanismStatusPersist<T extends DataTransferMechanismStatus>extends AbstractDBAdapter<T> {
    ///////////////////////////////////////////////////////////////////////////////////////
    // private members
    ///////////////////////////////////////////////////////////////////////////////////////

    private final String TRANSFERID = "TransferId";
    private final String DATESCHEDULEDTOTRANSFER = "DateScheduledToTransfer";
    private final String DATETRANSFERRED = "DateTransferred";
    private final String DATEOFNEXTTRANSFER = "DateOfNextTransfer";
    private final String WASSUCCESSFUL = "WasSuccessful";

    private static final String SQL_SELECT_PRIMARYKEY_COMMAND = "select [Key] from [DataTransferMechanismStatus] where TransferId=?";
    private static final String SQL_SELECT_BY_ID = "select * from [DataTransferMechanismStatus] where TransferId=?";
    private static final String SQL_SELECT_CURRENT_TRANSFER = "select * from DataTransferMechanismStatus order by [Key] DESC LIMIT 1";
    private static final String SQL_SELECT_LAST_FAILED_TRANSFER = "select * from DataTransferMechanismStatus where WasSuccessful = 0 and TransferId <> ? order by [Key] DESC LIMIT 1";
    private static final String SQL_SELECT_LAST_FOUR_TRANSFERS = "select * from DataTransferMechanismStatus where DateTransferred is not null order by [Key] DESC LIMIT 4";
    private static final String SQL_SELECTTOPURGE_COMMAND = "SELECT Key from DataTransferMechanismStatus";
    private static final String SQL_DELETE_BY_KEY_COMMAND = "delete from DataTransferMechanismStatus where Key = ? ";


    ///////////////////////////////////////////////////////////////////////////////////////
    // constructors
    ///////////////////////////////////////////////////////////////////////////////////////
    public DataTransferMechanismStatusPersist(Class<T> clazz, Context ctx, User user)
    {
        super(clazz, ctx, user);

        setDbTableName(DB_TABLE_DATATRANSFERMECHANISMSTATUS);
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
        return new String[]{data.getTransferId()};
    }

    @Override
    protected T BuildObject(Cursor cursorData)
    {
        T data = super.BuildObject(cursorData);

        data.setTransferId(ReadValue(cursorData, TRANSFERID, (String)null));
        data.setDateScheduledToTransfer(ReadValue(cursorData, DATESCHEDULEDTOTRANSFER, (Date)null, DateUtility.getHomeTerminalSqlDateFormat()));

        Object dteTransferred = ReadValue(cursorData, DATETRANSFERRED, (String)null);
        if(dteTransferred != null) {
            data.setDateTransferred(ReadValue(cursorData, DATETRANSFERRED, (Date) null, DateUtility.getHomeTerminalSqlDateFormat()));
        }

        Object dteNextTransfer = ReadValue(cursorData, DATEOFNEXTTRANSFER, (String)null);
        if(dteNextTransfer != null) {
            data.setDateOfNextTransfer(ReadValue(cursorData, DATEOFNEXTTRANSFER, (Date) null, DateUtility.getHomeTerminalSqlDateFormat()));
        }

        data.setWasSuccessful(ReadValue(cursorData, WASSUCCESSFUL, (boolean)false));

        return data;
    }

    @Override
    public ContentValues PersistContentValues(T data)
    {
        ContentValues content = super.PersistContentValues(data);

        PutValue(content, TRANSFERID, data.getTransferId());
        PutValue(content, DATESCHEDULEDTOTRANSFER, DateUtility.getHomeTerminalSqlDateFormat().format(data.getDateScheduledToTransfer()));
        if (data.getDateTransferred() != null) {
            PutValue(content, DATETRANSFERRED, DateUtility.getHomeTerminalSqlDateFormat().format(data.getDateTransferred()));
        }
        if (data.getDateOfNextTransfer() != null) {
            PutValue(content, DATEOFNEXTTRANSFER, DateUtility.getHomeTerminalSqlDateFormat().format(data.getDateOfNextTransfer()));
        }
        PutValue(content, WASSUCCESSFUL, data.getWasSuccessful());
        return content;
    }

    public DataTransferMechanismStatus FetchByTransferId(String transferId)
    {
        String[] selectionArgs =  new String[]{transferId};
        return ExecuteFetchRawQuery(SQL_SELECT_BY_ID, selectionArgs);
    }

    public DataTransferMechanismStatus FetchLastFailedTransfer(String transferId)
    {
        String[] selectionArgs =  new String[]{transferId};
        return ExecuteFetchRawQuery(SQL_SELECT_LAST_FAILED_TRANSFER, selectionArgs);
    }

    public List<T> FetchLastFourTransfers()
    {
        return ExecuteFetchListRawQuery(SQL_SELECT_LAST_FOUR_TRANSFERS, new String[0]);
    }

    public DataTransferMechanismStatus FetchCurrentTransfer() {
        return ExecuteFetchRawQuery(SQL_SELECT_CURRENT_TRANSFER, new String[0]);
    }

    public void DeleteAllTransfers() {
        this.open();
        Cursor cursor = this.ExecuteRawQuery(SQL_SELECTTOPURGE_COMMAND,  new String[0]);

        if (cursor != null && cursor.moveToFirst()) {

            while (!cursor.isAfterLast()) {
                ExecuteQuery(SQL_DELETE_BY_KEY_COMMAND, new String[]{cursor.getString(0)});
                cursor.moveToNext();
            }
        }
    }

}
