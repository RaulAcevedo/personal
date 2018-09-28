package com.jjkeller.kmbapi.proxydata;

/**
 * Created by ief5781 on 4/14/17.
 */

public class ListProxy<T> extends ProxyBase {
    private T[] list;

    public T[] getList() {
        return list;
    }

    public void setList(T[] list) {
        this.list = list;
    }
}
