package com.jjkeller.kmb.share;

import android.app.Dialog;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by T000684 on 1/11/2018.
 */

public class BroadcastDialogMessage {

    public enum DialogAction {SHOW, HIDE};

    private String message;
    private Integer messageId;
    private DialogAction dialogAction;

    private BroadcastDialogMessage(DialogAction action, Integer msgId, String msg){
        this.dialogAction = action;
        this.messageId = msgId;
        this.message = msg;
    }

    public static BroadcastDialogMessage buildHideDialogMessage(){
        return new BroadcastDialogMessage(DialogAction.HIDE, null, null);
    }

    public static BroadcastDialogMessage buildShowDialogMessage(String message){
        return new BroadcastDialogMessage(DialogAction.SHOW, null, message);
    }

    public static BroadcastDialogMessage buildShowDialogMessage(int messageId){
        return new BroadcastDialogMessage(DialogAction.SHOW, messageId, null);
    }

    public String getMessage() {
        return message;
    }

    public Integer getMessageId() {
        return messageId;
    }

    public DialogAction getDialogAction() {
        return dialogAction;
    }

}
