package com.jjkeller.kmbapi.controller.utility;

import android.content.Context;
import android.util.Log;

import com.jjkeller.kmbapi.R;
import com.jjkeller.kmbapi.common.TimeKeeper;
import com.jjkeller.kmbapi.configuration.GlobalState;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

//import android.R;

public class ErrorLogHelper {

	private static final String FILENAME = "kmberrlog.txt";
	//File will go to data/data/com.jjkeller.kmb/files/kmberrlog.txt
	
    private static final long MAX_ERRORLOG_FILESIZE = 512000;    // 512 Kb

    private static final String KMB_CLASS_PACKAGE = "com.jjkeller";
    
    public static void RecordException(Throwable ex)
    {
    	RecordException(GlobalState.getInstance(), ex, null);
    }
    
	public static void RecordException(Context ctx, Throwable ex)
	{
		RecordException(ctx, ex, null);
	}
	
	public static void RecordException(Context ctx, Throwable ex, String additionalInfo)
	{
		WriteLogEntry(ctx, FormatException(ctx, ex, additionalInfo));
	}
	
	public static void RecordMessage(String message)
	{
		RecordMessage(GlobalState.getInstance(), message);
	}
	
    public static void RecordMessage(Context ctx, String message)
    {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        
    	// Log times in the error log using phone's time
        SimpleDateFormat ErrorLogDateTimeFormat = DateUtility.getHomeTerminalDateTimeFormat();

		printWriter.append(String.format(Locale.getDefault(), "%s%s", ctx.getString(R.string.newline), ctx.getString(R.string.errorlog_headerseparator)));
        printWriter.append(String.format(Locale.getDefault(), "%s%s %s  %s", ctx.getString(R.string.newline), ctx.getString(R.string.app_name_api), ctx.getString(R.string.errorlog_messagerecordedat), ErrorLogDateTimeFormat.format(TimeKeeper.getInstance().now())));
        printWriter.append(String.format(Locale.getDefault(), "%s%s", ctx.getString(R.string.newline), ctx.getString(R.string.errorlog_headerseparator)));

        printWriter.append(String.format(Locale.getDefault(), "%s%s%s", ctx.getString(R.string.newline), ctx.getString(R.string.newline), message));
        printWriter.append(String.format(Locale.getDefault(), "%s%s%s%s%s%s%s", ctx.getString(R.string.newline), ctx.getString(R.string.errorlog_headerseparator), ctx.getString(R.string.newline), ctx.getString(R.string.newline), ctx.getString(R.string.newline), ctx.getString(R.string.newline), ctx.getString(R.string.newline)));

        WriteLogEntry(ctx, result.toString());
    }

	private static void WriteLogEntry(Context ctx, String s)
	{

        Log.e("ErrorLogHelper", s);
		// Check if the logfile is larger than MAX_ERRORLOG_FILESIZE and move it to a backup.
		File logFile = new File(ctx.getFilesDir(), FILENAME);
        if (logFile.exists() && logFile.length() > MAX_ERRORLOG_FILESIZE)
        {
            // the file is too big, move it to the backup directory
            try
            {
                Calendar now = Calendar.getInstance();
                File backupLogFolder = new File(ctx.getFilesDir().toString(),  "/" + GlobalState.TEMP_DATA_DIRECTORY);
                if (!backupLogFolder.exists())
                {
                    backupLogFolder.mkdirs();
                }
                String backupFileName = String.format("errorlog%1$d%2$03d%3$02d%4$02d.txt", now.get(Calendar.YEAR), now.get(Calendar.DAY_OF_YEAR), now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE));
                File backupLogFile = new File(backupLogFolder, backupFileName);
                
                FileUtility.CopyFile(logFile, backupLogFile);
                if(backupLogFile.exists()) logFile.delete();
            }
            catch(Throwable e) 
            { 
            	
            	Log.e("UnhandledCatch", e.getMessage() + ": " + Log.getStackTraceString(e));
            	
                // something bad happened trying to move the errorlog file
                // what to do here, maybe delete the file?
            }
        }
        
		FileOutputStream fos;

		try {
			fos = ctx.openFileOutput(FILENAME, Context.MODE_APPEND);
			fos.write(s.getBytes());
			fos.close();
		} catch (FileNotFoundException e) {
			
        	Log.e("UnhandledCatch", e.getMessage() + ": " + Log.getStackTraceString(e));
		} catch (IOException e) {
			
        	Log.e("UnhandledCatch", e.getMessage() + ": " + Log.getStackTraceString(e));
		}		
	}
	
	static List<String> GetLogText(Context ctx)
	{
		List<String> contents = new ArrayList<String>();
	
		File aFile = new File(ctx.getFilesDir().toString() + "/" + FILENAME);

		try {
            BufferedReader input =  new BufferedReader(new FileReader(aFile));
            try {
              String line = null;
              while (( line = input.readLine()) != null){
                contents.add(line);
              }
            }
            finally {
              input.close();
            }
          }
          catch (IOException ex){
        	
          	Log.e("UnhandledCatch", ex.getMessage() + ": " + Log.getStackTraceString(ex));
          }

		return contents;
	}	
	
    private static String FormatException(Context ctx, Throwable exception, String additionalInfo)
    {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);

    	// Log times in the error log using phone's time
        SimpleDateFormat ErrorLogDateTimeFormat = DateUtility.getHomeTerminalDateTimeFormat();

		printWriter.append(String.format(Locale.getDefault(), "%s%s", ctx.getString(R.string.newline), ctx.getString(R.string.errorlog_headerseparator)));
        printWriter.append(String.format(Locale.getDefault(), "%s%s %s:  %s", ctx.getString(R.string.newline), ctx.getString(R.string.app_name_api), ctx.getString(R.string.errorlog_errorrecordedat), ErrorLogDateTimeFormat.format(TimeKeeper.getInstance().now())));
        printWriter.append(String.format(Locale.getDefault(), "%s%s", ctx.getString(R.string.newline), ctx.getString(R.string.errorlog_headerseparator)));

        if (exception == null)
        {
        	printWriter.append(String.format(Locale.getDefault(), "%s%s%s%s", ctx.getString(R.string.newline), ctx.getString(R.string.newline), ctx.getString(R.string.errorlog_noexceptionprovided), ctx.getString(R.string.newline)));        	
        }
        else
        {
        	printWriter.append(String.format(Locale.getDefault(), "%s%s%s%s%s", ctx.getString(R.string.newline), ctx.getString(R.string.newline), ctx.getString(R.string.errorlog_exceptioninfolabel), ctx.getString(R.string.newline), ctx.getString(R.string.errorlog_textseparator)));
        	printWriter.append(String.format(Locale.getDefault(), "%s%s %s", ctx.getString(R.string.newline), ctx.getString(R.string.errorlog_messagelabel), exception.toString()));

        	if (exception.getCause() != null)
        		printWriter.append(String.format(Locale.getDefault(), "%s%s %s", ctx.getString(R.string.newline), ctx.getString(R.string.errorlog_causelabel), exception.getCause().toString()));
        	
        	if (additionalInfo != null && additionalInfo.length() > 0)
        	{
        		printWriter.append(String.format(Locale.getDefault(), "%s%s  %s%s%s", ctx.getString(R.string.newline), ctx.getString(R.string.newline), ctx.getString(R.string.errorlog_additionalinfolabel), ctx.getString(R.string.newline), ctx.getString(R.string.errorlog_textseparator)));
        		printWriter.append(String.format(Locale.getDefault(), "%s%s", ctx.getString(R.string.newline), additionalInfo));
        	}
        	
        	if (exception.getStackTrace().length > 0)
        	{
        		printWriter.append(String.format(Locale.getDefault(), "%s%s  %s%s%s%s", ctx.getString(R.string.newline), ctx.getString(R.string.newline), ctx.getString(R.string.errorlog_stacktraceinfolabel), ctx.getString(R.string.newline), ctx.getString(R.string.errorlog_textseparator), ctx.getString(R.string.newline)));
        		        		
        		int i=0;
        		boolean done = false;
        		StringBuilder sbStackTrace = new StringBuilder();  
        		
        		// determine if there is any KMB code at all in the stack trace
        		boolean stacktraceContainsAnyKmbCode = false;
        		while (!done){
    				String className = exception.getStackTrace()[i].getClassName();
    				if(className.startsWith(KMB_CLASS_PACKAGE)){
    					stacktraceContainsAnyKmbCode = true;
    					done = true;
    				}
    				i++;
    				if (i>=exception.getStackTrace().length)
    					done = true;
        		}
        		
        		i=0;
        		done = false;
        		boolean foundKmbCodeInCallStack = false;        		
        		while (!done)
        		{
        			if (sbStackTrace.length() > 0)
        			{
        				if (sbStackTrace.length() >= 999)
        					done = true;
        				else
        					sbStackTrace.append(ctx.getString(R.string.newline));
        			}

        			if (!done)
        			{
        				StringBuilder sbCurStackItem = new StringBuilder();
        				
        				// determine if the current stack trace item is for KMB code
        				String className = exception.getStackTrace()[i].getClassName();
        				if(stacktraceContainsAnyKmbCode && className.startsWith(KMB_CLASS_PACKAGE))
        					foundKmbCodeInCallStack = true;
        				
        				// determine if the stack trace item should be recorded
        				// always record the first 3 items in the traceback regardless
        				// do not record the item if there is some KMB stuff in the traceback, but we haven't moved down to find any yet 
        				boolean recordStackTraceItem = true;
        				if(i >= 3 && stacktraceContainsAnyKmbCode && !foundKmbCodeInCallStack){
        					recordStackTraceItem = false;
        				}
        				
        				if(recordStackTraceItem){
	        				sbCurStackItem.append(String.format(Locale.getDefault(), "%s %s.%s(%s:%s)", ctx.getString(R.string.at), className, exception.getStackTrace()[i].getMethodName(), exception.getStackTrace()[i].getFileName(), String.valueOf(exception.getStackTrace()[i].getLineNumber())));
	        				
	        				if (sbStackTrace.length() + sbCurStackItem.length() > 1000)
	        					sbStackTrace.append(sbCurStackItem.toString().subSequence(0, 1000-sbStackTrace.length()));
	        				else
	        					sbStackTrace.append(sbCurStackItem);
        				} 
        				else{
        					// skip this traceback level, put something in the log so that we know something was skipped
        					sbStackTrace.append(".");
        				}
        				
        				i++;
        				if (i>=exception.getStackTrace().length)
        					done = true;
        			}
        		}
        		
        		printWriter.append(String.format(Locale.getDefault(), "%s%s", ctx.getString(R.string.newline), sbStackTrace.toString()));
        	}
        }
        
        printWriter.append(String.format(Locale.getDefault(), "%s%s%s%s%s%s%s", ctx.getString(R.string.newline), ctx.getString(R.string.errorlog_headerseparator), ctx.getString(R.string.newline), ctx.getString(R.string.newline), ctx.getString(R.string.newline), ctx.getString(R.string.newline), ctx.getString(R.string.newline)));
        
        return result.toString();
    }
    
    /// <summary>
    /// Purge the error log file and all backup logs, then answer if successful
    /// </summary>
    /// <returns></returns>
	public static boolean Purge(Context ctx)
	{
		boolean isSuccessful = false;
		
        try
        {
            File backupLogFolder = new File(ctx.getFilesDir().toString(),  "/" + GlobalState.TEMP_DATA_DIRECTORY);
            File[] errLogFiles = backupLogFolder.listFiles();

            if(errLogFiles != null)
            {
            	for (int i = 0; i < errLogFiles.length; i++) {
	            	errLogFiles[i].delete();
				}
            	
            }
            
            File logFile = new File(ctx.getFilesDir().toString(), "/kmberrlog.txt");
            logFile.delete();
        }
        catch(Exception e) {
        	
        	Log.e("UnhandledCatch", e.getMessage() + ": " + Log.getStackTraceString(e));
        }
        
        return isSuccessful;
	}
	
}
