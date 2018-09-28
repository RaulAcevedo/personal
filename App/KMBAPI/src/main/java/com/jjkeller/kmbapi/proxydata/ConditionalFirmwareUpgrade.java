package com.jjkeller.kmbapi.proxydata;

public class ConditionalFirmwareUpgrade extends ProxyBase
{
    ///////////////////////////////////////////////////////////////////////////////////////
    // private members
    ///////////////////////////////////////////////////////////////////////////////////////
    //String eldSerialNumber, int generation, int majorVersion, int minorVersion, int patchVersion
    private String EldSerialNumber;
    private int Generation;
    private int MajorVersion;
    private int MinorVersion;
    private int PatchVersion;

    public ConditionalFirmwareUpgrade(String serialNumber, int generationNumber, int majorVersionNumber, int minorVersionNumber, int patchVersionNumber)
    {
        EldSerialNumber = serialNumber;
        Generation = generationNumber;
        MajorVersion = majorVersionNumber;
        MinorVersion = minorVersionNumber;
        PatchVersion = patchVersionNumber;
    }

    public String getEldSerialNumber() {
        return EldSerialNumber;
    }

    public void setEldSerialNumber(String eldSerialNumber) {
        this.EldSerialNumber = eldSerialNumber;
    }

    public int getGeneration() {
        return Generation;
    }

    public void setGeneration(int generation) {
        this.Generation = generation;
    }

    public int getMajorVersion() {
        return MajorVersion;
    }

    public void setMajorVersion(int majorVersion) {
        this.MajorVersion = majorVersion;
    }

    public int getMinorVersion() {
        return MinorVersion;
    }

    public void setMinorVersion(int minorVersion) {
        this.MinorVersion = minorVersion;
    }

    public int getPatchVersion() {
        return PatchVersion;
    }

    public void setPatchVersion(int patchVersion) {
        this.PatchVersion = patchVersion;
    }


}


