package com.jjkeller.kmbapi.eobrengine.eobrdatatypes;

import com.jjkeller.kmbapi.kmbeobr.GpsFix;
import com.jjkeller.kmbapi.kmbeobr.ObdData;

public class Eobr_Data_GenII {
	public byte ignition;
	public int recordId;
	public int eobrStat;
	public int activeBus;
	public int diagRecId;
	
	public GpsFix gpsFix;
	public ObdData obdData;
	
	public byte GetIgnition() {
		return this.ignition;
	}
	public void SetIgnition(byte ignition) {
		this.ignition = ignition;
	}
	
	public int GetRecordId() {
		return this.recordId;
	}
	public void SetRecordId(int recordid) {
		this.recordId = recordid;
	}
	
	public int GetEOBRStat() {
		return this.eobrStat;
	}
	public void SetEOBRStat(int eobrstat) {
		this.eobrStat = eobrstat;
	}
	
	public int GetActiveBus() {
		return this.activeBus;
	}
	public void SetActiveBus(int activebus) {
		this.activeBus = activebus;
	}
	
	public int GetDiagRecId() {
		return this.diagRecId;
	}
	public void SetDiagRecId(int diagrecid) {
		this.diagRecId = diagrecid;
	}
}
