package com.jjkeller.kmbapi.controller;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.jjkeller.kmbapi.R;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.EOBR.EobrReader;
import com.jjkeller.kmbapi.controller.dataaccess.AbstractDBAdapter;
import com.jjkeller.kmbapi.controller.share.ControllerBase;
import com.jjkeller.kmbapi.controller.share.KmbApplicationException;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.controller.utility.ErrorLogHelper;
import com.jjkeller.kmbapi.controller.utility.RESTWebServiceHelper;
import com.jjkeller.kmbapi.kmbeobr.Enums.EobrReturnCode;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileUploadController extends ControllerBase {

	public FileUploadController(Context ctx) {
		super(ctx);
	}

    /// <summary>
    /// Clear the KMB diagnostics.  
    /// Purge the KMB error log.
    /// </summary>
    /// <returns></returns>
	public boolean ClearKmbDiagnostics(Context ctx, ProgressDialog pd)
	{
		boolean isSuccessful = false;
		try
		{
			new ClearKmbDiagnosticsTask(ctx, pd).execute();
			isSuccessful = true;
		}
        catch (Exception excp)
        {
            // unknown error, should probably throw this error back
            this.HandleException(excp);
        }
        
		return isSuccessful;
	}
	
    /// <summary>
    /// Clear the EOBR diagnostics.  
    /// </summary>
    /// <returns></returns>
	public boolean ClearEobrDiagnostics(Context ctx, ProgressDialog pd)
	{
		boolean isSuccessful = false;
		try
		{
			new ClearEobrDiagnosticsTask(ctx, pd).execute();
			isSuccessful = true;
		}
        catch (Exception excp)
        {
            // unknown error, should probably throw this error back
            this.HandleException(excp);
        }
        
		return isSuccessful;
	}
	
    /// <summary>
    /// Upload the entire diagnostic package to DMO.
    /// Answer if successfully sent everything to DMO.
    /// If the network is not available, then ignore this.
    /// </summary>
    /// <param name="eventHandler"></param>
    /// <returns></returns>
    public boolean UploadDiagnosticPackage(ProgressDialog pd)
    {
        boolean isSuccessful = false;
        if (this.getIsNetworkAvailable())
        {
            try
            {
            	new UploadDiagnosticPackageTask(getContext(), pd).execute();

                isSuccessful = true;
            }
/*            catch (KMB.Services.AuthenticationLibrary.CFFaultException webServiceExcp)
            {
                this.HandleException(webServiceExcp);
            }
            catch (System.ServiceModel.CommunicationException commEx)
            {
                // some communication error, consider the service unavailable.
                Utility.ErrorLog.RecordException(commEx);
                Utility.NetworkUtilities.ReportNetworkError(commEx);
                throw new ApplicationException("Service is unavailable.\nTry again later.");
            }
            catch (System.TimeoutException timeoutEx)
            {
                // the call timed out, consider the service unavailable.
                Utility.ErrorLog.RecordException(timeoutEx);
                Utility.NetworkUtilities.ReportNetworkError(timeoutEx);
                throw new ApplicationException("Service is unavailable.\nTry again later.");
            }
*/            
            catch (Exception excp)
            {
                // unknown error, should probably throw this error back
                this.HandleException(excp);
            }
        }
        return isSuccessful;
    }


	private class UploadDiagnosticPackageTask extends AsyncTask<Void, String, Void> {
		ProgressDialog pd;
		Exception ex;
		Context ctx;
		
		public UploadDiagnosticPackageTask(Context ctx, ProgressDialog pd)
		{
			this.pd = pd;
			this.ctx = ctx;
		}
		
		protected void onPreExecute()
		{
			pd.setMessage(getContext().getString(R.string.msg_prepareupload));
			pd.show();
		}
		
		protected Void doInBackground(Void... params) {

			try
			{
				String uploadToken;
				RESTWebServiceHelper rwsh = new RESTWebServiceHelper(getContext());
	           
    			uploadToken = rwsh.InitiateFileUpload();
        		
	
	            publishProgress(getContext().getString(R.string.msg_uploadingerrorlog), "");

	            doFileUpload(uploadToken, getContext().getFilesDir() + File.separator, "kmberrlog.txt");
	            
	            publishProgress(getContext().getString(R.string.msg_uploadingdatabase), "");

	            File databasePath = getContext().getDatabasePath(AbstractDBAdapter.DATABASE_NAME);
	            doFileUpload(uploadToken, databasePath.getParent() + File.separator, databasePath.getName());
			}
			catch(Exception excp)
			{
				this.ex = excp;
			}
			return null;
		}

		protected void onProgressUpdate(String... updateVals) {
			if(!updateVals[0].equals(""))
			{
				pd.setMessage(updateVals[0]);
				pd.setProgress(0);
			}
			if(!updateVals[1].equals(""))
			{
				pd.setProgress(Integer.valueOf(updateVals[1]));
			}
		}

		protected void onPostExecute(Void unused) {
			try {
				if ((pd != null) && pd.isShowing()) {
					pd.dismiss();
				}

				if(ex != null)
				{
					HandleException(ex);

					AlertDialog.Builder builder= new AlertDialog.Builder(getContext());
					builder.setTitle(getContext().getString(R.string.msg_unabletouploaddiagnostics_title));
					builder.setMessage(R.string.msg_unabletouploaddiagnostics);
					builder.setPositiveButton("OK", new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {		            	
							UnlockScreenRotation(ctx);
						}
					});
					builder.show();
				}
				else
				{
					AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());
					builder1.setTitle(getContext().getString(R.string.uploadlabel));
					builder1.setMessage(getContext().getString(R.string.msgsuccessfullyuploadeddiagnosticinformation));
					builder1.setPositiveButton("OK", new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {		            	
							UnlockScreenRotation(ctx);
						}
					});
					builder1.show();
				}
			} catch (final IllegalArgumentException e) {
				// Handle or log or ignore
			} catch (final Exception e) {
				// Handle or log or ignore
			} finally {
				pd = null;
			} 
		}
        
        protected void doFileUpload(String uploadToken, String fileDir, String fileName) throws FileNotFoundException, IOException
        {
            File file = PrepareFileForUpload(uploadToken, fileDir, fileName);
            if(file != null)
            {
                FileInputStream fileStream = new FileInputStream(file);
                BufferedInputStream stream = new BufferedInputStream(fileStream);
                
                UploadFileToDMO(uploadToken, file, stream);
	            
	            // then, delete the backup copy
	            file.delete();
            }
        }
        
        private File PrepareFileForUpload(String uploadToken, String qualifiedDirectory, String fileName) throws IOException, FileNotFoundException
        {
        	File backupFile = null;
        	
            String qualifiedFilePath = qualifiedDirectory + fileName;
            File fileToUpload = new File(qualifiedFilePath);
            if (fileToUpload.exists())
            {
                // the file exists

                // first make a backup copy of the file
                String backupDirPath = qualifiedDirectory + "backup/";
                File backupDir = new File(backupDirPath);
                if (!backupDir.exists()) backupDir.mkdirs();

                String backupFilePath = backupDir.getPath() + "/" + fileName;
                backupFile = new File(backupFilePath);
                if (backupFile.exists()) backupFile.delete();

                this.CopyFile(fileToUpload, backupFile);
            }

            return backupFile;
        }

        /// <summary>
        /// Upload an individual file to DMO.
        /// </summary>
        private void UploadFileToDMO(String uploadToken, File fileName, BufferedInputStream stream)//, long totalBytesSent)
        {
			RESTWebServiceHelper rwsh = new RESTWebServiceHelper(getContext());
           
            // next, upload from the backup copy
    		long totalBytes = fileName.length();
    		if(totalBytes > 0)
    		{
                int chunkSize = 32 * 1024;
                byte[] fileChunk = new byte[chunkSize];

                long totalBytesSent = 0;
                while (totalBytesSent < totalBytes)
                {
                
	                // read the next chunk
	                int bytesRead = 0;
	    			try {
	    				bytesRead = stream.read(fileChunk, 0, fileChunk.length);
	    			} catch (IOException e) {
	    				this.ex = e;
	    			}
	                byte[] fileContentToSend;
	                if (bytesRead < fileChunk.length)
	                {
	                    // must be reading the last part of the file
	                    fileContentToSend = new byte[bytesRead];
	                    System.arraycopy(fileChunk, 0, fileContentToSend, 0, bytesRead);
	                }
	                else
	                {
	                    fileContentToSend = fileChunk;
	                }
	
                	try {
						rwsh.UploadFile(uploadToken, fileName.getName(), totalBytes, fileContentToSend);
					} catch (IOException e) {
						this.ex = e;
					}
	
	                // move the offset to the next location, so the next read is positioned correctly
	                totalBytesSent += bytesRead;
	                
	                long progress = ((totalBytesSent*100)/totalBytes);
	            	publishProgress("", String.valueOf(progress));
                }
    		}
        }

    	// Copies source file (src) to destination file (dst).
    	// If the dst file does not exist, it is created
    	private void CopyFile(File src, File dst) throws IOException {
    		InputStream in = new FileInputStream(src);
    		OutputStream out = new FileOutputStream(dst);

    		// Transfer bytes from in to out
    		byte[] buf = new byte[1024];
    		int len;
    		while ((len = in.read(buf)) > 0) {
    			out.write(buf, 0, len);
    		}
    		in.close();
    		out.close();
    	}
    	
	}
	
	public boolean UploadBackupErrorLogs(ProgressDialog pd)
    {
        boolean isSuccessful = false;
        if (this.getIsNetworkAvailable())
        {
            try
            {
            	new UploadBackupDiagnosticPackageTask(getContext(), pd).execute();

                isSuccessful = true;
            }
/*            catch (KMB.Services.AuthenticationLibrary.CFFaultException webServiceExcp)
            {
                this.HandleException(webServiceExcp);
            }
            catch (System.ServiceModel.CommunicationException commEx)
            {
                // some communication error, consider the service unavailable.
                Utility.ErrorLog.RecordException(commEx);
                Utility.NetworkUtilities.ReportNetworkError(commEx);
                throw new ApplicationException("Service is unavailable.\nTry again later.");
            }
            catch (System.TimeoutException timeoutEx)
            {
                // the call timed out, consider the service unavailable.
                Utility.ErrorLog.RecordException(timeoutEx);
                Utility.NetworkUtilities.ReportNetworkError(timeoutEx);
                throw new ApplicationException("Service is unavailable.\nTry again later.");
            }
*/            
            catch (Exception excp)
            {
                // unknown error, should probably throw this error back
                this.HandleException(excp);
            }
        }
        return isSuccessful;
    }
	
	private class UploadBackupDiagnosticPackageTask extends AsyncTask<Void, String, Void> {
		ProgressDialog pd;
		Exception ex;
		Context ctx;
		
		public UploadBackupDiagnosticPackageTask(Context ctx, ProgressDialog pd)
		{
			this.pd = pd;
			this.ctx = ctx;
		}
		
		protected void onPreExecute()
		{
			pd.setMessage(getContext().getString(R.string.msg_prepareupload));
			pd.show();
		}
		
		protected Void doInBackground(Void... params) {

			try
			{
				String uploadToken;
				RESTWebServiceHelper rwsh = new RESTWebServiceHelper(getContext());
	            
    			uploadToken = rwsh.InitiateFileUpload();
        		
	            File tempDir = new File(getContext().getFilesDir() + "/Temp/");
	            String[] errLogTextFiles = tempDir.list();
	            
	            if(errLogTextFiles != null)
	            {
	            	for (int i = 0; i < errLogTextFiles.length; i++) {
	            		int j = i + 1;
	            		publishProgress(getContext().getString(R.string.msg_uploadingerrorlog) + j + " of " + errLogTextFiles.length, "");
	            		
	            		doFileUpload(uploadToken, getContext().getFilesDir() + "/Temp/", errLogTextFiles[i]);
					}
	            }
	            
			}
			catch(Exception excp)
			{
				this.ex = excp;
			}
			return null;
		}

		protected void onProgressUpdate(String... updateVals) {
			if(!updateVals[0].equals(""))
			{
				pd.setMessage(updateVals[0]);
				pd.setProgress(0);
			}
			if(!updateVals[1].equals(""))
			{
				pd.setProgress(Integer.valueOf(updateVals[1]));
			}
		}

        protected void onPostExecute(Void unused) {
        	try
        	{
				if ((pd != null) && pd.isShowing()) {
					pd.dismiss();
				}

        		if(ex != null)
        		{
        			HandleException(ex);

        			AlertDialog.Builder builder= new AlertDialog.Builder(getContext());
        			builder.setTitle(getContext().getString(R.string.msg_unabletouploaddiagnostics_title));
        			builder.setMessage(R.string.msg_unabletouploaddiagnostics);
        			builder.setPositiveButton("OK", new OnClickListener() {
        				public void onClick(DialogInterface dialog, int which) {		            	
        					UnlockScreenRotation(ctx);
        				}
        			});
        			builder.show();
        		}
        		else
        		{
        			AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());
        			builder1.setTitle(getContext().getString(R.string.uploadolddiagnosticslabel));
        			builder1.setMessage(getContext().getString(R.string.msgsuccessfullyploadedolddiagnosticinformation));
        			builder1.setPositiveButton("OK", new OnClickListener() {
        				public void onClick(DialogInterface dialog, int which) {		            	
        					UnlockScreenRotation(ctx);
        				}
        			});
        			builder1.show();
        		}
        	} catch (final IllegalArgumentException e) {
        		// Handle or log or ignore
        	} catch (final Exception e) {
        		// Handle or log or ignore
        	} finally {
        		pd = null;
        	} 
        }
        
        protected void doFileUpload(String uploadToken, String fileDir, String fileName) throws FileNotFoundException, IOException
        {
            File file = PrepareFileForUpload(uploadToken, fileDir, fileName);
            if(file != null)
            {
                FileInputStream fileStream = new FileInputStream(file);
                BufferedInputStream stream = new BufferedInputStream(fileStream);
                UploadFileToDMO(uploadToken, file, stream);
                
	            // then, delete the backup copy
	            file.delete();
            }
        }
        
        private File PrepareFileForUpload(String uploadToken, String qualifiedDirectory, String fileName) throws IOException, FileNotFoundException
        {
            String qualifiedFilePath = qualifiedDirectory + fileName;
            File fileToUpload = new File(qualifiedFilePath);

            return fileToUpload;
        }

        /// <summary>
        /// Upload an individual file to DMO.
        /// </summary>
        private void UploadFileToDMO(String uploadToken, File fileName, BufferedInputStream stream)//, long totalBytesSent)
        {
			RESTWebServiceHelper rwsh = new RESTWebServiceHelper(getContext());
           
            // next, upload from the backup copy
    		long totalBytes = fileName.length();
    		if(totalBytes > 0)
    		{
                int chunkSize = 32 * 1024;
                byte[] fileChunk = new byte[chunkSize];

                long totalBytesSent = 0;
                while (totalBytesSent < totalBytes)
                {
                
	                // read the next chunk
	                int bytesRead = 0;
	    			try {
	    				bytesRead = stream.read(fileChunk, 0, fileChunk.length);
	    			} catch (IOException e) {
	    				this.ex = e;
	    			}
	                byte[] fileContentToSend;
	                if (bytesRead < fileChunk.length)
	                {
	                    // must be reading the last part of the file
	                    fileContentToSend = new byte[bytesRead];
	                    System.arraycopy(fileChunk, 0, fileContentToSend, 0, bytesRead);
	                }
	                else
	                {
	                    fileContentToSend = fileChunk;
	                }

	                try {
						rwsh.UploadFile(uploadToken, fileName.getName(), totalBytes, fileContentToSend);
					} catch (IOException e) {
						this.ex = e;
					}
	                
	                // move the offset to the next location, so the next read is positioned correctly
	                totalBytesSent += bytesRead;
	                
	                long progress = ((totalBytesSent*100)/totalBytes);
	            	publishProgress("", String.valueOf(progress));
                }
                
    		}
        }    	
	}
	
	public class ClearKmbDiagnosticsTask extends AsyncTask<Void, String, Void> {
		Context ctx;
		ProgressDialog pd;
		Exception ex;
		
		public ClearKmbDiagnosticsTask(Context ctx, ProgressDialog pd)
		{
			this.ctx = ctx;
			this.pd = pd;
		}
		
		protected void onPreExecute()
		{
			pd.setMessage(getContext().getString(R.string.msgclearingkmbdiagnostics));
			pd.show();
		}
		
		protected Void doInBackground(Void... params) {

			try
			{
				ErrorLogHelper.Purge(this.ctx);
			}
			catch(Exception excp)
			{
				this.ex = excp;
			}
			return null;
		}

		protected void onProgressUpdate(String... updateVals) {
			if(!updateVals[0].equals(""))
			{
				pd.setMessage(updateVals[0]);
				pd.setProgress(0);
			}
			if(!updateVals[1].equals(""))
			{
				pd.setProgress(Integer.valueOf(updateVals[1]));
			}
		}

        protected void onPostExecute(Void unused) {
        	try
        	{
				if ((pd != null) && pd.isShowing()) {
					pd.dismiss();
				}

        		if(ex != null)
        		{
        			HandleException(ex);

        			AlertDialog.Builder builder= new AlertDialog.Builder(getContext());
        			builder.setTitle(getContext().getString(R.string.msg_unabletoclearkmbdiagnosticinfo));
        			builder.setMessage(ex.getMessage());
        			builder.setPositiveButton("OK", new OnClickListener() {
        				public void onClick(DialogInterface dialog, int which) {		            	
        					UnlockScreenRotation(ctx);
        				}
        			});
        			builder.show();
        		}
        		else
        		{
        			AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());
        			builder1.setTitle(getContext().getString(R.string.clearkmbdiagnosticslabel));
        			builder1.setMessage(getContext().getString(R.string.msgsuccessfullyclearedkmbdiagnostics));
        			builder1.setPositiveButton("OK", new OnClickListener() {
        				public void onClick(DialogInterface dialog, int which) {		            	
        					UnlockScreenRotation(ctx);
        				}
        			});
        			builder1.show();
        		}
        	} catch (final IllegalArgumentException e) {
        		// Handle or log or ignore
        	} catch (final Exception e) {
        		// Handle or log or ignore
        	} finally {
        		pd = null;
        	} 
        }
 	}  
	
    public class ClearEobrDiagnosticsTask extends AsyncTask<Void, String, Void> {
		Context ctx;
		ProgressDialog pd;
		Exception ex;
		String clearingDeviceMessage;
		String unableToClearMessage;
		String clearDeviceTitle;
		String successfullyClearedMesseage;

		public ClearEobrDiagnosticsTask(Context ctx, ProgressDialog pd)
		{
			this.ctx = ctx;
			this.pd = pd;
			if (GlobalState.getInstance().getFeatureService().getIsEldMandateEnabled()) {
				clearingDeviceMessage = getContext().getString(R.string.msgclearingelddiagnostics);
				unableToClearMessage = getContext().getString(R.string.msg_unabletoclearelddiagnosticinfo);
				clearDeviceTitle = getContext().getString(R.string.clearelddiagnosticslabel);
				successfullyClearedMesseage = getContext().getString(R.string.msgsuccessfullyclearedelddiagnostics);
			} else {
				clearingDeviceMessage = getContext().getString(R.string.msgclearingeobrdiagnostics);
				unableToClearMessage = getContext().getString(R.string.msg_unabletocleareobrdiagnosticinfo);
				clearDeviceTitle = getContext().getString(R.string.cleareobrdiagnosticslabel);
				successfullyClearedMesseage = getContext().getString(R.string.msgsuccessfullyclearedeobrdiagnostics);

			}
		}
		
		protected void onPreExecute()
		{
			pd.setMessage(clearingDeviceMessage);
			pd.show();
		}
		
		protected Void doInBackground(Void... params) {

			try
			{		        
                EobrReader rdr = EobrReader.getInstance();
                
                if(rdr == null)
                	return null; 
                
                if (rdr.getCurrentConnectionState() == EobrReader.ConnectionState.ONLINE)
                {
                    String password = String.format("EOBR%s", rdr.getEobrSerialNumber());
                                        
                    int rc = clearErrorLogs(rdr, password);
                    
                    if (rc == EobrReturnCode.S_SUCCESS)
						rc = clearDiagnosticData(rdr, password);

                    if (rc != EobrReturnCode.S_SUCCESS)
                    {
                        throw new KmbApplicationException(String.format("API method '%s' failed with return code '%d'", "Technician_ClearAllRecordData", rc));
                    }
                }		        
			}
			catch(Exception excp)
			{
				this.ex = excp;
			}
			
			return null;
		}
		
		private int clearHistoricalRecords(EobrReader rdr, String password) {		
			return rdr.Technician_ClearAllRecordData(password, 0x01);
		}
		
		private int clearErrorLogs(EobrReader rdr, String password) {
			return rdr.Technician_ClearAllRecordData(password, 0x04);			
		}
		
		private int clearDiagnosticData(EobrReader rdr, String password) {			
			return rdr.Technician_ClearAllRecordData(password, 0x08);			
		}

		protected void onProgressUpdate(String... updateVals) {
			if(!updateVals[0].equals(""))
			{
				pd.setMessage(updateVals[0]);
				pd.setProgress(0);
			}
			if(!updateVals[1].equals(""))
			{
				pd.setProgress(Integer.valueOf(updateVals[1]));
			}
		}

		protected void onPostExecute(Void unused) {
			try
			{
				if ((pd != null) && pd.isShowing()) {
					pd.dismiss();
				}

				if(ex != null)
				{
					HandleException(ex);

					AlertDialog.Builder builder= new AlertDialog.Builder(getContext());
					builder.setTitle(unableToClearMessage);
					builder.setMessage(ex.getMessage());
					builder.setPositiveButton("OK", new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {		            	
							UnlockScreenRotation(ctx);
						}
					});
					builder.show();
				}
				else
				{
					AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());
					builder1.setTitle(clearDeviceTitle);
					builder1.setMessage(successfullyClearedMesseage);
					builder1.setPositiveButton("OK", new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {		            	
							UnlockScreenRotation(ctx);
						}
					});
					builder1.show();
				}
			} catch (final IllegalArgumentException e) {
				// Handle or log or ignore
			} catch (final Exception e) {
				// Handle or log or ignore
			} finally {
				pd = null;
			} 
		}
    }

    protected void UnlockScreenRotation(Context ctx)
	{
		// allow screen rotations again
		if(ctx instanceof Activity)
			((Activity)ctx).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
	}
    
    /**
     * Saves the console log from startDate to endDate to the error log
     * 
     * @param startDate
     * @param endDate
     * @return true if successful
     */
    public boolean SaveConsoleLog(Date startDate, Date endDate)
    {
    	boolean success = false;
    
    	try
    	{
	    	String log = EobrReader.getInstance().Technician_GetConsoleLog(this.getContext(), startDate, endDate);
	    	if(log != null)
	    	{
	    		SimpleDateFormat dateTimeFormat = DateUtility.getHomeTerminalDateTimeFormat();
	    		
	    		ErrorLogHelper.RecordMessage(String.format("%3$sConsole log from approximately %1$s to %2$s:%3$s%3$s%4$s", dateTimeFormat.format(startDate), dateTimeFormat.format(endDate), this.getContext().getString(R.string.newline), log));
	    		
	    		success = true;
	    	}
		}
		catch (Exception ex)
		{
	    	Log.e("UnhandledCatch", ex.getMessage() + ": " + Log.getStackTraceString(ex));	
		}
    	
    	return success;    	
    }
    
    
    public String SaveConsoleLogTestHarness(Context ctx, Date startDate, Date endDate) {
    	
    	String returnValue = null;
    
    	try {
	    	String log = EobrReader.getInstance().Technician_GetConsoleLog(ctx, startDate, endDate);
	    	
	    	if (log != null) {
	    		SimpleDateFormat dateTimeFormat = DateUtility.getHomeTerminalDateTimeFormat();
	    		ErrorLogHelper.RecordMessage(ctx, String.format("%3$sConsole log from approximately %1$s to %2$s:%3$s%3$s%4$s", dateTimeFormat.format(startDate), dateTimeFormat.format(endDate), ctx.getString(R.string.newline), log));	    		
	    		returnValue = log;
	    	}
		}
		catch (Exception ex) {
	    	Log.e("UnhandledCatch", ex.getMessage() + ": " + Log.getStackTraceString(ex));	
		}
    	
    	return returnValue;
    }
}
