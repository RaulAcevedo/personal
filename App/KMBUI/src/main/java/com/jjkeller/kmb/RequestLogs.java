package com.jjkeller.kmb;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.jjkeller.kmb.fragments.LeftNavFrag;
import com.jjkeller.kmb.fragments.RequestLogsFrag;
import com.jjkeller.kmb.interfaces.IRequestLogs.RequestLogsFragActions;
import com.jjkeller.kmb.interfaces.IRequestLogs.RequestLogsFragControllerMethods;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmbapi.common.TimeKeeper;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.abstracts.APIControllerBase;
import com.jjkeller.kmbapi.controller.interfaces.IAPIController;
import com.jjkeller.kmbapi.controller.share.MandateObjectFactory;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.controller.utility.ErrorLogHelper;
import com.jjkeller.kmbui.R;

import java.util.Date;

/**
 * Created by t000253 on 2/1/2016.
 */
public class RequestLogs extends BaseActivity
                         implements RequestLogsFragControllerMethods, RequestLogsFragActions, LeftNavFrag.OnNavItemSelectedListener,
                                    LeftNavFrag.ActivityMenuItemsListener{

    RequestLogsFrag _contentFrag;
private String _validationMsg = "";
private String _emailAddress = "";

private Date _startDate = DateUtility.AddDays(DateUtility.getCurrentDateTimeUTC(), -1);
private Date _endDate = DateUtility.getSixMonthDateTimeUTC();

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.baselayout);

        mFetchLocalDataTask = new FetchLocalDataTask(this.getClass().getSimpleName());
        mFetchLocalDataTask.execute();

        if (savedInstanceState == null) {
            _contentFrag = new RequestLogsFrag();
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.content_fragment, _contentFrag, "Content")
                    .commit();
        } else {
            _contentFrag = (RequestLogsFrag) getSupportFragmentManager()
                    .findFragmentByTag("Content");

            if(savedInstanceState.containsKey("startTime"))
                _contentFrag.SetStartDate(new Date(savedInstanceState.getLong("startTime")));

            if(savedInstanceState.containsKey("endTime"))
                _contentFrag.SetEndDate(new Date(savedInstanceState.getLong("endTime")));
        }
    }

    @Override
    protected void loadControls() {
        super.loadControls();
        loadContentFragment(new RequestLogsFrag());
    }

    protected void InitController()
    {
        IAPIController empLogCtrl = MandateObjectFactory.getInstance(this, GlobalState.getInstance().getFeatureService()).getCurrentEventController();

        this.setController(empLogCtrl);
    }

    public IAPIController getMyController(){ return this.getController(APIControllerBase.class); }

    @Override
    public void setFragments()
    {
        super.setFragments();

        Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
        _contentFrag = (RequestLogsFrag)f;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        this.CreateOptionsMenu(menu, false);
        return true;
    }

    public void handleSubmitButtonClick()
    {
        if (((APIControllerBase)this.getMyController()).getIsNetworkAvailable()) {
            boolean isValid = this.Validate();
            _emailAddress = _contentFrag.getEmailAddressTextView().getText().toString();
            _startDate = _contentFrag.GetStartDate();
            _endDate = _contentFrag.GetEndDate();
            //If the data is valid then proceed and if not show the message indicating which field is an issue.
            if (isValid) {
                //Retain the email address for future use
                SharedPreferences _userPref = this.getSharedPreferences(this.getString(R.string.sharedpreferencefile), 0);
                SharedPreferences.Editor editor = _userPref.edit();
                boolean rememberMeChecked = _userPref.getBoolean(this.getString(R.string.remembermechecked), true);

                if(rememberMeChecked){
                    editor.putString("defaultemailaddress", _emailAddress);
                    editor.commit();
                }

                mRequestLogTask = new RequestLogsTask();
                mRequestLogTask.execute();
                ShowMessage(this, getString(R.string.msg_logrequestsubmitted_title), getString(R.string.msg_logrequestsubmitted_body), new ShowMessageClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        super.onClick(dialog, id);
                        handleMenuItemSelected(0);
                    }
                });
            }
            else if(_validationMsg.length()>0)
            {
                this.ShowMessage(this, _validationMsg);
            }
        }
        else {
            ShowMessage(this, getString(R.string.no_network_connection));
        }
    }

    public void handleRememberMe(boolean isChecked){
        if(isChecked){
            _emailAddress = _contentFrag.getEmailAddressTextView().getText().toString();
            //Retain the email address for future use
            SharedPreferences _userPref = this.getSharedPreferences(this.getString(R.string.sharedpreferencefile), 0);
            SharedPreferences.Editor editor = _userPref.edit();
            editor.putBoolean(this.getString(R.string.remembermechecked), true);
            editor.commit();
            editor.putString(this.getString(R.string.defaultemailaddress), _emailAddress);
            editor.commit();
        }else{
            //remove the email address from memory
            SharedPreferences _userPref = this.getSharedPreferences(this.getString(R.string.sharedpreferencefile), 0);
            SharedPreferences.Editor editor = _userPref.edit();
            String defaultEmailAddress = _userPref.getString(this.getString(R.string.defaultemailaddress), "");
            if(defaultEmailAddress.length()> 0){
                editor.putBoolean(this.getString(R.string.remembermechecked), false);
                editor.commit();
                editor.remove(this.getString(R.string.defaultemailaddress));
                editor.commit();
            }
        }
    }

    private void handleMenuItemSelected(int itemPosition)
    {
        if (itemPosition == 0)
        {
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

    public String getActivityMenuItemList()
    {
        return this.getString(R.string.btncertifycancel);
    }

    private boolean Validate() {
        boolean isValid = false;

        String emailAddress = _contentFrag.getEmailAddressTextView().getText().toString();
        Date startDate = _contentFrag.GetStartDate();
        Date endDate = _contentFrag.GetEndDate();
        _validationMsg = "";

        if(emailAddress.length() == 0){
            //No email address was entered
            _validationMsg = this.getResources().getString(R.string.msgenteremailaddress);
        } else if(!android.util.Patterns.EMAIL_ADDRESS.matcher(emailAddress).matches()){
            //Email address format is invalid
            _validationMsg = this.getResources().getString(R.string.msgemailaddressformat);
        } else if(startDate == null){
            //Somehow no date got entered
            _validationMsg = this.getResources().getString(R.string.msgnostartdatevalue);
        } else if(endDate == null){
            //Somehow no date was entered
            _validationMsg = this.getResources().getString(R.string.msgnoenddatevalue);
        } else if (startDate.after(endDate)){
            //start date needs to come before the end date
            _validationMsg = this.getResources().getString(R.string.msginvaliddatevalues);
        } else if (startDate.after(TimeKeeper.getInstance().now())) {
            //start date needs to come before today
            _validationMsg = this.getResources().getString(R.string.msginvalidstartdatevalue);
        }else{
            //all values are valid so continue.
            isValid = true;
        }

        return isValid;
    }

    private static RequestLogsTask mRequestLogTask;
    private class RequestLogsTask extends AsyncTask<Void, Void, Boolean> {
        protected void onPreExecute() {
            LockScreenRotation();
        }

        protected Boolean doInBackground(Void... params) {
            boolean isSuccessful = false;

            try {
                isSuccessful = getMyController().RequestLogsEmail(_emailAddress, _contentFrag.GetStartDate(), _contentFrag.GetEndDate());
            } catch (Throwable e) {
                ErrorLogHelper.RecordException(RequestLogs.this, e);
            }

            return isSuccessful;
        }

        protected void onPostExecute(Boolean isSuccessful) {
            UnlockScreenRotation();

            if (isSuccessful) {
                Return(isSuccessful);
            } else {
                showMsg(getString(R.string.msgerrorsoccured));
            }
        }
    }

}
