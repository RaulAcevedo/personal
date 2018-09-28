package com.jjkeller.kmbapi.controller;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.jjkeller.kmbapi.R;
import com.jjkeller.kmbapi.controller.interfaces.IAppUpdate;
import com.jjkeller.kmbapi.controller.interfaces.IAppUpdateEvent;
import com.jjkeller.kmbapi.controller.interfaces.IAppUpdateHandler;
import com.jjkeller.kmbapi.controller.share.KmbApplicationException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class EncompassAppUpdate implements IAppUpdate {
    private IAppUpdateHandler _handler = null;
    public void addHandler(IAppUpdateHandler handler) {
        _handler = handler;
    }

    /// <summary>
    /// event to register delegates for update on the download
    /// progress
    /// </summary>
    private IAppUpdateEvent OnAppUpdateDownload = null;

    /// <summary>
    /// request to download the update
    /// </summary>
    private URLConnection _connection = null;

    /// <summary>
    /// total number of bytes expected to receive for the download file
    /// </summary>
    private long _maxSizeOfUpdate = -1;

    /// <summary>
    /// block size of the chunk to download at one time
    /// </summary>
    private final int DATA_BLOCK_SIZE = 65536;

    /// <summary>
    /// fully qualified path the update file which was just downloaded
    /// </summary>
    private File _filePath = null;


    private String LOCAL_FILENAME = "KellerMobile.apk";
    private String LOCAL_ALK_FILENAME = "KellerMobile-alk.apk";

    /// <summary>
    /// response to the request to download the update
    /// </summary>
    private InputStream _inputStream = null;

    /// <summary>
    /// stream to write the update file
    /// </summary>
    private OutputStream _outputStream = null;

    /// <summary>
    /// byte array used to download the update
    /// </summary>
    private byte[] _dataBuffer = null;


    /// <summary>
    /// number of bytes received for the downloaded update
    /// </summary>
    private long _totalReceived = 0;

    /// <summary>
    /// indicator if the user requests the download to cancel
    /// </summary>
    private boolean _cancelDownloadByUser = false;

    /// <summary>
    /// indicator if the download has completed
    /// </summary>
    private boolean _isDownloadComplete = false;

    /// <summary>
    /// indicator if the download was completely successful.
    /// (i.e. the file was downloaded intact)
    /// </summary>
    private boolean _isSuccessfullyDownloaded = false;

    /// <summary>
    /// Download the application update.  Answer if the update was
    /// successfully started.  This does not imply that it was
    /// completely downloaded.
    /// </summary>
    /// <returns></returns>
    public boolean downloadUpdates(IAppUpdateEvent onAppUpdateEvent) throws KmbApplicationException {
        boolean isSuccessful = false;

        try {
            // register the delegate
            this.OnAppUpdateDownload = onAppUpdateEvent;

            URL url = new URL(_handler.getApplicationUpdateInfo().getDownloadUrl());
            _connection = url.openConnection();
            _connection.connect();
            // this will be useful so that you can show a typical 0-100% progress bar
            _maxSizeOfUpdate = _connection.getContentLength();

            if (_handler.getPackageName().equals(_handler.getContext().getString(R.string.alkpackage))) {
                _filePath = new File(Environment.getExternalStorageDirectory() + "/download/", LOCAL_FILENAME);
            } else {
                _filePath = new File(Environment.getExternalStorageDirectory() + "/download/", LOCAL_ALK_FILENAME);
            }
            if(_filePath.exists()) {
                _filePath.delete();
            } else {
                File parentFile = _filePath.getParentFile();
                if(parentFile != null)
                    parentFile.mkdirs();
            }
            _filePath.createNewFile();

            // download the file
            _inputStream = new BufferedInputStream(url.openStream());
            _outputStream = new FileOutputStream(_filePath);

            _dataBuffer = new byte[DATA_BLOCK_SIZE];

            _totalReceived = 0;

            // start reading the file, asynchronously
            new EncompassAppUpdate.OnDataReadTask().execute();

            isSuccessful = true;
        } catch (MalformedURLException e) {
            String tag = _handler.getContext().getString(R.string.downloadupdates);
            String caption = _handler.getContext().getString(R.string.exception_webservicecommerror);
            String displayMessage = _handler.getContext().getString(R.string.exception_serviceunavailable);
            _handler.HandleExceptionAndThrow(e, tag, caption, displayMessage);
        } catch (FileNotFoundException e) {
            String tag = _handler.getContext().getString(R.string.downloadupdates);
            String caption = _handler.getContext().getString(R.string.exception_webservicecommerror);
            String displayMessage = _handler.getContext().getString(R.string.exception_serviceunavailable);
            _handler.HandleExceptionAndThrow(e, tag, caption, displayMessage);
        } catch (IOException e) {
            String tag = _handler.getContext().getString(R.string.downloadupdates);
            String caption = _handler.getContext().getString(R.string.exception_webservicecommerror);
            String displayMessage = _handler.getContext().getString(R.string.exception_serviceunavailable);
            _handler.HandleExceptionAndThrow(e, tag, caption, displayMessage);
        }

        return isSuccessful;
    }

    /// <summary>
    /// Answer if the download is still in progress.
    /// </summary>
    /// <returns></returns>
    public boolean isDownloadInProgress() {
        return _totalReceived > 0 && !_isDownloadComplete;
    }

    /// <summary>
    /// Cancel the download process
    /// </summary>
    /// <returns></returns>
    @Override
    public void cancelDownload()
    {
        _cancelDownloadByUser = true;
    }

    /// <summary>
    /// Execute the install that was just successfully downloaded.
    /// The install will run shelled into a new process.
    /// Answer if the process was able to start successfully.
    /// </summary>
    @Override
    public boolean performInstall() throws KmbApplicationException {
        // first, make sure that the download was successfully completed
        boolean isSuccessfullyStarted = false;
        if (!_isSuccessfullyDownloaded) {
            throw new KmbApplicationException("Update was not successfully downloaded.");
        }

        // execute the update file
        if (_filePath != null && _filePath.exists() && _filePath.length() > 0) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(_filePath), "application/vnd.android.package-archive");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                _handler.getContext().startActivity(intent);

                isSuccessfullyStarted = true;
            } catch (Exception e) {
                Log.e("UnhandledCatch", e.getMessage() + ": " + Log.getStackTraceString(e));
            }
        }

        return isSuccessfullyStarted;
    }

    /// <summary>
    /// Process the asynchronous read of the download file.
    /// The download file is read in chunks.  If the download has not completed
    /// yet, then issue another asynchronous read.
    /// </summary>
    /// <param name="res"></param>
    private class OnDataReadTask extends AsyncTask<Void, Void, Void> {

        Exception ex;

        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                int count;
                while ((count = _inputStream.read(_dataBuffer)) != -1 && !_cancelDownloadByUser) {
                    _totalReceived += count;
                    _outputStream.write(_dataBuffer, 0, count);
                    publishUpdateEvent();
                }

                if(count <= 0 || _cancelDownloadByUser) {
                    _isDownloadComplete = true;

                    // determine if the amount received was the amount expected
                    _isSuccessfullyDownloaded = ((FileOutputStream)_outputStream).getChannel().size() == _maxSizeOfUpdate;

                    publishUpdateEvent();
                }

                if(_isSuccessfullyDownloaded) shutDown();
            } catch (IOException e) {
                this.ex = e;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            if(ex != null)
            {
                if (ex.getClass() == IOException.class)
                    _handler.HandleException((IOException)ex);
            }
        }
    }

    /// <summary>
    /// Publish the progress of the update to the UI
    /// </summary>
    private void publishUpdateEvent() {
        if (this.OnAppUpdateDownload != null) {
            AppUpdateEventArgs args = new AppUpdateEventArgs();

            args.setMaxSize(_maxSizeOfUpdate);
            args.setTotalReceived(_totalReceived);
            args.setWasDownloadSuccessful(_isSuccessfullyDownloaded);
            args.setHasDownloadCompleted(_isDownloadComplete);

            this.OnAppUpdateDownload.onAppUpdateDownload(args);
        }
    }

    /// <summary>
    /// Clean up the local state of the controller.
    /// </summary>
    public void shutDown() throws IOException {
        // close the file stream
        if (_outputStream != null) {
            _outputStream.close();
            _outputStream = null;
        }

        // drop the delegates
        this.OnAppUpdateDownload = null;

        // general cleanup
        _maxSizeOfUpdate = 0;
        _totalReceived = 0;
        _dataBuffer = null;

        _connection = null;
        if( _inputStream != null ) {
            _inputStream.close();
            _inputStream = null;
        }
    }
}
