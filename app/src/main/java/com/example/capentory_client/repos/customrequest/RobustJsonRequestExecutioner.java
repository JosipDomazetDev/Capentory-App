package com.example.capentory_client.repos.customrequest;

import android.content.Context;

import androidx.annotation.Nullable;

import com.android.volley.TimeoutError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RobustJsonRequestExecutioner {
    private CustomRequest robustJsonObjectRequest;

    private static final int MAX_RETRIES = 8;
    private int retriesCounter = 0;
    private Context context;


    public RobustJsonRequestExecutioner(Context context, int method, String url, @Nullable String requestBody, NetworkSuccessHandler successHandler, NetworkErrorHandler errorHandler) {
        this.robustJsonObjectRequest = new CustomRequest(context, method, url,
                requestBody,
                response -> handleSuccessfulResponse(response, successHandler, errorHandler),
                error -> handleRetry(error, errorHandler));
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


}