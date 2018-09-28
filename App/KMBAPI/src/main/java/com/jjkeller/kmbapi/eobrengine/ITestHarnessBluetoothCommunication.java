package com.jjkeller.kmbapi.eobrengine;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;

interface ITestHarnessBluetoothCommunication {
    // Do not use the following 4 properties for anything else except sharing the bluetooth connection information with the
    // Testharness.  These are only being exposed here so that the TestHarness application can share the Bluetooth information.
    // Currently, the Testharness application communicates with the EOBR via KMBAPI methods and via its own "internal" methods.
    // Because of this, we need to be able to share bluetooth connection information.  For example, the Testharness initially
    // establishes EOBR communications using the KMBAPI methods.  Although some of the test cases within the Testharness will
    // continue to use the KMBAPI communication methods to communicate with the EOBR, there are several test cases within the
    // Testharness that will use its own "internal" methods to communicate with the EOBR.  Because of this, we need to "share",
    // or continue to use the bluetooth connection information originally established.  If we don't, we run into several errors
    // and issues. This solution was discussed with Jim M. in June, 2013 and was accepted as the best alternative at this point.

    BluetoothAdapter getBluetoothAdapter();

    BluetoothSocket getBlueToothSocket();

    String getCurrentBtAddress();

    boolean getIsSocketConnected();
}
