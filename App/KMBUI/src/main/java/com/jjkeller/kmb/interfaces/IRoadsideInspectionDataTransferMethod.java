package com.jjkeller.kmb.interfaces;

import com.jjkeller.kmbapi.enums.RoadsideDataTransferMethodEnum;

public interface IRoadsideInspectionDataTransferMethod {
    interface RoadsideInspectionDataTransferMethodActions {
        void onTransferMethodOkButtonClick(RoadsideDataTransferMethodEnum transferMethod, String comment);
        void onTransferMethodCancelButtonClick();
    }
}
