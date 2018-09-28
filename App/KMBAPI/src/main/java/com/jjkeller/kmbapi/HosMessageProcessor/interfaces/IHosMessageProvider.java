package com.jjkeller.kmbapi.HosMessageProcessor.interfaces;

/**
 * Created by ief5781 on 8/31/16.
 */
public interface IHosMessageProvider {
    void registerHosMessageReceiver(IHosMessageProcessor receiver);
}
