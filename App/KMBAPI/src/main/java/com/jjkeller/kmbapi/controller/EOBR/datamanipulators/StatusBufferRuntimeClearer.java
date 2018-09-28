package com.jjkeller.kmbapi.controller.EOBR.datamanipulators;

import com.jjkeller.kmbapi.controller.EOBR.ManipulableEobrReader;
import com.jjkeller.kmbapi.kmbeobr.EobrResponse;
import com.jjkeller.kmbapi.kmbeobr.StatusBuffer;

/**
 * Created by ief5781 on 4/24/17.
 */

public class StatusBufferRuntimeClearer implements IDataManipulator<EobrResponse> {

    @Override
    public Class<EobrResponse> getType() {
        return EobrResponse.class;
    }

    @Override
    public void manipulate(EobrResponse data) {
        StatusBuffer buffer = StatusBuffer.class.isInstance(data.getData()) ? (StatusBuffer)data.getData() : null;

        if(buffer != null) {
            buffer.setEngineOnTimeSeconds(-3600);
            buffer.setRunTimeSeconds(-3600);
        }
    }

    @Override
    public void register(ManipulableEobrReader eobrReader) {
        eobrReader.addManipulator(ManipulableEobrReader.SupportedMethod.GetStatusBuffer, this);
    }

    @Override
    public void unregister(ManipulableEobrReader eobrReader) {
        eobrReader.removeManipulator(ManipulableEobrReader.SupportedMethod.GetStatusBuffer, this);
    }
}
