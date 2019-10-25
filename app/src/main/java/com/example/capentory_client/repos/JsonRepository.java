package com.example.capentory_client.repos;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;

import com.example.capentory_client.viewmodels.customlivedata.StatusAwareLiveData;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class JsonRepository<L> {
    protected Context context;
    protected StatusAwareLiveData<L> mainContentRepoData = new StatusAwareLiveData<>();
    protected Map<String, RobustJsonObjectRequestExecutioner> requests = new HashMap<>();
    private static final String MAIN_REQUEST_KEY = "main_request";


    JsonRepository(Context context) {
        this.context = context;
    }


    public void addRequest(String key, int method, String url, NetworkSuccessHandler successHandler) {
        requests.put(key, new RobustJsonObjectRequestExecutioner(context, method, url, null, successHandler, this::handleErrorResponse
        ));
    }


    public void addMainRequest(int method, String url) {
        addRequest(MAIN_REQUEST_KEY, method, url, this::handleMainSuccessfulResponse);
    }

    public void launchMainRequest() {
        launchRequestFromKey(MAIN_REQUEST_KEY);
    }


    protected void addMainRequest(int method, String url, Map<String, String> toSend, boolean authenticate) {
        requests.put(MAIN_REQUEST_KEY,
                new RobustJsonObjectRequestExecutioner(context, method, url, new JSONObject(toSend), this::handleMainSuccessfulResponse, this::handleErrorResponse));
        Objects.requireNonNull(requests.get(MAIN_REQUEST_KEY)).shouldAuthenticate(authenticate);
    }


    public void launchRequestFromKey(String key) {
        Objects.requireNonNull(requests.get(key)).launchRequest();
        mainContentRepoData.postFetching();
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
    private void handleErrorResponse(Exception error) {
        mainContentRepoData.postError(error);
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
     * @param addJsonFormatInUrl whether url should explicitly query for json
     * @param path               specifies paths
     * @return the Url
     */
    protected static String getUrl(Context context, boolean addJsonFormatInUrl, String... path) {
        Uri.Builder urlBuilder = new Uri.Builder().scheme("http")
                .encodedAuthority(getSocket(context));

        for (String s : path) {
            urlBuilder.appendPath(s);
        }

        if (addJsonFormatInUrl) {
            return urlBuilder.appendQueryParameter("format", "json").build().toString();
        } else return urlBuilder.build().toString();
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
