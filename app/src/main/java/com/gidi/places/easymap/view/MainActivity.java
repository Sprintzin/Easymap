package com.gidi.places.easymap.view;

import android.annotation.TargetApi;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import com.gidi.places.easymap.R;
import com.gidi.places.easymap.control.Service;
import com.gidi.places.easymap.model.Place;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;


public class MainActivity extends AppCompatActivity implements FragListener, LocationListener {

    SupportMapFragment mapFragment;
    GoogleMapOptions options;
    TabLayout tabLayout;
    ViewPager viewPager;
    private static final String TAG = "MainActivity";
    private LocationManager lm;
    private String provider;
    double lat;
    double lng;
    private Marker marker;
    private Marker myMarker;
    MainActivity.FragmentPagerAdapter adapter;
    double myLat;
    double myLng;
    String myName;
    PowerConnectionReceiver receiver;
    NetworkReceiver networkReceiver;
    int connection = 0;
    private static final int CONNECTED = 1;
    private static final int NOT_CONNECTED = 2;
    private SearchView searchView;
    private final static int SERVICE_DONE = 1;
    private final static int SEARCH_BY_TEXT = 1;
    private final static int SEARCH_NEARBY = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LocalBroadcastManager.getInstance(this).registerReceiver(ServiceDoneReceiver,
                new IntentFilter("Service done"));

        //================================ GET LOCATION =========================
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        provider = lm.getBestProvider(criteria, true);
        Location lastKnownLocation = lm.getLastKnownLocation(provider);
        if (lastKnownLocation != null) {
            lat = lastKnownLocation.getLatitude();
            lng = lastKnownLocation.getLongitude();
        }

        if (isPhone()) {
            //phone:
            // Get the ViewPager and set it's PagerAdapter so that it can display items
            viewPager = (ViewPager) findViewById(R.id.viewpager);
            adapter = new FragmentPagerAdapter(
                    getSupportFragmentManager(),
                    MainActivity.this);
            viewPager.setAdapter(adapter);

            // Give the TabLayout the ViewPager
            tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
            tabLayout.setupWithViewPager(viewPager);

        } else {
            //Tablet:
            if (savedInstanceState == null) {
                //create all three fragments
                Fragment frag_search = SearchFragment.newInstance();
                Fragment frag_vav = FavFragment.newInstance();
                create_map_fragment();
                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                //add them to tablet containers
                ft.add(R.id.cont_map, mapFragment, "map");
                ft.add(R.id.cont_search, frag_search, "search");
                ft.add(R.id.cont_favs, frag_vav, "fav");
                ft.commit();

            }
        }

        //Check if the app was opened for the first time:
        final String PREFS_NAME = "MyPrefsFile";

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

        if (settings.getBoolean("my_first_time", true)) {
            beginSearch("restaurant");
            settings.edit().putBoolean("my_first_time", false).commit();
        }
    }

    /**
     * Public method that creates a google map fragment
     */
    public void create_map_fragment() {
        //Create a google map fragment
        options = new GoogleMapOptions();
        // - map type , and initial camera
        options.mapType(GoogleMap.MAP_TYPE_NORMAL);
        LatLng location = new LatLng(lat, lng);
        options.camera(CameraPosition.fromLatLngZoom(location, 12));

        // create the map fragment with the options - using newInstance(options) :
        mapFragment = SupportMapFragment.newInstance(options);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                // add a map marker:
                LatLng location = new LatLng(lat, lng);
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(location)
                        .title("Your location")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                marker = googleMap.addMarker(markerOptions);
            }
        });


    }

    /**
     * Method that finds what device the user uses
     *
     * @return boolean: true - phone, false - tablet
     */
    protected boolean isPhone() {

        View layout = findViewById(R.id.phone);

        if (layout != null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Listener method for fragment-activity communication
     *
     * @param place
     */
    @Override
    public void onPlaceSelected(Place place) {
        myLat = place.getLat();
        myLng = place.getLng();
        myName = place.getName();
        LatLng latLng = new LatLng(myLat, myLng);
        if (isPhone()) {
            if (mapFragment != null) {
                viewPager.setCurrentItem(1);
                mapFragment.getMap().animateCamera(CameraUpdateFactory.newLatLng(latLng));
                if (myMarker == null) {
                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(latLng)
                            .title(myName)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                    myMarker = mapFragment.getMap().addMarker(markerOptions);
                } else {
                    myMarker.setPosition(new LatLng(myLat, myLng));
                }
            } else {
                //make map...
                mapFragment.newInstance(options);
                viewPager.setCurrentItem(1);
            }

        } else {
            //Tablet:
            mapFragment.getMap().animateCamera(CameraUpdateFactory.newLatLng(latLng));
            if (myMarker == null) {
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(latLng)
                        .title(myName)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                myMarker = mapFragment.getMap().addMarker(markerOptions);
            } else {
                myMarker.setPosition(new LatLng(myLat, myLng));
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        final SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                // Do something
                return false;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                query = searchView.getQuery().toString();
                Log.e(TAG, "Main query is " + query);
                beginSearch(query);
                searchView.setQuery("", false);
                searchView.setIconified(true);
                if (viewPager.getCurrentItem() != 0) {
                    viewPager.setCurrentItem(0);
                }
                return true;
            }
        };

        searchView.setOnQueryTextListener(queryTextListener);
        return true;
    }

    public void beginSearch(String query) {
        Log.e(TAG, "got query " + query);
        Intent intent;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sp.edit();

        String search = sp.getString("search", "Search nearby");
        Log.e(TAG, "we will search by " + search);


        if (search.equals("Free search")) {

            if (connection == NOT_CONNECTED) {
                Toast.makeText(this, "No Internet connection. Can`t search", Toast.LENGTH_SHORT).show();

            } else {
                if (query.length() == 0) {
                    Toast.makeText(this, "Please enter text", Toast.LENGTH_SHORT).show();
                } else {
                    intent = new Intent(this, Service.class);
                    intent.setAction(Service.ACTION_SEARCH_BY_TEXT);
                    intent.putExtra("query", query);
                    this.startService(intent);
                    editor.putInt("refresh", SEARCH_BY_TEXT);
                    editor.commit();
                }
            }
        } else if (search.equals("Search nearby") || search.equals(null)) {
            if (connection == NOT_CONNECTED) {
                Toast.makeText(this, "No Internet connection. Can`t search", Toast.LENGTH_SHORT).show();
            } else {
                if (query.length() == 0) {
                    Toast.makeText(this, "Please enter text", Toast.LENGTH_SHORT).show();
                } else {
                    intent = new Intent(this, Service.class);
                    intent.setAction(Service.ACTION_SEARCH_NEARBY);
                    intent.putExtra("query", query);
                    intent.putExtra("lat", lat);
                    intent.putExtra("lng", lng);
                    Log.e(TAG, "lat" + lat + "lng" + lng);
                    this.startService(intent);
                    editor.putInt("refresh", SEARCH_NEARBY);
                    editor.commit();
                }
            }
        }
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onBackPressed() {
        if (!searchView.isIconified()) {
            searchView.setQuery("", false);
            searchView.setIconified(true);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // open the preferences activity
                Intent intent = new Intent(this, MyPrefs.class);
                startActivity(intent);
                break;
        }
        return true;
    }

    @Override
    public void onLocationChanged(Location location) {
        lat = location.getLatitude();
        lng = location.getLongitude();

        if (marker != null) {
            marker.setPosition(new LatLng(lat, lng));

        }

    }


    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        switch (i) {
            case LocationProvider.AVAILABLE:
                break;
            case LocationProvider.OUT_OF_SERVICE:
                Toast.makeText(this, "The location provider is out of service", Toast.LENGTH_SHORT).show();
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                Toast.makeText(this, "The location provider is temporarily unavailable", Toast.LENGTH_SHORT).show();
                break;
        }

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {
        Toast.makeText(this, "Please turn your GPS on", Toast.LENGTH_SHORT).show();

    }


//-------------------------------

    public class FragmentPagerAdapter extends android.support.v4.app.FragmentPagerAdapter {
        final int PAGE_COUNT = 3;
        private String tabTitles[] = new String[]{"Search", "Map", "Favorites"};
        private Context context;

        public FragmentPagerAdapter(FragmentManager fm, Context context) {
            super(fm);
            this.context = context;
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    return SearchFragment.newInstance();
                case 1:
                    if (mapFragment == null) {

                        options = new GoogleMapOptions();
                        // - map type , and initial camera
                        options.mapType(GoogleMap.MAP_TYPE_NORMAL);
                        LatLng location = new LatLng(lat, lng);
                        options.camera(CameraPosition.fromLatLngZoom(location, 12));
                        mapFragment = SupportMapFragment.newInstance(options);

                        mapFragment.getMapAsync(new OnMapReadyCallback() {
                            @Override
                            public void onMapReady(GoogleMap googleMap) {
                                // add a map marker:
                                LatLng location = new LatLng(lat, lng);
                                MarkerOptions markerOptions = new MarkerOptions()
                                        .position(location)
                                        .title("Your location")
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                                marker = googleMap.addMarker(markerOptions);
                            }
                        });
                    }


                    return mapFragment;
                case 2:
                    return FavFragment.newInstance();
            }

            return null;
        }


        @Override
        public CharSequence getPageTitle(int position) {
            // Generate title based on item position
            return tabTitles[position];

        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putDouble("lat", lat);
        outState.putDouble("lng", lng);
        outState.putDouble("myLat", myLat);
        outState.putDouble("myLng", myLng);
        outState.putString("myName", myName);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        myLat = savedInstanceState.getDouble("myLat", 0);
        myLng = savedInstanceState.getDouble("myLng", 0);
        myName = savedInstanceState.getString("myName");
        lat = savedInstanceState.getDouble("lat", 0);
        lng = savedInstanceState.getDouble("lng", 0);
        Log.e(TAG, "On Restore" + "my lat " + myLat + "my lng " + myLng + "my name " + myName);
        final LatLng myLatLng = new LatLng(myLat, myLng);
        final LatLng latLng = new LatLng(lat, lng);

        if (isPhone()) {

            //Phone:
            FragmentManager fm = getSupportFragmentManager();

            //go over all fragments in the activity:
            List<Fragment> allFrags = fm.getFragments();
            for (Fragment fragment : allFrags) {

                if (fragment instanceof SupportMapFragment) {
                    mapFragment = (SupportMapFragment) fragment;
                    // it's the map fragment - restore the marker...

                    mapFragment.getMapAsync(new OnMapReadyCallback() {


                        @Override
                        public void onMapReady(GoogleMap googleMap) {
                            MarkerOptions myMarkerOptions = new MarkerOptions()
                                    .position(myLatLng)
                                    .title(myName)
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));


                            MarkerOptions markerOptions = new MarkerOptions()
                                    .position(latLng)
                                    .title("Your location")
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));

                            myMarker = googleMap.addMarker(myMarkerOptions);
                            marker = googleMap.addMarker(markerOptions);

                        }
                    });

                }
            }
        } else {
            //Tablet:
            FragmentManager fm = getSupportFragmentManager();
            mapFragment = (SupportMapFragment) fm.findFragmentByTag("map");

            mapFragment.getMapAsync(new OnMapReadyCallback() {


                @Override
                public void onMapReady(GoogleMap googleMap) {
                    MarkerOptions myMarkerOptions = new MarkerOptions()
                            .position(myLatLng)
                            .title(myName)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));


                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(latLng)
                            .title("Your location")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));

                    myMarker = googleMap.addMarker(myMarkerOptions);
                    marker = googleMap.addMarker(markerOptions);

                }
            });

        }

    }


    /**
     * Automatic search is done here, along with the GPS listener and receivers
     */
    @Override
    protected void onResume() {
        super.onResume();

        receiver = new PowerConnectionReceiver();

        IntentFilter ifilter = new IntentFilter();
        ifilter.addAction(Intent.ACTION_POWER_CONNECTED);
        ifilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        registerReceiver(receiver, ifilter);


        networkReceiver = new NetworkReceiver();
        registerReceiver(networkReceiver,
                new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));


        Intent intent;
        lm.requestLocationUpdates(provider, 5000, 3, this);
        if (connection == CONNECTED) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            String query = sp.getString("query", "a");
            if (query.equals("a")) {

            } else {
                intent = new Intent(this, Service.class);
                intent.setAction(Service.ACTION_SEARCH_NEARBY);
                intent.putExtra("query", query);
                intent.putExtra("lat", lat);
                intent.putExtra("lng", lng);
                Log.e(TAG, "lat" + lat + "lng" + lng);
                this.startService(intent);

            }
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
        unregisterReceiver(networkReceiver);
        lm.removeUpdates(this);
    }


    /**
     * Class that checks if there is a network connection
     */
    public class NetworkReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager cm =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting();
            if (isConnected) {
                connection = CONNECTED;
                Intent networkIntent = new Intent("disconnected");
                networkIntent.putExtra("connection", connection);
                LocalBroadcastManager.getInstance(context).sendBroadcast(networkIntent);
                Log.e(TAG, "I sent a broadcast with connection " + connection);
            } else {
                connection = NOT_CONNECTED;
                Toast.makeText(MainActivity.this, "There is no internet connection", Toast.LENGTH_SHORT).show();

                Intent networkIntent = new Intent("disconnected");
                networkIntent.putExtra("connection", connection);
                LocalBroadcastManager.getInstance(context).sendBroadcast(networkIntent);
                Log.e(TAG, "I sent a broadcast with connection " + connection);
            }

        }
    }


    public class PowerConnectionReceiver extends BroadcastReceiver {

        public PowerConnectionReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(Intent.ACTION_POWER_CONNECTED)) {
                Toast.makeText(context, "The device is charging", Toast.LENGTH_SHORT).show();
            } else {
                intent.getAction().equals(Intent.ACTION_POWER_DISCONNECTED);
                Toast.makeText(context, "The device is not charging", Toast.LENGTH_SHORT).show();
            }
        }


    }

    private BroadcastReceiver ServiceDoneReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int done = intent.getIntExtra("Done", 0);
            Log.e(TAG, "done = " + done);

            if (done == SERVICE_DONE) {


            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(ServiceDoneReceiver);

    }
}


