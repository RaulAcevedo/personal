package com.jjkeller.kmb;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.jjkeller.kmb.fragments.LeftNavFrag;
import com.jjkeller.kmb.fragments.SystemMenuFrag;
import com.jjkeller.kmb.interfaces.ISystemMenu.SystemMenuFragControllerMethods;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.LogEntryController;
import com.jjkeller.kmbapi.enums.InspectionTypeEnum;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbui.R;

import static com.jjkeller.kmbapi.kmbeobr.Constants.FROM_MENU_EXTRA;

public class SystemMenu extends BaseActivity 
							implements SystemMenuFragControllerMethods, LeftNavFrag.OnNavItemSelectedListener,
							LeftNavFrag.ActivityMenuItemsListener {
    
	SystemMenuFrag _contentFrag;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);

		setContentView(R.layout.baselayout);
		
		mFetchLocalDataTask = new FetchLocalDataTask(this.getClass().getSimpleName());
		mFetchLocalDataTask.execute();
    }
	
    @Override
	protected void loadControls() {
    	super.loadControls();
		loadContentFragment(new SystemMenuFrag());		
	}
    
	protected void InitController()
	{		
		LogEntryController logEntryCtrl = new LogEntryController(this);
		
		this.setController(logEntryCtrl);	
	}
    
	public LogEntryController getMyController()
	{
		return (LogEntryController)this.getController();
	}

	@Override 
	protected void onActivityResult(int requestCode, int resultCode, Intent data) { 
	super.onActivityResult(requestCode, resultCode, data); 
		if (requestCode == EXIT_ACTIVITY) { 
            this.finish();
		} 

	} 
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.clear();
		this.CreateOptionsMenu(menu, false);
		return true;
	}
	
	@Override
	protected void onResume() {
    	super.onResume();
    	
    	// BSA 10/30/2014 - Invalidate the menu so it is rebuilt based on any recent data profile changes via Download
	    supportInvalidateOptionsMenu();
	    
	    // BSA 10/30/2014 - Reload the "Records" content menu based on any recent data profile changes via Download
	    if (_contentFrag != null)
	    {
	    	Bundle bundle = getIntent().getExtras();
	    	int displayMenuId = bundle.getInt(getString(R.string.menu));
	    	if (displayMenuId == R.string.mnu_sysmenu_records)
	    	{
	    		_contentFrag.Reload();	
	    	}
	    }
	}

	public String getActivityMenuItemList()
	{
		return getString(R.string.btndone);
	}

	private void handleMenuItemSelected(int itemPosition)
	{
		if (itemPosition == 0)
		{
			this.finish();
			this.startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
		}
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

	public void onNavItemSelected(int menuItem)
	{
		handleMenuItemSelected(menuItem);
	}
	
	@Override
	public void setFragments()
	{
		super.setFragments();

		Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
		_contentFrag = (SystemMenuFrag)f;
	}
		
	public boolean runMenuAction(int menuItemId, boolean isVehicleInMotion)
	{
		Bundle extras;

		EmployeeLog currentlog = GlobalState.getInstance().getCurrentEmployeeLog();
		boolean isCurrentLogExemptFromELDUse = currentlog != null ? currentlog.getIsExemptFromELDUse() : false;

		// 2/20/2013 JEH: Used Ctrl+1 to convert to if/else because I changed KMBUI to a Library and it required this change.
		if (menuItemId == R.string.mnu_sysmenu_records) {
			extras = new Bundle();
			extras.putString(this.getString(R.string.title), this.getString(R.string.records));
			extras.putInt(this.getString(R.string.menu), menuItemId);
			this.startActivity(SystemMenu.class, extras);
			return true;
		} else if (menuItemId == R.string.mnu_sysmenu_reports) {
			extras = new Bundle();
			extras.putString(this.getString(R.string.title), this.getString(R.string.reports));
			extras.putInt(this.getString(R.string.menu), menuItemId);
			this.startActivity(SystemMenu.class, extras);
			return true;
		} else if (menuItemId == R.string.mnu_sysmenu_teamdriver) {
			extras = new Bundle();
			extras.putString(this.getString(R.string.title), this.getString(R.string.teamdrivers));
			extras.putInt(this.getString(R.string.menu), menuItemId);
			this.startActivity(SystemMenu.class, extras);
			return true;
		} else if (menuItemId == R.string.mnu_sysmenu_teamdrivershare) {
			extras = new Bundle();
			extras.putString(this.getString(R.string.title), this.getString(R.string.teamdriversshare));
			extras.putInt(this.getString(R.string.menu), menuItemId);
			this.startActivity(SystemMenu.class, extras);
			return true;
		} else if (menuItemId == R.string.mnu_sysmenu_vehicleinspection) {
			extras = new Bundle();
			extras.putString(this.getString(R.string.title), this.getString(R.string.vehicleinspection));
			extras.putInt(this.getString(R.string.menu), menuItemId);
			this.startActivity(SystemMenu.class, extras);
			return true;
		} else if (menuItemId == R.string.mnu_sysmenu_diag) {
			extras = new Bundle();
			extras.putString(this.getString(R.string.title), this.getString(R.string.diagnostics));
			extras.putInt(this.getString(R.string.menu), menuItemId);
			this.startActivity(SystemMenu.class, extras);
			return true;
		} else if (menuItemId == R.string.mnu_sysmenu_file) {
			extras = new Bundle();
			extras.putString(this.getString(R.string.title), this.getString(R.string.file));
			extras.putInt(this.getString(R.string.menu), menuItemId);
			this.startActivity(SystemMenu.class, extras);
			return true;
		} else if (menuItemId == R.string.mnu_recordmenu_tripinfo) {
			this.startActivity(TripInfo.class);
			return true;
		} else if (menuItemId == R.string.mnu_recordmenu_emprules) {
			this.startActivity(EmployeeRules.class);
			return true;
		} else if (menuItemId == R.string.mnu_recordmenu_download) {
			this.startActivity(DownloadLogs.class);
			return true;
		} else if (menuItemId == R.string.lblsubmitlogstitle && GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled() && GlobalState.getInstance().getCurrentUser() != null && (GlobalState.getInstance().getCurrentUser().getExemptFromEldUse() || isCurrentLogExemptFromELDUse)) {
			SubmitLogsWhenExemptForEldUse(false);
			return true;
		} else if (menuItemId == R.string.mnu_recordmenu_certifysubmit) {
			if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()){
				extras = new Bundle();
				extras.putBoolean(this.getString(R.string.logout), true);
				this.startActivity(CertifyLogs.class, extras);
			}
			else{
				this.startActivity(SubmitLogs.class);
			}
			return true;
		} else if (menuItemId == R.string.mnu_recordmenu_unassigneddriving) {
			Bundle bundle = new Bundle();
			bundle.putBoolean(FROM_MENU_EXTRA, true);
			this.startActivity(UnassignedDrivingPeriods.class, bundle);
			return true;
		} else if (menuItemId == R.string.mnu_recordmenu_unidentifiedeldevents) {
			extras = new Bundle();
			extras.putBoolean(getString(R.string.parm_unidentifiedeldeventsshowallunsubmitted), true );
			this.startActivity(UnidentifiedELDEvents.class, extras);
			return true;
		} else if (menuItemId == R.string.mnu_recordmenu_editlocations) {
			this.startActivity(EditLogLocations.class);
			return true;
		} else if (menuItemId == R.string.mnu_recordmenu_editfuelpurchases) {
			this.startActivity(EditFuelPurchaseList.class);
			return true;
		} else if (menuItemId == R.string.mnu_reportsmenu_dutystatus) {
			startActivity(RptGridImage.class);
			return true;
		} else if (menuItemId == R.string.mnu_reportsmenu_eldevents) {
			startActivity(RptEldEvents.class);
			return true;
		} else if (menuItemId == R.string.mnu_reportsmenu_availhours) {
			startActivity(RptAvailHours.class);
			return true;
		} else if (menuItemId == R.string.mnu_reportsmenu_dailyhours) {
			startActivity(RptDailyHours.class);
			return true;
		} else if (menuItemId == R.string.mnu_reportsmenu_failurereport) {
			startActivity(RptDutyFailures.class);
			return true;
		} else if (menuItemId == R.string.mnu_reportsmenu_locationcodes) {
			startActivity(RptLocationCodes.class);
			return true;
		} else if (menuItemId == R.string.mnu_reportsmenu_datausage) {
			startActivity(RptDataUsage.class);
			return true;
		} else if (menuItemId == R.string.mnu_reportsmenu_dotauthority) {
			startActivity(RptDOTAuthority.class);
			return true;
		}else if (menuItemId == R.string.mnu_reportsmenu_malfunctionanddatadiagnostic) {
			startActivity(RptMalfunctionAndDataDiagnostic.class);
			return true;
		} else if (menuItemId == R.string.mnu_teamdrivermenu_start) {
			startActivity(TeamDriverAddDriver.class);
			return true;
		} else if (menuItemId == R.string.mnu_teamdrivermenu_end) {
			startActivity(TeamDriverEndDriver.class);
			return true;
		} else if (menuItemId == R.string.mnu_teamdriversharemenu_login) {
			this.startActivity(Login.class);
			return true;
		} else if (menuItemId == R.string.mnu_teamdriversharemenu_logout) {
			this.startActivity(Logout.class);
			return true;
		} else if (menuItemId == R.string.mnu_teamdriversharemenu_switch) {
			this.startActivity(SwitchUser.class);
			return true;
		} else if (menuItemId == R.string.mnu_dvirmenu_new) {
			extras = new Bundle();
			extras.putInt(getString(R.string.parm_vehicleinspectiontype), InspectionTypeEnum.POSTTRIP );
			extras.putBoolean(getString(R.string.parm_vehicleinspectionpoweredunit), true );
			this.startActivity(VehicleInspectionCreate.class, extras);
			return true;
		} else if (menuItemId == R.string.mnu_dvirmenu_newpretrip) {
			extras = new Bundle();
			extras.putInt(getString(R.string.parm_vehicleinspectiontype), InspectionTypeEnum.PRETRIP );
			extras.putBoolean(getString(R.string.parm_vehicleinspectionpoweredunit), true );
			startActivity(VehicleInspectionCreate.class, extras);
			return true;
		} else if (menuItemId == R.string.mnu_dvirmenu_newtrailer) {
			extras = new Bundle();
			extras.putInt(getString(R.string.parm_vehicleinspectiontype), InspectionTypeEnum.POSTTRIP );
			extras.putBoolean(getString(R.string.parm_vehicleinspectionpoweredunit), false );
			startActivity(VehicleInspectionCreate.class, extras);
			return true;
		} else if (menuItemId == R.string.mnu_dvirmenu_review) {
			this.startActivity(VehicleInspectionReview.class);
			return true;
		} else if (menuItemId == R.string.mnu_diagmenu_appsettings) {
			this.startActivity(RptTroubleApplication.class);
			return true;
		} else if (menuItemId == R.string.mnu_diagmenu_eldconfig || menuItemId == R.string.mnu_diagmenu_eobrconfig) {
			this.startActivity(RptTroubleEOBRDevice.class);
			return true;
		} else if (menuItemId == R.string.mnu_diagmenu_elddata || menuItemId == R.string.mnu_diagmenu_eobrdata) {
			this.startActivity(RptTroubleEobrRecord.class);
			return true;
		} else if (menuItemId == R.string.mnu_diagmenu_elddiscovery || menuItemId == R.string.mnu_diagmenu_eobrdiscovery) {
			if(GlobalState.getInstance().getCompanyConfigSettings(this).getIsGeotabEnabled())
				startActivity(DeviceDiscoveryGeoTab.class);
			else
				startActivity(DeviceDiscovery.class);
			return true;
		} else if (menuItemId == R.string.mnu_diagmenu_seteldconfig || menuItemId == R.string.mnu_diagmenu_seteobrconfig) {
			this.startActivity(EobrConfig.class);
			return true;
		} else if (menuItemId == R.string.mnu_diagmenu_eldselftest || menuItemId == R.string.mnu_diagmenu_eobrselftest) {
			this.startActivity(EobrSelfTest.class);
			return true;
		} else if (menuItemId == R.string.mnu_diagmenu_geotabconfig) {
			if(GlobalState.getInstance().getCompanyConfigSettings(this).getIsGeotabEnabled())
				this.startActivity(GeotabConfig.class);
				return true;
		} else if (menuItemId == R.string.mnu_diagmenu_uploaddiag) {
			this.startActivity(UploadDiagnostics.class);
			return true;
		} else if (menuItemId == R.string.mnu_diagmenu_odometercalibration) {
			this.startActivity(OdometerCalibration.class);
			return true;
		} else if (menuItemId == R.string.mnu_filemenu_changepassword) {
			this.startActivity(ChangePassword.class);
			return true;
		} else if (menuItemId == R.string.mnu_filemenu_roadsideinspection || menuItemId == R.string.mnu_filemenu_roadsideinspectionenabled) {
			this.startActivity(RoadsideInspection.class);
			return true;
		} else if (menuItemId == R.string.mnu_filemenu_checkforupdates) {
			GlobalState.getInstance().setIsAutoUpdate(false);
			this.startActivity(Updater.class);
			return true;
		} else if (menuItemId == R.string.mnu_filemenu_admin) {
			if(GlobalState.getInstance().getFeatureService().getShowDebugFunctions())
				startActivity(Admin.class);
			else
				startActivity(DailyPassword.class);
			return true;
		}else if(menuItemId == R.string.mnu_filemenu_requestlogs){
			this.startActivity(RequestLogs.class);
			return true;
		}else if (menuItemId == R.string.mnu_filemenu_exit) {
			startActivity(Logout.class);
			return true;
		}else if (menuItemId == R.string.mnu_filemenu_legal) {
			startActivity(Eula.class);
            return true;
        }
        return true;
	}
	
}
