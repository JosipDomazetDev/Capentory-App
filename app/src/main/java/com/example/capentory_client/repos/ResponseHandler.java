package com.example.capentory_client.repos;

import org.json.JSONObject;

public interface ResponseHandler {

    void handleSuccess(JSONObject payload);

    void handleError(Exception error);
}
