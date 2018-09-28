package com.jjkeller.kmbapi.eldmandate;

import com.jjkeller.kmbapi.controller.dataaccess.ApplicationStateFacade;
import com.jjkeller.kmbapi.proxydata.ApplicationStateSettings;

public class EventSequenceIdGenerator {

    private ApplicationStateFacade facade;
    private static final int MAX_SEQUENCEID = 65535;

    public EventSequenceIdGenerator(ApplicationStateFacade applicationStateFacade){
        facade = applicationStateFacade;
    }

    public int GetNextSequenceNumber() {
        synchronized (EventSequenceIdGenerator.class) {
            ApplicationStateSettings settings = facade.Fetch();
            if (settings == null) {
                settings = new ApplicationStateSettings();
                settings.setEventSequenceId(MAX_SEQUENCEID);
            }

            int currentSequenceId = settings.getEventSequenceId();

            int nextSequenceId;
            if (currentSequenceId < MAX_SEQUENCEID) {
                nextSequenceId = currentSequenceId + 1;
            } else {
                nextSequenceId = 0;
            }

            settings.setEventSequenceId(nextSequenceId);
            facade.Save(settings);

            return nextSequenceId;
        }
    }
}
