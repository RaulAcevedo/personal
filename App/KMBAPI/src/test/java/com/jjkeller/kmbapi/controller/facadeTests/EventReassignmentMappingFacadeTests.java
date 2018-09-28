package com.jjkeller.kmbapi.controller.facadeTests;


import com.jjkeller.kmbapi.common.TestBase;
import com.jjkeller.kmbapi.configuration.GlobalState;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import com.jjkeller.kmbapi.configuration.LoginCredentials;
import com.jjkeller.kmbapi.configuration.UserState;
import com.jjkeller.kmbapi.controller.ControllerFactory;
import com.jjkeller.kmbapi.controller.TeamDriverController;
import com.jjkeller.kmbapi.controller.dataaccess.EventReassignmentFacade;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.enums.DriverTypeEnum;
import com.jjkeller.kmbapi.enums.RuleSetTypeEnum;
import com.jjkeller.kmbapi.enums.TimeZoneEnum;
import com.jjkeller.kmbapi.featuretoggle.FeatureToggleService;
import com.jjkeller.kmbapi.proxydata.CompanyConfigSettings;
import com.jjkeller.kmbapi.proxydata.DrivingEventReassignmentMapping;

import org.junit.Before;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.booleanThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
// * Set of Integration Tests for the TeamDriverController
 */
@RunWith(RobolectricTestRunner.class)
@Config(application = GlobalState.class,
        manifest = "/build/intermediates/bundles/encompass/debug/AndroidManifest.xml"
)
public class EventReassignmentMappingFacadeTests extends TestBase {

    private GlobalState app;
    private FeatureToggleService ftService;
    private EventReassignmentFacade eventReassignmentFacade;
    private TeamDriverController teamDriverController;
    private Integer relatedEvent;

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        //set relatedEvent seed
        relatedEvent = 0;
        //Spin up GlobalState with a RoboElectric application
        app = (GlobalState) RuntimeEnvironment.application;
        //Mock up the feature toggle service, and enable Mandate mode
        ftService = mock(FeatureToggleService.class);
        when(ftService.getIsEldMandateEnabled()).thenReturn(true);

        //Make internal feature toggle field accessible, so as to directly
        //modify it in GlobalState and set our mock as the provider
        Field field = GlobalState.class.getDeclaredField("_featureToggleService");
        field.setAccessible(true);
        field.set(app, ftService);

        //Mock up a user and set standard user properties
        User user = mock(User.class);
        UserState userState = new UserState();
        when(user.getUserState()).thenReturn(userState);
        when(user.getHomeTerminalTimeZone()).thenReturn(TimeZoneEnum.CENTRAL_STANDARD_TIME);
        when(user.getRulesetTypeEnum()).thenReturn(new RuleSetTypeEnum(RuleSetTypeEnum.US60HOUR));
        when(user.getDriverType()).thenReturn(new DriverTypeEnum(DriverTypeEnum.PROPERTYCARRYING));

        //Mock up CompanyConfigSettings
        CompanyConfigSettings settings = new CompanyConfigSettings();
        settings.setDailyLogStartTime("00:00");
        settings.setDmoCompanyName("BrianTestCompany");
        app.setCompanyConfigSettings(app, settings);

        //Mock up login creds to return Guid.Empty and return our mocked
        //creds when queried by the user object
        LoginCredentials creds = mock(LoginCredentials.class);
        when(creds.getEmployeeId()).thenReturn("00000000-0000-0000-0000-000000000000");
        when(user.getCredentials()).thenReturn(creds);

        app.setCurrentUser(user);

        eventReassignmentFacade = new EventReassignmentFacade(app, user);

        //set our controller factory to use our faked facade
        teamDriverController = ControllerFactory.getInstance().getTeamDriverController();
    }


    @After
    public void tearDown() throws NoSuchFieldException, IllegalAccessException {
        closeDatabase(app);
    }


    /**
     * Public test method that inserts a single record, then validates it's presence using
     * all forms of Fetch
     */
    @Test
    public void assertSaveAndFetchByNaturalKey() {
        //Save one (1) record
        DrivingEventReassignmentMapping[] savedMappings = new DrivingEventReassignmentMapping[]{assertSaveRecordIsSuccessful()};
        //assert DB presence
        assertAreAllPresentInDb_ByNaturalKey(savedMappings);
        assertAreAllPresentInDb_ByRelatedEvent(savedMappings);
        assertAreAllPresentInDb_ByDriverId(savedMappings);
        assertAreAllPresentInDb_ByRecordId(savedMappings);
    }

    /**
     * Private method to save the record via the facade, bypassing the service call
     */
    private DrivingEventReassignmentMapping assertSaveRecordIsSuccessful() {
        String eventComment = "TestEventComment";
        Date eventEndDateTime = new Date();
        boolean isSubmitted = false;
        //persist a transition directly
        DrivingEventReassignmentMapping mapping = new DrivingEventReassignmentMapping();
        //Use employee we mocked up from set up
        mapping.setDriverToAssignEventTo(app.getCurrentUser().getCredentials().getEmployeeId());
        mapping.setRelatedEvent(relatedEvent++);
        mapping.setEventComment(eventComment);
        mapping.setIsSubmitted(isSubmitted);
        eventReassignmentFacade.Save(mapping);
        return mapping;
    }

    /**
     * Private method that asserts each provided mapping exists in the database
     *
     * @param mappings mappings to query for
     */
    private void assertAreAllPresentInDb_ByNaturalKey(DrivingEventReassignmentMapping[] mappings) {

        for (DrivingEventReassignmentMapping mapping : mappings) {
            DrivingEventReassignmentMapping dbMapping = eventReassignmentFacade.FetchByNaturalKey(mapping);
            assertNotNull(dbMapping);
            assertEquals(mapping.toString(), dbMapping.toString());
        }
    }

    /**
     * Private method that asserts a record is present in the DB by record Id
     * @param mappings set of Mappings to search for
     */
    private void assertAreAllPresentInDb_ByRecordId(DrivingEventReassignmentMapping[] mappings) {

        for (DrivingEventReassignmentMapping mapping : mappings) {
            DrivingEventReassignmentMapping dbMapping = eventReassignmentFacade.FetchByRecordId((int) mapping.getPrimaryKey());
            assertNotNull(dbMapping);
            assertEquals(mapping.toString(), dbMapping.toString());
        }
    }

    /**
     * Private method that asserts a record is present by checking for all records containing its
     * related event key
     * @param mappings set of Mappings to search for
     */
    private void assertAreAllPresentInDb_ByRelatedEvent(DrivingEventReassignmentMapping[] mappings) {
        //Get distinct RelatedEvents and # of occurrences
        HashMap<Integer, Integer> relatedEventCounter = new HashMap<>();
        for (DrivingEventReassignmentMapping mapping : mappings) {
            Integer keyCount = relatedEventCounter.get(mapping.getRelatedEvent());
            if (keyCount == null)
                relatedEventCounter.put(mapping.getRelatedEvent(), 1);
            else
                relatedEventCounter.put(mapping.getRelatedEvent(), keyCount++);
        }

        Iterator iterator = relatedEventCounter.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            ArrayList<DrivingEventReassignmentMapping> dbMappings = eventReassignmentFacade.FetchByRelatedEvent((int) entry.getKey());
            //Assert we got the same number of records back
            assertNotNull(dbMappings);
            assertEquals(entry.getValue(), dbMappings.size());
        }
    }

    /**
     * Private method that asserts a record is present by checking for all records containing its
     * driver id
     * @param mappings set of Mappings to search for
     */
    private void assertAreAllPresentInDb_ByDriverId(DrivingEventReassignmentMapping[] mappings) {
        //Get distinct DriverIds and # of occurrences
        HashMap<String, Integer> driverIdCounter = new HashMap<>();
        for (DrivingEventReassignmentMapping mapping : mappings) {
            Integer idCount = driverIdCounter.get(mapping.getDriverToAssignEventTo());
            if (idCount == null)
                driverIdCounter.put(mapping.getDriverToAssignEventTo(), 1);
            else
                driverIdCounter.put(mapping.getDriverToAssignEventTo(), idCount++);
        }

        Iterator iterator = driverIdCounter.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            ArrayList<DrivingEventReassignmentMapping> dbMappings = eventReassignmentFacade.FetchByDriverId((String)entry.getKey());
            //Assert we got the same number of records back
            assertNotNull(dbMappings);
            assertEquals(entry.getValue(), dbMappings.size());
        }
    }
}
