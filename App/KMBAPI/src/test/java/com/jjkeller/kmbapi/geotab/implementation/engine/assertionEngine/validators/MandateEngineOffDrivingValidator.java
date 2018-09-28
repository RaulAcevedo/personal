package com.jjkeller.kmbapi.geotab.implementation.engine.assertionEngine.validators;

import com.jjkeller.kmbapi.HosMessageProcessor.interfaces.IHOSMessage;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.MessageResult;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.MandateEngineOffDriving;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.MandateEngineOffNotDriving;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.MandateMovingDriving;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.MandateStoppedDriving;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.State;
import com.jjkeller.kmbapi.geotab.abstraction.state.StateValidityMappingBase;
import com.jjkeller.kmbapi.geotab.implementation.enums.StepDirection;
import com.jjkeller.kmbapi.kmbeobr.EventTypeEnum;
import com.jjkeller.kmbapi.geotab.implementation.engine.assertionEngine.state.StateTransitionDefinition;

import java.util.HashMap;
import java.util.HashSet;


/**
 * Implementation of StateValidityMappingBase for MandateEngineOffDriving
 */
public class MandateEngineOffDrivingValidator extends StateValidityMappingBase<MandateEngineOffDriving> {

    public MandateEngineOffDrivingValidator(MandateEngineOffDriving startingStateInstance, Class<? extends State> endStateClass, HashMap testMappings, StateValidityMappingBase originatingValidator) throws Exception {
        super(startingStateInstance, endStateClass, testMappings, originatingValidator);
    }

    @Override
    public void assertAdditionalCriteriaValidity(MessageResult result, State stateTested, IHOSMessage messageTested) {

    }

    @Override
    public HashSet<StateTransitionDefinition> GetTransitionMappings() {
        HashMap<Integer, EventTypeEnum> expectedRecordTypesforStoppedDriving = new HashMap<>();
        expectedRecordTypesforStoppedDriving.put(0, new EventTypeEnum(EventTypeEnum.IGNITIONON));

        HashMap<Integer, EventTypeEnum> expectedRecordTypesForEngineOffNotDriving = new HashMap<>();
        expectedRecordTypesForEngineOffNotDriving.put(0, new EventTypeEnum(EventTypeEnum.DRIVEEND));

        HashSet<StateTransitionDefinition> definitionMap = new HashSet<>();
        //Build actual immediate relationship
        StateTransitionDefinition stoppedNotDriveDef = new StateTransitionDefinition(StepDirection.OUTGOING, this, MandateStoppedDriving.class, expectedRecordTypesforStoppedDriving);
        StateTransitionDefinition engineOffNotDriving = new StateTransitionDefinition(StepDirection.OUTGOING, this, MandateEngineOffNotDriving.class, expectedRecordTypesForEngineOffNotDriving);

        definitionMap.add(new StateTransitionDefinition(StepDirection.INCOMING, this, MandateStoppedDriving.class, new HashMap<Integer, EventTypeEnum>(0)));
        definitionMap.add(new StateTransitionDefinition(StepDirection.INCOMING, this, MandateMovingDriving.class, new HashMap<Integer, EventTypeEnum>(0)));

        definitionMap.add(stoppedNotDriveDef);
        definitionMap.add(engineOffNotDriving);
        return definitionMap;
    }
}
