package com.jjkeller.kmbapi.controller.utility;

import android.content.Context;
import android.util.Log;

import com.jjkeller.kmbapi.CodeBlocks;
import com.jjkeller.kmbapi.controller.DataUsageController;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.Map;

/**
 * Created by aaz3239 on 4/13/17.
 */

public class HttpHelper {

    public static int TIMEOUT_MS = 15000;
    private Context _context;


    public HttpHelper(Context context){
        _context = context;
    }

    public String Post(String uri, Map<String, String> headers, String body) throws IOException {
        return Post(uri, headers, body, TIMEOUT_MS);
    }

    public String Post(String uri, Map<String, String> headers, String body, int timeoutMs ) throws IOException{

        if(uri == null || uri.isEmpty()){
            throw new IOException("Uri is missing");
        }

        HttpURLConnection connection = null;
        String result = "";


        long uidRxBefore = 0;
        long uidTxBefore = 0;

        if (DeviceInfo.IsAPIAvailable(8)) {
            uidRxBefore = DeviceInfo.GetRxBytes();
            uidTxBefore = DeviceInfo.GetTxBytes();
        }

        try{
            URL url = new URL(uri);

            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            if(headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    connection.addRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            connection.addRequestProperty("Content-Type", "application/json; charset=utf-8");

            connection.setConnectTimeout(timeoutMs);
            connection.setReadTimeout(timeoutMs);
            // write to the webservice
            OutputStreamWriter osw = new OutputStreamWriter(
                    connection.getOutputStream());
            osw.write(body);
            osw.flush();

            // read the response
            result = readStream(connection.getInputStream());

            osw.close();
        } catch (IOException ex) {
            // Retrieve Response code
            int responseCode = connection.getResponseCode();
            String detailMessage = String.format(Locale.US,"%s\nResponseCode: %d", ex.getMessage(), responseCode);

            throw new IOException(detailMessage, ex);

        } finally {
            connection.disconnect();
            printStats(uri, uidRxBefore, uidTxBefore);
        }
        return result;


    }

    public String Get(String uri, Map<String,String> headers, int timeout) throws IOException{

        return Get(uri, headers, timeout, new CodeBlocks.Func1IOException<InputStream, String>() {
            @Override
            public String execute(InputStream input1) throws IOException {
                return readStream(input1);
            }
        });
    }

    public byte[] GetByteStream(String uri, Map<String,String> headers, int timeout) throws IOException{

        return Get(uri, headers, timeout, new CodeBlocks.Func1IOException<InputStream, byte[]>() {
            @Override
            public byte[] execute(InputStream input1) throws IOException {
                return readByteStream(input1);
            }
        });
    }

    private <T> T Get(String uri, Map<String,String> headers, int timeout, CodeBlocks.Func1IOException<InputStream, T> readStream) throws IOException {
        T response = null;
        HttpURLConnection connection = null;

        long uidRxBefore = 0;
        long uidTxBefore = 0;

        if (DeviceInfo.IsAPIAvailable(8)) {
            uidRxBefore = DeviceInfo.GetRxBytes();
            uidTxBefore = DeviceInfo.GetTxBytes();
        }

        try {
            URL url = new URL(uri);

            // covers both http and https because httpsURLConnection extends
            // URLConnection
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            if(headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    connection.addRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            connection.addRequestProperty("Accept", "application/json   ");

            // set connection timeout and read timeout to 2 minutes
            connection.setConnectTimeout(timeout);
            connection.setReadTimeout(timeout);

            connection.connect();
            response = readStream.execute(connection.getInputStream());// readByteStream(connection.getInputStream());

        } catch (IOException ex) {
            // Retrieve Responsecode
            int responseCode = connection.getResponseCode();

            // Retrieve the detail message from the connection error stream and
            // create
            // and throw our own IOException that contains the detail message
            // returned
            // from the REST web service
            String errorResponse = readStream(connection.getErrorStream());
            String detailMessage = "";

            if (errorResponse != null && errorResponse.length() > 0) {
                // Error stream will contain something similar to this string
                // when an exception
                // is generated with specific text in the web service:
                // {"Detail":"Activation code is not invalid"}
                // Need to parse out the specific message after Detail and
                // remove the double quotes
                if (errorResponse.contains("Detail")) {
                    // remove leading and trailing {} and split on :
                    String errors[] = errorResponse.substring(1,
                            errorResponse.length() - 1).split(":");

                    if (errors.length > 0) {
                        for (int i = 0; i < errors.length; i++) {
                            // remove leading and trailing double quotes
                            if (errors[i].substring(1, errors[i].length() - 1)
                                    .equalsIgnoreCase("detail")) {
                                if (i < errors.length - 1)
                                    detailMessage = errors[i + 1].substring(1,
                                            errors[i + 1].length() - 1);

                                break;
                            }
                        }
                    }
                }
            }

            // if a detail message is detected, create a new IOException with
            // that message and throw
            // that exception, otherwise throw the original exception that was
            // generated
            if (!detailMessage.equals("")) {
                detailMessage = String.format("%s\nResponseCode: %d",
                        detailMessage, responseCode);
                IOException newException = new IOException(detailMessage);
                throw newException;
            } else {
                detailMessage = String.format("%s\nResponseCode: %d",
                        ex.getMessage(), responseCode);

                IOException newException = new IOException(detailMessage, ex);
                throw newException;

            }
        } finally {
            connection.disconnect();
            printStats(uri, uidRxBefore, uidTxBefore);
        }
        return response;
    }

    private String readStream(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();

        if (in != null) {
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line = "";

            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
        }

        return sb.toString();
    }

    private byte[] readByteStream(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        if (in != null) {
            int line;
            byte[] buffer = new byte[4096];

            while((line = in.read(buffer)) > 0) {
                out.write(buffer, 0, line);
            }
        }

        return out.toByteArray();
    }
    
    private void printStats(String url, long uidRxBefore, long uidTxBefore) {
        // print gzip stats to log
        if (DeviceInfo.IsAPIAvailable(8)) {
            long uidRxAfter = DeviceInfo.GetRxBytes();
            long uidTxAfter = DeviceInfo.GetTxBytes();
            long uidRxAfterDiff = uidRxAfter - uidRxBefore;
            long uidTxAfterDiff = uidTxAfter - uidTxBefore;
            Log.i("TrafficStats",
                    "RequestUri: " + url + " | After- SentTotal: "
                            + Long.toString(uidTxAfterDiff)
                            + " | After- ReceiveTotal: "
                            + Long.toString(uidRxAfterDiff)
                            + " | Before- Sent: " + Long.toString(uidTxBefore)
                            + " Received: " + Long.toString(uidRxBefore)
                            + " | After- Sent: " + Long.toString(uidTxAfter)
                            + " Received: " + Long.toString(uidRxAfter)
                            + " | Gzip= false");
            (new DataUsageController(this._context)).UpdateDataUsage(
                    uidTxAfter - uidTxBefore, uidRxAfter - uidRxBefore, url);
        }
    }
}
