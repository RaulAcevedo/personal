package com.jjkeller.kmbapi.controller.interfaces;

import android.content.Context;

import com.jjkeller.kmbapi.proxydata.ProxyBase;

/**
 * Base interface for a generic Utility class that operates on any object which extends ProxyBase
 * @param <T>
 */
public interface ITypedUtility<T extends ProxyBase> {
    void setInternalContext(Context ctx);
}
