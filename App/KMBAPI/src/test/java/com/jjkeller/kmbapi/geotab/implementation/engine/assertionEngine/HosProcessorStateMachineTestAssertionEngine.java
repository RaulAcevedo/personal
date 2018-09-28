package com.jjkeller.kmbapi.geotab.implementation.engine.assertionEngine;

import com.jjkeller.kmbapi.HosMessageProcessor.interfaces.IHOSMessage;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.SharedState;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.AobrdEngineOff;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.AobrdMovingDriving;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.AobrdMovingNotDriving;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.AobrdStoppedDriving;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.AobrdStoppedNotDriving;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.MandateEngineOffDriving;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.MandateEngineOffNotDriving;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.MandateMovingDriving;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.MandateMovingNotDriving;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.MandateStoppedDriving;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.MandateStoppedNotDriving;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.states.State;
import com.jjkeller.kmbapi.geotab.abstraction.state.StateValidityMappingBase;
import com.jjkeller.kmbapi.geotab.implementation.engine.assertionEngine.validators.AobrdMovingDrivingValidator;
import com.jjkeller.kmbapi.geotab.implementation.engine.assertionEngine.validators.AobrdMovingNotDrivingValidator;
import com.jjkeller.kmbapi.geotab.implementation.engine.assertionEngine.validators.AobrdStoppedDrivingValidator;
import com.jjkeller.kmbapi.geotab.implementation.engine.assertionEngine.validators.AobrdStoppedNotDrivingValidator;
import com.jjkeller.kmbapi.geotab.implementation.engine.assertionEngine.validators.MandateEngineOffDrivingValidator;
import com.jjkeller.kmbapi.geotab.implementation.engine.assertionEngine.validators.MandateEngineOffNotDrivingValidator;
import com.jjkeller.kmbapi.geotab.implementation.engine.assertionEngine.validators.MandateMovingDrivingValidator;
import com.jjkeller.kmbapi.geotab.implementation.engine.assertionEngine.validators.MandateMovingNotDrivingValidator;
import com.jjkeller.kmbapi.geotab.implementation.engine.assertionEngine.validators.MandateStoppedDrivingValidator;
import com.jjkeller.kmbapi.geotab.implementation.engine.assertionEngine.validators.MandateStoppedNotDrivingValidator;
import com.jjkeller.kmbapi.geotab.abstraction.base.HosProcessorStateMachineTestBase;
import com.jjkeller.kmbapi.geotab.implementation.engine.assertionEngine.validators.AobrdEngineOffValidator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Base class for testing Hos State Machine data. Provides methods to initialize an engine for a
 * specified initial State and test against all possible scenarios for that given starting state.
 */
public class HosProcessorStateMachineTestAssertionEngine extends HosProcessorStateMachineTestBase {

    /**
     * Internal instance of the HosProcessorStateMachineTestAssertionEngine
     */
    private final HosProcessorStateMachineTestAssertionEngine _internalEngine;
    /**
     * Field representing the initial states passed into the constructor
     */
    private final ArrayList<State> _initialStatesForConstructor = new ArrayList<>();
    /**
     * Field representing the validity object that will be executed.
     */
    private Set<StateValidityMappingBase> _validityObjects = new HashSet<>();
    private HashMap<Integer, IHOSMessage> _internalMessages = new HashMap<>();
    /**
     * Field representing a buildable object that should validateAgainstCurrentScenario all potential state combinations
     */
    private boolean _validateAllStates;
    /**
     * Field representing if the current Engine is valid
     */
    private boolean _isValidEngine;
    /**
     * Field representing the mapping between an instance of a State (initial state)
     * and the desired end state
     */
    private HashMap<IHOSMessage, HashMap<State, Class<? extends State>>> _internalStateInstances = new HashMap<>();


    private HosProcessorStateMachineTestAssertionEngine(HosProcessorStateMachineTestAssertionEngine existingEngine) throws Exception {
        super();
        this._validityObjects = existingEngine._validityObjects;
        this._validateAllStates = existingEngine._validateAllStates;
        this._isValidEngine = existingEngine._isValidEngine;
        this.sharedState = existingEngine.sharedState;
        this._internalStateInstances = existingEngine._internalStateInstances;
        this._internalEngine = this;
    }

    /**
     * Constructor for Engine accepting a validity base object.
     *
     * @param initialStates Initial State instance to use in validators
     * @throws Exception Throws exception from HosProcessorStateMachineTestBase
     */
    private HosProcessorStateMachineTestAssertionEngine(ArrayList<State> initialStates) throws Exception {
        super();
        this._isValidEngine = false;
        for (State initialState : initialStates) {
            this._initialStatesForConstructor.add(initialState);
        }
        _internalEngine = this;
    }

    /**
     * Static method that returns map of States : StateValidityMappingBase implementations
     *
     * @return HashMap containing StateValidity objects mapped to instance of State
     */
    public static HashMap<Class<? extends State>, Class<? extends StateValidityMappingBase>> GetValidityMappings() {
        return GetValidityMappings(MappingTypeEnum.ALL);
    }

    /**
     * Static method that returns map of States : StateValidityMappingBase implementations
     *
     * @return HashMap containing StateValidity objects mapped to instance of State
     */
    public static HashMap<Class<? extends State>, Class<? extends StateValidityMappingBase>> GetValidityMappings(MappingTypeEnum mappingTypeEnum) {
        HashMap<Class<? extends State>, Class<? extends StateValidityMappingBase>> validityMappings = new HashMap<>();

        switch (mappingTypeEnum) {
            default:
            case ALL:
                //Aobrd mappings
                validityMappings.put(AobrdEngineOff.class, AobrdEngineOffValidator.class);
                validityMappings.put(AobrdMovingDriving.class, AobrdMovingDrivingValidator.class);
                validityMappings.put(AobrdMovingNotDriving.class, AobrdMovingNotDrivingValidator.class);
                validityMappings.put(AobrdStoppedDriving.class, AobrdStoppedDrivingValidator.class);
                validityMappings.put(AobrdStoppedNotDriving.class, AobrdStoppedNotDrivingValidator.class);
                //Mandate mappings
                validityMappings.put(MandateEngineOffNotDriving.class, MandateEngineOffNotDrivingValidator.class);
                validityMappings.put(MandateEngineOffDriving.class, MandateEngineOffDrivingValidator.class);
                validityMappings.put(MandateMovingDriving.class, MandateMovingDrivingValidator.class);
                validityMappings.put(MandateMovingNotDriving.class, MandateMovingNotDrivingValidator.class);
                validityMappings.put(MandateStoppedDriving.class, MandateStoppedDrivingValidator.class);
                validityMappings.put(MandateStoppedNotDriving.class, MandateStoppedNotDrivingValidator.class);
                break;
            case AOBRD:
                validityMappings.put(AobrdEngineOff.class, AobrdEngineOffValidator.class);
                validityMappings.put(AobrdMovingDriving.class, AobrdMovingDrivingValidator.class);
                validityMappings.put(AobrdMovingNotDriving.class, AobrdMovingNotDrivingValidator.class);
                validityMappings.put(AobrdStoppedDriving.class, AobrdStoppedDrivingValidator.class);
                validityMappings.put(AobrdStoppedNotDriving.class, AobrdStoppedNotDrivingValidator.class);
                break;
            case MANDATE:
                validityMappings.put(MandateEngineOffNotDriving.class, MandateEngineOffNotDrivingValidator.class);
                validityMappings.put(MandateEngineOffDriving.class, MandateEngineOffDrivingValidator.class);
                validityMappings.put(MandateMovingDriving.class, MandateMovingDrivingValidator.class);
                validityMappings.put(MandateMovingNotDriving.class, MandateMovingNotDrivingValidator.class);
                validityMappings.put(MandateStoppedDriving.class, MandateStoppedDrivingValidator.class);
                validityMappings.put(MandateStoppedNotDriving.class, MandateStoppedNotDrivingValidator.class);
                break;

        }
        return validityMappings;
    }

    /**
     * Public static method that returns a new instance of the assertion engine using initial state
     * object and desired end state
     *
     * @param initialStates State object containing data that will be set as initial state of HosProcessorStateMachine
     * @return Returns a new instance of this class with the StateValidityMappingBase object
     * associated with the provided arguments set.
     * @throws Exception
     */
    public static HosProcessorStateMachineTestAssertionEngine getEngineFor(ArrayList<State> initialStates) throws Exception {
        boolean hasInvalidEngineConstructionParameters = false;
        StringBuilder exceptionMessageBuilder = new StringBuilder();
        /*
        If the initial state class is null throw an exception
         */
        if (initialStates == null) {
            hasInvalidEngineConstructionParameters = true;
            exceptionMessageBuilder.append("Provided parameter for initial state class is null.");
            exceptionMessageBuilder.append("\n");
        }


        if (hasInvalidEngineConstructionParameters)
            throw new IllegalArgumentException(exceptionMessageBuilder.toString());

        //Return a new instance of this class with the validity object instantiated
        return new HosProcessorStateMachineTestAssertionEngine(initialStates);
    }

    public static HosProcessorStateMachineTestAssertionEngine getEngineFromExisting(HosProcessorStateMachineTestAssertionEngine engine) throws Exception {
        if (engine == null)
            throw new IllegalArgumentException("Provided engine object is null.");
        return new HosProcessorStateMachineTestAssertionEngine(engine);
    }


    public HosProcessorStateMachineTestAssertionEngine withSharedState(SharedState sharedState) {
        if (assertEngineValidity(false)) {
            this._internalEngine.sharedState = sharedState;
        }
        return this._internalEngine;
    }

    public HosProcessorStateMachineTestAssertionEngine withRoundTripStateMappingForMessage(State initialStateClass, IHOSMessage messageToMap, Class<? extends State> desiredEndState) {

        if (this.assertEngineValidity(false)) {
            if (!this._internalEngine._initialStatesForConstructor.contains(initialStateClass)) {
                throw new IllegalArgumentException("Internal mapping of start state : end state could not locate instance of provided initialStateClass parameter.");
            }

            if (this._internalEngine._internalStateInstances.get(messageToMap) == null)
                this._internalEngine._internalStateInstances.put(messageToMap, new HashMap<State, Class<? extends State>>());

            this._internalEngine._internalStateInstances.get(messageToMap).put(initialStateClass, desiredEndState);
        }
        return this._internalEngine;
    }

    public HosProcessorStateMachineTestAssertionEngine WithMessages(HashMap<Integer, IHOSMessage> messageHashMap) {
        if (this.assertEngineValidity(false)) {
            boolean isValid = true;
            StringBuilder errorSB = new StringBuilder("Message HashMap provided has conflicts with existing message order.");
            errorSB.append("\n");
            errorSB.append("Conflicts:");
            errorSB.append("\n");
            for (int messageIterator = 0; messageIterator < messageHashMap.size(); messageIterator++) {
                if (this._internalEngine._internalMessages.containsKey(messageIterator)) {
                    isValid |= false;
                    errorSB.append("Existing Message: ");
                    errorSB.append(this._internalEngine._internalMessages.get(messageIterator).getClass().getName());
                    errorSB.append(" ").append("At Index: ").append(messageIterator);
                }
                IHOSMessage message = messageHashMap.get(messageIterator);
                this._internalEngine._internalMessages.put(messageIterator, message);
            }
            if (!isValid)
                throw new IllegalArgumentException(errorSB.toString());
        }
        return this._internalEngine;
    }


    public HosProcessorStateMachineTestAssertionEngine ValidateStateTransaction() throws Exception {
        if (assertEngineValidityForValidation(true, this._internalEngine._internalMessages)) {
            for (StateValidityMappingBase validator : this._internalEngine._validityObjects) {
                for (int messageIndex = 0; messageIndex < this._internalEngine._internalMessages.size(); messageIndex++) {
                    //Get the message
                    IHOSMessage message = this._internalEngine._internalMessages.get(messageIndex);
                    //Set desired end state if provided
                    HashMap messageMapForState = null;
                    if (this._internalEngine._internalStateInstances.containsKey(message))
                        messageMapForState = this._internalEngine._internalStateInstances.get(message);

                    if (messageMapForState != null) {
                        validator = validator.WithDesiredEndState((Class<? extends State>) messageMapForState.get(validator.GetCurrentInitialState()));
                        validator.Validate(message, this._internalEngine._internalMessages.size() > 1, !this._internalEngine._internalMessages.containsKey(messageIndex + 1));
                    } else {
                        throw new IllegalStateException("Unable to find end state for given starting state and current message.");
                    }
                }
            }
        }
        return this._internalEngine;
    }

    public Set<StateValidityMappingBase> GetValidators() {
        if (this.assertEngineValidity(true))
            return this._internalEngine._validityObjects;
        return null;
    }

    public HosProcessorStateMachineTestAssertionEngine initializeValidators() throws Exception {
        this.initializeValidatorsInternal();

        return this._internalEngine;
    }

    private void initializeValidatorsInternal() throws Exception {
        if (assertEngineValidityForValidation(false, this._internalEngine._internalMessages)) {
            //Iterate over collection of State : HashMap<Message,ExpectedState>
            for (State o : this._initialStatesForConstructor) {
                //Found an entry
                //New up a validator
                this._internalEngine._validityObjects.add(StateValidityMappingBase.getStateValidatorFor(o, null, GetValidityMappings(), null));
            }
        }
    }

    /**
     * Method that ensures we have an internal engine before access
     *
     * @return Boolean representing state of internal engine
     */
    private boolean assertEngineValidity(boolean isAttemptedProcessingOperation) {
        if (this._internalEngine == null) {
            throw new IllegalStateException("The internal engine has not been initialized. Did you use getEngineFor()?");
        }
        if (this._internalEngine._initialStatesForConstructor == null || this._internalEngine._initialStatesForConstructor.size() == 0) {
            throw new IllegalStateException("The initial list of starting states has not been initialized. Did you provide initial states in the .getEngineFor() method?");
        }
        //If this is a processing operation, we need to make sure we have a full internal engine,
        //not just an object reference
        if (isAttemptedProcessingOperation) {
            if (this._internalEngine._validityObjects == null || this._internalEngine._validityObjects.size() < 1) {
                throw new IllegalStateException("No validator objects have been initialized on internal validator. Did you call initializeValidators()?");
            }
            if (this._internalEngine.sharedState == null) {
                throw new IllegalStateException("SharedState object on internal validator is null. Did you call withSharedState()?");
            }
            return true;
        } else {
            return true;
        }
    }

    /**
     * Method that validates the engine for processing multiple messages
     *
     * @param isAttemptedProcessingOperation Boolean representing if this is being used to process
     *                                       arguments (read: validateAgainstCurrentScenario a state)
     * @param messageMap                     HashMap representing the messages to validateAgainstCurrentScenario
     * @return Returns boolean representing if this is valid
     */
    private boolean assertEngineValidityForValidation(boolean isAttemptedProcessingOperation, HashMap messageMap) {
        boolean isValidationEngineOk = this.assertEngineValidity(isAttemptedProcessingOperation);
        boolean isMessageStateMapValid = true;
        StringBuilder mapSB = new StringBuilder("\"Could not find end state mapping for multi message operation.");
        mapSB.append("\n");
        mapSB.append("Not Found Message Mappings:");
        mapSB.append("\n");
        if (this._internalEngine._internalStateInstances != null && isValidationEngineOk) {
            Iterator stateInstanceIterator = this._internalEngine._internalStateInstances.entrySet().iterator();
            stateInstanceLoop:
            while (stateInstanceIterator.hasNext()) {
                Map.Entry stateMessageMap = (Map.Entry) stateInstanceIterator.next();
                messageLoop:
                for (Map.Entry messageEntry : (Set<Map.Entry>) messageMap.entrySet()) {
                    if (!((HashMap) (stateMessageMap.getValue())).containsKey(messageEntry.getValue())) {
                        isMessageStateMapValid |= false;
                        mapSB.append("State: ");
                        mapSB.append(stateMessageMap.getKey().getClass().getName());
                        mapSB.append("\n");
                        mapSB.append("Message Expected: ");
                        mapSB.append(messageEntry.getValue().getClass().getName());
                    }
                }
            }
            if (!isMessageStateMapValid)
                //Ensure we have HOS message validity
                throw new IllegalStateException(mapSB.toString());
        } else if (!isValidationEngineOk) {
            throw new IllegalStateException("The internal engine could not be validated prior to asserting message validity.");
        } else {
            throw new IllegalStateException("The internal engine's state/message mapping collection is null. Can't process multiple messages.");
        }
        return isValidationEngineOk;
    }

    /**
     * Enum that defines which mapping will be used
     */
    private enum MappingTypeEnum {
        MANDATE,
        AOBRD,
        ALL
    }
}
