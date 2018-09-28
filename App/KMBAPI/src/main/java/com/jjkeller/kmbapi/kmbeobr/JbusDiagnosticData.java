package com.jjkeller.kmbapi.kmbeobr;

import java.util.Date;
import java.util.List;

public class JbusDiagnosticData {
	private int associatedEobrRecordId;
	private int recordId;
	private Date timestamp;
	private List<DTCInformation> dtcList;
	
	public int getAssociatedEobrRecordId()
	{
		return associatedEobrRecordId;
	}
	public void setAssociatedEobrRecordId(int associatedEobrRecordId)
	{
		this.associatedEobrRecordId = associatedEobrRecordId;
	}

	public Date getTimestamp()
	{
		return timestamp;
	}
	public void setTimestamp(Date timestamp)
	{
		this.timestamp = timestamp;
	}
	
	public List<DTCInformation> getDTCList()
	{
		return dtcList;
	}
	public void setDTCList(List<DTCInformation> dtcList)
	{
		this.dtcList = dtcList;
	}
	
	public void setRecordId(int recordId) {
		this.recordId = recordId; 
	}
	public int getRecordId() {
		return recordId;
	}
}
