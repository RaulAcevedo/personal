package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.jjkeller.kmb.interfaces.IEobrConfig.EobrConfigFragActions;
import com.jjkeller.kmb.interfaces.IEobrConfig.EobrConfigFragControllerMethods;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.enums.DatabusTypeEnum;
import com.jjkeller.kmbui.R;

import java.util.ArrayList;
import java.util.Arrays;

public class EobrConfigFrag extends BaseFragment
{
	private EobrConfigFragControllerMethods _controlListener;
	private EobrConfigFragActions _actionsListener;
	
	private Spinner _cboDatabusType;
	private EditText _tvCurrentTractorNumber;
	
	private DatabusTypeEnum _currentBusType;
	private LinearLayout _layoutBusType;
	private boolean _isEobrGenI;
	private String _currentTractorNumber;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.f_eobrconfig, container, false);
		findControls(v);
		return v;
	}

	protected void findControls(View v)
	{
		_tvCurrentTractorNumber = (EditText)v.findViewById(R.id.txtTractorNumber);
		_cboDatabusType = (Spinner)v.findViewById(R.id.eobrcnfg_spnenginedatabus);
		_layoutBusType = (LinearLayout)v.findViewById(R.id.layoutBusType);

		v.findViewById(R.id.btnSave).setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				_actionsListener.handleSaveButtonClick();
			}
		});

		v.findViewById(R.id.btnCancel).setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				_actionsListener.handleCancelButtonClick();
			}
		});
		if (!GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
			TextView deviceConfigLabel = (TextView) v.findViewById(R.id.textView1);
			deviceConfigLabel.setText(getString(R.string.eobrconfigurationlabel));
		}
	}
	
	protected void loadControls(Bundle savedInstanceState) {

		_currentBusType = _controlListener.getMyController().getCurrentBusType();
		_isEobrGenI = _controlListener.getMyController().getIsEobrGenI();
		_currentTractorNumber = _controlListener.getMyController().getCurrentTractorNumber();

		// 2/7/12 JHM - Setting field if Tractor Number doesn't match Serial Number
		if (_currentTractorNumber != null && !_currentTractorNumber.equalsIgnoreCase(_controlListener.getMyController().getSerialNumber()))
			this._tvCurrentTractorNumber.setText(_currentTractorNumber);

		if(_controlListener.getMyController().getIsGeotabDevice())
		{
			_layoutBusType.setVisibility(View.INVISIBLE);
		}
		else {
			// Note: get unsupported operation exception when attempting to remove an item from the
			// adapter that was generated from arraylist in resource file using createFromResource.
			// Cannot remove items from an array - need to use an array list, hence the change from createFromResource
			String[] databusTypes = this.getResources().getStringArray(R.array.databustype_array);
			ArrayAdapter<CharSequence> dataBusAdapter = new ArrayAdapter<CharSequence>(getActivity(), R.layout.kmb_spinner_item, new ArrayList<CharSequence>(Arrays.asList(databusTypes)));
			dataBusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

			// loop through all items in the adapter and remove either gen I items
			// or gen II items depending on the generation of the eobr connected to
			for (int i = dataBusAdapter.getCount() - 1; i >= 0; i--) {
				// if eobr is gen I and databustype is anything other than Auto Detect, J1708 or J1939, remove from adapter
				if (_isEobrGenI && DatabusTypeEnum.valueOfDMOEnum(dataBusAdapter.getItem(i).toString()).getValue() != DatabusTypeEnum.GPSONLY
						&& DatabusTypeEnum.valueOfDMOEnum(dataBusAdapter.getItem(i).toString()).getValue() != DatabusTypeEnum.J1708
						&& DatabusTypeEnum.valueOfDMOEnum(dataBusAdapter.getItem(i).toString()).getValue() != DatabusTypeEnum.J1939) {
					dataBusAdapter.remove(dataBusAdapter.getItem(i));
				}
				// if eobr is gen II only need to remove Auto Detect
				else if (!_isEobrGenI && DatabusTypeEnum.valueOfDMOEnum(dataBusAdapter.getItem(i).toString()).getValue() == DatabusTypeEnum.GPSONLY) {
					dataBusAdapter.remove(dataBusAdapter.getItem(i));
				}
			}

			this.getDatabusType().setAdapter(dataBusAdapter);

			// Set the selection to the current bus type
			if (_currentBusType != null) {
				for (int i = 0; i < dataBusAdapter.getCount(); i++) {
					DatabusTypeEnum itemType = DatabusTypeEnum.valueOfDMOEnum(dataBusAdapter.getItem(i).toString());
					if (itemType.getValue() == _currentBusType.getValue()) {
						this.getDatabusType().setSelection(i);
						break;
					}
				}
			}
		}
	}

	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		try
		{
			_actionsListener = (EobrConfigFragActions)activity;
		}
		catch (ClassCastException e)
		{
			throw new ClassCastException(activity.toString() + " must implement EobrConfigFragActions");
		}
		
		try{
        	_controlListener = (EobrConfigFragControllerMethods) activity;
        } catch (ClassCastException e) {
        	throw new ClassCastException(activity.toString() + " must implement EobrConfigFragControllerMethods");
        }
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		this.loadControls(savedInstanceState);
	}

	public Spinner getDatabusType()
	{
		return _cboDatabusType;
	}

	public EditText getCurrentTractorNumber()
	{
		return _tvCurrentTractorNumber;
	}
	
	public String getInitialTractorNumberValue()
	{
		return _currentTractorNumber;
	}
}
