package com.jjkeller.kmbapi.proxydata;

import com.google.gson.annotations.SerializedName;
import com.jjkeller.kmbapi.common.VersionUtility;
import com.jjkeller.kmbapi.proxydata.ProxyBase;

import java.text.NumberFormat;
import java.text.ParseException;

public class FirmwareVersion extends ProxyBase {
	//NOTE: The app uses Gson to deserialize JSON into this class.  It populates it using field names - the JSON needs to match the field names here.

	@SerializedName("CRC")
	private int crc;
	public int getCrc() { return crc; }
	public void setCrc(int crc) { this.crc = crc; }

	@SerializedName("Generation")
	private int generation;
	public int getGeneration() {
		return generation;
	}
	public void setGeneration(int generation) {
		this.generation = generation;
	}

	@SerializedName("Id")
	private String id;
	public String getId()
	{
		return id;
	}
	public void setId(String value)
	{
		id = value;
	}

	@SerializedName("IsActive")
	private boolean isActive;
	public boolean getIsActive() {
		return isActive;
	}
	public void setIsActive(boolean isActive) {
		this.isActive = isActive;
	}

	@SerializedName("IsDefaultForMajorMinor")
	private boolean isDefaultForMajorMinor;
	public boolean getIsDefaultForMajorMinor() {
		return isDefaultForMajorMinor;
	}
	public void setIsDefaultForMajorMinor(boolean IsDefaultForMajorMinor) {
		this.isDefaultForMajorMinor = isDefaultForMajorMinor;
	}

	@SerializedName("IsBreakingChange")
	private boolean isBreakingChange;
	public boolean getIsBreakingChange() { return isBreakingChange; }
	public void setIsBreakingChange(boolean breakingChange) { isBreakingChange = breakingChange; }

	@SerializedName("Major")
	private int major;
	public int getMajor() {
		return major;
	}
	public void setMajor(int major) {
		this.major = major;
	}

	@SerializedName("Minor")
	private int minor;
	public int getMinor() {
		return minor;
	}
	public void setMinor(int minor) {
		this.minor = minor;
	}
	
	@SerializedName("Patch")
	private int patch;
	public int getPatch() {
		return patch;
	}
	public void setPatch(int patch) {
		this.patch = patch;
	}

	public String getVersionString() {
		return VersionUtility.getVersionString(getMajor(), getMinor(), getPatch());
	}

	public String getImageFileName() {
		return VersionUtility.getImageFileName(getGeneration(), getMajor(), getMinor(), getPatch());
	}
}