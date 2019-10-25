package com.example.capentory_client.repos;

import android.content.Context;

import com.android.volley.Request;
import com.example.capentory_client.viewmodels.customlivedata.StatusAwareLiveData;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

public class AuthRepository extends JsonRepository<String> {
    protected StatusAwareLiveData<Boolean> logoutSuccessful = new StatusAwareLiveData<>();
    private final String LOGOUT_REQUEST_KEY = "logout_request";

    @Inject
    public AuthRepository(Context context) {
        super(context);
    }


    @Override
    public StatusAwareLiveData<String> fetchMainData(String... args) {

        if (args.length != 2) {
            throw new IllegalArgumentException("Passwort und Benutzer sind zu spezifizieren");
        }

        Map<String, String> postParam = new HashMap<>();
        postParam.put("username", args[0]);
        postParam.put("password", args[1]);

        addMainRequest(Request.Method.POST, getUrl(context, false, "api-token-auth/"), postParam, false);
        launchMainRequest();
        return mainContentRepoData;
    }


    @Override
    protected void handleMainSuccessfulResponse(JSONObject payload) {
        try {
            mainContentRepoData.postSuccess(payload.getString("token"));
        } catch (JSONException error) {
            mainContentRepoData.postError(error);
        }
    }


    public StatusAwareLiveData<Boolean> logout() {
        addRequest(LOGOUT_REQUEST_KEY, Request.Method.POST, getUrl(context, false, "api-token-clear/"),
                payload -> logoutSuccessful.postSuccess(true));
        logoutSuccessful.postFetching();
        launchRequestFromKey(LOGOUT_REQUEST_KEY);

        return logoutSuccessful;
    }
}
