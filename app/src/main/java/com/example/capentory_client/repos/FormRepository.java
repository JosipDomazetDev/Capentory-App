package com.example.capentory_client.repos;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.capentory_client.models.MergedItemField;
import com.example.capentory_client.viewmodels.customlivedata.StatusAwareLiveData;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FormRepository extends Repository {
    private StatusAwareLiveData<Map<String, MergedItemField>> mergedItemFieldsLiveData = new StatusAwareLiveData<>();

    @Inject
    public FormRepository(Context context) {
        super(context);
    }


    public StatusAwareLiveData<Map<String, MergedItemField>> getForm() {

        if(mergedItemFieldsLiveData.getValue() == null) {
            setForm();
        }
        return mergedItemFieldsLiveData;
    }

    private void setForm() {
        mergedItemFieldsLiveData.postFetching();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.OPTIONS, getUrl(false, "actualitem/"), null, payload -> {
                    try {
                        Log.e("FETCHING","frefrfddde");
                        payload = payload.getJSONObject("actions").getJSONObject("POST");
                        Map<String, MergedItemField> mergedItemFieldsSet = new HashMap<>();
                        Iterator<String> iterator = payload.keys();

                        while (iterator.hasNext()) {
                            String key = iterator.next();
                            mergedItemFieldsSet.put(key, new MergedItemField(key, payload.getJSONObject(key)));
                        }

                        mergedItemFieldsLiveData.postSuccess(mergedItemFieldsSet);
                    } catch (JSONException error) {
                        mergedItemFieldsLiveData.postError(error);
                    }
                }, error -> {
                    mergedItemFieldsLiveData.postError(error);
                }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                String credentials = "ralph" + ":" + "ralph";
                String auth = "Basic "
                        + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", auth);
                return headers;
            }
        };

        NetworkSingleton.getInstance(context).
                addToRequestQueue(jsonObjectRequest);


        mergedItemFieldsLiveData.postFetching();
    }
}

