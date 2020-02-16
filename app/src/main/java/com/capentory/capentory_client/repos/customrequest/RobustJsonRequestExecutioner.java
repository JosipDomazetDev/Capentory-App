package com.capentory.capentory_client.repos.customrequest;

import android.content.Context;

import androidx.annotation.Nullable;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.TimeoutError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RobustJsonRequestExecutioner {
    // eight minute timeout just in case we have a really really bad connection e.g. a really deep basement
    private static final int DEFAULT_TIMEOUT_MS = 60000 * 8;
    private RobustRequest robustJsonObjectRequest;

    private static final int MAX_RETRIES = 8;
    private int retriesCounter = 0;
    private Context context;
    private NetworkSuccessHandler successHandler;
    private NetworkErrorHandler errorHandler;
    private Response.Listener<String> successListener;
    private Response.ErrorListener errorListener;

    public RobustJsonRequestExecutioner(Context context, int method, String url, @Nullable String requestBody, NetworkSuccessHandler successHandler, NetworkErrorHandler errorHandler) {
        this.successHandler = successHandler;
        this.errorHandler = errorHandler;

        successListener = response -> handleSuccessfulResponse(response, this.successHandler, this.errorHandler);
        errorListener = error -> handleRetry(error, errorHandler);

        RobustRequest request = new RobustRequest(context, method, url,
                requestBody,
                successListener,
                errorListener);

        request.setRetryPolicy(new DefaultRetryPolicy(
                DEFAULT_TIMEOUT_MS,
                0,
                0));

        this.robustJsonObjectRequest = request;
        this.context = context;
    }


    public void toggleAuthenticate(boolean authenticate) {
        if (authenticate) {
            robustJsonObjectRequest.enableAuthentication();
        } else robustJsonObjectRequest.disableAuthentication();
    }

    private void handleSuccessfulResponse(String payload, NetworkSuccessHandler successHandler, NetworkErrorHandler errorHandler) {
        if (isValidJSON(payload, errorHandler)) {
            successHandler.handleSuccess(payload);
            resetRetry();
        }
    }

    private boolean isValidJSON(String payload, NetworkErrorHandler errorHandler) {
        if (payload == null) return true;

        Exception exception1 = null;
        Exception exception2 = null;
        try {
            new JSONObject(payload);
        } catch (JSONException e) {
            exception1 = e;
        }
        try {
            new JSONArray(payload);
        } catch (JSONException e) {
            exception2 = e;
        }


        // Means he failed both
        if (exception1 != null && exception2 != null) {
            handleRetry(exception1, errorHandler);
            return false;
        }

        return true;
    }

    /**
     * Launch/Starts the previously initialized network request
     */
    public void launchRequest() {
        NetworkSingleton.getInstance(context).
                addToRequestQueue(robustJsonObjectRequest);
    }

    /**
     * If the requests fails try again
     *
     * @param error        error hat was thrown on last attempt
     * @param errorHandler
     */
    private void handleRetry(Exception error, NetworkErrorHandler errorHandler) {
        if (retriesCounter < MAX_RETRIES) {
            if (error instanceof TimeoutError) {
                errorHandler.handleError(error);
                resetRetry();
                return;
            }
            launchRequest();
            retriesCounter++;
        } else {
            errorHandler.handleError(error);
            resetRetry();
        }
    }

    /**
     * Set the retry counter to zero
     */
    private void resetRetry() {
        retriesCounter = 0;
    }


    public void clearRequest() {
        context = null;
        successHandler = null;
        successListener = null;
        errorHandler = null;
        errorListener = null;
        robustJsonObjectRequest.cancel();
        robustJsonObjectRequest = null;
    }
}