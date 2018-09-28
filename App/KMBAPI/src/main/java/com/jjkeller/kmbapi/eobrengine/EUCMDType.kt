package com.jjkeller.kmbapi.eobrengine

class EUCMDType {
    companion object {
        const val EUCMD_INITIALIZE = 0x01
        const val EUCMD_GET_CLOCK_UTC = 0x02
        const val EUCMD_SET_CLOCK_UTC = 0x03
        const val EUCMD_GET_EOBR_DATA = 0x04
        const val EUCMD_CHANGE_ACTIVE_BUS = 0x05
        const val EUCMD_CHANGE_DATA_RATE = 0x06
        const val EUCMD_TEST_CONNECTION = 0x07
        const val EUCMD_START_FW_UPGRADE = 0x09
        const val EUCMD_GET_FW_IMAGE_REQ = 0x0A
        const val EUCMD_FW_IMAGE_BLOCK = 0x0B
        const val EUCMD_GET_UNIT_ID = 0x0C
        const val EUCMD_SET_UNIT_ID = 0x0D
        const val EUCMD_GET_SERIAL_NUMBER = 0x0E
        const val EUCMD_GET_REF_TIMESTAMP = 0x0F
        const val EUCMD_INVALID_CMD = 0xFF
        const val EUCMD_SHUTDOWN_DEVICE = 0x08
        const val EUCMD_GET_CLOCK_GPS = 0x10
        const val EUCMD_CLEAR_RECORD_DATA = 0x11
        const val EUCMD_SET_ENGINE_OFF_COMMS_TIMEOUT = 0x12
        const val EUCMD_SET_SERIAL_NUMBER = 0x13
        const val EUCMD_GET_DATA_RATE = 0x14
        const val EUCMD_GET_RECORD_ID_GIVEN_TIMESTAMP = 0x15
        const val EUCMD_SET_SYSTEM_MODE = 0x16
        const val EUCMD_GET_EOBR_REVISIONS = 0x17
        const val EUCMD_SET_DEBUG_FLAGS = 0x18
        const val EUCMD_SET_USB_REVISIONS = 0x19
        const val EUCMD_GET_COMPANY_PASSKEY = 0x1A
        const val EUCMD_SET_COMPANY_PASSKEY = 0x1B
        const val EUCMD_GET_ERROR_LOG = 0x1C
        const val BT_CONNECTION_ON = 0x2A   // This command is used in ST code only
        const val CONNECTION_STATUS = 0x2B   // This command is used in ST code only
        const val EUCMD_GET_NEXT_BAD_BLOCK = 0x2C
        const val EUCMD_USB_UNPLUG = 0x1D   // This command is only use by EzHost sent over to ST, put here for cautious, not to conflict with this command
        const val EUCMD_USB_CONNECTED = 0x1E   // This command is only use by EzHost sent over to ST, put here for cautious, not to conflict with this comamnd
        const val EUCMD_GET_ACTIVE_BUS = 0x1F
        const val EUCMD_GET_ENGINE_OFF_COMMS_TIMEOUT = 0x20
        const val EUCMD_SHUTDOWN = 0x21
        const val EUCMD_GET_SYSTEM_MODE = 0x22
        const val EUCMD_GET_NUM_OF_JBUS_DEVICES = 0x23
        const val EUCMD_GET_DEV_INFO_ON_JBUS = 0x24
        const val EUCMD_GET_JBUS_DIAG_DATA = 0x25
        const val EUCMD_GET_ACTIVE_JBUS_DIAG_ID = 0x26
        const val EUCMD_GET_SERIAL_NUMBER_EZHOST = 0x27  // This command is only use by EzHost sent over to ST, put here for cautious, not to conflict with this comamnd
        const val EUCMD_SET_CUSTOM_PARAMETER = 0x28
        const val EUCMD_GET_CUSTOM_PARAMETER = 0x29
        const val EUCMD_GET_STATUS_BUFFER = 0x30
        const val EUCMD_GET_HISTOGRAM = 0x31
        const val EUCMD_CLEAR_HISTOGRAM = 0x32
        const val EUCMD_CONSOLE_COMMAND = 0x33
        const val EUCMD_GET_CONSOLE_LOG = 0x34
        const val EUCMD_GET_EVENT_DATA = 0x35
        const val EUCMD_GET_TRIP_REPORT = 0x36
        const val EUCMD_RESET_STATUS_BUFFER = 0x37
        const val EUCMD_SET_THRESHOLDS = 0x38
        const val EUCMD_GET_THRESHOLDS = 0x39
        const val EUCMD_SET_ODOMETER_OFFSET = 0x40
        const val EUCMD_GET_ODOMETER_OFFSET = 0x41
        const val EUCMD_SET_SELF_TEST = 0x42
        const val EUCMD_GET_SELF_TEST = 0x43
        const val EUCMD_START_KEEP_ALIVE = 0x71
        const val EUCMD_STOP_KEEP_ALIVE = 0x72
        const val EUCMD_KEEP_ALIVE_NOTIFICATION = 0x73
        const val EUCMD_INIT_KEEP_ALIVE = 0x75
        const val EUCMD_GET_EOBR_HARDWARE = 0x76
        const val EUCMD_GET_EOBR_UPGRADE_REQ = 0x77
        const val EUCMD_GET_EOBR_UPGRADE_STATUS = 0x78
        const val EUCMD_GET_DRIVE_DATA = 0x79
        const val EUCMD_SET_REF_TIMESTAMP = 0x80
        const val EUCMD_GET_DIST_HRS = 0x81
        const val EUCMD_FILE_XFER_CONTROL = 0x82
        const val EUCMD_FILE_XFER_BLOCK = 0x83
        const val EUCMD_GET_DRIVER_EVENT = 0x85
        const val EUCMD_GET_DRIVER_COUNT = 0x86
    }
}