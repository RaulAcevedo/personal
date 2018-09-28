package com.jjkeller.kmb.test.kmbapi.controller.dataaccess;

import android.content.Context;
import android.util.SparseArray;

import com.jjkeller.kmbapi.controller.dataaccess.EmployeeLogEldEventFacade;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;

public class MockEmployeeLogEldEventFacade extends EmployeeLogEldEventFacade {

    public final SparseArray<EmployeeLogEldEvent> _fetchByKeyValues = new SparseArray<>();

    public MockEmployeeLogEldEventFacade(Context ctx, User user) {
        super(ctx, user);
    }

    @Override
    public EmployeeLogEldEvent FetchByKey(Integer uniqueKey) {
        return _fetchByKeyValues.get(uniqueKey);
    }
}