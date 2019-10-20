package com.example.capentory_client.repos;

import android.content.Context;

import androidx.annotation.Nullable;

import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;

import org.json.JSONObject;

class RobustJsonObjectRequestExecutioner {
    RobustJsonObjectRequest robustJsonObjectRequest;

    private static final int MAX_RETRIES = 8;
    private int retriesCounter = 0;
    private Context context;

    public RobustJsonObjectRequestExecutioner(Context context, int method, String url, @Nullable JSONObject jsonRequest, ResponseHandler responseHandler) {
        this.robustJsonObjectRequest = new RobustJsonObjectRequest(context, method, url,
                jsonRequest,
                payload -> handleSuccessfulResponse(payload, responseHandler), error -> handleRetry(error, responseHandler));
        this.context = context;
    }

    private void handleSuccessfulResponse(JSONObject payload, ResponseHandler responseHandler) {
        responseHandler.handleSuccess(payload);
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
     * @param error           error hat was thrown on last attempt
     * @param responseHandler
     */
    private void handleRetry(VolleyError error, ResponseHandler responseHandler) {
        if (retriesCounter < MAX_RETRIES) {
            if (error instanceof TimeoutError) {
                responseHandler.handleError(error);
                resetRetry();
                return;
            }
            launchRequest();
            retriesCounter++;
        } else {
            responseHandler.handleError(error);
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