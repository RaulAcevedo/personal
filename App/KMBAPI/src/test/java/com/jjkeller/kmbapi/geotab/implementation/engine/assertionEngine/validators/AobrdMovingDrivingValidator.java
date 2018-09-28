package com.jjkeller.kmbapi.geotab.implementation.engine.assertionEngine.validators;

import com.jjkeller.kmbapi.HosMessageProcessor.interfaces.IHOSMessage;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.MessageResult;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.AobrdEngineOff;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.AobrdMovingDriving;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.AobrdMovingNotDriving;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.AobrdStoppedDriving;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.State;
import com.jjkeller.kmbapi.geotab.abstraction.state.StateValidityMappingBase;
import com.jjkeller.kmbapi.geotab.implementation.engine.assertionEngine.state.StateTransitionDefinition;
import com.jjkeller.kmbapi.geotab.implementation.enums.StepDirection;
import com.jjkeller.kmbapi.kmbeobr.EventTypeEnum;

import java.util.HashMap;
import java.util.HashSet;

/**
 * ValidityMappingBase implementation for AobrdMovingDriving
 */
public class AobrdMovingDrivingValidator extends StateValidityMappingBase<AobrdMovingDriving> {

    public AobrdMovingDrivingValidator(AobrdMovingDriving startingStateInstance, Class<? extends State> endStateClass, HashMap testMappings, StateValidityMappingBase originatingValidator) throws Exception {
        super(startingStateInstance, endStateClass, testMappings, originatingValidator);
    }

    @Override
    public void assertAdditionalCriteriaValidity(MessageResult result, State stateTested, IHOSMessage messageTested) {

    }

    @Override
    public HashSet<StateTransitionDefinition> GetTransitionMappings() {
        HashMap<Integer, EventTypeEnum> expectedRecordTypesForStoppedDriving = new HashMap<>();
        expectedRecordTypesForStoppedDriving.put(0,new EventTypeEnum(EventTypeEnum.VEHICLESTOPPED));

        HashMap<Integer, EventTypeEnum> expectedRecordTypesForEngineOff = new HashMap<>();
        expectedRecordTypesForEngineOff.put(0, new EventTypeEnum(EventTypeEnum.IGNITIONOFF));
        expectedRecordTypesForEngineOff.put(1, new EventTypeEnum(EventTypeEnum.VEHICLESTOPPED));
        expectedRecordTypesForEngineOff.put(2,new EventTypeEnum(EventTypeEnum.DRIVEEND));

        //Build actual immediate relationship
        HashSet<StateTransitionDefinition> definitionMap = new HashSet<>();

        StateTransitionDefinition stoppedDrivingDef = new StateTransitionDefinition(StepDirection.OUTGOING, this, AobrdStoppedDriving.class, expectedRecordTypesForStoppedDriving);
        StateTransitionDefinition engineOffDef = new StateTransitionDefinition(StepDirection.OUTGOING, this, AobrdEngineOff.class, expectedRecordTypesForEngineOff);

        definitionMap.add(new StateTransitionDefinition(StepDirection.INCOMING, this, AobrdMovingNotDriving.class, new HashMap<Integer, EventTypeEnum>(0)));
        definitionMap.add(new StateTransitionDefinition(StepDirection.INCOMING, this, AobrdStoppedDriving.class, new HashMap<Integer, EventTypeEnum>(0)));

        definitionMap.add(stoppedDrivingDef);
        definitionMap.add(engineOffDef);

        return definitionMap;
    }
}
