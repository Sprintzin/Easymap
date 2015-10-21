package com.gidi.places.easymap.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.util.LruCache;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.gidi.places.easymap.R;
import com.gidi.places.easymap.model.PlacesContract;
import com.gidi.places.easymap.utils.HttpHandler;

/**
 * Adapter for search fragment
 */
public class Adapter extends CursorAdapter {
    private LruCache<String, Bitmap> bitmapCache;

     double myLat;
     double myLng;
    private final static String TAG = "Adapter";

    public void setLocation(double lat, double lng) {

        this.myLat = lat;
        this.myLng = lng;

    }

    class ViewHolder {
        long id;
        TextView textName;
        TextView textAddress;
        TextView textDistance;
        ImageView icon;
    }


    public Adapter(Context context, Cursor c) {
        super(context, c, 0);

        // prepare a cache for the images.
        // key : the url. value : the bitmap.

        //max size : 4 MB
        int numImages = 4 * 1024 * 1024;
        this.bitmapCache = new LruCache<String, Bitmap>(numImages) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                // this is how to calculate a bitmap size in bytes.
                // (bytes-in-a-row * height)
                return value.getRowBytes() * value.getHeight();
            }
        };
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        View view = LayoutInflater.from(context).inflate(R.layout.adapter_style, viewGroup, false);
        ViewHolder holder = new ViewHolder();
        holder.textName = (TextView) view.findViewById(R.id.name);
        holder.textAddress = (TextView) view.findViewById(R.id.address);
        holder.textDistance = (TextView) view.findViewById(R.id.distance);
        holder.icon = (ImageView) view.findViewById(R.id.imageView);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndex(PlacesContract.Places.ID));
        String address;
        String name = cursor.getString(cursor.getColumnIndex(PlacesContract.Places.NAME));
        address = cursor.getString(cursor.getColumnIndex(PlacesContract.Places.FORMATTED_ADDRESS));
        if (address == null) {
            address = cursor.getString(cursor.getColumnIndex(PlacesContract.Places.VICINITY));
        }
        double lat = cursor.getDouble(cursor.getColumnIndex(PlacesContract.Places.LAT));
        double lng = cursor.getDouble(cursor.getColumnIndex(PlacesContract.Places.LNG));
        Log.e(TAG, "Lat is "+lat+" , "+ "Lng is "+ lng);

        String icon = cursor.getString(cursor.getColumnIndex(PlacesContract.Places.ICON));


        ViewHolder holder = (ViewHolder) view.getTag();
        holder.id = id;
        holder.textName.setText(name);
        holder.textAddress.setText(address);

        double d = haversine(lat, lng, myLat, myLng);
        Log.e(TAG, "Lat is "+lat+" , "+ "Lng is "+ lng + "myLat is "+myLat+" , "+ "myLng is "+ myLng );

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String distance = sp.getString("distance", "km");
        if (distance.equals("miles")) {
            d = d / 1.61;

            String dist = String.format("%.2f", d);
            holder.textDistance.setText(dist + " miles");

        } else {

            String dist = String.format("%.2f", d);
            holder.textDistance.setText(dist + " km");
        }



        Bitmap cachedBmp = bitmapCache.get(icon);
        if (cachedBmp != null) {
            // we do - just use it!
            holder.icon.setVisibility(View.VISIBLE);
            holder.icon.setImageBitmap(cachedBmp);
        } else {
            //we don't... get it async
            //until the image is downloaded - hide the image view:
            holder.icon.setVisibility(View.INVISIBLE);

            GetImageTask task = new GetImageTask(id, holder);
            task.execute(icon);
        }
    }


    public static double haversine(double lat, double lng, double myLat, double myLng) {
        int r = 6371; // average radius of the earth in km
        double dLat = Math.toRadians(myLat - lat);
        double dLon = Math.toRadians(myLng - lng);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat)) * Math.cos(Math.toRadians(myLat))
                        * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = r * c;
        return d;
    }


    class GetImageTask extends AsyncTask<String, Void, Bitmap> {


        private final long id;
        private final ViewHolder holder;

        public GetImageTask(long id, ViewHolder holder) {
            this.id = id;
            this.holder = holder;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            //download:
            String address = params[0];
            Bitmap bitmap = HttpHandler.getBitmap(address, null);

            //save it in the cache for later:
            if (bitmap != null) {
                bitmapCache.put(address, bitmap);
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {

            // check - is the view's holder still holding the same movie we started the download for?
            // or did the view get recycled and now displaying a different movie?
            if (id == holder.id) {
                // it's still the same movie !

                //restore the visibility and show the thumb
                holder.icon.setVisibility(View.VISIBLE);

                if (result != null) {
                    holder.icon.setImageBitmap(result);
                } else {
                    //error in download...
                    holder.icon.setImageResource(R.mipmap.ic_launcher);
                }
            }
        }
    }
}

