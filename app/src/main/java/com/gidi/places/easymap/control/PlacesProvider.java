package com.gidi.places.easymap.control;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.gidi.places.easymap.dataBase.DbOpenHelper;

/**
 * The Content Provider class, that holds the main methods for DB (insert, query, delete etc.).
 */
public class PlacesProvider extends ContentProvider {
    DbOpenHelper dbOpenHelper;

    @Override
    public boolean onCreate() {
        dbOpenHelper = new DbOpenHelper(getContext());
        return true;
    }

    private String getTableName(Uri uri) {
        return uri.getPathSegments().get(0);
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        // get database
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();

        // query the right table
        Cursor cursor = db.query(
                getTableName(uri),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);

        // set what uri this cursor is relating to
        // when the data in this uri changed - a re-query is needed.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }


    /**
     * DB insert method
     *
     * @param uri
     * @param values
     * @return new row's uri
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // get database
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();

        // insert to the right table
        long id = db.insertWithOnConflict(
                getTableName(uri),
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE);

        // notify the content resolver - the data has changed!
        getContext().getContentResolver().notifyChange(uri, null);

        // return the new row's uri
        if (id > 0) {
            return ContentUris.withAppendedId(uri, id);
        } else {
            return null;
        }
    }

    /**
     * @param uri
     * @param selection
     * @param selectionArgs
     * @return count
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        // get database
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();

        // delete from the right table
        int count = db.delete(
                getTableName(uri),
                selection,
                selectionArgs);

        // notify the context resolver - the data changed!
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }


    /**
     * @param uri
     * @param values
     * @param selection
     * @param selectionArgs
     * @return count
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // get database
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();

        // update the right table
        int count = db.updateWithOnConflict(
                getTableName(uri),
                values,
                selection,
                selectionArgs,
                SQLiteDatabase.CONFLICT_REPLACE);

        // notify the content resolver - the data changed!
        getContext().getContentResolver().notifyChange(uri, null);

        return count;
    }
}
