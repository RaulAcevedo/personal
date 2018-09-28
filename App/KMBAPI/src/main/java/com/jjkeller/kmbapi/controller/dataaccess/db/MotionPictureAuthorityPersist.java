package com.jjkeller.kmbapi.controller.dataaccess.db;
import android.app.DownloadManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.jjkeller.kmbapi.controller.dataaccess.AbstractDBAdapter;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.proxydata.MotionPictureAuthority;

import java.util.Date;
import java.util.List;

/**
 * Created by tgrayeb on 9/28/2016.
 */
public class MotionPictureAuthorityPersist <T extends MotionPictureAuthority>extends AbstractDBAdapter<T> {
    ///////////////////////////////////////////////////////////////////////////////////////
    // private members
    ///////////////////////////////////////////////////////////////////////////////////////

    private final String MOTIONPICTUREAUTHORITYID = "MotionPictureAuthorityId";
    private final String COMPANYID = "CompanyId";
    private final String NAME = "Name";
    private final String ADDRESSLINE1 = "AddressLine1";
    private final String ADDRESSLINE2 = "AddressLine2";
    private final String CITY = "City";
    private final String STATE = "State";
    private final String ZIPCODE = "ZipCode";
    private final String BUSINESSHOURS = "BusinessHours";
    private final String DOTNUMBER = "DOTNumber";
    private static final String COMPANYKEY = "CompanyKey";
    private final String ISACTIVE = "IsActive";

    private static final String SQL_SELECT_PRIMARYKEY_COMMAND = "select [Key] from [MotionPictureAuthority] where CompanyKey=? and Name=? order by Name asc";
    private static final String SQL_SELECT_COMMAND = "select * from [MotionPictureAuthority] where CompanyKey=? order by Name asc";
    private static final String SQL_SELECT_ACTIVE_COMMAND = "select * from [MotionPictureAuthority] where CompanyKey=? and IsActive=1 order by Name asc";
    private static final String SQL_SELECT_BY_AUTHORITY_ID = "select * from [MotionPictureAuthority] where CompanyKey=? and MotionPictureAuthorityId=? order by Name asc";

    ///////////////////////////////////////////////////////////////////////////////////////
    // constructors
    ///////////////////////////////////////////////////////////////////////////////////////
    public MotionPictureAuthorityPersist(Class<T> clazz, Context ctx,  User user)
    {
        super(clazz, ctx, user);

        setDbTableName(DB_TABLE_MOTIONPICTUREAUTHORITY);
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
        return new String[]{Long.toString(this.getCurrentUser().getCompanyKey()), data.getName()};
    }

    @Override
    protected T BuildObject(Cursor cursorData)
    {
        T data = super.BuildObject(cursorData);

        data.setMotionPictureAuthorityId(ReadValue(cursorData, MOTIONPICTUREAUTHORITYID, (String)null));
        data.setName(ReadValue(cursorData, NAME, (String)null));
        data.setAddressLine1(ReadValue(cursorData, ADDRESSLINE1, (String)null));
        data.setAddressLine2(ReadValue(cursorData, ADDRESSLINE2, (String)null));
        data.setCity(ReadValue(cursorData, CITY, (String)null));
        data.setState(ReadValue(cursorData, STATE, (String)null));
        data.setZipCode(ReadValue(cursorData, ZIPCODE, (String)null));
        data.setBusinessHours(ReadValue(cursorData, BUSINESSHOURS, (String)null));
        data.setDOTNumber(ReadValue(cursorData, DOTNUMBER, (String)null));
        data.setCompanyKey(ReadValue(cursorData, COMPANYKEY, 0));
        data.setIsActive(ReadValue(cursorData, ISACTIVE, false));

        return data;
    }

    @Override
    public ContentValues PersistContentValues(T data)
    {
        ContentValues content = super.PersistContentValues(data);

        PutValue(content, MOTIONPICTUREAUTHORITYID, data.getMotionPictureAuthorityId());
        PutValue(content, NAME, data.getName());
        PutValue(content, ADDRESSLINE1, data.getAddressLine1());
        PutValue(content, ADDRESSLINE2, data.getAddressLine2());
        PutValue(content, CITY, data.getCity());
        PutValue(content, STATE, data.getState());
        PutValue(content, ZIPCODE, data.getZipCode());
        PutValue(content, BUSINESSHOURS, data.getBusinessHours());
        PutValue(content, DOTNUMBER, data.getDOTNumber());
        PutValue(content, COMPANYKEY, this.getCurrentUser().getCompanyKey());
        PutValue(content, ISACTIVE, data.getIsActive());

        return content;
    }

    public List<T> FetchActiveAuthorities()
    {
        String sql = SQL_SELECT_ACTIVE_COMMAND;
        String[] selectionArgs =  new String[]{String.valueOf(this.getCurrentUser().getCompanyKey())};

        List<T> activeAuthorityList = ExecuteFetchListRawQuery(sql, selectionArgs);

        return activeAuthorityList;
    }

    public MotionPictureAuthority FetchAuthorityByAuthorityId(String motionPictureAuthorityId) {
        String sql = SQL_SELECT_BY_AUTHORITY_ID;
        String[] selectionArgs =  new String[]{Long.toString(this.getCurrentUser().getCompanyKey()),motionPictureAuthorityId};
        MotionPictureAuthority authority = ExecuteFetchRawQuery(sql, selectionArgs);
        return authority;
    }
}