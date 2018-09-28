package com.jjkeller.kmb.interfaces;

import com.jjkeller.kmbapi.enums.InspectionDefectType;
import com.jjkeller.kmbapi.enums.InspectionTypeEnum;
import com.jjkeller.kmbapi.proxydata.EobrConfiguration;
import com.jjkeller.kmbapi.proxydata.VehicleInspection;

import java.util.Date;
import java.util.List;

public interface IVehicleInspectionCreate {
    public interface VehicleInspectionCreateControllerMethods {
    	public boolean DoesInspectionContainDefect(InspectionDefectType defect);
    	public void AddDefectToInspection(InspectionDefectType defect);
    	public void RemoveDefectFromInspection(InspectionDefectType defect);
    	public int[] GetSerializableDefectList();
        public void PutSerializableDefectList(int[] defectList);
        public void AssignInspectionDate(Date inspectionDate);
        public void StartInspection(InspectionTypeEnum inspectionType, boolean isPoweredUnit);
        public VehicleInspection getCurrentVehicleInspection();
        public List<EobrConfiguration> AllEobrDevices();
    }
}
