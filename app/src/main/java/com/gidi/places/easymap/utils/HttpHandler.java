package com.gidi.places.easymap.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Handler class that gets bitmaps and more
 */
public class HttpHandler {
    final static String TAG = "HttpHandler";

    // ----------------------------------------------------------------------------
    // -- HTTP GET

    public static String get(String address, String queryString) {

        HttpURLConnection connection = null;
        BufferedReader input = null;
        try {

            // the url
            URL url;
            if (queryString != null) {
                // with query string (separated by '?')
                url = new URL(address + "?" + queryString);
            } else {
                // no query string
                url = new URL(address);
            }

            // open and set up the connection:
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.setDoOutput(false);

            // check HTTP response code:
            // codes 200-299 are HTTP OK, 300+ are HTTP errors
            if (connection.getResponseCode() >= 300) {
                return null;
            }

            // we can read the response from the connection's input stream:
            // we'll wrap it with a Input Stream Reader to read it as text
            // we'll wrap it with a Buffered Reader to be more efficient
            input = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            // we'll read the response from the input stream line by line
            // and collect all the lines to a String Builder
            // until we get null - the end of the response
            StringBuilder response = new StringBuilder();
            String line = "";
            while ((line = input.readLine()) != null) {
                response.append(line + "\n");
            }

            // return as string:
            return response.toString();

        } catch (MalformedURLException e) {
            // oops
            e.printStackTrace();
            return null;
        } catch (ProtocolException e) {
            // oops
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            // oops
            e.printStackTrace();
            return null;
        } finally {

            // clean up
            // close streams and disconnect.

            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    //=============================BitmapListener==========================
    public interface GetBitmapListener{
        void onGetBitmapProgress(int progress, int size);
    }

    // ----------------------------------------------------------------------------
    // -- HTTP GET bitmap
    //    with a listener (may be null)
    //    while the image is being downloaded
    //    the method will call the listener to notify the progress
    public static Bitmap getBitmap(String address, GetBitmapListener listener) {

        HttpURLConnection connection = null;
        ByteArrayOutputStream byteArrayStream = null;
        InputStream input = null;

        try {
            // the url
            URL url = new URL(address);

            // prepare the connection:
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.setDoOutput(false);

            // check HTTP response code:
            // codes 200-299 are HTTP OK, 300+ are HTTP errors
            if (connection.getResponseCode() >= 300) {
                Log.e(TAG, "http connection status code " + connection.getResponseCode());
                return null;
            }

            // read response :
            // this time we'll just read the raw bytes (it's not text)
            input = connection.getInputStream();

            // the response length
            int length = connection.getContentLength();

            // notify the listener on the progress
            // ( 0 / length ):
            if (listener!=null){
                listener.onGetBitmapProgress(0, length);
            }

            // a stream to hold the read bytes.
            byteArrayStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int totalBytesRead = 0;
            int bytesRead = 0;

            // read the bytes from the input stream
            // until we get -1 : the end of the response
            // we'll write the bytes to the byteArrayStream (we'll use a buffer to be more efficient)
            // we'll count the numbers of bytes we read so far.
            while ((bytesRead = input.read(buffer, 0, buffer.length)) != -1) {
                totalBytesRead += bytesRead;
                byteArrayStream.write(buffer, 0, bytesRead);

                // notify the listener on the progress
                // ( totalBytesRead / length ):
                if (listener!=null){
                    listener.onGetBitmapProgress(totalBytesRead, length);
                }

            }

            // flush the output stream
            // (write all pending bytes in its internal buffer)
            byteArrayStream.flush();

            // get a byte array out of the byteArrayStream and convert them to a bitmap
            byte[] imageBytes = byteArrayStream.toByteArray();
            Bitmap b = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

            // return the bitmap
            return b;

        } catch (MalformedURLException e) {
            //oops
            e.printStackTrace();
            return null;
        } catch (ProtocolException e) {
            //oops
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            //oops
            e.printStackTrace();
            return null;
        } finally {

            // clean up
            // close buffers and disconnect

            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (byteArrayStream != null) {
                try {
                    byteArrayStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
