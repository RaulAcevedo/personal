package com.jjkeller.kmbapi.controller.utility;

import com.jjkeller.kmbapi.enums.DutyStatusEnum;
import com.jjkeller.kmbapi.enums.ExemptLogTypeEnum;
import com.jjkeller.kmbapi.proxydata.EldEventAdapterList;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

public class ExemptEmployeeLogUtilities {
	
	public EmployeeLogEldEvent[] getExemptEmployeeLogEvents(EmployeeLogEldEvent[] logEvents, ExemptLogTypeEnum exemptLogTypeEnum)
	{
		ArrayList<EmployeeLogEldEvent> exemptLogEventArrayList = new ArrayList<EmployeeLogEldEvent>();
		
		if (exemptLogTypeEnum.getValue() == ExemptLogTypeEnum.EXEMPTLOGTYPE100AIRMILE ||
				exemptLogTypeEnum.getValue() == ExemptLogTypeEnum.EXEMPTLOGTYPE150AIRMILENONCDL)
		{
			for (EmployeeLogEldEvent logEvent : logEvents)
			{
				EmployeeLogEldEvent exemptLogEvent = null;
				try{
					exemptLogEvent = (EmployeeLogEldEvent) cloneObject(logEvent);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				
				if (logEvent.isExemptOnDutyStatus())
				{
					exemptLogEvent.setDutyStatusEnum(new DutyStatusEnum(DutyStatusEnum.ONDUTY));
				}
				
				if (logEvent.isExemptOffDutyStatus())
				{
					exemptLogEvent.setDutyStatusEnum(new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
				}
				
				exemptLogEventArrayList.add(exemptLogEvent);
			}

			EmployeeLogEldEvent[] exemptLogEvents = exemptLogEventArrayList.toArray(new EmployeeLogEldEvent[exemptLogEventArrayList.size()]);
			return exemptLogEvents;
		}
		else
		{
			return logEvents;
		}
	}
    
    public EmployeeLogEldEvent[] getExemptEmployeeLogEvents(EmployeeLog employeeLog)
    {
    	EmployeeLog currentLog = (EmployeeLog) cloneObject(employeeLog);

		EmployeeLogEldEvent[] logEventArray = new EmployeeLogEldEvent[currentLog.getEldEventList().getEldEventList(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus).length];
    	
    	if (currentLog.getExemptLogType().getValue() == ExemptLogTypeEnum.EXEMPTLOGTYPE100AIRMILE ||
				currentLog.getExemptLogType().getValue() == ExemptLogTypeEnum.EXEMPTLOGTYPE150AIRMILENONCDL)
		{
	    	for(int i = 0; i < currentLog.getEldEventList().getEldEventList(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus).length; i++)
	    	{
				EmployeeLogEldEvent event = currentLog.getEldEventList().getEldEventList(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus)[i];
	    		
	    		if (event.isExemptOnDutyStatus())
    			{
    				event.setDutyStatusEnum(new DutyStatusEnum(DutyStatusEnum.ONDUTY));
    			}
    			
    			if (event.isExemptOffDutyStatus())
    			{
    				event.setDutyStatusEnum(new DutyStatusEnum(DutyStatusEnum.OFFDUTY));
    			}
        		
    			logEventArray[i] = event;
	    	}
    	}
    	else
    	{
    		logEventArray = currentLog.getEldEventList().getEldEventList(EldEventAdapterList.ListAccessorModifierEnum.DutyStatus);
    	}
    	
    	return logEventArray;
    }
	
	private Object cloneObject(Object obj){
        try{
        	Object clone = obj.getClass().newInstance();
            for (Field field : obj.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                if(field.get(obj) == null || Modifier.isFinal(field.getModifiers())){
                    continue;
                }
                if(field.getType().isPrimitive() || field.getType().equals(String.class)
                        || field.getType().getSuperclass().equals(Number.class)
                        || field.getType().equals(Boolean.class)){
                    field.set(clone, field.get(obj));
                }else{
                    Object childObj = field.get(obj);
                    if(childObj == obj){
                        field.set(clone, clone);
                    }else{
                        field.set(clone, cloneObject(field.get(obj)));
                    }
                }
            }
            return clone;
        }catch(Exception e){
            return null;
        }
    } 
}
