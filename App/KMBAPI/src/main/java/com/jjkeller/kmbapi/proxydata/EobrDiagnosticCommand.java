package com.jjkeller.kmbapi.proxydata;

import java.util.Date;

public class EobrDiagnosticCommand extends ProxyBase {

	///////////////////////////////////////////////////////////////////////////////////////
	// private members
	///////////////////////////////////////////////////////////////////////////////////////
	private String commandId;
	private String serialNumber;
	private String command;
	private Date responseTimestamp;
	private String respnose;
	
	
	///////////////////////////////////////////////////////////////////////////////////////
	// public get/set methods
	///////////////////////////////////////////////////////////////////////////////////////
	public String getDmoCommandId() {
		return commandId;
	}
	public void setDmoCommandId(String commandId) {
		this.commandId = commandId;
	}
	public String getSerialNumber() {
		return serialNumber;
	}
	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}
	public String getCommand() {
		return command;
	}
	public void setCommand(String command) {
		this.command = command;
	}
	public Date getResponseTimestamp() {
		return responseTimestamp;
	}
	public void setResponseTimestamp(Date responseTimestamp) {
		this.responseTimestamp = responseTimestamp;
	}
	public String getRespnose() {
		return respnose;
	}
	public void setRespnose(String respnose) {
		this.respnose = respnose;
	}
	
	///////////////////////////////////////////////////////////////////////////////////////
	// Custom methods
	///////////////////////////////////////////////////////////////////////////////////////

}