package com.jjkeller.kmbapi.enums;

/**
 * Created by T000684 on 9/14/2017.
 */

public enum LogStatus {
    ACTIVE_LOCAL_LOG(1),
    SUBMITTED_LOG(2),
    SERVER_COPY(3);

    private int value;

    LogStatus(final int statusValue) {
        this.value = statusValue;
    }

    public static LogStatus valueOf(int value) throws IllegalArgumentException {
        for (LogStatus status : values()) {
            if (status.getValue() == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Enum value undefined");
    }

    public int getValue() {
        return value;
    }
}

