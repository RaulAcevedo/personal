package com.jjkeller.kmb;

import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmbapi.datatransferfilestatus.DataTransferFileStatusEvent;
import com.jjkeller.kmbui.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static com.jjkeller.kmbapi.configuration.GlobalState.getContext;

public class DataTransferFileStatusBroadcastListener {
    private BaseActivity activity;

    public void register(BaseActivity activity){
        this.activity = activity;
        EventBus.getDefault().register(this);
    }

    public void unregister(){
        activity = null;
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode =  ThreadMode.MAIN)
    public void onMessageEvent(DataTransferFileStatusEvent event) {
        switch (event.eventType) {
            case SUCCESSFUL:
                activity.ShowMessage(activity, getContext().getString(R.string.roadside_data_transfer_email_success_message));
                break;
            case FAILURE:
                activity.ShowMessage(activity, getContext().getString(R.string.roadside_data_transfer_email_failure_message));
                break;

        }
    }
}
