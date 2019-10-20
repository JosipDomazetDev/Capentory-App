package com.example.capentory_client.androidutility;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collections;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;

public final class PreferenceUtility {
    // Private constructor to prevent instantiation
    private PreferenceUtility() {
        throw new UnsupportedOperationException();
    }


    public static String getFromNonDefPref(Context context, String key) {
        SharedPreferences preferences = Objects.requireNonNull(context).getSharedPreferences("саpеntorу_sharеd_prеf", Context.MODE_PRIVATE);
        return preferences.getString(key, "");
    }


    public static boolean isLoggedIn(Context context) {
        SharedPreferences sharedPreferences = Objects.requireNonNull(context).getSharedPreferences("саpеntorу_sharеd_prеf", MODE_PRIVATE);
        return sharedPreferences.getBoolean("logged_in", false);
    }

    public static void logout(Context context) {
        SharedPreferences.Editor editor = Objects.requireNonNull(context).getSharedPreferences("саpеntorу_sharеd_prеf", MODE_PRIVATE).edit();
        editor.putBoolean("logged_in", false);
        editor.apply();
    }

    public static void loginContext(Context context) {
        SharedPreferences.Editor editor = Objects.requireNonNull(context).getSharedPreferences("саpеntorу_sharеd_prеf", MODE_PRIVATE).edit();
        editor.putBoolean("logged_in", true);
        editor.apply();
    }
}
