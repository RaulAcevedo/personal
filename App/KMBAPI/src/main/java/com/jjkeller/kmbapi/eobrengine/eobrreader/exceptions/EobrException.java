package com.jjkeller.kmbapi.eobrengine.eobrreader.exceptions;

import com.jjkeller.kmbapi.kmbeobr.Enums.EobrReturnCode;

public abstract class EobrException extends RuntimeException
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;		
	
	private int returnCode;
	public int getReturnCode() {
		return returnCode;
	}

	public void setReturnCode(int returnCode) {
		this.returnCode = returnCode;
	}

	public static EobrException getEobrExceptionFromEobrReturnCodeValue(int code)
	{
		EobrException ex;
		switch(code)
		{
			case EobrReturnCode.S_COMMS_BUSY:
				ex = new CommBusyEobrException();
				ex.setReturnCode(EobrReturnCode.S_COMMS_BUSY); 
				return ex; 
			case EobrReturnCode.S_DATA_OUT_OF_BOUND: 
				ex =  new DataOutOfBoundsEobrException(); 
				ex.setReturnCode(EobrReturnCode.S_DATA_OUT_OF_BOUND); 
				return ex; 
			case EobrReturnCode.S_DEV_INTERNAL_ERROR:
				ex =  new InternalDeviceErrorEobrException(); 
				ex.setReturnCode(EobrReturnCode.S_DEV_INTERNAL_ERROR); 
				return ex; 
			case EobrReturnCode.S_FUNC_NOT_IMPLEMENTED:
				ex =  new FunctionNotImplmentedEobrException();
				ex.setReturnCode(EobrReturnCode.S_FUNC_NOT_IMPLEMENTED); 
				return ex; 
			case EobrReturnCode.S_GENERAL_ERROR:
				ex =  new GeneralErrorEobrException();
				ex.setReturnCode(EobrReturnCode.S_GENERAL_ERROR); 
				return ex; 
			case EobrReturnCode.S_INVALID_DATE_TIME:
				ex =  new InvalidDateTimeEobrException();
				ex.setReturnCode(EobrReturnCode.S_INVALID_DATE_TIME); 
				return ex; 
			case EobrReturnCode.S_NO_DATA:
				ex =  new NoDataEobrException();
				ex.setReturnCode(EobrReturnCode.S_NO_DATA); 
				return ex; 
			case EobrReturnCode.S_NO_HISTORICAL_DATA:
				ex =  new NoHistoricalDataEobrException();
				ex.setReturnCode(EobrReturnCode.S_NO_HISTORICAL_DATA); 
				return ex; 
			case EobrReturnCode.S_NO_SERIAL_DRIVER:
				ex =  new NoSerialDriverEobrException();
				ex.setReturnCode(EobrReturnCode.S_NO_SERIAL_DRIVER); 
				return ex; 
			case EobrReturnCode.S_WRONG_COMMAND_BACK:
				ex =  new WrongCommandBackEobrException();
				ex.setReturnCode(EobrReturnCode.S_WRONG_COMMAND_BACK); 
				return ex; 
			case EobrReturnCode.S_WRONG_SIGNATURE_CRC:
				ex =  new WrongCrcSignitureEobrException();
				ex.setReturnCode(EobrReturnCode.S_WRONG_SIGNATURE_CRC); 
				return ex; 
			case EobrReturnCode.S_DEV_NOT_CONNECTED:
				ex =  new DeviceNotConnectedEobrException();
				ex.setReturnCode(EobrReturnCode.S_DEV_NOT_CONNECTED); 
				return ex; 
				
			default:
				return null; 
		}
	} 
}