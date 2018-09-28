package com.jjkeller.kmb.share;

import android.app.Dialog;
import android.content.DialogInterface;

public class DialogMap {

	public DialogMap(String message, DialogInterface dialog){
		_message = message;
		_dialog = dialog;
	}
	
	private String _message;
	public String getMessage() {return _message;}

	private DialogInterface _dialog;
	public DialogInterface getDialog() {return _dialog;}
	
	public boolean equals(String messageToCheck){
		boolean answer = false;
		if(messageToCheck != null && messageToCheck.length() > 0){
			if(_message != null && _message.length() > 0){
				answer = _message.compareToIgnoreCase(messageToCheck) == 0;
			}
		}
		return answer;
	}
	
	public boolean equals(DialogInterface dlgToCheck){
		boolean answer = false;
		if(dlgToCheck != null && _dialog != null){
			answer = dlgToCheck == _dialog;
		}
		return answer;
	}
	
	public void show(){
		if(_dialog != null && _dialog instanceof Dialog){
			Dialog dlg = (Dialog)_dialog;
			dlg.hide();
			dlg.show();
		}
	}
}
