package com.jjkeller.kmbapi.geotab.implementation.engine.assertionEngine.validators;

import com.jjkeller.kmbapi.HosMessageProcessor.interfaces.IHOSMessage;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.MessageResult;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.AobrdEngineOff;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.AobrdMovingDriving;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.AobrdStoppedDriving;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.AobrdStoppedNotDriving;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.State;
import com.jjkeller.kmbapi.geotab.abstraction.state.StateValidityMappingBase;
import com.jjkeller.kmbapi.geotab.implementation.enums.StepDirection;
import com.jjkeller.kmbapi.kmbeobr.EventTypeEnum;
import com.jjkeller.kmbapi.geotab.implementation.engine.assertionEngine.state.StateTransitionDefinition;

import java.util.HashMap;
import java.util.HashSet;

/**
 * ValidityMappingBase implementation for AobrdStoppedDrivingValidator
 */
public class AobrdStoppedDrivingValidator extends StateValidityMappingBase<AobrdStoppedDriving> {

    public AobrdStoppedDrivingValidator(AobrdStoppedDriving startingStateInstance, Class<? extends State> endStateClass, HashMap testMappings, StateValidityMappingBase originatingValidator) throws Exception {
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
        expectedRecordTypesForEngineOff.put(1, new EventTypeEnum(EventTypeEnum.DRIVEEND));

        StateTransitionDefinition stoppedNotDrivingDef = new StateTransitionDefinition(StepDirection.OUTGOING, this, AobrdStoppedNotDriving.class, expectedRecordTypesForStoppedNotDriving);
        StateTransitionDefinition engineOffDef = new StateTransitionDefinition(StepDirection.OUTGOING, this, AobrdEngineOff.class, expectedRecordTypesForEngineOff);
        StateTransitionDefinition movingDrivingDef = new StateTransitionDefinition(StepDirection.OUTGOING, this, AobrdMovingDriving.class, expectedRecordTypesForMovingDriving);

        definitionMap.add(new StateTransitionDefinition(StepDirection.INCOMING, this, AobrdMovingDriving.class, new HashMap<Integer, EventTypeEnum>(0)));

        definitionMap.add(stoppedNotDrivingDef);
        definitionMap.add(engineOffDef);
        definitionMap.add(movingDrivingDef);

        return definitionMap;
    }
}
