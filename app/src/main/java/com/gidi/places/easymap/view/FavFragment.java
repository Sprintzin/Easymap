package com.gidi.places.easymap.view;


import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
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

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.gidi.places.easymap.R;
import com.gidi.places.easymap.model.Place;
import com.gidi.places.easymap.model.PlacesContract;


public class FavFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, LocationListener, AdapterView.OnItemClickListener {

    FavAdapter adapter;
    FragListener listener;
    TextView textView;
    double lat;
    double lng;
    private LocationManager lm;
    private String provider;
    ListView listView;

    public static FavFragment newInstance() {
        Bundle args = new Bundle();
        FavFragment fragment = new FavFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fav, container, false);
        listView = (ListView) view.findViewById(R.id.listView_fav);
        textView = (TextView) view.findViewById(R.id.textView);
        adapter = new FavAdapter(getActivity(), null);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        getLoaderManager().initLoader(1, null, this);
        registerForContextMenu(listView);

        //================================ GET LOCATION =========================
        lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        provider = lm.getBestProvider(criteria, true);
        Location lastKnownLocation = lm.getLastKnownLocation(provider);
        if (lastKnownLocation != null) {
            lat = lastKnownLocation.getLatitude();
            lng = lastKnownLocation.getLongitude();
            adapter.setLocation(lat, lng);
            adapter.notifyDataSetChanged();
        }


        return view;
    }

    //============================== Context menu===============================


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getActivity().getMenuInflater().inflate(R.menu.context_fav, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        long id = info.id;

        switch (item.getItemId()) {
            case R.id.context_delete:
                getActivity().getContentResolver()
                        .delete(PlacesContract.Favorites.FAV_URI,
                                PlacesContract.Favorites.ID + "=?",
                                new String[]{id + ""});
                return true;
            case R.id.context_share_fav:
                shareMethod(id);
                return true;
            case R.id.waze_fav:
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
                PlacesContract.Favorites.FAV_URI,
                null, PlacesContract.Places.ID + "=?",
                new String[]{id + ""},
                null);
        if (cursor.moveToFirst()) {
            if (cursor.getString(cursor.getColumnIndex(PlacesContract.Favorites.FORMATTED_ADDRESS)) != null) {
                address = cursor.getString(cursor.getColumnIndex(PlacesContract.Favorites.FORMATTED_ADDRESS));
            } else {
                address = cursor.getString(cursor.getColumnIndex(PlacesContract.Favorites.VICINITY));
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
                PlacesContract.Favorites.FAV_URI,
                null, PlacesContract.Places.ID + "=?",
                new String[]{id + ""},
                null);
        if (cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndex(PlacesContract.Favorites.NAME));
            String address;
            if (cursor.getString(cursor.getColumnIndex(PlacesContract.Favorites.FORMATTED_ADDRESS)) != null) {
                address = cursor.getString(cursor.getColumnIndex(PlacesContract.Favorites.FORMATTED_ADDRESS));
            } else {
                address = cursor.getString(cursor.getColumnIndex(PlacesContract.Favorites.VICINITY));

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
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        listener = (FragListener) activity;
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                PlacesContract.Favorites.FAV_URI,
                null,
                null,
                null,
                PlacesContract.Favorites.ID + " collate localized asc"
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
    public void onLocationChanged(Location location) {
        lat = location.getLatitude();
        lng = location.getLongitude();
        if (lat != 0 || lng != 0) {
            adapter.setLocation(lat, lng);
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
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Cursor cursor = getActivity().getContentResolver().query(
                PlacesContract.Favorites.FAV_URI,
                null,
                PlacesContract.Favorites.ID + "=?",
                new String[]{id + ""},
                null);
        if (cursor.moveToFirst()) {
            final double lat = cursor.getDouble(cursor.getColumnIndex(PlacesContract.Favorites.LAT));
            final double lng = cursor.getDouble(cursor.getColumnIndex(PlacesContract.Favorites.LNG));
            final String name = cursor.getString(cursor.getColumnIndex(PlacesContract.Favorites.NAME));
            Place place = new Place(lat, lng, name);

            listener.onPlaceSelected(place);

        }
        cursor.close();
    }


}
