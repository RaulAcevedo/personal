package com.jjkeller.kmbapi.firmwareupgrade;


import static com.jjkeller.kmbapi.firmwareupgrade.FirmwareUpdateMessage.ProcessEvent.UPDATE;

/**
 * Created by T000684 on 10/2/2017.
 */

public class FirmwareUpdateMessage {

    public enum ProcessEvent {UPDATE, START, COMPLETED, FAILED, DOWNGRADE};

    private ProcessEvent eventType;
    private int progress;
    private String promptMsg;

    private FirmwareUpdateMessage(ProcessEvent event, int progress, String message) {
        this.eventType = event;
        this.progress = progress;
        this.promptMsg = message;
    }

    public static FirmwareUpdateMessage getProgressMessage(ProcessEvent event) {
        return new FirmwareUpdateMessage(event, 0, null);
    }

    public static FirmwareUpdateMessage getProgressMessage(ProcessEvent event, String msgText) {
        return new FirmwareUpdateMessage(event, 0, msgText);
    }


    public static FirmwareUpdateMessage getProgressUpdateMessage(int percent) {
        return new FirmwareUpdateMessage(UPDATE, percent, null);
    }


    public ProcessEvent getEventType() {
        return eventType;
    }

    public int getProgress() {
        return progress;
    }

    public String getPromptMsg() {
        return promptMsg;
    }
}
