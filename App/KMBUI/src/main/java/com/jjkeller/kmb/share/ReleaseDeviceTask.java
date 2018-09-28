package com.jjkeller.kmb.share;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.jjkeller.kmbapi.controller.EOBR.EobrReader;
import com.jjkeller.kmbapi.controller.SystemStartupController;
import com.jjkeller.kmbapi.controller.share.KmbApplicationException;
import com.jjkeller.kmbapi.controller.utility.ErrorLogHelper;
import com.jjkeller.kmbui.R;

/**
 * Created by ief5781 on 9/16/16.
 */
public class ReleaseDeviceTask extends AsyncTask<Void, Void, Boolean> {
    public interface ITaskHost {
        void onError(KmbApplicationException ex, String message);
        void onReleaseCompletion(String message);
        BaseActivity getHostActivity();
    }

    private ProgressDialog progress;
    private KmbApplicationException ex;
    private String className;
    private SystemStartupController startupController;
    private ITaskHost taskHost;

    public ReleaseDeviceTask(ITaskHost taskHost) {
        this.taskHost = taskHost;
        this.className = taskHost.getHostActivity().getClass().getSimpleName();

        startupController = new SystemStartupController(taskHost.getHostActivity());

        showProgressDialog();
    }

    public void onPreExecute() {
        taskHost.getHostActivity().LockScreenRotation();

        if (progress != null && !progress.isShowing())
            progress.show();
    }

    public Boolean doInBackground(Void... unused) {
        try {
            if (startupController.ShutdownEobrDevice()) {
                EobrReader.getInstance().setUnitLicensePlateNumber(null);
            }
        } catch (KmbApplicationException kae) {
            ex = kae;
        }

        return true;
    }

    public void onPostExecute(Boolean success) {
        if (ex != null) {
            taskHost.onError(ex, getString(R.string.msg_releasefailed));
        } else {
            try {
                String connectionState = getString(R.string.lbldisconnected);
                if (startupController.IsEobrDeviceOnline()) {
                    connectionState = getString(R.string.lblonline);
                }

                taskHost.onReleaseCompletion(String.format(getString(R.string.msg_releasedpartnership), connectionState));
            } catch (KmbApplicationException kae) {
                taskHost.onError(kae, getString(R.string.msg_releasefailed));
            }
        }

        dismissProgressDialog();
        taskHost.getHostActivity().UnlockScreenRotation();
    }

    // 9/29/11 JHM - Added public methods so that dialogs and context can be
    // re-established after an orientation change (ie. activity recreated).
    public void showProgressDialog() {
        if (!taskHost.getHostActivity().isFinishing())
            progress = ProgressDialog.show(taskHost.getHostActivity(), "", getString(R.string.lblreleasing));
    }

    public void dismissProgressDialog() {
        try {
            if (progress != null && progress.isShowing()) progress.dismiss();
        } catch (Exception ex) {
            ErrorLogHelper.RecordMessage(String.format(BaseActivity.MSGASYNCDIALOGEXCEPTION, this.className, this.getClass().getSimpleName()));
        }
    }

    private String getString(int id) {
        return taskHost.getHostActivity().getString(id);
    }

}
