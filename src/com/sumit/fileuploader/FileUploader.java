package com.sumit.fileuploader;

import android.app.Activity;
import android.util.Log;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.EventDispatcher;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class FileUploader extends AndroidNonvisibleComponent implements Component {
    private final String TAG = "FileUploader";
    private final Activity activity;

    public FileUploader(ComponentContainer container) {
        super(container.$form());
        this.activity = container.$context();
    }

    @SimpleEvent
    public void Response(boolean success, String result) {
        EventDispatcher.dispatchEvent(this, "Response", success, result);
    }

    @SimpleEvent
    public void ErrorOccurred(String error) {
    }

    private void fireEvent(String name, Object[] args) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                EventDispatcher.dispatchEvent(FileUploader.this, name, args);
            }
        });
    }

    @SimpleFunction
    public void UploadFile(String uploadUrl, String fileName, String filePath) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    final File file = new File(filePath);
                    final String name = fileName.isEmpty() ? file.getName() : fileName;

                    // Prepare the connection
                    HttpURLConnection connection = (HttpURLConnection) new URL(uploadUrl).openConnection();
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    connection.setUseCaches(false);
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Connection", "Keep-Alive");
                    connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=*****");

                    // Create the multipart/form-data request
                    DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
                    String twoHyphens = "--";
                    String boundary = "*****";
                    String lineEnd = "\r\n";

                    // Add the file to the request
                    outputStream.writeBytes(twoHyphens + boundary + lineEnd);
                    outputStream.writeBytes("Content-Disposition: form-data; name=\"fileToUpload\";filename=\"" + name + "\"" + lineEnd);
                    outputStream.writeBytes(lineEnd);

                    // Read the file and write it to the request
                    FileInputStream fileInputStream = new FileInputStream(file);
                    int bytesRead, bufferSize = 1024;
                    byte[] buffer = new byte[bufferSize];
                    while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    fileInputStream.close();

                    // Finish the request
                    outputStream.writeBytes(lineEnd);
                    outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                    // Get the response from the server
                    int responseCode = connection.getResponseCode();

                    InputStream inputStream = responseCode == 200 ? connection.getInputStream() : connection.getErrorStream();

                    // Read response
                    BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    // Handle the server response
                    final JSONObject object = new JSONObject(response.toString());
                    final boolean success = object.getBoolean("success");
                    final String message = object.getString("result");
                    fireEvent("Response", new Object[]{success, message});

                    outputStream.flush();
                    outputStream.close();
                    connection.disconnect();
                } catch (Exception e) {
                    Log.e(TAG, "", e);
                    fireEvent("ErrorOccurred", new Object[]{e.getMessage()});
                }
            }

        }).start();
    }
}