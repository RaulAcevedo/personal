package com.jjkeller.kmbapi.proxydata;

import com.jjkeller.kmbapi.enums.FuelClassificationEnum;
import com.jjkeller.kmbapi.enums.FuelUnitEnum;

import java.util.Date;

public class FuelPurchase extends ProxyBase {

	///////////////////////////////////////////////////////////////////////////////////////
	// private members
	///////////////////////////////////////////////////////////////////////////////////////
	private float fuelAmount;
	private FuelUnitEnum fuelUnit = new FuelUnitEnum(FuelUnitEnum.NULL);
	private String stateCode;
	private FuelClassificationEnum fuelClassification = new FuelClassificationEnum(FuelClassificationEnum.NULL);
	private Date purchaseDate;
	private float fuelCost;
	private String vendorName;
	private String invoiceNumber;
	private String tractorNumber;
	
	///////////////////////////////////////////////////////////////////////////////////////
	// public get/set methods
	///////////////////////////////////////////////////////////////////////////////////////
	public float getFuelAmount() {
		return fuelAmount;
	}

	public void setFuelAmount(float fuelAmount) {
		this.fuelAmount = fuelAmount;
	}

	public FuelUnitEnum getFuelUnit() {
		return fuelUnit;
	}

	public void setFuelUnit(FuelUnitEnum fuelUnit) {
		this.fuelUnit = fuelUnit;
	}

	public String getStateCode() {
		return stateCode;
	}

	public void setStateCode(String stateCode) {
		this.stateCode = stateCode;
	}

	public FuelClassificationEnum getFuelClassification() {
		return fuelClassification;
	}

	public void setFuelClassification(FuelClassificationEnum fuelClassification) {
		this.fuelClassification = fuelClassification;
	}

	public Date getPurchaseDate() {
		return purchaseDate;
	}

	public void setPurchaseDate(Date purchaseDate) {
		this.purchaseDate = purchaseDate;
	}

	public float getFuelCost() {
		return fuelCost;
	}

	public void setFuelCost(float fuelCost) {
		this.fuelCost = fuelCost;
	}

	public String getVendorName() {
		return vendorName;
	}

	public void setVendorName(String vendorName) {
		this.vendorName = vendorName;
	}

	public String getInvoiceNumber() {
		return invoiceNumber;
	}

	public void setInvoiceNumber(String invoiceNumber) {
		this.invoiceNumber = invoiceNumber;
	}

	public String getTractorNumber() {
		return tractorNumber;
	}

	public void setTractorNumber(String tractorNumber) {
		this.tractorNumber = tractorNumber;
	}

	///////////////////////////////////////////////////////////////////////////////////////
	// Custom methods
	///////////////////////////////////////////////////////////////////////////////////////
}
