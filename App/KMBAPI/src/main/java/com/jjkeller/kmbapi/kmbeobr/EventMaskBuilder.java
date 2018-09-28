package com.jjkeller.kmbapi.kmbeobr;

/**
 * Created by ief5781 on 2/20/17.
 */

public class EventMaskBuilder {
    private int mask = 0;

    public int build() {
        return mask;
    }

    public EventMaskBuilder withEventTypes(EventTypeEnum... eventTypes) {
        for(EventTypeEnum eventType : eventTypes)
            this.withEventType(eventType);

        return this;
    }

    public EventMaskBuilder withEventType(EventTypeEnum eventType) {
        if(eventType.getValue() == EventTypeEnum.ANYTYPE)
            return this;

        mask |= (1 << eventType.getValue());

        return this;
    }
}
