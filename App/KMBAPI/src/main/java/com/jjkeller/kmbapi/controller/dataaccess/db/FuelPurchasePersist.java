package com.jjkeller.kmbapi.controller.dataaccess.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.jjkeller.kmbapi.controller.dataaccess.AbstractDBAdapter;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.enums.FuelClassificationEnum;
import com.jjkeller.kmbapi.enums.FuelUnitEnum;
import com.jjkeller.kmbapi.proxydata.FuelPurchase;

import java.util.Date;

public class FuelPurchasePersist<T extends FuelPurchase> extends AbstractDBAdapter<T> {

	///////////////////////////////////////////////////////////////////////////////////////
	// private members
	///////////////////////////////////////////////////////////////////////////////////////
	private static final String FUELAMOUNT = "FuelAmount";
	private static final String FUELUNIT = "FuelUnit";
	private static final String FUELCLASSIFICATION = "FuelClassification";
	private static final String PURCHASEDATE = "PurchaseDate";
	private static final String STATECODE = "StateCode";
	private static final String VENDORNAME = "VendorName";
	private static final String INVOICENUMBER = "InvoiceNumber";
	private static final String FUELCOST = "FuelCost";
	private static final String TRACTORNUMBER = "TractorNumber";

    private static final String SQL_SELECT_COMMAND = "select * from [FuelPurchase]";

    private static final String SQL_SELECT_PRIMARYKEY_COMMAND = "select [Key] from [FuelPurchase] where PurchaseDate=?";

    private static final String SQL_SELECT_UNSUBMITTED_COMMAND = "select * from [FuelPurchase] where IsSubmitted=0 Order By PurchaseDate desc";

    private static final String SQL_PURGE_COMMAND = "delete from FuelPurchase where PurchaseDate < ? AND IsSubmitted=1";
    
    ///////////////////////////////////////////////////////////////////////////////////////
	// constructors
	///////////////////////////////////////////////////////////////////////////////////////
	public FuelPurchasePersist(Class<T> clazz, Context ctx) {
		super(clazz, ctx);

		setDbTableName(DB_TABLE_FUELPURCHASE);
	}

	public FuelPurchasePersist(Class<T> clazz, Context ctx, User user) {
		super(clazz, ctx, user);

		setDbTableName(DB_TABLE_FUELPURCHASE);
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
		if(data.getPurchaseDate() == null)
			args = new String[]{""};
		else
			args = new String[]{DateUtility.getHomeTerminalSqlDateTimeFormat().format(data.getPurchaseDate())};
		
		return args;
	}
	
	@Override
	protected String getSelectCommand() {
		return SQL_SELECT_COMMAND;
	}

	@Override
	protected String getSelectUnsubmittedCommand(){
		return SQL_SELECT_UNSUBMITTED_COMMAND;
	}
	
	@Override
	protected T BuildObject(Cursor cursorData)
	{
		T data = super.BuildObject(cursorData);
		
		data.setFuelAmount(ReadValue(cursorData, FUELAMOUNT, (float)0));
		data.getFuelClassification().setValue(ReadValue(cursorData, FUELCLASSIFICATION, FuelClassificationEnum.NULL));
		data.setFuelCost(ReadValue(cursorData, FUELCOST, (float)0));
		data.getFuelUnit().setValue(ReadValue(cursorData, FUELUNIT, FuelUnitEnum.NULL));
		data.setVendorName(ReadValue(cursorData, VENDORNAME, (String)null));
		data.setTractorNumber(ReadValue(cursorData, TRACTORNUMBER, (String)null));
		data.setInvoiceNumber(ReadValue(cursorData, INVOICENUMBER, (String)null));
		data.setStateCode(ReadValue(cursorData, STATECODE, (String)null));
		data.setPurchaseDate(ReadValue(cursorData, PURCHASEDATE, (Date)null, DateUtility.getHomeTerminalSqlDateTimeFormat()));
		
		return data;
	}

	protected ContentValues PersistContentValues(T data)
	{
		ContentValues content = super.PersistContentValues(data);
		
		PutValue(content,FUELAMOUNT, data.getFuelAmount());
		PutValue(content,FUELCLASSIFICATION, data.getFuelClassification().getValue());
		PutValue(content,FUELCOST, data.getFuelCost());
		PutValue(content,FUELUNIT, data.getFuelUnit().getValue());
		PutValue(content,VENDORNAME, data.getVendorName());
		PutValue(content,TRACTORNUMBER, data.getTractorNumber());
		PutValue(content,INVOICENUMBER, data.getInvoiceNumber());
		PutValue(content,STATECODE, data.getStateCode());
		PutValue(content,PURCHASEDATE, data.getPurchaseDate(), DateUtility.getHomeTerminalSqlDateTimeFormat());
		PutValue(content, ISSUBMITTED, 0);

		return content ;
	}
	
	///////////////////////////////////////////////////////////////////////////////////////
	// custom methods
	///////////////////////////////////////////////////////////////////////////////////////
	
    /// <summary>
    /// Purge any old records, based on the cutoff date, using the PURGE command
    /// A parm of @cutoffDate will be added to the command.
    /// </summary>
    /// <param name="cutoffDate"></param>
    public void PurgeOldRecords(Date cutoffDate)
    {
		String sql = SQL_PURGE_COMMAND;
		String[] selectionArgs =  new String[]{DateUtility.getHomeTerminalSqlDateTimeFormat().format(cutoffDate)};

		ExecuteQuery(sql, selectionArgs);

    }
}
