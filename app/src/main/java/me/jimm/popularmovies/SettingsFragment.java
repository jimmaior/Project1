package me.jimm.popularmovies;


import android.content.SharedPreferences;
import android.os.Bundle;

import android.preference.DialogPreference;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragmentCompat {
    
    private static final String TAG = SettingsFragment.class.getSimpleName();

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        Log.d(TAG, "onCreatePreferences()");
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        super.onDestroy();
    }

}
