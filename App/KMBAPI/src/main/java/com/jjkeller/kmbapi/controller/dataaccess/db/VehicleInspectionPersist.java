package com.jjkeller.kmbapi.controller.dataaccess.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.jjkeller.kmbapi.controller.dataaccess.AbstractDBAdapter;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.enums.InspectionTypeEnum;
import com.jjkeller.kmbapi.proxydata.VehicleInspection;
import com.jjkeller.kmbapi.proxydata.VehicleInspectionDefect;

import java.util.Date;
import java.util.List;

public class VehicleInspectionPersist<T extends VehicleInspection> extends AbstractDBAdapter<T> {

	///////////////////////////////////////////////////////////////////////////////////////
	// private members
	///////////////////////////////////////////////////////////////////////////////////////
	private static final String EOBRSERIALNUMBER = "EobrSerialNumber";
	private static final String EOBRTRACTORNUMBER = "EobrTractorNumber";
	private static final String TRAILERNUMBER = "TrailerNumber";
	private static final String INSPECTIONTIMESTAMP = "InspectionTimestamp";
	private static final String ODOMETERREADING = "OdometerReading";
	private static final String ISCONDITIONSATISFACTORY = "IsConditionSatisfactory";
	private static final String AREDEFECTSCORRECTED = "AreDefectsCorrected";
	private static final String ARECORRECTIONSNOTNEEDED = "AreCorrectionsNotNeeded";
	private static final String REVIEWEDBYNAME = "ReviewedByName";
	private static final String REVIEWEDBYDATE = "ReviewedByDate";
	private static final String NOTES = "Notes";
	private static final String ISPOWEREDUNIT = "IsPoweredUnit";
	private static final String CREATEDBYUSERKEY = "CreatedByUserKey";
	private static final String INSPECTIONTYPE = "InspectionType";
	private static final String CERTIFIEDBYNAME = "CertifiedByName";
	private static final String CERTIFIEDBYDATE = "CertifiedByDate";
	private static final String SUBMITTIMESTAMP = "SubmitTimestamp";
	private static final String REVIEWEDBYEMPLOYEEID = "ReviewedByEmployeeId";
	
	private static final String SQL_SELECT_PRIMARYKEY_TRACTOR_COMMAND = "select [Key] from [VehicleInspection] where EobrSerialNumber=? and InspectionTimeStamp=?";
	private static final String SQL_SELECT_PRIMARYKEY_TRAILER_COMMAND = "select [Key] from [VehicleInspection] where lower(TrailerNumber)=? and InspectionTimeStamp=?";
	
	private static final String SQL_SELECT_RECENTTRACTORFORUSER_COMMAND = "select * from [VehicleInspection] where EobrSerialNumber=? and InspectionType=2 ORDER BY InspectionTimestamp DESC LIMIT 1";
	private static final String SQL_SELECT_RECENTTRACTORPREINSPECTIONFORUSER_COMMAND = "select * from [VehicleInspection] where EobrSerialNumber=? and InspectionType=1 ORDER BY InspectionTimestamp DESC LIMIT 1";
	private static final String SQL_SELECT_RECENTTRAILERFORUSER_COMMAND = "select * from [VehicleInspection] where lower(TrailerNumber)=? and InspectionType=2 ORDER BY InspectionTimestamp DESC LIMIT 1";
	private static final String SQL_SELECT_RECENTTRAILERPREINSPECTIONFORUSER_COMMAND = "select * from [VehicleInspection] where lower(TrailerNumber)=? and InspectionType=1 ORDER BY InspectionTimestamp DESC LIMIT 1";
	
	// SJN 2011.12.28 this persist object needs to know if a powered/towed unit is being worked with because the
	// natural key is different for each type of entity
	private boolean _isPoweredUnit = true;
	
    ///////////////////////////////////////////////////////////////////////////////////////
	// constructors
	///////////////////////////////////////////////////////////////////////////////////////
	public VehicleInspectionPersist(Class<T> clazz, Context ctx, boolean isPoweredUnit) {
		super(clazz, ctx);

		_isPoweredUnit = isPoweredUnit;
		setDbTableName(DB_TABLE_VEHICLEINSPECTION);
	}

	///////////////////////////////////////////////////////////////////////////////////////
	// @Override methods
	///////////////////////////////////////////////////////////////////////////////////////
	@Override
	public String getSelectPrimaryKeyCommand() {
		if(_isPoweredUnit)			
			return SQL_SELECT_PRIMARYKEY_TRACTOR_COMMAND;
		else
			return SQL_SELECT_PRIMARYKEY_TRAILER_COMMAND;
	}
	
	@Override
	protected String[] getSelectPrimaryKeyArgs(T data) {
		String [] args = null; 
		if(data.getIsPoweredUnit())
		{
			if(data.getSerialNumber() == null)
				args = new String[]{""};
			else if(data.getSerialNumber() != null && data.getInspectionTimeStamp() != null)
				args = new String[]{data.getSerialNumber(), DateUtility.getHomeTerminalSqlDateTimeFormat().format(data.getInspectionTimeStamp())};
		}
		else
		{
			if(data.getTrailerNumber() == null)
				args = new String[]{""};
			else if(data.getTrailerNumber() != null && data.getInspectionTimeStamp() != null)
				args = new String[]{data.getTrailerNumber().toLowerCase(), DateUtility.getHomeTerminalSqlDateTimeFormat().format(data.getInspectionTimeStamp())};			
		}
		
		return args;
	}

	protected ContentValues PersistContentValues(T data)
	{
		ContentValues content = super.PersistContentValues(data);
		
		PutValue(content,EOBRSERIALNUMBER, data.getSerialNumber());
		PutValue(content,EOBRTRACTORNUMBER, data.getTractorNumber());
		PutValue(content,TRAILERNUMBER, data.getTrailerNumber());
		PutValue(content,INSPECTIONTIMESTAMP, data.getInspectionTimeStamp(), DateUtility.getHomeTerminalSqlDateTimeFormat());
		PutValue(content,ODOMETERREADING, data.getInspectionOdometer());
		PutValue(content,ISCONDITIONSATISFACTORY, data.getIsConditionSatisfactory());
		PutValue(content,AREDEFECTSCORRECTED, data.getAreDefectsCorrected());
		PutValue(content,ARECORRECTIONSNOTNEEDED, data.getAreCorrectionsNotNeeded());
		PutValue(content,REVIEWEDBYNAME, data.getReviewedByName());
		PutValue(content,REVIEWEDBYDATE, data.getReviewedByDate(), DateUtility.getHomeTerminalSqlDateTimeFormat());
		PutValue(content,NOTES, data.getNotes());
		PutValue(content,ISPOWEREDUNIT, data.getIsPoweredUnit());
		PutValue(content,CREATEDBYUSERKEY, data.getCreatedByUserKey());
		PutValue(content,INSPECTIONTYPE, data.getInspectionTypeEnum().getValue());
		PutValue(content,CERTIFIEDBYNAME, data.getCertifiedByName());
		PutValue(content,CERTIFIEDBYDATE, data.getCertifiedByDate(), DateUtility.getHomeTerminalSqlDateTimeFormat());
		PutValue(content,ISSUBMITTED, false);
		PutValue(content,SUBMITTIMESTAMP, data.getSubmitTimestamp(), DateUtility.getHomeTerminalSqlDateTimeFormat());
		PutValue(content,REVIEWEDBYEMPLOYEEID, data.getReviewedByEmployeeId());

		return content ;
	}

	@Override
    protected void SaveRelatedData(T vehicleInspectionData)
    {
		super.SaveRelatedData(vehicleInspectionData);
		long inspectionKey = vehicleInspectionData.getPrimaryKey();
		
		if (!vehicleInspectionData.getDefectList().IsEmpty())
    	{
	    	VehicleInspectionDefectPersist<VehicleInspectionDefect> inspectionDefectPersist = new VehicleInspectionDefectPersist<VehicleInspectionDefect>(VehicleInspectionDefect.class, this.getContext(), inspectionKey);
			inspectionDefectPersist.Persist(vehicleInspectionData.getDefectList());
    	}
    }

	protected T BuildObject(Cursor cursorData)
	{
		T data = super.BuildObject(cursorData);
		
        data.setSerialNumber(ReadValue(cursorData, EOBRSERIALNUMBER, (String)null));
        data.setTractorNumber(ReadValue(cursorData, EOBRTRACTORNUMBER, (String)null));
        data.setTrailerNumber(ReadValue(cursorData, TRAILERNUMBER, (String)null));
        data.setInspectionTimeStamp(ReadValue(cursorData, INSPECTIONTIMESTAMP, (Date)null, DateUtility.getHomeTerminalSqlDateTimeFormat()));
        data.setInspectionOdometer(ReadValue(cursorData, ODOMETERREADING, (float)0));
        data.setIsConditionSatisfactory(ReadValue(cursorData, ISCONDITIONSATISFACTORY, false));
        data.setAreDefectsCorrected(ReadValue(cursorData, AREDEFECTSCORRECTED, false));
        data.setAreCorrectionsNotNeeded(ReadValue(cursorData, ARECORRECTIONSNOTNEEDED, true));
        data.setReviewedByName(ReadValue(cursorData, REVIEWEDBYNAME, (String)null));
        data.setReviewedByDate(ReadValue(cursorData, REVIEWEDBYDATE, (Date)null, DateUtility.getHomeTerminalSqlDateTimeFormat()));
        data.setNotes(ReadValue(cursorData, NOTES, (String)null));
        data.setIsPoweredUnit(ReadValue(cursorData, ISPOWEREDUNIT, true));
        data.setCreatedByUserKey(ReadValue(cursorData, CREATEDBYUSERKEY, (int)0));
        data.getInspectionTypeEnum().setValue(ReadValue(cursorData, INSPECTIONTYPE, InspectionTypeEnum.NULL));
        data.setCertifiedByName(ReadValue(cursorData, CERTIFIEDBYNAME, (String)null));
        data.setCertifiedByDate(ReadValue(cursorData, CERTIFIEDBYDATE, (Date)null, DateUtility.getHomeTerminalSqlDateTimeFormat()));
        data.setSubmitTimestamp(ReadValue(cursorData, SUBMITTIMESTAMP, (Date)null, DateUtility.getHomeTerminalSqlDateTimeFormat()));
        data.setReviewedByEmployeeId(ReadValue(cursorData, REVIEWEDBYEMPLOYEEID, (String)null));
		
		return data;
	}
	
	@Override
    protected void GetAdditionalData(T inspection)
    {
		super.GetAdditionalData(inspection);
        if (inspection != null)
        {
            // Get defects for the inspection.
            VehicleInspectionDefectPersist<VehicleInspectionDefect> inspectionDefectPersist = new VehicleInspectionDefectPersist<VehicleInspectionDefect>(VehicleInspectionDefect.class, this.getContext(), inspection.getPrimaryKey());
            List<VehicleInspectionDefect> defectList = inspectionDefectPersist.FetchList();
            VehicleInspectionDefect[] array = defectList.toArray(new VehicleInspectionDefect[defectList.size()]);
            if(defectList != null) inspection.getDefectList().setDefectList(array);
        }
    }
	
    /// <summary>
    /// Fetch the most recent inspection report for a specific user and eobr.
    /// </summary>
    /// <returns>List<VehicleInspection></returns>
    public VehicleInspection FetchRecentTractorForUser(User usr, String eobrSerialNumber) 
    {
    	VehicleInspection vehicleInspection = ExecuteFetchRawQuery(SQL_SELECT_RECENTTRACTORFORUSER_COMMAND, new String[] {eobrSerialNumber});		
		return vehicleInspection;					
    }
    
	
    /// <summary>
    /// Fetch the most recent pre inspection report for a specific user and eobr.
    /// </summary>
    /// <returns>List<VehicleInspection></returns>
    public VehicleInspection FetchRecentTractorPreInspectionForUser(User usr, String eobrSerialNumber) 
    {
    	VehicleInspection vehicleInspection = ExecuteFetchRawQuery(SQL_SELECT_RECENTTRACTORPREINSPECTIONFORUSER_COMMAND, new String[] {eobrSerialNumber});		
		return vehicleInspection;					
    }

    /// <summary>
    /// Fetch the most recent inspection report for a specific user and eobr.
    /// </summary>
    /// <returns>List<VehicleInspection></returns>
    public VehicleInspection FetchRecentTrailerForUser(User usr, String trailerNumber) 
    {
    	VehicleInspection vehicleInspection = ExecuteFetchRawQuery(SQL_SELECT_RECENTTRAILERFORUSER_COMMAND, new String[] {trailerNumber == null ? trailerNumber : trailerNumber.toLowerCase()});		
		return vehicleInspection;					
    }

    /// <summary>
    /// Fetch the most recent pre inspection report for a specific user and eobr.
    /// </summary>
    /// <returns>List<VehicleInspection></returns>
    public VehicleInspection FetchRecentTrailerPreInspectionForUser(User usr, String trailerNumber) 
    {
    	VehicleInspection vehicleInspection = ExecuteFetchRawQuery(SQL_SELECT_RECENTTRAILERPREINSPECTIONFORUSER_COMMAND, new String[] {trailerNumber == null ? trailerNumber : trailerNumber.toLowerCase()});		
    	return vehicleInspection;					
    }
    
}
