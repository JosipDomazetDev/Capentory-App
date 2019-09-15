package com.example.capentory_client.repos;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Base64;

import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.capentory_client.viewmodels.customlivedata.StatusAwareLiveData;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public abstract class JsonRepository<L> {
    protected Context context;
    private static final int MAX_RETRIES = 8;
    private int retriesCounter = 0;
    private JsonObjectRequest jsonObjectRequest;
    protected StatusAwareLiveData<L> statusAwareRepoLiveData = new StatusAwareLiveData<>();

    JsonRepository(Context context) {
        this.context = context;
    }

    /**
     * Handle a successful response from the network and reset the retry counter
     *
     * @param payload response as JSONObject from the server
     */
    private void handleSuccessfulResponse_(JSONObject payload) {
        handleSuccessfulResponse(payload);
        resetRetry();
    }

    /**
     * Handle a successful response from the network
     * Override: what to do with the payload
     *
     * @param payload response as JSONObject from the server
     */
    protected abstract void handleSuccessfulResponse(JSONObject payload);


    /**
     * If the requests fails try again
     *
     * @param error error hat was thrown on last attempt
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

    /**
     * Inform liveData about error
     * @param error that occurred
     */
    private void handleErrorResponse(Exception error) {
        statusAwareRepoLiveData.postError(error);
    }

    /**
     * Set the retry counter to zero
     */
    private void resetRetry() {
        retriesCounter = 0;
    }


    /**
     * Create a  launchable JsonObjectRequest that will fetch data from the specified url
     *
     * @param method Specify the type of the request i.e. GET, POST ...
     * @param url    Specify the url
     * @return JsonObjectRequest
     */
    protected void initRequest(int method, String url) {
        jsonObjectRequest = new JsonObjectRequest
                (method, url, null, this::handleSuccessfulResponse_, this::handleRetry) {
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
    /**
     * Launch/Starts the previously initialized network request
     */
    protected void launchRequest() {
        if (jsonObjectRequest == null)
            throw new IllegalArgumentException("Cannot launch empty/null request! Call initRequest to initialize it.");

        statusAwareRepoLiveData.postFetching();
        NetworkSingleton.getInstance(context).
                addToRequestQueue(jsonObjectRequest);
    }

    /**
     * Override this method to handle how data is LiveData is fetched and passed to other classes (e.g. ViewModels), Request needs to be launched with launchRequest()
     *
     * @param args optional params
     * @return LiveData
     */
    public abstract StatusAwareLiveData<L> fetchData(String... args);


    /**
     * Get an url
     * @param context used to retrieve ip and port from settings
     * @param addJsonFormatInUrl whether url should explicitly query for json
     * @param path specifies paths
     * @return the Url
     */
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

    /**
     * Get the socket from the settings
     * @param context used to retrieve ip and port from settings
     * @return the socket as String in the form of IP:Port
     */
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
