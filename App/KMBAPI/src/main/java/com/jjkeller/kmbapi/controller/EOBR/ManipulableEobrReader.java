package com.jjkeller.kmbapi.controller.EOBR;

import android.os.Bundle;

import com.jjkeller.kmbapi.controller.EOBR.datamanipulators.IDataManipulator;
import com.jjkeller.kmbapi.kmbeobr.EobrResponse;
import com.jjkeller.kmbapi.kmbeobr.StatusBuffer;
import com.jjkeller.kmbapi.kmbeobr.StatusRecord;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** The purpose of this class is to be able to inject "data manipulators" between
 *  the EOBR and the rest of the app.  Injecting this class into classes that depend
 *  on EobrReader will allow you to do things like fake bad odometer readings, etc.
 *  @see com.jjkeller.kmbapi.controller.EOBR.datamanipulators.IDataManipulator
 */
public class ManipulableEobrReader extends EobrReader {

    public enum SupportedMethod {
        Technician_GetDistHrs,
        GetStatusBuffer,
        Technician_GetCurrentData,
        Technician_GetHistoricalData,
    }

    private Map<SupportedMethod, List<IDataManipulator>> _manipulatorMap = new HashMap<>();

    public ManipulableEobrReader() {
        super();
    }

    public void addManipulator(SupportedMethod method, IDataManipulator manipulator) {
        List<IDataManipulator> manipulators = _manipulatorMap.get(method);
        if(manipulators == null) {
            manipulators = new ArrayList<>();
            _manipulatorMap.put(method, manipulators);
        }

        manipulators.add(manipulator);
    }

    public void removeManipulators(SupportedMethod method) {
        _manipulatorMap.remove(method);
    }

    public void removeManipulators() {
        _manipulatorMap.clear();
    }

    public void removeManipulator(SupportedMethod method, IDataManipulator manipulator) {
        List<IDataManipulator> manipulators = _manipulatorMap.get(method);
        if(manipulators != null)
            manipulators.remove(manipulator);
    }

    public void removeManipulator(IDataManipulator manipulator) {
        for(List<IDataManipulator> manipulators : _manipulatorMap.values())
            manipulators.remove(manipulator);
    }

    private <T> void applyManipulators(SupportedMethod method, T object) {
        List<IDataManipulator> manipulators = _manipulatorMap.get(method);
        if(manipulators != null) {
            for (IDataManipulator manipulator : manipulators) {
                if (manipulator.getType() == object.getClass()) {
                    manipulator.manipulate(object);
                }
            }
        }
    }

    @Override
    public Bundle Technician_GetDistHours(long timecode) {
        Bundle bundle = super.Technician_GetDistHours(timecode);

        applyManipulators(SupportedMethod.Technician_GetDistHrs, bundle);

        return bundle;
    }

    @Override
    public EobrResponse<StatusBuffer> GetStatusBuffer() {
        EobrResponse<StatusBuffer> response = super.GetStatusBuffer();

        applyManipulators(SupportedMethod.GetStatusBuffer, response);

        return response;
    }

    @Override
    public int Technician_GetCurrentData(StatusRecord statusRec, boolean updateRefTimestamp) {
        int rc = super.Technician_GetCurrentData(statusRec, updateRefTimestamp);

        applyManipulators(SupportedMethod.Technician_GetCurrentData, statusRec);

        return rc;
    }

    @Override
    public int Technician_GetHistoricalData(StatusRecord statusRec, Date timestamp) {
        int rc = super.Technician_GetHistoricalData(statusRec, timestamp);

        applyManipulators(SupportedMethod.Technician_GetHistoricalData, statusRec);

        return rc;
    }

    @Override
    public int Technician_GetHistoricalData(StatusRecord statusRec, int recordId) {
        int rc = super.Technician_GetHistoricalData(statusRec, recordId);

        applyManipulators(SupportedMethod.Technician_GetHistoricalData, statusRec);

        return rc;
    }
}
