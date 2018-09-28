package com.jjkeller.kmb.interfaces;

import com.jjkeller.kmbapi.employeelogeldevents.Enums;
import com.jjkeller.kmbapi.enums.DataDiagnosticEnum;
import com.jjkeller.kmbapi.enums.Malfunction;

import java.util.List;

public interface IAdminMalfunctionAndDataDiagnostic {
    interface AdminMalfunctionAndDataDiagnosticFragActions {
        void onAddMalfunctionClick(Malfunction selectedMalfunction);
        void onAddDataDiagnosticClick(DataDiagnosticEnum selectedDataDiagnostic);
        void onRemoveClick(Malfunction malfunction);
        void onRemoveClick(DataDiagnosticEnum dataDiagnostic);
        void onAddMalfunctionEventClick(Enums.EmployeeLogEldEventType eventType);
    }

    interface AdminMalfunctionAndDataDiagnosticMethods {
        List<Malfunction> getActiveMalfunctions();
        List<DataDiagnosticEnum> getActiveDataDiagnostics();
    }
}
