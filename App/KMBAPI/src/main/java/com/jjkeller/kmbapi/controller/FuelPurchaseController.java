package com.jjkeller.kmbapi.controller;

import android.content.Context;

import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.EOBR.EobrReader;
import com.jjkeller.kmbapi.controller.dataaccess.FuelPurchaseFacade;
import com.jjkeller.kmbapi.controller.share.ControllerBase;
import com.jjkeller.kmbapi.controller.utility.RESTWebServiceHelper;
import com.jjkeller.kmbapi.enums.FuelClassificationEnum;
import com.jjkeller.kmbapi.enums.FuelUnitEnum;
import com.jjkeller.kmbapi.proxydata.FuelPurchase;
import com.jjkeller.kmbapi.proxydata.FuelPurchaseList;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public class FuelPurchaseController extends ControllerBase {

	public FuelPurchaseController(Context ctx) {
		super(ctx);
	}

    /// <summary>
    /// Answer the Fuel Purchase that is being added to the system.
    /// It's considered 'pending' because it has not been saved anywhere yet.
    /// </summary>
	public FuelPurchase getWorkingFuelPurchase()
	{
		FuelPurchase fuelPurchase = GlobalState.getInstance().getFuelPurchase();
		if (fuelPurchase == null)
		{
			fuelPurchase = new FuelPurchase();
			fuelPurchase.setTractorNumber(EobrReader.getInstance().getEobrIdentifier());
		}
		
		return fuelPurchase;
	}
	public void setWorkingFuelPurchase(FuelPurchase fuelPurchase)
	{
		GlobalState.getInstance().setFuelPurchase(fuelPurchase);
	}
	
	public List<FuelPurchase> UnsubmittedFuelPurchaseList()
	{
		FuelPurchaseFacade facade = new FuelPurchaseFacade(this.getContext(), this.getCurrentUser());
		return facade.FetchAllUnsubmitted();
	}
	
    /// <summary>
    /// Begin to create a fuel purchase.   This will be built in memory, and
    /// committed later on.
    /// </summary>
    /// <param name="fuelAmount">amount of fuel being added</param>
    /// <param name="units">unit (gallon/liter) of the amount</param>
    /// <param name="stateCode">state where the purchase was made</param>
    public void StartFuelPurchase(float fuelAmount, FuelUnitEnum units, String stateCode, String tractorNumber)
    {
        FuelPurchase purch = this.getWorkingFuelPurchase();

        purch.setFuelAmount(fuelAmount);
        purch.setFuelUnit(units);
        purch.setStateCode(stateCode);
        purch.setTractorNumber(tractorNumber);

        this.setWorkingFuelPurchase(purch);
    }

    /// <summary>
    /// Add the fuel classification type, and the date, to the pending purchase.
    /// </summary>
    /// <param name="classification"></param>
    /// <param name="dateOfPurchase"></param>
    public void AddFuelTypeToPurchase(FuelClassificationEnum classification, Date dateOfPurchase)
    {
        FuelPurchase purch = this.getWorkingFuelPurchase();

        purch.setFuelClassification(classification);
        purch.setPurchaseDate(dateOfPurchase);

        this.setWorkingFuelPurchase(purch);
    }

    /// <summary>
    /// Add the fuel receipt information.  This is only needed if the classification
    /// of the purchase is 'receipted'.
    /// </summary>
    /// <param name="purchaseAmount"></param>
    /// <param name="vendor"></param>
    /// <param name="invoiceNumber"></param>
    public void AddFuelReceipt(float purchaseAmount, String vendor, String invoiceNumber)
    {
        FuelPurchase purch = this.getWorkingFuelPurchase();

        if (purchaseAmount > 0.0F) purch.setFuelCost(purchaseAmount);
        if (vendor != null && !vendor.equals("")) 
        	purch.setVendorName(vendor);
        if (invoiceNumber != null && !invoiceNumber.equals(""))
        	purch.setInvoiceNumber(invoiceNumber);

        this.setWorkingFuelPurchase(purch);

        this.SaveFuelPurchase();
    }

    public void CancelFuelPurchase()
    {
        this.setWorkingFuelPurchase(null);
    }

    public boolean SubmitFuelPurchasesToDMO()
	{
        boolean isSuccesful = false;
        if (this.getIsNetworkAvailable())
        {
            try
            {
                // first fetch all unsubmitted route positions
                FuelPurchaseFacade facade = new FuelPurchaseFacade(this.getContext(), this.getCurrentUser());
                List<FuelPurchase> unSubmittedItems = facade.FetchAllUnsubmitted();

                // are there any to send?
                if (unSubmittedItems != null && unSubmittedItems.size() > 0)
                {
                    // second, attempt to send the entire list to DMO
                    FuelPurchaseList listToSend = new FuelPurchaseList();
                    listToSend.setFuelPurchases(unSubmittedItems.toArray(new FuelPurchase[unSubmittedItems.size()]));
                 
                    
                    	RESTWebServiceHelper rswh = new RESTWebServiceHelper(getContext());
                    	rswh.SubmitFuelPurchases(listToSend.getFuelPurchases());
                    
                    // third, mark all as submitted successfully
                    facade.MarkAsSubmitted(unSubmittedItems);
                }

                isSuccesful = true;
            }
    		catch (IOException ioe) {
    			this.HandleException(ioe);			
    		}
        }

        return isSuccesful;
	}

    public void SaveFuelPurchase()
    {
        FuelPurchaseFacade facade = new FuelPurchaseFacade(this.getContext(), this.getCurrentUser());
        facade.Save(getWorkingFuelPurchase());
    }

}
