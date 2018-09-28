package com.jjkeller.kmbapi.controller.interfaces;

public interface IEventHandler<T> {
	public abstract void onEventChange(T e);
}
