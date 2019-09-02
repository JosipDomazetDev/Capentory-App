package com.example.capentory_client.repos;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Base64;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.capentory_client.models.MergedItemField;
import com.example.capentory_client.viewmodels.customlivedata.StatusAwareLiveData;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FormRepository {
    private StatusAwareLiveData<List<MergedItemField>> mergedItemFieldsLiveData = new StatusAwareLiveData<>();
    private Context context;

    @Inject
    public FormRepository(Context context) {
        this.context = context;
    }


    public StatusAwareLiveData<List<MergedItemField>> getForm() {
        setForm();
        return mergedItemFieldsLiveData;
    }

    private void setForm() {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        String server_ip = sharedPreferences.getString("server_ip", "capentory.hostname") + ":";
        String server_port = sharedPreferences.getString("server_port", "80");
        String url = "http://" + server_ip + server_port + "/api/actualitem/";


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.OPTIONS, url, null, payload -> {
                    try {
                        payload = payload.getJSONObject("actions").getJSONObject("POST");
                        List<MergedItemField> mergedItemFields = new ArrayList<>();
                        Iterator<String> iterator = payload.keys();

                        while (iterator.hasNext()) {
                            String key = iterator.next();
                            mergedItemFields.add(new MergedItemField(key, payload.getJSONObject(key)));
                        }

                        mergedItemFieldsLiveData.postSuccess(mergedItemFields);
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

        MySingleton.getInstance(context).
                addToRequestQueue(jsonObjectRequest);


        mergedItemFieldsLiveData.postFetching();
    }
}

