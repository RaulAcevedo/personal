package com.jjkeller.kmbapi.geotab.implementation.engine.assertionEngine.validators;

import com.jjkeller.kmbapi.HosMessageProcessor.interfaces.IHOSMessage;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.MessageResult;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.MandateEngineOffDriving;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.MandateEngineOffNotDriving;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.MandateMovingNotDriving;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.MandateStoppedNotDriving;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.State;
import com.jjkeller.kmbapi.geotab.abstraction.state.StateValidityMappingBase;
import com.jjkeller.kmbapi.geotab.implementation.engine.assertionEngine.state.StateTransitionDefinition;
import com.jjkeller.kmbapi.geotab.implementation.enums.StepDirection;
import com.jjkeller.kmbapi.kmbeobr.EventTypeEnum;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Implementation of StateValidityMappingBase for MandateEngineOffNotDriving
 */
public class MandateEngineOffNotDrivingValidator extends StateValidityMappingBase<MandateEngineOffNotDriving> {

    public MandateEngineOffNotDrivingValidator(MandateEngineOffNotDriving startingStateInstance, Class<? extends State> endStateClass, HashMap testMappings, StateValidityMappingBase originatingValidator) throws Exception {
        super(startingStateInstance, endStateClass, testMappings, originatingValidator);
    }

    @Override
    public void assertAdditionalCriteriaValidity(MessageResult result, State stateTested, IHOSMessage messageTested) {

    }

    @Override
    public HashSet<StateTransitionDefinition> GetTransitionMappings() {
        HashMap<Integer, EventTypeEnum> expectedRecordTypes = new HashMap<>();
        expectedRecordTypes.put(0, new EventTypeEnum(EventTypeEnum.IGNITIONON));

        HashSet<StateTransitionDefinition> definitionMap = new HashSet<>();
        //Build actual immediate relationship
        StateTransitionDefinition stoppedNotDriveDef = new StateTransitionDefinition(StepDirection.OUTGOING, this, MandateStoppedNotDriving.class, expectedRecordTypes);

        definitionMap.add(new StateTransitionDefinition(StepDirection.INCOMING, this, MandateStoppedNotDriving.class, new HashMap<Integer, EventTypeEnum>(0)));
        definitionMap.add(new StateTransitionDefinition(StepDirection.INCOMING, this, MandateMovingNotDriving.class, new HashMap<Integer, EventTypeEnum>(0)));
        definitionMap.add(new StateTransitionDefinition(StepDirection.INCOMING, this, MandateEngineOffDriving.class, new HashMap<Integer, EventTypeEnum>(0)));

        definitionMap.add(stoppedNotDriveDef);

        return definitionMap;
    }
}
