package com.example.capentory_client.ui;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import com.example.capentory_client.R;


public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }


}
