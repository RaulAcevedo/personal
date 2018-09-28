package com.jjkeller.kmbapi.geotab.implementation.engine.assertionEngine.state;

import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.State;
import com.jjkeller.kmbapi.geotab.abstraction.state.StateValidityMappingBase;
import com.jjkeller.kmbapi.geotab.implementation.enums.StepDirection;
import com.jjkeller.kmbapi.kmbeobr.EventTypeEnum;

import java.util.HashMap;

/**
 * Internal protected class that defines the state we expect to transition to in both directions
 * (e.g. in AobrdMovingNotDriving, we can transition to Stopped Not Driving (DOWN),
 * EngineOff (DOWN), or Moving Driving (UP).
 */
public class StateTransitionDefinition {
    public final StepDirection RelationToParent;
    public final Class<? extends State> OutgoingState;
    public final Class<? extends State> IncomingState;
    public HashMap<Integer, EventTypeEnum> ExpectedEventTypeReturnCollectionForRelationship = new HashMap<>();

    /**
     * Full constructor allowing definition of an OUTGOING or OUTGOING definition, as
     * well as UP and DOWN
     *
     * @param direction                                        Enum representing the direction from current state this step exists in.
     *                                                         Down represents "closer" to Engine Off, Up represents "closer" to Moving
     *                                                         Driving.
     * @param transitionDefinitionParent                       StateValidityMappingBase object
     *                                                         that contains these definitions.
     *                                                         Used to get initial state instance.
     * @param expectedEventTypeReturnCollectionForRelationship Expected EventTypeEnum at a given
     *                                                         position
     */
    public StateTransitionDefinition(StepDirection direction, StateValidityMappingBase transitionDefinitionParent, Class<? extends State> incomingState, HashMap<Integer, EventTypeEnum> expectedEventTypeReturnCollectionForRelationship) {
        this.RelationToParent = direction;
        if (expectedEventTypeReturnCollectionForRelationship != null) {
            this.ExpectedEventTypeReturnCollectionForRelationship = expectedEventTypeReturnCollectionForRelationship;
        }
        switch (this.RelationToParent) {
            case INCOMING:
                this.OutgoingState = transitionDefinitionParent._initialStateInstance.getClass();
                this.IncomingState = incomingState;
                break;
            case OUTGOING:
                this.OutgoingState = incomingState;
                this.IncomingState = transitionDefinitionParent._initialStateInstance.getClass();
                break;
            default:
                throw new IllegalArgumentException("Provided relationship to parent is null or invalid.");
        }
    }
}