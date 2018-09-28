package com.jjkeller.kmbapi.geotab.abstraction.state;


import com.jjkeller.kmbapi.HosMessageProcessor.interfaces.IHOSMessage;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.MessageResult;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.State;
import com.jjkeller.kmbapi.geotab.implementation.enums.AssertStatusDefinition;
import com.jjkeller.kmbapi.geotab.implementation.enums.StepDirection;
import com.jjkeller.kmbapi.kmbeobr.EventRecord;
import com.jjkeller.kmbapi.kmbeobr.EventTypeEnum;
import com.jjkeller.kmbapi.geotab.implementation.engine.assertionEngine.state.StateTransitionDefinition;

import junit.framework.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Abstract class that provides a mapping between a state and the expected
 * {@code ArrayList<EventRecord>} return value, and expected State return value
 * (what state we will end up in).
 * @param <InitialState> State object type defining what state we are starting in.
 */
public abstract class StateValidityMappingBase<InitialState extends State> {


    /**
     * Field representing the initial State the processing engine is in for a given test
     */
    public final State _initialStateInstance;
    /**
     * Field representing sub validation steps that occur if the desired end state is not provided
     */
    protected final HashMap<Integer, StateValidityMappingBase> _runSubValidators = new HashMap<>();
    /**
     * Field that determines with Validator originated this request
     */
    private final StateValidityMappingBase _originatingValidatorClassReference;
    /**
     * Field that represents the message results from processing
     */
    private final ArrayList<MessageResult> _returnedMessagesForValidator = new ArrayList<>();
    /**
     * Field representing the next validator to run in the map of sub validators
     */
    protected StateValidityMappingBase _nextValidator = null;
    /**
     * Field that represents the current direction (Incoming or Outgoing) of this validator
     */
    private StepDirection _currentStepDirection;

    /**
     * Field representing the expected event type return collection
     */
    private HashMap<Integer, EventTypeEnum> _expectedEventTypeReturnCollection = new HashMap<>();
    /**
     * Field representing the class of State that should be returned by processor.
     */
    private Class _desiredEndStateClassInstance;
    /**
     * Field that holds the available states we can immediately move to from current state.
     */
    private HashSet<StateTransitionDefinition> _availableStatesForTransition;
    /**
     * Field that represents a mapping between a given state and its validator
     */
    private HashMap _testMappings;

    /**
     * Field that represents a validator that can defer execution.
     */
    private boolean _shouldDeferValidation;

    /**
     * Field that represents the current message being processed
     */
    private IHOSMessage _messageBeingProcessed;

    /**
     * Constructor for StateValidityMappingBase
     *
     * @param startingStateInstance State object representing the starting state of validation
     * @param endStateClass Class representing the state we expect to end at
     * @param testMappings HashMap representing the mapping between states and validators
     * @param originatingValidator StateValidityMappingBase object representing the validator
     *                             that spawned this (null if primary)
     * @throws Exception
     */
    protected StateValidityMappingBase(InitialState startingStateInstance, Class<? extends State> endStateClass, HashMap testMappings, StateValidityMappingBase originatingValidator) throws Exception {
        boolean invalidConstructor = false;
        String assertFailMessage = "";
        /*
        If the initial state argument is null,
        we can't determine initial values so throw an exception
        */
        if (startingStateInstance == null) {
            invalidConstructor = true;
            assertFailMessage = "Provided parameter for starting state object is null.";
        }

        if (invalidConstructor)
            Assert.fail(assertFailMessage);

        //Set internal state object for initial state
        this._initialStateInstance = startingStateInstance;
        //Set desired end state class
        this._desiredEndStateClassInstance = endStateClass;
        //Set up transitional info
        this._availableStatesForTransition = this.GetTransitionMappings();
        //Set originating validator class
        this._originatingValidatorClassReference = originatingValidator;
        //Set step direction if this is a sub validator
        if (this._originatingValidatorClassReference != null)
            this._currentStepDirection = this._originatingValidatorClassReference._currentStepDirection;
        //Set the mappings between state and validator
        this._testMappings = testMappings;
    }

    /**
     * Public static method that returns a new validator for a given initial state.
     *
     * @param initialState              State that the validator will start it's message processing with
     * @param desiredEndState           Optional end state denoting an expected stopping state given provided
     *                                  messages
     * @param testMappings              HashMap consisting of a mapping between State and associated validator
     * @param originatingValidatorClass StateValidityMappingBase object that is the parent of this
     *                                  validator (sub validation process)
     * @return StateValidityMappingBase for the given initial state
     * @throws Exception
     */
    public static StateValidityMappingBase<State> getStateValidatorFor(State initialState, Class<? extends State> desiredEndState, HashMap<Class<? extends State>, Class<? extends StateValidityMappingBase>> testMappings, StateValidityMappingBase originatingValidatorClass) throws Exception {
                /*
        If the validity map doesn't have an entry for this state, we can't continue.
        */
        String assertFailMessage = null;
        if (testMappings == null) {
            assertFailMessage = "state:test HashMap is null, cannot determine match to corresponding test.";
        } else if (!testMappings.containsKey(initialState.getClass())) {
            assertFailMessage = "State: " + initialState.getClass() + " provided for initialState argument does not have a " +
                    "corresponding StateValidityMappingBase object," +
                    "or the object has not been added to InitializeValidityMappings().";
        }

        if (assertFailMessage != null) {
            Assert.fail(assertFailMessage);
        }
                /*
        Construct a concrete object for selected initialState's StateValidityMappingBase
         */
        Class<StateValidityMappingBase<State>> stateValidityEntry = (Class<StateValidityMappingBase<State>>) testMappings.get(initialState.getClass());
        /*
        This can throw an error if the derived class hasn't properly implemented the constructor
        logic from the base class. In that case, we want to expose that problem
         */

        return stateValidityEntry
                .getConstructor(initialState.getClass(), Class.class, HashMap.class, StateValidityMappingBase.class)
                .newInstance(initialState, desiredEndState, testMappings, originatingValidatorClass);
    }

    /**
     * Method that returns the list of message results created when validating
     *
     * @return ArrayList of message results
     */
    public ArrayList<MessageResult> GetMessageResults() {
        return this._returnedMessagesForValidator;
    }

    /**
     * Method that returns the HashMap of sub validators run during this process
     *
     * @return HashMap of sub validators
     */
    public HashMap<Integer, StateValidityMappingBase> GetSubValidators() {
        return this._runSubValidators;
    }

    /**
     * Public method that validates a message against the current validator
     * @param message IHOSMessage to validateAgainstCurrentScenario against current state
     * @param isMultiMessage Boolean denoting the message is the last message passed to validator
     * @return Returns a StateValidityMappingBase object that has had validation performed
     * @throws Exception
     */
    public StateValidityMappingBase Validate(IHOSMessage message, boolean isMultiMessage, boolean isLastMessage) throws Exception {

        /*
        We can validateAgainstCurrentScenario if:
        This is not a multi-message validation
        This IS a multi message validation, but this is the last message in the set
         */
        this._shouldDeferValidation = !isMultiMessage || !isLastMessage;
        //Set the message we're processing
        this._messageBeingProcessed = message;
        StateValidityMappingBase currentValidator = this;
        //Process this validator
                /*Determine intermediate states, and set expected values if needed
        If we DON'T find a validator match, we will need to:
        process the message result in the validateForProvidedMessage() call
         */
        currentValidator = currentValidator.validateForProvidedMessage(currentValidator, currentValidator.determineValidatorParameterDefinition());
        int subValidatorCount = 0;
        while (currentValidator._nextValidator != null) {
            currentValidator = currentValidator._nextValidator.Validate(message, isMultiMessage, isLastMessage);

            this._runSubValidators
                    .put(subValidatorCount, currentValidator);
            subValidatorCount++;
        }
        //When done, null out _nextValidator
        this._nextValidator = null;
        return this;
    }


    public StateValidityMappingBase WithDesiredEndState(Class<? extends State> endstate) throws Exception {
        if (endstate == null)
            throw new IllegalArgumentException("End State can not be null for WithDesiredEndState() call.");
        return getStateValidatorFor(this._initialStateInstance, endstate, this._testMappings, this._originatingValidatorClassReference);
    }

    /**
     * Method that validates a validator against a given IHOSMessage
     *
     * @param validator Validator used to process message
     * @param existingDefinition Boolean representing if we have determined a desired end state
     *                              before validation.
     * @return Returns a StateValidityMappingBase that has had a message validated against it's
     * state
     * @throws Exception
     */
    private StateValidityMappingBase validateForProvidedMessage(StateValidityMappingBase<? extends State> validator, StateTransitionDefinition existingDefinition) throws Exception {
        //Create message result from provided message
        MessageResult messageResult = validator._initialStateInstance.processMessage(validator._messageBeingProcessed);
        //Add the above to the collection
        validator._returnedMessagesForValidator.add(messageResult);
        StateTransitionDefinition foundDefinition = existingDefinition;

        /*If we don't yet have an end state determined, we expect to validateAgainstCurrentScenario,
        and the returned state isn't null, use it as the desired end state.
        We will call determineValidatorParameterDefinition() once more
        based on that. If nothing is returned, we have an invalid result and need to fail.
        */

        boolean validatorDefinitionIsInvalid = foundDefinition == null &&
                validator._desiredEndStateClassInstance != null &&
                validator._desiredEndStateClassInstance != validator._initialStateInstance.getClass();
        //If the above didn't find a match, the desired end state provided is wrong.
        if (!validator._shouldDeferValidation && validatorDefinitionIsInvalid) {
            //Fail
            StringBuilder definitionNotValidSB = this.assembleAssertMessage(null, AssertStatusDefinition.BASE_INFO, messageResult, validator._messageBeingProcessed, validator);
            Assert.fail(this.assembleAssertMessage(definitionNotValidSB, AssertStatusDefinition.VALIDATOR_DEFINITION_NOT_FOUND, messageResult, validator._messageBeingProcessed, validator).toString());
        }
        //Set validation parameters
        if (foundDefinition != null && !validator._shouldDeferValidation) {
            validator._desiredEndStateClassInstance = foundDefinition.OutgoingState;
            validator._expectedEventTypeReturnCollection = foundDefinition.ExpectedEventTypeReturnCollectionForRelationship;
            if (validator._currentStepDirection == null)
                validator._currentStepDirection = foundDefinition.RelationToParent;

            if (!validator._shouldDeferValidation) {
                validator.assertResultStateValidity(messageResult);
                //Assert EventRecords are valid
                validator.assertEventRecordValidity(messageResult);
                //Call method to allow optional special case test logic to fire
                validator.assertAdditionalCriteriaValidity(messageResult, validator._initialStateInstance, validator._messageBeingProcessed);
            }
        }

        //State returned is null, blow up next validator
        if (messageResult.getNewState() == null) {
            validator._nextValidator = null;
        } else {
            //Got a new state back, set it to the nextValidator, and return.
            //Upon next processing, we will process the message against all sub validators down the chain
            //We won't provide a desired end state, as we will determine that in the creation of the object
            validator._nextValidator = StateValidityMappingBase.getStateValidatorFor(messageResult.getNewState(), validator._shouldDeferValidation ? validator._desiredEndStateClassInstance : null, validator._testMappings, validator);
        }
        return validator;
    }


    /**
     * Method that determines if any intermediate steps are required to go from initial state to
     * desired state, and adds those to the _stateValidationSteps field.
     * @return Boolean representing if a match was found
     *
     */
    private StateTransitionDefinition determineValidatorParameterDefinition() throws Exception {
        if (this._availableStatesForTransition == null)
            Assert.fail("No State Transition information was found, can not determine state position.");
            /*
            Iterate over collection of potential non-immediate targets and add a validator for each.
            If the desired end state isn't immediately reachable from the current one, use the
            direction info and test the MAX state in that direction (e.g. EngineOff for DOWN and MovingDriving for UP)
             */
        HashSet<StateTransitionDefinition> outgoingTransitionDefinitions = new HashSet<>();
        HashSet<StateTransitionDefinition> incomingTransitions = new HashSet<>();
        //This represents the case where a user specifies a desired end state, but we don't find a
        //corresponding entry in our mappings. In this case, we need to fail this test
        StateTransitionDefinition definitionMatch = null;
        transitionDefLoop:
        for (StateTransitionDefinition transitionDefinition : this._availableStatesForTransition) {
            //If we aren't going the same direction, skip
            if (transitionDefinition.RelationToParent == StepDirection.INCOMING) {
                continue transitionDefLoop;
            } else if (this._desiredEndStateClassInstance == transitionDefinition.OutgoingState) {
                definitionMatch = transitionDefinition;
                break transitionDefLoop;
            }
            outgoingTransitionDefinitions.add(transitionDefinition);
        }
        return definitionMatch;
    }

    /**
     * Method that asserts the state returned from processing is the type we provided
     * for DesiredEndState
     *
     * @param result MessageResult generated by the processMessage() method of a State object.
     */
    private void assertResultStateValidity(MessageResult result) {
        if (result == null) {
            Assert.fail("MessageResult is null.");
        } else {
            StringBuilder assertStringBuilder = this.assembleAssertMessage(null, AssertStatusDefinition.BASE_INFO, result, this._messageBeingProcessed, this);


            assertStringBuilder.append("\n");
            boolean assertConditionResult = true, isFailure = false;
            //We have a desired end state, and that state isn't the same as what generated this validator, verify transition
            if (this._desiredEndStateClassInstance != null) {
                if (this._desiredEndStateClassInstance == this._initialStateInstance.getClass()) {
                    assertStringBuilder.append("Desired End State is the same as Initial Start State. Assuming round trip, assert no state change occurred and no events were returned");
                    assertConditionResult = result.getEventRecords().size() == 0 && result.getNewState() == null;
                    if (!assertConditionResult) {
                        this.assembleAssertMessage(assertStringBuilder, AssertStatusDefinition.EXPECTED_NO_RESULT_FOUND_RESULT, result, this._messageBeingProcessed, this);
                    }
                } else if (result.getNewState() == null && this._desiredEndStateClassInstance != null) {
                    this.assembleAssertMessage(assertStringBuilder, AssertStatusDefinition.EXPECTED_RESULT_FOUND_NONE, result, this._messageBeingProcessed, this);
                    isFailure = true;
                } else {
                    assertStringBuilder.append("Expected state should be: " + this._desiredEndStateClassInstance.getName());
                    assertStringBuilder.append("\n");
                    assertStringBuilder.append("Message result state: " + result.getNewState().toString());
                    assertConditionResult = result.getNewState().getClass() == this._desiredEndStateClassInstance;
                }
            } else {
                assertStringBuilder.append("Desired End State is null. Assuming message was not processed by current state, assert no state change occurred and no events were returned.");
                assertStringBuilder.append("\n");
                assertConditionResult = result.getEventRecords().size() == 0 && result.getNewState() == null;
                if (!assertConditionResult) {
                    this.assembleAssertMessage(assertStringBuilder, AssertStatusDefinition.EXPECTED_NO_RESULT_FOUND_RESULT, result, this._messageBeingProcessed, this);
                }
            }
            if (isFailure) {
                Assert.fail(assertStringBuilder.toString());
            } else {
                Assert.assertTrue(assertStringBuilder.toString(), assertConditionResult);
            }
        }
    }

    /**
     * Method that asserts the returned status records from processing conform to the situation
     * in question.
     *
     * @param result MessageResult generated by the processMessage() method of a State object.
     */
    private void assertEventRecordValidity(MessageResult result) {
        if (this._expectedEventTypeReturnCollection == null)
            Assert.fail("Expected Event Return Type collection is null. Cannot assert record validity.");

        StringBuilder assertStringBuilder = this.assembleAssertMessage(null, AssertStatusDefinition.BASE_INFO, result, this._messageBeingProcessed, this);
        /*Iterate through returned records and compare to what we expect to get from the expected
        event return type collection
         */
        ArrayList<EventRecord> resultantEventRecords = result.getEventRecords();
        for (int recordIndex = 0; recordIndex < this._expectedEventTypeReturnCollection.size(); recordIndex++) {
            EventTypeEnum testType = this._expectedEventTypeReturnCollection.get(recordIndex);
            EventRecord resultantRecord = resultantEventRecords.indexOf(recordIndex) >= 0 ? resultantEventRecords.get(recordIndex) : null;
            if (resultantRecord == null) {
                this.assembleAssertMessage(assertStringBuilder, AssertStatusDefinition.EXPECTED_END_EVENTRECORD_MISMATCH, result, this._messageBeingProcessed, this);
                Assert.fail(assertStringBuilder.toString());
            } else {
                assertStringBuilder.append("Asserting that event record returned at position: " + recordIndex + " matches intended record: " + testType.toDMOEnum() + " for validator: " + this.getClass().getName());
            }
            //Determine if the event record at a given position matches what we expect from our return collection
            Assert.assertTrue(assertStringBuilder.toString(),
                    resultantEventRecords.get(recordIndex).getEventType() == testType.getValue());
        }
    }

    public StringBuilder assembleAssertMessage(StringBuilder providedSB, AssertStatusDefinition assertStatusDefinition, MessageResult result, IHOSMessage message, StateValidityMappingBase validator) {
        StringBuilder assertStringBuilder = providedSB == null ? new StringBuilder() : providedSB;

        switch (assertStatusDefinition) {
            case BASE_INFO:
                assertStringBuilder.append("\n");
                assertStringBuilder.append("For initial state: " + validator._initialStateInstance.getClass().getName());
                assertStringBuilder.append("\n");
                assertStringBuilder.append("In validator: " + validator.getClass().getName());
                assertStringBuilder.append("\n");
                if (validator._desiredEndStateClassInstance != null)
                    assertStringBuilder.append("With provided State: ").append(validator._desiredEndStateClassInstance.getName());
                else
                    assertStringBuilder.append("Provided State is null.");
                assertStringBuilder.append("\n");
                //Append validator structure
                StateValidityMappingBase parentValidator = validator._originatingValidatorClassReference;
                String parentValidatorHeading = "With Parent validator: ";
                while (parentValidator != null) {
                    assertStringBuilder.append(parentValidatorHeading);
                    assertStringBuilder.append(parentValidator.getClass().getName());
                    parentValidator = parentValidator._originatingValidatorClassReference;
                    if (parentValidator != null) {
                        assertStringBuilder.append("\n");
                        assertStringBuilder.append("Multi-level validation detected. Displaying validator stack:");
                        assertStringBuilder.append("\n");
                        parentValidatorHeading = "Sub Validator: ";
                    }
                }
                break;
            case VALIDATOR_DEFINITION_NOT_FOUND:
                assertStringBuilder.append("\n");
                assertStringBuilder.append("The desired end state class provided to the validator was not found in the mappings for this State.");
                assertStringBuilder.append("\n");
                assertStringBuilder.append("Available State Targets: ");
                assertStringBuilder.append("\n");
                for (Object availableDef : validator._availableStatesForTransition) {
                    if (((StateTransitionDefinition) availableDef).RelationToParent == StepDirection.INCOMING)
                        continue;
                    assertStringBuilder.append(((StateTransitionDefinition) availableDef).OutgoingState.getName());
                    assertStringBuilder.append("\n");

                }
                break;
            case EXPECTED_NO_RESULT_FOUND_RESULT:
                assertStringBuilder.append("MessageResult state returned: " + result.getNewState());
                assertStringBuilder.append("\n");
                for (EventRecord eventRecord : result.getEventRecords()) {
                    assertStringBuilder.append("MessageResult EventRecord #");
                    assertStringBuilder.append(result.getEventRecords().indexOf(eventRecord) + ": ");
                    assertStringBuilder.append(eventRecord.toString());
                    assertStringBuilder.append("\n");
                }
                break;
            case EXPECTED_RESULT_FOUND_NONE:
                assertStringBuilder.append("Expected a return state/EventRecord collection from processing, and found null.");
                assertStringBuilder.append("\n");
                assertStringBuilder.append("Expected return state: " + this._desiredEndStateClassInstance.getName());
                assertStringBuilder.append("\n");
                break;
            case EXPECTED_END_EVENTRECORD_MISMATCH:
                assertStringBuilder.append("Mismatch between expected returned eventRecords and actual.");
                assertStringBuilder.append("\n");
                assertStringBuilder.append("Expected EventTypeEnums: ");
                assertStringBuilder.append("\n");
                for (Map.Entry expectedRecord : this._expectedEventTypeReturnCollection.entrySet()) {
                    assertStringBuilder.append(((EventTypeEnum) expectedRecord.getValue()).toDMOEnum());
                    assertStringBuilder.append("\n");
                }
                assertStringBuilder.append("\n");
                assertStringBuilder.append("MessageResult state returned: " + result.getNewState());
                assertStringBuilder.append("\n");
                for (EventRecord eventRecord : result.getEventRecords()) {
                    assertStringBuilder.append("MessageResult EventRecord #");
                    assertStringBuilder.append(result.getEventRecords().indexOf(eventRecord) + ": ");
                    assertStringBuilder.append(eventRecord.toString());
                    assertStringBuilder.append("\n");
                }
                break;
        }
        return assertStringBuilder;
    }

    public State GetCurrentInitialState() {
        return this._initialStateInstance;
    }

    /**
     * Method that asserts any additional criteria specified for specific use cases.
     *
     * @param result MessageResult generated by the processMessage() method of a State object.
     */
    public abstract void assertAdditionalCriteriaValidity(MessageResult result, State stateTested, IHOSMessage messageTested);

    /**
     * Method that returns a HashSet of transition definitions. Allows a given state to know where
     * it sits in relation to other included states
     *
     * @return Returns a HashSet of transition objects
     */
    protected abstract HashSet<StateTransitionDefinition> GetTransitionMappings();
}
