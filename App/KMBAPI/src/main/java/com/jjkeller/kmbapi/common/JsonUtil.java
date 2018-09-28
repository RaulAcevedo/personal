package com.jjkeller.kmbapi.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.jjkeller.kmbapi.enums.DataDiagnosticEnum;
import com.jjkeller.kmbapi.enums.Malfunction;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility for setting up the Json Serialization.
 *
 * Created by Charles Stebbins on 4/27/2017.
 */

public final class JsonUtil {

    public static final Type TYPE_LIST_OF_EMPLOYEE_LOG_ELD_EVENT = new TypeToken<List<EmployeeLogEldEvent>>() { }.getType();
    public static final Type TYPE_SET_OF_STRINGS = new TypeToken<Set<String>>() { }.getType();
    public static final Type TYPE_SET_OF_MALFUNCTIONS = new TypeToken<HashSet<Malfunction>>() { }.getType();
    public static final Type TYPE_SET_OF_DATA_DIAGNOSTIC_ENUMS = new TypeToken<HashSet<DataDiagnosticEnum>>() { }.getType();

    private JsonUtil() { }

    public static Gson getGson() {
        GsonBuilder builder = new GsonBuilder();
        return builder.create();
    }
}
