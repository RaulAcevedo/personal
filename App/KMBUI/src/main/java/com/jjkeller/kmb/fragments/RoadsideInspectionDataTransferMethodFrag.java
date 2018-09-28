package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.jjkeller.kmb.interfaces.IRoadsideInspectionDataTransferMethod;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbapi.enums.RoadsideDataTransferMethodEnum;
import com.jjkeller.kmbui.R;

public class RoadsideInspectionDataTransferMethodFrag extends BaseFragment {

    private IRoadsideInspectionDataTransferMethod.RoadsideInspectionDataTransferMethodActions actionsListener;
    private Button btnOK;
    private RoadsideDataTransferMethodEnum selectedGroupOption;
    private EditText commentText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.f_roadsideinspectiondatatransfermethod, container, false);
        findControls(v);
        return v;
    }

    private void findControls(View v) {
        commentText = (EditText) v.findViewById(R.id.output_file_comment_text);
        // setting lines programmatically to force the single line edit text to display more than 1 line
        commentText.setLines(3);

        v.findViewById(R.id.ok_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionsListener.onTransferMethodOkButtonClick(selectedGroupOption, commentText.getText().toString());
            }
        });

        v.findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionsListener.onTransferMethodCancelButtonClick();
            }
        });

        btnOK = (Button) v.findViewById(R.id.ok_button);
        RadioGroup radioDataTransferGroup = (RadioGroup) v.findViewById(R.id.radio_datatransfer_group);
        radioDataTransferGroup.setOnCheckedChangeListener(
                new RadioGroup.OnCheckedChangeListener() {
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        if (checkedId == R.id.email_output_file) {
                            selectedGroupOption = new RoadsideDataTransferMethodEnum(RoadsideDataTransferMethodEnum.EMAIL);
                        } else if (checkedId == R.id.transfer_output_file_web_service) {
                            selectedGroupOption = new RoadsideDataTransferMethodEnum(RoadsideDataTransferMethodEnum.WEBSERVICE);
                        }
                        btnOK.setEnabled(true);
                    }
                }
        );
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            actionsListener = (IRoadsideInspectionDataTransferMethod.RoadsideInspectionDataTransferMethodActions) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement RoadsideInspectionDataTransferMethodActions");
        }
    }

}
