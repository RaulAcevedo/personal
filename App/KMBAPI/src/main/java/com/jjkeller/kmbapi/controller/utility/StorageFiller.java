package com.jjkeller.kmbapi.controller.utility;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.jjkeller.kmbapi.R;
import com.jjkeller.kmbapi.common.TimeKeeper;
import com.jjkeller.kmbapi.eobrengine.eobrreader.exceptions.InternalDeviceErrorEobrException;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Locale;

/**
 * Created by ief5781 on 5/19/17.
 */

public class StorageFiller {
    private final String TAG = "StorageFiller";
    private final String FILL_FILENAME_PREFIX = "KMBFillFile";
    private final String EXTENSION = "dat";
    Context context;

    public StorageFiller(Context context) {
        this.context = context;
    }

    public FillTask fillStorage(int megabytes) {
        FillTask fillTask = new FillTask(megabytes);
        fillTask.execute();

        return fillTask;
    }

    public CleanupTask cleanupFiles() {
        CleanupTask cleanupTask = new CleanupTask();
        cleanupTask.execute();

        return cleanupTask;
    }

    public File[] getFillFiles() {
        return context.getFilesDir().listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String name) {
                return name.startsWith(FILL_FILENAME_PREFIX) && name.endsWith(".dat");
            }
        });
    }

    class CleanupTask extends AsyncTask<Void, Integer, Void> {
        ProgressDialog progress;
        File[] files;

        @Override
        protected Void doInBackground(Void... voids) {
            Log.i(TAG, "Cleanup started");

            for(File file : files) {
                file.delete();
            }

            Log.i(TAG, "Cleanup completed");

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            files = getFillFiles();

            progress = new ProgressDialog(context);
            progress.setTitle(context.getString(R.string.cleanupStorage));
            progress.setMessage(context.getString(R.string.deletingFiles));
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.setCanceledOnTouchOutside(false);
            progress.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    CleanupTask.this.cancel(false);
                }
            });

            progress.show();
        }

        @Override
        protected void onCancelled(Void aVoid) {
            super.onCancelled(aVoid);
            progress.dismiss();

            Log.i(TAG, "Storage cleanup cancelled.");
        }

        @Override
        protected void onProgressUpdate(Integer... totalMegabytes) {
            super.onProgressUpdate(totalMegabytes);

            progress.setProgress(totalMegabytes[0]);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            progress.dismiss();
        }
    }

    class FillTask extends AsyncTask<Void, Integer, Void> {
        ProgressDialog progress;
        int megabytes;

        public FillTask(int megabytes) {
            this.megabytes = megabytes;
        }

        private String getFilename() {
            return String.format(Locale.getDefault(),
                    "%s/%s_%d.%s",
                    context.getFilesDir().getPath(),
                    FILL_FILENAME_PREFIX,
                    TimeKeeper.getInstance().getCurrentDateTime().getMillis(),
                    EXTENSION);
        }

        private void fillToFile(int megabytes) {
            String filename = getFilename();

            Log.i(TAG, String.format(Locale.getDefault(),
                    "Writing %d MB to file %s",
                    megabytes,
                    filename));

            String command = String.format(Locale.getDefault(),
                    "dd if=/dev/zero of=%s bs=1048576 count=%d",
                    filename,
                    megabytes);

            try {
                Process dd = Runtime.getRuntime().exec(command);
                dd.waitFor();
            } catch (Exception e) {
                ErrorLogHelper.RecordException(context, e, "Unable to execute command to write to file " + filename);
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Log.i(TAG, String.format(Locale.getDefault(),
                    "Beginning to fill storage with %d megabytes",
                    megabytes));

            final int mbPerFile = 100;
            int cumulativeMB = 0;
            for(int i = 0; i <= megabytes / mbPerFile; i++) {
                if(this.isCancelled())
                    break;

                fillToFile(mbPerFile);
                cumulativeMB += mbPerFile;

                publishProgress(cumulativeMB);
            }

            int remainingMb = megabytes % mbPerFile;
            if(!this.isCancelled() && remainingMb != 0) {
                fillToFile(remainingMb);
                cumulativeMB += remainingMb;

                publishProgress(cumulativeMB);
            }

            Log.i(TAG, "Done filling storage.");

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progress = new ProgressDialog(context);
            progress.setTitle(context.getString(R.string.fillingStorage));
            progress.setMax(megabytes);
            progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progress.setCanceledOnTouchOutside(false);
            progress.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    FillTask.this.cancel(false);
                }
            });

            progress.show();
        }

        @Override
        protected void onCancelled(Void aVoid) {
            super.onCancelled(aVoid);
            progress.dismiss();

            Log.i(TAG, "Storage fill cancelled.");
        }

        @Override
        protected void onProgressUpdate(Integer... totalMegabytes) {
            super.onProgressUpdate(totalMegabytes);

            progress.setProgress(totalMegabytes[0]);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            progress.dismiss();
        }
    }}
