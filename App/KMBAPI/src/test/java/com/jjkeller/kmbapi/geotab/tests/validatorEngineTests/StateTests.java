package com.jjkeller.kmbapi.geotab.tests.validatorEngineTests;

import com.jjkeller.kmbapi.HosMessageProcessor.interfaces.IHOSMessage;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.AobrdEngineOff;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.AobrdMovingNotDriving;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.MandateEngineOffDriving;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.MandateEngineOffNotDriving;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.MandateMovingDriving;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.MandateMovingNotDriving;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.MandateStoppedDriving;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.MandateStoppedNotDriving;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.State;
import com.jjkeller.kmbapi.geotab.abstraction.state.StateValidityMappingBase;
import com.jjkeller.kmbapi.geotab.HosMessages;
import com.jjkeller.kmbapi.geotab.abstraction.base.HosProcessorStateMachineTestBase;
import com.jjkeller.kmbapi.geotab.implementation.engine.assertionEngine.HosProcessorStateMachineTestAssertionEngine;

import junit.framework.Assert;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Basic test class designed to ensure proper typing when returning a new AssertionEngine
 */
public class StateTests extends HosProcessorStateMachineTestBase {
    /**
     * Public constructor
     *
     * @throws Exception Thrown when HosProcessorStateMachineTestBase throws an exception
     */
    public StateTests() throws Exception {
    }

    /**
     * Test that asserts the engine will run with a single initial state, a provided end state,
     * and multiple messages
     *
     * @throws Exception
     */
    @Test
    public void test_StateTest_TestAOBRDStates_ForAOBRDEngineOff_WithMultipleMessages_AndDesiredEndState() throws Exception {
        ArrayList<State> initialStatesToValidate = new ArrayList<>();
        //New up and AobrdEngineOff state object
        State aobrdEngineOff = new AobrdEngineOff(sharedState);

        initialStatesToValidate.add(aobrdEngineOff);
        IHOSMessage engineOnStopped = HosMessages.EngineOnStopped(datetimeFormat.parse("2016.09.19 13:00"));
        IHOSMessage engineOnMoving = HosMessages.EngineOnMoving(datetimeFormat.parse("2016.09.19 13:02"));
        IHOSMessage engineOff = HosMessages.EngineOnStopped(datetimeFormat.parse("2016.09.19 13:03"));
        engineOff.setIgnitionOn(false);

        //Validate the state against an ignition on event
        HashMap<Integer, IHOSMessage> messageHashMap = new HashMap<>();
        messageHashMap.put(0, engineOnStopped);
        messageHashMap.put(1, engineOnMoving);
        messageHashMap.put(2, engineOff);

        HosProcessorStateMachineTestAssertionEngine engineTest = HosProcessorStateMachineTestAssertionEngine.getEngineFor(initialStatesToValidate)
                .withSharedState(sharedState)
                .withRoundTripStateMappingForMessage(aobrdEngineOff, engineOnStopped, MandateEngineOffNotDriving.class)
                .withRoundTripStateMappingForMessage(aobrdEngineOff, engineOnMoving, AobrdMovingNotDriving.class)
                .withRoundTripStateMappingForMessage(aobrdEngineOff, engineOff, AobrdEngineOff.class)
                .WithMessages(messageHashMap)
                .initializeValidators();

        //Assert the engine test returned isn't null
        Assert.assertTrue("Returned Processing Engine shouldn't be null.", engineTest != null);
        Assert.assertTrue("Ensure 1:1 mapping between initial state and validators", engineTest.GetValidators().size() == initialStatesToValidate.size());

        //Expose validators that were returned from process
        Set<StateValidityMappingBase> validatedEngineResults = engineTest.ValidateStateTransaction().GetValidators();
    }

    /**
     * Test that asserts the engine will run with a single initial state, a provided end state,
     * and multiple messages
     *
     * @throws Exception
     */
    @Test
    public void test_StateTest_TestMandateStates_ForMandateEngineOffDrivingAndNotDriving_WithMultipleMessages() throws Exception {

        //Create events to test
        IHOSMessage engineOnStopped = HosMessages.EngineOnStopped(datetimeFormat.parse("2016.09.19 13:00"));
        IHOSMessage engineOnMoving = HosMessages.EngineOnMoving(datetimeFormat.parse("2016.09.19 13:02"));
        IHOSMessage engineOff = HosMessages.EngineOnStopped(datetimeFormat.parse("2016.09.19 13:03"));
        engineOff.setIgnitionOn(false);

        HashMap<Integer, IHOSMessage> messageHashMap = new HashMap<>();

        messageHashMap.put(0, engineOnStopped);
        messageHashMap.put(1, engineOnMoving);
        messageHashMap.put(2, engineOff);

        ArrayList<State> initialStatesToValidate = new ArrayList<>();

        //New up mandate engine off events
        State mandateEngineOffDriving = new MandateEngineOffDriving(sharedState, datetimeFormat.parse("2016.09.19 10:00"));
        State mandateEngineOffNotDriving = new MandateEngineOffNotDriving(sharedState);
        initialStatesToValidate.add(mandateEngineOffDriving);
        initialStatesToValidate.add(mandateEngineOffNotDriving);

        HosProcessorStateMachineTestAssertionEngine engineTest = HosProcessorStateMachineTestAssertionEngine.getEngineFor(initialStatesToValidate)
                .withSharedState(sharedState)
                .withRoundTripStateMappingForMessage(mandateEngineOffDriving, engineOnStopped, MandateStoppedDriving.class)
                .withRoundTripStateMappingForMessage(mandateEngineOffDriving, engineOnMoving, MandateMovingDriving.class)
                .withRoundTripStateMappingForMessage(mandateEngineOffDriving, engineOff, MandateEngineOffDriving.class)
                .withRoundTripStateMappingForMessage(mandateEngineOffNotDriving, engineOnStopped, MandateStoppedNotDriving.class)
                .withRoundTripStateMappingForMessage(mandateEngineOffNotDriving, engineOnMoving, MandateMovingNotDriving.class)
                .withRoundTripStateMappingForMessage(mandateEngineOffNotDriving, engineOff, MandateEngineOffNotDriving.class)
                .WithMessages(messageHashMap)
                .initializeValidators();

        //Assert the engine test returned isn't null
        Assert.assertTrue("Returned Processing Engine shouldn't be null.", engineTest != null);
        Assert.assertTrue("Ensure 1:1 mapping between initial state and validators", engineTest.GetValidators().size() == initialStatesToValidate.size());

        //Expose validators that were returned from process
        Set<StateValidityMappingBase> validatedEngineResults = engineTest.ValidateStateTransaction().GetValidators();
    }


//    @Test
//    public void test_StateTest_TestMandateStates_NoEndState_WithSingleMessage() throws Exception {
//        ArrayList<State> initialStatesToValidate = new ArrayList<>();
//        Date drivingDateTest = datetimeFormat.parse("2016.09.19 13:00");
//        initialStatesToValidate.add(new MandateStoppedDriving(sharedState, drivingDateTest));
//        initialStatesToValidate.add(new MandateEngineOffNotDriving(sharedState));
//        initialStatesToValidate.add(new MandateEngineOffDriving(sharedState, drivingDateTest));
//        initialStatesToValidate.add(new MandateStoppedNotDriving(sharedState));
//        initialStatesToValidate.add(new MandateMovingDriving(sharedState));
//        initialStatesToValidate.add(new MandateMovingNotDriving(sharedState));
//        //Validate the state against an ignition on event
//        HashMap<Integer, IHOSMessage> messageHashMap = new HashMap<>();
//        messageHashMap.put(0, HosMessages.EngineOnStopped(datetimeFormat.parse("2016.09.19 15:00")));
//
//
//        HosProcessorStateMachineTestAssertionEngine engineTest = HosProcessorStateMachineTestAssertionEngine.getEngineFor(initialStatesToValidate)
//                .withSharedState(sharedState)
//                .WithMessages(messageHashMap)
//                .WI
//                .initializeValidators();
//
//        //Assert the engine test returned isn't null
//        Assert.assertTrue("Returned Processing Engine shouldn't be null.", engineTest != null);
//        Assert.assertTrue("Ensure 1:1 mapping between initial state and validators", engineTest.GetValidators().size() == initialStatesToValidate.size());
//
//        Set<StateValidityMappingBase> validatedEngineResults = engineTest.ValidateStateTransaction().GetValidators();
//    }
//
//    @Test
//    public void test_StateTest_TestAOBRDStates_NoEndState_WithSingleMessage() throws Exception {
//        ArrayList<State> initialStatesToValidate = new ArrayList<>();
//        Date drivingDateTest = datetimeFormat.parse("2016.09.19 13:00");
//        initialStatesToValidate.add(new AobrdEngineOff(sharedState));
//        initialStatesToValidate.add(new AobrdStoppedNotDriving(sharedState));
//        initialStatesToValidate.add(new AobrdStoppedDriving(sharedState, drivingDateTest));
//        initialStatesToValidate.add(new AobrdMovingNotDriving(sharedState, 0));
//        initialStatesToValidate.add(new AobrdMovingDriving(sharedState));
//
//        HosProcessorStateMachineTestAssertionEngine engineTest = HosProcessorStateMachineTestAssertionEngine.getEngineFor(initialStatesToValidate)
//                .withSharedState(sharedState)
//                .initializeValidators();
//
//        //Assert the engine test returned isn't null
//        Assert.assertTrue("Returned Processing Engine shouldn't be null.", engineTest != null);
//        Assert.assertTrue("Ensure 1:1 mapping between initial state and validators", engineTest.GetValidators().size() == initialStatesToValidate.size());
//        //Validate the state against an ignition on event
//        HashMap<Integer, IHOSMessage> messageHashMap = new HashMap<>();
//        messageHashMap.put(0, HosMessages.EngineOnStopped(datetimeFormat.parse("2016.09.19 15:00")));
//        Set<StateValidityMappingBase> validatedEngineResults = engineTest.ValidateStateTransaction(messageHashMap).GetValidators();
//    }
//
//    @Test
//    public void test_StateTest_TestAllStates_NoEndState_WithSingleMessage() throws Exception {
//        ArrayList<State> initialStatesToValidate = new ArrayList<>();
//        Date drivingDateTest = datetimeFormat.parse("2016.09.19 13:00");
//
//        initialStatesToValidate.add(new AobrdEngineOff(sharedState));
//        initialStatesToValidate.add(new AobrdStoppedNotDriving(sharedState));
//        initialStatesToValidate.add(new AobrdStoppedDriving(sharedState, drivingDateTest));
//        initialStatesToValidate.add(new AobrdMovingNotDriving(sharedState, 0));
//        initialStatesToValidate.add(new AobrdMovingDriving(sharedState));
//        initialStatesToValidate.add(new MandateEngineOffNotDriving(sharedState));
//        initialStatesToValidate.add(new MandateEngineOffDriving(sharedState, drivingDateTest));
//        initialStatesToValidate.add(new MandateStoppedNotDriving(sharedState));
//        initialStatesToValidate.add(new MandateStoppedDriving(sharedState, drivingDateTest));
//        initialStatesToValidate.add(new MandateMovingDriving(sharedState));
//        initialStatesToValidate.add(new MandateMovingNotDriving(sharedState));
//
//        HosProcessorStateMachineTestAssertionEngine engineTest = HosProcessorStateMachineTestAssertionEngine.getEngineFor(initialStatesToValidate)
//                .withSharedState(sharedState)
//                .initializeValidators();
//
//        //Assert the engine test returned isn't null
//        Assert.assertTrue("Returned Processing Engine shouldn't be null.", engineTest != null);
//        Assert.assertTrue("Ensure 1:1 mapping between initial state and validators", engineTest.GetValidators().size() == initialStatesToValidate.size());
//        //Validate the state against an ignition on event
//        HashMap<Integer, IHOSMessage> messageHashMap = new HashMap<>();
//        messageHashMap.put(0, HosMessages.EngineOnStopped(datetimeFormat.parse("2016.09.19 15:00")));
//        Set<StateValidityMappingBase> validatedEngineResults = engineTest.ValidateStateTransaction(messageHashMap).GetValidators();
//    }
//
//    @Test
//    public void test_StateTest_TestAllStates_NoEndState_WithMultipleMessages() throws Exception {
//        ArrayList<State> initialStatesToValidate = new ArrayList<>();
//        Date drivingDateTest = datetimeFormat.parse("2016.09.19 13:00");
//
//        State aobrdEngineOff = new AobrdEngineOff(sharedState);
//        initialStatesToValidate.add(aobrdEngineOff);
//        initialStatesToValidate.add(new AobrdStoppedNotDriving(sharedState));
//        initialStatesToValidate.add(new AobrdStoppedDriving(sharedState, drivingDateTest));
//        initialStatesToValidate.add(new AobrdMovingNotDriving(sharedState, 0));
//        initialStatesToValidate.add(new AobrdMovingDriving(sharedState));
//        initialStatesToValidate.add(new MandateEngineOffNotDriving(sharedState));
//        State mandateOffDriving = new MandateEngineOffDriving(sharedState, drivingDateTest);
//        initialStatesToValidate.add(mandateOffDriving);
//        initialStatesToValidate.add(new MandateStoppedNotDriving(sharedState));
//        initialStatesToValidate.add(new MandateStoppedDriving(sharedState, drivingDateTest));
//        initialStatesToValidate.add(new MandateMovingDriving(sharedState));
//        initialStatesToValidate.add(new MandateMovingNotDriving(sharedState));
//
//        HosProcessorStateMachineTestAssertionEngine engineTest = HosProcessorStateMachineTestAssertionEngine.getEngineFor(initialStatesToValidate)
//                .withSharedState(sharedState)
//                .initializeValidators();
//
//        //Assert the engine test returned isn't null
//        Assert.assertTrue("Returned Processing Engine shouldn't be null.", engineTest != null);
//        Assert.assertTrue("Ensure 1:1 mapping between initial state and validators", engineTest.GetValidators().size() == initialStatesToValidate.size());
//        //Validate the state against an ignition on event
//        HashMap<Integer, IHOSMessage> messageHashMap = new HashMap<>();
//        messageHashMap.put(0, HosMessages.EngineOnStopped(datetimeFormat.parse("2016.09.19 14:00")));
//        messageHashMap.put(1, HosMessages.EngineOnMoving(datetimeFormat.parse("2016.09.19 15:00")));
//        IHOSMessage eom = HosMessages.EngineOnStopped(datetimeFormat.parse("2016.09.19 25:00"));
//        messageHashMap.put(2, eom);
//        Set<StateValidityMappingBase> validatedEngineResults = engineTest.ValidateStateTransaction(messageHashMap).GetValidators();
//    }
}
