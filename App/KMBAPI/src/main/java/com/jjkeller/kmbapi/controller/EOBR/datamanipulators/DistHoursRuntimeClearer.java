package com.jjkeller.kmbapi.controller.EOBR.datamanipulators;

import android.content.Context;
import android.os.Bundle;

import com.jjkeller.kmbapi.R;
import com.jjkeller.kmbapi.controller.EOBR.ManipulableEobrReader;

/**
 * Created by ief5781 on 4/24/17.
 */

public class DistHoursRuntimeClearer implements IDataManipulator<Bundle> {
    private Context context;

    public DistHoursRuntimeClearer(Context context) {
        this.context = context;
    }

    @Override
    public Class<Bundle> getType() {
        return Bundle.class;
    }

    @Override
    public void manipulate(Bundle data) {
        data.putInt(context.getString(R.string.runtime), -60);
    }

    @Override
    public void register(ManipulableEobrReader eobrReader) {
        eobrReader.addManipulator(ManipulableEobrReader.SupportedMethod.Technician_GetDistHrs, this);
    }

    @Override
    public void unregister(ManipulableEobrReader eobrReader) {
        eobrReader.removeManipulator(ManipulableEobrReader.SupportedMethod.Technician_GetDistHrs, this);
    }
}
