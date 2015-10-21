package com.gidi.places.easymap.dataBase;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.gidi.places.easymap.model.PlacesContract;

/**
 * Helper class that handles the opening of the DB (creates and drops tables)
 */
public class DbOpenHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "places.db";
    private static final int DB_VERSION = 1;

    /**
     * CTOR for DbOpneHelper. Gets context.
     * @param context
     */
    public DbOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql;
        // create a table:
        sql =
                "CREATE TABLE " + PlacesContract.Places.TABLE_NAME
                        + " ("
                        + PlacesContract.Places.ID +      " INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + PlacesContract.Places.LAT +     " REAL,"
                        + PlacesContract.Places.LNG +     " REAL,"
                        + PlacesContract.Places.ICON +    " TEXT,"
                        + PlacesContract.Places.NAME +    " TEXT,"
                        + PlacesContract.Places.VICINITY+ " TEXT,"
                        + PlacesContract.Places.FORMATTED_ADDRESS + " TEXT"
                        + ")";
        db.execSQL(sql);

        sql =
                "CREATE TABLE " + PlacesContract.Favorites.TABLE_NAME
                        + " ("
                        + PlacesContract.Favorites.ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + PlacesContract.Favorites.LAT +     " REAL,"
                        + PlacesContract.Favorites.LNG +     " REAL,"
                        + PlacesContract.Favorites.ICON +    " TEXT,"
                        + PlacesContract.Favorites.NAME +    " TEXT,"
                        + PlacesContract.Favorites.VICINITY+ " TEXT,"
                        + PlacesContract.Favorites.FORMATTED_ADDRESS + " TEXT"
                        + ")";
        db.execSQL(sql);


    }

    /**
     * Method that drops the table and creates new one
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql;
        sql = "DROP TABLE IF EXISTS " + PlacesContract.Places.TABLE_NAME;
        db.execSQL(sql);

        sql = "DROP TABLE IF EXISTS " + PlacesContract.Favorites.TABLE_NAME;
        db.execSQL(sql);
        onCreate(db);
    }
}
