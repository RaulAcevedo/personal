package com.jjkeller.kmbapi.kmbeobr;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class DriveData {
	private long nextTimecode;
	private long driveOffTimecode;
	private List<VehicleLocation> vehicleLocations;
	
	public long getNextTimecode() {
		return nextTimecode;
	}
	public void setNextTimecode(long nextTimecode) {
		this.nextTimecode = nextTimecode;
	}

	public long getDriveOffTimecode() {
		return driveOffTimecode;
	}
	public void setDriveOffTimecode(long driveOffTimecode) {
		this.driveOffTimecode = driveOffTimecode;
	}

	public List<VehicleLocation> getVehicleLocations() {
		return vehicleLocations;
	}
	public void setVehicleLocations(List<VehicleLocation> vehicleLocations) {
		this.vehicleLocations = vehicleLocations;
	}

	public static int MaxVehicleLocationCount()
	{
		return 6;
	}
	
	public static DriveData FromByteBuffer(ByteBuffer buffer)
	{
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		
		DriveData driveData = new DriveData();
		driveData.setNextTimecode(buffer.getLong());
		driveData.setDriveOffTimecode(buffer.getLong());
		
		if(driveData.getNextTimecode() > 0)
		{
			int vehicleLocationDataLength = buffer.limit() - 18; //subtract 16 to ignore timestamps, another 2 to ignore cmd, len bytes
			byte[] vehicleLocationData = new byte[vehicleLocationDataLength];
			int recordCount = vehicleLocationDataLength / VehicleLocation.RecordLength();
			
			buffer.get(vehicleLocationData);
			
			List<VehicleLocation> locations = new ArrayList<VehicleLocation>();
			for(int i = 0; i < recordCount; i++)
			{
				int offset = i * VehicleLocation.RecordLength();
				ByteBuffer vehicleBuffer = ByteBuffer.wrap(vehicleLocationData, offset, VehicleLocation.RecordLength());
				
				VehicleLocation location = VehicleLocation.FromByteBuffer(vehicleBuffer);
				locations.add(location);
			}
			
			driveData.setVehicleLocations(locations);
		}
		
		return driveData;
	}
}
