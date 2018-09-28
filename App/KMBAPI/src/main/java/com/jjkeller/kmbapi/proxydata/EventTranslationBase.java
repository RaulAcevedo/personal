package com.jjkeller.kmbapi.proxydata;

import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.interfaces.IEventObject;
import com.jjkeller.kmbapi.kmbeobr.Constants;

import java.util.Date;

/**
 * Base abstract class representing a bridge between an IEventObject and a LogEvent
 * Implements IEventObject
 * @param <T> IEventObject to translate properties between from the LogEvent base
 */
public abstract class EventTranslationBase<T extends IEventObject> extends LogEvent implements IEventObject {

    /**
     * Enum to designate the State in which the IEventObject was created
     */
    public enum EventOriginEnum {
        NULL, AOBRD, MANDATE
    }

    /**
     * boolean property that shows mandate state at time of initialization
     */
    protected  final boolean mandateState;


    /**
     * Constructor that sets the internal T event to this, ensuring we can translate properties but
     * also allow for pure access of the IEventObject we want to translate
     */
    protected EventTranslationBase() {
        super();
        mandateState= GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled();
        this.initializingProcess = mandateState ? EventOriginEnum.MANDATE : EventOriginEnum.AOBRD;
    }

    /**
     * Constructor to call super(Date) constructor. Ensures we can create events with EventDateTime
     * populated without seconds
     * @param startDateTime Date object for start of event
     */
    protected EventTranslationBase(Date startDateTime)
    {
        super(startDateTime);
        mandateState= GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled();
        this.initializingProcess = mandateState ? EventOriginEnum.MANDATE : EventOriginEnum.AOBRD;
    }

    /**
     * Abstract method to be implemented by derived classes that defines the default properties to
     * be set when Item is created
     * @param useMandateMode boolean value determining which process to use
     *                       (ELD-MANDATE or AOBRD)
     */
    @Override
    public abstract void hydrate(boolean useMandateMode);


    /**
     * Public method that calculates the distance since the last valid coordinates using
     * MILES_TO_METERS constant and known uncertainty
     * @param uncert value representing GPS uncertainty
     * @return value representing number of miles given known uncertainty or null if uncertainty is less than or equal to 0
     */
    public static Float calculateDistanceSinceLastValidCoordinates(float uncert) {
        if (uncert >= 0) {
            int miles = Math.round(uncert / Constants.MILES_TO_METERS);

            // Must be between 0-6
            if (miles < 0)
                miles = 0;
            else if (miles > 6)
                miles = 6;

            return (float) miles;
        }
        return null;
    }

    /**
     * Internal instance of Class<T> that allows us to return values for an IEvent object from a different object that implements IEventObject
     */
    protected transient T _event = null;

    /**
     * Internal reference to a LocationTranslationBase object, representing the location object
     * contained on the base class
     */
    protected transient LocationAdapter _internalLocation;

    /**
     * Enum to represent the state of the feature toggle related to process switching
     */
    protected EventOriginEnum initializingProcess = EventOriginEnum.NULL;

    /**
     * Method that sets our internal pointer to this object. Allows for recursive getter/setter access
     * @param event Class<T>T</T> object that will be used to conatin translatable property references
     */
    protected void setInternalEventPointerReference(T event) {
        this._event = event;
    }

}
