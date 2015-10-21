package com.gidi.places.easymap.control;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.gidi.places.easymap.model.PlacesContract;
import com.gidi.places.easymap.utils.HttpHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Intent Service class
 */

public class Service extends IntentService {

    public static final String ACTION_SEARCH_BY_TEXT = "com.gidi.places.easymap.control.TEXT";
    public static final String ACTION_SEARCH_NEARBY = "com.gidi.places.easymap.control.NEARBY";

    public static final String EXTRA_QUERY = "com.gidi.places.easymap.control.extra.QUERY";
    public static final String WEB_KEY = "YOUR WEB KEY";
    private final static String TAG = "Service";
    public final static String URL_TEXT = "https://maps.googleapis.com/maps/api/place/textsearch/json";
    public final static String URL_NEARBY = "https://maps.googleapis.com/maps/api/place/nearbysearch/json";
    private final static int SERVICE_DONE = 1;
    private final static int SERVICE_START = 2;


    double lat;
    double lng;
    String query;


    public Service() {
        super("Service");
    }


    @Override
    protected void onHandleIntent(Intent intent) {

        if (intent != null) {
            String action = intent.getAction();

            if (action.equals(ACTION_SEARCH_BY_TEXT)) {

                getContentResolver().delete(PlacesContract.Places.CONTENT_URI, null, null);

                Intent broadcastIntent = new Intent("Service start");
                broadcastIntent.putExtra("Start", SERVICE_START);
                LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);

                query = intent.getStringExtra("query");
                searchByText(query);
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("query", query);
                editor.commit();


            } else if (action.equals(ACTION_SEARCH_NEARBY)) {
                getContentResolver().delete(PlacesContract.Places.CONTENT_URI, null, null);

                Intent broadcastIntent = new Intent("Service start");
                broadcastIntent.putExtra("Start", SERVICE_START);
                LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);

                query = intent.getStringExtra("query");
                lat = intent.getDoubleExtra("lat", 0);
                lng = intent.getDoubleExtra("lng", 0);
                searchNearby(query, lat, lng);
                Log.e(TAG, "lat" + lat + "lng" + lng);
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("query", query);
                editor.commit();
            }
        }

        Intent broadcastIntent = new Intent("Service done");
        broadcastIntent.putExtra("Done", SERVICE_DONE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
    }

    /**
     * Method, that reads JSON results and enters them into the Content provider
     *
     * @param query
     */
    private void searchByText(String query) {

        String queryString = null;
        try {
            queryString = "" +
                    "query=" + URLEncoder.encode(query, "utf-8") +
                    "&key=" + WEB_KEY;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String result = HttpHandler.get(URL_TEXT, queryString);
        if (result == null) {
            Toast.makeText(this, "No results", Toast.LENGTH_SHORT).show();
        } else {
//                    Log.e(TAG, result);

            JSONObject jsonObject;
            try {
                jsonObject = new JSONObject(result);
                JSONArray jsonArray = jsonObject.getJSONArray("results");
                Log.e(TAG, "after json array and object");


                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);
                    String formatted_address = object.getString("formatted_address");
                    Log.e(TAG, "formated addres" + formatted_address);

                    JSONObject geometry = object.getJSONObject("geometry");
                    Log.e(TAG, "geometry" + geometry);

                    JSONObject location = geometry.getJSONObject("location");
                    Log.e(TAG, "location" + location);

                    double lat = location.getDouble("lat");
                    double lng = location.getDouble("lng");
                    Log.e(TAG, "lat-lng" + lat + lng);

                    String icon = object.getString("icon");
                    Log.e(TAG, "icon" + icon);

                    String name = object.getString("name");
                    Log.e(TAG, "name" + name);

                    Log.e(TAG, "after json array and object" + lat + lng + formatted_address + icon + name);

                    ContentValues values = new ContentValues();
                    values.put(PlacesContract.Places.LAT, lat);
                    values.put(PlacesContract.Places.LNG, lng);
                    values.put(PlacesContract.Places.NAME, name);
                    values.put(PlacesContract.Places.FORMATTED_ADDRESS, formatted_address);
                    values.put(PlacesContract.Places.ICON, icon);

                    getContentResolver().insert(PlacesContract.Places.CONTENT_URI, values);

                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(Service.this, "error parsing results...", Toast.LENGTH_LONG).show();
            }

        }

    }

    private void searchNearby(String keyword, double lat, double lng) {

        String queryString = null;
        try {
            queryString = "" +
                    "location=" + lat + "," + lng +
                    "&keyword=" + URLEncoder.encode(keyword, "utf-8") +
                    "&radius=" + 5000 +
                    "&key=" + WEB_KEY;
            Log.e(TAG, "Query string lat" + lat + "lng" + lng);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String result = HttpHandler.get(URL_NEARBY, queryString);
        if (result == null) {
            Toast.makeText(this, "No results", Toast.LENGTH_SHORT).show();
        } else {
            Log.e(TAG, "Result is " + result);
        }

        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(result);
            JSONArray jsonArray = jsonObject.getJSONArray("results");
            Log.e(TAG, "after json array and object");


            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                String icon = object.getString("icon");
                Log.e(TAG, "icon" + icon);
                String name = object.getString("name");
                Log.e(TAG, "name" + name);
                String vicinity = object.getString("vicinity");
                JSONObject geometry = object.getJSONObject("geometry");
                Log.e(TAG, "geometry" + geometry);
                JSONObject location = geometry.getJSONObject("location");
                Log.e(TAG, "location" + location);
                lat = location.getDouble("lat");
                lng = location.getDouble("lng");
                Log.e(TAG, "lat-lng" + lat + lng);

                Log.e(TAG, "after json array and object" + lat + lng + vicinity + icon + name);

                ContentValues values = new ContentValues();
                values.put(PlacesContract.Places.LAT, lat);
                values.put(PlacesContract.Places.LNG, lng);
                values.put(PlacesContract.Places.NAME, name);
                values.put(PlacesContract.Places.VICINITY, vicinity);
                values.put(PlacesContract.Places.ICON, icon);

                getContentResolver().insert(PlacesContract.Places.CONTENT_URI, values);

            }
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(Service.this, "error parsing results...", Toast.LENGTH_LONG).show();
        }


    }


}
