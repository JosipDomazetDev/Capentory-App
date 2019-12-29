package com.capentory.capentory_client.androidutility;

import android.app.Activity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.capentory.capentory_client.R;
import com.google.android.material.navigation.NavigationView;

import java.util.Objects;

public final class DisplayUtility {

    // Private constructor to prevent instantiation
    private DisplayUtility() {
        throw new UnsupportedOperationException();
    }


    public static void displayLoggedInMenu(Activity activity) {
        NavigationView navigationView = Objects.requireNonNull(activity).findViewById(R.id.nav_view);
        navigationView.getMenu().clear();
        navigationView.inflateMenu(R.menu.activity_main_drawer_logged_in);
    }

    public static void displayLoggedOutMenu(Activity activity) {
        NavigationView navigationView = Objects.requireNonNull(activity).findViewById(R.id.nav_view);
        navigationView.getMenu().clear();
        navigationView.inflateMenu(R.menu.activity_main_drawer_logged_out);
    }


    public static void hideKeyboard(Activity activity) {
        //https://stackoverflow.com/questions/1109022/close-hide-the-android-soft-keyboard
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
