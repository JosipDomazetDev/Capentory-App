package com.example.capentory_client.repos;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.capentory_client.repos.customrequest.NetworkErrorHandler;
import com.example.capentory_client.repos.customrequest.NetworkSuccessHandler;
import com.example.capentory_client.repos.customrequest.RobustJsonObjectRequestExecutioner;
import com.example.capentory_client.viewmodels.customlivedata.StatusAwareLiveData;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class NetworkRepository<L> {
    protected Context context;
    protected StatusAwareLiveData<L> mainContentRepoData = new StatusAwareLiveData<>();
    protected Map<String, RobustJsonObjectRequestExecutioner> requests = new HashMap<>();
    private static final String MAIN_REQUEST_KEY = "main_request";


    NetworkRepository(Context context) {
        this.context = context;
    }


    public void addRequest(String key, int method, String url, NetworkSuccessHandler successHandler, NetworkErrorHandler networkErrorHandler) {
        requests.put(key, new RobustJsonObjectRequestExecutioner(context, method, url, null, successHandler, networkErrorHandler)
        );
    }


    public void addRequest(String key, int method, String url, NetworkSuccessHandler successHandler, StatusAwareLiveData specificLiveData) {
        requests.put(key, new RobustJsonObjectRequestExecutioner(context, method, url, null, successHandler, error -> handleErrorResponse(error, specificLiveData)
        ));
    }


    public void addRequestWithContent(String key, int method, String url, JSONObject jsonRequest, NetworkSuccessHandler successHandler, StatusAwareLiveData specificLiveData) {
        requests.put(key, new RobustJsonObjectRequestExecutioner(context, method, url, jsonRequest, successHandler, error -> handleErrorResponse(error, specificLiveData)
        ));
    }


    public void addMainRequest(int method, String url) {
        addRequest(MAIN_REQUEST_KEY, method, url, this::handleMainSuccessfulResponse, mainContentRepoData);
    }


    protected void addMainRequestWithContent(int method, String url, Map<String, String> toSend, boolean authenticate) {
        addRequestWithContent(MAIN_REQUEST_KEY, method, url, new JSONObject(toSend), this::handleMainSuccessfulResponse, mainContentRepoData);
        Objects.requireNonNull(requests.get(MAIN_REQUEST_KEY)).toggleAuthenticate(authenticate);
    }

    public void launchMainRequest() {
        launchRequestFromKey(MAIN_REQUEST_KEY, mainContentRepoData);
    }


    public void launchRequestFromKey(String key, StatusAwareLiveData specificLiveData) {
        specificLiveData.postFetching();
        Objects.requireNonNull(requests.get(key)).launchRequest();
    }


    /**
     * Handle a successful response from the network
     * Override: what to do with the payload
     *
     * @param payload response as JSONObject from the server
     */
    protected abstract void handleMainSuccessfulResponse(JSONObject payload);


    /**
     * Inform liveData about error
     *
     * @param error that occurred
     */
    protected void handleErrorResponse(Exception error, StatusAwareLiveData specificLiveData) {
        specificLiveData.postError(error);
    }


    /**
     * Override this method to handle how data is LiveData is fetched and passed to other classes (e.g. ViewModels), Request needs to be launched with launchRequest()
     *
     * @param args optional params
     * @return LiveData
     */
    public abstract StatusAwareLiveData<L> fetchMainData(String... args);


    /**
     * Get an url
     *
     * @param context            used to retrieve ip and port from settings
     * @param path               specifies paths
     * @return the Url
     */
    protected static String getUrl(Context context, boolean combinedPath, String... path) {
        Uri.Builder urlBuilder = new Uri.Builder().scheme("http")
                .encodedAuthority(getSocket(context));

        if (combinedPath) {
            urlBuilder.encodedPath(path[0]);
            for (int i = 1; i < path.length; i++) {
                urlBuilder.appendPath(path[i]);
            }
        } else for (String s : path) {
            urlBuilder.appendPath(s);
        }

        return urlBuilder.appendQueryParameter("format", "json").build().toString();
    }

    /**
     * Get the socket from the settings
     *
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
