package com.jjkeller.kmbapi.controller.EOBR.datamanipulators;

import com.jjkeller.kmbapi.controller.EOBR.ManipulableEobrReader;

/**
 * Created by ief5781 on 4/12/17.
 */

public interface IDataManipulator<T> {
    Class<T> getType();
    void manipulate(T data);
    void register(ManipulableEobrReader eobrReader);
    void unregister(ManipulableEobrReader eobrReader);
}
