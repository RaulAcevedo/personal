package com.jjkeller.kmbapi.controller.utility;

import android.os.Environment;

import com.jjkeller.kmbapi.controller.dataaccess.AbstractDBAdapter;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtility {

	public static boolean IsExternalStorageMounted()
	{
		boolean isMounted = false;
		String state = Environment.getExternalStorageState();
		
		if (Environment.MEDIA_MOUNTED.equals(state))
		{
			isMounted = true;
		}
		
		return isMounted;
	}
	
	//4-10-2014 MEC: Find out if the compliance file exists from CDW install
	public static boolean IsComplianceTabletFileInstalled() {
		boolean complianceTabletFileFound = false;
		if(IsExternalStorageMounted())
		{
			try
	    	{
				File complianceDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/KMB");
				if (complianceDir.exists())
				{
					File complianceFile = new File(complianceDir + "/complianceTablet.txt");
					complianceTabletFileFound = complianceFile.exists();
				}
	    	}
			catch(Throwable e){ complianceTabletFileFound = false; }
		}
		return complianceTabletFileFound;
	}
	
	public static boolean MoveDiagnosticsToSDCard(File filesDir)
	{
		boolean success = false;
		
		if(IsExternalStorageMounted())
		{
	    	try
	    	{
				File diagDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/kmbdiag");
				if (!diagDir.exists())
					diagDir.mkdir();
	
				File errorLog = new File(filesDir + "/kmberrlog.txt");
				if(errorLog.exists())
				{
					FileUtility.CopyFile(errorLog, new File(diagDir.getAbsolutePath() + "/" + errorLog.getName()));
				}
				
				File db = new File(AbstractDBAdapter.DATABASE_PATH + AbstractDBAdapter.DATABASE_NAME);
				if(db.exists())
				{
					FileUtility.CopyFile(db, new File(diagDir.getAbsolutePath() + "/" + AbstractDBAdapter.DATABASE_NAME));
				}
	
	            File tempDir = new File(filesDir + "/Temp/");
	            String[] errLogTextFiles = tempDir.list();            
	            if(errLogTextFiles != null)
	            {
	            	for (int i = 0; i < errLogTextFiles.length; i++) {
	            		FileUtility.CopyFile(new File(filesDir + "/Temp/", errLogTextFiles[i]), new File(diagDir.getAbsolutePath() + "/" + errLogTextFiles[i] + "_Temp" + String.valueOf(i+1)));
					}
	            }

	            success = true;
			}
			catch(Throwable e){ success = false; }
		}
		
		return success;
	}
	
	public static boolean CopyDatabaseToPhone()
	{
		boolean success = false;
		
		if(IsExternalStorageMounted())
		{
			try
			{
				File diagDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/kmbdiag");
				if (diagDir.exists())
				{
					File db = new File(AbstractDBAdapter.DATABASE_PATH + AbstractDBAdapter.DATABASE_NAME);
					if(db.exists())
					{
						FileUtility.CopyFile(new File(diagDir.getAbsolutePath() + "/" + AbstractDBAdapter.DATABASE_NAME), db);
					}
					success = true;
				}
				else
				{
					success = false;
				}
			}
			catch(Throwable e){ success = false; }
		}
		
		return success;
	}

	// Copies source file (src) to destination file (dst).
	// If the dst file does not exist, it is created
	public static void CopyFile(File src, File dst) throws IOException {
		InputStream in = new FileInputStream(src);
		OutputStream out = new FileOutputStream(dst);

		// Transfer bytes from in to out
		CopyFile(in, out);
		in.close();
		out.close();
	}

	public static void CopyFile(InputStream in, OutputStream out) throws IOException {
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
	}

	public static String InputStreamToString(InputStream inputStream) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		byte buf[] = new byte[1024];
		int len;
		try {
			while ((len = inputStream.read(buf)) != -1) {
				outputStream.write(buf, 0, len);
			}
			outputStream.close();
			inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return outputStream.toString();
	}

	public static byte[] InputStreamToBytes(InputStream inputStream) throws IOException
	{
		byte[] streamAsBytes = null;

		BufferedInputStream bufferedInputStream;
		ByteArrayOutputStream streamBytes = null;
		try
		{
			bufferedInputStream = new BufferedInputStream(inputStream);
			streamBytes = new ByteArrayOutputStream();

			int bytesRead;
			byte[] buffer = new byte[16384];
			while ((bytesRead = bufferedInputStream.read(buffer, 0, buffer.length)) > 0)
			{
				streamBytes.write(buffer, 0, bytesRead);
			}
			streamBytes.flush();

			streamAsBytes = streamBytes.toByteArray();
		}
		finally
		{
			if (streamBytes != null)
				streamBytes.close();
		}

		return streamAsBytes;
	}
}
