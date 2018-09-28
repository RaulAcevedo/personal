package com.jjkeller.kmbapi.firmwareupgrade;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by T000684 on 10/2/2017.
 */

public class FirmwareUpdateBroadcaster {

    public void onFirmwareUpdateStart(){
        EventBus.getDefault().post(FirmwareUpdateMessage.getProgressMessage(FirmwareUpdateMessage.ProcessEvent.START));
    }

    public void onDownloadFirmwareProgress(int progress){
        EventBus.getDefault().post(FirmwareUpdateMessage.getProgressUpdateMessage(progress));
    }

    public void onFirmwareUpdateFinished(boolean success){
        if(success){
            EventBus.getDefault().post(FirmwareUpdateMessage.getProgressMessage(FirmwareUpdateMessage.ProcessEvent.COMPLETED));
        }else{
            EventBus.getDefault().post(FirmwareUpdateMessage.getProgressMessage(FirmwareUpdateMessage.ProcessEvent.FAILED));
        }
    }

    public void shouldDowngradeFirmware(String tractorNumber){
        EventBus.getDefault().post(FirmwareUpdateMessage.getProgressMessage(FirmwareUpdateMessage.ProcessEvent.DOWNGRADE, tractorNumber));
    }

}
