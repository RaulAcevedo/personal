package com.jjkeller.kmbapi.controller.interfaces;

/**
 * Base interface definition for an EventObject. contains a hydrate method to allow internal
 * instantiation and assignment of required properties
 */
public interface IEventObject {
    //Method that accepts an Object that contains value(s) that are to be added to the IEventObject invoking the method
    void hydrate(boolean useSuper);
}
