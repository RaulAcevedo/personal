package com.jjkeller.kmbapi.controller.utility;

import com.jjkeller.kmbapi.employeelogeldevents.Enums;
import com.jjkeller.kmbapi.proxydata.EldEventAdapter;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;

public class GenericEventComparer implements Comparator<EldEventAdapter>, Serializable{
	@Override
	public int compare(EldEventAdapter a, EldEventAdapter b) {
		return this.compareForEldEvent(a, b);
	}

	private static final int EQUAL = 0;
	private static final int BEFORE = -1;
	private static final int AFTER = 1;

	private boolean isNull(Object o1, Object o2) {
		return o1 == null || o2 == null;
	}

	private int getCompareValueForNullConditions(Object o1, Object o2) {
		if (o1 == null && o2 == null) {
			return EQUAL;
		} else if (o1 == null) {
			return AFTER;
		} else if (o2 == null) {
			return BEFORE;
		} else {
			throw new RuntimeException("attempted to set compare value for non null object");
		}
	}

	private int compareInts(int a, int b) {
		return a > b ? +1 : a < b ? -1 : 0;
	}

	/**
	 * CompareTo implementation for EldEvents. Determines equality based on core properties, and if
	 * events are different types, orders them
	 *
	 * @param t  EldEventAdapter 1
	 * @param t1 EldEventAdapter 2
	 * @return int representing location or equality (Before (-), After (+), or Equal (0))
	 */
	private int compareForEldEvent(EldEventAdapter t, EldEventAdapter t1) {
		int returnVal;
		if (isNull(t, t1)) {
			returnVal = getCompareValueForNullConditions(t, t1);
		}else {

			EmployeeLogEldEvent typedAdapter1 = (EmployeeLogEldEvent) t, typedAdapter2 = (EmployeeLogEldEvent) t1;
			Date evt1Date = typedAdapter1.getEventDateTime();
			Date evt2Date = typedAdapter2.getEventDateTime();

			//CheckByDates
			if (isNull(evt1Date, evt2Date)) {
				returnVal = getCompareValueForNullConditions(evt1Date, evt2Date);
			} else {
				returnVal = evt1Date.compareTo(evt2Date);
			}

			//Compare event type tiebreaker 1
			if (returnVal == EQUAL) {
				if (isNull(typedAdapter1.getEventType(), typedAdapter2.getEventType())) {
					returnVal = getCompareValueForNullConditions(typedAdapter1.getEventType(), typedAdapter2.getEventType());
				} else {
					returnVal = compareInts(typedAdapter1.getEventType().getSortOrder(), typedAdapter2.getEventType().getSortOrder());
				}
			}

			//compare event code tiebreaker 2
			if (returnVal == EQUAL) {
				if (typedAdapter1.getEventType().getValue() == Enums.EmployeeLogEldEventType.ChangeInDriversIndication.getValue()) {
					//do this type descending because the clear code is = 0
					returnVal = compareInts(typedAdapter2.getEventCode(), typedAdapter1.getEventCode());
				} else {
					returnVal = compareInts(typedAdapter1.getEventCode(), typedAdapter2.getEventCode());
				}
			}

			//compare record status for tiebreaker 3
			if (returnVal == EQUAL) {
				if (isNull(typedAdapter1.getEventRecordStatus(), typedAdapter2.getEventRecordStatus())) {
					returnVal = getCompareValueForNullConditions(typedAdapter1.getEventRecordStatus(), typedAdapter2.getEventRecordStatus());
				} else {
					returnVal = typedAdapter1.getEventRecordStatus().compareTo(typedAdapter2.getEventRecordStatus());
				}
			}

			//compare eventOrigin tiebreaker 4
			if (returnVal == EQUAL) {
				if (isNull(typedAdapter1.getEventRecordOrigin(), typedAdapter2.getEventRecordOrigin())) {
					returnVal = getCompareValueForNullConditions(typedAdapter1.getEventRecordOrigin(), typedAdapter2.getEventRecordOrigin());
				} else {
					returnVal = typedAdapter1.getEventRecordOrigin().compareTo(typedAdapter2.getEventRecordOrigin());
				}
			}
		}
		return returnVal;
	}
}
