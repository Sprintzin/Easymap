package com.gidi.places.easymap.model;

import android.net.Uri;

// API key for web search = YOUR KEY (STORE HERE)
// API key for maps = YOUR KEY (STORE HERE)


/**
 * Public class that is used as contract. Stores AUTHORITY and Places.class
 */
public class PlacesContract {

    public static final String AUTHORITY = "com.gidi.places.easymap.model.provider.places";

    /**
     * Public class that stores TABLE_NAME,
     * columns names and other constants.
     */
    public static class Places{

        public static final String TABLE_NAME = "places";
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" +TABLE_NAME);

        public static final String ID = "_id";
        public static final String LAT = "lat";
        public static final String LNG = "lng";
        public static final String ICON = "icon";
        public static final String NAME = "name";
        public static final String VICINITY = "vicinity";
        public static final String FORMATTED_ADDRESS = "formatted_address";


    }

    /**
     * Public class that stores TABLE_NAME,
     * columns and other constants
     */
    public static class Favorites{
        public static final String TABLE_NAME = "favorites";
        public static final Uri FAV_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);

        public static final String ID = "_id";
        public static final String LAT = "lat";
        public static final String LNG = "lng";
        public static final String ICON = "icon";
        public static final String NAME = "name";
        public static final String VICINITY = "vicinity";
        public static final String FORMATTED_ADDRESS = "formatted_address";
    }
}
