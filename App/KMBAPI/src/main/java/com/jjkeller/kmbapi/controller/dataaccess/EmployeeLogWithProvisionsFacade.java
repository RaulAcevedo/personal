package com.jjkeller.kmbapi.controller.dataaccess;

import android.content.Context;

import com.jjkeller.kmbapi.controller.dataaccess.db.EmployeeLogWithProvisionsPersist;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.EmployeeLogWithProvisions;

import java.util.List;

/**
 * Created by jar5943 on 4/21/2016.
 */
public class EmployeeLogWithProvisionsFacade extends FacadeBase {
    public EmployeeLogWithProvisionsFacade(Context ctx)
    {
        super(ctx);
    }

    public void Save(EmployeeLogWithProvisions logPersonalConveyance, EmployeeLog empLog)
    {
        EmployeeLogWithProvisionsPersist<EmployeeLogWithProvisions> persist = new EmployeeLogWithProvisionsPersist<EmployeeLogWithProvisions>(EmployeeLogWithProvisions.class, this.getContext());
        persist.Save(logPersonalConveyance, empLog);
    }

    public void Delete(EmployeeLogWithProvisions provision) {
        EmployeeLogWithProvisionsPersist<EmployeeLogWithProvisions> persist = new EmployeeLogWithProvisionsPersist<EmployeeLogWithProvisions>(EmployeeLogWithProvisions.class, this.getContext());
        persist.Delete(provision);
    }

    public EmployeeLogWithProvisions FetchMostRecentForLog(EmployeeLog empLog, int empLogWithProvisionTypeEnum)
    {
        EmployeeLogWithProvisionsPersist<EmployeeLogWithProvisions> persist = new EmployeeLogWithProvisionsPersist<EmployeeLogWithProvisions>(EmployeeLogWithProvisions.class, this.getContext());
        return persist.FetchMostRecentForLog(empLog, empLogWithProvisionTypeEnum);
    }

    public EmployeeLogWithProvisions FetchForLogEldEventKey(EmployeeLog empLog, long logEldEventKey){
        EmployeeLogWithProvisionsPersist<EmployeeLogWithProvisions> persist = new EmployeeLogWithProvisionsPersist<EmployeeLogWithProvisions>(EmployeeLogWithProvisions.class, this.getContext());

        return persist.FetchForLogEldEvent(empLog, logEldEventKey);
    }

    public List<EmployeeLogWithProvisions> GetUnsubmitted(int empLogWithProvisionTypeEnum)
    {
        EmployeeLogWithProvisionsPersist<EmployeeLogWithProvisions> persist = new EmployeeLogWithProvisionsPersist<EmployeeLogWithProvisions>(EmployeeLogWithProvisions.class, this.getContext());
        return persist.FetchAllUnsubmittedByProvisionType(empLogWithProvisionTypeEnum);
    }

    public void MarkSubmitted(List<EmployeeLogWithProvisions> items)
    {
        EmployeeLogWithProvisionsPersist<EmployeeLogWithProvisions> persist = new EmployeeLogWithProvisionsPersist<EmployeeLogWithProvisions>(EmployeeLogWithProvisions.class, this.getContext());
        persist.MarkAsSubmitted(items);
    }

    public EmployeeLogWithProvisions FetchLastLogWithProvisions(){
        EmployeeLogWithProvisionsPersist<EmployeeLogWithProvisions> persist = new EmployeeLogWithProvisionsPersist<EmployeeLogWithProvisions>(EmployeeLogWithProvisions.class, this.getContext());

        return persist.FetchLastLogWithProvisions();
    }

    public void UpdateLogEldEventKey(int logWithProvisionsKey, int logEldEventKey){
        EmployeeLogWithProvisionsPersist<EmployeeLogWithProvisions> persist = new EmployeeLogWithProvisionsPersist<EmployeeLogWithProvisions>(EmployeeLogWithProvisions.class, this.getContext());

        persist.UpdateLogEldEventKey(logWithProvisionsKey, logEldEventKey);
    }
}
