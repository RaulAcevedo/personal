package com.jjkeller.kmb.share;

import android.app.ProgressDialog;
import android.content.Context;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


/**
 * Created by T000684 on 1/11/2018.
 */

public class BroadcastDialog {

    private Context context;
    private ProgressDialog dialog;


    public BroadcastDialog(){

    }


    public void register(Context activity){
        this.context = activity;
        EventBus.getDefault().register(this);
    }

    public void unregister(){
        context = null;
        hideDialog();
        EventBus.getDefault().unregister(this);
    }

    public void showDialog(String message){
        if(context != null) {
            dialog = ProgressDialog.show(context, "", message);
        }
    }


    public void hideDialog(){
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }


    @Subscribe(sticky = true, threadMode =  ThreadMode.MAIN)
    public void onMessageEvent(BroadcastDialogMessage msg){
        switch (msg.getDialogAction()) {
            case SHOW:
                if(msg.getMessageId() != null){
                    showDialog(context.getString(msg.getMessageId()));
                }else {
                    showDialog(msg.getMessage());
                }
                break;
            case HIDE:
                hideDialog();
                break;
            default:
                break;
        }
    }
}
