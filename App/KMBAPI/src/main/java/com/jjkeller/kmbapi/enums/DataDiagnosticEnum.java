package com.jjkeller.kmbapi.enums;

import com.jjkeller.kmbapi.R;

public enum DataDiagnosticEnum implements Describable{
    //Enum                      Code   DMO      Name Resource Id                                        Description Resource Id
    NONE(                           0, "None",  R.string.empty,                                         R.string.empty),
    POWER(                          1, "1",     R.string.datadiagnostic_power,                          R.string.datadiagnostic_power_desc),
    ENGINE_SYNCHRONIZATION(         2, "2",     R.string.datadiagnostic_enginesynchronization,          R.string.datadiagnostic_enginesynchronization_desc),
    MISSING_REQUIRED_DATA_ELEMENTS( 3, "3",     R.string.datadiagnostic_missingrequireddataelements,    R.string.datadiagnostic_missingrequireddataelements_desc),
    DATA_TRANSFER(                  4, "4",     R.string.datadiagnostic_datatransfer,                   R.string.datadiagnostic_datatransfer_desc),
    UNIDENTIFIED_DRIVING_RECORDS(   5, "5",     R.string.datadiagnostic_unidentifieddrivingrecords,     R.string.datadiagnostic_unidentifieddrivingrecords_desc),
    OTHER_ELD_IDENTIFIED(           6, "6",     R.string.datadiagnostic_othereldidentified,             R.string.datadiagnostic_othereldidentified_desc);

    int code;
    String dmoEnumCode;
    int descriptionResourceId;
    int nameResourceId;

    DataDiagnosticEnum(int code, String dmoEnum, int nameResourceId, int descriptionResourceId){
        this.code = code;
        this.dmoEnumCode = dmoEnum;
        this.descriptionResourceId = descriptionResourceId;
        this.nameResourceId = nameResourceId;
    }

    public String toDMOEnum()
    {
        return this.dmoEnumCode;
    }

    public int getValue(){
        return code;
    }

    public static DataDiagnosticEnum getByCode(int code){
        for (DataDiagnosticEnum dataEnum : values()){
            if (dataEnum.code == code){
                return dataEnum;
            }
        }
        throw new IllegalArgumentException("Enum value undefined");
    }

    public static DataDiagnosticEnum getByDmoValue(String dmoName) throws IllegalArgumentException {
        for (DataDiagnosticEnum dataDiagnosticEnum : values()){
            if (dataDiagnosticEnum.dmoEnumCode.equals(dmoName)){
                return dataDiagnosticEnum;
            }
        }
        throw new IllegalArgumentException("Enum value undefined");
    }

    public int getDescriptionKey() {
        return nameResourceId;
    }

    public int getFullDescriptionKey() {
        return descriptionResourceId;
    }
}