package com.jjkeller.kmbapi.eobrengine

import com.jjkeller.kmbapi.eobrengine.eobrdatatypes.Eobr_Data_Packet
import com.jjkeller.kmbapi.eobrengine.eobrdatatypes.Eobr_Data_Packet_GenII
import com.jjkeller.kmbapi.eobrengine.eobrdatatypes.Eobr_Driver_Event_Packet
import com.jjkeller.kmbapi.eobrengine.eobrdatatypes.Eobr_FW_Block_Packet
import com.jjkeller.kmbapi.eobrengine.eobrdatatypes.Eobr_Packet

internal interface ISendReceiveBulkData {
    fun receiveBulkData(eobrData: Boolean): ByteArray

    fun sendBulkData(eobrPacket: Eobr_Packet, packetSize: Int): Boolean

    fun sendBulkData(eobrPacket: Eobr_FW_Block_Packet, packetSize: Int): Boolean

    fun sendEobrDataPacketBulkData(dataPacket: Eobr_Data_Packet): Boolean

    fun sendEobrDataPacketBulkData(dataPacket: Eobr_Data_Packet_GenII): Boolean

    fun sendEobrFirmwareBlockPacket(blockPacket: Eobr_FW_Block_Packet): Boolean
    fun sendEobrDriverEventPacket(driverPacket: Eobr_Driver_Event_Packet): Boolean
}
