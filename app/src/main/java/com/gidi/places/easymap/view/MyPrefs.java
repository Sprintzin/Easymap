package com.gidi.places.easymap.view;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.gidi.places.easymap.R;
import com.gidi.places.easymap.model.PlacesContract;


public class MyPrefs extends PreferenceActivity implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

    ListPreference distance_prefs;
    ListPreference search_prefs;
    Preference preference;
    View.OnClickListener myOnClickListener;
    final static String TAG = "Prefs";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs);

        distance_prefs = (ListPreference) findPreference("distance");
        search_prefs = (ListPreference) findPreference("search");
        preference = findPreference("deleteAll");

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String distance = prefs.getString("distance", "km");
        String search = prefs.getString("search", "Search nearby");


        if(search_prefs.getValue() == null){
            search_prefs.setValueIndex(0); //set to index of your deafult value
        }
        if (distance_prefs.getValue()==null){
            distance_prefs.setValueIndex(1);
        }

        distance_prefs.setSummary(distance);
        search_prefs.setSummary(search);

        distance_prefs.setOnPreferenceChangeListener(this);
        search_prefs.setOnPreferenceChangeListener(this);
        preference.setOnPreferenceClickListener(this);

    }

    @Override
    public boolean onPreferenceChange(Preference preference, final Object newValue) {
        String key = preference.getKey();
        if (key.equals("distance")) {
            preference.setSummary((String) newValue);

            Snackbar snackbar = Snackbar
                    .make(getListView(), "Your choice is " + newValue, Snackbar.LENGTH_LONG)
                    .setAction("UNDO", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (newValue.equals("km")){
                                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MyPrefs.this);
                                SharedPreferences.Editor editor = sp.edit();
                                editor.putString("distance", "miles");
                                editor.commit();
                                Preference etp = findPreference("distance");
                                etp.setSummary("miles");
                            }else if (newValue.equals("miles")){
                                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MyPrefs.this);
                                SharedPreferences.Editor editor = sp.edit();
                                editor.putString("distance", "km");
                                editor.commit();
                                Preference etp =  findPreference("distance");
                                etp.setSummary("km");

                            }
                        }
                    });
            snackbar.setActionTextColor(Color.YELLOW);
            View snackbarView = snackbar.getView();
            snackbarView.setBackgroundColor(Color.DKGRAY);
            TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(Color.WHITE);
            snackbar.show();

        }else if(key.equals("search")){
            preference.setSummary((String) newValue);
            Snackbar snackbar = Snackbar
                    .make(getListView(), "Your choice is " + newValue, Snackbar.LENGTH_LONG)
                    .setAction("UNDO", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (newValue.equals("Search nearby")){
                                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MyPrefs.this);
                                SharedPreferences.Editor editor = sp.edit();
                                editor.putString("search", "Free search");
                                editor.commit();
                                Preference etp = findPreference("search");
                                etp.setSummary("Free search");
                            }else if (newValue.equals("Free search")){
                                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MyPrefs.this);
                                SharedPreferences.Editor editor = sp.edit();
                                editor.putString("search", "Search nearby");
                                editor.commit();
                                Preference etp =  findPreference("search");
                                etp.setSummary("Search nearby");

                            }
                        }
                    });
            snackbar.setActionTextColor(Color.YELLOW);
            View snackbarView = snackbar.getView();
            snackbarView.setBackgroundColor(Color.DKGRAY);
            TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(Color.WHITE);
            snackbar.show();
        }

        return true;

    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        if (key.equals("deleteAll"))
            getContentResolver().delete(PlacesContract.Favorites.FAV_URI, null, null);
        finish();
        return true;
    }
}
