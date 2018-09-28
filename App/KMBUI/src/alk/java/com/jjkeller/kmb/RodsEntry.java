package com.jjkeller.kmb;

import android.content.Intent;

import com.actionbarsherlock.view.MenuItem;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.enums.DutyStatusEnum;
import com.jjkeller.kmbui.R;

public class RodsEntry extends RodsEntryBase {

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int itemID = item.getItemId();
        String itemText = item.getTitle().toString();

        //Handle border crossing navigation
        if(itemText.equalsIgnoreCase(getResources().getString(R.string.rodsentry_bordercrossing)))
        {
            itemID = 3;
            handleNavItem(itemID);
        }
        // Check for Team Driver - Switch situation.
        else if (itemText.equalsIgnoreCase(getResources().getString(R.string.mnuteamdriverswitch))
                || itemText.equalsIgnoreCase(getResources().getString(R.string.mnuadditionaluserswitch)))
        {
            itemID = 4;
            handleNavItem(itemID);
        }
        else
        {
            switch (itemID) {
                case 0:
                case 1:
                case 2:
                    // Handle the default options for RODs entry
                    handleNavItem(itemID);
                    break;
                default: {
                    if (itemText.compareToIgnoreCase(this.getString(R.string.mnu_AlkCopilot)) == 0) {
                        if (IsComplianceTabletAndAlkActivated()) {
                            // CoPilot is activated, so show the CoPilot screen
                            this.startActivity(AlkCopilot.class);
                        } else {
                            // CoPilot is not activated, so show a message
                            ShowMessage(this, getString(R.string.alk_copilot_not_activated_title), getString(R.string.alk_copilot_not_activated_message));
                        }
                    } else {
                        // Handle the default system menu options
                        return super.onOptionsItemSelected(item);
                    }
                }
            }
        }

        return true;
    }

    @Override
    protected void displayAlertIfNecessary(DutyStatusEnum dutyStatus)
    {
        if (!this.IsCurrentActivity(AlkCopilot.class))
        {
            String alertMessage = this.getMyController().getAlertMessage();

            if (alertMessage != null && alertMessage.length() > 0)
            {
                ShowAlertMessage(alertMessage, dutyStatus);
            }
        }
    }

    @Override
    protected Class<?> getDrivingViewClass()
    {
        if (this.IsComplianceTabletAndAlkActivated()) {
            return AlkCopilot.class;
        } else {
            return DOTClocks.class;
        }
    }

    @Override
    protected void ShowDrivingView()
    {
        if(GlobalState.getInstance().getIsInPersonalConveyanceDutyStatus() && getDrivingViewClass() != AlkCopilot.class)
        {
            // the user is in PC mode
            if(!this.IsCurrentActivity(RodsEntry.class))
            {
                // not currently viewing the Rods home screen, so navigate here first
                // this is done because the dialog is modal and needs to sit on top of RODS
                this.startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
            }
            this.ShowPersonalConveyanceDrivingView();
        }
        else
            this.ShowClocksDrivingView();
    }

    @Override
    protected boolean ShouldDismissDrivingView()
    {
        boolean result;

        if (this.getMyController().IsVehicleInMotion()) {
            result = this.IsCurrentActivity(RodsEditLocation.class);
        }
        else {
            result = _personalConveyanceDrivingView != null ||
                    getDrivingViewClass() != AlkCopilot.class && this.IsCurrentActivity(getDrivingViewClass());
        }
        return result;
    }
}

