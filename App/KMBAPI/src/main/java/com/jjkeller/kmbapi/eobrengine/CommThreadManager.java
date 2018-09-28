package com.jjkeller.kmbapi.eobrengine;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.util.Log;

import com.jjkeller.kmbapi.controller.utility.ErrorLogHelper;
import com.jjkeller.kmbapi.eobrengine.eobrdatatypes.Eobr_Data_Packet;
import com.jjkeller.kmbapi.eobrengine.eobrdatatypes.Eobr_Data_Packet_GenII;
import com.jjkeller.kmbapi.eobrengine.eobrdatatypes.Eobr_Driver_Event_Packet;
import com.jjkeller.kmbapi.eobrengine.eobrdatatypes.Eobr_FW_Block_Packet;
import com.jjkeller.kmbapi.eobrengine.eobrdatatypes.Eobr_Packet;

import java.io.IOException;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CommThreadManager {

	private ISendReceiveBulkData sendReceiveBulkData;
	private volatile byte[] _bulkDataBytes = null;
	private volatile boolean _socketConnectionSuccessful = false;
	private volatile Exception _socketConnectionExeption = null;

	public static final int STANDARD_BARRIER_TIMEOUT = 10000;
	public static final int EXTENDED_BARRIER_TIMEOUT = 15000;
	public static final int SOCKETCONNECT_BARRIER_TIMEOUT = 20000;
	
	public CommThreadManager(ISendReceiveBulkData sendReceiveBulkData)
	{
		this.sendReceiveBulkData = sendReceiveBulkData;
	}

	public synchronized Bundle SendCommand(int cmdId, short activeDeviceCrc)
	{
		// Create barrier to wait on 2 threads (caller and ReceiveDataThread)
    	CyclicBarrier barrier = CreateBarrier();

    	// Start worker thread to get data from EOBR
      	ReceiveDataThread rdThread = new ReceiveDataThread(barrier, cmdId, activeDeviceCrc);
      	rdThread.start();
      	// Give thread a change to get started
      	Sleep(50);
      	
      	// Have current thread wait for worker thread to finish/timeout
      	boolean success = WaitForThread(barrier, rdThread, STANDARD_BARRIER_TIMEOUT);
      	try {
      		// Blocks the current Thread until the receiver finishes its execution and dies.
			rdThread.join(500);
		} catch (InterruptedException e) {
			Log.e("Thread.join", "InterruptedException exception", e);
		}
  		
      	// Process whatever response we have
      	Bundle bundle = new Bundle();
      	bundle.putByteArray(EobrEngineBase.COMMTHREAD_RESPONSE, _bulkDataBytes);
  		bundle.putInt(EobrEngineBase.COMMTHREAD_RETURNCODE, success? 0:1);
		return bundle;
	}

	public synchronized Bundle SendCommandWithData(Eobr_Packet packet)
	{
		return SendCommandWithData(packet, false);
	}

	/**
	 * Override method to extend the BARRIER_TIMEOUT for scenarios when there is to much History Data to read from EOBR
	 * @param packet Data Package from EOBR
	 * @param extendBarrierTime boolean to extend the BARRIER_TIMEOUT from 10s to 15s
	 * @return Bundle with COMMTHREAD_RESPONSE and COMMTHREAD_RETURNCODE
	 */
	public synchronized Bundle SendCommandWithData(Eobr_Packet packet, Boolean extendBarrierTime)
	{
		// Create barrier to wait on 2 threads (caller and ReceiveDataThread)
		CyclicBarrier barrier = CreateBarrier();

		// Start worker thread to get data from EOBR
		WithCommandDataThread wcdThread = new WithCommandDataThread(barrier, packet);
		wcdThread.start();
		// Give thread a change to get started
		Sleep(50);

		// Have current thread wait for worker thread to finish/timeout
		// extendBarrierTime indicates whether to use 10s or 15s threshold
		boolean success;
		if(extendBarrierTime){
			success = WaitForThread(barrier, wcdThread, EXTENDED_BARRIER_TIMEOUT);
		}else{
			success = WaitForThread(barrier, wcdThread, STANDARD_BARRIER_TIMEOUT);
		}

		try {
			// Blocks the current Thread until the receiver finishes its execution and dies.
			wcdThread.join(500);
		} catch (InterruptedException e) {
			Log.e("Thread.join", "InterruptedException exception", e);
		}

		// Process whatever response we have
		Bundle bundle = new Bundle();
		bundle.putByteArray(EobrEngineBase.COMMTHREAD_RESPONSE, _bulkDataBytes);
		bundle.putInt(EobrEngineBase.COMMTHREAD_RETURNCODE, success? 0:1);
		return bundle;
	}
	
	public synchronized Bundle SendEOBRDataPacket(Eobr_Data_Packet packet, boolean resetReferenceTimestampToCurrent){
		return SendEOBRDataPacket(packet, resetReferenceTimestampToCurrent, STANDARD_BARRIER_TIMEOUT);
	}

	public synchronized Bundle SendEOBRDataPacket(Eobr_Data_Packet packet, boolean resetReferenceTimestampToCurrent, int commandTimeoutMilliseconds)
	{
		// Create barrier to wait on 2 threads (caller and ReceiveDataThread)
    	CyclicBarrier barrier = CreateBarrier();

    	// Start worker thread to get data from EOBR
    	ReceiveEOBRDataThread redThread = new ReceiveEOBRDataThread(barrier, packet, resetReferenceTimestampToCurrent);
    	redThread.start();
      	// Give thread a change to get started
      	Sleep(50);
      	
      	// Have current thread wait for worker thread to finish/timeout
      	boolean success = WaitForThread(barrier, redThread, commandTimeoutMilliseconds);
  		
      	// Process whatever response we have
      	Bundle bundle = new Bundle();
      	bundle.putByteArray(EobrEngineBase.COMMTHREAD_RESPONSE, _bulkDataBytes);
  		bundle.putInt(EobrEngineBase.COMMTHREAD_RETURNCODE, success? 0:1);
		return bundle;
	}


	public synchronized Bundle sendEOBRFWUpdateDataPacket(Eobr_FW_Block_Packet packet, int commandTimeoutMilliseconds)
	{
		Bundle bundle = new Bundle();
		// Create barrier to wait on 2 threads (caller and ReceiveDataThread)
		CyclicBarrier barrier = CreateBarrier();

			// Start worker thread to get data from EOBR
			ReceiveEOBRFirmwareBlockPacketThread redThread = new ReceiveEOBRFirmwareBlockPacketThread(barrier, packet);
			redThread.start();
			// Give thread a change to get started
			Sleep(50);

			// Have current thread wait for worker thread to finish/timeout
			boolean success = WaitForThread(barrier, redThread, commandTimeoutMilliseconds);

			if(barrier.isBroken()){
				bundle.putByteArray(EobrEngineBase.COMMTHREAD_RESPONSE, null);
				bundle.putInt(EobrEngineBase.COMMTHREAD_RETURNCODE, 0);
			}else{
				bundle.putByteArray(EobrEngineBase.COMMTHREAD_RESPONSE, _bulkDataBytes);
				bundle.putInt(EobrEngineBase.COMMTHREAD_RETURNCODE, success ? 0 : 1);

			}

		return bundle;
	}
	
	public synchronized Bundle SendEOBRDataPacketGenII(Eobr_Data_Packet_GenII packet){
		return SendEOBRDataPacketGenII(packet, STANDARD_BARRIER_TIMEOUT);
	}

	public synchronized Bundle SendEOBRDataPacketGenII(Eobr_Data_Packet_GenII packet, int commandTimeoutMilliseconds)
	{
		// Create barrier to wait on 2 threads (caller and ReceiveDataThread)
    	CyclicBarrier barrier = CreateBarrier();

    	// Start worker thread to get data from EOBR
    	ReceiveEOBRDataGenIIThread redThread = new ReceiveEOBRDataGenIIThread(barrier, packet);
    	redThread.start();
      	// Give thread a change to get started
      	Sleep(50);
      	
      	// Have current thread wait for worker thread to finish/timeout
      	boolean success = WaitForThread(barrier, redThread, commandTimeoutMilliseconds);
      	try {
      		// Blocks the current Thread until the receiver finishes its execution and dies.
			redThread.join(500);
		} catch (InterruptedException e) {
			Log.e("Thread.join", "InterruptedException exception", e);
		}
  		
      	// Process whatever response we have
      	Bundle bundle = new Bundle();
      	bundle.putByteArray(EobrEngineBase.COMMTHREAD_RESPONSE, _bulkDataBytes);
  		bundle.putInt(EobrEngineBase.COMMTHREAD_RETURNCODE, success? 0:1);
		return bundle;
	}

	public synchronized Bundle SendEOBRDriverEventPacket(Eobr_Driver_Event_Packet packet){
		return SendEOBRDriverEventPacket(packet, STANDARD_BARRIER_TIMEOUT);
	}

	public synchronized Bundle SendEOBRDriverEventPacket(Eobr_Driver_Event_Packet packet, int commandTimeoutMilliseconds)
	{
		// Create barrier to wait on 2 threads (caller and ReceiveDataThread)
		CyclicBarrier barrier = CreateBarrier();

		// Start worker thread to get data from EOBR
		ReceiveEOBRDriverEventThread redThread = new ReceiveEOBRDriverEventThread(barrier, packet);
		redThread.start();
		// Give thread a change to get started
		Sleep(50);

		// Have current thread wait for worker thread to finish/timeout
		boolean success = WaitForThread(barrier, redThread, commandTimeoutMilliseconds);
		try {
			// Blocks the current Thread until the receiver finishes its execution and dies.
			redThread.join(500);
		} catch (InterruptedException e) {
			Log.e("Thread.join", "InterruptedException exception", e);
		}

		// Process whatever response we have
		Bundle bundle = new Bundle();
		bundle.putByteArray(EobrEngineBase.COMMTHREAD_RESPONSE, _bulkDataBytes);
		bundle.putInt(EobrEngineBase.COMMTHREAD_RETURNCODE, success? 0:1);
		return bundle;
	}


	public synchronized boolean ConnectToBTSocket(BluetoothSocket socket) throws IOException
	{		
		// Create barrier to wait on 2 threads (caller and ReceiveDataThread)
    	CyclicBarrier barrier = CreateBarrier();

    	// Start worker thread to get data from EOBR
    	//_socket = socket;
      	ConnectBTSocketThread btThread = new ConnectBTSocketThread(barrier);
      	btThread.socket = socket;
      	if(socket != null && socket.getRemoteDevice() != null && socket.getRemoteDevice().getAddress() != null)
      		btThread.setName("ConnectToBTSocket_addr:" + socket.getRemoteDevice().getAddress());
      	else
      		btThread.setName("ConnectToBTSocket");
      	btThread.start();
      	// Give thread a change to get started
      	Sleep(50);
      	
      	// Have current thread wait for worker thread to finish/timeout
		boolean success = WaitForThread(barrier, btThread, SOCKETCONNECT_BARRIER_TIMEOUT);		
  		try {
  			// Blocks the current Thread until the receiver finishes its execution and dies.
			btThread.join(500);
		} catch (InterruptedException e) {
			Log.e("Thread.join", "InterruptedException exception", e);
		}
		
      	// Process whatever response we have
      	if(_socketConnectionExeption != null)
      	{
      		if(_socketConnectionExeption.getClass() == IOException.class)
      			throw (IOException)_socketConnectionExeption;
      		else if(_socketConnectionExeption.getClass() == NullPointerException.class)
      			throw (NullPointerException)_socketConnectionExeption;
      	}
      	
      	return success && _socketConnectionSuccessful;
	}
	
	private class ReceiveDataThread extends Thread {
		CyclicBarrier barrier;
		int cmdId;
		short activeDeviceCrc;

		public ReceiveDataThread(CyclicBarrier barrier, int cmdId, short activeDeviceCrc) {
			this.barrier = barrier;
			this.cmdId = cmdId;
		    this.activeDeviceCrc = activeDeviceCrc;
		}

		public void run() {
			try
			{
				byte[] response = null;
				if (SendCommandWithoutData(cmdId, activeDeviceCrc))
				{
					response = sendReceiveBulkData.receiveBulkData(false);
				}

				if (!barrier.isBroken())
				{
					_bulkDataBytes = response;
					barrier.await();
				}
			}
			catch (InterruptedException e)
			{
				Log.e("Barrier", "ReceiveDataThread failed to receive data because the thread was interrupted", e);
				if(!barrier.isBroken())
				{
					try {
						barrier.await();
					} catch (InterruptedException e1) {
						ErrorLogHelper.RecordException(e1);
					} catch (BrokenBarrierException e1) {
						ErrorLogHelper.RecordException(e1);
					}
				}
				return;	
			}
			catch (BrokenBarrierException e)
			{
				Log.e("Barrier", "ReceiveDataThread failed to receive data because of a broken barrier", e);
				if(!barrier.isBroken())
				{
					try {
						barrier.await();
					} catch (InterruptedException e1) {
						ErrorLogHelper.RecordException(e1);
					} catch (BrokenBarrierException e1) {
						ErrorLogHelper.RecordException(e1);
					}
				}
				return;	
			}
		}
	}
	
	private class WithCommandDataThread extends Thread {
		CyclicBarrier barrier;
		Eobr_Packet packet;
		
		public WithCommandDataThread(CyclicBarrier barrier, Eobr_Packet packet) { 
			this.barrier = barrier;
			this.packet = packet;
		}

		public void run() {
			try
			{
				byte[] response = null;
				if (sendReceiveBulkData.sendBulkData(packet, EobrEngineBase.EOBR_PACKET_SIZE - EobrEngineBase.EOBR_PAYLOAD_SIZE + packet.getLen()))
				{
					response = sendReceiveBulkData.receiveBulkData(false);
				}

				if (!barrier.isBroken())
				{
					_bulkDataBytes = response;
					barrier.await();
				}
			}
			catch (InterruptedException e)
			{
				Log.e("Barrier", "WithCommandDataThread failed to receive data because the thread was interrupted", e);
				return;
			}
			catch (BrokenBarrierException e)
			{
				Log.e("Barrier", "WithCommandDataThread failed to receive data because of a broken barrier", e);
				return;
			}
		}
	}
	
	private class ReceiveEOBRDataThread extends Thread {
		CyclicBarrier barrier;
		Eobr_Data_Packet packet;
		@SuppressWarnings("unused")
		boolean resetReferenceTimestampToCurrent;
		
		public ReceiveEOBRDataThread(CyclicBarrier barrier, Eobr_Data_Packet packet, boolean resetReferenceTimestampToCurrent) { 
			this.barrier = barrier;
			this.packet = packet;
			this.resetReferenceTimestampToCurrent = resetReferenceTimestampToCurrent;
		}

		public void run() {
			try
			{
				byte[] response = null;
				if (sendReceiveBulkData.sendEobrDataPacketBulkData(packet))
				{
					response = sendReceiveBulkData.receiveBulkData(true);
				}

				if (!barrier.isBroken())
				{
					_bulkDataBytes = response;
					barrier.await();
				}
			}
			catch (InterruptedException e)
			{
				Log.e("Barrier", "ReceiveEOBRDataThread failed to receive data because the thread was interrupted", e);
				return;
			}
			catch (BrokenBarrierException e)
			{
				Log.e("Barrier", "ReceiveEOBRDataThread failed to receive data because of a broken barrier", e);
				return;
			}
		}
	}

	private class ReceiveEOBRFirmwareBlockPacketThread extends Thread {
		CyclicBarrier barrier;
		Eobr_FW_Block_Packet packet;

		public ReceiveEOBRFirmwareBlockPacketThread(CyclicBarrier barrier, Eobr_FW_Block_Packet packet) {
			this.barrier = barrier;
			this.packet = packet;
		}

		public void run() {
			try
			{
				byte[] response = null;
				if (sendReceiveBulkData.sendEobrFirmwareBlockPacket(packet))
				{
					response = sendReceiveBulkData.receiveBulkData(true);
				}

				if (!barrier.isBroken()){
					_bulkDataBytes = response;
					barrier.await();
				}
			}
			catch (InterruptedException e)
			{
				Log.e("Barrier", "ReceiveEOBRFirmwareBlockPacketThread failed to receive data because the thread was interrupted", e);
				return;
			}
			catch (BrokenBarrierException e)
			{
				Log.e("Barrier", "ReceiveEOBRFirmwareBlockPacketThread failed to receive data because of a broken barrier", e);
				return;
			}
		}
	}
	
	private class ReceiveEOBRDataGenIIThread extends Thread {
		CyclicBarrier barrier;
		Eobr_Data_Packet_GenII packet;
		
		public ReceiveEOBRDataGenIIThread(CyclicBarrier barrier, Eobr_Data_Packet_GenII packet) { 
			this.barrier = barrier;
			this.packet = packet;
		}

		public void run() {
			try
			{
				byte[] response = null;
				if (sendReceiveBulkData.sendEobrDataPacketBulkData(packet))
				{
					response = sendReceiveBulkData.receiveBulkData(true);
				}

				if (!barrier.isBroken()) {
					_bulkDataBytes = response;
					barrier.await();
				}
			}
			catch (InterruptedException e)
			{
				Log.e("Barrier", "ReceiveEOBRDataGenIIThread failed to receive data because the thread was interrupted", e);
				return;
			}
			catch (BrokenBarrierException e)
			{
				Log.e("Barrier", "ReceiveEOBRDataGenIIThread failed to receive data because of a broken barrier", e);
				return;
			}
		}
	}

	private class ReceiveEOBRDriverEventThread extends Thread {
		CyclicBarrier barrier;
		Eobr_Driver_Event_Packet packet;

		public ReceiveEOBRDriverEventThread(CyclicBarrier barrier, Eobr_Driver_Event_Packet packet) {
			this.barrier = barrier;
			this.packet = packet;
		}

		public void run() {
			try
			{
				byte[] response = null;
				if (sendReceiveBulkData.sendEobrDriverEventPacket(packet))
				{
					response = sendReceiveBulkData.receiveBulkData(true);
				}

				if (!barrier.isBroken()) {
					_bulkDataBytes = response;
					barrier.await();
				}
			}
			catch (InterruptedException e)
			{
				Log.e("Barrier", "ReceiveEOBRDriverEventThread failed to receive data because the thread was interrupted", e);
				return;
			}
			catch (BrokenBarrierException e)
			{
				Log.e("Barrier", "ReceiveEOBRDriverEventThread failed to receive data because of a broken barrier", e);
				return;
			}
		}
	}

	private class ConnectBTSocketThread extends Thread {
		CyclicBarrier barrier;
		volatile BluetoothSocket socket;
		
		public ConnectBTSocketThread(CyclicBarrier barrier) { 
			this.barrier = barrier;
		}

		public void run() {
			try 
			{
				_socketConnectionSuccessful = false;
				_socketConnectionExeption = null;
				
				socket.connect();
				_socketConnectionSuccessful = true;

				if(!barrier.isBroken())
				{
					barrier.await();
					this.join(500);
				}
			} 
			catch (IOException e)
			{
				Log.e("Barrier", "ConnectBTSocketThread failed because of an IOException", e);					
				_socketConnectionSuccessful = false;
				_socketConnectionExeption = e;
				if(!barrier.isBroken())
				{
					try {
						barrier.await();
						this.join(500);
					} catch (InterruptedException e1) {
						ErrorLogHelper.RecordException(e1);
					} catch (BrokenBarrierException e1) {
						ErrorLogHelper.RecordException(e1);
					}
				}
				return;				
			}
			catch (NullPointerException e) {
				Log.e("Barrier", "ConnectBTSocketThread failed because of a NullPointerException", e);					
				_socketConnectionSuccessful = false;
				_socketConnectionExeption = e;
				if(!barrier.isBroken())
				{
					try {
						barrier.await();
						this.join(500);
					} catch (InterruptedException e1) {
						ErrorLogHelper.RecordException(e1);
					} catch (BrokenBarrierException e1) {
						ErrorLogHelper.RecordException(e1);
					}
				}
				return;
			} 
			catch (InterruptedException e) {
				Log.e("Barrier", "ConnectBTSocketThread failed to connect because the thread was interrupted", e);					
				_socketConnectionSuccessful = false;
				return;
			} 
			catch (BrokenBarrierException e) {
				Log.e("Barrier", "ConnectBTSocketThread failed to connect because of a broken barrier", e);					
				_socketConnectionSuccessful = false;
				return;
			}
		}
	}
	
	// Most common usage of calling thread and worker thread being the only 2 parties
	private CyclicBarrier CreateBarrier()
	{
		return this.CreateBarrier(2);
	}
	
	// Create the barrier with a custom number of parties
	private CyclicBarrier CreateBarrier(int parties)
	{
		return new CyclicBarrier(parties, new Runnable() {
			public void run() {
			}
		});
	}
	
	// A method to pause the calling thread so that worker threads can get a headstart
	private void Sleep(long duration)
	{
  		try {
  			Thread.sleep(duration);
  		}
  		catch (InterruptedException e) {
  		
        	Log.e("UnhandledCatch", e.getMessage() + ": " + Log.getStackTraceString(e));
  		}
  	}
	
	// This method will cause the current thread to wait on the CyclicBarrier for a default duration of 2.5 seconds.
	// If the other worker threads (typically 1 thread) finish, they call await() and the barrier can continue.
	// If not enough threads finish, then the TimeoutException occurs and we attempt to close/interupt the socket 
	// to the EOBR so that it stops blocking.
	private boolean WaitForThread(CyclicBarrier barrier, Thread t, int timeoutMillis)
	{
		boolean success = true;
		
  		try {
  			barrier.await(timeoutMillis, TimeUnit.MILLISECONDS);
  		} 
  		catch (InterruptedException e) {
  			Log.e("Barrier", "WaitForThread was interrupted", e);
  			success = false;
  		}
  		catch (BrokenBarrierException e) {
  			Log.e("Barrier", "WaitForThread had its barrier broken", e);
  			success = false;
  		}
  		catch (TimeoutException e) { 			
  			Log.e("Barrier", String.format("WaitForThread timed out, %s", t.getClass().getName()), e);
  			success = false;
			if(t instanceof ConnectBTSocketThread)
			{
	  			if(((ConnectBTSocketThread) t).socket != null)
	  			{
					((ConnectBTSocketThread) t).socket = null;
					//t.interrupt();
					//try {
						//_socket.close();
						//_socket = null;
					//} catch (IOException e1) {
					//	_socketConnectionExeption = e1;
					//} 
	  			}
			}
			else
			{
				Log.i("Barrier", String.format("Interrupting thread %d", t.getId()));
				
				t.interrupt();
				
				// when not handling socket connect threads, then log the timeouts
				ErrorLogHelper.RecordException(e);
			}
  		}
  		
  		return success;
  	}
	
	private boolean SendCommandWithoutData(int cmdId, short activeDeviceCrc)
	{
		boolean bStatus;
		
		Eobr_Packet eobrPacket = new Eobr_Packet();
		eobrPacket.setCmd((byte)cmdId);
		eobrPacket.setLen((byte)0);
		
		eobrPacket.setCrc(activeDeviceCrc);
		// sizeof(EOBR_PACKET) - EOBR_PAYLOAD_SIZE + eobrPacket.getLen() = 64 - 60 + len
		int packetSize = EobrEngineBase.EOBR_PACKET_SIZE - EobrEngineBase.EOBR_PAYLOAD_SIZE + eobrPacket.getLen();

		bStatus = sendReceiveBulkData.sendBulkData(eobrPacket, packetSize);
		
		return bStatus;
	}
}
