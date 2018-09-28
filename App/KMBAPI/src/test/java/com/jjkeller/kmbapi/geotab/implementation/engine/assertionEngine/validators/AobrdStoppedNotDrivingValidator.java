package com.jjkeller.kmbapi.geotab.implementation.engine.assertionEngine.validators;

import com.jjkeller.kmbapi.HosMessageProcessor.interfaces.IHOSMessage;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.MessageResult;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.AobrdEngineOff;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.AobrdMovingNotDriving;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.AobrdStoppedDriving;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.AobrdStoppedNotDriving;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.State;
import com.jjkeller.kmbapi.kmbeobr.EventTypeEnum;
import com.jjkeller.kmbapi.geotab.abstraction.state.StateValidityMappingBase;
import com.jjkeller.kmbapi.geotab.implementation.engine.assertionEngine.state.StateTransitionDefinition;
import com.jjkeller.kmbapi.geotab.implementation.enums.StepDirection;

import java.util.HashMap;
import java.util.HashSet;

/**
 * ValidityMappingBase implementation for AobrdStoppedNotDriving
 */
public class AobrdStoppedNotDrivingValidator extends StateValidityMappingBase<AobrdStoppedNotDriving> {

    public AobrdStoppedNotDrivingValidator(AobrdStoppedNotDriving startingStateInstance, Class<? extends State> endStateClass, HashMap testMappings, StateValidityMappingBase originatingValidator) throws Exception {
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

        HashMap<Integer, EventTypeEnum> expectedRecordTypesForEngineOff = new HashMap<>();
        expectedRecordTypesForEngineOff.put(0, new EventTypeEnum(EventTypeEnum.IGNITIONOFF));

        StateTransitionDefinition movingNotDrivingDef = new StateTransitionDefinition(StepDirection.OUTGOING, this, AobrdMovingNotDriving.class, expectedRecordTypesForMovingNotDriving);
        StateTransitionDefinition engineOffDef = new StateTransitionDefinition(StepDirection.OUTGOING, this, AobrdEngineOff.class, expectedRecordTypesForEngineOff);

        definitionMap.add(new StateTransitionDefinition(StepDirection.INCOMING, this, AobrdMovingNotDriving.class, new HashMap<Integer, EventTypeEnum>(0)));
        definitionMap.add(new StateTransitionDefinition(StepDirection.INCOMING, this, AobrdStoppedDriving.class, new HashMap<Integer, EventTypeEnum>(0)));

        definitionMap.add(movingNotDrivingDef);
        definitionMap.add(engineOffDef);

        return definitionMap;
    }
}