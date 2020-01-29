package com.capentory.capentory_client.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.preference.PreferenceFragmentCompat;

import com.capentory.capentory_client.R;


public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String SERVER_IP_KEY = "server_ip";
    public static final String SERVER_PORT_KEY = "server_port";
    public static final String TRUST_ALL_CERTICATES_KEY = "trust_all_certificates";
    public static final String LIGHTNING_KEY = "switch_lightning";
    public static final String ENFORCE_ZEBRA_KEY = "switch_enforce_zebra";
    public static final String TEXT_MODE_KEY = "text_filter_mode";
    public static final String BARCODE_FORMATS_KEY = "barcode_formats_key";
    public static final String SHAKE_SENSITIVITY_KEY = "shake_sensitivity";
    public static final String COMPRESS_RATE_KEY = "compress_rate";


    @Override

    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        PreferenceManager.setDefaultValues(getContext(), R.xml.preferences, false);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        prefs.registerOnSharedPreferenceChangeListener(this);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.e("XXXXXX","YYYYYYY");
        if (key.equals(TRUST_ALL_CERTICATES_KEY)) {
            if (sharedPreferences.getBoolean(key, false)) {
                MainActivity.allowAllSSCertificates();
            } else MainActivity.disallowAllSSCertificates();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        prefs.unregisterOnSharedPreferenceChangeListener(this);
    }
}
