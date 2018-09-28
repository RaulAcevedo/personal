package com.jjkeller.kmbapi.enums;

/**
 * Created by jar5943 on 4/21/2016.
 */
public enum EmployeeLogProvisionTypeEnum {
    PERSONALCONVEYANCE(0, "Personal conveyance"),
    HYRAIL(1, "Hyrail"),
    NONREGULATED(2, "Non-Regulated");

    private static EmployeeLogProvisionTypeEnum[] _values;

    static {
        _values = EmployeeLogProvisionTypeEnum.values();
    }

    private int value;
    private String string;

    EmployeeLogProvisionTypeEnum(int value, String string) {
        this.value = value;
        this.string = string;
    }

    public int getValue() {
        return value;
    }
    public String getString() { return string; }

    public static EmployeeLogProvisionTypeEnum fromInt(int value) {
        return _values[value];
    }
}