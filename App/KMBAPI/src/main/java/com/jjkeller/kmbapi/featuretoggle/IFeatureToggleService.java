package com.jjkeller.kmbapi.featuretoggle;

/**
 * Created by ief5781 on 9/2/16.
 */
public interface IFeatureToggleService {
    boolean getSetMobileStartTimestampToNull();

    boolean getSelectiveFeatureTogglesEnabled();

    boolean getIsEldMandateEnabled();

    boolean getIgnoreServerTime();

    boolean getShowDebugFunctions();

    boolean getUseCloudServices();

    boolean getDefaultTripInformation();

    boolean getAlkCopilotEnabled();

    boolean getPersonalConveyanceEnabled();

    boolean getHyrailEnabled();

    boolean getNonRegDrivingEnabled();

    boolean getGeotabInjectDataStallsEnabled();

    boolean getIgnoreFirmwareUpdate();

    boolean getForceComplianceTabletMode();

    boolean getIsForceCrashesEnabled();

    boolean getAutoAssignUnidentifiedELDEvents();

    void setSelectiveFeatureTogglesEnabled(boolean isEnabled);

    void setIsEldMandateEnabled(boolean isEnabled);

    void setIgnoreServerTime(boolean isEnabled);

    void setShowDebugFunctions(boolean isEnabled);

    void setUseCloudServices(boolean isEnabled);

    void setDefaultTripInformation(boolean isEnabled);

    void setAlkCopilotEnabled(boolean isEnabled);

    void setPersonalConveyanceEnabled(boolean isEnabled);


    void setIgnoreFirmwareUpdate(boolean isEnabled);

    void setForceComplianceTabletMode(boolean isEnabled);

    void setSetMobileStartTimestampToNull(boolean isEnabled);

    void setHyrailEnabled(boolean isEnabled);

    void setNonRegDrivingEnabled(boolean isEnabled);

    void setGeotabInjectDataStallsEnabled(boolean isEnabled);

    void setIsForceCrashesEnabled(boolean isEnabeled);
}
