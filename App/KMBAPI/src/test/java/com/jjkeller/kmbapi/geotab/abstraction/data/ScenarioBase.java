package com.jjkeller.kmbapi.geotab.abstraction.data;

import com.jjkeller.kmbapi.HosMessageProcessor.interfaces.IHOSMessage;
import com.jjkeller.kmbapi.geotab.implementation.data.ScenarioSettings;
import com.jjkeller.kmbapi.geotab.implementation.engine.dataEngine.ScenarioComponentBuilder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Abstract class representing a scenario, and the properties required for it to create data
 */
public abstract class ScenarioBase {

    /**
     * Field that represents the settings to be used in creation of scenario data
     */
    protected final ScenarioSettings _settings;

    /**
     * Field that represents the state where a scenario has generated events such that it satisfies
     * the scenario described by business rules
     */
    protected final AtomicBoolean _hasSatisfiedScenarioRequirements = new AtomicBoolean(false);

    /**
     * Field that represents a scenario as an array of DataMapper objects
     */
    public ArrayList<IHOSMessage> ScenarioAsData;

    /**
     * Field that represents the data mapper we are currently referencing
     */
    protected IHOSMessage CurrentIHOSMessage;

    /**
     * Internal ScenarioComponentBuilder that allows easy construction of a scenario
     */
    protected ScenarioComponentBuilder _internalComponentBuilder;
    /**
     * Field that represents an internal scenario object
     */
    private ScenarioBase _internalScenario;


    /**
     * Base constructor accepting scenario settings
     *
     * @param settings Setting to use when running scenario
     */
    public ScenarioBase(ScenarioSettings settings) {
        if (settings == null)
            throw new IllegalArgumentException("Settings object was null");

        this._settings = settings;
        this.ScenarioAsData = new ArrayList<>();
        this._internalComponentBuilder = ScenarioComponentBuilder.getComponentBuilder(settings);
        this._internalScenario = this;
    }

    /**
     * Public method that returns the description of this scenario.
     * This includes base data/info, and any additional info created by the derived class
     * @param dumpGeneratedMessagesAsString Boolean representing if we would like to return
     *                                      ALL generated messages as a string
     * @return String with scenario description
     */
    public String getScenarioDescription(boolean dumpGeneratedMessagesAsString) {
        if (this._internalScenario == null)
            throw new IllegalStateException("Internal scenario object is null. Can not generate scenario description.");

        //String builder for description. First block is base info
        StringBuilder scenarioDescriptionSB = new StringBuilder("---Scenario Info---")
                .append("\n")
                .append("\n")
                .append("Scenario Name:")
                .append(" ")
                .append(this._internalScenario.getClass().getName())
                .append("\n")
                .append("Generation Date:")
                .append(" ")
                .append(Calendar.getInstance().getTime())
                .append("\n")
                .append("\n")
                .append("---Scenario Info---")
                .append("\n")
                .append("\n");
        //Per instance scenario description
        scenarioDescriptionSB.append("---Supplemental Scenario Description---")
                .append("\n")
                .append("\n")
                .append(this.emitInnerScenarioDescription())
                .append("\n")
                .append("---Supplemental Scenario Description---")
                .append("\n")
                .append("\n");
        if (this._settings != null) {
            scenarioDescriptionSB.append("---Scenario Settings---")
                    .append("\n")
                    .append("\n")
                    .append(this._internalScenario._settings.toString())
                    .append("\n")
                    .append("\n")
                    .append("---Scenario Settings---")
                    .append("\n")
                    .append("\n");
        }

        //General scenario data
        StringBuilder scenarioDataSB = new StringBuilder("---Scenario Data---")
                .append("\n")
                .append("\n");
        if (this._internalScenario.ScenarioAsData == null || this._internalScenario.ScenarioAsData.size() < 1) {
            scenarioDataSB.append("No messages were generated for this scenario.")
                    .append("\n");
        } else if (this._internalScenario.ScenarioAsData != null) {
            int messageCount = this._internalScenario.ScenarioAsData.size();
            scenarioDataSB.append("Number of Messages Generated:")
                    .append(" ")
                    .append(messageCount)
                    .append("\n")
                    .append("\n");
        }
        if (dumpGeneratedMessagesAsString && this._internalScenario.ScenarioAsData != null) {
            scenarioDataSB.append("---Messages---")
                    .append("\n")
                    .append("\n");
            this.appendMessageStrings(scenarioDataSB, this._internalScenario.ScenarioAsData)
                    .append("\n")
                    .append("---Messages---")
                    .append("\n")
                    .append("\n");
        }
        scenarioDataSB.append("---Scenario Data---");
        //Append data
        scenarioDescriptionSB.append(scenarioDataSB.toString());

        return scenarioDescriptionSB.toString();
    }

    /**
     * Public method that generates a scenario using provided data and settings
     *
     * @param args Arguments required to create given scenario
     * @return ScenarioBase object with scenario generated
     */
    public ScenarioBase createScenarioData(Object... args) {
        if (this._internalScenario == null)
            throw new IllegalStateException("Internal scenario object is null. Can not generate scenario.");
        //Start scenario
        /*
        If our argument list contains a list of message mapper objects, we will use that as the
        base for this scenario rather than creating a bookend event.
        This allows scenario chaining/default behavior
         */
        Set<IHOSMessage> SeedIHOSMessages = new HashSet<>();
        argTypeCheckLoop:
        for (Object arg : args) {
            if (arg instanceof List<?> && ((List) arg).size() > 0) {
                if (((List) arg).get(0) instanceof IHOSMessage) {
                    for (IHOSMessage msg : (List<IHOSMessage>) arg) {
                        SeedIHOSMessages.add(msg);
                    }
                }
            }
        }
        if (SeedIHOSMessages.size() > 0) {
            for (IHOSMessage seedHOSMessage : SeedIHOSMessages) {
                this._internalScenario.ScenarioAsData.add(seedHOSMessage);
            }
            //Set our current message mapper to the last passed in event
            this._internalScenario.CurrentIHOSMessage = this._internalScenario.ScenarioAsData.get(this._internalScenario.ScenarioAsData.size() - 1);
        }
        //Generate intermediate data while we should continue
        while (this._internalScenario.shouldContinueGeneration()) {
            //Create an array of DataMapper objects from provided args
            ArrayList<IHOSMessage> synthesizedScenarioDataPoints = this._internalScenario.synthesize(args);
            if (synthesizedScenarioDataPoints != null) {
                for (int synthasizedDataIndex = 0; synthasizedDataIndex < synthesizedScenarioDataPoints.size(); synthasizedDataIndex++) {
                    if (!this._internalScenario.validate(synthesizedScenarioDataPoints.get(synthasizedDataIndex))) {
                        //TODO: Need to use justin's logcat implementation
                        throw new IllegalStateException("Validation of scenario data point failed.");
                    }
                }
                //Add newly created data to scenario data
                this._internalScenario.ScenarioAsData.addAll(synthesizedScenarioDataPoints);
            }
            //Set the current message to the last one in the ScenarioAsData collection
            this._internalScenario.CurrentIHOSMessage = this._internalScenario.ScenarioAsData.get(this._internalScenario.ScenarioAsData.size() - 1);
            //post-processing to determine if we should continue
            this._internalScenario = this._internalScenario.continueGeneration();
        }

        return this._internalScenario;
    }

    /**
     * Public instance method that determines if a calling process can continue to provide input
     * used to generate a scenario
     *
     * @return Boolean representing if we should continue creating scenario data
     */
    protected synchronized boolean shouldContinueGeneration() {
        if (this._internalScenario == null)
            throw new IllegalStateException("Internal scenario object is null.");

        //If we've satisfied the requirements, don't continue generating data
        return !this._internalScenario._hasSatisfiedScenarioRequirements.get();
    }

    /**
     * Private instance method that appends a given array list of hos messages' data as string to the
     * logging.
     * @param messageStringBuilder string builder to append to
     * @param messages messages to pull values from
     * @return String builder with new data appended
     */
    private StringBuilder appendMessageStrings(StringBuilder messageStringBuilder, ArrayList<IHOSMessage> messages) {
        for (IHOSMessage message : messages) {
            messageStringBuilder.append("---Message Timestamp")
                    .append(" ")
                    .append(message.getTimestampUtc())
                    .append("---")
                    .append("\n")
                    .append("Gps Latitude:")
                    .append(" ")
                    .append(message.getGpsLatitude())
                    .append("\n")
                    .append("Gps Longitude:")
                    .append(" ")
                    .append(message.getGpsLongitude())
                    .append("\n")
                    .append("Speedometer:")
                    .append(" ")
                    .append(message.getSpeedometer())
                    .append("\n")
                    .append("Tachometer:")
                    .append(" ")
                    .append(message.getTachometer())
                    .append("\n")
                    .append("Odometer:")
                    .append(" ")
                    .append(message.getOdometer())
                    .append("\n")
                    .append("Trip Odometer:")
                    .append(" ")
                    .append(message.getTripOdometer())
                    .append("\n")
                    .append("Engine Hours:")
                    .append(" ")
                    .append(message.getEngineHours())
                    .append("\n")
                    .append("Trip Engine Seconds:")
                    .append(" ")
                    .append(message.getTripEngineSeconds())
                    .append("\n")
                    .append("Serial Number Crc:")
                    .append(" ")
                    .append(message.getSerialNumberCrc())
                    .append("\n")
                    .append("Is GPS Valid:")
                    .append(" ")
                    .append(message.isGpsValid() ? "Yes" : "No")
                    .append("\n")
                    .append("Is Ignition On:")
                    .append(" ")
                    .append(message.isIgnitionOn() ? "Yes" : "No")
                    .append("\n")
                    .append("Is Engine Activity Detected:")
                    .append(" ")
                    .append(message.isEngineActivityDetected() ? "Yes" : "No")
                    .append("\n")
                    .append("Is DateTime Valid:")
                    .append(" ")
                    .append(message.isDatetimeValid() ? "Yes" : "No")
                    .append("\n")
                    .append("\n");

        }
        return messageStringBuilder;
    }

    /**
     * Public abstract instance method that accepts an array of arguments used to continue
     * creating scenario data
     *
     * @param args Arguments needed to start a scenario
     * @return ScenarioBase object with the start of a scenario initialized
     */
    protected abstract ScenarioBase continueGeneration(Object... args);

    /**
     * Public abstract instance method that accepts an array of arguments used to generate a data
     * point using internal methods on the scenario itself. Affords ability to generate dynamic
     * or randomized driving/move data
     *
     * @param args Arguments needed to start a scenario
     * @return DataMapper object with data
     */
    protected abstract ArrayList<IHOSMessage> synthesize(Object... args);

    /**
     * Public abstract instance method that validates a given data mapper object
     * against whatever criteria the derived scenario provides
     *
     * @param dataMapper DataMapper object to validateAgainstCurrentScenario
     * @return Boolean representing if the object is valid
     */
    protected abstract boolean validate(IHOSMessage dataMapper);

    /**
     * protected abstract method that returns a String containing scenario description.
     * Added to base Scenario Description
     *
     * @return String containing any information about a scenario that would be useful
     */
    protected abstract String emitInnerScenarioDescription();

    /**
     * Public abstract method that is called after a scenario is generated. Cleans up/finalizes
     * anything for a given scenario
     *
     * @param args Arguments for cleanup
     * @return ScenarioBase object that has been cleaned up
     */
    public abstract ScenarioBase cleanup(Object... args);


}
