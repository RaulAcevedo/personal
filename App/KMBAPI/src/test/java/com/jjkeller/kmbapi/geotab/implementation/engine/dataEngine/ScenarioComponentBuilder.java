package com.jjkeller.kmbapi.geotab.implementation.engine.dataEngine;

import com.jjkeller.kmbapi.HosMessageProcessor.interfaces.IHOSMessage;
import com.jjkeller.kmbapi.geotab.implementation.data.ScenarioSettings;
import com.jjkeller.kmbapi.geotabengine.HOSMessage;
import com.jjkeller.kmbapi.common.EngineModel;
import com.jjkeller.kmbapi.common.TestBase;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Public class that generates series of IHOSMessages to represent individual components in a scenario
 */
public class ScenarioComponentBuilder extends TestBase {

    protected final EngineModel _internalEngineModel;
    protected final AtomicBoolean _hasRegisteredToGenerate = new AtomicBoolean(false);
    protected final ArrayList<ScenarioComponentBuilder> _repeatComponentGenerationCollection = new ArrayList<>();
    //Constants
    private final float MILESINLONDEGREE = 54.583f;
    private final int SECONDSINHOUR = 3600;
    protected ScenarioSettings _scenarioSettings;
    protected GenerationSettings _generationSettings;
    protected IHOSMessage _startingMessage;
    protected ScenarioComponentBuilder _internalComponent;
    private ArrayList<IHOSMessage> _internalScenarioComponentCollection;

    private ScenarioComponentBuilder(ScenarioComponentBuilder existingBase) {
        this(existingBase._generationSettings._CurrentIHOSMessage, existingBase._scenarioSettings, existingBase._generationSettings, existingBase._internalEngineModel, existingBase._internalScenarioComponentCollection);
        if (!this._hasRegisteredToGenerate.compareAndSet(false, existingBase._hasRegisteredToGenerate.get())) {
            throw new IllegalStateException("Scenario Builder chaining failed. Generated builder had already registered to generate a component, and cloned builder attempted to modify that.");
        }
    }

    private ScenarioComponentBuilder(IHOSMessage startingMessageState, ScenarioSettings settings, GenerationSettings generationSettings, EngineModel currentEngineModel, ArrayList<IHOSMessage> existingMessages) {
        if (startingMessageState != null)
            this._startingMessage = new HOSMessage(startingMessageState);
        if (settings != null)
            this._scenarioSettings = settings;
        if (generationSettings != null)
            this._generationSettings = new GenerationSettings(generationSettings);
        if (existingMessages != null)
            this._internalScenarioComponentCollection = new ArrayList<>(existingMessages);
        else
            this._internalScenarioComponentCollection = new ArrayList<>();
        if (currentEngineModel != null)
            this._internalEngineModel = currentEngineModel;
        else
            //Only param that can not be null, as it contains chaining info
            throw new IllegalStateException("Engine Model parameter is null, can not chain Components without constant message source of truth.");
        this._internalComponent = this;
    }


    public static ScenarioComponentBuilder getComponentBuilder(ScenarioSettings settings) {
        ScenarioComponentBuilder returnBuilder = new ScenarioComponentBuilder(null, settings, null, new EngineModel(settings.InitialOdometer), null);

        return returnBuilder;
    }


    public ArrayList<IHOSMessage> getComponentData() {
        if (this._internalComponent == null)
            throw new IllegalStateException("Internal Component Builder is null. Can not chain builders.");
        if (this._internalComponent._internalScenarioComponentCollection == null)
            throw new IllegalArgumentException("Internal Component Builder's data has not been initialized correctly.");

        return this._internalComponent._internalScenarioComponentCollection;
    }

    public ScenarioComponentBuilder withInitialMessage(IHOSMessage initialMessage) {
        if (initialMessage == null)
            throw new IllegalArgumentException("Initial Message argument is null.");
        if (this._internalComponent == null)
            throw new IllegalStateException("Internal Component Builder is null. Can not generate scenario data.");
        this._internalComponent._startingMessage = initialMessage;

        return this._internalComponent;
    }

    public ScenarioComponentBuilder initializeMessageGeneration() {
        if (this._internalComponent == null)
            throw new IllegalStateException("Internal Component Builder is null. Can not set settings.");

        if (!this._internalComponent._hasRegisteredToGenerate.compareAndSet(false, true)) {
            throw new IllegalStateException("Component Builder has already registered it's intent to generate messages.");
        }
        return this._internalComponent;
    }

    public ScenarioComponentBuilder generateMessagesForDuration(int numberOfSeconds) {
        if (this._internalComponent == null)
            throw new IllegalStateException("Internal Component Builder is null. Can not set duration.");

        if (!this._internalComponent._hasRegisteredToGenerate.get()) {
            throw new IllegalStateException("Component Builder has not registered to generate messages. Please call initializeMessageGeneration() first.");
        }

        if (this._internalComponent._generationSettings == null)
            this._internalComponent._generationSettings = new GenerationSettings();

        //Clear Duration in messages
        this._internalComponent._generationSettings.DurationInNumberOfMessages = null;
        this._internalComponent._generationSettings.DurationInSeconds = numberOfSeconds;

        return this._internalComponent;
    }

    public ScenarioComponentBuilder generateNumberOfMessages(int numberOfMessages) {
        if (this._internalComponent == null)
            throw new IllegalStateException("Internal Component Builder is null. Can not set duration.");

        if (!this._internalComponent._hasRegisteredToGenerate.get()) {
            throw new IllegalStateException("Component Builder has not registered to generate messages. Please call initializeMessageGeneration() first.");
        }

        if (this._internalComponent._generationSettings == null)
            this._internalComponent._generationSettings = new GenerationSettings();
        //Clear Duration in seconds
        this._internalComponent._generationSettings.DurationInSeconds = null;
        this._internalComponent._generationSettings.DurationInNumberOfMessages = numberOfMessages;

        return this._internalComponent;
    }

    public ScenarioComponentBuilder withConstantVSS(float speedInMPH) {
        if (this._internalComponent == null)
            throw new IllegalStateException("Internal Component Builder is null. Can not generate scenario data.");
        if (!this._internalComponent._hasRegisteredToGenerate.get())
            throw new IllegalStateException("Component Builder has not registered to generate messages. Please call initializeMessageGeneration() first.");
        if (this._internalComponent._generationSettings == null)
            throw new IllegalStateException("Generation Settings on internal component builder is null. Please call initializeMessageGeneration() first.");

        this._internalComponent._generationSettings.SpeedToSet = speedInMPH;
        this._internalComponent._generationSettings.SpeedChangeIsDynamic = false;

        return this._internalComponent;
    }

    public ScenarioComponentBuilder withVSSChangeOverAggregate(float speedInMPH) {
        if (this._internalComponent == null)
            throw new IllegalStateException("Internal Component Builder is null. Can not generate scenario data.");
        if (!this._internalComponent._hasRegisteredToGenerate.get())
            throw new IllegalStateException("Component Builder has not registered to generate messages. Please call initializeMessageGeneration() first.");
        if (this._internalComponent._generationSettings == null)
            throw new IllegalStateException("Generation Settings on internal component builder is null. Please call initializeMessageGeneration() first.");

        this._internalComponent._generationSettings.SpeedToSet = speedInMPH;
        this._internalComponent._generationSettings.SpeedChangeIsDynamic = true;

        return this._internalComponent;
    }

    public ScenarioComponentBuilder withIgnitionState(boolean isIgnitionOn) {
        if (this._internalComponent == null)
            throw new IllegalStateException("Internal Component Builder is null. Can not generate scenario data.");
        if (!this._internalComponent._hasRegisteredToGenerate.get())
            throw new IllegalStateException("Component Builder has not registered to generate messages. Please call initializeMessageGeneration() first.");
        if (this._internalComponent._generationSettings == null)
            throw new IllegalStateException("Generation Settings on internal component builder is null. Please call initializeMessageGeneration() first.");

        this._internalComponent._generationSettings.IgnitionStateToSet = isIgnitionOn;

        return this._internalComponent;
    }

    public ScenarioComponentBuilder synthesizeComponent() {
        if (this._internalComponent == null)
            throw new IllegalStateException("Internal Component Builder is null. Can not generate scenario data.");
        if (!this._internalComponent._hasRegisteredToGenerate.get() || this._internalComponent._generationSettings == null)
            throw new IllegalStateException("This builder has not registered it's intent to generate messages, or has not provided settings for generation.");
        //Set the current HOS message if it hasn't been set already
        if (this._internalComponent._generationSettings._CurrentIHOSMessage == null)
            setStartingPoint();


        ArrayList<IHOSMessage> currentDataBlock = new ArrayList<>();

        if (this._internalComponent._generationSettings.IsMultiStep.compareAndSet(true, false)) {
            currentDataBlock.addAll(this._internalComponent._internalScenarioComponentCollection);
            for (ScenarioComponentBuilder builder : this._internalComponent._repeatComponentGenerationCollection) {
                //Use above data as starting data
                builder._internalScenarioComponentCollection = currentDataBlock;
                //Set the current HOS message == last message in data
                builder._generationSettings._CurrentIHOSMessage = builder._internalScenarioComponentCollection.get(builder._internalScenarioComponentCollection.size() - 1);
                //Get the current data with new loop, and set up for next iteration
                currentDataBlock.addAll(builder.generateMessages());
            }
        }
        currentDataBlock.addAll(this._internalComponent.generateMessages());


        this._internalComponent._internalScenarioComponentCollection.addAll(currentDataBlock);
        this._internalComponent._generationSettings._CurrentIHOSMessage = new HOSMessage(this._internalComponent._internalScenarioComponentCollection.get(this._internalComponent._internalScenarioComponentCollection.size() - 1));

        return this._internalComponent;
    }

    public ScenarioComponentBuilder step() {
        if (this._internalComponent == null)
            throw new IllegalStateException("Internal Component Builder is null. Can not generate scenario data.");
        if (!this._internalComponent._hasRegisteredToGenerate.get() || this._internalComponent._generationSettings == null)
            throw new IllegalStateException("This builder has not registered it's intent to generate messages, or has not provided settings for generation.");
        if (!this._internalComponent._generationSettings.IsMultiStep.get())
            throw new IllegalStateException("Can not mark series of commands as Step without first specifying chainForMultiStep().");

        this._internalComponent._repeatComponentGenerationCollection.add(new ScenarioComponentBuilder(this._internalComponent));
        return this._internalComponent;
    }

    public ScenarioComponentBuilder chainForMultiStep() {
        if (this._internalComponent == null)
            throw new IllegalStateException("Internal Component Builder is null. Can not generate scenario data.");
        if (this._internalComponent._generationSettings == null)
            throw new IllegalStateException("This builder has not provided settings for generation.");


        if (!this._internalComponent._generationSettings.IsMultiStep.compareAndSet(false, true)) {
            throw new IllegalStateException("Builder is already multi-step.");
        }

        //set the starting point, as normal synthesis process won't have set this yet
        if (this._internalComponent._generationSettings._CurrentIHOSMessage == null)
            setStartingPoint();

        this._internalComponent._repeatComponentGenerationCollection.add(new ScenarioComponentBuilder(this._internalComponent));
        return this.initializeMessageGeneration(this._internalComponent);
    }

    public ScenarioComponentBuilder chain() {
        if (this._internalComponent == null)
            throw new IllegalStateException("Internal Component Builder is null. Can not chain component.");
        if (!this._internalComponent._hasRegisteredToGenerate.get() || this._internalComponent._generationSettings == null)
            throw new IllegalStateException("This builder has not registered it's intent to generate messages, or has not provided settings for generation.");


        return this.initializeMessageGeneration(this._internalComponent);
    }

    private ScenarioComponentBuilder initializeMessageGeneration(ScenarioComponentBuilder existingComponentBuilder) {
        if (this._internalComponent == null)
            throw new IllegalStateException("Internal Component Builder is null. Can not chain builders.");
        this._internalComponent = new ScenarioComponentBuilder(existingComponentBuilder);
        return this._internalComponent;
    }

    private void setStartingPoint() {
        if (this._internalComponent._startingMessage != null) {
            this._internalComponent._generationSettings._CurrentIHOSMessage = new HOSMessage(this._internalComponent._startingMessage);
            if (this._internalComponent._generationSettings.IgnitionStateToSet == null)
                this._internalComponent._generationSettings.IgnitionStateToSet = this._internalComponent._generationSettings._CurrentIHOSMessage.isIgnitionOn();
            if (this._internalComponent._generationSettings.SpeedToSet == null)
                this._internalComponent._generationSettings.SpeedToSet = this._internalComponent._generationSettings._CurrentIHOSMessage.getSpeedometer();
            //Add initial message to collection
            this._internalComponent._internalScenarioComponentCollection.add(this._internalComponent._generationSettings._CurrentIHOSMessage);
            this._internalComponent._startingMessage = null;
        }
    }

    /**
     * Protected method that generates an ArrayList of IHOSMessages
     *
     * @return ArrayList of Moving IHOSMessages
     */
    private ArrayList<IHOSMessage> generateMessages() {
        int duration;
        Float speedChange = null;
        IHOSMessage hosMessage = new HOSMessage(this._internalComponent._generationSettings._CurrentIHOSMessage);

        if (this._internalComponent._generationSettings.DurationInNumberOfMessages != null) {
            duration = Math.round(this._internalComponent._generationSettings.DurationInNumberOfMessages * this._internalComponent._scenarioSettings.SimulatedPollingTime);
        } else {
            duration = this._internalComponent._generationSettings.DurationInSeconds;
        }

        if (this._internalComponent._generationSettings.SpeedChangeIsDynamic) {
            if (this._internalComponent._generationSettings.SpeedToSet == null)
                speedChange = 0.0f;
            else {
                speedChange = ((this._internalComponent._generationSettings.SpeedToSet - this._internalComponent._internalEngineModel.getVss()) / (duration / this._internalComponent._scenarioSettings.SimulatedPollingTime));
            }
        }


        //Change ignition state if needed
        if (this._internalComponent._generationSettings.IgnitionStateToSet)
            this._internalComponent._internalEngineModel.turnIgnitionOn();
        else
            this._internalComponent._internalEngineModel.turnIgnitionOff();

        ArrayList<IHOSMessage> hosMessages = new ArrayList<>();
        for (int i = 0; i < duration; i += this._internalComponent._scenarioSettings.SimulatedPollingTime) {
            //Synthesize 2 seconds for new event
            hosMessage = this.synthesizeTime(hosMessage, null);
            //Update Timekeeper to be timestamp of event we are cloning
            Calendar synthCalendar = Calendar.getInstance();
            synthCalendar.setTime(hosMessage.getTimestampUtc().toDate());
            this.Now(synthCalendar.get(Calendar.YEAR), synthCalendar.get(Calendar.MONTH), synthCalendar.get(Calendar.DAY_OF_MONTH),
                    synthCalendar.get(Calendar.HOUR_OF_DAY), synthCalendar.get(Calendar.MINUTE), synthCalendar.get(Calendar.SECOND), TimeZone.getDefault());
            /*
            Update speed. If this is an acceleration/deceleration event, do so over the duration.
            If this is constant speed, just call vss() and provide the settings value
             */
            if (speedChange != null) {
                this._internalComponent._internalEngineModel.vss(this._internalComponent._internalEngineModel.getVss() + speedChange);
            } else {
                this._internalComponent._internalEngineModel.vss(this._internalComponent._generationSettings.SpeedToSet);
            }


            float timeSpentTraveling = this._scenarioSettings.SimulatedPollingTime / SECONDSINHOUR;
            hosMessage = this.synthesizeEngineParams(hosMessage, timeSpentTraveling);
            hosMessages.add(hosMessage);
        }
        return hosMessages;
    }

    /**
     * Protected method that increments an event's time by the desired amount and returns a copy
     * of that incremented event
     *
     * @param hosMessage Event to advance chronologically
     * @return Returns an IHOSMessage that is a copy of the provided hosMessage, with time
     * incremented by the provided value
     */
    private IHOSMessage synthesizeTime(IHOSMessage hosMessage, Integer secondsToAdd) {
        int timeSpanSeconds = secondsToAdd == null ? (int) this._scenarioSettings.SimulatedPollingTime : secondsToAdd;
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        calendar.setTime(this.Now());


        IHOSMessage hosMessageReturn = new HOSMessage(hosMessage);
        calendar.add(Calendar.SECOND, timeSpanSeconds);
        hosMessageReturn.setTimestampUtc(new DateTime(calendar.getTime().getTime()));

        return hosMessageReturn;
    }

    /**
     * Protected method that modifies the engine params for an event based on distance traveled
     * and time spent traveling
     *
     * @param hosMessage         IHOSMessage to modify
     * @param timeSpentTraveling Time as a float representing the time spent traveling the provided
     *                           distance
     * @return Returns an IHOSMessage with updated properties
     */
    private IHOSMessage synthesizeEngineParams(IHOSMessage hosMessage, float timeSpentTraveling) {

        float distanceTraveled = this._internalComponent._internalEngineModel.getOdometer() - this._internalComponent._generationSettings._CurrentIHOSMessage.getOdometer();

        IHOSMessage returnMessage = new HOSMessage(hosMessage);

        returnMessage.setTachometer(this._internalComponent._internalEngineModel.getRpm());

        returnMessage.setSpeedometer(this._internalComponent._internalEngineModel.getVss());
        returnMessage.setOdometer(this._internalComponent._internalEngineModel.getOdometer());
        returnMessage.setIgnitionOn(this._internalComponent._internalEngineModel.isIgnitionOn());

        returnMessage.setTripOdometer(hosMessage.getTripOdometer() + distanceTraveled);

        float newLon = hosMessage.getGpsLongitude() + distanceTraveled / MILESINLONDEGREE;
        returnMessage.setGpsLongitude(newLon);

        returnMessage.setEngineHours(hosMessage.getEngineHours() + timeSpentTraveling);

        returnMessage.setTripEngineSeconds(hosMessage.getTripEngineSeconds() + ( timeSpentTraveling * SECONDSINHOUR) );

        return returnMessage;
    }

    public class GenerationSettings {
        public final AtomicBoolean IsMultiStep = new AtomicBoolean(false);
        public Integer DurationInSeconds;
        public Integer DurationInNumberOfMessages;
        public Float SpeedToSet;
        public Boolean IgnitionStateToSet;
        public Boolean SpeedChangeIsDynamic;
        protected IHOSMessage _CurrentIHOSMessage;

        public GenerationSettings() {
        }

        public GenerationSettings(GenerationSettings startingSettings) {
            this._CurrentIHOSMessage = new HOSMessage(startingSettings._CurrentIHOSMessage);
            this.DurationInNumberOfMessages = startingSettings.DurationInNumberOfMessages;
            this.DurationInSeconds = startingSettings.DurationInSeconds;
            this.SpeedToSet = startingSettings.SpeedToSet;
            this.IgnitionStateToSet = startingSettings.IgnitionStateToSet;
            this.SpeedChangeIsDynamic = startingSettings.SpeedChangeIsDynamic;
            this.IsMultiStep.set(startingSettings.IsMultiStep.get());
        }
    }
}
