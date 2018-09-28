package com.jjkeller.kmbapi.controller.utility;

import com.jjkeller.kmbapi.common.ITimeKeeper;
import com.jjkeller.kmbapi.common.TimeKeeper;
import com.jjkeller.kmbapi.controller.share.IoTHubSettings;

import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AzureIoTHubServiceHelperTest {

    private ITimeKeeper existingTimeKeeper;

    @Before
    public void setUp() throws Exception {
        existingTimeKeeper = TimeKeeper.getInstance();
    }

    @After
    public void tearDown() throws Exception {
        TimeKeeper.setTimeKeeper(existingTimeKeeper);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void isIoTHubSettingsValid_should_return_false_for_null() throws Exception {
        boolean result = AzureIoTHubServiceHelper.isIoTHubSettingsValid(null);
        assertFalse(result);
    }

    @Test
    public void isIoTHubSettingsValid_should_return_false_for_null_expiration() throws Exception {
        IoTHubSettings settings = new IoTHubSettings();
        settings.setExpirationUtc(null);

        boolean result = AzureIoTHubServiceHelper.isIoTHubSettingsValid(settings);

        assertFalse(result);
    }

    @Test
    public void isIoTHubSettingsValid_should_return_false_for_expiration_prior_to_current_time() throws Exception {
        givenTimeKeeper(DateTime.parse("2017-01-15T13:00:00-06:00"));
        IoTHubSettings settings = new IoTHubSettings();
        settings.setExpirationUtc(Instant.parse("2017-01-15T12:59:59-06:00"));

        boolean result = AzureIoTHubServiceHelper.isIoTHubSettingsValid(settings);

        assertFalse(result);
    }

    @Test
    public void isIoTHubSettingsValid_should_return_false_for_expiration_equal_to_current_time() throws Exception {
        givenTimeKeeper(DateTime.parse("2017-01-15T13:00:00-06:00"));
        IoTHubSettings settings = new IoTHubSettings();
        settings.setExpirationUtc(Instant.parse("2017-01-15T13:00:00-06:00"));

        boolean result = AzureIoTHubServiceHelper.isIoTHubSettingsValid(settings);

        assertFalse(result);
    }

    @Test
    public void isIoTHubSettingsValid_should_return_true_for_expiration_later_than_current_time() throws Exception {
        givenTimeKeeper(DateTime.parse("2017-01-15T13:00:00-06:00"));
        IoTHubSettings settings = new IoTHubSettings();
        settings.setExpirationUtc(Instant.parse("2017-01-15T13:00:01-06:00"));

        boolean result = AzureIoTHubServiceHelper.isIoTHubSettingsValid(settings);

        assertTrue(result);
    }

    private void givenTimeKeeper(DateTime currentDateTime) {
        ITimeKeeper mockTimeKeeper = mock(ITimeKeeper.class);
        when(mockTimeKeeper.getCurrentDateTime()).thenReturn(currentDateTime);
        TimeKeeper.setTimeKeeper(mockTimeKeeper);
    }

}
