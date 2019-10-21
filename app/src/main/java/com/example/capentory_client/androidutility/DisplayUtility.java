package com.example.capentory_client.androidutility;

import android.app.Activity;
import android.content.Context;

import com.example.capentory_client.R;
import com.example.capentory_client.ui.MainActivity;
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
}
