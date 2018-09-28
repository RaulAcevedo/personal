package com.jjkeller.kmbapi.kmbeobr;

public class EobrResponse<T> extends EobrResponseBase {

	private T data;

	public EobrResponse(int returnCode)
	{
		super(returnCode);
	}

	public EobrResponse(int returnCode, T data) {
		super(returnCode);

		setData(data);
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}
}
