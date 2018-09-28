package com.jjkeller.kmb;

import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.enums.SpecialDrivingCategoryConfigurationMessageEnum;
import com.jjkeller.kmbui.R;

public class ConfigurationSpecialDrivingCategory {
    private BaseActivity _activity;
    private User _user;

    public ConfigurationSpecialDrivingCategory(BaseActivity activity) {
        _activity = activity;
        _user = GlobalState.getInstance().getCurrentUser();
    }

    public String getSpecialDrivingCategoryConfigurationMessage() {
        if (_user.getSpecialDrivingCategoryConfigurationMessageEnum()
                == SpecialDrivingCategoryConfigurationMessageEnum.ACTIVATION) {
            return ActivationSpecialDrivingCategoryMessage();
        }

        if (_user.getSpecialDrivingCategoryConfigurationMessageEnum()
                != SpecialDrivingCategoryConfigurationMessageEnum.NONE) {
            return ConfigurationsSpecialDrivingCategoryMessage();
        }

        return null;
    }

    private String ConfigurationsSpecialDrivingCategoryMessage() {
        StringBuilder message = new StringBuilder(_activity.getString(R.string.msg_configurationchangedspecialdrivingcategory));

        switch (_user.getSpecialDrivingCategoryConfigurationMessageEnum()) {
            case NONE:
            case ACTIVATION:
                InitializeSpecialDrivingCategoryConfigurationMessageEnum();
                return null;
            case PERSONALCONVEYANCE:
                message.append(_activity.getString(R.string.msg_personalconveyanceconfiguration)
                        + getEnabledDisabled(_user.getIsPersonalConveyanceAllowed()));
                break;
            case YARDMOVE:
                message.append(_activity.getString(R.string.msg_yardmoveconfiguration)
                        + getEnabledDisabled(_user.getYardMoveAllowed()));
                break;
            case PERSONALCONVEYANCEANDYARDMOVE:
                message.append(_activity.getString(R.string.msg_personalconveyanceconfiguration)
                        + getEnabledDisabled(_user.getIsPersonalConveyanceAllowed()));
                message.append("\n");
                message.append(_activity.getString(R.string.msg_yardmoveconfiguration)
                        + getEnabledDisabled(_user.getYardMoveAllowed()));
                break;
        }

        InitializeSpecialDrivingCategoryConfigurationMessageEnum();
        return message.toString();
    }

    private String ActivationSpecialDrivingCategoryMessage() {
        StringBuilder message = new StringBuilder(_activity.getString(R.string.msg_configurationinitializedspecialdrivingcategory));

        message.append(_activity.getString(R.string.msg_personalconveyanceconfiguration)
                + getEnabledDisabled(_user.getIsPersonalConveyanceAllowed()));
        message.append("\n");
        message.append(_activity.getString(R.string.msg_yardmoveconfiguration)
                + getEnabledDisabled(_user.getYardMoveAllowed()));

        InitializeSpecialDrivingCategoryConfigurationMessageEnum();
        return message.toString();
    }

    private void InitializeSpecialDrivingCategoryConfigurationMessageEnum() {
        _user.setSpecialDrivingCategoryConfigurationMessageEnum(SpecialDrivingCategoryConfigurationMessageEnum.NONE);
    }

    private String getEnabledDisabled(boolean value) {
        return value ? _activity.getString(R.string.msg_enabled) : _activity.getString(R.string.msn_disabled);
    }
}
