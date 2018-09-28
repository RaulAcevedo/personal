package com.jjkeller.kmbapi.controller.dataaccess.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.jjkeller.kmbapi.controller.dataaccess.AbstractDBAdapter;
import com.jjkeller.kmbapi.enums.InspectionDefectType;
import com.jjkeller.kmbapi.proxydata.DefectList;
import com.jjkeller.kmbapi.proxydata.VehicleInspectionDefect;

public class VehicleInspectionDefectPersist <T extends VehicleInspectionDefect> extends AbstractDBAdapter<T> {

	///////////////////////////////////////////////////////////////////////////////////////
	// private members
	///////////////////////////////////////////////////////////////////////////////////////
	private long _vehicleInspectionKey;
	
	private static final String VEHICLEINSPECTIONKEY = "VehicleInspectionKey";
	private static final String DEFECT = "Defect";
	
	private static final String SQL_SELECT_PRIMARYKEY_COMMAND = "select [Key] from [VehicleInspectionDefect] where VehicleInspectionKey=? and Defect=?";
	private static final String SQL_SELECT_COMMAND = "select * from [VehicleInspectionDefect] where VehicleInspectionKey=?";
	
    ///////////////////////////////////////////////////////////////////////////////////////
	// constructors
	///////////////////////////////////////////////////////////////////////////////////////
	public VehicleInspectionDefectPersist (Class<T> clazz, Context ctx, long vehicleInspectionKey)
	{
		super(clazz, ctx);

		_vehicleInspectionKey = vehicleInspectionKey;
		
		setDbTableName(DB_TABLE_VEHICLEINSPECTIONDEFECT);
	}
	
	public VehicleInspectionDefectPersist(Class<T> clazz, Context ctx) {
		super(clazz, ctx);

		setDbTableName(DB_TABLE_VEHICLEINSPECTIONDEFECT);
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
		if(data.getVehicleInspectionKey() == null)
			args = new String[]{""};
		else
			args = new String[]{data.getVehicleInspectionKey(), String.valueOf(data.getInspectionDefectType().getValue())};
		
		return args;
	}
	
	@Override
	protected String getSelectCommand()
	{
		return SQL_SELECT_COMMAND;
	}
	
	@Override
	protected String[] getSelectArgs()
	{
		return new String[]{String.valueOf(_vehicleInspectionKey)};
	}
	
	protected ContentValues PersistContentValues(T data)
	{
		ContentValues content = super.PersistContentValues(data);
		
		PutValue(content,VEHICLEINSPECTIONKEY, _vehicleInspectionKey);
		PutValue(content,DEFECT, data.getInspectionDefectType().getValue());

		return content ;
	}
	
	protected T BuildObject(Cursor cursorData)
	{
		T data = super.BuildObject(cursorData);

        data.setVehicleInspectionKey(ReadValue(cursorData, VEHICLEINSPECTIONKEY, (String)null));
        data.getInspectionDefectType().setValue(ReadValue(cursorData, DEFECT, InspectionDefectType.NULL));
        
		return data;
	}
	
	public void Persist(DefectList defectList)
	{
		if(defectList != null)
		{
			@SuppressWarnings("unchecked")
			T[] list = (T[])defectList.getDefectList();
			Persist(list);
		}
	}
	
}
