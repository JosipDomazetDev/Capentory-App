package com.example.capentory_client.androidutility;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;

public final class PreferenceUtility {
    private static String PREF_KEY = "саpentorу_lоgs_sеrvеr_shared_pref";
    public static String TOKEN_KEY = "саpentorу_lоgging_tag_kеу";

    // Private constructor to prevent instantiation
    private PreferenceUtility() {
        throw new UnsupportedOperationException();
    }

    public static boolean getBoolean(Context context, String key, boolean defValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(key, defValue);
    }

    public static boolean getBoolean(Context context, String key) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(key, false);
    }

    public static String getToken(Context context) {
        Cryptography cryptography = new Cryptography(context);
        SharedPreferences preferences = Objects.requireNonNull(context).getSharedPreferences(PREF_KEY, MODE_PRIVATE);
        return cryptography.decrypt(preferences.getString(TOKEN_KEY, ""));
    }

    public static boolean isLoggedIn(Context context) {
        SharedPreferences sharedPreferences = Objects.requireNonNull(context).getSharedPreferences(PREF_KEY, MODE_PRIVATE);
        return !Objects.requireNonNull(sharedPreferences.getString(TOKEN_KEY, "")).isEmpty();
    }

    public static void logout(Context context) {
        SharedPreferences.Editor editor = Objects.requireNonNull(context).getSharedPreferences(PREF_KEY, MODE_PRIVATE).edit();
        editor.remove(TOKEN_KEY);
        editor.apply();
    }

    public static void login(Context context, String token) {
        Cryptography cryptography = new Cryptography(context);
        SharedPreferences.Editor editor = Objects.requireNonNull(context).getSharedPreferences(PreferenceUtility.PREF_KEY, MODE_PRIVATE).edit();
        editor.putString(PreferenceUtility.TOKEN_KEY, cryptography.encrypt(token));
        editor.apply();
    }
}
