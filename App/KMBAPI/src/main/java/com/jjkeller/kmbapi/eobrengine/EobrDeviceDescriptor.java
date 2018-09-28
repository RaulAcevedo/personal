package com.jjkeller.kmbapi.eobrengine;

import android.os.Parcel;
import android.os.Parcelable;

public class EobrDeviceDescriptor implements Parcelable {

	public EobrDeviceDescriptor(String name, String address, int eobrGen, short crc){
		_name = name;
		_address = address;
		_eobrGen = eobrGen;
		_crc = crc;
	}
	
	private String _name;
	public String getName(){
		return _name;
	}
	public void setName(String val){
		_name = val;
	}
	
	private String _address;
	public String getAddress(){
		return _address;
	}
	public void setAddress(String val){
		_address = val;
	}
	
	private int _eobrGen;
	public int getEobrGen(){
		return _eobrGen;
	}
	public void setEobrGen(int val){
		_eobrGen = val;
	}
	
	private short _crc;
	public short getCrc(){
		return _crc;
	}
	public void setCrc(short val){
		_crc = val;
	}
	
	public String toString(){
		return _name;
	}
	
	public int describeContents() {
		return this.hashCode();
	}
	
	public void writeToParcel(Parcel dest, int flags) {
	      dest.writeString(_name);
	      dest.writeString(_address);
	}
}
