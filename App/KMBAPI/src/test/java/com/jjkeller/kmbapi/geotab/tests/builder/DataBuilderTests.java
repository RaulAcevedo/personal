package com.jjkeller.kmbapi.geotab.tests.builder;

import com.jjkeller.kmbapi.HosMessageProcessor.HosMessageProcessor;
import com.jjkeller.kmbapi.HosMessageProcessor.interfaces.IHOSMessage;
import com.jjkeller.kmbapi.featuretoggle.IFeatureToggleService;
import com.jjkeller.kmbapi.geotab.abstraction.data.ScenarioManagerBase;
import com.jjkeller.kmbapi.geotab.implementation.data.ScenarioSettings;
import com.jjkeller.kmbapi.geotab.implementation.engine.dataEngine.GeotabDataBuilder;
import com.jjkeller.kmbapi.geotab.implementation.engine.dataEngine.scenarioManagers.ApprovalScenarioManager;
import com.jjkeller.kmbapi.geotab.implementation.engine.dataEngine.scenarioManagers.BasicScenarioManager;
import com.jjkeller.kmbapi.geotab.implementation.engine.dataEngine.scenarios.SimpleDrivingScenario;
import com.jjkeller.kmbapi.geotab.implementation.enums.ScenarioSelectorEnum;
import com.jjkeller.kmbapi.geotabengine.HOSMessage;
import com.jjkeller.kmbapi.kmbeobr.Thresholds;
import com.jjkeller.kmbapi.common.TestBase;

import junit.framework.Assert;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import static org.mockito.Mockito.when;

/**
 * Test class that tests data validity/translation using the DataBuilder architecture
 */
public class DataBuilderTests extends TestBase {

    //region DataGeneration
    private final float MILESINLATDEGREE = 68.94f;
    private final float MILESINLONDEGREE = 54.583f;
    private final int SECONDSINHOUR = 3600;
    private final int RPMPERMPH = 10;
    private final int IDLERPM = 900;
    private final int MESSAGE_RATE_SECONDS = 2;
    //region DataGeneration
    @Mock
    IFeatureToggleService _stubFeatureToggleService;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        when(_stubFeatureToggleService.getIsEldMandateEnabled()).thenReturn(false);
    }

    /**
     * Test that asserts we can create a builder using the Builder pattern implementation that
     * has a DataOperation object which is an instanceof ApprovalDataOperation
     */
    @Test
    public void test_GeotabDataBuilder_TestInitializationOfBuilder_ForBasicOperation() {
        GeotabDataBuilder builder = GeotabDataBuilder.getDataBuilder()
                .usingDataOperationScheme(GeotabDataBuilder.DataOperationScheme.BASIC);
        Assert.assertTrue("Assert this builder is using the BASIC data operation object", builder.getCurrentScenarioManager() instanceof BasicScenarioManager);
    }

    /**
     * Test that asserts we can create a builder using the Builder pattern implementation that
     * has a DataOperation object which is an instanceof BasicDataOperation
     */
    @Test
    public void test_GeotabDataBuilder_TestInitializationOfBuilder_ForApprovalOperation() {
        GeotabDataBuilder builder = GeotabDataBuilder.getDataBuilder()
                .usingDataOperationScheme(GeotabDataBuilder.DataOperationScheme.APPROVALS);
        Assert.assertTrue("Assert this builder is using the APPROVALS data operation object", builder.getCurrentScenarioManager() instanceof ApprovalScenarioManager);
    }

    /**
     * Test that asserts we can create a normal scenario using the builder's exposed methods
     */
    @Test
    public void test_GeotabDataBuilder_TestInitializationOfScenario_ForBothOperationSchemes_WithNormalScenario() {
        //Create settings
        ScenarioSettings settings = new ScenarioSettings(null);

        GeotabDataBuilder builderForBasic = GeotabDataBuilder.getDataBuilder()
                .usingDataOperationScheme(GeotabDataBuilder.DataOperationScheme.BASIC)
                .setScenarioType(ScenarioSelectorEnum.DRIVING_CYCLE_SIMPLE);

        GeotabDataBuilder builderForApprovals = GeotabDataBuilder.getDataBuilder()
                .usingDataOperationScheme(GeotabDataBuilder.DataOperationScheme.APPROVALS)
                .setScenarioType(ScenarioSelectorEnum.DRIVING_CYCLE_SIMPLE);

        //Get the managers and initialize the scenarios
        ScenarioManagerBase approvalManager = builderForApprovals.getCurrentScenarioManager()
                .initializeScenario(settings);
        ScenarioManagerBase basicManager = builderForBasic.getCurrentScenarioManager()
                .initializeScenario(settings);

        //Null asserts for managers
        Assert.assertTrue("Assert Approval Scenario Manager Is Not Null.", approvalManager != null);
        Assert.assertTrue("Assert Basic Scenario Manager Is Not Null.", basicManager != null);
        //Type asserts for managers
        Assert.assertTrue("Assert Approval Scenario Manager Is Expected Type.", approvalManager instanceof ApprovalScenarioManager);
        Assert.assertTrue("Assert Basic Scenario Manager Is Expected Type.", basicManager instanceof BasicScenarioManager);
        //Null asserts for scenarios
        Assert.assertTrue("Assert Approval Scenario Is Not Null.", approvalManager.getCurrentScenario() != null);
        Assert.assertTrue("Assert Basic Scenario Is Not Null.", basicManager.getCurrentScenario() != null);
        //Type asserts for scenarios
        Assert.assertTrue("Assert Approval Scenario Is Expected Type.", approvalManager.getCurrentScenario() instanceof SimpleDrivingScenario);
        Assert.assertTrue("Assert Basic Scenario Is Expected Type.", basicManager.getCurrentScenario() instanceof SimpleDrivingScenario);

    }

    //REGION Code to generate normal scenario for comparison
    public int rpmForSpeed(float speed) {
        float rpm = IDLERPM;
        rpm += speed * RPMPERMPH;
        return Math.round(rpm);
    }

    public ArrayList<IHOSMessage> generateNonMovingMessages(IHOSMessage hosMessageTemplate, int duration) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        calendar.setTime(hosMessageTemplate.getTimestampUtc().toDate());

        IHOSMessage hosMessage = new HOSMessage(hosMessageTemplate);
        ArrayList<IHOSMessage> hosMessages = new ArrayList<>();
        for (int i = 0; i < duration; i += MESSAGE_RATE_SECONDS) {
            calendar.add(Calendar.SECOND, MESSAGE_RATE_SECONDS);
            hosMessage.setTimestampUtc(new DateTime(calendar.getTime()));
            hosMessages.add(hosMessage);
        }

        return hosMessages;
    }

    public ArrayList<IHOSMessage> generateMovingMessages(IHOSMessage hosMessageTemplate, int duration, float endingSpeed) {

        IHOSMessage hosMessage = new HOSMessage(hosMessageTemplate);
        float speedChange = (endingSpeed - hosMessage.getSpeedometer()) / MESSAGE_RATE_SECONDS;
        ArrayList<IHOSMessage> hosMessages = new ArrayList<>();
        for (int i = 0; i < duration; i += MESSAGE_RATE_SECONDS) {
            hosMessage.setSpeedometer(hosMessage.getSpeedometer() + speedChange);
            hosMessage = changesOver2sec(hosMessage);
            hosMessages.add(hosMessage);
        }

        return hosMessages;
    }

    public IHOSMessage changesOver2sec(IHOSMessage hosMessage) {

        int timeSpanSeconds = 2;
        float time = timeSpanSeconds / SECONDSINHOUR;
        float speed = hosMessage.getSpeedometer();
        float distance = time * speed;

        hosMessage.setOdometer(hosMessage.getOdometer() + distance);

        hosMessage.setTripOdometer(hosMessage.getTripOdometer() + distance);

        Date newTime = new Date(hosMessage.getTimestampUtc().getMillis() + 2000);
        hosMessage.setTimestampUtc(new DateTime(newTime));

        float newLon = hosMessage.getGpsLongitude() + distance / MILESINLONDEGREE;
        hosMessage.setGpsLongitude(newLon);

        hosMessage.setEngineHours(hosMessage.getEngineHours() + time);

        hosMessage.setTripEngineSeconds(hosMessage.getTripEngineSeconds() + timeSpanSeconds);

        return hosMessage;
    }

    public HashMap<Integer, IHOSMessage> getNormalTestResultsFromLongform() {
        HashMap<Integer, IHOSMessage> messages = new HashMap<>();
        Calendar calendar = Calendar.getInstance();

        _stubFeatureToggleService.setIsEldMandateEnabled(false);
        HosMessageProcessor processor = new HosMessageProcessor(_stubFeatureToggleService, Thresholds.DEFAULT);

        //Create Engine On message
        IHOSMessage engineOnStoppedMessage = new HOSMessage();
        engineOnStoppedMessage.setTimestampUtc(new DateTime(calendar.getTime()));
        engineOnStoppedMessage.setIgnitionOn(true);
        engineOnStoppedMessage.setTachometer(2000);
        engineOnStoppedMessage.setEngineActivityDetected(engineOnStoppedMessage.isIgnitionOn());

        //Add initial message to message collection
        messages.put(messages.size(), engineOnStoppedMessage);
        //This object will hold the current message such that it can be passed between
        //scenario structures
        IHOSMessage currentMessage = engineOnStoppedMessage;

        //Create events for pause between EngineOnStopped and engine on moving
        //This represents 1 minute of stopped time
        ArrayList<IHOSMessage> idleMessages = this.generateNonMovingMessages(currentMessage, 60);
        for (IHOSMessage message : idleMessages) {
            messages.put(messages.size(), message);
        }

        //Get last record in collection
        currentMessage = messages.get(messages.size() - 1);
        //Generate an array of messages that represent the acceleration of a vehicle from
        //the starting speed to 30KPH
        ArrayList<IHOSMessage> accelerationMessages = generateMovingMessages(currentMessage, 60, 30);
        //Add those to the message map
        for (IHOSMessage accelerationMessage : accelerationMessages) {
            messages.put(messages.size(), accelerationMessage);
        }

        //Get last record in collection
        currentMessage = messages.get(messages.size() - 1);
        //Create messages to transition back down to Stopped
        ArrayList<IHOSMessage> decelerationMessages = generateMovingMessages(currentMessage, 60, 0);
        //Add those to the message map
        for (IHOSMessage decelerationMessage : decelerationMessages) {
            messages.put(messages.size(), decelerationMessage);
        }

        //Get last record in collection
        currentMessage = messages.get(messages.size() - 1);
        //Now accelerate to initiate drive on over 2 minutes
        //Generate an array of messages that represent the acceleration of a vehicle from
        //the stopped speed (0) to 100KPH
        ArrayList<IHOSMessage> accelToDriveOnMessages = generateMovingMessages(currentMessage, 120, 100);
        //Add those to the message map
        for (IHOSMessage accelToDriveOnMessage : accelToDriveOnMessages) {
            messages.put(messages.size(), accelToDriveOnMessage);
        }

        //Get last record in collection
        currentMessage = messages.get(messages.size() - 1);
        //Drive for a while (5 min to be exact)
        ArrayList<IHOSMessage> drivingAtSetSpeedMessages = this.generateMovingMessages(currentMessage, 300, currentMessage.getSpeedometer());
        //Add those to the message map
        for (IHOSMessage driveAtSetSpeed : drivingAtSetSpeedMessages) {
            messages.put(messages.size(), driveAtSetSpeed);
        }

        //Halt driving by decelerating to 0
        //Get last record in collection
        currentMessage = messages.get(messages.size() - 1);
        //Create messages to transition back down to Stopped
        ArrayList<IHOSMessage> driveHaltedMessages = generateMovingMessages(currentMessage, 120, 0);
        //Add those to the message map
        for (IHOSMessage driveHaltedMessage : driveHaltedMessages) {
            messages.put(messages.size(), driveHaltedMessage);
        }

        //Get last record in collection
        currentMessage = messages.get(messages.size() - 1);
        //Sit until Drive Off
        ArrayList<IHOSMessage> waitingForDriveStopMessages = this.generateNonMovingMessages(currentMessage, 300);
        for (IHOSMessage message : waitingForDriveStopMessages) {
            messages.put(messages.size(), message);
        }
        return messages;
    }
//REGION Code to generate normal scenario for comparison

    /**
     * Test that asserts we can not provide a null strategy for generation of scenario data
     */
    @Test
    public void test_GeotabDataBuilder_TestFailure_FailAssignmentOfOperationStrategy() {
        //The below code should throw an IllegalArgumentException, as the provided operation strategy
        //is not a valid type
        try {
            GeotabDataBuilder builder = GeotabDataBuilder.getDataBuilder()
                    .usingDataOperationScheme(GeotabDataBuilder.DataOperationScheme.BASIC)
                    .setScenarioType(null);
        } catch (IllegalArgumentException expectedException) {
            boolean exceptionIsRightType = expectedException instanceof IllegalArgumentException;
            Assert.assertTrue("Assert exception thrown is IllegalArgumentException for invalid parameter.", exceptionIsRightType);
        } catch (Exception ex) {
            Assert.fail("Exception thrown was not expected type for scenario.");
        }
    }

    /**
     * Test that asserts we can not set a Scenario before first setting a data operation scheme
     */
    @Test
    public void test_GeotabDataBuilder_TestFailure_FailStrategyAssignment() {
        //The below code should throw an UnsupportedOperationException, as the internal Data Operation
        //object will be null when trying to set it's strategy
        try {
            GeotabDataBuilder builder = GeotabDataBuilder.getDataBuilder()
                    .setScenarioType(ScenarioSelectorEnum.DRIVING_CYCLE_SIMPLE);
        } catch (UnsupportedOperationException expectedException) {
            boolean exceptionIsRightType = expectedException instanceof UnsupportedOperationException;
            Assert.assertTrue("Assert exception thrown is UnsupportedOperationException for invalid object structure.", exceptionIsRightType);
        } catch (Exception ex) {
            Assert.fail("Exception thrown was not expected type for scenario.");
        }
    }
}
