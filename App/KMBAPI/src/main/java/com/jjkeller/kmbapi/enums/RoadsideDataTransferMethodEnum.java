package com.jjkeller.kmbapi.enums;

import com.jjkeller.kmbapi.R;

public class RoadsideDataTransferMethodEnum extends EnumBase {

    public static final int EMAIL = 0;
    public static final int WEBSERVICE = 1;

    public RoadsideDataTransferMethodEnum(int value) {
        super(value);
    }

    @Override
    public void setValue(int value) throws IndexOutOfBoundsException {
        switch (value) {
            case EMAIL:
            case WEBSERVICE:
                this.value = value;
                break;
            default:
                super.setValue(value);
        }
    }

    @Override
    protected int getArrayId() {
        return R.array.roadside_data_transfer_method_array;
    }

    @Override
    public String toDMOEnum() {
        // doesn't apply to this enum
        return null;
    }

}
