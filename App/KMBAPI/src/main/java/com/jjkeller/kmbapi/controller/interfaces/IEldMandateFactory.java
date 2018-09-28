package com.jjkeller.kmbapi.controller.interfaces;


/**
 * Base Interface for a factory which will return IAPIEventController implementations
 */
public interface IEldMandateFactory {
     IAPIController getCurrentEventController();
}
