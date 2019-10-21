package com.example.capentory_client.repos;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.example.capentory_client.viewmodels.customlivedata.StatusAwareLiveData;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

public class AuthRepository extends JsonRepository<String> {
    protected StatusAwareLiveData<Boolean> logoutSuccessful = new StatusAwareLiveData<>();


    @Inject
    public AuthRepository(Context context) {
        super(context);
    }


    @Override
    public StatusAwareLiveData<String> fetchData(String... args) {
        if (args.length != 2) {
            throw new IllegalArgumentException("Passwort und Benutzer sind zu spezifizieren");
        }

        Map<String, String> postParam = new HashMap<>();
        postParam.put("username", args[0]);
        postParam.put("password", args[1]);

        initPost(getUrl(context, false, "api-token-auth/"), postParam);
        launchRequest();
        return statusAwareRepoLiveData;
    }


    @Override
    protected void handleSuccessfulResponse(JSONObject payload) {
        try {
            statusAwareRepoLiveData.postSuccess(payload.getString("token"));
        } catch (JSONException error) {
            statusAwareRepoLiveData.postError(error);
        }
    }


    public StatusAwareLiveData<Boolean> logout() {
        RobustJsonObjectRequestExecutioner robustJsonObjectRequestExecutioner = new RobustJsonObjectRequestExecutioner(context,
                Request.Method.POST, getUrl(context, false, "api-token-clear/"), null, new ResponseHandler() {
            @Override
            public void handleSuccess(JSONObject payload) {
                logoutSuccessful.postSuccess(true);
            }

            @Override
            public void handleError(Exception error) {
                logoutSuccessful.postError(error);
            }
        });
        logoutSuccessful.postFetching();
        robustJsonObjectRequestExecutioner.launchRequest();
        return logoutSuccessful;
    }
}
