package com.gidi.places.easymap.view;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.gidi.places.easymap.R;
import com.gidi.places.easymap.dataBase.ProviderHandler;
import com.gidi.places.easymap.model.Place;
import com.gidi.places.easymap.model.PlacesContract;

public class SearchFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, LocationListener, AdapterView.OnItemClickListener {

    private final static String TAG = "Search Fragment";

    private ListView listView;

    int connection = 1;

    private final static int SERVICE_DONE = 1;
    private final static int SERVICE_START = 2;




    FragListener listener;
    private LocationManager lm;
    private String provider;
    Adapter adapter;

    private double lat;
    private double lng;
    String query;
    public ProgressBar progressBar;

    public static SearchFragment newInstance() {
        Bundle args = new Bundle();
        SearchFragment fragment = new SearchFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);


        listView = (ListView) view.findViewById(R.id.listView_search);
        listView.setOnItemClickListener(this);
        registerForContextMenu(listView);
        adapter = new Adapter(getActivity(), null);
        listView.setAdapter(adapter);

        getLoaderManager().initLoader(1, null, this);

        //================================ GET LOCATION =========================
        lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        provider = lm.getBestProvider(criteria, true);
        Location lastKnownLocation = lm.getLastKnownLocation(provider);
        if (lastKnownLocation != null) {
            lat = lastKnownLocation.getLatitude();
            lng = lastKnownLocation.getLongitude();
            //Until real location is available for the adapter, get this one:
            adapter.setLocation(lat, lng);
            adapter.notifyDataSetChanged();
        }
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(ServiceDoneReceiver,
                new IntentFilter("Service done"));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(ServiceStartReceiver,
                new IntentFilter("Service start"));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(ConnectionReceiver,
                new IntentFilter("disconnected"));

        return view;
    }

    //============================== Context menu===============================
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo
            menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getActivity().getMenuInflater().inflate(R.menu.context_search, menu);

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        long id = info.id;

        switch (item.getItemId()) {
            case R.id.context_save:
                ProviderHandler.searchToFavs(getActivity(), id);
                return true;
            case R.id.context_share:
                shareMethod(id);
                return true;
            case R.id.waze:
                waze(id);
                return true;
        }

        return super.onContextItemSelected(item);

    }

    /**
     * Method that takes the address/vicinity of the place and searches it on Waze
     *
     * @param id
     */

    private void waze(long id) {
        String address = null;

        Cursor cursor = getActivity().getContentResolver().query(
                PlacesContract.Places.CONTENT_URI,
                null, PlacesContract.Places.ID + "=?",
                new String[]{id + ""},
                null);
        if (cursor.moveToFirst()) {
            if (cursor.getString(cursor.getColumnIndex(PlacesContract.Places.FORMATTED_ADDRESS)) != null) {
                address = cursor.getString(cursor.getColumnIndex(PlacesContract.Places.FORMATTED_ADDRESS));
            } else {
                address = cursor.getString(cursor.getColumnIndex(PlacesContract.Places.VICINITY));

            }
        }
        try {
            String url = "waze://?q=" + address;
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            Intent intent =
                    new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.waze"));
            startActivity(intent);
        }
        cursor.close();

    }

    /**
     * Method that shares the name and the address externally
     *
     * @param id
     */
    private void shareMethod(long id) {

        Cursor cursor = getActivity().getContentResolver().query(
                PlacesContract.Places.CONTENT_URI,
                null, PlacesContract.Places.ID + "=?",
                new String[]{id + ""},
                null);
        if (cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndex(PlacesContract.Places.NAME));
            String address;
            if (cursor.getString(cursor.getColumnIndex(PlacesContract.Places.FORMATTED_ADDRESS)) != null) {
                address = cursor.getString(cursor.getColumnIndex(PlacesContract.Places.FORMATTED_ADDRESS));
            } else {
                address = cursor.getString(cursor.getColumnIndex(PlacesContract.Places.VICINITY));

            }

            Intent intent;
            intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, "Check out this place: " + name);
            intent.putExtra(Intent.EXTRA_TEXT, "At :" + address);

            startActivity(intent);


        }
        cursor.close();

    }
    //=====================================================================


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        return new CursorLoader(getActivity(),
                PlacesContract.Places.CONTENT_URI,
                null,
                null,
                null,
                PlacesContract.Places.ID + " collate localized asc"
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        listener = (FragListener) activity;
    }

    @Override
    public void onResume() {
        super.onResume();
        lm.requestLocationUpdates(provider, 5000, 1, this);
        adapter.notifyDataSetChanged();

    }

    @Override
    public void onPause() {
        super.onPause();
        lm.removeUpdates(this);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(ServiceDoneReceiver);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(ConnectionReceiver);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(ServiceStartReceiver);


    }

    @Override
    public void onLocationChanged(Location location) {
        lat = location.getLatitude();
        lng = location.getLongitude();
        if (lat != 0 || lng != 0) {
            adapter.setLocation(lat, lng);
            Log.e(TAG, "" + lat + lng);
            adapter.notifyDataSetChanged();
        }

    }


    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Cursor cursor = getActivity().getContentResolver().query(
                PlacesContract.Places.CONTENT_URI,
                null,
                PlacesContract.Places.ID + "=?",
                new String[]{id + ""},
                null);
        if (cursor.moveToFirst()) {
            final double lat = cursor.getDouble(cursor.getColumnIndex(PlacesContract.Places.LAT));
            final double lng = cursor.getDouble(cursor.getColumnIndex(PlacesContract.Places.LNG));
            final String name = cursor.getString(cursor.getColumnIndex(PlacesContract.Places.NAME));
            Place place = new Place(lat, lng, name);

            listener.onPlaceSelected(place);

        }
        cursor.close();


    }


    private BroadcastReceiver ServiceDoneReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int done = intent.getIntExtra("Done", 0);
            Log.e(TAG, "done = " + done);

            if (done == SERVICE_DONE) {
                progressBar.setVisibility(View.INVISIBLE);
                progressBar.setIndeterminate(false);
            }
        }
    };

    private BroadcastReceiver ConnectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            connection = intent.getIntExtra("connection", 0);
            Log.e(TAG, "Got extra connection (Receiver): " + connection);
        }
    };

    private BroadcastReceiver ServiceStartReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int start = intent.getIntExtra("Start", 0);
            if (start == SERVICE_START){
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setIndeterminate(true);
            }
        }
    };



}
