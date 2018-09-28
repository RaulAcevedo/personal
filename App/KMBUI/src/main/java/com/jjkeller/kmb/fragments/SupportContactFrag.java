package com.jjkeller.kmb.fragments;

import android.os.Bundle;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbapi.controller.utility.DeviceInfo;
import com.jjkeller.kmbui.R;

/**
 * Created by T000682 on 2/25/2018.
 */

public class SupportContactFrag extends BaseFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.f_supportcontact, container, false);

        TextView supportWebsite = (TextView) v.findViewById(R.id.support_contact_website);
        TextView supportEmail = (TextView) v.findViewById(R.id.support_contact_email);
        TextView supportPhone = (TextView) v.findViewById(R.id.support_contact_phone);

        if(!DeviceInfo.IsComplianceTablet()){
            supportWebsite.setAutoLinkMask(Linkify.WEB_URLS);
            supportEmail.setAutoLinkMask(Linkify.EMAIL_ADDRESSES);
            supportPhone.setAutoLinkMask(Linkify.PHONE_NUMBERS);

            supportWebsite.setText(supportWebsite.getText());
            supportEmail.setText(supportEmail.getText());
            supportPhone.setText(supportPhone.getText());
        }

        return v;
    }
}
