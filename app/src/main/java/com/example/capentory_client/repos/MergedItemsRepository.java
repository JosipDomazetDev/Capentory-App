package com.example.capentory_client.repos;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;

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
public class MergedItemsRepository extends Repository {
    private StatusAwareLiveData<List<MergedItem>> mergedItemsLiveData = new StatusAwareLiveData<>();


    @Inject
    public MergedItemsRepository(Context context) {
        super(context);
    }


    public StatusAwareLiveData<List<MergedItem>> getMergedItems(String currentRoomString) {
        setItems(currentRoomString);
        return mergedItemsLiveData;
    }

    public void setItems(String currentRoomString) {
        mergedItemsLiveData.postFetching();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, getUrl(true, "actualroom", currentRoomString), null, payload -> {
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

        NetworkSingleton.getInstance(context).
                addToRequestQueue(jsonObjectRequest);
    }

}
