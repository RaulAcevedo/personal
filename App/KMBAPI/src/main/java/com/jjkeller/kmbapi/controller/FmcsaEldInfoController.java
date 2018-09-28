package com.jjkeller.kmbapi.controller;

import android.content.Context;

import com.jjkeller.kmbapi.R;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.EOBR.EobrReader;
import com.jjkeller.kmbapi.controller.dataaccess.EldRegistrationInfoFacade;
import com.jjkeller.kmbapi.controller.dataaccess.FacadeFactory;
import com.jjkeller.kmbapi.controller.share.ControllerBase;
import com.jjkeller.kmbapi.controller.utility.RESTWebServiceHelper;
import com.jjkeller.kmbapi.kmbeobr.Constants;
import com.jjkeller.kmbapi.proxydata.EldRegistrationInfo;
import com.jjkeller.kmbapi.proxydata.EobrConfiguration;
import com.jjkeller.kmbapi.proxydata.FmcsaEldProviderInfo;
import com.jjkeller.kmbapi.proxydata.FmcsaEldRegistrationInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FmcsaEldInfoController extends ControllerBase {

    public FmcsaEldInfoController(Context context) {
        super(context);
    }

    public void SynchronizeFmcsaEldInfo() {

        RESTWebServiceHelper helper = null;

        try {
            EldRegistrationInfoFacade facade = new EldRegistrationInfoFacade(this.getContext(), this.getCurrentUser());
            Date updateDate = facade.FetchOldestChangeDate();
            if (updateDate != null) {
                helper = new RESTWebServiceHelper(getContext());
                FmcsaEldRegistrationInfo[] downloadedRegistrationInfos = helper.DownloadFmcsaEldRegistrationInfo(updateDate);

                if (downloadedRegistrationInfos == null || downloadedRegistrationInfos.length < 1) return;
            }

            // go get all of the registration infos to replace them all
            if (helper == null) helper = new RESTWebServiceHelper(getContext());
            FmcsaEldRegistrationInfo[] registrationInfos = helper.DownloadFmcsaEldRegistrationInfo(null);
            FmcsaEldProviderInfo providerInfo = helper.DownloadFmcsaEldProviderInfo();

            facade.DeleteAll();

            List<EldRegistrationInfo> infoList = new ArrayList<>();
            for (FmcsaEldRegistrationInfo info : registrationInfos) {
                EldRegistrationInfo newInfo = new EldRegistrationInfo();
                newInfo.setName(info.getName());
                newInfo.setEldIdentifier(info.getIdentifier());
                newInfo.setFirmwareType(info.getFirmwareType());
                newInfo.setMinAppVersion(info.getMinAppVersion());
                newInfo.setMaxAppVersion(info.getMaxAppVersion());
                newInfo.setFmcsaProviderName(providerInfo.getProviderName());
                newInfo.setFmcsaRegistrationId(info.getRegistrationId());

                infoList.add(newInfo);
            }

            facade.Save(infoList);
        } catch (IOException ex) {
            this.HandleException(ex, "SynchronizeFmcsaEldInfo");
        } catch (Exception ex) {
            this.HandleException(ex, "SynchronizeFmcsaEldInfo");
        }
    }

    /**
     * If connected to Eobr Device, return it's Serial Number;
     * If not connected to ELD, return the last ELD we were connected to.
     */
    public String getEobrDeviceSerialNumber() {
        if (EobrReader.getIsEobrDeviceAvailable()) {    // connected to ELD
            return EobrReader.getInstance().getEobrSerialNumber();
        }
        else {
            EobrConfiguration mostRecentConfig = FacadeFactory.GetInstance().getEobrConfigurationFacade(getContext()).FetchMostRecentlyConnectedEobr();
            if (mostRecentConfig != null) {
                return mostRecentConfig.getSerialNumber();
            }
        }

        return "";
    }

    private EldRegistrationInfo getCurrentRegistrationInfo(String eobrDeviceSerialNumber) {
        try{
            EobrConfiguration config = FacadeFactory.GetInstance().getEobrConfigurationFacade(getContext()).Fetch(eobrDeviceSerialNumber);

            // retrieve the registration info for this version/generation
            String firmwareType = "";
            if (config != null) {
                switch (config.getEobrGeneration()) {
                    case Constants.GENERATION_GEN_I: {
                        // GEN I
                        firmwareType = "Gen 1";
                        break;
                    }
                    case Constants.GENERATION_GEN_II: {
                        // Bruce Lightner: Until the KMB app starts reporting that information plus the latest ELD firmware gets loaded onto all the ELD's out there
                        // the serial no. format probably is the only way to know the difference.  The serial no. format of Gen 2 and Gen 2.5 are different.
                        // Examples include Gen 2: 113499-0797 and Gen 2.5: 50096-0053097.  However, once Gen 2.5.1 devices are fielded then the serial no. "format" likely will not be enough.
                        String[] pieces = eobrDeviceSerialNumber.split("-");
                        if (pieces.length == 2 && pieces[0].length() == 5 && pieces[1].length() == 7) {
                            firmwareType = "Gen 2.5";
                        } else {
                            // GEN II
                            firmwareType = "Gen 2";
                        }
                        break;
                    }
                    case Constants.GENERATION_GEOTAB: {
                        // GEN GO7
                        firmwareType = "GO7";
                        break;
                    }
                }
            }

            EldRegistrationInfoFacade facade = new EldRegistrationInfoFacade(getContext(), this.getCurrentUser());
            List<EldRegistrationInfo> eldRegistrationInfo = facade.fetchRegistrationInfoByTypeAndVersion(firmwareType, GlobalState.getInstance().getPackageVersionName());

            if (eldRegistrationInfo.isEmpty()){
                eldRegistrationInfo = facade.fetchDefaultRegistrationInfoByType("Gen 2");
            }

            return eldRegistrationInfo.isEmpty() ? null : eldRegistrationInfo.get(0);
        } catch (Exception ex){
            this.HandleException(ex, "FmcsaEldInfoController.getCurrentRegistrationIdentifier");
        }

        return null;
    }

    public String getCurrentRegistrationIdentifier(String eobrDeviceSerialNumber){
        EldRegistrationInfo eldRegistrationInfo = getCurrentRegistrationInfo(eobrDeviceSerialNumber);
        if (eldRegistrationInfo != null){
            return eldRegistrationInfo.getFmcsaRegistrationId();
        }

        return getContext().getString(R.string.eld_registration_id);
    }

    public String getCurrentEldIdentifier(String eobrDeviceSerialNumber){
        EldRegistrationInfo eldRegistrationInfo = getCurrentRegistrationInfo(eobrDeviceSerialNumber);
        if (eldRegistrationInfo != null){
            return eldRegistrationInfo.getEldIdentifier();
        }

        return getContext().getString(R.string.eld_registration_id);
    }
}
