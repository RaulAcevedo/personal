package com.jjkeller.kmbapi.geotab.implementation.engine.assertionEngine.validators;

import com.jjkeller.kmbapi.HosMessageProcessor.interfaces.IHOSMessage;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.MessageResult;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.MandateEngineOffNotDriving;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.MandateMovingDriving;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.MandateMovingNotDriving;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.MandateStoppedDriving;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.MandateStoppedNotDriving;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.State;
import com.jjkeller.kmbapi.geotab.abstraction.state.StateValidityMappingBase;
import com.jjkeller.kmbapi.geotab.implementation.engine.assertionEngine.state.StateTransitionDefinition;
import com.jjkeller.kmbapi.geotab.implementation.enums.StepDirection;
import com.jjkeller.kmbapi.kmbeobr.EventTypeEnum;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Implementation of StateValidityMappingBase for MandateStoppedNotDriving
 */
public class MandateStoppedNotDrivingValidator extends StateValidityMappingBase<MandateStoppedNotDriving> {

    public MandateStoppedNotDrivingValidator(MandateStoppedNotDriving startingStateInstance, Class<? extends State> endStateClass, HashMap testMappings, StateValidityMappingBase originatingValidator) throws Exception {
        super(startingStateInstance, endStateClass, testMappings, originatingValidator);
    }

    @Override
    public void assertAdditionalCriteriaValidity(MessageResult result, State stateTested, IHOSMessage messageTested) {

    }

    @Override
    public HashSet<StateTransitionDefinition> GetTransitionMappings() {
        //Build actual immediate relationship
        HashSet<StateTransitionDefinition> definitionMap = new HashSet<>();
        HashMap<Integer, EventTypeEnum> expectedRecordTypesForMovingNotDriving = new HashMap<>();
        expectedRecordTypesForMovingNotDriving.put(0, new EventTypeEnum(EventTypeEnum.MOVE));

        HashMap<Integer, EventTypeEnum> expectedRecordTypesForMovingDriving = new HashMap<>();
        expectedRecordTypesForMovingNotDriving.put(0, new EventTypeEnum(EventTypeEnum.DRIVESTART));
        expectedRecordTypesForMovingNotDriving.put(1, new EventTypeEnum(EventTypeEnum.MOVE));

        HashMap<Integer, EventTypeEnum> expectedRecordTypesForEngineOff = new HashMap<>();
        expectedRecordTypesForEngineOff.put(0, new EventTypeEnum(EventTypeEnum.IGNITIONOFF));

        StateTransitionDefinition movingNotDrivingDef = new StateTransitionDefinition(StepDirection.OUTGOING, this, MandateMovingNotDriving.class, expectedRecordTypesForMovingNotDriving);
        StateTransitionDefinition movingDrivingDef = new StateTransitionDefinition(StepDirection.OUTGOING, this, MandateMovingDriving.class, expectedRecordTypesForMovingDriving);
        StateTransitionDefinition engineOffDef = new StateTransitionDefinition(StepDirection.OUTGOING, this, MandateEngineOffNotDriving.class, expectedRecordTypesForEngineOff);

        definitionMap.add(new StateTransitionDefinition(StepDirection.INCOMING, this, MandateMovingNotDriving.class, new HashMap<Integer, EventTypeEnum>(0)));
        definitionMap.add(new StateTransitionDefinition(StepDirection.INCOMING, this, MandateStoppedDriving.class, new HashMap<Integer, EventTypeEnum>(0)));
        definitionMap.add(new StateTransitionDefinition(StepDirection.INCOMING, this, MandateEngineOffNotDriving.class, new HashMap<Integer, EventTypeEnum>(0)));

        definitionMap.add(movingNotDrivingDef);
        definitionMap.add(engineOffDef);
        definitionMap.add(movingDrivingDef);

        return definitionMap;
    }
}
