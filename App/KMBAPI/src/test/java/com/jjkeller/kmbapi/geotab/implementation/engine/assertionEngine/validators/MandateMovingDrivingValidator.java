package com.jjkeller.kmbapi.geotab.implementation.engine.assertionEngine.validators;

import com.jjkeller.kmbapi.HosMessageProcessor.interfaces.IHOSMessage;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.MessageResult;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.MandateEngineOffDriving;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.MandateMovingDriving;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.MandateMovingNotDriving;
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
 * Implementation of StateValidityMappingBase for MandateMovingDriving
 */
public class MandateMovingDrivingValidator extends StateValidityMappingBase<MandateMovingDriving> {

    public MandateMovingDrivingValidator(MandateMovingDriving startingStateInstance, Class<? extends State> endStateClass, HashMap testMappings, StateValidityMappingBase originatingValidator) throws Exception {
        super(startingStateInstance, endStateClass, testMappings, originatingValidator);
    }

    @Override
    public void assertAdditionalCriteriaValidity(MessageResult result, State stateTested, IHOSMessage messageTested) {

    }

    @Override
    public HashSet<StateTransitionDefinition> GetTransitionMappings() {
        HashMap<Integer, EventTypeEnum> expectedRecordTypesForStoppedDriving = new HashMap<>();
        expectedRecordTypesForStoppedDriving.put(0, new EventTypeEnum(EventTypeEnum.VEHICLESTOPPED));

        HashMap<Integer, EventTypeEnum> expectedRecordTypesForEngineOff = new HashMap<>();
        expectedRecordTypesForEngineOff.put(0, new EventTypeEnum(EventTypeEnum.IGNITIONOFF));
        expectedRecordTypesForEngineOff.put(1, new EventTypeEnum(EventTypeEnum.VEHICLESTOPPED));

        //Build actual immediate relationship
        HashSet<StateTransitionDefinition> definitionMap = new HashSet<>();

        StateTransitionDefinition stoppedDrivingDef = new StateTransitionDefinition(StepDirection.OUTGOING, this, MandateStoppedDriving.class, expectedRecordTypesForStoppedDriving);
        StateTransitionDefinition engineOffDef = new StateTransitionDefinition(StepDirection.OUTGOING, this, MandateEngineOffDriving.class, expectedRecordTypesForEngineOff);

        definitionMap.add(new StateTransitionDefinition(StepDirection.INCOMING, this, MandateStoppedNotDriving.class, new HashMap<Integer, EventTypeEnum>(0)));
        definitionMap.add(new StateTransitionDefinition(StepDirection.INCOMING, this, MandateMovingNotDriving.class, new HashMap<Integer, EventTypeEnum>(0)));
        definitionMap.add(new StateTransitionDefinition(StepDirection.INCOMING, this, MandateStoppedDriving.class, new HashMap<Integer, EventTypeEnum>(0)));

        definitionMap.add(stoppedDrivingDef);
        definitionMap.add(engineOffDef);

        return definitionMap;
    }
}
