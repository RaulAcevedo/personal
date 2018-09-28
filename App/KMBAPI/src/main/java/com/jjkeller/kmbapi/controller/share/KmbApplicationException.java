package com.jjkeller.kmbapi.controller.share;

public class KmbApplicationException extends Exception{
	
	private static final long serialVersionUID = 1L;
	private String _displayMessage;
	private String _caption;
	
	public KmbApplicationException(){
		super();
	}
	
	public KmbApplicationException(String message){
		super(message);
	}
	
	public String getCaption()
	{
		if(_caption == null || _caption.length() == 0)
			return "Error occurred";
		else return _caption;
	}	
	public void setCaption(String caption)
	{
		_caption = caption;		
	}
	
	public String getDisplayMessage()
	{
		String answer = null;
		
		if(_displayMessage != null && _displayMessage.length()>0)
			answer = _displayMessage;
		else answer = this.getMessage();
		
		return answer;
	}
	public void setDisplayMessage(String displayMessage)
	{
		_displayMessage = displayMessage;
	}
}
