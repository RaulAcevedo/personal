package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.jjkeller.kmb.interfaces.IEditFuelPurchaseList.EditFuelPurchaseListFragActions;
import com.jjkeller.kmb.interfaces.IEditFuelPurchaseList.EditFuelPurchaseListFragControllerMethods;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbapi.controller.FuelPurchaseController;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.proxydata.FuelPurchase;
import com.jjkeller.kmbui.R;

import java.text.DecimalFormat;
import java.util.List;

public class EditFuelPurchaseListFrag extends BaseFragment{
	EditFuelPurchaseListFragControllerMethods controlListener;
	EditFuelPurchaseListFragActions actionsListener;
	
	private GridView _grid;
	private List<FuelPurchase> _fuelPurchaseList;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        // Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.f_editfuelpurchaselist, container, false);
		findControls(v);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		this.loadControls();
	}
	
	private void loadControls(){
		_fuelPurchaseList = controlListener.getMyController().UnsubmittedFuelPurchaseList();
		_grid.setAdapter(new FuelPurchaseAdapter(getActivity(), R.layout.grdeditfuelpurchases, _fuelPurchaseList.toArray(new FuelPurchase[_fuelPurchaseList.size()]), controlListener.getMyController()));
	}
	
	protected void findControls(View v){
		_grid = (GridView)v.findViewById(R.id.grdEditFuelPurchases);
	}
	
	@Override
    public void onAttach(Activity activity) {
		super.onAttach(activity);
        try {
        	actionsListener = (EditFuelPurchaseListFragActions) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement EditFuelPurchaseListFragActions");
        }
        
        try{
        	controlListener = (EditFuelPurchaseListFragControllerMethods) activity;
        } catch (ClassCastException e) {
        	throw new ClassCastException(activity.toString() + " must implement EditFuelPurchaseListFragControllerMethods");
        }
    }
	
	private class FuelPurchaseAdapter extends ArrayAdapter<FuelPurchase> {

        private FuelPurchase[] items;
        private Context _ctx;
        
        public FuelPurchaseAdapter(Context context, int textViewResourceId, FuelPurchase[] items, FuelPurchaseController fuelPurchaseController) {
                super(context, textViewResourceId, items);
                this.items = items;
                this._ctx = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
                View v = convertView;
                if (v == null) {
                    LayoutInflater li = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    v = li.inflate(R.layout.grdeditfuelpurchases, null);
                }
                final FuelPurchase fuelPurchase = items[position];
                
                if (fuelPurchase != null) {
					
                	TextView tvDate = (TextView)v.findViewById(R.id.efptvDate);
                	tvDate.setText(DateUtility.getDateFormat().format(fuelPurchase.getPurchaseDate()));

                	TextView tvTime = (TextView)v.findViewById(R.id.efptvTime);
                	tvTime.setText(DateUtility.getHomeTerminalTime12HourFormat().format(fuelPurchase.getPurchaseDate()));
                	
                	TextView tvState = (TextView)v.findViewById(R.id.efptvState);
					tvState.setText(fuelPurchase.getStateCode());
                	
                	TextView tvFuel = (TextView)v.findViewById(R.id.efptvFuel);
                	StringBuilder sb = new StringBuilder(String.valueOf(new DecimalFormat("###").format(fuelPurchase.getFuelAmount())));
                	sb.append(fuelPurchase.getFuelClassification().getStringAbbr(this._ctx));
                	sb.append(fuelPurchase.getFuelUnit().getStringAbbr(this._ctx));
                	tvFuel.setText(sb.toString());
                	
                	Button btn = (Button)v.findViewById(R.id.btnEditFuelPurchase);
                	
					btn.setOnClickListener (
							new OnClickListener() {
					        public void onClick(View v) {
					        	actionsListener.handleEditButtonClick(fuelPurchase);
					        }
					    }); 
					
                }
                return v;
        }
	}

}
