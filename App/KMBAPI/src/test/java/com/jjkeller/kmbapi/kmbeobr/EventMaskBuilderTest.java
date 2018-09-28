package com.jjkeller.kmbapi.kmbeobr;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class EventMaskBuilderTest {

    @Test
    public void BuildReturnsCorrectMask_WithOneType() {
        int mask = new EventMaskBuilder()
                .withEventType(new EventTypeEnum(EventTypeEnum.ERROR))
                .build();

        Assert.assertEquals(1 << EventTypeEnum.ERROR, mask);
    }

    @Test
    public void BuildReturnsCorrectMask_WithMultipleTypes() {
        int mask = new EventMaskBuilder()
                .withEventType(new EventTypeEnum(EventTypeEnum.IGNITIONOFF))
                .withEventType(new EventTypeEnum(EventTypeEnum.IGNITIONON))
                .withEventType(new EventTypeEnum(EventTypeEnum.TABRESET))
                .build();

        Assert.assertEquals(44, mask);
    }

    @Test
    public void BuildReturnsCorrectMask_WithVarArgs() {
        int mask = new EventMaskBuilder()
                .withEventTypes(
                        new EventTypeEnum(EventTypeEnum.IGNITIONOFF),
                        new EventTypeEnum(EventTypeEnum.IGNITIONON),
                        new EventTypeEnum(EventTypeEnum.TABRESET))
                .build();

        Assert.assertEquals(44, mask);
    }

    @Test
    public void BuildReturnsCorrectMask_WithVarArgsThroughMethod() {
        int mask = getMask(
            new EventTypeEnum(EventTypeEnum.IGNITIONOFF),
            new EventTypeEnum(EventTypeEnum.IGNITIONON),
            new EventTypeEnum(EventTypeEnum.TABRESET)
        );

        Assert.assertEquals(44, mask);
    }

    private int getMask(EventTypeEnum... eventTypes) {
        return new EventMaskBuilder().withEventTypes(eventTypes).build();
    }
}
