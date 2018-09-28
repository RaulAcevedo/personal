package com.jjkeller.kmbapi.geotab.implementation.engine.assertionEngine.validators;

import com.jjkeller.kmbapi.HosMessageProcessor.interfaces.IHOSMessage;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.MessageResult;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.MandateEngineOffDriving;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.MandateEngineOffNotDriving;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.MandateMovingDriving;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.MandateStoppedDriving;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.MandateStoppedNotDriving;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.State;
import com.jjkeller.kmbapi.geotab.abstraction.state.StateValidityMappingBase;
import com.jjkeller.kmbapi.geotab.implementation.enums.StepDirection;
import com.jjkeller.kmbapi.kmbeobr.EventTypeEnum;
import com.jjkeller.kmbapi.geotab.implementation.engine.assertionEngine.state.StateTransitionDefinition;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Implementation of StateValidityMappingBase for MandateStoppedDriving
 */
public class MandateStoppedDrivingValidator extends StateValidityMappingBase<MandateStoppedDriving> {

    public MandateStoppedDrivingValidator(MandateStoppedDriving startingStateInstance, Class<? extends State> endStateClass, HashMap testMappings, StateValidityMappingBase originatingValidator) throws Exception {
        super(startingStateInstance, endStateClass, testMappings, originatingValidator);
    }

    @Override
    public void assertAdditionalCriteriaValidity(MessageResult result, State stateTested, IHOSMessage messageTested) {

    }

    @Override
    public HashSet<StateTransitionDefinition> GetTransitionMappings() {
        //Build actual immediate relationship
        HashSet<StateTransitionDefinition> definitionMap = new HashSet<>();
        HashMap<Integer, EventTypeEnum> expectedRecordTypesForStoppedNotDriving = new HashMap<>();
        expectedRecordTypesForStoppedNotDriving.put(0, new EventTypeEnum(EventTypeEnum.DRIVEEND));

        HashMap<Integer, EventTypeEnum> expectedRecordTypesForMovingDriving = new HashMap<>();
        expectedRecordTypesForMovingDriving.put(0, new EventTypeEnum(EventTypeEnum.MOVE));

        HashMap<Integer, EventTypeEnum> expectedRecordTypesForEngineOff = new HashMap<>();
        expectedRecordTypesForEngineOff.put(0, new EventTypeEnum(EventTypeEnum.IGNITIONOFF));

        StateTransitionDefinition stoppedNotDrivingDef = new StateTransitionDefinition(StepDirection.OUTGOING, this, MandateStoppedNotDriving.class, expectedRecordTypesForStoppedNotDriving);
        StateTransitionDefinition engineOffDef = new StateTransitionDefinition(StepDirection.OUTGOING, this, MandateEngineOffNotDriving.class, expectedRecordTypesForEngineOff);
        StateTransitionDefinition movingDrivingDef = new StateTransitionDefinition(StepDirection.OUTGOING, this, MandateMovingDriving.class, expectedRecordTypesForMovingDriving);

        definitionMap.add(new StateTransitionDefinition(StepDirection.INCOMING, this, MandateMovingDriving.class, new HashMap<Integer, EventTypeEnum>(0)));
        definitionMap.add(new StateTransitionDefinition(StepDirection.INCOMING, this, MandateEngineOffDriving.class, new HashMap<Integer, EventTypeEnum>(0)));

        definitionMap.add(stoppedNotDrivingDef);
        definitionMap.add(engineOffDef);
        definitionMap.add(movingDrivingDef);

        return definitionMap;
    }
}
