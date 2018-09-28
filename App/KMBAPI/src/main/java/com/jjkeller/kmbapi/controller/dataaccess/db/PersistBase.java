package com.jjkeller.kmbapi.controller.dataaccess.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.controller.utility.ErrorLogHelper;
import com.jjkeller.kmbapi.proxydata.ProxyBase;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public abstract class PersistBase<T extends ProxyBase> {

	///////////////////////////////////////////////////////////////////////////////////////
	// private members
	///////////////////////////////////////////////////////////////////////////////////////
	private final Class<T> clazz;

	private User user;
	private Context ctx;
	protected final String KEY = "Key";
    protected final String ISSUBMITTED = "IsSubmitted";
	
	///////////////////////////////////////////////////////////////////////////////////////
	// constructors
	///////////////////////////////////////////////////////////////////////////////////////
	public PersistBase(Class<T> clazz, Context ctx)
	{
		this.user = GlobalState.getInstance().getCurrentUser();
		this.clazz = clazz;
		this.ctx = ctx;
	}
	
	public PersistBase(Class<T> clazz, Context ctx, User user)
	{
		this.clazz = clazz;
		this.user = user;
		this.ctx = ctx;
	}
	
	///////////////////////////////////////////////////////////////////////////////////////
	// get/set methods
	///////////////////////////////////////////////////////////////////////////////////////
	public Context getContext()
	{
		return this.ctx;
	}
	public void setContext(Context ctx)
	{
		this.ctx = ctx;		
	}
	
	public User getCurrentUser()
	{
		return this.user;
	}
	public void setCurrentUser(User user)
	{
		this.user = user;
	}

	protected String getProxyDataClassName()
	{
		return clazz.getSimpleName();
	}

	protected abstract String getSelectCommand();
	
	///////////////////////////////////////////////////////////////////////////////////////
	// data methods
	///////////////////////////////////////////////////////////////////////////////////////
	protected T CreateFromCursor(Cursor cursorData){
		T obj = null;

		if (cursorData != null && cursorData.moveToFirst())
		{
			obj = BuildObject(cursorData);
			GetAdditionalData(obj);
		}	
		
		return obj;
	}
	
	protected List<T> CreateListFromCursor(Cursor cursorData){
		List<T> objList = new ArrayList<T>();

		if (cursorData != null && cursorData.moveToFirst())
		{
			while(!cursorData.isAfterLast())
			{
				T obj = BuildObject(cursorData);
				GetAdditionalData(obj);
				objList.add(obj);
				cursorData.moveToNext();
			}
		}	
		
		return objList;
	}
	
	protected T BuildObject(Cursor cursorData) {
		T obj = null;

		try {
			obj = clazz.newInstance();
		} catch (IllegalAccessException e) {
			ErrorLogHelper.RecordException(this.getContext(), e);
			e.printStackTrace();
		} catch (InstantiationException e) {
			ErrorLogHelper.RecordException(this.getContext(), e);
			e.printStackTrace();
		}
		
		obj.setPrimaryKey(ReadValue(cursorData, KEY, (long)0));

		return obj;
	}

	protected void GetAdditionalData(T obj) {}

	public void Persist(T data) {}

	protected void SaveRelatedData(T data) {}

	protected abstract ContentValues PersistContentValues(T data);
	
	///////////////////////////////////////////////////////////////////////////////////////
	// ReadValue methods
	///////////////////////////////////////////////////////////////////////////////////////
	protected int ReadValue(Cursor cursorData, String columnName, int defaultValue)
	{
		int retVal = defaultValue;
		int index = cursorData.getColumnIndex(columnName);
		
		if(index != -1 && !cursorData.isNull(index))
		{
			retVal = cursorData.getInt(index);
		}
		
		return retVal;
	}

	protected Integer ReadValue(Cursor cursorData, String columnName, Integer defaultValue)
	{
		Integer retVal = defaultValue;
		int index = cursorData.getColumnIndex(columnName);

		if(index != -1 && !cursorData.isNull(index))
		{
			retVal = cursorData.getInt(index);
		}

		return retVal;
	}

	protected Long ReadValue(Cursor cursorData, String columnName, Long defaultValue)
	{
		long retVal = defaultValue;
		int index = cursorData.getColumnIndex(columnName);
		
		if(index != -1 && !cursorData.isNull(index))
		{
			retVal = cursorData.getLong(index);
		}
		
		return retVal;
	}
	
	protected String ReadValue(Cursor cursorData, String columnName, String defaultValue)
	{
		String retVal = defaultValue;
		int index = cursorData.getColumnIndex(columnName);
		
		if(index != -1 && !cursorData.isNull(index))
		{
			retVal = cursorData.getString(index);
		}
		
		return retVal;
	}

	protected float ReadValue(Cursor cursorData, String columnName, float defaultValue)
	{
		float retVal = defaultValue;
		int index = cursorData.getColumnIndex(columnName);

		if(index != -1 && !cursorData.isNull(index))
		{
			retVal = cursorData.getFloat(index);
		}

		return retVal;
	}

	protected Float ReadValue(Cursor cursorData, String columnName, Float defaultValue)
	{
		Float retVal = defaultValue;
		int index = cursorData.getColumnIndex(columnName);
		
		if(index != -1 && !cursorData.isNull(index))
		{
			retVal = cursorData.getFloat(index);
		}
		
		return retVal;
	}

	protected long ReadValue(Cursor cursorData, String columnName, long defaultValue)
	{
		long retVal = defaultValue;
		int index = cursorData.getColumnIndex(columnName);

		if(index != -1 && !cursorData.isNull(index))
		{
			retVal = cursorData.getLong(index);
		}

		return retVal;
	}

	protected double ReadValue(Cursor cursorData, String columnName, double defaultValue)
    {
        double retVal = defaultValue;
        int index = cursorData.getColumnIndex(columnName);

        if(index != -1 && !cursorData.isNull(index))
        {
            retVal = cursorData.getFloat(index);
        }

        return retVal;
    }

	protected Double ReadValue(Cursor cursorData, String columnName, Double defaultValue)
	{
		Double retVal = defaultValue;
		int index = cursorData.getColumnIndex(columnName);

		if(index != -1 && !cursorData.isNull(index))
		{
			retVal = (double) cursorData.getFloat(index);
		}

		return retVal;
	}

	protected Date ReadValue(Cursor cursorData, String columnName, Date defaultValue, SimpleDateFormat dateFormat)
	{
		Date retVal = defaultValue;
		int index = cursorData.getColumnIndex(columnName);
		
		if(index != -1 && !cursorData.isNull(index))
		{
			try {
				retVal = dateFormat.parse(cursorData.getString(index));
			} catch (ParseException e) {
				ErrorLogHelper.RecordException(this.getContext(), e);
				e.printStackTrace();
			}
		}
		
		return retVal;
	}

    protected DateTime ReadValue(Cursor cursorData, String columnName, DateTime defaultValue, SimpleDateFormat dateFormat)
    {
        DateTime retVal = defaultValue;
        int index = cursorData.getColumnIndex(columnName);

        if(index != -1 && !cursorData.isNull(index))
        {
            try {
                String val = cursorData.getString(index);
                Date dateVal = dateFormat.parse(val);
                retVal = new DateTime(dateVal);
            } catch (ParseException e) {
                ErrorLogHelper.RecordException(this.getContext(), e);
                e.printStackTrace();
            }
        }

        return retVal;
    }

	protected Boolean ReadValue(Cursor cursorData, String columnName, boolean defaultValue)
	{
		boolean retVal = defaultValue;
		int index = cursorData.getColumnIndex(columnName);
		
		if(index != -1 && !cursorData.isNull(index))
		{
			retVal = cursorData.getShort(index) == 1 ? true : false;
		}
		
		return retVal;
	}
	
	protected Object ReadValue(Cursor cursorData, String columnName, Object defaultValue) throws Exception
	{
		throw new Exception("ReadValue should not be called with an Object");
	}

	///////////////////////////////////////////////////////////////////////////////////////
	// PutValue methods
	///////////////////////////////////////////////////////////////////////////////////////
	protected <E> void PutValue(ContentValues content, String key, E value)
	{
		if(value == null)
		{
			content.putNull(key);
		}
		else
		{
			if(value.getClass() == String.class)
				content.put(key, (String)value);
			else if(value.getClass() == Integer.class)
				content.put(key, (Integer)value);
			else if(value.getClass() == Long.class)
				content.put(key, (Long)value);
			else if(value.getClass() == Float.class)
				content.put(key, (Float)value);
			else if(value.getClass() == Boolean.class)
				content.put(key, (Boolean)value);
			else if(value.getClass() == Double.class)
				content.put(key, (Double)value);
			else if(value.getClass() == Short.class)
				content.put(key, (Short)value);
			else if(value.getClass() == Byte.class)
				content.put(key, (Byte)value);
			else if(value.getClass() == byte[].class)
				content.put(key, (byte[])value);
			else
				//TODO Throw an exception when an unhandled class is present
				content.putNull(key);
		}
	}

	protected void PutValue(ContentValues content, String key, Date value, SimpleDateFormat dateFormat)
	{
		if(value == null)
		{
			content.putNull(key);
		}
		else
		{
			content.put(key, dateFormat.format(value));
		}
	}

    protected void PutValue(ContentValues content, String key, DateTime value, SimpleDateFormat dateFormat)
    {
        if(value == null)
        {
            content.putNull(key);
        }
        else
        {
            content.put(key, dateFormat.format(value.toDate()));
        }
    }
}
