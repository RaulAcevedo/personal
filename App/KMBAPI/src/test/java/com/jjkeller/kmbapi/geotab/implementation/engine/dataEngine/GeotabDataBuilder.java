package com.jjkeller.kmbapi.geotab.implementation.engine.dataEngine;

import com.jjkeller.kmbapi.HosMessageProcessor.interfaces.IHOSMessage;
import com.jjkeller.kmbapi.geotab.abstraction.data.ScenarioManagerBase;
import com.jjkeller.kmbapi.geotab.implementation.data.ScenarioSettings;
import com.jjkeller.kmbapi.geotab.implementation.engine.dataEngine.scenarioManagers.ApprovalScenarioManager;
import com.jjkeller.kmbapi.geotab.implementation.engine.dataEngine.scenarioManagers.BasicScenarioManager;
import com.jjkeller.kmbapi.geotab.implementation.enums.ScenarioSelectorEnum;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by bja6001 on 10/7/16.
 */

public class GeotabDataBuilder {
    /**
     * Field representing an internal DataBuilder object
     */
    protected GeotabDataBuilder _internalBuilder;

    /**
     * Field representing a collection of DataMapper objects to pass into the Scenario
     * and to be used as seed data
     */
    protected ArrayList<IHOSMessage> _scenarioSeedData;

    /**
     * Field that represents a collection of DataMapperBase objects that have been processed, such
     * that they now contain multiple representations (Raw and HOS) of an event or series of
     * events
     */
    protected HashMap<Integer, IHOSMessage> _scenarioAsDataMapperCollection;

    /**
     * Field that represents a collection of validation results, mapping a given index from the
     * _scenarioAsDataMapperCollection to it's validation result
     */
    protected HashMap<Integer, Boolean> _scenarioValidationResults;

    /**
     * Field representing the validator used to ensure data integrity and mapping between input
     * and output
     */
    protected ScenarioManagerBase _internalScenarioManager;

    /**
     * Protected constructor for builder pattern
     */
    protected GeotabDataBuilder() {
        this._internalBuilder = this;
    }

    //Builder logic
    public static GeotabDataBuilder getDataBuilder() {
        GeotabDataBuilder returnBuilder = new GeotabDataBuilder();
        //New up data collection
        returnBuilder._scenarioSeedData = new ArrayList<>();
        returnBuilder._scenarioValidationResults = new HashMap<>();
        returnBuilder._scenarioAsDataMapperCollection = new HashMap<>();
        //Assign the internal builder property
        returnBuilder._internalBuilder = returnBuilder;
        return returnBuilder;
    }
    //Builder logic

    //Methods dealing with the creation of scenarios using data operation

    /**
     * Instance method that uses the internal data operation object to create a scenario from the
     * provided settings
     *
     * @param settings ScenarioSettings to use
     * @return GeotabDataBuilder containing a scenario represented by multiple DataMapper objects
     */
    public GeotabDataBuilder initializeScenario(ScenarioSettings settings) {
        if (this._internalBuilder._internalScenarioManager == null)
            throw new UnsupportedOperationException("The internal operation processor object is null, data can not be created. Did you call usingDataOperationScheme()?");
        //call Data Operation's generate method and save output, passing in potential existing messages
        this._internalBuilder._internalScenarioManager = this._internalBuilder._internalScenarioManager.initializeScenario(settings);


        return this._internalBuilder;
    }
    /**
     * Instance method that uses the internal data operation object to create a scenario from the
     * provided enum specified in previous calls
     *
     * @return GeotabDataBuilder containing a scenario represented by multiple DataMapper objects
     */
    public GeotabDataBuilder generateScenario() {
        if (this._internalBuilder._internalScenarioManager == null)
            throw new UnsupportedOperationException("The internal operation processor object is null, data can not be created. Did you call usingDataOperationScheme()?");
        //call Data Operation's generate method and save output, passing in potential existing messages
        this._internalBuilder._internalScenarioManager = this._internalBuilder._internalScenarioManager.generateScenario(this._internalBuilder._scenarioSeedData);
        //Set this object's representation of the scenario data == to the manager's returned one
        ArrayList<IHOSMessage> scenarioData = this._internalBuilder._internalScenarioManager.getCurrentScenario().ScenarioAsData;
        int order = 0;
        for (IHOSMessage dataPoint : scenarioData) {
            this._internalBuilder._scenarioAsDataMapperCollection.put(order, dataPoint);
            order++;
        }

        return this._internalBuilder;
    }
    //Methods dealing with the creation of scenarios using data operation


    //Methods dealing with Validation of Data
    /**
     * Instance method that accepts a ValidatorScheme enum and returns a GeotabDataBuilder object
     * with the selected validator initialized
     *
     * @param selectedScheme Scheme desired for validation (Basic or Approvals-based)
     * @return GeotabDataBuilder with selected validator initialized
     */
    public GeotabDataBuilder usingDataOperationScheme(DataOperationScheme selectedScheme) {
        ScenarioManagerBase validatorBaseReturn;
        switch (selectedScheme) {
            case BASIC:
                validatorBaseReturn = new BasicScenarioManager(ScenarioSelectorEnum.NONE);
                break;
            case APPROVALS:
                validatorBaseReturn = new ApprovalScenarioManager(ScenarioSelectorEnum.NONE);
                break;
            default:
                throw new IllegalArgumentException("Enum provided to method does not match a Validator Scheme.");
        }
        this._internalBuilder._internalScenarioManager = validatorBaseReturn;
        return this._internalBuilder;
    }

    /**
     * Instance method that accepts a DataValidationTypeTarget enum defining what strategy will be
     * evaluated. Enums have description of associated strategy
     *
     * @param strategy Type of validation to perform
     * @return GeotabDataBuilder with an initialized validator that uses provided strategy
     */
    public GeotabDataBuilder setScenarioType(ScenarioSelectorEnum strategy) {
        if (this._internalBuilder._internalScenarioManager == null)
            throw new UnsupportedOperationException("Can not set validation strategy. No validator object found. Did you call usingDataOperationScheme() first?");

        //Set the strategy
        this._internalBuilder._internalScenarioManager.setCurrentValidationStrategy(strategy);
        //return internal builder object
        return this._internalBuilder;
    }
    //Methods dealing with Validation of Data

    //Methods dealing with returning properties of Builder

    /**
     * Instance method that returns the current DataOperationBase object present in the internal
     * builder object
     *
     * @return DataOperationBase object present on current internal builder
     */
    public ScenarioManagerBase getCurrentScenarioManager() {
        if (this._internalBuilder == null)
            throw new UnsupportedOperationException("The internal builder is null. Did you use the Builder's getDataBuilder() method to create this object?");

        return this._internalBuilder._internalScenarioManager;
    }

    /**
     * Instance method that returns the list of fully hydrated IHOSMessage objects (read: objects that
     * have had companion data generated for them as part of a scenario)
     *
     * @return Returns a HashMap representing a series of messages with their order as the key
     */
    public HashMap<Integer, IHOSMessage> getScenarioAsMessages() {
        if (this._internalBuilder == null)
            throw new IllegalStateException("The internal builder is null. Did you use the Builder's getDataBuilder() method to create this object?");
        if (this._internalBuilder._scenarioAsDataMapperCollection == null)
            throw new IllegalStateException("The internal scenario collection is null. Did you generate it?");

        HashMap<Integer, IHOSMessage> returnScenario = new HashMap<>();
        for (int dataMapperContainerIndex = 0; dataMapperContainerIndex < this._internalBuilder._scenarioAsDataMapperCollection.size(); dataMapperContainerIndex++) {
            returnScenario.put(dataMapperContainerIndex, this._internalBuilder._scenarioAsDataMapperCollection.get(dataMapperContainerIndex));
        }
        return returnScenario;
    }
    //Methods dealing with returning properties of Builder

    //Methods dealing with Adding Existing Data to builder/validator

    /**
     * Instance method that accepts an IHOSMessage and adds it to the internal data mappings
     * storage object. If a validator scheme has not been set, defaults to using Approval-based
     * validation
     * @param message Message to add
     * @return GeotabDataBuilder with message added to internal data mappings storage
     */
    public GeotabDataBuilder withProvidedMessage(IHOSMessage message) {
        if (this._internalBuilder._scenarioSeedData == null)
            throw new UnsupportedOperationException("Internal collection of data mappings is null. Did you use the Builder's getDataBuilder() method to create this object?");

        ArrayList<IHOSMessage> messages = new ArrayList<>();
        messages.add(message);
        return this.withProvidedMessages(messages);
    }

    /**
     * Instance method that accepts an Array of IHOSMessage and adds them to the internal data mappings
     * storage object
     * @param messages Messages to add
     * @return GeotabDataBuilder with message added to internal data mappings storage
     */
    public GeotabDataBuilder withProvidedMessages(ArrayList<IHOSMessage> messages) {
        if (this._internalBuilder._scenarioSeedData == null)
            throw new UnsupportedOperationException("Internal collection of data mappings is null. Did you use the Builder's getDataBuilder() method to create this object?");
        this._internalBuilder._scenarioSeedData.addAll(messages);
        return this._internalBuilder;
    }




    //Methods dealing with Adding Existing Data to builder/validator


    //Classes
    /**
     * Public class level enum representing the type of validator to use (Approvals or Basic)
     */
    public enum DataOperationScheme {
        BASIC,
        APPROVALS
    }
    //Classes
}
