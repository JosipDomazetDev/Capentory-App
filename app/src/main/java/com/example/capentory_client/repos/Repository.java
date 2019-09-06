package com.example.capentory_client.repos;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.capentory_client.androidutility.ToastUtility;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public abstract class Repository {
    protected Context context;
    private static final int MAX_RETRIES = 8;
    int retriesCounter = 0;
    private JsonObjectRequest jsonObjectRequest;

    public Repository(Context context) {
        this.context = context;
    }

    protected void initRequest(int method, String url) {
        jsonObjectRequest = getJsonObjectRequest(method, url);
    }

    protected void launchRequest() {
        NetworkSingleton.getInstance(context).
                addToRequestQueue(jsonObjectRequest);
    }

    @NonNull
    private JsonObjectRequest getJsonObjectRequest(int method, String url) {
        return new JsonObjectRequest
                (method, url, null, this::handleNetworkResponse_, this::handleRetry) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                String credentials = "ralph" + ":" + "ralph";
                String auth = "Basic "
                        + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", auth);
                headers.put("Connection", "close");
                return headers;
            }
        };
    }

    private void handleNetworkResponse_(JSONObject jsonObject) {
        handleNetworkResponse(jsonObject);
        resetRetry();
    }

    protected abstract void handleNetworkResponse(JSONObject payload);

    protected abstract void handleErrorResponse(Exception error);

    protected abstract void setData();

    protected void handleRetry(VolleyError error) {
        if (retriesCounter < MAX_RETRIES) {
            if (error instanceof TimeoutError) {
                handleErrorResponse(error);
                resetRetry();
                return;
            }

            Log.e("eee", "Retrying....");
            Log.e("eee", "....");
            launchRequest();
            retriesCounter++;
        } else {
            handleErrorResponse(error);
            resetRetry();
        }
    }

    protected void resetRetry() {
        retriesCounter = 0;
    }

    protected static String getUrl(Context context, boolean addJsonFormatInUrl, String... path) {
        Uri.Builder urlBuilder = new Uri.Builder().scheme("http")
                .encodedAuthority(getSocket(context))
                .appendPath("api");

        for (String s : path) {
            urlBuilder.appendPath(s);
        }

        if (addJsonFormatInUrl) {
            return urlBuilder.appendQueryParameter("format", "json").build().toString();
        } else return urlBuilder.build().toString();
    }

    private static String getSocket(Context context) {
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
