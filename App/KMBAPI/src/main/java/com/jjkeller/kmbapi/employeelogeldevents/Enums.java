package com.jjkeller.kmbapi.employeelogeldevents;

import android.content.Context;
import com.jjkeller.kmbapi.R;

public class Enums {

	public enum EmployeeLogEldEventType {
		DutyStatusChange(1, 400),
		IntermediateLog(2, 500),
		ChangeInDriversIndication(3, 300),
		Certification(4, 600),
		LoginLogout(5, 200),
		EnginePowerUpPowerDown(6, 100),
		Malfunction_DataDiagnosticDetection(7, 700);


		public static final int ARRAYID = R.array.eldeventtype_array;

		public static String[] getList(Context ctx) {
			return ctx.getResources().getStringArray(ARRAYID);
		}

		private int id;
		private int sortOrder;

		EmployeeLogEldEventType(int id, int sortOrder) {
			this.id = id;
			this.sortOrder = sortOrder;
		}

		public int getValue() { return id; }

		public int getSortOrder(){
			return sortOrder;
		}

		public static EmployeeLogEldEventType setFromInt(int i) {
			for (EmployeeLogEldEventType b : EmployeeLogEldEventType .values()) {
				if (b.getValue() == i) { return b; }
			}
			return null;
		}

		public static EmployeeLogEldEventType valueOf(Context ctx, String name) throws IllegalArgumentException
		{
			String[] array = ctx.getResources().getStringArray(ARRAYID);

			for (int index = 0; index < array.length; index++) {
				if (name.compareTo(array[index]) == 0)
					return setFromInt(index + 1); //Convert base 0 array to event type value
			}

			throw new IllegalArgumentException("Enum value undefined");
		}
	}
	
	public enum CompositeEmployeeLogEldEventTypeEventCodeEnum {

		OffDutyEvent,
		SleeperEvent,
		DrivingEvent,
		OnDutyEvent,
		//IntermediateLog has Conventional and Reduced.
		IntermediateLog,
		DriverIndicatesPersonalUseEvent,
		DriverIndicatesYardMoveEvent,
		DriverIndicates_PCYMWT_CleardEvent,
		CertificationEvent,
		LoginEvent,
		LogoutEvent,
		//EnginePowerEvent has Conventional and Reduced
		EnginePowerUpEvent,
		//EngineShutDown has Conventional and Reduced
		EngineShutDownEvent,
		EldMalfunctionLoggedEvent,
		EldMalfunctionCleardEvent,
		DataDiagnosticLoggedEvent,
		DataDiagnosticCleardEvent
	}
	
	public enum EmployeeLogEldEventRecordStatus {
		Active(1),
		InactiveChanged(2),
		InactiveChangeRequested(3),
		InactiveChangeRejected(4);

		private final int id;
		EmployeeLogEldEventRecordStatus(int id) { this.id = id; }

		public int getValue() { return id; }

		public static EmployeeLogEldEventRecordStatus setFromInt(int i) {
			for (EmployeeLogEldEventRecordStatus b : EmployeeLogEldEventRecordStatus .values()) {
				if (b.getValue() == i) { return b; }
			}
			return null;
		}

	}
	
	public class EmployeeLogEldEventRecordOrigin {
		public static final int AutomaticallyRecorded = 1;
		public static final int EditedEnteredByDriver = 2;
		public static final int EditRequestedByAuthenticatedUser = 3;
		public static final int AssumedUnidentifiedDriver = 4;
	}

	public enum SpecialDrivingCategory{
		None,
		PersonalConveyance,
		YardMove,
		Hyrail,
		NonRegulated
	}

	/**
	 * Ability to pass saveEldEvent an indicator as to where the save
	 * is coming from so if necessary, conditional logic can execute
	 */
	public enum ActionInitiatingSaveEnum {
		EditLog,
		ClaimUnidentifiedEvent
	}
}
