package com.jjkeller.kmbapi.controller.utility;

import android.content.Context;

import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.interfaces.IIoTHubSettingsCreator;
import com.jjkeller.kmbapi.controller.share.IoTHubSettings;
import com.jjkeller.kmbapi.proxydata.EngineRecordList;
import com.jjkeller.kmbapi.proxydata.EventDataRecordList;
import com.jjkeller.kmbapi.proxydata.MobileDevice;
import com.jjkeller.kmbapi.proxydata.RoutePositionList;
import com.jjkeller.kmbapi.proxydata.TripRecordList;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AzureIoTHubServiceHelper implements IWebAPIServiceHelper {

    private static IoTHubSettings iotHubSettings;

    private final IIoTHubSettingsCreator iotHubSettingsCreator;
    private Context _context;

    public AzureIoTHubServiceHelper(IIoTHubSettingsCreator iotHubSettingsCreator, Context context) {
        this._context = context;
        this.iotHubSettingsCreator = iotHubSettingsCreator;
    }

    @Override
    public void SubmitEventDataRecords(EventDataRecordList eventDataRecords) throws IOException {
        IoTHubSettings settings = createIoTHubSettings();

        IotDataSerialization serializer = new IotDataSerialization();
        String jsonBody = serializer.getGson().toJson(eventDataRecords);

        Map<String,String> headers = new HashMap<>();

        headers.put("Authorization", settings.getToken());
        headers.put("iothub-app-companyid", GlobalState.getInstance().getCompanyConfigSettings(_context).getDmoCompanyId());
        headers.put("iothub-app-messagetype", "eventdata");

        HttpHelper http = new HttpHelper(_context);
        http.Post(settings.getEventsUri(),headers,jsonBody);
    }

    @Override
    public void SubmitEngineRecords(EngineRecordList engineRecords) throws IOException{
        IoTHubSettings settings = createIoTHubSettings();

        IotDataSerialization serializer = new IotDataSerialization();
        String jsonBody = serializer.getGson().toJson(engineRecords);

        Map<String,String> headers = new HashMap<>();

        headers.put("Authorization", settings.getToken());
        headers.put("iothub-app-companyid", GlobalState.getInstance().getCompanyConfigSettings(_context).getDmoCompanyId());
        headers.put("iothub-app-messagetype", "enginerecord");

        HttpHelper http = new HttpHelper(_context);
        http.Post(settings.getEventsUri(),headers,jsonBody);
    }

    @Override
    public void SubmitRoutePositions(RoutePositionList routePositions) throws IOException{
        IoTHubSettings settings = createIoTHubSettings();

        IotDataSerialization serializer = new IotDataSerialization();
        String jsonBody = serializer.getGson().toJson(routePositions);

        Map<String,String> headers = new HashMap<>();

        headers.put("Authorization", settings.getToken());
        headers.put("iothub-app-companyid", GlobalState.getInstance().getCompanyConfigSettings(_context).getDmoCompanyId());
        headers.put("iothub-app-messagetype", "routeposition");

        HttpHelper http = new HttpHelper(_context);
        http.Post(settings.getEventsUri(),headers,jsonBody);
    }

    @Override
    public void SubmitTripRecords(TripRecordList tripRecords) throws IOException{
        IoTHubSettings settings = createIoTHubSettings();

        IotDataSerialization serializer = new IotDataSerialization();
        String jsonBody = serializer.getGson().toJson(tripRecords);

        Map<String,String> headers = new HashMap<>();

        headers.put("Authorization", settings.getToken());
        headers.put("iothub-app-companyid", GlobalState.getInstance().getCompanyConfigSettings(_context).getDmoCompanyId());
        headers.put("iothub-app-messagetype", "triprecord");

        HttpHelper http = new HttpHelper(_context);
        http.Post(settings.getEventsUri(),headers,jsonBody);
    }



    private IoTHubSettings createIoTHubSettings() throws IOException {
        if (isIoTHubSettingsValid(iotHubSettings)) {
            return iotHubSettings;
        } else {
            MobileDevice mobileDevice = MobileDevice.forCurrentDevice(GlobalState.getInstance());
            iotHubSettings = iotHubSettingsCreator.CreateIoTHubSettings(mobileDevice);
            return iotHubSettings;
        }
    }

    static boolean isIoTHubSettingsValid(IoTHubSettings settings) {
        return settings != null && settings.getExpirationUtc() != null && DateUtility.getCurrentDateTime().isBefore(settings.getExpirationUtc());
    }

}
