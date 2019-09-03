package com.example.capentory_client.repos;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

public abstract class Repository {
    protected Context context;

    public Repository(Context context) {
        this.context = context;
    }

    protected String getUrl(boolean addJsonFormatInUrl, String... path) {
        Uri.Builder urlBuilder = new Uri.Builder().scheme("http")
                .encodedAuthority(getSocket())
                .appendPath("api");

        for (String s : path) {
            urlBuilder.appendPath(s);
        }

        if (addJsonFormatInUrl) {
            return urlBuilder.appendQueryParameter("format", "json").build().toString();
        } else return urlBuilder.build().toString();
    }


    private String getSocket() {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        String ip = sharedPreferences.getString("server_ip", "capentory.hostname");
        String port = sharedPreferences.getString("server_port", "80");
        if (port != null && !port.isEmpty()) {
            return ip + ":" + port;
        }

        return ip;
    }


}
