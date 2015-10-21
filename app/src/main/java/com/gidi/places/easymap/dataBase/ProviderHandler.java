package com.gidi.places.easymap.dataBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.gidi.places.easymap.model.PlacesContract;

/**
 * Gets position, reads the columns, enters them to new table
 */
public class ProviderHandler {

    public static void searchToFavs(Context context, long id) {
        Cursor cursor = context.getContentResolver().query(
                PlacesContract.Places.CONTENT_URI,
                null, PlacesContract.Places.ID + "=?",
                new String[]{id + ""},
                null);
        if (cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndex(PlacesContract.Places.NAME));
            double lat = cursor.getDouble(cursor.getColumnIndex(PlacesContract.Places.LAT));
            double lng = cursor.getDouble(cursor.getColumnIndex(PlacesContract.Places.LNG));
            String icon = cursor.getString(cursor.getColumnIndex(PlacesContract.Places.ICON));
            String address = cursor.getString(cursor.getColumnIndex(PlacesContract.Places.FORMATTED_ADDRESS));
            String vicinity = cursor.getString(cursor.getColumnIndex(PlacesContract.Places.VICINITY));


            ContentValues values = new ContentValues();
            values.put(PlacesContract.Favorites.LAT, lat);
            values.put(PlacesContract.Favorites.LNG, lng);
            values.put(PlacesContract.Favorites.NAME, name);
            values.put(PlacesContract.Favorites.FORMATTED_ADDRESS, address);
            values.put(PlacesContract.Favorites.VICINITY, vicinity);
            values.put(PlacesContract.Favorites.ICON, icon);

            context.getContentResolver().insert(PlacesContract.Favorites.FAV_URI, values);
        }
        cursor.close();

    }
}
