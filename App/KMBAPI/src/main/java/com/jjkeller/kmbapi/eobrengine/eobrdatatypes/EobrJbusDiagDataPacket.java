package com.jjkeller.kmbapi.eobrengine.eobrdatatypes;

import com.jjkeller.kmbapi.eobrengine.LittleEndianHelper;

import java.util.ArrayList;

public class EobrJbusDiagDataPacket extends EobrPacketBase {
	private static final int DTC_INFO_OFFSET = 18;

	private int recordId;
	private int associatedEobrRecordId;
	private long timecode;
	private EobrJbusDiagDataDtcInfo[] dtcList;

	public EobrJbusDiagDataPacket(byte[] response){
		setCmd(response[0]);
		setSize(response[1]);

		setRecordId(LittleEndianHelper.Companion.getInt(response, 2, 4));

		setTimecode(LittleEndianHelper.Companion.getLong(response, 6, 8));

		setAssociatedEobrRecordId(LittleEndianHelper.Companion.getInt(response, 14, 4));

		// Get the DTC list
		ArrayList<EobrJbusDiagDataDtcInfo> dtcList = buildDtcList(response);
		setDtcList(dtcList.toArray(new EobrJbusDiagDataDtcInfo[dtcList.size()]));
	}
	
	public int getRecordId()
	{
		return this.recordId;
	}
	public void setRecordId(int recordId)
	{
		this.recordId = recordId;
	}
	
	public int getAssociatedEobrRecordId()
	{
		return associatedEobrRecordId;
	}
	public void setAssociatedEobrRecordId(int associatedEobrRecordId)
	{
		this.associatedEobrRecordId = associatedEobrRecordId;
	}
	
	public long getTimecode()
	{
		return timecode;
	}
	public void setTimecode(long timecode)
	{
		this.timecode = timecode;
	}
	
	public EobrJbusDiagDataDtcInfo[] getDtcList()
	{
		return dtcList;
	}
	public void setDtcList(EobrJbusDiagDataDtcInfo[] dtcList)
	{
		this.dtcList = dtcList;
	}

	private ArrayList<EobrJbusDiagDataDtcInfo> buildDtcList(byte[] response) {
        ArrayList<EobrJbusDiagDataDtcInfo> dtcList = new ArrayList<EobrJbusDiagDataDtcInfo>();
	    for (int i = EobrJbusDiagDataPacket.DTC_INFO_OFFSET; i < getSize(); i += EobrJbusDiagDataDtcInfo.SIZE)
        {
            EobrJbusDiagDataDtcInfo dtc = new EobrJbusDiagDataDtcInfo();

            // Response is an unsigned byte, so cast to a short to avoid negatives
            dtc.setType((short)(response[i] & 0xFF));

			int rawSource = LittleEndianHelper.Companion.getInt(response,
				i + EobrJbusDiagDataDtcInfo.SOURCE_OFFSET,
					EobrJbusDiagDataDtcInfo.SOURCE_LENGTH);
            dtc.setSource(rawSource);

            int rawDtc = LittleEndianHelper.Companion.getInt(response,
					i + EobrJbusDiagDataDtcInfo.DTC_OFFSET,
					EobrJbusDiagDataDtcInfo.DTC_LENGTH);
            dtc.setDTC(rawDtc);

            dtcList.add(dtc);
        }
        return dtcList;
    }
}
