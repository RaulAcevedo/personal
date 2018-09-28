package com.jjkeller.kmbapi.configuration;

public class KmbUserInfo {

	public KmbUserInfo(){}
	
	private String _kmbUsername;
	public String getKmbUsername() {
		return this._kmbUsername == null ? null : this._kmbUsername.toLowerCase();
	}
	public void setKmbUsername(String kmbUsername) {
		this._kmbUsername = kmbUsername == null ? null : kmbUsername.toLowerCase();
	}
	
	private String _kmbPassword;
	public String getKmbPassword()
	{
		return this._kmbPassword;
	}
	public void setKmbPassword(String password)
	{
		this._kmbPassword = password;
	}
	
	private String _dmoEmployeeId;
	public String getDmoEmployeeId()
	{
		return this._dmoEmployeeId;
	}
	public void setDmoEmployeeId(String dmoEmployeeId)
	{
		this._dmoEmployeeId = dmoEmployeeId;
	}
	
	private String _kmbVersionNumber = "1.0.0.0";
	public String getKmbVersionNumber()
	{
		return this._kmbVersionNumber;
	}
	public void setKmbVersionNumber(String kmbVersionNumber)
	{
		this._kmbVersionNumber = kmbVersionNumber;
	}
	
	
}
