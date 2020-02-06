package com.capentory.capentory_client.ui;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.capentory.capentory_client.R;
import com.capentory.capentory_client.androidutility.UserUtility;
import com.capentory.capentory_client.androidutility.PreferenceUtility;
import com.capentory.capentory_client.models.SerializerEntry;
import com.capentory.capentory_client.models.Stocktaking;
import com.google.android.material.navigation.NavigationView;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import dagger.android.support.DaggerAppCompatActivity;

public class MainActivity extends DaggerAppCompatActivity {
    //TODO: Update this for every fragment
    private static final int[] TOP_LEVEL_DESTINATIONS = new int[]{
            R.id.settingsFragment, R.id.homeScreenFragment, R.id.stocktakingFragment, R.id.roomFragment,
            R.id.itemsFragment,  R.id.viewPagerFragment , R.id.itemDetailFragment};

    private static Stocktaking stocktaking;
    private static SerializerEntry serializer;

    // We are not allowed to store a static context
    public static Stocktaking getStocktaking(Context context) {
        if (stocktaking == null)
            throw new IllegalArgumentException(context.getString(R.string.error_stocktaking));
        return stocktaking;
    }

    public static void setStocktaking(Stocktaking stocktaking) {
        MainActivity.stocktaking = stocktaking;
    }

    public static void setSerializer(SerializerEntry selectedSerializer) {
        serializer = selectedSerializer;
    }

    // We are not allowed to store a static context
    public static SerializerEntry getSerializer(Context context) {
        if (serializer == null)
            throw new IllegalArgumentException(context.getString(R.string.error_serializer));
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
            UserUtility.displayLoggedInMenu(this);
        } else {
            UserUtility.displayLoggedOutMenu(this);
        }

        if (PreferenceUtility.getBoolean(this, SettingsFragment.TRUST_ALL_CERTICATES_KEY)) {
            allowAllSSCertificates();
        } else disallowAllSSCertificates();
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


    public static void allowAllSSCertificates() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }};

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((arg0, arg1) -> true);
        } catch (Exception ignored) {
        }
    }

    public static void disallowAllSSCertificates() {
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, null, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(null);
        } catch (Exception ignored) {
        }
    }
}
