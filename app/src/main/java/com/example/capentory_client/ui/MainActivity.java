package com.example.capentory_client.ui;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.capentory_client.R;
import com.example.capentory_client.androidutility.DisplayUtility;
import com.example.capentory_client.androidutility.PreferenceUtility;
import com.example.capentory_client.models.SerializerEntry;
import com.example.capentory_client.models.Stocktaking;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONException;

import dagger.android.support.DaggerAppCompatActivity;

public class MainActivity extends DaggerAppCompatActivity {
    //TODO: Update this for every fragment
    private static final int[] TOP_LEVEL_DESTINATIONS = new int[]{
            R.id.settingsFragment, R.id.homeScreenFragment, R.id.stocktakingFragment, R.id.roomFragment, R.id.itemsFragment, R.id.itemDetailFragment};

    private static Stocktaking stocktaking;
    private static SerializerEntry serializer;

    public static Stocktaking getStocktaking() {
        if (stocktaking == null)
            throw new IllegalArgumentException("Sie müssen eine Inventur anlegen!");
        return stocktaking;
    }

    public static void setStocktaking(Stocktaking stocktaking) {
        MainActivity.stocktaking = stocktaking;
    }

    public static void setSerializer(SerializerEntry selectedSerializer) {
        serializer = selectedSerializer;
    }

    public static SerializerEntry getSerializer() {
        if (serializer == null)
            throw new IllegalArgumentException("Sie müssen eine Inventur anlegen und eine Datenbank auswählen!");
        return serializer;
    }

    public static void clearInventory() {
        MainActivity.setSerializer(null);
        MainActivity.setStocktaking(null);
    }


    protected DrawerLayout drawer;
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupNavigation();


        if (PreferenceUtility.isLoggedIn(this)) {
            DisplayUtility.displayLoggedInMenu(this);
        } else {
            DisplayUtility.displayLoggedOutMenu(this);
        }

    }

    private void setupNavigation() {
        drawer = findViewById(R.id.drawer_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        appBarConfiguration = new AppBarConfiguration.Builder(TOP_LEVEL_DESTINATIONS)
                .setDrawerLayout(drawer)
                .build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        NavigationView navView = findViewById(R.id.nav_view);
        NavigationUI.setupWithNavController(navView, navController);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.onNavDestinationSelected(item, navController)
                || super.onOptionsItemSelected(item);
    }
}
