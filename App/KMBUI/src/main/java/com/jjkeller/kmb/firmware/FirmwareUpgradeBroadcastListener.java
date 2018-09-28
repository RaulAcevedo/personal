package com.jjkeller.kmb.firmware;

import android.util.Log;

import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmb.share.FirmwareUpdateProgressListeners.FirmwareUpdateProgressListenerFactory;
import com.jjkeller.kmbapi.firmwareupgrade.FirmwareUpdateMessage;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by T000684 on 9/29/2017.
 */

public class FirmwareUpgradeBroadcastListener {

    private IFirmwareUpdateProgressListener handler;
    private IFirmwareUpdateProgressListener persistedHandler;//used to track the handler/listener between app `onStop` event
    private BaseActivity activity;

    public FirmwareUpgradeBroadcastListener(){
        EventBus.getDefault().unregister(this);
    }

    public void register(BaseActivity activity){
        this.activity = activity;
        EventBus.getDefault().register(this);
    }

    public void unregister(){
        handler = null;
        activity = null;
        EventBus.getDefault().unregister(this);
    }

    public void updateHandler(){
        if(handler == null && persistedHandler != null) {
            handler = persistedHandler;
        }
    }

    public IFirmwareUpdateProgressListener getPersistedHandler(){
        return persistedHandler;
    }

    public void setPersistedHandler(IFirmwareUpdateProgressListener handlerToPersist){
        persistedHandler = handlerToPersist;
    }

    @Subscribe(threadMode =  ThreadMode.MAIN)
    public void onMessageEvent(FirmwareUpdateMessage message){

        if(handler == null && message.getEventType() == FirmwareUpdateMessage.ProcessEvent.START || message.getEventType() == FirmwareUpdateMessage.ProcessEvent.DOWNGRADE){
            handler = FirmwareUpdateProgressListenerFactory.getProgressListener(activity);
        }

        if(handler != null) {
            switch (message.getEventType()) {
                case START:
                    handler.onFirmwareUpdateStart(activity);
                    setPersistedHandler(handler);
                    break;
                case COMPLETED:
                    setPersistedHandler(null);
                    handler.onFirmwareUpdateFinished(activity, true);
                    break;
                case FAILED:
                    setPersistedHandler(null);
                    handler.onFirmwareUpdateFinished(activity, false);
                    break;
                case UPDATE:
                    handler.onDownloadFirmwareProgress(activity, message.getProgress());
                    setPersistedHandler(handler);
                    break;
                case DOWNGRADE:
                    handler.shouldDowngradeFirmware(activity, message.getPromptMsg());
                    setPersistedHandler(handler);
                    break;
                default:
                    setPersistedHandler(handler);
                    break;
            }
        }else{
            Log.i("FirmwareUpgradeListener", "NO handler configured in Firmware update broadcast listener!!");
        }
    }

    public void setHandler(IFirmwareUpdateProgressListener listener) {
        this.handler = listener;
    }
}
