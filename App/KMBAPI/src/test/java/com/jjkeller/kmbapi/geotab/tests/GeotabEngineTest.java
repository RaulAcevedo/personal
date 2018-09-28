package com.jjkeller.kmbapi.geotab.tests;

import android.content.Context;

import com.jjkeller.kmbapi.HosMessageProcessor.interfaces.IHOSMessage;
import com.jjkeller.kmbapi.HosMessageProcessor.interfaces.IVehicleStateMachine;
import com.jjkeller.kmbapi.HosMessageProcessor.stateMachine.VehicleStateMachine;
import com.jjkeller.kmbapi.configuration.AppSettings;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.GeotabController;
import com.jjkeller.kmbapi.controller.dataaccess.EobrConfigurationFacade;
import com.jjkeller.kmbapi.controller.dataaccess.FacadeFactory;
import com.jjkeller.kmbapi.controller.interfaces.IFacadeFactory;
import com.jjkeller.kmbapi.featuretoggle.FeatureToggleService;
import com.jjkeller.kmbapi.featuretoggle.IFeatureToggleService;
import com.jjkeller.kmbapi.geotabengine.GeotabDataEnhanced;
import com.jjkeller.kmbapi.geotabengine.GeotabEngine;
import com.jjkeller.kmbapi.geotabengine.GeotabMessageProcessor;
import com.jjkeller.kmbapi.geotabengine.GeotabUsbService;
import com.jjkeller.kmbapi.geotabengine.HOSMessage;
import com.jjkeller.kmbapi.geotabengine.events.DriveOnEventRecord;
import com.jjkeller.kmbapi.geotabengine.events.GeoTabSyntheticEventRecordData;
import com.jjkeller.kmbapi.geotabengine.events.IgnOnEventRecord;
import com.jjkeller.kmbapi.geotabengine.events.MoveStartEventRecord;
import com.jjkeller.kmbapi.geotabengine.interfaces.IGeotabMessageProcessor;
import com.jjkeller.kmbapi.kmbeobr.EventRecord;
import com.jjkeller.kmbapi.common.TestBase;
import com.jjkeller.kmbapi.geotab.HosMessages;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

/**
 * Created by ief5781 on 11/15/16.
 */
@RunWith(RobolectricTestRunner.class)
@Config(application = GlobalState.class,
        manifest = "/build/intermediates/bundles/encompass/debug/AndroidManifest.xml"
)
public class GeotabEngineTest extends TestBase {
    private GlobalState app;

    @Before
    public void init() throws NoSuchFieldException, IllegalAccessException {
        MockitoAnnotations.initMocks(this);
        app = (GlobalState) RuntimeEnvironment.application;

        FeatureToggleService ftService = mock(FeatureToggleService.class);
        when(ftService.getIsEldMandateEnabled()).thenReturn(false);
    }

    @Captor
    private ArgumentCaptor<List<EventRecord>> eventCaptor;

    class UniqueTimecodeVerifier extends ArgumentMatcher<List<EventRecord>> {
        @Override
        public boolean matches(Object argument) {
            List<EventRecord> list = (ArrayList<EventRecord>)argument;

            Set<Long> timecodes = new HashSet<>();
            for(EventRecord event : list) {
                if(timecodes.contains(event.getTimecode()))
                    return false;
                timecodes.add(event.getTimecode());
            }

            return true;
        }
    }
    
    @Test
    public void receiveMessagesThatCreateMultipleEvents_MakesEventsHaveUniqueTimecode() {
        IFeatureToggleService featureToggleService = mock(FeatureToggleService.class);
        IVehicleStateMachine stateMachine = mock(VehicleStateMachine.class);
        GeotabUsbService usbService = mock(GeotabUsbService.class);
        Context ctx = mock(Context.class);
        GeotabController geotabController = mock(GeotabController.class);

        IGeotabMessageProcessor processor = new GeotabMessageProcessor(featureToggleService, stateMachine);
        Date now = Now();

        IHOSMessage message = HosMessages.EngineOnMoving(now);
        ArrayList<EventRecord> events = new ArrayList<>();
        events.add(new IgnOnEventRecord(message, new GeoTabSyntheticEventRecordData(0)));
        events.add(new MoveStartEventRecord(message, new GeoTabSyntheticEventRecordData(0)));
        events.add(new DriveOnEventRecord(message, new GeoTabSyntheticEventRecordData(0), message));

        when(stateMachine.processMessage(isA(HOSMessage.class)))
                .thenReturn(events);

        GeotabDataEnhanced data = new GeotabDataEnhanced();
        data.setDatetime(message.getTimestampUtc().getMillis());
        data.setStatus((byte)(1 << 3));

        IFacadeFactory facadeFactory = mock(FacadeFactory.class);
        when(facadeFactory.getEobrConfigurationFacade(isA(Context.class)))
                .thenReturn(mock(EobrConfigurationFacade.class));

        AppSettings appSettings = mock(AppSettings.class);

        GeotabEngine engine = spy(new GeotabEngine(ctx, processor, featureToggleService, usbService, geotabController, facadeFactory, appSettings));
        doNothing().when(engine).SubmitGeotabDriverChange();

        engine.receiveGeotabData(data);

         verify(geotabController).SaveEventsAndHosDataForDriver(anyString(), argThat(new UniqueTimecodeVerifier()), isA(HOSMessage.class));
    }

    @Test
    public void receiveMultipleMessagesThatCreateEventsWithSameTimecode_MakesEventsHaveUniqueTimecode() {
        IFeatureToggleService featureToggleService = mock(FeatureToggleService.class);
        IVehicleStateMachine stateMachine = mock(VehicleStateMachine.class);
        GeotabUsbService usbService = mock(GeotabUsbService.class);
        Context ctx = mock(Context.class);
        GeotabController geotabController = mock(GeotabController.class);

        IGeotabMessageProcessor processor = new GeotabMessageProcessor(featureToggleService, stateMachine);

        Date now = Now();
        IHOSMessage moveMessage = HosMessages.EngineOnMoving(now);
        EventRecord moveEvent = new MoveStartEventRecord(moveMessage, new GeoTabSyntheticEventRecordData(0));

        //the message doesn't matter, what does matter is the timestamp passed in
        //both events should have the same time
        EventRecord driveEvent = new DriveOnEventRecord(moveMessage, new GeoTabSyntheticEventRecordData(0), moveMessage);

        when(stateMachine.processMessage(isA(HOSMessage.class)))
                .thenReturn(new ArrayList<EventRecord>(Arrays.asList(moveEvent)))
                .thenReturn(new ArrayList<EventRecord>(Arrays.asList(driveEvent)));

        GeotabDataEnhanced data = new GeotabDataEnhanced();
        data.setDatetime(moveMessage.getTimestampUtc().getMillis());
        data.setStatus((byte)(1 << 3));

        IFacadeFactory facadeFactory = mock(FacadeFactory.class);
        when(facadeFactory.getEobrConfigurationFacade(isA(Context.class)))
                .thenReturn(mock(EobrConfigurationFacade.class));

        AppSettings appSettings = mock(AppSettings.class);

        GeotabEngine engine = spy(new GeotabEngine(ctx, processor, featureToggleService, usbService, geotabController, facadeFactory, appSettings));
        doNothing().when(engine).SubmitGeotabDriverChange();

        //process 2 messages - one for move, one for drive
        engine.receiveGeotabData(data);
        engine.receiveGeotabData(data);

        verify(geotabController, times(2)).SaveEventsAndHosDataForDriver(anyString(), eventCaptor.capture(), isA(HOSMessage.class));

        ArrayList<EventRecord> events = new ArrayList<>();
        events.addAll(eventCaptor.getAllValues().get(0));
        events.addAll(eventCaptor.getAllValues().get(1));

        boolean uniqueTimecodes = new UniqueTimecodeVerifier().matches(events);

        Assert.assertTrue("Timecodes should be unique", uniqueTimecodes);
    }
}
