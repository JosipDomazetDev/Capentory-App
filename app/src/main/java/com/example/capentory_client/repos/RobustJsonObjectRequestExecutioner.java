package com.example.capentory_client.repos;

import android.content.Context;

import androidx.annotation.Nullable;

import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;

import org.json.JSONObject;

class RobustJsonObjectRequestExecutioner {
    private RobustJsonObjectRequest robustJsonObjectRequest;

    private static final int MAX_RETRIES = 8;
    private int retriesCounter = 0;
    private Context context;

    public RobustJsonObjectRequestExecutioner(Context context, int method, String url, @Nullable JSONObject jsonRequest, NetworkSuccessHandler successHandler, NetworkErrorHandler errorHandler) {
        this.robustJsonObjectRequest = new RobustJsonObjectRequest(context, method, url,
                jsonRequest,
                payload -> handleSuccessfulResponse(payload, successHandler), error -> handleRetry(error, errorHandler));
        this.context = context;
    }

    public void shouldAuthenticate(boolean authenticate) {
        if (authenticate) {
            robustJsonObjectRequest.enableAuthentication();
        } else robustJsonObjectRequest.disableAuthentication();
    }

    private void handleSuccessfulResponse(JSONObject payload, NetworkSuccessHandler successHandler) {
        successHandler.handleSuccess(payload);
        resetRetry();
    }


    /**
     * Launch/Starts the previously initialized network request
     */
    protected void launchRequest() {
        NetworkSingleton.getInstance(context).
                addToRequestQueue(robustJsonObjectRequest);
    }

    /**
     * If the requests fails try again
     *
     * @param error        error hat was thrown on last attempt
     * @param errorHandler
     */
    private void handleRetry(VolleyError error, NetworkErrorHandler errorHandler) {
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


}