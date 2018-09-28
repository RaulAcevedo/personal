package com.jjkeller.kmbapi.featuretoggle;

    import com.jjkeller.kmbapi.configuration.AppSettings;
/**
 * Created by BJA6001 on 11/24/2015.
 */
public class SetMobileStartTimestampToNull extends FeatureToggle<SetMobileStartTimestampToNull> {
    public SetMobileStartTimestampToNull(AppSettings _appSettings){

        super(SetMobileStartTimestampToNull.class, _appSettings);
    }

}
