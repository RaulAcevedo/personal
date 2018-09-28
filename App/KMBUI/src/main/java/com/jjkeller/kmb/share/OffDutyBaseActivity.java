package com.jjkeller.kmb.share;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.jjkeller.kmb.fragments.ShowOffDutyFrag;
import com.jjkeller.kmb.interfaces.IOffDuty;
import com.jjkeller.kmbapi.calcengine.OffDuty;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.HosAuditController;
import com.jjkeller.kmbui.R;

/**
 * Created by t000155 on 12/17/2015.
 */
public abstract class OffDutyBaseActivity extends BaseActivity
        implements IOffDuty.ShowOffDutyFragActions{


    private Handler _timerHandler;
// This variable is used to supress the off duty counter because on RptDailyHours it should be shown only for check hours mode

    private boolean _Supress;
    private ShowOffDutyFrag _offDutyFrag;
    public ShowOffDutyFrag getOffDutyFragment()
    {
        return _offDutyFrag;
    }
    public void SupressOffDutyCounter(boolean hide){_Supress=hide;}
    public void setOffDutyFragment(ShowOffDutyFrag frag)
    {
        _offDutyFrag = frag;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (_timerHandler != null)
            _timerHandler.removeCallbacks(_heatbeatTimerTask);
    }

    @Override
    protected void onResume()
    {
        LoadOffDutyFragment();
        super.onResume();
    }

    public void setFragments(){

        View offDutyLayout = findViewById(R.id.offduty_fragment);
        if (offDutyLayout != null)
        {
            Fragment ln = getSupportFragmentManager().findFragmentById(R.id.offduty_fragment);
            if(ln != null)
                this.setOffDutyFragment((ShowOffDutyFrag) ln);
        }
    }

    protected void loadControls(Bundle savedInstanceState){
        super.loadControls(savedInstanceState);
        LoadOffDutyFragment();
        HandleOffDutyMessage();
    }

    protected void loadControls(){
        super.loadControls();
        LoadOffDutyFragment();
        HandleOffDutyMessage();
    }

    public boolean getOffDutyMsgCloseBtnPressed(){
        return GlobalState.getInstance().getOffDutyMsgCloseBtnPressed();
    }

    public void setOffDutyMsgCloseBtnPressed(boolean status){
        GlobalState.getInstance().setOffDutyMsgCloseBtnPressed(status);
    }

    public boolean IsOffDuty (){

        if (getController()==null)
                return false;
        HosAuditController c = new HosAuditController(getController().getContext());
        OffDuty od = c.getOffDutyInfo();
        if (od!=null)
            return od.getIsOffDuty();
        else
            return false;

    }

    public void CloseOffDutyMessage(){

        setOffDutyMsgCloseBtnPressed(true);
        HideOffDutyFragment();

    }

    public void HideOffDutyFragment(){
        FrameLayout layout = (FrameLayout)findViewById(R.id.offduty_fragment);
        if (layout!=null)
            layout.setVisibility(View.GONE);
    }

    private void ShowOffDutyFragment(){
        FrameLayout layout = (FrameLayout)findViewById(R.id.offduty_fragment);
        if (layout!=null)
            layout.setVisibility(View.VISIBLE);
    }

    private void ManageHeartbeatTimer()
    {
        if(_timerHandler == null && IsOffDuty())
        {
            _timerHandler = new Handler();
            _timerHandler.removeCallbacks(_heatbeatTimerTask);
            _timerHandler.postDelayed(_heatbeatTimerTask, 500);
        }
        else if(_timerHandler != null && !IsOffDuty())  {
            _timerHandler.removeCallbacks(_heatbeatTimerTask);
            _timerHandler = null;
        }
    }

    private Runnable _heatbeatTimerTask = new Runnable()
    {
        public void run()
        {
            HandleOffDutyMessage();
            if (_timerHandler != null) {
                _timerHandler.removeCallbacks(_heatbeatTimerTask);
                _timerHandler.postDelayed(_heatbeatTimerTask, 60000);
            }
        }
    };

    private void SetOffDutyFragment() {
        _offDutyFrag.SetOffDutyFragment();
    }

    protected void LoadOffDutyFragment(){

        View offDutyLayout = findViewById(R.id.offduty_fragment);
        if(offDutyLayout != null) {

            _offDutyFrag = new ShowOffDutyFrag();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.offduty_fragment, _offDutyFrag);

            try {
                transaction.commit();

            }
            catch (Exception e) {
                Log.e("UnhandledCatch", e.getMessage() + ": " + Log.getStackTraceString(e));
            }
        }
    }

    private void HandleOffDutyMessage(){
        if (IsOffDuty() && !getOffDutyMsgCloseBtnPressed() && !_Supress) {
            SetOffDutyFragment();
            ShowOffDutyFragment();
        }
        else {
            HideOffDutyFragment();
        }
        ManageHeartbeatTimer();
    }

    public void updateOffDutyMessage() {
        _offDutyFrag.SetOffDutyFragment();
    }

    public void handlebtnCloseOffDutyClick(){
        CloseOffDutyMessage();
    }
}
