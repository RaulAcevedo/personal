package com.jjkeller.kmbapi.enums;

public enum UnidentifiedEldEventStatus {
    NONE(0),
    LOCAL(1),
    SUBMITTED(2),
    CLAIMED(3);

    private static final UnidentifiedEldEventStatus[] values = UnidentifiedEldEventStatus.values();

    public final int value;

    UnidentifiedEldEventStatus(int value) {
        this.value = value;
    }

    public static UnidentifiedEldEventStatus fromValue(int value) {
        return values[value];
    }
}
