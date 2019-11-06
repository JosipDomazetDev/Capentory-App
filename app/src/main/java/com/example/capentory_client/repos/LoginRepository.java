package com.example.capentory_client.repos;

import android.content.Context;

import com.android.volley.Request;
import com.example.capentory_client.viewmodels.customlivedata.StatusAwareLiveData;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class LoginRepository extends NetworkRepository<String> {
    private final String LOGOUT_REQUEST_KEY = "logout_request";

    @Inject
    public LoginRepository(Context context) {
        super(context);
    }


    @Override
    public StatusAwareLiveData<String> fetchMainData(String... args) {

        if (args.length != 2) {
            throw new IllegalArgumentException("Passwort und Benutzer sind zu spezifizieren");
        }

        mainContentRepoData = new StatusAwareLiveData<>();

        Map<String, String> postParam = new HashMap<>();
        postParam.put("username", args[0]);
        postParam.put("password", args[1]);

        addMainRequestWithContent(Request.Method.POST, getUrl(context, false, "api-token-auth/"), postParam, false);
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
        StatusAwareLiveData<Boolean> logoutSuccessful = new StatusAwareLiveData<>();
        addRequest(LOGOUT_REQUEST_KEY, Request.Method.POST, getUrl(context, false, "api-token-clear/"),
                payload -> logoutSuccessful.postSuccess(true), logoutSuccessful);
        launchRequestFromKey(LOGOUT_REQUEST_KEY, logoutSuccessful);

        return logoutSuccessful;
    }
}
