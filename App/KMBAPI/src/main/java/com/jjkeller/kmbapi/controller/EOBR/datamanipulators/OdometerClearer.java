package com.jjkeller.kmbapi.controller.EOBR.datamanipulators;

import com.jjkeller.kmbapi.controller.EOBR.ManipulableEobrReader;
import com.jjkeller.kmbapi.kmbeobr.StatusRecord;

/**
 * Created by ief5781 on 4/24/17.
 */

public class OdometerClearer implements IDataManipulator<StatusRecord> {

    @Override
    public Class<StatusRecord> getType() {
        return StatusRecord.class;
    }

    @Override
    public void manipulate(StatusRecord data) {
        data.setOdometerReading(-1);
        data.setTachometer(1000);
    }

    @Override
    public void register(ManipulableEobrReader eobrReader) {
        eobrReader.addManipulator(ManipulableEobrReader.SupportedMethod.Technician_GetCurrentData, this);
        eobrReader.addManipulator(ManipulableEobrReader.SupportedMethod.Technician_GetHistoricalData, this);
    }

    @Override
    public void unregister(ManipulableEobrReader eobrReader) {
        eobrReader.removeManipulator(ManipulableEobrReader.SupportedMethod.Technician_GetCurrentData, this);
        eobrReader.removeManipulator(ManipulableEobrReader.SupportedMethod.Technician_GetHistoricalData, this);
    }
}
