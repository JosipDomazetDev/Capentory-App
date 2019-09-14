package com.example.capentory_client.repos;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Base64;

import androidx.annotation.NonNull;

import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.capentory_client.viewmodels.customlivedata.StatusAwareLiveData;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public abstract class Repository<L> {
    protected Context context;
    private static final int MAX_RETRIES = 8;
    private int retriesCounter = 0;
    private JsonObjectRequest jsonObjectRequest;
    protected StatusAwareLiveData<L> statusAwareRepoLiveData;

    public Repository(Context context) {
        this.context = context;
    }

    protected void initRequest(int method, String url) {
        jsonObjectRequest = getJsonObjectRequest(method, url);
    }

    /**
     * Create a  launchable JsonObjectRequest that will fetch data from the specified url
     * @param method Specify the type of the request i.e. GET, POST ...
     * @param url Specify the url
     * @return JsonObjectRequest
     */
    @NonNull
    private JsonObjectRequest getJsonObjectRequest(int method, String url) {
        return new JsonObjectRequest
                (method, url, null, this::handleSuccessfulNetworkResponse_, this::handleRetry) {
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


    /*
     Handle a successful response from the network
     */
    private void handleSuccessfulNetworkResponse_(JSONObject jsonObject) {
        handleSuccessfulNetworkResponse(jsonObject);
        resetRetry();
    }

    protected abstract void handleSuccessfulNetworkResponse(JSONObject payload);



     /*
     Handle a failure from the network
     */
    private void handleRetry(VolleyError error) {
        if (retriesCounter < MAX_RETRIES) {
            if (error instanceof TimeoutError) {
                handleErrorResponse(error);
                resetRetry();
                return;
            }
            launchRequest();
            retriesCounter++;
        } else {
            handleErrorResponse(error);
            resetRetry();
        }
    }

    private void handleErrorResponse(Exception error) {
        statusAwareRepoLiveData.postError(error);
    }

    private void resetRetry() {
        retriesCounter = 0;
    }


    /**
     * Try to fetch the data
     */
    protected void fetchData() {
        statusAwareRepoLiveData.postFetching();
        launchRequest();
    }

    /**
     * Launch/Starts the previously initialized network request
     */
    private void launchRequest() {
        NetworkSingleton.getInstance(context).
                addToRequestQueue(jsonObjectRequest);
    }

    /**
     * Override this method to handle how data is LiveData is passed to other classes (e.g. ViewModels)
     * @param args optional params
     * @return LiveData
     */
    public abstract StatusAwareLiveData<L> getData(String... args);


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
