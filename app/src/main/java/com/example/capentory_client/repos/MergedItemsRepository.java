package com.example.capentory_client.repos;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Base64;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.capentory_client.models.MergedItem;
import com.example.capentory_client.viewmodels.customlivedata.StatusAwareLiveData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MergedItemsRepository {
    private StatusAwareLiveData<List<MergedItem>> mergedItemsLiveData = new StatusAwareLiveData<>();
    private Context context;


    @Inject
    public MergedItemsRepository(Context context) {
        this.context = context;
    }


    // Pretend to get data from a webservice or online source
    public StatusAwareLiveData<List<MergedItem>> getMergedItems(String currentRoomString) {
        mergedItemsLiveData.postFetching();
        setItems(currentRoomString);
        return mergedItemsLiveData;
    }

    public void setItems(String currentRoomString) {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        String server_ip = sharedPreferences.getString("server_ip", "capentory.hostname") + ":";
        String server_port = sharedPreferences.getString("server_port", "80");

        String url = "http://" + server_ip + server_port + "/api/actualroom/" + currentRoomString + "/?format=json";


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, payload -> {
                    try {
                        JSONArray allItems = payload.optJSONArray("all_items");
                        List<MergedItem> mergedItems = new ArrayList<>();

                        for (int i = 0; i < allItems.length(); i++) {
                            JSONObject jsonItem = allItems.getJSONObject(i);
                            mergedItems.add(new MergedItem(currentRoomString, jsonItem));
                        }

                        mergedItemsLiveData.postSuccess(mergedItems);
                    } catch (JSONException error) {
                        mergedItemsLiveData.postError(error);
                    }
                }, error -> mergedItemsLiveData.postError(error)) {
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
    }

}
