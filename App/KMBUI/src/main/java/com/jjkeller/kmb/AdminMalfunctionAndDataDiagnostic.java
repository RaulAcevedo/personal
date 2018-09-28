package com.jjkeller.kmb;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.actionbarsherlock.view.Menu;
import com.jjkeller.kmb.fragments.AdminMalfunctionAndDataDiagnosticFrag;
import com.jjkeller.kmb.fragments.AdminMissingDataMalfunctionFrag;
import com.jjkeller.kmb.interfaces.IAdminMalfunctionAndDataDiagnostic;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmbapi.controller.AdminController;
import com.jjkeller.kmbapi.controller.share.KmbApplicationException;
import com.jjkeller.kmbapi.employeelogeldevents.Enums;
import com.jjkeller.kmbapi.enums.DataDiagnosticEnum;
import com.jjkeller.kmbapi.enums.Malfunction;
import com.jjkeller.kmbui.R;

import java.util.List;

public class AdminMalfunctionAndDataDiagnostic extends BaseActivity implements IAdminMalfunctionAndDataDiagnostic.AdminMalfunctionAndDataDiagnosticFragActions, IAdminMalfunctionAndDataDiagnostic.AdminMalfunctionAndDataDiagnosticMethods {

    private static final String TAG = "Admin";
    private AdminMalfunctionAndDataDiagnosticFrag debugEventsFrag;

    private int _currentFrag;

    protected static final int DEBUG_EVENTS = 0;
    protected static final int MISSING_DATA_MALFUNCTION = 1;

    private int _currentItemIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.baselayout);

        if (savedInstanceState != null) {
            _currentFrag = savedInstanceState.getInt("currentFrag");
            _currentItemIndex = savedInstanceState.getInt("currentItemIndex");
        }

        // Used for handling highlighting the selected item in the leftnav
        // We have to allow the leftnav to highlight the selected item
        this.setLeftNavSelectedItem(_currentItemIndex);
        this.setLeftNavAllowChange(true);

        if (_currentFrag <= 0) {
            _currentFrag = DEBUG_EVENTS;
            loadContentFragment(new AdminMalfunctionAndDataDiagnosticFrag());
        }

        new FetchLocalDataTask(this.getClass().getSimpleName()).execute();
    }

    @Override
    protected void InitController() {
        setController(new AdminController(this));
    }

    private AdminController getAdminController() {
        return getController(AdminController.class);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return CreateOptionsMenu(menu, false);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("currentFrag", _currentFrag);
        outState.putInt("currentItemIndex", _currentItemIndex);

        super.onSaveInstanceState(outState);
    }

    public String getActivityMenuItemList()
    {
        return getString(R.string.adminmalfunctionanddatadiagnostic_actionitems);
    }

    public void onNavItemSelected(int itemPosition)
    {
        switch (itemPosition) {
            case 0:
                loadContentFragment(new AdminMalfunctionAndDataDiagnosticFrag());
                _currentFrag = DEBUG_EVENTS;
                _currentItemIndex = itemPosition;
                break;
            case 1:
                loadContentFragment(new AdminMissingDataMalfunctionFrag());
                _currentFrag = MISSING_DATA_MALFUNCTION;
                _currentItemIndex = itemPosition;
                break;
            case 2:
                this.finish();
                this.startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
                break;
        }
    }

    @Override
    public void setFragments() {
        super.setFragments();

        if (_currentFrag == DEBUG_EVENTS)
        {
            Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
            debugEventsFrag = (AdminMalfunctionAndDataDiagnosticFrag) f;
        }
    }

    @Override
    public void onAddMalfunctionClick(Malfunction selectedMalfunction) {
        new AddTask().execute(selectedMalfunction);
    }

    @Override
    public void onAddDataDiagnosticClick(DataDiagnosticEnum selectedDataDiagnostic) {
        new AddTask().execute(selectedDataDiagnostic);
    }

    @Override
    public void onRemoveClick(Malfunction malfunction) {
        new RemoveTask().execute(malfunction);
    }

    @Override
    public void onRemoveClick(DataDiagnosticEnum dataDiagnostic) {
        new RemoveTask().execute(dataDiagnostic);
    }

    @Override
    public void onAddMalfunctionEventClick(final Enums.EmployeeLogEldEventType eventType) {

        final ProgressDialog progressDialog = ProgressDialog.show(this, "", getString(R.string.msgsaving));

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                getAdminController().AddMissingDataDiagnostic(eventType);

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                AdminMalfunctionAndDataDiagnostic.this.showMsg(getString(R.string.msgsuccessfullycreatedevent));
            }
        }.execute();
    }

    @Override
    public List<Malfunction> getActiveMalfunctions() {
        return getAdminController().GetDebugMalfunctions();
    }

    @Override
    public List<DataDiagnosticEnum> getActiveDataDiagnostics() {
        return getAdminController().GetDebugDataDiagnostics();
    }

    private abstract class SaveTask extends AsyncTask<Object, Void, Void> {
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = CreateSaveDialog();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            DismissProgressDialog(getApplication(), this.getClass(), progressDialog);
            debugEventsFrag.loadControls();
        }
    }

    private class AddTask extends SaveTask {
        @Override
        protected Void doInBackground(Object... params) {
            try {
                if (params != null && params.length > 0) {
                    if (params[0] instanceof Malfunction) {
                        getAdminController().AddDebugMalfunction((Malfunction) params[0]);
                    } else if (params[0] instanceof DataDiagnosticEnum) {
                        getAdminController().AddDebugDataDiagnostic((DataDiagnosticEnum) params[0]);
                    }
                }
            } catch (Throwable throwable) {
                Log.e(TAG, "Error while adding malfunction or data diagnostic", throwable);
            }
            return null;
        }
    }

    private class RemoveTask extends SaveTask {
        @Override
        protected Void doInBackground(Object... params) {
            try {
                if (params != null && params.length > 0) {
                    if (params[0] instanceof Malfunction) {
                        getAdminController().RemoveDebugMalfunction((Malfunction) params[0]);
                    } else if (params[0] instanceof DataDiagnosticEnum) {
                        getAdminController().RemoveDebugDataDiagnostic((DataDiagnosticEnum) params[0]);
                    }
                }
            } catch (Throwable throwable) {
                Log.e(TAG, "Error while removing malfunction or data diagnostic", throwable);
            }
            return null;
        }
    }
}
