package com.jjkeller.kmbapi.enums;

public class DataTransferFileStatusEnum extends EnumBase {
    public static final int PENDING = 0;
    public static final int SENT = 1;
    public static final int SUCCESS = 2;
    public static final int ERROR = 3;
    public static final int WARNING = 5;
    public static final int INFORMATIONAL = 6;
    public static final int NOTVALIDATED = 7;
    public static final int UNKNOWN = 8;

    public DataTransferFileStatusEnum(int value) { super(value); }

    @Override
    public void setValue(int value) throws IndexOutOfBoundsException {
        switch (value) {
            case PENDING:
            case SENT:
            case SUCCESS:
            case ERROR:
            case WARNING:
            case INFORMATIONAL:
            case NOTVALIDATED:
            case UNKNOWN:
                this.value = value;
                break;
            default:
                super.setValue(value);
        }
    }

    @Override
    protected int getArrayId() {
        return 0;
    }

    @Override
    public String toDMOEnum() {
        // doesn't apply to this enum
        return null;
    }
}
