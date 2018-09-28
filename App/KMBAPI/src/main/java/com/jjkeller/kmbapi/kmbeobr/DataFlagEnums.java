package com.jjkeller.kmbapi.kmbeobr;

/**
 * Created by ief5781 on 2/21/17.
 */

//these enums specify the bit-index of flags in the Data field of EventRecords
public class DataFlagEnums {
    public enum ErrorEventFlags implements IDataFlag {
        VSS_FAULT(9), ODO_FAULT(10);

        private final int index;
        ErrorEventFlags(int index) { this.index = index; }
        public int getIndex() { return index; }

        public EventTypeEnum getEventType() {
            return new EventTypeEnum(EventTypeEnum.ERROR);
        }
    }

    public enum GpsEventFlags implements IDataFlag {
        GPS_FAULT(0);

        private final int index;
        GpsEventFlags(int index) { this.index = index; }
        public int getIndex() { return index; }

        public EventTypeEnum getEventType() {
            return new EventTypeEnum(EventTypeEnum.GPS);
        }
    }
}
