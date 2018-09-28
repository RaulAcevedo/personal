/*
Copyright 2010 AndroidPlot.com. All rights reserved.

Redistribution and use in binary form, without modification, is
permitted provided that the following condition is met:


      Redistributions in binary form must reproduce the above copyright notice, this list
      of conditions and the following disclaimer in the documentation and/or other materials
      provided with the distribution.

THIS SOFTWARE IS PROVIDED BY ANDROIDPLOT.COM ``AS IS'' AND ANY EXPRESS OR IMPLIED
WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL ANDROIDPLOT.COM OR
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

The views and conclusions contained in the software and documentation are those of the
authors and should not be interpreted as representing official policies, either expressed
or implied, of AndroidPlot.com.
*/

package com.jjkeller.kmb;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.jjkeller.kmb.fragments.LeftNavFrag;
import com.jjkeller.kmb.fragments.RptGridImageFrag;
import com.jjkeller.kmb.share.GridLogData;
import com.jjkeller.kmb.share.OffDutyBaseActivity;
import com.jjkeller.kmb.share.ViewOnlyModeNavHandler;
import com.jjkeller.kmbapi.common.TimeKeeper;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.LogGridSummary;
import com.jjkeller.kmbapi.controller.abstracts.APIControllerBase;
import com.jjkeller.kmbapi.controller.interfaces.IAPIController;
import com.jjkeller.kmbapi.controller.share.MandateObjectFactory;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.enums.ExemptLogTypeEnum;
import com.jjkeller.kmbapi.enums.TimeZoneEnum;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbui.R;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

public class RptGridImage extends OffDutyBaseActivity implements AdapterView.OnItemSelectedListener, LeftNavFrag.OnNavItemSelectedListener, LeftNavFrag.ActivityMenuItemsListener
{
	private ViewOnlyModeNavHandler _viewOnlyHandler;
	
	private RptGridImageFrag _contentFragment;

	private boolean _dataLoaded = false;
	private int _datePicked;
		
	private List<Date> _empLogDateList;
	private Spinner _cboLogDate;	
	private Button _btnPreviousDay;
	private Button _btnNextDay;
	private GridLogData _gridLogData = new GridLogData(); 
	private int _myIndex;
    private TextView _txtLogDate;
	
	private TextView _exemptlbl;
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);


		_viewOnlyHandler = new ViewOnlyModeNavHandler(this);
		_viewOnlyHandler.setCurrentActivity(ViewOnlyModeNavHandler.ViewOnlyModeActivity.VIEWGRID);
		
		_datePicked = 0;

		setContentView(R.layout.rptgridimage);

		loadContentFragment(new RptGridImageFrag());

		if(_viewOnlyHandler.getIsViewOnlyMode())
			_myIndex = _viewOnlyHandler.getCurrentActivity().index();
		else
			_myIndex = 0;
		
		// Used for handling highlighting the selected item in the leftnav
		// If not using multiple fragments within an activity, we have to manually set the selected item
		this.setLeftNavSelectedItem(_myIndex);
		this.setLeftNavAllowChange(true);

		mFetchLocalDataTask = new FetchLocalDataTask(this.getClass().getSimpleName(), savedInstanceState);
		mFetchLocalDataTask.setAutoUnlockScreenRotationOnPostExecute(true);
		mFetchLocalDataTask.execute();

	}

	@Override
	public void onResume()
	{
		this.setLeftNavSelectedItem(_myIndex);
		loadLeftNavFragment();
		
		super.onResume();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (GlobalState.getInstance().getIsViewOnlyMode())
		{
			//disable the back button in view only mode
			if (keyCode == KeyEvent.KEYCODE_BACK)
			{
			    return true;
			}
		}
		
		return super.onKeyDown(keyCode, event);
	}
	
	protected IAPIController getMyController()
	{
		return (IAPIController) this.getController();
	}

	protected void InitController()
	{
		this.setController(MandateObjectFactory.getInstance(this, GlobalState.getInstance().getFeatureService()).getCurrentEventController());
	}

	@Override
	protected void loadData()
	{
		_empLogDateList = getMyController().GetLogDateListForReport();

		_dataLoaded = true;

	}

	private void loadGridData(Bundle savedInstanceState)
	{

		if (savedInstanceState != null)	{
			_gridLogData.loadDataFromSavedState(savedInstanceState);
		}
		else
		{
			boolean keepDate = false;

			if (getIntent().getExtras() != null) {
				// Check to see if the log date should not be changed
				keepDate = getIntent().getExtras().getBoolean(getString(R.string.state_keepdate), false);

				// reset the keepdate flag
				getIntent().getExtras().putBoolean(getString(R.string.state_keepdate), false);
			}

			if (!keepDate && !GlobalState.getInstance().isReviewEldEvent() )
			{
				try {
					getMyController().setSelectedLogForReport(GlobalState.getInstance().getCurrentEmployeeLog());
				} catch (Exception ex) {
					// There was no log for today
					Log.i("EmployeeLogs", "no log found for today's date: " + TimeKeeper.getInstance().now().toString());
				}
			}

			CreateDataForGrid();
		}
	}


	@Override
	protected void loadControls()
	{
		loadControls(null);
	}

	@Override
	protected void loadControls(Bundle savedInstanceState)
	{
		super.loadControls();


		String[] logDateArray = new String[_empLogDateList.size()];

		// put the log date in an array for the spinner control.
		for (int index = 0; index < _empLogDateList.size(); index++)
		{
			Date logDate = _empLogDateList.get(index);
			logDateArray[index] = DateUtility.getHomeTerminalShortDateFormat().format(logDate);
		}

		_cboLogDate = (Spinner)this.findViewById(R.id.cboLogDate);
		_btnPreviousDay = (Button)this.findViewById(R.id.btnPreviousDay);
		_btnNextDay = (Button)this.findViewById(R.id.btnNextDay);
		_exemptlbl = (TextView)this.findViewById(R.id.txtExemptLog);
        _txtLogDate = (TextView)this.findViewById(R.id.txtLogDate);
        _txtLogDate.setVisibility(View.GONE);
        if (GlobalState.getInstance().isReviewEldEvent()) {
            _btnNextDay.setVisibility(View.GONE);
            _btnPreviousDay.setVisibility(View.GONE);
            _cboLogDate.setVisibility(View.GONE);
            _txtLogDate.setVisibility(View.VISIBLE);
            _txtLogDate.setText(DateUtility.getHomeTerminalDateFormat().format(GlobalState.getInstance().getReviewEldEventDate()));
        }

        else {

            _btnPreviousDay.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    int index = _cboLogDate.getSelectedItemPosition();

                    if (index < _cboLogDate.getCount() - 1) {
                        _cboLogDate.setSelection(index + 1);
                        loadSelectedLog();
                    }
                }
            });

            _btnNextDay.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    int index = _cboLogDate.getSelectedItemPosition();

                    if (index > 0) {
                        _cboLogDate.setSelection(index - 1);
                        loadSelectedLog();
                    }
                }
            });
        }

		ArrayAdapter<String> logDateAdapter = new ArrayAdapter<String>(this, R.layout.kmb_spinner_item, logDateArray);
		logDateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		getLogDateSpinner().setAdapter(logDateAdapter);
		getLogDateSpinner().setOnItemSelectedListener(this);


		if (savedInstanceState != null)
		{
			getLogDateSpinner().setSelection(savedInstanceState.getInt(getResources().getString(R.string.state_logdate)));
			setExemptLabel();
		}else{
			EmployeeLog empLog = getMyController().getSelectedLogForReport();
			if(empLog != null){
				setLogDateSpinner(DateUtility.getHomeTerminalShortDateFormat().format(empLog.getLogDate()));
				setExemptLabel();
			}
		}


		if (_contentFragment != null)
		{
			FormatGrid();
		}
        loadSelectedLog();

        loadGridData(savedInstanceState);
	}
	
	private void setExemptLabel()
	{
		
		if (_exemptlbl != null)
		{
			boolean isVisible = getMyController().getSelectedLogForReport().getExemptLogType().getValue() != ExemptLogTypeEnum.NULL; 
			if (!isVisible)
				_exemptlbl.setVisibility(View.GONE);
			else
				_exemptlbl.setVisibility(View.VISIBLE);
		}

	}	
	public Spinner getLogDateSpinner()
	{
		return _cboLogDate;
	}
	
	public void setLogDateSpinner(String logDate)
	{
		for(int i = 0; i < _cboLogDate.getCount(); i++)
		{
			if (_cboLogDate.getItemAtPosition(i).toString().equals(logDate))
				_cboLogDate.setSelection(i);
		}
	}
	
	private void loadSelectedLog()
	{
		Date selectedDate = null;

        if (GlobalState.getInstance().isReviewEldEvent()){
            selectedDate = GlobalState.getInstance().getReviewEldEventDate();
        }
        else {
            try {
                selectedDate = DateUtility.getHomeTerminalShortDateFormat().parse(getLogDateSpinner().getSelectedItem().toString());
            } catch (ParseException e) {
                Log.e("UnhandledCatch", e.getMessage() + ": " + Log.getStackTraceString(e));
            }
        }

		_gridLogData.setLogDate(selectedDate);
			
		getMyController().setSelectedLogForReport(getMyController().EmployeeLogsForDutyStatusReport(selectedDate).get(0));

			
		if (_datePicked == 1)
		{
			// Create new fragment and transaction
			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
				
			// Replace whatever is in the fragment_container view with this fragment
			transaction.replace(R.id.content_fragment, new RptGridImageFrag(), "Content");
									
			// Commit the transaction
			transaction.commit();	

			CreateDataForGrid();
			
			FormatGrid();
			
			setExemptLabel();
		}
		else
		{
			_datePicked++;
		}		
	}
	

	public void onItemSelected(AdapterView<?> parent, View v, int position, long id)
	{
		loadSelectedLog();
	}

	public void onNothingSelected(AdapterView<?> parent)
	{
	}


	@Override
	public void setFragments()
	{
		super.setFragments();

		Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
		_contentFragment = (RptGridImageFrag) fragment;

		if (_contentFragment != null && _dataLoaded) {
			FormatGrid();
			//Hide grid legend if Eld Mandate Feature Toggle Off
			if(!GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled())
			{
				FrameLayout legend = (FrameLayout)findViewById(R.id.layoutgridlegend);
				legend.setVisibility(View.GONE);
			}
		}

	}

	private void CreateDataForGrid()
	{
		TimeZoneEnum currentUserHomeTerminalTimeZone = ((APIControllerBase)this.getMyController()).getCurrentUser().getHomeTerminalTimeZone();
		EmployeeLog empLog = getMyController().getSelectedLogForReport();
		Date currentHomeTerminalTimeNow = ((APIControllerBase)getMyController()).getCurrentClockHomeTerminalTime();

		_gridLogData.CreateDataForGrid(currentUserHomeTerminalTimeZone, empLog, currentHomeTerminalTimeNow, this);
	}

	private void FormatGrid()
	{
		if(_gridLogData != null && _gridLogData.getLogDate() != null) {
			_contentFragment.FormatGrid(_gridLogData);
			SetGridHours();
		}
	}
	
	private void SetGridHours()
	{
		EmployeeLog empLog = getMyController().getSelectedLogForReport();
		LogGridSummary summary = getMyController().GetLogGridSummary(empLog);
		_contentFragment.setGridHours(
				summary.getOffDutyMinutesTotal(),
				summary.getSleeperMinutesTotal(),
				summary.getOnDutyMinutesTotal(),
				summary.getDrivingMinutesTotal(),
				summary.getOffDutyWellsiteMinutesTotal(),
				empLog.getExemptLogType()
		);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		this.CreateOptionsMenu(menu, false);
        if (GlobalState.getInstance().isReviewEldEvent())
            return false;

		return true;
	}

	@Override
	public String getActivityMenuItemList()
	{
		return _viewOnlyHandler.getActivityMenuItemList(getString(R.string.rptdutystatus_actionitems_tablet));
	}

	private void handleMenuItemSelected(int itemPosition)
	{
		if (_viewOnlyHandler.getIsViewOnlyMode())
		{
			Intent intent = _viewOnlyHandler.handleMenuItemSelected(itemPosition);
			
			if(intent != null)
			{
				intent.putExtra(getResources().getString(R.string.state_keepdate), false);

				this.finish();
                this.startActivity(intent);
			}
		}
		else
		{
			switch (itemPosition)
			{
				case 0:
					//DO Nothing
				case 1:
					this.startActivity(RptDutyStatus.class);
                    finish();
					break;
				case 2:
					ClearRecentlyStartedActivityUri();
					this.startActivity(RptLogDetail.class);
                    finish();
					break;
				case 3:
					this.finish();
                    if (GlobalState.getInstance().isReviewEldEvent())
                        this.startActivity(EditLogRequest.class);
                    else
					    this.startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    GlobalState.getInstance().setIsReviewEldEvent(false);
					break;
			}
		}
	}

	@Override
	public void onNavItemSelected(int itemPosition)
	{
		handleMenuItemSelected(itemPosition);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		//See if home button was pressed
		this.GoHome(item, this.getController());
		
		handleMenuItemSelected(item.getItemId());
		super.onOptionsItemSelected(item);
		return true;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{					
		if (getLogDateSpinner() != null)
			outState.putInt(getResources().getString(R.string.state_logdate), (int)getLogDateSpinner().getSelectedItemId());

		_gridLogData.onSaveInstanceState(outState);

		super.onSaveInstanceState(outState);
	}

}
